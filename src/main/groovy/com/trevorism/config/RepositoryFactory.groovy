package com.trevorism.config

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.PingingDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.model.User
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton

@Factory
class RepositoryFactory {

    @Singleton
    @Named("question")
    Repository<Question> questionRepository(SecureHttpClient secureHttpClient) {
        new PingingDatastoreRepository<Question>(Question, secureHttpClient)
    }

    @Singleton
    @Named("answer")
    Repository<Answer> answerRepository(SecureHttpClient secureHttpClient) {
        new FastDatastoreRepository<Answer>(Answer, secureHttpClient)
    }

    @Singleton
    @Named("user")
    Repository<User> userRepository(SecureHttpClient secureHttpClient) {
        new FastDatastoreRepository<User>(User, secureHttpClient)
    }
}
