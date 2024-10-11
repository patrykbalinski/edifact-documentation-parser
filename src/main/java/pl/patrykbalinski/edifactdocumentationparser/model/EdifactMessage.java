package pl.patrykbalinski.edifactdocumentationparser.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
public class EdifactMessage {

    String type;
    String version;
    String release;
    String controlAgency;
    String revision;
    String date;
    List<SegmentDetails> segmentDetailsList;
    List<Segment> segments;
}
