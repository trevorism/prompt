package com.trevorism.config

import com.trevorism.event.ChannelClient
import com.trevorism.event.DefaultChannelClient
import com.trevorism.event.DefaultEventClient
import com.trevorism.event.EventClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.QuestionAnsweredEvent
import com.trevorism.model.QuestionAskedEvent
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
    ChannelClient channelClient(SecureHttpClient secureHttpClient) {
        new DefaultChannelClient(secureHttpClient)
    }
}
