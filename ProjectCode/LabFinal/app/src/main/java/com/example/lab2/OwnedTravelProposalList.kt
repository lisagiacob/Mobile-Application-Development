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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import kotlinx.coroutines.flow.StateFlow
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnedTravelProposalList(
    profileVm: ProfileViewModel,
    travelViewModel: TravelViewModel = viewModel(factory = Factory),
    userId: String,                                            // his proposals to show
    onNavigateToOwnedTravelProposal: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    topBar: @Composable () -> Unit
) {
    val proposalsFlow: StateFlow<List<Travel>> = if (userId == profileVm.id) {
        profileVm.proposals
    } else {
        travelViewModel.proposalsFor(userId)
    }

    val proposals by proposalsFlow.collectAsState()

    var deleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = topBar) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = proposals,
                key = { it.id }
            ) { travel ->
                TravelCard(
                    travel = travel,
                    onClick = { onNavigateToOwnedTravelProposal(travel.id) },
                    onEdit  = { onNavigateToEdit(travel.id) },
                    onDelete= { deleteId = travel.id },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Delete confirmation
        deleteId?.let { id ->
            AlertDialog(
                title           = { Text("Delete Trip") },
                text            = { Text("Are you sure you want to delete this travel proposal?") },
                onDismissRequest= { deleteId = null },
                confirmButton   = {
                    Button(
                        onClick = {
                        travelViewModel.deleteTravelById(id)
                        deleteId = null
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF60935D),        // Verde bottone
                            contentColor = Color.White)
                    ) {
                        Text("OK")
                    }
                },
                dismissButton   = {
                    OutlinedButton(
                        onClick = { deleteId = null },
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


@Composable
fun TravelCard(
    travel: Travel,
    onClick: () -> Unit,
    onEdit: (String) -> Unit,
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

            // Title + actions
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
                    IconButton(onClick = { onEdit(travel.id) }) {
                        Icon(Icons.Filled.Mode, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(travel.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}





@Composable
fun OldTravelProposalList(
    profileVm: ProfileViewModel,
    travelVm:  TravelViewModel = viewModel(factory = Factory),
    userId:    String,
    onClick:   (String) -> Unit,
    topBar:    @Composable () -> Unit
) {
    // pick the right flow
    /*
    val tripsFlow = if (false/*userId == profileVm.id*/) profileVm.oldTrips
    else travelVm.oldTripsFor(userId)

     */

    val oldTripsFlow: StateFlow<List<Travel>> = if (userId == profileVm.id) {
        profileVm.oldTrips
    } else {
        travelVm.oldTripsFor(userId)
    }

    val oldTrips by oldTripsFlow.collectAsState()
    //val oldTrips by profileVm.oldTrips.collectAsState()

    //val oldTrips by tripsFlow.collectAsState()

    Scaffold(topBar = topBar) { padding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding      = PaddingValues(16.dp)
        ) {
            items(oldTrips, key = { it.id }) { travel ->
                val localDate = travel.dateRange.first.toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val month = localDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                val year = localDate.year

                Text(
                    text = "$month $year",
                    modifier = Modifier
                        .padding(8.dp)
                )

                TravelCardOld(
                    travel  = travel,
                    onClick = { onClick(travel.id) },
                    onEdit  = {},
                    onDelete= {},
                    modifier= Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
fun TravelCardOld(
    travel: Travel,
    onClick: () -> Unit,
    onEdit: (String) -> Unit,
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
                        // For simplicity, using an Image composable with the URI
                        Image(
                            painter = rememberImagePainter(travelImage.uri),
                            contentDescription = travel.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                    is TravelImage.RemoteUrl-> {
                        Image(
                            painter = rememberImagePainter(travelImage.url),
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


            }
        }
    }
}