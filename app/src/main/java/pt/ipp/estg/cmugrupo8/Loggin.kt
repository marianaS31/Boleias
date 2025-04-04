    /*
    package pt.ipp.estg.cmugrupo8

    import android.os.Bundle
    import android.util.Log
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavController
    import com.google.firebase.auth.FirebaseAuth

    class LoginActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                LoginScreen( navController = null) // Replace null with NavController if applicable
            }
        }
    }

    @Composable
    fun LoginScreen(
        navController: NavController?
    ) {
        val firebaseAuth = remember { FirebaseAuth.getInstance() }

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            navController?.navigate("home")
        }

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            firebaseAuth.app.applicationContext,
                                            "Login Successful",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController?.navigate("home")
                                    } else {
                                        Toast.makeText(
                                            firebaseAuth.app.applicationContext,
                                            "Login Failed: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("LoginActivity", "Login Failed: ${task.exception?.message}")
                                    }
                                }
                        } else {
                            Toast.makeText(
                                firebaseAuth.app.applicationContext,
                                "Please fill out all fields",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController?.navigate("register") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Don't have an account? Register")
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LoginScreenPreview() {
        // Preview with placeholder FirebaseAuth and NavController
        LoginScreen( navController = null)
    }
    */
    package pt.ipp.estg.cmugrupo8

    import android.os.Bundle
    import android.util.Log
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavController
    import com.google.firebase.auth.FirebaseAuth
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await
    import kotlinx.coroutines.withContext

    class LoginActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                LoginScreen(navController = null) // Replace null with NavController if applicable
            }
        }
    }


    @Composable
    fun LoginScreen(
        navController: NavController?
    ) {
        val firebaseAuth = remember { FirebaseAuth.getInstance() }

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            navController?.navigate("home")
        }

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                // Login Title
                Text(
                    text = stringResource(id = R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Email Input Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(id = R.string.email_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password Input Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(id = R.string.password_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                val stringSuccess = stringResource(id = R.string.login_success)
                val stringFailed = stringResource(id = R.string.login_failed)
                val stringEmptyFields = stringResource(id = R.string.login_empty_fields)
                val ctx = LocalContext.current
                // Login Button
                var authBol by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    // Use `await()` for Firebase login
                                    withContext(Dispatchers.IO) {
                                        val res = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                                    if (res!=null){
                                        authBol = true
                                    }
                                        }
                                } catch (e: Exception) {

                                        Log.e("LoginScreen", "Login Failed: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            Toast.makeText(
                                firebaseAuth.app.applicationContext,
                                stringEmptyFields,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = stringResource(id = R.string.login_button))
                    }
                }
                if (authBol) {
                    Toast.makeText(
                        ctx,
                        stringSuccess,
                        Toast.LENGTH_SHORT
                    ).show()
                    navController?.navigate("home")
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Register Prompt
                TextButton(
                    onClick = { navController?.navigate("register") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(id = R.string.register_prompt))
                }
            }
        }
    }




    @Preview(showBackground = true)
    @Composable
    fun LoginScreenPreview() {
        // Preview with placeholder FirebaseAuth and NavController
        LoginScreen(navController = null)
    }
