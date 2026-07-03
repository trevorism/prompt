package com.trevorism.model

class QuestionAskedEvent {

    String questionId
    String text
    String askerIdentityId
    String targetIdentityId
    boolean privateQuestion
    boolean askChatGpt
    Date createDate
}
