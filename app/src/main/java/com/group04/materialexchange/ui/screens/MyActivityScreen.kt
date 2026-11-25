
package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.group04.materialexchange.domain.ExchangeData
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.*
import com.group04.materialexchange.viewmodel.ExchangeViewModel

@Composable
fun MyActivityScreen(
    data: ExchangeData,
    busy: Boolean,
    vm: ExchangeViewModel,
    initialTab: String,
    edit: (String) -> Unit,
    openItem: (String) -> Unit,
    add: () -> Unit
) {
    val student = data.student ?: return

    var tab by rememberSaveable(initialTab) {
        mutableStateOf(initialTab)
    }

    var confirmComplete by remember {
        mutableStateOf<String?>(null)
    }

    val myListings = data.listings.filter {
        it.ownerId == student.id
    }

    val sentRequests = data.requests.filter {
        it.senderId == student.id
    }

    val receivedRequests = data.requests.filter {
        it.ownerId == student.id
    }

    PageColumn {
        PageTitle("My Activity")

        Spacer(Modifier.height(8.dp))

        listOf(
            "My Listings",
            "Sent Requests",
            "Received Requests"
        ).forEach { option ->

            Button(
                onClick = { tab = option },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tab == option) MutedRose else Blush,
                    contentColor = Color.Black
                )
            ) {
                if (option != "My Listings") {
                    Glyph("mail", Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    option,
                    fontWeight = if (tab == option)
                        FontWeight.Bold
                    else
                        FontWeight.Normal
                )

                Spacer(Modifier.weight(1f))

                if (tab == option) {
                    Text("✓", color = ReuseGreen)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when (tab) {

            "My Listings" -> {

                if (myListings.isEmpty()) {

                    NoticeBox(
                        "No listings yet",
                        "List a material to help another student reuse it.",
                        "Create Listing",
                        add
                    )

                } else {

                    myListings.forEach { item ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = Blush
                            )
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                Text(
                                    item.title,
                                    fontWeight = FontWeight.Bold
                                )

                                StatusLabel(item.status)

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {

                                    TextButton(
                                        onClick = {
                                            openItem(item.id)
                                        }
                                    ) {
                                        Text("Details")
                                    }

                                    TextButton(
                                        onClick = {
                                            edit(item.id)
                                        },
                                        enabled = item.status == "Available" && !busy
                                    ) {
                                        Text("Edit")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Sent Requests" -> {

                if (sentRequests.isEmpty()) {

                    NoticeBox(
                        "No sent requests yet",
                        "Open a material in Home and send a request."
                    )

                } else {

                    sentRequests.asReversed().forEach { request ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = Blush
                            )
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    request.itemTitle,
                                    fontWeight = FontWeight.Bold
                                )

                                StatusLabel(request.status)

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    TextButton(
                                        onClick = {
                                            openItem(request.itemId)
                                        }
                                    ) {
                                        Text("Details")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Received Requests" -> {

                if (receivedRequests.isEmpty()) {

                    NoticeBox(
                        "No received requests yet",
                        "Requests from other students will appear here."
                    )

                } else {

                    receivedRequests.asReversed().forEach { request ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = Blush
                            )
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    "Request from ${request.senderName}",
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    request.itemTitle,
                                    fontWeight = FontWeight.Bold
                                )

                                StatusLabel(request.status)

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    TextButton(
                                        onClick = {
                                            openItem(request.itemId)
                                        }
                                    ) {
                                        Text("Details")
                                    }

                                    if (request.status == "Pending") {

                                        TextButton(
                                            onClick = {
                                                vm.respond(request.id, true)
                                            },
                                            enabled = !busy
                                        ) {
                                            Text(
                                                "Accept",
                                                color = ReuseGreen
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                vm.respond(request.id, false)
                                            },
                                            enabled = !busy
                                        ) {
                                            Text(
                                                "Reject",
                                                color = RejectRed
                                            )
                                        }
                                    }

                                    if (request.status == "Accepted") {

                                        TextButton(
                                            onClick = {
                                                confirmComplete = request.id
                                            },
                                            enabled = !busy
                                        ) {
                                            Text("Complete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (confirmComplete != null) {

        AlertDialog(
            onDismissRequest = {
                confirmComplete = null
            },

            title = {
                Text("Complete exchange?")
            },

            text = {
                Text(
                    "Confirm only after the material has been handed over. " +
                            "Both students will see the exchange as completed."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val requestId = confirmComplete

                        confirmComplete = null

                        if (requestId != null) {
                            vm.complete(requestId)
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        confirmComplete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
