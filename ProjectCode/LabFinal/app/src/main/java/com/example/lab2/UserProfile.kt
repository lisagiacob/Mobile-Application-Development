package com.example.lab2

//noinspection UsingMaterialAndMaterial3Libraries,UsingMaterialAndMaterial3Libraries,UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries,UsingMaterialAndMaterial3Libraries

import UserRepository
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.lab2.ui.theme.GreenBackground
import com.example.lab2.ui.theme.GreenButton
import com.example.lab2.ui.theme.GreenDivider
import com.example.lab2.ui.theme.PopupBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


//salvo le variabili come mutableState
//il viewModel sta a parte del lyfecicle dell app, quindi anche se cambiamo qualcosa nel
// lifecycle lui si salva lo stato

//salvo nel viewModel e in una classe a parte perche il lab dice di farlo (ha senso)
//questo mi permette di 'ricordare' tra i cambi di schermata, le modifiche al nome ecc fatte dall'utente



class ProfileViewModel(
    private val userRepository: UserRepository,
    private val model: UserModel,
    private val travelModel: TravelModel,
    val isCurrentUser: Boolean = true
) : ViewModel() {

    init {
        fetchAchievements()
    }

    // Firestore-backed user model state
    private val _userModel = MutableStateFlow(model)
    val userModel: StateFlow<UserModel> = _userModel

    // Expose existing user fields
    val id: String = model.id
    val firstName: StateFlow<String>
        get() = _userModel.map { it.firstName.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.firstName.value)
    val lastName: StateFlow<String>
        get() = _userModel.map { it.lastName.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.lastName.value)
    val username: StateFlow<String>
        get() = _userModel.map { it.username.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.username.value)
    val email: StateFlow<String>
        get() = _userModel.map { it.email.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.email.value)
    val bio: StateFlow<String>
        get() = _userModel.map { it.bio.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.bio.value)
    val hashtag1: StateFlow<String>
        get() = _userModel.map { it.hashtag1.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.hashtag1.value)
    val hashtag2: StateFlow<String>
        get() = _userModel.map { it.hashtag2.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.hashtag2.value)
    val hashtag3: StateFlow<String>
        get() = _userModel.map { it.hashtag3.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.hashtag3.value)
    val profileImage: StateFlow<UserImage?>
        get() = _userModel.map { it.profileImage.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.profileImage.value)
    val achievements: StateFlow<List<String>?>
        get() = _userModel.map { it.achievements.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, model.achievements.value)


    private val _reviews = MutableStateFlow<List<Reviews>>(emptyList())
    val reviews: StateFlow<List<Reviews>> = _reviews

    val proposals: StateFlow<List<Travel>> =
        travelModel.getProposalsForUser(id)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val bookedTrips: StateFlow<List<Travel>> =
        travelModel.getBookedTrips(id)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val oldTrips: StateFlow<List<Travel>> =
        combine(proposals, bookedTrips) { props, booked ->
            (props + booked).filter {
                it.dateRange.second.toDate().before(Date())
            }.sortedByDescending { it.dateRange.first.toDate() }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favorites: StateFlow<List<Travel>> =
        travelModel.getFavoriteTrips(id)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _achievementsTot: MutableStateFlow<List<Achievement>> =
        MutableStateFlow(emptyList())
    val achievementsTot: StateFlow<List<Achievement>> = _achievementsTot


    fun fetchAchievements() {
        viewModelScope.launch {
            val result = userRepository.getAchievements()
            _achievementsTot.value = result
        }
    }

    var newFirstName by mutableStateOf(firstName.value)
        private set
    var newLastName by mutableStateOf(lastName.value)
        private set
    var newUsername by mutableStateOf(username.value)
        private set
    var newEmail by mutableStateOf(email.value)
        private set
    var newBio by mutableStateOf(bio.value)
        private set
    var newHashtag1 by mutableStateOf(hashtag1.value)
        private set
    var newHashtag2 by mutableStateOf(hashtag2.value)
        private set
    var newHashtag3 by mutableStateOf(hashtag3.value)
        private set
    var tempImage by mutableStateOf<UserImage?>(null)
        private set

    var showEditProfile by mutableStateOf(false)
        private set

    var firstNameError by mutableStateOf<String?>(null)
        private set
    var lastNameError by mutableStateOf<String?>(null)
        private set
    var usernameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set

    private val emailRegex = Regex("^[A-Za-z](.*)(@)(.+)(\\.)(.+)")

    fun isFriend(friendId: String): Boolean {
        return resolvedFriends.value.any { it.first == friendId }
    }

    fun addFriend(friendId: String) {
        viewModelScope.launch {
            try {
                // 1) write the two friend docs
                userRepository.addFriendBidirectional(model.id, friendId)

                // 2) refresh the in‑memory list of resolved friends
                refreshResolvedFriends()

                // 3) get the new total number of friends
                val count = userRepository.getFriendCount(model.id)

                // 4) decide if we hit a threshold
                val achId = when (count) {
                    1 -> "f1"
                    5 -> "f5"
                    10 -> "f10"
                    else -> null
                }

                // 5) if so, award it
                achId?.let { addAchievement(it, model.id) }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore aggiungendo amico", e)
            }
        }
    }


    fun addAchievement(achId: String, userId: String) {
        viewModelScope.launch {
            try {
                userRepository.addAchievement(achId, userId)
                //refreshAchievements()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore aggiungendo achievement", e)
            }
        }
    }

    private val _resolvedFriends =
        MutableStateFlow<List<Triple<String, String?, String?>>>(emptyList())
    val resolvedFriends: StateFlow<List<Triple<String, String?, String?>>> = _resolvedFriends

    fun loadFriends() {
        viewModelScope.launch {
            try {
                val resolved = userRepository.getResolvedFriendList(id)
                _resolvedFriends.value = resolved
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore caricamento amici", e)
            }
        }
    }

    fun refreshResolvedFriends() {
        viewModelScope.launch {
            try {
                val updated = userRepository.getResolvedFriendList(model.id)
                _resolvedFriends.value = updated
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore aggiornando lista amici", e)
            }
        }
    }

    fun removeFriend(currentUserId: String, friendUserId: String) {
        viewModelScope.launch {
            try {
                userRepository.removeFriendBidirectional(currentUserId, friendUserId)
                refreshResolvedFriends()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore rimozione amico", e)
            }
        }
    }


    /*fun refreshAchievements() {
        viewModelScope.launch {
            try {
                val updated = userRepository.getUserAchievements(model.id)
                achievements.value = updated
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore aggiornando lista amici", e)
            }
        }
    }*/

    fun addFavorite(travelId: String) {
        viewModelScope.launch {
            userRepository.addFavorite(model.id, travelId)
        }
    }

    fun removeFavorite(travelId: String) {
        viewModelScope.launch {
            userRepository.removeFavorite(model.id, travelId)
        }
    }

    // --- Firestore integration ---
    fun loadUserFromFirestore(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.saveUser(model)
                showEditProfile = false
            } catch (e: Exception) {
                Log.e("ProfileVM", "Failed to save user", e)
            }
        }
    }

    private val _resolvedReviews = MutableStateFlow<List<ResolvedReview>>(emptyList())
    val resolvedReviews: StateFlow<List<ResolvedReview>> = _resolvedReviews

    fun getReviewsForUser(userId: String) {
        viewModelScope.launch {
            try {
                val reviews = userRepository.fetchReviewsForUser(userId)
                if (reviews != null) {
                    _reviews.value = reviews

                    val resolved = userRepository.resolveReviews(reviews)
                    _resolvedReviews.value = resolved

                    val average = resolved.map { it.stars }.average().toInt()
                    _reviewScore.value = average
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore caricamento utente con recensioni", e)
            }
        }

    }

    fun loadUserWithReviews(userId: String) {
        viewModelScope.launch {
            try {
                val updatedUser = userRepository.fetchUserWithReviews(userId)
                if (updatedUser != null) {
                    _userModel.value = updatedUser
                    _reviews.value = updatedUser.reviews.value

                    val resolved = userRepository.resolveReviews(updatedUser.reviews.value)
                    _resolvedReviews.value = resolved

                    val average = resolved.map { it.stars }.average().toInt()
                    _reviewScore.value = average
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore caricamento utente con recensioni", e)
            }
        }
    }


    fun addReviewToUser(
        reviewedUserId: String,
        reviewerId: String,
        reviewText: String,
        stars: Int
    ) {
        viewModelScope.launch {
            try {
                userRepository.addReviewToUser(reviewedUserId, reviewerId, reviewText, stars)
                loadUserWithReviews(reviewedUserId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore aggiungendo recensione", e)
            }
        }
    }

    fun saveUserToFirestore() {
        if (!isCurrentUser) return
        // update local model first
        model.apply {
            storeFirstName(newFirstName)
            storeLastName(newLastName)
            storeUsername(newUsername)
            storeEmail(newEmail)
            storeBio(newBio)
            storeHashtag1(newHashtag1)
            storeHashtag2(newHashtag2)
            storeHashtag3(newHashtag3)
            tempImage?.let(::storeProfileImage)
        }

        viewModelScope.launch {
            try {
                userRepository.saveUser(model)
                showEditProfile = false
            } catch (e: Exception) {
                Log.e("ProfileVM", "Failed to save user", e)
            }
        }
    }


    fun updateShowEditProfile(show: Boolean) {
        if (isCurrentUser) showEditProfile = show
    }

    fun updateFirstName(input: String) {
        if (isCurrentUser) {
            newFirstName = input
            firstNameError = if (input.isBlank()) "First name must not be empty" else null
        }
    }

    fun updateLastName(input: String) {
        if (isCurrentUser) {
            newLastName = input
            lastNameError = if (input.isBlank()) "Last name must not be empty" else null
        }
    }

    fun updateUsername(input: String) {
        if (isCurrentUser) {
            newUsername = input
            usernameError = if (input.isBlank()) "Username must not be empty" else null
        }
    }

    fun updateEmail(input: String) {
        if (isCurrentUser) {
            newEmail = input
            emailError = when {
                input.isBlank() -> "Email must not be empty"
                !emailRegex.matches(input) -> "Invalid email address"
                else -> null
            }
        }
    }

    fun updateBio(input: String) {
        if (isCurrentUser) newBio = input
    }

    fun updateHashtag1(input: String) {
        if (isCurrentUser) newHashtag1 = input
    }

    fun updateHashtag2(input: String) {
        if (isCurrentUser) newHashtag2 = input
    }

    fun updateHashtag3(input: String) {
        if (isCurrentUser) newHashtag3 = input
    }

    fun updateImage(uri: Uri) {
        if (isCurrentUser) tempImage = UserImage.UriImage(uri)
    }

    fun isProfileValid(): Boolean {
        updateFirstName(newFirstName)
        updateLastName(newLastName)
        updateUsername(newUsername)
        updateEmail(newEmail)
        return listOf(firstNameError, lastNameError, usernameError, emailError)
            .all { it == null }
    }

    fun saveProfile(context: Context) {
        if (!isCurrentUser || !isProfileValid()) return

        model.storeFirstName(newFirstName)
        model.storeLastName(newLastName)
        model.storeUsername(newUsername)
        model.storeEmail(newEmail)
        model.storeBio(newBio)
        model.storeHashtag1(newHashtag1)
        model.storeHashtag2(newHashtag2)
        model.storeHashtag3(newHashtag3)
        model.image = tempImage
        tempImage?.let { model.storeProfileImage(it) }

        viewModelScope.launch {
            UserStore.addOrUpdate(model, context)
            val updated = userRepository.fetchUserWithReviews(model.id)
            updated?.let {
                _userModel.value = it
            }
        }

        showEditProfile = false
    }


    private val _reviewScore = MutableStateFlow(model.reviewScore.value)
    val reviewScore: StateFlow<Int> = _reviewScore.asStateFlow()


    fun isOldTrip(travelId: String): Boolean =
        oldTrips.value.any { it.id == travelId }


    // 1. Combine your two trip‐lists into one count flow
    val totalTripsCount: StateFlow<Int> = combine(
        bookedTrips,
        oldTrips
    ) { booked, old ->
        // simply sum their sizes
        booked.size + old.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        // initial value is zero trips
        initialValue = 0
    )


    private val _unlockedIds = MutableStateFlow<List<String>>(emptyList())




    val proposalsCount: StateFlow<Int> = proposals
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )


    val friendsCount: StateFlow<Int> = resolvedFriends
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)


    init {
        refreshAll()
    }



    private fun refreshAll() = viewModelScope.launch {
        delay(500)

        val totalCount = totalTripsCount.value
        val organized = proposalsCount.value
        val fCount = friendsCount.value
        val reviewCount = userRepository.countTravelReviewsByUser(model.id)

        Log.d(
            "PROFILE",
            "After delay: total=$totalCount, props=$organized, friends=$fCount, rev=$reviewCount"
        )

        val currentUnlocked = userRepository.getUserAchievements(model.id).also {
            _unlockedIds.value = it
        }

        listOf(1 to "j1", 5 to "j5", 10 to "j10").forEach { (th, id) ->
            if (totalCount >= th && !currentUnlocked.contains(id)) addAchievement(id, model.id)
        }
        listOf(1 to "o1", 5 to "o5", 10 to "o10").forEach { (th, id) ->
            if (organized >= th && !currentUnlocked.contains(id)) addAchievement(id, model.id)
        }
        listOf(1 to "f1", 5 to "f2", 10 to "f3").forEach { (th, id) ->
            if (fCount >= th && !currentUnlocked.contains(id)) addAchievement(id, model.id)
        }
        listOf(1 to "r1", 5 to "r2", 10 to "r3").forEach { (th, id) ->
            if (reviewCount >= th && !currentUnlocked.contains(id)) addAchievement(id, model.id)
        }

        _unlockedIds.value = userRepository.getUserAchievements(model.id)
        Log.d("PROFILE", "Sweep done; unlocked: ${_unlockedIds.value}")
    }
}




    @Composable
