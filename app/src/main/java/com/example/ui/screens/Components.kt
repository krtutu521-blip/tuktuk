package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// Matrix/Cyber Grid Canvas Backdrop
@Composable
fun CyberBackgroundGrid(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lineBrush = Brush.linearGradient(
                colors = listOf(NeonBlue.copy(alpha = 0.08f), NeonPurple.copy(alpha = 0.04f))
            )

            // Vertical shifting grid lines
            var xValue = 0f + gridOffset
            while (xValue < width) {
                drawLine(
                    brush = lineBrush,
                    start = Offset(xValue, 0f),
                    end = Offset(xValue, height),
                    strokeWidth = 1.5f
                )
                xValue += 60f
            }

            // Horizontal shifting grid lines
            var yValue = 0f + gridOffset
            while (yValue < height) {
                drawLine(
                    brush = lineBrush,
                    start = Offset(0f, yValue),
                    end = Offset(width, yValue),
                    strokeWidth = 1.5f
                )
                yValue += 60f
            }

            // Ambient background radial glow in bottom right
            drawCircle(
                color = NeonPurple.copy(alpha = 0.12f),
                radius = 450f,
                center = Offset(width - 50f, height - 100f)
            )

            // Ambient background radial glow in top left
            drawCircle(
                color = NeonBlue.copy(alpha = 0.15f),
                radius = 350f,
                center = Offset(150f, 150f)
            )
        }
    }
}

// Glassmorphism Premium Frame with Glowing Borders
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonBlue,
    glowWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CyberCardBg.copy(alpha = 0.85f))
            .border(
                width = glowWidth,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.8f),
                        NeonPurple.copy(alpha = 0.4f),
                        borderColor.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            content()
        }
    }
}

// Glowing Custom Interaction Button
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = NeonBlue,
    testTag: String? = null
) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    var bModifier = modifier
        .height(56.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(
            Brush.horizontalGradient(
                colors = listOf(primaryColor, NeonPurple)
            )
        )
    
    if (testTag != null) {
        bModifier = bModifier.testTag(testTag)
    }

    Box(
        modifier = bModifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Glowing text overlay
        Text(
            text = text.uppercase(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    color = primaryColor,
                    offset = Offset(0f, 0f),
                    blurRadius = 14f
                )
            ),
            letterSpacing = 2.sp
        )
    }
}

// Cyberpunk Text Field with Floating Neon States
@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    testTag: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var rawModifier = modifier.fillMaxWidth()
    
    if (testTag != null) {
        rawModifier = rawModifier.testTag(testTag)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label, 
                color = SecondaryText, 
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        singleLine = true,
        textStyle = TextStyle(color = TextWhite, fontSize = 15.sp),
        keyboardOptions = keyboardOptions,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = NeonBlue
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonBlue,
            unfocusedBorderColor = GlassBg,
            focusedLabelColor = NeonBlue,
            unfocusedLabelColor = SecondaryText,
            cursorColor = NeonBlue
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = rawModifier
    )
}

// Real-time circular radar pulse
@Composable
fun GlowPulse(
    color: Color = NeonBlue,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val sizePercent by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "size"
    )
    val opacityPercent by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "opacity"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.width / 2f) * sizePercent

            // Pulsing ring
            drawCircle(
                color = color.copy(alpha = 0.4f * opacityPercent),
                radius = maxRadius,
                center = center
            )

            // Inner core beacon
            drawCircle(
                color = color,
                radius = 12f,
                center = center
            )
        }
    }
}
