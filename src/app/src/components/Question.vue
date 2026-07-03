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
const accentClass = computed(() => (isApproval.value ? 'border-l-blue-500' : 'border-l-slate-300'))

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
  <div class="bg-white rounded-lg border border-slate-200 border-l-4 shadow-sm p-5 mb-4" :class="accentClass">
    <div class="flex items-center justify-between">
      <span class="kind-label">{{ isApproval ? 'Approval' : 'Question' }}</span>
      <va-chip v-if="overdue" color="danger" size="small">{{ isApproval ? 'Expired' : 'Overdue' }}</va-chip>
    </div>

    <p class="text-lg text-slate-800 mt-2 mb-3 whitespace-pre-line">{{ text }}</p>

    <div class="flex items-center justify-between flex-wrap gap-2">
      <span class="meta"><b class="text-slate-600">{{ user }}</b> · {{ formatDate(date) }}</span>
      <va-button v-if="answerButtonVisible" preset="primary" size="small" @click="showAnswerPrompt">
        {{ isApproval ? 'Review approval' : 'Answer' }}
      </va-button>
    </div>

    <va-form v-if="answerFormVisible" class="mt-4">
      <va-textarea
        class="w-full"
        v-model="answerText"
        :label="isApproval ? 'Reason (optional)' : 'Your answer'"
      />
      <div v-if="errorMessage.length > 0" class="text-red-600 text-sm mt-1">{{ errorMessage }}</div>

      <div v-if="isApproval" class="flex justify-end gap-2 mt-3">
        <va-button color="success" size="small" @click="handleDecision(true)">
          <va-inner-loading :loading="loading"> Approve </va-inner-loading>
        </va-button>
        <va-button color="danger" size="small" @click="handleDecision(false)">
          <va-inner-loading :loading="loading"> Reject </va-inner-loading>
        </va-button>
        <va-button preset="secondary" size="small" @click="handleCancel"> Cancel </va-button>
      </div>
      <div v-else class="flex justify-end gap-2 mt-3">
        <va-button type="submit" color="primary" size="small" @click="handleSubmit">
          <va-inner-loading :loading="loading"> Submit </va-inner-loading>
        </va-button>
        <va-button preset="secondary" size="small" @click="handleCancel"> Cancel </va-button>
      </div>
    </va-form>
  </div>
</template>

<style scoped></style>
