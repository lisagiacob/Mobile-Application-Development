@file:OptIn(ExperimentalLayoutApi::class)

package com.example.lab2

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SliderDefaults


@Composable
fun AddTravelScreen(
    travelId: String?,
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: AddTravelViewModel = viewModel(factory = Factory),
    vm: TravelViewModel = viewModel(factory = Factory)
) {
    val context = LocalContext.current.applicationContext  //mi serve per passarlo a createTravel, così posso accedere al contenuto delle immagini

    Scaffold(
        //topBar = topBar
    )
    { innerPadding ->

        if (travelId != null) {
            LaunchedEffect(travelId) {
                vm.getTravelById(travelId)
            }
            val travel by vm.currentTravel.collectAsState()

            LaunchedEffect(travel) {
                travel?.let { viewModel.loadTravelForEditing(it) }
            }
        }
        var currentStep by remember { mutableIntStateOf(0) }
        val title by viewModel.travelTitle.collectAsState()
        val description by viewModel.travelDescription.collectAsState()


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AirplanemodeActive,
                    contentDescription = "Travel Icon",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF60935D)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MY NEW TRAVEL",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .background(Color(0xFFC9DAC8), shape = RoundedCornerShape(24.dp))
            ) {
                IconButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC9DAC8).copy(alpha = 0.5f))
                ) {
                    Text("X", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when (currentStep) {
                            0 -> Step1(viewModel)
                            1 -> Step2(viewModel)
                            2 -> Step3(viewModel)
                            3 -> Step4(viewModel)
                            4 -> Step5(viewModel)
                            5 -> Step6(viewModel)
                            6 -> Step7(viewModel)
                        }
                    }
                }

                StepIndicator(
                    currentStep = currentStep + 1,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (currentStep > 0) {
                    Button(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.buttonColors( containerColor = Color(0xFF60935D))
                    ) {
                        Text("Back", color = Color.White)
                    }
                } else {
                    //Spacer(modifier = Modifier.width(1.dp))
                }

                //Spacer(modifier = Modifier.width(32.dp))

                if (currentStep < 6) {
                    Button(
                        onClick = { currentStep++ },
                        enabled = when (currentStep) {
                            0 -> title.isNotBlank() && description.isNotBlank()
                            1 -> viewModel.isStep2Valid()
                            2 -> viewModel.isStep3Valid()
                            3 -> viewModel.isStep4Valid()
                            4 -> viewModel.isStep5Valid()
                            5 -> viewModel.isStep6Valid()
                            else -> true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60935D))
                    ) {
                        Text("Next", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.createTravel(context, travelId) //travelId è null se è un nuovo viaggio, altrimenti è l'id del viaggio esistente da referenziare
                            viewModel.resetFields()
                            onNavigateToProfile()
                        },
                        enabled = viewModel.isStep7Valid(),
                        colors = ButtonDefaults.buttonColors( containerColor = Color(0xFF60935D))
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..7) {
            Text(
                text = "$i",
                fontWeight = if (i == currentStep) FontWeight.Bold else FontWeight.Normal,
                fontSize = 18.sp,
                color = if (i == currentStep) Color(0xFF1F4632) else Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}


@Composable
fun Step1(viewModel: AddTravelViewModel) {
    val travelTitle by viewModel.travelTitle.collectAsState()
    val travelDescription by viewModel.travelDescription.collectAsState()
    val imageUris = viewModel.imageUris
    var menuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.setImageUris(uris)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
        ) {
            if (imageUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(imageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Placeholder",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Modifica immagine",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Gray, CircleShape)
                    .padding(6.dp)
                    .clickable { menuExpanded = true }
            )

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Choose from gallery") },
                    onClick = {
                        menuExpanded = false
                        galleryLauncher.launch("image/*")
                    }
                )
            }
        }

        OutlinedTextField(
            value = travelTitle,
            onValueChange = { viewModel.setTravelTitle(it) },
            placeholder = { Text("Title", fontSize = 20.sp) },
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )

        OutlinedTextField(
            value = travelDescription,
            onValueChange = { viewModel.setTravelDescription(it) },
            label = { Text("Description", fontSize = 16.sp) },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )
    }
}

