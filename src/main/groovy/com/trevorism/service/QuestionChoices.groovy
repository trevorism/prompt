package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Choice
import com.trevorism.model.Question
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException

class QuestionChoices {

    private static final int MINIMUM_CHOICES = 2

    static List<Choice> normalize(List<Choice> choices) {
        if (!choices)
            return []

        List<String> assignedValues = []
        return choices.collect { Choice choice ->
            String label = choice?.label?.trim()
            String value = choice?.value?.trim() ?: deriveValue(label)
            String uniqueValue = makeUnique(value, assignedValues)
            assignedValues << uniqueValue
            new Choice(value: uniqueValue, label: label)
        }
    }

    static void validateForCreate(Question question) {
        List<Choice> choices = question.choices ?: []
        if (choices.isEmpty())
            return

        if (choices.size() < MINIMUM_CHOICES)
            throw badRequest("A multiple choice question requires at least ${MINIMUM_CHOICES} choices")
        if (choices.any { !it.label })
            throw badRequest("Every choice requires a label")
        if (choices.collect { it.value }.toSet().size() != choices.size())
            throw badRequest("Choice values must be unique")
        if (question.askChatGpt)
            throw badRequest("Chat GPT cannot answer a multiple choice question")
    }

    static void validateSelection(Question question, Answer answer) {
        List<String> selected = answer?.selectedChoices ?: []
        List<Choice> choices = question.choices ?: []

        if (choices.isEmpty()) {
            if (!selected.isEmpty())
                throw badRequest("This question does not offer choices")
            return
        }

        if (selected.isEmpty())
            throw badRequest("Select an option to answer this question")
        if (!question.allowMultipleAnswers && selected.size() > 1)
            throw badRequest("Only one option may be selected for this question")

        List<String> offered = choices.collect { it.value }
        String unknown = selected.find { !offered.contains(it) }
        if (unknown)
            throw badRequest("Unknown choice: ${unknown}")
    }

    static String describe(Question question, List<String> selectedValues) {
        List<Choice> choices = question.choices ?: []
        return (selectedValues ?: []).collect { String value ->
            choices.find { it.value == value }?.label ?: value
        }.join(", ")
    }

    private static String deriveValue(String label) {
        String slug = label?.toLowerCase()?.replaceAll(/[^a-z0-9]+/, "-")?.replaceAll(/(^-+)|(-+$)/, "")
        return slug ?: "choice"
    }

    private static String makeUnique(String value, List<String> assignedValues) {
        if (!assignedValues.contains(value))
            return value

        int suffix = 2
        while (assignedValues.contains("${value}-${suffix}".toString())) {
            suffix++
        }
        return "${value}-${suffix}"
    }

    private static HttpStatusException badRequest(String message) {
        new HttpStatusException(HttpStatus.BAD_REQUEST, message)
    }
}
