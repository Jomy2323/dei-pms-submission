package pt.ulisboa.tecnico.rnl.dei.dms.person.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

// Data Transfer Object (DTO)
public record PersonDto(
    long id,
    
    @NotBlank(message = "O nome não pode estar vazio")
    String name,
    
    @NotBlank(message = "O IST ID não pode estar vazio")
    @Pattern(regexp = "^ist1[0-9]+$", message = "IST ID inválido")
    String istId,
    
    @NotBlank(message = "O email não pode estar vazio")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@tecnico\\.ulisboa\\.pt$", message = "Email inválido")
    String email,
    
    @NotBlank(message = "O tipo não pode estar vazio")
    String type

    
) {
    public PersonDto(Person person) {
        this(person.getId(), person.getName(), person.getIstId(), person.getEmail(),
             person.getType().toString());
    }
}
