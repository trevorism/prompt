package com.trevorism.controller

import com.google.gson.Gson
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.PingingDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Answer
import com.trevorism.model.ChatGptMessage
import com.trevorism.model.Email
import com.trevorism.model.Question
import com.trevorism.model.UiAnswer
import com.trevorism.model.User
import com.trevorism.schedule.DefaultScheduleService
import com.trevorism.schedule.ScheduleService
import com.trevorism.schedule.factory.DefaultScheduledTaskFactory
import com.trevorism.schedule.factory.EndpointSpec
import com.trevorism.schedule.factory.ScheduledTaskFactory
import com.trevorism.schedule.model.HttpMethod
import com.trevorism.schedule.model.ScheduledTask
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.AnswerService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.temporal.ChronoUnit

@Controller("/api/question")
class QuestionController {

    private static final Logger log = LoggerFactory.getLogger(QuestionController.class.name)
    private Repository<Question> repository
    private SecureHttpClient secureHttpClient

    @Inject
    AnswerService answerService

    QuestionController(SecureHttpClient secureHttpClient) {
        this.secureHttpClient = secureHttpClient
        this.repository = new PingingDatastoreRepository<>(Question, secureHttpClient)
    }

    @Tag(name = "Question Operations")
    @Operation(summary = "Creates a new question **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Question createQuestion(@Body Question question, Authentication authentication) {
        if (question.text == null || question.text.isEmpty())
            throw new RuntimeException("Question text is required")

        question.createDate = new Date()
        question.answered = false
        question.identityId = authentication?.attributes?.get("id")
        Question created = repository.create(question)

        if (question.askChatGpt) {
            String response = secureHttpClient.post("https://chat.action.trevorism.com/api/chat", new ChatGptMessage(message: question.text).toJson())
            ChatGptMessage chatGptResponse = ChatGptMessage.fromJson(response)
            answerService.answerQuestion(created.id, new Answer(text: chatGptResponse.message), "Chat GPT")
        }


        if (question.dueDate) {
            Date oneDayBefore = Date.from(question.dueDate.toInstant().minus(1, ChronoUnit.DAYS))
            if(oneDayBefore.after(new Date())) {
                ScheduleService scheduleService = new DefaultScheduleService(secureHttpClient)
                ScheduledTaskFactory factory = new DefaultScheduledTaskFactory()
                Gson gson = new Gson()
                Email email = new Email([subject: "Trevorism is prompting you to answer a question", body: buildEmailBody(created), recipients: [getUserEmail(question.identityId)]])
                EndpointSpec endpointSpec = new EndpointSpec("https://email.action.trevorism.com/mail", HttpMethod.POST, gson.toJson(email))
                ScheduledTask st = factory.createImmediateTask("testSched", oneDayBefore, endpointSpec)
                scheduleService.create(st)
            }
        }
        return created
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
    Question update(String id, @Body Question question) {
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

    private static String buildEmailBody(Question question) {
        return """Trevorism is prompting you to answer a question. The question is:
        
        ${question.text}
        
        This notification is being sent because the answer is being requested by ${question.dueDate}

        Please answer it here: https://prompt.action.trevorism.com/answer/${question.id}
        """
    }

    String getUserEmail(String id) {
        Repository<User> userRepository = new FastDatastoreRepository<>(User, secureHttpClient)
        User user = userRepository.get(id)
        return user.email
    }
}
