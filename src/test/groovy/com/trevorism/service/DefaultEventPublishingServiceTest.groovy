package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.EventClient
import com.trevorism.event.model.EventSubscription
import com.trevorism.model.Answer
import com.trevorism.model.ApprovalDecidedEvent
import com.trevorism.model.ApprovalRequestedEvent
import com.trevorism.model.Question
import com.trevorism.model.QuestionAnsweredEvent
import com.trevorism.model.QuestionAskedEvent
import org.junit.jupiter.api.Test

class DefaultEventPublishingServiceTest {

    @Test
    void publishQuestionAskedSendsMappedPayloadToAskedTopic() {
        Fixture f = new Fixture()

        Date created = new Date()
        Question question = new Question(id: "q1", text: "Deploy?", identityId: "asker1",
                targetIdentityId: "target1", privateQuestion: true, askChatGpt: false, createDate: created)

        f.service.publishQuestionAsked(question)

        assert f.approvalRequested.topic == null
        assert f.asked.topic == DefaultEventPublishingService.QUESTION_ASKED_TOPIC
        QuestionAskedEvent payload = f.asked.payload
        assert payload.questionId == "q1"
        assert payload.text == "Deploy?"
        assert payload.askerIdentityId == "asker1"
        assert payload.targetIdentityId == "target1"
        assert payload.privateQuestion
        assert !payload.askChatGpt
        assert payload.createDate == created
    }

    @Test
    void publishQuestionAnsweredSendsMappedPayloadToAnsweredTopic() {
        Fixture f = new Fixture()

        Date answered = new Date()
        Question question = new Question(id: "q1", text: "Deploy?", identityId: "asker1")
        Answer answer = new Answer(id: "a1", questionId: "q1", text: "Approved", identityId: "answerer1", answeredDate: answered)

        f.service.publishQuestionAnswered(question, answer, "alice")

        assert f.approvalDecided.topic == null
        assert f.answered.topic == DefaultEventPublishingService.QUESTION_ANSWERED_TOPIC
        QuestionAnsweredEvent payload = f.answered.payload
        assert payload.questionId == "q1"
        assert payload.questionText == "Deploy?"
        assert payload.askerIdentityId == "asker1"
        assert payload.answerId == "a1"
        assert payload.answerText == "Approved"
        assert payload.answererIdentityId == "answerer1"
        assert payload.answererUsername == "alice"
        assert payload.answeredDate == answered
    }

    @Test
    void publishQuestionAskedForApprovalRoutesToApprovalRequestedTopic() {
        Fixture f = new Fixture()

        Date created = new Date()
        Question question = new Question(id: "q1", text: "Deploy prod?", identityId: "requester1",
                targetIdentityId: "approver1", kind: "approval", createDate: created)

        f.service.publishQuestionAsked(question)

        assert f.asked.topic == null
        assert f.approvalRequested.topic == DefaultEventPublishingService.APPROVAL_REQUESTED_TOPIC
        ApprovalRequestedEvent payload = f.approvalRequested.payload
        assert payload.questionId == "q1"
        assert payload.text == "Deploy prod?"
        assert payload.requesterIdentityId == "requester1"
        assert payload.approverIdentityId == "approver1"
        assert payload.createDate == created
    }

    @Test
    void publishQuestionAnsweredForApprovalRoutesToApprovalDecidedTopic() {
        Fixture f = new Fixture()

        Date decided = new Date()
        Question question = new Question(id: "q1", text: "Deploy prod?", identityId: "requester1",
                targetIdentityId: "approver1", kind: "approval")
        Answer answer = new Answer(id: "a1", questionId: "q1", text: "LGTM", identityId: "approver1",
                approved: true, answeredDate: decided)

        f.service.publishQuestionAnswered(question, answer, "alice")

        assert f.answered.topic == null
        assert f.approvalDecided.topic == DefaultEventPublishingService.APPROVAL_DECIDED_TOPIC
        ApprovalDecidedEvent payload = f.approvalDecided.payload
        assert payload.questionId == "q1"
        assert payload.questionText == "Deploy prod?"
        assert payload.requesterIdentityId == "requester1"
        assert payload.decisionAnswerId == "a1"
        assert payload.approved
        assert payload.reason == "LGTM"
        assert payload.approverIdentityId == "approver1"
        assert payload.approverUsername == "alice"
        assert payload.decidedDate == decided
    }

