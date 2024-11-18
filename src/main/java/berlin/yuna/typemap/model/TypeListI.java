package berlin.yuna.typemap.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TypeListI<C extends TypeListI<C>> extends List<Object>, TypeI<C> {

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C addReturn(final int index, final Object value);

    /**
     * Adds the specified value
     *
     * @param value the value to be added
     * @return the updated TypeList instance for chaining.
     */
    default C addReturn(final Object value) {
        return addReturn(-1, value);
    }

    /**
     * Adds all entries to this specified List
     *
     * @param collection which provides all entries to add
     * @return the updated {@link TypeListI} instance for chaining.
     */
    default C addAllReturn(final Collection<?> collection) {
        addAll(collection);
        return (C) this;
    }

    /**
     * Fluent typecheck if the current {@link TypeI} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    @SuppressWarnings("java:S1452")
    default Optional<TypeMapI<?>> typeMapOpt() {
        return Optional.empty();
    }

    /**
     * Fluent typecheck if the current {@link TypeI} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    @SuppressWarnings("java:S1452")
    default Optional<TypeListI<?>> typeListOpt() {
        return Optional.of(this);
    }
}
