<template>
  <div class="sc-view">
    <h1 class="text-h4 mb-4">Scientific Committee Dashboard</h1>
    
    <!-- Pending Proposals Section -->
    <v-card class="mb-6 elevation-3">
      <v-card-title class="d-flex align-center">
        <span>Pending Thesis Proposals</span>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="Pesquisar"
          single-line
          hide-details
          density="compact"
          class="max-width-200"
        ></v-text-field>
      </v-card-title>
      
      <v-data-table-server
        :headers="headers"
        :items="pendingProposals"
        :items-length="totalProposals"
        :search="search"
        :loading="loading"
        item-value="id"
        class="elevation-1"
        @update:options="fetchProposals"
      >
        <template v-slot:item.submissionDate="{ item }">
          {{ formatDate(item.submissionDate) }}
        </template>
        
        <template v-slot:item.actions="{ item }">
          <v-btn
            color="primary"
            size="small"
            variant="outlined"
            class="mr-2"
            @click="openProposalDetails(item)"
          >
            <v-icon left>mdi-file-document-outline</v-icon>
            Review
          </v-btn>
        </template>
      </v-data-table-server>
    </v-card>
    
  <!-- Recently Approved Proposals -->
  <v-card class="mb-6 elevation-3">
    <v-card-title>
      <span>Recently Approved Proposals</span>
    </v-card-title>
    
    <v-list v-if="recentlyApproved.length > 0">
      <v-list-item
        v-for="thesis in recentlyApproved"
        :key="thesis.id"
        :subtitle="'Estudante: ' + (thesis.student?.name || 'N/A')"
      >
        <template v-slot:prepend>
          <v-avatar color="green" size="36">
            <v-icon color="white">mdi-check</v-icon>
          </v-avatar>
        </template>
        
        <v-list-item-title>{{ thesis.title || 'Sem título' }}</v-list-item-title>
        <v-list-item-subtitle>
          Aprovado a {{ formatDate(thesis.submissionDate) }}
        </v-list-item-subtitle>
      </v-list-item>
    </v-list>
    
    <v-list-item v-else>
      <v-list-item-title class="text-center text-grey">
        Sem aprovações recentes
      </v-list-item-title>
    </v-list-item>
  </v-card>

    <!-- Thesis Details Dialog -->
    <v-dialog v-model="showDetailsDialog" max-width="800">
      <v-card v-if="selectedThesis">
        <v-card-title class="text-h5">
          Proposta de Tese
        </v-card-title>
        
        <v-card-text>
          <div class="proposal-details">
            <div class="detail-section">
              <h3 class="text-h6">Informação de Tese</h3>
              <div class="d-flex">
                <p class="text-subtitle-1 font-weight-bold">Título:</p>
                <p class="ml-2">{{ selectedThesis.title }}</p>
              </div>
              <div class="d-flex">
                <p class="text-subtitle-1 font-weight-bold">Data de Submissão:</p>
                <p class="ml-2">{{ formatDate(selectedThesis.submissionDate) }}</p>
              </div>
            </div>
            
            <div class="detail-section">
              <h3 class="text-h6">Informação do Estudante</h3>
              <div class="d-flex">
                <p class="text-subtitle-1 font-weight-bold">Nome:</p>
                <p class="ml-2">{{ selectedThesis.student?.name || 'N/A' }}</p>
              </div>
              <div class="d-flex">
                <p class="text-subtitle-1 font-weight-bold">IST ID:</p>
                <p class="ml-2">{{ selectedThesis.student?.istId || 'N/A' }}</p>
              </div>
              <div class="d-flex">
                <p class="text-subtitle-1 font-weight-bold">Email:</p>
                <p class="ml-2">{{ selectedThesis.student?.email || 'N/A' }}</p>
              </div>
            </div>

            <div class="detail-section">
              <h3 class="text-h6">Juri Proposto</h3>
              <v-list density="compact">
                <v-list-item
                  v-for="professor in selectedThesis.juryMembers"
                  :key="professor.id"
                  :title="professor.name"
                  :subtitle="professor.email"
                ></v-list-item>
              </v-list>
            </div>
            
            <div class="detail-section">
              <h3 class="text-h6">Decisão SC</h3>
              <v-textarea
                v-model="reviewComments"
                label="Comentários (opcional)"
                rows="3"
                variant="outlined"
                class="mb-4"
              ></v-textarea>
              
              <div class="decision-buttons">
                <v-btn
                  color="error"
                  variant="outlined"
                  @click="rejectProposal"
                  class="mr-4"
                  :loading="processingDecision"
                >
                  <v-icon start>mdi-close</v-icon>
                  Rejeitar Proposta
                </v-btn>
                
                <v-btn
                  color="success"
                  @click="approveProposal"
                  :loading="processingDecision"
                >
                  <v-icon start>mdi-check</v-icon>
                  Aceitar Proposta
                </v-btn>
              </div>
            </div>
          </div>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn 
            variant="outlined" 
            @click="showDetailsDialog = false"
            :disabled="processingDecision"
          >
            Fechar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Confirmation Dialog -->
    <v-dialog v-model="showConfirmDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h5">
          {{ confirmDialogTitle }}
        </v-card-title>
        
        <v-card-text>
          {{ confirmDialogMessage }}
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn 
            variant="outlined" 
            @click="showConfirmDialog = false"
          >
            Cancelar
          </v-btn>
          <v-btn 
            :color="confirmAction === 'approve' ? 'success' : 'error'"
            @click="confirmDecision"
            :loading="processingDecision"
          >
            Confirmar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RemoteServices from '@/services/RemoteService'
