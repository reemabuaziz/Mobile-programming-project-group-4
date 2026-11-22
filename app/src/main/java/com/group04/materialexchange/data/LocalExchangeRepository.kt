package com.group04.materialexchange.data

import com.group04.materialexchange.domain.*

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class LocalExchangeRepository(context: Context) : ExchangeRepository {
    private val prefs = context.getSharedPreferences("material_exchange_alpha", Context.MODE_PRIVATE)
    private val accounts = mutableListOf<JSONObject>()
    private val mutableData = MutableStateFlow(ExchangeData())
    override val data = mutableData.asStateFlow()
    init {
        val saved = prefs.getString("data", null)
        if (saved == null) seed() else restore(JSONObject(saved))
    }
    private fun id() = UUID.randomUUID().toString()
    private fun user() = requireNotNull(data.value.student) { "Sign in to continue." }
    private fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.decode(salt, Base64.NO_WRAP), 20000, 256)
        return try { Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, Base64.NO_WRAP) }
        finally { spec.clearPassword() }
    }
    private fun account(student: Student, password: String): JSONObject {
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        return JSONObject().put("id", student.id).put("name", student.name).put("email", student.email)
            .put("salt", salt).put("hash", hash(password, salt))
    }
    private fun seed() {
        accounts.addAll(listOf(
            account(Student("narjes", "Narjes Hassan", "2240002170@iau.edu.sa"), "Demo1234"),
            account(Student("ahmed", "Ahmed", "ahmed@iau.edu.sa"), "Demo1234"),
            account(Student("sara", "Sara", "sara@iau.edu.sa"), "Demo1234")
        ))
        mutableData.value = ExchangeData(listings = listOf(
            MaterialItem("arduino", "Arduino Uno R3", "Boards", "Good", 1, "Free", "Engineering College", "ahmed", "Ahmed", photo = "arduino", description = "Working Arduino board, ready for a new student project."),
            MaterialItem("pla", "PLA Filament - Black", "Filament", "Like New", 1, "Exchange", "Library", "sara", "Sara", photo = "filament"),
            MaterialItem("esp32", "ESP32 Board", "Boards", "Good", 1, "Free", "Computer Science College", "narjes", "Narjes Hassan", photo = "arduino"),
            MaterialItem("servo", "Servo Motor", "Motors", "Good", 2, "Exchange", "Engineering College", "narjes", "Narjes Hassan", status = "Reserved", photo = "placeholder"),
            MaterialItem("pi", "Raspberry Pi 4", "Boards", "Good", 1, "Exchange", "Student Center", "narjes", "Narjes Hassan", photo = "arduino")
        ), requests = listOf(
            MaterialRequest("sara-pi", "pi", "Raspberry Pi 4", "sara", "Sara", "narjes"),
            MaterialRequest("ahmed-servo", "servo", "Servo Motor", "ahmed", "Ahmed", "narjes", "Accepted")
        ), notices = listOf(ExchangeNotice(id(), "narjes", "New Request", "Sara requested your Raspberry Pi 4.", System.currentTimeMillis() - 600000)))
        save()
    }
    override suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        require(ExchangeRules.validEmail(email)) { "Use your IAU university email." }
        val a = accounts.firstOrNull { it.getString("email").equals(email.trim(), true) }
        require(a != null && hash(password, a.getString("salt")) == a.getString("hash")) { "Email or password is incorrect. Try the demo account below." }
        mutableData.value = data.value.copy(student = Student(a.getString("id"), a.getString("name"), a.getString("email")))
        save()
    }
    override suspend fun register(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        require(name.trim().length >= 2) { "Enter your full name." }
        require(ExchangeRules.validEmail(email)) { "Use an IAU university email (name@iau.edu.sa)." }
        require(password.length >= 8) { "Use at least 8 characters for your password." }
        require(accounts.none { it.getString("email").equals(email.trim(), true) }) { "This email already has a local account. Sign in instead." }
        val student = Student(id(), name.trim(), email.trim().lowercase())
        accounts.add(account(student, password))
        mutableData.value = data.value.copy(student = student)
        save()
    }
    override suspend fun signOut() { mutableData.value = data.value.copy(student = null); save() }
    override suspend fun updateProfile(name: String) {
        require(name.trim().length >= 2) { "Enter a name with at least 2 characters." }
        val student = user().copy(name = name.trim())
        accounts.first { it.getString("id") == student.id }.put("name", student.name)
        mutableData.value = data.value.copy(student = student, listings = data.value.listings.map {
            if (it.ownerId == student.id) it.copy(ownerName = student.name) else it
        })
        save()
    }
    override suspend fun publish(draft: ListingDraft, editId: String?) {
        val student = user()
        ExchangeRules.draftError(draft)?.let { error(it) }
        val previous = editId?.let { itemId -> data.value.listings.first { it.id == itemId } }
        require(previous == null || (previous.ownerId == student.id && previous.status == "Available")) { "Only your available listings can be edited." }
        val item = MaterialItem(editId ?: id(), draft.title.trim(), draft.category, draft.condition,
            draft.quantity.toInt(), draft.exchangeType, draft.pickupArea, student.id, student.name,
            photo = draft.photo, description = draft.description.trim())
        mutableData.value = data.value.copy(listings = if (editId == null) data.value.listings + item else data.value.listings.map { if (it.id == editId) item else it })
        save()
    }
    override suspend fun requestMaterial(itemId: String) {
        val student = user()
        val item = data.value.listings.first { it.id == itemId }
        ExchangeRules.requestError(item, student.id, data.value.requests)?.let { error(it) }
        val request = MaterialRequest(id(), item.id, item.title, student.id, student.name, item.ownerId)
        mutableData.value = data.value.copy(requests = data.value.requests + request,
            notices = data.value.notices + ExchangeNotice(id(), item.ownerId, "New Request", "${student.name} requested your ${item.title}.", System.currentTimeMillis()))
        save()
    }
    override suspend fun respond(requestId: String, accept: Boolean) {
        val request = data.value.requests.first { it.id == requestId }
        require(request.ownerId == user().id && request.status == "Pending") { "This request can no longer be changed." }
        val item = data.value.listings.first { it.id == request.itemId }
        require(!accept || item.status == "Available") { "This item is no longer available." }
        val otherPending = if (accept) data.value.requests.filter { it.itemId == item.id && it.id != requestId && it.status == "Pending" } else emptyList()
        mutableData.value = data.value.copy(
            requests = data.value.requests.map {
                when { it.id == requestId -> it.copy(status = if (accept) "Accepted" else "Rejected")
                    it in otherPending -> it.copy(status = "Rejected")
                    else -> it }
            },
            listings = data.value.listings.map { if (accept && it.id == item.id) it.copy(status = "Reserved") else it },
            notices = data.value.notices + ExchangeNotice(id(), request.senderId, if (accept) "Request Accepted" else "Request Rejected", "Your request for ${item.title} was ${if (accept) "accepted" else "rejected"}.", System.currentTimeMillis()) +
                otherPending.map { ExchangeNotice(id(), it.senderId, "Request Rejected", "${item.title} was reserved for another student.", System.currentTimeMillis()) }
        )
        save()
    }
    override suspend fun complete(requestId: String) {
        val r = data.value.requests.first { it.id == requestId }
        require(r.ownerId == user().id && r.status == "Accepted") { "Only the owner can complete an accepted exchange." }
        mutableData.value = data.value.copy(
            requests = data.value.requests.map { if (it.id == r.id) it.copy(status = "Completed") else it },
            listings = data.value.listings.map { if (it.id == r.itemId) it.copy(status = "Completed") else it },
            notices = data.value.notices + listOf(r.ownerId, r.senderId).map { ExchangeNotice(id(), it, "Exchange Completed", "Your ${r.itemTitle} exchange has been completed.", System.currentTimeMillis()) }
        )
        save()
    }
    override suspend fun markRead() {
        val student = user()
        mutableData.value = data.value.copy(notices = data.value.notices.map { if (it.userId == student.id) it.copy(read = true) else it })
        save()
    }
    override suspend fun reload() { withContext(Dispatchers.IO) { prefs.getString("data", null)?.let { restore(JSONObject(it)) } } }
    private fun save() {
        val s = data.value
        val obj = JSONObject().put("accounts", JSONArray(accounts)).put("session", s.student?.id ?: "")
        obj.put("listings", JSONArray(s.listings.map { i -> JSONObject().apply {
            put("id", i.id); put("title", i.title); put("category", i.category); put("condition", i.condition)
            put("quantity", i.quantity); put("exchangeType", i.exchangeType); put("pickupArea", i.pickupArea)
            put("ownerId", i.ownerId); put("ownerName", i.ownerName); put("status", i.status); put("photo", i.photo); put("description", i.description)
        } }))
        obj.put("requests", JSONArray(s.requests.map { r -> JSONObject().apply {
            put("id", r.id); put("itemId", r.itemId); put("itemTitle", r.itemTitle); put("senderId", r.senderId)
            put("senderName", r.senderName); put("ownerId", r.ownerId); put("status", r.status)
        } }))
        obj.put("notices", JSONArray(s.notices.map { n -> JSONObject().apply {
            put("id", n.id); put("userId", n.userId); put("title", n.title); put("message", n.message); put("time", n.time); put("read", n.read)
        } }))
        prefs.edit().putString("data", obj.toString()).apply()
    }
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    private fun restore(obj: JSONObject) {
        accounts.clear(); accounts.addAll(obj.getJSONArray("accounts").objects())
        val session = obj.optString("session")
        val a = accounts.firstOrNull { it.getString("id") == session }
        mutableData.value = ExchangeData(
            student = a?.let { Student(it.getString("id"), it.getString("name"), it.getString("email")) },
            listings = obj.getJSONArray("listings").objects().map { MaterialItem(it.getString("id"), it.getString("title"), it.getString("category"), it.getString("condition"), it.getInt("quantity"), it.getString("exchangeType"), it.getString("pickupArea"), it.getString("ownerId"), it.getString("ownerName"), it.getString("status"), it.optString("photo"), it.optString("description")) },
            requests = obj.getJSONArray("requests").objects().map { MaterialRequest(it.getString("id"), it.getString("itemId"), it.getString("itemTitle"), it.getString("senderId"), it.getString("senderName"), it.getString("ownerId"), it.getString("status")) },
            notices = obj.getJSONArray("notices").objects().map { ExchangeNotice(it.getString("id"), it.getString("userId"), it.getString("title"), it.getString("message"), it.getLong("time"), it.getBoolean("read")) }
        )
    }
}
