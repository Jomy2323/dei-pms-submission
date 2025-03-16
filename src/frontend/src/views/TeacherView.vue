<template>
  <div class="teacher-dashboard">
    <!-- Professor Info Card -->
    <v-card class="elevation-3">
      <v-card-title>Painel do Professor</v-card-title>
      <v-card-text>
        <div class="d-flex align-center">
          <v-avatar color="primary" size="64" class="mr-4">
            <v-icon size="36" color="white">mdi-account</v-icon>
          </v-avatar>
          <div>
            <h2 class="text-h5 mb-1">{{ teacher?.name || 'Professor' }}</h2>
            <div class="text-body-1 text-medium-emphasis">
              <v-icon small class="mr-1">mdi-email</v-icon>
              {{ teacher?.email || 'Sem email disponível' }}
            </div>
          </div>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import RemoteServices from '../services/RemoteService'
import { useAppearanceStore } from '../stores/appearance'
import DeiError from '../models/DeiError'

const appearance = useAppearanceStore()
const teacher = ref(null)
const loading = ref(false)

const loadTeacherData = async () => {
  loading.value = true
  appearance.loading = true

  try {
    teacher.value = await RemoteServices.getCurrentUser()
  } catch (error) {
    console.error('Erro ao carregar dados do professor:', error)
    appearance.pushError(new DeiError('Falha ao carregar dados do professor'))
  } finally {
    loading.value = false
    appearance.loading = false
  }
}

onMounted(loadTeacherData)
</script>

<style scoped>
.teacher-dashboard {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}
</style>