package com.trevorism.service

import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ChatGptMessage
import jakarta.inject.Singleton

@Singleton
class DefaultChatService implements ChatService {

    private static final String CHAT_URL = "https://chat.action.trevorism.com/api/chat"

    private final SecureHttpClient secureHttpClient

    DefaultChatService(SecureHttpClient secureHttpClient) {
        this.secureHttpClient = secureHttpClient
    }

    @Override
    String ask(String questionText) {
        String response = secureHttpClient.post(CHAT_URL, new ChatGptMessage(message: questionText).toJson())
        return ChatGptMessage.fromJson(response).message
    }
}
