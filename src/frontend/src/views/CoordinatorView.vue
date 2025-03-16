<template>
  <div class="coordinator-dashboard">
    <!-- Coordinator Info Section -->
    <v-card class="mb-6 elevation-3">
      <v-card-title>Painel de Coordenador</v-card-title>
      <v-card-text>
        <div class="d-flex align-center mb-4">
          <v-avatar color="primary" size="64" class="mr-4">
            <v-icon size="36" color="white">mdi-account</v-icon>
          </v-avatar>
          <div>
            <h2 class="text-h5 mb-1">{{ coordinator?.name || 'Coordinator' }}</h2>
            <div class="text-body-1 text-medium-emphasis mb-1">
              <v-icon small class="mr-1">mdi-email</v-icon>
              {{ coordinator?.email || 'No email available' }}
            </div>
            <div class="text-body-1 text-medium-emphasis">
              <v-icon small class="mr-1">mdi-card-account-details</v-icon>
              IST ID: {{ coordinator?.istId || 'Not available' }}
            </div>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <!-- Thesis Workflows Section -->
    <v-card class="mb-6 elevation-3">
      <v-card-title>Workflows de Tese</v-card-title>

      <v-data-table
        :headers="thesisHeaders"
        :items="theses"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.status="{ item }">
          <v-chip
            :color="getStatusColor(item.status)"
            text-color="white"
            size="small"
          >
            {{ getDisplayStatus(item.status) }}
          </v-chip>
        </template>

        <template v-slot:item.student="{ item }">
          {{ item.student?.name || 'N/A' }}
        </template>

        <template v-slot:item.actions="{ item }">
          <v-btn
            color="primary"
            size="small"
            @click="handleThesisAction(item)"
            :disabled="!canTakeAction(item.status)"
          >
            {{ getActionLabel(item.status) }}
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- Defense Workflows Section -->
    <v-card class="mb-6 elevation-3">
      <v-card-title>Workflows de Defesa</v-card-title>

      <v-data-table
        :headers="defenseHeaders"
        :items="defenses"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.status="{ item }">
          <v-chip
            :color="getStatusColor(item.status)"
            text-color="white"
            size="small"
          >
            {{ getDisplayStatus(item.status) }}
          </v-chip>
        </template>

        <template v-slot:item.student="{ item }">
          {{ item.student?.name || 'N/A' }}
        </template>

        <template v-slot:item.actions="{ item }">
          <v-btn
            color="primary"
            size="small"
            @click="handleDefenseAction(item)"
            :disabled="!canTakeDefenseAction(item.status)"
          >
            {{ getDefenseActionLabel(item.status) }}
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- Thesis Action Dialog -->
    <v-dialog v-model="thesisActionDialog" max-width="500px">
      <v-card v-if="selectedThesis">
        <v-card-title>
          {{ getActionDialogTitle(selectedThesis.status) }}
        </v-card-title>

        <v-card-text>
          <div class="d-flex flex-column">
            <div class="mb-4">
              <strong>Tese:</strong> {{ selectedThesis.title }}
            </div>
            <div class="mb-4">
              <strong>Estudante:</strong> {{ selectedThesis.student?.name || 'N/A' }}
            </div>

            <!-- Select Jury President if status is APPROVED_BY_SC -->
            <div v-if="selectedThesis.status === 'APPROVED_BY_SC'" class="mb-4">
              <v-select
                v-model="selectedJuryPresident"
                :items="juryMembers"
                item-title="name"
                item-value="id"
                label="Select Jury President"
                return-object
                required
              ></v-select>
            </div>

            <!-- Document Signing confirmation if status is JURY_PRESIDENT_ASSIGNED -->
            <div v-if="selectedThesis.status === 'JURY_PRESIDENT_ASSIGNED'" class="mb-4">
              <p class="text-body-1 mb-2">Por favor confirme que já assinou o documento.</p>
              <v-checkbox
                v-model="documentSigned"
                label="Confirmo que assinei o documento"
                hide-details
              ></v-checkbox>
            </div>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="thesisActionDialog = false">
            Cancelar
          </v-btn>
          <v-btn
            color="primary"
            @click="confirmThesisAction"
            :disabled="!canConfirmAction"
          >
            Confirmar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Defense Action Dialog -->
    <v-dialog v-model="defenseActionDialog" max-width="500px">
      <v-card v-if="selectedDefense">
        <v-card-title>
          {{ getDefenseActionDialogTitle(selectedDefense.status) }}
        </v-card-title>

        <v-card-text>
          <div class="d-flex flex-column">
            <div class="mb-4">
              <strong>Tese:</strong> {{ selectedDefense.title || selectedDefense.thesis?.title }}
            </div>
            <div class="mb-4">
              <strong>Student:</strong> {{ selectedDefense.student?.name || 'N/A' }}
            </div>

            <!-- Schedule Defense if status is UNSCHEDULED -->
            <div v-if="selectedDefense.status === 'UNSCHEDULED'" class="mb-4">
              <v-text-field
                v-model="defenseDate"
                label="Data e Hora de defesa"
                type="datetime-local"
                :min="minDefenseDate"
                :error-messages="defenseDateError"
                required
              ></v-text-field>
              <p class="text-caption text-medium-emphasis">
                Defense date must be in the future
              </p>
            </div>

            <!-- Grade input if status is UNDER_REVIEW -->
            <div v-if="selectedDefense.status === 'UNDER_REVIEW'" class="mb-4">
              <v-slider
                v-model="defenseGrade"
                :min="0"
                :max="20"
                :step="0.1"
                thumb-label
                label="Nota Final"
              ></v-slider>
              <div class="text-center">
                <strong>Grade:</strong> {{ defenseGrade.toFixed(1) }}
              </div>
            </div>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="defenseActionDialog = false">
            Cancel
          </v-btn>
          <v-btn
            color="primary"
            @click="confirmDefenseAction"
            :disabled="!canConfirmDefenseAction"
          >
            Confirmar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import RemoteServices from '../services/RemoteService';
