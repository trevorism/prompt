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

    axios.get("/api/list/my/").then(result =>{
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
      <div v-for="item in questionList">
        <question :date="item.question.createDate" :user="item.question.identityId" :text="item.question.text"></question>
        <div v-for="answer in item.answers">
          <answer :date="answer.answeredDate" :user="answer.identityId" :text="answer.text"></answer>
        </div>
      </div>
    </div>
    <div v-else>
      You must login to view your questions.
    </div>
  </div>
</template>

<style scoped>

</style>