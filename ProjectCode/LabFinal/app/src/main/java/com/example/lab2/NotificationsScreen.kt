package com.example.lab2

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import coil.compose.AsyncImage
import com.example.lab2.ui.theme.GreenButton
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

//TODO: Eliminare user Jhon Doe perchè scazza TUTTO
@Composable
fun NotificationsScreen(
    topBar: @Composable () -> Unit,
    navController: NavHostController,
    viewModel: NotificationViewModel = viewModel(),
    userId: String
) {
    val authVm: AuthViewModel = viewModel()
    val authState by authVm.authState.collectAsState()

    val notifications by viewModel.notifications.collectAsState()
    val relatedTravel by viewModel.relatedTravels.collectAsState()
    val relatedUsers by viewModel.relatedUsers.collectAsState()

    // Recupera userId solo se autenticato
    //val userId = (authState as? AuthViewModel.AuthState.Success)?.user?.uid

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.newSystemNotifications, lifecycleOwner) {
        viewModel.newSystemNotifications
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { notification ->
                showSystemNotification(context, notification)
            }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            Log.d("Lisa aaa", "aaa")
            viewModel.observeNotifications(userId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        topBar()

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nessuna notifica")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification, navController, viewModel, userId)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationModel,
    navController: NavHostController,
    viewModel: NotificationViewModel,
    userId: String?
) {
    val relatedTravel = viewModel.relatedTravels.collectAsState().value[notification.relatedTravelId]
    val relatedUsers = viewModel.relatedUsers.collectAsState().value
    val senderUser = relatedUsers[notification.senderId]
    val relatedUser = relatedUsers[notification.relatedUserId]

    val travelImage = relatedTravel?.images
        ?.firstOrNull { it is TravelImage.RemoteUrl }
        ?.let { (it as TravelImage.RemoteUrl).url }

    val senderImage = when (val img = senderUser?.image) {
        is UserImage.UrlImage -> img.url
        is UserImage.UriImage -> img.uri.toString()
        else -> null
    }

    val relatedUserImage = when (val img = relatedUser?.image) {
        is UserImage.UrlImage -> img.url
        is UserImage.UriImage -> img.uri.toString()
        else -> null
    }

    val background =
        if (!notification.read) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = true) {
                when (notification.type) {
                    "application", "travel_review" -> {
                        if (userId != null) {
                            viewModel.markNotificationAsRead(notification.id, userId)
                        }

                        notification.relatedTravelId?.let { travelId ->
                            navController.navigate(Screen.Participants.routeWithTravelId(travelId))
                        }
                    }

                    "user_review" -> {
                        if (userId != null) {
                            viewModel.markNotificationAsRead(notification.id, userId)
                        }

                        navController.navigate(Screen.Profile.base)
                    }

                    "application-status" -> {
                        if (userId != null) {
                            viewModel.markNotificationAsRead(notification.id, userId)
                        }

                        notification.relatedTravelId?.let { travelId ->
                            navController.navigate(
                                Screen.TravelProposal.routeWithProposalId(
                                    travelId
                                )
                            )
                        }
                    }

                    "friend_added" -> {
                        if (userId != null) {
                            viewModel.markNotificationAsRead(notification.id, userId)
                        }

                        notification.relatedUserId?.let { userId ->
                            navController.navigate(
                                Screen.OtherUserProfile.routeWithUserId(userId)
                            )
                        }
                    }
                }

            },
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.read) GreenButton else Color.LightGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {


        Row(
            modifier = Modifier
                .padding(16.dp)
                .background(background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(text = notification.message)
            }

            when (notification.type) {
                "application", "travel_review", "application-status" -> {
                    if (travelImage != null) {
                        AsyncImage(
                            model = travelImage,
                            contentDescription = "Travel Image",
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                "user_review", "friend_added" -> {
                    val userImage = if (notification.type == "user_review") senderImage else relatedUserImage
                    if (userImage != null) {
                        val interactionSource = remember { MutableInteractionSource() }

                        AsyncImage(
                            model = userImage,
                            contentDescription = "User Image",
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Meglio nulla
                    }
                }
            }
        }
    }

    fun formatTimestamp(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}