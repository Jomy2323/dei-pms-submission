import { defineStore } from 'pinia'

export const useRoleStore = defineStore('role', {
  state: () => ({
    currentRole: 'staff',
  }),
  getters: {
    isStaff(): boolean {
      return this.currentRole === 'staff'
    },
    isStudent(): boolean {
      return this.currentRole === 'student'
    },
    isCoordinator(): boolean {
      return this.currentRole === 'coordinator'
    },
    isTeacher(): boolean {
      return this.currentRole === 'teacher'
    },
    currentActiveRole(): string {
        return this.currentRole
    }
  },
  persist: true
})