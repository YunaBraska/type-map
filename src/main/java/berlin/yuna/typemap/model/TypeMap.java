package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.ArgsDecoder;
import berlin.yuna.typemap.logic.JsonDecoder;
import berlin.yuna.typemap.logic.TypeConverter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static berlin.yuna.typemap.logic.TypeConverter.iterateOverArray;
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
     */
    public TypeMap(final String json) {
        this(JsonDecoder.jsonMapOf(json));
    }

    /**
     * Constructs a new {@link TypeMap} of the specified command line arguments.
     */
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
            if (mapOrCollection instanceof Type)
                return ((Type<?>) mapOrCollection).value();
            return mapOrCollection instanceof Optional ? ((Optional<?>) mapOrCollection).orElse(null) : mapOrCollection;
        }

        Object value = mapOrCollection;
        for (final Object key : path) {
            if (key == null || value == null) {
                return null;
            } else if (value instanceof Map<?, ?>) {
                value = ((Map<?, ?>) value).get(key);
            } else if (value instanceof Collection<?>) {
                if (key instanceof Number) {
                    final int index = ((Number) key).intValue();
                    final List<?> list = (List<?>) value;
                    value = (index >= 0 && index < list.size()) ? list.get(index) : null;
                } else {
                    value = ((Collection<?>) value).stream().filter(item -> Objects.equals(item, key)
                        || (item instanceof Map.Entry && Objects.equals(((Map.Entry<?, ?>) item).getKey(), key))
                    ).map(o -> o instanceof Map.Entry ? ((Map.Entry<?, ?>) o).getValue() : o).findFirst().orElse(null);
                }
            } else if (value.getClass().isArray()) {
                final int index = key instanceof Number ? ((Number) key).intValue() : -1;
                final AtomicInteger itemCount = new AtomicInteger(0);
                final AtomicReference<Object> result = new AtomicReference<>(null);
                iterateOverArray(value, item -> {
                    if (result.get() == null && (index > -1 ? index == itemCount.getAndIncrement() : Objects.equals(item, key)))
                        result.set(item);
                });
                return result.get();
            } else if (value instanceof Type) {
                value = ((Type<?>) value).value();
            } else if (value instanceof Optional<?>) {
                value = ((Optional<?>) value).orElse(null);
            } else if (value instanceof Map.Entry) {
                final Map.Entry<?, ?> pair = (Map.Entry<?, ?>) value;
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
