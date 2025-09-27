package com.example.lab2

/*
oauth client id
1017267840136-tu3k7mteur9cjgm1rhele761t0q3puct.apps.googleusercontent.com


client secret
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


*/

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCbrt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.lab2.ui.auth.LoginScreen
import com.example.lab2.ui.auth.RegisterScreen
import com.example.lab2.ui.theme.DarkGreen
import com.example.lab2.ui.theme.LAb2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //SupabaseManager.initialize()

        //DBSeeder.clearFirestoreDatabase()
        //DBSeeder.seedAllUsers()
        //DBSeeder.seedAllTravels()
        //DBSeeder.seedAllParticipants()
        //DBSeeder

        createNotificationChannel(this)

        setContent {
            LAb2Theme (
                darkTheme = false,
                dynamicColor = false
                ){

                val navController = rememberNavController()

                val authVm: AuthViewModel = viewModel()
                val authState by authVm.authState.collectAsState()

                val notificationVm: NotificationViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                val notifications by notificationVm.notifications.collectAsState()

                // Osserva notifiche solo se utente autenticato (no guest check in base a come li gestisci)
                LaunchedEffect(authState) {
                    if (authState is AuthViewModel.AuthState.Success) {
                        (authState as? AuthViewModel.AuthState.Success)?.user?.uid?.let { userId ->
                            notificationVm.observeNotifications(userId)
                        }
                    }
                }

                // Mostra la prima notifica non letta
                LaunchedEffect(notifications) {
                    val unread = notifications.firstOrNull { !it.read }
                    // Solo per testare
                    //val unread = notifications.firstOrNull()
                    if (unread != null && authState is AuthViewModel.AuthState.Success) {
                        snackbarHostState.showSnackbar(unread.message)

                        // Segna come letta nel DB
                        (authState as? AuthViewModel.AuthState.Success)?.user?.uid?.let { userId ->
                            val unread = notifications.firstOrNull { !it.read }
                            if (unread != null) {
                                snackbarHostState.showSnackbar(unread.message)

                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .collection("notifications")
                                    .whereEqualTo("message", unread.message)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { docs ->
                                        docs.firstOrNull()?.reference?.update("read", true)
                                    }
                            }
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (authState is AuthViewModel.AuthState.Success || authState is AuthViewModel.AuthState.Guest) {
                            BottomNavigationBar(navController)
                        }
                    },
                    modifier = Modifier.background(Color.White)
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        RootNavHost(navController, authVm)
                    }
                }
            }
        }
    }


}




