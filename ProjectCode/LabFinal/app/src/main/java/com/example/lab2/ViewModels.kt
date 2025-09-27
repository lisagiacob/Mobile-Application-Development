package com.example.lab2

import PackingRepository
import UserRepository
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.lab2.Factory.currentUserId
import com.example.lab2.Factory.travelRepository
import com.example.lab2.Factory.userRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.Date


class TravelViewModel(
    private val model: TravelModel,
    private val currentUser: UserModel,
    private val repository: FirestoreTravelRepository
) : ViewModel() {

    private val _proposals: MutableStateFlow<List<Travel>> = MutableStateFlow(emptyList())
    val proposals: StateFlow<List<Travel>> = _proposals

    private val _bookedTrips: MutableStateFlow<List<Travel>> = MutableStateFlow(emptyList())
    val bookedTrips: StateFlow<List<Travel>> = _bookedTrips

    private val _refTitle: MutableStateFlow<String> = MutableStateFlow("")
    val refTitle: StateFlow<String> = _refTitle


    private val _currentTravel = MutableStateFlow<Travel?>(null)
    val currentTravel: StateFlow<Travel?> = _currentTravel

    private val _participants = MutableStateFlow<Map<String, Pair<Participant, Boolean?>>>(emptyMap())
    val participants: StateFlow<Map<String, Pair<Participant, Boolean?>>> = _participants

    fun getParticipantsForTravel(travelId: String) {
        viewModelScope.launch {
            repository.getParticipantsForTravel(travelId)
                .collect { map ->
                    _participants.value = map
                }
        }
    }

    fun getTravelById(id: String) {
        viewModelScope.launch {
            model.getTravelById(id)
                .collect { t ->
                    t?.let {
                        _currentTravel.value = t //updatedTravel
                        if (t.referencedTravel != null) {
                            _refTitle.value = repository.getRefTitle(t.referencedTravel)
                        }
                    }

                }
        }
    }

    //add participants info temp properties
    var newNames = mutableStateListOf<String>()
    var newSurnames = mutableStateListOf<String>()
    var newBirthDates =  mutableStateListOf<String>()
    var newNumbers = mutableStateListOf<String>()

    var peopleNumber by mutableStateOf(0)  //number of additional participants

    //add participants info temp errors
    var nameError by mutableStateOf<String?>(null)
        private set

    var surnameError by mutableStateOf<String?>(null)
        private set

    var numberError by mutableStateOf<String?>(null)
        private set

    var dateError by mutableStateOf<String?>(null)
        private set

    private fun validateName(name: String) {
        nameError = if (name.isBlank()) "Name must not be empty" else null
    }

    private fun validateSurname(surname: String) {
        surnameError = if (surname.isBlank()) "Surname must not be empty" else null
    }

    private fun validateNumber(number: String) {
        val regex = Regex(
            pattern = "^\\+([1-9]\\d{0,2}) ?(?:\\(?\\d{1,4}\\)? ?)?[\\d\\s.-]{5,}$"
        )

        numberError = if (number.isBlank()) "Number must not be empty"
        else if(!regex.matches(number.trim())) "Number is not valid"
        else null
    }

    private fun validateBirthDate(date: String) {
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        if (date.isBlank()) {
            dateError = "Date must not be empty"
        }
        else {
            try {
                val birthDate = sdf.parse(date)
                if (birthDate != null) {
                    dateError = if (date.isBlank()) "Date must not be empty"
                    else if (birthDate > currentDate) "Date cannot be in the future"
                    else null
                }

            } catch (e: ParseException) {
                dateError = "Wrong date format"
            }
        }
    }

    fun updateName(index: Int, name: String) {
        newNames[index] = name
        validateName(name)
    }

    fun updateSurname(index: Int, surname: String) {
        newSurnames[index] = surname
        validateSurname(surname)
    }

    fun updateNumber(index: Int, number: String) {
        newNumbers[index] = number
        validateNumber(number)
    }

    fun updateBirthDate(index: Int, date: String) {
        newBirthDates[index] = date
        validateBirthDate(date)
    }

    fun isInfoValid(): Boolean {
        for (i in 0 until peopleNumber - 1) {
            validateName(newNames[i])
            validateSurname(newSurnames[i])
            validateBirthDate(newBirthDates[i])
            validateNumber(newNumbers[i])
        }
        return nameError == null &&
                surnameError == null &&
                dateError == null &&
                numberError == null
    }

    fun saveParticipantsInfo(travelId: String) {
        if (isInfoValid()) {
            // Update the underlying model with the temporary values.
            val participants = mutableListOf<AdditionalParticipant>()
            for (i in 0 until peopleNumber - 1) {
                val p = AdditionalParticipant(newNames[i], newSurnames[i], newBirthDates[i], newNumbers[i])
                participants.add(p)

            }
            viewModelScope.launch {
                try {
                    repository.addPendingApplication(
                        travelId = travelId,
                        participant = Participant(currentUser.id, currentUser.username.value, participants)
                    )
                }
                catch (e: Exception) {
                    Log.e("addPendingApplicationError: ", e.toString())
                }
            }

            // Clear the temporary values
            newNames.clear()
            newSurnames.clear()
            newBirthDates.clear()
            newNumbers.clear()

        }
    }

    fun updateProposal(travel: Travel, context: Context) {
        viewModelScope.launch {
            try{
                repository.updateTravel(travel, context)
            } catch (e: Exception) {
                Log.e("updateTravelError", e.toString())
            }
        }
    }

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    fun loadReviews(travelId: String) {
        viewModelScope.launch {
            repository.getReviewsForTravel(travelId)
                .catch { emit(emptyList()) }
                .collectLatest { list ->
                    Log.d("DEBUG", "Collected ${list.size} reviews")
                    _reviews.value = list
                }
        }
    }

    fun addReview(travelId: String, description: String, rating: Int, images: List<TravelImage>,
                  context: Context) {
        val newReview = Review(
            description = description,
            rating = rating,
            date = Timestamp.now(),
            reviewerId = Firebase.firestore.document("users/${currentUser.id}"),
            reviewerUsername =Firebase.firestore.document("users/${currentUser.id}"),
            userImage = Firebase.firestore.document("users/${currentUser.id}"),

            images = images.toMutableList()
        )

        viewModelScope.launch {
            try {
                repository.addReviewToTravel(travelId, newReview, context)
            } catch (e: Exception) {
                Log.e("addReviewError", e.toString())
            }
        }
    }

    suspend fun getUserImageFromRef(ref: DocumentReference): UserImage? {
        val doc = ref.get().await()
        val imageUrl = doc.getString("image")
        return imageUrl?.let { UserImage.UrlImage(it) }
    }

    suspend fun getUsernameFromRef(ref: DocumentReference): String {
        val doc = ref.get().await()
        return doc.getString("username") ?: "Unknown"
    }

    fun applyToTravel(travelId: String, userId: String, username: String, additionalParticipants: List<AdditionalParticipant>? = null) {
        viewModelScope.launch {
            repository.addPendingApplication(travelId, Participant(userId, username, additionalParticipants))
        }
    }

    fun enableParticipant(travelId: String, participant: Participant) {
        viewModelScope.launch {
            model.setParticipantEnabled(travelId, participant.userId, true)
        }
    }

    //Avvia una coroutine che osserva continuamente i partecipanti del viaggio con ID travelId.
    fun disableParticipant(travelId: String, participant: Participant) {
        viewModelScope.launch {
            model.setParticipantEnabled(travelId, participant.userId, false)
        }
    }

    fun observeParticipantsForTravel(travelId: String) {
        viewModelScope.launch {
            launch {
                repository.getParticipantsForTravel(travelId).collect { map ->
                    _participants.value = map
                }
            }

            launch {
                val travel = repository.getTravelById(travelId)
                _currentTravel.value = travel
            }
        }
    }

    // Delegate delete into the VM
    fun deleteTravelById(travelId: String) {
        viewModelScope.launch {
            //remove from db
            repository.deleteTravel(travelId)
            //remove from local model
            model.deleteTravel(travelId)
        }

    }

    /*private val _pendingApplications = MutableStateFlow<List<Pair<Travel, Participant>>>(emptyList())
    val pendingApplications: StateFlow<List<Pair<Travel, Participant>>> = _pendingApplications

    fun observePendingApplications(userId: String) {
        viewModelScope.launch {
            model.getPendingApplicationsForOwner(userId)
                .collect {
                    _pendingApplications.value = it
                }
        }
    }*/

    fun proposalsFor(userId: String): StateFlow<List<Travel>> {
        val proposals = model.getProposalsForUser(userId)
        viewModelScope.launch {
            proposals.collect {
                _proposals.value = it
            }
        }
        return proposals.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    /* Emits only the travels for the given user that have already ended. */
    fun oldTripsFor(userId: String): StateFlow<List<Travel>> {

        return combine(proposals, bookedTrips) { props, booked ->
            // merge, filter to those whose end date is before today, then sort newest first
            (props + booked)
                .filter { travel ->
                    travel.dateRange.second.toDate().before(Date())
                }
                .sortedByDescending { travel ->
                    travel.dateRange.first.toDate()
                }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }
}



class OtherUserProfileVmFactory(
    private val userId: String,
    private val travelModel: TravelModel,
    private val signedInUserId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val otherUser = runBlocking { UserStore.getUserById(userId) }
            ?: throw IllegalArgumentException("No user with id=$userId")

        return ProfileViewModel(
            userRepository = userRepository,  // <— new
            model         = otherUser,
            travelModel   = travelModel,
            isCurrentUser = (userId == signedInUserId)
        ) as T
    }
}



object Factory : ViewModelProvider.Factory{
    //private val userModel: UserModel = UserModel()
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val userRepository = UserRepository(db)
    private val travelRepository = FirestoreTravelRepository(db)
    val travelModel = TravelModel(travelRepository)

    private var currentUserId: String? = null

    fun setCurrentUserId(uid: String) {
        currentUserId = uid
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {


        return when{

            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel() as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                val uid = currentUserId
                    ?: throw IllegalStateException(
                        "Factory: you must call setCurrentUserId() before creating ProfileViewModel"
                    )

                val user: UserModel = runBlocking {
                    userRepository.fetchUserById(uid).toUserModel()
                }

                @Suppress("UNCHECKED_CAST")
                ProfileViewModel(
                    userRepository = userRepository,
                    model          = user,
                    travelModel    = travelModel,
                    isCurrentUser  = true
                ) as T
            }


            // modelClass.isAssignableFrom(UserViewModel::class.java) ->
            //     ProfileViewModel(userModel) as T
            modelClass.isAssignableFrom(TravelViewModel::class.java) -> {
                val uid = currentUserId
                    ?: throw IllegalStateException("Factory: no userId for TravelViewModel")
                val user = runBlocking {
                    userRepository.fetchUserById(uid).toUserModel()
                }
                @Suppress("UNCHECKED_CAST")
                TravelViewModel(travelModel, user, travelRepository) as T
            }


            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                val uid = currentUserId ?: ""
                @Suppress("UNCHECKED_CAST")
                SearchViewModel(
                    userStore = UserStore,
                    currentUserId = uid
                ) as T
            }

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                val uid = currentUserId
                    ?: throw IllegalStateException("Factory: no userId for HomeViewModel")
                val user = runBlocking {
                    userRepository.fetchUserById(uid).toUserModel()
                }
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(travelModel, user, travelRepository) as T
            }

            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ChatViewModel(
                    firestore = FirebaseFirestore.getInstance(),
                    auth      = FirebaseAuth.getInstance()
                ) as T
            }


            modelClass.isAssignableFrom(ForumViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ForumViewModel(
                    firestore = FirebaseFirestore.getInstance(),
                    auth      = FirebaseAuth.getInstance()
                ) as T
            }

            modelClass.isAssignableFrom(FilterViewModel::class.java) ->
                FilterViewModel(travelModel) as T

            modelClass.isAssignableFrom(AddTravelViewModel::class.java) -> {
                val uid = currentUserId
                    ?: throw IllegalStateException("Factory: no userId for AddTravelViewModel")
                val user = runBlocking {
                    userRepository.fetchUserById(uid).toUserModel()
                }
                @Suppress("UNCHECKED_CAST")
                AddTravelViewModel(travelModel, user, travelRepository) as T
            }

            modelClass.isAssignableFrom(ChatListViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ChatListViewModel(
                    firestore = FirebaseFirestore.getInstance(),
                    auth      = FirebaseAuth.getInstance()
                ) as T
            }
            //else -> throw IllegalArgumentException("Unknown ViewModel")
            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")


        }
    }


}


