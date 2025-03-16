package pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain;

import jakarta.persistence.*;
import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.*;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.DefenseWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.ThesisWorkflowRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.converter.DefenseStatusConverter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "defense_workflows")
public class DefenseWorkflow {
    
    public enum DefenseStatus {
        DEFENSE_SCHEDULED("Defesa Agendada"),
        UNDER_REVIEW("Em Revisão"),
        SUBMITTED_TO_FENIX("Submetido ao Fenix"),
        UNSCHEDULED("Por Agendar");
        
        private final String databaseValue;
        
        DefenseStatus(String databaseValue) {
            this.databaseValue = databaseValue;
        }
        
        public String getDatabaseValue() {
            return databaseValue;
        }
        
        public static DefenseStatus fromDatabaseValue(String value) {
            for (DefenseStatus status : DefenseStatus.values()) {
                if (status.getDatabaseValue().equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown database value: " + value);
        }
        
        @Override
        public String toString() {
            return databaseValue;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Person student;
    
    @OneToOne
    @JoinColumn(name = "thesis_id", nullable = false)
    private ThesisWorkflow thesis;
    
    @Column(name = "status", nullable = false)
    @Convert(converter = DefenseStatusConverter.class)
    private DefenseStatus status = DefenseStatus.UNSCHEDULED;
    
    @Column(name = "defense_date", nullable = true)
    private LocalDateTime defenseDate;
    
    @Column(name = "grade", precision = 4, scale = 2)
    private BigDecimal grade;
    
    public DefenseWorkflow() {
    }
    
    public DefenseWorkflow(Person student, ThesisWorkflow thesis, DefenseStatus status, LocalDateTime defenseDate) {
        this.student = student;
        this.thesis = thesis;
        this.status = status;
        this.defenseDate = defenseDate;
    }
    
    public DefenseWorkflow(DefenseWorkflowDto dto, PersonRepository personRepository, ThesisWorkflowRepository thesisRepository) {
        // Handle the required fields
        this.status = DefenseStatus.valueOf(dto.getStatus());
        this.defenseDate = dto.getDefenseDate();
        
        // Handle the Student reference
        if (dto.getStudentId() != null) {
            this.student = personRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, 
                    "Student with ID " + dto.getStudentId() + " not found"));
        }
        
        // Handle the Thesis reference
        if (dto.getThesisId() != null) {
            this.thesis = thesisRepository.findById(dto.getThesisId())
                .orElseThrow(() -> new DEIException(ErrorMessage.THESIS_NOT_FOUND, 
                    "Thesis with ID " + dto.getThesisId() + " not found"));
            
            // Validate that thesis is completed
            if (this.thesis.getStatus() != ThesisWorkflow.ThesisStatus.SUBMITTED_TO_FENIX) {
                throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Defense can only be created for a thesis with SUBMITTED_TO_FENIX status");
            }
            
            // Ensure student matches thesis student
            if (this.student != null && !this.student.equals(this.thesis.getStudent())) {
                throw new DEIException(ErrorMessage.VALIDATION_ERROR, 
                    "Student must match the thesis student");
            } else if (this.student == null) {
                // If student wasn't explicitly provided, use the one from thesis
                this.student = this.thesis.getStudent();
            }
        }
        
        // Set grade if provided
        if (dto.getGrade() != null) {
            // Validate grade is between 0 and 20
            if (dto.getGrade().compareTo(BigDecimal.ZERO) < 0 || 
                dto.getGrade().compareTo(new BigDecimal("20")) > 0) {
                throw new DEIException(ErrorMessage.VALIDATION_ERROR, 
                    "Grade must be between 0 and 20");
            }
            this.grade = dto.getGrade();
        }
    }
}