@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun RootNavHost(
    navController: NavHostController,
    authVm: AuthViewModel,
    //profileViewModel: ProfileViewModel
) {
    // at start we are in auth bramch, then after the login / registration we go in the app one
    val authState by authVm.authState.collectAsState()
    val start = when (authState) {
        is AuthViewModel.AuthState.Success -> "app"
        is AuthViewModel.AuthState.Guest   -> "app"
        else                               -> "auth"
    }
    val notificationVm: NotificationViewModel = viewModel()

    NavHost(
        navController,
        startDestination = start) {


        //auth branch for login or registration
        navigation(startDestination = "login", route = "auth") {
            composable("login") {
                LoginScreen(
                    authVm = authVm,
                    onLoginSuccess = { fbUser ->
                        if (fbUser != null) {
                            Factory.setCurrentUserId(fbUser.uid)
                            navController.navigate("app") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            Factory.setCurrentUserId("GUEST")
                            // NON navigare a "app"!
                        }
                    },
                    navToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    authVm = authVm,
                    navController = navController,
                    onRegisterSuccess = { fbUser ->
                        Factory.setCurrentUserId(fbUser.uid)
                        navController.navigate("app") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    navBack = { navController.popBackStack() }
                )
            }
        }

        // APP graph (only active once you've navigated here)
        navigation(startDestination = Screen.Home.base, route = "app") {
            composable(Screen.Home.base) {
                // NOW your factory has a userId, so this ViewModel can be created
                val profileVm: ProfileViewModel = viewModel(factory = Factory)
                LaunchedEffect(Unit) {
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        profileVm.loadUserFromFirestore(it)
                    }
                }
                val authState by authVm.authState.collectAsState()

                fun navigateOrLogin(destination: String) {
                    if (authState is AuthViewModel.AuthState.Guest) {
                        navController.navigate(Screen.Login.base)
                    } else {
                        navController.navigate(destination)
                    }
                }
                HomePageScreen(
                    vm = viewModel(factory = Factory),
                    homeVm = viewModel(factory = Factory),
                    topBar = {
                        TopBar(
                            iconL = "chat",
                            onNavigateToL = {
                                navigateOrLogin(Screen.ChatList.base)
                            },
                            onNavigateToR = {
                                navigateOrLogin(Screen.Notifications.base)
                            },
                            page = "Home"
                        )
                    },
                    onNavigateToTravelProposal = { travelId ->
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                    },
                    onNavigateToOwnedTravelProposal = { travelId ->
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                    },
                    onNavigateToSuggestedTravelProposal = {
                        navController.navigate(Screen.SuggestedTrips.base)
                    },
                    authVm = authVm
                )
            }

            composable(Screen.Profile.base) {
                val currentAuthState = authState
                if (currentAuthState is AuthViewModel.AuthState.Success) {
                    val parentEntry = remember { navController.getBackStackEntry("app") }
                    val profileVm = viewModel<ProfileViewModel>(parentEntry, factory = Factory)
                    var showSettingsMenu by remember { mutableStateOf(false) }

                    ProfileScreen(
                        navController = navController,
                        vm = profileVm,
                        signedInVm = viewModel(factory = Factory),
                        topBar = { isMenuVisible, toggleMenu ->
                            TopBarUserProfile(
                                vm = profileVm,
                                navController = navController,
                                isSettingsMenuVisible = isMenuVisible,
                                authVm = authVm,
                                onSettingsMenuToggle = toggleMenu
                            )
                        },


                                onNavigateToTravelProposal = { travelId ->
                            navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                        },
                        onNavigateToOwnedTravelProposal = { travelId ->
                            navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                        },
                        onNavigateToOtherTravelProposal = {
                            navController.navigate(Screen.OtherUserProposals.base)
                        },
                        onNavigateToTravelProposalList = {
                            navController.navigate(
                                Screen.OwnedProposals.routeWithUserId(profileVm.id)
                            )
                        },
                        onNavigateToOldTrips = { id ->
                            navController.navigate(Screen.OldProposals.routeWithUserId(id)) {
                                launchSingleTop = false
                            }
                        },
                        onNavigateToBookedTravelList = {
                            navController.navigate(Screen.BookedTrips.base)
                        },
                        onNavigateToFriendsList = { navController.navigate("fullFriendsList/${profileVm.id}") }
                    )
                } else {
                    // redirect se guest / loading / error
                    LaunchedEffect(currentAuthState) {
                        navController.navigate(Screen.Login.base) {
                            popUpTo(Screen.Profile.base) { inclusive = true }
                        }
                    }
                }
            }

            composable("fullFriendsList/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("app")
                }

                val signedInVm = viewModel<ProfileViewModel>(parentEntry, factory = Factory)

                val targetVm = if (userId == signedInVm.id) {
                    signedInVm
                } else {
                    viewModel(
                        key = "OtherProfileVm-$userId",
                        factory = OtherUserProfileVmFactory(
                            userId = userId,
                            travelModel = Factory.travelModel,
                            signedInUserId = signedInVm.id
                        )
                    )
                }


                FriendsListScreen(
                    navController = navController,
                    profileVm = targetVm,
                    signedInUserId = signedInVm.id
                )
            }

            composable("editProfile") {
                RegisterScreen(
                    authVm = authVm,
                    onRegisterSuccess = {  },
                    navBack = { navController.popBackStack() },
                    navController = navController,
                    isEditMode = true
                )
            }
            composable("profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                if (userId == null) return@composable

                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("app") }
                val profileVm = viewModel<ProfileViewModel>(parentEntry, factory = Factory)

                ProfileScreen(
                    navController = navController,
                    vm = profileVm,
                    signedInVm = viewModel(factory = Factory),
                    isOwnProfile = false,
                    topBar = { _, _ -> },
                    onNavigateToTravelProposal = { travelId ->
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                    },
                    onNavigateToOwnedTravelProposal = { travelId ->
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                    },
                    onNavigateToOtherTravelProposal = {
                        navController.navigate(Screen.OtherUserProposals.base)
                    },
                    onNavigateToTravelProposalList = {
                        navController.navigate(Screen.OwnedProposals.routeWithUserId(userId))
                    },
                    onNavigateToOldTrips = { id ->
                        navController.navigate(Screen.OldProposals.routeWithUserId(id)) {
                            launchSingleTop = false
                        }
                    },
                    onNavigateToBookedTravelList = {
                        navController.navigate(Screen.BookedTrips.base)
                    },
                    onNavigateToFriendsList = { navController.navigate("fullFriendsList/${profileVm.id}") }

                )
  }


            composable(
                route = Screen.Profile.withFromBottomNavArg(),
                arguments = listOf(
                    navArgument("fromBottomNav") {
                        type = NavType.StringType
                        defaultValue = "false"
                    }
                )
            ) { backStackEntry ->

                val fromBottomNav = backStackEntry.arguments?.getString("fromBottomNav") == "true"

                val currentAuthState = authState
                if (currentAuthState is AuthViewModel.AuthState.Success) {
                    val parentEntry = remember { navController.getBackStackEntry("app") }
                    val profileVm = viewModel<ProfileViewModel>(parentEntry, factory = Factory)
                    var showSettingsMenu by remember { mutableStateOf(false) }

                    ProfileScreen(
                        navController = navController,
                        vm = profileVm,
                        signedInVm = viewModel(factory = Factory),
                        topBar = { isMenuVisible, toggleMenu ->
                            TopBarUserProfile(
                                vm = profileVm,
                                navController = navController,
                                isSettingsMenuVisible = isMenuVisible,
                                authVm = authVm,
                                onSettingsMenuToggle = toggleMenu
                            )
                        },

                        onNavigateToTravelProposal = { travelId ->
                            navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                        },
                        onNavigateToOwnedTravelProposal = { travelId ->
                            navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                        },
                        onNavigateToOtherTravelProposal = {
                            navController.navigate(Screen.OtherUserProposals.base)
                        },
                        onNavigateToTravelProposalList = {
                            navController.navigate(
                                Screen.OwnedProposals.routeWithUserId(profileVm.id)
                            )
                        },
                        onNavigateToOldTrips = { id ->
                            navController.navigate(Screen.OldProposals.routeWithUserId(id)) {
                                launchSingleTop = false
                            }
                        },
                        onNavigateToBookedTravelList = {
                            navController.navigate(Screen.BookedTrips.base)
                        },
                        onNavigateToFriendsList = { navController.navigate("fullFriendsList/${profileVm.id}") }
                    )
                } else {
                    // redirect se guest / loading / error
                    LaunchedEffect(currentAuthState) {
                        navController.navigate(Screen.Login.base) {
                            popUpTo(Screen.Profile.base) { inclusive = true }
                        }
                    }
                }
            }

            //Screen Favorite - intanto metto qua il bottone per Firebase
            /*composable(Screen.Favorites.base) {
                HomeButtonScreen(
                    topBar        = {
                        TopBar(
                            iconL = "chat",
                            onNavigateToL = { navController.navigate(Screen.ChatList.base) },
                            onNavigateToR = { navController.navigate(Screen.Notifications.base)},
                            page = "Participants"
                        )
                    }
                )
            }*/

            // Other‑User Profile Screen
            composable(
                route     = Screen.OtherUserProfile.withUserIdArg(),
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStack ->
                val userId = backStack.arguments!!.getString("userId")!!
                OtherUserProfileScreen(navController, userId, authVm)
            }


            // Participants Screen
            composable(
                route     = Screen.Participants.withTravelIdArg(),
                arguments = listOf(navArgument("travelId") { type = NavType.StringType })
            ) { backStack ->
                val travelId = backStack.arguments!!.getString("travelId")!!
                ParticipantsScreen(
                    navController = navController,
                    travelId      = travelId,
                    topBar        = {
                        TopBar(
                            onNavigateToL = { navController.popBackStack() },
                            onNavigateToR = { navController.navigate(Screen.Notifications.base)},
                            page = "Participants"
                        )
                    }
                )
            }

            // Travel Proposal Screen
            composable(
                route     = Screen.TravelProposal.withProposalIdArg(),
                arguments = listOf(navArgument("travelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val travelId = backStackEntry.arguments!!.getString("travelId")!!
                val pvm = viewModel<PackingViewModel>()






                TravelProposalScreen(
                    navController = navController,
                    travelId      = travelId,
                    onNavigateToDuplicateTravel = { dupId ->
                        navController.navigate(
                            Screen.AddTravel.withOptionalTravelIdArg()
                                .replace("{travelId}", dupId)
                        )
                    },
                    onNavigateToSuitcase = {navController.navigate(Screen.Suitcase.base)},
                    onNavigateToItems = {navController.navigate(Screen.ItemsScreen.base)},
                    pvm = pvm,
                    //vm            = viewModel(factory = Factory),
                    UserVm        = viewModel(factory = Factory),
                    onNavigateToLogin = {
                        navController.navigate("auth") {
                            popUpTo("app") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    authVm = authVm
                )
            }



            // Add Participants Info
            composable(
                route     = Screen.AddParticipantsInfo.withTravelIdCountArg(),
                arguments = listOf(
                    navArgument("travelId") { type = NavType.StringType },
                    navArgument("count")    { type = NavType.IntType }
                )
            ) { entry ->
                val travelId = entry.arguments!!.getString("travelId")
                val count    = entry.arguments!!.getInt("count")
                AddParticipantsInfoScreen(
                    travelId = travelId ?: "",
                    navController = navController,
                    peopleCount  = count

                )
            }


            // Add Travel
            composable(
                route = Screen.AddTravel.withOptionalTravelIdArg(),
                arguments = listOf(
                    navArgument("travelId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStack ->
                val travelId: String? = backStack.arguments?.getString("travelId")
                AddTravelScreen(
                    travelId = travelId,
                    onNavigateToHome = {
                        navController
                            .navigate(Screen.Home.base) {
                                popUpTo(Screen.Home.base) { inclusive = false }
                            }
                    },
                    onNavigateToProfile = {
                        navController
                            .navigate(Screen.Profile.base) {
                                popUpTo(Screen.Home.base)
                            }
                    }
                )
            }


            // Search
            composable(Screen.Search.base) {
                SearchScreen(navController = navController)
            }


            // Edit Travel
            composable(
                route = "editTravel/{travelId}",
                arguments = listOf(
                    navArgument("travelId") { type = NavType.StringType }
                )
            ) { backStack ->
                val travelId = backStack.arguments!!.getString("travelId")!!
                EditTravelScreen(
                    travelId = travelId,
                    onNavigateToProfile = {
                        navController
                            .navigate(Screen.Profile.base) {
                                popUpTo(Screen.Home.base)
                            }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }


            composable(route = Screen.OwnedProposals.withUserIdArg(),
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) {backStackEntry ->
                val parentEntry = remember { navController.getBackStackEntry("app") }
                val profileVm  = viewModel<ProfileViewModel>(parentEntry, factory = Factory)
                val userId = backStackEntry.arguments!!.getString("userId")!!

                val travelVm:  TravelViewModel  = viewModel(factory = Factory)
                OwnedTravelProposalList(
                    profileVm                   = profileVm,
                    travelViewModel             = travelVm,
                    userId                      = userId,
                    topBar = {
                        TopBar(
                            onNavigateToL = { navController.popBackStack() },
                            onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                            page = "Proposals"   //differenziare con my proposals o user's proposals?
                        )
                    },
                    onNavigateToOwnedTravelProposal = { travelId ->
                        // view an existing proposal
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))

                    },
                    onNavigateToEdit = { travelId ->
                        // edit an existing travel
                        navController.navigate("editTravel/$travelId")

                    }
                )
            }

            composable(Screen.BookedTrips.base) {
                val parentEntry = remember { navController.getBackStackEntry("app") }
                val profileVm  = viewModel<ProfileViewModel>(parentEntry, factory = Factory)
                BookedTravelList(
                    profileVm = profileVm,
                    onNavigateToTravel = { travelId ->
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                    },
                    onNavigateToSuitcase = {navController.navigate(Screen.Suitcase.base)},
                    topBar = {
                        TopBar(
                            onNavigateToL = { navController.popBackStack() },
                            onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                            page = "My Booked Trips"
                        )
                    }
                )
            }

            composable(
                route = Screen.BookedTrips.withUserIdArg(),
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { entry ->
                val uid = entry.arguments!!.getString("userId")!!
                val vm: ProfileViewModel = viewModel(
                    key = "OtherProfileVm-$uid",
                    factory = OtherUserProfileVmFactory(
                        userId = uid,
                        travelModel = Factory.travelModel,
                        signedInUserId = ""
                    )
                )

                BookedTravelList(
                    profileVm = vm,
                    onNavigateToTravel = { travelId ->
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                    },
                    onNavigateToSuitcase = {navController.navigate(Screen.Suitcase.base)},
                    topBar = {
                        TopBar(
                            onNavigateToL = { navController.popBackStack() },
                            onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                            page = "Booked Trips"
                        )
                    }
                )
            }




            composable(Screen.SuggestedTrips.base) {

                TravelProposalList(
                    homeVm = viewModel(factory = Factory),
                    onNavigateToTravelProposal = { travelId ->
                        navController.navigate(
                            Screen.TravelProposal.routeWithProposalId(travelId)
                        )
                    },
                    topBar = {
                        TopBar(
                            onNavigateToL = { navController.popBackStack() },
                            onNavigateToR = { navController.navigate(Screen.Notifications.base)},
                            page         = "Suggested Trips"
                        )
                    }
                )
            }


            composable(Screen.Notifications.base) {
                val userId = (authState as? AuthViewModel.AuthState.Success)?.user?.uid
                Log.d("LISA SCLERA", userId!!)
                NotificationsScreen(
                    topBar = {
                        TopBar(
                            onNavigateToL = { navController.popBackStack() },
                            onNavigateToR = { },
                            page = "Notifications"
                        )
                    },
                    navController = navController,
                    viewModel = notificationVm,
                    userId = userId
                )
            }

//            composable(Screen.OldProposals.base) {
//                val parentEntry = remember { navController.getBackStackEntry("app") }
//                val profileVm  = viewModel<ProfileViewModel>(parentEntry, factory = Factory)
//                OldTravelProposalList(
//                    profileVm = profileVm,
//                    onClick   = { tid ->
//                        navController.navigate(Screen.TravelProposal.routeWithProposalId(tid))
//                    },
//                    topBar = {
//                        TopBar(
//                            onNavigateToL = { navController.popBackStack() },
//                            onNavigateToR = { navController.navigate(Screen.Notifications.base) },
//                            page = "Old Trips"
//                        )
//                    }
//                )
//            }

            composable(
                route     = Screen.OldProposals.withUserIdArg(),
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { entry ->
                val uid = entry.arguments!!.getString("userId")!!
                val vm: ProfileViewModel = viewModel(
                    key = "OtherProfileVm-$uid",
                    factory = OtherUserProfileVmFactory(
                        userId = uid,
                        travelModel = Factory.travelModel,
                        signedInUserId = ""
                    )
                )
                OldTravelProposalList(
                    profileVm = vm,
                    userId    = uid,
                    onClick   = { tid ->
                        navController.navigate(Screen.TravelProposal.routeWithProposalId(tid))
                    },
                    //userId = uid,
                    topBar = {
                        TopBar(
                            onNavigateToL = { navController.popBackStack() },
                            onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                            page = "Old Trips"
                        )
                    }
                )
            }


            composable(
                route = Screen.Chat.withChatIdArg(),
                arguments = listOf(navArgument("chatId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments!!.getString("chatId")!!
                // You’ll probably want a ChatViewModel here to load messages:
                val vm: ChatViewModel = viewModel(factory = Factory)
                LaunchedEffect(chatId) {
                    vm.openChat(chatId)
                }
                ChatScreen(
                    chatId    = chatId,
                    chatViewModel = vm,
                    onBack    = { navController.popBackStack() }
                )
            }


            composable(Screen.ChatList.base) {
                ChatListScreen(navController)
            }



            composable(
                route = Screen.Chat.withChatIdArg(),
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments!!.getString("chatId")!!
                val chatVm: ChatViewModel = viewModel(factory = Factory)
                LaunchedEffect(chatId) { chatVm.openChat(chatId) }
                ChatScreen(
                    chatId    = chatId,
                    chatViewModel = chatVm,
                    onBack    = { navController.popBackStack() }
                )
            }

//            composable(Screen.Favorites.base) {
//                val currentAuthState = authState
//                if (currentAuthState is AuthViewModel.AuthState.Success) {
//                    val parentEntry = remember { navController.getBackStackEntry("app") }
//                    val profileVm = viewModel<ProfileViewModel>(parentEntry, factory = Factory)
//
//                    Favorites(
//                        profileVm = profileVm,
//                        onNavigateToTravel = { travelId ->
//                            navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
//                        },
//                        topBar = {
//                            TopBar(
//                                iconL = "chat",
//                                onNavigateToL = { navController.navigate(Screen.ChatList.base) },
//                                onNavigateToR = { navController.navigate(Screen.Notifications.base) },
//                                page = "Favorites"
//                            )
//                        }
//                    )
//                } else {
//                    // redirect se guest / loading / error
//                    LaunchedEffect(currentAuthState) {
//                        navController.navigate(Screen.Login.base) {
//                            popUpTo(Screen.Profile.base) { inclusive = true }
//                        }
//                    }
//                }
//            }

            composable(Screen.Favorites.base) {
                when (val currentAuthState = authState) {
                    is AuthViewModel.AuthState.Success -> {
                        val parentEntry = remember { navController.getBackStackEntry("app") }
                        val profileVm = viewModel<ProfileViewModel>(parentEntry, factory = Factory)

                        Favorites(
                            profileVm = profileVm,
                            onNavigateToTravel = { travelId ->
                                navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                            },
                            topBar = {
                                TopBar(
                                    iconL = "chat",
                                    onNavigateToL = { navController.navigate(Screen.ChatList.base) },
                                    onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                                    page = "Favorites"
                                )
                            }
                        )
                    }

                    is AuthViewModel.AuthState.Guest,
                    is AuthViewModel.AuthState.Loading,
                    is AuthViewModel.AuthState.Error -> {
                        LaunchedEffect(currentAuthState) {
                            navController.navigate(Screen.Login.base) {
                                popUpTo(Screen.Favorites.base) { inclusive = true }
                            }
                        }
                    }

                    AuthViewModel.AuthState.Idle -> TODO()
                }
            }

            composable(Screen.Suitcase.base) {
                val travelId = navController.previousBackStackEntry?.arguments?.getString("travelId") ?: return@composable
                val userId = (authState as AuthViewModel.AuthState.Success).user?.uid
                val viewModel = viewModel<PackingViewModel>()

                if (userId != null) {
                    PackingScreen(
                        travelId = travelId,
                        navController = navController,
                        topBar = {
                            TopBar(
                                onNavigateToL = { navController.popBackStack() },
                                onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                                page = "PACKING THE SUITCASE"
                            )
                        },
                        viewModel = viewModel,
                        userId = userId
                    )
                }
            }

            composable(Screen.ItemsScreen.base) {
                if (authState is AuthViewModel.AuthState.Success) {
                    val userId = (authState as AuthViewModel.AuthState.Success).user?.uid
                    val viewModel = viewModel<PackingViewModel>()

                    val backStackEntry = remember { navController.previousBackStackEntry }
                    val travelId = backStackEntry?.savedStateHandle?.get<String>("travelId") ?: return@composable
                    val selectedCategories = backStackEntry?.savedStateHandle?.get<List<String>>("selectedCategories") ?: emptyList()

                    if (userId != null) {
                        ItemsScreen(
                            topBar = {
                                TopBar(
                                    iconL = "arrow",
                                    onNavigateToL = { navController.popBackStack() },
                                    onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                                    page = "Favorites"
                                )
                            },
                            userId = userId,
                            travelId = travelId,
                            selectedCategories = selectedCategories,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToTravelProposal = { travelId ->
                                navController.navigate(Screen.TravelProposal.routeWithProposalId(travelId))
                            },
                        )
                    }
                }
            }

        }
    }
}









//TODO: fix home page per Guest

enum class Screen(val base: String) {
    Home("home"),
    Search("search"),
    AddTravel("addTravel"),
    TravelProposal("travelProposal"),
    AddParticipantsInfo("add_participants"),
    OtherUserProposals("otherProposals"),
    Favorites("favorites"),
    Profile("profile"),
    OtherUserProfile("otherUserProfile"),
    Participants("participants"),
    OwnedProposals("ownedProposals"),
    Notifications("notifications"),
    SuggestedTrips("suggestedTrips"),
    OldProposals("oldProposals"),
    BookedTrips("bookedTrips"),
    Login("login"),
    Register("register"),
    ChatList("chatList"),
    Chat("chat"),
    Suitcase("suitcase"),
    ItemsScreen("items");


    // Helpers:
    fun withUserIdArg()       = "$base/{userId}"
    fun routeWithUserId(id: String) = "$base/$id"

    // Add Participants route: participants/{travelId}
    fun withTravelIdCountArg()         = "$base/{travelId}/{count}"
    fun routeWithTravelIdCount(id: String, count: Int) = "$base/$id/$count"

    // Participants route: participants/{travelId}
    fun withTravelIdArg()         = "$base/{travelId}"
    fun routeWithTravelId(id: String) = "$base/$id"

    // Travel-proposal route: travelProposal/{travelId}
    fun withProposalIdArg()         = "$base/{travelId}"
    fun routeWithProposalId(id: String) = "$base/$id"

    // helper for NavHost: defines that travelId is optional
    fun withOptionalTravelIdArg() = "$base?travelId={travelId}"

    // when navigating
    fun navWithTravelId(id: String?): String =
        if (id != null) "$base/$id" else base

    fun createRoute(id: String) = "travelProposal/$id"

    fun Screen.homeWithId(userId: String) = "home/$userId"

    fun withFromBottomNavArg() = "$base?fromBottomNav={fromBottomNav}"
    fun routeWithFromBottomNav(fromBottomNav: Boolean) = "$base?fromBottomNav=$fromBottomNav"


    // Helpers for chat:
    fun withChatIdArg() = "$base/{chatId}"
    fun routeWithChatId(id: String) = "$base/$id"
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    //mi serviranno per non far aprire il profilo ai Guest
    val authVm: AuthViewModel = viewModel(factory = Factory)
    val authState by authVm.authState.collectAsState()

    val currentRoute = navController
        .currentBackStackEntryAsState().value
        ?.destination
        ?.route
        ?: Screen.Home.base

    val screens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.AddTravel,
        Screen.Favorites,
        Screen.Profile
    )
    val labels = listOf("Home", "Search", "Add", "Favorites", "Profile")
    val selectedIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Search,
        Icons.Filled.Add,
        Icons.Filled.Favorite,
        Icons.Filled.Person
    )
    val unselectedIcons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Search,
        Icons.Outlined.Add,
        Icons.Default.FavoriteBorder,
        Icons.Outlined.Person
    )


    NavigationBar (
        modifier = Modifier.height(90.dp),
        containerColor = Color(0xFFC9DAC8)
    ){
        screens.forEachIndexed { index, screen ->

            val isSelected = when (screen) {
                Screen.Profile -> {
                    currentRoute.startsWith(Screen.Profile.base) || currentRoute.startsWith(Screen.Login.base)
                }
                else -> currentRoute.startsWith(screen.base)
            }
            val destination = if (screen == Screen.Profile) {
                if (authState is AuthViewModel.AuthState.Success) {
                    Screen.Profile.routeWithFromBottomNav(true)
                } else {
                    Screen.Login.base
                }
            } else {
                screen.base
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    val isGuest = authState is AuthViewModel.AuthState.Guest

                    val guestRestrictedTabs = listOf(Screen.Profile, Screen.AddTravel)

                    if (isGuest && screen in guestRestrictedTabs) {
                        navController.navigate("auth") { //login in auth dovrebbe far partire sut. Login
                            popUpTo("app") { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(destination) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcons[index]
                        else unselectedIcons[index],
                        contentDescription = labels[index],
                        tint = DarkGreen
                    )
                },
                label = {
                    if (isSelected) Text(labels[index])
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF60935D)
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    iconL: String = "arrow",
    onNavigateToL: () -> Unit,
    onNavigateToR: () -> Unit,
    page: String
) {
    CenterAlignedTopAppBar(
        title = { Text(page) },
        modifier = Modifier
            .padding(bottom = 6.dp),
        navigationIcon = {
            if(iconL == "chat") {
                IconButton(onClick = onNavigateToL) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "chat"
                    )
                }
            }
            else {
                IconButton(onClick = onNavigateToL) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back Home"
                    )
                }
            }
        },
        actions = {
            // Toggle edit mode or navigate to an edit screen
            IconButton(onClick = onNavigateToR) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications"
                )
            }
        }
    )
}




















