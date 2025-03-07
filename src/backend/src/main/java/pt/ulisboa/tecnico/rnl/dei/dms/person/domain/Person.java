package pt.ulisboa.tecnico.rnl.dei.dms.person.domain;


import jakarta.persistence.*;

import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;

@Data
@Entity
@Table(name = "people")
public class Person {

	public enum PersonType {
		COORDINATOR, STAFF, STUDENT, TEACHER, SC
	}

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "ist_id", nullable = false, unique = true)
	private String istId;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
    private PersonType type;


	// TODO: maybe add more fields? ...or maybe not? what makes sense here?

	protected Person() {
	}

	public Person(String name, String istId, PersonType type) {
		this.name = name;
		this.istId = istId;
		this.type = type;
	}

	public Person(PersonDto personDto) {
		this(personDto.name(), personDto.istId(),
				PersonType.valueOf(personDto.type().toUpperCase()));
		System.out.println("PersonDto: " + personDto);
		System.out.println("PersonType: " + personDto.type());

	}
}
