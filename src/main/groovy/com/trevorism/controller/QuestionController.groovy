package com.trevorism.controller

import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.UiAnswer
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.AnswerService
import com.trevorism.service.QuestionService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject

@Controller("/api/question")
class QuestionController {

    @Inject
    QuestionService questionService

    @Inject
    AnswerService answerService

    @Tag(name = "Question Operations")
    @Operation(summary = "Creates a new question **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Question createQuestion(@Body Question question, Authentication authentication) {
        questionService.create(question, requesterId(authentication))
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Get a list of all Questions **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<Question> list(Authentication authentication) {
        questionService.listVisible(requesterId(authentication), authentication.getRoles())
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "View a Question by id **Secure")
    @Get(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Question get(String id, Authentication authentication) {
        questionService.get(id, requesterId(authentication), authentication.getRoles())
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Update a Question **Secure")
    @Put(value = "{id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Question update(String id, @Body Question question, Authentication authentication) {
        questionService.update(id, question, requesterId(authentication), authentication.getRoles())
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Delete a Question by id **Secure")
    @Delete(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    boolean delete(String id, Authentication authentication) {
        questionService.delete(id, requesterId(authentication), authentication.getRoles())
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "Answer a question by id **Secure")
    @Post(value = "{id}/answer", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    UiAnswer answerQuestion(String id, @Body Answer answer, Authentication authentication) {
        answerService.answerQuestion(id, answer, requesterId(authentication))
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Callback invoked by the schedule service at a question's due date **Secure")
    @Post(value = "{id}/duedate/callback")
    @Secure(value = Roles.SYSTEM, allowInternal = true)
    HttpResponse<?> dueDateCallback(String id) {
        questionService.markOverdueIfUnanswered(id)
        return HttpResponse.ok()
    }

    private static String requesterId(Authentication authentication) {
        authentication.getAttributes().get("id") as String
    }
}
