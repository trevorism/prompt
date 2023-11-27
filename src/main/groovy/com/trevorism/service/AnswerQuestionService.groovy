package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.FilterBuilder
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.QuestionListItem

@jakarta.inject.Singleton
class AnswerQuestionService implements AnswerService {

    private Repository<Answer> answerRepository
    private Repository<Question> questionRepository

    AnswerQuestionService(SecureHttpClient secureHttpClient) {
        this.answerRepository = new FastDatastoreRepository<>(Answer, secureHttpClient)
        this.questionRepository = new FastDatastoreRepository<>(Question, secureHttpClient)
    }

    @Override
    Answer answerQuestion(String questionId, Answer answer, String identityId) {
        Question question = questionRepository.get(questionId)

        answer.answeredDate = new Date()
        answer.questionId = questionId
        answer.identityId = identityId
        Answer created = answerRepository.create(answer)

        question.answered = true
        Question updated = questionRepository.update(questionId, question)

        return created
    }

    @Override
    List<QuestionListItem> getAllQuestions() {
        List<Question> questions = questionRepository.list()
        List<Answer> answers = answerRepository.list()
        List<QuestionListItem> result = []
        questions.each { question ->
            QuestionListItem item = new QuestionListItem()
            item.question = question
            item.answers = answers.findAll { it.questionId == question.id }
            result << item
        }
        return result
    }

    @Override
    List<Question> getUnansweredQuestions() {
        List<Question> questions = questionRepository.filter(new FilterBuilder().addFilter(
                new SimpleFilter("answered", "=", false)).build())
        return questions
    }

    @Override
    List<QuestionListItem> getMyQuestions(String identityId) {
        List<Question> questions = questionRepository.filter(new FilterBuilder().addFilter(
                new SimpleFilter("identityId", "=", identityId)).build())
        List<Answer> answers = answerRepository.list()
        List<QuestionListItem> result = []
        questions.each { question ->
            QuestionListItem item = new QuestionListItem()
            item.question = question
            item.answers = answers.findAll { it.questionId == question.id }
            result << item
        }
        return result
    }

    @Override
    List<Question> getPendingQuestions(String identityId) {
        List<Question> questions = questionRepository.filter(new FilterBuilder().addFilter(
                new SimpleFilter("targetIdentityId", "=", identityId), new SimpleFilter("answered", "=", false)).build())
        return questions
    }
}