import { useAppearanceStore } from '../stores/appearance';
import DeiError from '../models/DeiError';

// Access store
const appearance = useAppearanceStore();

// Status mapping
const statusMapping = {
  'PROPOSAL_SUBMITTED': 'Proposta de Júri Submetida',
  'APPROVED_BY_SC': 'Aprovado pelo SC',
  'JURY_PRESIDENT_ASSIGNED': 'Presidente do Júri Atribuído',
  'DOCUMENT_SIGNED': 'Documento Assinado',
  'SUBMITTED_TO_FENIX': 'Submetido ao Fenix',
  'DEFENSE_SCHEDULED': 'Defesa Agendada',
  'UNDER_REVIEW': 'Em Revisão',
  'UNSCHEDULED': 'Por Agendar'
};

// Data refs
const coordinator = ref(null);
const loading = ref(false);
const theses = ref([]);
const defenses = ref([]);
const thesisActionDialog = ref(false);
const defenseActionDialog = ref(false);
const selectedThesis = ref(null);
const selectedDefense = ref(null);
const selectedJuryPresident = ref(null);
const documentSigned = ref(false);
const defenseDate = ref('');
const defenseGrade = ref(14);
const juryMembers = ref([]);
const defenseDateError = ref('');
const existingDefenses = ref(new Set()); // Track which theses already have defenses

// Calculate minimum date for defense (now + 1 day)
const minDefenseDate = computed(() => {
  const now = new Date();
  now.setDate(now.getDate() + 1); // Add at least 1 day
  return now.toISOString().split('T')[0]; // Format as YYYY-MM-DD
});

// Table headers
const thesisHeaders = [
  { title: 'Thesis Title', key: 'title' },
  { title: 'Student', key: 'student' },
  { title: 'Status', key: 'status' },
  { title: 'Actions', key: 'actions' }
];

const defenseHeaders = [
  { title: 'Thesis Title', key: 'title' },
  { title: 'Student', key: 'student' },
  { title: 'Status', key: 'status' },
  { title: 'Actions', key: 'actions' }
];

// Helper functions
const getDisplayStatus = (status) => {
  return statusMapping[status] || status;
};

const getStatusColor = (status) => {
  const colors = {
    'PROPOSAL_SUBMITTED': 'blue',
    'APPROVED_BY_SC': 'indigo',
    'JURY_PRESIDENT_ASSIGNED': 'cyan',
    'DOCUMENT_SIGNED': 'amber',
    'SUBMITTED_TO_FENIX': 'green',
    'DEFENSE_SCHEDULED': 'purple',
    'UNDER_REVIEW': 'orange',
    'UNSCHEDULED': 'red'
  };
  return colors[status] || 'grey';
};

// Thesis workflow actions
const canTakeAction = (status) => {
  return status === 'APPROVED_BY_SC' || status === 'JURY_PRESIDENT_ASSIGNED';
};

const getActionLabel = (status) => {
  switch (status) {
    case 'APPROVED_BY_SC':
      return 'Atribuir Presidente';
    case 'JURY_PRESIDENT_ASSIGNED':
      return 'Assinar Documento';
    default:
      return 'Ver';
  }
};

