package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.secure.Roles
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultQuestionServiceTest {

    List<Question> publishedAsked
    List<Question> publishedOverdue
    List<Map> aiAnswers
    List<Question> scheduled
    InMemoryRepository<Question> repo
    DefaultQuestionService service

    @BeforeEach
    void setup() {
        publishedAsked = []
        publishedOverdue = []
        aiAnswers = []
        scheduled = []
        repo = new InMemoryRepository<Question>()

        EventPublishingService pub = [
                publishQuestionAsked   : { Question q -> publishedAsked << q },
                publishQuestionAnswered: { Question q, Answer a, String u -> },
                publishQuestionOverdue : { Question q -> publishedOverdue << q }
        ] as EventPublishingService

        AnswerService ans = [
                answerQuestion: { String qid, Answer a, String id -> aiAnswers << [questionId: qid, text: a.text, identityId: id]; null }
        ] as AnswerService

        ChatService chat = [ask: { String t -> "AI answer" }] as ChatService
        DueDateScheduler sched = [scheduleDueDateCallback: { Question q -> scheduled << q }] as DueDateScheduler

        service = new DefaultQuestionService(repo, pub, ans, chat, sched)
    }

    @Test
    void createSetsFieldsPersistsAndPublishesAsked() {
        Question created = service.create(new Question(text: "hello"), "asker")

        assert created.id
        assert created.identityId == "asker"
        assert !created.answered
        assert created.createDate != null
        assert repo.get(created.id) != null
        assert publishedAsked.size() == 1
        assert publishedAsked[0].id == created.id
    }

    @Test
    void createRejectsEmptyText() {
        try {
            service.create(new Question(text: ""), "asker")
            assert false
        } catch (HttpStatusException e) {
            assert e.status == HttpStatus.BAD_REQUEST
        }
    }

    @Test
    void createWithChatGptCreatesAiAnswer() {
        service.create(new Question(text: "deploy?", askChatGpt: true), "asker")

        assert aiAnswers.size() == 1
        assert aiAnswers[0].text == "AI answer"
        assert aiAnswers[0].identityId == DefaultQuestionService.CHAT_GPT_IDENTITY
    }

    @Test
    void createWithoutChatGptDoesNotCallChat() {
        service.create(new Question(text: "just asking", askChatGpt: false), "asker")
        assert aiAnswers.isEmpty()
    }

    @Test
    void createWithFutureDueDateSchedulesCallback() {
        service.create(new Question(text: "x", dueDate: new Date(System.currentTimeMillis() + 3_600_000)), "asker")
        assert scheduled.size() == 1
    }

    @Test
    void createWithPastDueDateDoesNotSchedule() {
        service.create(new Question(text: "x", dueDate: new Date(System.currentTimeMillis() - 3_600_000)), "asker")
        assert scheduled.isEmpty()
    }

    @Test
    void getReturnsQuestionForParty() {
        Question stored = repo.create(new Question(text: "secret", identityId: "asker", targetIdentityId: "target", privateQuestion: true))
        assert service.get(stored.id, "asker", [Roles.USER])
        assert service.get(stored.id, "target", [Roles.USER])
    }

    @Test
    void getHidesPrivateQuestionFromStranger() {
        Question stored = repo.create(new Question(text: "secret", identityId: "asker", targetIdentityId: "target", privateQuestion: true))
        try {
            service.get(stored.id, "stranger", [Roles.USER])
            assert false
        } catch (HttpStatusException e) {
            assert e.status == HttpStatus.NOT_FOUND
        }
    }

    @Test
    void listVisibleFiltersPrivateQuestions() {
        repo.create(new Question(text: "pub", privateQuestion: false))
        repo.create(new Question(text: "priv", identityId: "asker", privateQuestion: true))

        assert service.listVisible("stranger", [Roles.USER]).size() == 1
        assert service.listVisible("asker", [Roles.USER]).size() == 2
    }

    @Test
    void updateByOwnerSucceeds() {
        Question stored = repo.create(new Question(text: "x", identityId: "asker"))
        Question updated = service.update(stored.id, new Question(text: "y"), "asker", [Roles.USER])
        assert updated.text == "y"
    }

    @Test
    void updateByNonOwnerIsForbidden() {
        Question stored = repo.create(new Question(text: "x", identityId: "asker", privateQuestion: false))
        try {
            service.update(stored.id, new Question(text: "z"), "stranger", [Roles.USER])
            assert false
        } catch (HttpStatusException e) {
            assert e.status == HttpStatus.FORBIDDEN
        }
    }

    @Test
    void deleteByOwnerSucceeds() {
        Question stored = repo.create(new Question(text: "x", identityId: "asker"))
        service.delete(stored.id, "asker", [Roles.USER])
        assert repo.get(stored.id) == null
    }

    @Test
    void markOverdueFiresForUnansweredAndSetsFlag() {
        Question stored = repo.create(new Question(text: "x", answered: false, overdueNotified: false))
        service.markOverdueIfUnanswered(stored.id)
        assert publishedOverdue.size() == 1
        assert repo.get(stored.id).overdueNotified
    }

    @Test
    void markOverdueSkipsAnsweredQuestion() {
        Question stored = repo.create(new Question(text: "x", answered: true))
        service.markOverdueIfUnanswered(stored.id)
        assert publishedOverdue.isEmpty()
    }

    @Test
    void markOverdueSkipsAlreadyNotifiedQuestion() {
        Question stored = repo.create(new Question(text: "x", answered: false, overdueNotified: true))
        service.markOverdueIfUnanswered(stored.id)
        assert publishedOverdue.isEmpty()
    }
}
