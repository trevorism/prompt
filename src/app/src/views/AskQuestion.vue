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
      requestApproval: false,
      collapsed: false,
      askUser: '',
      userOptions: [],
      dueDate: null,
      errorMessage: '',
      loading: false
    }
  },
  methods: {
    handleSubmit: function () {
      if (this.text.length === 0) {
        this.errorMessage = 'Please enter a question'
        return
      }
      if (this.requestApproval && !this.askUser) {
        this.errorMessage = 'Select an approver for an approval request'
        return
      }
      this.errorMessage = ''
      const dueDateISO = this.dueDate ? new Date(this.dueDate).toISOString() : null;
      this.loading = true
      axios
        .post('api/question', {
          text: this.text,
          askChatGpt: this.askChatGpt,
          targetIdentityId: this.askUser,
          privateQuestion: this.privateQuestion,
          dueDate: dueDateISO,
          kind: this.requestApproval ? 'approval' : 'question'
        })
        .then(() => {
          this.errorMessage = ''
          this.loading = false
          this.$router.push('/')
        })
        .catch(() => {
          this.loading = false
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
    <div class="page-container">
      <h1 class="text-2xl font-bold text-slate-800 mb-1">Ask a question</h1>
      <p class="meta mb-5">Direct it to a specific user, or ask the whole team.</p>

      <div class="bg-white rounded-lg border border-slate-200 shadow-sm p-6">
        <va-form>
          <va-textarea class="w-full" v-model="text" label="Your question" :min-rows="3" />

          <VaCollapse v-model="collapsed" header="Advanced options" class="mt-4">
            <div class="grid gap-4 pt-3">
              <VaSelect
                class="max-w-sm"
                v-model="askUser"
                label="Ask a specific user"
                :options="userOptions"
                text-by="username"
                value-by="id"
                @update:modelValue="handleSelectChange"
              />
              <VaCheckbox v-model="privateQuestion" label="Private to the selected user" />
              <VaCheckbox v-model="requestApproval" label="Request approval (they respond Approve / Reject)" />
              <VaCheckbox v-model="askChatGpt" label="Also ask Chat-GPT" />
              <div class="flex items-center gap-3">
                <va-date-input class="max-w-xs" v-model="dueDate" label="Due date" mode="single" />
                <va-button v-if="dueDate" preset="plain" size="small" @click="clearDate">Clear</va-button>
              </div>
            </div>
          </VaCollapse>

          <div v-if="errorMessage.length > 0" class="text-red-600 text-sm mt-3">{{ errorMessage }}</div>

          <div class="flex justify-end gap-2 mt-6">
            <va-button preset="secondary" to="/">Cancel</va-button>
            <va-button type="submit" color="primary" @click="handleSubmit">
              <va-inner-loading :loading="loading">Submit</va-inner-loading>
            </va-button>
          </div>
        </va-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Keep unchecked checkboxes visible on the white card (Vuestic's default box is white). */
.va-checkbox {
  --va-checkbox-background: #e2e8f0; /* slate-200 */
}
</style>
