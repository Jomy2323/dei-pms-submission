<template>
  <div class="student-dashboard" v-if="isDataLoaded">
    <!-- Student information section -->
    <div class="info-card">
      <h2>Bem-vindo, {{ student?.name || 'Estudante' }}</h2>
      <div class="student-details">
        <p><strong>IST ID:</strong> {{ student?.istId || 'N/A' }}</p>
        <p><strong>Email:</strong> {{ student?.email || 'N/A' }}</p>
      </div>
    </div>

    <!-- Thesis workflow section -->
    <div class="workflow-card">
      <h3>Workflow de Tese</h3>
      
      <!-- If thesis exists, show progress -->
      <div v-if="thesisWorkflow" class="workflow-progress">
        <div class="progress-bar">
          <div class="progress-steps">
            <div 
              v-for="(step, index) in thesisSteps" 
              :key="step"
              class="step" 
              :class="{ 
                active: isThesisStepActive(step),
                current: getCurrentStepIndex() === index
              }"
            >
              <div class="step-indicator">{{ index + 1 }}</div>
              <div class="step-label">{{ step }}</div>
            </div>
          </div>
        </div>
        <p><strong>Estado Atual:</strong> {{ getDisplayStatus(thesisWorkflow.status) }}</p>
        <p v-if="thesisWorkflow.title"><strong>Título:</strong> {{ thesisWorkflow.title }}</p>
      </div>
      
      <!-- Show button if no thesis -->
      <div v-else class="no-workflow">
        <p>Não existe nenhuma tese submetida.</p>
        <button 
          class="submit-btn" 
          @click="openThesisDialog"
        >
          Submeter Proposta de Tese
        </button>
      </div>
    </div>

    <!-- Defense workflow section (only shown if thesis is completed) -->
    <div v-if="isThesisCompleted" class="workflow-card">
      <h3>Workflow de Defesa</h3>
      <div v-if="defenseWorkflow" class="workflow-progress">
        <div class="progress-bar">
          <div class="progress-steps">
            <div 
              v-for="(step, index) in defenseSteps" 
              :key="step"
              class="step" 
              :class="{ 
                active: isDefenseStepActive(step),
                current: getCurrentDefenseStepIndex() === index
              }"
            >
              <div class="step-indicator">{{ index + 1 }}</div>
              <div class="step-label">{{ step }}</div>
            </div>
          </div>
        </div>
        <p><strong>Estado atual:</strong> {{ getDisplayStatus(defenseWorkflow.status) }}</p>
        <p v-if="defenseWorkflow.defenseDate"><strong>Data da Defesa:</strong> {{ formatDate(defenseWorkflow.defenseDate) }}</p>
        <p v-if="defenseWorkflow.grade"><strong>Nota Final:</strong> {{ defenseWorkflow.grade }}</p>
      </div>
      <div v-else class="no-workflow">
        <p>Aguardando agendamento da defesa pelo coordenador.</p>
      </div>
    </div>

    <!-- Thesis Submission Dialog -->
    <div v-if="showThesisDialog" class="dialog-overlay" @click.self="closeThesisDialog">
      <div class="dialog">
        <h3>Submeter Proposta de Tese</h3>
        <v-text-field
          v-model="thesisProposal.title"
          label="Título da Tese"
          placeholder="Digite o título da sua tese"
          variant="outlined"
          color="primary"
          class="mt-3"
          dense
          hide-details="auto"
          required
        ></v-text-field>
        
        <div class="form-field">
          <label>Selecione os Membros do Júri (máximo 5):</label>
          <div class="professors-list">
            <p v-if="!professors.length">❌ Nenhum professor encontrado.</p>
            
            <div v-for="professor in professors" :key="professor.id" class="professor-item">
              <input 
                type="checkbox" 
                :id="'prof-' + professor.id" 
                :value="professor.id" 
                v-model="thesisProposal.juryMemberIds"
                :disabled="thesisProposal.juryMemberIds.length >= 5 && !thesisProposal.juryMemberIds.includes(professor.id)"
              />
              <label :for="'prof-' + professor.id">{{ professor.name }} (ID: {{ professor.id }})</label>
            </div>
          </div>
        </div>

        
        <div class="dialog-actions">
          <button class="btn-cancel" @click="closeThesisDialog">Cancelar</button>
          <button 
            class="btn-submit" 
            @click="submitThesisProposal"
            :disabled="!isProposalValid"
          >
            Submeter
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading-container">
    <p>Carregando dados do estudante...</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RemoteServices from '../services/RemoteService'
import { useAppearanceStore } from '../stores/appearance'
import type PersonDto from '../models/PersonDto'
import DeiError from '../models/DeiError'

// Access store
const appearance = useAppearanceStore()

// Loading state
const isDataLoaded = ref(false)

// Student data
const student = ref<PersonDto | null>(null)

