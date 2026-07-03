package com.trevorism.service

import com.trevorism.model.Question
import com.trevorism.secure.Roles

class QuestionVisibility {

    static boolean canView(Question question, String requesterId, Collection<String> requesterRoles) {
        if (!question.privateQuestion)
            return true
        if (requesterId && (requesterId == question.identityId || requesterId == question.targetIdentityId))
            return true
        return isPrivileged(requesterRoles)
    }

    /**
     * Modifying/deleting a question is a stronger right than reading it: only the asker (or an
     * admin/internal caller) may change it — the target is a reader, not an owner.
     */
    static boolean canModify(Question question, String requesterId, Collection<String> requesterRoles) {
        if (requesterId && requesterId == question.identityId)
            return true
        return isPrivileged(requesterRoles)
    }

    static boolean isPrivileged(Collection<String> requesterRoles) {
        requesterRoles?.any { it == Roles.ADMIN || it == Roles.INTERNAL } ?: false
    }
}
