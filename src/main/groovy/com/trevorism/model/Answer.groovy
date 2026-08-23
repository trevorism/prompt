package com.trevorism.model

class Answer {

    String id
    Date answeredDate
    String questionId
    String text
    String identityId
    Boolean approved
    Map metadata
    List<String> selectedChoices = []
}