@Composable
fun Step2(viewModel: AddTravelViewModel) {
    val context = LocalContext.current

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startDate = viewModel.startDate
    val endDate = viewModel.endDate

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                calendar.set(Calendar.HOUR_OF_DAY, 12)  //per evitare problemi di fuso orario
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                viewModel.updateStartDate(calendar.time)  //startSate
                showStartPicker = false
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showEndPicker) {
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                /*calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)*/

                viewModel.updateEndDate(calendar.time)  //endDate
                showEndPicker = false
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        // set previous dates as disabled if start date is selected
        startDate?.let {
            dialog.datePicker.minDate = it.time
        }
        dialog.show()
    }

    val dateError = remember(startDate, endDate) {
        try {
            startDate != null && endDate != null
                    && endDate.before(startDate)
        } catch (e: Exception) {
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val startDateStr = startDate?.let { sdf.format(it) } ?: ""
        OutlinedTextField(
            value = startDateStr, //sdf.format(viewModel.startDate)
            onValueChange = {},
            readOnly = true,
            label = { Text("Start date") },
            placeholder = { Text("gg/mm/aaaa") },
            trailingIcon = {
                IconButton(onClick = { showStartPicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )
        val endDateStr = endDate?.let { sdf.format(it) } ?: ""
        OutlinedTextField(
            value = endDateStr,
            onValueChange = {},
            readOnly = true,
            isError = dateError,
            label = { Text("End date") },
            placeholder = { Text("gg/mm/aaaa") },
            trailingIcon = {
                IconButton(onClick = { showEndPicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )

        if (dateError) {
            Text(
                "The end date should be after the start date",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Age range: ${viewModel.ageRange.start.toInt()} - ${viewModel.ageRange.endInclusive.toInt()}")
            RangeSlider(
                value = viewModel.ageRange,
                onValueChange = { viewModel.updateAgeRange(it) },
                valueRange = 18f..100f,
                steps = 82,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF60935D),
                    activeTrackColor = Color(0xFF60935D),
                    inactiveTrackColor = Color.LightGray
                )
            )
        }

        OutlinedTextField(
            value = viewModel.groupSize,
            onValueChange = { input ->
                viewModel.updateGroupSize(input)
            },
            label = { Text("Group size") },
            placeholder = { Text("Es. 5") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )

    }
}


@Composable
fun Step3(viewModel: AddTravelViewModel) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startDate = viewModel.startDate
    val endDate = viewModel.endDate

    val startCalendar = Calendar.getInstance().apply {
        time = startDate ?: Date()
    }
    val endCalendar = Calendar.getInstance().apply {
        time = endDate ?: Date()
    }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val date = sdf.format(pickedCalendar.time)
                viewModel.updateSelectedDate(date)
                showDatePicker = false
            },
            startCalendar.get(Calendar.YEAR),
            startCalendar.get(Calendar.MONTH),
            startCalendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = startCalendar.timeInMillis
            datePicker.maxDate = endCalendar.timeInMillis
        }.show()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
    ) {

        Text(
            "Proposed Locations",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Date", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("Location", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
            Text("Night stay", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }

        val listState = rememberLazyListState()

        LaunchedEffect(viewModel.locations.size) {
            if (viewModel.locations.isNotEmpty()) {
                listState.animateScrollToItem(viewModel.locations.lastIndex)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
        ) {
            itemsIndexed(viewModel.locations) { index, (date, location, overnight) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    IconButton(onClick = {
                        viewModel.removeLocationAt(index)
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }

                    val dateParts = date.split("/")
                    val displayDate = if (dateParts.size >= 2) {
                        "${dateParts[0]}/${dateParts[1]}"
                    } else {
                        date
                    }

                    Text(
                        text = displayDate,
                        modifier = Modifier.width(64.dp),
                        fontSize = 14.sp
                    )

                    Text(
                        text = location,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )

                    if (overnight) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Overnight",
                            tint = Color(0xFF60935D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.locationName,
            onValueChange = { viewModel.updateLocationName(it) },
            label = { Text("Add a location") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )

        OutlinedTextField(
            value = viewModel.selectedDate,
            onValueChange = {},
            label = { Text("Select date") },
            placeholder = { Text("gg/mm/aaaa") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.overnightStay,
                onCheckedChange = { viewModel.updateOvernightStay(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF60935D),
                    uncheckedColor = Color.Gray,
                    checkmarkColor = Color.White
                )
            )
            Text("Overnight Stay")
        }

        Button(
            onClick = { viewModel.addLocationEntry() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60935D))
        ) {
            Text("Add location to list", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

    }
}


@Composable
fun Step4(viewModel: AddTravelViewModel) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startDate = viewModel.startDate ?: Date()
    val endDate = viewModel.endDate ?: Date()

    val diff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1

    LaunchedEffect(Unit) {
        viewModel.initializeItineraryList(diff)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = "Itinerary Icon",
                tint = Color(0xFF60935D),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ITINERARY",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(diff) { dayIndex ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF7EF), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "DAY ${dayIndex + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF60935D)
                    )
                    OutlinedTextField(
                        value = viewModel.itineraryDescriptions.getOrNull(dayIndex) ?: "",
                        onValueChange = {
                            viewModel.updateItinerary(dayIndex, it)
                        },
                        label = { Text("What will you do?") },
                        placeholder = { Text("Describe the plan...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF60935D),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF60935D),
                            focusedLabelColor = Color(0xFF60935D),
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

    }
}


@Composable
fun Step5(viewModel: AddTravelViewModel) {
    var activityText by remember { mutableStateOf("") }
    var isGroupActivity by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(16.dp),
    ) {

        Text(
            "Proposed Activities",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Activity", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
            Text("Group", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }

        Box(
            modifier = Modifier
                .weight(1f),
        ) {
            Column {
                viewModel.activityList.forEachIndexed { index, (text, isGroup) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.removeActivity(index) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Black)
                        }
                        Text(
                            text = text,
                            modifier = Modifier.weight(2f),
                            fontSize = 14.sp
                        )
                        if (isGroup) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Group",
                                tint = Color(0xFF60935D),
                                modifier = Modifier
                                    .weight(1f)
                                    .size(20.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = activityText,
            onValueChange = { activityText = it },
            label = { Text("Add an activity") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF60935D),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF60935D),
                focusedLabelColor = Color(0xFF60935D),
            ),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isGroupActivity,
                onCheckedChange = { isGroupActivity = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF60935D),
                    uncheckedColor = Color.Gray,
                    checkmarkColor = Color.White
                )
            )
            Text("Group Activity")
        }

        Button(
            onClick = {
                if (activityText.isNotBlank()) {
                    viewModel.addActivity(activityText, isGroupActivity)
                    activityText = ""
                    isGroupActivity = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60935D))
        ) {
            Text("Add activity to list", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

    }
}


@Composable
fun Step6(viewModel: AddTravelViewModel) {
    var notIncludedText by remember { mutableStateOf("") }
    val greenColor = Color(0xFF1F4632)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        ) {
        Text(
            text = "Price Range",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = greenColor,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "€",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = greenColor
            )

            BasicTextField(
                value = viewModel.price,
                onValueChange = {
                    val cleaned = it.replace(",", ".").filter { c -> c.isDigit() || c == '.' }
                    if (cleaned.count { ch -> ch == '.' } <= 1) {
                        viewModel.updatePrice(cleaned)
                    }
                },
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    color = greenColor,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                decorationBox = { innerTextField ->
                    if (viewModel.price.isEmpty()) {
                        Text(
                            text = "0",
                            fontSize = 28.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            )
        }



    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step7(viewModel: AddTravelViewModel) {
    val greenColor = Color(0xFF1F4632)
    var tagInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Add Tags to Your Travel",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = greenColor,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            placeholder = { Text("Example: adventure, relax") },
            trailingIcon = {
                IconButton(onClick = {
                    if (tagInput.isNotBlank()) {
                        viewModel.addTag(tagInput.trim())
                        tagInput = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add tag", tint = greenColor)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = greenColor,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = greenColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "Added Tags:",
            fontWeight = FontWeight.SemiBold,
            color = greenColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.tags.forEachIndexed { index, tag ->
                AssistChip(
                    onClick = { viewModel.removeTag(index) },
                    label = { Text(tag) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF60935D),
                        //labelColor = greenColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))



        Spacer(modifier = Modifier.height(100.dp))

    }
}
