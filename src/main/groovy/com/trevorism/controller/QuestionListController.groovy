package com.trevorism.controller

import com.trevorism.model.Question
import com.trevorism.model.QuestionListItem
import com.trevorism.model.UiQuestion
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.AnswerService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject

@Controller("/api/list")
class QuestionListController {

    @Inject
    AnswerService answerService

    @Tag(name = "App Operations")
    @Operation(summary = "Get a list of all Question and Answers **Secure")
    @Get(value = "/question", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<QuestionListItem> all() {
        answerService.getAllQuestions()
    }

    @Tag(name = "App Operations")
    @Operation(summary = "Get a list of all Question and Answers **Secure")
    @Get(value = "/my", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<QuestionListItem> my(Authentication authentication) {
        answerService.getMyQuestions(authentication.getAttributes().get("id"))
    }

    @Tag(name = "App Operations")
    @Operation(summary = "Get a list of all Question and Answers **Secure")
    @Get(value = "/unanswered", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<UiQuestion> unanswered() {
        answerService.getUnansweredQuestions()
    }

    @Tag(name = "App Operations")
    @Operation(summary = "Get a list of all Question and Answers **Secure")
    @Get(value = "/pending", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<UiQuestion> pending(Authentication authentication) {
        answerService.getPendingQuestions(authentication.getAttributes().get("id"))
    }
}
