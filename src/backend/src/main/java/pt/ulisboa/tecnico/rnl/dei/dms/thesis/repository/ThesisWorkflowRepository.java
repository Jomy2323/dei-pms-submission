package pt.ulisboa.tecnico.rnl.dei.dms.thesis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow.ThesisStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThesisWorkflowRepository extends JpaRepository<ThesisWorkflow, Long> {
    // Find by student
    Optional<ThesisWorkflow> findByStudent(Person student);
    
    // Find all by student
    List<ThesisWorkflow> findAllByStudent(Person student);
    
    // Find all by status
    List<ThesisWorkflow> findByStatus(ThesisStatus status);
    
    // Find all by jury member using native query
    @Query(value = "SELECT * FROM thesis_workflows t WHERE :juryMemberId = ANY(t.jury_member_ids)", nativeQuery = true)
    List<ThesisWorkflow> findByJuryMembersContaining(@Param("juryMemberId") Long juryMemberId);
    
    // Find all by jury president
    List<ThesisWorkflow> findByJuryPresident(Person juryPresident);
    
    // Count theses by status
    long countByStatus(ThesisStatus status);
    
    // Find theses between two statuses (for filtering)
    List<ThesisWorkflow> findByStatusIn(List<ThesisStatus> statuses);
}