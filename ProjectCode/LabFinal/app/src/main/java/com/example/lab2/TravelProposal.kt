package com.example.lab2

import UserRepository
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.lab2.ui.theme.DarkGreen
import com.example.lab2.ui.theme.GreenBackground
import com.example.lab2.ui.theme.GreenDivider
import com.example.lab2.ui.theme.PopupBg
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.Dp
import com.example.lab2.UserStore.getUserModelById
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.ZoneId

@Composable
fun TravelProposalScreen(
    navController: NavHostController,
    travelId: String,
    onNavigateToDuplicateTravel: (String) -> Unit,
    onNavigateToSuitcase: () -> Unit,
    onNavigateToItems: () -> Unit,
    vm: TravelViewModel = viewModel(factory = Factory),
    pvm: PackingViewModel = viewModel(),
    UserVm: ProfileViewModel   = viewModel(factory = Factory),
    onNavigateToLogin: () -> Unit,
    authVm: AuthViewModel
) {
    val context = LocalContext.current.applicationContext
    var showInfo by remember { mutableStateOf(false)}
    var showApplyPopup by remember { mutableStateOf(false) }
    //val proposals by UserVm.proposals.collectAsState()
    var showSuccessPopup by remember { mutableStateOf(false) }

    LaunchedEffect(travelId) {
        vm.loadReviews(travelId)
        vm.getParticipantsForTravel(travelId)
        vm.getTravelById(travelId)

    }
    val participants by vm.participants.collectAsState()
    val proposal by vm.currentTravel.collectAsState(initial = null)
    val reviews by vm.reviews.collectAsState()
    val refTitle by vm.refTitle.collectAsState()



    //val participants by vm.participants.collectAsState()
    //Log.d("participants: ", "vm.participants in view: ${participants}")

    /*for (p in participants) {
        proposal!!.participants[p.key] = p.value
    }*/

    if (proposal == null) {
        // loading / error state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...", style = MaterialTheme.typography.bodyMedium)
        }
        return
    } else {

        val userId = UserVm.id
        val username = UserVm.username.collectAsState().value
        val profileImage by UserVm.profileImage.collectAsState()
        val dateRange = proposal!!.dateRange.first.toDate() to proposal!!.dateRange.second.toDate()

        LaunchedEffect(Unit) {
            Log.d("DEBUG_PROFILE", "Profile image = $profileImage")
        }

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val isPast = dateRange.second.before(Date())


        val isParticipant = participants.contains(userId)
        val canReview = isPast && isParticipant
        //val oldTrips by UserVm.oldTrips.collectAsState()

        val (participantsTot, setParticipantsTot) = remember { mutableIntStateOf(0) }


        val authState by authVm.authState.collectAsState()

        // 2) Helper: either go to destination or to Login
        fun navigateOrLogin(destination: String) {
            if (authState is AuthViewModel.AuthState.Guest) {
                navController.navigate(Screen.Login.base)
            } else {
                navController.navigate(destination)
            }
        }


        Scaffold(
            //topBar = topBar,
            topBar = {
                TopBar(
                    onNavigateToL = { navController.popBackStack() },
                    onNavigateToR = {
                        navigateOrLogin(Screen.Notifications.base)
                    },
                    page         = "Travel Proposal"
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                FABRow(
                    vm,
                    pvm,
                    navController = navController,
                    userId,
                    username,
                    travelId,
                    proposal!!,
                    dateRange,
                    participantsTot,
                    onApplyClick = { groupCount ->
                        showApplyPopup = true
                        navController.navigate(
                            Screen.AddParticipantsInfo.routeWithTravelIdCount(travelId, groupCount)
                        )
                    },
                    onNavigateToDuplicateTravel,
                    onShareClick = { /* share logic or popup*/ },
                    onNavigateToLogin = onNavigateToLogin,
                    authVm = authVm,
                    onNavigateToSuitcase = onNavigateToSuitcase,
                    onNavigateToItems = onNavigateToItems,
                )
            },
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
                //.padding(16.dp)
                //.then(Modifier.padding(innerPadding)), // this makes sure content respects system insets without doubling bottom padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showSuccessPopup) {
                    SuccessPopup(onDismiss = {showSuccessPopup = false})
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = proposal!!.title,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(8.dp)
                    )

                    StaticAverageRatingView(reviews)
                }


                // Travel image carousel
                ImagesCarousel(proposal!!.images)

                // LINK AD ALTRO VIAGGIO
                if (proposal!!.referencedTravel != null) {
                    val aString = buildAnnotatedString {
                        append("Based on: ")

                        // clickable
                        pushStringAnnotation(
                            tag = "LINK",
                            annotation = "" // PLACEHLDER
                        )
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF0B3E28),
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(refTitle)
                        }
                        pop()
                    }

                    Text(
                        aString,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .clickable{
                                navController.navigate(
                                    Screen.TravelProposal.routeWithTravelId(proposal!!.referencedTravel!!)
                                )
                            },
                        textAlign = TextAlign.Right
                    )
                }

                //participants tab and heart button row
                ParticipantsAndHeartRow(
                    navController        = navController,
                    travelId             = travelId,
                    ownerId              = proposal!!.owner.first,
                    vm                   = UserVm,
                    participants         = participants,
                    participantsTot      = participantsTot,
                    setParticipantsTot   = setParticipantsTot,
                    authVm = authVm
                    //pendingApplications  = proposal!!.pendingApplications
                )

                HorizontalDivider(color = Color(0xFF0B3E28))

                //Organizer
                OrganizerRow(proposal!!.owner, navController, authVm = authVm )

                HorizontalDivider(color = Color(0xFF0B3E28))

                Spacer(modifier = Modifier.height(24.dp))

                // Travel Description
                DescriptionBox(proposal!!.description)

                Spacer(modifier = Modifier.height(24.dp))

                //Tags
                TagsRow(proposal!!.tags)

                Spacer(Modifier.padding(8.dp))

                Text(
                    text = "Itinerary",
                    style = MaterialTheme.typography.headlineSmall
                )
                HorizontalDivider(color = Color(0xFF0B3E28))

                // Itinerary carousel
                ItineraryCarousel(proposal!!.itinerary)

                Spacer(Modifier.padding(8.dp))

                // Information
                if (!showInfo) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column {
                            Text(
                                text = "See more Informations",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = { showInfo = !showInfo }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Show the informations"
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF0B3E28))

                    Spacer(Modifier.padding(16.dp))
                }
                //See more
                else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column {
                            Text(
                                text = "Information",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = { showInfo = !showInfo }
                        ) {
                            Icon(
                                //imageVector = Icons.Default.AddCircleOutline,
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "Show the informations"
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF0B3E28))

                    Spacer(Modifier.padding(16.dp))

                    InformationCard(
                        dateRange,
                        proposal!!.price,
                        proposal!!.groupSize,
                        participants,
                        proposal!!.ageRange,
                        proposal!!.locations,
                        proposal!!.activities,
                        participantsTot
                    )
                }

                //Reviews

                if (isPast) {
                    Spacer(Modifier.padding(16.dp))

                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    HorizontalDivider(color = Color(0xFF0B3E28))

                    //Reviews
                    if (reviews.isEmpty()) {
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
                                text = "No reviews yet",
                                fontSize = 18.sp
                            )
                        }
                    } else {
                        ReviewsCarousel(reviews, vm)
                    }

                    val myUserRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                    val hasAlreadyReviewed = reviews.any { it.reviewerId?.id == myUserRef.id }

                    var showReviewDialog by remember { mutableStateOf(false) }

                    if (showReviewDialog) {
                        AddReviewDialogWithPhotos(
                            userProfileImageUrl = profileImage,
                            onDismiss = { showReviewDialog = false },
                            onSubmit = { rating, text, images ->
                                vm.addReview(
                                    travelId,
                                    text,
                                    rating,
                                    images.map { TravelImage.UriImage(it.toString()) },
                                    context
                                )
                                showReviewDialog = false
                            }
                        )
                    }

                    val isOwner = proposal!!.owner.first == userId
                    val isParticipant = participants.contains(userId)

                    if (!isOwner && isParticipant) {
                        Button(
                            onClick = {
                                if (!hasAlreadyReviewed) showReviewDialog = true
                            },
                            enabled = !hasAlreadyReviewed,
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!hasAlreadyReviewed) Color(0xFF60935D) else Color.Gray,
                                contentColor = Color.White,
                                disabledContainerColor = Color.LightGray,
                                disabledContentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Add review",
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.padding(16.dp))

                //Forum
                Text(
                    text = "Forum",
                    style = MaterialTheme.typography.headlineSmall
                )
                HorizontalDivider(color = Color(0xFF0B3E28))

                Spacer(Modifier.padding(16.dp))


                val forumVM: ForumViewModel = viewModel(factory = Factory)
                ForumCard(proposal!!, forumVM, userId, navController, authVm) //UserVm.id
                Spacer(Modifier.padding(25.dp))

            }


        }

    }
}

