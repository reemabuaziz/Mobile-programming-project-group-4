package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group04.materialexchange.domain.ExchangeData
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.*
import com.group04.materialexchange.viewmodel.ExchangeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(data: ExchangeData, vm: ExchangeViewModel) {
    val student=data.student ?: return
    val notices=data.notices.filter{it.userId==student.id}.sortedByDescending{it.time}
    PageColumn {
        PageTitle("Notifications")
        if(notices.any{!it.read}) TextButton(onClick={vm.markRead()},modifier=Modifier.align(Alignment.End)){Text("Mark all as read")}
        if(notices.isEmpty()) NoticeBox("You're all caught up","Request updates and completed exchanges will appear here.")
        notices.forEach { notice ->
            Card(Modifier.fillMaxWidth(),shape=CardShape,colors=CardDefaults.cardColors(containerColor=Blush)) {
                Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                    Text("${if(notice.read) "" else "• "}${notice.title}",fontWeight=FontWeight.Bold,color=if(notice.read) Color.Black else ReuseGreen)
                    Text(notice.message,fontWeight=FontWeight.SemiBold)
                    Text(relativeTime(notice.time),fontSize=14.sp,color=Color.DarkGray)
                }
            }
        }
    }
}
private fun relativeTime(time: Long): String {
    val minutes=((System.currentTimeMillis()-time)/60000).coerceAtLeast(0)
    return when {
        minutes<1 -> "Just now"
        minutes<60 -> "$minutes minutes ago"
        minutes<1440 -> "${minutes/60} hours ago"
        minutes<2880 -> "Yesterday"
        else -> SimpleDateFormat("d MMM yyyy",Locale.ENGLISH).format(Date(time))
    }
}
