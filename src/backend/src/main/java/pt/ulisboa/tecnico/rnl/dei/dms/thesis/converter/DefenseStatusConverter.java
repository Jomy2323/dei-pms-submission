package pt.ulisboa.tecnico.rnl.dei.dms.thesis.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow.DefenseStatus;

@Converter(autoApply = true)
public class DefenseStatusConverter implements AttributeConverter<DefenseStatus, String> {

    @Override
    public String convertToDatabaseColumn(DefenseStatus status) {
        if (status == null) {
            return DefenseStatus.UNSCHEDULED.getDatabaseValue(); // Default to UNSCHEDULED
        }
        return status.getDatabaseValue();
    }

    @Override
    public DefenseStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return DefenseStatus.UNSCHEDULED; // Default to UNSCHEDULED when null is found in DB
        }
        return DefenseStatus.fromDatabaseValue(dbData);
    }
}
