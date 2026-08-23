package com.trevorism.model

class ApprovalRequestedEvent {

    String questionId
    String text
    String requesterIdentityId
    String approverIdentityId
    Date createDate
    List<Choice> choices = []
    boolean allowMultipleAnswers
}