import { useAppearanceStore } from '@/stores/appearance'
import DeiError from '@/models/DeiError'

// Access store for loading and error states
const appearance = useAppearanceStore()

// Table data
const headers = [
  { title: 'Thesis Title', key: 'title', align: 'start', sortable: true },
  { title: 'Student', key: 'student.name', align: 'start', sortable: true },
  { title: 'IST ID', key: 'student.istId', align: 'start', sortable: true },
  { title: 'Submission Date', key: 'submissionDate', align: 'start', sortable: true },
  { title: 'Actions', key: 'actions', align: 'center', sortable: false }
]

// State variables
const loading = ref(false)
const search = ref('')
const pendingProposals = ref([])
const recentlyApproved = ref([])
const totalProposals = ref(0)
const showDetailsDialog = ref(false)
const showConfirmDialog = ref(false)
const selectedThesis = ref(null)
const reviewComments = ref('')
const processingDecision = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogMessage = ref('')
const confirmAction = ref('')

const fetchProposals = async (options: any) => {
  loading.value = true
  appearance.loading = true
  
  try {
    // Fetch theses with the status "PROPOSAL_SUBMITTED"
    const response = await RemoteServices.getThesesByStatus('PROPOSAL_SUBMITTED')
    
    // Fetch full details for each thesis
    const detailedProposals = await Promise.all(
      (response || []).map(async (thesis) => {
        try {
          // Fetch full thesis details by ID
          return await RemoteServices.getThesisById(thesis.id)
        } catch (error) {
          console.error(`Error fetching details for thesis ${thesis.id}:`, error)
          return thesis
        }
      })
    )
    
    pendingProposals.value = detailedProposals
    totalProposals.value = pendingProposals.value.length
    
    // Similar approach for approved theses
    const approved = await RemoteServices.getThesesByStatus('APPROVED_BY_SC')
    const detailedApproved = await Promise.all(
      (approved || []).map(async (thesis) => {
        try {
          return await RemoteServices.getThesisById(thesis.id)
        } catch (error) {
          console.error(`Error fetching details for approved thesis ${thesis.id}:`, error)
          return thesis
        }
      })
    )
    
    recentlyApproved.value = detailedApproved
      .filter(thesis => thesis && thesis.student)
      .sort((a, b) => new Date(b.submissionDate).getTime() - new Date(a.submissionDate).getTime())
      .slice(0, 5)
  } catch (error) {
    console.error('Error fetching thesis proposals:', error)
    appearance.pushError(new DeiError('Error fetching thesis proposals'))
    
    // Reset arrays to prevent rendering errors
    pendingProposals.value = []
    recentlyApproved.value = []
    totalProposals.value = 0
  } finally {
    loading.value = false
    appearance.loading = false
  }
}

