package pt.ulisboa.tecnico.rnl.dei.dms.thesis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow.ThesisStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.ThesisWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.ThesisSubmissionDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.service.ThesisWorkflowService;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.*;
import org.springframework.util.StringUtils;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/thesis")
public class ThesisWorkflowController {

    @Autowired
    private ThesisWorkflowService thesisService;

    @Autowired
    private PersonRepository personRepository;  

    @Autowired
    private ThesisWorkflowRepository thesisRepository; 
    /**
     * Get all thesis workflows
     */
    @GetMapping
    public ResponseEntity<List<ThesisWorkflowDto>> getAllTheses() {
        List<ThesisWorkflow> theses = thesisService.findAll();
        return ResponseEntity.ok(thesisService.toDtoList(theses));
    }

    /**
     * Get theses by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ThesisWorkflowDto>> getThesesByStatus(@PathVariable String status) {
        try {
            ThesisStatus thesisStatus = ThesisStatus.valueOf(status.toUpperCase());
            List<ThesisWorkflow> theses = thesisService.findByStatus(thesisStatus);
            return ResponseEntity.ok(thesisService.toDtoList(theses));
        } catch (IllegalArgumentException e) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE);
        }
    }

    /**
     * Submit jury proposal (Student action)
     */
    @PostMapping("/submit")
    public ResponseEntity<ThesisWorkflowDto> submitJuryProposal(
            @RequestParam Long studentId,
            @RequestBody ThesisSubmissionDto request) {
        
        ThesisWorkflow thesis = thesisService.submitJuryProposal(
            studentId, 
            request.getJuryMemberIds(), 
            request.getTitle()
        );
        return ResponseEntity.ok(thesisService.toDto(thesis));
    }

    /**
     * Approve by Scientific Committee (SC action)
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ThesisWorkflowDto> approveByScientificCommittee(
            @PathVariable Long id,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.SC) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        ThesisWorkflow thesis = thesisService.approveByScientificCommittee(id);
        return ResponseEntity.ok(thesisService.toDto(thesis));
    }

    /**
     * Assign jury president (Coordinator action)
     */
    @PostMapping("/{id}/president")
    public ResponseEntity<ThesisWorkflowDto> assignJuryPresident(
            @PathVariable Long id,
            @RequestParam Long presidentId,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.COORDINATOR) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        ThesisWorkflow thesis = thesisService.assignJuryPresident(id, presidentId);
        return ResponseEntity.ok(thesisService.toDto(thesis));
    }

    /**
     * Upload signed document (Coordinator action)
     */
    @PostMapping("/{id}/document")
    public ResponseEntity<ThesisWorkflowDto> uploadSignedDocument(
            @PathVariable Long id,
            @RequestParam String documentPath,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.COORDINATOR) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        ThesisWorkflow thesis = thesisService.uploadSignedDocument(id, documentPath);
        return ResponseEntity.ok(thesisService.toDto(thesis));
    }

    /**
     * Submit to Fenix (Staff action)
     */
    @PostMapping("/{id}/fenix")
    public ResponseEntity<ThesisWorkflowDto> submitToFenix(
            @PathVariable Long id,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.STAFF) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        ThesisWorkflow thesis = thesisService.submitToFenix(id);
        return ResponseEntity.ok(thesisService.toDto(thesis));
    }

    /**
     * Revert to previous state (role-based)
     */
    @PostMapping("/{id}/revert")
    public ResponseEntity<ThesisWorkflowDto> revertToPreviousState(
            @PathVariable Long id,
            @RequestParam Person.PersonType role) {
        
        ThesisWorkflow thesis = thesisService.revertToPreviousState(id, role);
        return ResponseEntity.ok(thesisService.toDto(thesis));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ThesisWorkflowDto> getThesisByStudentId(@PathVariable Long studentId) {
        Person student = personRepository.findById(studentId)
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, studentId.toString()));
        
        return thesisRepository.findByStudent(student)
                .map(thesis -> ResponseEntity.ok(thesisService.toDto(thesis)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Reject a thesis proposal (SC action)
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectThesisProposal(
            @PathVariable Long id,
            @RequestParam Person.PersonType role,
            @RequestBody(required = false) Map<String, String> payload) {
        
        if (role != Person.PersonType.SC) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        // Extract comments if provided
        String comments = payload != null ? payload.get("comments") : null;
        
        thesisService.rejectThesisProposal(id, comments);
        return ResponseEntity.ok().build(); // Return 200 OK with no content
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getThesisById(@PathVariable Long id) {
        ThesisWorkflow thesis = thesisService.findById(id);
        
        // Create a detailed response map
        Map<String, Object> response = new HashMap<>(toDto(thesis));
        
        // Fetch and add student details
        if (thesis.getStudent() != null) {
            Person student = personRepository.findById(thesis.getStudent().getId())
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND));
            
            Map<String, Object> studentDetails = new HashMap<>();
            studentDetails.put("id", student.getId());
            studentDetails.put("name", student.getName());
            studentDetails.put("istId", student.getIstId());
            studentDetails.put("email", student.getEmail());
            
            response.put("student", studentDetails);
        }
        
        // Fetch and add jury members
        List<Map<String, Object>> juryMembers = thesis.getJuryMemberIdsList().stream()
            .map(memberId -> personRepository.findById(memberId)
                .map(member -> {
                    Map<String, Object> memberDetails = new HashMap<>();
                    memberDetails.put("id", member.getId());
                    memberDetails.put("name", member.getName());
                    memberDetails.put("email", member.getEmail());
                    return memberDetails;
                })
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        response.put("juryMembers", juryMembers);
        
        return ResponseEntity.ok(response);
    }
    
    // Helper method to convert thesis to basic DTO
    private Map<String, Object> toDto(ThesisWorkflow thesis) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", thesis.getId());
        dto.put("title", thesis.getTitle());
        dto.put("status", thesis.getStatus());
        dto.put("submissionDate", thesis.getSubmissionDate());
        dto.put("studentId", thesis.getStudent() != null ? thesis.getStudent().getId() : null);
        dto.put("juryMemberIds", thesis.getJuryMemberIdsList());
        return dto;
    }}