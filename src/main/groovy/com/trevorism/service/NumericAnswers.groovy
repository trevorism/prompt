package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.AnswerType
import com.trevorism.model.Question
import com.trevorism.model.QuestionKind
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException

class NumericAnswers {

    private static final String NUMBER = /-?\d+(\.\d+)?/

    static boolean expectsNumber(Question question) {
        question?.answerType == AnswerType.NUMBER
    }

    static void validateForCreate(Question question) {
        if (question.answerType != null && !(question.answerType in AnswerType.ALL))
            throw badRequest("answerType must be one of ${AnswerType.ALL}")
        if (!expectsNumber(question)) {
            if (question.unit || question.minValue != null || question.maxValue != null)
                throw badRequest("unit, minValue and maxValue apply only to number questions")
            return
        }
        if (question.choices)
            throw badRequest("A number question cannot offer choices")
        if (question.kind == QuestionKind.APPROVAL)
            throw badRequest("An approval cannot expect a number")
        if (question.askChatGpt)
            throw badRequest("Chat GPT cannot answer a number question")
        if (question.minValue != null && question.maxValue != null && question.minValue > question.maxValue)
            throw badRequest("minValue must not be greater than maxValue")
    }

    static void applyToAnswer(Question question, Answer answer) {
        if (!expectsNumber(question)) {
            answer.value = null
            return
        }
        String text = answer.text?.trim()
        if (!text || !(text ==~ NUMBER))
            throw badRequest("Enter a number")
        double value = Double.parseDouble(text)
        if (question.minValue != null && value < question.minValue)
            throw badRequest("Enter a number of at least ${format(question.minValue)}")
        if (question.maxValue != null && value > question.maxValue)
            throw badRequest("Enter a number of at most ${format(question.maxValue)}")
        answer.value = value
        answer.text = question.unit ? "${format(value)} ${question.unit}".toString() : format(value)
    }

    static String format(double value) {
        value == Math.rint(value) && Math.abs(value) < 1e15 ? String.valueOf((long) value) : String.valueOf(value)
    }

    private static HttpStatusException badRequest(String message) {
        new HttpStatusException(HttpStatus.BAD_REQUEST, message)
    }
}
