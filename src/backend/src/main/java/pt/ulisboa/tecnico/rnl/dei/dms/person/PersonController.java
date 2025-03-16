package pt.ulisboa.tecnico.rnl.dei.dms.person;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.LoginRequestDto;
import pt.ulisboa.tecnico.rnl.dei.dms.person.service.PersonService;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person; 
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;

@RestController
@CrossOrigin(origins = "http://localhost:5173")  // Adjust as needed
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    private PersonService personService;

    @GetMapping("/people")
    public List<PersonDto> getPeople() {
        logger.debug("Received request to get all people.");
        List<PersonDto> people = personService.getPeople();
        logger.debug("Returning {} people.", people.size());
        return people;
    }

    @PostMapping("/people")
    public PersonDto createPerson(@Valid @RequestBody PersonDto personDto) {
        logger.debug("Received request to create person: {}", personDto);
        PersonDto createdPerson = personService.createPerson(personDto);
        logger.debug("Created person with ID: {}", createdPerson.id());
        return createdPerson;
    }

    @GetMapping("/people/{id}")
    public PersonDto getPerson(@PathVariable long id) {
        logger.debug("Received request to get person with ID: {}", id);
        PersonDto person = personService.getPerson(id);
        if (person != null) {
            logger.debug("Found person with ID: {}", person.id());
        } else {
            logger.debug("No person found with ID: {}", id);
        }
        return person;
    }

    @PutMapping("/people/{id}")
    public ResponseEntity<PersonDto> updatePerson(
        @PathVariable Long id, 
        @RequestBody PersonDto personDto,
        @RequestParam Person.PersonType role
    ) {
        // Optional: Add role-based authorization
        if (role != Person.PersonType.COORDINATOR && role != Person.PersonType.STAFF) {
            throw new DEIException(ErrorMessage.UNAUTHORIZED);
        }
    
        logger.debug("Received request to update person with ID: {}. Data: {}", id, personDto);
        PersonDto updatedPerson = personService.updatePerson(id, personDto);
        logger.debug("Updated person with ID: {}", updatedPerson.id());
        
        return ResponseEntity.ok(updatedPerson);
    }
    
    
    @DeleteMapping("/people/{id}")
    public ResponseEntity<?> deletePerson(@PathVariable long id) {
        logger.debug("Received request to delete person with ID: {}", id);
    
        personService.deletePerson(id);
        logger.debug("Successfully deleted person with ID: {}", id);
    
        return ResponseEntity.noContent().build();
    }
    

    @GetMapping("/people/search/byIstId")
    public ResponseEntity<PersonDto> getPersonByIstId(@RequestParam String istId) {
        logger.debug("Received request to search for person with IST ID: {}", istId);
        Optional<PersonDto> personOpt = personService.getPersonByIstId(istId);

        if (personOpt.isPresent()) {
            logger.debug("Person found with IST ID: {}", istId);
            return ResponseEntity.ok(personOpt.get());
        } else {
            logger.debug("No person found with IST ID: {}", istId);
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/people/search/byEmail")
    public ResponseEntity<PersonDto> getPersonByEmail(@RequestParam String email) {
        logger.debug("Received request to search for person with email: {}", email);
        Optional<PersonDto> personOpt = personService.getPersonByEmail(email);

        if (personOpt.isPresent()) {
            logger.debug("Person found with email: {}", email);
            return ResponseEntity.ok(personOpt.get());
        } else {
            logger.debug("No person found with email: {}", email);
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> checkLogin(@Valid @RequestBody LoginRequestDto loginRequest) {
        logger.debug("Received login request for IST ID: {} and role: {}", 
                     loginRequest.getIstId(), loginRequest.getRole());
        
        Optional<PersonDto> personOpt = personService.getPersonByIstIdAndRole(
                loginRequest.getIstId(), loginRequest.getRole());
        
        if (personOpt.isPresent()) {
            logger.debug("Login successful for IST ID: {}", loginRequest.getIstId());
            return ResponseEntity.ok(personOpt.get());
        } else {
            logger.debug("Login failed for IST ID: {} with role: {}", 
                         loginRequest.getIstId(), loginRequest.getRole());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid IST ID or role");
        }
    }

    @GetMapping("/people/search/byType")
    public ResponseEntity<List<PersonDto>> getPeopleByType(@RequestParam String type) {
        logger.debug("Received request to search for people by type: {}", type);
        
        List<PersonDto> people = personService.getPeopleByType(type);

        if (people.isEmpty()) {
            logger.debug("No people found with type: {}", type);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(people);
    }
}
