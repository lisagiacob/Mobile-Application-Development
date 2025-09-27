package com.example.lab2
import android.content.pm.PackageManager
import android.os.Build
import com.example.lab2.Travel
import com.example.lab2.TravelModel

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun HomePageScreen(
    vm: ProfileViewModel,
    homeVm: HomeViewModel,
    topBar: @Composable () -> Unit,
    onNavigateToTravelProposal: (String) -> Unit,
    onNavigateToOwnedTravelProposal: (String) -> Unit,
    onNavigateToSuggestedTravelProposal: () -> Unit,
    authVm: AuthViewModel
){
    val myProposals      by homeVm.myProposals.collectAsState()
    val friendsProposals by homeVm.friendsProposals.collectAsState()
    val suggestedTrips by homeVm.suggestedTrips.collectAsState()
    val someTrips by homeVm.someTrips.collectAsState()

    val authState by authVm.authState.collectAsState()
    val isLoggedIn = authState is AuthViewModel.AuthState.Success

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Log.d("PERMISSION", "Notification permission granted")
            } else {
                Log.d("PERMISSION", "Notification permission denied")
            }
        }
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("PERMISSION", "Notification permission already granted")
            }
        }
    }


    Scaffold (
        topBar = topBar
    ) { innerPadding ->

        if(isLoggedIn) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {

                item {
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
                                text = "Suggested Trips",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.Bottom)
                        ) {
                            Text(
                                text = "See all",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = Color(0xFF007AFF),
                                modifier = Modifier
                                    .clickable { onNavigateToSuggestedTravelProposal() }
                                    .padding(8.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF0B3E28))


                    // Proposals carousel
                    //val proposals = listOf("Proposal 1 description", "Proposal 2 description", "Proposal 3 description", "Proposal 4 description", "Proposal 5 description", "Proposal 6 description", "Proposal 7 description")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(200.dp)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(suggestedTrips, key = { it.id }) { travel ->
                            ProposalCard(travel, onClick = onNavigateToTravelProposal)

                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your Friends Proposals",
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )
                    }

                    HorizontalDivider(color = Color(0xFF0B3E28))


                    // Proposals carousel
                    //val proposals = listOf("Proposal 1 description", "Proposal 2 description", "Proposal 3 description", "Proposal 4 description", "Proposal 5 description", "Proposal 6 description", "Proposal 7 description")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(200.dp)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(friendsProposals, key = { it.id }) { travel ->
                            ProposalCard(travel, onClick = onNavigateToTravelProposal)

                        }
                    }

                }

                //Spacer(modifier = Modifier.height(20.dp))

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "My Proposals",
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                        )
                    }

                    HorizontalDivider(color = Color(0xFF0B3E28))


                    // Proposals carousel
                    //val proposals = listOf("Proposal 1 description", "Proposal 2 description", "Proposal 3 description", "Proposal 4 description", "Proposal 5 description", "Proposal 6 description", "Proposal 7 description")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(200.dp)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(myProposals, key = { it.id }) { travel ->
                            ProposalCard(
                                travel = travel,
                                onClick = { travelId -> onNavigateToOwnedTravelProposal(travelId) }
                            )
                        }
                    }
                }
            }

        }

        else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Suggested Trips",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF0B3E28), modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(someTrips, key = { it.id }) { travel ->
                        ProposalCard(travel, onClick = onNavigateToTravelProposal)
                    }
                }
            }
        }
    }
}


@Composable
fun ProposalCard(
    travel: Travel,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isTravelLog: Boolean = false
) {
    val travelImage = travel.images.firstOrNull()

    Box(
        modifier = modifier
            .width(350.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x5860935D))
            .clickable { onClick(travel.id) },
        contentAlignment = Alignment.Center
    ) {
        when (travelImage) {
            is TravelImage.Resource -> {
                Image(
                    painter = painterResource(id = travelImage.resId),
                    contentDescription = travel.title,
                    contentScale = ContentScale.Crop,
                    alpha = 0.7f,
                    modifier = Modifier.matchParentSize()
                )
            }

            is TravelImage.UriImage -> {
                Image(
                    painter = rememberAsyncImagePainter(model = travelImage.uri),
                    contentDescription = travel.title,
                    contentScale = ContentScale.Crop,
                    alpha = 0.7f,
                    modifier = Modifier.matchParentSize()
                )
            }
            is TravelImage.RemoteUrl -> {
                Image(
                    painter = rememberAsyncImagePainter(model = travelImage.url),
                    contentDescription = travel.title,
                    contentScale = ContentScale.Crop,
                    alpha = 0.7f,
                    modifier = Modifier.matchParentSize()
                )
            }

            else -> {
                // Fallback a placeholder
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = travel.title,
                    contentScale = ContentScale.Crop,
                    alpha = 0.7f,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
        ) {
            Text(
                text = travel.title,
                color = Color.White,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        if (isTravelLog) {
            Box(
                modifier = Modifier
                    //.fillMaxWidth()
                    .align(Alignment.TopStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                val localDate = travel.dateRange.first.toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val month = localDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                val year = localDate.year

                Text(
                    text = "$month $year",
                    color = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                )
            }
        }
    }
}
