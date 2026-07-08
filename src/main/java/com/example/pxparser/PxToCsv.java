package com.example.pxparser;

import java.io.IOException;
import java.util.List;

/**
 * Writes a {@link PxFile} as CSV: one column per STUB/HEADING variable
 * plus a trailing value column, DATA expanded in row-major order.
 */
public final class PxToCsv {

    private PxToCsv() {}

    public static void write(PxFile px, Appendable out) throws IOException {
        List<String> vars = px.variables();
        List<List<String>> axes = vars.stream().map(px::values).toList();

        for (String v : vars) out.append(csv(v)).append(',');
        out.append("value\n");

        int[] idx = new int[vars.size()];
        for (String cell : px.data()) {
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
    }

    static String csv(String s) {
        return s.contains(",") || s.contains("\"") || s.contains("\n")
            ? '"' + s.replace("\"", "\"\"") + '"'
            : s;
    }
}
