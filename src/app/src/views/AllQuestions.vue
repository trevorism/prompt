<script>
import axios from 'axios'
import Question from '../components/Question.vue'
import Answer from '../components/Answer.vue'
import { useAuth } from '@trevorism/ui-auth'

export default {
  setup() {
    const { isAuthenticated } = useAuth()
    return { isAuthenticated }
  },
  components: { Answer, Question },
  data() {
    return {
      questionList: []
    }
  },
  methods: {
    appendAnsweredQuestion(answer) {
      let self = this
      let question = self.questionList.find((q) => q.question.id === answer.questionId)
      if (question) {
        question.answers.unshift(answer)
      }
    }
  },
  mounted() {
    let self = this

    axios
      .get('/api/list/question/')
      .then((result) => {
        self.questionList = result.data
      })
      .catch(() => {})
  }
}
</script>

<template>
  <div>
    <div v-if="isAuthenticated">
      <div v-if="questionList.length === 0" class="empty-state">No questions yet.</div>
      <div v-for="item in questionList">
        <question
          :id="item.question.id"
          :date="item.question.createDate"
          :user="item.question.username"
          :text="item.question.text"
          :kind="item.question.kind"
          :due-date="item.question.dueDate"
          :answered="item.question.answered"
          :choices="item.question.choices"
          :allow-multiple-answers="item.question.allowMultipleAnswers"
          :answerMode="false"
          @answeredQuestion="appendAnsweredQuestion"
        ></question>
        <div v-for="answer in item.answers">
          <answer :date="answer.answeredDate" :user="answer.username" :text="answer.text" :approved="answer.approved"></answer>
        </div>
      </div>
    </div>
    <div v-else>You must login to view all questions.</div>
  </div>
</template>

<style scoped></style>
