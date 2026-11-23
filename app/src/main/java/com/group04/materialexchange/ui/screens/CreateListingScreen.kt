package com.group04.materialexchange.ui.screens

import androidx.compose.runtime.Composable
import com.group04.materialexchange.domain.ExchangeData
import com.group04.materialexchange.ui.components.ListingFormContent
import com.group04.materialexchange.viewmodel.ExchangeViewModel

@Composable
fun CreateListingScreen(
    data: ExchangeData,
    busy: Boolean,
    vm: ExchangeViewModel,
    back: () -> Unit,
    done: () -> Unit
) {
    ListingFormContent(editId = null, data = data, busy = busy, vm = vm, back = back, done = done)
}
