package com.trevorism.model

/**
 * The kinds a Question can take. Plain questions default to QUESTION; APPROVAL marks a directed
 * approval request whose answer is a decision.
 */
class QuestionKind {

    static final String QUESTION = "question"
    static final String APPROVAL = "approval"
}