const getActionDialogTitle = (status) => {
  switch (status) {
    case 'APPROVED_BY_SC':
      return 'Assign Jury President';
    case 'JURY_PRESIDENT_ASSIGNED':
      return 'Assinar Documento';
    default:
      return 'Detalhes da Tese';
  }
};

const canConfirmAction = computed(() => {
  if (!selectedThesis.value) return false;
  
  switch (selectedThesis.value.status) {
    case 'APPROVED_BY_SC':
      return !!selectedJuryPresident.value;
    case 'JURY_PRESIDENT_ASSIGNED':
      return documentSigned.value;
    default:
      return false;
  }
});

// Watch for changes in selected thesis to update jury members
watch(selectedThesis, async (newThesis) => {
  if (newThesis && newThesis.status === 'APPROVED_BY_SC') {
    try {
      // Convert jury member IDs to numbers (they might be stored as strings)
      let juryMemberIds = [];
      
      if (newThesis.juryMemberIds) {
        // Handle different possible formats of juryMemberIds
        if (Array.isArray(newThesis.juryMemberIds)) {
          juryMemberIds = newThesis.juryMemberIds.map(id => 
            typeof id === 'string' ? parseInt(id, 10) : id
          );
        } else if (typeof newThesis.juryMemberIds === 'string') {
          // It might be a string like "{6,7,8}" from PostgreSQL array
          juryMemberIds = newThesis.juryMemberIds
            .replace('{', '')
            .replace('}', '')
            .split(',')
            .map(id => parseInt(id.trim(), 10));
        }
      }
      
      console.log("Jury member IDs:", juryMemberIds);
      
      if (juryMemberIds.length > 0) {
        const professors = await RemoteServices.getProfessors();
        console.log("All professors:", professors);
        
        // Filter professors to get jury members
        juryMembers.value = professors.filter(prof => 
          juryMemberIds.includes(prof.id)
        );
        
        console.log("Filtered jury members:", juryMembers.value);
      }
    } catch (error) {
      console.error("Error fetching jury members:", error);
      appearance.pushError(new DeiError('Failed to load jury members'));
    }
  }
});

// Watch for changes in defenseDate to validate it
watch(defenseDate, (newDate) => {
  if (!newDate) {
    defenseDateError.value = 'Defense date is required';
    return;
  }
  
  const selectedDate = new Date(newDate);
  const now = new Date();
  
  if (selectedDate <= now) {
    defenseDateError.value = 'Defense date must be in the future';
  } else {
    defenseDateError.value = '';
  }
});

const handleThesisAction = async (thesis) => {
  appearance.loading = true;
  
  try {
    console.log("Action triggered for thesis:", thesis);
    
    // Fetch full thesis details
    const fullThesis = await RemoteServices.getThesisById(thesis.id);
    console.log("Fetched thesis details:", fullThesis);
    
    // Fetch student details
    if (fullThesis.studentId) {
      fullThesis.student = await RemoteServices.getPersonById(fullThesis.studentId);
      console.log("Fetched student details:", fullThesis.student);
    }
    
    selectedThesis.value = fullThesis;
    selectedJuryPresident.value = null;
    documentSigned.value = false;
    thesisActionDialog.value = true;
  } catch (error) {
    console.error('Error loading thesis details:', error);
    appearance.pushError(new DeiError('Failed to load thesis details'));
  } finally {
    appearance.loading = false;
  }
};

const confirmThesisAction = async () => {
  if (!selectedThesis.value) return;
  
  appearance.loading = true;
  
  try {
    switch (selectedThesis.value.status) {
      case 'APPROVED_BY_SC':
        if (selectedJuryPresident.value) {
          console.log("Assigning jury president:", selectedJuryPresident.value.id, "to thesis:", selectedThesis.value.id);
          
          // Make the actual API call
          await RemoteServices.assignJuryPresident(
            selectedThesis.value.id, 
            selectedJuryPresident.value.id
          );
        }
        break;
        
      case 'JURY_PRESIDENT_ASSIGNED':
        if (documentSigned.value) {
          console.log("Uploading signed document for thesis:", selectedThesis.value.id);
          
          // Make the actual API call 
          await RemoteServices.uploadSignedDocument(
            selectedThesis.value.id,
            'signed_document.pdf' // Default document path
          );
        }
        break;
    }
    
    thesisActionDialog.value = false;
    // Important: Reload the data to see the updated state
    await loadData();
  } catch (error) {
    console.error('Error performing thesis action:', error);
    appearance.pushError(new DeiError('Failed to complete the action'));
  } finally {
    appearance.loading = false;
  }
};