class SearchViewModel(
    private val userStore: UserStore,
    private val currentUserId: String

) : ViewModel() {

    // MutableStateFlow holding the current query
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // MutableStateFlow holding the list of search results.
    private val _searchResults = MutableStateFlow<List<UserModel>>(emptyList())
    val searchResults: StateFlow<List<UserModel>> = _searchResults.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(300) // add debouncing to reduce rapid UI updates
                .distinctUntilChanged()
                .collect { currentQuery ->
                    _searchResults.value = performSearch(currentQuery)
                }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    // Searches for users whose first name, last name, or username contains the query
    private suspend fun performSearch(query: String): List<UserModel> {
        val allUsers = userStore.getAllUsers()
        return if (query.isBlank()) {
            listOf()

        } else {
            val lowerQuery = query.lowercase()
            allUsers.filter { user ->
                user.id != currentUserId && (
                        user.firstName.value.lowercase().contains(lowerQuery) ||
                                user.lastName.value.lowercase().contains(lowerQuery) ||
                                user.username.value.lowercase().contains(lowerQuery)
                        )
            }
        }
    }
}


data class TravelFilter(
    val tags: Set<String> = emptySet(),
    val startDate: Date? = null,
    val endDate: Date? = null,
    val ageRange: IntRange? = null,
    val status: TravelStatus? = null,
    val durationRange: IntRange? = null,
    val priceRange: ClosedFloatingPointRange<Double>? = null,
    val freeSpotsMin: Int? = null,
    val groupSizeRange: IntRange? = null,
    val locationQuery: String = ""

)