fun OtherUserProfileScreen(
    navController: NavHostController,
    userId: String,
    authVm: AuthViewModel
) {
    val signedInVm: ProfileViewModel = viewModel(factory = Factory)
    var showSettingsMenu by remember { mutableStateOf(false) }

    val otherVm: ProfileViewModel = viewModel(
        key = "OtherProfileVm-$userId",
        factory = OtherUserProfileVmFactory(
            userId           = userId,
            travelModel      = Factory.travelModel,
            signedInUserId   = signedInVm.id
        )
    )

    ProfileScreen(
        navController             = navController,
        vm                        = otherVm,
        signedInVm                = signedInVm,

        topBar = { isMenuVisible, toggleMenu ->
            TopBarUserProfile(
                vm = otherVm,
                navController = navController,
                isSettingsMenuVisible = isMenuVisible,
                authVm = authVm,
                onSettingsMenuToggle = { toggleMenu(it) }
            )

        },

        isOwnProfile              = otherVm.id == signedInVm.id,
        onNavigateToTravelProposal       = { id -> navController.navigate(Screen.TravelProposal.routeWithProposalId(id)) },
        onNavigateToOwnedTravelProposal  = { id -> navController.navigate(Screen.TravelProposal.routeWithProposalId(id)) },
        onNavigateToOtherTravelProposal  = { navController.navigate(Screen.OwnedProposals.routeWithUserId(otherVm.id)) },
        onNavigateToTravelProposalList   = { navController.navigate(Screen.OwnedProposals.routeWithUserId(otherVm.id)) },
        onNavigateToOldTrips = { id ->
            navController.navigate(Screen.OldProposals.routeWithUserId(id))
        },
        onNavigateToBookedTravelList = {
            navController.navigate(Screen.BookedTrips.routeWithUserId(otherVm.id))
        },
        onNavigateToFriendsList = {
            navController.navigate("fullFriendsList/${otherVm.id}")
        } )
}






