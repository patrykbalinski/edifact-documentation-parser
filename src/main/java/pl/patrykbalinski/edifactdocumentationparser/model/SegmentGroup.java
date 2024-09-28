package pl.patrykbalinski.edifactdocumentationparser.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SegmentGroup implements Segment {
    private String number;
    private String name;
    private boolean mandatory;
    private int maxOccurrences;
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
