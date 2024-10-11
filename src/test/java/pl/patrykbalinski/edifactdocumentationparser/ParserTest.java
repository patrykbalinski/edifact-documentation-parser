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
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ParserTest {

    @ParameterizedTest
    @MethodSource
    public void should_parse_edifact_documentation_file(String inputFilepath, String expectedType, String expectedVersion, String expectedRelease, String expectedControlAgency, String expectedRevision, String expectedDate, int expectedSegmentDetailsNumber) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(inputFilepath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found: " + inputFilepath);
        }

        Parser parser = new Parser();

        EdifactMessage edifactMessage = parser.parse(inputStream);

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
        assertThat(edifactMessage.getSegmentDetailsList().size()).isEqualTo(expectedSegmentDetailsNumber);
    }

    public static Stream<Arguments> should_parse_edifact_documentation_file() {
        return Stream.of(
                Arguments.arguments("edifact_documentations/INVOIC_D.23A/INVOIC_D.23A", "INVOIC", "D", "23A", "UN", "16", "2023-07-21", 247),
                Arguments.arguments("edifact_documentations/ORDERS_D.22B/ORDERS_D.22B", "ORDERS", "D", "22B", "UN", "16", "2022-12-20", 256)
        );
    }
}