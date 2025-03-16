package pt.ulisboa.tecnico.rnl.dei.dms.thesis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThesisSubmissionDto {
    private String title;
    private List<Long> juryMemberIds;
}