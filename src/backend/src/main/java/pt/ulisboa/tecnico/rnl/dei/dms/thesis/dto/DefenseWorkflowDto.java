package pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto;

import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class DefenseWorkflowDto {
    private Long id;
    private Long studentId;
    private Long thesisId;
    private String status;
    private LocalDateTime defenseDate;
    private BigDecimal grade;
    
    // Default constructor for deserialization
    public DefenseWorkflowDto() {
    }
    
    // Constructor for creating DTO from entity
    public DefenseWorkflowDto(DefenseWorkflow defenseWorkflow) {
        this.id = defenseWorkflow.getId();
        
        if (defenseWorkflow.getStudent() != null) {
            this.studentId = defenseWorkflow.getStudent().getId();
        }
        
        if (defenseWorkflow.getThesis() != null) {
            this.thesisId = defenseWorkflow.getThesis().getId();
        }
        
        this.status = defenseWorkflow.getStatus().name();
        this.defenseDate = defenseWorkflow.getDefenseDate();
        this.grade = defenseWorkflow.getGrade();
    }
}