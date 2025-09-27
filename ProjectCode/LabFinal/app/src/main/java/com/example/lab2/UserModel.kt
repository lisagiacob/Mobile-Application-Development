package com.example.lab2

import android.content.Context
import android.media.Image
import android.net.Uri
import android.util.Log
import com.example.lab2.SupabaseStorageService.uploadImageFromUri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt


sealed class UserImage {
    data class Resource(val resId: Int) : UserImage()
    data class UriImage(val uri: Uri) : UserImage()
    data class UrlImage(val url: String) : UserImage()
}



data class FirestoreUser(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val hashtag1: String = "",
    val hashtag2: String = "",
    val hashtag3: String = "",
    val image: String? = null,
    val dateOfBirth: String = "",
    val fiscalCode: String = "",
    val phone: String = "",
    val achievements: List<String>? = null,

)

// FirestoreUser.kt
fun FirestoreUser.toUserModel(
    image: UserImage? = null,
    initialReviews: List<Reviews> = emptyList(),
    initialFriends: List<FriendEntry> = emptyList()
): UserModel {
    return UserModel(
        id             = this.id,
        firstName      = this.firstName,
        lastName       = this.lastName,
        username       = this.username,
        email          = this.email,
        bio            = this.bio,
        hashtag1       = this.hashtag1,
        hashtag2       = this.hashtag2,
        hashtag3       = this.hashtag3,
        initialReviews = initialReviews,
        image          = image,
        initialFriends = initialFriends,
        dateOfBirth    = this.dateOfBirth,
        fiscalCode     = this.fiscalCode,
        phone          = this.phone,
        achievements   = this.achievements
    )
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val star: Int,
    var enabled: Boolean? = null
)

