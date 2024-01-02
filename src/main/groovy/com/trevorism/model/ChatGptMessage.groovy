package com.trevorism.model

import com.google.gson.Gson

class ChatGptMessage {
    private static final Gson gson = new Gson()

    String message
    String context = "Keep all responses under 150 words."

    String toJson() {
        gson.toJson(this)
    }

    static ChatGptMessage fromJson(String json) {
        gson.fromJson(json, ChatGptMessage)
    }
}
