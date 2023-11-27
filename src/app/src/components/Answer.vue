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
</script>

<template>
  <va-card class="border border-red-400 ml-16 mt-8 mb-8 mr-8">
    <va-card-content class="text-2xl">
      {{ text }}
    </va-card-content>
    <va-divider></va-divider>
    <div class="text-right text-base mr-8">
      <b>{{ user }}</b> answered on <b>{{ date }}</b>
    </div>
  </va-card>
</template>

<style scoped>
</style>
