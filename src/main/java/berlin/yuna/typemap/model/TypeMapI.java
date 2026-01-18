package berlin.yuna.typemap.model;

import berlin.yuna.typemap.logic.XmlEncoder;

import java.util.Map;
import java.util.stream.Stream;

import static berlin.yuna.typemap.model.Type.empty;
import static berlin.yuna.typemap.model.Type.typeOf;
public interface TypeMapI<C extends TypeMapI<C>> extends Map<Object, Object>, TypeInfo<C> {

    /**
     * Adds a mapping and returns this instance for chaining.
     *
     * @param key   map key
     * @param value map value
     * @return this map instance
     */
    @SuppressWarnings("unchecked")
    default C putR(final Object key, final Object value) {
        put(key, value);
        return (C) this;
    }

    default Type<TypeMapI<?>> typeMapOpt() {
        return typeOf(this);
    }

    default Type<TypeListI<?>> typeListOpt() {
        return empty();
    }

    default String toXML() {
        return XmlEncoder.toXmlMap(this);
    }

    /**
     * Streams map content as key/value pairs.
     */
    default Stream<Pair<String, Object>> streamPairs() {
        return streamPairs(null, (Object[]) null);
    }

    /**
     * Streams map content as key/value pairs, converting values to the given type.
     *
     * @param valueType optional target type for values
     */
    default <V> Stream<Pair<String, V>> streamPairs(final Class<V> valueType) {
        return streamPairs(valueType, (Object[]) null);
    }

    /**
     * Streams map content as key/value pairs from a nested map/list path, converting values to the given type.
     *
     * @param valueType optional target type for values
     * @param path      path to a nested map/list
     */
    default <V> Stream<Pair<String, V>> streamPairs(final Class<V> valueType, final Object... path) {
        return TypeInfo.super.streamAny(valueType, path).map(p -> new Pair<>(String.valueOf(p.key()), p.value()));
    }

    /**
     * Streams map content as key/value pairs from a nested map/list path.
     *
     * @param path path to a nested map/list
     */
    default Stream<Pair<String, Object>> streamPairs(final Object... path) {
        return streamPairs(null, path);
    }
}
