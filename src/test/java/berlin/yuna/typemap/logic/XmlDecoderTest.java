package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.TypeMap;
import berlin.yuna.typemap.model.TypeList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class XmlDecoderTest {

    final Path pomPath = Paths.get(System.getProperty("user.dir"), "pom.xml");

    @Test
    void decodeXml() throws IOException {
        final TypeList result = XmlDecoder.xmlTypeOf(pomPath.toFile());
        result.get(String.class, "project", "description");
        assertThat(result.get(String.class, "project", "groupId")).isEqualTo("berlin.yuna");
        assertThat(result.get(String.class, "project", "artifactId")).isEqualTo("type-map");
        assertThat(result.get(Integer.class, "project", "properties", "java-version")).isEqualTo(17);
        assertThat(result.asList(Pair.class, "project", "dependencies")).hasSize(5);

        assertThat(XmlDecoder.xmlTypeOf(Files.newInputStream(pomPath))).isEqualTo(result);
        assertThat(XmlDecoder.xmlTypeOf(new String(Files.readAllBytes(pomPath)))).isEqualTo(result);

        assertThat(XmlDecoder.xmlTypeOf((File) null)).isEqualTo(new TypeList());
        assertThat(XmlDecoder.xmlTypeOf((String) null)).isEqualTo(new TypeList());
        assertThat(XmlDecoder.xmlTypeOf((InputStream) null)).isEqualTo(new TypeList());

        XmlDecoder.xmlTypeOf(XmlEncoder.toXml(result));
    }

    @Test
    void encodeXml() {
        final TypeList xml1 = XmlDecoder.xmlTypeOf(pomPath.toFile());
        final String encode1 = XmlEncoder.toXml(xml1);

        final TypeList xml2 = XmlDecoder.xmlTypeOf(encode1);
        final String encode2 = XmlEncoder.toXml(xml2);

        assertThat(encode1).isNotNull().isNotBlank().isNotEmpty().isEqualTo(encode2);
        assertThat(xml1).isEqualTo(xml2);
    }

    @Test
    void decodeEncode_withInvalidSource() {
        assertThat(XmlEncoder.toXml(null)).isEmpty();
        assertThat(XmlEncoder.toXml(new TypeList())).isEmpty();
        assertThat(XmlDecoder.xmlTypeOf("")).isEqualTo(new TypeList());
        assertThat(XmlDecoder.xmlTypeOf((File) null)).isEqualTo(new TypeList());
        assertThat(XmlDecoder.xmlTypeOf((String) null)).isEqualTo(new TypeList());
        assertThat(XmlDecoder.xmlTypeOf((InputStream) null)).isEqualTo(new TypeList());
    }

    @Test
    void shouldRoundTripXmlMapWithMultipleEntries() {
        final LinkedTypeMap input = new LinkedTypeMap();
        final TypeList items = new TypeList();
        items.add(new Pair<>("item", "a"));
        input.put("root", items);
        input.put("extra", "value");

        final String xml = XmlEncoder.toXmlMap(input);
        final LinkedTypeMap output = TypeMap.fromXml(xml);

        assertThat(output).containsOnlyKeys("root");
        final TypeList rootList = (TypeList) output.get("root");
        final Pair<?, ?> itemPair = rootList.stream()
            .filter(Pair.class::isInstance)
            .map(Pair.class::cast)
            .filter(pair -> "item".equals(pair.getKey()))
            .findFirst()
            .orElseThrow();
        final Pair<?, ?> extraPair = rootList.stream()
            .filter(Pair.class::isInstance)
            .map(Pair.class::cast)
            .filter(pair -> "extra".equals(pair.getKey()))
            .findFirst()
            .orElseThrow();
        assertThat(itemPair.getKey()).isEqualTo("item");
        assertThat(((TypeList) itemPair.getValue()).get(0)).isEqualTo("a");
        assertThat(((TypeList) extraPair.getValue()).get(0)).isEqualTo("value");
    }

    @Test
    void shouldStreamXmlObject() {
        final String xml = """
            <root>
              <item id="1"><name>neo</name><flags><flag>true</flag><flag>false</flag></flags></item>
              <item id="2"><name>trinity</name><flags/></item>
            </root>
            """;
        try (final Stream<Pair<String, Object>> stream = XmlDecoder.streamXmlObject(xml, StandardCharsets.UTF_8)) {
            final Map<String, Object> result = stream.collect(Collectors.toMap(Pair::key, Pair::value));
            assertThat(result).containsOnlyKeys("root");
            final TypeList rootList = (TypeList) result.get("root");
            assertThat(rootList).hasSize(2);
            final Pair<?, ?> first = (Pair<?, ?>) rootList.get(0);
            assertThat(first.getKey()).isEqualTo("item");
            final TypeList firstItem = (TypeList) first.getValue();
            assertThat(firstItem.get(String.class, "@id")).isEqualTo("1");
            assertThat(firstItem.get(String.class, "name", 0)).isEqualTo("neo");
            final Pair<?, ?> flags = (Pair<?, ?>) firstItem.get(2);
            final TypeList flagList = (TypeList) flags.getValue();
            assertThat(flagList).hasSize(2);
            final Pair<?, ?> firstFlag = (Pair<?, ?>) flagList.get(0);
            final Pair<?, ?> secondFlag = (Pair<?, ?>) flagList.get(1);
            assertThat(firstFlag.getKey()).isEqualTo("flag");
            assertThat(((TypeList) firstFlag.getValue()).get(0)).isEqualTo("true");
            assertThat(secondFlag.getKey()).isEqualTo("flag");
            assertThat(((TypeList) secondFlag.getValue()).get(0)).isEqualTo("false");
        }
    }

    @Test
    void shouldStreamXmlHandlesEmptyAndText() {
        final String xml = "<root><empty/><text> hi </text></root>";
        try (final Stream<Pair<String, Object>> stream = XmlDecoder.streamXmlObject(xml)) {
            final Map<String, Object> result = stream.collect(Collectors.toMap(Pair::key, Pair::value));
            final TypeList root = (TypeList) result.get("root");
            assertThat(((Pair<?, ?>) root.get(0)).getValue()).isInstanceOf(TypeList.class);
            final Object textValue = ((Pair<?, ?>) root.get(1)).getValue();
            assertThat(textValue).isInstanceOf(TypeList.class);
            assertThat(((TypeList) textValue).get(0)).isEqualTo("hi");
        }
    }

    @Test
    void shouldStreamXmlAcrossOverloads() throws Exception {
        final String xml = "<root><item id=\"1\">alpha</item><item id=\"2\">beta</item></root>";
        final Path file = Files.createTempFile("stream-xml", ".xml");
        Files.writeString(file, xml, StandardCharsets.UTF_8);
        final File asFile = file.toFile();
        final java.net.URI uri = file.toUri();
        final java.net.URL url = uri.toURL();

        final Map<String, Object> expected = collect(XmlDecoder.streamXmlObject(xml));
        final Map<String, Object> fromBytes = collect(XmlDecoder.streamXmlObject(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        final Map<String, Object> fromDefault = collect(XmlDecoder.streamXmlObject(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        final Map<String, Object> fromFile = collect(XmlDecoder.streamXmlObject(file));
        final Map<String, Object> fromFileWithCharset = collect(XmlDecoder.streamXmlObject(file, StandardCharsets.UTF_8));
        final Map<String, Object> fromAsFile = collect(XmlDecoder.streamXmlObject(asFile));
        final Map<String, Object> fromAsFileCharset = collect(XmlDecoder.streamXmlObject(asFile, StandardCharsets.UTF_8));
        final Map<String, Object> fromUri = collect(XmlDecoder.streamXmlObject(uri));
        final Map<String, Object> fromUriCharset = collect(XmlDecoder.streamXmlObject(uri, StandardCharsets.UTF_8));
        final Map<String, Object> fromUrl = collect(XmlDecoder.streamXmlObject(url));
        final Map<String, Object> fromUrlCharset = collect(XmlDecoder.streamXmlObject(url, StandardCharsets.UTF_8));

        assertThat(expected).isEqualTo(fromBytes)
            .isEqualTo(fromDefault)
            .isEqualTo(fromFile)
            .isEqualTo(fromFileWithCharset)
            .isEqualTo(fromAsFile)
            .isEqualTo(fromAsFileCharset)
            .isEqualTo(fromUri)
            .isEqualTo(fromUriCharset)
            .isEqualTo(fromUrl)
            .isEqualTo(fromUrlCharset);
        assertThat(((TypeList) expected.get("root"))).hasSize(2);
    }

    @Test
    @EnabledIfSystemProperty(named = "perf", matches = "true")
    void benchmarkStreamXmlObject() {
        final String xml = largeXml(2000);
        final AtomicInteger count = new AtomicInteger();
        final long memBefore = usedMemoryKb();
        final long start = System.nanoTime();
        try (final Stream<Pair<String, Object>> stream = XmlDecoder.streamXmlObject(xml, StandardCharsets.UTF_8)) {
            stream.forEach(p -> {
                count.incrementAndGet();
                assertThat(p.key()).isEqualTo("root");
            });
        }
        final long durationMs = (System.nanoTime() - start) / 1_000_000;
        final long memDelta = usedMemoryKb() - memBefore;
        System.out.printf("benchmarkStreamXmlObject elements=%d took=%dms memDelta=%dKB%n", count.get(), durationMs, memDelta);
        assertThat(count.get()).isEqualTo(1);
    }

    private static String largeXml(final int size) {
        final StringBuilder builder = new StringBuilder(size * 48).append("<root>");
        for (int i = 0; i < size; i++) {
            builder.append("<item id=\"").append(i).append("\"><name>n").append(i).append("</name><value>").append(i * 2).append("</value></item>");
        }
        builder.append("</root>");
        return builder.toString();
    }

    private static long usedMemoryKb() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }

    private static Map<String, Object> collect(final Stream<Pair<String, Object>> stream) {
        try (Stream<Pair<String, Object>> s = stream) {
            return s.collect(Collectors.toMap(Pair::key, Pair::value));
        }
    }
}
