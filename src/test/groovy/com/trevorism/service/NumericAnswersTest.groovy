package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.AnswerType
import com.trevorism.model.Choice
import com.trevorism.model.Question
import com.trevorism.model.QuestionKind
import com.trevorism.model.UiAnswer
import com.trevorism.model.UiQuestion
import com.trevorism.model.User
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class NumericAnswersTest {

    InMemoryRepository<Answer> answerRepo
    InMemoryRepository<Question> questionRepo
    AnswerQuestionService service
    List<Answer> publishedAnswers

    @BeforeEach
    void setup() {
        answerRepo = new InMemoryRepository<Answer>()
        questionRepo = new InMemoryRepository<Question>()
        publishedAnswers = []
        EventPublishingService pub = [
                publishQuestionAsked   : { Question q -> },
                publishQuestionAnswered: { Question q, Answer a, String u -> publishedAnswers << a },
                publishQuestionOverdue : { Question q -> }
        ] as EventPublishingService
        service = new AnswerQuestionService(answerRepo, questionRepo, new InMemoryRepository<User>(), pub)
    }

    private Question numberQuestion(Map properties = [:]) {
        questionRepo.create(new Question([text: "Body weight", createDate: new Date(), answerType: AnswerType.NUMBER] + properties))
    }

    private static void assertBadRequest(String message, Closure action) {
        HttpStatusException error = assertThrows(HttpStatusException) { action() }
        assert error.status == HttpStatus.BAD_REQUEST
        assert error.message == message
    }

    @Test
    void aNumberAnswerStoresItsValueAndNormalizedTextWithTheUnit() {
        Question question = numberQuestion(unit: "lb")

        UiAnswer result = service.answerQuestion(question.id, new Answer(text: " 182.50 "), "me")

        assert result.text == "182.5 lb"
        assert answerRepo.list()[0].value == 182.5d
        assert publishedAnswers[0].value == 182.5d
    }

    @Test
    void wholeNumbersAreWrittenWithoutADecimalPoint() {
        Question question = numberQuestion()

        assert service.answerQuestion(question.id, new Answer(text: "-3"), "me").text == "-3"
        assert answerRepo.list()[0].value == -3d
    }

    @Test
    void anythingButANumberIsRejected() {
        Question question = numberQuestion()

        assertBadRequest("Enter a number") { service.answerQuestion(question.id, new Answer(text: "about 180"), "me") }
        assertBadRequest("Enter a number") { service.answerQuestion(question.id, new Answer(text: ""), "me") }
        assertBadRequest("Enter a number") { service.answerQuestion(question.id, new Answer(text: "1e3"), "me") }
        assert answerRepo.list().isEmpty()
    }

    @Test
    void theRangeIsEnforced() {
        Question question = numberQuestion(minValue: 0d, maxValue: 10d)

        assertBadRequest("Enter a number of at least 0") { service.answerQuestion(question.id, new Answer(text: "-1"), "me") }
        assertBadRequest("Enter a number of at most 10") { service.answerQuestion(question.id, new Answer(text: "10.5"), "me") }
        assert service.answerQuestion(question.id, new Answer(text: "10"), "me").text == "10"
    }

    @Test
    void textQuestionsAreUnchangedAndCarryNoValue() {
        Question question = questionRepo.create(new Question(text: "Why?", createDate: new Date()))

        assert service.answerQuestion(question.id, new Answer(text: "because", value: 4d), "me").text == "because"
        assert answerRepo.list()[0].value == null
    }

    @Test
    void theAnswerTypeAndRangeAreShownToTheAnswerer() {
        Question question = numberQuestion(unit: "lb", minValue: 100d, maxValue: 300d)

        UiQuestion shown = service.getQuestion(question.id, "asker", [])

        assert [shown.answerType, shown.unit, shown.minValue, shown.maxValue] == [AnswerType.NUMBER, "lb", 100d, 300d]
    }

    @Test
    void creatingANumberQuestionValidatesItsSettings() {
        NumericAnswers.validateForCreate(new Question(answerType: AnswerType.NUMBER, unit: "lb", minValue: 1d, maxValue: 2d))
        NumericAnswers.validateForCreate(new Question())
        NumericAnswers.validateForCreate(new Question(answerType: AnswerType.TEXT))

        assertBadRequest("answerType must be one of [text, number]") { NumericAnswers.validateForCreate(new Question(answerType: "date")) }
        assertBadRequest("unit, minValue and maxValue apply only to number questions") { NumericAnswers.validateForCreate(new Question(unit: "lb")) }
        assertBadRequest("A number question cannot offer choices") {
            NumericAnswers.validateForCreate(new Question(answerType: AnswerType.NUMBER, choices: [new Choice(value: "a", label: "A")]))
        }
        assertBadRequest("An approval cannot expect a number") { NumericAnswers.validateForCreate(new Question(answerType: AnswerType.NUMBER, kind: QuestionKind.APPROVAL)) }
        assertBadRequest("Chat GPT cannot answer a number question") { NumericAnswers.validateForCreate(new Question(answerType: AnswerType.NUMBER, askChatGpt: true)) }
        assertBadRequest("minValue must not be greater than maxValue") { NumericAnswers.validateForCreate(new Question(answerType: AnswerType.NUMBER, minValue: 5d, maxValue: 1d)) }
    }
}
