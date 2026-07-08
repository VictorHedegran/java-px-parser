package com.example.pxparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PxParserTest {

    static Path fixture;
    static PxFile px;

    @BeforeAll
    static void parseFixture() throws Exception {
        fixture = Paths.get(PxParserTest.class.getResource("/TAB2017.px").toURI());
        px = PxParser.parse(fixture);
    }

    @Test
    void scalarKeywords() {
        assertEquals(List.of("ANSI"), px.keyword("CHARSET"));
        assertEquals(List.of("TAB2017"), px.keyword("MATRIX"));
        assertEquals(List.of("2"), px.keyword("DECIMALS"));
        assertEquals(List.of("Kommunalskatter efter region och år"), px.keyword("TITLE"));
    }

    @Test
    void insertionOrderPreserved() {
        List<String> keys = List.copyOf(px.keywords().keySet());
        assertEquals("CHARSET", keys.get(0));
        assertEquals("DATA", keys.get(keys.size() - 1));
    }

    @Test
    void stubAndHeading() {
        assertEquals(List.of("region"), px.stub());
        assertEquals(List.of("år", "tabellinnehåll"), px.heading());
        assertEquals(List.of("region", "år", "tabellinnehåll"), px.variables());
    }

    @Test
    void quotesStrippedFromSubscriptedKeys() {
        assertTrue(px.keywords().containsKey("VALUES(region)"));
        assertFalse(px.keywords().containsKey("VALUES(\"region\")"));
        assertTrue(px.keywords().containsKey("VALUENOTEX(tabellinnehåll,Skattesats, total kommunal)"));
    }

    @Test
    void regionValuesAndCodes() {
        List<String> regions = px.values("region");
        assertEquals(290, regions.size());
        assertEquals("Upplands Väsby", regions.get(0));
        assertEquals("Kiruna", regions.get(289));

        List<String> codes = px.codes("region");
        assertEquals(290, codes.size());
        assertEquals("0114", codes.get(0));
        assertEquals("2584", codes.get(289));
    }

    @Test
    void contentsValuesKeepCommasInsideQuotes() {
        assertEquals(
            List.of("Skattesats, total kommunal", "Skattesats till kommun", "Skattesats till region"),
            px.values("tabellinnehåll")
        );
    }

    @Test
    void timevalSplitsIntoTwoValues() {
        assertEquals(List.of("TLIST(A1)", "2026"), px.keyword("TIMEVAL(år)"));
    }

    @Test
    void dataCells() {
        List<String> data = px.data();
        assertEquals(870, data.size());
        assertEquals("31.75", data.get(0));
        assertEquals("19.42", data.get(1));
        assertEquals("11.34", data.get(869));
    }

    @Test
    void missingKeywordIsEmptyList() {
        assertEquals(List.of(), px.keyword("NO_SUCH_KEYWORD"));
        assertEquals(List.of(), px.values("no_such_variable"));
    }

    @Test
    void parseQuirks() {
        assertEquals(List.of("a;b"), PxParser.parse("K=\"a;b\";").keyword("K"));
        assertEquals(List.of("a,b"), PxParser.parse("K=\"a,b\";").keyword("K"));
        assertEquals(List.of("1", "2", "3"), PxParser.parse("K=1,2,3;").keyword("K"));
        assertEquals(List.of("a", "b"), PxParser.parse("K=\"a\",,\"b\";").keyword("K"));
        assertTrue(PxParser.parse("K=\"a\"").keywords().isEmpty());
        assertEquals(List.of("1", "2", "3", "4"), PxParser.parse("DATA=\n1 2\n3\t4;").data());
        assertEquals(List.of("a b"), PxParser.parse("K=a b;").keyword("K"));
        assertEquals(List.of("a", "b"), PxParser.parse("K= a , b ;").keyword("K"));
    }
}
