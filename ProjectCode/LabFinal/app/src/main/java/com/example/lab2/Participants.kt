package com.example.lab2

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.contentcapture.ContentCaptureManager.Companion.isEnabled
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.lab2.UserStore.getUserModelById
import com.example.lab2.ui.theme.GreenBackground
import com.example.lab2.ui.theme.GreenButton
import com.example.lab2.ui.theme.GreenDivider
import com.example.lab2.ui.theme.PopupBg
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ParticipantsScreen(
    navController: NavHostController,
    travelId: String,
    vm: TravelViewModel      = viewModel(factory = Factory),
    UserVm: ProfileViewModel = viewModel(factory = Factory),
    topBar: @Composable () -> Unit
) {

    LaunchedEffect(travelId) {
        vm.observeParticipantsForTravel(travelId)
    }

    var showPopupForIndex by remember { mutableStateOf<Int?>(null) } //for the dropdown menu, to track which row is expanded
    var showPopupForIndex1 by remember { mutableStateOf<Int?>(null) } // same but for pending applications

    val travel by vm.currentTravel.collectAsState()
    val participants by vm.participants.collectAsState()
    val pendingApplications = participants
        ?.filterValues { it.second == null }
        ?.map { it.value.first }
        ?.toList()
        ?: emptyList()
    val confirmedParticipants = participants?.filterValues { it.second != null } ?: emptyMap()

    Log.d("ALL participants:", participants.toString())
    Log.d("Not conf. participants:", pendingApplications.toString())
    Scaffold(
        topBar = topBar,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            confirmedParticipants.toList().let { participantsList ->
                itemsIndexed(participantsList) { index, (userId, value) ->
                    val participant = value.first
                    var localSwitchState by remember(key1 = userId) { mutableStateOf(value.second) }

                    LaunchedEffect(value.second) {
                        localSwitchState = value.second
                    }

                    Row(
                        modifier = Modifier
                            .widthIn(max = 410.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(1.7.dp, GreenButton, shape = RoundedCornerShape(50))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.padding(4.dp))
                        //image or monogram
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .clickable {
                                    navController.navigate(
                                        Screen.OtherUserProfile.routeWithUserId(userId)
                                    )

                                },
                            contentAlignment = Alignment.Center
                        ) {


                            var userModel by remember { mutableStateOf<UserModel?>(null) }

                            LaunchedEffect(userId) {
                                userModel = getUserModelById(userId)
                            }
                            val img by remember(userModel) {
                                userModel?.profileImage ?: MutableStateFlow(null)
                            }.collectAsState()


                            Box(modifier = Modifier.clip(CircleShape)) {
                                when (img) {
                                    is UserImage.UrlImage -> Image(
                                        painter = rememberAsyncImagePainter((img as UserImage.UrlImage).url),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    is UserImage.UriImage -> Image(
                                        painter = rememberAsyncImagePainter((img as UserImage.UriImage).uri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    is UserImage.Resource -> Image(
                                        painter = painterResource((img as UserImage.Resource).resId),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    null -> Text(
                                        text = userModel?.username?.value?.firstOrNull()?.uppercase() ?: "",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        //username
                        Text(
                            text = participant.username,
                            Modifier
                                .weight(1f)
                                .clickable {
                                    navController.navigate(Screen.OtherUserProfile.routeWithUserId(userId))

                                },
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Default ),
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = MaterialTheme.typography.bodyLarge.fontStyle
                        )

                        Spacer(Modifier.weight(.1f))

                        //additional participants
                        if (participant.additionalParticipants != null) {
                            Box(
                                modifier = Modifier
                                    .weight(.4f)
                                    .width(12.dp)
                                    .background(GreenBackground, shape = RoundedCornerShape(30)),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = "+${participant.additionalParticipants.size}",
                                    modifier = Modifier
                                        .clickable {
                                            showPopupForIndex = if (showPopupForIndex == index) null else index
                                        },
                                    color = GreenDivider
                                )

                                if (showPopupForIndex == index) {
                                    Popup(
                                        alignment = Alignment.TopStart,
                                        onDismissRequest = { showPopupForIndex = null }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(PopupBg, shape = RoundedCornerShape(20.dp))
                                                .border(1.dp, GreenButton, shape = RoundedCornerShape(20.dp))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column (
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ){
                                                participant.additionalParticipants.forEach { person ->
                                                    Text(buildAnnotatedString {
                                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                            append("${person.name} ${person.surname}\n")
                                                        }
                                                        append("Age ${person.birthDate?.let {
                                                            calculateAge(
                                                                it
                                                            )
                                                        }}")
                                                        if (UserVm.id == travel?.owner?.first){
                                                            append("\nPhone: ${person.cellphone}")
                                                        }
                                                    }, modifier = Modifier.padding(bottom = 8.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        // only owner can see the switch
                        if (UserVm.id == travel?.owner?.first) {
                            Switch(
                                checked = localSwitchState!!,
                                onCheckedChange = { checked ->
                                    localSwitchState = checked
                                    if (checked) {
                                        vm.enableParticipant(travelId, participant)
                                    } else {
                                        vm.disableParticipant(travelId, participant)
                                    }
                                },
                                modifier = Modifier.padding(start = 8.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.White,
                                    checkedTrackColor = GreenButton,
                                    uncheckedTrackColor = Color.Gray.copy(0.5f),
                                    uncheckedBorderColor = Color.Transparent,
                                )
                            )
                        }

                        Spacer(Modifier.padding(4.dp))
                    }
                }
            }

            //pending applications

            //only owner of travel can see pending requests
            if (UserVm.id == travel?.owner?.first && pendingApplications.isNotEmpty()) { //UserVm.id == travel?.owner?.first &&

                item {
                    Spacer(Modifier.padding(10.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .background(Color(0xFFF9A0A8), shape = RoundedCornerShape(30)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("NEW!!",
                            color = Color(0xFFB00020),
                            fontWeight = FontWeight.Bold,
                        )
                    }

                }

                itemsIndexed(pendingApplications) { index, participant ->

                    Row(
                        modifier = Modifier
                            .widthIn(max = 410.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(1.7.dp, GreenButton, shape = RoundedCornerShape(50))
                            .padding(8.dp)
                            .clickable {
                                navController.navigate(Screen.OtherUserProfile.routeWithUserId(participant.userId))

                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.padding(4.dp))
                        //image or monogram
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            var userModel by remember { mutableStateOf<UserModel?>(null) }

                            LaunchedEffect(participant.userId) {
                                userModel = getUserModelById(participant.userId)
                            }
                            val img by remember(userModel) {
                                userModel?.profileImage ?: MutableStateFlow(null)
                            }.collectAsState()


                            Box(modifier = Modifier.clip(CircleShape)) {
                                when (img) {
                                    is UserImage.UrlImage -> Image(
                                        painter = rememberAsyncImagePainter((img as UserImage.UrlImage).url),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    is UserImage.UriImage -> Image(
                                        painter = rememberAsyncImagePainter((img as UserImage.UriImage).uri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    is UserImage.Resource -> Image(
                                        painter = painterResource((img as UserImage.Resource).resId),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    null -> Text(
                                        text = userModel?.username?.value?.firstOrNull()?.uppercase() ?: "",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = participant.username,
                            modifier = Modifier.weight(1f)
                                .clickable {
                                    navController.navigate(Screen.OtherUserProfile.routeWithUserId(participant.userId))

                                },
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Default ),
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = MaterialTheme.typography.bodyLarge.fontStyle
                        )

                        //additional participants
                        if (participant.additionalParticipants != null) {
                            Box(
                                modifier = Modifier
                                    .weight(.36f)
                                    .width(12.dp)
                                    .background(GreenBackground, shape = RoundedCornerShape(30)),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = "+${participant.additionalParticipants.size}",
                                    modifier = Modifier
                                        .clickable {
                                            showPopupForIndex1 = if (showPopupForIndex1 == index) null else index
                                        },
                                    color = GreenDivider
                                )

                                if (showPopupForIndex1 == index) {
                                    Popup(
                                        alignment = Alignment.TopStart,
                                        onDismissRequest = { showPopupForIndex1 = null }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(PopupBg, shape = RoundedCornerShape(20.dp))
                                                .border(1.dp, GreenButton, shape = RoundedCornerShape(20.dp))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column (
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ){
                                                participant.additionalParticipants.forEach { person ->
                                                    Text(buildAnnotatedString {
                                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                            append("${person.name} ${person.surname}\n")
                                                        }
                                                        append("Age ${
                                                            person.birthDate?.let { runCatching { calculateAge(it) }.getOrNull() ?: "?" } ?: "?"
                                                        }")
                                                        if (UserVm.id == travel?.owner?.first){
                                                            append("\nPhone: ${person.cellphone}")
                                                        }
                                                    }, modifier = Modifier.padding(bottom = 8.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.weight(.4f))

                        IconButton(
                            onClick = { vm.enableParticipant(travelId, participant) },
                            modifier = Modifier.padding(start = 6.dp)
                        ){
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Accept",
                                tint = GreenButton,
                                modifier = Modifier
                                    .size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = { vm.disableParticipant(travelId, participant) },
                            modifier = Modifier.padding(end = 4.dp)
                        ){
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Reject",
                                tint = Color(0xFFB00020),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.padding(4.dp))
                    }
                }
            }
        }

    }
}

fun calculateAge(birthDateStr: String): Int {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val birthDate = LocalDate.parse(birthDateStr, formatter)
    val today = LocalDate.now()
    return Period.between(birthDate, today).years
}