package com.group04.materialexchange.domain

import kotlinx.coroutines.flow.StateFlow

data class Student(val id: String, val name: String, val email: String)
data class MaterialItem(
    val id: String, val title: String, val category: String, val condition: String,
    val quantity: Int, val exchangeType: String, val pickupArea: String,
    val ownerId: String, val ownerName: String, val status: String = "Available",
    val photo: String = "", val description: String = ""
)
data class MaterialRequest(
    val id: String, val itemId: String, val itemTitle: String,
    val senderId: String, val senderName: String, val ownerId: String,
    val status: String = "Pending"
)
data class ExchangeNotice(
    val id: String, val userId: String, val title: String, val message: String,
    val time: Long, val read: Boolean = false
)
data class ExchangeData(
    val student: Student? = null, val listings: List<MaterialItem> = emptyList(),
    val requests: List<MaterialRequest> = emptyList(), val notices: List<ExchangeNotice> = emptyList()
)
data class ListingDraft(
    val title: String = "", val category: String = "", val condition: String = "",
    val quantity: String = "1", val exchangeType: String = "", val pickupArea: String = "",
    val photo: String = "", val description: String = ""
)

object ExchangeRules {
    val categories = listOf("Electronics", "Boards", "Filament", "Motors", "Other")
    val conditions = listOf("Like New", "Good", "Used")
    val exchangeTypes = listOf("Free", "Exchange")
    val pickupAreas = listOf("Engineering College", "Computer Science College", "Library", "Student Center")
    fun validEmail(email: String) = Regex("^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)*iau\\.edu\\.sa$", RegexOption.IGNORE_CASE).matches(email.trim())
    fun draftError(d: ListingDraft): String? = when {
        d.title.trim().length < 3 -> "Enter an item title (at least 3 characters)."
        d.category !in categories -> "Choose a category."
        d.condition !in conditions -> "Choose a condition."
        d.quantity.toIntOrNull() !in 1..999 -> "Quantity must be a whole number from 1 to 999."
        d.exchangeType !in exchangeTypes -> "Choose an exchange type."
        d.pickupArea !in pickupAreas -> "Choose a campus pickup area."
        d.photo.isBlank() -> "Add an item photo."
        else -> null
    }
    fun requestError(item: MaterialItem, userId: String, requests: List<MaterialRequest>): String? = when {
        item.ownerId == userId -> "You cannot request your own material."
        item.status != "Available" -> "This material is no longer available."
        requests.any { it.itemId == item.id && it.senderId == userId && it.status in listOf("Pending", "Accepted") } -> "You already requested this material."
        else -> null
    }
}

interface ExchangeRepository {
    val data: StateFlow<ExchangeData>
    suspend fun signIn(email: String, password: String)
    suspend fun register(name: String, email: String, password: String)
    suspend fun signOut()
    suspend fun updateProfile(name: String)
    suspend fun publish(draft: ListingDraft, editId: String? = null)
    suspend fun requestMaterial(itemId: String)
    suspend fun respond(requestId: String, accept: Boolean)
    suspend fun complete(requestId: String)
    suspend fun markRead()
    suspend fun reload()
    suspend fun signInWithGoogle(idToken: String) {
        error("Google sign-in is unavailable in this repository.")
    }

    suspend fun resetPassword(email: String) {
        error("Password reset is unavailable in this repository.")
    }
}

class RequestMaterialUseCase(private val repository: ExchangeRepository) {
    suspend operator fun invoke(itemId: String) = repository.requestMaterial(itemId)
}
