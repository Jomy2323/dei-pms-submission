package pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.*;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.*;



import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThesisWorkflowDto {
    private Long id;
    private Long studentId;
    private String status;
    private LocalDateTime submissionDate;
    private List<Long> juryMemberIds;
    private Long juryPresidentId;
    private String documentPath;
    private String title;
    private PersonDto student;
    private List<PersonDto> juryMembers;

    // Constructor to convert from ThesisWorkflow entity
    public ThesisWorkflowDto(ThesisWorkflow thesisWorkflow) {
        this.id = thesisWorkflow.getId();
        this.studentId = thesisWorkflow.getStudent() != null ? thesisWorkflow.getStudent().getId() : null;
        this.status = thesisWorkflow.getStatus().name();
        this.submissionDate = thesisWorkflow.getSubmissionDate();
        this.juryMemberIds = thesisWorkflow.getJuryMemberIdsList();
        this.juryPresidentId = thesisWorkflow.getJuryPresident() != null ? thesisWorkflow.getJuryPresident().getId() : null;
        this.documentPath = thesisWorkflow.getDocumentPath();
        this.title = thesisWorkflow.getTitle();
        this.student = thesisWorkflow.getStudent() != null ? new PersonDto(thesisWorkflow.getStudent()) : null;
        this.juryMembers = thesisWorkflow.getJuryMemberIdsList().stream()
                .map(memberId -> new PersonDto(thesisWorkflow.getStudent())) // Ensure proper mapping
                .collect(Collectors.toList());
    }
}
