<template>
  <v-dialog 
    v-model="dialog" 
    max-width="400"
    persistent
    :retain-focus="false"
  >
    
    <v-card>
      <v-card-title class="headline">Login</v-card-title>
      <v-card-text>
        <v-form ref="form">
          <v-text-field
            v-model="istId"
            label="IST ID*"
            outlined
            required
            :rules="istIdRules"
          ></v-text-field>
        </v-form>
        
        <v-alert v-if="errorMessage" type="error" class="mt-2">
          {{ errorMessage }}
        </v-alert>
      </v-card-text>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancel" variant="plain" @click="closeDialog">
          Cancel
        </v-btn>
        <v-btn color="primary" text="Login" variant="tonal" @click="login">
          Login
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import RemoteService from '@/services/RemoteService'

const props = defineProps({
  role: { type: String, required: true },
  showDialog: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'success'])

// Local state
const dialog = ref(props.showDialog)
const form = ref<any>(null)
const istId = ref('')
const errorMessage = ref('')

// Vuetify validation rules for IST ID
const istIdRules = [
  (v: string) => !!v || 'O IST ID é obrigatório.',
  (v: string) =>
    /^ist1\d+$/i.test(v) || "O IST ID deve começar com 'ist1' seguido de dígitos."
]

// Ensure the dialog state reflects props and is reset when opened
watch(() => props.showDialog, (newVal) => {
  dialog.value = newVal
  if (newVal) {
    istId.value = ''
    errorMessage.value = ''
    nextTick(() => {
      form.value?.resetValidation()
    })
  }
})

// Ensure parent component is notified when dialog is closed
watch(() => dialog.value, (newVal) => {
  if (!newVal && props.showDialog) {
    emit('close')
  }
})

const login = async () => {
  // Validate the form first
  const valid = await form.value?.validate()
  if (!valid || !valid.valid) {
    // Form is invalid, do nothing—validation messages are shown automatically
    return
  }
  
  try {
    const user = await RemoteService.checkLogin(istId.value, props.role.toLowerCase())
    emit('success', user)
    closeDialog()
  } catch (error: any) {
    // Display server error message
    errorMessage.value = error.message
    // Reset the IST ID field and validation so user can re-enter
    istId.value = ''
    nextTick(() => {
      form.value?.resetValidation()
    })
  }
}

const closeDialog = () => {
  dialog.value = false
  istId.value = ''
  errorMessage.value = ''
  nextTick(() => {
    form.value?.resetValidation()
  })
  emit('close')
}
</script>