enum class TravelStatus { AVAILABLE, ENDED }

class FilterViewModel(
    private val travelModel: TravelModel
) : ViewModel() {

    private val _filter = MutableStateFlow(TravelFilter())
    val filter: StateFlow<TravelFilter> = _filter

    val filteredTravels: StateFlow<List<Travel>> =
        _filter
            .flatMapLatest { travelModel.getFilteredTravels(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    fun updateFilter(transform: TravelFilter.() -> TravelFilter) {
        _filter.value = _filter.value.transform()
    }
}




class AddTravelViewModel (
    private val model: TravelModel,
    private val currentUser: UserModel,
    private val repository: FirestoreTravelRepository): ViewModel() {

    //STEP1
    private val _travelTitle = MutableStateFlow("")
    val travelTitle: StateFlow<String> = _travelTitle

    private val _travelDescription = MutableStateFlow("")
    val travelDescription: StateFlow<String> = _travelDescription


    fun setTravelTitle(title: String) {
        _travelTitle.value = title
    }

    fun setTravelDescription(description: String) {
        _travelDescription.value = description
    }

    private val _imageUris = mutableStateListOf<Uri>()
    val imageUris: List<Uri> get() = _imageUris

    var imageUrls: MutableList<TravelImage> = mutableStateListOf()

    fun setImageUris(uris: List<Uri>) {
        _imageUris.clear()
        _imageUris.addAll(uris)
    }

    var tags = mutableStateListOf<String>()

    fun addTag(tag: String) {
        if (!tags.contains(tag)) {
            tags.add(tag)
        }
    }

    fun removeTag(index: Int) {
        if (index in tags.indices) {
            tags.removeAt(index)
        }
    }

    //STEP2

    var startDate by mutableStateOf<Date?>(null)
    var endDate by mutableStateOf<Date?>(null)
    var ageRange by mutableStateOf(18f..100f)
    var groupSize by mutableStateOf("")

    fun updateStartDate(date: Date) {
        startDate = date
    }

    fun updateEndDate(date: Date) {
        endDate = date
    }

    fun updateAgeRange(range: ClosedFloatingPointRange<Float>) {
        ageRange = range
    }

    fun updateGroupSize(size: String) {
        groupSize = size
    }

    // STEP3
    var locationName by mutableStateOf("")
    var selectedDate by mutableStateOf("")
    var overnightStay by mutableStateOf(false)

    var locations = mutableStateListOf<Triple<String, String, Boolean>>()

    fun updateLocationName(name: String) {
        locationName = name
    }

    fun updateSelectedDate(date: String) {
        selectedDate = date
    }

    fun updateOvernightStay(value: Boolean) {
        overnightStay = value
    }

    fun addLocationEntry() {
        if (locationName.isNotBlank() && selectedDate.isNotBlank()) {
            locations.add(Triple(selectedDate, locationName, overnightStay))
            locationName = ""
            selectedDate = ""
            overnightStay = false
        }
    }

    fun removeLocationAt(index: Int) {
        if (index in locations.indices) {
            locations.removeAt(index)
        }
    }

    //STEP4
    var itineraryDescriptions = mutableStateListOf<String>()
        private set

    fun initializeItineraryList(newSize: Int) {
        if (itineraryDescriptions.size < newSize) {
            repeat(newSize - itineraryDescriptions.size) {
                itineraryDescriptions.add("")
            }
        } else if (itineraryDescriptions.size > newSize) {
            for (i in itineraryDescriptions.size - 1 downTo newSize) {
                itineraryDescriptions.removeAt(i)
            }
        }
    }

    fun updateItinerary(index: Int, value: String) {
        if (index in itineraryDescriptions.indices) {
            itineraryDescriptions[index] = value
        }
    }

    // STEP 5
    var activityList = mutableStateListOf<Pair<String, Boolean>>()

    fun addActivity(activity: String, isGroup: Boolean) {
        activityList.add(activity to isGroup)
    }

    fun removeActivity(index: Int) {
        if (index in activityList.indices) {
            activityList.removeAt(index)
        }
    }

    // STEP 6
    var price by mutableStateOf("")
    var notIncludedItems = mutableStateListOf<String>()

    fun updatePrice(newPrice: String) {
        price = newPrice
    }

    fun addNotIncludedItem(item: String) {
        notIncludedItems.add(item)
    }

    fun removeNotIncludedItem(index: Int) {
        if (index in notIncludedItems.indices) {
            notIncludedItems.removeAt(index)
        }
    }

    fun createTravel(context: Context, travelId: String? = null) {

        val imageUriList: MutableList<TravelImage> = _imageUris
            .map { uri -> TravelImage.UriImage(uri.toString()) as TravelImage }
            .toMutableList()
        val imageList: MutableList<TravelImage> = (imageUrls + imageUriList).toMutableList()

        val newTravel = Travel(
            id = "",
            title = travelTitle.value,
            owner = Pair(currentUser.id, currentUser.username.value),
            description = travelDescription.value,
            dateRange = Pair(Timestamp(startDate!!), Timestamp(endDate!!)),
            ageRange = "${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()}",
            price = price,
            groupSize = groupSize,
            locations = locations.toMutableList(), //.map { it.second }.toMutableList(),
            referencedTravel = travelId,  //null per nuovo viaggio, altrimenti è l'id del viaggio esistente da referenziare
            images = imageList,
            tags = tags.toMutableList(),
            itinerary = itineraryDescriptions.toMutableList(),
            activities = activityList.toMutableList()
        )

        viewModelScope.launch {
            repository.addTravel(newTravel, context)
        }
    }

    fun isStep2Valid(): Boolean {
        val ageDefault = 18f..100f
        return startDate != null && endDate != null
                && groupSize.isNotBlank()
                && ageRange != ageDefault
    }

    fun isStep3Valid(): Boolean {
        return locations.isNotEmpty()
    }

    fun isStep4Valid(): Boolean {
        return itineraryDescriptions.all { it.isNotBlank() }
    }

    fun isStep5Valid(): Boolean {
        return activityList.isNotEmpty()
    }

    fun isStep6Valid(): Boolean {
        return price.isNotBlank()
    }

    fun isStep7Valid(): Boolean {
        return tags.isNotEmpty()
    }
    fun resetFields() {
        // STEP1
        _travelTitle.value = ""
        _travelDescription.value = ""
        _imageUris.clear()

        // TAGS (STEP7)
        tags.clear()

        // STEP2
        startDate = null
        endDate = null
        ageRange = 18f..100f
        groupSize = ""

        // STEP3
        locationName = ""
        selectedDate = ""
        overnightStay = false
        locations.clear()

        // STEP4
        itineraryDescriptions.clear()

        // STEP5
        activityList.clear()

        // STEP6
        price = ""
        notIncludedItems.clear()
    }
    fun loadTravelForEditing(travel: Travel) {
        // STEP 1
        _travelTitle.value = travel.title
        _travelDescription.value = travel.description
        _imageUris.clear()
        _imageUris.addAll(
            travel.images
                .filterIsInstance<TravelImage.UriImage>()
                .mapNotNull { it.uri?.let(Uri::parse) }
        )

        imageUrls.addAll(
            travel.images
                .filterIsInstance<TravelImage.RemoteUrl>()
        )

        // STEP 2 - Date, Age, Group size
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        startDate = travel.dateRange.first.toDate()   // sdf.format() //travel.dateRange.first.parseAndFormat(inputFormat)
        endDate = travel.dateRange.second.toDate() //travel.dateRange.second.parseAndFormat(inputFormat)

        ageRange = travel.ageRange
            .split("-")
            .mapNotNull { it.trim().toFloatOrNull() }
            .takeIf { it.size == 2 }
            ?.let { it[0]..it[1] }
            ?: 18f..100f

        groupSize = travel.groupSize

        // STEP 3 - Locations
        locations.clear()
        locations.addAll(travel.locations)

        // STEP 4 - Itinerary
        itineraryDescriptions.clear()
        itineraryDescriptions.addAll(travel.itinerary)

        // STEP 5 - Activities
        activityList.clear()
        activityList.addAll(travel.activities)

        // STEP 6 - Price
        price = travel.price

        // STEP 7 - Tags
        tags.clear()
        tags.addAll(travel.tags)
    }

    private fun String.parseAndFormat(
        outputFormat: SimpleDateFormat
    ): String {
        val possibleFormats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        )

        for (format in possibleFormats) {
            try {
                val parsedDate = format.parse(this)
                if (parsedDate != null) {
                    return outputFormat.format(parsedDate)
                }
            } catch (_: Exception) {}
        }

        return "" // fallback se tutte falliscono
    }



    fun getEditedTravel(original: Travel): Travel {
        val uiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val modelFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

         val saveStart = startDate?.let { Timestamp(it) }

        val saveEnd = startDate?.let { Timestamp(it) }

        val updatedImages = _imageUris.map {
            TravelImage.UriImage(it.toString())
        }.toMutableList()

        return original.copy(
            title = _travelTitle.value,
            description = _travelDescription.value,
            dateRange = saveStart!! to saveEnd!!,
            ageRange = "${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()}",
            groupSize = groupSize,
            locations = locations.toMutableList(),
            itinerary = itineraryDescriptions.toMutableList(),
            activities = activityList.toMutableList(),
            price = price,
            tags = tags.toMutableList(),
            images = (original.images + updatedImages).distinct().toMutableList()  //updatedImages.takeIf { it.isNotEmpty() } ?: original.images
        )
    }
}




class AuthViewModel: ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: FirebaseUser?) : AuthState()
        data class Error(val message: String) : AuthState()
        object Guest : AuthState()
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading

        Log.d("Auth", "Attempting login with email='$email' password='$password'")
        auth.signInWithEmailAndPassword(email, password)
            .addOnFailureListener { Log.e("AuthError", it.localizedMessage) }



        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { _authState.value = AuthState.Success(it) }
            }
            .addOnFailureListener { ex ->
                _authState.value = AuthState.Error(ex.localizedMessage ?: "Unknown error")
            }
    }

    fun signUp(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        dateOfBirth: String,
        fiscalCode: String,
        phone: String
    ) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener


                val profileMap = mapOf(
                    "id"        to user.uid,
                    "firstName" to firstName,
                    "lastName"  to lastName,
                    "username"  to username,
                    "email"     to email,
                    "bio"       to "",
                    "hashtag1"  to "",
                    "hashtag2"  to "",
                    "hashtag3"  to "",
                    "imageUrl"  to "",
                    "dateOfBirth" to dateOfBirth,
                    "fiscalCode"  to fiscalCode,
                    "phoneNumber" to phone
                )

                db.collection("users")
                    .document(user.uid)
                    .set(profileMap)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Success(user)
                    }
                    .addOnFailureListener { ex ->
                        _authState.value = AuthState.Error("Profile save failed: ${ex.localizedMessage}")
                    }
            }
            .addOnFailureListener { ex ->
                _authState.value = AuthState.Error(ex.localizedMessage ?: "Unknown error")
            }
    }
    fun updateUserData(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        dateOfBirth: String,
        fiscalCode: String,
        phone: String
    ) {
        val user = auth.currentUser ?: return
        val updatedProfile = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username,
            "email" to email,
            "dateOfBirth" to dateOfBirth,
            "fiscalCode" to fiscalCode,
            "phoneNumber" to phone
        )

        db.collection("users")
            .document(user.uid)
            .update(updatedProfile)
            .addOnSuccessListener {
                _authState.value = AuthState.Success(user)
            }
            .addOnFailureListener { ex ->
                _authState.value = AuthState.Error("Update failed: ${ex.localizedMessage}")
            }
    }

    fun fetchCurrentUserData(onComplete: (UserModel?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(null)
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.data
                if (data != null) {
                    val user = UserModel(
                        id = uid,
                        firstName = data["firstName"] as? String ?: "",
                        lastName = data["lastName"] as? String ?: "",
                        username = data["username"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        bio = data["bio"] as? String ?: "",
                        hashtag1 = data["hashtag1"] as? String ?: "",
                        hashtag2 = data["hashtag2"] as? String ?: "",
                        hashtag3 = data["hashtag3"] as? String ?: "",
                        image = (data["imageUrl"] as? String)?.let { UserImage.UrlImage(it) },
                        dateOfBirth = data["dateOfBirth"] as? String ?: "",
                        fiscalCode = data["fiscalCode"] as? String ?: "",
                        phone = data["phoneNumber"] as? String ?: ""
                    )
                    onComplete(user)
                } else onComplete(null)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }


    fun updatePassword(newPassword: String, callback: (success: Boolean, errorMsg: String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "User non autenticato")
            return
        }
        user.updatePassword(newPassword)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { ex ->
                callback(false, ex.localizedMessage)
            }
    }

    fun updateUserData(username: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "username" to username,
            "phoneNumber" to phone
        )
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                _authState.value = AuthState.Success(auth.currentUser)
            }
            .addOnFailureListener { ex ->
                _authState.value = AuthState.Error("Update failed: ${ex.localizedMessage}")
            }
    }

    fun continueAsGuest() {
        _authState.value = AuthState.Guest
    }

    fun reset() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

}

