package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.EventClient
import com.trevorism.event.model.EventSubscription
import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.QuestionAnsweredEvent
import com.trevorism.model.QuestionAskedEvent
import org.junit.jupiter.api.Test

class DefaultEventPublishingServiceTest {

    @Test
    void publishQuestionAskedSendsMappedPayloadToAskedTopic() {
        RecordingEventClient<QuestionAskedEvent> askedClient = new RecordingEventClient<>()
        RecordingEventClient<QuestionAnsweredEvent> answeredClient = new RecordingEventClient<>()
        DefaultEventPublishingService service = new DefaultEventPublishingService(askedClient, answeredClient, new FakeChannelClient([]))

        Date created = new Date()
        Question question = new Question(id: "q1", text: "Deploy?", identityId: "asker1",
                targetIdentityId: "target1", privateQuestion: true, askChatGpt: false, createDate: created)

        service.publishQuestionAsked(question)

        assert answeredClient.topic == null
        assert askedClient.topic == DefaultEventPublishingService.QUESTION_ASKED_TOPIC
        QuestionAskedEvent payload = askedClient.payload
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
        RecordingEventClient<QuestionAskedEvent> askedClient = new RecordingEventClient<>()
        RecordingEventClient<QuestionAnsweredEvent> answeredClient = new RecordingEventClient<>()
        DefaultEventPublishingService service = new DefaultEventPublishingService(askedClient, answeredClient, new FakeChannelClient([]))

        Date answered = new Date()
        Question question = new Question(id: "q1", text: "Deploy?", identityId: "asker1")
        Answer answer = new Answer(id: "a1", questionId: "q1", text: "Approved", identityId: "answerer1", answeredDate: answered)

        service.publishQuestionAnswered(question, answer, "alice")

        assert askedClient.topic == null
        assert answeredClient.topic == DefaultEventPublishingService.QUESTION_ANSWERED_TOPIC
        QuestionAnsweredEvent payload = answeredClient.payload
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
    void publishSwallowsEventClientFailure() {
        EventClient throwing = new EventClient() {
            @Override
            String sendEvent(String topic, Object event) {
                throw new RuntimeException("bus down")
            }
        }
        DefaultEventPublishingService service = new DefaultEventPublishingService(throwing, throwing, new FakeChannelClient([]))

        // Neither call should propagate the exception
        service.publishQuestionAsked(new Question(id: "q1", text: "x"))
        service.publishQuestionAnswered(new Question(id: "q1"), new Answer(id: "a1"), "alice")
    }

    @Test
    void publishEnsuresTopicsExactlyOnceAcrossMultiplePublishes() {
        FakeChannelClient channelClient = new FakeChannelClient([])
        DefaultEventPublishingService service = new DefaultEventPublishingService(new RecordingEventClient<>(), new RecordingEventClient<>(), channelClient)

        service.publishQuestionAsked(new Question(id: "q1", text: "x"))
        service.publishQuestionAsked(new Question(id: "q2", text: "y"))
        service.publishQuestionAnswered(new Question(id: "q1"), new Answer(id: "a1"), "alice")

        // Topics created once total, not per publish
        assert channelClient.listTopicsCallCount == 1
        assert channelClient.createdTopics == [DefaultEventPublishingService.QUESTION_ASKED_TOPIC, DefaultEventPublishingService.QUESTION_ANSWERED_TOPIC]
    }

    @Test
    void ensureTopicsCreatesOnlyMissingTopics() {
        FakeChannelClient channelClient = new FakeChannelClient([DefaultEventPublishingService.QUESTION_ASKED_TOPIC])
        DefaultEventPublishingService service = new DefaultEventPublishingService(new RecordingEventClient<>(), new RecordingEventClient<>(), channelClient)

        service.ensureTopics()

        assert channelClient.createdTopics == [DefaultEventPublishingService.QUESTION_ANSWERED_TOPIC]
    }

    @Test
    void ensureTopicsSwallowsChannelClientFailure() {
        ChannelClient throwing = new FakeChannelClient([]) {
            @Override
            List<String> listTopics() {
                throw new RuntimeException("channel down")
            }
        }
        DefaultEventPublishingService service = new DefaultEventPublishingService(new RecordingEventClient<>(), new RecordingEventClient<>(), throwing)

        // Should not propagate
        service.ensureTopics()
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
