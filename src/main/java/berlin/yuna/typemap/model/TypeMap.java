package berlin.yuna.typemap.model;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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

    public <E> E[] getArray(final Object key, final E[] typeIndicator, final Class<E> componentType) {
        final ArrayList<E> result = get(key, ArrayList::new, componentType);
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
    }

    public <E> E[] getArray(final Object key, final IntFunction<E[]> generator, final Class<E> componentType) {
        return get(key, ArrayList::new, componentType).stream().toArray(generator);
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
