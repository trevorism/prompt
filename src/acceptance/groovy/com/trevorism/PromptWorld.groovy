package com.trevorism

import com.google.gson.Gson
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient

class PromptWorld {

    static final String BASE_URL = System.getenv("ACCEPTANCE_BASE_URL") ?: "https://prompt.action.trevorism.com"
    static final String MARKER = "[acceptance]"

    private final Gson gson = new Gson()
    private final SecureHttpClient authClient = new AppClientSecureHttpClient()
    private final HttpClient anonClient = new JsonHttpClient()

    final List<String> createdQuestionIds = []
    final List<String> createdAnswerIds = []

    String body
    boolean rejected
    Map lastQuestion
    Map lastAnswer
    String currentQuestionId

    Map createQuestion(Map question) {
        body = authClient.post("${BASE_URL}/api/question/".toString(), gson.toJson(question))
        Map created = gson.fromJson(body, Map)
        if (created?.id) createdQuestionIds << (created.id as String)
        return created
    }

    Map createPlainQuestion() {
        createQuestion([text: "${MARKER} plain question".toString(), kind: "question"])
    }

    Map createApproval(String approverId) {
        createQuestion([text: "${MARKER} approval request".toString(), kind: "approval", targetIdentityId: approverId])
    }

    Map fetchQuestion(String id) {
        body = authClient.get("${BASE_URL}/api/question/${id}".toString())
        return gson.fromJson(body, Map)
    }

    Map answer(String id, Map answerBody) {
        body = authClient.post("${BASE_URL}/api/question/${id}/answer".toString(), gson.toJson(answerBody))
        Map created = gson.fromJson(body, Map)
        if (created?.id) createdAnswerIds << (created.id as String)
        return created
    }

    Map answerText(String id, String text) {
        answer(id, [text: text])
    }

    Map decide(String id, boolean approved, String reason) {
        answer(id, [text: reason, approved: approved])
    }

    // Anonymous calls: the trevorism HttpClient throws on non-2xx, so a rejected (401)
    // secured endpoint surfaces as an exception -> rejected == true.
    void anonGet(String path) {
        try {
            body = anonClient.get("${BASE_URL}/${path}".toString())
            rejected = false
        } catch (Exception ignored) {
            rejected = true
            body = null
        }
    }

    void anonPost(String path) {
        try {
            body = anonClient.post("${BASE_URL}/${path}".toString(), "{}")
            rejected = false
        } catch (Exception ignored) {
            rejected = true
            body = null
        }
    }

    void cleanup() {
        createdAnswerIds.each { String id -> try { authClient.delete("${BASE_URL}/api/answer/${id}".toString()) } catch (ignored) {} }
        createdQuestionIds.each { String id -> try { authClient.delete("${BASE_URL}/api/question/${id}".toString()) } catch (ignored) {} }
        createdAnswerIds.clear()
        createdQuestionIds.clear()
    }
}
