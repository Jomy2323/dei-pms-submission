package pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow.DefenseStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DefenseWorkflowRepository extends JpaRepository<DefenseWorkflow, Long> {
    // Find by student
    Optional<DefenseWorkflow> findByStudent(Person student);
    
    // Find all by student
    List<DefenseWorkflow> findAllByStudent(Person student);
    
    // Find by thesis
    Optional<DefenseWorkflow> findByThesis(ThesisWorkflow thesis);
    
    // Find all by status
    List<DefenseWorkflow> findByStatus(DefenseStatus status);
    
    // Find defenses scheduled before a certain date
    List<DefenseWorkflow> findByDefenseDateBeforeAndStatus(
        LocalDateTime date, DefenseStatus status);
    
    // Count defenses by status
    long countByStatus(DefenseStatus status);
    
    // Find defenses between two statuses (for filtering)
    List<DefenseWorkflow> findByStatusIn(List<DefenseStatus> statuses);
}