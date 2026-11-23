package com.group04.materialexchange.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.group04.materialexchange.domain.*
import com.group04.materialexchange.ui.components.*
import com.group04.materialexchange.ui.theme.*

@Composable
fun HomeFeedScreen(data: ExchangeData, openItem: (String)->Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var filterOpen by remember { mutableStateOf(false) }
    var condition by rememberSaveable { mutableStateOf("Any") }
    var type by rememberSaveable { mutableStateOf("Any") }
    var availableOnly by rememberSaveable { mutableStateOf(false) }
    val items=data.listings.filter { i ->
        (category=="All" || i.category==category) &&
        (condition=="Any" || i.condition==condition) && (type=="Any" || i.exchangeType==type) &&
        (!availableOnly || i.status=="Available") &&
        (i.title.contains(search,true) || i.category.contains(search,true) || i.description.contains(search,true))
    }
    PageColumn {
        Text("Project Material Exchange",fontSize=24.sp,fontWeight=FontWeight.Bold,
            modifier=Modifier.align(Alignment.CenterHorizontally).padding(vertical=12.dp))
        ExchangeField(search,{search=it},"Search materials...",icon="search")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            (listOf("All")+ExchangeRules.categories).forEach { c -> FilterChip(selected=category==c,onClick={category=c},
                label={Text(c,fontWeight=FontWeight.SemiBold)},shape=CardShape,
                colors=FilterChipDefaults.filterChipColors(containerColor=Blush,selectedContainerColor=MutedRose),
                modifier=Modifier.heightIn(min=48.dp)) }
        }
        OutlinedButton(onClick={filterOpen=true},shape=CardShape,colors=ButtonDefaults.outlinedButtonColors(containerColor=Blush)) {
            Glyph("filter"); Spacer(Modifier.width(8.dp));Text("Filter${if(condition!="Any" || type!="Any" || availableOnly) " •" else ""}")
        }
        Spacer(Modifier.height(8.dp))
        Text("Available Materials",fontSize=20.sp,fontWeight=FontWeight.Bold)
        if(items.isEmpty()) NoticeBox("No materials found", "Try another keyword or clear the filters.","Clear search & filters") {
            search="";category="All";condition="Any";type="Any";availableOnly=false
        }
        items.forEach { i ->
            Card(onClick={openItem(i.id)}, modifier=Modifier.fillMaxWidth(),shape=CardShape,
                colors=CardDefaults.cardColors(containerColor=Color.White),border=BorderStroke(1.dp,Color.Black)) {
                Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    ItemPhoto(i.photo,i.title,Modifier.size(108.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Text(i.title,fontWeight=FontWeight.Bold)
                        Text("Condition: ${i.condition}")
                        Text(i.exchangeType,fontWeight=FontWeight.SemiBold)
                        StatusLabel(i.status)
                    }
                }
            }
        }
        Spacer(Modifier.height(88.dp))
    }
    if(filterOpen) AlertDialog(onDismissRequest={filterOpen=false},title={Text("Filter materials")},text={
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            ChoiceField("Condition",condition,listOf("Any")+ExchangeRules.conditions){condition=it}
            ChoiceField("Exchange Type",type,listOf("Any")+ExchangeRules.exchangeTypes){type=it}
            Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(availableOnly,{availableOnly=it});Text("Available only") }
        }
    },confirmButton={TextButton(onClick={filterOpen=false}){Text("Apply")}},dismissButton={TextButton(onClick={condition="Any";type="Any";availableOnly=false;filterOpen=false}){Text("Reset")}})
}


@Preview(showBackground = true)
@Composable
private fun HomeFeedScreenPreview() {
    val sampleListings = listOf(
        MaterialItem("arduino", "Arduino Uno R3", "Boards", "Good", 1, "Free", "Engineering College", "ahmed", "Ahmed", photo = "arduino"),
        MaterialItem("pla", "PLA Filament - Black", "Filament", "Like New", 1, "Exchange", "Library", "sara", "Sara", photo = "filament")
    )
    ExchangeTheme {
        HomeFeedScreen(ExchangeData(listings = sampleListings)) {}
    }
}
