<script setup>
defineProps({
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
  approved: {
    type: Boolean,
    required: false,
    default: null
  }
})

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}
</script>

<template>
  <div class="ml-8 mb-4 bg-white rounded-lg border border-slate-200 border-l-4 border-l-emerald-400 shadow-sm p-4">
    <div class="flex items-center gap-2">
      <span class="kind-label">Answer</span>
      <va-chip v-if="approved === true" color="success" size="small">Approved</va-chip>
      <va-chip v-if="approved === false" color="danger" size="small">Rejected</va-chip>
    </div>
    <p class="text-slate-800 mt-2 mb-2 whitespace-pre-line">{{ text }}</p>
    <div class="meta"><b class="text-slate-600">{{ user }}</b> answered · {{ formatDate(date) }}</div>
  </div>
</template>

<style scoped></style>