// Allow actions for both "UNSCHEDULED" (to schedule) and "UNDER_REVIEW" (to assign grade)
const canTakeDefenseAction = (status) => {
  return status === 'UNSCHEDULED' || status === 'UNDER_REVIEW';
};

// Adjust button label based on defense status
const getDefenseActionLabel = (status) => {
  switch (status) {
    case 'UNSCHEDULED':
      return 'Schedule Defense';
    case 'UNDER_REVIEW':
      return 'Assign Grade';
    default:
      return 'View';
  }
};

// Adjust dialog title based on defense status
const getDefenseActionDialogTitle = (status) => {
  switch (status) {
    case 'UNSCHEDULED':
      return 'Schedule Defense Date';
    case 'UNDER_REVIEW':
      return 'Assign Final Grade';
    default:
      return 'Defense Details';
  }
};

const canConfirmDefenseAction = computed(() => {
  if (!selectedDefense.value) return false;

  switch (selectedDefense.value.status) {
    case 'UNSCHEDULED':
      // Check that date is valid and in the future
      if (!defenseDate.value || defenseDateError.value) {
        return false;
      }
      const selectedDate = new Date(defenseDate.value);
      const now = new Date();
      return selectedDate > now;
      
    case 'UNDER_REVIEW':
      return defenseGrade.value !== null && defenseGrade.value >= 0 && defenseGrade.value <= 20;
      
    default:
      return false;
  }
});

// Improved handleDefenseAction function
const handleDefenseAction = async (defense) => {
  appearance.loading = true;

  try {
    console.log("Defense action triggered:", defense);
    selectedDefense.value = defense;

    // Reset validation
    defenseDateError.value = '';

    if (defense.status === 'UNDER_REVIEW') {
      // Default grade if not set
      defenseGrade.value = defense.grade || 14;
      defenseDate.value = '';
    } else if (defense.status === 'UNSCHEDULED') {
      // Set default date to tomorrow
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(10, 0, 0, 0); // 10:00 AM
      
      // Format as YYYY-MM-DDThh:mm
      const year = tomorrow.getFullYear();
      const month = String(tomorrow.getMonth() + 1).padStart(2, '0');
      const day = String(tomorrow.getDate()).padStart(2, '0');
      const hours = String(tomorrow.getHours()).padStart(2, '0');
      const minutes = String(tomorrow.getMinutes()).padStart(2, '0');
      
      defenseDate.value = `${year}-${month}-${day}T${hours}:${minutes}`;
      defenseGrade.value = 14;
    } else {
      defenseGrade.value = 14;
      defenseDate.value = '';
    }

    // If thesis info is missing, try to fetch it
    if (!defense.thesis && defense.thesisId) {
      try {
        defense.thesis = await RemoteServices.getThesisById(defense.thesisId);
        console.log("Fetched thesis for defense:", defense.thesis);
      } catch (error) {
        console.error("Error fetching thesis for defense:", error);
      }
    }

    defenseActionDialog.value = true;
  } catch (error) {
    console.error('Error preparing defense action:', error);
    appearance.pushError(new DeiError('Failed to prepare defense action'));
  } finally {
    appearance.loading = false;
  }
};

// Improved confirmDefenseAction function
const confirmDefenseAction = async () => {
  if (!selectedDefense.value) return;

  appearance.loading = true;

  try {
    if (selectedDefense.value.status === 'UNSCHEDULED') {
      // Validate defense date is in the future
      const selectedDate = new Date(defenseDate.value);
      const now = new Date();
      
      if (selectedDate <= now) {
        throw new Error('Defense date must be in the future');
      }
      
      console.log("Scheduling defense ID:", selectedDefense.value.id, "with date:", defenseDate.value);
      
      // Here's the key change - update the existing defense rather than scheduling a new one
      await RemoteServices.updateDefenseSchedule(
        selectedDefense.value.id, // Use the defense ID instead of the thesis ID
        new Date(defenseDate.value).toISOString()
      );

    } else if (selectedDefense.value.status === 'UNDER_REVIEW') {
      console.log("Assigning grade to defense:", selectedDefense.value.id, "with grade:", defenseGrade.value);

      await RemoteServices.assignGrade(
        selectedDefense.value.id,
        defenseGrade.value
      );
    }

    defenseActionDialog.value = false;
    await loadData(); // Reload data to reflect changes
  } catch (error) {
    console.error('Error performing defense action:', error);
    appearance.pushError(new DeiError(
      error.message || 'Failed to complete the action'
    ));
  } finally {
    appearance.loading = false;
  }
};


