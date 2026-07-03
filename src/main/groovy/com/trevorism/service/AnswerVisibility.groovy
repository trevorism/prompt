package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question

/**
 * Read/modify rules for answers. An answer's readability tracks its question (you can read an
 * answer if you can read the question it belongs to), plus the author can always read their own.
 * Modifying/deleting an answer is author-only (or admin/internal).
 */
class AnswerVisibility {

    static boolean canView(Answer answer, Question question, String requesterId, Collection<String> requesterRoles) {
        if (requesterId && requesterId == answer.identityId)
            return true
        return question != null && QuestionVisibility.canView(question, requesterId, requesterRoles)
    }

    static boolean canModify(Answer answer, String requesterId, Collection<String> requesterRoles) {
        if (requesterId && requesterId == answer.identityId)
            return true
        return QuestionVisibility.isPrivileged(requesterRoles)
    }
}
