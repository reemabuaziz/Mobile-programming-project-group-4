package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.group04.materialexchange.domain.ExchangeRules
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.RejectRed
import com.group04.materialexchange.viewmodel.ExchangeViewModel

@Composable
fun SignInScreen(
    busy: Boolean,
    vm: ExchangeViewModel,
    onCreateAccount: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    var recovery by remember { mutableStateOf(false) }

    PageColumn(Modifier.imePadding()) {
        AccountHeader(compact = false)
        ExchangeField(email, { email = it; localError = null }, "University Email", icon = "mail", keyboard = KeyboardType.Email)
        ExchangeField(password, { password = it; localError = null }, "Password", icon = "lock", password = true)
        TextButton(onClick = { recovery = true }, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot password?", fontWeight = FontWeight.Bold)
        }
        localError?.let { Text(it, color = RejectRed) }
        RoseButton("Sign In", {
            localError = when {
                !ExchangeRules.validEmail(email) -> "Enter your IAU university email."
                password.length < 8 -> "Your password must have at least 8 characters."
                else -> null
            }
            if (localError == null) vm.signIn(email, password)
        }, enabled = !busy)
        Text("Don't have an account?", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        TextButton(onClick = { vm.clearError(); onCreateAccount() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Create Account", fontWeight = FontWeight.Bold)
        }
        NoticeBox(
            "Try the local demo",
            "Email: 2240002170@iau.edu.sa\nPassword: Demo1234\nUse demo details only; accounts are stored on this device.",
            "Fill demo details"
        ) {
            email = "2240002170@iau.edu.sa"
            password = "Demo1234"
        }
    }

    if (recovery) {
        AlertDialog(
            onDismissRequest = { recovery = false },
            title = { Text("Password help") },
            text = { Text("This alpha uses local demo accounts. The demo password is Demo1234. Email password reset will require Firebase Authentication in the connected version; no email is sent by this build.") },
            confirmButton = { TextButton(onClick = { recovery = false }) { Text("Got it") } }
        )
    }
}