class UserModel(
    val id: String,
    firstName: String = "",
    lastName: String = "",
    username: String = "",
    email: String = "",
    bio: String = "",
    hashtag1: String = "",
    hashtag2: String = "",
    hashtag3: String = "",
    initialReviews: List<Reviews> = emptyList(),
    var image: UserImage? = null,
    initialFriends: List<FriendEntry> = emptyList(),
    dateOfBirth:String ="",
    fiscalCode:String ="",
    phone:String="",
    achievements: List<String>? = null,

) {

    // private val _users = MutableStateFlow(mutableMapOf<String, User>())
    // val users: StateFlow<MutableMap<String, User>> = _users

    private val _currentId = MutableStateFlow("1")  //id of the logged in user, for now set to 1
    val currentId: StateFlow<String> = _currentId

    private val _firstName = MutableStateFlow(firstName)
    val firstName: StateFlow<String> = _firstName

    private val _lastName = MutableStateFlow(lastName)
    val lastName: StateFlow<String> = _lastName

    private val _username = MutableStateFlow(username)
    val username: StateFlow<String> = _username

    private val _email = MutableStateFlow(email)
    val email: StateFlow<String> = _email

    private val _bio = MutableStateFlow(bio)
    val bio: StateFlow<String> = _bio

    private val _hashtag1 = MutableStateFlow(hashtag1)
    val hashtag1: StateFlow<String> = _hashtag1

    private val _hashtag2 = MutableStateFlow(hashtag2)
    val hashtag2: StateFlow<String> = _hashtag2

    private val _hashtag3 = MutableStateFlow(hashtag3)
    val hashtag3: StateFlow<String> = _hashtag3

    private val _profileImage = MutableStateFlow<UserImage?>(image)
    val profileImage: StateFlow<UserImage?> = _profileImage
    private val _dateOfBirth = MutableStateFlow(dateOfBirth)
    val dateOfBirth: StateFlow<String> = _dateOfBirth

    private val _fiscalCode = MutableStateFlow(fiscalCode)
    val fiscalCode: StateFlow<String> = _fiscalCode

    private val _phone = MutableStateFlow(phone)
    val phone: StateFlow<String> = _phone

    private val _achievements = MutableStateFlow(achievements)
    val achievements: StateFlow<List<String>?> = _achievements

    // Aggiungi funzioni di aggiornamento, se ti servono
    fun storeDateOfBirth(newDate: String) { _dateOfBirth.value = newDate }
    fun storeFiscalCode(newCode: String) { _fiscalCode.value = newCode }
    fun storePhone(newPhone: String) { _phone.value = newPhone }
    fun storeProfileImage(newImage: UserImage) {
        _profileImage.value = newImage
    }

    private val _reviews = MutableStateFlow(initialReviews.toMutableList())
    val reviews: StateFlow<List<Reviews>> = _reviews

    fun setReviews(newReviews: List<Reviews>) {
        _reviews.value = newReviews.toMutableList()
        updateReviewScore()
    }

    private val _proposals = MutableStateFlow(
        mutableListOf(
            Pair("Northern Lights", R.drawable.northern_lights),
            Pair("Isola Bella di Taormina", R.drawable.isola_bella_taormina),
            Pair("Paris", R.drawable.parigi)
        )
    )
    val proposals: StateFlow<MutableList<Pair<String, Int>>> = _proposals

    private val _bookedTrips = MutableStateFlow(
        mutableListOf(
            Pair("Northern Lights", R.drawable.northern_lights),
            Pair("Isola Bella di Taormina", R.drawable.isola_bella_taormina),
            Pair("Paris", R.drawable.parigi)
        )
    )
    val bookedTrips: StateFlow<MutableList<Pair<String, Int>>> = _bookedTrips

    private val _friends = MutableStateFlow(initialFriends)
    val friends: StateFlow<List<FriendEntry>> = _friends

    fun addFriend(friendEntry: FriendEntry) {
        if (_friends.value.none { it.friendId == friendEntry.friendId }) {
            _friends.value = _friends.value + friendEntry
        }
    }

    fun addAllFriends(friendList: List<FriendEntry>) {
        _friends.value = friendList
    }




    private val _reviewScore = MutableStateFlow(0)
    val reviewScore: StateFlow<Int> = _reviewScore

    fun updateReviewScore() {
        val average = _reviews.value.map { it.stars }.average().roundToInt()
        _reviewScore.value = average
    }



    fun removeProposalAt(index: Int) {
        val list = _proposals.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _proposals.value = list
        }
    }

    fun removeFriendById(friendId: String) {
        _friends.value = _friends.value.filterNot {
            it.friendId?.id == friendId
        }
    }


    fun storeFirstName(newName: String) { _firstName.value = newName }
    fun storeLastName(newName: String) { _lastName.value = newName }
    fun storeUsername(newName: String) { _username.value = newName }
    fun storeEmail(newEmail: String) { _email.value = newEmail }
    fun storeBio(newBio: String) { _bio.value = newBio }
    fun storeHashtag1(newHashtag1: String) { _hashtag1.value = newHashtag1 }
    fun storeHashtag2(newHashtag2: String) { _hashtag2.value = newHashtag2 }
    fun storeHashtag3(newHashtag3: String) { _hashtag3.value = newHashtag3 }
    //fun storeProfileImage(uri: Uri) { _profileImageUri.value = uri }


    fun toFirestoreUser(imageUrl: String?): FirestoreUser {
        return FirestoreUser(
            id        = this.id,
            firstName = this.firstName.value,
            lastName  = this.lastName.value,
            username  = this.username.value,
            email     = this.email.value,
            bio       = this.bio.value,
            hashtag1  = this.hashtag1.value,
            hashtag2  = this.hashtag2.value,
            hashtag3  = this.hashtag3.value,
            image = imageUrl,
            /*when (val img = profileImage.value) {
                is UserImage.UriImage -> img.uri.toString()
                is UserImage.UrlImage -> img.url
                else -> null
            },*/
            dateOfBirth = this.dateOfBirth.value,
            fiscalCode  = this.fiscalCode.value,
            phone       = this.phone.value



        )
    }




}



object UserStore {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")



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

            // 2. Crea notifica
            val notification = mapOf(
                "message" to "Hai ricevuto una nuova recensione personale!",
                "timestamp" to Timestamp.now(),
                "read" to false,
                "type" to "user_review",
                "relatedUserId" to userId,
                "senderId" to reviewerId
            )

