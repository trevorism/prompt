package com.trevorism.controller

import com.trevorism.model.Answer
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.AnswerService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject

@Controller("/api/answer")
class AnswerController {

    @Inject
    AnswerService answerService

    @Tag(name = "Answer Operations")
    @Operation(summary = "Get a list of all Answers **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<Answer> list(Authentication authentication) {
        answerService.listVisibleAnswers(requesterId(authentication), authentication.getRoles())
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "View a Answer by id **Secure")
    @Get(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Answer get(String id, Authentication authentication) {
        answerService.getAnswer(id, requesterId(authentication), authentication.getRoles())
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "Update a Answer **Secure")
    @Put(value = "{id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Answer update(String id, @Body Answer answer, Authentication authentication) {
        answerService.updateAnswer(id, answer, requesterId(authentication), authentication.getRoles())
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "Delete a Answer by id **Secure")
    @Delete(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    boolean delete(String id, Authentication authentication) {
        answerService.deleteAnswer(id, requesterId(authentication), authentication.getRoles())
    }

    private static String requesterId(Authentication authentication) {
        authentication.getAttributes().get("id") as String
    }
}
