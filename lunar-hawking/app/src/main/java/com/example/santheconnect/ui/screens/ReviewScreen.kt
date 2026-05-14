package com.example.santheconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.santheconnect.ui.theme.KarnatakaRed
import com.example.santheconnect.ui.theme.Saffron
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import com.example.santheconnect.data.Review
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import com.example.santheconnect.data.ReviewRepository
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val currentUserId = remember(auth.currentUser) { auth.currentUser?.uid ?: "" }
    val scope = rememberCoroutineScope()
    val reviews by ReviewRepository.getReviews().collectAsState(initial = emptyList())

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Form State
    var userName by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }
    var hasVoiceSelected by remember { mutableStateOf(false) }
    var photoUrl by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var rating by remember { mutableIntStateOf(5) }
    var isSaving by remember { mutableStateOf(false) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }

    // Speech Recognizer Setup
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer.destroy() }
    }



    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isRecording = false
                }

                override fun onError(error: Int) {
                    isRecording = false
                }

                override fun onResults(results: Bundle?) {
                    val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!data.isNullOrEmpty()) {
                        reviewText = if (reviewText.isEmpty()) data[0] else "$reviewText ${data[0]}"
                        hasVoiceSelected = true
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer.startListening(speechIntent)
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Review Wall", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = KarnatakaRed)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    userName = ""
                    reviewText = ""
                    hasVoiceSelected = false
                    photoUrl = ""
                    rating = 5
                    isSaving = false
                    showBottomSheet = true
                },
                containerColor = KarnatakaRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Review")
            }
        }
    ) { padding ->
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp, top = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSaving) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            color = KarnatakaRed
                        )
                    }
                    Text(
                        "Share Your Experience",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = KarnatakaRed
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Your Name (Optional)") },
                        placeholder = { Text(auth.currentUser?.displayName ?: "Anonymous") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = KarnatakaRed,
                            cursorColor = KarnatakaRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Your Review") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = KarnatakaRed,
                            cursorColor = KarnatakaRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        trailingIcon = {
                            if (reviewText.isNotEmpty()) {
                                IconButton(onClick = { reviewText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear text")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = { photoUrl = it },
                        label = { Text("Photo URL (Optional)") },
                        placeholder = { Text("https://example.com/image.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = KarnatakaRed,
                            cursorColor = KarnatakaRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        trailingIcon = {
                            if (photoUrl.isNotEmpty()) {
                                IconButton(onClick = { photoUrl = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear URL")
                                }
                            }
                        }
                    )

                    // ImgBB Instructions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "How to add a Photo:",
                            style = MaterialTheme.typography.titleSmall,
                            color = KarnatakaRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "1. Go to imgbb.com on your Laptop (No mobile phone).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                        Text(
                            "2. Upload your image (do nothing else).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                        Text(
                            "3. In 'Embed codes', select 'Direct link'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                        Text(
                            "4. Copy the link and paste it in the box above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://imgbb.com/"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("Open ImgBB.com", fontSize = 14.sp)
                        }
                    }



                    if (photoUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Preview",
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.1f)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image),
                            placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Rating",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { index ->
                            IconButton(onClick = { rating = index + 1 }) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star ${index + 1}",
                                    tint = Saffron,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OptionButton(
                            icon = if (isRecording) Icons.Default.GraphicEq else Icons.Default.Mic,
                            label = if (isRecording) "Listening..." else if (hasVoiceSelected) "Add More Voice" else "Voice to Text",
                            isSelected = hasVoiceSelected || isRecording,
                            onClick = {
                                audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (reviewText.isNotEmpty()) {
                                scope.launch {
                                    isSaving = true
                                    val finalName = userName.ifBlank { auth.currentUser?.displayName ?: "Anonymous" }

                                    val newReview = Review(
                                        author = finalName,
                                        authorId = currentUserId,
                                        content = reviewText,
                                        hasVoiceNote = hasVoiceSelected,
                                        hasPhoto = photoUrl.isNotBlank(),
                                        photoUrl = photoUrl.ifBlank { null },
                                        rating = rating
                                    )

                                    ReviewRepository.addReview(newReview)

                                    android.widget.Toast.makeText(context, "Review saved!", android.widget.Toast.LENGTH_SHORT).show()
                                    isSaving = false
                                    showBottomSheet = false
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Please write a review", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isSaving) "Saving..." else "Save Review", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (selectedPhotoUrl != null) {
            PhotoViewerDialog(
                url = selectedPhotoUrl!!,
                onDismiss = { selectedPhotoUrl = null }
            )
        }

        if (reviewToDelete != null) {
            AlertDialog(
                onDismissRequest = { reviewToDelete = null },
                title = { Text("Delete Review", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this review?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            reviewToDelete?.let { review ->
                                scope.launch {
                                    ReviewRepository.deleteReview(review.id, currentUserId)
                                }
                            }
                            reviewToDelete = null
                        }
                    ) {
                        Text("Delete", color = KarnatakaRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reviewToDelete = null }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color.White
            )
        }
        

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Tourist Experiences",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = KarnatakaRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Voice notes & photos from fellow travelers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            if (reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Forum,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No reviews yet. Be the first to post!",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        currentUserId = currentUserId,
                        onDelete = {
                            reviewToDelete = review
                        },
                        onPhotoClick = { url -> selectedPhotoUrl = url }
                    )
                }
            }
        }
    }
}

@Composable
fun OptionButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) Saffron.copy(alpha = 0.2f) else KarnatakaRed.copy(alpha = 0.1f),
            modifier = Modifier.size(64.dp),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Saffron) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) Saffron else KarnatakaRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Saffron else Color.Black
        )
    }
}

@Composable
fun ReviewCard(review: Review, currentUserId: String, onDelete: () -> Unit, onPhotoClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Saffron.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(review.author.take(1), fontWeight = FontWeight.Bold, color = Saffron)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        review.author,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < review.rating) Saffron else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                // STRICT PERMISSION CHECK: Only show if logged in AND IDs match exactly
                val isOwner = currentUserId.isNotEmpty() && review.authorId.isNotEmpty() && review.authorId == currentUserId

                if (isOwner) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                review.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!review.photoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = review.photoUrl,
                    contentDescription = "Review Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                        .clickable { onPhotoClick(review.photoUrl) },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image),
                    placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                )
            }

            if (review.hasVoiceNote || review.hasPhoto) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (review.hasVoiceNote) {
                        ReviewTag(Icons.Default.Mic, "Voice Note", KarnatakaRed)
                    }
                    if (review.hasPhoto) {
                        ReviewTag(
                            icon = Icons.Default.Photo,
                            label = "Photo",
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewTag(icon: ImageVector, label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PhotoViewerDialog(url: String, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Full Screen Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}