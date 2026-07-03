package com.trevorism.service

import com.trevorism.model.Question
import com.trevorism.secure.Roles
import org.junit.jupiter.api.Test

class QuestionVisibilityTest {

    @Test
    void nonPrivateQuestionIsViewableByAnyone() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: false)
        assert QuestionVisibility.canView(q, "stranger", [Roles.USER])
        assert QuestionVisibility.canView(q, null, null)
    }

    @Test
    void privateQuestionIsViewableByAsker() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
        assert QuestionVisibility.canView(q, "asker", [Roles.USER])
    }

    @Test
    void privateQuestionIsViewableByTarget() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
        assert QuestionVisibility.canView(q, "target", [Roles.USER])
    }

    @Test
    void privateQuestionIsNotViewableByUnrelatedUser() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
        assert !QuestionVisibility.canView(q, "stranger", [Roles.USER])
    }

    @Test
    void privateQuestionIsNotViewableByNullRequester() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
        assert !QuestionVisibility.canView(q, null, [Roles.USER])
    }

    @Test
    void privateQuestionIsViewableByAdmin() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
        assert QuestionVisibility.canView(q, "stranger", [Roles.ADMIN])
    }

    @Test
    void privateQuestionIsViewableByInternal() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
        assert QuestionVisibility.canView(q, "stranger", [Roles.INTERNAL])
    }

    @Test
    void privateQuestionHandlesNullRolesSafely() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
        assert !QuestionVisibility.canView(q, "stranger", null)
    }

    @Test
    void askerCanModify() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target")
        assert QuestionVisibility.canModify(q, "asker", [Roles.USER])
    }

    @Test
    void targetCannotModify() {
        // The target may read the question but must not modify or delete it
        Question q = new Question(identityId: "asker", targetIdentityId: "target")
        assert !QuestionVisibility.canModify(q, "target", [Roles.USER])
    }

    @Test
    void strangerCannotModify() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target")
        assert !QuestionVisibility.canModify(q, "stranger", [Roles.USER])
    }

    @Test
    void nullRequesterCannotModify() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target")
        assert !QuestionVisibility.canModify(q, null, [Roles.USER])
    }

    @Test
    void adminAndInternalCanModify() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target")
        assert QuestionVisibility.canModify(q, "stranger", [Roles.ADMIN])
        assert QuestionVisibility.canModify(q, "stranger", [Roles.INTERNAL])
    }

    @Test
    void canModifyHandlesNullRolesSafely() {
        Question q = new Question(identityId: "asker", targetIdentityId: "target")
        assert !QuestionVisibility.canModify(q, "stranger", null)
    }
}
