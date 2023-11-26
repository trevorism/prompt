package com.trevorism.service

import com.trevorism.model.Answer

interface AnswerService {

    Answer answerQuestion(String questionId, Answer answer, String identityId)
}
