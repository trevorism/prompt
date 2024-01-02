<script>
import axios from 'axios'
import Question from '../components/Question.vue'
import Answer from '../components/Answer.vue'
import { useCookies } from 'vue3-cookies'

export default {
  components: { Answer, Question },
  data() {
    return {
      authenticated: false,
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
    const { cookies } = useCookies()
    self.authenticated = !!cookies.get('session')

    axios
      .get('/api/list/pending/')
      .then((result) => {
        self.questionList = result.data
      })
      .catch(() => {
        self.authenticated = false
      })
  }
}
</script>

<template>
  <div>
    <div v-if="authenticated">
      <div v-for="question in questionList">
        <question
          :id="question.id"
          :date="question.createDate"
          :user="question.username"
          :text="question.text"
          :answerMode="true"
          @answeredQuestion="appendAnsweredQuestion"
        ></question>
        <answer v-if="answered" :date="answer.answeredDate" :user="answer.username" :text="answer.text"></answer>
      </div>
    </div>
    <div v-else>You must login to view your questions pending an answer.</div>
  </div>
</template>

<style scoped></style>
