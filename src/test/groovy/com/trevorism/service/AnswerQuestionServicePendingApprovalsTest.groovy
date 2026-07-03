package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnswerQuestionServicePendingApprovalsTest {

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
    void getPendingApprovalsReturnsOnlyApprovalKind() {
        questionRepo.create(new Question(text: "plain", kind: "question", targetIdentityId: "me", answered: false, createDate: new Date()))
        questionRepo.create(new Question(text: "approve me", kind: "approval", targetIdentityId: "me", answered: false, createDate: new Date()))

        List<Question> result = service.getPendingApprovals("me")

        assert result.size() == 1
        assert result[0].kind == "approval"
        assert result[0].text == "approve me"
    }

    @Test
    void getPendingApprovalsIsEmptyWhenNoApprovals() {
        questionRepo.create(new Question(text: "plain", kind: "question", targetIdentityId: "me", answered: false, createDate: new Date()))
        assert service.getPendingApprovals("me").isEmpty()
    }
}
