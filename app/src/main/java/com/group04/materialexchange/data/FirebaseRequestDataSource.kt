package com.group04.materialexchange.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.group04.materialexchange.domain.ExchangeNotice
import com.group04.materialexchange.domain.MaterialRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRequestDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val listeners = mutableListOf<ListenerRegistration>()
    private var generation = 0

    private suspend fun <T> Task<T>.awaitResult(): T =
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

    fun stop() {
        generation++
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    fun observe(
        userId: String,
        onRequests: (List<MaterialRequest>) -> Unit,
        onNotices: (List<ExchangeNotice>) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()
        val currentGeneration = generation

        var sent = emptyList<MaterialRequest>()
        var received = emptyList<MaterialRequest>()

        fun active(): Boolean {
            return currentGeneration == generation &&
                    auth.currentUser?.uid == userId
        }

        fun publishRequests() {
            onRequests(
                (sent + received).distinctBy { it.id }
            )
        }

        listeners += db.collection("requests")
            .whereEqualTo("requesterId", userId)
            .addSnapshotListener { snapshot, error ->
                if (!active()) return@addSnapshotListener

                if (error != null) {
                    onError(
                        error.localizedMessage
                            ?: "Could not load sent requests."
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        sent = snapshot.documents.map {
                            toRequest(it)
                        }
                        publishRequests()
                    } catch (e: Exception) {
                        onError(
                            e.message ?: "Invalid request data."
                        )
                    }
                }
            }

        listeners += db.collection("requests")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (!active()) return@addSnapshotListener

                if (error != null) {
                    onError(
                        error.localizedMessage
                            ?: "Could not load received requests."
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        received = snapshot.documents.map {
                            toRequest(it)
                        }
                        publishRequests()
                    } catch (e: Exception) {
                        onError(
                            e.message ?: "Invalid request data."
                        )
                    }
                }
            }

        listeners += db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (!active()) return@addSnapshotListener

                if (error != null) {
                    onError(
                        error.localizedMessage
                            ?: "Could not load notifications."
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        onNotices(
                            snapshot.documents
                                .map { toNotice(it) }
                                .sortedByDescending { it.time }
                        )
                    } catch (e: Exception) {
                        onError(
                            e.message ?: "Invalid notification data."
                        )
                    }
                }
            }
    }

    private fun toRequest(
        document: DocumentSnapshot
    ): MaterialRequest {
        val status = when (document.getString("status")) {
            "PENDING" -> "Pending"
            "ACCEPTED" -> "Accepted"
            "REJECTED" -> "Rejected"
            "COMPLETED" -> "Completed"
            "CANCELLED" -> "Cancelled"
            else -> error("Unknown request status.")
        }

        return MaterialRequest(
            id = document.id,
            itemId = requireNotNull(
                document.getString("listingId")
            ),
            itemTitle = requireNotNull(
                document.getString("itemTitle")
            ),
            senderId = requireNotNull(
                document.getString("requesterId")
            ),
            senderName = requireNotNull(
                document.getString("requesterName")
            ),
            ownerId = requireNotNull(
                document.getString("ownerId")
            ),
            status = status
        )
    }

    private fun toNotice(
        document: DocumentSnapshot
    ): ExchangeNotice {
        return ExchangeNotice(
            id = document.id,
            userId = requireNotNull(
                document.getString("userId")
            ),
            title = requireNotNull(
                document.getString("title")
            ),
            message = requireNotNull(
                document.getString("message")
            ),
            time = requireNotNull(
                document.getLong("createdAt")
            ),
            read = document.getBoolean("isRead") ?: false
        )
    }

    suspend fun requestMaterial(listingId: String) {
        val uid = requireNotNull(auth.currentUser?.uid) {
            "Sign in to request a material."
        }

        val listingRef = db.collection("listings")
            .document(listingId)

        val profileRef = db.collection("users")
            .document(uid)

        val requestId = "${listingId}_$uid"

        val requestRef = db.collection("requests")
            .document(requestId)

        val notificationId = UUID.randomUUID().toString()

        val noticeRef = db.collection("notifications")
            .document(notificationId)

        db.runTransaction { transaction ->
            val listing = transaction.get(listingRef)
            val existing = transaction.get(requestRef)
            val profile = transaction.get(profileRef)

            require(listing.exists()) {
                "This material no longer exists."
            }

            require(profile.exists()) {
                "Reload your profile and try again."
            }

            val ownerId = requireNotNull(
                listing.getString("ownerId")
            )

            require(ownerId != uid) {
                "You cannot request your own material."
            }

            val oldStatus = existing.getString("status")

            if (
                existing.exists() &&
                oldStatus in listOf("PENDING", "ACCEPTED")
            ) {
                return@runTransaction requestId
            }

            require(
                listing.getString("status") == "AVAILABLE"
            ) {
                "This material is no longer available."
            }

            if (existing.exists()) {
                require(
                    oldStatus in listOf("REJECTED", "CANCELLED")
                ) {
                    "This exchange cannot be requested again."
                }
            }

            val name = requireNotNull(
                profile.getString("fullName")
            )

            val title = requireNotNull(
                listing.getString("title")
            )

            val now = maxOf(
                System.currentTimeMillis(),
                (existing.getLong("updatedAt") ?: 0L) + 1L
            )

            val createdAt =
                existing.getLong("createdAt") ?: now

            transaction.set(
                requestRef,
                mapOf(
                    "requestId" to requestId,
                    "listingId" to listingId,
                    "requesterId" to uid,
                    "requesterName" to name,
                    "ownerId" to ownerId,
                    "itemTitle" to title,
                    "status" to "PENDING",
                    "createdAt" to createdAt,
                    "updatedAt" to now,
                    "notificationId" to notificationId
                )
            )

            transaction.set(
                noticeRef,
                mapOf(
                    "notificationId" to notificationId,
                    "userId" to ownerId,
                    "requestId" to requestId,
                    "type" to "NEW_REQUEST",
                    "title" to "New Request",
                    "message" to "$name requested your $title.",
                    "isRead" to false,
                    "createdAt" to now
                )
            )

            requestId
        }.awaitResult()
    }

    suspend fun markRead() {
        val uid = requireNotNull(auth.currentUser?.uid) {
            "Sign in to continue."
        }

        val snapshot = db.collection("notifications")
            .whereEqualTo("userId", uid)
            .whereEqualTo("isRead", false)
            .get(Source.SERVER)
            .awaitResult()

        snapshot.documents.chunked(400).forEach { documents ->
            val batch = db.batch()

            documents.forEach {
                batch.update(it.reference, "isRead", true)
            }

            batch.commit().awaitResult()
        }

    }
    private fun noticeData(
        notificationId: String,
        userId: String,
        requestId: String,
        type: String,
        title: String,
        message: String,
        time: Long
    ): Map<String, Any> {
        return mapOf(
            "notificationId" to notificationId,
            "userId" to userId,
            "requestId" to requestId,
            "type" to type,
            "title" to title,
            "message" to message,
            "isRead" to false,
            "createdAt" to time
        )
    }

    suspend fun respond(
        requestId: String,
        accept: Boolean
    ) {
        val uid = requireNotNull(auth.currentUser?.uid) {
            "Sign in to continue."
        }

        val requestRef = db.collection("requests")
            .document(requestId)

        val notificationId = UUID.randomUUID().toString()

        val noticeRef = db.collection("notifications")
            .document(notificationId)

        db.runTransaction { transaction ->
            val request = transaction.get(requestRef)

            require(request.exists()) {
                "This request no longer exists."
            }

            require(request.getString("ownerId") == uid) {
                "Only the material owner can respond."
            }

            val listingId = requireNotNull(
                request.getString("listingId")
            )

            val listingRef = db.collection("listings")
                .document(listingId)

            val listing = transaction.get(listingRef)

            require(listing.exists()) {
                "This material no longer exists."
            }

            require(listing.getString("ownerId") == uid) {
                "You do not own this material."
            }

            val currentStatus = request.getString("status")

            // A retry must not create another notification.
            if (!accept && currentStatus == "REJECTED") {
                return@runTransaction true
            }

            if (
                accept &&
                currentStatus in listOf("ACCEPTED", "COMPLETED") &&
                listing.getString("acceptedRequestId") == requestId
            ) {
                return@runTransaction true
            }

            require(currentStatus == "PENDING") {
                "This request can no longer be changed."
            }

            if (accept) {
                require(
                    listing.getString("status") == "AVAILABLE"
                ) {
                    "This material is no longer available."
                }
            }

            val requesterId = requireNotNull(
                request.getString("requesterId")
            )

            val title = requireNotNull(
                listing.getString("title")
            )

            val now = maxOf(
                System.currentTimeMillis(),
                (request.getLong("updatedAt") ?: 0L) + 1L,
                (listing.getLong("updatedAt") ?: 0L) + 1L
            )

            transaction.update(
                requestRef,
                mapOf<String, Any>(
                    "status" to if (accept) "ACCEPTED" else "REJECTED",
                    "updatedAt" to now,
                    "notificationId" to notificationId
                )
            )

            if (accept) {
                transaction.update(
                    listingRef,
                    mapOf<String, Any>(
                        "status" to "RESERVED",
                        "acceptedRequestId" to requestId,
                        "updatedAt" to now
                    )
                )
            }

            transaction.set(
                noticeRef,
                noticeData(
                    notificationId = notificationId,
                    userId = requesterId,
                    requestId = requestId,
                    type = if (accept) {
                        "REQUEST_ACCEPTED"
                    } else {
                        "REQUEST_REJECTED"
                    },
                    title = if (accept) {
                        "Request Accepted"
                    } else {
                        "Request Rejected"
                    },
                    message = "Your request for $title was " +
                            if (accept) "accepted." else "rejected.",
                    time = now
                )
            )

            true
        }.awaitResult()
    }

    suspend fun complete(requestId: String) {
        val uid = requireNotNull(auth.currentUser?.uid) {
            "Sign in to continue."
        }

        val requestRef = db.collection("requests")
            .document(requestId)

        val requesterNoticeId = UUID.randomUUID().toString()
        val ownerNoticeId = UUID.randomUUID().toString()

        val requesterNoticeRef = db.collection("notifications")
            .document(requesterNoticeId)

        val ownerNoticeRef = db.collection("notifications")
            .document(ownerNoticeId)

        db.runTransaction { transaction ->
            val request = transaction.get(requestRef)

            require(request.exists()) {
                "This request no longer exists."
            }

            require(request.getString("ownerId") == uid) {
                "Only the material owner can complete the exchange."
            }

            val listingId = requireNotNull(
                request.getString("listingId")
            )

            val listingRef = db.collection("listings")
                .document(listingId)

            val listing = transaction.get(listingRef)

            require(listing.exists()) {
                "This material no longer exists."
            }

            require(
                listing.getString("ownerId") == uid &&
                        listing.getString("acceptedRequestId") == requestId
            ) {
                "This is not the reserved request for this material."
            }

            if (
                request.getString("status") == "COMPLETED" &&
                listing.getString("status") == "COMPLETED"
            ) {
                return@runTransaction true
            }

            require(
                request.getString("status") == "ACCEPTED" &&
                        listing.getString("status") == "RESERVED"
            ) {
                "Only an accepted exchange can be completed."
            }

            val requesterId = requireNotNull(
                request.getString("requesterId")
            )

            val title = requireNotNull(
                listing.getString("title")
            )

            val now = maxOf(
                System.currentTimeMillis(),
                (request.getLong("updatedAt") ?: 0L) + 1L,
                (listing.getLong("updatedAt") ?: 0L) + 1L
            )

            transaction.update(
                requestRef,
                mapOf<String, Any>(
                    "status" to "COMPLETED",
                    "updatedAt" to now,
                    "notificationId" to requesterNoticeId,
                    "completionOwnerNotificationId" to ownerNoticeId
                )
            )

            transaction.update(
                listingRef,
                mapOf<String, Any>(
                    "status" to "COMPLETED",
                    "updatedAt" to now
                )
            )

            val message = "Your $title exchange has been completed."

            transaction.set(
                requesterNoticeRef,
                noticeData(
                    notificationId = requesterNoticeId,
                    userId = requesterId,
                    requestId = requestId,
                    type = "EXCHANGE_COMPLETED",
                    title = "Exchange Completed",
                    message = message,
                    time = now
                )
            )

            transaction.set(
                ownerNoticeRef,
                noticeData(
                    notificationId = ownerNoticeId,
                    userId = uid,
                    requestId = requestId,
                    type = "EXCHANGE_COMPLETED",
                    title = "Exchange Completed",
                    message = message,
                    time = now
                )
            )

            true
        }.awaitResult()
    }
}