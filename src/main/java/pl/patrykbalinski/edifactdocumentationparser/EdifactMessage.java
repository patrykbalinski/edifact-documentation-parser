package pl.patrykbalinski.edifactdocumentationparser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EdifactMessage {
    private String type;
    private String version;
    private String release;
    private String controlAgency;
    private String revision;
    private String date;
    private List<Segment> segments;

    public EdifactMessage() {
        this.segments = new ArrayList<>();
    }
}
