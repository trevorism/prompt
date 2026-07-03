package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question

interface EventPublishingService {

    void publishQuestionAsked(Question question)

    void publishQuestionAnswered(Question question, Answer answer, String answererUsername)

    void publishQuestionOverdue(Question question)
}
