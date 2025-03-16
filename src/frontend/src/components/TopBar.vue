<template>
  <UtilBar />
</template>

<script setup lang="ts">
import UtilBar from '@/components/UtilBar.vue'

const navbarItems = [
  { name: 'Pessoal', path: '/people', icon: 'mdi-account-group' },
  { name: 'Estatísticas', path: '/statistics', icon: 'mdi-chart-bar' },
]
</script>
