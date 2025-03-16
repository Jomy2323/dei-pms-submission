import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { useAppearanceStore } from '@/stores/appearance'
import DeiError from '@/models/DeiError'
import type PersonDto from '../models/PersonDto'
import type StudentDto from '../models/StudentDto'
import type ThesisDto from '../models/ThesisDto'
import type DefenseDto from '../models/DefenseDto'

// Initialize Axios Client
const httpClient = axios.create()

// Check if Debug Mode is Enabled from .env
const DEBUG_MODE = import.meta.env.VITE_DEBUG_MODE === "true"

httpClient.defaults.timeout = 50000
httpClient.defaults.baseURL = import.meta.env.VITE_ROOT_API
httpClient.defaults.headers.post['Content-Type'] = 'application/json'

export default class RemoteServices {

  // ---------------------
  //       API CALLS
  // ---------------------
  static async getPeople(): Promise<PersonDto[]> {
    if (DEBUG_MODE) console.log("🔍 Fetching people...");
    return httpClient.get('/people')
  }

  // Get person by ID
  static async getPersonById(personId: number): Promise<PersonDto> {
    if (DEBUG_MODE) console.log(`🔍 Fetching person details for ID: ${personId}`);
    try {
      const response = await httpClient.get(`/people/${personId}`);
      return response;
    } catch (error) {
      console.error(`❌ Error fetching person details for ID ${personId}:`, error);
      throw error;
    }
  }

  static async createPerson(person: PersonDto): Promise<PersonDto> {
    if (DEBUG_MODE) console.log("💾 Creating new person:", person);
    return httpClient.post('/people', person)
  }

  // -> Check if IST ID already exists
  static async checkIfIstIdExists(istId: string): Promise<boolean> {
    if (!istId || istId.trim() === "") {
      console.warn("❌ IST ID is empty or invalid:", istId);
      return false;
    }

    if (DEBUG_MODE) console.log(`🔍 Checking IST ID: ${istId}`);

    try {
      const response: any = await httpClient.get("/people/search/byIstId", { params: { istId } });

      // If we got 'noContent: true', it means 204 → ID doesn't exist → it's available
      if (response && response.noContent) {
        if (DEBUG_MODE) console.log(`ℹ️ IST ID is available: ${istId}`);
        return false; // ID is available
      }

      // Otherwise, 200 → ID found
      if (DEBUG_MODE) console.log(`✅ IST ID exists: ${istId}`);
      return true; // ID exists
    } catch (error) {
      if (DEBUG_MODE) console.error("❌ Error checking IST ID:", error);
      return false;
    }
  }

  // -> Check if Email already exists
  static async checkIfEmailExists(email: string): Promise<boolean> {
    if (!email || email.trim() === "") {
      console.warn("❌ Email is empty or invalid:", email);
      return false;
    }

    if (DEBUG_MODE) console.log(`🔍 Checking Email: ${email}`);

    try {
      const response: any = await httpClient.get("/people/search/byEmail", { params: { email } });

      // If we got 'noContent: true', it means 204 → Email doesn't exist → it's available
      if (response && response.noContent) {
        if (DEBUG_MODE) console.log(`ℹ️ Email is available: ${email}`);
        return false; // Email is available
      }

      // Otherwise, 200 → Email found
      if (DEBUG_MODE) console.log(`✅ Email exists: ${email}`);
      return true; // Email exists
    } catch (error) {
      if (DEBUG_MODE) console.error("❌ Error checking Email:", error);
      return false;
    }
  }

  static async checkLogin(istId: string, role: string): Promise<PersonDto> {
    // Convert role to the format expected by the backend
    const backendRole = role.toUpperCase();
    
    if (DEBUG_MODE) console.log(`🔍 Attempting login: IST ID = ${istId}, Role = ${backendRole}`);
    try {
      const user = await httpClient.post('/login', { istId, role: backendRole });
      
      // Save the user to localStorage
      if (DEBUG_MODE) console.log('Saving user to localStorage:', user);
      localStorage.setItem('currentUser', JSON.stringify(user));
      
      return user.data;
    } catch (error: any) {
      console.error('Full error object:', error);
      
      if (error.response && error.response.data) {
        console.log('Error response data:', JSON.stringify(error.response.data, null, 2));
      }
      
      let errorMessage = 'Login failed. Please try again.';
      if (error.response && error.response.data) {
        const data = error.response.data;
        const errorCode = Number(data.code);      
        if (errorCode === 1001) {
          errorMessage = 'Não existe nenhum utilizador com esse IST ID.';
        } else if (errorCode === 1008) {
          errorMessage = 'O seguinte IST ID não tem permissões para aceder a esta função';
        } else if (data.message) {
          errorMessage = data.message;
        }
      }
      
      if (DEBUG_MODE) {
        console.error('Login error details:', error.response?.data);
      }
      throw new Error(errorMessage);
    }
  }

