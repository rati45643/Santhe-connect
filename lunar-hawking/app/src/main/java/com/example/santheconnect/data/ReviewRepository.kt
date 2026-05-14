package com.example.santheconnect.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object ReviewRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    // 🔥 GET REVIEWS
    fun getReviews(): Flow<List<Review>> = callbackFlow {
        val subscription = reviewsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    Review(
                        id = doc.id,
                        author = data["author"] as? String ?: "Anonymous",
                        authorId = data["authorId"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        hasVoiceNote = data["hasVoiceNote"] as? Boolean ?: false,
                        hasPhoto = data["hasPhoto"] as? Boolean ?: false,
                        photoUrl = (data["photoUrl"] as? String)?.takeIf { it.isNotBlank() },
                        rating = (data["rating"] as? Number)?.toInt() ?: 5
                    )
                } ?: emptyList()

                trySend(reviews)
            }

        awaitClose { subscription.remove() }
    }

    // 🔥 ADD REVIEW
    suspend fun addReview(review: Review) {
        val data = hashMapOf(
            "author" to review.author,
            "authorId" to review.authorId,
            "content" to review.content,
            "hasVoiceNote" to review.hasVoiceNote,
            "hasPhoto" to review.hasPhoto,
            "photoUrl" to review.photoUrl,
            "rating" to review.rating,
            "timestamp" to Timestamp.now()
        )

        reviewsCollection.add(data).await()
    }

    // 🔥 DELETE REVIEW (Authorized)
    suspend fun deleteReview(id: String, currentUserId: String) {
        val doc = reviewsCollection.document(id).get().await()
        val authorId = doc.getString("authorId") ?: ""
        
        if (authorId == currentUserId && currentUserId.isNotEmpty()) {
            reviewsCollection.document(id).delete().await()
        }
    }
}

// 🔥 DATA CLASS
data class Review(
    val id: String = "",
    val author: String = "Anonymous",
    val authorId: String = "",
    val content: String = "",
    val hasVoiceNote: Boolean = false,
    val hasPhoto: Boolean = false,
    val photoUrl: String? = null,
    val rating: Int = 5
)