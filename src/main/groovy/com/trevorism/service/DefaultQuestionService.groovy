package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.model.Answer
import com.trevorism.model.Question
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class DefaultQuestionService implements QuestionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultQuestionService.class.name)

    private final Repository<Question> questionRepository
    private final EventPublishingService eventPublishingService
    private final AnswerService answerService
    private final ChatService chatService
    private final DueDateScheduler dueDateScheduler

    DefaultQuestionService(
            @Named("question") Repository<Question> questionRepository,
            EventPublishingService eventPublishingService,
            AnswerService answerService,
            ChatService chatService,
            DueDateScheduler dueDateScheduler) {
        this.questionRepository = questionRepository
        this.eventPublishingService = eventPublishingService
        this.answerService = answerService
        this.chatService = chatService
        this.dueDateScheduler = dueDateScheduler
    }

    @Override
    Question create(Question question, String requesterId) {
        if (question.text == null || question.text.isEmpty())
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Question text is required")

        question.createDate = new Date()
        question.answered = false
        question.identityId = requesterId
        Question created = questionRepository.create(question)

        eventPublishingService.publishQuestionAsked(created)

        if (question.askChatGpt) {
            String answerText = chatService.ask(question.text)
            answerService.answerQuestion(created.id, new Answer(text: answerText), ChatService.CHAT_GPT_IDENTITY)
        }

        if (created.dueDate && created.dueDate.after(new Date()))
            dueDateScheduler.scheduleDueDateCallback(created)
        else if (created.dueDate)
            log.debug("Due date ${created.dueDate} is not in the future; no overdue callback scheduled for question ${created.id}")

        return created
    }

    @Override
    List<Question> listVisible(String requesterId, Collection<String> requesterRoles) {
        questionRepository.list().findAll { QuestionVisibility.canView(it, requesterId, requesterRoles) }
    }

    @Override
    Question get(String id, String requesterId, Collection<String> requesterRoles) {
        Question question = questionRepository.get(id)
        if (question == null || !QuestionVisibility.canView(question, requesterId, requesterRoles))
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Question not found")
        return question
    }

    @Override
    Question update(String id, Question question, String requesterId, Collection<String> requesterRoles) {
        authorizeModify(id, requesterId, requesterRoles)
        try {
            return questionRepository.update(id, question)
        } catch (Exception e) {
            log.error("Unable to update question", e)
            throw new RuntimeException("Unable to update due to: ${e.message}")
        }
    }

    @Override
    boolean delete(String id, String requesterId, Collection<String> requesterRoles) {
        authorizeModify(id, requesterId, requesterRoles)
        questionRepository.delete(id)
    }

    @Override
    void markOverdueIfUnanswered(String id) {
        Question question = questionRepository.get(id)
        if (question != null && !question.answered && !question.overdueNotified) {
            eventPublishingService.publishQuestionOverdue(question)
            question.overdueNotified = true
            questionRepository.update(id, question)
        }
    }

    private void authorizeModify(String id, String requesterId, Collection<String> requesterRoles) {
        Question existing = questionRepository.get(id)
        if (existing == null || !QuestionVisibility.canView(existing, requesterId, requesterRoles))
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Question not found")
        if (!QuestionVisibility.canModify(existing, requesterId, requesterRoles))
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this question")
    }
}
