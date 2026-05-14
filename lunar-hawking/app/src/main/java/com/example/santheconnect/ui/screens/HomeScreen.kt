package com.example.santheconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.santheconnect.data.MarketRepository
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.santheconnect.ui.theme.KarnatakaRed
import com.example.santheconnect.ui.theme.KarnatakaYellow
import com.example.santheconnect.ui.theme.Saffron

@OptIn(ExperimentalMaterial3Api::class)   // ✅ ONLY FIX ADDED
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Santhe Connect", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Saffron),
                actions = {
                    val auth = remember { FirebaseAuth.getInstance() }
                    val user = auth.currentUser
                    
                    IconButton(onClick = { navController.navigate("settings") }) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Saffron.copy(alpha = 0.1f), Color.White))),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Discover the Soul of Karnataka",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = KarnatakaRed
                )
            }

            item {
                FeatureCard(
                    title = "Local Eatery Map",
                    subtitle = "Find Local Food & Heritage Stays",
                    icon = Icons.Default.LocationOn,
                    backgroundColor = KarnatakaYellow,
                    onClick = { navController.navigate("map") }
                )
            }

            item {
                FeatureCard(
                    title = "Santhe Calendar",
                    subtitle = "Weekly markets schedule",
                    icon = Icons.Default.DateRange,
                    backgroundColor = Saffron,
                    onClick = { navController.navigate("calendar") }
                )
            }

            item {
                FeatureCard(
                    title = "Review Wall",
                    subtitle = "Voice notes & local experiences",
                    icon = Icons.Default.Star,
                    backgroundColor = KarnatakaRed.copy(alpha = 0.8f),
                    onClick = { navController.navigate("reviews") }
                )
            }

            item {
                val activeSanthes = remember { MarketRepository.getSanthesForToday() }
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = if (activeSanthes.isNotEmpty()) "Today's Specialty Tag" else "Upcoming Specialty",
                            fontWeight = FontWeight.Bold, 
                            color = Saffron
                        )
                        if (activeSanthes.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 8.dp)
                            ) {
                                activeSanthes.forEach { santhe ->
                                    SpecialtyTag(
                                        text = "${santhe.specialty} @ ${santhe.name}",
                                        color = Saffron
                                    )
                                }
                            }
                        } else {
                            Text("No active markets today. Check the calendar!", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Our Mission",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KarnatakaRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ImpactGoalItem(
                            title = "Inclusive Tourism",
                            description = "Spreading tourist spending beyond 5-star hotels into the local economy.",
                            color = KarnatakaYellow
                        )
                        ImpactGoalItem(
                            title = "Vocal for Local",
                            description = "Giving digital visibility to micro-entrepreneurs in food and retail.",
                            color = Saffron
                        )
                        ImpactGoalItem(
                            title = "Cultural Exchange",
                            description = "Preserving traditional Karnataka hospitality and heritage.",
                            color = KarnatakaRed
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Saffron),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                Icons.Default.Phone, 
                                contentDescription = null, 
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Helpline Support", 
                                fontWeight = FontWeight.Bold, 
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                            Text(
                                "+919019542275", 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SpecialtyTag(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ImpactGoalItem(title: String, description: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(top = 6.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Text(description, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}