@Composable
fun ImagesCarousel(images: List<TravelImage>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(images.size) { index ->
            Box(
                modifier = Modifier
                    .width(350.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x5860935D)),
                contentAlignment = Alignment.Center
            ) {

                val travelImage = images[index]
                when (travelImage) {

                    is TravelImage.Resource -> {
                        Image(
                            painter = painterResource(id = travelImage.resId),
                            contentDescription = "Image $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is TravelImage.UriImage -> {
                        Image(
                            painter = rememberImagePainter(travelImage.uri),
                            contentDescription = "Image $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is TravelImage.RemoteUrl -> {
                        Image(
                            painter = rememberImagePainter(travelImage.url),
                            contentDescription = "Image $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ParticipantsAndHeartRow(
    navController: NavHostController,
    travelId: String,
    ownerId: String,
    vm: ProfileViewModel,
    participants: Map<String, Pair<Participant, Boolean?>>,
    participantsTot: Int,
    setParticipantsTot: (Int) -> Unit,
    authVm: AuthViewModel,

    //pendingApplications: List<Participant>,
)
{   val enabledParticipants = participants.filterValues{it.second == true}
    var additionalNumber = 0
    val pendingApplications = participants.filterValues{it.second == null}

    // Recompute additional participants whenever the map changes
    LaunchedEffect(participants) {
        var additional = 0
        enabledParticipants.values.forEach { (participant, _) ->
            additional += participant.additionalParticipants?.size ?: 0
        }
        setParticipantsTot(enabledParticipants.size + additional)
    }

    Row(
        modifier = Modifier
            .padding(bottom = 10.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconSize = 40.dp
        val newReq = pendingApplications.size
        val overlap = (-12).dp


        if (participants.isEmpty()) {
            Text("0 participating")
        } else {
            // faccio sovrapporre le icone avatar
            //considero solo i participants enabled
            val first3Participants = enabledParticipants.entries.take(3)
            first3Participants.forEachIndexed { index, (userId, pair) ->
                val participant = pair.first

                val userModel by produceState<UserModel?>(initialValue = null, userId) {
                    value = getUserModelById(userId)
                }

                val img by userModel?.profileImage?.collectAsState(initial = null)
                    ?: remember { mutableStateOf(null) }

                val authState by authVm.authState.collectAsState()


                Box( // contenitore senza clip
                    modifier = Modifier
                        .zIndex(index.toFloat())
                        .offset(x = overlap * index)
                        .size(iconSize)
                        .clickable {
                            if (authState is AuthViewModel.AuthState.Guest) {
                                navController.navigate(Screen.Login.base)
                            } else {
                                navController.navigate(
                                    Screen.Participants.routeWithTravelId(travelId)
                                )
                            }
                        }


                ) {
                    Box( // avatar cerchiato
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        when (img) {
                            is UserImage.UriImage -> Image(
                                painter = rememberAsyncImagePainter((img as UserImage.UriImage).uri),
                                contentDescription = "Profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            is UserImage.UrlImage -> Image(
                                painter = rememberAsyncImagePainter((img as UserImage.UrlImage).url),
                                contentDescription = "Profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            is UserImage.Resource -> Image(
                                painter = painterResource((img as UserImage.Resource).resId),
                                contentDescription = "Default profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            else -> Text(
                                text = participant.username.firstOrNull()?.uppercase() ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (index == (first3Participants.size - 1) && newReq > 0 && participantsTot <= 3
                        && ownerId == vm.id) {  //only owner sees the red circle for new user requests
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                //.offset(x = 2.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

        }

        if (participantsTot > 3) {

            Box(
                modifier = Modifier
                    .offset(x = overlap * 3)
                    .zIndex(3f)
                    .size(iconSize)
                    .wrapContentSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                OutlinedButton(
                    onClick = {
                        navController.navigate(
                            Screen.Participants.routeWithTravelId(travelId)
                        )
                    },
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor   = Color.Black
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+${participantsTot - 3}", style = MaterialTheme.typography.bodySmall)
                }
                if (newReq > 0 && ownerId == vm.id) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .offset(x = 2.dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f)) // spinge il cuore a destra
        if (ownerId != vm.id) {

            val favoriteTrips by vm.favorites.collectAsState()

            val isFavorite = favoriteTrips.any { it.id == travelId }
            val authState by authVm.authState.collectAsState()

            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite
                else Icons.Default.FavoriteBorder,
                contentDescription = "Add to favorites",
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
                    .clickable {
                        if (authState is AuthViewModel.AuthState.Guest) {
                            // Guest → force login
                            navController.navigate(Screen.Login.base)
                        } else {
                            // Authenticated → toggle favorite
                            if (!isFavorite) vm.addFavorite(travelId)
                            else vm.removeFavorite(travelId)
                        }
                    },
                tint = Color.Red
            )
        }
    }
}


@Composable
fun OrganizerRow(
    owner: Pair<String, String>,
    navController: NavHostController,
    chatViewModel: ChatViewModel = viewModel(factory = Factory),  // or hiltViewModel()
    authVm: AuthViewModel
) {
    val ownerModel by produceState<UserModel?>(initialValue = null, owner.first) {
        value = getUserModelById(owner.first )
    }

    val img by ownerModel?.profileImage?.collectAsState(initial = null)
        ?: remember { mutableStateOf<UserImage?>(null) }

    val authState by authVm.authState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)

                .clickable {

                    if (authState is AuthViewModel.AuthState.Guest) {
                        // redirect guest to login
                        navController.navigate(Screen.Login.base)
                    } else {
                        // authenticated → go to that user’s profile
                        navController.navigate(Screen.OtherUserProfile.routeWithUserId(owner.first))
                    }


                },
            contentAlignment = Alignment.Center
        ){
            when (img) {
                is UserImage.UriImage -> {
                    Image(
                        painter            = rememberAsyncImagePainter((img as UserImage.UriImage).uri),
                        contentDescription = "Organizer picture",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                }
                is UserImage.UrlImage -> {
                    Image(
                        painter            = rememberAsyncImagePainter((img as UserImage.UrlImage).url),
                        contentDescription = "Organizer picture",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                }
                is UserImage.Resource -> {
                    Image(
                        painter            = painterResource((img as UserImage.Resource).resId),
                        contentDescription = "Default organizer picture",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                }
                else -> {
                    Text(
                        text  = owner.second.firstOrNull()?.uppercase() ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(25.dp))
        val authState by authVm.authState.collectAsState()

        Column {
            Text(
                text = "Organizer",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                modifier = Modifier.clickable {
                    if (authState is AuthViewModel.AuthState.Guest) {
                        navController.navigate(Screen.Login.base)
                    } else {
                        navController.navigate("otherUserProfile/${owner.first}")
                    }
                },
                text = owner.second,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))


        val scope = rememberCoroutineScope()

        IconButton(
            onClick = {
                if (authState is AuthViewModel.AuthState.Guest) {
                    // Guest → force login
                    navController.navigate(Screen.Login.base)
                } else {
                    // Authenticated → do the chat flow
                    scope.launch {
                        // 1️⃣ get or create the chat doc
                        val chatId = chatViewModel.getOrCreateChatWith(owner.first)
                        // 2️⃣ navigate into your chat screen
                        navController.navigate(Screen.Chat.routeWithChatId(chatId))
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Chat With the Organizer",
                tint = GreenDivider
            )
        }
    }
}

@Composable
fun DescriptionBox(description: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            //.height(50.dp)
            .clip(RoundedCornerShape(10.dp)) //ritaglia solo il contenuto disegnato dopo!
            .background(Color(0x5860935D))
            .border(
                width = 1.dp,
                color = Color(0x5860935D),
                shape = RoundedCornerShape(11.dp)
            ),
    ) {
        if (description != null) {
            Column (
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(description, modifier = Modifier.padding(8.dp))
            }
        }
        else {
            Text("No description")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsRow(tags: List<String>?) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags?.forEach { tag ->
            Box(
                modifier = Modifier
                    .background(Color(0xFF60935D), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text(text = tag, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ItineraryCarousel(itinerary: List<String>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(itinerary.size) { index ->
            Box(
                modifier = Modifier
                    .width(350.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp)) //ritaglia solo il contenuto disegnato dopo!
                    .background(Color(0x5860935D)),
                //contentAlignment = Alignment.Center
            ) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(text = "Day ${ index + 1}", fontWeight = FontWeight.Bold)
                    }

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(text = itinerary[index])
                    }
                }
            }
        }
    }
}

@Composable
fun InformationCard(
    dateRange: Pair<Date, Date>,
    price: String,
    groupSize: String, participants: Map<String, Pair<Participant, Boolean?>>,
    ageRange: String, locations: List<Triple<String, String, Boolean>>,
    activities: List<Pair<String, Boolean>>,
    participantsTot: Int) {
    Card(
        modifier = Modifier
            //.width(400.dp),
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "DATES",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0B3E28),
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                val startDate = dateRange.first.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val firstDay = startDate.dayOfMonth.toString()
                val endDate = dateRange.second.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val lastDay = endDate.dayOfMonth.toString() + " " + endDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + endDate.year.toString()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x5860935D))
                        .padding(8.dp)
                ) {
                    Text("$firstDay - $lastDay")
                }

            }

            // Age and Price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                //AGE
                Column(
                    modifier = Modifier.weight(.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "SUGGESTED AGE",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0B3E28),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x5860935D))
                            .padding(8.dp)
                    ) {
                        Text(ageRange)
                    }
                }

                //PRICE
                Column(
                    modifier = Modifier.weight(.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "PRICE",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0B3E28),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x5860935D))
                            .padding(8.dp)
                    ) {
                        Text("$price €")
                    }
                }
            }

            // Group size and remaining spots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // GROUP SIZE
                Column(
                    modifier = Modifier.weight(.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "GROUP SIZE",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0B3E28),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x5860935D))
                            .padding(8.dp)
                    ) {
                        Text(groupSize)
                    }
                }

                // FREE SPOTS
                Column(
                    modifier = Modifier.weight(.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //controllo se il groupSize è in formato "3" o "3-8"
                    val spots = groupSize.split('-')
                    val maxSpots = if (spots.size == 2) spots[1].trim().toInt() else spots[0].toInt()
                    val freeSpots = maxSpots - participantsTot
                    Text(
                        "FREE SPOTS",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0B3E28),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0x5860935D))
                            .padding(8.dp)
                    ) {
                        Text(freeSpots.toString())
                    }
                }
            }

            Spacer(Modifier.padding(8.dp))

            //LOCATIONS
            Text(
                "LOCATIONS",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0B3E28),
                modifier = Modifier.padding(top = 10.dp)
            )
            Spacer(Modifier.padding(6.dp))

            LocationTable(locations)

            Spacer(Modifier.padding(10.dp))

            //ACTIVITIES
            Text(
                "GROUP ACTIVITIES",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0B3E28),
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.padding(6.dp))

            ActivitiesTable(activities)

            Spacer(Modifier.padding(8.dp))
        }
    }
}