            // 3. Salva la notifica
            usersCollection
                .document(userId)
                .collection("notifications")
                .add(notification)
                .await()

        } catch (e: Exception) {
            Log.e("UserRepository", "Errore aggiungendo recensione", e)
        }
    }



    suspend fun getUserById(id: String): UserModel? {
        return try {
            val doc = usersCollection.document(id).get().await()
            val fsUser = doc.toObject(FirestoreUser::class.java)?.copy(id = doc.id)
            fsUser?.toUserModel(
                image = fsUser.image?.let { UserImage.UrlImage(it) }
            )
        } catch (e: Exception) {
            Log.e("UserStore", "Error fetching user by id: $id", e)
            null
        }
    }

    suspend fun getUsersByUsername(username: String): List<UserModel?>? {
        return try {
            val snapshot = usersCollection.whereEqualTo("username", username).get().await()
            //val fsUser = doc.toObjects(FirestoreUser::class.java)?.copy(id = doc.id)

            val users = snapshot.documents.map { doc ->
                val fsUser = doc.toObject(FirestoreUser::class.java)?.copy(id = doc.id)
                fsUser?.toUserModel(
                    image = fsUser.image?.let { UserImage.UrlImage(it) }
                )
            }
            users
        } catch (e: Exception) {
            Log.e("UserStore", "Error fetching user by username: $username", e)
            null
        }
    }

    suspend fun getAllUsers(): List<UserModel> {
        return try {
            val snapshot = usersCollection.whereNotEqualTo("id", "GUEST").get().await()
            snapshot.documents.mapNotNull { doc ->
                val fsUser = doc.toObject(FirestoreUser::class.java)?.copy(id = doc.id)
                fsUser?.toUserModel(
                    image = fsUser.image?.let { UserImage.UrlImage(it) }
                )
            }
        } catch (e: Exception) {
            Log.e("UserStore", "Errore nel recuperare tutti gli utenti", e)
            emptyList()
        }
    }



    suspend fun getUserWithReviews(userId: String): UserModel? {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            val fsUser = userDoc.toObject(FirestoreUser::class.java)?.copy(id = userDoc.id)
            if (fsUser == null) return null

            val reviewsSnapshot = usersCollection.document(userId)
                .collection("reviews")
                .get().await()

            val reviews = reviewsSnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Reviews::class.java)
                } catch (e: Exception) {
                    Log.e("UserStore", "Errore nel parsing recensione", e)
                    null
                }
            }

            val friends = usersCollection
                .document(userId)
                .collection("friends")
                .get()
                .await()
                .documents.mapNotNull { it.toObject(FriendEntry::class.java) }

            fsUser.toUserModel(initialReviews = reviews, initialFriends = friends)
        } catch (e: Exception) {
            Log.e("UserStore", "Errore fetching user with reviews", e)
            null
        }
    }


    suspend fun addOrUpdate(user: UserModel, context: Context) {
        Log.d("update user","user.image: ${user.image}")
        val imageUri = when (val img = user.image) {
            is UserImage.UriImage -> img.uri.toString()
            else -> null
        }
        Log.d("update user","imageUri: $imageUri")
        val imageUrl = imageUri?.let {
            CoroutineScope(Dispatchers.Main).async {
                uploadImageFromUri(context, Uri.parse(it), "images")
            }
        }?.await()

        try {
            val fsUser = user.toFirestoreUser(imageUrl)
            usersCollection.document(user.id).set(fsUser).await()
        } catch (e: Exception) {
            Log.e("UserStore", "Error updating user", e)
        }
    }

    suspend fun removeUser(id: String) {
        try {
            usersCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e("UserStore", "Error deleting user with id: $id", e)
        }
    }

    suspend fun getUserModelById(userId: String): UserModel? {
        return getUserById(userId)
    }
}



data class Reviews(
    val content: String = "",
    val stars: Int = 0,
    val date: Timestamp? = null,
    val reviewerUsername: DocumentReference? = null,
    val reviewerImage: DocumentReference? = null,
    val reviewerId: DocumentReference?= null
)
data class ResolvedReview(
    val content: String,
    val stars: Int,
    val date: Timestamp,
    val reviewerId: String,
    val reviewerUsername: String,
    val reviewerImageUrl: String?
)

data class FriendEntry(
    val friendId: DocumentReference? = null,
    val imageUrl: DocumentReference? = null,
    val username: DocumentReference? = null,
)
