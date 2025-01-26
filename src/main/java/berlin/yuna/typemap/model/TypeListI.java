package berlin.yuna.typemap.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static berlin.yuna.typemap.model.Type.empty;
import static berlin.yuna.typemap.model.Type.typeOf;

public interface TypeListI<C extends TypeListI<C>> extends List<Object>, TypeInfo<C> {

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C addR(final int index, final Object value);

    /**
     * Adds the specified value
     *
     * @param value the value to be added
     * @return the updated TypeList instance for chaining.
     */
    default C addR(final Object value) {
        return addR(-1, value);
    }

    /**
     * Adds all entries to this specified List
     *
     * @param collection which provides all entries to add
     * @return the updated {@link TypeListI} instance for chaining.
     */
    default C addAllR(final Collection<?> collection) {
        addAll(collection);
        return (C) this;
    }

    /**
     * Fluent typecheck if the current {@link TypeInfo} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    @SuppressWarnings("java:S1452")
    default Type<TypeMapI<?>> typeMapOpt() {
        return empty();
    }

    /**
     * Fluent typecheck if the current {@link TypeInfo} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    @SuppressWarnings("java:S1452")
    default Type<TypeListI<?>> typeListOpt() {
        return typeOf(this);
    }
}
