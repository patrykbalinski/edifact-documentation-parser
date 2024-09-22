package pl.patrykbalinski.edifactdocumentationparser;


import lombok.Getter;

@Getter
public abstract class Segment {
    String number;
    String name;
    boolean mandatory;
    int maxOccurrences;
}
