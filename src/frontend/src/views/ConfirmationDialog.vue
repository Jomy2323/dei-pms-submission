<template>
    <v-dialog v-model="internalDialog" max-width="400">
      <v-card>
        <v-card-title class="headline">{{ title }}</v-card-title>
        <v-card-text>{{ message }}</v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="cancel">Cancel</v-btn>
          <v-btn color="primary" text @click="confirm">Confirm</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </template>
  
  <script setup lang="ts">
  import { ref, watch, defineProps, defineEmits } from 'vue'
  
  const props = defineProps({
    modelValue: { type: Boolean, required: true },
    title: { type: String, default: 'Confirmar' },
    message: { type: String, default: 'Tem a certeza que deseja prosseguir?' }
  })
  
  const emit = defineEmits(['update:modelValue', 'confirm'])
  
  const internalDialog = ref(props.modelValue)
  
  watch(() => props.modelValue, (val) => {
    internalDialog.value = val
  })
  
  watch(internalDialog, (val) => {
    emit('update:modelValue', val)
  })
  
  const cancel = () => {
    internalDialog.value = false
  }
  
  const confirm = () => {
    emit('confirm')
    internalDialog.value = false
  }
  </script>
  