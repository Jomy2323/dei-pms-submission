package pt.ulisboa.tecnico.rnl.dei.dms.thesis.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.ThesisWorkflow.ThesisStatus;
import pt.ulisboa.tecnico.rnl.dei.dms.thesis.domain.DefenseWorkflow.DefenseStatus;

@Converter(autoApply = true)
public class ThesisStatusConverter implements AttributeConverter<ThesisStatus, String> {

    @Override
    public String convertToDatabaseColumn(ThesisStatus status) {
        if (status == null) {
            return null;
        }
        return status.getDatabaseValue();
    }

    @Override
    public ThesisStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ThesisStatus.fromDatabaseValue(dbData);
    }
}

