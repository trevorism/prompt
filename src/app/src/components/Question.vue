<script setup>
import axios from 'axios'
import { computed, ref } from 'vue'

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
  },
  kind: {
    type: String,
    required: false,
    default: 'question'
  },
  dueDate: {
    type: [Number, String],
    required: false,
    default: null
  },
  answered: {
    type: Boolean,
    required: false,
    default: false
  }
})

const emit = defineEmits(['answeredQuestion'])

const isApproval = computed(() => props.kind === 'approval')
const overdue = computed(() => props.dueDate && !props.answered && new Date(props.dueDate) < new Date())

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const answerText = ref('')
const errorMessage = ref('')
const loading = ref(false)
const answerFormVisible = ref(props.answerMode)
const answerButtonVisible = ref(!props.answerMode)

const submitResponse = (payload, emptyMessage) => {
  if (payload.text.length === 0) {
    errorMessage.value = emptyMessage
    return
  }

  answerFormVisible.value = false
  answerButtonVisible.value = false
  loading.value = true
  axios
    .post('/api/question/' + props.id + '/answer', payload)
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
      errorMessage.value = 'Error submitting response'
    })
}

const handleSubmit = () => {
  submitResponse({ text: answerText.value }, 'Please enter an answer')
}

const handleDecision = (approved) => {
  submitResponse(
    { text: answerText.value || (approved ? 'Approved' : 'Rejected'), approved: approved },
    'Please enter a reason'
  )
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
      <va-card-title>
        {{ isApproval ? 'Approval Request' : 'Question' }}
        <va-chip v-if="overdue" color="danger" size="small" class="ml-2">{{ isApproval ? 'Expired' : 'Overdue' }}</va-chip>
      </va-card-title>
      <va-card-content class="text-lg">
        {{ text }}
      </va-card-content>
      <div class="text-right" v-if="answerButtonVisible">
        <va-button color="primary" class="m-4" @click="showAnswerPrompt">{{ isApproval ? 'Review Approval' : 'Answer Question' }}</va-button>
      </div>
      <va-divider></va-divider>
      <div class="text-right text-base mr-8">
        <b>{{ user }}</b> asked on <b>{{ formatDate(date) }}</b>
      </div>
      <va-form v-if="answerFormVisible">
        <va-textarea
          class="block p-4 w-full text-base text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-red-500 focus:border-red-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-red-500 dark:focus:border-red-500"
          v-model="answerText"
          :label="isApproval ? 'Reason (optional)' : 'Your Answer'"
        >
        </va-textarea>
        <div class="text-center w-full">
          <div v-if="errorMessage.length > 0" class="text-center text-red-600">{{ errorMessage }}</div>
        </div>
        <va-button-group v-if="isApproval" class="w-full my-2 flex justify-center space-x-4">
          <va-button color="success" @click="handleDecision(true)">
            <va-inner-loading :loading="loading"> Approve </va-inner-loading>
          </va-button>
          <va-button color="danger" @click="handleDecision(false)">
            <va-inner-loading :loading="loading"> Reject </va-inner-loading>
          </va-button>
          <va-button preset="secondary" @click="handleCancel">
            <va-inner-loading :loading="loading"> Cancel </va-inner-loading>
          </va-button>
        </va-button-group>
        <va-button-group v-else class="w-full my-2 flex justify-center space-x-4">
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
