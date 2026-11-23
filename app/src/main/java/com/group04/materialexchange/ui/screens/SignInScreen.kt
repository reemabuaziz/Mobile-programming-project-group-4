package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.group04.materialexchange.R
import com.group04.materialexchange.domain.ExchangeRules
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.RejectRed
import com.group04.materialexchange.viewmodel.ExchangeViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    busy: Boolean,
    vm: ExchangeViewModel,
    onCreateAccount: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var recovery by remember { mutableStateOf(false) }
    var resetSent by remember { mutableStateOf(false) }
    var googleBusy by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) {
        CredentialManager.create(context)
    }

    val working = busy || googleBusy

    PageColumn(Modifier.imePadding()) {
        AccountHeader(compact = false)

        ExchangeField(
            email,
            {
                email = it
                localError = null
            },
            "University Email",
            icon = "mail",
            keyboard = KeyboardType.Email
        )

        ExchangeField(
            password,
            {
                password = it
                localError = null
            },
            "Password",
            icon = "lock",
            password = true
        )

        TextButton(
            onClick = {
                localError = null
                recovery = true
            },
            enabled = !working,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                "Forgot password?",
                fontWeight = FontWeight.Bold
            )
        }

        localError?.let {
            Text(it, color = RejectRed)
        }

        RoseButton(
            "Sign In",
            {
                vm.clearError()

                localError = when {
                    !ExchangeRules.validEmail(email) ->
                        "Enter your IAU university email."

                    password.length < 8 ->
                        "Your password must have at least 8 characters."

                    else -> null
                }

                if (localError == null) {
                    vm.signIn(email.trim(), password)
                }
            },
            enabled = !working
        )

        Text(
            "or",
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        OutlinedButton(
            onClick = {
                localError = null
                vm.clearError()
                googleBusy = true

                scope.launch {
                    try {
                        val googleOption =
                            GetSignInWithGoogleOption.Builder(
                                context.getString(
                                    R.string.default_web_client_id
                                )
                            ).build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleOption)
                            .build()

                        val result = credentialManager.getCredential(
                            context = context,
                            request = request
                        )

                        val credential = result.credential

                        require(
                            credential is CustomCredential &&
                                    credential.type ==
                                    GoogleIdTokenCredential
                                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            "Google returned an unsupported credential."
                        }

                        val googleCredential =
                            GoogleIdTokenCredential.createFrom(
                                credential.data
                            )

                        vm.signInWithGoogle(
                            googleCredential.idToken
                        )
                    } catch (
                        e: GetCredentialCancellationException
                    ) {
                        // Closing the account chooser is not an error.
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        localError = e.localizedMessage
                            ?: "Google sign-in failed. Please try again."
                    } finally {
                        googleBusy = false
                    }
                }
            },
            enabled = !working,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                if (googleBusy) {
                    "Opening Google..."
                } else {
                    "Sign in with Google"
                },
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "Don't have an account?",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        TextButton(
            onClick = {
                vm.clearError()
                onCreateAccount()
            },
            enabled = !working,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        ) {
            Text(
                "Create Account",
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (recovery) {
        AlertDialog(
            onDismissRequest = {
                if (!busy) recovery = false
            },
            title = {
                Text("Reset password")
            },
            text = {
                Column {
                    Text(
                        "Enter the university email used for your account."
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                        },
                        label = { Text("University Email") },
                        singleLine = true,
                        enabled = !busy
                    )

                    localError?.let {
                        Text(it, color = RejectRed)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        if (!ExchangeRules.validEmail(email)) {
                            localError =
                                "Enter your IAU university email."
                        } else {
                            localError = null

                            vm.resetPassword(email.trim()) {
                                recovery = false
                                resetSent = true
                            }
                        }
                    }
                ) {
                    Text("Send reset email")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        recovery = false
                        localError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (resetSent) {
        AlertDialog(
            onDismissRequest = { resetSent = false },
            title = { Text("Check your email") },
            text = {
                Text(
                    "If this email has a password account, " +
                            "you will receive a reset link. " +
                            "Check your spam folder too."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { resetSent = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