data class NotificationModel(
    val id: String,
    val message: String,
    val timestamp: Timestamp,
    val read: Boolean,
    val type: String,
    val relatedTravelId: String? = null,
    val relatedUserId: String? = null,
    val senderId: String? = null
)

class NotificationViewModel() : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _newSystemNotifications = MutableSharedFlow<NotificationModel>(extraBufferCapacity = 10)
    val newSystemNotifications: SharedFlow<NotificationModel> = _newSystemNotifications

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    private val listenerRegistration = MutableStateFlow<ListenerRegistration?>(null)

    private val travelModel by lazy { TravelModel(FirestoreTravelRepository()) }

    val _relatedTravels = MutableStateFlow<Map<String, Travel>>(emptyMap())
    val relatedTravels: StateFlow<Map<String, Travel>> = _relatedTravels

    val _relatedUsers = MutableStateFlow<Map<String, UserModel>>(emptyMap())
    val relatedUsers: StateFlow<Map<String, UserModel>> = _relatedUsers

    //Così non mi provo a ritirare più volte gli stessi user/travel
    private val observedTravelIds = mutableSetOf<String>()
    private val observedUserIds = mutableSetOf<String>()

//    fun observeTravel(travelId: String) {
//        db.collection("travels").document(travelId)
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    Log.e("TravelFlow", "Firestore error: $e")
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    Log.d("ToTravel", "Snapshot id=${snapshot.id}, exists=${snapshot.exists()}, data=${snapshot.data}")
//                    val travel = snapshot.toTravel(snapshot.id)
//                    _relatedTravels.update { currentMap ->
//                        currentMap + (travelId to travel)
//                    }
//                }
//            }
//    }
    fun observeNotifications(userId: String) {
        listenerRegistration.value?.remove()

        val listener = db.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NOTIF_VM", "Errore snapshot: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e("NOTIF_VM", "Snapshot nullo")
                    return@addSnapshotListener
                }

                // Prima calcolo i vecchi ID
                val oldNotificationIds = _notifications.value.map { it.id }.toSet()

                // Poi costruisco la nuova lista
                val list = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val message = doc.getString("message")
                    val timestamp = doc.getTimestamp("timestamp")
                    val read = doc.getBoolean("read")
                    val type = doc.getString("type")

                    if (message == null || timestamp == null || read == null || type == null) {
                        Log.w("NOTIF_VM", " → Campo mancante: la notifica viene scartata")
                        return@mapNotNull null
                    }

                    val relatedTravelId = doc.getString("relatedTravelId")
                    val relatedUserId = doc.getString("relatedUserId")
                    val senderId = doc.getString("senderId")

                    if (relatedTravelId != null && observedTravelIds.add(relatedTravelId)) {
                        observeTravel(relatedTravelId)
                    }
                    if (relatedUserId != null && observedUserIds.add(relatedUserId)) {
                        observeUser(relatedUserId)
                    }
                    if (senderId != null && observedUserIds.add(senderId)) {
                        observeUser(senderId)
                    }

                    NotificationModel(id, message, timestamp, read, type, relatedTravelId, relatedUserId, senderId)
                }

                // Calcolo le nuove notifiche
                val newNotifications = list.filter { !oldNotificationIds.contains(it.id) }

                // Aggiorno lo StateFlow
                _notifications.value = list

                // Emissione delle notifiche di sistema
                newNotifications.forEach { notif ->
                    viewModelScope.launch {
                        _newSystemNotifications.emit(notif)
                    }
                }
            }

        listenerRegistration.value = listener
    }

    fun observeTravel(travelId: String) {
        db.collection("travels").document(travelId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NOTIF_VM", "Errore Travel snapshot: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val travel = snapshot.toTravel(snapshot.id)
                    if (travel != null) {
                        _relatedTravels.update { currentMap ->
                            currentMap + (travelId to travel)
                        }
                    } else {
                        Log.e("NOTIF_VM", "Travel nullo per id=$travelId")
                    }
                }
            }
    }

    fun observeUser(userId: String) {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NOTIF_VM", "Errore User snapshot: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val firestoreUser = snapshot.toObject(FirestoreUser::class.java)
                    if (firestoreUser != null) {
                        val userImage = firestoreUser.image?.let { UserImage.UrlImage(it) }
                        val userModel = firestoreUser.copy(id = userId).toUserModel(image = userImage)
                        _relatedUsers.update { currentMap ->
                            currentMap + (userId to userModel)
                        }
                    } else {
                        Log.e("NOTIF_VM", "User nullo per id=$userId")
                    }
                }
            }
    }

    override fun onCleared() {
        listenerRegistration.value?.remove()
        super.onCleared()
    }

    fun markNotificationAsRead(notificationId: String, userId: String) {
        viewModelScope.launch {
            updateNotificationReadStatus(notificationId, userId, true)
        }
    }

    suspend fun updateNotificationReadStatus(notificationId: String, userId: String, read: Boolean) {
        db.collection("users")
            .document(userId)
            .collection("notifications")
            .document(notificationId)
            .update("read", read)
            .await()
    }