    @Test
    void publishSwallowsEventClientFailure() {
        EventClient throwing = new EventClient() {
            @Override
            String sendEvent(String topic, Object event) {
                throw new RuntimeException("bus down")
            }
        }
        DefaultEventPublishingService service = new DefaultEventPublishingService(
                throwing, throwing, throwing, throwing, new FakeChannelClient([]))

        // None should propagate the exception
        service.publishQuestionAsked(new Question(id: "q1", text: "x"))
        service.publishQuestionAnswered(new Question(id: "q1"), new Answer(id: "a1"), "alice")
        service.publishQuestionAsked(new Question(id: "q2", text: "y", kind: "approval"))
        service.publishQuestionAnswered(new Question(id: "q2", kind: "approval"), new Answer(id: "a2", approved: false), "bob")
    }

    @Test
    void publishEnsuresTopicsExactlyOnceAcrossMultiplePublishes() {
        Fixture f = new Fixture()

        f.service.publishQuestionAsked(new Question(id: "q1", text: "x"))
        f.service.publishQuestionAsked(new Question(id: "q2", text: "y"))
        f.service.publishQuestionAnswered(new Question(id: "q1"), new Answer(id: "a1"), "alice")

        // Topics created once total, not per publish
        assert f.channel.listTopicsCallCount == 1
        assert f.channel.createdTopics == [
                DefaultEventPublishingService.QUESTION_ASKED_TOPIC,
                DefaultEventPublishingService.QUESTION_ANSWERED_TOPIC,
                DefaultEventPublishingService.APPROVAL_REQUESTED_TOPIC,
                DefaultEventPublishingService.APPROVAL_DECIDED_TOPIC]
    }

    @Test
    void ensureTopicsCreatesOnlyMissingTopics() {
        Fixture f = new Fixture([DefaultEventPublishingService.QUESTION_ASKED_TOPIC, DefaultEventPublishingService.APPROVAL_DECIDED_TOPIC])

        f.service.ensureTopics()

        assert f.channel.createdTopics == [
                DefaultEventPublishingService.QUESTION_ANSWERED_TOPIC,
                DefaultEventPublishingService.APPROVAL_REQUESTED_TOPIC]
    }

    @Test
    void ensureTopicsSwallowsChannelClientFailure() {
        ChannelClient throwing = new FakeChannelClient([]) {
            @Override
            List<String> listTopics() {
                throw new RuntimeException("channel down")
            }
        }
        DefaultEventPublishingService service = new DefaultEventPublishingService(
                new RecordingEventClient<>(), new RecordingEventClient<>(),
                new RecordingEventClient<>(), new RecordingEventClient<>(), throwing)

        // Should not propagate
        service.ensureTopics()
    }

    private static class Fixture {
        RecordingEventClient<QuestionAskedEvent> asked = new RecordingEventClient<>()
        RecordingEventClient<QuestionAnsweredEvent> answered = new RecordingEventClient<>()
        RecordingEventClient<ApprovalRequestedEvent> approvalRequested = new RecordingEventClient<>()
        RecordingEventClient<ApprovalDecidedEvent> approvalDecided = new RecordingEventClient<>()
        FakeChannelClient channel
        DefaultEventPublishingService service

        Fixture(List<String> existingTopics = []) {
            channel = new FakeChannelClient(existingTopics)
            service = new DefaultEventPublishingService(asked, answered, approvalRequested, approvalDecided, channel)
        }
    }

    private static class RecordingEventClient<T> implements EventClient<T> {
        String topic
        T payload

        @Override
        String sendEvent(String topic, T event) {
            this.topic = topic
            this.payload = event
            return "event-id"
        }
    }

    private static class FakeChannelClient implements ChannelClient {
        List<String> existingTopics
        List<String> createdTopics = []
        int listTopicsCallCount = 0

        FakeChannelClient(List<String> existingTopics) {
            this.existingTopics = existingTopics
        }

        @Override
        List<String> listTopics() {
            listTopicsCallCount++
            existingTopics
        }

        @Override
        String createTopic(String name) {
            createdTopics << name
            return name
        }

        @Override
        String deleteTopic(String name) { name }

        @Override
        List<EventSubscription> listSubscriptions() { [] }

        @Override
        EventSubscription createSubscription(EventSubscription subscription) { subscription }

        @Override
        EventSubscription getSubscription(String name) { null }

        @Override
        EventSubscription deleteSubscription(String name) { null }
    }
}
