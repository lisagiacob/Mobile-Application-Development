package com.example.lab2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.lab2.ui.theme.DarkGreen
import com.example.lab2.ui.theme.TransparentGreen


@Composable
fun PackingScreen(
    travelId: String,
    navController: NavHostController,
    topBar: @Composable () -> Unit,
    viewModel: PackingViewModel,
    userId: String,
) {
    val selectedNeeds = remember { mutableStateListOf<String>() }
    val selectedActivities = remember { mutableStateListOf<String>() }
    var carryOn by remember { mutableStateOf(true) }

    val needs = listOf("Essentials", "Documents", "Baby", "Couple")
    val activities = listOf("Sport", "Night Life", "Fotography", "Higene", "Clothes", "Elegant nights")

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Selezione necessaria") },
            text = { Text("Seleziona almeno una voce tra Needs e Activities per procedere.") }
        )
    }

    val hasItems = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkIfItemsExist(userId, travelId) { exists ->
            hasItems.value = exists
        }
    }

    Scaffold(topBar = topBar) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()), // per rendere tutto scrollabile se serve
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(TransparentGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PACKING THE SUITCASE",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryBox("Needs", needs, selectedNeeds)

                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryBox("Activities", activities, selectedActivities)

                    Spacer(modifier = Modifier.height(34.dp))

                    // Switch bag type
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 34.dp), // regola quanto spazio c'è ai lati,
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("        Carry on")
                            Text("Hold " +
                                    "luggage")
                        }

                        Switch(
                            checked = carryOn,
                            onCheckedChange = { carryOn = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkGreen,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = DarkGreen,
                                uncheckedTrackColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (selectedNeeds.isEmpty() && selectedActivities.isEmpty() && !hasItems.value) {
                                showDialog = true
                            } else {
                                val selectedCategories = selectedNeeds + selectedActivities
                                navController.currentBackStackEntry?.savedStateHandle?.set("travelId", travelId)
                                navController.currentBackStackEntry?.savedStateHandle?.set("selectedCategories", selectedCategories)
                                navController.navigate(Screen.ItemsScreen.base)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0B3E28),
                            contentColor = Color.White
                        )
                    ) {
                        Text("See all items")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBox(
    title: String,
    categories: List<String>,
    selected: SnapshotStateList<String>
) {
    Column(
        modifier = Modifier
            .border(1.dp, DarkGreen, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        // Manual wrap: 3 per riga
        categories.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                rowItems.forEach { label ->
                    val isSelected = selected.contains(label)
                    OutlinedButton(
                        onClick = {
                            if (isSelected) selected.remove(label)
                            else selected.add(label)
                        },
                        border = BorderStroke(1.dp, Color(0xFF0B3E28)),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFE6F0EC) else Color.Transparent,
                            contentColor = Color(0xFF0B3E28)
                        )
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}

val defaultItemsForCategory = mapOf(
    "Essentials" to listOf("Passport", "Painkillers", "House Keys"),
    "Documents" to listOf("ID Card", "Travel Insurance"),
    "Baby" to listOf("Diapers", "Wipes", "Bottle"),
    "Couple" to listOf("Gift", "Lingerie"),
    "Sport" to listOf("Sneakers", "Tracksuit"),
    "Night Life" to listOf("Heels", "Clutch"),
    "Fotography" to listOf("Camera", "Tripod", "SD Card"),
    "Higene" to listOf("Toothbrush", "Deodorant"),
    "Clothes" to listOf("5 Panties", "3 T-Shirts", "2 Pants", "1 Skirt", "Belt", "Heels", "Pochette"),
    "Elegant nights" to listOf("Dress", "Perfume", "Earrings")
)

@Composable
fun ItemsScreen(
    topBar: @Composable () -> Unit,
    userId: String,
    travelId: String,
    selectedCategories: List<String>,
    viewModel: PackingViewModel,
    onBack: () -> Unit,
    onNavigateToTravelProposal: (String) -> Unit
) {
    val itemStates by viewModel.itemStates.collectAsState()
    var newItem by remember { mutableStateOf("") }

    // Carica gli item salvati dal database
    LaunchedEffect(Unit) {
        val allSuggested = selectedCategories
            .flatMap { defaultItemsForCategory[it] ?: emptyList() }

        viewModel.loadItemsWithDefaults(userId, travelId, allSuggested)
    }

    Scaffold(
        topBar = topBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            // Item suggeriti per ogni categoria selezionata
            selectedCategories.forEach { category ->
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                val suggestedItems = defaultItemsForCategory[category] ?: emptyList()
                suggestedItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = itemStates[item] ?: false,
                            onCheckedChange = { checked ->
                                viewModel.toggleItem(item, checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = TransparentGreen,
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Item personalizzati già salvati
            val allSuggested = selectedCategories.flatMap { defaultItemsForCategory[it] ?: emptyList() }.toSet()
            val customItems = itemStates.keys.filterNot { it in allSuggested }

            if (customItems.isNotEmpty()) {
                Text("Your items", style = MaterialTheme.typography.titleMedium)
                customItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = itemStates[item] ?: false,
                            onCheckedChange = { checked ->
                                viewModel.toggleItem(item, checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = TransparentGreen,
                                uncheckedColor = Color.Black,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(item)
                    }
                }
            }

            Divider()
            Spacer(modifier = Modifier.height(28.dp))

            // Campo per item personalizzati
            Text("Add custom item", style = MaterialTheme.typography.titleMedium)
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){

                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    label = { Text("Enter item") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val trimmed = newItem.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.addCustomItem(trimmed)
                            newItem = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TransparentGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    viewModel.saveItems(userId, travelId)
                    onNavigateToTravelProposal(travelId)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TransparentGreen,
                    contentColor = Color.Black
                )
            ) {
                Text("Save")
            }
        }
    }
}
