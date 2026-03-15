package me.edrakai.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import me.edrakai.ui.theme.EdrakColors

// ─────────────────────────────────────────────────────────────────────────────
// Login Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToVoiceSetup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome       -> onNavigateToHome()
                is AuthEffect.NavigateToVoiceSetup -> onNavigateToVoiceSetup()
                is AuthEffect.ShowError            -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = EdrakColors.Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = EdrakColors.Error,
                    contentColor = Color.White,
                )
            }
        }
    ) { padding ->
        AuthContent(
            modifier = Modifier.padding(padding),
            title = "Welcome back 👋",
            subtitle = "Sign in to continue listening",
            content = {
                EdrakTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
                    label = "Email",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    error = state.emailError,
                )
                Spacer(Modifier.height(16.dp))
                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
                    label = "Password",
                    imeAction = ImeAction.Done,
                    error = state.passwordError,
                )
                Spacer(Modifier.height(32.dp))
                EdrakPrimaryButton(
                    text = "Sign In",
                    isLoading = state.isLoading,
                    onClick = { viewModel.onEvent(AuthEvent.LoginClicked) },
                )
                Spacer(Modifier.height(24.dp))
                AuthNavLink(
                    text = "Don't have an account? ",
                    linkText = "Sign Up",
                    onClick = onNavigateToRegister,
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Register Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVoiceSetup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToVoiceSetup -> onNavigateToVoiceSetup()
                is AuthEffect.ShowError            -> snackbarHostState.showSnackbar(effect.message)
                else                               -> Unit
            }
        }
    }

    Scaffold(
        containerColor = EdrakColors.Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = EdrakColors.Error, contentColor = Color.White)
            }
        }
    ) { padding ->
        AuthContent(
            modifier = Modifier.padding(padding),
            title = "Create account 🎙️",
            subtitle = "Set up Edrak to start listening",
            content = {
                EdrakTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(AuthEvent.NameChanged(it)) },
                    label = "Full Name",
                    leadingIcon = Icons.Default.Person,
                    imeAction = ImeAction.Next,
                    error = state.nameError,
                )
                Spacer(Modifier.height(16.dp))
                EdrakTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
                    label = "Email",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    error = state.emailError,
                )
                Spacer(Modifier.height(16.dp))
                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
                    label = "Password",
                    imeAction = ImeAction.Next,
                    error = state.passwordError,
                )
                Spacer(Modifier.height(16.dp))
                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onEvent(AuthEvent.ConfirmPasswordChanged(it)) },
                    label = "Confirm Password",
                    imeAction = ImeAction.Done,
                )
                Spacer(Modifier.height(32.dp))
                EdrakPrimaryButton(
                    text = "Create Account",
                    isLoading = state.isLoading,
                    onClick = { viewModel.onEvent(AuthEvent.RegisterClicked) },
                )
                Spacer(Modifier.height(24.dp))
                AuthNavLink(
                    text = "Already have an account? ",
                    linkText = "Sign In",
                    onClick = onNavigateToLogin,
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared Composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AuthContent(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EdrakColors.Background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))

        // Logo glow
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(EdrakColors.Primary.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "E", fontSize = 36.sp, color = EdrakColors.Primary, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = EdrakColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = EdrakColors.TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(48.dp))
        content()
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun EdrakTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    error: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = EdrakColors.Primary) },
        isError = error != null,
        supportingText = error?.let { { Text(it, color = EdrakColors.Error) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EdrakColors.Primary,
            unfocusedBorderColor = EdrakColors.TextSecondary.copy(alpha = 0.4f),
            focusedLabelColor = EdrakColors.Primary,
            unfocusedLabelColor = EdrakColors.TextSecondary,
            cursorColor = EdrakColors.Primary,
            focusedTextColor = EdrakColors.TextPrimary,
            unfocusedTextColor = EdrakColors.TextPrimary,
        ),
    )
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Done,
    error: String? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EdrakColors.Primary) },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = EdrakColors.TextSecondary,
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = error?.let { { Text(it, color = EdrakColors.Error) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EdrakColors.Primary,
            unfocusedBorderColor = EdrakColors.TextSecondary.copy(alpha = 0.4f),
            focusedLabelColor = EdrakColors.Primary,
            unfocusedLabelColor = EdrakColors.TextSecondary,
            cursorColor = EdrakColors.Primary,
            focusedTextColor = EdrakColors.TextPrimary,
            unfocusedTextColor = EdrakColors.TextPrimary,
        ),
    )
}

@Composable
private fun EdrakPrimaryButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EdrakColors.Primary,
            contentColor = EdrakColors.Background,
            disabledContainerColor = EdrakColors.Primary.copy(alpha = 0.5f),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = EdrakColors.Background,
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun AuthNavLink(text: String, linkText: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = EdrakColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
            Text(linkText, color = EdrakColors.Primary, fontWeight = FontWeight.Bold)
        }
    }
}
