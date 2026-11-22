package com.group04.materialexchange.navigation

import android.net.Uri

object Routes {
    const val SignIn = "sign_in"
    const val CreateAccount = "create_account"
    const val Home = "home"
    const val ItemDetails = "details/{itemId}"
    const val CreateListing = "create"
    const val EditListing = "edit/{itemId}"
    const val MyActivity = "activity?tab={tab}"
    const val Notifications = "notifications"
    const val Profile = "profile"

    fun itemDetails(itemId: String) = "details/${Uri.encode(itemId)}"
    fun editListing(itemId: String) = "edit/${Uri.encode(itemId)}"
    fun myActivity(tab: String = "My Listings") = "activity?tab=${Uri.encode(tab)}"
}
