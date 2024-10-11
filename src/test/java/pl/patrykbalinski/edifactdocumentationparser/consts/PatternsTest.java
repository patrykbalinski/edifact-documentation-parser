package pl.patrykbalinski.edifactdocumentationparser.consts;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pl.patrykbalinski.edifactdocumentationparser.consts.Patterns.*;

class PatternsTest {
    @ParameterizedTest
    @MethodSource
    public void segment_group_start_pattern_should_match_segment_group_start_line(String line, String expectedNumber, String expectedName, String expectedMandatory, int expectedMaxOccurrences) {
        Matcher segmentMatcher = SEGMENT_GROUP_LINE_PATTERN.matcher(line);

        assertThat(segmentMatcher.matches()).isTrue();
        assertThat(segmentMatcher.group(1)).isEqualTo(expectedNumber);
        assertThat(segmentMatcher.group(2)).isEqualTo(expectedName);
        assertThat(segmentMatcher.group(3)).isEqualTo(expectedMandatory);
        assertThat(Integer.parseInt(segmentMatcher.group(4))).isEqualTo(expectedMaxOccurrences);
    }

    public static Stream<Arguments> segment_group_start_pattern_should_match_segment_group_start_line() {
        return Stream.of(
                Arguments.arguments("00120       ---- Segment group 1  ------------------ C   99999------------+", "00120", "Segment group 1", "C", 99999),
                Arguments.arguments("00270       ---- Segment group 3  ------------------ C   9999------------+|", "00270", "Segment group 3", "C", 9999),
                Arguments.arguments("01530       ---- Segment group 33 ------------------ C   10-------------+||", "01530", "Segment group 33", "C", 10),
                Arguments.arguments("02340       ---- Segment group 52 ------------------ M   100--------------+", "02340", "Segment group 52", "M", 100)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void segment_group_start_pattern_should_not_match_single_segment_lines_and_space_lines(String line) {
        assertThat(SEGMENT_GROUP_LINE_PATTERN.matcher(line).matches()).isFalse();
    }

    public static Stream<Arguments> segment_group_start_pattern_should_not_match_single_segment_lines_and_space_lines() {
        return Stream.of(
                Arguments.arguments("00010   UNH Message header                           M   1     "),
                Arguments.arguments("00280   RFF Reference                                M   1               ||"),
                Arguments.arguments("00290   DTM Date/time/period                         C   5---------------+|"),
                Arguments.arguments("01540   PCI Package identification                   M   1              |||"),
                Arguments.arguments(""),
                Arguments.arguments("                                                                          |"),
                Arguments.arguments("                                                                         ||")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void single_segment_pattern_should_match_single_segment_line(String line, String expectedNumber, String expectedCode, String expectedName, String expectedMandatory, int expectedMaxOccurrences) {
        Matcher segmentMatcher = SINGLE_SEGMENT_LINE_PATTERN.matcher(line);

        assertThat(segmentMatcher.matches()).isTrue();
        assertThat(segmentMatcher.group(1)).isEqualTo(expectedNumber);
        assertThat(segmentMatcher.group(2)).isEqualTo(expectedCode);
        assertThat(segmentMatcher.group(3)).isEqualTo(expectedName);
        assertThat(segmentMatcher.group(4)).isEqualTo(expectedMandatory);
        assertThat(Integer.parseInt(segmentMatcher.group(5))).isEqualTo(expectedMaxOccurrences);
    }

    public static Stream<Arguments> single_segment_pattern_should_match_single_segment_line() {
        return Stream.of(
                Arguments.arguments("00010   UNH Message header                           M   1     ", "00010", "UNH", "Message header", "M", 1),
                Arguments.arguments("00070   FTX Free text                                C   99    ", "00070", "FTX", "Free text", "C", 99),
                Arguments.arguments("00130   RFF Reference                                M   1                |", "00130", "RFF", "Reference", "M", 1),
                Arguments.arguments("00570   RFF Reference                                M   1               ||", "00570", "RFF", "Reference", "M", 1),
                Arguments.arguments("00800   DTM Date/time/period                         C   5---------------+|", "00800", "DTM", "Date/time/period", "C", 5),
                Arguments.arguments("02050   DTM Date/time/period                         C   5--------------++|", "02050", "DTM", "Date/time/period", "C", 5)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void single_segment_line_pattern_should_not_match_segment_group_start_line_and_space_lines(String line) {
        assertThat(SINGLE_SEGMENT_LINE_PATTERN.matcher(line).matches()).isFalse();
    }

    public static Stream<Arguments> single_segment_line_pattern_should_not_match_segment_group_start_line_and_space_lines() {
        return Stream.of(
                Arguments.arguments("00120       ---- Segment group 1  ------------------ C   99999------------+", "00120"),
                Arguments.arguments("00270       ---- Segment group 3  ------------------ C   9999------------+|", "00270"),
                Arguments.arguments("01530       ---- Segment group 33 ------------------ C   10-------------+||", "01530"),
                Arguments.arguments("02340       ---- Segment group 52 ------------------ M   100--------------+", "02340"),
                Arguments.arguments(""),
                Arguments.arguments("                                                                          |"),
                Arguments.arguments("                                                                         ||")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void segment_clarification_header_line_pattern_should_match_segment_details_line(String line, String expectedNumber, String expectedCode, String expectedName) {
        Matcher segmentMatcher = SEGMENT_CLARIFICATION_HEADER_LINE_PATTERN.matcher(line);

        assertThat(segmentMatcher.matches()).isTrue();
        assertThat(segmentMatcher.group(1)).isEqualTo(expectedNumber);
        assertThat(segmentMatcher.group(2)).isEqualTo(expectedCode);
        assertThat(segmentMatcher.group(3)).isEqualTo(expectedName);
    }

    public static Stream<Arguments> segment_clarification_header_line_pattern_should_match_segment_details_line() {
        return Stream.of(
                Arguments.arguments("00010   UNH, Message header", "00010", "UNH", "Message header"),
                Arguments.arguments("00130      RFF, Reference", "00130", "RFF", "Reference"),
                Arguments.arguments("00960         TAX, Duty/tax/fee details", "00960", "TAX", "Duty/tax/fee details"),
                Arguments.arguments("00120   Segment group 1:  RFF-DTM-GIR-LOC-MEA-QTY-FTX-MOA-RTE", "00120", null, "Segment group 1:  RFF-DTM-GIR-LOC-MEA-QTY-FTX-MOA-RTE"),
                Arguments.arguments("00270      Segment group 3:  RFF-DTM", "00270", null, "Segment group 3:  RFF-DTM")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void segment_clarification_description_line_pattern_should_match_segment_details_line(String line) {
        Matcher segmentMatcher = SEGMENT_CLARIFICATION_DESCRIPTION_LINE_PATTERN.matcher(line);

        assertThat(segmentMatcher.matches()).isTrue();
    }

    public static Stream<Arguments> segment_clarification_description_line_pattern_should_match_segment_details_line() {
        return Stream.of(
                Arguments.arguments("        A service segment starting and uniquely identifying a message. The"),
                Arguments.arguments("           A segment identifying the reference by its number and where"),
                Arguments.arguments("              A segment identifying and providing information relating to")
        );
    }
}