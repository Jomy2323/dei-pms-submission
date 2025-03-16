package pt.ulisboa.tecnico.rnl.dei.dms.thesis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow.DefenseStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow.ThesisStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.DefenseWorkflowRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.ThesisWorkflowRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.DefenseWorkflowDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DefenseWorkflowService {

    @Autowired
    private DefenseWorkflowRepository defenseRepository;

    @Autowired
    private ThesisWorkflowRepository thesisRepository;

    /**
     * Find all defense workflows
     */
    public List<DefenseWorkflow> findAll() {
        return defenseRepository.findAll();
    }

    /**
     * Find defense workflow by ID
     */
    public DefenseWorkflow findById(Long id) {
        return defenseRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.DEFENSE_NOT_FOUND, id.toString()));
    }

    /**
     * Find defense workflow by student
     */
    public Optional<DefenseWorkflow> findByStudent(Person student) {
        return defenseRepository.findByStudent(student);
    }

    /**
     * Find defense workflow by thesis
     */
    public Optional<DefenseWorkflow> findByThesis(ThesisWorkflow thesis) {
        return defenseRepository.findByThesis(thesis);
    }

    /**
     * Find defense workflows by status
     */
    public List<DefenseWorkflow> findByStatus(DefenseStatus status) {
        return defenseRepository.findByStatus(status);
    }

    /**
     * Schedule a defense (Coordinator action)
     */
    @Transactional
    public DefenseWorkflow scheduleDefense(Long thesisId, LocalDateTime defenseDate) {
        // Validate thesis exists
        ThesisWorkflow thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new DEIException(ErrorMessage.THESIS_NOT_FOUND, thesisId.toString()));
        
        // Validate thesis is complete
        if (thesis.getStatus() != ThesisStatus.SUBMITTED_TO_FENIX) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Thesis workflow must be completed before scheduling defense");
        }
        
        // Validate defense date is in the future
        if (defenseDate.isBefore(LocalDateTime.now())) {
            throw new DEIException(ErrorMessage.VALIDATION_ERROR, 
                    "Defense date must be in the future");
        }
        
        // Check if defense already exists for this thesis
        if (defenseRepository.findByThesis(thesis).isPresent()) {
            throw new DEIException(ErrorMessage.DEFENSE_ALREADY_EXISTS, 
                    "Defense already exists for this thesis");
        }
        
        // Create new defense workflow
        DefenseWorkflow defense = new DefenseWorkflow();
        defense.setStudent(thesis.getStudent());
        defense.setThesis(thesis);
        defense.setDefenseDate(defenseDate);
        defense.setStatus(DefenseStatus.DEFENSE_SCHEDULED);
        
        return defenseRepository.save(defense);
    }

    /**
     * Update defenses to UNDER_REVIEW when defense date has passed
     * This would typically be called by a scheduled task
     */
    @Transactional
    public void updateDefenseStatuses() {
        List<DefenseWorkflow> scheduledDefenses = defenseRepository.findByDefenseDateBeforeAndStatus(
                LocalDateTime.now(), DefenseStatus.DEFENSE_SCHEDULED);
        
        for (DefenseWorkflow defense : scheduledDefenses) {
            defense.setStatus(DefenseStatus.UNDER_REVIEW);
            defenseRepository.save(defense);
        }
    }

    /**
     * Assign grade and submit to Fenix (Coordinator action)
     */
    @Transactional
    public DefenseWorkflow assignGradeAndSubmit(Long defenseId, BigDecimal grade) {
        DefenseWorkflow defense = findById(defenseId);
        
        // Validate current status
        if (defense.getStatus() != DefenseStatus.UNDER_REVIEW) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Defense is not in UNDER_REVIEW state");
        }
        
        // Validate grade is between 0 and 20
        if (grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(new BigDecimal("20")) > 0) {
            throw new DEIException(ErrorMessage.VALIDATION_ERROR, 
                    "Grade must be between 0 and 20");
        }
        
        defense.setGrade(grade);
        defense.setStatus(DefenseStatus.SUBMITTED_TO_FENIX);
        return defenseRepository.save(defense);
    }

    /**
     * Revert to previous state with role-based authorization
     */
    @Transactional
    public DefenseWorkflow revertToPreviousState(Long defenseId, Person.PersonType role) {
        DefenseWorkflow defense = findById(defenseId);
        
        // Determine previous state based on current state
        DefenseStatus currentStatus = defense.getStatus();
        DefenseStatus previousStatus;
        
        switch (currentStatus) {
            case SUBMITTED_TO_FENIX:
                // Only COORDINATOR can revert from SUBMITTED_TO_FENIX
                if (role != Person.PersonType.COORDINATOR) {
                    throw new DEIException(ErrorMessage.UNAUTHORIZED, 
                            "Only coordinators can revert from SUBMITTED_TO_FENIX status");
                }
                previousStatus = DefenseStatus.UNDER_REVIEW;
                break;
                
            case UNDER_REVIEW:
                // Only COORDINATOR can revert from UNDER_REVIEW
                if (role != Person.PersonType.COORDINATOR) {
                    throw new DEIException(ErrorMessage.UNAUTHORIZED, 
                            "Only coordinators can revert from UNDER_REVIEW status");
                }
                previousStatus = DefenseStatus.DEFENSE_SCHEDULED;
                break;
                
            default:
                throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                        "Cannot revert from DEFENSE_SCHEDULED state");
        }
        
        defense.setStatus(previousStatus);
        return defenseRepository.save(defense);
    }
    
    /**
     * Manually set defense to UNDER_REVIEW state
     * This can be used for testing or to bypass the automatic update
     */
    @Transactional
    public DefenseWorkflow setUnderReview(Long defenseId) {
        DefenseWorkflow defense = findById(defenseId);
        
        if (defense.getStatus() != DefenseStatus.DEFENSE_SCHEDULED) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Defense is not in DEFENSE_SCHEDULED state");
        }
        
        defense.setStatus(DefenseStatus.UNDER_REVIEW);
        return defenseRepository.save(defense);
    }
    
    /**
     * Convert entity to DTO
     */
    public DefenseWorkflowDto toDto(DefenseWorkflow defense) {
        return new DefenseWorkflowDto(defense);
    }
    
    /**
     * Convert list of entities to DTOs
     */
    public List<DefenseWorkflowDto> toDtoList(List<DefenseWorkflow> defenses) {
        return defenses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}