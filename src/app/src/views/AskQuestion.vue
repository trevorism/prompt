<script setup>

import HeaderBar from "@trevorism/ui-header-bar";
</script>

<template>
  <div>
    <header-bar :local=false></header-bar>
    <va-button-group>
      <va-button color="primary" to="/"> Submit</va-button>
      <va-button color="danger" to="/"> Cancel</va-button>
    </va-button-group>
  </div>

</template>

<style scoped>

</style>