//    private val _relatedTravels = MutableStateFlow<Map<String, Travel?>>(emptyMap())
//    val relatedTravels: StateFlow<Map<String, Travel?>> = _relatedTravels

    fun loadRelatedTravelsForNotifications(notifications: List<NotificationModel>) {
        notifications
            .filter { it.type == "travel_review" && it.relatedTravelId != null }
            .map { it.relatedTravelId!! }
            .distinct()
            .forEach { travelId ->
                observeTravel(travelId)
            }
    }
}






class ChatViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {

    private val chats = firestore.collection("chats")


    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    private var listenerRegistration: ListenerRegistration? = null


    private val _otherUserId = MutableStateFlow<String?>(null)
    val otherUserId: StateFlow<String?> = _otherUserId

    private val _otherUserName  = MutableStateFlow<String>("")
    private val _otherUserImage = MutableStateFlow<String?>(null)

    val otherUserName: StateFlow<String>   = _otherUserName
    val otherUserImage: StateFlow<String?> = _otherUserImage

    val currentUserId: String?
        get() = auth.currentUser?.uid



    suspend fun getOrCreateChatWith(ownerId: String): String {
        val currentUserId = auth.currentUser!!.uid
        // here we’ll query for any chat whose participants exactly match:
        val participants = listOf(currentUserId, ownerId).sorted()
        val existing = chats
            .whereEqualTo("participants", participants)
            .limit(1)
            .get()
            .await()

        return if (existing.documents.isNotEmpty()) {
            existing.documents.first().id
        } else {
            // create new
            val newChat = hashMapOf(
                "participants" to participants,
                "lastMessage" to "",
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "seenBy" to listOf<String>()
            )
            val docRef = chats.add(newChat).await()
            docRef.id
        }
    }


