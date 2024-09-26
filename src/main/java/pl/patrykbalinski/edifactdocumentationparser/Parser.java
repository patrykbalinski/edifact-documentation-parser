package pl.patrykbalinski.edifactdocumentationparser;

import pl.patrykbalinski.edifactdocumentationparser.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    static final Pattern SINGLE_SEGMENT_LINE_PATTERN = Pattern.compile("^(\\d{5})\\s+([A-Z]{3})\\s+(.*?)\\s+([MC])\\s+(\\d+).*$");
    static final Pattern SEGMENT_GROUP_LINE_PATTERN = Pattern.compile("^(\\d{5})\\s+---- ([A-Za-z ]+[0-9]+)\\s+.*?\\s+([MC])\\s+(\\d+).*$");
    static final Pattern SEGMENT_CLARIFICATION_HEADER_LINE_PATTERN = Pattern.compile("^(\\d{5})\\s+([A-Z]{3})?,? ?(.*)$");
    static final Pattern SEGMENT_CLARIFICATION_DESCRIPTION_LINE_PATTERN = Pattern.compile("^\\s+(.*)$");

    public static EdifactMessage parse(InputStream inputStream) throws IOException {
        EdifactMessage edifactMessage = new EdifactMessage();

        List<String> content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            content = reader.lines().toList();
        }

        edifactMessage.setType(content.get(33).substring(58));
        edifactMessage.setVersion(content.get(34).substring(58));
        edifactMessage.setRelease(content.get(35).substring(58));
        edifactMessage.setControlAgency(content.get(36).substring(58));
        edifactMessage.setRevision(content.get(38).substring(58));
        edifactMessage.setDate(content.get(39).substring(58));

        edifactMessage.setSegmentDetailsList(parseSegmentDetails(content.subList(content.indexOf("4.1    Segment clarification"), content.indexOf("4.2    Segment index (alphabetical sequence by tag)"))));
        edifactMessage.setSegments(parseMessageStructure(content.subList(content.indexOf("4.3    Message structure"), content.size())));

        System.out.println(edifactMessage.getSegmentDetailsList());

        return edifactMessage;
    }

    public static List<SegmentDetails> parseSegmentDetails(List<String> lines) {
        List<SegmentDetails> segmentDetailsList = new ArrayList<>();

        String number = null;
        String code = null;
        String name = null;
        StringBuilder description = new StringBuilder();

        for (String line : lines) {
            Matcher segmentClarificationHeaderMatcher = SEGMENT_CLARIFICATION_HEADER_LINE_PATTERN.matcher(line);
            Matcher segmentClarificationDescriptionMatcher = SEGMENT_CLARIFICATION_DESCRIPTION_LINE_PATTERN.matcher(line);

            if (segmentClarificationHeaderMatcher.matches()) {
                if (name != null) {
                    segmentDetailsList.add(new SegmentDetails(number, code, name, description.toString().trim()));
                }

                number = segmentClarificationHeaderMatcher.group(1);
                code = segmentClarificationHeaderMatcher.group(2);
                name = segmentClarificationHeaderMatcher.group(3);
                description.setLength(0);
            } else if (segmentClarificationDescriptionMatcher.matches()) {
                description.append(line.trim()).append(" ");
            }
        }

        if (code != null) {
            segmentDetailsList.add(new SegmentDetails(number, code, name, description.toString().trim()));
        }

        return segmentDetailsList;
    }

    public static List<Segment> parseMessageStructure(List<String> lines) {
        List<Segment> elements = new ArrayList<>();

        List<SegmentGroup> groupStack = new ArrayList<>();
        SegmentGroup currentGroup = null;

        for (String line : lines) {
            Matcher singleSegmentLineMatcher = SINGLE_SEGMENT_LINE_PATTERN.matcher(line);
            Matcher segmentGroupLineMatcher = SEGMENT_GROUP_LINE_PATTERN.matcher(line);

            if (singleSegmentLineMatcher.matches()) {
                String number = singleSegmentLineMatcher.group(1);
                String code = singleSegmentLineMatcher.group(2);
                String name = singleSegmentLineMatcher.group(3).trim();
                boolean mandatory = singleSegmentLineMatcher.group(4).equals("M");
                int maxOccurrences = Integer.parseInt(singleSegmentLineMatcher.group(5));

                SingleSegment singleSegment = new SingleSegment(number, code, name, mandatory, maxOccurrences);

                if (!groupStack.isEmpty()) {
                    currentGroup.addSegment(singleSegment);
                } else {
                    elements.add(singleSegment);
                }

                if (line.contains("-+")) {
                    for (int i = 0; i < line.chars().filter(ch -> ch == '+').count(); i++) {
                        if (!groupStack.isEmpty()) {
                            groupStack.remove(groupStack.size() - 1);
                            currentGroup = groupStack.isEmpty() ? null : groupStack.get(groupStack.size() - 1);
                        }
                    }
                }

                if (line.contains("-+")) {
                    long plusCount = line.chars().filter(ch -> ch == '+').count();

                    for (int i = 0; i < plusCount && !groupStack.isEmpty(); i++) {
                        groupStack.remove(groupStack.size() - 1);
                    }

                    currentGroup = groupStack.isEmpty() ? null : groupStack.get(groupStack.size() - 1);
                }

            } else if (segmentGroupLineMatcher.matches()) {
                String groupNumber = segmentGroupLineMatcher.group(1);
                String groupName = segmentGroupLineMatcher.group(2);
                boolean mandatory = segmentGroupLineMatcher.group(3).equals("M");
                int maxOccurrences = Integer.parseInt(segmentGroupLineMatcher.group(4));

                SegmentGroup newGroup = new SegmentGroup(groupNumber, groupName, mandatory, maxOccurrences);

                if (!groupStack.isEmpty()) {
                    currentGroup.addSegment(newGroup);
                } else {
                    elements.add(newGroup);
                }

                groupStack.add(newGroup);
                currentGroup = newGroup;
            }
        }

        return elements;
    }
}
