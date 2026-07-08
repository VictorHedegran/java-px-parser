package com.example.pxparser;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java Main <file.px>");
            return;
        }
        PxToCsv.write(PxParser.parse(Path.of(args[0])), System.out);
    }
}
