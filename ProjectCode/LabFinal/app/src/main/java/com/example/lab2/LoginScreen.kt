package com.example.lab2.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lab2.AuthViewModel
import com.example.lab2.Screen
import com.example.lab2.UserModel
import com.google.firebase.auth.FirebaseUser

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    authVm: AuthViewModel = viewModel(),
    onLoginSuccess: (FirebaseUser?) -> Unit,
    navToRegister: () -> Unit,

) {
    val state by authVm.authState.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LOGIN", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { authVm.signIn(email, password) },
            //modifier = Modifier.fillMaxWidth(),
            enabled = state !is AuthViewModel.AuthState.Loading
        ) {
            if (state is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(Modifier.size(20.dp))
            } else Text("Log In")
        }
        TextButton(onClick = {
            authVm.reset(); navToRegister()
        }) {
            Text("Don't have an account? Register")
        }
        if (state is AuthViewModel.AuthState.Error) {
            Text(
                (state as AuthViewModel.AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (state is AuthViewModel.AuthState.Success) {
            LaunchedEffect(state) {
                onLoginSuccess((state as AuthViewModel.AuthState.Success).user)
            }
        }
        //Se non si logga ma continua come guest:
        if (state is AuthViewModel.AuthState.Guest) {
            LaunchedEffect(state) {
                onLoginSuccess(null)
            }
        }
        Text("or")
        TextButton(onClick = { authVm.continueAsGuest() }) {
            Text("Continue as Guest")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authVm: AuthViewModel = viewModel(),
    onRegisterSuccess: (FirebaseUser) -> Unit,
    navBack: () -> Unit,
    isEditMode: Boolean = false
) {
    val state by authVm.authState.collectAsState()
    var first by rememberSaveable { mutableStateOf("") }
    var last  by rememberSaveable { mutableStateOf("") }
    var usern by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pass  by rememberSaveable { mutableStateOf("") }
    var conf  by rememberSaveable { mutableStateOf("") }
    var dob   by rememberSaveable { mutableStateOf("") }
    var fiscal by rememberSaveable { mutableStateOf("") }
    var phone  by rememberSaveable { mutableStateOf("") }
    var userModel by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            authVm.fetchCurrentUserData { user ->
                user?.let {
                    first = it.firstName.value
                    last  = it.lastName.value
                    usern = it.username.value
                    email = it.email.value
                    dob   = it.dateOfBirth.value
                    fiscal = it.fiscalCode.value
                    phone  = it.phone.value
                    userModel = it
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        if (isEditMode) {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Profile.base) {
                            popUpTo(Screen.Register.base) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEditMode) "EditData" else "REGISTER",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = first,
                onValueChange = { first = it },
                label = { Text("First Name") },
                enabled = !isEditMode
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = last,
                onValueChange = { last = it },
                label = { Text("Last Name") },
                enabled = !isEditMode
            )
            Spacer(Modifier.height(8.dp))

            if (!isEditMode) {
                OutlinedTextField(
                    value = usern,
                    onValueChange = { usern = it },
                    label = { Text("Username") }
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                enabled = !isEditMode
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = conf,
                onValueChange = { conf = it },
                label = { Text("Confirm") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (dd/mm/yyyy)") },
                enabled = !isEditMode
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = fiscal,
                onValueChange = { fiscal = it },
                label = { Text("Fiscal Code") }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") }
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isEditMode) {
                        // Update mode
                        if (pass.isNotBlank() || conf.isNotBlank()) {
                            if (pass == conf) {
                                authVm.updatePassword(pass) { success, errorMsg ->
                                    if (success) {
                                        // Password aggiornata, aggiorna anche altri dati
                                        authVm.updateUserData(username = usern, phone = phone)
                                        navController.navigate(Screen.Profile.base) {
                                            popUpTo(Screen.Register.base) { inclusive = true }
                                        }
                                    } else {
                                        authVm._authState.value =
                                            AuthViewModel.AuthState.Error(errorMsg ?: "Password update failed")
                                    }
                                }
                            } else {
                                authVm._authState.value =
                                    AuthViewModel.AuthState.Error("Passwords don’t match")
                            }
                        } else {
                            // Nessuna password da aggiornare, aggiorna solo altri dati
                            authVm.updateUserData(username = usern, phone = phone)
                            navController.navigate(Screen.Profile.base) {
                                popUpTo(Screen.Register.base) { inclusive = true }
                            }
                        }
                    } else {
                        // Register mode
                        if (pass != conf) {
                            authVm.reset()
                            authVm._authState.value =
                                AuthViewModel.AuthState.Error("Passwords don’t match")
                        } else {
                            authVm.signUp(first, last, usern, email, pass, dob, fiscal, phone)
                        }
                    }
                },
                enabled = state !is AuthViewModel.AuthState.Loading
            ) {
                if (state is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(Modifier.size(20.dp))
                } else Text(if (isEditMode) "Edit Data" else "Register")
            }

            if (!isEditMode) {
                TextButton(onClick = {
                    authVm.reset(); navBack()
                }) {
                    Text("Already have an account? Login")
                }
                Text("or")
                TextButton(onClick = { authVm.reset(); navBack() }) {
                    Text("Continue as Guest")
                }
            }

            if (state is AuthViewModel.AuthState.Error) {
                Text(
                    (state as AuthViewModel.AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (state is AuthViewModel.AuthState.Success) {
                LaunchedEffect(state) {
                    (state as AuthViewModel.AuthState.Success).user?.let {
                        onRegisterSuccess(it)
                    }
                }
            }
        }
    }
}

