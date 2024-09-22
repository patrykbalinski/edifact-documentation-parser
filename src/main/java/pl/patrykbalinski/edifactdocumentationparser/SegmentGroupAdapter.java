package pl.patrykbalinski.edifactdocumentationparser;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SegmentGroupAdapter extends TypeAdapter<SegmentGroup> {

    @Override
    public void write(JsonWriter out, SegmentGroup segmentGroup) throws IOException {
        out.beginObject();
        out.name("number").value(segmentGroup.getNumber());
        out.name("name").value(segmentGroup.getName());
        out.name("mandatory").value(segmentGroup.isMandatory());
        out.name("maxOccurrences").value(segmentGroup.getMaxOccurrences());
        out.name("segments");
        out.beginArray();
        for (Segment segment : segmentGroup.getSegments()) {
            out.beginObject();
            out.name("number").value(segment.getNumber());
            out.name("name").value(segment.getName());
            out.name("mandatory").value(segment.isMandatory());
            out.name("maxOccurrences").value(segment.getMaxOccurrences());
            out.endObject();
        }
        out.endArray();
        out.endObject();
    }

    @Override
    public SegmentGroup read(JsonReader jsonReader) {
        return null;
    }
}