// Workflow data
const thesisWorkflow = ref(null)
const defenseWorkflow = ref(null)

// Thesis proposal state
const showThesisDialog = ref(false)
const professors = ref<PersonDto[]>([])
const thesisProposal = ref({
  title: '',
  juryMemberIds: []
})

// Status mapping between API enum values and display values
const statusMapping = {
  // Thesis statuses
  'PROPOSAL_SUBMITTED': 'Proposta de Júri Submetida',
  'APPROVED_BY_SC': 'Aprovado pelo SC',
  'JURY_PRESIDENT_ASSIGNED': 'Presidente do Júri Atribuído',
  'DOCUMENT_SIGNED': 'Documento Assinado',
  'SUBMITTED_TO_FENIX': 'Submetido ao Fenix',
  // Defense statuses
  'DEFENSE_SCHEDULED': 'Defesa Agendada',
  'UNDER_REVIEW': 'Em Revisão',
  'UNSCHEDULED' : 'Por Agendar'
}

// Helper function to convert API status to display status
const getDisplayStatus = (status: string) => {
  return statusMapping[status] || status
}

// Thesis workflow steps (display values)
const thesisSteps = [
  'Proposta de Júri Submetida',
  'Aprovado pelo SC',
  'Presidente do Júri Atribuído',
  'Documento Assinado',
  'Submetido ao Fenix'
]

// Defense workflow steps (display values)
const defenseSteps = [
  'Defesa Agendada',
  'Em Revisão',
  'Submetido ao Fenix'
]

// Computed properties
const isThesisCompleted = computed(() => {
  return thesisWorkflow.value?.status === 'SUBMITTED_TO_FENIX' || 
         getDisplayStatus(thesisWorkflow.value?.status) === 'Submetido ao Fenix'
})

const isProposalValid = computed(() => {
  return thesisProposal.value.title.trim() !== '' && 
         thesisProposal.value.juryMemberIds.length >= 1 && 
         thesisProposal.value.juryMemberIds.length <= 5
})

// Function to get current step index for thesis workflow
const getCurrentStepIndex = () => {
  if (!thesisWorkflow.value || !thesisWorkflow.value.status) return -1
  
  const currentDisplayStatus = getDisplayStatus(thesisWorkflow.value.status)
  return thesisSteps.indexOf(currentDisplayStatus)
}

// Function to get current step index for defense workflow
const getCurrentDefenseStepIndex = () => {
  if (!defenseWorkflow.value || !defenseWorkflow.value.status) return -1
  
  const currentDisplayStatus = getDisplayStatus(defenseWorkflow.value.status)
  return defenseSteps.indexOf(currentDisplayStatus)
}

// Functions to check active steps in progress bars
const isThesisStepActive = (step: string) => {
  if (!thesisWorkflow.value || !thesisWorkflow.value.status) return false
  
  const currentDisplayStatus = getDisplayStatus(thesisWorkflow.value.status)
  const currentIndex = thesisSteps.indexOf(currentDisplayStatus)
  const stepIndex = thesisSteps.indexOf(step)
  
  return currentIndex >= 0 && stepIndex <= currentIndex
}

const isDefenseStepActive = (step: string) => {
  if (!defenseWorkflow.value || !defenseWorkflow.value.status) return false
  
  const currentDisplayStatus = getDisplayStatus(defenseWorkflow.value.status)
  const currentIndex = defenseSteps.indexOf(currentDisplayStatus)
  const stepIndex = defenseSteps.indexOf(step)
  
  return currentIndex >= 0 && stepIndex <= currentIndex
}

// Helper function to format dates
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('pt-PT', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric',
    hour: '2-digit', 
    minute: '2-digit'
  })
}

const openThesisDialog = async () => {
  appearance.loading = true;
  
  try {
    console.log("📡 Fetching professors...");
    const result = await RemoteServices.getProfessors();
    console.log("✅ Raw professors data:", result);
    
    // Check if the result is an array
    if (Array.isArray(result)) {
      professors.value = result;
      console.log("✅ Professors loaded as array:", professors.value);
    } else {
      console.error("❌ Professors data is not an array:", result);
      professors.value = []; // Set default empty array
    }

    showThesisDialog.value = true;
  } catch (error: any) {
    console.error('❌ Error loading professors:', error);
    appearance.pushError(new DeiError('Erro ao carregar professores. Por favor, tente novamente.'));
  } finally {
    appearance.loading = false;
  }
};


const closeThesisDialog = () => {
  showThesisDialog.value = false
  thesisProposal.value = { title: '', juryMemberIds: [] }
}

