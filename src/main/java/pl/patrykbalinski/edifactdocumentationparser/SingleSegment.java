package pl.patrykbalinski.edifactdocumentationparser;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SingleSegment extends Segment {
    private String code;

    public SingleSegment(String number, String code, String name, boolean mandatory, int maxOccurrences) {
        super.number = number;
        super.name = name;
        super.mandatory = mandatory;
        super.maxOccurrences = maxOccurrences;
        this.code = code;
    }
}
