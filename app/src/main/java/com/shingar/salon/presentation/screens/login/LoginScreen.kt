package com.shingar.salon.presentation.screens.login

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.navigation.NavRoutes
import com.shingar.salon.ui.theme.*

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueMedium, PinkPrimary)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shingar Sallon",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "Manager",
                    fontSize = 16.sp,
                    color = White.copy(alpha = 0.8f),
                    letterSpacing = 3.sp
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 240.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Sign in to manage your salon",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        focusedLabelColor = PinkPrimary
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        focusedLabelColor = PinkPrimary
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Demo Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        navController.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null,
                            modifier = Modifier.size(16.dp), tint = SoftPurple)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Owner Login", color = SoftPurple, fontSize = 13.sp)
                    }
                    TextButton(onClick = {
                        navController.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Person, contentDescription = null,
                            modifier = Modifier.size(16.dp), tint = DarkBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Staff Login", color = DarkBlue, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
