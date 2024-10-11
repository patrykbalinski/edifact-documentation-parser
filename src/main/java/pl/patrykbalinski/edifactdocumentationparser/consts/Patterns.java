package pl.patrykbalinski.edifactdocumentationparser.consts;

import java.util.regex.Pattern;

public class Patterns {

    public static final Pattern SINGLE_SEGMENT_LINE_PATTERN = Pattern.compile("^(\\d{5})\\s+([A-Z]{3})\\s+(.*?)\\s+([MC])\\s+(\\d+).*$");
    public static final Pattern SEGMENT_GROUP_LINE_PATTERN = Pattern.compile("^(\\d{5})\\s+---- ([A-Za-z ]+[0-9]+)\\s+.*?\\s+([MC])\\s+(\\d+).*$");
    public static final Pattern SEGMENT_CLARIFICATION_HEADER_LINE_PATTERN = Pattern.compile("^(\\d{5})\\s+([A-Z]{3})?,? ?(.*)$");
    public static final Pattern SEGMENT_CLARIFICATION_DESCRIPTION_LINE_PATTERN = Pattern.compile("^\\s+(.*)$");
}
