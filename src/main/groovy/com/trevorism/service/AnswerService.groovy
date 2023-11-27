package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.QuestionListItem

interface AnswerService {

    Answer answerQuestion(String questionId, Answer answer, String identityId)

    List<QuestionListItem> getAllQuestions()
    List<QuestionListItem> getMyQuestions(String identityId)
    List<Question> getUnansweredQuestions()
    List<Question> getPendingQuestions(String identityId)
}
