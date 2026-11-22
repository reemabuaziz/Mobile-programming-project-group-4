package com.group04.materialexchange.ui.components

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group04.materialexchange.domain.*
import com.group04.materialexchange.ui.theme.*
import com.group04.materialexchange.viewmodel.ExchangeViewModel

@Composable
fun ListingFormContent(editId: String?, data: ExchangeData, busy: Boolean, vm: ExchangeViewModel, back: () -> Unit, done: () -> Unit) {
    val old=data.listings.firstOrNull{it.id==editId}
    var title by rememberSaveable(editId) { mutableStateOf(old?.title.orEmpty()) }
    var category by rememberSaveable(editId) { mutableStateOf(old?.category.orEmpty()) }
    var condition by rememberSaveable(editId) { mutableStateOf(old?.condition.orEmpty()) }
    var quantity by rememberSaveable(editId) { mutableStateOf(old?.quantity?.toString() ?: "1") }
    var type by rememberSaveable(editId) { mutableStateOf(old?.exchangeType.orEmpty()) }
    var pickup by rememberSaveable(editId) { mutableStateOf(old?.pickupArea.orEmpty()) }
    var photo by rememberSaveable(editId) { mutableStateOf(old?.photo.orEmpty()) }
    var description by rememberSaveable(editId) { mutableStateOf(old?.description.orEmpty()) }
    var localError by remember { mutableStateOf<String?>(null) }
    var discard by remember { mutableStateOf(false) }
    var photoMenu by remember { mutableStateOf(false) }
    val context=LocalContext.current
    val initial=old?.let{ListingDraft(it.title,it.category,it.condition,it.quantity.toString(),it.exchangeType,it.pickupArea,it.photo,it.description)} ?: ListingDraft()
    val draft=ListingDraft(title,category,condition,quantity,type,pickup,photo,description)
    fun leave() { if(draft!=initial) discard=true else back() }
    BackHandler { leave() }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photo=uri.toString()
        }
    }
    PageColumn(Modifier.imePadding()) {
        PageTitle(if(editId==null) "Create Listing" else "Edit Listing",{leave()})
        Box(Modifier.fillMaxWidth().height(160.dp).background(Color(0xFFF8D4D4),CardShape).clickable{photoMenu=true},contentAlignment=Alignment.Center) {
            if(photo.isBlank()) Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Glyph("photo",Modifier.size(48.dp));Text("Add Item Photo",fontSize=20.sp,fontWeight=FontWeight.Bold)
            } else { ItemPhoto(photo,title,Modifier.fillMaxSize().padding(8.dp));Text("Change photo",modifier=Modifier.align(Alignment.BottomCenter).background(Blush).padding(4.dp)) }
        }
        ExchangeField(title,{title=it},"Item Title")
        ChoiceField("Category",category,ExchangeRules.categories){category=it}
        ChoiceField("Condition",condition,ExchangeRules.conditions){condition=it}
        ExchangeField(quantity,{quantity=it.filter{c->c.isDigit()}.take(3)},"Quantity",keyboard=KeyboardType.Number)
        ChoiceField("Exchange Type",type,ExchangeRules.exchangeTypes){type=it}
        ChoiceField("Pickup Area",pickup,ExchangeRules.pickupAreas){pickup=it}
        ExchangeField(description,{description=it},"Description (optional)")
        if(localError!=null) Text(localError!!,color=RejectRed)
        RoseButton(if(editId==null) "Publish Listing" else "Save Changes",{
            localError=ExchangeRules.draftError(draft)
            if(localError==null) vm.publish(draft,editId,done)
        },enabled=!busy)
    }
    if(photoMenu) AlertDialog(onDismissRequest={photoMenu=false},title={Text("Item photo")},text={
        Column {
            TextButton(onClick={photoMenu=false;picker.launch(arrayOf("image/*"))}){Text("Choose from device")}
            TextButton(onClick={photo="arduino";photoMenu=false}){Text("Use sample board photo")}
            TextButton(onClick={photo="filament";photoMenu=false}){Text("Use sample filament photo")}
        }
    },confirmButton={TextButton(onClick={photoMenu=false}){Text("Cancel")}})
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Discard changes?")},text={Text("Your unsaved listing changes will be lost.")},
        confirmButton={TextButton(onClick={discard=false;back()}){Text("Discard",color=RejectRed)}},dismissButton={TextButton(onClick={discard=false}){Text("Keep Editing")}})
}
