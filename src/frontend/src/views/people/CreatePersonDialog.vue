<template>
  <div class="pa-4 text-center">
    <v-dialog v-model="dialog" max-width="400">
      <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-regular"
          prepend-icon="mdi-plus"
          text="Adicionar Pessoa"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
      </template>

      <v-card prepend-icon="mdi-account" title="Nova Pessoa">
        <v-card-text>
          <v-form ref="form">
            <v-text-field
              label="Nome*"
              required
              v-model="newPerson.name"
              :rules="[nameRequired, nameFormat]"
            ></v-text-field>

            <v-text-field
              label="IST ID*"
              required
              v-model="newPerson.istId"
              :rules="[istIdRequired, istIdFormat]"
              :error-messages="fieldError"
            ></v-text-field>

            <v-text-field
              label="Email*"
              required
              v-model="newPerson.email"
              :rules="[emailRequired, emailFormat]"
            ></v-text-field>

            <v-select
              :items="['Coordenador', 'Staff', 'Aluno', 'Professor', 'SC']"
              label="Categoria*"
              required
              v-model="newPerson.type"
              :rules="[typeRequired]"
            ></v-select>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text="Close" variant="plain" @click="dialog = false">Close</v-btn>
          <v-btn color="primary" text="Save" variant="tonal" @click="savePerson">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type PersonDto from "@/models/people/PersonDto";
import RemoteService from "@/services/RemoteService";

// Import DEBUG_MODE from environment variables
const DEBUG_MODE = import.meta.env.VITE_DEBUG_MODE === "true"; 

const dialog = ref(false);
const form = ref<any>(null);
const emit = defineEmits(["person-created"]);
const fieldError = ref<string | null>(null);

const typeMappings = {
  Coordenador: "COORDINATOR",
  Staff: "STAFF",
  Aluno: "STUDENT",
  Professor: "TEACHER",
  SC: "SC",
};

const newPerson = ref<PersonDto>({ name: "", istId: "", type: "" });

// Validation Rules
const nameRequired = (value: string) => !!value || "O nome é obrigatório.";
const nameFormat = (value: string) =>
  /^[A-Za-zÀ-ÖØ-öø-ÿ\s]+$/.test(value) || "O nome só pode conter letras e espaços.";

const istIdRequired = (value: string) => !!value || "O IST ID é obrigatório.";

const istIdFormat = async (value: string) => {
  if (/^ist1\d+$/.test(value)) return true;
  return "O IST ID deve ter o formato 'ist1xxxxxx'.";
};

const typeRequired = (value: string) => !!value || "A categoria é obrigatória.";

const emailRequired = (value: string) => !!value || "O email é obrigatório.";

const emailFormat = (value: string) =>
  /^[a-zA-Z0-9._%+-]+@tecnico\.ulisboa\.pt$/.test(value) || "O email deve terminar com @tecnico.ulisboa.pt";


const savePerson = async () => {
  fieldError.value = null;

  const isValid = await form.value?.validate();
  if (!isValid.valid) {
    return; 
  }

  const id_exists = await RemoteService.checkIfIstIdExists(newPerson.value.istId);
  if (DEBUG_MODE) console.log(`🔍 IST ID check result: ${id_exists}`);

  if (id_exists) {
    fieldError.value = "Este IST ID já está em uso!";
    return; 
  }

  const email_exists = await RemoteService.checkIfEmailExists(newPerson.value.email);
  if (DEBUG_MODE) console.log(`🔍 Email check result: ${email_exists}`);

  if (email_exists) {
    fieldError.value = "Este email já está registado!";
    return; 
  }

  try {
    
    newPerson.value.type = typeMappings[newPerson.value.type as keyof typeof typeMappings];

    if (DEBUG_MODE) console.log("💾 Saving new person:", newPerson.value);

    await RemoteService.createPerson(newPerson.value);

    newPerson.value = { name: '', istId: '', type: '' };
    form.value?.reset();
    emit('person-created');
    dialog.value = false;
  } catch (error) {
    fieldError.value = "Ocorreu um erro ao salvar a pessoa.";

    if (DEBUG_MODE) console.error("❌ Error saving person:", error);
  }
};

</script>
