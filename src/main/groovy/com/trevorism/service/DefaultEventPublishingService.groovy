package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.EventClient
import com.trevorism.model.Answer
import com.trevorism.model.ApprovalDecidedEvent
import com.trevorism.model.ApprovalExpiredEvent
import com.trevorism.model.ApprovalRequestedEvent
import com.trevorism.model.Question
import com.trevorism.model.QuestionAnsweredEvent
import com.trevorism.model.QuestionAskedEvent
import com.trevorism.model.QuestionKind
import com.trevorism.model.QuestionOverdueEvent
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class DefaultEventPublishingService implements EventPublishingService {

    static final String QUESTION_ASKED_TOPIC = "questionAsked"
    static final String QUESTION_ANSWERED_TOPIC = "questionAnswered"
    static final String APPROVAL_REQUESTED_TOPIC = "approvalRequested"
    static final String APPROVAL_DECIDED_TOPIC = "approvalDecided"
    static final String QUESTION_OVERDUE_TOPIC = "questionOverdue"
    static final String APPROVAL_EXPIRED_TOPIC = "approvalExpired"

    private static final Logger log = LoggerFactory.getLogger(DefaultEventPublishingService.class.name)

    private final EventClient<QuestionAskedEvent> questionAskedEventClient
    private final EventClient<QuestionAnsweredEvent> questionAnsweredEventClient
    private final EventClient<ApprovalRequestedEvent> approvalRequestedEventClient
    private final EventClient<ApprovalDecidedEvent> approvalDecidedEventClient
    private final EventClient<QuestionOverdueEvent> questionOverdueEventClient
    private final EventClient<ApprovalExpiredEvent> approvalExpiredEventClient
    private final ChannelClient channelClient
    private volatile boolean topicsEnsured = false

    DefaultEventPublishingService(
            @Named("questionAsked") EventClient<QuestionAskedEvent> questionAskedEventClient,
            @Named("questionAnswered") EventClient<QuestionAnsweredEvent> questionAnsweredEventClient,
            @Named("approvalRequested") EventClient<ApprovalRequestedEvent> approvalRequestedEventClient,
            @Named("approvalDecided") EventClient<ApprovalDecidedEvent> approvalDecidedEventClient,
            @Named("questionOverdue") EventClient<QuestionOverdueEvent> questionOverdueEventClient,
            @Named("approvalExpired") EventClient<ApprovalExpiredEvent> approvalExpiredEventClient,
            ChannelClient channelClient) {
        this.questionAskedEventClient = questionAskedEventClient
        this.questionAnsweredEventClient = questionAnsweredEventClient
        this.approvalRequestedEventClient = approvalRequestedEventClient
        this.approvalDecidedEventClient = approvalDecidedEventClient
        this.questionOverdueEventClient = questionOverdueEventClient
        this.approvalExpiredEventClient = approvalExpiredEventClient
        this.channelClient = channelClient
    }

    @Override
    void publishQuestionAsked(Question question) {
        ensureTopicsOnce()
        if (isApproval(question)) {
            ApprovalRequestedEvent event = new ApprovalRequestedEvent(questionId: question.id, text: question.text,
                    requesterIdentityId: question.identityId, approverIdentityId: question.targetIdentityId,
                    createDate: question.createDate)
            publish(approvalRequestedEventClient, APPROVAL_REQUESTED_TOPIC, event)
            return
        }
        QuestionAskedEvent event = new QuestionAskedEvent(questionId: question.id, text: question.text,
                askerIdentityId: question.identityId, targetIdentityId: question.targetIdentityId,
                privateQuestion: question.privateQuestion, askChatGpt: question.askChatGpt,
                createDate: question.createDate)
        publish(questionAskedEventClient, QUESTION_ASKED_TOPIC, event)
    }

    @Override
    void publishQuestionAnswered(Question question, Answer answer, String answererUsername) {
        ensureTopicsOnce()
        if (isApproval(question)) {
            ApprovalDecidedEvent event = new ApprovalDecidedEvent(questionId: question.id, questionText: question.text,
                    requesterIdentityId: question.identityId, decisionAnswerId: answer.id, approved: answer.approved,
                    reason: answer.text, approverIdentityId: answer.identityId, approverUsername: answererUsername,
                    decidedDate: answer.answeredDate)
            publish(approvalDecidedEventClient, APPROVAL_DECIDED_TOPIC, event)
            return
        }
        QuestionAnsweredEvent event = new QuestionAnsweredEvent(questionId: question.id, questionText: question.text,
                askerIdentityId: question.identityId, answerId: answer.id, answerText: answer.text,
                answererIdentityId: answer.identityId, answererUsername: answererUsername,
                answeredDate: answer.answeredDate)
        publish(questionAnsweredEventClient, QUESTION_ANSWERED_TOPIC, event)
    }

    @Override
    void publishQuestionOverdue(Question question) {
        ensureTopicsOnce()
        if (isApproval(question)) {
            ApprovalExpiredEvent event = new ApprovalExpiredEvent(questionId: question.id, text: question.text,
                    requesterIdentityId: question.identityId, approverIdentityId: question.targetIdentityId,
                    dueDate: question.dueDate)
            publish(approvalExpiredEventClient, APPROVAL_EXPIRED_TOPIC, event)
            return
        }
        QuestionOverdueEvent event = new QuestionOverdueEvent(questionId: question.id, text: question.text,
                askerIdentityId: question.identityId, targetIdentityId: question.targetIdentityId,
                dueDate: question.dueDate)
        publish(questionOverdueEventClient, QUESTION_OVERDUE_TOPIC, event)
    }

    private static boolean isApproval(Question question) {
        QuestionKind.APPROVAL == question?.kind
    }

    void ensureTopics() {
        try {
            List<String> topics = channelClient.listTopics()
            [QUESTION_ASKED_TOPIC, QUESTION_ANSWERED_TOPIC, APPROVAL_REQUESTED_TOPIC, APPROVAL_DECIDED_TOPIC,
             QUESTION_OVERDUE_TOPIC, APPROVAL_EXPIRED_TOPIC].each { String topic ->
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
