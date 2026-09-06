<script setup>
import { MenuBar } from "@trevorism/ui-header-bar";
import { useAuth } from "@trevorism/ui-auth";
import Question from "../components/Question.vue";

import {reactive, ref} from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router';

const props = defineProps({
  id: {
    type: String,
    required: true
  }
});

const router = useRouter();
const { isAuthenticated: authenticated } = useAuth()
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
    <menu-bar></menu-bar>
    <div class="page-container">
    <div v-if="renderable">
      <question
          :id="question.data.id"
          :date="question.data.createDate"
          :user="question.data.username"
          :text="question.data.text"
          :kind="question.data.kind"
          :due-date="question.data.dueDate"
          :answered="question.data.answered"
          :choices="question.data.choices"
          :allow-multiple-answers="question.data.allowMultipleAnswers"
          :answerMode="true"
          @answeredQuestion="answerSingleQuestion"
      ></question>
    </div>
    <div v-else class="empty-state">You must login to view your question.</div>
    </div>
  </div>
</template>

<style scoped></style>