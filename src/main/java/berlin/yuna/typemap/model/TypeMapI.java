package berlin.yuna.typemap.model;

import java.util.*;

public interface TypeMapI<C extends TypeMapI<C>> extends Map<Object, Object>, TypeInfo<C> {

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated TypeMap instance for chaining.
     */
    default C putR(final Object key, final Object value) {
        put(key, value);
        return (C) this;
    }

    /**
     * Fluent typecheck if the current {@link TypeInfo} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    default Optional<TypeMapI<?>> typeMapOpt() {
        return Optional.of(this);
    }

    /**
     * Fluent typecheck if the current {@link TypeInfo} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    default Optional<TypeListI<?>> typeListOpt() {
        return Optional.empty();
    }
}
