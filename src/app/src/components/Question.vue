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
    type: String,
    required: true
  },
})

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

</script>

<template>
  <va-card class="border-double border-4 border-indigo-600 m-8">
    <va-card-title>Question</va-card-title>
    <va-card-content class="text-2xl">
      {{ text }}
    </va-card-content>
      <va-divider></va-divider>
    <div class="text-right text-base mr-8">
      <b>{{ user }}</b> asked on <b>{{ formatDate(date) }}</b>
    </div>
  </va-card>
</template>

<style scoped>
</style>
