package com.mahoor.ourchat.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.mahoor.ourchat.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val messagesRef = db.collection("messages")

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun getDisplayName(): String {
        val email = getCurrentUserEmail() ?: return "Unknown"
        return if (email.startsWith("mahoor")) "Mahoor" else "Homayoun"
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    suspend fun sendTextMessage(text: String) {
        val message = ChatMessage(
            sender = getDisplayName(),
            text = text,
            fileType = "text"
        )
        messagesRef.add(message).await()
    }

    suspend fun uploadFile(uri: Uri, fileName: String, fileType: String) {
        val storageRef = storage.reference.child("files/${System.currentTimeMillis()}_$fileName")
        storageRef.putFile(uri).await()

        val downloadUrl = storageRef.downloadUrl.await().toString()

        val message = ChatMessage(
            sender = getDisplayName(),
            fileUrl = downloadUrl,
            fileName = fileName,
            fileType = fileType
        )
        messagesRef.add(message).await()
    }
}