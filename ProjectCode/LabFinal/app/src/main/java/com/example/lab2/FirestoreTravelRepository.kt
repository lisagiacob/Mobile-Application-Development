package com.example.lab2

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.lab2.SupabaseStorageService.uploadImageFromUri
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class FirestoreTravelRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    suspend fun getAllTravels(): List<Travel> {

        val snapshot = db.collection("travels")
            .limit(20).get().await()

        return snapshot.documents.mapNotNull {

            it.toTravel(it.id)
        }
    }

    suspend fun getSuggestedTravels(): List<Travel> {

        val snapshot = db.collection("travels")
            .whereNotEqualTo("owner", FirebaseAuth.getInstance().currentUser?.uid)
            .limit(20)
            .get().await()

        return snapshot.documents.mapNotNull {
            it.toTravel(it.id)
        }
    }

    suspend fun getFriendsProposals(): List<Travel> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return emptyList()

        val friendsSnap = db.collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .await()

        val friendsIds = friendsSnap.documents.mapNotNull { it.id }

        if (friendsIds.isEmpty()) {
            return emptyList()
        }

        val friendRefs = friendsIds.map { id -> db.document("users/$id") }
        val travelsSnap = db.collection("travels")
            .whereIn("owner", friendRefs)
            .get()
            .await()

        return travelsSnap.documents.mapNotNull { it.toTravel(it.id) }
    }


    //just to visualize a list of travels, participants and review will be requested from TravelProposal
    fun getProposalsForUser(userId: String): Flow<List<Travel>> = callbackFlow {
        Log.d("User: ", userId)
        val userRef = db.collection("users").document(userId)
        val listener = db.collection("travels").whereEqualTo("owner", userRef)
            .addSnapshotListener { snapshot, er ->
                if (snapshot != null) {
                    val travels = snapshot.documents.mapNotNull {
                        it.toTravel(it.id)
                    }
                    trySend(travels)
                } else {
                    Log.e("Error", er.toString())
                    trySend(emptyList())
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    //i bookedTrips con data passata vengono messi in oldTrips
    fun getBookedTrips(userId: String): Flow<List<Travel>> = callbackFlow {
        //questo listener osserva  solo i documenti con userId dato
        val userRef = db.collection("users").document(userId)
        val listener = db.collection("participants").whereEqualTo("userId", userRef).whereEqualTo("enabled", true)
            .addSnapshotListener { snapshot, er ->
                if (snapshot != null) {
                    val bookedTrips: MutableList<Travel> = mutableListOf()

                    launch {
                        for (doc in snapshot.documents) {

                            val travelId = doc.getDocumentReference("travelId")?.id
                            if (travelId != null) {
                                //Log.d("BookedTrips", "BookedTrips: Found travelId: $travelId")
                                val travel = getTravelById(travelId)
                                if (travel != null) {
                                    bookedTrips.add(travel)
                                }
                            } else {
                                Log.e("Error", "travelId is null in bookedTrips")
                            }
                        }
                        Log.d("BookedTrips", "BookedTrips in getBookedTrips $bookedTrips")
                        trySend(bookedTrips)
                    }
                } else {
                    Log.e("Error", er.toString())
                    trySend(emptyList())
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    fun getFavoriteTrips(userId: String): Flow<List<Travel>> = callbackFlow {

        val listener = db.collection("users").document(userId)
            .addSnapshotListener {snapshot, er ->
                if (snapshot != null) {
                    launch {
                        val favoriteTravels: MutableList<Travel> = mutableListOf()

                        val favoriteIds = snapshot.get("favorites") as? List<String>
                        if (!favoriteIds.isNullOrEmpty()) {
                            try {
                                val snap = db.collection("travels")
                                    .whereIn(FieldPath.documentId(), favoriteIds)
                                    .get().await()

                                for (doc in snap.documents) {
                                    val travel = doc.toTravel(doc.id)
                                    if (travel != null) {
                                        favoriteTravels.add(travel)
                                    }
                                }

                                trySend(favoriteTravels)
                            } catch (e: Exception) {
                                Log.e("Favorites", "Errore Firestore: ${e.message}")
                                trySend(emptyList())
                            }
                        }
                        else {
                            trySend(emptyList())
                        }
                    }
                } else {
                    Log.e("Favorites", er.toString())
                    trySend(emptyList())
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    suspend fun getTravelById(travelId: String): Travel? {

        val snapshot = db.collection("travels").document(travelId)
            .get().await()

        if (snapshot == null) {
            return null
        }

        return snapshot.toTravel(snapshot.id)
    }

    //usato per mostrare i singoli travels
//    fun getTravelFlowById(travelId: String): Flow<Travel?> = callbackFlow {
//        //uso un listener perchè così l'owner può vedere le nuove richiestre in tempo reale
//        val listener = db.collection("travels").document(travelId)
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    Log.e("TravelFlow", "Firestore error: $e")
//                    return@addSnapshotListener
//                }
//                if (snapshot != null) {
//
//                    Log.d("ToTravel", "Snapshot id=${snapshot.id}, exists=${snapshot.exists()}")
//                    //Log.d("Travel by id", "Travel by id found")
//                    //mettere snapshot.id come id nel Travel, così poi lo possiamo recuperare dal db ed avere id automatico
//                    trySend(snapshot.toTravel(snapshot.id))
//                } else {
//                    Log.d("Travel by id", "Travel by id not found")
//                    trySend(null)
//                }
//            }  //.whereEqualTo("id", travelId)
//
//        awaitClose {
//            listener.remove()
//        }
//    }

    fun getTravelFlowById(travelId: String): Flow<Travel?> = callbackFlow {
        val listener = db.collection("travels").document(travelId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("TravelFlow", "Firestore error: $e")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("ToTravel", "Snapshot id=${snapshot.id}, exists=${snapshot.exists()}, data=${snapshot.data}")  // <--- aggiungi questo log

                    trySend(snapshot.toTravel(snapshot.id))
                } else {
                    Log.d("ToTravel", "Snapshot is null")
                    trySend(null)
                }
            }

        awaitClose {
            listener.remove()
        }
    }


    suspend fun addTravel(travel: Travel, context: Context) {

        val imageUploadDeferreds = travel.images.mapNotNull { img ->
            val imageUri = when (img) {
                is TravelImage.UriImage -> img.uri
                else -> null
            }

            imageUri?.let {
                // Ogni upload avviene in parallelo su Dispatchers.IO
                CoroutineScope(Dispatchers.Main).async {
                    uploadImageFromUri(context, Uri.parse(it), "travelimages")
                }
            }
        }

        // Aspettiamo che tutti gli upload finiscano e raccogliamo gli URL risultanti
        val imageUrls = imageUploadDeferreds.mapNotNull { it.await() }

        val travelData = if (imageUrls.isNotEmpty()) {
            travel.toFirestoreMap(imageUrls)
        } else {
            travel.toFirestoreMap(null)  //if we have no images to add
        }

        db.collection("travels")
            .add(travelData)
            .await()
    }

    suspend fun deleteTravel(travelId: String) {
        db.collection("travels")
            .document(travelId)
            .delete()
            .await()
    }

    suspend fun updateTravel(travel: Travel, context: Context) {
        val oldImages = mutableListOf<String>()

        val uploadDeferreds = travel.images.mapNotNull { img ->
            when (img) {
                is TravelImage.RemoteUrl -> {
                    oldImages.add(img.url)
                    null
                }

                is TravelImage.UriImage -> {
                    //  upload in parallelo su Dispatchers.IO
                    CoroutineScope(Dispatchers.Main).async {
                        uploadImageFromUri(context, Uri.parse(img.uri), "travelimages")
                    }
                }

                else -> null
            }
        }

        // Aspettiamo tutti gli upload e filtriamo i non null
        val imageUrls = uploadDeferreds.mapNotNull { it.await() }

        val travelData = if (imageUrls.isNotEmpty()) {  //if we have new images to add
            val newImgList = (oldImages + imageUrls).distinct()
            travel.toFirestoreMap(newImgList)
        } else {
            travel.toFirestoreMap()  //if we have no images to add
        }

        db.collection("travels")
            .document(travel.id)
            .set(travelData, SetOptions.merge())
            .await()
    }

    suspend fun getRefTitle(travelId: String): String {
        val snapshot = db.collection("travels").document(travelId).get().await()
        if (snapshot != null) {
            return snapshot.getString("title") ?: ""
        }
        return ""
    }

    // PARTICIPANTS
    fun getParticipantsForTravel(travelId: String): Flow<Map<String, Pair<Participant, Boolean?>>> =
        callbackFlow {
            val travelRef = db.collection("travels").document(travelId)
            val listener =
                db.collection("participants")  //.document(travelId).collection("participants")
                    .whereEqualTo("travelId", travelRef)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            Log.d("Participants", "Participants: Snapshot not null")
                            launch {
                                val participants = snapshot.documents.mapNotNull { it ->
                                    val userRef =
                                        it.getDocumentReference("userId") ?: return@mapNotNull null

                                    try {

                                        val userSnapshot = userRef.get().await()
                                        Log.d(
                                            "Participants",
                                            "Participants: UserSnapshot id: ${userSnapshot?.id}"
                                        )
                                        val username = userSnapshot?.getString("username")
                                        val enabled = if (it.contains("enabled")) {
                                            it.getBoolean("enabled")
                                        } else {
                                            null
                                        }
                                        val additionalParticipants =
                                            (it.get("additionalParticipants") as? List<Map<*, *>>)?.map {
                                                AdditionalParticipant(
                                                    name = it["name"] as String,
                                                    surname = it["surname"] as String,
                                                    birthDate = it["birthDate"] as String,
                                                    cellphone = it["cellphone"] as String
                                                )
                                            }

                                        "${userSnapshot?.id}" to Pair(
                                            Participant(
                                                userSnapshot.id,
                                                username ?: "",
                                                additionalParticipants
                                            ), enabled
                                        )
                                    } catch (e: Exception) {
                                        Log.e("Error", e.toString())
                                        null
                                    }
                                }
                                Log.d(
                                    "Participants",
                                    "Participants in getParticipants: $participants"
                                )
                                trySend(participants.toMap())
                            }
                        } else {
                            trySend(emptyMap())
                        }
                    }

            awaitClose { listener.remove() }
        }

    suspend fun addParticipant(travelId: String, participant: Participant, enabled: Boolean) {
        val participantData = mapOf(
            "userId" to db.document("users/${participant.userId}"),
            "travelId" to db.document("travels/$travelId"),
            "enabled" to enabled,
            "username" to participant.username,
            "additionalParticipants" to participant.additionalParticipants?.map {
                mapOf(
                    "name" to it.name,
                    "surname" to it.surname,
                    "birthDate" to it.birthDate,
                    "cellphone" to it.cellphone
                )
            }
        )

        db.collection("participants")
            .document("${participant.userId},$travelId")
            .set(participantData)
            .await()
    }

//    suspend fun setParticipantEnabled(travelId: String, participant: String, enabled: Boolean){
//        val updateData = mapOf("enabled" to enabled)
//
//        Log.d("Owner", participant)
//        Log.d("Travek", travelId)
//        db.collection("participants")
//            .document("$participant,$travelId")
//            .set(updateData, SetOptions.merge())  //!Merge: aggiorna solo il campo enabled!
//            .await()
//    }

    //Cpn notifiche
    suspend fun setParticipantEnabled(travelId: String, participant: String, enabled: Boolean) {
        val updateData = mapOf("enabled" to enabled)

        db.collection("participants")
            .document("$participant,$travelId")
            .set(updateData, SetOptions.merge()) //!Merge: aggiorna solo il campo enabled!
            .await()

        // Recupera info sul viaggio per notifica
        val travelSnapshot = db.collection("travels").document(travelId).get().await()
        val travelTitle = travelSnapshot.getString("title") ?: "un viaggio"

        // Crea messaggio in base all'esito
        val status = if (enabled) "accettata" else "rifiutata"
        val message = "La tua candidatura a \"$travelTitle\" è stata $status"

        // Aggiungi notifica all’utente candidato
        val notification = mapOf(
            "message" to message,
            "timestamp" to Timestamp.now(),
            "read" to false,
            "type" to "application-status",
            "relatedTravelId" to travelId
        )

        db.collection("users")
            .document(participant)
            .collection("notifications")
            .add(notification)
            .await()
    }

//    suspend fun addPendingApplication(travelId: String, participant: Participant){
//        val participantData = mapOf(
//            "userId" to db.document("users/${participant.userId}"),
//            "travelId" to db.document("travels/$travelId"),
//            "enabled" to null,
//            "username" to participant.username,
//            "additionalParticipants" to participant.additionalParticipants?.map {
//                mapOf(
//                    "name" to it.name,
//                    "surname" to it.surname,
//                    "birthDate" to it.birthDate,
//                    "cellphone" to it.cellphone
//                )
//            }
//        )
//
//        db.collection("participants")
//            .document("${participant.userId},$travelId")
//            .set(participantData)
//            .await()
//    }

    //Con notifiche
    suspend fun addPendingApplication(travelId: String, participant: Participant) {
        val participantData = mapOf(
            "userId" to db.document("users/${participant.userId}"),
            "travelId" to db.document("travels/$travelId"),
            "enabled" to null,
            "username" to participant.username,
            "additionalParticipants" to participant.additionalParticipants?.map {
                mapOf(
                    "name" to it.name,
                    "surname" to it.surname,
                    "birthDate" to it.birthDate,
                    "cellphone" to it.cellphone
                )
            }
        )

        // Salva la candidatura
        db.collection("participants")
            .document("${participant.userId},$travelId")
            .set(participantData)
            .await()

        // Recupera il travel per sapere chi è l'owner
        val travelSnapshot = db.collection("travels").document(travelId).get().await()
        val ownerRef = travelSnapshot.getDocumentReference("owner")  // tipo: DocumentReference

        val ownerId = ownerRef?.id ?: return  // fallback sicuro

        // Aggiungi notifica all’owner
        val notification = mapOf(
            "message" to "${participant.username} si è candidato al tuo viaggio!",
            "timestamp" to Timestamp.now(),
            "read" to false,
            "type" to "application",
            "relatedTravelId" to travelId,
            "senderId" to participant.userId
        )

        db.collection("users")
            .document(ownerId)
            .collection("notifications")
            .add(notification)
            .await()
    }

    fun DocumentSnapshot.toParticipant(): Participant? {
        return try {
            // Estrai il riferimento al documento utente
            val userRef = get("userId") as? DocumentReference
            val userId = userRef?.id ?: return null

            // Estrai lo username
            val username = getString("username") ?: ""

            // Estrai e converti la lista degli additionalParticipants
            val rawAdditional = get("additionalParticipants") as? List<*>
            val additionalParticipants = rawAdditional?.mapNotNull { entry ->
                if (entry is Map<*, *>) {
                    AdditionalParticipant(
                        name = entry["name"] as? String,
                        surname = entry["surname"] as? String,
                        birthDate = entry["birthDate"] as? String,
                        cellphone = entry["cellphone"] as? String
                    )
                } else null
            }

            Participant(
                userId = userId,
                username = username,
                additionalParticipants = additionalParticipants
            )

        } catch (e: Exception) {
            Log.e("Firestore", "Errore parsing Participant: ${this.id}", e)
            null
        }
    }

    fun getPendingApplicationsForOwner(userId: String): Flow<List<Pair<Travel, Participant>>> =
        callbackFlow {
            val userRef = FirebaseFirestore.getInstance().document("users/$userId")
            val travelsListener = db.collection("travels")
                .whereEqualTo("owner", userRef) // o "ownerId" se hai un campo così
                .addSnapshotListener { travelSnapshot, travelError ->
                    if (travelSnapshot == null || travelError != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    launch {
                        val allResults = mutableListOf<Pair<Travel, Participant>>()

                        travelSnapshot.documents.forEach { travelDoc ->
                            val travel = travelDoc.toTravel(travelDoc.id) ?: return@forEach
                            val travelRef = Firebase.firestore.document("travels/101")

                            Log.d("travel: ", travelRef.path)
                            db.collection("participants")
                                .whereEqualTo("travelId", travelRef)
                                .whereEqualTo("enabled", null)
                                .get()
                                .addOnSuccessListener { pendingDocs ->
                                    pendingDocs.documents.forEach { pDoc ->
                                        val participant = pDoc.toParticipant()
                                        if (participant != null) {
                                            allResults.add(travel to participant)
                                        }
                                    }
                                    trySend(allResults.toList())
                                }
                                .addOnFailureListener {
                                    Log.e("Firestore", "Failed to fetch participants: $it")
                                }
                        }
                    }
                }

            awaitClose { travelsListener.remove() }
        }

    //REVIEWS
    fun getReviewsForTravel(travelId: String): Flow<List<Review>> = callbackFlow {
        val listener = db.collection("travels")
            .document(travelId)
            .collection("reviews")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reviews = snapshot.documents.mapNotNull { doc ->
                        val rawImages = doc.get("images") as? List<String>

                        val images: MutableList<TravelImage> = rawImages?.map {
                            TravelImage.RemoteUrl(it)
                        }?.toMutableList() ?: mutableListOf()

                        Review(
                            description = doc.getString("description") ?: "",
                            rating = doc.getLong("rating")?.toInt() ?: 0,
                            date = doc.getTimestamp("date"),
                            reviewerUsername = doc.getDocumentReference("reviewerUsername"),
                            reviewerId = doc.getDocumentReference("reviewerId"),
                            userImage = doc.getDocumentReference("userImage"),
                            images = images
                        )
                    }
                    trySend(reviews)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun addReviewToTravel(travelId: String, review: Review, context: Context) {

        val imageUrls = mutableListOf<String>()

        review.images.forEach { img ->
            val imageUri = when (img) {
                is TravelImage.UriImage -> img.uri
                else -> null
            }

            if (imageUri != null) {
                val url = uploadImageFromUri(context, Uri.parse(imageUri), "reviewimages")
                url?.let { imageUrls.add(it) }
            }
        }

        val reviewData = mapOf(
            "description" to review.description,
            "rating" to review.rating,
            "date" to review.date,
            "reviewerId" to review.reviewerId,
            "reviewerUsername" to review.reviewerUsername,
            "userImage" to review.userImage,
            "images" to imageUrls
        )

        db.collection("travels")
            .document(travelId)
            .collection("reviews")
            .add(reviewData)
            .await()

        // 1. Recupera owner del viaggio
        val travelSnapshot = db
            .collection("travels")
            .document(travelId)
            .get()
            .await()

        val ownerRef = travelSnapshot.getDocumentReference("owner")
        val ownerId = ownerRef?.id ?: return

        // 2. Crea la notifica
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val notification = mapOf(
            "message" to "Nuova recensione al tuo viaggio!",
            "timestamp" to Timestamp.now(),
            "read" to false,
            "type" to "travel_review",
            "relatedTravelId" to travelId,
            "senderId" to currentUserId
        )

        // 3. Salva la notifica
        db.collection("users")
            .document(ownerId)
            .collection("notifications")
            .add(notification)
            .await()
    }

    //SEARCH
    suspend fun getFilteredTravels(filter: TravelFilter): List<Travel> {
        var query: Query = db.collection("travels")

        if (filter.locationQuery.isNotBlank()) {
            val locQuery = filter.locationQuery.trim().lowercase()   //in places tutti i luoghi soon in lowercase per la ricerca
            query = query.whereArrayContains("places", locQuery)
        }

        if (filter.startDate != null) {
            query = query.whereGreaterThanOrEqualTo("startDate", filter.startDate)
        }

        if (filter.endDate != null) {
            query = query.whereLessThanOrEqualTo("endDate", filter.endDate)
        }

        if (filter.status != null) {
            val today = Timestamp.now()
            if (filter.status == TravelStatus.AVAILABLE){
                query = query.whereGreaterThan("startDate", today)
            }
            else {
                query = query.whereLessThanOrEqualTo("endDate", today)
            }
        }

        if (filter.priceRange != null) {
            query = query
                .whereGreaterThanOrEqualTo("price", filter.priceRange.start)
                .whereLessThanOrEqualTo("price", filter.priceRange.endInclusive)
        }

        val snapshot = query.get().await()
        return if (snapshot != null) {
            snapshot.documents.mapNotNull { doc ->
                doc.toTravel(doc.id)
            }
        } else {
            emptyList()
        }
    }
}

fun timestampToDateString(timestamp: Timestamp): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val date = timestamp.toDate()
    return formatter.format(date)
}

fun stringToTimestamp(dateStr: String): Timestamp? {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = formatter.parse(dateStr)
        date?.let { Timestamp(it) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}