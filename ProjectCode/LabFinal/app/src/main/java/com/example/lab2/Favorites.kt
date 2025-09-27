package com.example.lab2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun Favorites(
    profileVm: ProfileViewModel,
    onNavigateToTravel: (String) -> Unit,
    topBar: @Composable () -> Unit
) {
    val favorites by profileVm.favorites.collectAsState()

    var removeId by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = topBar) { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You have no favorite trips yet",
                    fontSize = 18.sp
                )
            }
        }
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(favorites, key = { it.id }) { travel ->
                    TravelCardFavorites(
                        travel = travel,
                        onClick = { onNavigateToTravel(travel.id) },
                        onDelete = {removeId = travel.id},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Delete confirmation
            removeId?.let { id ->
                AlertDialog(
                    title = { Text("Remove Trip") },
                    text = { Text("Are you sure you want to remove this travel from your favorites?") },
                    onDismissRequest = { removeId = null },
                    confirmButton = {
                        Button(
                            onClick = {
                            profileVm.removeFavorite(id)
                            removeId = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF60935D),        // Verde bottone
                                contentColor = Color.White)
                            ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { removeId = null },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF60935D)           // Bordo e testo verdi
                            )
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun TravelCardFavorites(
    travel: Travel,
    onClick: () -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, Color(0xFF60935D), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            // Image or placeholder
            travel.images.firstOrNull()?.let { travelImage ->
                when (travelImage) {
                    is TravelImage.Resource -> {
                        // Image with resource ID
                        Image(
                            painter = painterResource(id = travelImage.resId),
                            contentDescription = travel.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                    is TravelImage.UriImage -> {
                        // Image from URI
                        Image(
                            painter = rememberAsyncImagePainter(travelImage.uri),
                            contentDescription = travel.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                    is TravelImage.RemoteUrl -> {
                        // Image from URI
                        Image(
                            painter = rememberAsyncImagePainter(travelImage.url),
                            contentDescription = travel.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("No image", color = Color.DarkGray)
            }

            // Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = travel.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF203322),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row {
                    IconButton(onClick = { onDelete(travel.id) }) {
                        Icon(Icons.Filled.Favorite,
                            contentDescription = "Remove from favorites",
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 8.dp)
                                .clickable {
                                    onDelete(travel.id)
                                },
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}