package com.group04.materialexchange.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.group04.materialexchange.domain.ExchangeRules
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.RejectRed
import com.group04.materialexchange.viewmodel.ExchangeViewModel

@Composable
fun CreateAccountScreen(
    busy: Boolean,
    vm: ExchangeViewModel,
    onSignIn: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler { onSignIn() }
    PageColumn(Modifier.imePadding()) {
        AccountHeader(compact = true)
        ExchangeField(name, { name = it; localError = null }, "Full Name", icon = "user")
        ExchangeField(email, { email = it; localError = null }, "University Email", icon = "mail", keyboard = KeyboardType.Email)
        ExchangeField(password, { password = it; localError = null }, "Password", icon = "lock", password = true)
        ExchangeField(confirm, { confirm = it; localError = null }, "Confirm Password", icon = "lock", password = true)
        localError?.let { Text(it, color = RejectRed) }
        RoseButton("Create Account", {
            localError = when {
                name.trim().length < 2 -> "Enter your full name."
                !ExchangeRules.validEmail(email) -> "Enter your IAU university email."
                password.length < 8 -> "Your password must have at least 8 characters."
                password != confirm -> "Passwords do not match."
                else -> null
            }
            if (localError == null) vm.register(name, email, password)
        }, enabled = !busy)
        Text("Already have an account?", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        TextButton(onClick = { vm.clearError(); onSignIn() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Sign In", fontWeight = FontWeight.Bold)
        }
    }
}
