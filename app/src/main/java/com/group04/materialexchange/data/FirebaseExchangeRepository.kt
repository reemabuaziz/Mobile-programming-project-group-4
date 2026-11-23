package com.group04.materialexchange.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.group04.materialexchange.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.firebase.auth.GoogleAuthProvider
import android.content.Context

class FirebaseExchangeRepository(
    context: Context
) : ExchangeRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val mutableData = MutableStateFlow(ExchangeData())
    override val data = mutableData.asStateFlow()
    private val listingSource =
        FirebaseListingDataSource(context)
    private val requestSource =
        FirebaseRequestDataSource()
    private val mutableSyncError =
        MutableStateFlow<String?>(null)

    val syncError = mutableSyncError.asStateFlow()

    private fun publishSession(student: Student) {
        mutableSyncError.value = null

        mutableData.value =
            if (data.value.student?.id == student.id) {
                data.value.copy(student = student)
            } else {
                ExchangeData(student = student)
            }

        listingSource.observe(
            userId = student.id,
            onItems = { items ->
                mutableData.value = data.value.copy(
                    listings = items
                )
            },
            onError = { message ->
                mutableSyncError.value = message
            }
        )

        requestSource.observe(
            userId = student.id,
            onRequests = { requests ->
                mutableData.value = data.value.copy(
                    requests = requests
                )
            },
            onNotices = { notices ->
                mutableData.value = data.value.copy(
                    notices = notices
                )
            },
            onError = { message ->
                mutableSyncError.value = message
            }
        )
    }

    fun close() {
        listingSource.stop()
        requestSource.stop()
    }
    private suspend fun <T> Task<T>.waitForResult(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                continuation.resume(result)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
            addOnCanceledListener {
                continuation.cancel()
            }
        }

    private fun checkEmail(email: String) {
        require(ExchangeRules.validEmail(email)) {
            "Use your IAU university email."
        }
    }

    private fun checkName(name: String) {
        require(name.trim().length in 2..100) {
            "Enter a name between 2 and 100 characters."
        }
    }

    private suspend fun loadOrCreateProfile(
        firebaseUser: FirebaseUser,
        preferredName: String? = null
    ): Student {
        val uid = firebaseUser.uid
        val email = requireNotNull(firebaseUser.email) {
            "The account has no email address."
        }

        checkEmail(email)

        val reference = db.collection("users").document(uid)

        val fallbackName = preferredName
            ?: firebaseUser.displayName
            ?: email.substringBefore("@")

        val student = db.runTransaction { transaction ->
            val snapshot = transaction.get(reference)

            if (snapshot.exists()) {
                Student(
                    id = uid,
                    name = requireNotNull(
                        snapshot.getString("fullName")
                    ) {
                        "The user profile is missing its name."
                    },
                    email = email
                )
            } else {
                val name = fallbackName.trim()
                checkName(name)

                transaction.set(
                    reference,
                    mapOf(
                        "userId" to uid,
                        "fullName" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                )

                Student(
                    id = uid,
                    name = name,
                    email = email
                )
            }
        }.waitForResult()

        return student
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        checkName(cleanName)
        checkEmail(cleanEmail)

        require(password.length >= 8) {
            "Use at least 8 characters for your password."
        }

        val result = auth.createUserWithEmailAndPassword(
            cleanEmail,
            password
        ).waitForResult()

        val firebaseUser = requireNotNull(result.user)

        try {
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()

            firebaseUser.updateProfile(updates).waitForResult()

            val student = loadOrCreateProfile(
                firebaseUser,
                cleanName
            )

            publishSession(student)        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            auth.signOut()
            mutableData.value = ExchangeData()

            throw IllegalStateException(
                "Your account was created, but saving your profile failed. " +
                        "Sign in with the same email and password to retry. " +
                        (e.localizedMessage ?: ""),
                e
            )
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) {
        val cleanEmail = email.trim()
        checkEmail(cleanEmail)

        require(password.isNotBlank()) {
            "Enter your password."
        }

        val result = auth.signInWithEmailAndPassword(
            cleanEmail,
            password
        ).waitForResult()

        val firebaseUser = requireNotNull(result.user)

        try {
            val student = loadOrCreateProfile(firebaseUser)
            publishSession(student)        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            auth.signOut()
            mutableData.value = ExchangeData()
            throw e
        }
    }

    override suspend fun signOut() {
        listingSource.stop()
        requestSource.stop()
        auth.signOut()

        mutableSyncError.value = null
        mutableData.value = ExchangeData()
    }

    override suspend fun updateProfile(name: String) {
        val cleanName = name.trim()
        checkName(cleanName)

        val firebaseUser = requireNotNull(auth.currentUser) {
            "Sign in to continue."
        }

        db.collection("users")
            .document(firebaseUser.uid)
            .update("fullName", cleanName)
            .waitForResult()

        val currentStudent = requireNotNull(data.value.student) {
            "Reload your profile and try again."
        }

        mutableData.value = data.value.copy(
            student = currentStudent.copy(name = cleanName)
        )
    }

    override suspend fun reload() {
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            listingSource.stop()
            requestSource.stop()
            mutableData.value = ExchangeData()
            return
        }

        val student = loadOrCreateProfile(firebaseUser)
        publishSession(student)    }

    override suspend fun publish(
        draft: ListingDraft,
        editId: String?
    ) {
        val student = requireNotNull(data.value.student) {
            "Sign in to continue."
        }

        val item = listingSource.publish(
            student = student,
            draft = draft,
            editId = editId
        )

        mutableData.value = data.value.copy(
            listings = listOf(item) +
                    data.value.listings.filterNot {
                        it.id == item.id
                    }
        )
    }

    override suspend fun requestMaterial(itemId: String) {
        val student = requireNotNull(data.value.student) {
            "Sign in to continue."
        }

        require(auth.currentUser?.uid == student.id) {
            "Your session changed. Sign in again."
        }

        requestSource.requestMaterial(itemId)
    }

    override suspend fun markRead() {
        requestSource.markRead()
    }
    override suspend fun respond(
        requestId: String,
        accept: Boolean
    ) {
        requestSource.respond(requestId, accept)
    }

    override suspend fun complete(requestId: String) {
        requestSource.complete(requestId)
    }
    override suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(
            idToken,
            null
        )

        val result = auth.signInWithCredential(credential)
            .waitForResult()

        val firebaseUser = requireNotNull(result.user)

        try {
            val email = requireNotNull(firebaseUser.email) {
                "The Google account has no email address."
            }

            checkEmail(email)

            require(firebaseUser.isEmailVerified) {
                "Use a verified Google email address."
            }

            val student = loadOrCreateProfile(firebaseUser)
            publishSession(student)        } catch (e: CancellationException) {
            auth.signOut()
            mutableData.value = ExchangeData()
            throw e
        } catch (e: Exception) {
            auth.signOut()
            mutableData.value = ExchangeData()
            throw e
        }
    }

    override suspend fun resetPassword(email: String) {
        val cleanEmail = email.trim()
        checkEmail(cleanEmail)

        auth.sendPasswordResetEmail(cleanEmail)
            .waitForResult()
    }
}
