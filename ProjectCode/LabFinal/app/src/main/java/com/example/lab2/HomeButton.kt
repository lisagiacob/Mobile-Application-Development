package com.example.lab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lab2.DBSeeder.clearFirestoreDatabase
import com.google.firebase.Timestamp
import java.util.UUID

@Composable
fun HomeButtonScreen(
    topBar: @Composable () -> Unit,
) {

    Scaffold (
        topBar = topBar
    ){  innerPadding ->
        // UI with profile fields using the ViewModel's state

        fun clearDBandSeed(){
            // clearFirestoreDatabase è asincrona -> i seeder devono essere dentro altrimenti
            // potrebbero partire prima della fine della delete
            clearFirestoreDatabase {
                DBSeeder.seedAllUsers()
                DBSeeder.seedAllTravels()
                DBSeeder.seedAllParticipants()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { clearDBandSeed() },
                colors = ButtonDefaults.buttonColors(
                    containerColor =  Color(0xFF60935D)
                )
            ) {
                Text("Clear DB and Seed")
            }


            val context = LocalContext.current
            Button(onClick = {
                showSystemNotification(context, NotificationModel(
                    id = UUID.randomUUID().toString(),
                    message = "Notifica di test manuale",
                    timestamp = Timestamp.now(),
                    read = false,
                    type = "application",
                    relatedTravelId = null,
                    relatedUserId = null,
                    senderId = null
                ))
            }) {
                Text("Test notifica sistema")
            }
        }
    }
}
