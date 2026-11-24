package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.group04.materialexchange.domain.*
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.ExchangeTheme

@Composable
fun ItemDetailsScreen(
    itemId: String,
    data: ExchangeData,
    busy: Boolean,
    onRequest: (String, () -> Unit) -> Unit,
    back: () -> Unit,
    activity: () -> Unit
) {
    val item=data.listings.firstOrNull{it.id==itemId}
    var confirm by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    LaunchedEffect(busy) { if(!busy) sending=false }
    if(item==null && busy) { PageColumn { PageTitle("Item Details",back);Box(Modifier.fillMaxWidth().padding(24.dp),contentAlignment=Alignment.Center){CircularProgressIndicator()} };return }
    if(item==null) { PageColumn { PageTitle("Item Details",back);NoticeBox("Item not found","Return to the feed to choose a material.","Go back",back) };return }
    val studentId=data.student?.id.orEmpty()
    val error=ExchangeRules.requestError(item,studentId,data.requests)
    val myRequest=data.requests.lastOrNull{it.itemId==item.id && it.senderId==studentId}
    PageColumn {
        PageTitle("Item Details",back)
        ItemPhoto(item.photo,item.title,Modifier.fillMaxWidth().height(240.dp))
        Column(Modifier.padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            Text(item.title,fontSize=20.sp,fontWeight=FontWeight.Bold)
            Text("Category: ${item.category}")
            Text("Condition: ${item.condition}")
            Text("Quantity: ${item.quantity}")
            Text("Exchange Type: ${item.exchangeType}")
            Text("Pickup Area: ${item.pickupArea}")
            Text("Owner: ${item.ownerName}")
            StatusLabel(item.status)
            if(myRequest!=null) Text("Your request: ${myRequest.status}",fontWeight=FontWeight.SemiBold)
            if(item.description.isNotBlank()) Text(item.description)
        }
        if(error!=null) Text(error,fontSize=14.sp,color=Color.DarkGray)
        RoseButton(if(sending) "Sending..." else "Request Material",{confirm=true},enabled=error==null && !busy)
    }
    if(confirm) AlertDialog(onDismissRequest={confirm=false},title={Text("Request ${item.title}?")},text={Text("Send a request to ${item.ownerName}. Pickup is at ${item.pickupArea} after the owner accepts.")},
        confirmButton={TextButton(onClick={confirm=false;sending=true;onRequest(item.id) { sent = true }},enabled=!busy){Text("Send Request")}},
        dismissButton={TextButton(onClick={confirm=false}){Text("Cancel")}})
    if(sent) AlertDialog(onDismissRequest={sent=false;activity()},title={Text("Request sent")},
        text={Text("Your request is now Pending. You can follow its status in Sent Requests.")},
        confirmButton={TextButton(onClick={sent=false;activity()}){Text("View Sent Requests")}})
}


@Preview(showBackground = true)
@Composable
private fun ItemDetailsScreenPreview() {
    val sampleItem = MaterialItem(
        id = "arduino",
        title = "Arduino Uno R3",
        category = "Boards",
        condition = "Good",
        quantity = 1,
        exchangeType = "Free",
        pickupArea = "Engineering College",
        ownerId = "ahmed",
        ownerName = "Ahmed",
        photo = "arduino",
        description = "Working Arduino board, ready for a new student project."
    )
    ExchangeTheme {
        ItemDetailsScreen(
            itemId = sampleItem.id,
            data = ExchangeData(
                student = Student("narjes", "Narjes Hasan", "2240002170@iau.edu.sa"),
                listings = listOf(sampleItem)
            ),
            busy = false,
            onRequest = { _, _ -> },
            back = {},
            activity = {}
        )
    }
}
