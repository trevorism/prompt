package com.trevorism.model

class ApprovalDecidedEvent {

    String questionId
    String questionText
    String requesterIdentityId
    String decisionAnswerId
    Boolean approved
    String reason
    String approverIdentityId
    String approverUsername
    Date decidedDate
}
