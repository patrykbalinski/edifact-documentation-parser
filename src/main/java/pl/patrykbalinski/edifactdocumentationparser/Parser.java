package pl.patrykbalinski.edifactdocumentationparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    static final Pattern SINGLE_SEGMENT_PATTERN = Pattern.compile("(^\\d{5})\\s+([A-Z]{3})\\s+(.*?)\\s+([MC])\\s+(\\d+).*$");
    static final Pattern SEGMENT_GROUP_PATTERN = Pattern.compile("^(\\d{5})\\s+---- ([A-Za-z ]+[0-9]+)\\s+.*?\\s+([MC])\\s+(\\d+).*$");


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

        edifactMessage.setSegments(parseMessageStructure(content.subList(content.indexOf("4.3    Message structure"), content.size())));

        return edifactMessage;
    }

    public static List<Segment> parseMessageStructure(List<String> lines) {
        List<Segment> elements = new ArrayList<>();

        List<SegmentGroup> groupStack = new ArrayList<>();  // Stos do śledzenia zagnieżdżonych grup
        SegmentGroup currentGroup = null;

        for (String line : lines) {
            Matcher singleSegmentMatcher = SINGLE_SEGMENT_PATTERN.matcher(line);
            Matcher segmentGroupMatcher = SEGMENT_GROUP_PATTERN.matcher(line);

            // Dopasowanie dla pojedynczego segmentu
            if (singleSegmentMatcher.matches()) {
                String number = singleSegmentMatcher.group(1);
                String code = singleSegmentMatcher.group(2);
                String name = singleSegmentMatcher.group(3).trim();
                boolean mandatory = singleSegmentMatcher.group(4).equals("M");
                int maxOccurrences = Integer.parseInt(singleSegmentMatcher.group(5));

                SingleSegment singleSegment = new SingleSegment(number, code, name, mandatory, maxOccurrences);

                // Jeśli jesteśmy w grupie, dodajemy segment do bieżącej grupy
                if (!groupStack.isEmpty()) {
                    currentGroup.addSegment(singleSegment);
                } else {
                    elements.add(singleSegment);
                }

                if (line.contains("-+")) {
                    // Zdejmujemy ostatnią grupę ze stosu, zamykając ją
                    for (int i = 0; i < line.chars().filter(ch -> ch == '+').count(); i++) {
                        if (!groupStack.isEmpty()) {
                            groupStack.remove(groupStack.size() - 1);
                            currentGroup = groupStack.isEmpty() ? null : groupStack.get(groupStack.size() - 1);
                        }
                    }
                }

                if (line.contains("-+")) {
                    // Liczymy, ile razy występuje znak '+' w linii
                    long plusCount = line.chars().filter(ch -> ch == '+').count();

                    // Zdejmujemy odpowiednią liczbę grup ze stosu
                    for (int i = 0; i < plusCount && !groupStack.isEmpty(); i++) {
                        groupStack.remove(groupStack.size() - 1);
                    }

                    // Ustawiamy aktualną grupę na ostatni element stosu lub null, jeśli stos jest pusty
                    currentGroup = groupStack.isEmpty() ? null : groupStack.get(groupStack.size() - 1);
                }

                // Dopasowanie dla grupy segmentów
            } else if (segmentGroupMatcher.matches()) {
                String groupNumber = segmentGroupMatcher.group(1);
                String groupName = segmentGroupMatcher.group(2);
                boolean mandatory = segmentGroupMatcher.group(3).equals("M");
                int maxOccurrences = Integer.parseInt(segmentGroupMatcher.group(4));

                SegmentGroup newGroup = new SegmentGroup(groupNumber, groupName, mandatory, maxOccurrences);

                // Jeżeli mamy otwartą grupę, dodajemy nową grupę do bieżącej grupy
                if (!groupStack.isEmpty()) {
                    currentGroup.addSegment(newGroup);
                } else {
                    elements.add(newGroup);
                }

                // Nową grupę dodajemy na stos i ustawiamy jako bieżącą grupę
                groupStack.add(newGroup);
                currentGroup = newGroup;

                // Zamykanie grupy segmentów
            }
        }

        return elements;
    }
}
