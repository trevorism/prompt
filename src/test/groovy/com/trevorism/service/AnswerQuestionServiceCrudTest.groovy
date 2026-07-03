package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.User
import com.trevorism.secure.Roles
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnswerQuestionServiceCrudTest {

    InMemoryRepository<Answer> answerRepo
    InMemoryRepository<Question> questionRepo
    InMemoryRepository<User> userRepo
    AnswerQuestionService service

    @BeforeEach
    void setup() {
        answerRepo = new InMemoryRepository<Answer>()
        questionRepo = new InMemoryRepository<Question>()
        userRepo = new InMemoryRepository<User>()
        EventPublishingService pub = [
                publishQuestionAsked   : { Question q -> },
                publishQuestionAnswered: { Question q, Answer a, String u -> },
                publishQuestionOverdue : { Question q -> }
        ] as EventPublishingService
        service = new AnswerQuestionService(answerRepo, questionRepo, userRepo, pub)
    }

    @Test
    void answerToPublicQuestionIsViewableByAnyone() {
        Question q = questionRepo.create(new Question(privateQuestion: false))
        Answer a = answerRepo.create(new Answer(questionId: q.id, identityId: "answerer"))
        assert service.getAnswer(a.id, "stranger", [Roles.USER])
    }

    @Test
    void answerToPrivateQuestionIsHiddenFromStrangerButVisibleToPartyAndAuthor() {
        Question q = questionRepo.create(new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true))
        Answer a = answerRepo.create(new Answer(questionId: q.id, identityId: "answerer"))

        assert service.getAnswer(a.id, "asker", [Roles.USER])
        assert service.getAnswer(a.id, "answerer", [Roles.USER])
        try {
            service.getAnswer(a.id, "stranger", [Roles.USER])
            assert false
        } catch (HttpStatusException e) {
            assert e.status == HttpStatus.NOT_FOUND
        }
    }

    @Test
    void listVisibleAnswersFiltersByQuestionVisibility() {
        Question pub = questionRepo.create(new Question(privateQuestion: false))
        Question priv = questionRepo.create(new Question(identityId: "asker", privateQuestion: true))
        answerRepo.create(new Answer(questionId: pub.id, identityId: "a1"))
        answerRepo.create(new Answer(questionId: priv.id, identityId: "a2"))

        assert service.listVisibleAnswers("stranger", [Roles.USER]).size() == 1
        assert service.listVisibleAnswers("asker", [Roles.USER]).size() == 2
    }

    @Test
    void updateAnswerByAuthorSucceeds() {
        Question q = questionRepo.create(new Question(privateQuestion: false))
        Answer a = answerRepo.create(new Answer(questionId: q.id, identityId: "answerer", text: "x"))
        Answer updated = service.updateAnswer(a.id, new Answer(text: "y"), "answerer", [Roles.USER])
        assert updated.text == "y"
    }

    @Test
    void updateAnswerByNonAuthorIsForbidden() {
        Question q = questionRepo.create(new Question(privateQuestion: false))
        Answer a = answerRepo.create(new Answer(questionId: q.id, identityId: "answerer", text: "x"))
        try {
            service.updateAnswer(a.id, new Answer(text: "z"), "stranger", [Roles.USER])
            assert false
        } catch (HttpStatusException e) {
            assert e.status == HttpStatus.FORBIDDEN
        }
    }

    @Test
    void deleteAnswerByAuthorSucceeds() {
        Question q = questionRepo.create(new Question(privateQuestion: false))
        Answer a = answerRepo.create(new Answer(questionId: q.id, identityId: "answerer"))
        service.deleteAnswer(a.id, "answerer", [Roles.USER])
        assert answerRepo.get(a.id) == null
    }
}
