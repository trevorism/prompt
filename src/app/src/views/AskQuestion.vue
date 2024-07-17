<script>
import HeaderBar from '@trevorism/ui-header-bar'
import axios from 'axios'
export default {
  name: 'AskQuestion',
  components: {
    HeaderBar
  },
  data() {
    return {
      text: '',
      askChatGpt: false,
      privateQuestion: false,
      collapsed: false,
      askUser: '',
      userOptions: [],
      dueDate: null,
      errorMessage: ''
    }
  },
  methods: {
    handleSubmit: function () {
      if (this.text.length === 0) {
        this.errorMessage = 'Please enter a question'
        return
      }
      this.errorMessage = ''
      const dueDateISO = this.dueDate ? new Date(this.dueDate).toISOString() : null;
      axios
        .post('api/question', {
          text: this.text,
          askChatGpt: this.askChatGpt,
          targetIdentityId: this.askUser,
          privateQuestion: this.privateQuestion,
          dueDate: dueDateISO
        })
        .then(() => {
          this.errorMessage = ''
          this.$router.push('/')
        })
        .catch(() => {
          this.errorMessage = 'Error submitting question'
        })
    },
    handleSelectChange(newValue) {
      if(this.askUser === null){
        this.privateQuestion = false
      }
    },
    clearDate() {
      this.dueDate = null
    }
  },
  mounted() {
    axios.get('api/user').then((result) => {
      this.userOptions = result.data
      this.userOptions.unshift({ id: null, username: 'Anyone' })
    });
  }
}
</script>

<template>
  <div>
    <header-bar :local="false"></header-bar>
    <div class="container px-8">
      <h1 class="text-xl font-bold text-center">What is your question?</h1>

      <va-form>
        <va-textarea
          class="block p-4 w-full text-base text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
          v-model="text"
          label="Your Question"
        >
        </va-textarea>
        <VaCollapse v-model="collapsed" header="Advanced Options" >
          <div class="border-2 border-gray-500 p-6">
            <div class="w-1/2"><VaCheckbox class="mt-4 w-full" v-model="askChatGpt" label="Ask Chat-GPT?" /></div>
            <div class="w-1/2"><VaSelect class="mt-4 w-full" v-model="askUser" label="Ask Specific User" :options="userOptions" text-by="username" value-by="id" @update:modelValue="handleSelectChange" /></div>
            <div class="w-1/2"><VaCheckbox class="mt-4 w-full" v-model="privateQuestion" label="Should the question be private to the selected user?" /></div>
            <div class="w-1/2 mt-4">Due Date: <va-date-input v-model="dueDate" mode="single" /> <va-chip color="warning" @click="clearDate">Clear</va-chip></div>
          </div>
        </VaCollapse>


        <div class="text-center w-full">
          <div v-if="errorMessage.length > 0" class="text-center text-red-600">{{ errorMessage }}</div>
        </div>
        <va-button-group class="w-full my-2 flex justify-center space-x-4">
          <va-button type="submit" color="primary" @click="handleSubmit">Submit</va-button>
          <va-button color="danger" to="/">Cancel</va-button>
        </va-button-group>
      </va-form>
    </div>
  </div>
</template>

<style scoped>
.va-checkbox {
  --va-checkbox-background: lightgray;
}
</style>
