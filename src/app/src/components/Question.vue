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
  },
  choices: {
    type: Array,
    required: false,
    default: () => []
  },
  allowMultipleAnswers: {
    type: Boolean,
    required: false,
    default: false
  }
})

const emit = defineEmits(['answeredQuestion'])

const isApproval = computed(() => props.kind === 'approval')
const hasChoices = computed(() => props.choices.length > 0)
const overdue = computed(() => props.dueDate && !props.answered && new Date(props.dueDate) < new Date())
const accentClass = computed(() => (isApproval.value ? 'border-l-blue-500' : 'border-l-slate-300'))

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const answerText = ref('')
const selectedChoices = ref([])
const singleChoice = ref('')
const errorMessage = ref('')
const loading = ref(false)
const answerFormVisible = ref(props.answerMode)
const answerButtonVisible = ref(!props.answerMode)

const chosenValues = () => {
  if (!hasChoices.value) {
    return []
  }
  if (props.allowMultipleAnswers) {
    return selectedChoices.value
  }
  return singleChoice.value ? [singleChoice.value] : []
}

const submitResponse = (payload, emptyMessage) => {
  const chosen = chosenValues()
  if (hasChoices.value && chosen.length === 0) {
    errorMessage.value = props.allowMultipleAnswers
      ? 'Please select at least one option'
      : 'Please select an option'
    return
  }
  if (!hasChoices.value && payload.text.length === 0) {
    errorMessage.value = emptyMessage
    return
  }
  payload.selectedChoices = chosen

  answerFormVisible.value = false
  answerButtonVisible.value = false
  loading.value = true
  axios
    .post('/api/question/' + props.id + '/answer', payload)
    .then((answer) => {
      answerFormVisible.value = false
      answerButtonVisible.value = true
      answerText.value = ''
      selectedChoices.value = []
      singleChoice.value = ''
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
  const fallback = hasChoices.value ? '' : approved ? 'Approved' : 'Rejected'
  submitResponse({ text: answerText.value || fallback, approved: approved }, 'Please enter a reason')
}

const handleCancel = () => {
  answerText.value = ''
  selectedChoices.value = []
  singleChoice.value = ''
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
      <span class="kind-label">{{ isApproval ? 'Approval' : hasChoices ? 'Poll' : 'Question' }}</span>
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
      <fieldset v-if="hasChoices" class="mb-4">
        <legend class="kind-label mb-2">
          {{ allowMultipleAnswers ? 'Select all that apply' : 'Select one' }}
        </legend>
        <div class="flex flex-col gap-1">
          <template v-if="allowMultipleAnswers">
            <va-checkbox
              v-for="choice in choices"
              :key="choice.value"
              v-model="selectedChoices"
              :array-value="choice.value"
              :label="choice.label"
            />
          </template>
          <template v-else>
            <va-radio
              v-for="choice in choices"
              :key="choice.value"
              v-model="singleChoice"
              :option="choice.value"
              :label="choice.label"
              :name="'choice-' + id"
            />
          </template>
        </div>
      </fieldset>

      <va-textarea
        class="w-full"
        v-model="answerText"
        :label="hasChoices ? 'Comment (optional)' : isApproval ? 'Reason (optional)' : 'Your answer'"
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
