package com.trevorism.service

import com.trevorism.model.Answer
import com.trevorism.model.Question
import com.trevorism.secure.Roles
import org.junit.jupiter.api.Test

class AnswerVisibilityTest {

    private static Question publicQuestion() {
        new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: false)
    }

    private static Question privateQuestion() {
        new Question(identityId: "asker", targetIdentityId: "target", privateQuestion: true)
    }

    @Test
    void answerToPublicQuestionIsViewableByAnyone() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert AnswerVisibility.canView(a, publicQuestion(), "stranger", [Roles.USER])
    }

    @Test
    void answerToPrivateQuestionIsHiddenFromUnrelatedUser() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert !AnswerVisibility.canView(a, privateQuestion(), "stranger", [Roles.USER])
    }

    @Test
    void answerToPrivateQuestionIsViewableByQuestionParty() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert AnswerVisibility.canView(a, privateQuestion(), "asker", [Roles.USER])
        assert AnswerVisibility.canView(a, privateQuestion(), "target", [Roles.USER])
    }

    @Test
    void authorCanAlwaysViewOwnAnswer() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert AnswerVisibility.canView(a, privateQuestion(), "answerer", [Roles.USER])
    }

    @Test
    void answerWithMissingQuestionIsHiddenFromNonAuthor() {
        Answer a = new Answer(identityId: "answerer", questionId: "gone")
        assert !AnswerVisibility.canView(a, null, "stranger", [Roles.USER])
    }

    @Test
    void adminCanViewAnyAnswer() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert AnswerVisibility.canView(a, privateQuestion(), "stranger", [Roles.ADMIN])
    }

    @Test
    void authorCanModifyOwnAnswer() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert AnswerVisibility.canModify(a, "answerer", [Roles.USER])
    }

    @Test
    void nonAuthorCannotModifyAnswer() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert !AnswerVisibility.canModify(a, "asker", [Roles.USER])
        assert !AnswerVisibility.canModify(a, "target", [Roles.USER])
        assert !AnswerVisibility.canModify(a, null, [Roles.USER])
    }

    @Test
    void adminAndInternalCanModifyAnswer() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert AnswerVisibility.canModify(a, "stranger", [Roles.ADMIN])
        assert AnswerVisibility.canModify(a, "stranger", [Roles.INTERNAL])
    }

    @Test
    void canModifyHandlesNullRolesSafely() {
        Answer a = new Answer(identityId: "answerer", questionId: "q1")
        assert !AnswerVisibility.canModify(a, "stranger", null)
    }
}
