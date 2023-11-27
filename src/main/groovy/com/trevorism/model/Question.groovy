package com.trevorism.model

class Question {

    String id
    String text
    Date createDate
    boolean answered
    String identityId

    String targetIdentityId
    Date dueDate
    String responseType
    List<String> answerFields
}
