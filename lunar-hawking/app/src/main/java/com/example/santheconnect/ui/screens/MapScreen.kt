package com.example.santheconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.santheconnect.data.MarketRepository
import com.example.santheconnect.data.Santhe
import com.example.santheconnect.data.Eatery
import com.example.santheconnect.ui.theme.Saffron
import com.example.santheconnect.ui.theme.KarnatakaRed
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    // Initialize with static data immediately for slow internet support
    var santhes by remember { mutableStateOf(MarketRepository.getStaticSanthes()) }
    var eateries by remember { mutableStateOf(MarketRepository.getStaticEateries()) } 
    LaunchedEffect(Unit) {
        // Fetch remote data in background
        santhes = MarketRepository.getAllSanthes()
        eateries = MarketRepository.getAllEateries()
    }
    val today = remember { java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) }
    
    var showOnlyToday by remember { mutableStateOf(false) }
    var capturedLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Visibility toggles
    var showSanthes by remember { mutableStateOf(true) }
    var showStays by remember { mutableStateOf(true) }
    var showFood by remember { mutableStateOf(true) }

    // Optimization: Pre-calculate filtered list to make "Today Only" switch instant
    val visibleSanthes by remember(santhes, showOnlyToday) {
        derivedStateOf {
            if (showOnlyToday) {
                santhes.filter { it.dayOfWeek == today }
            } else {
                santhes
            }
        }
    }
    
    val dharwad = LatLng(15.4589, 75.0078)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(dharwad, 10f)
    }

    var selectedSanthe by remember { mutableStateOf<Santhe?>(null) }
    var selectedEatery by remember { mutableStateOf<Eatery?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var showNearByDialog by remember { mutableStateOf(false) }
    var nearByCategory by remember { mutableStateOf<String?>(null) } // "Eatery" or "Home-stay"
    var eaterySubCategory by remember { mutableStateOf<String?>(null) } // "Jolada Rotti" or "Thatte Idli"
    var isAutoSearching by remember { mutableStateOf(false) }
    var isDiscoveryMode by remember { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            
            val onLocationAcquired = { location: android.location.Location ->
                capturedLocation = LatLng(location.latitude, location.longitude)
                scope.launch {
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(capturedLocation!!, 15f),
                        500 // Faster animation
                    )
                }
                
                // DISCOVERY MODE (Dual Discovery: Stay & Specialty Food)
                if (isDiscoveryMode) {
                    val currentEateries = eateries
                    val currentSanthes = santhes
                    
                    // Search both Eateries and Santhes for a "Stay"
                    val nearestStayEatery = currentEateries.filter { 
                        it.type == "Home-stay" || MarketRepository.stayKeywords.any { k -> it.name.contains(k, true) || it.specialty.contains(k, true) }
                    }.minByOrNull { calculateDistance(it.latitude, it.longitude, location.latitude, location.longitude) }
                    
                    val nearestStaySanthe = currentSanthes.filter { 
                        MarketRepository.stayKeywords.any { k -> it.name.contains(k, true) || it.specialty.contains(k, true) }
                    }.minByOrNull { calculateDistance(it.latitude, it.longitude, location.latitude, location.longitude) }

                    val nearestStay = if (nearestStayEatery != null && nearestStaySanthe != null) {
                        if (calculateDistance(nearestStayEatery.latitude, nearestStayEatery.longitude, location.latitude, location.longitude) < 
                            calculateDistance(nearestStaySanthe.latitude, nearestStaySanthe.longitude, location.latitude, location.longitude)) nearestStayEatery else nearestStaySanthe
                    } else nearestStayEatery ?: nearestStaySanthe

                    // Search both for Specialty Food
                    val nearestFoodEatery = currentEateries.filter { eatery ->
                        eatery.type == "Food" && MarketRepository.foodKeywords.any { keyword ->
                            eatery.specialty.contains(keyword, ignoreCase = true)
                        }
                    }.minByOrNull { calculateDistance(it.latitude, it.longitude, location.latitude, location.longitude) }

                    val nearestFoodSanthe = currentSanthes.filter { santhe ->
                        MarketRepository.foodKeywords.any { keyword ->
                            santhe.specialty.contains(keyword, ignoreCase = true)
                        }
                    }.minByOrNull { calculateDistance(it.latitude, it.longitude, location.latitude, location.longitude) }

                    val nearestFood = if (nearestFoodEatery != null && nearestFoodSanthe != null) {
                        if (calculateDistance(nearestFoodEatery.latitude, nearestFoodEatery.longitude, location.latitude, location.longitude) < 
                            calculateDistance(nearestFoodSanthe.latitude, nearestFoodSanthe.longitude, location.latitude, location.longitude)) nearestFoodEatery else nearestFoodSanthe
                    } else nearestFoodEatery ?: nearestFoodSanthe

                    if (nearestStay != null || nearestFood != null) {
                        val stayName = when(nearestStay) {
                            is Eatery -> (nearestStay as Eatery).name
                            is Santhe -> (nearestStay as Santhe).name
                            else -> "None found"
                        }
                        val foodName = when(nearestFood) {
                            is Eatery -> (nearestFood as Eatery).name
                            is Santhe -> (nearestFood as Santhe).name
                            else -> "None found"
                        }
                        android.widget.Toast.makeText(context, "✨ Nearest Eatery Stay: $stayName\n🍲 Nearest Specialty: $foodName", android.widget.Toast.LENGTH_LONG).show()
                        
                        val target = nearestStay ?: nearestFood
                        if (target != null) {
                            val targetLat = if (target is Eatery) (target as Eatery).latitude else (target as Santhe).latitude
                            val targetLon = if (target is Eatery) (target as Eatery).longitude else (target as Santhe).longitude
                            scope.launch {
                                cameraPositionState.animate(
                                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(LatLng(targetLat, targetLon), 16f),
                                    500
                                )
                                if (target is Eatery) {
                                    selectedEatery = target as Eatery
                                    selectedSanthe = null
                                } else {
                                    selectedSanthe = target as Santhe
                                    selectedEatery = null
                                }
                                showSheet = true
                            }
                        }
                    } else {
                        android.widget.Toast.makeText(context, "No local eateries or stays found nearby.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    isDiscoveryMode = false
                }
            }

            try {
                // Try lastLocation for instant result (great for slow internet/GPS)
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) onLocationAcquired(loc)
                }

                // Also start high-accuracy request for better precision
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                fusedLocationClient.getCurrentLocation(request, CancellationTokenSource().token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            onLocationAcquired(location)
                        } else {
                            isAutoSearching = false
                            isDiscoveryMode = false
                            android.widget.Toast.makeText(context, "Could not acquire GPS. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        isAutoSearching = false
                        isDiscoveryMode = false
                        android.widget.Toast.makeText(context, "Location error: ${it.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
            } catch (e: SecurityException) {
                isAutoSearching = false
                isDiscoveryMode = false
            }
        } else {
            isAutoSearching = false
            isDiscoveryMode = false
            android.widget.Toast.makeText(context, "Location permission denied", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Karnataka Explorer Map 🍛", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Today Only", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = showOnlyToday,
                            onCheckedChange = { showOnlyToday = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KarnatakaRed
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Saffron)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    locationPermissionLauncher.launch(arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                },
                containerColor = KarnatakaRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Capture GPS")
            }
        }
    ) { padding ->
        // Wrap everything in a Box with a background color to confirm screen is active
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.LightGray.copy(alpha = 0.1f)) // Fallback visible area
        ) {
            GoogleMap(
                modifier = Modifier.matchParentSize(), // Ensure it takes all available space
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false, // Clean up UI
                    myLocationButtonEnabled = hasLocationPermission
                ),
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                )
            ) { 
                // Santhe Markers (Markets & Crafts)
                if (showSanthes) {
                    for (santhe in visibleSanthes) {
                        val isCraft = santhe.name.contains("Handloom", ignoreCase = true) || 
                                      santhe.name.contains("Woodcraft", ignoreCase = true)
                        val isStay = MarketRepository.stayKeywords.any { santhe.specialty.contains(it, true) || santhe.name.contains(it, true) }
                        
                        // If it's a stay but showStays is off, skip (handled by eatery logic mostly, but Santhes can be stays too)
                        if (isStay && !showStays) continue

                        Marker(
                            state = MarkerState(position = LatLng(santhe.latitude, santhe.longitude)),
                            title = if (isStay) "🏡 ${santhe.name}" else if (isCraft) "🎨 ${santhe.name}" else "🛍️ ${santhe.name}",
                            snippet = "Specialty: ${santhe.specialty} (${santhe.village})",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (isStay) BitmapDescriptorFactory.HUE_GREEN else if (isCraft) BitmapDescriptorFactory.HUE_VIOLET else BitmapDescriptorFactory.HUE_ORANGE
                            ),
                            onClick = {
                                selectedSanthe = santhe
                                selectedEatery = null
                                showSheet = true
                                false
                            }
                        )
                    }
                }

                // Eatery Markers (Food & Stay)
                for (eatery in eateries) {
                    val isHomeStay = eatery.type == "Home-stay"
                    
                    if (isHomeStay && !showStays) continue
                    if (!isHomeStay && !showFood) continue

                    Marker(
                        state = MarkerState(position = LatLng(eatery.latitude, eatery.longitude)),
                        title = if (isHomeStay) "🏡 ${eatery.name}" else "🍲 ${eatery.name}",
                        snippet = "${eatery.specialty} (${eatery.type})",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isHomeStay) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                        ),
                        onClick = {
                            selectedEatery = eatery
                            selectedSanthe = null
                            showSheet = true
                            false
                        }
                    )
                }

                // Captured GPS Marker
                capturedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "📍 My New Location",
                        snippet = "Captured via GPS",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                    )
                }
            } // Close GoogleMap correctly

            // Legend & Info - UPDATED FOR CLARITY
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .width(180.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🗺️ FILTERS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = KarnatakaRed)
                    Divider(Modifier.padding(vertical = 8.dp))
                    
                    ToggleLegendItem(Icons.Default.Home, "Eatery Stays", Color(0xFF388E3C), showStays) { showStays = it }
                    ToggleLegendItem(Icons.Default.Restaurant, "Eateries", Color.Red, showFood) { showFood = it }
                    ToggleLegendItem(Icons.Default.ShoppingBag, "Santhes", Saffron, showSanthes) { showSanthes = it }
                    
                    Divider(Modifier.padding(vertical = 8.dp))
                    
                    LegendItem(Icons.Default.MyLocation, "Quick Discover ✨", Color.Cyan) {
                        isDiscoveryMode = true
                        locationPermissionLauncher.launch(arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                }
            }
        } // End Box
    } // End Scaffold

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            DetailContent(
                santhe = selectedSanthe,
                eatery = selectedEatery,
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                    }
                }
            )
        }
    }

    if (showNearByDialog) {
        AlertDialog(
            onDismissRequest = { 
                showNearByDialog = false
                nearByCategory = null
                eaterySubCategory = null
            },
            title = { Text("Find Nearest 🔍", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (nearByCategory == null) {
                        Text("What are you looking for?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { nearByCategory = "Eatery" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                            ) { Text("Eatery 🍲") }
                            Button(
                                onClick = { nearByCategory = "Home-stay" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                            ) { Text("Eatery Map 🏡") }
                        }
                    } else if (nearByCategory == "Eatery" && eaterySubCategory == null) {
                        Text("Select Specialty:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Card(onClick = { eaterySubCategory = "Jolada Rotti" }) {
                                    AsyncImage(
                                        model = "https://images.unsplash.com/photo-1626074353765-517a681e40be?q=80&w=200",
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Text("Jolada Rotti", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Card(onClick = { eaterySubCategory = "Thatte Idli" }) {
                                    AsyncImage(
                                        model = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?q=80&w=200",
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Text("Thatte Idli", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    } else {
                        val selection = if (nearByCategory == "Home-stay") "Home Stay" else eaterySubCategory
                        Text("Finding nearest $selection near you...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KarnatakaRed)
                        
                        Button(
                            onClick = {
                                isAutoSearching = true
                                // Trigger location capture
                                locationPermissionLauncher.launch(arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = KarnatakaRed)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Near Me (Start Search)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { 
                    showNearByDialog = false
                    nearByCategory = null
                    eaterySubCategory = null
                }) { Text("Cancel") }
            }
        )
    }
} // End MapScreen

// Distance calculation helper
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371 // radius of earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

@Composable
fun ToggleLegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    label: String, 
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.7f),
            colors = SwitchDefaults.colors(checkedTrackColor = color)
        )
    }
}

@Composable
fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    label: String, 
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .then(if (onClick != null) Modifier.clickable { onClick.invoke() } else Modifier)
            .padding(horizontal = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun DetailContent(santhe: Santhe?, eatery: Eatery?, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        val title = santhe?.name ?: eatery?.name ?: ""
        val subtitle = if (santhe != null) {
            "Village: ${santhe.village}"
        } else {
            val typeLabel = if (eatery?.type == "Home-stay") "Local Eatery Stay 🏡" else "Local Food 🍲"
            "Category: $typeLabel"
        }
        val specialty = santhe?.specialty ?: eatery?.specialty ?: ""
        val imageUrl = eatery?.imageUrl

        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = Color.DarkGray, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = Saffron.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Specialty", fontWeight = FontWeight.Black, color = Saffron, fontSize = 14.sp)
                Text(specialty, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (imageUrl != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("📸 Local Photo / Menu", fontWeight = FontWeight.Black, color = KarnatakaRed, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = imageUrl,
                contentDescription = "Menu Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        val context = LocalContext.current
        Button(
            onClick = {
                val lat = santhe?.latitude ?: eatery?.latitude ?: 0.0
                val lon = santhe?.longitude ?: eatery?.longitude ?: 0.0
                val gmmIntentUri = android.net.Uri.parse("google.navigation:q=$lat,$lon")
                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try {
                    context.startActivity(mapIntent)
                } catch (e: Exception) {
                    val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, 
                        android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon"))
                    context.startActivity(webIntent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = KarnatakaRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Get Directions 🍛", fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}