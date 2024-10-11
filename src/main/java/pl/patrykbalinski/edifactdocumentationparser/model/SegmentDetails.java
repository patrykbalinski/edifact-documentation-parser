package pl.patrykbalinski.edifactdocumentationparser.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class SegmentDetails {

    String number;
    String code;
    String name;
    String description;
}
