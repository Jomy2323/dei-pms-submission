// src/models/DefenseDto.ts
export default interface DefenseDto {
  id: number;
  status: string;
  defenseDate: string;
  studentId?: number;
  thesisId?: number;
  grade?: number;
}