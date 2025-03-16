package pt.ulisboa.tecnico.rnl.dei.dms.thesis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow.ThesisStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.ThesisWorkflowRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto.ThesisWorkflowDto;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository.DefenseWorkflowRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow.DefenseStatus;



import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import java.util.Objects;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto; 


@Service
public class ThesisWorkflowService {

    @Autowired
    private ThesisWorkflowRepository thesisRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DefenseWorkflowRepository defenseRepository;


    /**
     * Find all thesis workflows
     */
    public List<ThesisWorkflow> findAll() {
        return thesisRepository.findAll();
    }

    /**
     * Find thesis workflow by ID
     */
    public ThesisWorkflow findById(Long id) {
        return thesisRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.THESIS_NOT_FOUND, id.toString()));
    }

    /**
     * Find thesis workflows by status
     */
    public List<ThesisWorkflow> findByStatus(ThesisStatus status) {
        return thesisRepository.findByStatus(status);
    }

    /**
     * Submit a new thesis jury proposal (Student action)
     */
    @Transactional
    public ThesisWorkflow submitJuryProposal(Long studentId, List<Long> juryMemberIds, String title) {
        // Validate input parameters
        if (studentId == null) {
            throw new DEIException(ErrorMessage.VALIDATION_ERROR, "Student ID cannot be null");
        }

        // Validate title first
        if (!StringUtils.hasText(title) || title.trim().length() < 3) {
            throw new DEIException(ErrorMessage.VALIDATION_ERROR, "Thesis title must be at least 3 characters long");
        }

        // Ensure juryMemberIds is not null
        if (juryMemberIds == null) {
            throw new DEIException(ErrorMessage.VALIDATION_ERROR, "Jury member IDs cannot be null");
        }

        // Validate jury members count
        if (juryMemberIds.isEmpty() || juryMemberIds.size() > 5) {
            throw new DEIException(ErrorMessage.VALIDATION_ERROR, "Jury must have between 1 and 5 members");
        }

        // Validate student exists and is a student
        Person student = personRepository.findById(studentId)
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, studentId.toString()));
        
        if (student.getType() != Person.PersonType.STUDENT) {
            throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, "Not a student");
        }

        // Check if student already has a thesis
        if (thesisRepository.findByStudent(student).isPresent()) {
            throw new DEIException(ErrorMessage.THESIS_ALREADY_EXISTS, "Student already has a thesis");
        }

        // Validate jury members
        List<Person> juryMembers = personRepository.findAllById(juryMemberIds);
        
        // Validate we found all jury members
        if (juryMembers.size() != juryMemberIds.size()) {
            throw new DEIException(ErrorMessage.PERSON_NOT_FOUND, "One or more jury members not found");
        }
        
        // Validate all jury members are professors
        for (Person member : juryMembers) {
            if (member.getType() != Person.PersonType.TEACHER) {
                throw new DEIException(ErrorMessage.VALIDATION_ERROR, 
                        "All jury members must be teachers");
            }
        }

        // Create new thesis workflow
        ThesisWorkflow thesis = new ThesisWorkflow(
            student, 
            ThesisStatus.PROPOSAL_SUBMITTED, 
            LocalDateTime.now(), 
            title, 
            juryMemberIds
        );

        return thesisRepository.save(thesis);
    }

    /**
     * Approve by Scientific Committee (SC action)
     */
    @Transactional
    public ThesisWorkflow approveByScientificCommittee(Long thesisId) {
        ThesisWorkflow thesis = findById(thesisId);
        
        // Validate current status
        if (thesis.getStatus() != ThesisStatus.PROPOSAL_SUBMITTED) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Thesis is not in PROPOSAL_SUBMITTED state");
        }
        
        thesis.setStatus(ThesisStatus.APPROVED_BY_SC);
        return thesisRepository.save(thesis);
    }

    /**
     * Coordinator assigns jury president (Coordinator action)
     */
    @Transactional
    public ThesisWorkflow assignJuryPresident(Long thesisId, Long presidentId) {
        ThesisWorkflow thesis = findById(thesisId);
        
        // Validate current status
        if (thesis.getStatus() != ThesisStatus.APPROVED_BY_SC) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Thesis is not in APPROVED_BY_SC state");
        }
        
        // Validate president exists
        Person president = personRepository.findById(presidentId)
                .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, presidentId.toString()));
        
        // Validate president is a jury member
        List<Long> juryMemberIds = thesis.getJuryMemberIdsList();
        if (!juryMemberIds.contains(presidentId)) {
            throw new DEIException(ErrorMessage.VALIDATION_ERROR, 
                    "Jury president must be a jury member");
        }
        
        thesis.setJuryPresident(president);
        thesis.setStatus(ThesisStatus.JURY_PRESIDENT_ASSIGNED);
        return thesisRepository.save(thesis);
    }

    /**
     * Coordinator uploads signed document (Coordinator action)
     */
    @Transactional
    public ThesisWorkflow uploadSignedDocument(Long thesisId, String documentPath) {
        ThesisWorkflow thesis = findById(thesisId);
        
        // Validate current status
        if (thesis.getStatus() != ThesisStatus.JURY_PRESIDENT_ASSIGNED) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Thesis is not in JURY_PRESIDENT_ASSIGNED state");
        }
        
        thesis.setDocumentPath(documentPath);
        thesis.setStatus(ThesisStatus.DOCUMENT_SIGNED);
        return thesisRepository.save(thesis);
    }

    @Transactional
    public ThesisWorkflow submitToFenix(Long thesisId) {
        ThesisWorkflow thesis = findById(thesisId);
        
        // Validate current status
        if (thesis.getStatus() != ThesisStatus.DOCUMENT_SIGNED) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Thesis is not in DOCUMENT_SIGNED state");
        }
        
        // Update thesis status
        thesis.setStatus(ThesisStatus.SUBMITTED_TO_FENIX);
        thesis = thesisRepository.save(thesis);
        
        // Check if defense workflow already exists
        Optional<DefenseWorkflow> existingDefense = defenseRepository.findByThesis(thesis);
        
        if (!existingDefense.isPresent()) {
            // Create new defense workflow
            DefenseWorkflow defense = new DefenseWorkflow();
            defense.setStudent(thesis.getStudent());
            defense.setThesis(thesis);
            // Explicitly set to null to indicate it's not yet scheduled
            defense.setStatus(DefenseStatus.UNSCHEDULED);
            defense.setDefenseDate(null);
            defense.setGrade(null);
            
            defenseRepository.save(defense);
        }
        
        return thesis;
    }
    /**
     * Revert to previous state with role-based authorization
     */
    @Transactional
    public ThesisWorkflow revertToPreviousState(Long thesisId, Person.PersonType role) {
        ThesisWorkflow thesis = findById(thesisId);
        
        // Determine previous state based on current state
        ThesisStatus currentStatus = thesis.getStatus();
        ThesisStatus previousStatus;
        
        switch (currentStatus) {
            case SUBMITTED_TO_FENIX:
                // Only STAFF can revert from SUBMITTED_TO_FENIX
                if (role != Person.PersonType.STAFF) {
                    throw new DEIException(ErrorMessage.UNAUTHORIZED, 
                            "Only staff can revert from SUBMITTED_TO_FENIX status");
                }
                previousStatus = ThesisStatus.DOCUMENT_SIGNED;
                break;
                
            case DOCUMENT_SIGNED:
                // Only COORDINATOR can revert from DOCUMENT_SIGNED
                if (role != Person.PersonType.COORDINATOR) {
                    throw new DEIException(ErrorMessage.UNAUTHORIZED, 
                            "Only coordinators can revert from DOCUMENT_SIGNED status");
                }
                previousStatus = ThesisStatus.JURY_PRESIDENT_ASSIGNED;
                break;
                
            case JURY_PRESIDENT_ASSIGNED:
                // Only COORDINATOR can revert from JURY_PRESIDENT_ASSIGNED
                if (role != Person.PersonType.COORDINATOR) {
                    throw new DEIException(ErrorMessage.UNAUTHORIZED, 
                            "Only coordinators can revert from JURY_PRESIDENT_ASSIGNED status");
                }
                previousStatus = ThesisStatus.APPROVED_BY_SC;
                break;
                
            case APPROVED_BY_SC:
                // Only SC can revert from APPROVED_BY_SC
                if (role != Person.PersonType.SC) {
                    throw new DEIException(ErrorMessage.UNAUTHORIZED, 
                            "Only scientific committee can revert from APPROVED_BY_SC status");
                }
                previousStatus = ThesisStatus.PROPOSAL_SUBMITTED;
                break;
                
            default:
                throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                        "Cannot revert from PROPOSAL_SUBMITTED state");
        }
        
        thesis.setStatus(previousStatus);
        return thesisRepository.save(thesis);
    }

    // Conversion methods
    public ThesisWorkflowDto toDto(ThesisWorkflow thesis) {
        return new ThesisWorkflowDto(thesis);
    }
    
    public List<ThesisWorkflowDto> toDtoList(List<ThesisWorkflow> theses) {
        return theses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ThesisWorkflowDto toDetailedDto(ThesisWorkflow thesis) {
        // Convert student to DTO if available
        PersonDto studentDto = thesis.getStudent() != null ? new PersonDto(thesis.getStudent()) : null;
    
        // Convert jury members to DTO list
        List<PersonDto> juryMembers = thesis.getJuryMemberIdsList().stream()
            .map(memberId -> personRepository.findById(memberId)
                .map(PersonDto::new)
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    
        // Return a new ThesisWorkflowDto instead of modifying an existing one
        return new ThesisWorkflowDto(
            thesis.getId(),
            thesis.getStudent() != null ? thesis.getStudent().getId() : null,
            thesis.getStatus().name(),
            thesis.getSubmissionDate(),
            thesis.getJuryMemberIdsList(),
            thesis.getJuryPresident() != null ? thesis.getJuryPresident().getId() : null,
            thesis.getDocumentPath(),
            thesis.getTitle(),
            studentDto,  // Pass student DTO
            juryMembers   // Pass jury members list
        );
    }
    
    /**
     * Reject a thesis proposal (SC action)
     * This will delete the thesis, requiring the student to submit a new proposal
     */
    @Transactional
    public void rejectThesisProposal(Long thesisId, String comments) {
        ThesisWorkflow thesis = findById(thesisId);
        
        // Validate current status
        if (thesis.getStatus() != ThesisStatus.PROPOSAL_SUBMITTED) {
            throw new DEIException(ErrorMessage.INVALID_WORKFLOW_STATE, 
                    "Thesis is not in PROPOSAL_SUBMITTED state");
        }
        
        // Delete the thesis
        thesisRepository.delete(thesis);
        
    }
}