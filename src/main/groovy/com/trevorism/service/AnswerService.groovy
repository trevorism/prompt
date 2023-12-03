package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.QuestionListItem
import com.trevorism.model.UiAnswer
import com.trevorism.model.UiQuestion

interface AnswerService {

    UiAnswer answerQuestion(String questionId, Answer answer, String identityId)

    List<QuestionListItem> getAllQuestions()
    List<QuestionListItem> getMyQuestions(String identityId)
    List<UiQuestion> getUnansweredQuestions()
    List<UiQuestion> getPendingQuestions(String identityId)
}
