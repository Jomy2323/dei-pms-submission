// src/models/ThesisDto.ts
import type PersonDto from '@/models/PersonDto'

export default interface ThesisDto {
  id: number;
  title: string;
  status: string;
  submissionDate: string;
  studentId?: number;
  documentPath?: string;
  juryMemberIds?: number[];
  juryPresidentId?: number;
  juryMembers?: PersonDto[];
  juryPresident?: PersonDto;
}