package com.group04.materialexchange.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group04.materialexchange.R

@Composable
fun ColumnScope.AccountHeader(compact: Boolean) {
    Spacer(Modifier.height(if (compact) 8.dp else 20.dp))
    Image(
        painter = painterResource(R.drawable.reuse_logo),
        contentDescription = "Project Material Exchange logo",
        modifier = Modifier.size(if (compact) 112.dp else 144.dp).align(Alignment.CenterHorizontally)
    )
    if (!compact) {
        Text(
            "Project Material Exchange",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
    Text(
        "Reuse materials. Save money. Reduce waste.",
        fontSize = 14.sp,
        color = Color.DarkGray,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
    Spacer(Modifier.height(if (compact) 8.dp else 24.dp))
}
