<script setup>
import axios from 'axios'
import { ref } from 'vue'

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  text: {
    type: String,
    required: true
  },
  user: {
    type: String,
    required: true
  },
  date: {
    type: Number,
    required: true
  },
  answerMode: {
    type: Boolean,
    required: false,
    default: false
  }
})

const emit = defineEmits(['answeredQuestion'])

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const answerText = ref('')
const errorMessage = ref('')
const loading = ref(false)
const answerFormVisible = ref(props.answerMode)
const answerButtonVisible = ref(!props.answerMode)

const handleSubmit = () => {
  if (answerText.value.length === 0) {
    errorMessage.value = 'Please enter an answer'
    return
  }

  answerFormVisible.value = false
  answerButtonVisible.value = false
  loading.value = true
  axios
    .post('/api/question/' + props.id + '/answer', {
      text: answerText.value
    })
    .then((answer) => {
      answerFormVisible.value = false
      answerButtonVisible.value = true
      answerText.value = ''
      errorMessage.value = ''
      loading.value = false
      emit('answeredQuestion', answer.data)
    })
    .catch(() => {
      answerFormVisible.value = true
      answerButtonVisible.value = true
      loading.value = false
      errorMessage.value = 'Error submitting question'
    })
}

const handleCancel = () => {
  answerText.value = ''
  errorMessage.value = ''
  answerFormVisible.value = false
  answerButtonVisible.value = true
}
const showAnswerPrompt = () => {
  answerFormVisible.value = true
  answerButtonVisible.value = false
}
</script>

<template>
  <div>
    <va-card class="border-double border-4 border-indigo-600 m-4">
      <va-card-title>Question</va-card-title>
      <va-card-content class="text-lg">
        {{ text }}
      </va-card-content>
      <div class="text-right" v-if="answerButtonVisible">
        <va-button color="primary" class="m-4" @click="showAnswerPrompt">Answer Question</va-button>
      </div>
      <va-divider></va-divider>
      <div class="text-right text-base mr-8">
        <b>{{ user }}</b> asked on <b>{{ formatDate(date) }}</b>
      </div>
      <va-form v-if="answerFormVisible">
        <va-textarea
          class="block p-4 w-full text-base text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-red-500 focus:border-red-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-red-500 dark:focus:border-red-500"
          v-model="answerText"
          label="Your Answer"
        >
        </va-textarea>
        <div class="text-center w-full">
          <div v-if="errorMessage.length > 0" class="text-center text-red-600">{{ errorMessage }}</div>
        </div>
        <va-button-group class="w-full my-2 flex justify-center space-x-4">
          <va-button type="submit" color="primary" @click="handleSubmit">
            <va-inner-loading :loading="loading"> Submit </va-inner-loading>
          </va-button>
          <va-button color="danger" @click="handleCancel">
            <va-inner-loading :loading="loading"> Cancel </va-inner-loading>
          </va-button>
        </va-button-group>
      </va-form>
    </va-card>
  </div>
</template>

<style scoped></style>
