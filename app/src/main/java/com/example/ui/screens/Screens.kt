package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.MainActivity
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import com.google.android.gms.maps.CameraUpdateFactory
import com.example.data.model.ActivityLogEntity
import com.example.data.model.FamilyLinkEntity
import com.example.data.model.LocationEntity
import com.example.data.model.UserEntity
import com.example.ui.AuthUiState
import com.example.ui.LinkUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var progress by remember { mutableStateOf(0.0f) }
    
    LaunchedEffect(Unit) {
        // Animate fake download loader to complete simulation
        val steps = 20
        for (i in 1..steps) {
            delay(100)
            progress = i.toFloat() / steps
        }
        delay(300)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CyberBackgroundGrid()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Pulsing Hologram Shield Image / Icon representation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(GlassBg)
                    .border(2.dp, NeonBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GlowPulse(color = NeonBlue, modifier = Modifier.size(90.dp))
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Cyber Shield Logo",
                    tint = NeonBlue,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cyber Styled App Name
            Text(
                text = "PARENT CONTROL",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                letterSpacing = 4.sp,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = NeonBlue,
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cyber Subtitle
            Text(
                text = "Family Safety & Location Sharing".uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonPink,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Shifting Loading Progress
            LinearProgressIndicator(
                progress = { progress },
                trackColor = CyberCardBg,
                color = NeonBlue,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .border(0.5.dp, GlassBg, RoundedCornerShape(3.dp))
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SECURE PROTOCOL BOOTING... ${ (progress * 100).toInt() }%",
                fontSize = 10.sp,
                color = SecondaryText,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// ROLE SELECTION SCREEN
// ==========================================
@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CyberBackgroundGrid()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "SELECT NODE IDENTITY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = 2.sp,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = Shadow(color = NeonPurple, blurRadius = 10f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configure terminal for parent tracking or child beacon",
                    color = SecondaryText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Cards Selector Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Parent Gateway Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CyberCardBg)
                        .border(1.5.dp, NeonBlue, RoundedCornerShape(20.dp))
                        .clickable { onRoleSelected("parent") }
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(GlassBg, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Parent Icon",
                                tint = NeonBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Column {
                            Text(
                                text = "PARENT TERMINAL",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Monitor children, link tracking beacons, view real-time maps",
                                color = SecondaryText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Child Beacon Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CyberCardBg)
                        .border(1.5.dp, NeonPink, RoundedCornerShape(20.dp))
                        .clickable { onRoleSelected("child") }
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(GlassBgPurple, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PortableWifiOff, // represents radio beacon
                                contentDescription = "Child Icon",
                                tint = NeonPink,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Column {
                            Text(
                                text = "CHILD BEACON",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Share consent-based coordinates securely with linked parent",
                                color = SecondaryText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Bottom Policy Banner
            Text(
                text = "EDUCATIONAL FAMILY SAFETY DEPLOYMENT ONLY. ALL USERS RECEIVE DENSE TRANSPARENT SHIELD NOTIFICATIONS.",
                color = SecondaryText.copy(alpha = 0.6f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

// ==========================================
// AUTHENTICATION SCREEN (LOGIN / REGISTER)
// ==========================================
@Composable
fun AuthScreen(
    initRole: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    
    var isLoginMode by remember { mutableStateOf(true) }
    var role by remember { mutableStateOf(initRole) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showForgotModal by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Observe AuthState errors
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Error) {
            Toast.makeText(context, (authState as AuthUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetAuthError()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CyberBackgroundGrid()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(GlassBg, CircleShape)
                        .border(1.dp, NeonBlue.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, 
                        contentDescription = "Back icon", 
                        tint = NeonBlue
                    )
                }

                Text(
                    text = "TERMINAL SYNC: ${role.uppercase()}",
                    color = NeonBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Main Glass Panel for forms
            GlassCard(
                borderColor = if (role == "parent") NeonBlue else NeonPink,
                glowWidth = 1.5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tab Swappables
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(CyberBackground.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    // Log In Pill Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLoginMode) (if (role == "parent") NeonBlue else NeonPink) else Color.Transparent)
                            .clickable { isLoginMode = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOGIN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoginMode) CyberBackground else TextWhite
                        )
                    }

                    // Register Pill Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isLoginMode) (if (role == "parent") NeonBlue else NeonPink) else Color.Transparent)
                            .clickable { isLoginMode = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "REGISTER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isLoginMode) CyberBackground else TextWhite
                        )
                    }
                }

                if (authState is AuthUiState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = if (role == "parent") NeonBlue else NeonPink)
                    }
                } else {
                    // Registration profile names
                    if (!isLoginMode) {
                        CyberTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Full Name",
                            testTag = "reg_name_input"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    CyberTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                        testTag = "email_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CyberTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                        testTag = "password_input"
                    )

                    if (!isLoginMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CyberTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Confirm Password",
                            isPassword = true,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                            testTag = "confirm_password_input"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary helpers (Remember Me / Forgot password)
                    if (isLoginMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = if (role == "parent") NeonBlue else NeonPink,
                                        uncheckedColor = SecondaryText
                                    )
                                )
                                Text("Remember Access", color = SecondaryText, fontSize = 11.sp)
                            }

                            Text(
                                text = "Forgot Code?",
                                color = if (role == "parent") NeonBlue else NeonPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showForgotModal = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Execution
                    val buttonColor = if (role == "parent") NeonBlue else NeonPink
                    NeonButton(
                        text = if (isLoginMode) "ESTABLISH CONNECTION" else "INITIALIZE TARGET ACCOUNT",
                        primaryColor = buttonColor,
                        onClick = {
                            if (isLoginMode) {
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "All credentials cells must be filled.", Toast.LENGTH_SHORT).show()
                                    return@NeonButton
                                }
                                viewModel.signInUser(email, password)
                            } else {
                                if (name.trim().isBlank()) {
                                    Toast.makeText(context, "Name is required.", Toast.LENGTH_SHORT).show()
                                    return@NeonButton
                                }
                                if (!email.contains("@") || !email.contains(".")) {
                                    Toast.makeText(context, "A valid email address is required.", Toast.LENGTH_SHORT).show()
                                    return@NeonButton
                                }
                                if (password.length < 6) {
                                    Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                                    return@NeonButton
                                }
                                if (password != confirmPassword) {
                                    Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                    return@NeonButton
                                }
                                viewModel.signUpUser(name, email, password, role)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "submit_auth"
                    )
                }
            }
        }
    }

    // Modern Neon cyber recovery modal
    if (showForgotModal) {
        AlertDialog(
            onDismissRequest = { showForgotModal = false },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, NeonBlue, RoundedCornerShape(20.dp)),
            title = {
                Text("SECURE CREDENTIAL RECOVERY", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Input your node email to trigger a secure reset packet from Firestore authentication systems.",
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CyberTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = "Email Address",
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotEmail.isBlank()) {
                            Toast.makeText(context, "Please type email.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.handleForgotPassword(forgotEmail) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            showForgotModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                ) {
                    Text("TRANSMIT RESET", color = CyberBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotModal = false }) {
                    Text("CANCEL", color = SecondaryText)
                }
            }
        )
    }
}

// ==========================================
// PARENT TERMINAL VIEW / CONTROLLER
// ==========================================
@Composable
fun ParentDashboardScreen(
    viewModel: MainViewModel,
    onOpenMap: () -> Unit
) {
    val context = LocalContext.current

    val parentLoc by viewModel.parentLocation.collectAsState()
    val distanceStr by viewModel.distanceFromParent.collectAsState()
    val travelTimeStr by viewModel.estimatedTravelTime.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineOk = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseOk = results[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineOk || coarseOk) {
            viewModel.startParentLocationUpdates()
        } else {
            Toast.makeText(context, "Location permission is required to track your distance relative to the child.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        val finePerm = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (finePerm == PackageManager.PERMISSION_GRANTED || coarsePerm == PackageManager.PERMISSION_GRANTED) {
            viewModel.startParentLocationUpdates()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopParentLocationUpdates()
        }
    }

    val user by viewModel.currentUser.collectAsState()
    val links by viewModel.parentLinks.collectAsState()
    val selectedKid by viewModel.selectedChild.collectAsState()
    val location by viewModel.selectedChildLocation.collectAsState()
    val status by viewModel.selectedChildStatus.collectAsState()
    val logs by viewModel.selectedChildLogs.collectAsState()
    val linkState by viewModel.linkState.collectAsState()
    val accessDenied by viewModel.accessDenied.collectAsState()

    var childEmailInput by remember { mutableStateOf("") }
    var showLinkDialog by remember { mutableStateOf(false) }

    LaunchedEffect(linkState) {
        if (linkState is LinkUiState.Success) {
            Toast.makeText(context, (linkState as LinkUiState.Success).message, Toast.LENGTH_LONG).show()
            showLinkDialog = false
            childEmailInput = ""
            viewModel.resetLinkState()
        } else if (linkState is LinkUiState.Error) {
            Toast.makeText(context, (linkState as LinkUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetLinkState()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CyberBackgroundGrid()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Upper Header Actions (Styled matching Sleek Interface design HTML)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PARENT CONTROL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonBlue,
                        letterSpacing = 2.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = NeonBlue.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 6f
                            )
                        )
                    )
                    Text(
                        text = "Security Hub",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Key Link adder floating trigger
                    IconButton(
                        onClick = { showLinkDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(GlassBg, CircleShape)
                            .border(1.dp, NeonBlue.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.AddLink, contentDescription = "Link Child", tint = NeonBlue, modifier = Modifier.size(18.dp))
                    }

                    // Bell Notifications static action from mockup
                    IconButton(
                        onClick = { 
                            Toast.makeText(context, "All shield nodes synchronized and functioning within normal parameters.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(CyberCardBg, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = SecondaryText, modifier = Modifier.size(18.dp))
                    }

                    // User avatar initials with linear gradient border matching mockup
                    val initials = remember(user?.name) {
                        val name = user?.name ?: ""
                        if (name.isBlank()) "OP"
                        else {
                            val parts = name.trim().split("\\s+".toRegex())
                            if (parts.size >= 2) {
                                "${parts[0].firstOrNull() ?: 'O'}${parts[1].firstOrNull() ?: 'P'}".uppercase()
                            } else {
                                "${parts[0].take(2)}".uppercase()
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonBlue, NeonPurple)
                                )
                            )
                            .padding(1.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CyberBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Logout Action button
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(GlassBgPurple, CircleShape)
                            .border(1.dp, NeonPink.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Disconnect Terminal", tint = NeonPink, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Quick Selective Kids Row
            if (links.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    links.forEach { link ->
                        val isSelected = selectedKid?.childUid == link.childUid
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonBlue.copy(alpha = 0.15f) else CyberCardBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NeonBlue else GlassBg,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectChildForTracking(link) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (isSelected) NeonBlue else SecondaryText, 
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = link.childName,
                                    color = if (isSelected) NeonBlue else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedKid == null) {
                // Empty State Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(borderColor = NeonBlue, modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty, 
                                contentDescription = "Empty Beacons", 
                                tint = NeonBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "NO LINKED TRAFFIC BEACONS",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Link a child profile using their email cell above to start fetching locations securely and in compliance.",
                                color = SecondaryText,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            NeonButton(
                                text = "Secure Connect Link",
                                onClick = { showLinkDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                val activeKid = selectedKid!!
                
                // Active Child Info Grid
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Glass Core telemetry HUD card (Polished to Sleek Interface specs)
                        GlassCard(borderColor = NeonBlue) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Premium glass-morphism Child Avatar container
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(NeonBlue.copy(alpha = 0.15f), NeonPurple.copy(alpha = 0.15f))
                                                )
                                            )
                                            .border(1.dp, NeonBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👦", fontSize = 22.sp)
                                    }

                                    Column {
                                        Text(
                                            text = activeKid.childName,
                                            fontSize = 16.sp,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.3).sp
                                        )
                                        val subText = remember(location) {
                                            if (location != null) {
                                                val formats = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                                "Sensing via satellite at ${formats.format(Date(location!!.timestamp))}"
                                            } else {
                                                "Establishing orbital synchronization..."
                                            }
                                        }
                                        Text(
                                            text = subText,
                                            fontSize = 11.sp,
                                            color = SecondaryText
                                        )
                                    }
                                }

                                // Interactive live beacon status badge matching HTML mockup
                                val isKidOnline = status?.online ?: false
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (isKidOnline) SuccessGreen.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))
                                        .border(
                                            1.dp, 
                                            if (isKidOnline) SuccessGreen.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f), 
                                            RoundedCornerShape(100.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isKidOnline) {
                                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                            val pulseAlpha by infiniteTransition.animateFloat(
                                                initialValue = 0.4f,
                                                targetValue = 1.0f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(1000, easing = EaseInOutSine),
                                                    repeatMode = RepeatMode.Reverse
                                                ),
                                                label = "g_pulse"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(SuccessGreen.copy(alpha = pulseAlpha), CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color.Red, CircleShape)
                                            )
                                        }
                                        Text(
                                            text = if (isKidOnline) "LIVE" else "OFFLINE",
                                            fontSize = 9.sp,
                                            color = if (isKidOnline) SuccessGreen else Color.Red,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }

                            Divider(color = GlassBg, modifier = Modifier.padding(vertical = 12.dp))

                            // Device metrics blocks (3-column premium cards layout from mockup)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Column 1: Battery Cells
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("BATTERY CELLS", fontSize = 8.sp, color = SecondaryText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.BatteryChargingFull, 
                                                contentDescription = "Battery info", 
                                                tint = if ((location?.battery ?: 0) > 20) SuccessGreen else WarningOrange,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("${location?.battery ?: "--"}%", fontSize = 13.sp, color = if ((location?.battery ?: 0) > 20) SuccessGreen else WarningOrange, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Column 2: GPS Resolution Accuracy
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("GPS CAPTURE", fontSize = 8.sp, color = SecondaryText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("±${location?.accuracy?.toInt() ?: "--"}M", fontSize = 13.sp, color = NeonBlue, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Column 3: Telemetry Speed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SPEED VECTOR", fontSize = 8.sp, color = SecondaryText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${location?.speed?.toInt() ?: 0} KM/H", fontSize = 13.sp, color = NeonPink, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Divider(color = GlassBg, modifier = Modifier.padding(vertical = 12.dp))

                            // Street Physical address report header and representation
                            Text("RESOLVED COORDINATE PHYSICAL PATH", fontSize = 9.sp, color = SecondaryText, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PinDrop, contentDescription = "Pin Drop", tint = NeonBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = location?.address ?: "Waiting for GPS signal...",
                                    fontSize = 12.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (parentLoc != null && location != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("ORBITAL ROUTING & PROXIMITY", fontSize = 9.sp, color = NeonBlue, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Distance Card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                            .border(1.dp, NeonBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("SATELLITE DISTANCE", fontSize = 8.sp, color = SecondaryText, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(distanceStr ?: "---", fontSize = 14.sp, color = NeonBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Estimated Travel Time Card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                            .border(1.dp, NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("ESTIMATED TRAVEL", fontSize = 8.sp, color = SecondaryText, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(travelTimeStr ?: "---", fontSize = 14.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Upgraded Tactical Cyber Map Gate matching HTML design
                        val currentStreet = location?.address ?: "Resolving satellite..."
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(CyberCardBg)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.verifyAccessAndOpenMap {
                                        onOpenMap()
                                    }
                                }
                        ) {
                            // Map Simulation Background
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Subtle coordinate grids/dots matching HTML mockup
                                val gap = 15f
                                val dotRadius = 1f
                                var x = 0f
                                while (x < size.width) {
                                    var y = 0f
                                    while (y < size.height) {
                                        drawCircle(
                                            color = NeonBlue.copy(alpha = 0.06f),
                                            radius = dotRadius,
                                            center = Offset(x, y)
                                        )
                                        y += gap
                                    }
                                    x += gap
                                }
                            }

                            // Center pulsing marker simulation
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowPulse(color = NeonBlue, modifier = Modifier.size(50.dp))
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(TextWhite, CircleShape)
                                        .border(2.dp, NeonBlue, CircleShape)
                                        .shadow(elevation = 6.dp, shape = CircleShape)
                                )
                            }

                            // Address Overlay capsule at bottom
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .background(CyberBackground.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                    .border(1.dp, NeonBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PinDrop, contentDescription = "Active street", tint = NeonBlue, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentStreet,
                                        color = NeonBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        modifier = Modifier.widthIn(max = 240.dp)
                                    )
                                }
                            }

                            // Map load trigger floating hover on top right of image
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .size(34.dp)
                                    .background(NeonBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Map, contentDescription = "Open live map", tint = CyberBackground, modifier = Modifier.size(16.dp))
                            }
                            
                            // Visual overlay indicator on top left
                            Text(
                                text = "GPS RADAR MAP SIGNAL",
                                color = TextWhite.copy(alpha = 0.4f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            )
                        }
                    }

                    item {
                        if (location != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(NeonPink, NeonBlue)
                                        )
                                    )
                                    .clickable {
                                        viewModel.openGoogleMapsNavigation(context, location!!.latitude, location!!.longitude)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Navigate to Child",
                                        tint = TextWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "GO TO GOOGLE MAP",
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    // Quick Commands area from the Sleek Interface
                    item {
                        GlassCard(borderColor = NeonPurple) {
                            Text(
                                text = "TACTICAL SHIELD COMMANDS",
                                fontSize = 9.sp,
                                color = NeonPurple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Ping Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(NeonPurple, NeonPink)
                                            )
                                        )
                                        .clickable {
                                            viewModel.addFamilyLog(activeKid.childEmail, "Parent triggered instant active beacon ping.")
                                            Toast.makeText(context, "Transmitting orbital telemetry ping to child beacon transceiver.", Toast.LENGTH_LONG).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CellTower, contentDescription = "Ping", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("PING BEACON", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    }
                                }

                                // Run Diagnostics Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyberBackground)
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.addFamilyLog(activeKid.childEmail, "Parent executed remote secure system diagnostics.")
                                            Toast.makeText(context, "Orbital database links verified. High fidelity location stream active.", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = "Diagnostics", tint = NeonBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("DIAGNOSTICS", color = NeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Activity logs head and lists
                    item {
                        Text(
                            text = "SECURE ACTIVITY LOGS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    if (logs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No historic status updates verified on blockchain.", color = SecondaryText, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(logs) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberCardBg.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .border(0.5.dp, GlassBg, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(NeonPink, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = log.action,
                                        color = TextWhite,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                Text(
                                    text = format.format(Date(log.timestamp)),
                                    color = SecondaryText,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }
                    }

                    // Security link revocation panel
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.removeFamilyLink(activeKid.childEmail)
                                    Toast
                                        .makeText(context, "Secure link revoked.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("REVOKE SECURITY LINK", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }

    // Connect link secure dialog
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { 
                showLinkDialog = false
                viewModel.resetLinkState()
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, NeonBlue, RoundedCornerShape(20.dp)),
            title = {
                Text("SECURE BEACON LINKING", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Input the secure beacon profile email of your child below. The beacon device must be register-configured in Firestore databases to authorize connection sync.",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (linkState is LinkUiState.Loading) {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NeonBlue)
                        }
                    } else {
                        CyberTextField(
                            value = childEmailInput,
                            onValueChange = { childEmailInput = it },
                            label = "Child Email Code cell"
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addFamilyLink(childEmailInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                ) {
                    Text("SYNC LINK", color = CyberBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showLinkDialog = false
                    viewModel.resetLinkState()
                }) {
                    Text("CANCEL", color = SecondaryText)
                }
            }
        )
    }

    if (accessDenied) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.dismissAccessDenied()
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(2.dp, Color.Red, RoundedCornerShape(20.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PortableWifiOff,
                        contentDescription = "Access Error Alert",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACCESS DENIED", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Security validation failed. You must establish an active secure connection link with this beacon device in your family links ledger first before attempting to synchronize live coordinate streams.",
                    color = TextWhite,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissAccessDenied()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("DISMISS", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// TACTICAL RADAR MAP SCREEN
// ==========================================
@Composable
fun LiveMapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val selectedKid by viewModel.selectedChild.collectAsState()
    val location by viewModel.selectedChildLocation.collectAsState()
    val status by viewModel.selectedChildStatus.collectAsState()
    val isRadarStyle by viewModel.isRadarStyleMap.collectAsState()

    val parentLoc by viewModel.parentLocation.collectAsState()
    val distanceStr by viewModel.distanceFromParent.collectAsState()
    val travelTimeStr by viewModel.estimatedTravelTime.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startParentLocationUpdates()
        onDispose {
            viewModel.stopParentLocationUpdates()
        }
    }

    var radarSweepDegrees by remember { mutableFloatStateOf(0f) }

    // Run custom Sweep scan rotation in radar layout
    LaunchedEffect(isRadarStyle) {
        if (isRadarStyle) {
            while (true) {
                radarSweepDegrees = (radarSweepDegrees + 3.5f) % 360f
                delay(16)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isRadarStyle) {
            // HIGH FIDELITY NEON CYBER RADAR BOARD
            CyberBackgroundGrid()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Main Core Sweep Radar
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1f)
                            .border(1.5.dp, NeonBlue, CircleShape)
                            .background(CyberCardBg.copy(alpha = 0.5f), CircleShape)
                    ) {
                        // Sweep Overlay scan Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width / 2f

                            // Draw Radar cross lines
                            drawLine(
                                color = NeonBlue.copy(alpha = 0.25f),
                                start = Offset(0f, center.y),
                                end = Offset(size.width, center.y),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = NeonBlue.copy(alpha = 0.25f),
                                start = Offset(center.x, 0f),
                                end = Offset(center.x, size.height),
                                strokeWidth = 1f
                            )

                            // Inner concentric radar scope circles
                            drawCircle(color = NeonBlue.copy(alpha = 0.15f), radius = radius * 0.7f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                            drawCircle(color = NeonBlue.copy(alpha = 0.10f), radius = radius * 0.4f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1f))

                            // Scan sweep sweep projection
                            val sweepAngleRad = Math.toRadians(radarSweepDegrees.toDouble())
                            val endX = center.x + radius * Math.cos(sweepAngleRad).toFloat()
                            val endY = center.y + radius * Math.sin(sweepAngleRad).toFloat()

                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(NeonBlue, Color.Transparent),
                                    start = center,
                                    end = Offset(endX, endY)
                                ),
                                start = center,
                                end = Offset(endX, endY),
                                strokeWidth = 5f
                            )
                        }

                        // Coordinates plotted markers inside radar
                        if (location != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    // Simulated pixel offset around center scope based on coordinates
                                    .offset(x = 10.dp, y = (-25).dp)
                                    .size(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowPulse(color = NeonBlue, modifier = Modifier.size(50.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.size(10.dp).background(NeonPink, CircleShape))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = selectedKid?.childName ?: "Target",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = TextStyle(shadow = Shadow(color = NeonPink, blurRadius = 4f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // REAL INTEGRATED GOOGLE MAP CONTAINER
            val kidCoord = if (location != null) LatLng(location!!.latitude, location!!.longitude) else LatLng(37.7749, -122.4194)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(kidCoord, 15f)
            }

            // Lock camera to child location changes
            LaunchedEffect(location) {
                if (location != null) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location!!.latitude, location!!.longitude),
                            15f
                        )
                    )
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = true),
                properties = MapProperties(
                    mapType = com.google.maps.android.compose.MapType.SATELLITE
                )
            ) {
                if (location != null) {
                    Marker(
                        state = MarkerState(position = LatLng(location!!.latitude, location!!.longitude)),
                        title = selectedKid?.childName ?: "Safety Beacon",
                        snippet = "Battery: ${location?.battery}% | Speed: ${location?.speed?.toInt() ?: 0} km/h"
                    )
                }
                if (parentLoc != null) {
                    Marker(
                        state = MarkerState(position = LatLng(parentLoc!!.latitude, parentLoc!!.longitude)),
                        title = "Parent (Your Device)",
                        snippet = "Proximity: ${distanceStr ?: "Calculating..."}",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }
        }

        // FLOATING OVERLAY COMMAND INTERFACES (Consolidated UI controller overlay)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Floating maps header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(CyberCardBg, CircleShape)
                        .border(1.dp, NeonBlue.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardBg)
                        .border(0.5.dp, GlassBg, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    // Tactial radar view selection
                    IconButton(
                        onClick = { viewModel.setRadarStyleMap(true) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRadarStyle) NeonBlue else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio, 
                            contentDescription = "Radar style", 
                            tint = if (isRadarStyle) CyberBackground else NeonBlue
                        )
                    }

                    // Google Map visual standard view selection
                    IconButton(
                        onClick = { viewModel.setRadarStyleMap(false) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isRadarStyle) NeonBlue else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public, 
                            contentDescription = "Public standard map", 
                            tint = if (!isRadarStyle) CyberBackground else NeonBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Lower HUD dashboard analytics Panel
            GlassCard(borderColor = NeonPink) {
                Text(
                    text = "TARGET BEACON METRIC",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPink,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedKid?.childName ?: "Inactive",
                            fontSize = 18.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        val formats = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                        Text(
                            text = if (location != null) "Last Telemetry: ${formats.format(Date(location!!.timestamp))}" else "No link found",
                            fontSize = 11.sp,
                            color = SecondaryText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (status?.online == true) SuccessGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (status?.online == true) "🟢 ONLINE" else "🔴 OFFLINE",
                            fontSize = 10.sp,
                            color = if (status?.online == true) SuccessGreen else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(color = GlassBg, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("LATITUDE", fontSize = 10.sp, color = SecondaryText)
                        Text(if (location != null) String.format("%.6f", location!!.latitude) else "---", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Column {
                        Text("LONGITUDE", fontSize = 10.sp, color = SecondaryText)
                        Text(if (location != null) String.format("%.6f", location!!.longitude) else "---", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("BATTERY", fontSize = 10.sp, color = SecondaryText)
                        Text("${location?.battery ?: "--"}%", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (distanceStr != null && travelTimeStr != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                            .border(0.5.dp, NeonBlue.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PROXIMITY RANGE", fontSize = 8.sp, color = SecondaryText, fontWeight = FontWeight.Bold)
                            Text(distanceStr!!, color = NeonBlue, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EST. TRAVEL TIME", fontSize = 8.sp, color = SecondaryText, fontWeight = FontWeight.Bold)
                            Text(travelTimeStr!!, color = NeonPurple, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Physical Resolved location string
                Text("CURRENT PHYSICAL BASE ADDRESS", fontSize = 9.sp, color = SecondaryText, fontWeight = FontWeight.Bold)
                Text(
                    text = location?.address ?: "Waiting for GPS signal...",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ==========================================
// CHILD BEACON VIEW / CONTROLLER
// ==========================================
@Composable
fun ChildDashboardScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val isSharing by viewModel.shareLocationEnabled.collectAsState()
    val context = LocalContext.current

    // Request coarse/fine background location permissions at launch!
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineOk = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseOk = results[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (!fineOk && !coarseOk) {
            Toast.makeText(context, "Location permissions mandatory to share satellite telemetry securely with parent.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        
        val list = mutableListOf<String>()
        if (finePerm != PackageManager.PERMISSION_GRANTED) list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (coarsePerm != PackageManager.PERMISSION_GRANTED) list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        
        // Background coordinates permission (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bgPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            if (bgPerm != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifyPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (notifyPerm != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (list.isNotEmpty()) {
            permissionLauncher.launch(list.toTypedArray())
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CyberBackgroundGrid()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE TRACKING BEACON", fontSize = 11.sp, color = NeonPink, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text(user?.name ?: "Target Child node", fontSize = 18.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .background(GlassBgPurple, CircleShape)
                        .border(1.dp, NeonPink.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Disconnect Device", tint = NeonPink)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Status display
            GlassCard(borderColor = if (isSharing) NeonPink else NeonPurple) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(GlassBgPurple)
                            .border(2.dp, if (isSharing) NeonPink else NeonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSharing) {
                            GlowPulse(color = NeonPink, modifier = Modifier.size(90.dp))
                        }
                        Icon(
                            imageVector = if (isSharing) Icons.Default.CellTower else Icons.Default.PortableWifiOff,
                            contentDescription = "Radar Active",
                            tint = if (isSharing) NeonPink else NeonPurple,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (isSharing) "BEACON SHIELD ACTIVE" else "TRANSCEIVER INACTIVE",
                        fontSize = 18.sp,
                        color = if (isSharing) NeonPink else SecondaryText,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSharing) "Transmitting coordinates securely to linked parent node" else "Sharing deactivated. Local privacy sandbox active.",
                        fontSize = 12.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Status details blocks
            GlassCard(borderColor = NeonBlue) {
                Text("DIAGNOSTIC SYSTEM SENSORS", fontSize = 11.sp, color = NeonBlue, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(14.dp))
                
                // Sensor lines
                StatusSensorRow(label = "Local GPS Status", value = "INTEGRATED: HIGH ACCURACY", icon = Icons.Default.GpsFixed, iconColor = SuccessGreen)
                Divider(color = GlassBg, modifier = Modifier.padding(vertical = 10.dp))
                StatusSensorRow(label = "Sync Transmission Grid", value = "SECURE CLOUD STORAGE", icon = Icons.Default.Backup, iconColor = SuccessGreen)
                Divider(color = GlassBg, modifier = Modifier.padding(vertical = 10.dp))
                StatusSensorRow(label = "Transparency Guard Notification", value = "ALWAYS VERIFIED ACTIVE", icon = Icons.Default.Info, iconColor = NeonBlue)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Consent explicit sharing switch loader card
            GlassCard(borderColor = NeonPink) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CONSENT LOCATION SHARING", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Deactivating shuts down the location collection foreground service instantly.",
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isSharing,
                        onCheckedChange = { viewModel.setShareLocation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonPink,
                            checkedTrackColor = GlassBgPurple,
                            uncheckedThumbColor = SecondaryText,
                            uncheckedTrackColor = CyberBackground
                        ),
                        modifier = Modifier.testTag("location_sharing_switch")
                    )
                }
            }
        }
    }
}

@Composable
fun StatusSensorRow(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = "sensor", tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Text(value, color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