@Composable
fun LocationTable(locations: List<Triple<String, String, Boolean>>) {
    // Table structure
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "DATE",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0B3E28),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Text(
                "CITY",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0B3E28),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Text(
                "NIGHT STAY",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0B3E28),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }

        //Data Rows
        locations.forEach { (date, city, nightStay) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val dateText = date.split("/")[0] + "/" + date.split("/")[1]
                Text(
                    dateText, //LocalDate.parse(date).format(formatter),
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
                Text(
                    city,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
                if (nightStay) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Overnight",
                        tint = Color(0xFF1F3D2D),
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                } else {
                    Text(
                        "",
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivitiesTable(activities: List<Pair<String, Boolean>>) {
    // Table structure
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
    ) {
        // Header Row for the table
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "ACTIVITY",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0B3E28),
                modifier = Modifier
                    .weight(1f) // Make each column of te same weight
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                "MANDATORY",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0B3E28),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
        }

        activities.forEach { (activity, mandatory) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    activity,
                    modifier = Modifier
                        .weight(1f) // Make the activity column flexible
                        .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
                )

                // mandatory icon or empty text
                if (mandatory) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mandatory",
                        tint = Color(0xFF1F3D2D),
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.CenterHorizontally) // Center text horizontally
                    )
                } else {
                    Text(
                        "",
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.CenterHorizontally) // Center text horizontally
                    )
                }
            }
        }
    }
}

