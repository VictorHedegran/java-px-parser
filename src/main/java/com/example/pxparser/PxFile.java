package com.example.pxparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parsed PX file: keywords in file order, each mapping to its list of values.
 */
public final class PxFile {

    private final Map<String, List<String>> keywords;

    PxFile(Map<String, List<String>> keywords) {
        this.keywords = Collections.unmodifiableMap(keywords);
    }

    /** All keywords in file order. Values lists are the parsed values. */
    public Map<String, List<String>> keywords() {
        return keywords;
    }

    /** Values for a keyword, or an empty list if absent. */
    public List<String> keyword(String name) {
        return keywords.getOrDefault(name, List.of());
    }

    public List<String> stub() {
        return keyword("STUB");
    }

    public List<String> heading() {
        return keyword("HEADING");
    }

    public List<String> values(String variable) {
        return keyword("VALUES(" + variable + ")");
    }

    public List<String> codes(String variable) {
        return keyword("CODES(" + variable + ")");
    }

    public List<String> data() {
        return keyword("DATA");
    }

    /** STUB variables followed by HEADING variables: the axes of DATA. */
    public List<String> variables() {
        List<String> vars = new ArrayList<>(stub());
        vars.addAll(heading());
        return vars;
    }
}
