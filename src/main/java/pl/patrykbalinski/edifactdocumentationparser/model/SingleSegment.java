package pl.patrykbalinski.edifactdocumentationparser.model;


import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class SingleSegment implements Segment {

    String number;
    String code;
    String name;
    boolean mandatory;
    int maxOccurrences;
}
