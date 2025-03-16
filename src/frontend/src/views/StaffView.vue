<template>
  <div class="staff-dashboard">
    <h1 class="text-h4 mb-4">Staff Dashboard</h1>
    <v-card-text>
    <div class="d-flex align-center mb-4">
      <!-- Staff Avatar -->
      <v-avatar color="secondary" size="64" class="mr-4">
        <v-icon size="36" color="white">mdi-account-tie</v-icon>
      </v-avatar>

      <div>
        <!-- Staff Name -->
        <h2 class="text-h5 mb-1">{{ staff?.name || 'Staff Member' }}</h2>

        <!-- Staff Email -->
        <div class="text-body-1 text-medium-emphasis mb-1">
          <v-icon small class="mr-1">mdi-email</v-icon>
          {{ staff?.email || 'No email available' }}
        </div>

        <!-- Staff IST ID -->
        <div class="text-body-1 text-medium-emphasis">
          <v-icon small class="mr-1">mdi-card-account-details</v-icon>
          IST ID: {{ staff?.istId || 'Not available' }}
        </div>
      </div>
    </div>
  </v-card-text>

    <!-- People Management Section -->
    <v-card class="mb-6 elevation-3">
      <v-card-title class="d-flex align-center">
        <span>People Management</span>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="openNewPersonDialog">
          <v-icon left>mdi-account-plus</v-icon>
          Add Person
        </v-btn>
      </v-card-title>

      <v-card-text>
        <v-text-field
          v-model="peopleSearch"
          prepend-icon="mdi-magnify"
          label="Search People"
          single-line
          hide-details
          class="mb-4"
        ></v-text-field>

        <v-data-table
          :headers="peopleHeaders"
          :items="filteredPeople"
          :search="peopleSearch"
          :loading="peopleLoading"
          item-value="id"
          class="elevation-1"
        >
          <template v-slot:item.type="{ item }">
            <v-chip
              :color="getTypeColor(item.type)"
              text-color="white"
              size="small"
            >
              {{ item.type }}
            </v-chip>
          </template>

          <template v-slot:item.actions="{ item }">
            <v-btn
              icon
              variant="text"
              color="blue"
              @click="editPerson(item)"
              class="mr-1"
            >
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn
              icon
              variant="text"
              color="red"
              @click="confirmDeletePerson(item)"
            >
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- Thesis Workflows to Submit to Fenix -->
    <v-card class="mb-6 elevation-3">
      <v-card-title>
        <span>Thesis Documents to Submit to Fenix</span>
      </v-card-title>

      <v-data-table
        :headers="thesisHeaders"
        :items="thesesToSubmit"
        :loading="thesisLoading"
        class="elevation-1"
      >
        <template v-slot:item.status="{ item }">
          <v-chip color="amber" text-color="white" size="small">
            {{ getDisplayStatus(item.status) }}
          </v-chip>
        </template>
        
        <template v-slot:item.student="{ item }">
          {{ item.student?.name || 'N/A' }}
        </template>
        
        <template v-slot:item.actions="{ item }">
          <v-btn
            color="success"
            size="small"
            @click="submitToFenix(item)"
            :disabled="item.status !== 'DOCUMENT_SIGNED'"
          >
            <v-icon left>mdi-check-circle</v-icon>
            Submit to Fenix
          </v-btn>
          <v-btn
            color="primary"
            size="small"
            variant="text"
            class="ml-2"
            @click="viewThesisDetails(item)"
          >
            <v-icon>mdi-eye</v-icon>
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- Add/Edit Person Dialog -->
    <v-dialog v-model="personDialog" max-width="600px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ editMode ? 'Edit Person' : 'Add New Person' }}</span>
        </v-card-title>

        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="editedPerson.name"
                  label="Name*"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="editedPerson.istId"
                  label="IST ID*"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="editedPerson.email"
                  label="Email*"
                  type="email"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-select
                  v-model="editedPerson.type"
                  :items="personTypes"
                  label="Type*"
                  required
                ></v-select>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="closePersonDialog">
            Cancel
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="savePerson">
            Save
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h5">
          Confirm Deletion
        </v-card-title>
        <v-card-text>
          Are you sure you want to delete {{ personToDelete?.name }}? This action cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="deleteDialog = false">
            Cancel
          </v-btn>
          <v-btn color="red-darken-1" variant="text" @click="deletePerson">
            Delete
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- View Thesis Details Dialog -->
    <v-dialog v-model="thesisDetailsDialog" max-width="800px">
      <v-card v-if="selectedThesis">
        <v-card-title class="text-h5">
          Thesis Details
        </v-card-title>
        <v-card-text>
          <v-list>
            <v-list-item>
              <template v-slot:prepend>
                <v-icon color="primary">mdi-format-title</v-icon>
              </template>
              <v-list-item-title>Title</v-list-item-title>
              <v-list-item-subtitle>{{ selectedThesis.title }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
              <template v-slot:prepend>
                <v-icon color="primary">mdi-account</v-icon>
              </template>
              <v-list-item-title>Student</v-list-item-title>
              <v-list-item-subtitle>{{ selectedThesis.student?.name || 'N/A' }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item>
              <template v-slot:prepend>
                <v-icon color="primary">mdi-clock-outline</v-icon>
              </template>
              <v-list-item-title>Status</v-list-item-title>
              <v-list-item-subtitle>{{ getDisplayStatus(selectedThesis.status) }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-subheader>Jury Members</v-list-subheader>
            <v-list-item v-for="member in selectedThesis.juryMembers" :key="member.id">
              <template v-slot:prepend>
                <v-icon color="secondary" v-if="member.isPresident">mdi-crown</v-icon>
                <v-icon v-else>mdi-account-tie</v-icon>
              </template>
              <v-list-item-title>{{ member.name }}</v-list-item-title>
              <v-list-item-subtitle>{{ member.email }}</v-list-item-subtitle>
            </v-list-item>
            
            <v-list-item v-if="selectedThesis.signedDocument">
              <template v-slot:prepend>
                <v-icon color="green">mdi-file-document-check</v-icon>
              </template>
              <v-list-item-title>Signed Document</v-list-item-title>
              <v-list-item-subtitle>
                <v-btn color="primary" size="small" variant="text" @click="downloadDocument(selectedThesis)">
                  <v-icon left>mdi-download</v-icon>
                  Download
                </v-btn>
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="thesisDetailsDialog = false">
            Close
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Submission Confirmation Dialog -->
    <v-dialog v-model="confirmSubmitDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h5">
          Confirm Submission
        </v-card-title>
        <v-card-text>
          Are you sure you want to submit this thesis to Fenix?
          <div class="mt-3">
            <strong>Thesis:</strong> {{ thesisToSubmit?.title || 'Unknown thesis' }}
          </div>
          <div class="mt-1">
            <strong>Student:</strong> {{ thesisToSubmit?.student?.name || 'Unknown student' }}
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="confirmSubmitDialog = false">
            Cancel
          </v-btn>
          <v-btn 
            color="success" 
            @click="confirmSubmitToFenix"
          >
            Confirm Submission
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import RemoteServices from '../services/RemoteService';
import { useAppearanceStore } from '../stores/appearance';
import DeiError from '../models/DeiError';
import type PersonDto from '../models/PersonDto';

// Access store
const appearance = useAppearanceStore();

// People management
const staff = ref(null);
const people = ref<PersonDto[]>([]);
const peopleLoading = ref(false);
const peopleSearch = ref('');
const personDialog = ref(false);
const editMode = ref(false);
const editedPerson = ref<any>({
  name: '',
  istId: '',
  email: '',
  type: ''
});
const personTypes = ['STAFF', 'STUDENT', 'PROFESSOR', 'COORDINATOR', 'SC'];
const deleteDialog = ref(false);
const personToDelete = ref<PersonDto | null>(null);

// Thesis management
const thesesToSubmit = ref([]);
const thesisLoading = ref(false);
const thesisDetailsDialog = ref(false);
const selectedThesis = ref(null);
const confirmSubmitDialog = ref(false);
const thesisToSubmit = ref(null);

const peopleHeaders = [
  { title: 'Name', key: 'name', align: 'start', sortable: true },
  { title: 'IST ID', key: 'istId', align: 'start', sortable: true },
  { title: 'Email', key: 'email', align: 'start', sortable: true },
  { title: 'Type', key: 'type', align: 'start', sortable: true },
  { title: 'Actions', key: 'actions', align: 'center', sortable: false }
];

// Thesis management
const thesisHeaders = [
  { title: 'Thesis Title', key: 'title', align: 'start', sortable: true },
  { title: 'Student', key: 'student', align: 'start', sortable: true },
  { title: 'Status', key: 'status', align: 'center', sortable: true },
  { title: 'Actions', key: 'actions', align: 'center', sortable: false }
];

// Status mapping
const statusMapping = {
  'PROPOSAL_SUBMITTED': 'Proposta de Júri Submetida',
  'APPROVED_BY_SC': 'Aprovado pelo SC',
  'JURY_PRESIDENT_ASSIGNED': 'Presidente do Júri Atribuído',
  'DOCUMENT_SIGNED': 'Documento Assinado',
  'SUBMITTED_TO_FENIX': 'Submetido ao Fenix',
  'DEFENSE_SCHEDULED': 'Defesa Agendada',
  'UNDER_REVIEW': 'Em Revisão'
};

// Helper function to convert API status to display status
const getDisplayStatus = (status: string) => {
  return statusMapping[status] || status;
};

// Filtered people based on search
const filteredPeople = computed(() => {
  return people.value;
});

// Type colors for badges
const getTypeColor = (type: string) => {
  const colors = {
    'STAFF': 'indigo',
    'STUDENT': 'green',
    'PROFESSOR': 'blue',
    'COORDINATOR': 'purple',
    'SC': 'orange'
  };
  return colors[type] || 'grey';
};

// Load all people
const loadPeople = async () => {
  peopleLoading.value = true;
  appearance.loading = true;
  
  try {
    const result = await RemoteServices.getPeople();
    people.value = result;
  } catch (error) {
    console.error('Error loading people:', error);
    appearance.pushError(new DeiError('Failed to load people. Please try again.'));
  } finally {
    peopleLoading.value = false;
    appearance.loading = false;
  }
};

// Load theses that need to be submitted to Fenix
const loadThesesToSubmit = async () => {
  thesisLoading.value = true;
  
  try {
    // Get theses with document signed status
    const result = await RemoteServices.getThesesByStatus('DOCUMENT_SIGNED');
    
    // Fetch full details for each thesis
    const detailedTheses = await Promise.all(
      (result || []).map(async (thesis) => {
        try {
          const fullThesis = await RemoteServices.getThesisById(thesis.id);
          
          // Fetch student information
          if (fullThesis.studentId) {
            fullThesis.student = await RemoteServices.getPersonById(fullThesis.studentId);
          }
          
          // Fetch jury members
          if (fullThesis.juryMemberIds && fullThesis.juryMemberIds.length > 0) {
            const professors = await RemoteServices.getProfessors();
            fullThesis.juryMembers = professors.filter(prof => 
              fullThesis.juryMemberIds.includes(prof.id)
            );
            
            // Mark jury president if assigned
            if (fullThesis.juryPresidentId) {
              fullThesis.juryMembers.forEach(member => {
                member.isPresident = member.id === fullThesis.juryPresidentId;
              });
            }
          }
          
          return fullThesis;
        } catch (error) {
          console.error(`Error fetching details for thesis ${thesis.id}:`, error);
          return thesis;
        }
      })
    );
    
    thesesToSubmit.value = detailedTheses;
  } catch (error) {
    console.error('Error loading theses to submit:', error);
    appearance.pushError(new DeiError('Failed to load theses. Please try again.'));
  } finally {
    thesisLoading.value = false;
  }
};

// Person dialog functions
const openNewPersonDialog = () => {
  editMode.value = false;
  editedPerson.value = {
    name: '',
    istId: '',
    email: '',
    type: 'STUDENT' // Default type
  };
  personDialog.value = true;
};

const editPerson = (person: PersonDto) => {
  editMode.value = true;
  editedPerson.value = { ...person };
  personDialog.value = true;
};

const closePersonDialog = () => {
  personDialog.value = false;
};

const savePerson = async () => {
  // Validate required fields
  if (!editedPerson.value.name || !editedPerson.value.istId || !editedPerson.value.email || !editedPerson.value.type) {
    appearance.pushError(new DeiError('Por favor, preencha todos os campos obrigatórios.'));
    return;
  }

  appearance.loading = true;

  try {
    if (editMode.value && editedPerson.value.id) {
      // Update existing person
      // Pass the ID separately and the person data as the second argument
      await RemoteServices.updatePerson(
        editedPerson.value.id, 
        editedPerson.value
      );
    } else {
      // Create new person
      await RemoteServices.createPerson(editedPerson.value);
    }

    personDialog.value = false;
    await loadPeople();
  } catch (error) {
    console.error('Error saving person:', error);
    appearance.pushError(new DeiError('Falha ao salvar pessoa. Por favor, tente novamente.'));
  } finally {
    appearance.loading = false;
  }
};
// Delete person functions
const confirmDeletePerson = (person: PersonDto) => {
  personToDelete.value = person;
  deleteDialog.value = true;
};

const deletePerson = async () => {
  if (!personToDelete.value) return;
  
  appearance.loading = true;
  
  try {
    await RemoteServices.deletePerson(personToDelete.value.id);
    deleteDialog.value = false;
    await loadPeople();
  } catch (error) {
    console.error('Error deleting person:', error);
    appearance.pushError(new DeiError('Failed to delete person. Please try again.'));
  } finally {
    appearance.loading = false;
  }
};

// Thesis functions
const viewThesisDetails = (thesis) => {
  selectedThesis.value = thesis;
  thesisDetailsDialog.value = true;
};

// Show confirmation dialog for thesis submission
const submitToFenix = (thesis) => {
  thesisToSubmit.value = thesis;
  confirmSubmitDialog.value = true;
};

// Actual thesis submission after confirmation
const confirmSubmitToFenix = async () => {
  if (!thesisToSubmit.value) return;
  
  appearance.loading = true;
  
  try {
    await RemoteServices.submitToFenix(thesisToSubmit.value.id);
    
    // Close both dialogs
    confirmSubmitDialog.value = false;
    thesisDetailsDialog.value = false;
    
    // Show success message
    
    // Refresh the data
    await loadThesesToSubmit();
  } catch (error) {
    console.error('Error submitting thesis to Fenix:', error);
    appearance.pushError(new DeiError('Failed to submit thesis to Fenix. Please try again.'));
  } finally {
    appearance.loading = false;
  }
};

const downloadDocument = (thesis) => {
  // This would usually download the signed document
  // For demonstration purposes, show a message
};

// Initialize component
onMounted(async () => {
  // Get current logged in staff member info
  staff.value = await RemoteServices.getCurrentUser();
  
  // Then load other data
  await loadPeople();
  await loadThesesToSubmit();
});
</script>

<style scoped>
.staff-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>