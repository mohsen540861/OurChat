package com.mahoor.ourchat.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahoor.ourchat.model.ChatMessage
import com.mahoor.ourchat.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repo = ChatRepository()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow("")
    val currentUser: StateFlow<String> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    init {
        if (repo.isLoggedIn()) {
            _isLoggedIn.value = true
            _currentUser.value = repo.getDisplayName()
            startListening()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            val result = repo.login(email, password)
            if (result.isSuccess) {
                _isLoggedIn.value = true
                _currentUser.value = repo.getDisplayName()
                startListening()
            } else {
                _loginError.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun logout() {
        repo.logout()
        _isLoggedIn.value = false
        _currentUser.value = ""
        _messages.value = emptyList()
    }

    private fun startListening() {
        viewModelScope.launch {
            repo.getMessages().collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true
            try {
                repo.sendTextMessage(text)
            } finally {
                _isSending.value = false
            }
        }
    }

    fun sendFile(uri: Uri, fileName: String, fileType: String) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                repo.uploadFile(uri, fileName, fileType)
            } finally {
                _isSending.value = false
            }
        }
    }
}