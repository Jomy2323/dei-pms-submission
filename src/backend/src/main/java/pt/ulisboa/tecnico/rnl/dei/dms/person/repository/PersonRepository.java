package pt.ulisboa.tecnico.rnl.dei.dms.person.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import java.util.List;

@Repository
@Transactional
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByIstId(String istId);
    Optional<Person> findByEmail(String email);
    Optional<Person> findByIstIdAndType(String istId, Person.PersonType type);
    List<Person> findByType(Person.PersonType type);
    
}
