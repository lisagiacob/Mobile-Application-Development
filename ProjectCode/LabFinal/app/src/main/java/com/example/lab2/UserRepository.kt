import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.lab2.Achievement
import com.example.lab2.FirestoreUser
import com.example.lab2.FriendEntry
import com.example.lab2.NotificationModel
import com.example.lab2.ResolvedReview
import com.example.lab2.Reviews
import com.example.lab2.UserImage
import com.example.lab2.UserModel
import com.example.lab2.UserStore
import com.example.lab2.toUserModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.type.Date
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale


class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Fetches a user document by ID and maps it to FirestoreUser DTO.
     */
    private val usersCollection = db.collection("users")


    suspend fun fetchUserById(uid: String): FirestoreUser {
        if (uid == "GUEST") {
            return FirestoreUser(
                id = "GUEST",
                firstName = "Guest",
                lastName = "",
                username = "guest_user",
                email = "",
                bio = "Enjoying the app anonymously",
                hashtag1 = "#guest",
                hashtag2 = "#explorer",
                hashtag3 = "#anonymous",
                image = null
            )
        }
        val snap = db.collection("users").document(uid).get().await()
        if (!snap.exists()) error("User $uid not found")
        val dto = snap.toObject(FirestoreUser::class.java)!!
        // if the DTO’s id is blank, use the document’s key
        return dto.copy(id = dto.id.takeIf { it.isNotBlank() } ?: snap.id)
    }



    /*
    suspend fun fetchUserById(userId: String): FirestoreUser =
        db.collection("users")
            .document(userId)
            .get()
            .await()  // from kotlinx-coroutines-play-services
            .toObject(FirestoreUser::class.java)
            ?.copy(id = userId)
            ?: throw IllegalStateException("User \$userId not found in Firestore")


     */


    /**
     * Listens for real-time updates on a user document.
     */
    fun listenUserById(
        userId: String,
        onUpdate: (FirestoreUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .addSnapshotListener { snap, err ->
                when {
                    err != null -> onError(err)
                    snap != null -> snap.toObject(FirestoreUser::class.java)
                        ?.copy(id = userId)
                        ?.let(onUpdate)
                }
            }
    }

    /**
     * Saves or updates a user's data in Firestore.
     * Maps UserModel state to FirestoreUser and writes to the document.
     */
    suspend fun saveUser(userModel: UserModel) {
        // Create a map of FirestoreUser fields
        val raw = mapOf(
            "id"        to userModel.id,
            "firstName" to userModel.firstName.value,
            "lastName"  to userModel.lastName.value,
            "username"  to userModel.username.value,
            "email"     to userModel.email.value,
            "bio"       to userModel.bio.value,
            "hashtag1"  to userModel.hashtag1.value,
            "hashtag2"  to userModel.hashtag2.value,
            "hashtag3"  to userModel.hashtag3.value,
            "image"     to (userModel.profileImage.value as? UserImage.UrlImage)?.url,
            "friends" to userModel.friends.value,
            "birthDate"  to userModel.dateOfBirth.value,
            "fiscalCode" to userModel.fiscalCode.value,
            "phone"      to userModel.phone.value


        )
        val data = raw.filterValues { it != null }

        db.collection("users")
            .document(userModel.id)
            .set(data, SetOptions.merge())
            .await()
    }
    suspend fun getFriendEntries(userId: String): List<FriendEntry> {
        return try {
            db.collection("users")
                .document(userId)
                .collection("friends")
                .get()
                .await()
                .documents.mapNotNull { it.toObject(FriendEntry::class.java) }
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore caricando amici", e)
            emptyList()
        }
    }
    suspend fun addFriendBidirectional(userId1: String, userId2: String) {
        try {
            val user1Ref = usersCollection.document(userId1)
            val user2Ref = usersCollection.document(userId2)

            val entry1 = FriendEntry(friendId = user2Ref, imageUrl = user2Ref)
            val entry2 = FriendEntry(friendId = user1Ref, imageUrl = user1Ref)

            user1Ref.collection("friends").document(userId2).set(entry1).await()
            user2Ref.collection("friends").document(userId1).set(entry2).await()


            // 2. Crea notifica per userId2 (che è stato aggiunto da userId1)
            val notification = mapOf(
                "message" to "Sei stato aggiunto come amico!",
                "timestamp" to Timestamp.now(),
                "read" to false,
                "type" to "friend_added",
                "relatedTravelId" to null,
                "relatedUserId" to userId1,   // chi ti ha aggiunto
                "senderId" to userId1
            )

            // 3. Salva la notifica nella subcollection "notifications" di userId2
            usersCollection
                .document(userId2)
                .collection("notifications")
                .add(notification)
                .await()
        }catch (e: Exception) {
            Log.e("UserRepository", "Errore aggiungendo amico", e)
        }
    }


    suspend fun getResolvedFriendList(userId: String): List<Triple<String, String?, String?>> {
        return getFriendEntries(userId).mapNotNull { entry ->
            try {
                val friendDoc = entry.friendId?.get()?.await()
                val id = friendDoc?.id ?: return@mapNotNull null
                val image = friendDoc.getString("image")
                val username = friendDoc.getString("username")
                Triple(id, image, username)
            } catch (e: Exception) {
                Log.e("UserRepository", "Errore nel risolvere un amico", e)
                null
            }
        }
    }
    suspend fun removeFriendBidirectional(userId1: String, userId2: String) {
        try {
            val user1Ref = usersCollection.document(userId1)
            val user2Ref = usersCollection.document(userId2)

            user1Ref.collection("friends").document(userId2).delete().await()

            user2Ref.collection("friends").document(userId1).delete().await()

            Log.d("UserRepository", "Amicizia rimossa tra $userId1 e $userId2")
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore rimuovendo amicizia", e)
        }
    }


    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
    suspend fun fetchReviewsForUser(userId: String): List<Reviews>? {

        val reviewsSnapshot = usersCollection
            .document(userId)
            .collection("reviews")
            .get()
            .await()

        if (reviewsSnapshot != null) {

            return reviewsSnapshot.documents.mapNotNull { doc ->
                try {
                    val content = doc.getString("content") ?: return@mapNotNull null
                    val stars = doc.getLong("stars")?.toInt() ?: return@mapNotNull null
                    val date = doc.getTimestamp("date") ?: return@mapNotNull null

                    val reviewerUsernameRef = doc.getDocumentReference("reviewerUsername")
                    val reviewerImageRef = doc.getDocumentReference("reviewerImage")

                    Reviews(
                        content = content,
                        stars = stars,
                        date = date,
                        reviewerUsername = reviewerUsernameRef,
                        reviewerImage = reviewerImageRef
                    )
                } catch (e: Exception) {
                    Log.e("UserRepository", "Errore parsing recensione", e)
                    null
                } catch (e: Exception) {
                    Log.e("UserRepository", "Errore parsing recensione", e)
                    null
                }
            }
        }
        return null
    }

    suspend fun fetchUserWithReviews(userId: String): UserModel? {
        val userDoc = usersCollection.document(userId).get().await()
        val fsUser = userDoc.toObject(FirestoreUser::class.java)?.copy(id = userId) ?: return null
        val reviewsSnapshot = usersCollection
            .document(userId)
            .collection("reviews")
            .get()
            .await()

        val reviews = reviewsSnapshot.documents.mapNotNull { doc ->
            try {
                val content = doc.getString("content") ?: return@mapNotNull null
                val stars = doc.getLong("stars")?.toInt() ?: return@mapNotNull null
                val date = doc.getTimestamp("date") ?: return@mapNotNull null

                val reviewerUsernameRef = doc.getDocumentReference("reviewerUsername")
                val reviewerImageRef = doc.getDocumentReference("reviewerImage")
                val reviewerIdRef = doc.getDocumentReference("reviewerId")

                Reviews(
                    content = content,
                    stars = stars,
                    date = date,
                    reviewerUsername = reviewerUsernameRef,
                    reviewerImage = reviewerImageRef,
                    reviewerId = reviewerIdRef,

                    )
            } catch (e: Exception) {
                Log.e("UserRepository", "Errore parsing recensione", e)
                null
            } catch (e: Exception) {
                Log.e("UserRepository", "Errore parsing recensione", e)
                null
            }
        }

        return fsUser.toUserModel(
            image = fsUser.image?.let { UserImage.UrlImage(it) },
            initialReviews = reviews
        )
    }
    suspend fun resolveReviews(reviews: List<Reviews>): List<ResolvedReview> {
        return reviews.mapNotNull { review ->
            try {
                val reviewerDoc = review.reviewerUsername?.get()?.await()
                val username = reviewerDoc?.getString("username") ?: "Unknown"
                val id = reviewerDoc?.getString("id") ?: "Unknown"

                val imageUrl = review.reviewerImage?.get()?.await()?.getString("image")

                ResolvedReview(
                    content = review.content,
                    stars = review.stars,
                    date = review.date ?: Timestamp.now(),
                    reviewerUsername = username,
                    reviewerId = id,
                    reviewerImageUrl = imageUrl
                )
            } catch (e: Exception) {
                Log.e("UserRepository", "Errore risolvendo review", e)
                null
            }
        }
    }


    suspend fun addReviewToUser(
        userId: String,
        reviewerId: String,
        reviewText: String,
        stars: Int
    ) {
        try {
            val reviewerRef = usersCollection.document(reviewerId)
            val reviewData = mapOf(
                "content" to reviewText,
                "stars" to stars,
                "date" to Timestamp.now(),
                "reviewerUsername" to reviewerRef,
                "reviewerImage" to reviewerRef,
                "reviewerId" to reviewerRef
            )

            // 1. Salva la recensione
            usersCollection
                .document(userId)
                .collection("reviews")
                .add(reviewData)
                .await()

            // 2. Crea la notifica
            val notification = mapOf(
                "message" to "Hai ricevuto una nuova recensione!",
                "timestamp" to Timestamp.now(),
                "read" to false,
                "type" to "user_review",
                "relatedTravelId" to null,
                "relatedUserId" to userId,
                "senderId" to reviewerId
            )

            // 3. Salvala
            usersCollection
                .document(userId)
                .collection("notifications")
                .add(notification)
                .await()

        } catch (e: Exception) {
            Log.e("UserRepository", "Errore aggiungendo recensione", e)
        }
    }

    suspend fun addFavorite(userId: String, travelId: String) {

        try {
            //add travelId to favorites list
            usersCollection
                .document(userId)
                .update("favorites", FieldValue.arrayUnion(travelId)).await()
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                //if the user doesn't have a favorites field yet, create one
                val data = mapOf("favorites" to listOf(travelId))
                usersCollection
                    .document(userId)
                    .set(data, SetOptions.merge()).await()
            } else {
                Log.d("UserRepository", "Errore aggiungendo ai preferiti", e)
            }
        }
    }

    suspend fun removeFavorite(userId: String, travelId: String) {
        db.collection("users").document(userId)
            .update("favorites", FieldValue.arrayRemove(travelId))
            .await()
    }

    suspend fun getAchievements(): List<Achievement> {
        val snapshot = db.collection("achievements").get().await()
        return snapshot.documents.mapNotNull { doc ->
            Achievement(
                id = doc.id,
                title = doc.getString("title") ?: return@mapNotNull null,
                description = doc.getString("description") ?: return@mapNotNull null,
                star = doc.getLong("star")?.toInt() ?: 0
            )
        }

    }

    suspend fun addAchievement(achId: String, userId: String) {
        try {
            usersCollection
                .document(userId)
                .update("achievements", FieldValue.arrayUnion(achId)).await()
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                //if the user doesn't have an achievements field yet, create one
                val data = mapOf("achievements" to listOf(achId))
                usersCollection
                    .document(userId)
                    .set(data, SetOptions.merge()).await()
            } else {
                Log.d("UserRepository", "Errore aggiungendo achievement", e)
            }
        }
    }

    suspend fun getFriendCount(userId: String): Int {
        return try {
            usersCollection
                .document(userId)
                .collection("friends")
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore contando amici", e)
            0
        }
    }

    suspend fun getUserAchievements(userId: String): List<String> {
        return try {
            val snap = usersCollection
                .document(userId)
                .get()
                .await()
            @Suppress("UNCHECKED_CAST")
            snap.get("achievements") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore recuperando achievements utente", e)
            emptyList()
        }
    }



    suspend fun countTravelReviewsByUser(userId: String): Int {
        return try {
            val snapshot = db
                .collectionGroup("reviews")
                .get()
                .await()

            Log.d("ReviewCount", "Fetched ${snapshot.size()} total review docs")

            val count = snapshot.documents.count { doc ->
                val ref = doc.get("reviewerId") as? DocumentReference
                val matches = ref?.id == userId
                if (matches) {
                    Log.d("ReviewCount", "  → matched review ${doc.id} in travel ${doc.reference.parent.parent?.id}")
                }
                matches
            }

            Log.d("ReviewCount", "User $userId wrote $count reviews")
            count
        } catch (e: Exception) {
            Log.e("ReviewCount", "Failed to count reviews for user $userId", e)
            0
        }
    }







}



class PackingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val participantsCollection = db.collection("participants")

    suspend fun loadPackedItems(userId: String, travelId: String): Map<String, Boolean> {
        val docId = "$userId,$travelId"
        val snapshot = participantsCollection.document(docId).get().await()
        @Suppress("UNCHECKED_CAST")
        return snapshot.get("items") as? Map<String, Boolean> ?: emptyMap()
    }

    suspend fun savePackedItems(userId: String, travelId: String, items: Map<String, Boolean>) {
        val docId = "$userId,$travelId"
        participantsCollection
            .document(docId)
            .set(mapOf("items" to items), SetOptions.merge())
            .await()
    }

    suspend fun hasPackedItems(userId: String, travelId: String): Boolean {
        val docId = "$userId,$travelId"
        val snapshot = participantsCollection.document(docId).get().await()
        val map = snapshot.get("items") as? Map<*, *>
        return !map.isNullOrEmpty()
    }


}
