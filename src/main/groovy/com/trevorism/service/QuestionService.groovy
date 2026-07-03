package com.trevorism.service

import com.trevorism.model.Question

interface QuestionService {

    Question create(Question question, String requesterId)

    List<Question> listVisible(String requesterId, Collection<String> requesterRoles)

    Question get(String id, String requesterId, Collection<String> requesterRoles)

    Question update(String id, Question question, String requesterId, Collection<String> requesterRoles)

    boolean delete(String id, String requesterId, Collection<String> requesterRoles)

    void markOverdueIfUnanswered(String id)
}
