// src/models/StudentDto.ts
import type PersonDto from '@/models/PersonDto'
import type ThesisDto from '@/models/ThesisDto'
import type DefenseDto from '@/models/DefenseDto'

export default interface StudentDto {
  // Student details (can be nested or at root level)
  id?: number;
  name?: string;
  istId?: string;
  email?: string;
  type?: string;
  student?: PersonDto;  // Keep this for backward compatibility
  
  // Workflow information
  thesisWorkflow?: ThesisDto | null;
  defenseWorkflow?: DefenseDto | null;
}