# px-parser

Small Java 21 library for parsing PX (PC-Axis) statistics files, with a CSV converter and a CLI.

## Use as a dependency

### Via JitPack (from GitHub, no publishing infra)

Add the JitPack repository and depend on a git tag:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.VictorHedegran</groupId>
  <artifactId>java-px-parser</artifactId>
  <version>v0.1.0</version>
</dependency>
```

Gradle:

```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.VictorHedegran:java-px-parser:v0.1.0")
}
```

Any pushed tag becomes a version. `main-SNAPSHOT` tracks the latest commit on `main`.

### Via local install

```sh
./mvnw install
```

Then depend on `com.example:px-parser:0.1.0-SNAPSHOT` from other local projects.

## API

```java
import com.example.pxparser.PxFile;
import com.example.pxparser.PxParser;
import com.example.pxparser.PxToCsv;

PxFile px = PxParser.parse(Path.of("table.px")); // ISO-8859-1, the PX default
// or PxParser.parse(String text)

px.variables();      // STUB + HEADING variable names
px.values("region"); // category values for a variable
px.data();           // DATA cells in row-major order

StringBuilder csv = new StringBuilder();
PxToCsv.write(px, csv); // one column per variable + trailing "value" column
```

## CLI

The build also produces an executable jar (classifier `cli`):

```sh
./mvnw package
java -jar target/px-parser-0.1.0-SNAPSHOT-cli.jar input.px > output.csv
```

## Releasing a new version

Tag and push — JitPack builds on first request:

```sh
git tag v0.2.0
git push origin v0.2.0
```
