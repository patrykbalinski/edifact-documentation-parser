package pl.patrykbalinski.edifactdocumentationparser;

import lombok.extern.slf4j.Slf4j;
import pl.patrykbalinski.edifactdocumentationparser.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static pl.patrykbalinski.edifactdocumentationparser.consts.DocumentationFileHooks.*;
import static pl.patrykbalinski.edifactdocumentationparser.consts.Flags.SEGMENT_MANDATORY_FLAG;
import static pl.patrykbalinski.edifactdocumentationparser.consts.Patterns.*;

@Slf4j
public class Parser {

    public EdifactMessage parse(InputStream inputStream) throws IOException {
        List<String> content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            content = reader.lines().toList();
        }

        String type = content.get(MESSAGE_TYPE_LINE_NUMBER).substring(MESSAGE_HEADER_INFO_START_INDEX);
        String version = content.get(VERSION_LINE_NUMBER).substring(MESSAGE_HEADER_INFO_START_INDEX);
        String release = content.get(RELEASE_LINE_NUMBER).substring(MESSAGE_HEADER_INFO_START_INDEX);
        String controlAgency = content.get(CONTROL_AGENCY_LINE_NUMBER).substring(MESSAGE_HEADER_INFO_START_INDEX);
        String revision = content.get(REVISION_LINE_NUMBER).substring(MESSAGE_HEADER_INFO_START_INDEX);
        String date = content.get(DATE_LINE_NUMBER).substring(MESSAGE_HEADER_INFO_START_INDEX);
        List<SegmentDetails> segmentDetailsList = parseSegmentDetails(content.subList(content.indexOf(SEGMENT_DETAILS_BEGIN_LINE_TEXT), content.indexOf(SEGMENT_DETAILS_END_LINE_TEXT)));
        List<Segment> segments = parseMessageStructure(content.subList(content.indexOf(MESSAGE_STRUCTURE_BEGIN_LINE_TEXT), content.size()));

        EdifactMessage edifactMessage = new EdifactMessage(type, version, release, controlAgency, revision, date, segmentDetailsList, segments);

        log.info("Document parsed, details = {}", edifactMessage.getSegmentDetailsList());

        return edifactMessage;
    }

    private List<SegmentDetails> parseSegmentDetails(List<String> lines) {
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

    private List<Segment> parseMessageStructure(List<String> lines) {
        List<Segment> segments = new ArrayList<>();
        List<SegmentGroup> segmentGroups = new ArrayList<>();
        SegmentGroup currentGroup = null;

        for (String line : lines) {
            Matcher singleSegmentLineMatcher = SINGLE_SEGMENT_LINE_PATTERN.matcher(line);
            Matcher segmentGroupLineMatcher = SEGMENT_GROUP_LINE_PATTERN.matcher(line);

            if (singleSegmentLineMatcher.matches()) {
                currentGroup = handleSingleSegmentLine(line, singleSegmentLineMatcher, segments, segmentGroups, currentGroup);
            } else if (segmentGroupLineMatcher.matches()) {
                currentGroup = handleSegmentGroupLine(segmentGroupLineMatcher, segments, segmentGroups, currentGroup);
            }
        }

        return segments;
    }

    private SegmentGroup handleSingleSegmentLine(String line, Matcher matcher, List<Segment> segments, List<SegmentGroup> segmentGroups, SegmentGroup currentGroup) {
        String number = matcher.group(1);
        String code = matcher.group(2);
        String name = matcher.group(3);
        boolean mandatory = matcher.group(4).equals(SEGMENT_MANDATORY_FLAG);
        int maxOccurrences = Integer.parseInt(matcher.group(5));

        SingleSegment singleSegment = new SingleSegment(number, code, name, mandatory, maxOccurrences);

        if (!segmentGroups.isEmpty()) {
            currentGroup.addSegment(singleSegment);
        } else {
            segments.add(singleSegment);
        }

        currentGroup = updateGroupStackAfterPlus(line, segmentGroups, currentGroup);
        return currentGroup;
    }

    private SegmentGroup handleSegmentGroupLine(Matcher matcher, List<Segment> segments, List<SegmentGroup> segmentGroups, SegmentGroup currentGroup) {
        String groupNumber = matcher.group(1);
        String groupName = matcher.group(2);
        boolean mandatory = matcher.group(3).equals(SEGMENT_MANDATORY_FLAG);
        int maxOccurrences = Integer.parseInt(matcher.group(4));

        SegmentGroup newGroup = new SegmentGroup(groupNumber, groupName, mandatory, maxOccurrences);

        if (!segmentGroups.isEmpty()) {
            currentGroup.addSegment(newGroup);
        } else {
            segments.add(newGroup);
        }

        segmentGroups.add(newGroup);
        return newGroup;
    }

    private SegmentGroup updateGroupStackAfterPlus(String line, List<SegmentGroup> segmentGroups, SegmentGroup currentGroup) {
        if (line.contains("-+")) {
            long plusCount = line.chars().filter(ch -> ch == '+').count();

            for (int i = 0; i < plusCount && !segmentGroups.isEmpty(); i++) {
                segmentGroups.remove(segmentGroups.size() - 1);
            }

            currentGroup = segmentGroups.isEmpty() ? null : segmentGroups.get(segmentGroups.size() - 1);
        }
        return currentGroup;
    }

}
