package com.trevorism.model

class ApprovalExpiredEvent {

    String questionId
    String text
    String requesterIdentityId
    String approverIdentityId
    Date dueDate
}
