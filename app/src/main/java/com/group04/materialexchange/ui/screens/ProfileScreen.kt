package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RatingBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material3.Switch
import androidx.compose.ui.text.style.TextAlign
import com.group04.materialexchange.platform.ExchangeNotifications
import com.group04.materialexchange.domain.ExchangeData
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.*
import com.group04.materialexchange.viewmodel.ExchangeViewModel

@Composable
fun ProfileScreen(data: ExchangeData, busy: Boolean, vm: ExchangeViewModel) {
    val student=data.student ?: return
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("exchange_settings", android.content.Context.MODE_PRIVATE) }
    var reminderEnabled by rememberSaveable { mutableStateOf(preferences.getBoolean("daily_reminder", false)) }
    var appRating by rememberSaveable { mutableStateOf(0f) }
    val feedbackSnackbar = remember { SnackbarHostState() }
    val uiScope = rememberCoroutineScope()
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { /* WorkManager remains scheduled; the user can grant permission later in Settings. */ }
    var signOut by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf(false) }
    var name by rememberSaveable(student.id,student.name) { mutableStateOf(student.name) }
    val completed=data.requests.count { it.status=="Completed" && (it.ownerId==student.id || it.senderId==student.id) }
    PageColumn {
        SnackbarHost(hostState = feedbackSnackbar)
        PageTitle("Profile")
        Box(Modifier.size(144.dp).background(Blush,CircleShape).align(Alignment.CenterHorizontally), contentAlignment=Alignment.Center) {
            Text(student.name.split(" ").filter{it.isNotBlank()}.take(2).map{it.first().uppercaseChar()}.joinToString(""),fontSize=28.sp,fontWeight=FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(student.name,fontSize=24.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=24.dp))
        Text(student.email,fontSize=18.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=24.dp))
        Button(onClick={name=student.name;edit=true},modifier=Modifier.align(Alignment.CenterHorizontally),shape=CardShape,
            colors=ButtonDefaults.buttonColors(containerColor=MutedRose,contentColor=Color.Black)) { Text("Edit Profile") }
        Spacer(Modifier.height(8.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Blush),shape=CardShape,modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Text("Your Reuse Impact: $completed",fontWeight=FontWeight.Bold)
                Text("Materials Successfully Reused",fontWeight=FontWeight.Bold)
                Text("My Listings: ${data.listings.count{it.ownerId==student.id}}",fontWeight=FontWeight.Bold)
                Text("Completed Exchanges: $completed",fontWeight=FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Blush), shape=CardShape, modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("Daily activity reminder", fontWeight=FontWeight.Bold)
                Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()) {
                    Text("Remind me to check requests")
                    Switch(checked=reminderEnabled, onCheckedChange={ enabled ->
                        reminderEnabled = enabled
                        preferences.edit().putBoolean("daily_reminder", enabled).apply()
                        if (enabled) {
                            ExchangeNotifications.scheduleDaily(context)
                            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else ExchangeNotifications.cancelDaily(context)
                    })
                }
                Text("The reminder is scheduled by WorkManager and may run around once a day when battery conditions allow. Android controls the exact time.", fontSize=12.sp)
                OutlinedButton(onClick={
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    ExchangeNotifications.scheduleOneTimeTest(context)
                    uiScope.launch { feedbackSnackbar.showSnackbar("Your one-time test reminder has been queued.") }
                }, modifier=Modifier.fillMaxWidth()) { Text("Schedule a test reminder") }
            }
        }
        Card(colors=CardDefaults.cardColors(containerColor=Blush), shape=CardShape, modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("Rate your app experience", fontWeight=FontWeight.Bold)
                Text("Optional prototype feedback", fontSize=12.sp)
                AndroidView(
                    factory = { context -> RatingBar(context).apply { numStars = 5; stepSize = 0.5f; rating = appRating } },
                    update = { view ->
                        view.rating = appRating
                        view.setOnRatingBarChangeListener { _, rating, fromUser -> if (fromUser) appRating = rating }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(if (appRating > 0f) "Your rating: $appRating / 5" else "Choose a rating", fontSize=12.sp, textAlign=TextAlign.Start)
            }
        }
        RoseButton("Sign Out",{signOut=true},Modifier.widthIn(max=200.dp).align(Alignment.CenterHorizontally),enabled=!busy)
    }
    if(signOut) AlertDialog(onDismissRequest={signOut=false},title={Text("Sign out?")},text={Text("Your local listings and requests will be saved.")},
        confirmButton={TextButton(onClick={signOut=false;vm.signOut()}){Text("Sign Out")}},dismissButton={TextButton(onClick={signOut=false}){Text("Cancel")}})
    if(edit) AlertDialog(onDismissRequest={edit=false},title={Text("Edit Profile")},
        text={Column { ExchangeField(name,{name=it},"Full Name"); Text("Your university email remains ${student.email}.",fontSize=12.sp) }},
        confirmButton={TextButton(onClick={vm.updateProfile(name){edit=false}},enabled=name.trim().length>=2 && !busy){Text("Save")}},
        dismissButton={TextButton(onClick={edit=false}){Text("Cancel")}})
}
