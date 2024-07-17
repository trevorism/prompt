<script setup>
import HeaderBar from "@trevorism/ui-header-bar";
import Question from "../components/Question.vue";

import {reactive, ref} from 'vue'
import axios from 'axios'
import { useCookies } from 'vue3-cookies'
import { useRouter } from 'vue-router';

const props = defineProps({
  id: {
    type: String,
    required: true
  }
});

const router = useRouter();
const { cookies } = useCookies()
const authenticated = ref(!!cookies.get('session'))
const question = reactive({data: null});
const renderable = ref(false)

axios.get('/api/list/' + props.id)
  .then((result) => {
    question.data = result.data
    renderable.value = authenticated.value
  })
  .catch(() => {
    renderable.value = false
  });

const answerSingleQuestion = () => {
  router.push('/');
}

</script>

<template>
  <div>
    <header-bar :local="false"></header-bar>
    <div v-if="renderable">
      <question
          :id="question.data.id"
          :date="question.data.createDate"
          :user="question.data.username"
          :text="question.data.text"
          :answerMode="true"
          @answeredQuestion="answerSingleQuestion"
      ></question>
    </div>
    <div v-else>You must login to view your question.</div>
  </div>
</template>

<style scoped></style>