package com.example.lab2

import UserRepository
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.firstOrNull
import java.text.DateFormat



@Composable
fun RemoteAvatar(
    userId: String,
    size: Dp,
    repository: UserRepository = UserRepository(FirebaseFirestore.getInstance())
) {
    // 1️⃣ fetch the FirestoreUser once
    val imageUrl by produceState<String?>(initialValue = null, userId) {
        try {
            value = repository.fetchUserById(userId).image  // uses your `image` field
        } catch (_: Exception) {
            value = null
        }
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
                Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun remoteDisplayName(
    userId: String,
    repository: UserRepository = UserRepository(FirebaseFirestore.getInstance())
): String {
    val name by produceState("…", userId) {
        try {
            value = repository.fetchUserById(userId).username
        } catch (_: Exception) {
            value = "Unknown"
        }
    }
    return name
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    chatViewModel: ChatViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(chatId) {
        chatViewModel.openChat(chatId)
    }

    val messages by chatViewModel.messages.collectAsState()
    val otherUserName = chatViewModel.otherUserName.collectAsState(initial = "Loading...")
    val otherUserImage = chatViewModel.otherUserImage.collectAsState(initial = null)
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val otherId by chatViewModel.otherUserId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (otherId != null) {
                            RemoteAvatar(userId = otherId!!, size = 40.dp)
                        } else {
                            // placeholder while loading
                            Icon(
                                Icons.Filled.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(text = chatViewModel.otherUserName.collectAsState("…").value)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        placeholder = { Text("Type a message") },
                        textStyle = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                chatViewModel.sendMessage(chatId, inputText)
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)    // respect topBar / bottomBar
                .padding(16.dp)            // your 16.dp all-around inset
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages) { message ->
                    val isMe = message.senderId == chatViewModel.currentUserId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (!isMe) {
                            RemoteAvatar(userId = message.senderId, size = 32.dp)
                            Spacer(Modifier.width(4.dp))
                        }
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .padding(4.dp)
                                .widthIn(max = 250.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                if (!isMe) {
                                    // optional: show sender’s name above message
                                    Text(
                                        text = remoteDisplayName(message.senderId),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Text(text = message.text)
                            }
                        }
                        if (isMe) {
                            Spacer(Modifier.width(4.dp))
                            RemoteAvatar(userId = message.senderId, size = 32.dp)
                        }
                    }
                }
            }
        }

    }
}


@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier
                .padding(4.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}




@Composable
fun ChatListScreen(
    navController: NavHostController,
    vm: ChatListViewModel = viewModel(factory = Factory)
) {
    val previews by vm.previews.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                onNavigateToL = { navController.popBackStack() },
                onNavigateToR = { navController.navigate(Screen.Notifications.base) },
                page = "Chats"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding
        ) {
            items(previews) { chat ->
                ChatListItem(
                    preview = chat,
                    onClick = {
                        navController.navigate(
                            Screen.Chat.routeWithChatId(chat.chatId)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ChatListItem(
    preview: ChatPreview,
    onClick: () -> Unit,
    userRepository: UserRepository = UserRepository(FirebaseFirestore.getInstance())
) {
    //Fetch the other user’s model
    val otherUser by produceState<UserModel?>(initialValue = null, preview.otherUserId) {
        value = userRepository.fetchUserById(preview.otherUserId)
            .toUserModel()  // your extension to map Firestore → UserModel
    }

    //Collect their profile image (Uri or URL)
    val img by otherUser?.profileImage?.collectAsState(initial = null)
        ?: remember { mutableStateOf<UserImage?>(null) }


    //  collect the username flow, defaulting to blank
    val username = remoteDisplayName(preview.otherUserId)



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RemoteAvatar(userId = preview.otherUserId, size = 48.dp)

        Spacer(Modifier.width(12.dp))

        //Textual info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = username,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text  = preview.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        //Timestamp
        preview.lastTimestamp?.toDate()?.let { date ->
            Text(
                text  = DateFormat.getTimeInstance(DateFormat.SHORT).format(date),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


