package berlin.yuna.typemap.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.Type.empty;
import static berlin.yuna.typemap.model.Type.typeOf;

public interface TypeListI<C extends TypeListI<C>> extends List<Object>, TypeInfo<C> {

    /**
     * Adds a value at the given index (or appends when index is negative) and returns this instance for chaining.
     *
     * @param index index to insert at, or negative to append
     * @param value value to add
     * @return this list instance
     */
    C addR(final int index, final Object value);

    /**
     * Appends a value and returns this instance for chaining.
     *
     * @param value value to add
     * @return this list instance
     */
    default C addR(final Object value) {
        return addR(-1, value);
    }

    /**
     * Adds all values from the given collection and returns this instance for chaining.
     *
     * @param collection collection of values to add
     * @return this list instance
     */
    default C addAllR(final Collection<?> collection) {
        addAll(collection);
        return (C) this;
    }

    @SuppressWarnings("java:S1452")
    default Type<TypeMapI<?>> typeMapOpt() {
        return empty();
    }

    @SuppressWarnings("java:S1452")
    default Type<TypeListI<?>> typeListOpt() {
        return typeOf(this);
    }

    /**
     * Streams list content as index/value pairs.
     */
    default Stream<Pair<Integer, Object>> streamPairs() {
        return streamPairs(null, (Object[]) null);
    }

    /**
     * Streams list content as index/value pairs, converting values to the given type.
     *
     * @param valueType optional target type for values
     */
    default <V> Stream<Pair<Integer, V>> streamPairs(final Class<V> valueType) {
        return streamPairs(valueType, (Object[]) null);
    }

    /**
     * Streams list content as index/value pairs from a nested map/list path, converting values to the given type.
     *
     * @param valueType optional target type for values
     * @param path      path to a nested map/list
     */
    default <V> Stream<Pair<Integer, V>> streamPairs(final Class<V> valueType, final Object... path) {
        return TypeInfo.super.streamAny(valueType, path)
            .map(p -> new Pair<>(convertObj(p.key(), Integer.class), p.value()));
    }
}
