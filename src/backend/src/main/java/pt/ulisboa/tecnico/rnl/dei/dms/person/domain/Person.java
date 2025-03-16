package pt.ulisboa.tecnico.rnl.dei.dms.person.domain;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;


import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.*;

@Data
@Entity
@Table(name = "people")
public class Person {

	public enum PersonType {
		COORDINATOR, STAFF, STUDENT, TEACHER, SC;

		@JsonCreator
		public static PersonType fromString(String value) {
			if (value == null) {
				throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, "null");
			}
			
			try {
				return PersonType.valueOf(value.toUpperCase());
			} catch (IllegalArgumentException e) {
				for (PersonType type : PersonType.values()) {
					if (type.name().equalsIgnoreCase(value)) {
						return type;
					}
				}
				throw new DEIException(ErrorMessage.INVALID_PERSON_TYPE, value);
			}
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "ist_id", nullable = false, unique = true)
	private String istId;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
    private PersonType type;

	protected Person() {
	}

	public Person(String name, String istId, String email, PersonType type) {
		this.name = name;
		this.istId = istId;
		this.type = type;
		this.email = email;
	}

	public Person(PersonDto personDto) {
		this(personDto.name(), personDto.istId(), personDto.email(),
				PersonType.valueOf(personDto.type().toUpperCase()));
		System.out.println("PersonDto: " + personDto);
		System.out.println("PersonType: " + personDto.type());

	}
}
