package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;
import berlin.yuna.typemap.model.TypeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonDecoderTest {

    private static final String COMPLEX_JSON = "{\"name\":\"neo\",\"nested\":{\"level\":1,\"list\":[{\"x\":1},2,true]},\"tags\":[\"a\",\"b\"]}";

    @Test
    void shouldStreamObjectOverloads() throws Exception {
        final Path file = Files.createTempFile("stream-json", ".json");
        Files.writeString(file, COMPLEX_JSON, StandardCharsets.UTF_8);
        final File asFile = file.toFile();
        final URI uri = file.toUri();
        final URL url = uri.toURL();

        final List<Supplier<Stream<Pair<String, Object>>>> suppliers = List.of(
            supplier(() -> JsonDecoder.streamJsonObject(new ByteArrayInputStream(COMPLEX_JSON.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonObject(new ByteArrayInputStream(COMPLEX_JSON.getBytes(StandardCharsets.UTF_8)))),
            supplier(() -> JsonDecoder.streamJsonObject(COMPLEX_JSON, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonObject(COMPLEX_JSON)),
            supplier(() -> JsonDecoder.streamJsonObject(new StringBuilder(COMPLEX_JSON), StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonObject(new StringBuilder(COMPLEX_JSON))),
            supplier(() -> JsonDecoder.streamJsonObject(file, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonObject(file)),
            supplier(() -> JsonDecoder.streamJsonObject(asFile, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonObject(asFile)),
            supplier(() -> JsonDecoder.streamJsonObject(uri, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonObject(uri)),
            supplier(() -> JsonDecoder.streamJsonObject(url, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonObject(url))
        );
        suppliers.forEach(this::assertComplex);
    }

    @Test
    void shouldStreamArrayWithIndexKeysAcrossOverloads() throws Exception {
        final String array = "[{\"x\":1},2,true]";
        final Path file = Files.createTempFile("stream-json-array", ".json");
        Files.writeString(file, array, StandardCharsets.UTF_8);
        final File asFile = file.toFile();
        final URI uri = file.toUri();
        final URL url = uri.toURL();

        final List<Supplier<Stream<Pair<Integer, Object>>>> suppliers = List.of(
            supplier(() -> JsonDecoder.streamJsonArray(array)),
            supplier(() -> JsonDecoder.streamJsonArray(array, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonArray(new ByteArrayInputStream(array.getBytes(StandardCharsets.UTF_8)))),
            supplier(() -> JsonDecoder.streamJsonArray(new ByteArrayInputStream(array.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonArray(file)),
            supplier(() -> JsonDecoder.streamJsonArray(file, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonArray(asFile)),
            supplier(() -> JsonDecoder.streamJsonArray(asFile, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonArray(uri)),
            supplier(() -> JsonDecoder.streamJsonArray(uri, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJsonArray(url)),
            supplier(() -> JsonDecoder.streamJsonArray(url, StandardCharsets.UTF_8))
        );
        suppliers.forEach(this::assertArray);
    }

    @Test
    void shouldStreamGenericOverloads() throws Exception {
        final Path file = Files.createTempFile("stream-json-generic", ".json");
        Files.writeString(file, COMPLEX_JSON, StandardCharsets.UTF_8);
        final File asFile = file.toFile();
        final URI uri = file.toUri();
        final URL url = uri.toURL();

        final List<Supplier<Stream<Pair<Object, Object>>>> suppliers = List.of(
            supplier(() -> JsonDecoder.streamJson(new StringBuilder(COMPLEX_JSON))),
            supplier(() -> JsonDecoder.streamJson(new StringBuilder(COMPLEX_JSON), StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJson(file)),
            supplier(() -> JsonDecoder.streamJson(file, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJson(asFile)),
            supplier(() -> JsonDecoder.streamJson(asFile, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJson(uri)),
            supplier(() -> JsonDecoder.streamJson(uri, StandardCharsets.UTF_8)),
            supplier(() -> JsonDecoder.streamJson(url)),
            supplier(() -> JsonDecoder.streamJson(url, StandardCharsets.UTF_8))
        );

        suppliers.forEach(streamSupplier -> assertComplex(() -> streamSupplier.get().map(p -> new Pair<>(String.valueOf(p.key()), p.value()))));
    }

    @Test
    void shouldDecodeEscapesInsideString() {
        final String escaped = "{\"msg\":\"line\\nquote\\\"unicode\\u00a9\"}";
        try (Stream<Pair<String, Object>> stream = JsonDecoder.streamJsonObject(escaped)) {
            final Map<String, Object> result = stream.collect(Collectors.toMap(Pair::key, Pair::value));
            assertThat(result).containsEntry("msg", "line\nquote\"unicode\u00a9");
        }
    }

    @Test
    void shouldHandleInvalidEscapesAsUnchecked() {
        final String invalid = "{\"msg\":\"bad\\u00\"}";
        assertThatThrownBy(() -> JsonDecoder.streamJsonObject(invalid).toList())
            .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void shouldStopArrayStreamOnMalformedSeparator() {
        final String broken = "[1 2]";
        assertThatThrownBy(() -> JsonDecoder.streamJsonArray(broken).toList())
            .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void shouldStopObjectStreamOnMalformedSeparator() {
        final String broken = "{\"a\":1 \"b\":2}";
        assertThatThrownBy(() -> JsonDecoder.streamJsonObject(broken).toList())
            .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void shouldStreamDeeplyNestedObject() {
        final String json = """
            {
              "title":"matrix",
              "meta":{"rating":null,"scores":[1,2,[3,4]],"active":true},
              "items":[{"id":1,"tags":["red","blue"],"data":{"pi":3.14}},{"id":2,"tags":[],"data":{"empty":{}}}]
            }
            """;

        try (Stream<Pair<String, Object>> stream = JsonDecoder.streamJsonObject(json)) {
            final TypeMap root = new TypeMap();
            stream.forEach(p -> root.put(p.key(), p.value()));

            assertThat(root.asString("title")).isEqualTo("matrix");
            assertThat(root.get("meta")).isInstanceOf(LinkedTypeMap.class);
            final LinkedTypeMap meta = (LinkedTypeMap) root.get("meta");
            assertThat(meta).containsEntry("rating", null);
            assertThat(meta.asBoolean("active")).isTrue();
            assertThat(meta.get("scores")).isInstanceOf(TypeList.class);
            final TypeList scores = (TypeList) meta.get("scores");
            assertThat(scores).hasSize(3);
            assertThat(scores.get(2)).isInstanceOf(TypeList.class);
            assertThat((TypeList) scores.get(2)).containsExactly(3L, 4L);

            assertThat(root.get("items")).isInstanceOf(TypeList.class);
            final TypeList items = (TypeList) root.get("items");
            assertThat(items).hasSize(2);
            final LinkedTypeMap first = (LinkedTypeMap) items.get(0);
            assertThat(first.asInt("id")).isEqualTo(1);
            assertThat((TypeList) first.get("tags")).containsExactly("red", "blue");
            assertThat(((LinkedTypeMap) first.get("data")).asDouble("pi")).isEqualTo(3.14);
            final LinkedTypeMap second = (LinkedTypeMap) items.get(1);
            assertThat(second.asInt("id")).isEqualTo(2);
            assertThat((TypeList) second.get("tags")).isEmpty();
            assertThat(((LinkedTypeMap) second.get("data")).get("empty")).isInstanceOf(LinkedTypeMap.class);
        }
    }

    @Test
    void shouldStreamArrayWithNestedObjectsAndUnicode() {
        final String json = """
            [
              {"msg":"привет","nums":[0,-1,2.5],"flag":false},
              {"msg":"こんにちは","nums":[10,20],"flag":true}
            ]
            """;

        try (Stream<Pair<Integer, Object>> stream = JsonDecoder.streamJsonArray(json)) {
            final List<Pair<Integer, Object>> pairs = stream.toList();
            assertThat(pairs).hasSize(2);
            final LinkedTypeMap first = (LinkedTypeMap) pairs.get(0).value();
            assertThat(first.asString("msg")).isEqualTo("привет");
            assertThat((TypeList) first.get("nums")).containsExactly(0L, -1L, 2.5d);
            assertThat(first.asBoolean("flag")).isFalse();

            final LinkedTypeMap second = (LinkedTypeMap) pairs.get(1).value();
            assertThat(second.asString("msg")).isEqualTo("こんにちは");
            assertThat((TypeList) second.get("nums")).containsExactly(10L, 20L);
            assertThat(second.asBoolean("flag")).isTrue();
        }
    }

    @Test
    void shouldReturnEmptyOnNullOrWhitespace() {
        assertThat(JsonDecoder.streamJson((InputStream) null)).isEmpty();
        try (final Stream<Pair<Object, Object>> stream = JsonDecoder.streamJson("   ")) {
            assertThat(stream).isEmpty();
        }
    }

    @Test
    void shouldFailOnInvalidJson() {
        assertThatThrownBy(() -> JsonDecoder.streamJson("oops").toList())
            .isInstanceOf(UncheckedIOException.class);
        assertThatThrownBy(() -> JsonDecoder.streamJson("{\"a\":1").toList())
            .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    @EnabledIfSystemProperty(named = "perf", matches = "true")
    void benchmarkStreamJsonArray() {
        final String payload = largeArrayJson(5000);
        final AtomicInteger count = new AtomicInteger();
        final long memBefore = usedMemoryKb();
        final long start = System.nanoTime();

        try (final Stream<Pair<Integer, Object>> stream = JsonDecoder.streamJsonArray(payload)) {
            stream.forEach(p -> {
                count.incrementAndGet();
                if (count.get() == 1) {
                    assertThat(p.value()).isInstanceOf(LinkedTypeMap.class);
                }
            });
        }

        final long durationMs = (System.nanoTime() - start) / 1_000_000;
        final long memDelta = usedMemoryKb() - memBefore;
        System.out.printf("benchmarkStreamJsonArray items=%d took=%dms memDelta=%dKB%n", count.get(), durationMs, memDelta);
        assertThat(count.get()).isEqualTo(5000);
    }

    @Test
    @EnabledIfSystemProperty(named = "perf", matches = "true")
    void benchmarkStreamJsonObject() {
        final String payload = largeObjectJson(4000);
        final AtomicInteger count = new AtomicInteger();
        final long memBefore = usedMemoryKb();
        final long start = System.nanoTime();

        try (final Stream<Pair<String, Object>> stream = JsonDecoder.streamJsonObject(payload)) {
            stream.forEach(p -> {
                count.incrementAndGet();
                if ("k0".equals(p.key())) {
                    assertThat(p.value()).isInstanceOf(LinkedTypeMap.class);
                }
            });
        }

        final long durationMs = (System.nanoTime() - start) / 1_000_000;
        final long memDelta = usedMemoryKb() - memBefore;
        System.out.printf("benchmarkStreamJsonObject entries=%d took=%dms memDelta=%dKB%n", count.get(), durationMs, memDelta);
        assertThat(count.get()).isEqualTo(4000);
    }

    private void assertComplex(final Supplier<Stream<Pair<String, Object>>> supplier) {
        final TypeMap data = new TypeMap();
        try (final Stream<Pair<String, Object>> closeable = supplier.get()) {
            closeable.forEach(p -> data.put(p.key(), p.value()));
        }


        assertThat(data).containsKeys("name", "nested", "tags");
        assertThat(data).containsEntry("name", "neo");
        assertThat(data.get("nested")).isInstanceOf(LinkedTypeMap.class);
        final LinkedTypeMap nested = (LinkedTypeMap) data.get("nested");
        assertThat(nested).containsEntry("level", 1L);
        assertThat(nested.get("list")).isInstanceOf(TypeList.class);
        final TypeList list = (TypeList) nested.get("list");
        assertThat(list).hasSize(3);
        assertThat((LinkedTypeMap) list.get(0)).containsEntry("x", 1L);
        assertThat(list).contains(2L, true);
        assertThat(data.get("tags")).isInstanceOf(TypeList.class);
        assertThat((TypeList) data.get("tags")).containsExactly("a", "b");
    }

    private void assertArray(final Supplier<Stream<Pair<Integer, Object>>> supplier) {
        try (Stream<Pair<Integer, Object>> stream = supplier.get()) {
            final Map<Integer, Object> result = stream.collect(Collectors.toMap(Pair::key, Pair::value));
            assertThat(result.keySet()).containsExactly(0, 1, 2);
            assertThat((LinkedTypeMap) result.get(0)).containsEntry("x", 1L);
            assertThat(result).containsEntry(1, 2L);
            assertThat(result).containsEntry(2, true);
        }
    }

    private <T> Supplier<T> supplier(final Callable<T> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static String largeArrayJson(final int size) {
        final StringBuilder sb = new StringBuilder(size * 64).append('[');
        for (int i = 0; i < size; i++) {
            if (i > 0)
                sb.append(',');
            sb.append("{\"id\":").append(i).append(",\"name\":\"n").append(i).append("\",\"flags\":[true,false,true]}");
        }
        sb.append(']');
        return sb.toString();
    }

    private static String largeObjectJson(final int size) {
        final StringBuilder sb = new StringBuilder(size * 48).append('{');
        for (int i = 0; i < size; i++) {
            if (i > 0)
                sb.append(',');
            sb.append("\"k").append(i).append("\":{\"idx\":").append(i).append(",\"nested\":{\"v\":").append(i * 2).append("}}");
        }
        sb.append('}');
        return sb.toString();
    }

    private static long usedMemoryKb() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }
}
