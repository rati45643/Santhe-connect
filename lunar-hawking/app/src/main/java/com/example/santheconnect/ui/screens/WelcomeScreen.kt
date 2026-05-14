package com.example.santheconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.santheconnect.ui.theme.Saffron
import com.example.santheconnect.ui.theme.KarnatakaRed

@Composable
fun WelcomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Beautiful Background Image
        AsyncImage(
            model = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=1000",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Stronger Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.9f)),
                        startY = 0f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // App Name & Branding
            Text(
                "Santhe Connect",
                style = MaterialTheme.typography.displaySmall.copy(
                    shadow = Shadow(color = Color.Black, offset = Offset(4f, 4f), blurRadius = 8f)
                ),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            
            Text(
                "Connecting Karnataka's Soul to the World",
                color = Saffron,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                "Discover hidden village markets, taste authentic local food, and support our traditional craftsmen. Every visit makes a difference.",
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Achievements Section
            Text(
                "Our Impact so far",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AchievementCard("200+", "Local Markets", Icons.Default.Verified)
                AchievementCard("10k+", "Happy Tourists", Icons.Default.Star)
                AchievementCard("₹50L+", "Local Revenue", Icons.Default.Verified)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Reviews Section
            Text(
                "What People Say",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(sampleReviews) { review ->
                    ReviewWelcomeCard(review)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            // Get Started Button
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KarnatakaRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AchievementCard(count: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp)
            .width(80.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Saffron, modifier = Modifier.size(24.dp))
        Text(count, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.LightGray, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun ReviewWelcomeCard(review: WelcomeReview) {
    Card(
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Saffron)
                ) {
                    Text(
                        review.userName.take(1),
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(review.userName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\"${review.comment}\"",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                maxLines = 3
            )
        }
    }
}

data class WelcomeReview(val userName: String, val comment: String)
val sampleReviews = listOf(
    WelcomeReview("Aditi M.", "Found the best Jolada Rotti in Hubli! Truly life-changing experience."),
    WelcomeReview("Rahul S.", "Supportive local artisans. I bought amazing Channapatna toys."),
    WelcomeReview("Sneha K.", "The map is so accurate. Found a tiny market in Coorg!")
)
