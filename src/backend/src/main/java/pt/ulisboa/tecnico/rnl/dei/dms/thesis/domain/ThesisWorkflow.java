package pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.*;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.ThesisWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.converter.ThesisStatusConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "thesis_workflows")
public class ThesisWorkflow {
    
    public enum ThesisStatus {
        PROPOSAL_SUBMITTED("Proposta de Júri Submetida"),
        APPROVED_BY_SC("Aprovado pelo SC"),
        JURY_PRESIDENT_ASSIGNED("Presidente do Júri Atribuído"),
        DOCUMENT_SIGNED("Documento Assinado"),
        SUBMITTED_TO_FENIX("Submetido ao Fenix");
        
        private final String databaseValue;
        
        ThesisStatus(String databaseValue) {
            this.databaseValue = databaseValue;
        }
        
        public String getDatabaseValue() {
            return databaseValue;
        }
        
        public static ThesisStatus fromDatabaseValue(String value) {
            for (ThesisStatus status : ThesisStatus.values()) {
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
    
    @Column(name = "status", nullable = false)
    @Convert(converter = ThesisStatusConverter.class)
    private ThesisStatus status;
    
    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;
    
    @Column(name = "jury_member_ids", columnDefinition = "text[]")
    private String[] juryMemberIds;
    
    @ManyToOne
    @JoinColumn(name = "jury_president_id")
    private Person juryPresident;
    
    @Column(name = "document_path")
    private String documentPath;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    public ThesisWorkflow() {
    }
    
    public ThesisWorkflow(Person student, ThesisStatus status, LocalDateTime submissionDate, String title, List<Long> juryMemberIds) {
        this.student = student;
        this.status = status;
        this.submissionDate = submissionDate;
        this.title = title;
        this.juryMemberIds = juryMemberIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
    }
    
    public ThesisWorkflow(ThesisWorkflowDto dto, PersonRepository personRepository) {
        // Handle the required fields
        this.status = ThesisStatus.valueOf(dto.getStatus());
        this.submissionDate = dto.getSubmissionDate();
        this.title = dto.getTitle();
        
        // Handle the Student reference
        if (dto.getStudentId() != null) {
            this.student = personRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, 
                    "Student with ID " + dto.getStudentId() + " not found"));
        }
        
        // Handle jury member IDs as a text array
        if (dto.getJuryMemberIds() != null && !dto.getJuryMemberIds().isEmpty()) {
            this.juryMemberIds = dto.getJuryMemberIds().stream()
                .map(String::valueOf)
                .toArray(String[]::new);
        }
        
        // Handle the Jury President reference
        if (dto.getJuryPresidentId() != null) {
            this.juryPresident = personRepository.findById(dto.getJuryPresidentId())
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, 
                    "Jury president with ID " + dto.getJuryPresidentId() + " not found"));
        }
        
        // Set document path if provided
        this.documentPath = dto.getDocumentPath();
    }
    
    // Utility method to get jury member IDs as a list
    public List<Long> getJuryMemberIdsList() {
        if (this.juryMemberIds == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(this.juryMemberIds)
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }
}