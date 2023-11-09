package berlin.yuna.typemap.model;


import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static berlin.yuna.typemap.logic.TypeConverter.*;
import static java.util.Optional.ofNullable;

@SuppressWarnings("unused")
public class TypeMap extends ConcurrentHashMap<Object, Object> {

    public TypeMap() {
        this(null);
    }

    public TypeMap(final TypeMap typeMap) {
        ofNullable(typeMap).ifPresent(super::putAll);
    }

    @Override
    public TypeMap put(final Object key, final Object value) {
        super.put(key, value);
        return this;
    }

    public <T> Optional<T> get(final Object key, final Class<T> type) {
        return ofNullable(super.get(key)).map(object -> convertObj(object, type));
    }

    public <T extends Collection<E>, E> T get(final Object key, final Supplier<? extends T> output, final Class<E> itemType) {
        return collectionOf(super.get(key), output, itemType);
    }

    @SuppressWarnings("unchecked")
    public <E> E[] getArray(final Object key, final Class<E> componentType) {
        if (componentType == null) return null;
        final Object input = super.get(key);
        Stream<E> stream = null;
        if (input instanceof Collection<?>) {
            final Collection<?> collection = (Collection<?>) input;
            stream = collection.stream().map(item -> convertObj(item, componentType)).filter(Objects::nonNull);
        } else if (input != null && input.getClass().isArray()) {
            stream = IntStream.range(0, Array.getLength(input)).mapToObj(i -> convertObj(Array.get(input, i), componentType)).filter(Objects::nonNull);
        } else if (input != null) {
            stream = ofNullable(convertObj(input, componentType)).map(Stream::of).orElseGet(Stream::empty);
        }

        if (stream != null) {
            return stream.toArray(size -> (E[]) Array.newInstance(componentType, size));
        }

        return (E[]) Array.newInstance(componentType, 0);
    }

    public <K, V, M extends Map<K, V>> M get(final Object key, final Supplier<M> output, final Class<K> keyType, final Class<V> valueType) {
        final Object value = super.get(key);
        if (output != null && keyType != null && valueType != null && value instanceof Map<?, ?>) {
            final Map<?, ?> input = (Map<?, ?>) value;
            return mapOf(input, output, keyType, valueType);
        }
        return ofNullable(output).map(Supplier::get).orElse(null);
    }
}
