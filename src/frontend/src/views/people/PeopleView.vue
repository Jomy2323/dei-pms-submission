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
      <v-chip v-else color="green" text-color="white">
        Aluno
      </v-chip>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon @click="editPerson(item)" class="mr-2">mdi-pencil</v-icon>
      <v-icon @click="deletePerson(item)">mdi-delete</v-icon>
    </template>

  </v-data-table>

</template>

<script setup lang="ts">
import type PeopleDto from '@/models/PeopleDto'
import RemoteService from '@/services/RemoteService'
import CreatePersonDialog from './CreatePersonDialog.vue'
import { reactive, ref } from 'vue'

let search = ref('')
let loading = ref(true)
const headers = [
  { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
  {
    title: 'Nome',
    key: 'name',
    value: 'name',
    sortable: true,
    filterable: true
  },
  {
    title: 'IST ID',
    key: 'istId',
    value: 'istId',
    sortable: true,
    filterable: true
  },
  {
    title: 'Tipo',
    key: 'type',
    value: 'type',
    sortable: true,
    filterable: true
  },
  {
    title: 'Ações',
    key: 'actions',
    value: 'actions',
    sortable: false,
    filterable: false
  }
  // TODO: maybe add another column with possible actions? (edit / delete)
]

const people: PeopleDto[] = reactive([])

getPeople()
async function getPeople() {
  people.splice(0, people.length)
  people.push(...(await RemoteService.getPeople()))
  loading.value = false
  console.log(people)
}

const editPerson = (person: PeopleDto) => {
  console.log('Editing person:', person)
}

const deletePerson = (person: PeopleDto) => {
  console.log('Deleting person:', person)
}


const fuzzySearch = (value: string, search: string) => {
  // Regex to match any character in between the search characters
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}

</script>
