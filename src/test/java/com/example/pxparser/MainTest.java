package com.example.pxparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MainTest {

    static Path fixture;
    static Map<String, List<String>> px;

    @BeforeAll
    static void parseFixture() throws Exception {
        fixture = Paths.get(MainTest.class.getResource("/TAB2017.px").toURI());
        px = Main.parse(Files.readString(fixture, StandardCharsets.ISO_8859_1));
    }

    @Test
    void scalarKeywords() {
        assertEquals(List.of("ANSI"), px.get("CHARSET"));
        assertEquals(List.of("TAB2017"), px.get("MATRIX"));
        assertEquals(List.of("2"), px.get("DECIMALS"));
        assertEquals(List.of("Kommunalskatter efter region och år"), px.get("TITLE"));
    }

    @Test
    void insertionOrderPreserved() {
        List<String> keys = List.copyOf(px.keySet());
        assertEquals("CHARSET", keys.get(0));
        assertEquals("DATA", keys.get(keys.size() - 1));
    }

    @Test
    void stubAndHeading() {
        assertEquals(List.of("region"), px.get("STUB"));
        assertEquals(List.of("år", "tabellinnehåll"), px.get("HEADING"));
    }

    @Test
    void quotesStrippedFromSubscriptedKeys() {
        assertTrue(px.containsKey("VALUES(region)"));
        assertFalse(px.containsKey("VALUES(\"region\")"));
        assertTrue(px.containsKey("VALUENOTEX(tabellinnehåll,Skattesats, total kommunal)"));
    }

    @Test
    void regionValuesAndCodes() {
        List<String> regions = px.get("VALUES(region)");
        assertEquals(290, regions.size());
        assertEquals("Upplands Väsby", regions.get(0));
        assertEquals("Kiruna", regions.get(289));

        List<String> codes = px.get("CODES(region)");
        assertEquals(290, codes.size());
        assertEquals("0114", codes.get(0));
        assertEquals("2584", codes.get(289));
    }

    @Test
    void contentsValuesKeepCommasInsideQuotes() {
        assertEquals(
            List.of("Skattesats, total kommunal", "Skattesats till kommun", "Skattesats till region"),
            px.get("VALUES(tabellinnehåll)")
        );
    }

    @Test
    void timevalSplitsIntoTwoValues() {
        assertEquals(List.of("TLIST(A1)", "2026"), px.get("TIMEVAL(år)"));
    }

    @Test
    void dataCells() {
        List<String> data = px.get("DATA");
        assertEquals(870, data.size());
        assertEquals("31.75", data.get(0));
        assertEquals("19.42", data.get(1));
        assertEquals("11.34", data.get(869));
    }

    @Test
    void parseQuirks() {
        assertEquals(List.of("a;b"), Main.parse("K=\"a;b\";").get("K"));
        assertEquals(List.of("a,b"), Main.parse("K=\"a,b\";").get("K"));
        assertEquals(List.of("1", "2", "3"), Main.parse("K=1,2,3;").get("K"));
        assertEquals(List.of("a", "b"), Main.parse("K=\"a\",,\"b\";").get("K"));
        assertTrue(Main.parse("K=\"a\"").isEmpty());
        assertEquals(List.of("1", "2", "3", "4"), Main.parse("DATA=\n1 2\n3\t4;").get("DATA"));
        assertEquals(List.of("a b"), Main.parse("K=a b;").get("K"));
        assertEquals(List.of("a", "b"), Main.parse("K= a , b ;").get("K"));
    }

    @Test
    void csvEscaping() {
        assertEquals("abc", Main.csv("abc"));
        assertEquals("\"a,b\"", Main.csv("a,b"));
        assertEquals("\"a\"\"b\"", Main.csv("a\"b"));
        assertEquals("\"a\nb\"", Main.csv("a\nb"));
    }

    @Test
    void endToEndCsvOutput() throws Exception {
        String[] lines = runMain(fixture.toString()).split("\n", -1);
        assertEquals("region,år,tabellinnehåll,value", lines[0]);
        assertEquals(872, lines.length);
        assertEquals("", lines[871]);
        assertEquals("Upplands Väsby,2026,\"Skattesats, total kommunal\",31.75", lines[1]);
        assertEquals("Upplands Väsby,2026,Skattesats till kommun,19.42", lines[2]);
        assertEquals("Upplands Väsby,2026,Skattesats till region,12.33", lines[3]);
        assertEquals("Vallentuna,2026,\"Skattesats, total kommunal\",31.23", lines[4]);
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
