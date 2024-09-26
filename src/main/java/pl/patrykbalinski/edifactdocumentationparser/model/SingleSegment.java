package pl.patrykbalinski.edifactdocumentationparser.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SingleSegment extends Segment {
    private String number;
    private String code;
    private String name;
    private boolean mandatory;
    private int maxOccurrences;

    public SingleSegment(String number, String code, String name, boolean mandatory, int maxOccurrences) {
        this.number = number;
        this.name = name;
        this.mandatory = mandatory;
        this.maxOccurrences = maxOccurrences;
        this.code = code;
    }
}
