package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.FilterBuilder
import com.trevorism.data.model.filtering.FilterConstants
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.*

@jakarta.inject.Singleton
class AnswerQuestionService implements AnswerService {

    private Repository<Answer> answerRepository
    private Repository<Question> questionRepository
    private Repository<User> userRepository

    AnswerQuestionService(SecureHttpClient secureHttpClient) {
        this.answerRepository = new FastDatastoreRepository<>(Answer, secureHttpClient)
        this.questionRepository = new FastDatastoreRepository<>(Question, secureHttpClient)
        this.userRepository = new FastDatastoreRepository<>(User, secureHttpClient)
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

        return new UiAnswer(id: created.id, answeredDate: created.answeredDate, questionId: created.questionId,
                text: created.text, username: username)
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
                            answered: question.answered, username: findMatchingUsername(users, question))
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
                            answered: question.answered, username: findMatchingUsername(users, question))
                }
        return questions
    }

    @Override
    List<User> getActiveUsers() {
        userRepository.filter(new SimpleFilter("active", FilterConstants.OPERATOR_EQUAL, true)).sort { a, b -> a.username <=> b.username }
    }

    @Override
    UiQuestion getQuestion(String id) {
        List<User> users = userRepository.list()
        Question question = questionRepository.get(id)
        return new UiQuestion(id: question.id, text: question.text, createDate: question.createDate,
                answered: question.answered, username: findMatchingUsername(users, question))
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
                answered: question.answered, username: findMatchingUsername(users, question))
        item.answers = answers.findAll { it.questionId == question.id }
                .sort { a, b -> b.answeredDate <=> a.answeredDate }
                .collect { Answer answer ->
                    String username = users.find({ it.id == answer.identityId })?.username
                    if(answer.identityId == "Chat GPT")
                        username = "Chat GPT"

                    new UiAnswer(id: answer.id, answeredDate: answer.answeredDate, questionId: answer.questionId,
                            text: answer.text, username: username)
                }
        return item
    }

    private static String findMatchingUsername(List<User> users, question) {
        users.find({ it.id == question.identityId })?.username
    }

}
