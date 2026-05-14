package com.example.santheconnect.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.santheconnect.ui.theme.KarnatakaRed
import com.example.santheconnect.ui.theme.Saffron
import com.google.firebase.auth.userProfileChangeRequest
import coil.compose.AsyncImage
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var photoUrl by remember { mutableStateOf(user?.photoUrl?.toString() ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KarnatakaRed)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Immersive Background
            AsyncImage(
                model = "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?q=80&w=1000",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // Dark Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f))
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
                // Profile Photo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Saffron.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.photoUrl != null) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = KarnatakaRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.displayName ?: "No Name Set",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = user?.email ?: "No Email",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Change Display Name
                Text("Update Name", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Display Name", color = Color.White) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Saffron,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Saffron
                    )
                )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    isLoading = true
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Name updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                enabled = !isLoading
            ) {
                Text("Save Name")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Update Profile Image URL
            Text("Update Profile Image URL", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = photoUrl,
                onValueChange = { photoUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Profile Image URL", color = Color.White) },
                placeholder = { Text("https://example.com/image.jpg", color = Color.LightGray) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Saffron
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    isLoading = true
                    val profileUpdates = userProfileChangeRequest {
                        this.photoUri = if (photoUrl.isNotBlank()) Uri.parse(photoUrl) else null
                    }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Profile image updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                enabled = !isLoading
            ) {
                Text("Save Profile Image")
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.2f))

            // Change Email
            Text("Update Email", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email Address", color = Color.White) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Saffron
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    isLoading = true
                    user?.verifyBeforeUpdateEmail(email)?.addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Verification email sent to $email", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                enabled = !isLoading
            ) {
                Text("Verify & Update Email")
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.2f))

            // Change Password
            Text("Change Password", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current Password", color = Color.White) },
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Saffron
                ),
                trailingIcon = {
                    IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                        Icon(
                            imageVector = if (isCurrentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isCurrentPasswordVisible) "Hide password" else "Show password",
                            tint = Color.White
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New Password", color = Color.White) },
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Saffron
                ),
                trailingIcon = {
                    IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                        Icon(
                            imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isNewPasswordVisible) "Hide password" else "Show password",
                            tint = Color.White
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(context, "Fill both fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    val credential = EmailAuthProvider.getCredential(user?.email!!, currentPassword)
                    user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                isLoading = false
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                                    currentPassword = ""
                                    newPassword = ""
                                } else {
                                    Toast.makeText(context, "Error: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Re-authentication failed!", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = KarnatakaRed),
                enabled = !isLoading
            ) {
                Text("Update Password", color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Account Button
            Button(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KarnatakaRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, KarnatakaRed)
            ) {
                Text("Delete Account", color = KarnatakaRed)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text("Logout", color = KarnatakaRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", fontWeight = FontWeight.Bold, color = KarnatakaRed) },
            text = { Text("Are you sure? This action is permanent and cannot be undone. All your data will be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isLoading = true
                        user?.delete()?.addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}. You might need to re-login to perform this action.", Toast.LENGTH_LONG).show()
                            }
                        }
                        showDeleteAccountDialog = false
                    }
                ) {
                    Text("Delete Permanently", color = KarnatakaRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}
