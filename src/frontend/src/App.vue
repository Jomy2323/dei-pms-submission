<template>
  <v-layout id="app">
    <TopBar />
    <v-main
      style="margin-top: 96px"
      class="pa-10 d-flex flex-column flex-grow-1 flex-shrink-1 text-center"
    >
      <v-card class="elevation-4 pa-8" width="100%">
        <ErrorMessage />
        <LoadingOverlay />

        <RouterView />
      </v-card>
    </v-main>
  </v-layout>
</template>

<script setup lang="ts">
import TopBar from '@/components/TopBar.vue'
import ErrorMessage from '@/components/ErrorMessage.vue';
import LoadingOverlay from '@/components/LoadingOverlay.vue';


import { RouterLink, RouterView } from 'vue-router'

import { useAppearanceStore } from './stores/appearance';
import { onMounted, onUnmounted } from 'vue';

const appearanceStore = useAppearanceStore();

const setWidth = () => (appearanceStore.windowWidth = window.innerWidth);
setWidth();
onMounted(() => {
  window.addEventListener('resize', setWidth);
  appearanceStore.clearErrors();

});
onUnmounted(() => {
  window.removeEventListener('resize', setWidth);
});
</script>

<style scoped>
#app {
  display: flex;
  flex-direction: column;
}
</style>
