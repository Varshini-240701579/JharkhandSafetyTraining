package com.example.jharkhandsafetytraining.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF121418)
private val CardSurface = Color(0xFF1E222B)
private val CardBorder = Color(0xFF374151)
private val InputContainer = Color(0xFF161920)
private val SafetyAmber = Color(0xFFFFB300)
private val ActionAmber = Color(0xFFD97706)
private val MutedLabel = Color(0xFF9CA3AF)
private val UnfocusedBorder = Color(0xFF4B5563)
private val PlaceholderGray = Color(0xFF6B7280)
private val ErrorRed = Color(0xFFF87171)

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit = {},
    onGoToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = onLoggedIn,
    onNavigateToRegister: () -> Unit = onGoToRegister
) {
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White.copy(alpha = 0.6f),
        focusedContainerColor = InputContainer,
        unfocusedContainerColor = InputContainer,
        focusedLabelColor = SafetyAmber,
        unfocusedLabelColor = MutedLabel,
        focusedBorderColor = SafetyAmber,
        unfocusedBorderColor = UnfocusedBorder,
        cursorColor = SafetyAmber,
        focusedPlaceholderColor = PlaceholderGray,
        unfocusedPlaceholderColor = PlaceholderGray
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Industrial Safety Emblem Header
            Surface(
                color = SafetyAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "⚠ INDUSTRIAL & MINE SAFETY PORTAL",
                    color = SafetyAmber,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "झारखंड खान सुरक्षा",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "JHARKHAND MINE SAFETY",
                color = SafetyAmber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Vocational Safety Training & Certification Hub",
                color = MutedLabel,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hazard Amber Accent Line
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(4.dp)
                    .background(SafetyAmber, shape = RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // High-Contrast Dark Industrial Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Worker Sign In / श्रमिक लॉग इन",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Enter your registered 10-digit mobile number and 4-digit security PIN.",
                        color = MutedLabel,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mobile Number Input
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { input ->
                            val digitsOnly = input.filter(Char::isDigit)
                            if (digitsOnly.length <= 10) {
                                phone = digitsOnly
                            }
                        },
                        label = { Text("Mobile Number (मोबाइल नंबर)") },
                        placeholder = { Text("10-digit mobile number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Digit PIN Input with Show/Hide Toggle
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { input ->
                            val digitsOnly = input.filter(Char::isDigit)
                            if (digitsOnly.length <= 4) {
                                pin = digitsOnly
                            }
                        },
                        label = { Text("4-Digit PIN (4-अंकीय पिन)") },
                        placeholder = { Text("••••") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (!viewModel.isLoading) {
                                    viewModel.login(phone, pin, onLoginSuccess)
                                }
                            }
                        ),
                        visualTransformation = if (showPin) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = { showPin = !showPin },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (showPin) "HIDE" else "SHOW",
                                    color = SafetyAmber,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error Banner
                    viewModel.errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = ErrorRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠ $msg",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(phone, pin, onLoginSuccess)
                        },
                        enabled = !viewModel.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionAmber,
                            contentColor = Color.White,
                            disabledContainerColor = ActionAmber.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Verifying...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = "लॉग इन / Login",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // High-Contrast Screen Switcher
                    TextButton(
                        onClick = {
                            viewModel.clearError()
                            onNavigateToRegister()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "New worker? Register here",
                            color = SafetyAmber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}