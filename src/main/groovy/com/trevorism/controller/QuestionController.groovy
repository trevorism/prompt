package com.trevorism.controller

import com.trevorism.data.PingingDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.UiAnswer
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.AnswerService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/question")
class QuestionController {

    private Repository<Question> repository
    private static final Logger log = LoggerFactory.getLogger(QuestionController.class.name)

    @Inject
    AnswerService answerService

    QuestionController(SecureHttpClient secureHttpClient) {
        this.repository = new PingingDatastoreRepository<>(Question, secureHttpClient)
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Creates a new question **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Question createQuestion(@Body Question question, Authentication authentication) {
        question.createDate = new Date()
        question.answered = false
        question.identityId = authentication?.attributes?.get("id")
        repository.create(question)
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Get a list of all Questions **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<Question> list() {
        repository.list()
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "View a Question by id **Secure")
    @Get(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Question get(String id) {
        repository.get(id)
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Update a Question **Secure")
    @Put(value = "{id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Question update(String id,@Body Question question) {
        try {
            Question updated = repository.update(id, question)
            return updated
        } catch (Exception e) {
            log.error("Unable to update question", e)
            throw new RuntimeException("Unable to update due to: ${e.message}")
        }
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Delete a Question by id **Secure")
    @Delete(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    boolean delete(String id) {
        repository.delete(id)
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "Answer a question by id **Secure")
    @Post(value = "{id}/answer", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    UiAnswer answerQuestion(String id, @Body Answer answer, Authentication authentication) {
        String identityId = authentication?.attributes?.get("id")
        answerService.answerQuestion(id, answer, identityId)
    }
}
