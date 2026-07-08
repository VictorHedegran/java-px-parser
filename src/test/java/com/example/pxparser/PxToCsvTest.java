package com.example.pxparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class PxToCsvTest {

    @Test
    void csvEscaping() {
        assertEquals("abc", PxToCsv.csv("abc"));
        assertEquals("\"a,b\"", PxToCsv.csv("a,b"));
        assertEquals("\"a\"\"b\"", PxToCsv.csv("a\"b"));
        assertEquals("\"a\nb\"", PxToCsv.csv("a\nb"));
    }

    @Test
    void cartesianExpansion() throws Exception {
        Path fixture = Paths.get(PxToCsvTest.class.getResource("/TAB2017.px").toURI());
        StringBuilder out = new StringBuilder();
        PxToCsv.write(PxParser.parse(fixture), out);

        String[] lines = out.toString().split("\n", -1);
        assertEquals("region,år,tabellinnehåll,value", lines[0]);
        assertEquals(872, lines.length);
        assertEquals("", lines[871]);
        assertEquals("Upplands Väsby,2026,\"Skattesats, total kommunal\",31.75", lines[1]);
        assertEquals("Upplands Väsby,2026,Skattesats till kommun,19.42", lines[2]);
        assertEquals("Upplands Väsby,2026,Skattesats till region,12.33", lines[3]);
        assertEquals("Vallentuna,2026,\"Skattesats, total kommunal\",31.23", lines[4]);
        assertEquals("Kiruna,2026,Skattesats till region,11.34", lines[870]);
    }
}