const loadData = async () => {
  loading.value = true;
  appearance.loading = true;
  
  try {
    // Get current user (coordinator)
    await RemoteServices.updateDefenseStatuses();
    coordinator.value = await RemoteServices.getCurrentUser();
    console.log("Current user (coordinator):", coordinator.value);
    
    // Fetch theses statuses
    console.log("Fetching theses by status...");
    const approvedTheses = await RemoteServices.getThesesByStatus('APPROVED_BY_SC') || [];
    const assignedTheses = await RemoteServices.getThesesByStatus('JURY_PRESIDENT_ASSIGNED') || [];
    const signedTheses = await RemoteServices.getThesesByStatus('DOCUMENT_SIGNED') || [];
    const submittedToFenixTheses = await RemoteServices.getThesesByStatus('SUBMITTED_TO_FENIX') || [];

    console.log("Theses counts:", {
      approved: approvedTheses.length,
      assigned: assignedTheses.length,
      signed: signedTheses.length,
      submitted: submittedToFenixTheses.length
    });

    theses.value = [
      ...approvedTheses, 
      ...assignedTheses, 
      ...signedTheses,
      ...submittedToFenixTheses
    ];
    
    // Fetch student details for theses
    console.log("Fetching student details for theses...");
    await Promise.all(theses.value.map(async (thesis) => {
      if (thesis.studentId) {
        try {
          thesis.student = await RemoteServices.getPersonById(thesis.studentId);
        } catch (error) {
          console.error(`Error fetching student ${thesis.studentId}:`, error);
        }
      }
    }));
    
    // Fetch defense workflows
    console.log("Fetching defenses by status...");
    
    try {
      // Use the RemoteServices method instead of direct httpClient
      console.log("Fetching UNSCHEDULED defenses...");
      const unscheduledDefenses = await RemoteServices.getDefensesByStatus('UNSCHEDULED') || [];
      console.log("Unscheduled defenses:", unscheduledDefenses);
      
      const scheduledDefenses = await RemoteServices.getDefensesByStatus('DEFENSE_SCHEDULED') || [];
      const underReviewDefenses = await RemoteServices.getDefensesByStatus('UNDER_REVIEW') || [];
      
      console.log("Defense counts:", {
        scheduled: scheduledDefenses.length,
        unscheduled: unscheduledDefenses.length,
        underReview: underReviewDefenses.length
      });
      
      // Combine and process defenses
      defenses.value = [
        ...scheduledDefenses, 
        ...unscheduledDefenses, 
        ...underReviewDefenses
      ];
    } catch (error) {
      console.error("Error fetching defenses:", error);
      defenses.value = [];
    }
    
    // Process defense data
    defenses.value = defenses.value.map(defense => {
      // Ensure we have a title property for display
      if (!defense.title && defense.thesis && defense.thesis.title) {
        defense.title = defense.thesis.title;
      }
      return defense;
    });
    
    // Build a set of thesis IDs that already have defenses to prevent duplicates
    existingDefenses.value = new Set(
      defenses.value
        .filter(d => d.thesisId)
        .map(d => d.thesisId)
    );
    
    // Fetch student details for defenses
    console.log("Fetching student details for defenses...");
    await Promise.all(defenses.value.map(async (defense) => {
      if (defense.studentId) {
        try {
          defense.student = await RemoteServices.getPersonById(defense.studentId);
        } catch (error) {
          console.error(`Error fetching student ${defense.studentId}:`, error);
        }
      }
      
      // If we don't have thesis info, try to fetch it
      if (!defense.thesis && defense.thesisId) {
        try {
          const thesis = await RemoteServices.getThesisById(defense.thesisId);
          defense.thesis = thesis;
          if (!defense.title && thesis.title) {
            defense.title = thesis.title;
          }
        } catch (error) {
          console.error(`Error fetching thesis ${defense.thesisId}:`, error);
        }
      }
    }));
    
    console.log("Final defenses array:", defenses.value);
    console.log("Data loading complete");
    
  } catch (error) {
    console.error('Error loading data:', error);
    appearance.pushError(new DeiError('Failed to load dashboard data: ' + (error.message || 'Unknown error')));
  } finally {
    loading.value = false;
    appearance.loading = false;
  }
};
// Initialize component
onMounted(async () => {
  await loadData();
});
</script>

<style scoped>
.coordinator-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>