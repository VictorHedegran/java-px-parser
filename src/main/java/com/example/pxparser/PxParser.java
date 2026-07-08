package com.example.pxparser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses PX (PC-Axis) file text into a {@link PxFile}.
 */
public final class PxParser {

    private PxParser() {}

    /** Parses a PX file from disk using the ISO-8859-1 encoding PX files default to. */
    public static PxFile parse(Path file) throws IOException {
        return parse(Files.readString(file, StandardCharsets.ISO_8859_1));
    }

    public static PxFile parse(String text) {
        Map<String, List<String>> px = new LinkedHashMap<>();
        String key = null;
        List<String> vals = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (quoted) {
                cur.append(c);
            } else if (key == null) {
                if (c == '=') {
                    key = cur.toString().trim();
                    cur.setLength(0);
                } else cur.append(c);
            } else if (
                c == ';' ||
                (key.equals("DATA") ? Character.isWhitespace(c) : c == ',')
            ) {
                String v = cur.toString().trim();
                if (!v.isEmpty()) vals.add(v);
                cur.setLength(0);
                if (c == ';') {
                    px.put(key, vals);
                    key = null;
                    vals = new ArrayList<>();
                }
            } else {
                cur.append(c);
            }
        }
        return new PxFile(px);
    }
}
