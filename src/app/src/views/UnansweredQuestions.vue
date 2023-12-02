<script>
import axios from "axios";
import Question from "../components/Question.vue";
import Answer from "../components/Answer.vue";
import { useCookies } from "vue3-cookies";

export default {
  components: {Answer, Question},
  data () {
    return {
      authenticated: false,
      questionList : []
    }
  },
  methods: {

  },
  mounted () {
    let self = this
    const { cookies } = useCookies();
    self.authenticated = !! cookies.get("session")

    axios.get("/api/list/unanswered/").then(result =>{
      self.questionList = result.data;
    }).catch(() => {
      self.authenticated = false
    })
  }
}
</script>

<template>
  <div>
    <div v-if="authenticated">
      <div v-for="question in questionList">
        <question :date="question.createDate" :user="question.username" :text="question.text"></question>
      </div>
    </div>
    <div v-else>
      You must login to view all unanswered questions.
    </div>
  </div>
</template>

<style scoped>

</style>