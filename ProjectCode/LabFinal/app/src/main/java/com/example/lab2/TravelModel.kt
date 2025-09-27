package com.example.lab2

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


data class Travel (
    var id: String,
    var title: String,
    val owner: Pair<String, String>, //coppia userId, username  , cambiare poi con ownerId?
    var description: String,
    var dateRange: Pair<Timestamp, Timestamp>,  //<String, String>
    var ageRange: String,
    var price: String,
    var groupSize: String,
    var participants: MutableMap<String, Pair<Participant, Boolean?>> = linkedMapOf(),  //key userId, value : username, lista di additional participants  //hashmap per mantenere ordine inserimento
    var locations: MutableList<Triple<String, String, Boolean>>,  //tuple singleDate, location  e overnightStay(String, String, Boolean),
    val referencedTravel: String?,
    var images: MutableList<TravelImage> = mutableListOf(),
    var tags: MutableList<String> = mutableListOf(),  //tag to briefly describe the travel in a user-fiendly way
    var itinerary: MutableList<String>,
    var activities: MutableList<Pair<String, Boolean>>,
    var reviews: MutableList<Review> = mutableListOf(),
    var questions: MutableList<String> = mutableListOf(),
    var answers: MutableList<Pair<Int, String>> = mutableListOf(), //coppie domandaId (indice domanda in list), risposta
    var pendingApplications: MutableList<Participant> = mutableListOf()  //userId, username, lista di additional participants
)

fun Travel.toFirestoreMap(imagesUrl: List<String>? = null): Map<String, Any?> {
    return mapOf(
        //"id" to id,
        "title" to title,
        "description" to description,
        "owner" to Firebase.firestore.document("users/${owner.first}"),
        "ownerName" to owner.second,
        "startDate" to dateRange.first,
        "endDate" to dateRange.second,
        "ageRange" to ageRange,
        "price" to price.toDouble(),
        "groupSize" to groupSize,
        "tags" to tags,
        "itinerary" to itinerary,
        "questions" to questions,
        "answers" to answers.map { mapOf("questionIndex" to it.first, "answer" to it.second) },
        "activities" to activities.map { mapOf("name" to it.first, "enabled" to it.second) },
        "locations" to locations.map { mapOf("date" to it.first, "place" to it.second, "overnight" to it.third) },
        "places" to locations.map { it.second },
        "referencedTravel" to referencedTravel,
        "images" to (imagesUrl   //if imageUrls are provided (create travel), use them, otherwise use images (edit travel)
            ?: images.mapNotNull {
                when (it) {
                    is TravelImage.RemoteUrl -> it.url  //dovremmo lasciare solo questo?
                    else -> null
                }
            }),
    )
}