  static getCurrentUser(): PersonDto | null {
    try {
      const storedUser = localStorage.getItem('currentUser');
      if (DEBUG_MODE) console.log('Retrieved from localStorage:', storedUser);
      
      if (storedUser) {
        const userData = JSON.parse(storedUser);
        if (DEBUG_MODE) console.log('Parsed user data:', userData);
        return userData;
      }
      
      if (DEBUG_MODE) console.log('No user found in localStorage');
      return null;
    } catch (error) {
      console.error('Error getting current user from storage:', error);
      return null;
    }
  }
  
  static logout(): void {
    localStorage.removeItem('currentUser');
    if (DEBUG_MODE) console.log('User logged out, removed from localStorage');
  }

  static async deletePerson(id: number): Promise<void> {
    try {
      await httpClient.delete(`/people/${id}`)
    } catch (error) {
      console.error("❌ Error deleting person:", error)
      throw error
    }
  }

  // ---------------------
  // STUDENT DASHBOARD APIS
  // ---------------------
  
  static async getStudentDashboard(studentId: number): Promise<any> {
    if (DEBUG_MODE) console.log(`🔍 Fetching student dashboard data for ID: ${studentId}`);
    try {
      // The response is already just the data from the interceptor
      const data = await httpClient.get(`/api/dashboard/student/${studentId}`);
      
      // Log the data for debugging
      if (DEBUG_MODE) {
        console.log('Dashboard data received:', data);
      }
      
      // Return the data directly - it's already been extracted by the interceptor
      return data;
    } catch (error) {
      console.error("❌ Error fetching student dashboard:", error);
      return null;
    }
  }
  
  // Get specific thesis workflow
  static async getThesisWorkflow(studentId: number): Promise<ThesisDto | null> {
    if (DEBUG_MODE) console.log(`🔍 Fetching thesis workflow for student ID: ${studentId}`);
    try {
      const response = await httpClient.get(`/api/thesis/student/${studentId}`);
      return response.data;
    } catch (error) {
      // Don't throw error for 404 (not found)
      if (error.response?.status === 404) {
        if (DEBUG_MODE) console.log(`No thesis found for student ${studentId}`);
        return null;
      }
      console.error("❌ Error fetching thesis workflow:", error);
      return null;
    }
  }
  
  // Get specific defense workflow
  static async getDefenseWorkflow(studentId: number): Promise<DefenseDto | null> {
    if (DEBUG_MODE) console.log(`🔍 Fetching defense workflow for student ID: ${studentId}`);
    try {
      const response = await httpClient.get(`/api/defense/student/${studentId}`);
      return response.data;
    } catch (error) {
      console.error("❌ Error fetching defense workflow:", error);
      return null;
    }
  }
  
  // Get student by IST ID
  static async getStudentByIstId(istId: string): Promise<PersonDto> {
    if (DEBUG_MODE) console.log(`🔍 Fetching student by IST ID: ${istId}`);
    try {
      const response = await httpClient.get(`/people/search/byIstId`, { params: { istId } });
      return response.data;
    } catch (error) {
      console.error("❌ Error fetching student by IST ID:", error);
      throw error;
    }
  }
  
