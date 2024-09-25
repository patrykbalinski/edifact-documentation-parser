package pl.patrykbalinski.edifactdocumentationparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.patrykbalinski.edifactdocumentationparser.model.EdifactMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ParserTest {

    @ParameterizedTest
    @MethodSource
    public void should_parse_edifact_documentation_file(String inputFilepath, String expectedType, String expectedVersion, String expectedRelease, String expectedControlAgency, String expectedRevision, String expectedDate) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(inputFilepath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found: " + inputFilepath);
        }

        EdifactMessage edifactMessage = Parser.parse(inputStream);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        System.out.println(gson.toJson(edifactMessage));

        assertThat(edifactMessage.getType()).isEqualTo(expectedType);
        assertThat(edifactMessage.getVersion()).isEqualTo(expectedVersion);
        assertThat(edifactMessage.getRelease()).isEqualTo(expectedRelease);
        assertThat(edifactMessage.getControlAgency()).isEqualTo(expectedControlAgency);
        assertThat(edifactMessage.getRevision()).isEqualTo(expectedRevision);
        assertThat(edifactMessage.getDate()).isEqualTo(expectedDate);
    }

    public static Stream<Arguments> should_parse_edifact_documentation_file() {
        return Stream.of(
                Arguments.arguments("edifact_documentations/INVOIC_D.23A/INVOIC_D.23A", "INVOIC", "D", "23A", "UN", "16", "2023-07-21"),
                Arguments.arguments("edifact_documentations/ORDERS_D.22B/ORDERS_D.22B", "ORDERS", "D", "22B", "UN", "16", "2022-12-20")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void segment_group_start_pattern_should_match_segment_group_start_line(String line, String expectedNumber, String expectedName, String expectedMandatory, int expectedMaxOccurrences) {
        Matcher segmentMatcher = Parser.SEGMENT_GROUP_PATTERN.matcher(line);

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
        assertThat(Parser.SEGMENT_GROUP_PATTERN.matcher(line).matches()).isFalse();
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
    public void single_segment_pattern_should_single_segment_line(String line, String expectedNumber, String expectedCode, String expectedName, String expectedMandatory, int expectedMaxOccurrences) {
        Matcher segmentMatcher = Parser.SINGLE_SEGMENT_PATTERN.matcher(line);

        assertThat(segmentMatcher.matches()).isTrue();
        assertThat(segmentMatcher.group(1)).isEqualTo(expectedNumber);
        assertThat(segmentMatcher.group(2)).isEqualTo(expectedCode);
        assertThat(segmentMatcher.group(3)).isEqualTo(expectedName);
        assertThat(segmentMatcher.group(4)).isEqualTo(expectedMandatory);
        assertThat(Integer.parseInt(segmentMatcher.group(5))).isEqualTo(expectedMaxOccurrences);
    }

    public static Stream<Arguments> single_segment_pattern_should_single_segment_line() {
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
    public void single_segment_pattern_should_not_match_segment_group_start_line_and_space_lines(String line) {
        assertThat(Parser.SINGLE_SEGMENT_PATTERN.matcher(line).matches()).isFalse();
    }

    public static Stream<Arguments> single_segment_pattern_should_not_match_segment_group_start_line_and_space_lines() {
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
}