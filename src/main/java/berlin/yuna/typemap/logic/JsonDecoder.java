package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.TypeInfo;
import berlin.yuna.typemap.model.TypeList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import berlin.yuna.typemap.model.Type;

import static berlin.yuna.typemap.logic.JsonEncoder.unescapeJson;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

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
     * @param json input stream containing a JSON array
     * @param charset charset to decode the stream
     * @return lazy stream of Type elements (caller must close)
     * @throws IOException when the stream cannot be read or the payload is not an array
     */
    public static Stream<Type<?>> streamArray(final InputStream json, final Charset charset) throws IOException {
        final LenientStream stream = new LenientStream(new InputStreamReader(json, charset), 8 * 1024);
        final int start = stream.nextNonWhitespace();
        if (start != '[') {
            throw new IllegalStateException("Expected array start '['");
        }

        final Iterator<Type<?>> iterator = new Iterator<Type<?>>() {
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
            public Type<?> next() {
                try {
                    final Object value = parseValue(stream);
                    final int sep = stream.nextNonWhitespace();
                    if (sep == ']') {
                        endReached = true;
                    } else if (sep != ',') {
                        throw new IllegalStateException("Invalid array separator");
                    }
                    return Type.typeOf(value);
                } catch (final IOException e) {
                    endReached = true;
                    throw new IllegalStateException(e);
                }
            }
        };

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL), false)
            .onClose(() -> closeQuietly(stream.reader));
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
        final BufferedInputStream buffered = input instanceof final BufferedInputStream bufferedInputStream ? bufferedInputStream : new BufferedInputStream(input);
        buffered.mark(1024);
        int ch;
        do {
            ch = buffered.read();
        } while (ch != -1 && Character.isWhitespace(ch));
        buffered.reset();
        if (ch == '<') {
            return XmlDecoder.xmlTypeOf(buffered);
        }
        try (final Reader reader = buffered(new InputStreamReader(buffered, charset))) {
            return parse(reader);
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
