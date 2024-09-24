package pl.patrykbalinski.edifactdocumentationparser;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data()
public class SegmentGroup extends Segment {
    String number;
    String name;
    boolean mandatory;
    int maxOccurrences;
    private final List<Segment> segments;

    public SegmentGroup(String number, String name, boolean mandatory, int maxOccurrences) {
        this.number = number;
        this.name = name;
        this.mandatory = mandatory;
        this.maxOccurrences = maxOccurrences;
        this.segments = new ArrayList<>();
    }

    public void addSegment(Segment segment) {
        this.segments.add(segment);
    }
}
