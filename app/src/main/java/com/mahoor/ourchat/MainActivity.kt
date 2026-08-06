package com.mahoor.ourchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahoor.ourchat.screens.ChatScreen
import com.mahoor.ourchat.screens.LoginScreen
import com.mahoor.ourchat.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val colorScheme = if (isSystemInDarkTheme()) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                val viewModel: ChatViewModel = viewModel()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val messages by viewModel.messages.collectAsState()
                val isSending by viewModel.isSending.collectAsState()
                val loginError by viewModel.loginError.collectAsState()

                if (isLoggedIn) {
                    ChatScreen(
                        messages = messages,
                        currentUser = currentUser,
                        isSending = isSending,
                        onSendText = { text -> viewModel.sendText(text) },
                        onSendFile = { uri, name, type ->
                            viewModel.sendFile(uri, name, type)
                        },
                        onLogout = { viewModel.logout() }
                    )
                } else {
                    LoginScreen(
                        onLogin = { email, password ->
                            viewModel.login(email, password)
                        },
                        errorMessage = loginError
                    )
                }
            }
        }
    }
}