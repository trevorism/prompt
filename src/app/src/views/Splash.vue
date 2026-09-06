<script setup>
import { MenuBar } from '@trevorism/ui-header-bar'
import { useAuth } from '@trevorism/ui-auth'
import { ref } from 'vue'
import PendingQuestions from './PendingQuestions.vue'
import AllQuestions from './AllQuestions.vue'
import UnansweredQuestions from './UnansweredQuestions.vue'
import MyQuestions from './MyQuestions.vue'

const selectedTab = ref(0)
const { isAuthenticated, loading, login } = useAuth()
</script>

<template>
  <div>
    <menu-bar></menu-bar>
    <div class="page-container">
      <div class="flex items-start justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl font-bold text-slate-800">Prompt</h1>
          <p class="meta">Ask questions and request approvals across the team.</p>
        </div>
        <va-button v-if="isAuthenticated" color="primary" to="/ask">Ask a question</va-button>
        <va-button v-else-if="!loading" color="primary" @click="login()">Login</va-button>
      </div>

      <template v-if="isAuthenticated">
        <va-tabs v-model="selectedTab" grow>
          <va-tab> All </va-tab>
          <va-tab> Approvals </va-tab>
          <va-tab> Need an Answer </va-tab>
          <va-tab> Unanswered </va-tab>
          <va-tab> My Questions </va-tab>
        </va-tabs>
        <div class="mt-5">
          <div v-if="selectedTab === 0">
            <all-questions />
          </div>
          <div v-if="selectedTab === 1">
            <pending-questions endpoint="/api/list/approvals" empty-message="No approvals awaiting your decision." />
          </div>
          <div v-if="selectedTab === 2">
            <pending-questions empty-message="Nothing is waiting on you right now." />
          </div>
          <div v-if="selectedTab === 3">
            <unanswered-questions />
          </div>
          <div v-if="selectedTab === 4">
            <my-questions />
          </div>
        </div>
      </template>
      <div v-else-if="loading" class="empty-state">Checking your session…</div>
      <div v-else class="empty-state">Please log in to view and answer questions.</div>
    </div>
  </div>
</template>

<style scoped></style>
