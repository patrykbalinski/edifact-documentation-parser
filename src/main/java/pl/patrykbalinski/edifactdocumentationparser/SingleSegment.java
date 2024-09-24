package pl.patrykbalinski.edifactdocumentationparser;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SingleSegment extends Segment {
    String number;
    private String code;
    String name;
    boolean mandatory;
    int maxOccurrences;

    public SingleSegment(String number, String code, String name, boolean mandatory, int maxOccurrences) {
        this.number = number;
        this.name = name;
        this.mandatory = mandatory;
        this.maxOccurrences = maxOccurrences;
        this.code = code;
    }
}
