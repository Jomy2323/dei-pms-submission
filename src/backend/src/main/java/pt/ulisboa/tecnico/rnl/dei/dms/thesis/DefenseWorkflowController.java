package pt.ulisboa.tecnico.rnl.dei.dms.thesis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow.DefenseStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.DefenseWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.service.DefenseWorkflowService;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/defense")
public class DefenseWorkflowController {

    @Autowired
    private DefenseWorkflowService defenseService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DefenseWorkflowRepository defenseRepository; 
    /**
     * Get all defense workflows
     */
    @GetMapping
    public ResponseEntity<List<DefenseWorkflowDto>> getAllDefenses() {
        List<DefenseWorkflow> defenses = defenseService.findAll();
        return ResponseEntity.ok(defenseService.toDtoList(defenses));
    }

    /**
     * Get defense by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DefenseWorkflowDto> getDefenseById(@PathVariable Long id) {
        DefenseWorkflow defense = defenseService.findById(id);
        return ResponseEntity.ok(defenseService.toDto(defense));
    }

    /**
     * Get defenses by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DefenseWorkflowDto>> getDefensesByStatus(@PathVariable String status) {
        try {
            DefenseStatus defenseStatus = DefenseStatus.valueOf(status.toUpperCase());
            List<DefenseWorkflow> defenses = defenseService.findByStatus(defenseStatus);
            return ResponseEntity.ok(defenseService.toDtoList(defenses));
        } catch (IllegalArgumentException e) {
            throw new DEIException(ErrorMessage.INVALID_DEFENSE_STATE);
        }
    }

    /**
     * Schedule defense (Coordinator action)
     */
    @PostMapping("/schedule")
    public ResponseEntity<DefenseWorkflowDto> scheduleDefense(
            @RequestParam Long thesisId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime defenseDate,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.COORDINATOR) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        DefenseWorkflow defense = defenseService.scheduleDefense(thesisId, defenseDate);
        return ResponseEntity.ok(defenseService.toDto(defense));
    }

    /**
     * Manually set defense to UNDER_REVIEW (for testing or bypassing automatic update)
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<DefenseWorkflowDto> setUnderReview(
            @PathVariable Long id,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.COORDINATOR) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        DefenseWorkflow defense = defenseService.setUnderReview(id);
        return ResponseEntity.ok(defenseService.toDto(defense));
    }

    /**
     * Assign grade and submit to Fenix (Coordinator action)
     */
    @PostMapping("/{id}/grade")
    public ResponseEntity<DefenseWorkflowDto> assignGradeAndSubmit(
            @PathVariable Long id,
            @RequestParam BigDecimal grade,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.COORDINATOR) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        DefenseWorkflow defense = defenseService.assignGradeAndSubmit(id, grade);
        return ResponseEntity.ok(defenseService.toDto(defense));
    }

    /**
     * Revert to previous state (role-based)
     */
    @PostMapping("/{id}/revert")
    public ResponseEntity<DefenseWorkflowDto> revertToPreviousState(
            @PathVariable Long id,
            @RequestParam Person.PersonType role) {
        
        DefenseWorkflow defense = defenseService.revertToPreviousState(id, role);
        return ResponseEntity.ok(defenseService.toDto(defense));
    }

    /**
     * Update defense statuses (trigger automatic transition from SCHEDULED to UNDER_REVIEW)
     * This would typically be called by a scheduled task, but can be exposed as an endpoint for testing
     */
    @PostMapping("/update-statuses")
    public ResponseEntity<Void> updateDefenseStatuses(@RequestParam Person.PersonType role) {
        if (role != Person.PersonType.COORDINATOR && role != Person.PersonType.STAFF) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        defenseService.updateDefenseStatuses();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<DefenseWorkflowDto> getDefenseByStudentId(@PathVariable Long studentId) {
        Person student = personRepository.findById(studentId)
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, studentId.toString()));
        
        return defenseRepository.findByStudent(student)
                .map(defense -> ResponseEntity.ok(defenseService.toDto(defense)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update the schedule of an existing defense (Coordinator action)
     */
    @PostMapping("/{id}/schedule")
    public ResponseEntity<DefenseWorkflowDto> updateDefenseSchedule(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime defenseDate,
            @RequestParam Person.PersonType role) {
        
        if (role != Person.PersonType.COORDINATOR) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
        
        DefenseWorkflow defense = defenseService.findById(id);
        
        // Verify this is an UNSCHEDULED defense
        if (defense.getStatus() != DefenseStatus.UNSCHEDULED) {
            throw new DEIException(ErrorMessage.INVALID_DEFENSE_STATE, 
                    "Only UNSCHEDULED defenses can be scheduled");
        }
        
        // Update the defense status and date
        defense.setStatus(DefenseStatus.DEFENSE_SCHEDULED);
        defense.setDefenseDate(defenseDate);
        
        // Save the updated defense
        defense = defenseRepository.save(defense);
        
        return ResponseEntity.ok(defenseService.toDto(defense));
    }
}