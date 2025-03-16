package pt.ulisboa.tecnico.rnl.dei.dms.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto.DashboardDTO;
import pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto.StudentDetailDTO;
import pt.ulisboa.tecnico.rnl.dei.dms.dashboard.dto.StudentWorkflowDTO;
import pt.ulisboa.tecnico.rnl.dei.dms.dashboard.service.DashboardService;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get dashboard data based on user role
     */
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(@RequestParam String role) {
        try {
            Person.PersonType personType = Person.PersonType.fromString(role);
            DashboardDTO dashboard = dashboardService.getDashboardForRole(personType);
            return ResponseEntity.ok(dashboard);
        } catch (IllegalArgumentException e) {
            throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, role);
        }
    }

    /**
     * Get detailed information for a specific student
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentDetailDTO> getStudentDetail(@PathVariable Long studentId) {
        StudentDetailDTO detail = dashboardService.getStudentDetail(studentId);
        return ResponseEntity.ok(detail);
    }

    /**
     * Filter students by workflow status
     */
    @GetMapping("/filter")
    public ResponseEntity<List<StudentWorkflowDTO>> filterStudentsByWorkflowStatus(
            @RequestParam String workflowType,
            @RequestParam String status) {
        
        if (!workflowType.equalsIgnoreCase("thesis") && !workflowType.equalsIgnoreCase("defense")) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_TYPE);
        }
        
        List<StudentWorkflowDTO> students = dashboardService.filterStudentsByWorkflowStatus(
                workflowType, status);
        
        return ResponseEntity.ok(students);
    }
}