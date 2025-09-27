package com.example.lab2

//import android.net.Uri

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.lab2.UserStore.getUserModelById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SearchTab {
    Users, Places
}

private val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")


@Composable
fun SearchScreen(
    navController: NavHostController,
) {

    val searchVm: SearchViewModel   = viewModel(factory = Factory)
    val userQuery   by searchVm.query.collectAsState()

    val filterVm: FilterViewModel   = viewModel(factory = Factory)
    val placeQuery by filterVm.filter.map { it.locationQuery }
        .collectAsState(initial = "")

    var selectedTab by rememberSaveable { mutableStateOf(SearchTab.Places) }
    var selectedUser by remember { mutableStateOf<UserModel?>(null) }       //if you put this as remember saveable when you open the travel list the app crashes (dont ask how i know or how i figured it out)

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var (showFilters, setShowFilters) = rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopBarSearchScreen(
                selectedTab     = selectedTab,
                isSearching     = isSearching,
                showFilters     = showFilters,
                setShowFilters = setShowFilters,
                query           = if (selectedTab == SearchTab.Users) userQuery else placeQuery,
                onQueryChange   = { newText ->
                    if (selectedTab == SearchTab.Users) {
                        searchVm.updateQuery(newText)
                    } else {
                        filterVm.updateFilter { copy(locationQuery = newText) }
                    }
                },
                onBackClick     = {
                    if (selectedTab == SearchTab.Places && !showFilters) {
                        showFilters = true
                    } else {
                        navController.navigate(Screen.Home.base)
                    }
                },               onStartSearch    = { isSearching = true },
                onCancelSearch   = { isSearching = false },
                onApplyFilters   = { setShowFilters(false) },
                modifier         = Modifier
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            RefinedToggle(
                selectedOption   = selectedTab,
                onOptionSelected = { tab ->
                    selectedTab = tab
                    isSearching = false
                    setShowFilters(true)
                }
            )

            when (selectedTab) {
                SearchTab.Users -> {
                    val results by searchVm.searchResults.collectAsState()
                    UserSearchContent(
                        searchResults  = results,
                        navController
                    )
                }
                SearchTab.Places -> {
                    PlacesSearchContent(
                        viewModel                   = filterVm,
                        placeQuery                  = placeQuery,
                        showFilters                 = showFilters,
                        onQueryChange               = { filterVm.updateFilter { copy(locationQuery = it) } },
                        onNavigateToTravelProposal = { travelId ->
                            navController.navigate(Screen.TravelProposal.createRoute(travelId))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSearchScreen(
    selectedTab: SearchTab,
    isSearching: Boolean,
    showFilters: Boolean,
    setShowFilters: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onStartSearch: () -> Unit,
    onCancelSearch: () -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onStartSearch() },
                    placeholder = { Text("Search...") },
                    //leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            onCancelSearch()
                            setShowFilters(true)
                            onQueryChange("")

                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "chat")
            }
        },
        actions = {
            IconButton(onClick = {
                if (selectedTab == SearchTab.Places && showFilters) {
                    onApplyFilters()
                }
            }) {
                Icon(Icons.Default.Search, contentDescription = "Apply Filters")
            }
        },
        modifier = modifier
    )
}

@Composable
fun RefinedToggle(
    selectedOption: SearchTab,
    onOptionSelected: (SearchTab) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60935D)
) {
    Row(
        modifier = modifier
            .wrapContentWidth()
            .height(40.dp)
            .border(
                border = BorderStroke(1.dp, activeColor),
                shape = RoundedCornerShape(50)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf(SearchTab.Users, SearchTab.Places)
        options.forEach { option ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (selectedOption == option) activeColor else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onOptionSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (option == SearchTab.Users) "Users" else "Places",
                    color = if (selectedOption == option) Color.White else activeColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun UserSearchContent(
    searchResults: List<UserModel>,
    navController: NavHostController
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(searchResults) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate(Screen.OtherUserProfile.routeWithUserId(user.id))

                    }
            ) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable {
                            navController.navigate(
                                Screen.OtherUserProfile.routeWithUserId(user.id)
                            )

                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.clip(CircleShape)) {
                        when (user.image) {
                            is UserImage.UrlImage -> Image(
                                painter = rememberAsyncImagePainter((user.image as UserImage.UrlImage).url),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            is UserImage.UriImage -> Image(
                                painter = rememberAsyncImagePainter((user.image as UserImage.UriImage).uri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            is UserImage.Resource -> Image(
                                painter = painterResource((user.image as UserImage.Resource).resId),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            null -> Text(
                                text = user.username.value.first().uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${user.firstName.value} ${user.lastName.value}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = user.username.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PlacesSearchContent(
    viewModel: FilterViewModel,
    placeQuery: String,
    showFilters: Boolean,
    onQueryChange: (String) -> Unit,
    onNavigateToTravelProposal: (String) -> Unit
) {
    val filter by viewModel.filter.collectAsState()
    val travels by viewModel.filteredTravels.collectAsState()

    Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
        ) {
            if(showFilters){
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ){

                    // — TAGS
                    TagRow(
                        options = listOf("Hiking","Relax", "Sea", "Rock Climbing"),
                        selected = filter.tags,
                        onToggle = { tag ->
                            viewModel.updateFilter {
                                // toggle tag in the filter set
                                val newTags = if (tags.contains(tag)) tags - tag else tags + tag
                                copy(tags = newTags)                }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    // — DATES
                    DatePickers(
                        start = filter.startDate,
                        end   = filter.endDate,
                        onStartChange = { date ->
                            viewModel.updateFilter { copy(startDate = date) }
                        },
                        onEndChange = { date ->
                            viewModel.updateFilter { copy(endDate = date) }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    // — AGE
                    AgeRow(
                        selectedRange   = filter.ageRange,
                        onRangeSelected = { newRange ->
                            viewModel.updateFilter { copy(ageRange = newRange) }
                        }
                    )

                    Spacer(Modifier.height(16.dp))


                    // — STATUS
                    StatusRow(
                        selectedStatus   = filter.status,
                        onStatusSelected = { newStatus ->
                            viewModel.updateFilter { copy(status = newStatus) }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    // — DURATION
                    DurationRangeInput(
                        lowerValue = filter.durationRange?.first?.toString().orEmpty(),
                        upperValue = filter.durationRange?.last?.toString().orEmpty(),
                        onValueChange  = { lowStr, highStr ->
                            viewModel.updateFilter {
                                val range = lowStr.toIntOrNull()?.let { low ->
                                    highStr.toIntOrNull()?.let { high -> low..high }
                                }
                                copy(durationRange = range)
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))


                    // — PRICE
                    PriceRangeInput(
                        start = filter.priceRange?.start
                            .toString()
                            .takeIf { it != "null" }
                            .orEmpty(),
                        end   = filter.priceRange?.endInclusive
                            .toString()
                            .takeIf { it != "null" }
                            .orEmpty(),
                        onChange = { lowStr, highStr ->
                            val range = lowStr.toDoubleOrNull()?.let { low ->
                                highStr.toDoubleOrNull()?.let { high -> low..high }
                            }
                            viewModel.updateFilter { copy(priceRange = range) }
                        }
                    )

                    Spacer(Modifier.height(16.dp))


                    // — FREE SPOTS
                    NumberInput(
                        "Number of free spots",
                        filter.freeSpotsMin?.toString().orEmpty(),
                        { committedText ->
                            viewModel.updateFilter {
                                copy(freeSpotsMin = committedText.toIntOrNull())
                            }
                        },
                        modifier     = Modifier,
                        activeColor  = Color(0xFF60935D),
                        suffixText   = "spots"
                    )

                    Spacer(Modifier.height(16.dp))


                    // — GROUP SIZE
                    GroupSizeRow(
                        selectedRange   = filter.groupSizeRange,
                        onRangeSelected = { newRange ->
                            viewModel.updateFilter { copy(groupSizeRange = newRange) }
                        }
                    )


                    TravelProposalList(
                        travels = travels,
                        onNavigateToTravelProposal = onNavigateToTravelProposal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }else{
                // — FILTERED LIST SCREEN —
                TravelProposalList(
                    travels                   = travels,
                    onNavigateToTravelProposal = onNavigateToTravelProposal,
                    modifier                  = Modifier.fillMaxSize()
                )
            }


        }



}




@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagRow(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Divider(color = Color.Gray, thickness = 1.dp)

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { tag ->
                val isSelected = selected.contains(tag)
                Button(
                    onClick = { onToggle(tag) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF60935D) else Color.Transparent,
                        contentColor = if (isSelected) Color.White else Color(0xFF60935D)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF60935D)),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(text = tag, softWrap = false) // evita il wrap interno
                }
            }
        }
    }
}


@SuppressLint("RememberReturnType")
@Composable
fun DatePickers(
    start: Date?,
    end: Date?,
    onStartChange: (Date?) -> Unit,
    onEndChange: (Date?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var startText by remember { mutableStateOf("") }
    LaunchedEffect(start) {
        startText = start?.let { dateFormat.format(it) }.orEmpty()
    }

    var endText by remember { mutableStateOf("") }
    LaunchedEffect(end) {
        endText = end?.let { dateFormat.format(it) }.orEmpty()
    }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }


    val startPicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)

                val startDate: Date = cal.time
                startText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)
                onStartChange(startDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val endPicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 23, 59, 59)
                cal.set(Calendar.MILLISECOND, 999)

                val endDate: Date = cal.time
                endText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)
                onEndChange(endDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Dates",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = startText,
                onValueChange = { new ->
                    startText = new
                    onStartChange(new.takeIf { it.isNotBlank() }?.let {
                        runCatching {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it)
                        }.getOrNull()
                    })
                },
                label = { Text("Start Date") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = { startPicker.show() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Start Date"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF60935D),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFF60935D),
                    focusedLabelColor = Color(0xFF60935D),
                ),
            )

            OutlinedTextField(
                value = endText,
                onValueChange = { new ->
                    endText = new
                    onEndChange(new.takeIf { it.isNotBlank() }?.let {
                        runCatching {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it)
                        }.getOrNull()
                    })
                },
                label = { Text("End Date") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = { endPicker.show()  }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select End Date"
                        )
                    }
                },
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


@Composable
fun AgeRow(
    selectedRange: IntRange?,
    onRangeSelected: (IntRange?) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60935D)
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Age",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf(18..25, 25..35, 35 ..45, 45 ..100 )) { range ->
                val isSelected = selectedRange == range
                Button(
                    onClick = {
                        onRangeSelected(if (isSelected) null else range)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) activeColor else Color.Transparent,
                        contentColor = if (isSelected) Color.White else activeColor
                    ),
                    shape = RoundedCornerShape(50.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, activeColor)
                ) {
                    Text(text = "${range.first} - ${range.last}")
                }
            }
        }
    }
}


@Composable
fun StatusRow(
    selectedStatus: TravelStatus?,
    onStatusSelected: (TravelStatus?) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60935D)
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(TravelStatus.AVAILABLE, TravelStatus.ENDED).forEach { status ->
                val label = status.name.lowercase().replaceFirstChar(Char::titlecase)
                val isSelected = selectedStatus == status

                Button(
                    onClick = {
                        onStatusSelected(if (isSelected) null else status)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) activeColor else Color.Transparent,
                        contentColor    = if (isSelected) Color.White   else activeColor
                    ),
                    shape = RoundedCornerShape(50.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, activeColor)
                ) {
                    Text(text = label)
                }
            }
        }
    }
}


