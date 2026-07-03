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
