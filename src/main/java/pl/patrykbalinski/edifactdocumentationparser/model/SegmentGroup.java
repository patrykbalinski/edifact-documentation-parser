package pl.patrykbalinski.edifactdocumentationparser.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@RequiredArgsConstructor
public class SegmentGroup implements Segment {

    String number;
    String name;
    boolean mandatory;
    int maxOccurrences;
    List<Segment> segments = new ArrayList<>();

    public void addSegment(Segment segment) {
        this.segments.add(segment);
    }
}