// Submit thesis proposal
// Submit thesis proposal
const submitThesisProposal = async () => {
  if (!isProposalValid.value || !student.value?.id) return
  
  appearance.loading = true
  
  try {
    const submittedThesis = await RemoteServices.submitThesisProposal(
      student.value.id,
      thesisProposal.value.title,
      thesisProposal.value.juryMemberIds
    )
    
    console.log('Received thesis response:', submittedThesis)
    
    // Even if the response is undefined, we should still refresh the data
    closeThesisDialog()
    
    // Reload the student dashboard data to get the updated thesis workflow
    await loadStudentData()
    
  } catch (error: any) {
    console.error('Error submitting thesis proposal:', error)
    appearance.pushError(new DeiError('Erro ao submeter proposta. Por favor, tente novamente.'))
  } finally {
    appearance.loading = false
  }
}

const loadStudentData = async () => {
  isDataLoaded.value = false
  appearance.loading = true
  
  try {
    // Get the current user
    const currentUser = await RemoteServices.getCurrentUser()
    
    if (!currentUser) {
      throw new Error('No authenticated user found. Please log in first.')
    }
    
    // Set the student data from the authenticated user
    student.value = currentUser
    
    console.log('Using authenticated student ID:', student.value.id)
    
    // Load dashboard data for the authenticated student
    // This is now the direct data, not a response object
    const dashboard = await RemoteServices.getStudentDashboard(student.value.id)
    console.log('Dashboard data received:', dashboard)
    
    // Check if the dashboard data exists
    if (dashboard) {
      // Set workflow data
      thesisWorkflow.value = dashboard.thesisWorkflow || null
      defenseWorkflow.value = dashboard.defenseWorkflow || null
      
      console.log('Thesis workflow loaded:', thesisWorkflow.value)
      console.log('Defense workflow loaded:', defenseWorkflow.value)
    } else {
      console.warn('Dashboard data is null or undefined')
    }
    
    isDataLoaded.value = true
  } catch (error) {
    console.error('Error loading student data:', error)
    appearance.pushError(new DeiError('Erro ao carregar dados do estudante. Por favor, tente novamente.'))
  } finally {
    appearance.loading = false
  }
}// Initialize component
onMounted(async () => {
  await loadStudentData()
})
</script>

<style scoped>
.student-dashboard {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.info-card, .workflow-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.student-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.workflow-progress {
  margin-top: 15px;
}

.progress-bar {
  margin: 30px 0;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.progress-steps::before {
  content: '';
  position: absolute;
  top: 15px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #e0e0e0;
  z-index: 1;
}

.step {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 18%;
}

.step-indicator {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.step.current .step-indicator {
  background-color: #2196F3; /* Blue color for current step */
  color: white;
  box-shadow: 0 0 10px rgba(33, 150, 243, 0.5);
}

.step.active .step-indicator {
  background-color: #4CAF50; /* Green for completed steps */
}

.step:not(.active):not(.current) .step-indicator {
  background-color: #e0e0e0; /* Grey for inactive steps */
}

.step-label {
  font-size: 12px;
  text-align: center;
}

.no-workflow {
  text-align: center;
  margin: 20px 0;
}

.submit-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 10px;
  transition: background-color 0.3s ease;
}

.submit-btn:hover {
  background-color: #45a049;
}

.submit-btn:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

/* Dialog styling */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.form-field {
  margin-bottom: 15px;
}

.form-field label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

.form-field input[type="text"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: border-color 0.3s ease;
}

.form-field input[type="text"]:focus {
  outline: none;
  border-color: #4CAF50;
}

.professors-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  background-color: #f9f9f9;
}

.professor-item {
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.professor-item input[type="checkbox"] {
  margin-right: 10px;
  accent-color: #4CAF50;
}

.professor-item label {
  flex-grow: 1;
  cursor: pointer;
  transition: color 0.3s ease;
}

.professor-item label:hover {
  color: #4CAF50;
}

.selection-count {
  margin-top: 5px;
  font-size: 12px;
  color: #666;
  text-align: right;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 15px;
}

.btn-cancel, .btn-submit {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-cancel {
  background-color: #f1f1f1;
  color: #333;
}

.btn-cancel:hover {
  background-color: #e0e0e0;
}

.btn-submit {
  background-color: #4CAF50;
  color: white;
}

.btn-submit:hover {
  background-color: #45a049;
}

.btn-submit:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

/* Responsive adjustments */
@media (max-width: 600px) {
  .student-dashboard {
    padding: 10px;
  }

  .info-card, .workflow-card {
    padding: 15px;
  }

  .progress-steps {
    flex-direction: column;
    align-items: center;
  }

  .step {
    width: 100%;
    margin-bottom: 10px;
    flex-direction: row;
    justify-content: flex-start;
    align-items: center;
  }

  .step-indicator {
    margin-right: 10px;
    margin-bottom: 0;
  }

  .step-label {
    text-align: left;
  }

  .dialog {
    width: 95%;
    margin: 0 10px;
  }
}
</style>