package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.FilterBuilder
import com.trevorism.data.model.filtering.FilterConstants
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.model.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@jakarta.inject.Singleton
class AnswerQuestionService implements AnswerService {

    private static final Logger log = LoggerFactory.getLogger(AnswerQuestionService.class.name)

    private Repository<Answer> answerRepository
    private Repository<Question> questionRepository
    private Repository<User> userRepository
    private EventPublishingService eventPublishingService

    AnswerQuestionService(
            @Named("answer") Repository<Answer> answerRepository,
            @Named("question") Repository<Question> questionRepository,
            @Named("user") Repository<User> userRepository,
            EventPublishingService eventPublishingService) {
        this.answerRepository = answerRepository
        this.questionRepository = questionRepository
        this.userRepository = userRepository
        this.eventPublishingService = eventPublishingService
    }

    @Override
    UiAnswer answerQuestion(String questionId, Answer answer, String identityId) {
        Question question = questionRepository.get(questionId)
        List<User> users = userRepository.list()

        answer.answeredDate = new Date()
        answer.questionId = questionId
        answer.identityId = identityId
        Answer created = answerRepository.create(answer)

        question.answered = true
        questionRepository.update(questionId, question)

        String username = users.find({ it.id == created.identityId })?.username
        if(identityId == "Chat GPT")
            username = "Chat GPT"

        eventPublishingService.publishQuestionAnswered(question, created, username)

        return new UiAnswer(id: created.id, answeredDate: created.answeredDate, questionId: created.questionId,
                text: created.text, username: username, approved: created.approved)
    }

    @Override
    List<QuestionListItem> getAllQuestions() {
        List<Question> questions = questionRepository.filter(new SimpleFilter("privateQuestion", FilterConstants.OPERATOR_EQUAL, false))
        return appendAnswersToQuestions(questions)
    }

    @Override
    List<UiQuestion> getUnansweredQuestions() {
        List<User> users = userRepository.list()
        List<UiQuestion> questions = questionRepository
                .filter(new FilterBuilder()
                        .addFilter(new SimpleFilter("answered", FilterConstants.OPERATOR_EQUAL, false))
                        .addFilter(new SimpleFilter("privateQuestion", FilterConstants.OPERATOR_EQUAL, false))
                        .build())
                .sort { a, b -> b.createDate <=> a.createDate }
                .collect { Question question ->
                    new UiQuestion(id: question.id, text: question.text, createDate: question.createDate,
                            answered: question.answered, username: findMatchingUsername(users, question), kind: question.kind, dueDate: question.dueDate)
                }
        return questions
    }

    @Override
    List<QuestionListItem> getMyQuestions(String identityId) {
        List<Question> questions = questionRepository.filter(new SimpleFilter("identityId", FilterConstants.OPERATOR_EQUAL, identityId))
        return appendAnswersToQuestions(questions)
    }

    @Override
    List<UiQuestion> getPendingQuestions(String identityId) {
        List<User> users = userRepository.list()
        List<UiQuestion> questions = questionRepository
                .filter(new FilterBuilder().addFilter(
                        new SimpleFilter("targetIdentityId", FilterConstants.OPERATOR_EQUAL, identityId),
                        new SimpleFilter("answered", FilterConstants.OPERATOR_EQUAL, false)).build())
                .sort { a, b -> b.createDate <=> a.createDate }
                .collect { Question question ->
                    new UiQuestion(id: question.id, text: question.text, createDate: question.createDate,
                            answered: question.answered, username: findMatchingUsername(users, question), kind: question.kind, dueDate: question.dueDate)
                }
        return questions
    }

    @Override
    List<UiQuestion> getPendingApprovals(String identityId) {
        getPendingQuestions(identityId).findAll { it.kind == "approval" }
    }

    @Override
    List<User> getActiveUsers() {
        userRepository.filter(new SimpleFilter("active", FilterConstants.OPERATOR_EQUAL, true)).sort { a, b -> a.username <=> b.username }
    }

    @Override
    UiQuestion getQuestion(String id, String requesterId, Collection<String> requesterRoles) {
        Question question = questionRepository.get(id)
        if (question == null || !QuestionVisibility.canView(question, requesterId, requesterRoles))
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Question not found")

        List<User> users = userRepository.list()
        return new UiQuestion(id: question.id, text: question.text, createDate: question.createDate,
                answered: question.answered, username: findMatchingUsername(users, question), kind: question.kind, dueDate: question.dueDate)
    }

    @Override
    List<Answer> listVisibleAnswers(String requesterId, Collection<String> requesterRoles) {
        Map<String, Question> questionsById = questionRepository.list().collectEntries { [(it.id): it] }
        answerRepository.list().findAll { AnswerVisibility.canView(it, questionsById[it.questionId], requesterId, requesterRoles) }
    }

    @Override
    Answer getAnswer(String id, String requesterId, Collection<String> requesterRoles) {
        Answer answer = answerRepository.get(id)
        if (answer == null || !canViewAnswer(answer, requesterId, requesterRoles))
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Answer not found")
        return answer
    }

    @Override
    Answer updateAnswer(String id, Answer answer, String requesterId, Collection<String> requesterRoles) {
        authorizeAnswerModify(id, requesterId, requesterRoles)
        try {
            return answerRepository.update(id, answer)
        } catch (Exception e) {
            log.error("Unable to update answer", e)
            throw new RuntimeException("Unable to update due to: ${e.message}")
        }
    }

    @Override
    boolean deleteAnswer(String id, String requesterId, Collection<String> requesterRoles) {
        authorizeAnswerModify(id, requesterId, requesterRoles)
        answerRepository.delete(id)
    }

    private boolean canViewAnswer(Answer answer, String requesterId, Collection<String> requesterRoles) {
        Question question = answer.questionId ? questionRepository.get(answer.questionId) : null
        AnswerVisibility.canView(answer, question, requesterId, requesterRoles)
    }

    private void authorizeAnswerModify(String id, String requesterId, Collection<String> requesterRoles) {
        Answer existing = answerRepository.get(id)
        if (existing == null || !canViewAnswer(existing, requesterId, requesterRoles))
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Answer not found")
        if (!AnswerVisibility.canModify(existing, requesterId, requesterRoles))
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this answer")
    }

    private ArrayList<QuestionListItem> appendAnswersToQuestions(List<Question> questions) {
        List<Answer> answers = answerRepository.list()
        List<User> users = userRepository.list()
        List<QuestionListItem> result = []
        questions.sort { a, b -> b.createDate <=> a.createDate }
                .each { question ->
                    QuestionListItem item = createQuestionListItem(question, answers, users)
                    result << item
                }
        return result
    }

    private static QuestionListItem createQuestionListItem(Question question, List<Answer> answers, List<User> users) {
        QuestionListItem item = new QuestionListItem()
        item.question = new UiQuestion(id: question.id, text: question.text, createDate: question.createDate,
                answered: question.answered, username: findMatchingUsername(users, question), kind: question.kind, dueDate: question.dueDate)
        item.answers = answers.findAll { it.questionId == question.id }
                .sort { a, b -> b.answeredDate <=> a.answeredDate }
                .collect { Answer answer ->
                    String username = users.find({ it.id == answer.identityId })?.username
                    if(answer.identityId == "Chat GPT")
                        username = "Chat GPT"

                    new UiAnswer(id: answer.id, answeredDate: answer.answeredDate, questionId: answer.questionId,
                            text: answer.text, username: username, approved: answer.approved)
                }
        return item
    }

    private static String findMatchingUsername(List<User> users, question) {
        users.find({ it.id == question.identityId })?.username
    }

}
