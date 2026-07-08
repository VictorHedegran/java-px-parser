package com.example.pxparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void endToEndCsvOutput() throws Exception {
        String fixture = Paths.get(MainTest.class.getResource("/TAB2017.px").toURI()).toString();
        String[] lines = runMain(fixture).split("\n", -1);
        assertEquals("region,år,tabellinnehåll,value", lines[0]);
        assertEquals(872, lines.length);
        assertEquals("Upplands Väsby,2026,\"Skattesats, total kommunal\",31.75", lines[1]);
        assertEquals("Kiruna,2026,Skattesats till region,11.34", lines[870]);
    }

    @Test
    void noArgsPrintsUsage() throws Exception {
        assertEquals("Usage: java Main <file.px>\n", runMain());
    }

    static String runMain(String... args) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true, StandardCharsets.UTF_8));
        try {
            Main.main(args);
        } finally {
            System.setOut(original);
        }
        return buf.toString(StandardCharsets.UTF_8);
    }
}
