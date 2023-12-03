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
})

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

</script>

<template>
  <va-card class="border border-red-400 ml-16 mt-4 mb-4 mr-4">
    <va-card-content class="text-base">
      {{ text }}
    </va-card-content>
    <va-divider></va-divider>
    <div class="text-right text-base mr-8">
      <b>{{ user }}</b> answered on <b>{{ formatDate(date) }}</b>
    </div>
  </va-card>
</template>

<style scoped>
</style>
