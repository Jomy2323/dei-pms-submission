<template>
  <v-row align="center">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Pessoas</h2>
    </v-col>
    <v-col cols="auto">
      <CreatePersonDialog @person-created="getPeople" />
    </v-col>
  </v-row>

  <v-text-field
    v-model="search"
    label="Search"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    hide-details
    single-line
  ></v-text-field>

  <v-data-table
    :headers="headers"
    :items="people"
    :search="search"
    :loading="loading"
    :custom-filter="fuzzySearch"
    item-key="id"
    class="text-left"
    no-data-text="Sem pessoas a apresentar."
  >
    <template v-slot:[`item.type`]="{ item }">
      <v-chip v-if="item.type === 'COORDINATOR'" color="purple" text-color="white">
        Coordenador
      </v-chip>
      <v-chip v-else-if="item.type === 'STAFF'" color="red" text-color="white">
        Staff
      </v-chip>
      <v-chip v-else-if="item.type === 'TEACHER'" color="blue" text-color="white">
        Professor
      </v-chip>
      <v-chip v-else-if="item.type === 'SC'" color="orange" text-color="white">
        SC
      </v-chip>
      <v-chip v-else color="green" text-color="white">
        Aluno
      </v-chip>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon @click="editPerson(item)" class="mr-2">mdi-pencil</v-icon>
      <!-- Instead of directly calling deletePerson, call openDeleteDialog -->
      <v-icon @click="openDeleteDialog(item)">mdi-delete</v-icon>
    </template>
  </v-data-table>

  <!-- Edit Dialog -->
  <EditPersonDialog
    v-if="showEditDialog"
    :person="selectedPerson"
    :showDialog="showEditDialog"
    @close="showEditDialog = false"
    @person-updated="handlePersonUpdated"
  />

    <!-- Confirmation Dialog for Delete -->
    <ConfirmationDialog
    v-model="deleteDialog"
    title="Confirmar eliminação"
    :message="errorMessage || 'Tem a certeza que deseja eliminar esta pessoa?'"
    @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import type PeopleDto from '@/models/PeopleDto'
import RemoteService from '@/services/RemoteService'
import CreatePersonDialog from './CreatePersonDialog.vue'
import EditPersonDialog from './EditPersonDialog.vue'
import ConfirmationDialog from '../ConfirmationDialog.vue'
import { ref } from 'vue'

// Search, loading and headers
const search = ref('')
const loading = ref(true)
const headers = [
  { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
  { title: 'Nome', key: 'name', value: 'name', sortable: true, filterable: true },
  { title: 'IST ID', key: 'istId', value: 'istId', sortable: true, filterable: true },
  { title: 'Tipo', key: 'type', value: 'type', sortable: true, filterable: true },
  { title: 'Email', key: 'email', value: 'email', sortable: true, filterable: true },
  { title: 'Ações', key: 'actions', value: 'actions', sortable: false, filterable: false }
]

// Reactive people list as an array of PeopleDto
const people = ref<PeopleDto[]>([])

// Fetch people from backend
async function getPeople() {
  // Clear existing people and push the new list
  people.value = await RemoteService.getPeople()
  loading.value = false
  console.log(people.value)
}

getPeople()

// For editing
const selectedPerson = ref<PeopleDto | null>(null)
const showEditDialog = ref(false)

const editPerson = (person: PeopleDto) => {
  console.log('Editing person:', person)
  selectedPerson.value = { ...person }  // Make a copy
  showEditDialog.value = true
}

// For deletion
const deleteDialog = ref(false)
const personToDelete = ref<PeopleDto | null>(null)
const errorMessage = ref<string | null>(null);


const openDeleteDialog = (person: PeopleDto) => {
  personToDelete.value = person
  deleteDialog.value = true
}

const confirmDelete = async () => {
  if (!personToDelete.value) return;

  try {
    await RemoteService.deletePerson(personToDelete.value.id);
    people.value = people.value.filter(p => p.id !== personToDelete.value!.id);
    errorMessage.value = null; // Clear previous errors on success
  } catch (error: any) {
    console.error("❌ Error deleting person:", error);
    errorMessage.value = error.message; // Set the user-friendly error message
  } finally {
    deleteDialog.value = false; // Close the confirmation dialog
  }
};


// Fuzzy search function remains unchanged
const fuzzySearch = (value: string, search: string) => {
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}

// Update local list when a person is updated via edit dialog
const handlePersonUpdated = (updatedPerson: PeopleDto) => {
  const index = people.value.findIndex(p => p.id === updatedPerson.id)
  if (index !== -1) {
    people.value.splice(index, 1, updatedPerson)
  }
  showEditDialog.value = false
}
</script>
