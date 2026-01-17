package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static berlin.yuna.typemap.logic.JsonEncoder.unescapeJson;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static java.util.Spliterators.spliteratorUnknownSize;

public class JsonLenientParser {

    public static Object parse(final Reader reader) throws IOException {
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

    public static Stream<Object> streamArray(final Reader reader) throws IOException {
        final LenientStream stream = new LenientStream(reader, 8 * 1024);
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

    @SuppressWarnings("java:S3776")
    public static Stream<Pair<String, Object>> streamObject(final Reader reader) throws IOException {
        final LenientStream stream = new LenientStream(reader, 8 * 1024);
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

    public static <T> Stream<T> errorTolerantStream(final Iterator<T> iterator, final Runnable onClose) {
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

    public static <T> Stream<T> errorTolerantStream(final Stream<T> input) {
        try {
            return errorTolerantStream(input.iterator(), input::close);
        } catch (final Exception ignored) {
            closeQuietly(input);
            return Stream.empty();
        }
    }

    public static Object parseValue(final LenientStream stream) throws IOException {
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
    public static LinkedTypeMap parseObject(final LenientStream stream) throws IOException {
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

    public static String parseKey(final LenientStream stream, final int first) throws IOException {
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

    public static TypeList parseArray(final LenientStream stream) throws IOException {
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

    public static String parseString(final LenientStream stream) throws IOException {
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

    public static char decodeEscape(final int esc, final LenientStream stream) throws IOException {
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

    public static Object parsePrimitive(final LenientStream stream, final char first) throws IOException {
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

    public static Object convertToPrimitive(final String value) {
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

    public static void closeQuietly(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception ignored) {
                // ignore
            }
        }
    }

    public static class LenientStream {
        private static final int PUSHBACK_SIZE = 16;
        private final PushbackReader reader;
        private final StringBuilder raw;
        private final int rawLimit;

        public LenientStream(final Reader reader, final int rawLimit) {
            this.reader = new PushbackReader(reader, PUSHBACK_SIZE);
            this.rawLimit = rawLimit;
            this.raw = new StringBuilder(Math.min(rawLimit, 1024));
        }

        public int read() throws IOException {
            final int ch = reader.read();
            appendRaw(ch);
            return ch;
        }

        public void unread(final int ch) throws IOException {
            if (ch != -1) {
                reader.unread(ch);
            }
        }

        public int nextNonWhitespace() throws IOException {
            int ch;
            do {
                ch = read();
            } while (ch != -1 && Character.isWhitespace(ch));
            return ch;
        }

        public String rawText() {
            return raw.toString();
        }

        private void appendRaw(final int ch) {
            if (ch != -1 && raw.length() < rawLimit) {
                raw.append((char) ch);
            }
        }
    }
}
