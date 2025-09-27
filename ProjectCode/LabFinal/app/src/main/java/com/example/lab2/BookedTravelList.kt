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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.lab2.ui.theme.DarkGreen

@Composable
fun BookedTravelList(
    profileVm: ProfileViewModel,
    onNavigateToTravel: (String) -> Unit,
    onNavigateToSuitcase: () -> Unit,
    topBar: @Composable () -> Unit
) {
    val bookedTrips by profileVm.bookedTrips.collectAsState()

    Scaffold(topBar = topBar) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(bookedTrips, key = { it.id }) { travel ->
                TravelCardBooked(
                    travel = travel,
                    onClick = { onNavigateToTravel(travel.id) },
                    onEdit = {},     // non editabile
                    onDelete = {},   // non cancellabile
                    modifier = Modifier.fillMaxWidth(),
                    onNavigateToSuitcase

                )
            }
        }
    }
}


@Composable
fun TravelCardBooked(
    travel: Travel,
    onClick: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSuitcase: () -> Unit,
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
                    IconButton(onClick = { onNavigateToSuitcase() }) {
                        Icon(
                            imageVector = Icons.Outlined.Luggage,
                            contentDescription = "Pack the suitcase",
                            modifier = Modifier.background(
                                color = Color.Transparent,
                                shape = CircleShape),
                            tint = DarkGreen
                        )
                    }
                }
            }
        }
    }
}