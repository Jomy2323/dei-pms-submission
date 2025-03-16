package pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto;

import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;

import java.util.List;

/**
 * DTO for student with workflow status information
 */
@Data
public class StudentWorkflowDTO {
    private Long studentId;
    private String name;
    private String istId;
    private String email;
    private String thesisStatus;
    private String defenseStatus;
}
