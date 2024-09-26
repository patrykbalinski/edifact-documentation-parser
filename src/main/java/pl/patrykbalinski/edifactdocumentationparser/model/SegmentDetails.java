package pl.patrykbalinski.edifactdocumentationparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SegmentDetails {
    private String number;
    private String code;
    private String name;
    private String description;
}
