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
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.PushbackInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static berlin.yuna.typemap.logic.JsonEncoder.unescapeJson;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.Spliterators.spliteratorUnknownSize;

public class JsonDecoder {

    /**
     * Parses JSON or XML content from a string and returns a {@link TypeInfo} (map/list) structure.
     * Uses the streaming path under the hood to keep memory usage predictable.
     */
    @SuppressWarnings("java:S1452")
    public static TypeInfo<?> jsonTypeOf(final String jsonOrXml) {
        return jsonOrXml == null
                ? new TypeList()
                : jsonTypeOf(new ByteArrayInputStream(jsonOrXml.getBytes(UTF_8)));
    }

    /**
     * Parses JSON or XML content from a stream and returns a {@link TypeInfo} (map/list) structure.
     */
    @SuppressWarnings("java:S1452")
    public static TypeInfo<?> jsonTypeOf(final InputStream jsonOrXml) {
        return jsonTypeOf(jsonOrXml, UTF_8);
    }

    /**
     * Parses JSON or XML content from a stream with the given charset and returns a {@link TypeInfo}.
     */
    @SuppressWarnings("java:S1452")
    public static TypeInfo<?> jsonTypeOf(final InputStream jsonOrXml, final Charset charset) {
        try {
            final Object result = detectAndParse(jsonOrXml, charset);
            return result instanceof TypeInfo ? (TypeInfo<?>) result : new TypeList().addR(result);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses JSON into a {@link LinkedTypeMap}, auto-detecting XML if provided.
     */
    public static LinkedTypeMap jsonMapOf(final String json) {
        if (json == null)
            return new LinkedTypeMap();
        try (final ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(UTF_8))) {
            return jsonMapOf(in, UTF_8);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    /**
     * Parses JSON into a {@link LinkedTypeMap}, auto-detecting XML if provided.
     */
    public static LinkedTypeMap jsonMapOf(final InputStream json) {
        return jsonMapOf(json, UTF_8);
    }

    /**
     * Parses JSON into a {@link LinkedTypeMap} using the provided charset, auto-detecting XML if provided.
     */
    public static LinkedTypeMap jsonMapOf(final InputStream json, final Charset charset) {
        try {
            final Object result = detectAndParse(json, charset);
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
     * Parses JSON into a {@link TypeList}, auto-detecting XML if provided.
     */
    public static TypeList jsonListOf(final String json) {
        if (json == null) {
            return new TypeList();
        }
        try (final ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(UTF_8))) {
            return jsonListOf(in, UTF_8);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses JSON into a {@link TypeList}, auto-detecting XML if provided.
     */
    public static TypeList jsonListOf(final InputStream json) {
        return jsonListOf(json, UTF_8);
    }

    /**
     * Parses JSON into a {@link TypeList} using the provided charset, auto-detecting XML if provided.
     */
    public static TypeList jsonListOf(final InputStream json, final Charset charset) {
        try {
            final Object result = detectAndParse(json, charset);
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
     * @return lazy stream of Type elements (caller must close)
     */
    public static Stream<Object> streamArray(final InputStream json, final Charset charset) throws IOException {
        final LenientStream stream = new LenientStream(new InputStreamReader(json, charset), 8 * 1024);
        final int start = stream.nextNonWhitespace();
        if (start != '[') {
            throw new IllegalStateException("Expected array start '['");
        }

        final Iterator<Object> iterator = new Iterator<>() {
            private boolean endReached;

            @Override
            public boolean hasNext() {
                if (endReached) {
                    return false;
                }
                try {
                    final int next = stream.nextNonWhitespace();
                    if (next == ']') {
                        endReached = true;
                        return false;
                    }
                    stream.unread(next);
                    return true;
                } catch (final IOException e) {
                    endReached = true;
                    return false;
                }
            }

            @Override
            public Object next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                try {
                    final Object value = parseValue(stream);
                    final int sep = stream.nextNonWhitespace();
                    if (sep == ']') {
                        endReached = true;
                    } else if (sep != ',') {
                        throw new IllegalStateException("Invalid array separator");
                    }
                    return value;
                } catch (final IOException e) {
                    endReached = true;
                    throw new IllegalStateException(e);
                }
            }
        };

        return StreamSupport.stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .onClose(() -> closeQuietly(stream.reader));
    }

    /**
     * Streams top-level JSON object entries as {@link Pair} key/value without loading the whole payload.
     *
     * @param json    input stream containing a JSON object
     * @param charset charset to decode the stream
     * @return lazy stream of entries (caller must close)
     */
    @SuppressWarnings("java:S3776")
    public static Stream<Pair<String, Object>> streamObject(final InputStream json, final Charset charset) throws IOException {
        final LenientStream stream = new LenientStream(new InputStreamReader(json, charset), 8 * 1024);
        final int start = stream.nextNonWhitespace();
        if (start != '{')
            throw new IllegalStateException("Expected object start '{'");
        final Iterator<Pair<String, Object>> iterator = new Iterator<>() {
            private boolean endReached;

            @Override
            public boolean hasNext() {
                if (endReached) {
                    return false;
                }
                try {
                    final int next = stream.nextNonWhitespace();
                    if (next == '}') {
                        endReached = true;
                        return false;
                    }
                    stream.unread(next);
                    return true;
                } catch (final IOException e) {
                    endReached = true;
                    return false;
                }
            }

            @Override
            public Pair<String, Object> next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                try {
                    final int first = stream.nextNonWhitespace();
                    if (first == '}') {
                        endReached = true;
                        return null;
                    }
                    final String key = parseKey(stream, first);
                    final int sep = stream.nextNonWhitespace();
                    if (sep != ':') {
                        throw new IllegalStateException("Invalid JSON object separator");
                    }
                    final Object value = parseValue(stream);
                    final int endOrComma = stream.nextNonWhitespace();
                    if (endOrComma == '}') {
                        endReached = true;
                    } else if (endOrComma != ',') {
                        throw new IllegalStateException("Invalid JSON object separator");
                    }
                    return new Pair<>(key, value);
                } catch (final IOException e) {
                    endReached = true;
                    throw new IllegalStateException(e);
                }
            }
        };

        return errorTolerantStream(iterator, () -> closeQuietly(stream.reader));
    }

    /**
     * Streams a JSON array as index/value {@link Pair} entries without loading the whole payload.
     *
     * @param json    input stream containing a JSON array
     * @param charset charset to decode the stream
     * @return lazy stream of index/value pairs (caller must close)
     */
    public static Stream<Pair<Integer, Object>> streamJsonArray(final InputStream json, final Charset charset) {
        try {
            final AtomicInteger index = new AtomicInteger(0);
            return errorTolerantStream(streamArray(json, charset).map(value -> new Pair<>(index.getAndIncrement(), value)));
        } catch (final Exception ignored) {
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
        try {
            return streamJsonArray(Files.newInputStream(path), charset);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try {
            return streamJsonArray(uri.toURL(), charset);
        } catch (final MalformedURLException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final URI uri) {
        return streamJsonArray(uri, UTF_8);
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final URL url, final Charset charset) {
        if (url == null)
            return Stream.empty();
        try {
            return streamJsonArray(url.openStream(), charset);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream<Pair<Integer, Object>> streamJsonArray(final URL url) {
        return streamJsonArray(url, UTF_8);
    }

    /**
     * Streams a JSON object as key/value {@link Pair} entries without loading the whole payload.
     */
    public static Stream<Pair<String, Object>> streamJsonObject(final InputStream json, final Charset charset) {
        try {
            return errorTolerantStream(streamObject(json, charset));
        } catch (final Exception ignored) {
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
        try {
            return streamJsonObject(Files.newInputStream(path), charset);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try {
            return streamJsonObject(uri.toURL(), charset);
        } catch (final MalformedURLException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final URI uri) {
        return streamJsonObject(uri, UTF_8);
    }

    public static Stream<Pair<String, Object>> streamJsonObject(final URL url, final Charset charset) {
        if (url == null)
            return Stream.empty();
        try {
            return streamJsonObject(url.openStream(), charset);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
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
     * @return lazy stream of pairs (caller must close); returns an empty stream on invalid/IO input
     */
    @SuppressWarnings("unchecked")
    public static Stream<Pair<Object, Object>> streamJson(final InputStream json, final Charset charset) {
        if (json == null)
            return Stream.empty();
        try {
            final PushbackInputStream pushback = json instanceof final PushbackInputStream p ? p : new PushbackInputStream(json, 1);
            final int ch = nextNonWhitespace(pushback);
            if (ch == -1) {
                closeQuietly(pushback);
                return Stream.empty();
            }
            pushback.unread(ch);
            try {
                if (ch == '{')
                    return (Stream<Pair<Object, Object>>) (Stream<?>) streamJsonObject(pushback, charset);
                if (ch == '[')
                    return (Stream<Pair<Object, Object>>) (Stream<?>) streamJsonArray(pushback, charset);
                closeQuietly(pushback);
                throw new UncheckedIOException(new IOException("Unsupported top-level token: " + (char) ch));
            } catch (final UncheckedIOException e) {
                closeQuietly(pushback);
                throw e;
            } catch (final Exception e) {
                closeQuietly(pushback);
                throw new UncheckedIOException(new IOException(e));
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Streams a JSON object or array as {@link Pair} entries using UTF-8.
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
        try {
            return streamJson(Files.newInputStream(path), charset);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try {
            return streamJson(uri.toURL(), charset);
        } catch (final MalformedURLException e) {
            throw new UncheckedIOException(new IOException(e));
        }
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
        try {
            return streamJson(url.openStream(), charset);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Streams a JSON object or array from a {@link URL} using UTF-8.
     */
    public static Stream<Pair<Object, Object>> streamJson(final URL url) {
        return streamJson(url, UTF_8);
    }

    /**
     * Detects whether the input stream is XML (leading '<') or JSON and delegates to the appropriate parser.
     * Uses streaming to avoid buffering whole payloads; wraps incoming streams in a {@link BufferedInputStream} if needed.
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
                return parse(reader);
            }
        }
    }

    private static void closeQuietly(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception ignored) {
                // ignore
            }
        }
    }

    private static <T> Stream<T> errorTolerantStream(final Iterator<T> iterator, final Runnable onClose) {
        final Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(final Consumer<? super T> action) {
                try {
                    if (iterator.hasNext()) {
                        action.accept(iterator.next());
                        return true;
                    }
                    return false;
                } catch (final Exception e) {
                    throw new UncheckedIOException(new IOException(e));
                }
            }
        };
        return StreamSupport.stream(spliterator, false).onClose(onClose);
    }

    private static <T> Stream<T> errorTolerantStream(final Stream<T> input) {
        try {
            return errorTolerantStream(input.iterator(), input::close);
        } catch (final Exception ignored) {
            closeQuietly(input);
            return Stream.empty();
        }
    }

    private static Object parse(final Reader reader) throws IOException {
        final LenientStream stream = new LenientStream(reader, 8 * 1024);
        try {
            final Object value = parseValue(stream);
            if (value instanceof final LinkedTypeMap singleMap && singleMap.size() == 1 && singleMap.containsKey(""))
                return singleMap.get("");
            if (value instanceof final LinkedTypeMap map && map.isEmpty() && "{}".contentEquals(stream.rawText().strip()))
                return null;
            return value;
        } catch (final RuntimeException ignored) {
            final String raw = stream.rawText();
            return raw.isEmpty() ? null : raw;
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

    private static Object parseValue(final LenientStream stream) throws IOException {
        final int ch = stream.nextNonWhitespace();
        if (ch == -1) {
            return null;
        }
        return switch (ch) {
            case '{' -> parseObject(stream);
            case '[' -> parseArray(stream);
            case '"' -> parseString(stream);
            default -> parsePrimitive(stream, (char) ch);
        };
    }

    @SuppressWarnings("java:S3776")
    private static LinkedTypeMap parseObject(final LenientStream stream) throws IOException {
        final LinkedTypeMap map = new LinkedTypeMap();
        while (true) {
            final int first = stream.nextNonWhitespace();
            if (first == '}') {
                return map;
            } else if (first == -1) {
                throw new IllegalStateException("Unterminated object");
            }
            final String key = parseKey(stream, first);
            final int separator = stream.nextNonWhitespace();
            if (separator == ':') {
                final Object value = parseValue(stream);
                map.put(key, value);
                final int sep = stream.nextNonWhitespace();
                if (sep == '}') {
                    return map;
                } else if (sep != ',') {
                    throw new IllegalStateException("Invalid JSON object separator");
                }
            } else if (separator == '}' || separator == ',') {
                map.put("", key);
                if (separator == '}') {
                    return map;
                }
            } else {
                throw new IllegalStateException("Invalid JSON object separator");
            }
        }
    }

    private static String parseKey(final LenientStream stream, final int first) throws IOException {
        if (first == '"') {
            return parseString(stream);
        }
        final StringBuilder token = new StringBuilder();
        token.append((char) first);
        while (true) {
            final int ch = stream.read();
            if (ch == -1) {
                return token.toString();
            }
            if (ch == ':') {
                stream.unread(ch);
                return token.toString();
            }
            if (!Character.isWhitespace(ch)) {
                token.append((char) ch);
            }
        }
    }

    private static TypeList parseArray(final LenientStream stream) throws IOException {
        final TypeList list = new TypeList();
        while (true) {
            int ch = stream.nextNonWhitespace();
            if (ch == ']') {
                return list;
            }
            if (ch == -1) {
                throw new IllegalStateException("Unterminated array");
            }
            stream.unread(ch);
            list.add(parseValue(stream));
            ch = stream.nextNonWhitespace();
            if (ch == ']') {
                return list;
            } else if (ch != ',') {
                throw new IllegalStateException("Invalid JSON array separator");
            }
        }
    }

    private static String parseString(final LenientStream stream) throws IOException {
        final StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        while (true) {
            final int ch = stream.read();
            if (ch == -1) {
                throw new IllegalStateException("Unterminated string");
            }
            if (escaped) {
                sb.append(decodeEscape(ch, stream));
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                break;
            } else {
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }

    private static char decodeEscape(final int esc, final LenientStream stream) throws IOException {
        return switch (esc) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> {
                final char[] hex = new char[4];
                for (int i = 0; i < 4; i++) {
                    final int h = stream.read();
                    if (h == -1) {
                        throw new IllegalStateException("Incomplete unicode escape");
                    }
                    hex[i] = (char) h;
                }
                yield (char) Integer.parseInt(new String(hex), 16);
            }
            default -> (char) esc;
        };
    }

    private static Object parsePrimitive(final LenientStream stream, final char first) throws IOException {
        final StringBuilder token = new StringBuilder();
        token.append(first);
        while (true) {
            final int ch = stream.read();
            if (ch == -1 || ch == ',' || ch == '}' || ch == ']' || Character.isWhitespace(ch)) {
                if (ch != -1) {
                    stream.unread(ch);
                }
                break;
            }
            token.append((char) ch);
        }
        final String value = token.toString();
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            case "null" -> null;
            default -> convertToPrimitive(value);
        };
    }

    private static Object convertToPrimitive(final String value) {
        if ("null".equals(value)) {
            return null;
        } else if ("true".equals(value) || "false".equals(value)) {
            return Boolean.parseBoolean(value);
        } else {
            final Object result;
            if (value.contains(".") || value.contains("e") || value.contains("E")) {
                result = convertObj(value, Double.class);
            } else {
                result = convertObj(value, Long.class);
            }
            return result == null ? unescapeJson(value) : result;
        }
    }

    private static class LenientStream {
        private static final int PUSHBACK_SIZE = 16;
        private final PushbackReader reader;
        private final StringBuilder raw;
        private final int rawLimit;

        LenientStream(final Reader reader, final int rawLimit) {
            this.reader = new PushbackReader(reader, PUSHBACK_SIZE);
            this.rawLimit = rawLimit;
            this.raw = new StringBuilder(Math.min(rawLimit, 1024));
        }

        int read() throws IOException {
            final int ch = reader.read();
            appendRaw(ch);
            return ch;
        }

        void unread(final int ch) throws IOException {
            if (ch != -1) {
                reader.unread(ch);
            }
        }

        int nextNonWhitespace() throws IOException {
            int ch;
            do {
                ch = read();
            } while (ch != -1 && Character.isWhitespace(ch));
            return ch;
        }

        String rawText() {
            return raw.toString();
        }

        private void appendRaw(final int ch) {
            if (ch != -1 && raw.length() < rawLimit) {
                raw.append((char) ch);
            }
        }
    }

    private JsonDecoder() {
        // static util class
    }
}