@Composable
fun DurationRangeInput(
    lowerValue: String,
    upperValue: String,
    onValueChange: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60935D)
) {
    var lowerText by remember { mutableStateOf(lowerValue) }
    var upperText by remember { mutableStateOf(upperValue) }

    LaunchedEffect(lowerValue) { if (lowerValue != lowerText) lowerText = lowerValue }
    LaunchedEffect(upperValue) { if (upperValue != upperText) upperText = upperValue }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Duration", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = lowerText,
                onValueChange = { new ->
                    lowerText = new
                    onValueChange(new, upperText)
                },
                placeholder = { Text("0", color = Color.White.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = activeColor,
                    unfocusedContainerColor = activeColor,
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    cursorColor             = Color.White
                ),
                suffix = { Text(" days", color = Color.White) }
            )

            Box(modifier = Modifier.size(width = 30.dp, height = 1.dp).background(Color.Gray))

            OutlinedTextField(
                value = upperText,
                onValueChange = { new ->
                    upperText = new
                    onValueChange(lowerText, new)
                },
                placeholder = { Text("0", color = Color.White.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = activeColor,
                    unfocusedContainerColor = activeColor,
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    cursorColor             = Color.White
                ),
                suffix = { Text(" days", color = Color.White) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}



@Composable
fun PriceRangeInput(
    start: String,
    end: String,
    onChange: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60935D)
) {
    var startText by remember { mutableStateOf(start) }
    var endText   by remember { mutableStateOf(end) }

    LaunchedEffect(start) { if (start != startText) startText = start }
    LaunchedEffect(end)   { if (end   != endText)   endText   = end }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Price", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = startText,
                onValueChange = { new ->
                    startText = new
                    onChange(new, endText)
                },
                placeholder = { Text("0", color = Color.White.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = activeColor,
                    unfocusedContainerColor = activeColor,
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    cursorColor             = Color.White
                ),
                suffix = { Text(" €", color = Color.White) }
            )

            Box(modifier = Modifier.size(width = 30.dp, height = 1.dp).background(Color.Gray))

            OutlinedTextField(
                value = endText,
                onValueChange = { new ->
                    endText = new
                    onChange(startText, new)
                },
                placeholder = { Text("0", color = Color.White.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = activeColor,
                    unfocusedContainerColor = activeColor,
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    cursorColor             = Color.White
                ),
                suffix = { Text(" €", color = Color.White) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun NumberInput(
    label: String,
    initialValue: String,
    onValueCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60935D),
    suffixText: String = ""
) {
    var text by remember { mutableStateOf(initialValue) }

    LaunchedEffect(initialValue) {
        if (initialValue != text) text = initialValue
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { new ->
                    text = new
                },
                placeholder = { Text("0", color = Color.White.copy(0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = activeColor,
                    unfocusedContainerColor = activeColor,
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    cursorColor             = Color.White
                ),
                suffix = {
                    if (suffixText.isNotEmpty()) Text(" $suffixText", color = Color.White)
                }
            )


        }
        Spacer(Modifier.height(16.dp))
    }
}




@Composable
fun GroupSizeRow(
    selectedRange: IntRange?,
    onRangeSelected: (IntRange?) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60935D)
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Group size",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5..8, 8..10, 10 .. 15/*, 0..4*/).forEach { range ->
                val label = "${range.first} - ${range.last} people"
                val isSelected = selectedRange == range

                Button(
                    onClick = {
                        onRangeSelected(if (isSelected) null else range)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) activeColor else Color.Transparent,
                        contentColor    = if (isSelected) Color.White   else activeColor
                    ),
                    shape  = RoundedCornerShape(50.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, activeColor)
                ) {
                    Text(text = label)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}




@Composable
fun TravelProposalList(
    travels: List<Travel>,
    onNavigateToTravelProposal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(travels, key = { it.id }) { travel ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 2.dp,
                        color = Color(0xFF60935D),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onNavigateToTravelProposal(travel.id) }
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






















