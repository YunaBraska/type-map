package berlin.yuna.typemap.model;

import java.util.Collection;
import java.util.List;

public interface TypeListI<C extends TypeListI<C>> extends List<Object>, TypeInfo<C> {

    /**
     * Adds the specified value
     *
     * @param value the value to be added
     * @return the updated TypeList instance for chaining.
     */
    C addReturn(final Object value);

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C addReturn(final int index, final Object value);

    /**
     * Adds all entries to this specified List
     *
     * @param collection which provides all entries to add
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C addAllReturn(final Collection<?> collection);
}
