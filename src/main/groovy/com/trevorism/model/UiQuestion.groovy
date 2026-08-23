package com.trevorism.model

class UiQuestion {

    String id
    String text
    Date createDate
    boolean answered
    String username
    String kind
    Date dueDate
    List<Choice> choices = []
    boolean allowMultipleAnswers

}
