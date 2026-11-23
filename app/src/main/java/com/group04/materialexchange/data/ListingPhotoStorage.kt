package com.group04.materialexchange.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

object ListingPhotoStorage {

    private const val BUCKET =
        "gs://project-material-exchange.firebasestorage.app"

    private const val MAX_BYTES = 2L * 1024 * 1024
    private const val MAX_SIDE = 1280

    private val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance(BUCKET)

    private val cache = object : LruCache<String, Bitmap>(
        12 * 1024
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return max(1, value.allocationByteCount / 1024)
        }
    }

    private suspend fun <T> Task<T>.awaitPhotoResult(): T =
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

    suspend fun upload(
        context: Context,
        photo: String,
        listingId: String
    ): String {
        val uid = requireNotNull(
            FirebaseAuth.getInstance().currentUser?.uid
        ) {
            "Sign in before uploading a photo."
        }

        require(
            photo.startsWith("content:") ||
                    photo.startsWith("file:")
        ) {
            "Choose a photo from your device."
        }

        val bytes = withContext(Dispatchers.IO) {
            compressPhoto(context, Uri.parse(photo))
        }

        require(bytes.size.toLong() <= MAX_BYTES) {
            "The photo is too large. Choose a smaller photo."
        }

        val path =
            "listing_photos/$uid/$listingId/" +
                    "${UUID.randomUUID()}.jpg"

        val reference = storage.reference.child(path)

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        reference.putBytes(bytes, metadata)
            .awaitPhotoResult()

        return "$BUCKET/$path"
    }

    suspend fun load(photo: String): Bitmap {
        val uid = requireNotNull(
            FirebaseAuth.getInstance().currentUser?.uid
        ) {
            "Sign in to view photos."
        }

        require(photo.startsWith("$BUCKET/listing_photos/")) {
            "Unsupported photo location."
        }

        val cacheKey = "$uid:$photo"

        cache.get(cacheKey)?.let {
            return it
        }

        val bytes = storage.getReferenceFromUrl(photo)
            .getBytes(MAX_BYTES)
            .awaitPhotoResult()

        val bitmap = withContext(Dispatchers.IO) {
            requireNotNull(
                BitmapFactory.decodeByteArray(
                    bytes,
                    0,
                    bytes.size
                )
            ) {
                "The photo could not be opened."
            }
        }

        cache.put(cacheKey, bitmap)
        return bitmap
    }

    private fun compressPhoto(
        context: Context,
        uri: Uri
    ): ByteArray {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        requireNotNull(resolver.openInputStream(uri)).use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        require(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "This file is not a supported image."
        }

        var sample = 1

        while (
            max(bounds.outWidth, bounds.outHeight) / sample >
            MAX_SIDE * 2
        ) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
        }

        val original = requireNotNull(
            requireNotNull(
                resolver.openInputStream(uri)
            ).use {
                BitmapFactory.decodeStream(it, null, options)
            }
        ) {
            "The photo could not be opened."
        }

        val orientation = runCatching {
            requireNotNull(
                resolver.openInputStream(uri)
            ).use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                    setScale(-1f, 1f)

                ExifInterface.ORIENTATION_ROTATE_180 ->
                    setRotate(180f)

                ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                    setScale(1f, -1f)

                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    setRotate(90f)
                    postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_ROTATE_90 ->
                    setRotate(90f)

                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    setRotate(-90f)
                    postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_ROTATE_270 ->
                    setRotate(-90f)
            }
        }

        val oriented = Bitmap.createBitmap(
            original,
            0,
            0,
            original.width,
            original.height,
            matrix,
            true
        )

        val ratio = minOf(
            1f,
            MAX_SIDE.toFloat() /
                    max(oriented.width, oriented.height)
        )

        val resized = Bitmap.createScaledBitmap(
            oriented,
            max(1, (oriented.width * ratio).roundToInt()),
            max(1, (oriented.height * ratio).roundToInt()),
            true
        )

        return try {
            ByteArrayOutputStream().use {
                check(
                    resized.compress(
                        Bitmap.CompressFormat.JPEG,
                        80,
                        it
                    )
                ) {
                    "Photo compression failed."
                }

                it.toByteArray()
            }
        } finally {
            listOf(original, oriented, resized)
                .distinct()
                .forEach {
                    it.recycle()
                }
        }
    }
}