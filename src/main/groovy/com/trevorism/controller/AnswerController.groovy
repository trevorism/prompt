package com.trevorism.controller

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Answer
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/answer")
class AnswerController {

    private static final Logger log = LoggerFactory.getLogger(AnswerController.class.name)
    private Repository<Answer> repository

    AnswerController(SecureHttpClient httpClient) {
        this.repository = new FastDatastoreRepository<>(Answer, httpClient)
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "Get a list of all Answers **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<Answer> list() {
        repository.list()
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "View a Answer by id **Secure")
    @Get(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Answer get(String id) {
        repository.get(id)
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "Update a Answer **Secure")
    @Put(value = "{id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Answer update(String id, Answer answer) {
        try {
            Answer updated = repository.update(id, answer)
            return updated
        } catch (Exception e) {
            log.error("Unable to update answer", e)
            throw new RuntimeException("Unable to update due to: ${e.message}")
        }
    }

    @Tag(name = "Answer Operations")
    @Operation(summary = "Delete a Answer by id **Secure")
    @Delete(value = "{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    boolean delete(String id) {
        repository.delete(id)
    }
}
