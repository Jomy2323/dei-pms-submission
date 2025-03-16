package pt.ulisboa.tecnico.rnl.dei.dms.person.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonService {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    @Autowired
    private PersonRepository personRepository;

    private Person fetchPersonOrThrow(long id) {
        logger.debug("Fetching person with ID: {}", id);
        return personRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("No person found with ID: {}", id);
                    return new DEIException(ErrorMessage.NO_SUCH_PERSON, Long.toString(id));
                });
    }

    @Transactional
    public List<PersonDto> getPeople() {
        logger.debug("Fetching all people from repository...");
        List<PersonDto> people = personRepository.findAll().stream()
                .map(PersonDto::new)
                .toList();

        logger.debug("Fetched {} people.", people.size());
        return people;
    }

    @Transactional
    public PersonDto createPerson(PersonDto personDto) {
        logger.debug("Attempting to create person: {}", personDto);

        if (personRepository.findByIstId(personDto.istId()).isPresent()) {
            logger.error("Duplicate IST ID detected: {}", personDto.istId());
            throw new DEIException(ErrorMessage.DUPLICATE_IST_ID, personDto.istId());
        }

        if (personRepository.findByEmail(personDto.email()).isPresent()) {
            logger.error("Duplicate email detected: {}", personDto.email());
            throw new DEIException(ErrorMessage.DUPLICATE_EMAIL, personDto.email());
        }

        Person.PersonType personType;
        try {
            personType = Person.PersonType.valueOf(personDto.type().toUpperCase());
            logger.debug("Parsed person type: {}", personType);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid person type provided: {}", personDto.type());
            throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, personDto.type());
        }

        Person person = new Person(personDto);
        person.setId(null); // ensure new person is created

        PersonDto savedPersonDto = new PersonDto(personRepository.save(person));
        logger.debug("Created new person with ID: {}", savedPersonDto.id());

        return savedPersonDto;
    }

    @Transactional
    public PersonDto getPerson(long id) {
        logger.debug("Getting person with ID: {}", id);
        PersonDto personDto = new PersonDto(fetchPersonOrThrow(id));
        logger.debug("Fetched person with ID: {}", personDto.id());
        return personDto;
    }

    @Transactional
    public PersonDto updatePerson(Long id, PersonDto personDto) {
        // Find the existing person
        Person existingPerson = personRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.PERSON_NOT_FOUND, id.toString()));
    
        // Update fields
        existingPerson.setName(personDto.name());
        existingPerson.setEmail(personDto.email());
        
        // Validate email uniqueness
        Optional<Person> existingWithEmail = personRepository.findByEmail(personDto.email());
        if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(id)) {
            throw new DEIException(ErrorMessage.DUPLICATE_EMAIL);
        }
    
        // Save and return updated person
        existingPerson = personRepository.save(existingPerson);
        return new PersonDto(existingPerson);
    }

    @Transactional
    public void deletePerson(long id) {
        logger.debug("Deleting person with ID: {}", id);

        Person person = personRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, "ID: " + id));

        try {
            personRepository.deleteById(id);
            logger.debug("Deleted person with ID: {}", id);
        } catch (Exception e) {
            logger.error("Unexpected error while deleting person with ID {}: {}", id, e.getMessage());
            throw new DEIException(ErrorMessage.INTERNAL_ERROR, "Erro interno ao eliminar a pessoa.");
        }
    }


    @Transactional
    public Optional<PersonDto> getPersonByIstId(String istId) {
        logger.debug("Searching for person with IST ID: {}", istId);
        Optional<Person> personOpt = personRepository.findByIstId(istId);

        if (personOpt.isPresent()) {
            logger.debug("Person found: {}", personOpt.get());
        } else {
            logger.debug("No person found with IST ID: {}", istId);
        }

        return personOpt.map(PersonDto::new);
    }

    @Transactional
    public Optional<PersonDto> getPersonByEmail(String email) {
        logger.debug("Searching for person with email: {}", email);
        Optional<Person> personOpt = personRepository.findByEmail(email);

        if (personOpt.isPresent()) {
            logger.debug("Person found with email: {}", email);
        } else {
            logger.debug("No person found with email: {}", email);
        }

        return personOpt.map(PersonDto::new);
    }

    @Transactional
    public Optional<PersonDto> getPersonByIstIdAndRole(String istId, String role) {
        logger.debug("Searching for person with IST ID: {} and role: {}", istId, role);

        // Parse the requested role into a PersonType enum.
        Person.PersonType requestedType;
        try {
            requestedType = Person.PersonType.valueOf(role.toUpperCase());
            logger.debug("Parsed person type: {}", requestedType);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid person type provided: {}", role);
            throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, role);
        }

        // First, try to find a person by IST ID.
        Person person = personRepository.findByIstId(istId)
                .orElseThrow(() -> {
                    logger.error("No person found with IST ID: {}", istId);
                    return new DEIException(ErrorMessage.NO_SUCH_PERSON, "IST ID: " + istId);
                });

        // Then, check if the person's type matches the requested type.
        if (!person.getType().equals(requestedType)) {
            logger.error("Person with IST ID: {} does not have the required role. Expected: {}, Actual: {}",
                    istId, requestedType.name(), person.getType().name());
            throw new DEIException(ErrorMessage.ID_NOT_THAT_ROLE,
                    "IST ID: " + istId + ", Expected Role: " + requestedType.name() + ", Actual Role: " + person.getType().name());
        }

        logger.debug("Person found with IST ID: {} and matching role: {}", istId, role);
        return Optional.of(new PersonDto(person));
    }

    @Transactional
    public List<PersonDto> getPeopleByType(String type) {
        logger.debug("Fetching people with type: {}", type);

        // Convert String to ENUM
        PersonType personType;
        try {
            personType = PersonType.valueOf(type.toUpperCase()); // Ensure case consistency
        } catch (IllegalArgumentException e) {
            logger.error("Invalid person type: {}", type);
            throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, type);
        }

        List<Person> people = personRepository.findByType(personType);

        if (people.isEmpty()) {
            logger.debug("No people found for type: {}", personType);
        } else {
            logger.debug("Found {} people of type: {}", people.size(), personType);
        }

        return people.stream().map(PersonDto::new).collect(Collectors.toList());
    }

}
