import { defineStore } from 'pinia'

export const useRoleStore = defineStore('role', {
  state: () => ({
    currentRole: '',
    user: null as any
  }),
  getters: {
    isHome: (state) => state.currentRole === 'HOME',
    isStaff: (state) => state.currentRole === 'STAFF',
    isStudent: (state) => state.currentRole === 'STUDENT',
    isCoordinator: (state) => state.currentRole === 'COORDINATOR',
    isTeacher: (state) => state.currentRole === 'TEACHER',
    isSC: (state) => state.currentRole === 'SC',
    currentActiveRole: (state) => state.currentRole
  },
  actions: {
    setRole(role: string, user:any) {
      // Convert role to uppercase to match backend expectations
      this.currentRole = role.toUpperCase();
      this.user = user;
    },
    logout(){
      this.currentRole = '';
      this.user = null;
    }
  },
  persist: true
})