@Composable
fun AddReviewDialogWithPhotos(
    userProfileImageUrl: UserImage?,
    onDismiss: () -> Unit,
    onSubmit: (Int, String, List<Uri>) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            selectedImages = selectedImages + uris
        }
    }

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
                        style = MaterialTheme.typography.titleLarge,
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { i ->
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star $i",
                                tint = Color(0xFFFFD700)
                            )
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

                // Image previews
                if (selectedImages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add photo",
                                modifier = Modifier.size(40.dp),
                                tint = Color.DarkGray
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedImages.size) { index ->
                            Image(
                                painter = rememberAsyncImagePainter(selectedImages[index]),
                                contentDescription = "Selected image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                                    .clickable { galleryLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add another photo",
                                    tint = Color.DarkGray
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        onSubmit(rating, reviewText, selectedImages)
                        onDismiss()
                    },
                    enabled = rating > 0,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF60935D),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text("Submit", color = Color.White)
                }

            }
        }
    }
}
@Composable
fun StaticAverageRatingView(reviews: List<Review>) {
    val avgRating = if (reviews.isNotEmpty()) {
        val sum = reviews.sumOf { it.rating }
        val average = sum.toDouble() / reviews.size
        String.format("%.1f", average)
    } else {
        "0.0"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating star",
            tint = Color(0xFFFEE421),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = avgRating,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF0B3E28)
        )
    }
}

