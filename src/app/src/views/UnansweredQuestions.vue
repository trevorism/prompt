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
      answered: false,
      questionList: [],
      answer: {}
    }
  },
  methods: {
    setAnswer(answerFromServer) {
      let self = this
      self.answer = answerFromServer
      self.answered = true
    }
  },
  mounted() {
    let self = this

    axios
      .get('/api/list/unanswered/')
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
      <div v-if="questionList.length === 0" class="empty-state">No unanswered questions right now.</div>
      <div v-for="question in questionList">
        <question
          :id="question.id"
          :date="question.createDate"
          :user="question.username"
          :text="question.text"
          :kind="question.kind"
          :due-date="question.dueDate"
          :answered="question.answered"
          :choices="question.choices"
          :allow-multiple-answers="question.allowMultipleAnswers"
          :answerMode="true"
          @answeredQuestion="setAnswer"
        ></question>
        <answer v-if="answered" :date="answer.answeredDate" :user="answer.username" :text="answer.text" :approved="answer.approved"></answer>
      </div>
    </div>
    <div v-else>You must login to view all unanswered questions.</div>
  </div>
</template>

<style scoped></style>
