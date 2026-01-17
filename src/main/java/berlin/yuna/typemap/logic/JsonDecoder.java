package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.Type;
import berlin.yuna.typemap.model.TypeInfo;
import berlin.yuna.typemap.model.TypeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.PushbackInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

public class JsonDecoder {

    /**
     * Parses JSON or XML content from a string and returns a {@link TypeInfo} (map/list) structure.
     * Uses the streaming path under the hood to keep memory usage predictable.
     */
    @SuppressWarnings("java:S1452")
    public static TypeInfo<?> typeOf(final String jsonOrXml) {
        return jsonOrXml == null
            ? new TypeList()
            : typeOf(new ByteArrayInputStream(jsonOrXml.getBytes(UTF_8)));
    }

    /**
     * Parses JSON or XML content from a stream and returns a {@link TypeInfo} (map/list) structure.
     */
    @SuppressWarnings("java:S1452")
    public static TypeInfo<?> typeOf(final InputStream jsonOrXml) {
        return typeOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML content from a stream with the given charset and returns a {@link TypeInfo}.
     */
    @SuppressWarnings("java:S1452")
    public static TypeInfo<?> typeOf(final InputStream jsonOrXml, final Charset charset) {
        try {
            final Object result = detectAndParse(jsonOrXml, charset);
            return result instanceof TypeInfo ? (TypeInfo<?>) result : new TypeList().addR(result);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses JSON or XML into a {@link LinkedTypeMap}.
     * For JSON objects, the map contains the object entries.
     * For JSON arrays, XML, or primitive values, the map contains a single entry with key "" and the parsed value.
     * Returns an empty map when input is null, blank, or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final String jsonOrXml) {
        if (jsonOrXml == null)
            return new LinkedTypeMap();
        try (final ByteArrayInputStream in = new ByteArrayInputStream(jsonOrXml.getBytes(UTF_8))) {
            return mapOf(in, UTF_8);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    /**
     * Parses JSON or XML into a {@link LinkedTypeMap}.
     * For JSON objects, the map contains the object entries.
     * For JSON arrays, XML, or primitive values, the map contains a single entry with key "" and the parsed value.
     * Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final InputStream jsonOrXml) {
        return mapOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML into a {@link LinkedTypeMap} using the provided charset for JSON content.
     * For JSON objects, the map contains the object entries.
     * For JSON arrays, XML, or primitive values, the map contains a single entry with key "" and the parsed value.
     * Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final InputStream jsonOrXml, final Charset charset) {
        try {
            final Object result = detectAndParse(jsonOrXml, charset);
            if (result instanceof final LinkedTypeMap map) {
                return map;
            } else if (result != null) {
                return new LinkedTypeMap().putR("", result);
            }
        } catch (final IOException ignored) {
            // fallthrough to empty map
        }
        return new LinkedTypeMap();
    }

    /**
     * Parses JSON or XML into a {@link TypeList}.
     * For JSON arrays or XML, returns the parsed list.
     * For JSON objects or primitive values, returns a list containing the parsed value.
     * Returns an empty list when input is null, blank, or cannot be parsed.
     */
    public static TypeList listOf(final String jsonOrXml) {
        if (jsonOrXml == null) {
            return new TypeList();
        }
        try (final ByteArrayInputStream in = new ByteArrayInputStream(jsonOrXml.getBytes(UTF_8))) {
            return listOf(in, UTF_8);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses JSON or XML into a {@link TypeList}.
     * For JSON arrays or XML, returns the parsed list.
     * For JSON objects or primitive values, returns a list containing the parsed value.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final InputStream jsonOrXml) {
        return listOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} using the provided charset for JSON content.
     * For JSON arrays or XML, returns the parsed list.
     * For JSON objects or primitive values, returns a list containing the parsed value.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final InputStream jsonOrXml, final Charset charset) {
        try {
            final Object result = detectAndParse(jsonOrXml, charset);
            if (result instanceof final TypeList list) {
                return list;
            } else if (result instanceof final LinkedTypeMap map) {
                return new TypeList().addR(map);
            } else if (result != null) {
                return new TypeList(singletonList(result));
            }
        } catch (final IOException ignored) {
            // fallthrough
        }
        return new TypeList();
    }

    /**
     * Parses JSON or XML CharSequence into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Returns an empty map when input is null, blank, or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final CharSequence jsonOrXml) {
        return jsonOrXml == null ? new LinkedTypeMap() : mapOf(jsonOrXml.toString());
    }

    /**
     * Parses JSON or XML CharSequence into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null, blank, or cannot be parsed.
     */
    public static TypeList listOf(final CharSequence jsonOrXml) {
        return jsonOrXml == null ? new TypeList() : listOf(jsonOrXml.toString());
    }

    /**
     * Parses JSON or XML file into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Uses the provided charset for JSON content. Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final Path jsonOrXml, final Charset charset) {
        if (jsonOrXml == null) {
            return new LinkedTypeMap();
        }
        try (InputStream in = Files.newInputStream(jsonOrXml)) {
            return mapOf(in, charset);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    public static LinkedTypeMap mapOf(final Path jsonOrXml) {
        return mapOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML file into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final Path jsonOrXml, final Charset charset) {
        if (jsonOrXml == null) {
            return new TypeList();
        }
        try (InputStream in = Files.newInputStream(jsonOrXml)) {
            return listOf(in, charset);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses JSON or XML file into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final Path jsonOrXml) {
        return listOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML file into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Uses the provided charset for JSON content. Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final File jsonOrXml, final Charset charset) {
        return jsonOrXml == null ? new LinkedTypeMap() : mapOf(jsonOrXml.toPath(), charset);
    }

    /**
     * Parses JSON or XML file into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final File jsonOrXml) {
        return mapOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML file into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final File jsonOrXml, final Charset charset) {
        return jsonOrXml == null ? new TypeList() : listOf(jsonOrXml.toPath(), charset);
    }

    /**
     * Parses JSON or XML file into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final File jsonOrXml) {
        return listOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML URI into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Uses the provided charset for JSON content. Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final URI jsonOrXml, final Charset charset) {
        if (jsonOrXml == null) {
            return new LinkedTypeMap();
        }
        try (InputStream in = jsonOrXml.toURL().openStream()) {
            return mapOf(in, charset);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    /**
     * Parses JSON or XML URI into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final URI jsonOrXml) {
        return mapOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML URI into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URI jsonOrXml, final Charset charset) {
        if (jsonOrXml == null) {
            return new TypeList();
        }
        try (InputStream in = jsonOrXml.toURL().openStream()) {
            return listOf(in, charset);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses JSON or XML URI into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URI jsonOrXml) {
        return listOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML URL into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Uses the provided charset for JSON content. Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final URL jsonOrXml, final Charset charset) {
        if (jsonOrXml == null) {
            return new LinkedTypeMap();
        }
        try (InputStream in = jsonOrXml.openStream()) {
            return mapOf(in, charset);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    /**
     * Parses JSON or XML URL into a {@link LinkedTypeMap}.
     * JSON objects become map entries; arrays, XML, and primitive values are wrapped under the "" key.
     * Returns an empty map when input is null or cannot be parsed.
     */
    public static LinkedTypeMap mapOf(final URL jsonOrXml) {
        return mapOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML URL into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URL jsonOrXml, final Charset charset) {
        if (jsonOrXml == null) {
            return new TypeList();
        }
        try (InputStream in = jsonOrXml.openStream()) {
            return listOf(in, charset);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses JSON or XML URL into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URL jsonOrXml) {
        return listOf(jsonOrXml, UTF_8);
    }

    public static Object jsonOf(final String json) {
        if (json == null) {
            return null;
        }
        return jsonOf(new ByteArrayInputStream(json.getBytes(UTF_8)), UTF_8);
    }

    /**
     * Auto-detects JSON vs XML from a stream and parses it leniently (trailing commas tolerated; malformed input falls back to raw text).
     *
     * @param json stream containing JSON or XML
     * @return parsed structure or null on empty/invalid input
     */
    public static Object jsonOf(final InputStream json) {
        return jsonOf(json, UTF_8);
    }

    /**
     * Auto-detects JSON vs XML from a stream and parses it leniently with the provided charset for JSON paths.
     */
    public static Object jsonOf(final InputStream json, final Charset charset) {
        if (json == null) {
            return null;
        }
        try {
            return detectAndParse(json, charset);
        } catch (final IOException ignored) {
            return null;
        }
    }

    /**
     * Streams top-level JSON array elements as {@link Type} without loading the whole payload.
     *
     * @param json    input stream containing a JSON array
     * @param charset charset to decode the stream
     * @return lazy stream of Type elements (close the stream to release the input)
     */
    public static Stream<Object> streamArray(final InputStream json, final Charset charset) throws IOException {
        return JsonLenientParser.streamArray(new InputStreamReader(json, charset));
    }

    /**
     * Streams top-level JSON object entries as {@link Pair} key/value without loading the whole payload.
     *
     * @param json    input stream containing a JSON object
     * @param charset charset to decode the stream
     * @return lazy stream of entries (close the stream to release the input)
     */
    @SuppressWarnings("java:S3776")
    public static Stream<Pair<String, Object>> streamObject(final InputStream json, final Charset charset) throws IOException {
        return JsonLenientParser.streamObject(new InputStreamReader(json, charset));
    }

    /**
     * Streams a JSON array as index/value {@link Pair} entries without loading the whole payload.
     *
     * @param json    input stream containing a JSON array
     * @param charset charset to decode the stream
     * @return lazy stream of index/value pairs (close the stream to release the input)
     */
    public static Stream<Pair<Integer, Object>> streamJsonArray(final InputStream json, final Charset charset) {
        try {
            final AtomicInteger index = new AtomicInteger(0);
            return JsonLenientParser.errorTolerantStream(streamArray(json, charset).map(value -> new Pair<>(index.getAndIncrement(), value)));
        } catch (final Exception ignored) {
            JsonLenientParser.closeQuietly(json);
            return Stream.empty();
        }
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final InputStream json) {
        return streamJsonArray(json, UTF_8);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final String json, final Charset charset) {
        if (json == null) {
            return Stream.empty();
        }
        return streamJsonArray(new ByteArrayInputStream(json.getBytes(charset)), charset);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final String json) {
        return streamJsonArray(json, UTF_8);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final CharSequence json, final Charset charset) {
        return json == null ? Stream.empty() : streamJsonArray(json.toString(), charset);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final CharSequence json) {
        return streamJsonArray(json, UTF_8);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final Path path, final Charset charset) {
        if (path == null)
            return Stream.empty();
        return open(() -> Files.newInputStream(path), charset, JsonDecoder::streamJsonArray);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final Path path) {
        return streamJsonArray(path, UTF_8);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final File file, final Charset charset) {
        if (file == null)
            return Stream.empty();
        return streamJsonArray(file.toPath(), charset);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final File file) {
        return streamJsonArray(file, UTF_8);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final URI uri, final Charset charset) {
        if (uri == null)
            return Stream.empty();
        return open(() -> uri.toURL().openStream(), charset, JsonDecoder::streamJsonArray);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final URI uri) {
        return streamJsonArray(uri, UTF_8);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final URL url, final Charset charset) {
        if (url == null)
            return Stream.empty();
        return open(url::openStream, charset, JsonDecoder::streamJsonArray);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final URL url) {
        return streamJsonArray(url, UTF_8);
    }

    /**
     * Streams a JSON object as key/value {@link Pair} entries without loading the whole payload.
     * Close the returned stream to release the input.
     */
    public static Stream<Pair<String, Object>> streamJsonObject(final InputStream json, final Charset charset) {
        try {
            return streamObject(json, charset);
        } catch (final Exception ignored) {
            JsonLenientParser.closeQuietly(json);
            return Stream.empty();
        }
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final InputStream json) {
        return streamJsonObject(json, UTF_8);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final String json, final Charset charset) {
        if (json == null) {
            return Stream.empty();
        }
        return streamJsonObject(new ByteArrayInputStream(json.getBytes(charset)), charset);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final String json) {
        return streamJsonObject(json, UTF_8);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final CharSequence json, final Charset charset) {
        return json == null ? Stream.empty() : streamJsonObject(json.toString(), charset);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final CharSequence json) {
        return streamJsonObject(json, UTF_8);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final Path path, final Charset charset) {
        if (path == null)
            return Stream.empty();
        return open(() -> Files.newInputStream(path), charset, JsonDecoder::streamJsonObject);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final Path path) {
        return streamJsonObject(path, UTF_8);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final File file, final Charset charset) {
        if (file == null)
            return Stream.empty();
        return streamJsonObject(file.toPath(), charset);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final File file) {
        return streamJsonObject(file, UTF_8);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final URI uri, final Charset charset) {
        if (uri == null)
            return Stream.empty();
        return open(() -> uri.toURL().openStream(), charset, JsonDecoder::streamJsonObject);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final URI uri) {
        return streamJsonObject(uri, UTF_8);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final URL url, final Charset charset) {
        if (url == null)
            return Stream.empty();
        return open(url::openStream, charset, JsonDecoder::streamJsonObject);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final URL url) {
        return streamJsonObject(url, UTF_8);
    }

    /**
     * Streams a JSON object or array as {@link Pair} entries without buffering the entire payload.
     * Arrays are surfaced as index-based pairs (index as String, value as element).
     *
     * @param json    input stream containing JSON object/array
     * @param charset charset to decode the stream
     * @return lazy stream of pairs (close the stream to release the input); returns an empty stream on invalid/IO input
     */
    @SuppressWarnings("unchecked")
    public static Stream<Pair<Object, Object>> streamJson(final InputStream json, final Charset charset) {
        if (json == null)
            return Stream.empty();
        try {
            final PushbackInputStream pushback = json instanceof final PushbackInputStream p ? p : new PushbackInputStream(json, 1);
            final int ch = nextNonWhitespace(pushback);
            if (ch == -1) {
                JsonLenientParser.closeQuietly(pushback);
                return Stream.empty();
            }
            pushback.unread(ch);
            try {
                if (ch == '{')
                    return (Stream<Pair<Object, Object>>) (Stream<?>) streamJsonObject(pushback, charset);
                if (ch == '[')
                    return (Stream<Pair<Object, Object>>) (Stream<?>) streamJsonArray(pushback, charset);
                JsonLenientParser.closeQuietly(pushback);
                throw new UncheckedIOException(new IOException("Unsupported top-level token: " + (char) ch));
            } catch (final UncheckedIOException e) {
                JsonLenientParser.closeQuietly(pushback);
                throw e;
            } catch (final Exception e) {
                JsonLenientParser.closeQuietly(pushback);
                throw new UncheckedIOException(new IOException(e));
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Streams a JSON object or array as {@link Pair} entries using UTF-8.
     * Close the returned stream to release the input.
     */
    public static Stream<Pair<Object, Object>> streamJson(final InputStream json) {
        return streamJson(json, UTF_8);
    }

    /**
     * Streams a JSON object or array from a {@link String} with the provided charset.
     */
    public static Stream<Pair<Object, Object>> streamJson(final String json, final Charset charset) {
        if (json == null) {
            return Stream.empty();
        }
        return streamJson(new ByteArrayInputStream(json.getBytes(charset)), charset);
    }

    /**
     * Streams a JSON object or array from a {@link String} using UTF-8.
     */
    public static Stream<Pair<Object, Object>> streamJson(final String json) {
        return streamJson(json, UTF_8);
    }

    /**
     * Streams a JSON object or array from a {@link CharSequence} with the provided charset.
     */
    public static Stream<Pair<Object, Object>> streamJson(final CharSequence json, final Charset charset) {
        return json == null ? Stream.empty() : streamJson(json.toString(), charset);
    }

    /**
     * Streams a JSON object or array from a {@link CharSequence} using UTF-8.
     */
    public static Stream<Pair<Object, Object>> streamJson(final CharSequence json) {
        return streamJson(json, UTF_8);
    }

    /**
     * Streams a JSON object or array from a {@link Path} with the provided charset.
     */
    public static Stream<Pair<Object, Object>> streamJson(final Path path, final Charset charset) {
        if (path == null) {
            return Stream.empty();
        }
        return open(() -> Files.newInputStream(path), charset, JsonDecoder::streamJson);
    }

    /**
     * Streams a JSON object or array from a {@link Path} using UTF-8.
     */
    public static Stream<Pair<Object, Object>> streamJson(final Path path) {
        return streamJson(path, UTF_8);
    }

    /**
     * Streams a JSON object or array from a {@link File} with the provided charset.
     */
    public static Stream<Pair<Object, Object>> streamJson(final File file, final Charset charset) {
        if (file == null) {
            return Stream.empty();
        }
        return streamJson(file.toPath(), charset);
    }

    /**
     * Streams a JSON object or array from a {@link File} using UTF-8.
     */
    public static Stream<Pair<Object, Object>> streamJson(final File file) {
        return streamJson(file, UTF_8);
    }

    /**
     * Streams a JSON object or array from a {@link URI} with the provided charset.
     */
    public static Stream<Pair<Object, Object>> streamJson(final URI uri, final Charset charset) {
        if (uri == null) {
            return Stream.empty();
        }
        return open(() -> uri.toURL().openStream(), charset, JsonDecoder::streamJson);
    }

    /**
     * Streams a JSON object or array from a {@link URI} using UTF-8.
     */
    public static Stream<Pair<Object, Object>> streamJson(final URI uri) {
        return streamJson(uri, UTF_8);
    }

    /**
     * Streams a JSON object or array from a {@link URL} with the provided charset.
     */
    public static Stream<Pair<Object, Object>> streamJson(final URL url, final Charset charset) {
        if (url == null)
            return Stream.empty();
        return open(url::openStream, charset, JsonDecoder::streamJson);
    }

    /**
     * Streams a JSON object or array from a {@link URL} using UTF-8.
     * Close the returned stream to release the input.
     */
    public static Stream<Pair<Object, Object>> streamJson(final URL url) {
        return streamJson(url, UTF_8);
    }

    public interface InputSupplier {
        InputStream open() throws IOException;
    }

    public interface StreamFactory<T> {
        Stream<T> open(InputStream input, Charset charset) throws IOException;
    }

    public static <T> Stream<T> open(final InputSupplier supplier, final Charset charset, final StreamFactory<T> factory) {
        if (supplier == null) {
            return Stream.empty();
        }
        try {
            return open(supplier.open(), charset, factory);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> Stream<T> open(final InputStream input, final Charset charset, final StreamFactory<T> factory) {
        if (input == null) {
            return Stream.empty();
        }
        try {
            return factory.open(input, charset);
        } catch (final IOException e) {
            JsonLenientParser.closeQuietly(input);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Detects whether the input stream is XML (leading '<') or JSON and delegates to the appropriate parser.
     * Uses streaming to avoid buffering whole payloads; wraps incoming streams in a {@link PushbackInputStream}.
     *
     * @param input   stream to parse
     * @param charset charset to use when decoding JSON content
     * @return parsed structure or null on failure/empty input
     * @throws IOException when the stream cannot be read
     */
    public static Object detectAndParse(final InputStream input, final Charset charset) throws IOException {
        if (input == null) {
            return null;
        }
        try (final PushbackInputStream pushback = new PushbackInputStream(input, 1)) {
            final int ch = nextNonWhitespace(pushback);
            if (ch == -1) {
                return null;
            }
            pushback.unread(ch);
            if (ch == '<') {
                return XmlDecoder.xmlTypeOf(pushback);
            }
            try (final Reader reader = buffered(new InputStreamReader(pushback, charset))) {
                return JsonLenientParser.parse(reader);
            }
        }
    }

    private static Reader buffered(final Reader reader) {
        return reader instanceof BufferedReader ? reader : new BufferedReader(reader, 8192);
    }

    private static int nextNonWhitespace(final InputStream input) throws IOException {
        int ch;
        do {
            ch = input.read();
        } while (ch != -1 && Character.isWhitespace(ch));
        return ch;
    }

    private JsonDecoder() {
        // static util class
    }
}