fun DocumentSnapshot.toTravel(id: String): Travel? {
    return try {

        //val id = getString("id") ?: return null
        val title = getString("title") ?: ""
        val description = getString("description") ?: ""
        val ownerRef = getDocumentReference("owner") ?: return null
        val ownerId = ownerRef.id
        val ownerName = getString("ownerName") ?: ""
        val dateRangeList = get("dateRange") as? List<*>
        val startDate = getTimestamp("startDate")
        val endDate = getTimestamp("endDate")
        /*val dateRange = if (dateRangeList?.size == 2)
            Pair(dateRangeList[0] as Timestamp, dateRangeList[1] as Timestamp)
        else null*/

        val ageRange = getString("ageRange") ?: ""
        val price = (getDouble("price") ?: 0.0).toString()

        val groupSize = getString("groupSize") ?: ""

        val tags = (get("tags") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        val itinerary = (get("itinerary") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        val questions = (get("questions") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()

        val answers = (get("answers") as? List<Map<*, *>>)?.mapNotNull {
            val index = (it["questionIndex"] as? Long)?.toInt()
            val answer = it["answer"] as? String
            if (index != null && answer != null) index to answer else null
        }?.toMutableList() ?: mutableListOf()

        val activities = (get("activities") as? List<Map<*, *>>)?.mapNotNull {
            val name = it["name"] as? String
            val enabled = it["enabled"] as? Boolean
            if (name != null && enabled != null) name to enabled else null
        }?.toMutableList() ?: mutableListOf()

        val locations = (get("locations") as? List<Map<*, *>>)?.mapNotNull {
            val date = it["date"] as? String
            val place = it["place"] as? String
            val overnight = it["overnight"] as? Boolean
            if (date != null && place != null && overnight != null) Triple(date, place, overnight) else null
        }?.toMutableList() ?: mutableListOf()

        val referencedTravel = getString("referencedTravel")

        val rawImages = get("images")
        //Log.d("ImageDebug", "Raw images field: $rawImages")


        val imageUrls = (get("images") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        //Log.d("ImageDebug", "Filtered imageUrls: $imageUrls")

        val images = imageUrls.map {
            when {
                it.startsWith("https://") || it.startsWith("http://") -> TravelImage.RemoteUrl(it)
                it.startsWith("content://") || it.startsWith("file://") -> TravelImage.UriImage(it)
                else -> TravelImage.UriImage(it)
            }
        }.toMutableList() as MutableList<TravelImage>

        Travel(
            id = id,
            title = title,
            owner = ownerId to ownerName,
            description = description,
            dateRange = startDate!! to endDate!!,
            ageRange = ageRange,
            price = price,
            groupSize = groupSize,
            tags = tags,
            itinerary = itinerary,
            questions = questions,
            answers = answers,
            activities = activities,
            locations = locations,
            referencedTravel = referencedTravel,
            images = images
        );
    } catch (e: Exception) {
        Log.e("toTravel", "Errore parsing Travel ${id}", e)
        null
    }
}

data class Participant (
    val userId: String,
    val username: String,
    val additionalParticipants: List<AdditionalParticipant>?
)

data class AdditionalParticipant (
    val name: String?,
    val surname: String?,
    val birthDate: String?,
    val cellphone: String?
)

sealed class TravelImage {
    data class Resource(val resId: Int) : TravelImage()
    data class UriImage(val uri: String) : TravelImage()
    data class RemoteUrl(val url: String) : TravelImage()
}

data class Review(
    val description: String = "",
    val rating: Int = 0,
    val date: Timestamp? = null,
    val reviewerId: DocumentReference? = null,
    val reviewerUsername: DocumentReference?= null,
    val userImage: DocumentReference? = null,
    val images: MutableList<TravelImage> = mutableListOf()
)
class TravelModel(private val repository: FirestoreTravelRepository) {

    //manteniamo questa mappa e gli aggiungiamo tutti i viaggi che vengono fetchati a poco a poco? però così non vede gli aggiornamenti
    private val _travels = MutableStateFlow<Map<String, Travel>>(emptyMap())
    val travels: StateFlow<Map<String, Travel>> = _travels

    fun setTravels(newMap: Map<String, Travel>) {
        _travels.value = newMap
    }

    fun updateTravel(travel: Travel) {
        _travels.value = _travels.value.toMutableMap().apply {
            put(travel.id, travel)
        }
    }

    fun deleteTravel(travelId: String) {
        _travels.value = _travels.value.toMutableMap().apply {
            remove(travelId)
        }
    }


    fun addQuestion(travelId: String, newQuestion: String) {
        val updatedMap = travels.value.toMutableMap()
        val travel = updatedMap[travelId]

        if (travel != null) {
            val updatedQuestions = travel.questions.toMutableList()
            updatedQuestions.add(newQuestion)
            updatedMap[travelId] = travel.copy(questions = updatedQuestions)
            _travels.value = updatedMap
        }
    }

    fun addAnswer(travelId: String, questionId: Int, newAnswer: String) {
        val updatedMap = travels.value.toMutableMap()
        val travel = updatedMap[travelId]

        if (travel != null) {
            val updatedAnswers = travel.answers.toMutableList()
            updatedAnswers.add(questionId to newAnswer)
            updatedMap[travelId] = travel.copy(answers = updatedAnswers)
            _travels.value = updatedMap
        }
        //_travels.value[travelId]?.answers?.add(Pair(questionId, newAnswer))
    }

    fun addReview(travelId: String, newReview: Review) {
        _travels.value = _travels.value.toMutableMap().also { map ->
            val travel = map[travelId]
            if (travel != null) {
                val updatedReviews = travel.reviews.toMutableList().apply {
                    add(newReview)
                }
                val updatedTravel = travel.copy(reviews = updatedReviews)
                map[travelId] = updatedTravel
            }
        }
    }

    //PARTECIPANTS

    fun addParticipant(travelId: String, participant: Participant) {
        val updatedMap = travels.value.toMutableMap()
        val travel = updatedMap[travelId]

        if (travel != null) {
            //remove from pending list
            val pendingApp = travel.pendingApplications.toMutableList()
            pendingApp.removeIf { it.userId == participant.userId }

            //add to participants
            val participants = travel.participants.toMutableMap()
            participants[participant.userId] = Pair(participant, true)

            updatedMap[travelId] = travel.copy(participants = participants, pendingApplications = pendingApp)
            _travels.value = updatedMap
        }
        //_travels.value[travelId]?.participants?.set(userId, Pair(username, additionalParticipants))
    }

    //TODO: se uno vuole aggiungere additional members dopo aver fatto l'apply come fa? se il propriwtario può solo disabilitarlo e non eliminarlo, l'utente non può rifare la procedura
    /*fun addAdditionalMembers(travelId: String, userId: String, username: String, additionalParticipants: List<AdditionalParticipant>) {
        _travels.value[travelId]?.participants?.set(userId, Triple(username, additionalParticipants, true))
    }*/

    suspend fun setParticipantEnabled(travelId: String, userId: String, enabled: Boolean) {
        repository.setParticipantEnabled(travelId, userId, enabled)

        val updatedMap = travels.value.toMutableMap()
        val travel = updatedMap[travelId]

        Log.d("DEBUG", "Updated map: ${_travels.value[travelId]?.participants}")
        if (travel != null) {
            val participants = travel.participants.toMutableMap()
            val participant = participants[userId]?.first
            if (participant != null) {
                participants[userId] = participant to enabled
                updatedMap[travelId] = travel.copy(participants = participants)
                _travels.value = updatedMap
            }
        }
    }

    //return proposals of logged user
    fun getProposalsForUser(userId: String): Flow<List<Travel>> =
        repository.getProposalsForUser(userId)

    fun getBookedTrips(userId: String): Flow<List<Travel>> =
        repository.getBookedTrips(userId)

    fun getTravelById(travelId: String): Flow<Travel?> =
        repository.getTravelFlowById(travelId)

    fun getFavoriteTrips(userId: String): Flow<List<Travel>> =
        repository.getFavoriteTrips(userId)


    //return travels not owned by [userId] – (the friends proposals for now)

    fun getFriendsProposalsFlow(userId: String): Flow<List<Travel>> =
        travels
            .map { map ->
                map.values.filter { it.owner.first != userId }
            }
            .distinctUntilChanged()

    private val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")


    //SEARCH
    suspend fun getFilteredTravels(filter: TravelFilter): Flow<List<Travel>> {

        val travels = repository.getFilteredTravels(filter)

        // Local filters
        return flowOf(travels.filter { travel ->

                    val maxGroupSize = parseRange(travel.groupSize).last
                    val freeSpots = maxGroupSize - travel.participants.size

                    val startDate = travel.dateRange.first.toDate()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                    val endDate = travel.dateRange.second.toDate()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                    val fTags = filter.tags.map{
                        it.lowercase()
                    }

                    val tTags = travel.tags.map{
                        it.lowercase()
                    }

                    val checks = listOf(
                        //tags here because only one array query can be performed at once
                        "tags" to (filter.tags.isEmpty()
                                || tTags.any { it in fTags }),
                        "ageRange" to run {
                            val tripRange = parseAgeRange(travel.ageRange)
                            val fRange = filter.ageRange
                            fRange == null ||
                                    (tripRange.first <= fRange.last && tripRange.last >= fRange.first)
                        },
                        "duration" to (filter.durationRange == null
                                || filter.durationRange.contains(
                            calculateDurationDays(
                                startDate,
                                endDate
                            )
                        )),
                        "freeSpots" to (filter.freeSpotsMin == null
                                || freeSpots >= filter.freeSpotsMin),
                        "groupSize" to (filter.groupSizeRange == null
                                ||
                                parseRange(travel.groupSize).first <= filter.groupSizeRange!!.last
                                && parseRange(travel.groupSize).last >= filter.groupSizeRange!!.first
                                ),
                    )


                    checks.forEach { (name, passed) ->
                        Log.d("FilterDebug", "Trip ${travel.id} – $name → $passed")
                    }

                    checks.all { it.second }
            })
            .distinctUntilChanged()
    }

    private fun parseRange(rangeStr: String): IntRange {
        val range = rangeStr.split("-")
        if (range.size == 1){
            val single = range[0].trim().toInt()
            return single..single
        }

        val (low, high) = range.map { it.trim().toInt() }
        return low..high
    }


    private fun parseAgeRange(ageRange: String): IntRange {
        val (low, high) = ageRange
            .split("-")
            .map { it.trim().toInt() }
        return low..high
    }


    private fun calculateDurationDays(startDate: LocalDate, endDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(startDate, endDate).toInt()
    }

    // to visualize pending applications notifications
    fun getPendingApplicationsForOwner(userId: String): Flow<List<Pair<Travel, Participant>>> {
        return repository.getPendingApplicationsForOwner(userId)
    }


}


