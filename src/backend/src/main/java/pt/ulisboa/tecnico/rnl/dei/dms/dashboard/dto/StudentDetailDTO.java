package pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto;

import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.DefenseWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.ThesisWorkflowDto;

/**
 * DTO for detailed student information including complete workflow objects
 */
@Data
public class StudentDetailDTO {
    private Long id;
    private String name;
    private String istId;
    private String email;
    private String type;
    private ThesisWorkflowDto thesisWorkflow;
    private DefenseWorkflowDto defenseWorkflow;

    public void setStudent(Person student) {
        if (student != null) {
            this.id = student.getId();
            this.name = student.getName();
            this.istId = student.getIstId();
            this.email = student.getEmail();
            this.type = student.getType().toString();
        }
    }
}