    /** Call this once you know the chatId to start streaming messages */
    fun openChat(chatId: String) {
        // Detach previous listener (if any)
        listenerRegistration?.remove()


            firestore.collection("chats")
                .document(chatId)
                .get()
                .addOnSuccessListener { snap ->
                    val participants =
                        snap.get("participants") as? List<String> ?: return@addOnSuccessListener
                    val me = auth.currentUser!!.uid
                    val other = participants.first { it != me }
                    _otherUserId.value = other

                    // fetch that user’s profile doc once:
                    firestore.collection("users")
                        .document(other)
                        .get()
                        .addOnSuccessListener { userSnap ->
                            _otherUserName.value = userSnap.getString("username") ?: "Unknown"
                            _otherUserImage.value = userSnap.getString("profileImage")
                        }
                }

        // attach the real-time listener for messages
        listenerRegistration = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // TODO: report/log error
                    return@addSnapshotListener
                }
                val msgs = snapshots
                    ?.documents
                    ?.mapNotNull { it.toObject(Message::class.java) }
                    ?: emptyList()
                _messages.value = msgs
            }

    }

    fun sendMessage(chatId: String, text: String) {
        val me = auth.currentUser ?: return
        val msg = hashMapOf(
            "senderId"  to me.uid,
            "text"      to text,
            "timestamp" to FieldValue.serverTimestamp(),
            "seenBy"    to listOf<String>()
        )

        val msgsCol = firestore.collection("chats").document(chatId).collection("messages")
        msgsCol.add(msg)

        firestore.collection("chats")
            .document(chatId)
            //.collection("messages")
            .update(
                "lastMessage", text,
                "lastMessageTimestamp", FieldValue.serverTimestamp()
            )
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }




}



class ChatListViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _previews = MutableStateFlow<List<ChatPreview>>(emptyList())
    val previews: StateFlow<List<ChatPreview>> = _previews

    private var listener: ListenerRegistration? = null

    init {
        val me = auth.currentUser!!.uid
        listener = firestore.collection("chats")
            .whereArrayContains("participants", me)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, _ ->
                val list = snaps?.documents?.mapNotNull { doc ->
                    val participants = doc.get("participants") as List<String>
                    val other = participants.first { it != me }
                    // You’ll want to fetch the other user’s name here (or denormalize it into chats)
                    val lastMsg = doc.getString("lastMessage") ?: ""
                    val ts = doc.getTimestamp("lastMessageTimestamp")
                    ChatPreview(
                        chatId         = doc.id,
                        otherUserId    = other,
                        otherUserName  = doc.getString("otherDisplayName") ?: "Chat",
                        lastMessage    = lastMsg,
                        lastTimestamp  = ts,
                        unseenCount    = 0
                    )
                } ?: emptyList()
                _previews.value = list
            }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}




class ForumViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()


    //map questionId → list of its answers
    private val _answersMap = MutableStateFlow<Map<String, List<Answer>>>(emptyMap())
    val answersMap: StateFlow<Map<String, List<Answer>>> = _answersMap

    private var questionsListener: ListenerRegistration? = null
    private val answersListeners = mutableMapOf<String, ListenerRegistration>()


    var ownerId: String? = null
        private set

    val currentUserId: String? get() = auth.currentUser?.uid
    private var listener: ListenerRegistration? = null

    fun startListening(travelId: String) {
        // fetch ownerId for rule-checking
        firestore.collection("travels").document(travelId)
            .get()
            .addOnSuccessListener { snap ->
                ownerId = snap.getString("ownerId")
            }

        //Listen to questions
        questionsListener?.remove()
        questionsListener = firestore.collection("travels")
            .document(travelId)
            .collection("questions")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snaps, _ ->
                val qs = snaps?.documents
                    ?.mapNotNull { it.toObject(Question::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                _questions.value = qs

                //For each new question, attach an answers listener
                val seenIds = answersListeners.keys
                val newIds  = qs.map { it.id }.toSet()
                // remove listeners for deleted questions
                (seenIds - newIds).forEach { qid ->
                    answersListeners.remove(qid)?.remove()
                }
                // add listeners for brand‐new questions
                (newIds - seenIds).forEach { qid ->
                    val listener = firestore.collection("travels")
                        .document(travelId)
                        .collection("questions")
                        .document(qid)
                        .collection("answers")
                        .orderBy("timestamp", Query.Direction.ASCENDING)
                        .addSnapshotListener { asnaps, _ ->
                            val list = asnaps?.documents
                                ?.mapNotNull { it.toObject(Answer::class.java)?.copy(id = it.id) }
                                ?: emptyList()
                            // merge into the map
                            _answersMap.update { old ->
                                old + (qid to list)
                            }
                        }
                    answersListeners[qid] = listener
                }
            }
    }

    fun askQuestion(travelId: String, text: String) {
        val me = auth.currentUser?.uid ?: return
        val q = mapOf(
            "askerId" to me,
            "text"    to text,
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("travels")
            .document(travelId)
            .collection("questions")
            .add(q)
    }

    fun postAnswer(travelId: String, questionId: String, text: String) {
        val me = auth.currentUser?.uid ?: return
        firestore.collection("travels")
            .document(travelId)
            .collection("questions")
            .document(questionId)
            .collection("answers")
            .add(mapOf(
                "answererId" to me,
                "text"       to text,
                "timestamp"  to FieldValue.serverTimestamp()
            ))
    }

    override fun onCleared() {
        questionsListener?.remove()
        answersListeners.values.forEach { it.remove() }
        super.onCleared()
    }
}

class PackingViewModel(
    private val repository: PackingRepository = PackingRepository()
) : ViewModel() {

    private val _itemStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val itemStates: StateFlow<Map<String, Boolean>> = _itemStates.asStateFlow()

    fun loadItems(userId: String, travelId: String) {
        viewModelScope.launch {
            try {
                val loaded = repository.loadPackedItems(userId, travelId)
                _itemStates.value = loaded
            } catch (e: Exception) {
                Log.e("UIPackingVM", "Failed to load items", e)
            }
        }
    }

    fun toggleItem(item: String, checked: Boolean) {
        _itemStates.update { it.toMutableMap().apply { put(item, checked) } }
    }

    fun addCustomItem(item: String) {
        _itemStates.update { it.toMutableMap().apply { putIfAbsent(item, false) } }
    }

    fun saveItems(userId: String, travelId: String) {
        viewModelScope.launch {
            try {
                repository.savePackedItems(userId, travelId, _itemStates.value)
            } catch (e: Exception) {
                Log.e("UIPackingVM", "Failed to save items", e)
            }
        }
    }

    fun checkIfItemsExist(
        userId: String,
        travelId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val exists = repository.hasPackedItems(userId, travelId)
                onResult(exists)
            } catch (e: Exception) {
                Log.e("PackingVM", "Errore nel controllare se ci sono items", e)
                onResult(false)
            }
        }
    }

    fun ensureSuggestedItemsExist(suggested: List<String>) {
        _itemStates.update { current ->
            val updated = current.toMutableMap()
            suggested.forEach { item ->
                if (!updated.containsKey(item)) {
                    updated[item] = false
                }
            }
            updated
        }
    }

    fun loadItemsWithDefaults(userId: String, travelId: String, suggested: List<String>) {
        viewModelScope.launch {
            try {
                val loaded = repository.loadPackedItems(userId, travelId).toMutableMap()

                // Aggiungi tutti i suggeriti se non già presenti
                suggested.forEach { item ->
                    if (!loaded.containsKey(item)) {
                        loaded[item] = false
                    }
                }

                _itemStates.value = loaded
            } catch (e: Exception) {
                Log.e("PackingVM", "Errore nel caricare items con default", e)
            }
        }
    }
}

data class Question(
    val id: String = "",
    val askerId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val answerText: String? = null,
    val answeredAt: Timestamp? = null,
    val answererId: String? = null
)


data class Answer(
    val id: String = "",
    val answererId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)