package com.trevorism.gcloud

import com.trevorism.PromptWorld

/**
 * @author tbrooks
 */

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

def contextRootContent
def pingContent

Given(~/^the prompt application is alive$/) { ->
    try{
        new URL("${PromptWorld.BASE_URL}/api/ping").text
    }
    catch (Exception ignored){
        Thread.sleep(10000)
        new URL("${PromptWorld.BASE_URL}/api/ping").text
    }
}

When(~/^I navigate to "([^"]*)"$/) { String url ->
    contextRootContent = new URL("${PromptWorld.BASE_URL}/api").text
}

Then(~/^then a link to the help page is displayed$/) { ->
    assert contextRootContent
    assert contextRootContent.contains("/help")
}

When(~/^I ping the application deployed to "([^"]*)"$/) { String url ->
    pingContent = new URL("${PromptWorld.BASE_URL}/api/ping").text
}

Then(~/^pong is returned, to indicate the service is alive$/) { ->
    assert pingContent == "pong"
}
