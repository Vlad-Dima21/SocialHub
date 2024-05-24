package com.vladima.socialhub.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.socialhub.R
import com.vladima.socialhub.models.User
import com.vladima.socialhub.ui.MainActivity
import com.vladima.socialhub.ui.auth.ui.theme.SocialHubTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        if (auth.currentUser != null) {
            startMainActivity()
        }

        val viewModel: AuthViewModel by viewModels()

        lifecycleScope.launch {
            viewModel.errorMsg.collect { msg ->
                if (msg != null) {
                    Toast.makeText(this@AuthActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.isSuccess.collect { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(this@AuthActivity, getString(R.string.user_logged_in), Toast.LENGTH_SHORT).show()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            SocialHubTheme {
                val errorMsg by viewModel.errorMsg.collectAsState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(32.dp)
                ) {
                    AnimatedVisibility(
                        visible = viewModel.authenticationMethod == 0,
                        modifier = Modifier.align(Alignment.Center),
                        enter = slideInHorizontally() + fadeIn(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            Text(
                                text = stringResource(id = R.string.app_name),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(64.dp))
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.email))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                value = viewModel.email,
                                onValueChange = { viewModel.email = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = listOf(R.string.WrongEmail, R.string.FieldsNotFilled, R.string.EmailAlreadyInUse).contains(errorMsg)
                            )
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.user_name))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                value = viewModel.userName,
                                onValueChange = { viewModel.userName = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = errorMsg == R.string.FieldsNotFilled
                            )
                            OutlinedTextField(
                                label = {
                                    Text(stringResource(id = R.string.password))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = PasswordVisualTransformation(),
                                value = viewModel.password,
                                onValueChange = { viewModel.password = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = errorMsg == R.string.FieldsNotFilled
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(onClick = viewModel::signUp, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                    Text(
                                        text = getString(R.string.sign_up),
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                                Button(onClick = ::loginGoogle, modifier = Modifier.weight(0.5f)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.google),
                                        contentDescription = null,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                            }
                            Row {
                                Text(
                                    modifier = Modifier.padding(top = ButtonDefaults.TextButtonContentPadding.calculateTopPadding() + ButtonDefaults.ContentPadding.calculateTopPadding() - 3.dp),
                                    text = getString(R.string.already_account),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                TextButton(onClick = {
                                    viewModel.apply {
                                        email = ""
                                        userName = ""
                                        password = ""
                                    }
                                    viewModel.authenticationMethod = 1
                                }) {
                                    Text(getString(R.string.login_instead))
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = viewModel.authenticationMethod == 1,
                        modifier = Modifier.align(Alignment.Center),
                        enter = slideInHorizontally() + fadeIn(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            Text(
                                text = stringResource(id = R.string.app_name),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(64.dp))
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.email))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                value = viewModel.email,
                                onValueChange = { viewModel.email = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = listOf(R.string.WrongEmail, R.string.FieldsNotFilled).contains(errorMsg)
                            )
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.password))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = PasswordVisualTransformation(),
                                value = viewModel.password,
                                onValueChange = { viewModel.password = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = errorMsg == R.string.FieldsNotFilled
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(onClick = viewModel::logIn, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                    Text(
                                        text = getString(R.string.login),
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                                Button(onClick = ::loginGoogle, modifier = Modifier.weight(0.5f)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.google),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                            }
                            Row {
                                Text(
                                    modifier = Modifier.padding(top = ButtonDefaults.TextButtonContentPadding.calculateTopPadding() + ButtonDefaults.ContentPadding.calculateTopPadding() - 3.dp),
                                    text = getString(R.string.create_account),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                TextButton(onClick = {
                                    viewModel.apply {
                                        email = ""
                                        userName = ""
                                        password = ""
                                    }
                                    viewModel.authenticationMethod = 0
                                }) {
                                    Text(getString(R.string.sign_up_instead))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun loginGoogle() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, options)
        googleSignInClient.signInIntent.also {
            googleSignInResult.launch(it)
        }
    }

    private val googleSignInResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            CoroutineScope(Dispatchers.IO).launch {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).await()
                account?.let {
                    withContext(Dispatchers.Main) {
                        googleAuthForFirebase(it)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Google authentication failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val auth = FirebaseAuth.getInstance()
        val usersCollection = Firebase.firestore.collection("users")
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()
                if (usersCollection.whereEqualTo("userUID", auth.currentUser!!.uid).get().await().documents.isEmpty()) {
                    GoogleSignIn.getLastSignedInAccount(this@AuthActivity)!!
                        .let { account ->
                            usersCollection.add(
                                User(
                                    auth.currentUser!!.uid,
                                    account.displayName ?: ""
                                )
                            ).await()
                        }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthActivity, "Google authentication succeeded", Toast.LENGTH_LONG).show()
                    startMainActivity()
                }
            } catch(e: Exception) {
                Log.e("GOOGLE_ERROR", e.stackTraceToString())
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthActivity, "Google authentication failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}