// Format date
const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('pt-PT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const openProposalDetails = async (thesis: any) => {
  try {
    // Fetch full thesis details
    const fullThesisDetails = await RemoteServices.getThesisById(thesis.id)
    
    console.log('Thesis Details:', fullThesisDetails)
    console.log('Jury Member IDs:', fullThesisDetails.juryMemberIds)
    
    // Fetch student details by ID
    if (fullThesisDetails.studentId) {
      const studentDetails = await RemoteServices.getPersonById(fullThesisDetails.studentId)
      fullThesisDetails.student = studentDetails
    }
    
    // Fetch professors and jury members
    const allProfessors = await RemoteServices.getProfessors()
    console.log('All Professors:', allProfessors)
    
    // Fetch jury member details
    if (fullThesisDetails.juryMemberIds && fullThesisDetails.juryMemberIds.length > 0) {
      // Filter professors to match jury member IDs
      fullThesisDetails.juryMembers = allProfessors.filter(prof => 
        fullThesisDetails.juryMemberIds.includes(prof.id)
      )
      
      console.log('Filtered Jury Members:', fullThesisDetails.juryMembers)
    }
    
    selectedThesis.value = fullThesisDetails
    reviewComments.value = ''
    showDetailsDialog.value = true
  } catch (error) {
    console.error('Error fetching thesis details:', error)
    appearance.pushError(new DeiError('Failed to load thesis details'))
  }
}

// Prepare for approval
const approveProposal = () => {
  confirmAction.value = 'approve'
  confirmDialogTitle.value = 'Confirmar'
  confirmDialogMessage.value = 'Tem a certeza que quer aceitar esta proposta?'
  showConfirmDialog.value = true
}

// Prepare for rejection
const rejectProposal = () => {
  confirmAction.value = 'reject'
  confirmDialogTitle.value = 'Confirmar'
  confirmDialogMessage.value = 'Are you sure you want to reject this thesis proposal? This will return the proposal to the student for revision.'
  showConfirmDialog.value = true
}

// Confirm decision (approve or reject)
const confirmDecision = async () => {
  if (!selectedThesis.value) return
  
  processingDecision.value = true
  appearance.loading = true
  
  try {
    if (confirmAction.value === 'approve') {
      // Call the API to approve the thesis
      await RemoteServices.approveThesis(
        selectedThesis.value.id,
        'SC',
        reviewComments.value
      )
      
    } else {
      // Call the API to reject the thesis
      await RemoteServices.rejectThesis(
        selectedThesis.value.id,
        'SC',
        reviewComments.value
      )
      
    }
    
    // Close dialogs and refresh data
    showConfirmDialog.value = false
    showDetailsDialog.value = false
    fetchProposals({})
  } catch (error) {
    console.error(`Error ${confirmAction.value === 'approve' ? 'approving' : 'rejecting'} thesis:`, error)
    appearance.pushError(new DeiError(`Error ${confirmAction.value === 'approve' ? 'approving' : 'rejecting'} thesis proposal`))
  } finally {
    processingDecision.value = false
    appearance.loading = false
  }
}

// Initialize component
onMounted(async () => {
  await fetchProposals({})
})
</script>

<style scoped>
.sc-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.max-width-200 {
  max-width: 200px;
}

.proposal-details {
  padding: 16px;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.detail-section:last-child {
  border-bottom: none;
}

.decision-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>