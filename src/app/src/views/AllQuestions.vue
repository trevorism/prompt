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
    const { cookies } = useCookies()
    self.authenticated = !!cookies.get('user_name')

    axios
      .get('/api/list/question/')
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
