<script setup>

import HeaderBar from "@trevorism/ui-header-bar";
import {ref} from "vue";
import PendingQuestions from "./PendingQuestions.vue";
import AllQuestions from "./AllQuestions.vue";
import UnansweredQuestions from "./UnansweredQuestions.vue";
import MyQuestions from "./MyQuestions.vue";
import {useCookies} from "vue3-cookies";

const selectedTab = ref(0);
const {cookies} = useCookies();
const authenticated = ref(!!cookies.get("session"));

</script>

<template>
  <div>
    <header-bar :local=false></header-bar>
    <div class="m-6">
      <div v-if="authenticated">
      <va-button color="violet-600" to="/ask">Ask A Question</va-button>
      </div>
      <div v-else>
        <va-button color="primary" href="https://login.auth.trevorism.com/?return_url=https://prompt.action.trevorism.com"> Login </va-button>
      </div>
      <va-tabs v-model="selectedTab">
        <va-tab> Need an Answer </va-tab>
        <va-tab> All Unanswered Questions </va-tab>
        <va-tab> My Questions </va-tab>
        <va-tab> All Questions </va-tab>
      </va-tabs>
      <div class="border border-gray-300 rounded-xl p-6">
        <div v-if="selectedTab === 0">
          <pending-questions />
        </div>
        <div v-if="selectedTab === 1">
          <unanswered-questions />
        </div>
        <div v-if="selectedTab === 2">
          <my-questions />
        </div>
        <div v-if="selectedTab === 3">
          <all-questions />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>