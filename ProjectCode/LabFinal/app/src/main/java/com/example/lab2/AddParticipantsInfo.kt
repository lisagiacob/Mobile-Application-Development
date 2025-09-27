package com.example.lab2

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.lab2.ui.theme.DarkGreen
import com.example.lab2.ui.theme.GreenButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipantsInfoScreen(
    navController: NavHostController,
    travelId: String,
    peopleCount: Int,
    vm: TravelViewModel = viewModel(factory = Factory),
    UserVm: ProfileViewModel = viewModel(factory = Factory),
    //topBar: @Composable () -> Unit,
) {

    LaunchedEffect(peopleCount) {
        vm.peopleNumber = peopleCount
    }


    LaunchedEffect(travelId) {
        Log.d("AddParticipantsInfo", "👀 Entered screen for travelId=$travelId, peopleNumber=${peopleCount}")
    }

    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val defaultDate = formatter.parse("01/01/2000")!!.time
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = defaultDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )
    var selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToString(it)  //convert to date "dd/MM/aaaa"
    } ?: "01/01/2000"

    for (i in 0..< peopleCount-1) {
        vm.newBirthDates.add("01/01/2000")
        vm.newNames.add("")
        vm.newSurnames.add("")
        vm.newNumbers.add("")
    }

    Scaffold(
        topBar = {
            TopBar(
                onNavigateToL = { navController.popBackStack() },
                onNavigateToR = { navController.navigate(Screen.Notifications)},
                page         = "Add Participants"
            )
        },
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (peopleCount > 1) {

                items(peopleCount - 1) { index ->
                    Card(
                        modifier = Modifier
                            .widthIn(max = 410.dp)
                            .padding(8.dp)
                            .border(1.7.dp, GreenButton, RoundedCornerShape(45.dp))
                            .background(Color.White),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {


                            Text(
                                text = "Additional Participant ${index + 1}",
                                style = MaterialTheme.typography.titleLarge,
                                color = DarkGreen

                            )

                            Spacer(Modifier.padding(6.dp))
                            //Name
                            OutlinedTextField(
                                modifier = Modifier
                                    .padding(bottom = 3.dp, start = 13.dp, end = 13.dp),
                                value = vm.newNames[index],
                                onValueChange = { vm.updateName(index, it) },
                                label = { Text("Name") },
                                isError = vm.nameError != null,
                                singleLine = true,
                                shape = RoundedCornerShape(11.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF60935D),
                                    unfocusedBorderColor = Color(0xFF60935D),
                                    cursorColor = Color(0xFF60935D),
                                    focusedLabelColor = Color(0xFF60935D),
                                ),
                            )
                            if (vm.nameError != null) {
                                Text(
                                    text = vm.nameError!!,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelMedium //non mi fa usare caption
                                )
                            }

                            Spacer(Modifier.padding(4.dp))
                            //Surname
                            OutlinedTextField(
                                modifier = Modifier
                                    .padding(bottom = 3.dp, start = 13.dp, end = 13.dp),
                                value = vm.newSurnames[index],
                                onValueChange = { vm.updateSurname(index,it) },
                                label = { Text("Surname") },
                                isError = vm.surnameError != null,
                                singleLine = true,
                                shape = RoundedCornerShape(11.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF60935D),
                                    unfocusedBorderColor = Color(0xFF60935D),
                                    cursorColor = Color(0xFF60935D),
                                    focusedLabelColor = Color(0xFF60935D),
                                ),
                            )
                            if (vm.surnameError != null) {
                                Text(
                                    text = vm.surnameError!!,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(Modifier.padding(4.dp))

                            if (showDatePicker) {
                                DatePickerDialog(
                                    onDismissRequest = { showDatePicker = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            if (datePickerState.selectedDateMillis != null) {
                                                selectedDate = convertMillisToString(
                                                    datePickerState.selectedDateMillis!!)
                                            }
                                            vm.updateBirthDate(index, selectedDate)
                                            showDatePicker = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDatePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                ) {
                                    DatePicker(
                                        state = datePickerState,
                                        showModeToggle = false,)
                                }

                            }

                            OutlinedTextField(
                                value = selectedDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Birth date") },
                                placeholder = { Text("gg/mm/aaaa") },
                                isError = vm.dateError != null,
                                shape = RoundedCornerShape(11.dp),
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            Icons.Default.CalendarToday, contentDescription = null,
                                            tint = Color(0xFF60935D)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(bottom = 3.dp, start = 13.dp, end = 13.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF60935D),
                                    unfocusedBorderColor = Color(0xFF60935D),
                                    cursorColor = Color(0xFF60935D),
                                    focusedLabelColor = Color(0xFF60935D),
                                ),
                            )

                            if (vm.dateError != null) {
                                Text(
                                    "Invalid Date: Birth date cannot be in the future.",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.padding(4.dp))
                            //Phone Number
                            OutlinedTextField(
                                modifier = Modifier
                                    .padding(bottom = 3.dp, start = 13.dp, end = 13.dp),
                                value = vm.newNumbers[index],
                                onValueChange = { vm.updateNumber(index, it) },
                                label = { Text("Phone Number") },
                                placeholder = { Text("es. (+39)1234567890") },
                                isError = vm.numberError != null,
                                singleLine = true,
                                shape = RoundedCornerShape(11.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF60935D),
                                    unfocusedBorderColor = Color(0xFF60935D),
                                    cursorColor = Color(0xFF60935D),
                                    focusedLabelColor = Color(0xFF60935D),
                                ),
                            )
                            if (vm.numberError != null) {
                                Text(
                                    text = vm.numberError!!,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(Modifier.padding(4.dp))
                        }
                    }
                }
                item {
                    Spacer(Modifier.padding(4.dp))
                }

                item {
                    Button(
                        onClick = {
                            vm.saveParticipantsInfo(travelId)
                            navController.popBackStack()

                        },
                        modifier = Modifier.width(100.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenButton,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Apply",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                //TODO: impostare success popup nella sschermata del viaggio dopo aver salvato i dati
            }
        }
    }
}

fun convertMillisToString(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}