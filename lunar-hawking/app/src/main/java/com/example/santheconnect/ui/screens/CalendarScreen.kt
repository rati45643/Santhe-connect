package com.example.santheconnect.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.santheconnect.data.MarketRepository
import com.example.santheconnect.data.Santhe
import com.example.santheconnect.ui.theme.KarnatakaRed
import com.example.santheconnect.ui.theme.Saffron
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var santhes by remember { mutableStateOf<List<Santhe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Refresh list function
    val refreshList = {
        scope.launch {
            isLoading = true
            santhes = MarketRepository.getAllSanthes()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshList()
    }

    val filteredSanthes = santhes.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.village.contains(searchQuery, ignoreCase = true)
    }
    
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Santhe Calendar", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Saffron)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = KarnatakaRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Santhe")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search location or name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Saffron,
                    cursorColor = Saffron
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Saffron)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSanthes) { santhe ->
                        val isActiveToday = santhe.dayOfWeek == today
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActiveToday) Saffron.copy(alpha = 0.1f) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(if (isActiveToday) 4.dp else 1.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(santhe.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Village: ${santhe.village}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    
                                    // Directions button removed as per user request

                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Specialty: ${santhe.specialty}", style = MaterialTheme.typography.bodySmall)
                                        Text("Day: ${getDayName(santhe.dayOfWeek)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    
                                    if (isActiveToday) {
                                        Surface(
                                            color = Saffron,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                "ACTIVE TODAY",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSantheDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newSanthe ->
                scope.launch {
                    val success = MarketRepository.addSanthe(newSanthe)
                    if (success) {
                        Toast.makeText(context, "Santhe added successfully!", Toast.LENGTH_SHORT).show()
                        refreshList()
                    } else {
                        Toast.makeText(context, "Failed to add Santhe", Toast.LENGTH_SHORT).show()
                    }
                }
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSantheDialog(onDismiss: () -> Unit, onSave: (Santhe) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(Calendar.MONDAY) }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var isLocating by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Re-trigger location capture
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Your Local Santhe", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Santhe Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Place (Village/Town)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Specialty (e.g. Chillies, Silk)") }, modifier = Modifier.fillMaxWidth())
                
                Text("Select Day of Week", fontWeight = FontWeight.Bold, color = KarnatakaRed)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..7).forEach { day ->
                        FilterChip(
                            selected = dayOfWeek == day,
                            onClick = { dayOfWeek = day },
                            label = { Text(getDayNameShort(day)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Saffron,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text("Location Coordinates", fontWeight = FontWeight.Bold, color = KarnatakaRed)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = if (latitude == 0.0) "" else latitude.toString(),
                        onValueChange = { latitude = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = if (longitude == 0.0) "" else longitude.toString(),
                        onValueChange = { longitude = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            isLocating = true
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { location ->
                                    isLocating = false
                                    if (location != null) {
                                        latitude = location.latitude
                                        longitude = location.longitude
                                        Toast.makeText(context, "Location Captured!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    enabled = !isLocating
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isLocating) "Auto-detect GPS" else "Use Current GPS")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && village.isNotBlank() && latitude != 0.0) {
                        onSave(Santhe(id = java.util.UUID.randomUUID().toString(), name = name, village = village, specialty = specialty, dayOfWeek = dayOfWeek, latitude = latitude, longitude = longitude))
                    } else {
                        Toast.makeText(context, "Please fill all fields and capture location", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = KarnatakaRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun getDayName(day: Int): String = when(day) {
    Calendar.SUNDAY -> "Sunday"
    Calendar.MONDAY -> "Monday"
    Calendar.TUESDAY -> "Tuesday"
    Calendar.WEDNESDAY -> "Wednesday"
    Calendar.THURSDAY -> "Thursday"
    Calendar.FRIDAY -> "Friday"
    Calendar.SATURDAY -> "Saturday"
    else -> ""
}

private fun getDayNameShort(day: Int): String = when(day) {
    Calendar.SUNDAY -> "Sun"
    Calendar.MONDAY -> "Mon"
    Calendar.TUESDAY -> "Tue"
    Calendar.WEDNESDAY -> "Wed"
    Calendar.THURSDAY -> "Thu"
    Calendar.FRIDAY -> "Fri"
    Calendar.SATURDAY -> "Sat"
    else -> ""
}
