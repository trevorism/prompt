package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Choice
import com.trevorism.model.Question
import com.trevorism.model.UiAnswer
import com.trevorism.model.UiQuestion
import com.trevorism.model.User
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnswerQuestionServiceChoiceTest {

    InMemoryRepository<Answer> answerRepo
    InMemoryRepository<Question> questionRepo
    InMemoryRepository<User> userRepo
    AnswerQuestionService service
    List<Answer> publishedAnswers

    @BeforeEach
    void setup() {
        answerRepo = new InMemoryRepository<Answer>()
        questionRepo = new InMemoryRepository<Question>()
        userRepo = new InMemoryRepository<User>()
        publishedAnswers = []
        EventPublishingService pub = [
                publishQuestionAsked   : { Question q -> },
                publishQuestionAnswered: { Question q, Answer a, String u -> publishedAnswers << a },
                publishQuestionOverdue : { Question q -> }
        ] as EventPublishingService
        service = new AnswerQuestionService(answerRepo, questionRepo, userRepo, pub)
    }

    private Question createChoiceQuestion(boolean allowMultipleAnswers = false) {
        questionRepo.create(new Question(text: "pick a color", createDate: new Date(),
                allowMultipleAnswers: allowMultipleAnswers, choices: [
                new Choice(value: "red", label: "Red"),
                new Choice(value: "green", label: "Green")
        ]))
    }

    @Test
    void answerWithASelectionDerivesTheTextFromTheLabel() {
        Question question = createChoiceQuestion()

        UiAnswer result = service.answerQuestion(question.id, new Answer(selectedChoices: ["green"]), "me")

        assert result.text == "Green"
        assert result.selectedChoices == ["green"]
    }

    @Test
    void answerWithMultipleSelectionsJoinsTheLabels() {
        Question question = createChoiceQuestion(true)

        UiAnswer result = service.answerQuestion(question.id, new Answer(selectedChoices: ["red", "green"]), "me")

        assert result.text == "Red, Green"
        assert result.selectedChoices == ["red", "green"]
    }

    @Test
    void answerKeepsAnExplicitCommentInsteadOfTheDerivedLabel() {
        Question question = createChoiceQuestion()

        UiAnswer result = service.answerQuestion(question.id, new Answer(text: "Red, because it is faster", selectedChoices: ["red"]), "me")

        assert result.text == "Red, because it is faster"
        assert result.selectedChoices == ["red"]
    }

    @Test
    void answerMarksTheQuestionAnsweredAndPublishesTheSelection() {
        Question question = createChoiceQuestion()

        service.answerQuestion(question.id, new Answer(selectedChoices: ["red"]), "me")

        assert questionRepo.get(question.id).answered
        assert publishedAnswers.size() == 1
        assert publishedAnswers[0].selectedChoices == ["red"]
    }

    @Test
    void answerRejectsAnUnknownChoice() {
        Question question = createChoiceQuestion()

        HttpStatusException e = null
        try {
            service.answerQuestion(question.id, new Answer(selectedChoices: ["purple"]), "me")
        } catch (HttpStatusException ex) {
            e = ex
        }

        assert e?.status == HttpStatus.BAD_REQUEST
        assert answerRepo.list().isEmpty()
        assert !questionRepo.get(question.id).answered
    }

    @Test
    void answerRejectsASecondSelectionOnASingleSelectQuestion() {
        Question question = createChoiceQuestion()

        HttpStatusException e = null
        try {
            service.answerQuestion(question.id, new Answer(selectedChoices: ["red", "green"]), "me")
        } catch (HttpStatusException ex) {
            e = ex
        }

        assert e?.status == HttpStatus.BAD_REQUEST
    }

    @Test
    void answerRejectsAMissingQuestion() {
        HttpStatusException e = null
        try {
            service.answerQuestion("does-not-exist", new Answer(text: "hello"), "me")
        } catch (HttpStatusException ex) {
            e = ex
        }

        assert e?.status == HttpStatus.NOT_FOUND
    }

    @Test
    void freeFormAnswersAreStillAcceptedOnQuestionsWithoutChoices() {
        Question question = questionRepo.create(new Question(text: "why", createDate: new Date()))

        UiAnswer result = service.answerQuestion(question.id, new Answer(text: "because"), "me")

        assert result.text == "because"
        assert result.selectedChoices == []
    }

    @Test
    void getQuestionExposesTheChoicesToTheUi() {
        Question question = createChoiceQuestion(true)

        UiQuestion result = service.getQuestion(question.id, "me", [])

        assert result.choices.collect { it.label } == ["Red", "Green"]
        assert result.allowMultipleAnswers
    }

    @Test
    void questionListItemsCarryTheChoicesAndTheSelection() {
        Question question = createChoiceQuestion()
        service.answerQuestion(question.id, new Answer(selectedChoices: ["green"]), "me")

        def items = service.getAllQuestions()

        assert items.size() == 1
        assert items[0].question.choices.collect { it.value } == ["red", "green"]
        assert items[0].answers[0].selectedChoices == ["green"]
    }
}
