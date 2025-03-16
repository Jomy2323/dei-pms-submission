<template>
  <v-app-bar color="secondary" :height="36" class="px-2">
    <v-toolbar-items>
      <v-btn
        href="https://dei.tecnico.ulisboa.pt/"
        selected-class="no-active"
        class="dei-title"
        size="small"
      >
        Departamento de Engenharia Informática
      </v-btn>
    </v-toolbar-items>
    <v-spacer />
    <span>Current Role: {{ currentRole || 'Nenhuma' }}</span>
    <v-spacer />
    <v-toolbar-items class="align-center">
      <DarkModeSwitch />
    </v-toolbar-items>

    <v-toolbar-items class="ms-2">
      <v-btn size="small" @click="changeRole('student')">Aluno</v-btn>
      <v-btn size="small" @click="changeRole('coordinator')">Coordenador</v-btn>
      <v-btn size="small" @click="changeRole('staff')">Staff</v-btn>
      <v-btn size="small" @click="changeRole('sc')">SC</v-btn>
      <v-btn size="small" @click="changeRole('teacher')">Professor</v-btn>
    </v-toolbar-items>
    <v-toolbar-items class="ms-2">
      <v-btn size="small" variant="text" @click="logout">
        Terminar sessão
        <v-icon size="small" class="ms-1" icon="mdi-logout"></v-icon>
      </v-btn>
    </v-toolbar-items>

    <!-- Login Modal -->
    <LoginDialog
      v-if="showLoginDialog"
      :role="selectedRole"
      :showDialog="showLoginDialog"
      @success="handleLoginSuccess"
      @close="showLoginDialog = false"
    />
  </v-app-bar>
</template>

<script setup lang="ts">
import DarkModeSwitch from './DarkModeSwitch.vue'
import LoginDialog from '@/views/LoginDialog.vue'
import { useRoleStore } from '@/stores/role'
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const roleStore = useRoleStore()
const router = useRouter()

const currentRole = ref(roleStore.currentRole)
const selectedRole = ref('')

const showLoginDialog = ref(false)

const logout = () => {
  roleStore.logout() 
  router.push('/') 
}
const changeRole = (role: string) => {
  selectedRole.value = role.toLowerCase()
  showLoginDialog.value = true
}

const handleLoginSuccess = (user: any) => {
  roleStore.setRole(selectedRole.value, user)
  router.push(`/${selectedRole.value}`)
  showLoginDialog.value = false
}

watch(
  () => roleStore.currentRole,
  (newRole) => {
    currentRole.value = newRole
  }
)
</script>

<style scoped>
.dei-title:hover {
  background-color: transparent !important;
}
</style>