  static async getProfessors(): Promise<PersonDto[]> {
    if (DEBUG_MODE) console.log("🔍 Fetching professors for jury selection");
    try {
      // Make sure this endpoint returns only teachers
      const response = await httpClient.get('/people/search/byType', { params: { type: 'TEACHER' } });
      console.log("Professor response data:", response);
      
      if (Array.isArray(response)) {
        return response;
      } else {
        console.error("Invalid response format from getProfessors:", response);
        return [];
      }
    } catch (error) {
      console.error("❌ Error fetching professors:", error);
      return [];
    }
  }
  
  
  // Submit thesis proposal
  static async submitThesisProposal(studentId: number, title: string, juryMemberIds: number[]): Promise<ThesisDto | null> {
    if (DEBUG_MODE) {
      console.log(`💾 Submitting thesis proposal for student ${studentId}:`, { 
        title, 
        juryMemberIds,
        juryMemberIdsType: typeof juryMemberIds,
        juryMemberIdsArray: Array.isArray(juryMemberIds)
      });
    }

    try {
      // Explicitly convert to a plain array and ensure no proxy/reactive wrapper
      const normalizedJuryIds = Array.isArray(juryMemberIds) 
        ? juryMemberIds.map(id => Number(id)) 
        : [];

      // Validate inputs
      if (!studentId) {
        throw new Error('Student ID is required');
      }
      if (normalizedJuryIds.length === 0) {
        throw new Error('At least one jury member is required');
      }
      if (!title || title.trim().length < 3) {
        throw new Error('Thesis title must be at least 3 characters long');
      }

      // Send the request with an object containing both title and jury member IDs
      const requestBody = {
        title: title,
        juryMemberIds: normalizedJuryIds
      };

      // Make the API call
      const response = await httpClient.post(`/api/thesis/submit?studentId=${studentId}`, requestBody);

      // If successful, fetch the updated thesis workflow
      if (response) {
        // If the API returned data directly, use it
        if (response.data) {
          return response.data;
        }
        
        // Otherwise fetch the latest workflow data
        return await this.getThesisWorkflow(studentId);
      }
      
      return null;
    } catch (error) {
      console.error("❌ Error submitting thesis proposal:", error);
      throw error;
    }
  }

  // Get theses by status
  static async getThesesByStatus(status: string): Promise<ThesisDto[]> {
    if (DEBUG_MODE) console.log(`🔍 Fetching theses with status: ${status}`);
    try {
      const response = await httpClient.get(`/api/thesis/status/${status}`);
      return response || [];
    } catch (error) {
      console.error(`❌ Error fetching theses with status ${status}:`, error);
      return [];
    }
  }

  static async getThesisById(thesisId: number): Promise<any> {
    if (DEBUG_MODE) console.log(`🔍 Fetching thesis details for ID: ${thesisId}`);
    try {
      const response = await httpClient.get(`/api/thesis/${thesisId}`);
      
      // Log the full response for debugging
      console.log('Full Thesis Response:', response);
      
      return response;
    } catch (error) {
      console.error(`❌ Error fetching thesis details for ID ${thesisId}:`, error);
      throw error;
    }
  }

