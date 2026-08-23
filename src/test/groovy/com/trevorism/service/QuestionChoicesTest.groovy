package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Choice
import com.trevorism.model.Question
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import org.junit.jupiter.api.Test

class QuestionChoicesTest {

    private static Question choiceQuestion(boolean allowMultipleAnswers = false) {
        new Question(text: "pick", allowMultipleAnswers: allowMultipleAnswers, choices: [
                new Choice(value: "red", label: "Red"),
                new Choice(value: "green", label: "Green")
        ])
    }

    @Test
    void normalizeReturnsEmptyListForNull() {
        assert QuestionChoices.normalize(null) == []
    }

    @Test
    void normalizeDerivesValueFromLabel() {
        List<Choice> result = QuestionChoices.normalize([new Choice(label: "Ship It Now")])

        assert result.size() == 1
        assert result[0].value == "ship-it-now"
        assert result[0].label == "Ship It Now"
    }

    @Test
    void normalizeTrimsLabelAndKeepsExplicitValue() {
        List<Choice> result = QuestionChoices.normalize([new Choice(value: " r ", label: "  Red  ")])

        assert result[0].value == "r"
        assert result[0].label == "Red"
    }

    @Test
    void normalizeStripsPunctuationAndEdgeSeparators() {
        assert QuestionChoices.normalize([new Choice(label: "Yes, absolutely!")])[0].value == "yes-absolutely"
    }

    @Test
    void normalizeFallsBackWhenLabelHasNoUsableCharacters() {
        assert QuestionChoices.normalize([new Choice(label: "***")])[0].value == "choice"
    }

    @Test
    void normalizeSuffixesDuplicateDerivedValues() {
        List<Choice> result = QuestionChoices.normalize([
                new Choice(label: "Yes"), new Choice(label: "Yes"), new Choice(label: "Yes")
        ])

        assert result.collect { it.value } == ["yes", "yes-2", "yes-3"]
    }

    @Test
    void validateForCreateAllowsAQuestionWithoutChoices() {
        QuestionChoices.validateForCreate(new Question(text: "free form"))
    }

    @Test
    void validateForCreateRejectsASingleChoice() {
        Question question = new Question(text: "pick", choices: [new Choice(value: "red", label: "Red")])

        HttpStatusException e = catchBadRequest { QuestionChoices.validateForCreate(question) }

        assert e.message.contains("at least 2 choices")
    }

    @Test
    void validateForCreateRejectsABlankLabel() {
        Question question = new Question(text: "pick", choices: [
                new Choice(value: "red", label: "Red"), new Choice(value: "blank", label: null)
        ])

        assert catchBadRequest { QuestionChoices.validateForCreate(question) }.message.contains("label")
    }

    @Test
    void validateForCreateRejectsDuplicateValues() {
        Question question = new Question(text: "pick", choices: [
                new Choice(value: "red", label: "Red"), new Choice(value: "red", label: "Rouge")
        ])

        assert catchBadRequest { QuestionChoices.validateForCreate(question) }.message.contains("unique")
    }

    @Test
    void validateForCreateRejectsChatGptOnAChoiceQuestion() {
        Question question = choiceQuestion()
        question.askChatGpt = true

        assert catchBadRequest { QuestionChoices.validateForCreate(question) }.message.contains("Chat GPT")
    }

    @Test
    void validateSelectionAllowsAFreeFormAnswerOnAFreeFormQuestion() {
        QuestionChoices.validateSelection(new Question(text: "why"), new Answer(text: "because"))
    }

    @Test
    void validateSelectionRejectsChoicesOnAFreeFormQuestion() {
        Question question = new Question(text: "why")
        Answer answer = new Answer(selectedChoices: ["red"])

        assert catchBadRequest { QuestionChoices.validateSelection(question, answer) }.message.contains("does not offer choices")
    }

    @Test
    void validateSelectionRejectsAnEmptySelection() {
        Answer answer = new Answer(text: "no opinion")

        assert catchBadRequest { QuestionChoices.validateSelection(choiceQuestion(), answer) }.message.contains("Select an option")
    }

    @Test
    void validateSelectionAcceptsOneChoiceOnASingleSelectQuestion() {
        QuestionChoices.validateSelection(choiceQuestion(), new Answer(selectedChoices: ["red"]))
    }

    @Test
    void validateSelectionRejectsTwoChoicesOnASingleSelectQuestion() {
        Answer answer = new Answer(selectedChoices: ["red", "green"])

        assert catchBadRequest { QuestionChoices.validateSelection(choiceQuestion(), answer) }.message.contains("Only one option")
    }

    @Test
    void validateSelectionAcceptsTwoChoicesWhenMultipleAnswersAreAllowed() {
        QuestionChoices.validateSelection(choiceQuestion(true), new Answer(selectedChoices: ["red", "green"]))
    }

    @Test
    void validateSelectionRejectsAnUnknownChoice() {
        Answer answer = new Answer(selectedChoices: ["purple"])

        assert catchBadRequest { QuestionChoices.validateSelection(choiceQuestion(), answer) }.message.contains("purple")
    }

    @Test
    void describeJoinsTheMatchingLabels() {
        assert QuestionChoices.describe(choiceQuestion(true), ["red", "green"]) == "Red, Green"
    }

    @Test
    void describeFallsBackToTheRawValueWhenNoChoiceMatches() {
        assert QuestionChoices.describe(choiceQuestion(), ["purple"]) == "purple"
    }

    @Test
    void describeReturnsEmptyStringForNoSelection() {
        assert QuestionChoices.describe(choiceQuestion(), null) == ""
    }

    private static HttpStatusException catchBadRequest(Closure closure) {
        try {
            closure.call()
        } catch (HttpStatusException e) {
            assert e.status == HttpStatus.BAD_REQUEST
            return e
        }
        throw new AssertionError("Expected a BAD_REQUEST HttpStatusException")
    }
}
