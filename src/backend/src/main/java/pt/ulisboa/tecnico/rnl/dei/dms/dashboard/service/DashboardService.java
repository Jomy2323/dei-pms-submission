package pt.ulisboa.tecnico.rnl.dei.dms.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.DefenseWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.ThesisWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.DefenseWorkflowRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.ThesisWorkflowRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto.DashboardDTO;
import pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto.StudentWorkflowDTO;
import pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto.StudentDetailDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ThesisWorkflowRepository thesisRepository;

    @Autowired
    private DefenseWorkflowRepository defenseRepository;

    /**
     * Get dashboard data based on user role
     */
    public DashboardDTO getDashboardForRole(Person.PersonType role) {
        DashboardDTO dashboard = new DashboardDTO();

        // Get all students and their workflows
        List<StudentWorkflowDTO> students = getAllStudentsWithWorkflows();
        dashboard.setStudents(students);

        // Role-specific filtering
        switch (role) {
            case COORDINATOR:
                dashboard.setThesesNeedingAction(
                    thesisRepository.findByStatus(ThesisWorkflow.ThesisStatus.APPROVED_BY_SC)
                        .stream()
                        .map(ThesisWorkflow::getId)
                        .collect(Collectors.toList())
                );
                dashboard.setDefensesNeedingAction(
                    defenseRepository.findByStatus(DefenseWorkflow.DefenseStatus.UNDER_REVIEW)
                        .stream()
                        .map(DefenseWorkflow::getId)
                        .collect(Collectors.toList())
                );
                break;
            case SC:
                dashboard.setThesesNeedingAction(
                    thesisRepository.findByStatus(ThesisWorkflow.ThesisStatus.PROPOSAL_SUBMITTED)
                        .stream()
                        .map(ThesisWorkflow::getId)
                        .collect(Collectors.toList())
                );
                break;
            case STAFF:
                dashboard.setThesesNeedingAction(
                    thesisRepository.findByStatus(ThesisWorkflow.ThesisStatus.DOCUMENT_SIGNED)
                        .stream()
                        .map(ThesisWorkflow::getId)
                        .collect(Collectors.toList())
                );
                break;
            case STUDENT:
            case TEACHER:
                // No extra data needed
                break;
        }

        return dashboard;
    }

    /**
     * Get detailed information for a specific student
     */
    public StudentDetailDTO getStudentDetail(Long studentId) {
        // Validate student existence
        Person student = personRepository.findById(studentId)
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, studentId.toString()));

        if (student.getType() != Person.PersonType.STUDENT) {
            throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, "Not a student");
        }

        // Get workflows
        Optional<ThesisWorkflow> thesis = thesisRepository.findByStudent(student);
        Optional<DefenseWorkflow> defense = defenseRepository.findByStudent(student);

        // Convert entity to DTO before setting
        StudentDetailDTO detail = new StudentDetailDTO();
        detail.setId(student.getId());
        detail.setName(student.getName());
        detail.setIstId(student.getIstId());
        detail.setEmail(student.getEmail());
        detail.setType(student.getType().name());

        thesis.ifPresent(tw -> detail.setThesisWorkflow(new ThesisWorkflowDto(tw)));
        defense.ifPresent(dw -> detail.setDefenseWorkflow(new DefenseWorkflowDto(dw)));

        return detail;
    }

    /**
     * Filter students by thesis or defense status
     */
    public List<StudentWorkflowDTO> filterStudentsByWorkflowStatus(String workflowType, String status) {
        List<Person> students = personRepository.findByType(Person.PersonType.STUDENT);
        List<StudentWorkflowDTO> result = new ArrayList<>();

        for (Person student : students) {
            Optional<ThesisWorkflow> thesis = thesisRepository.findByStudent(student);
            Optional<DefenseWorkflow> defense = defenseRepository.findByStudent(student);

            boolean matchesFilter = false;

            if ("thesis".equalsIgnoreCase(workflowType) && thesis.isPresent()) {
                matchesFilter = thesis.get().getStatus().name().equalsIgnoreCase(status);
            } else if ("defense".equalsIgnoreCase(workflowType) && defense.isPresent()) {
                matchesFilter = defense.get().getStatus().name().equalsIgnoreCase(status);
            }

            if (matchesFilter) {
                StudentWorkflowDTO dto = new StudentWorkflowDTO();
                dto.setStudentId(student.getId());
                dto.setName(student.getName());
                dto.setIstId(student.getIstId());
                dto.setEmail(student.getEmail());
                dto.setThesisStatus(thesis.map(t -> t.getStatus().name()).orElse(null));
                dto.setDefenseStatus(defense.map(d -> d.getStatus().name()).orElse(null));
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * Helper method to get all students with their workflow statuses
     */
    private List<StudentWorkflowDTO> getAllStudentsWithWorkflows() {
        List<Person> students = personRepository.findByType(Person.PersonType.STUDENT);
        List<StudentWorkflowDTO> result = new ArrayList<>();

        for (Person student : students) {
            Optional<ThesisWorkflow> thesis = thesisRepository.findByStudent(student);
            Optional<DefenseWorkflow> defense = defenseRepository.findByStudent(student);

            StudentWorkflowDTO dto = new StudentWorkflowDTO();
            dto.setStudentId(student.getId());
            dto.setName(student.getName());
            dto.setIstId(student.getIstId());
            dto.setEmail(student.getEmail());
            dto.setThesisStatus(thesis.map(t -> t.getStatus().name()).orElse(null));
            dto.setDefenseStatus(defense.map(d -> d.getStatus().name()).orElse(null));

            result.add(dto);
        }

        return result;
    }
}
