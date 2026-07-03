package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.QuestionListItem
import com.trevorism.model.UiAnswer
import com.trevorism.model.UiQuestion
import com.trevorism.model.User

interface AnswerService {

    UiAnswer answerQuestion(String questionId, Answer answer, String identityId)

    List<QuestionListItem> getAllQuestions()
    List<QuestionListItem> getMyQuestions(String identityId)
    List<UiQuestion> getUnansweredQuestions()
    List<UiQuestion> getPendingQuestions(String identityId)
    List<UiQuestion> getPendingApprovals(String identityId)
    List<User> getActiveUsers()

    UiQuestion getQuestion(String id, String requesterId, Collection<String> requesterRoles)

    List<Answer> listVisibleAnswers(String requesterId, Collection<String> requesterRoles)
    Answer getAnswer(String id, String requesterId, Collection<String> requesterRoles)
    Answer updateAnswer(String id, Answer answer, String requesterId, Collection<String> requesterRoles)
    boolean deleteAnswer(String id, String requesterId, Collection<String> requesterRoles)
}
