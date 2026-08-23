package com.trevorism.model

class QuestionAnsweredEvent {

    String questionId
    String questionText
    String askerIdentityId
    String answerId
    String answerText
    String answererIdentityId
    String answererUsername
    Date answeredDate
    List<String> selectedChoices = []
}
