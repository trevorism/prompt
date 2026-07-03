package com.trevorism.service

interface ChatService {

    /** Identity attributed to answers produced by the AI chat service. */
    String CHAT_GPT_IDENTITY = "Chat GPT"

    /**
     * Asks the AI chat service the given question text and returns its answer.
     */
    String ask(String questionText)
}