@Composable
fun ReviewsCarousel(reviews: List<Review>,vm: TravelViewModel) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(reviews) { review ->
            val userImage by produceState<UserImage?>(initialValue = null, review.userImage) {
                value = review.userImage?.let { vm.getUserImageFromRef(it) }
            }
            val reviewerName by produceState(initialValue = "Loading...", key1 = review.reviewerUsername) {
                value = review.reviewerUsername?.let { vm.getUsernameFromRef(it) } ?: "Unknown"
            }

            Column(
                modifier = Modifier
                    .width(250.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (userImage) {
                        is UserImage.UriImage -> {
                            AsyncImage(
                                model = (userImage as UserImage.UriImage).uri,
                                contentDescription = "Reviewer Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                        is UserImage.UrlImage -> {
                            AsyncImage(
                                model = (userImage as UserImage.UrlImage).url,
                                contentDescription = "Reviewer Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }

                        is UserImage.Resource -> {
                            Image(
                                painter = painterResource(id=(userImage as UserImage.Resource).resId),
                                contentDescription = "Reviewer Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }

                        null -> {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reviewerName.uppercase().take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = reviewerName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = formatDate(review.date?.toDate()?.time ?: System.currentTimeMillis()),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "⭐".repeat(review.rating),
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = review.description,
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (review.images.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(review.images) { img ->
                            AsyncImage(
                                model = (img as TravelImage.RemoteUrl).url,
                                contentDescription = "Review image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}



fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun ForumCard(
    proposal: Travel,
    vm: ForumViewModel,
    userId: String,
    navController: NavHostController,
    authVm: AuthViewModel
    )
{
    val ownerId = proposal.owner.first
    val travelId = proposal.id
    val current = vm.currentUserId
    val isOwner = current == ownerId

    val answersMap by vm.answersMap.collectAsState()
    val authState by authVm.authState.collectAsState()

    LaunchedEffect(travelId) {
        vm.startListening(travelId)
    }

    val questions by vm.questions.collectAsState()


    Column(modifier = Modifier.fillMaxWidth()) {

        if (questions.isEmpty()) {
            Text(
                text = if (isOwner) {
                    "No questions yet. Your travelers can ask here!"
                } else {
                    "Be the first to ask a question!"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(questions) { q ->
                    Column {
                        // ─── User’s question ─────────────────────────────────
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RemoteAvatar(userId = q.askerId, size = 32.dp)
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(2.dp, Color(0xFF60935D), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Text(q.text)
                            }
                        }
                        Spacer(Modifier.height(4.dp))

                        // ─── Owner’s answer (if any) ───────────────────────────
                        val answersForThis = answersMap[q.id].orEmpty()
                        answersForThis.forEach { ans ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 250.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x5860935D))
                                        .padding(12.dp)
                                ) {
                                    Text(ans.text)
                                }
                                Spacer(Modifier.width(8.dp))
                                RemoteAvatar(userId = ans.answererId, size = 28.dp)
                            }
                            Spacer(Modifier.height(4.dp))

                        }

                        // ─── Answer input (only if owner AND unanswered) ──────
                        if (isOwner) {
                            var draft by remember { mutableStateOf("") }
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = draft,
                                    onValueChange = { draft = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Type your reply…") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF60935D),
                                        unfocusedBorderColor = Color(0xFF60935D)
                                    )
                                )
                                IconButton(onClick = {
                                    vm.postAnswer(travelId, q.id, draft.trim())
                                    draft = ""
                                }) {
                                    Icon(Icons.Filled.Send, contentDescription = "Send")
                                }
                            }
                        }
                    }
                }
            }


        }


        if (!isOwner) {
            var draftQuestion by remember { mutableStateOf("") }
            OutlinedTextField(
                value = draftQuestion,
                onValueChange = { draftQuestion = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Ask a question…") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF60935D),
                    unfocusedBorderColor = Color(0xFF60935D)
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        if (authState is AuthViewModel.AuthState.Guest) {
                            // Guest → force login
                            navController.navigate(Screen.Login.base)
                        } else {
                            // Authenticated → send question
                            if (draftQuestion.isNotBlank()) {
                                vm.askQuestion(travelId, draftQuestion.trim())
                                draftQuestion = ""
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Send, contentDescription = "Ask")
                    }
                }
            )
        }
    }

}


@Composable
fun RemoteAvatar(
    userId: String,
    size: Dp
) {
    // Hold the downloaded URL
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Fire off a one-time fetch
    LaunchedEffect(userId) {
        val snap = FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()
        // try both keys, whichever your documents actually have:
        imageUrl = snap.getString("image") ?: snap.getString("imageUrl")
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            Image(
                painter            = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun FABRow(
    vm: TravelViewModel,
    pvm: PackingViewModel,
    navController : NavHostController,
    userId: String,
    travelId: String,
    username: String,
    proposal: Travel,
    dateRange: Pair<Date, Date>,
    participantsTot: Int,
    onApplyClick: (groupCount: Int) -> Unit,
    onNavigateToDuplicateTravel: (String) -> Unit,
    onShareClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authVm: AuthViewModel,
    onNavigateToSuitcase: () -> Unit,
    onNavigateToItems: () -> Unit,
) {

    var showSharePopup by remember { mutableStateOf(false) }
    var showApplyPopup by remember { mutableStateOf(false) }
    var showSuccessPopup by remember { mutableStateOf(false) }
    val participants by vm.participants.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .offset(y = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val acceptedParticipants = participants.filter { (_, pair) -> pair.second == true }.toMap()

        // Slot sinistro (Suitcase)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (userId in acceptedParticipants.keys || userId == proposal.owner.first) {
                FloatingActionButton(
                    onClick = {
                                onNavigateToSuitcase()
                    },
                    shape = CircleShape,
                    containerColor = PopupBg
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Luggage,
                        contentDescription = "Pack the suitcase",
                        tint = DarkGreen
                    )
                }
            }
        }

        // Slot centrale (Apply)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (userId != proposal.owner.first &&
                isAtLeastAWeekFromNow(dateRange.first) &&
                userId !in participants.keys) {

                FloatingActionButton(
                    onClick = { showApplyPopup = true },
                    modifier = Modifier.width(100.dp),
                    shape = RoundedCornerShape(30.dp),
                    containerColor = Color(0xFF60935D),
                    elevation = FloatingActionButtonDefaults.elevation()
                ) {
                    Text(
                        text = "Apply",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Slot destro (Share)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            FloatingActionButton(
                onClick = { showSharePopup = true },
                shape = CircleShape,
                containerColor = PopupBg
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share Trip",
                    tint = DarkGreen
                )
            }
        }

        // Popups (fuori dalla Row)
        if (showSharePopup) {
            ShareOptionsPopup(
                proposal = proposal,
                onDismiss = { showSharePopup = false },
                onNavigateToDuplicateTravel = { id ->
                    showSharePopup = false
                    onNavigateToDuplicateTravel(id)
                },
                navController = navController,
                authVm = authVm
            )
        }

        if (showApplyPopup) {
            ApplyPopup(
                proposal        = proposal,
                vm              = vm,
                userId          = userId,
                username        = username,
                participantsTot = participantsTot,
                onDismiss       = { showApplyPopup = false },
                onNavigateToApply = { groupCount ->
                    onApplyClick(groupCount)
                },
                onApply = {
                    showSuccessPopup = true
                },
                onNavigateToLogin = onNavigateToLogin,
                authVm = authVm
            )
        }
    }
}

@Composable
fun ShareOptionsPopup(
    proposal: Travel,
    onDismiss: () -> Unit,
    onNavigateToDuplicateTravel: (String) -> Unit,
    onCopyLink: (String) -> Unit = {},
    onSendWhatsapp: (String) -> Unit = {},
    navController: NavHostController,
    authVm: AuthViewModel,
) {
    val authState by authVm.authState.collectAsState()

    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                //.width(300.dp)
                //.height(150.dp)
                .padding(8.dp)
                .border(1.dp, PopupBg, RoundedCornerShape(20.dp))
                .background(PopupBg, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 8.dp
        ) {
            Row ( modifier = Modifier
                //.fillMaxWidth()
                //.height(150.dp)
                .background(PopupBg)
                .padding(8.dp)
                .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                //Import Trip
                Column (
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (authState is AuthViewModel.AuthState.Guest) {
                                navController.navigate(Screen.Login.base)
                            } else {
                                onNavigateToDuplicateTravel(proposal.id)
                                onDismiss()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Import Trip",
                        tint = GreenDivider,
                        modifier = Modifier
                            .background(GreenBackground.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)

                    )
                    Text(text = "Create new trip from\nthis one", textAlign = TextAlign.Center, color = Color.Black)

                }

                //Copy Link
                /*Column (
                    modifier = Modifier
                        .weight(1f)
                        .clickable { },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy Link",
                        tint = GreenDivider,
                        modifier = Modifier
                            .background(GreenBackground.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                            .padding(4.dp)
                    )
                    Text("Copy Link", textAlign = TextAlign.Center)
                }*/

                //Sent via Whatsapp
                /*Column (
                    modifier = Modifier
                        .weight(1f)
                        .clickable { },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        imageVector = Icons.Default.Whatsapp,
                        contentDescription = "Send via Whatsapp",
                        tint = GreenDivider,
                        modifier = Modifier
                            .background(GreenBackground.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    )
                    Text("Send via Whatsapp", textAlign = TextAlign.Center)
                }*/
            }
        }
    }
}

@Composable
fun ApplyPopup(
    proposal: Travel,
    vm: TravelViewModel,
    userId: String,
    username: String,
    participantsTot: Int,
    onDismiss: () -> Unit,
    onNavigateToApply: (groupCount: Int) -> Unit,
    onApply: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authVm: AuthViewModel
) {
    val authState by authVm.authState.collectAsState()
    val isLoggedIn = authState is AuthViewModel.AuthState.Success

    LaunchedEffect(authState) {
        Log.d("AUTHSTATE_POPUP", "Auth state in ApplyPopup: $authState")
    }

    var selectedOption by remember { mutableStateOf("Just me") }
    var selectedCount by remember { mutableIntStateOf(2) }
    var showDropdown by remember { mutableStateOf(false) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    val group = proposal.groupSize.split('-')

        val maxSpots = if (group.size != 1) group[1].trim().toInt()
        else group[0].trim().toInt()

    val freeSpots = maxSpots - participantsTot

    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = PopupBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Who is joining?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkGreen
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }

                if(isLoggedIn) {
                    // Options
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedOption == "Just me",
                                onClick = { selectedOption = "Just me" },
                                colors = RadioButtonDefaults.colors(selectedColor = DarkGreen)
                            )
                            Text("Just me")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedOption == "Group",
                                onClick = { selectedOption = "Group" },
                                colors = RadioButtonDefaults.colors(selectedColor = DarkGreen)
                            )
                            Text("We are")

                            Spacer(modifier = Modifier.width(8.dp))

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GreenBackground,
                                    modifier = Modifier
                                        .clickable { showDropdown = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$selectedCount",
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = showDropdown,
                                    onDismissRequest = { showDropdown = false },
                                    offset = DpOffset(12.dp, 670.dp),
                                    modifier = Modifier
                                        .height(110.dp)
                                        .width(44.dp)
                                        .background(PopupBg)
                                ) {
                                    (2..freeSpots).forEach { count ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedCount = count
                                                showDropdown = false
                                            },
                                            content = { Text("$count") }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))
                            Text("people")
                        }
                    }
                    val coroutineScope = rememberCoroutineScope()
                    // Apply Button
                    Button(
                        onClick = {

                            if (selectedOption == "Just me") {
                                vm.applyToTravel(proposal.id, userId, username)
                                onApply() //show SuccessPopup
                            } else {
                                //vm.peopleNumber = selectedCount
                                onNavigateToApply(selectedCount) //non si conta il logged user
                            }

                            onDismiss()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply")
                    }
                }
                else {
                    // Se è in Guest
                    Text(
                        "To apply, please ",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Row (
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ){
                        Text(
                            text = "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkGreen,
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                    onNavigateToLogin()
                                }
                        )
                    }
                }
            }
        }
    }

}

fun isAtLeastAWeekFromNow(date: Date): Boolean {
    return try {
        val inputDate = date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(today, inputDate)
        daysBetween >= 7
    } catch (e: Exception) {
        false
    }
}

@Composable
fun SuccessPopup(
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.TopCenter,
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            color = PopupBg, // light green
            shape = RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Application request successfully sent!",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}