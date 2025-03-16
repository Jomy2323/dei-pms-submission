package pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto;

import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;

import java.util.List;

/**
 * DTO for dashboard data based on user role
 */
@Data
public class DashboardDTO {
    // List of all students with their workflow statuses
    private List<StudentWorkflowDTO> students;
    
    // List of thesis IDs needing action based on role
    private List<Long> thesesNeedingAction;
    
    // List of defense IDs needing action based on role
    private List<Long> defensesNeedingAction;
}


