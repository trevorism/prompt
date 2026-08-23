package com.trevorism.gcloud

import com.trevorism.PromptWorld

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

World {
    new PromptWorld()
}

After { ->
    cleanup()
}

When(~/^I GET "(.*)" anonymously$/) { String path ->
    anonGet(path)
}

When(~/^I POST "(.*)" anonymously$/) { String path ->
    anonPost(path)
}

Then(~/^the response body is "(.*)"$/) { String expected ->
    assert body?.trim() == expected
}

Then(~/^the request is rejected$/) { ->
    assert rejected
}

Given(~/^a plain question is created$/) { ->
    lastQuestion = createPlainQuestion()
    currentQuestionId = lastQuestion.id
}

Then(~/^the created question is unanswered$/) { ->
    assert currentQuestionId
    assert lastQuestion.answered == false
}

When(~/^the question is answered$/) { ->
    lastAnswer = answerText(currentQuestionId, "${PromptWorld.MARKER} an answer".toString())
}

Then(~/^the question is marked answered$/) { ->
    Map q = fetchQuestion(currentQuestionId)
    assert q.answered == true
}

Given(~/^an approval request is created for a user$/) { ->
    lastQuestion = createApproval("acceptance-approver")
    currentQuestionId = lastQuestion.id
}

Then(~/^the created question has kind "(.*)"$/) { String kind ->
    assert lastQuestion.kind == kind
}

When(~/^the approval is approved with a reason$/) { ->
    lastAnswer = decide(currentQuestionId, true, "${PromptWorld.MARKER} looks good".toString())
}

Then(~/^the decision is approved$/) { ->
    assert lastAnswer.approved == true
}

Given(~/^a multiple choice question is created with choices "(.*)"$/) { String labels ->
    lastQuestion = createMultipleChoice(false, labels.split(",").toList())
    currentQuestionId = lastQuestion.id
}

Given(~/^a multi select question is created with choices "(.*)"$/) { String labels ->
    lastQuestion = createMultipleChoice(true, labels.split(",").toList())
    currentQuestionId = lastQuestion.id
}

When(~/^a multiple choice question is attempted with choices "(.*)"$/) { String labels ->
    attemptCreateQuestion([text   : "${PromptWorld.MARKER} invalid multiple choice".toString(), kind: "question",
                           choices: labels.split(",").collect { [label: it] }])
}

Then(~/^the created question offers (\d+) choices$/) { Integer count ->
    assert lastQuestion.choices.size() == count
}

Then(~/^the created question allows multiple answers$/) { ->
    assert lastQuestion.allowMultipleAnswers == true
}

Then(~/^the created question does not allow multiple answers$/) { ->
    assert lastQuestion.allowMultipleAnswers == false
}

When(~/^the choice "(.*)" is selected$/) { String value ->
    lastAnswer = chooseOption(currentQuestionId, [value])
}

When(~/^the choices "(.*)" are selected$/) { String values ->
    lastAnswer = chooseOption(currentQuestionId, values.split(",").toList())
}

When(~/^the choice "(.*)" is attempted$/) { String value ->
    attemptChooseOption(currentQuestionId, [value])
}

When(~/^the choices "(.*)" are attempted$/) { String values ->
    attemptChooseOption(currentQuestionId, values.split(",").toList())
}

Then(~/^the answer text is "(.*)"$/) { String expected ->
    assert lastAnswer.text == expected
}

Then(~/^the question is not marked answered$/) { ->
    Map q = fetchQuestion(currentQuestionId)
    assert q.answered == false
}