  // Approve thesis by SC
static async approveThesis(thesisId: number, role: string, comments?: string): Promise<ThesisDto> {
  if (DEBUG_MODE) console.log(`🔍 Approving thesis ${thesisId}`);
  try {
    const response = await httpClient.post(`/api/thesis/${thesisId}/approve`, 
      { comments }, 
      { params: { role } }
    );
    return response;
  } catch (error) {
    console.error(`❌ Error approving thesis ${thesisId}:`, error);
    throw error;
  }
}

// Reject thesis
static async rejectThesis(thesisId: number, role: string, comments: string): Promise<ThesisDto> {
  if (DEBUG_MODE) console.log(`🔍 Rejecting thesis ${thesisId}`);
  try {
    const response = await httpClient.post(`/api/thesis/${thesisId}/reject`, 
      { comments }, 
      { params: { role } }
    );
    return response;
  } catch (error) {
    console.error(`❌ Error rejecting thesis ${thesisId}:`, error);
    throw error;
  }
}


/**
 * Upload signed document for a thesis (Coordinator action)
 */
static async uploadSignedDocument(thesisId: number, documentPath: string = 'signed_document.pdf'): Promise<any> {
  if (DEBUG_MODE) console.log(`🔍 Uploading signed document for thesis ${thesisId}`);
  try {
    const user = RemoteServices.getCurrentUser();
    if (!user || user.type !== 'COORDINATOR') {
      throw new Error('This action can only be performed by a coordinator');
    }
    
    const response = await httpClient.post(`/api/thesis/${thesisId}/document`, null, {
      params: {
        documentPath: documentPath,
        role: 'COORDINATOR'
      }
    });
    return response;
  } catch (error) {
    console.error(`❌ Error uploading signed document for thesis ${thesisId}:`, error);
    throw error;
  }
}

static async assignGrade(defenseId: number, grade: number): Promise<any> {
  if (DEBUG_MODE) console.log(`🔍 Assigning grade ${grade} to defense ${defenseId}`);
  try {
    const user = RemoteServices.getCurrentUser();
    if (!user || user.type !== 'COORDINATOR') {
      throw new Error('This action can only be performed by a coordinator');
    }
    
    const response = await httpClient.post(`/api/defense/${defenseId}/grade`, null, {
      params: {
        grade: grade,
        role: 'COORDINATOR'
      }
    });
    return response;
  } catch (error) {
    console.error(`❌ Error assigning grade to defense ${defenseId}:`, error);
    throw error;
  }
}

/**
 * Set a defense to UNDER_REVIEW state manually (Coordinator action)
 * This would normally happen automatically based on the defense date
 */
static async setDefenseUnderReview(defenseId: number): Promise<any> {
  if (DEBUG_MODE) console.log(`🔍 Setting defense ${defenseId} to UNDER_REVIEW state`);
  try {
    const user = RemoteServices.getCurrentUser();
    if (!user || user.type !== 'COORDINATOR') {
      throw new Error('This action can only be performed by a coordinator');
    }
    
    const response = await httpClient.post(`/api/defense/${defenseId}/review`, null, {
      params: {
        role: 'COORDINATOR'
      }
    });
    return response;
  } catch (error) {
    console.error(`❌ Error setting defense ${defenseId} to UNDER_REVIEW:`, error);
    throw error;
  }
}

static async assignJuryPresident(thesisId: number, presidentId: number): Promise<any> {
  console.log(`🧪 DEBUG: Assigning president ${presidentId} to thesis ${thesisId}`);
  
  try {
    const result = await httpClient.post(`/api/thesis/${thesisId}/president`, null, {
      params: {
        presidentId: presidentId,
        role: 'COORDINATOR'
      }
    });
    
    console.log(`🧪 DEBUG: Assignment successful:`, result);
    return result;
  } catch (error) {
    console.error(`🧪 DEBUG: Assignment failed:`, error);
    throw error;
  }
}

static async submitToFenix(thesisId: number) {
  if (DEBUG_MODE) console.log(`🔍 Submitting thesis ${thesisId} to Fenix`);
  try {
    const user = RemoteServices.getCurrentUser();
    
    if (!user || user.type !== 'STAFF') {
      throw new Error('This action can only be performed by staff');
    }
    
    const response = await httpClient.post(`/api/thesis/${thesisId}/fenix`, null, {
      params: {
        role: 'STAFF'
      }
    });
    
    return response;
  } catch (error) {
    console.error(`❌ Error submitting thesis ${thesisId} to Fenix:`, error);
    throw error;
  }
}

static async getDefensesByStatus(status:string) {
  if (DEBUG_MODE) console.log(`🔍 Fetching defenses with status: ${status}`);
  try {
    // Make sure the status is uppercase to match enum values
    const statusUpper = status.toUpperCase();
    
    const response = await httpClient.get(`/api/defense/status/${statusUpper}`);
    
    if (DEBUG_MODE) console.log(`Defense response for status ${statusUpper}:`, response);
    
    // Handle different response formats
    if (Array.isArray(response)) {
      return response;
    } else if (response && response.data && Array.isArray(response.data)) {
      return response.data;
    } else if (response) {
      // If we got a single item (not an array), wrap it in an array
      return [response];
    }
    
    return [];
  } catch (error) {
    // Special handling for 404 (Not Found) - this might mean there are no defenses of this status
    if (error.response && error.response.status === 404) {
      console.log(`No defenses found with status ${status}`);
      return [];
    }
    
    console.error(`❌ Error fetching defenses with status ${status}:`, error);
    return [];
  }
}

static async scheduleDefense(thesisId:number, defenseDate:string) {
  if (DEBUG_MODE) console.log(`🔍 Scheduling defense for thesis ${thesisId} on ${defenseDate}`);
  try {
    const user = RemoteServices.getCurrentUser();
    if (!user || user.type !== 'COORDINATOR') {
      throw new Error('This action can only be performed by a coordinator');
    }
    
    // Validate that the defense date is in the future
    const selectedDate = new Date(defenseDate);
    const now = new Date();
    if (selectedDate <= now) {
      throw new Error('Defense date must be in the future');
    }
    
    // Try with body format instead of params
    const response = await httpClient.post(`/api/defense/schedule`, {
      thesisId: Number(thesisId),
      defenseDate: defenseDate,
      role: 'COORDINATOR'
    });
    
    if (DEBUG_MODE) console.log('Defense scheduling response:', response);
    
    return response;
  } catch (error) {
    // Extract the error message from the response if available
    let errorMessage = 'Error scheduling defense';
    
    if (error.response && error.response.data) {
      if (error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error;
      }
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    console.error(`❌ Error scheduling defense: ${errorMessage}`);
    throw new Error(errorMessage);
  }
}

/**
 * Update an existing defense's schedule (Coordinator action)
 * This updates the status and date of an UNSCHEDULED defense
 */
static async updateDefenseSchedule(defenseId:number, defenseDate:string) {
  if (DEBUG_MODE) console.log(`🔍 Updating defense schedule for defense ${defenseId} to ${defenseDate}`);
  try {
    const user = RemoteServices.getCurrentUser();
    if (!user || user.type !== 'COORDINATOR') {
      throw new Error('This action can only be performed by a coordinator');
    }
    
    // Validate that the defense date is in the future
    const selectedDate = new Date(defenseDate);
    const now = new Date();
    if (selectedDate <= now) {
      throw new Error('Defense date must be in the future');
    }
    
    // Make the API call to update the existing defense
    const response = await httpClient.post(`/api/defense/${defenseId}/schedule`, null, {
      params: {
        defenseDate: defenseDate,
        role: 'COORDINATOR'
      }
    });
    
    if (DEBUG_MODE) console.log('Defense scheduling update response:', response);
    
    return response;
  } catch (error) {
    // Extract the error message from the response if available
    let errorMessage = 'Error updating defense schedule';
    
    if (error.response && error.response.data) {
      if (error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error;
      }
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    console.error(`❌ Error updating defense schedule: ${errorMessage}`);
    throw new Error(errorMessage);
  }
}

static async updateDefenseStatuses(): Promise<void> {
  if (DEBUG_MODE) console.log('🔍 Updating defense statuses');
  try {
    const user = RemoteServices.getCurrentUser();
    if (!user || (user.type !== 'COORDINATOR' && user.type !== 'STAFF')) {
      throw new Error('This action can only be performed by coordinator or staff');
    }
    
    await httpClient.post('/api/defense/update-statuses', null, {
      params: {
        role: user.type
      }
    });
  } catch (error) {
    console.error('❌ Error updating defense statuses:', error);
    throw error;
  }
}

static async updatePerson(personId: number, personData: PersonDto): Promise<PersonDto> {
  if (DEBUG_MODE) console.log(`💾 Updating person with ID: ${personId}`, personData);
  
  try {
    const user = RemoteServices.getCurrentUser();
    if (!user || (user.type !== 'COORDINATOR' && user.type !== 'STAFF')) {
      throw new Error('Esta ação só pode ser realizada por coordenador ou staff');
    }

    const response = await httpClient.put(`/people/${personId}`, personData, {
      params: {
        role: user.type
      }
    });

    return response;
  } catch (error) {
    console.error(`❌ Erro ao atualizar pessoa com ID ${personId}:`, error);
    throw error;
  }
}// ---------------------
  //     ERROR HANDLING
  // ---------------------
  static async errorMessage(error: any): Promise<string> {
    if (error.message === 'Network Error') {
      return 'Unable to connect to the server';
    } else if (error.message.startsWith('timeout')) {
      return 'Request timeout - Server took too long to respond';
    } else {
      return error.response?.data?.message ?? 'Unknown Error';
    }
  }

  static async handleError(error: any): Promise<never> {
    // Create a domain-specific error object
    const deiErr = new DeiError(
      await RemoteServices.errorMessage(error),
      error.response?.data?.code ?? -1
    );

    // Show error in the UI
    const appearance = useAppearanceStore();
    appearance.pushError(deiErr);
    appearance.loading = false;

    // Show error message in debug mode
    if (DEBUG_MODE) console.error("❌ Error details:", error);

    throw deiErr;
  }
}

// -------------------------
//   AXIOS INTERCEPTORS
// -------------------------
httpClient.interceptors.request.use(
  (request) => {
    if (DEBUG_MODE) console.log("🔍 Request made with:", request);
    return request;
  },
  RemoteServices.handleError
);

httpClient.interceptors.response.use(
  (response) => {
    if (DEBUG_MODE) console.log("✅ Response received:", response);
    if (response.status === 204) {
      return { ...response, noContent: true };
    }
    return response.data;
  },
  (error) => {
    // If there's no status (e.g., network error), treat it as critical
    const status = error.response?.status;

    // 1) If it's a server error (>= 500) or a network error, show the global error
    if (!status || status >= 500) {
      return RemoteServices.handleError(error);
    }

    // 2) If it's a 4xx error (e.g., 400 or 401), just rethrow
    //    Let the calling code handle the error message in the component
    return Promise.reject(error);
  }
);