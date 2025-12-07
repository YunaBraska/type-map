package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;
import berlin.yuna.typemap.model.TypeMap;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonDecoderTest {

    private static final String COMPLEX_JSON = "{\"name\":\"neo\",\"nested\":{\"level\":1,\"list\":[{\"x\":1},2,true]},\"tags\":[\"a\",\"b\"]}";

    @Test
    void shouldStreamAllOverloads() throws Exception {
        final Path file = Files.createTempFile("stream-json", ".json");
        Files.writeString(file, COMPLEX_JSON, StandardCharsets.UTF_8);
        final File asFile = file.toFile();
        final URI uri = file.toUri();
        final URL url = uri.toURL();

        assertComplex(JsonDecoder.streamJson(new ByteArrayInputStream(COMPLEX_JSON.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        assertComplex(JsonDecoder.streamJson(new ByteArrayInputStream(COMPLEX_JSON.getBytes(StandardCharsets.UTF_8))));
        assertComplex(JsonDecoder.streamJson(COMPLEX_JSON, StandardCharsets.UTF_8));
        assertComplex(JsonDecoder.streamJson(COMPLEX_JSON));
        assertComplex(JsonDecoder.streamJson(new StringBuilder(COMPLEX_JSON), StandardCharsets.UTF_8));
        assertComplex(JsonDecoder.streamJson(new StringBuilder(COMPLEX_JSON)));
        assertComplex(JsonDecoder.streamJson(file, StandardCharsets.UTF_8));
        assertComplex(JsonDecoder.streamJson(file));
        assertComplex(JsonDecoder.streamJson(asFile, StandardCharsets.UTF_8));
        assertComplex(JsonDecoder.streamJson(asFile));
        assertComplex(JsonDecoder.streamJson(uri, StandardCharsets.UTF_8));
        assertComplex(JsonDecoder.streamJson(uri));
        assertComplex(JsonDecoder.streamJson(url, StandardCharsets.UTF_8));
        assertComplex(JsonDecoder.streamJson(url));
    }

    @Test
    void shouldStreamArrayWithIndexKeys() throws Exception {
        final String array = "[{\"x\":1},2,true]";
        try (final Stream<Pair<String, Object>> stream = JsonDecoder.streamJson(array)) {
            final Map<String, Object> result = stream.collect(Collectors.toMap(Pair::key, Pair::value));
            assertThat(result.keySet()).containsExactly("0", "1", "2");
            assertThat((LinkedTypeMap) result.get("0")).containsEntry("x", 1L);
            assertThat(result).containsEntry("1", 2L);
            assertThat(result).containsEntry("2", true);
        }
    }

    @Test
    void shouldReturnEmptyOnNullOrWhitespace() throws Exception {
        assertThat(JsonDecoder.streamJson((InputStream) null)).isEmpty();
        try (final Stream<Pair<String, Object>> stream = JsonDecoder.streamJson("   ")) {
            assertThat(stream).isEmpty();
        }
    }

    @Test
    void shouldFailOnInvalidJson() {
        assertThatThrownBy(() -> JsonDecoder.streamJson("oops").toList())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expected JSON object or array");
        assertThatThrownBy(() -> JsonDecoder.streamJson("{\"a\":1").toList())
            .isInstanceOf(IllegalStateException.class);
    }

    private void assertComplex(final Stream<Pair<String, Object>> stream) {
        final TypeMap data = new TypeMap();
        try (final Stream<Pair<String, Object>> closeable = stream) {
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
}
