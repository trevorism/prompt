package com.trevorism.controller

import com.trevorism.model.Question
import com.trevorism.model.User
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.AnswerService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/user")
class UserController {

    private static final Logger log = LoggerFactory.getLogger(QuestionController.class.name)

    @Inject
    AnswerService answerService

    @Tag(name = "User Operations")
    @Operation(summary = "Get a list of all Users **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<User> list() {
        answerService.getAllUsers()
    }
}