//ProfileView
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    //userId: String,
    navController: NavHostController,
    onNavigateToTravelProposal: (String) -> Unit,
    onNavigateToOwnedTravelProposal: (String) -> Unit,
    onNavigateToOtherTravelProposal: () -> Unit,
    onNavigateToTravelProposalList: (String) -> Unit,
    onNavigateToOldTrips: (String) -> Unit,
    onNavigateToBookedTravelList: (String) -> Unit,
    vm: ProfileViewModel,
    onNavigateToFriendsList: () -> Unit,
    signedInVm: ProfileViewModel, // current logged user vm (might be a duplicate of the otherr user vm if viewing your own profile)
    //i need it because when i am seeing another user profile i need a reference to my current user profile
    isOwnProfile: Boolean= true,
    topBar: @Composable (Boolean, (Boolean) -> Unit) -> Unit

) {

    LaunchedEffect(vm.id){
        vm.loadFriends()
        signedInVm.refreshResolvedFriends()

        if (isOwnProfile) {
            vm.loadUserWithReviews(vm.id)
        } else {
            vm.getReviewsForUser(vm.id)
        }
    }
    val firstName by vm.firstName.collectAsState()
    val lastName by vm.lastName.collectAsState()
    val initials = (firstName.take(1) + lastName.take(1)).uppercase()
    val email by vm.email.collectAsState()
    val bio by vm.bio.collectAsState()

    val reviews by vm.reviews.collectAsState()
    var showSettingsMenu by remember { mutableStateOf(false) }

    val proposals   by vm.proposals.collectAsState()
    val bookedTrips by vm.bookedTrips.collectAsState()
    Log.d("BookedTrips", "bookedTrips: $bookedTrips")
    val oldTrips    by vm.oldTrips.collectAsState()
    //var showEditProfile by remember { mutableStateOf(false) }
    val resolvedReviews by vm.resolvedReviews.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            topBar(showSettingsMenu) { newValue ->
                showSettingsMenu = newValue
            }
        }
    ) { innerPadding ->
        // UI with profile fields using the ViewModel's state
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //riga con nome e cognome come nel figma
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                horizontalArrangement = Arrangement.Center, //spazio tra i campi
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$firstName $lastName",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                )
                if (isOwnProfile) {
                    // Spazio accanto al nome per la matita (edit)
                    IconButton(onClick = { vm.updateShowEditProfile(true) }) {
                        Icon(
                            imageVector = Icons.Filled.Mode,
                            contentDescription = "Edit Profile",
                            tint = Color(0xFF60935D),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // riga con email
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center, //spazio tra i campi
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(email)
            }

            //riga con immagine e altre cose (viaggi organizzati e tags)
            ImageAndTagsRow(vm, signedInVm, isOwnProfile, initials, proposals.size, oldTrips.size)


            // Friends e achievements
            FriendsAndAchievementsRow(vm, navController, onNavigateToFriendsList)

            //bio section
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                val configuration = LocalConfiguration.current
                val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

                val padding = if (isPortrait) 8.dp else 60.dp
                //val padding = if (this.maxWidth < this.maxHeight) 8.dp else 36.dp // Portrait vs Landscape logic
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = padding, end = padding),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x5860935D))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF60935D),
                                shape = RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bio,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            //proposals start
            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Proposals",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                ) {
                    if (proposals.isNotEmpty()) {
                        Text(
                            text = "See all",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = Color(0xFF007AFF),
                            modifier = Modifier
                                .clickable {
                                    onNavigateToTravelProposalList(vm.id)
                                }
                                .padding(8.dp)
                        )
                    }
                }

            }

            HorizontalDivider(color = Color(0xFF0B3E28))


            // Proposals carousel
            if (proposals.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF0E7E7)), //Color(0x5860935D)
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No proposals yet",
                        fontSize = 18.sp
                    )
                }
            }
            else {

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(proposals, key = { it.id }) { travel ->
                        ProposalCard(
                            travel,
                            onClick = { onNavigateToOwnedTravelProposal(travel.id) }

                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Booked Trips",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(end = 8.dp)
                ) {
                    if (bookedTrips.isNotEmpty()) {
                        Text(
                            text = "See All",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = Color(0xFF007AFF), // light blue
                            modifier = Modifier
                                .clickable {
                                    onNavigateToBookedTravelList(vm.id)
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = Color(0xFF0B3E28))


            if (bookedTrips.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF0E7E7)), //Color(0x5860935D)
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No booked trips yet",
                        fontSize = 18.sp
                    )
                }

            } else {

                // Booked trips carousel

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookedTrips, key = { it.id }) { travel ->
                        ProposalCard(
                            travel,
                            onClick = { onNavigateToTravelProposal(travel.id) }
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Old Trips",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(end = 8.dp)
                ) {
                    if (oldTrips.isNotEmpty()) {
                        Text(
                            text = "See All",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = Color(0xFF007AFF), // light blue
                            modifier = Modifier
                                .clickable {
                                    onNavigateToOldTrips(vm.id)
                                }
                                .padding(8.dp)
                        )
                    }
                }

            }
            HorizontalDivider(color = Color(0xFF0B3E28))


            if (oldTrips.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF0E7E7)), //Color(0x5860935D)
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No old trips yet",
                        fontSize = 18.sp
                    )
                }
            } else {
                // Travel description carousel

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(oldTrips, key = { it.id }) { travel ->
                        ProposalCard(
                            travel,
                            onClick = { onNavigateToTravelProposal(travel.id) },
                            isTravelLog = true
                        )

                    }

                }


                Spacer(modifier = Modifier.height(20.dp))
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reviews",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall                    )
                }


            }
            HorizontalDivider(color = Color(0xFF0B3E28))

            if (reviews.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF0E7E7)), //Color(0x5860935D)
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reviews yet",
                        fontSize = 18.sp
                    )
                }
            } else {

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(resolvedReviews) { review ->
                        Column(
                            modifier = Modifier
                                .width(250.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (review.reviewerImageUrl != null) {
                                    AsyncImage(
                                        model = review.reviewerImageUrl,
                                        contentDescription = "Reviewer Image",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .clickable { navController.navigate(Screen.OtherUserProfile.routeWithUserId(review.reviewerId)) },
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Gray)
                                            .clickable { navController.navigate(Screen.OtherUserProfile.routeWithUserId(review.reviewerId))},
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = review.reviewerUsername.firstOrNull()?.uppercase() ?: "",
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column (
                                    modifier = Modifier.clickable {
                                        // naviga al profilo
                                        navController.navigate(Screen.OtherUserProfile.routeWithUserId(review.reviewerId))
                                    }
                                ) {
                                    Text(
                                        text = review.reviewerUsername,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = review.date.toDate().toString(),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "⭐".repeat(review.stars),
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = review.content,
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }


            val myFirst by signedInVm.firstName.collectAsState()
            val myLast by signedInVm.lastName.collectAsState()
            val myPhoto by signedInVm.profileImage.collectAsState()
            val myUsername = "$myFirst $myLast"

            val resolvedReviews by vm.resolvedReviews.collectAsState()
            val myUserId = signedInVm.id
            val hasAlreadyReviewed = resolvedReviews.any { it.reviewerId == myUserId }

            var showDialog by remember { mutableStateOf(false) }

            val profileImage by vm.profileImage.collectAsState()

            if (showDialog) {
                AddReviewDialog(
                    userProfileImage = profileImage,
                    onDismiss = { showDialog = false },
                    onSubmit = { rating, text ->
                        vm.addReviewToUser(
                            reviewedUserId = vm.id,
                            reviewerId = signedInVm.id,
                            reviewText = text,
                            stars = rating
                        )
                    }
                )
            }

            if (!isOwnProfile) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(top = 12.dp),
                    enabled = !hasAlreadyReviewed,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (!hasAlreadyReviewed) Color(0xFF60935D) else Color.Gray
                    )
                ) {
                    Text("Add review", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Full-screen edit dialog
    if (vm.showEditProfile) {
        EditProfileDialog(
            vm = vm,
            onDismiss = { vm.updateShowEditProfile(false) },
            initials = initials
        )
    }
}

@Composable
fun FriendsAndAchievementsRow(
    vm: ProfileViewModel,
    navController: NavHostController,
    onNavigateToFriendsList: () -> Unit
) {
    //your friends tab
    Row {
        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val padding = if (isPortrait) 8.dp else 60.dp
        var showAchievements by remember { mutableStateOf(false) }
        val resolvedFriends by vm.resolvedFriends.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp, start = padding, end = padding),
            horizontalArrangement = Arrangement.spacedBy(8.dp), //spazio tra i campi
            verticalAlignment = Alignment.CenterVertically
        ) {Text(
            text = "Friends",
            style = MaterialTheme.typography.body1.copy(fontSize = 14.sp),
            modifier = Modifier.padding(top = 8.dp)
        )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    //.fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(resolvedFriends.take(3)) { (friendId, imageUrl, username) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable {
                                navController.navigate(Screen.OtherUserProfile.routeWithUserId(friendId))
                            },
                        contentAlignment = Alignment.Center
                    )
                    {
                        if (imageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = username?.take(2)?.uppercase() ?: friendId.take(2).uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }


                if (resolvedFriends.size > 3) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable {
                                    onNavigateToFriendsList()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+${resolvedFriends.size - 3}", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))  //mtto l'icona degli achievements tutta a destra




            Box(
                modifier = Modifier.size(55.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Achievements",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 8.dp)
                        .clickable {
                            showAchievements = true
                        },
                    tint = Color.Yellow
                )

                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = "Achievements",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 8.dp),
                    tint = Color.Black
                )
            }
            // ACHIEVEMENTS
            if (showAchievements) {
                AchievementsPopup(
                    vm = vm,
                    onDismiss = { showAchievements = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageAndTagsRow(
    vm: ProfileViewModel,
    signedInVm: ProfileViewModel,
    isOwnProfile: Boolean,
    initials: String,
    proposalSize: Int,
    oldSize: Int,
    ) {

    val hashtag1 by vm.hashtag1.collectAsState()
    val hashtag2 by vm.hashtag2.collectAsState()
    val hashtag3 by vm.hashtag3.collectAsState()
    val reviewScore by vm.reviewScore.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 24.dp,
            alignment = Alignment.CenterHorizontally
        ),
    ) {


        //parte con immagine, organized e joined

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Profile image section
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        val img by vm.profileImage.collectAsState()

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            when (val value = img) {
                                is UserImage.UriImage -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(value.uri),
                                        contentDescription = "Profile picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                is UserImage.UrlImage -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(value.url),
                                        contentDescription = "Profile picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                is UserImage.Resource -> {
                                    Image(
                                        painter = painterResource(value.resId),
                                        contentDescription = "Default profile picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                null -> {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.h4,
                                        color = Color.White
                                    )
                                }
                            }

                        }
                    }

                    if (vm.id != signedInVm.id) {
                        val coroutineScope = rememberCoroutineScope()
                        val resolvedFriends by signedInVm.resolvedFriends.collectAsState()
                        val isFriend = resolvedFriends.any { it.first == vm.id }

                        IconButton(
                            onClick = {

                                coroutineScope.launch {
                                    if (isFriend) {
                                        signedInVm.removeFriend(signedInVm.id, vm.id)
                                        vm.removeFriend(vm.id, signedInVm.id)
                                    } else {
                                        signedInVm.addFriend(vm.id)
                                        vm.addFriend(signedInVm.id)
                                    }

                                    signedInVm.refreshResolvedFriends()
                                    vm.refreshResolvedFriends()

                                    val updatedCount = signedInVm.resolvedFriends.value.size
                                    val achievements = mapOf(1 to "f1", 5 to "f2", 10 to "f3")

                                    achievements[updatedCount]?.let { achId ->
                                        if (!signedInVm.achievements.value.orEmpty().contains(achId)) {
                                            signedInVm.addAchievement(achId, signedInVm.id)
                                        }
                                        if (!vm.achievements.value.orEmpty().contains(achId)) {
                                            vm.addAchievement(achId, vm.id)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-6).dp, y = (-6).dp)
                                .size(30.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFriend) Icons.Default.Check else Icons.Default.PersonAdd,
                                contentDescription = if (isFriend) "Amico aggiunto" else "Aggiungi amico",
                                tint = if (isFriend) Color.Gray else Color(0xFF60935D)
                            )
                        }
                    }




                    //Stella in alto a sinistra
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (reviewScore >= 2) { //fill the star if reviewScore is >= 2
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star2 Filled",
                                modifier = Modifier
                                    //.offset(y = (-4).dp) // Spostamento verso l’alto
                                    .size(28.dp),
                                tint = Color(0xFFFEE421)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = "Star2 Outline",
                            modifier = Modifier
                                .size(28.dp),
                            tint = Color(0xFF0B3E28)
                        )
                    }


                    //Stella in alto a destra
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        if (reviewScore >= 4) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star4 Filled",
                                modifier = Modifier
                                    //.offset(y = (-4).dp) // Spostamento verso l’alto
                                    .size(28.dp),
                                tint = Color(0xFFFEE421)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = "Star4 Outline",
                            modifier = Modifier
                                .size(28.dp),
                            tint = Color(0xFF0B3E28)
                        )
                    }

                }

                //Stella in basso a sinistra
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (reviewScore >= 1) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star1 Filled",
                            modifier = Modifier
                                //.offset(y = (-4).dp) // Spostamento verso l’alto
                                .size(28.dp),
                            tint = Color(0xFFFEE421)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = "Star1 Outline",
                        modifier = Modifier
                            .size(28.dp),
                        tint = Color(0xFF0B3E28)
                    )
                }
                //Stella in centro
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (reviewScore >= 3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star3 Filled",
                            modifier = Modifier
                                //.offset(y = (-4).dp) // Spostamento verso l’alto
                                .size(28.dp),
                            tint = Color(0xFFFEE421)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = "Star3 Outline",
                        modifier = Modifier
                            .size(28.dp),
                        tint = Color(0xFF0B3E28)
                    )
                }
                //Stella in basso a destra
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (reviewScore >= 5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star5 Filled",
                            modifier = Modifier
                                //.offset(y = (-4).dp) // Spostamento verso l’alto
                                .size(28.dp),
                            tint = Color(0xFFFEE421)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = "Star5 Outline",
                        modifier = Modifier
                            .size(28.dp),
                        tint = Color(0xFF0B3E28)
                    )
                }
            }
        }

        //riga con organized e joined
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
            //.fillMaxWidth()
        ) {
            Row(
                //modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp) //spazio tra i campi
            ) {
                // Column for the badge and its label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 30.dp )
                            .background(
                                color = Color(0xFF60935D),
                                shape = CircleShape //RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$proposalSize",
                            style = MaterialTheme.typography.body1,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "organized",
                        style = MaterialTheme.typography.body1,
                        color = Color.Black
                    )
                }


                //second line organized
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF60935D),
                                shape = CircleShape //RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$oldSize",
                            style = MaterialTheme.typography.body1,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "joined",
                        style = MaterialTheme.typography.body1,
                        color = Color.Black
                    )
                }
            }
        }

    }


    //hashtag
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {


        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                hashtag1 to vm::updateHashtag1,
                hashtag2 to vm::updateHashtag2,
                hashtag3 to vm::updateHashtag3
            ).forEach { (hashtag, _) ->
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF60935D),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .width(IntrinsicSize.Min)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = hashtag,
                        onValueChange = {},
                        enabled = false,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementsPopup(
    vm: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val userAchievements by vm.achievements.collectAsState()
    val achievementsTot by vm.achievementsTot.collectAsState()

    val achievements = achievementsTot.map {
        if (userAchievements?.contains(it.id) == true) {
            it.copy(enabled = true)
        }
        else {
            it.copy(enabled = false)
        }
    }

    Popup(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(1.dp, Color.White, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            elevation = 8.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Achievements",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenDivider
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GreenDivider,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Divider(color = GreenButton, thickness = 1.dp)



                //elenco di achievements


                LazyColumn (
                    modifier = Modifier.padding(4.dp)
                ){
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                //.height(150.dp)
                                //.padding(4.dp)
                                .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp)),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalArrangement = Arrangement.Center
                        ) {
                            achievements.forEach { (id, title, description, star, enabled) ->
                                val starColor = if (enabled == true) Color.Yellow else Color.Gray
                                val color = if (enabled == true) GreenDivider else Color.Gray

                                val grayscaleFilter = ColorFilter.colorMatrix(
                                    ColorMatrix().apply { setToSaturation(0f) }
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                        .weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp),
                                            //.offset(x = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            //Achievement image
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {

                                                val img = when {
                                                    id.contains("f") -> R.drawable.friendship
                                                    id.contains("j") -> R.drawable.world
                                                    id.contains("o") -> R.drawable.organizer
                                                    id.contains("r") -> R.drawable.review
                                                    else -> R.drawable.world
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(100.dp)
                                                        .clip(CircleShape)
                                                        .border(2.dp, color, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Image(
                                                        painter = painterResource(img),
                                                        contentDescription = "Achievement icon",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                        colorFilter = if (enabled == true) null else grayscaleFilter
                                                    )
                                                }
                                            }
                                            if (star >= 2) {

                                                //Stella in alto a sinistra
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize(),
                                                    contentAlignment = Alignment.TopStart
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Star2 Filled",
                                                        modifier = Modifier
                                                            //.offset(y = (-4).dp) // Spostamento verso l’alto
                                                            .size(28.dp),
                                                        tint = starColor
                                                    )

                                                    Icon(
                                                        imageVector = Icons.Default.StarOutline,
                                                        contentDescription = "Star2 Outline",
                                                        modifier = Modifier
                                                            .size(28.dp),
                                                        tint = Color(0xFF0B3E28)
                                                    )
                                                }

                                                //Stella in alto a destra
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize(),
                                                    contentAlignment = Alignment.TopEnd
                                                ) {

                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Star 3 Filled",
                                                        modifier = Modifier
                                                            //.offset(y = (-4).dp) // Spostamento verso l’alto
                                                            .size(28.dp),
                                                        tint = starColor
                                                    )

                                                    Icon(
                                                        imageVector = Icons.Default.StarOutline,
                                                        contentDescription = "Star 3 Outline",
                                                        modifier = Modifier
                                                            .size(28.dp),
                                                        tint = Color(0xFF0B3E28)
                                                    )
                                                }
                                            }
                                        }
                                        if (star == 1 || star == 3) {
                                            //Stella in centro
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(),
                                                contentAlignment = Alignment.TopCenter
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Star3 Filled",
                                                    modifier = Modifier
                                                        //.offset(y = (-4).dp) // Spostamento verso l’alto
                                                        .size(28.dp),
                                                    tint = starColor
                                                )

                                                Icon(
                                                    imageVector = Icons.Default.StarOutline,
                                                    contentDescription = "Star3 Outline",
                                                    modifier = Modifier
                                                        .size(28.dp),
                                                    tint = Color(0xFF0B3E28)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        title,
                                        fontSize = 16.sp,
                                        color = color,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        description,
                                        fontSize = 16.sp,
                                        color = color,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                        //fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


//popup reviews
@Composable
fun AddReviewDialog(
    userProfileImage: UserImage?,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),

            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Review",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row {
                        (1..5).forEach { i ->
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star $i",
                                    tint = Color(0xFFFFD700) // gold
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Write your review...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Button(
                    onClick = {
                        onSubmit(rating, reviewText)
                        onDismiss()
                    },
                    enabled = rating > 0,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (rating > 0) Color(0xFF60935D) else Color.Gray
                    )
                ) {
                    Text("Submit", color = Color.White)
                }

            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileDialog(
    vm: ProfileViewModel,
    onDismiss: () -> Unit,
    initials: String
) {
    val context = LocalContext.current
    val context1 = LocalContext.current.applicationContext
    var menuExpanded by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showCamera = true
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            localImageUri = it
            vm.updateImage(it)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable(enabled = false, onClick = {})
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .clickable { onDismiss() },
                                tint = Color.Gray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .offset(y = (-20).dp)
                                .zIndex(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                val vmImage by vm.profileImage.collectAsState()
                                when {
                                    localImageUri != null -> Image(
                                        painter = rememberAsyncImagePainter(localImageUri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    vmImage is UserImage.UrlImage -> Image(
                                        painter = rememberAsyncImagePainter((vmImage as UserImage.UrlImage).url),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // … other cases …
                                    else -> Text(initials, style = MaterialTheme.typography.h4, color = Color.White)
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .background(Color(0xFF60935D), CircleShape)
                                    .padding(6.dp)
                                    .clickable { menuExpanded = true }
                            )

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(onClick = {
                                    menuExpanded = false
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        showCamera = true
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }) {
                                    Text("Take Photo")
                                }
                                DropdownMenuItem(onClick = {
                                    menuExpanded = false
                                    galleryLauncher.launch("image/*")
                                }) {
                                    Text("Choose from Gallery")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(60.dp))



                        OutlinedTextField(
                            value = vm.newUsername,
                            onValueChange = { vm.updateUsername(it) },
                            label = { Text("Username") },
                            isError = vm.usernameError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        vm.usernameError?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.caption)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = vm.newBio,
                            onValueChange = { vm.updateBio(it) },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))


                        Text("Tags", style = MaterialTheme.typography.subtitle1)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val hashtagList = listOf(
                                vm.newHashtag1 to vm::updateHashtag1,
                                vm.newHashtag2 to vm::updateHashtag2,
                                vm.newHashtag3 to vm::updateHashtag3
                            )
                            hashtagList.forEach { (text, update) ->
                                var state by remember { mutableStateOf(TextFieldValue(text)) }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF60935D), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = state,
                                            onValueChange = {
                                                state = it
                                                if (it.text.startsWith("#")) update(it.text)
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    if (!state.text.startsWith("#")) {
                                        Text("Must start with #", color = Color.Red, fontSize = 10.sp)
                                    }
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (vm.isProfileValid()) vm.saveProfile(context1)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF60935D)) // verde corretto
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}




// This composable sets up a CameraX preview with ImageCapture
@Composable
fun CameraCapture(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create a PreviewView only once
    val previewView = remember { PreviewView(context) }
    // Hold the ImageCapture instance
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Bind the camera only once using LaunchedEffect
    LaunchedEffect(previewView) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            onError(
                ImageCaptureException(
                    ImageCapture.ERROR_UNKNOWN,
                    "Camera initialization failed", exc
                )
            )
        }
    }

    // Display the preview and capture button overlay
    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                //modifier = Modifier.background(Color(0xFF0B3E28)),

                onClick = {
                    imageCapture?.let { imgCapture ->
                        // Create a temporary file to save the captured image
                        val photoFile = File(
                            context.cacheDir,
                            "CameraXPhoto_${System.currentTimeMillis()}.jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        imgCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    onImageCaptured(Uri.fromFile(photoFile))
                                }
                                override fun onError(exc: ImageCaptureException) {
                                    onError(exc)
                                }
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF0B3E28),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Capture",
                    color = Color.White
                )

            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarUserProfile(
    vm: ProfileViewModel,
    navController: NavHostController,
    showChatIconInsteadOfBack: Boolean = false,
    isSettingsMenuVisible: Boolean,
    authVm: AuthViewModel,
    onSettingsMenuToggle: (Boolean) -> Unit
) {
    val username by vm.username.collectAsState()

    CenterAlignedTopAppBar(
        title = { Text(username) },
        navigationIcon = {
            if (showChatIconInsteadOfBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Go back Home")
                }
            } else {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back Home")
                }
            }
        },
        actions = {
            if (vm.isCurrentUser) {
                Box {
                    IconButton(onClick = { onSettingsMenuToggle(!isSettingsMenuVisible) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Impostazioni",
                            tint = Color(0xFF60935D)
                        )
                    }

                    DropdownMenu(
                        expanded = isSettingsMenuVisible,
                        onDismissRequest = { onSettingsMenuToggle(false) },
                        modifier = Modifier
                            .background(GreenBackground, RoundedCornerShape(8.dp))
                            //.width(200.dp)
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Options",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Divider(color = Color.White, thickness = 1.dp)

                        DropdownMenuItem(onClick = {
                            onSettingsMenuToggle(false)
                            navController.navigate("editProfile")
                        }) {
                            Text("Manage Data", color = Color.Black, fontSize = 14.sp)
                        }

                        DropdownMenuItem(onClick = {
                            onSettingsMenuToggle(false)
                            authVm.logout()
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }) {
                            Text("Logout", color = Color.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun FriendsListScreen(
    navController: NavHostController,
    profileVm: ProfileViewModel,
    signedInUserId: String
) {
    val resolvedFriends by profileVm.resolvedFriends.collectAsState()

    LaunchedEffect(Unit) {
        profileVm.loadFriends()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.h5.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .weight(6f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, Color(0xFF60935D), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resolvedFriends) { (friendId, imageUrl, username) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    navController.navigate(Screen.OtherUserProfile.routeWithUserId(friendId))
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = "Friend Image",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = username?.take(2)?.uppercase() ?: friendId.take(2).uppercase(),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = username ?: friendId,
                                style = MaterialTheme.typography.body1
                            )
                        }

                        if (profileVm.id == signedInUserId) {
                            IconButton(onClick = {
                                profileVm.removeFriend(profileVm.id, friendId)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Friend",
                                    tint = Color.Red
                                )
                            }
                        }

                    }
                }

            }
        }
    }
}
