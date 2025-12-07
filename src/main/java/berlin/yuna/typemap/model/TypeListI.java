package berlin.yuna.typemap.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.Type.empty;
import static berlin.yuna.typemap.model.Type.typeOf;

public interface TypeListI<C extends TypeListI<C>> extends List<Object>, TypeInfo<C> {

    C addR(final int index, final Object value);

    default C addR(final Object value) {
        return addR(-1, value);
    }

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

    default Stream<Pair<Integer, Object>> streamPairs() {
        return streamPairs(null, (Object[]) null);
    }

    default <V> Stream<Pair<Integer, V>> streamPairs(final Class<V> valueType) {
        return streamPairs(valueType, (Object[]) null);
    }

    default <V> Stream<Pair<Integer, V>> streamPairs(final Class<V> valueType, final Object... path) {
        return TypeInfo.super.streamAny(valueType, path)
            .map(p -> new Pair<>(convertObj(p.key(), Integer.class), p.value()));
    }
}
