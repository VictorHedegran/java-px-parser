package com.example.pxparser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java Main <file.px>");
            return;
        }

        Map<String, List<String>> px = parse(
            Files.readString(Path.of(args[0]), StandardCharsets.ISO_8859_1)
        );

        List<String> vars = new ArrayList<>(px.getOrDefault("STUB", List.of()));
        vars.addAll(px.getOrDefault("HEADING", List.of()));
        List<List<String>> axes = vars
            .stream()
            .map(v -> px.get("VALUES(" + v + ")"))
            .toList();

        StringBuilder out = new StringBuilder();
        for (String v : vars) out.append(csv(v)).append(',');
        out.append("value\n");

        int[] idx = new int[vars.size()];
        for (String cell : px.get("DATA")) {
            for (int d = 0; d < vars.size(); d++) out.append(
                csv(axes.get(d).get(idx[d]))
            ).append(',');
            out.append(cell).append('\n');
            for (
                int d = vars.size() - 1;
                d >= 0 && ++idx[d] == axes.get(d).size();
                d--
            ) idx[d] = 0;
        }
        System.out.print(out);
    }

    static Map<String, List<String>> parse(String text) {
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
        return px;
    }

    static String csv(String s) {
        return s.contains(",") || s.contains("\"") || s.contains("\n")
            ? '"' + s.replace("\"", "\"\"") + '"'
            : s;
    }
}
