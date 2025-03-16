<template>
    <v-dialog v-model="dialog" max-width="400">
      <v-card>
        <v-card-title class="headline">Editar Pessoa</v-card-title>
        <v-card-text>
          <v-form ref="form">
            <v-text-field
              v-model="localPerson.name"
              label="Nome*"
              required
              :rules="[nameRequired, nameFormat]"
            ></v-text-field>
            <v-text-field
              v-model="localPerson.istId"
              label="IST ID*"
              required
              :rules="[istIdRequired, istIdFormat]"
            ></v-text-field>
            <v-text-field
              v-model="localPerson.email"
              label="Email*"
              required
              :rules="[emailRequired, emailFormat]"
            ></v-text-field>
            <v-select
              v-model="localPerson.type"
              :items="['Coordenador', 'Staff', 'Aluno', 'Professor', 'SC']"
              label="Categoria*"
              required
              :rules="[typeRequired]"
            ></v-select>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" @click="closeDialog">Cancelar</v-btn>
          <v-btn color="primary" text="Salvar" @click="saveEdit">Salvar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </template>
  
  <script setup lang="ts">
  import { ref, watch, nextTick } from 'vue'
  import RemoteService from '@/services/RemoteService'
  
  // Receive the person to edit and a flag to show/hide dialog
  const props = defineProps({
    person: { type: Object, required: true },
    showDialog: { type: Boolean, default: false }
  })
  const emit = defineEmits(['close', 'person-updated'])
  
  const dialog = ref(props.showDialog)
  const localPerson = ref({ ...props.person })
  
  // Validation rules (similar to create dialog)
  const nameRequired = (value: string) => !!value || 'O nome é obrigatório.'
  const nameFormat = (value: string) => /^[A-Za-zÀ-ÖØ-öø-ÿ\s]+$/.test(value) || 'O nome só pode conter letras e espaços.'
  const istIdRequired = (value: string) => !!value || 'O IST ID é obrigatório.'
  const istIdFormat = (value: string) => /^ist1\d+$/i.test(value) || "O IST ID deve começar com 'ist1' seguido de dígitos."
  const emailRequired = (value: string) => !!value || 'O email é obrigatório.'
  const emailFormat = (value: string) => /^[a-zA-Z0-9._%+-]+@tecnico\.ulisboa\.pt$/.test(value) || "O email deve terminar com @tecnico.ulisboa.pt"
  const typeRequired = (value: string) => !!value || 'A categoria é obrigatória.'
  
  const form = ref<any>(null)
  
  watch(() => props.showDialog, (newVal) => {
    dialog.value = newVal
    if (newVal) {
      // Reset form and local data when dialog opens
      localPerson.value = { ...props.person }
      nextTick(() => form.value?.resetValidation())
    }
  })
  
  const saveEdit = async () => {
    const valid = await form.value.validate()
    if (!valid || !valid.valid) return
  
    try {
      // Call the update API (assuming newPerson.type mapping is needed as in your create dialog)
      const updatedPerson = await RemoteService.updatePerson(localPerson.value.id, localPerson.value)
      emit('person-updated', updatedPerson)
      closeDialog()
    } catch (error) {
      // Handle error (e.g., show error message, not included here for brevity)
      console.error("Erro ao atualizar pessoa:", error)
    }
  }
  
  const closeDialog = () => {
    dialog.value = false
    emit('close')
  }
  </script>
  