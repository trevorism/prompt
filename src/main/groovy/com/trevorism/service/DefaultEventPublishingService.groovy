package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.EventClient
import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.QuestionAnsweredEvent
import com.trevorism.model.QuestionAskedEvent
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class DefaultEventPublishingService implements EventPublishingService {

    static final String QUESTION_ASKED_TOPIC = "questionAsked"
    static final String QUESTION_ANSWERED_TOPIC = "questionAnswered"

    private static final Logger log = LoggerFactory.getLogger(DefaultEventPublishingService.class.name)

    private final EventClient<QuestionAskedEvent> questionAskedEventClient
    private final EventClient<QuestionAnsweredEvent> questionAnsweredEventClient
    private final ChannelClient channelClient
    private volatile boolean topicsEnsured = false

    DefaultEventPublishingService(
            @Named("questionAsked") EventClient<QuestionAskedEvent> questionAskedEventClient,
            @Named("questionAnswered") EventClient<QuestionAnsweredEvent> questionAnsweredEventClient,
            ChannelClient channelClient) {
        this.questionAskedEventClient = questionAskedEventClient
        this.questionAnsweredEventClient = questionAnsweredEventClient
        this.channelClient = channelClient
    }

    @Override
    void publishQuestionAsked(Question question) {
        ensureTopicsOnce()
        QuestionAskedEvent event = new QuestionAskedEvent(questionId: question.id, text: question.text,
                askerIdentityId: question.identityId, targetIdentityId: question.targetIdentityId,
                privateQuestion: question.privateQuestion, askChatGpt: question.askChatGpt,
                createDate: question.createDate)
        publish(questionAskedEventClient, QUESTION_ASKED_TOPIC, event)
    }

    @Override
    void publishQuestionAnswered(Question question, Answer answer, String answererUsername) {
        ensureTopicsOnce()
        QuestionAnsweredEvent event = new QuestionAnsweredEvent(questionId: question.id, questionText: question.text,
                askerIdentityId: question.identityId, answerId: answer.id, answerText: answer.text,
                answererIdentityId: answer.identityId, answererUsername: answererUsername,
                answeredDate: answer.answeredDate)
        publish(questionAnsweredEventClient, QUESTION_ANSWERED_TOPIC, event)
    }

    void ensureTopics() {
        try {
            List<String> topics = channelClient.listTopics()
            [QUESTION_ASKED_TOPIC, QUESTION_ANSWERED_TOPIC].each { String topic ->
                if (!topics.contains(topic)) {
                    channelClient.createTopic(topic)
                }
            }
        } catch (Exception e) {
            log.warn("Unable to ensure event topics exist", e)
        }
    }

    private void ensureTopicsOnce() {
        if (!topicsEnsured) {
            synchronized (this) {
                if (!topicsEnsured) {
                    ensureTopics()
                    topicsEnsured = true
                }
            }
        }
    }

    private static <T> void publish(EventClient<T> client, String topic, T event) {
        try {
            client.sendEvent(topic, event)
        } catch (Exception e) {
            log.warn("Unable to publish event to topic ${topic}", e)
        }
    }
}
