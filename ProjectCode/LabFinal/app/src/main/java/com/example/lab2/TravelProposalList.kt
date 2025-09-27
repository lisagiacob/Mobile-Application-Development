package com.example.lab2

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter

@Composable
fun TravelProposalList(
    homeVm: HomeViewModel,
    onNavigateToTravelProposal: (String) -> Unit,
    topBar: @Composable () -> Unit
) {
    val suggestedTrips = homeVm.suggestedTrips.collectAsState()

    Scaffold (
        topBar = topBar
    ) { innerPadding ->

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = innerPadding.calculateTopPadding(), bottom = 4.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(suggestedTrips.value, key = { it.id }) { travel ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 2.dp,
                            color = Color(0xFF60935D),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            onNavigateToTravelProposal(travel.id)                        }
                ) {
                    Column {

                        val firstImage = travel.images.firstOrNull()
                        val painter = when (firstImage) {
                            is TravelImage.Resource -> painterResource(id = firstImage.resId)
                            is TravelImage.UriImage -> rememberAsyncImagePainter(model = firstImage.uri)
                            is TravelImage.RemoteUrl -> rememberAsyncImagePainter(model = firstImage.url)
                            else -> painterResource(id = R.drawable.placeholder)
                        }

                        Image(
                            painter = painter,
                            contentDescription = travel.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        Text(
                            text = travel.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF203322),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}