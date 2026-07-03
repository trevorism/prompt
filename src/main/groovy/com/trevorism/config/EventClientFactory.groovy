package com.trevorism.config

import com.trevorism.event.ChannelClient
import com.trevorism.event.DefaultChannelClient
import com.trevorism.event.DefaultEventClient
import com.trevorism.event.EventClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ApprovalDecidedEvent
import com.trevorism.model.ApprovalExpiredEvent
import com.trevorism.model.ApprovalRequestedEvent
import com.trevorism.model.QuestionAnsweredEvent
import com.trevorism.model.QuestionAskedEvent
import com.trevorism.model.QuestionOverdueEvent
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton

@Factory
class EventClientFactory {

    @Singleton
    @Named("questionAsked")
    EventClient<QuestionAskedEvent> questionAskedEventClient(SecureHttpClient secureHttpClient) {
        new DefaultEventClient<QuestionAskedEvent>(secureHttpClient)
    }

    @Singleton
    @Named("questionAnswered")
    EventClient<QuestionAnsweredEvent> questionAnsweredEventClient(SecureHttpClient secureHttpClient) {
        new DefaultEventClient<QuestionAnsweredEvent>(secureHttpClient)
    }

    @Singleton
    @Named("approvalRequested")
    EventClient<ApprovalRequestedEvent> approvalRequestedEventClient(SecureHttpClient secureHttpClient) {
        new DefaultEventClient<ApprovalRequestedEvent>(secureHttpClient)
    }

    @Singleton
    @Named("approvalDecided")
    EventClient<ApprovalDecidedEvent> approvalDecidedEventClient(SecureHttpClient secureHttpClient) {
        new DefaultEventClient<ApprovalDecidedEvent>(secureHttpClient)
    }

    @Singleton
    @Named("questionOverdue")
    EventClient<QuestionOverdueEvent> questionOverdueEventClient(SecureHttpClient secureHttpClient) {
        new DefaultEventClient<QuestionOverdueEvent>(secureHttpClient)
    }

    @Singleton
    @Named("approvalExpired")
    EventClient<ApprovalExpiredEvent> approvalExpiredEventClient(SecureHttpClient secureHttpClient) {
        new DefaultEventClient<ApprovalExpiredEvent>(secureHttpClient)
    }

    @Singleton
    ChannelClient channelClient(SecureHttpClient secureHttpClient) {
        new DefaultChannelClient(secureHttpClient)
    }
}
