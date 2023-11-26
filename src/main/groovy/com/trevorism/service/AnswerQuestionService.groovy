package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Answer
import com.trevorism.model.Question

@jakarta.inject.Singleton
class AnswerQuestionService implements AnswerService{

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
        Question updated =questionRepository.update(questionId, question)

        return created
    }
}
