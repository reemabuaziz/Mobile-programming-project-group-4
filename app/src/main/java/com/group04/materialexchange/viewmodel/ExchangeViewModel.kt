package com.group04.materialexchange.viewmodel

import com.group04.materialexchange.data.FirebaseExchangeRepository
import com.group04.materialexchange.domain.*

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect

class ExchangeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository =
        FirebaseExchangeRepository(application)
    private val requestMaterial = RequestMaterialUseCase(repository)
    val data = repository.data
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()
    private var retryAction: (() -> Unit)? = null
    init {
        viewModelScope.launch {
            repository.syncError.collect { message ->
                if (message != null) {
                    mutableError.value = message
                }
            }
        }

        runAction {
            reload()
        }
    }
    fun clearError() { mutableError.value = null }
    fun retry() { retryAction?.invoke() }
    private fun runAction(onSuccess: () -> Unit = {}, action: suspend ExchangeRepository.() -> Unit) {
        if (mutableBusy.value) return
        retryAction = { runAction(onSuccess, action) }
        mutableBusy.value = true; mutableError.value = null
        viewModelScope.launch {
            try {
                repository.action()
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableError.value =
                    e.message ?: "Something went wrong. Please try again."
            } finally {
                mutableBusy.value = false
            }

        }
    }
    fun request(itemId: String, onSuccess: () -> Unit) = runAction(onSuccess) { this@ExchangeViewModel.requestMaterial(itemId) }
    fun signIn(email: String, password: String) = runAction { signIn(email, password) }
    fun register(name: String, email: String, password: String) = runAction { register(name, email, password) }
    fun signOut() = runAction { signOut() }
    fun updateProfile(name: String, done: () -> Unit) = runAction(done) { updateProfile(name) }
    fun publish(draft: ListingDraft, editId: String?, done: () -> Unit) = runAction(done) { publish(draft, editId) }
    fun respond(requestId: String, accept: Boolean) = runAction { respond(requestId, accept) }
    fun complete(requestId: String) = runAction { complete(requestId) }
    fun markRead() = runAction { markRead() }
    fun reload() = runAction { reload() }
    fun signInWithGoogle(idToken: String) = runAction {
        signInWithGoogle(idToken)
    }

    fun resetPassword(
        email: String,
        onSent: () -> Unit
    ) = runAction(onSent) {
        resetPassword(email)
    }
    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}
