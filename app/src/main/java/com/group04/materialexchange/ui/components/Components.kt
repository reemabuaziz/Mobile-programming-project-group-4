package com.group04.materialexchange.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group04.materialexchange.R
import com.group04.materialexchange.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Glyph(name: String, modifier: Modifier = Modifier.size(24.dp), color: Color = Color.Black, description: String? = null) {
    Canvas(modifier.semantics { if (description != null) contentDescription = description }) {
        val k = size.minDimension / 24f
        fun p(x: Float, y: Float) = Offset(x * k, y * k)
        fun line(a: Float, b: Float, c: Float, d: Float) = drawLine(color, p(a,b), p(c,d), 1.8f*k)
        fun path(vararg points: Pair<Float, Float>) {
            val path = Path().apply { moveTo(points[0].first*k,points[0].second*k); points.drop(1).forEach { lineTo(it.first*k,it.second*k) } }
            drawPath(path, color, style = Stroke(1.8f*k))
        }
        when(name) {
            "home" -> { path(3f to 10f,12f to 3f,21f to 10f,21f to 21f,15f to 21f,15f to 14f,9f to 14f,9f to 21f,3f to 21f,3f to 10f) }
            "activity" -> { line(4f,8f,20f,8f); path(16f to 4f,20f to 8f,16f to 12f); line(20f,17f,4f,17f); path(8f to 13f,4f to 17f,8f to 21f) }
            "bell" -> { path(4f to 18f,6f to 14f,6f to 9f,8f to 5f,12f to 4f,16f to 5f,18f to 9f,18f to 14f,20f to 18f,4f to 18f); line(10f,21f,14f,21f) }
            "user" -> { drawCircle(color,4f*k,p(10f,7f),style=Stroke(1.8f*k)); path(2f to 22f,2f to 18f,5f to 14f,15f to 14f,18f to 18f,18f to 22f); line(18f,4f,24f,4f); line(19f,8f,24f,8f) }
            "mail" -> { path(2f to 5f,22f to 5f,22f to 19f,2f to 19f,2f to 5f); path(2f to 6f,12f to 13f,22f to 6f) }
            "lock" -> { drawRect(color,p(4f,10f),Size(16f*k,12f*k),style=Stroke(1.8f*k)); path(7f to 10f,7f to 5f,9f to 2f,15f to 2f,17f to 5f,17f to 10f) }
            "search" -> { drawCircle(color,7f*k,p(10f,10f),style=Stroke(2f*k)); line(15f,15f,22f,22f) }
            "plus" -> { line(12f,4f,12f,20f); line(4f,12f,20f,12f) }
            "back" -> { path(14f to 4f,6f to 12f,14f to 20f); line(6f,12f,22f,12f) }
            "filter" -> { line(3f,5f,21f,5f); line(6f,11f,18f,11f); line(10f,17f,14f,17f) }
            "photo" -> { drawRoundRect(color,p(2f,2f),Size(20f*k,20f*k),cornerRadius=androidx.compose.ui.geometry.CornerRadius(3f*k),style=Stroke(1.5f*k)); drawCircle(color,2.5f*k,p(8f,8f),style=Stroke(1.5f*k)); path(3f to 21f,13f to 11f,21f to 19f) }
            else -> { drawCircle(color,8f*k,p(12f,12f),style=Stroke(1.8f*k)); line(12f,7f,12f,13f); drawCircle(color,1f*k,p(12f,17f)) }
        }
    }
}

@Composable
fun PageColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}

@Composable
fun PageTitle(text: String, back: (() -> Unit)? = null) {
    Box(Modifier.fillMaxWidth().heightIn(min = 56.dp), contentAlignment = Alignment.Center) {
        if (back != null) IconButton(onClick=back, modifier=Modifier.align(Alignment.CenterStart)) { Glyph("back", description="Back") }
        Text(text, fontSize=24.sp, fontWeight=FontWeight.Bold, modifier=Modifier.padding(horizontal=48.dp))
    }
}

@Composable
fun RoseButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min=52.dp), enabled=enabled, shape=CardShape,
        colors=ButtonDefaults.buttonColors(containerColor=MutedRose, contentColor=Color.Black,
            disabledContainerColor=Blush, disabledContentColor=Color.DarkGray)) {
        Text(text, fontSize=16.sp, fontWeight=FontWeight.Bold)
    }
}

@Composable
fun ExchangeField(value: String, change: (String) -> Unit, label: String, modifier: Modifier = Modifier,
                  icon: String? = null, password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(value, change, modifier.fillMaxWidth(), label={ Text(label) }, singleLine=true,
        shape=CardShape, leadingIcon=if(icon==null) null else {{ Glyph(icon) }},
        trailingIcon=if(password) {{ TextButton(onClick={visible=!visible}){Text(if(visible) "Hide" else "Show")} }} else null,
        visualTransformation=if(password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions=KeyboardOptions(keyboardType=if(password) KeyboardType.Password else keyboard),
        colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=Blush, focusedContainerColor=Blush,
            unfocusedBorderColor=MutedRose, focusedBorderColor=ReuseGreen))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceField(label: String, value: String, choices: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, {expanded=!expanded}) {
        OutlinedTextField(value, {}, readOnly=true, label={Text(label)},
            trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},
            modifier=Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), shape=CardShape,
            colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=Blush, focusedContainerColor=Blush, unfocusedBorderColor=MutedRose))
        ExposedDropdownMenu(expanded, {expanded=false}) {
            choices.forEach { c -> DropdownMenuItem(text={Text(c)}, onClick={onChange(c);expanded=false}) }
        }
    }
}

@Composable
fun NoticeBox(title: String, text: String, actionLabel: String? = null, action: () -> Unit = {}) {
    Card(Modifier.fillMaxWidth(), shape=CardShape, colors=CardDefaults.cardColors(containerColor=Blush)) {
        Column(Modifier.padding(20.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(title,fontWeight=FontWeight.Bold,fontSize=18.sp)
            Text(text)
            if(actionLabel != null) TextButton(onClick=action) { Text(actionLabel) }
        }
    }
}

@Composable
fun ItemPhoto(photo: String, title: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(null,photo) {
        if(photo.startsWith("content:") || photo.startsWith("file:")) {
            value=withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(Uri.parse(photo))?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize=2 })
                } }.getOrNull()
            }
        }
    }
    when {
        photo == "arduino" -> Image(painterResource(R.drawable.arduino),title,modifier,contentScale=ContentScale.Fit)
        photo == "filament" -> Image(painterResource(R.drawable.filament),title,modifier,contentScale=ContentScale.Fit)
        bitmap != null -> Image(bitmap!!.asImageBitmap(),title,modifier.clip(CardShape),contentScale=ContentScale.Crop)
        else -> Box(modifier.background(Blush,CardShape),contentAlignment=Alignment.Center) { Glyph("photo",Modifier.size(48.dp),ReuseGreen,description=title) }
    }
}

@Composable
fun StatusLabel(status: String) {
    val color = when(status) { "Available", "Accepted", "Completed" -> ReuseGreen; "Rejected" -> RejectRed; else -> Color.DarkGray }
    Text("Status: $status", color=color, fontWeight=FontWeight.SemiBold)
}
