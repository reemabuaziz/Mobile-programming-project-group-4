package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val item = data.listings.firstOrNull { it.id == itemId }

    var confirmRequest by remember { mutableStateOf(false) }
    var requestSent by remember { mutableStateOf(false) }

    if (item == null) {
        PageColumn {
            PageTitle("Item Details", back)

            NoticeBox(
                title = "Item not found",
                text = "Return to the feed to choose a material.",
                actionLabel = "Go back",
                action = back
            )
        }
        return
    }

    val requestError = ExchangeRules.requestError(
        item,
        data.student?.id.orEmpty(),
        data.requests
    )

    PageColumn {
        PageTitle("Item Details", back)

        ItemPhoto(
            photo = item.photo,
            title = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            ItemDetailRow("Category", item.category)
            ItemDetailRow("Condition", item.condition)
            ItemDetailRow("Quantity", item.quantity.toString())
            ItemDetailRow("Exchange Type", item.exchangeType)
            ItemDetailRow("Pickup Area", item.pickupArea)
            ItemDetailRow("Owner", item.ownerName)

            StatusLabel(item.status)

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    fontSize = 14.sp
                )
            }
        }

        if (requestError != null) {
            Text(
                text = requestError,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }

        RoseButton(
            text = "Request Material",
            onClick = { confirmRequest = true },
            enabled = requestError == null && !busy
        )
    }

    if (confirmRequest) {
        AlertDialog(
            onDismissRequest = { confirmRequest = false },
            title = {
                Text("Request ${item.title}?")
            },
            text = {
                Text(
                    "Send a request to ${item.ownerName}. " +
                            "Pickup is at ${item.pickupArea} after the owner accepts."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRequest = false
                        onRequest(item.id) {
                            requestSent = true
                        }
                    },
                    enabled = !busy
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmRequest = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (requestSent) {
        AlertDialog(
            onDismissRequest = {
                requestSent = false
                activity()
            },
            title = {
                Text("Request sent")
            },
            text = {
                Text(
                    "Your request is now Pending. " +
                            "You can follow its status in Sent Requests."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        requestSent = false
                        activity()
                    }
                ) {
                    Text("View Sent Requests")
                }
            }
        )
    }
}

@Composable
private fun ItemDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium
        )

        Text(text = value)
    }
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
                student = Student(
                    "Narjes",
                    "Narjes Hasan",
                    "2240002170@iau.edu.sa"
                ),
                listings = listOf(sampleItem)
            ),
            busy = false,
            onRequest = { _, _ -> },
            back = {},
            activity = {}
        )
    }
}