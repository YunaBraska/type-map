package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.ArgsDecoder;
import berlin.yuna.typemap.logic.JsonDecoder;
import berlin.yuna.typemap.logic.TypeConverter;
import berlin.yuna.typemap.logic.XmlDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static berlin.yuna.typemap.logic.TypeConverter.iterateOverArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * {@link TypeMap} is a specialized implementation of {@link HashMap} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link TypeMap}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class TypeMap extends HashMap<Object, Object> implements TypeMapI<TypeMap> {

    /**
     * Default constructor for creating an empty {@link TypeMap}.
     */
    public TypeMap() {
        this((Map<?, ?>) null);
    }

    /**
     * Constructs a new {@link TypeMap} of the specified json.
     *
     * @deprecated use {@link #fromJson(String)} or {@link #fromXml(String)} for clarity
     */
    @Deprecated(forRemoval = true)
    public TypeMap(final String json) {
        this(JsonDecoder.jsonMapOf(json));
    }

    /**
     * Constructs a new {@link TypeMap} of the specified command line arguments.
     *
     * @deprecated use {@link #fromArgs(String[])} instead
     */
    @Deprecated(forRemoval = true)
    public TypeMap(final String[] cliArgs) {
        this(ArgsDecoder.argsOf(String.join(" ", cliArgs)));
    }

    /**
     * Constructs a new {@link TypeMap} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public TypeMap(final Map<?, ?> map) {
        ofNullable(map).ifPresent(super::putAll);
    }

    /**
     * Parses JSON into a {@link TypeMapI} (returns {@link LinkedTypeMap} when possible).
     *
     * @param json raw json content
     * @return populated map or empty map when input is blank
     */
    public static LinkedTypeMap fromJson(final String json) {
        return (json == null || json.isBlank())
            ? new LinkedTypeMap()
            : JsonDecoder.jsonMapOf(json);
    }

    /**
     * Parses JSON CharSequence into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromJson(final CharSequence json) {
        return json == null
            ? new LinkedTypeMap()
            : fromJson(json.toString());
    }

    /**
     * Parses JSON file into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromJson(final Path json) {
        try (final InputStream in = Files.newInputStream(json)) {
            return fromJson(in);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    /**
     * Parses JSON stream into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromJson(final InputStream json) {
        return fromJson(readStream(json));
    }

    /**
     * Parses JSON or XML stream into a {@link TypeMapI}.
     */
    public static LinkedTypeMap from(final InputStream input) {
        return from(input, UTF_8);
    }

    /**
     * Parses JSON or XML stream into a {@link TypeMapI} with the given charset for JSON content.
     */
    public static LinkedTypeMap from(final InputStream input, final Charset charset) {
        return input == null ? new LinkedTypeMap() : JsonDecoder.jsonMapOf(input, charset);
    }

    /**
     * Parses XML into a {@link LinkedTypeMap} preserving element order.
     */
    public static LinkedTypeMap fromXml(final String xml) {
        return fromXml(XmlDecoder.xmlTypeOf(xml));
    }

    /**
     * Parses XML CharSequence into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromXml(final CharSequence xml) {
        return xml == null
            ? new LinkedTypeMap()
            : fromXml(xml.toString());
    }

    /**
     * Parses XML file into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromXml(final Path xml) {
        try (final InputStream in = Files.newInputStream(xml)) {
            return fromXml(in);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    /**
     * Parses XML stream into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromXml(final InputStream xml) {
        return fromXml(readStream(xml));
    }

    /**
     * Parses CLI args into a {@link LinkedTypeMap}.
     */
    public static LinkedTypeMap fromArgs(final String args) {
        return (args == null || args.isBlank())
            ? new LinkedTypeMap()
            : ArgsDecoder.argsOf(args);
    }

    /**
     * Parses CLI args array into a {@link LinkedTypeMap}.
     */
    public static LinkedTypeMap fromArgs(final String[] args) {
        return (args == null || args.length == 0)
            ? new LinkedTypeMap()
            : ArgsDecoder.argsOf(String.join(" ", args));
    }

    /**
     * Parses CLI args CharSequence into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromArgs(final CharSequence args) {
        return args == null
            ? new LinkedTypeMap()
            : fromArgs(args.toString());
    }

    /**
     * Parses CLI args file into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromArgs(final Path args) {
        try (final InputStream in = Files.newInputStream(args)) {
            return fromArgs(in);
        } catch (final IOException ignored) {
            return new LinkedTypeMap();
        }
    }

    /**
     * Parses CLI args stream into a {@link TypeMapI}.
     */
    public static LinkedTypeMap fromArgs(final InputStream args) {
        return fromArgs(readStream(args));
    }

    private static LinkedTypeMap fromXml(final TypeList xml) {
        if (xml == null || xml.isEmpty())
            return new LinkedTypeMap();
        final Object first = xml.get(0);
        if (first instanceof final Pair<?, ?> pair)
            return new LinkedTypeMap().putR(pair.getKey(), pair.getValue());
        return new LinkedTypeMap().putR("root", xml);
    }

    private static String readStream(final InputStream stream) {
        if (stream == null) {
            return "";
        }
        try (InputStream in = stream) {
            return new String(in.readAllBytes(), UTF_8);
        } catch (final IOException ignored) {
            return "";
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated {@link ConcurrentTypeMap} instance for chaining.
     */
    public TypeMap addR(final Object key, final Object value) {
        return putR(key, value);
    }

    /**
     * Returns a {@link TypeMap} containing mappings.
     *
     * @param input key and value pairs
     * @return a new {@link TypeMap} containing the specified mappings.
     */
    public static TypeMap mapOf(final Object... input) {
        if (input == null)
            return new TypeMap();
        if ((input.length & 1) != 0)
            throw new InternalError("length is odd");

        final TypeMap result = new TypeMap();
        for (int i = 0; i < input.length; i += 2) {
            result.put(input[i], input[i + 1]);
        }
        return result;
    }

    @SuppressWarnings("java:S3776")
    public static Object treeGet(final Object mapOrCollection, final Object... path) {
        if (path == null || path.length == 0) {
            if (mapOrCollection instanceof final Type<?> type)
                return type.value();
            return mapOrCollection instanceof final Optional<?> optional ? optional.orElse(null) : mapOrCollection;
        }

        Object value = mapOrCollection;
        for (final Object key : path) {
            if (key == null || value == null) {
                return null;
            } else if (value instanceof final Map<?, ?> map) {
                value = map.get(key);
            } else if (value instanceof final Collection<?> collection) {
                if (key instanceof final Number numberKey && value instanceof final List<?> list) {
                    final int index = numberKey.intValue();
                    value = (index >= 0 && index < list.size()) ? list.get(index) : null;
                } else {
                    value = collection.stream().filter(item -> Objects.equals(item, key)
                        || (item instanceof final Map.Entry<?, ?> entry && Objects.equals(entry.getKey(), key))
                    ).map(o -> o instanceof final Map.Entry<?, ?> entry ? entry.getValue() : o).findFirst().orElse(null);
                }
            } else if (value.getClass().isArray()) {
                final int index = key instanceof final Number num ? num.intValue() : -1;
                final AtomicInteger itemCount = new AtomicInteger(0);
                final AtomicReference<Object> result = new AtomicReference<>(null);
                iterateOverArray(value, item -> {
                    if (result.get() == null && (index > -1 ? index == itemCount.getAndIncrement() : Objects.equals(item, key)))
                        result.set(item);
                });
                return result.get();
            } else if (value instanceof final Type<?> type) {
                value = type.value();
            } else if (value instanceof final Optional<?> optional) {
                value = optional.orElse(null);
            } else if (value instanceof final Map.Entry<?, ?> pair) {
                value = (Objects.equals(pair.getKey(), key)) ? pair.getValue() : null;
            } else {
                value = null;
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<K, V>> M convertAndMap(final Object input, final Supplier<M> output, final Class<K> keyType, final Class<V> valueType) {
        if (output != null && keyType != null && valueType != null && input instanceof Map<?, ?>) {
            return TypeConverter.mapOf((Map<?, ?>) input, output, keyType, valueType);
        }
        return ofNullable(output).map(Supplier::get).orElse((M) emptyMap());
    }

    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<K, V>> M convertAndMap(final Object input, final Supplier<M> output, final Function<Object, K> keyMapper, final Function<Object, V> valueMapper) {
        if (output != null && keyMapper != null && valueMapper != null && input instanceof Map<?, ?>) {
            final M result = output.get();
            if (result == null)
                return (M) emptyMap();
            ((Map<?, ?>) input).forEach((key, value) -> {
                final K newKey = keyMapper.apply(key);
                final V newValue = valueMapper.apply(value);
                if (key != null && value != null)
                    result.put(newKey, newValue);
            });
            return result;
        }
        return ofNullable(output).map(Supplier::get).orElse((M) emptyMap());
    }
}
