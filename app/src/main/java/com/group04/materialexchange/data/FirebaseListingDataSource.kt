package com.group04.materialexchange.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.group04.materialexchange.domain.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseListingDataSource(context: Context) {

    private val appContext = context.applicationContext
    private val db = FirebaseFirestore.getInstance()

    private var listener: ListenerRegistration? = null
    private var generation = 0

    private data class PendingListing(
        val id: String,
        var photoPath: String? = null
    )

    private val pending = mutableMapOf<
            Triple<String, String?, ListingDraft>,
            PendingListing
            >()

    private suspend fun <T> Task<T>.awaitListingResult(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener {
                continuation.resume(it)
            }
            addOnFailureListener {
                continuation.resumeWithException(it)
            }
            addOnCanceledListener {
                continuation.cancel()
            }
        }

    fun observe(
        userId: String,
        onItems: (List<MaterialItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()
        val currentGeneration = generation

        listener = db.collection("listings")
            .addSnapshotListener { snapshot, error ->
                if (
                    currentGeneration != generation ||
                    FirebaseAuth.getInstance().currentUser?.uid != userId
                ) {
                    return@addSnapshotListener
                }

                if (error != null) {
                    onError(
                        error.localizedMessage
                            ?: "Could not load materials."
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                try {
                    val items = snapshot.documents
                        .filter {
                            it.getString("status") != "REMOVED"
                        }
                        .sortedByDescending {
                            it.getLong("createdAt") ?: 0L
                        }
                        .map { toItem(it) }

                    onItems(items)
                } catch (e: Exception) {
                    onError(
                        e.localizedMessage
                            ?: "Some material data could not be read."
                    )
                }
            }
    }

    fun stop() {
        generation++
        listener?.remove()
        listener = null
    }

    private fun toItem(doc: DocumentSnapshot): MaterialItem {
        val condition = when (doc.getString("condition")) {
            "NEW" -> "New"
            "LIKE_NEW" -> "Like New"
            "GOOD" -> "Good"
            "FAIR" -> "Used"
            else -> error("Unknown condition for ${doc.id}.")
        }

        val status = when (doc.getString("status")) {
            "AVAILABLE" -> "Available"
            "RESERVED" -> "Reserved"
            "COMPLETED" -> "Completed"
            "REMOVED" -> "Removed"
            else -> error("Unknown status for ${doc.id}.")
        }

        return MaterialItem(
            id = doc.id,
            title = requireNotNull(doc.getString("title")),
            category = requireNotNull(doc.getString("category")),
            condition = condition,
            quantity = requireNotNull(doc.getLong("quantity")).toInt(),
            exchangeType = when (doc.getString("exchangeType")) {
                "FREE" -> "Free"
                "EXCHANGE" -> "Exchange"
                else -> error("Unknown exchange type.")
            },
            pickupArea = requireNotNull(doc.getString("pickupArea")),
            ownerId = requireNotNull(doc.getString("ownerId")),
            ownerName = requireNotNull(doc.getString("ownerName")),
            status = status,
            photo = doc.getString("photoPath").orEmpty(),
            description = doc.getString("description").orEmpty()
        )
    }

    suspend fun publish(
        student: Student,
        draft: ListingDraft,
        editId: String?
    ): MaterialItem {
        ExchangeRules.draftError(draft)?.let {
            error(it)
        }

        require(draft.title.trim().length <= 120) {
            "Keep the title within 120 characters."
        }

        require(draft.description.trim().length <= 2000) {
            "Keep the description within 2000 characters."
        }

        require(
            FirebaseAuth.getInstance().currentUser?.uid == student.id
        ) {
            "Sign in to continue."
        }

        val key = Triple(student.id, editId, draft)

        val operation = pending.getOrPut(key) {
            PendingListing(
                editId ?: db.collection("listings").document().id
            )
        }

        val reference = db.collection("listings")
            .document(operation.id)

        val before = reference.get(Source.SERVER)
            .awaitListingResult()

        if (editId != null) {
            require(before.exists()) {
                "This material no longer exists."
            }
        }

        if (before.exists()) {
            require(
                before.getString("ownerId") == student.id &&
                        before.getString("status") == "AVAILABLE"
            ) {
                "Only your available listings can be edited."
            }
        }

        val photoPath = operation.photoPath ?: when {
            draft.photo == "arduino" ||
                    draft.photo == "filament" -> draft.photo

            draft.photo.startsWith("gs://") -> {
                require(
                    before.exists() &&
                            before.getString("photoPath") == draft.photo
                ) {
                    "Choose a photo from your device."
                }

                draft.photo
            }

            else -> ListingPhotoStorage.upload(
                context = appContext,
                photo = draft.photo,
                listingId = operation.id
            )
        }.also {
            operation.photoPath = it
        }

        val condition = when (draft.condition) {
            "Like New" -> "LIKE_NEW"
            "Good" -> "GOOD"
            "Used" -> "FAIR"
            else -> error("Choose a supported condition.")
        }

        val exchangeType = when (draft.exchangeType) {
            "Free" -> "FREE"
            "Exchange" -> "EXCHANGE"
            else -> error("Choose an exchange type.")
        }

        db.runTransaction { transaction ->
            val current = transaction.get(reference)

            require(
                FirebaseAuth.getInstance().currentUser?.uid == student.id
            ) {
                "Your session changed. Sign in again."
            }

            if (editId != null) {
                require(current.exists()) {
                    "This material no longer exists."
                }
            }

            if (current.exists()) {
                require(
                    current.getString("ownerId") == student.id &&
                            current.getString("status") == "AVAILABLE"
                ) {
                    "This listing can no longer be edited."
                }
            }

            val now = System.currentTimeMillis()
            val createdAt = if (current.exists()) {
                requireNotNull(current.getLong("createdAt"))
            } else {
                now
            }

            transaction.set(
                reference,
                mapOf(
                    "listingId" to operation.id,
                    "ownerId" to student.id,
                    "ownerName" to student.name,
                    "title" to draft.title.trim(),
                    "category" to draft.category,
                    "condition" to condition,
                    "quantity" to draft.quantity.toInt(),
                    "exchangeType" to exchangeType,
                    "pickupArea" to draft.pickupArea,
                    "photoPath" to photoPath,
                    "description" to draft.description.trim(),
                    "status" to "AVAILABLE",
                    "createdAt" to createdAt,
                    "updatedAt" to maxOf(now, createdAt)
                )
            )

            true
        }.awaitListingResult()

        pending.remove(key)

        return MaterialItem(
            id = operation.id,
            title = draft.title.trim(),
            category = draft.category,
            condition = draft.condition,
            quantity = draft.quantity.toInt(),
            exchangeType = draft.exchangeType,
            pickupArea = draft.pickupArea,
            ownerId = student.id,
            ownerName = student.name,
            photo = photoPath,
            description = draft.description.trim()
        )
    }
}