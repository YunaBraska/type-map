package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.JsonDecoder;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Optional.ofNullable;

/**
 * {@link ConcurrentTypeList} is a specialized implementation of {@link CopyOnWriteArrayList} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link ConcurrentTypeList}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class ConcurrentTypeList extends CopyOnWriteArrayList<Object> implements TypeListI<ConcurrentTypeList> {

    /**
     * Default constructor for creating an empty {@link ConcurrentTypeList}.
     */
    public ConcurrentTypeList() {
        this((Collection<?>) null);
    }

    /**
     * Constructs a new {@link ConcurrentTypeList} of the specified json.
     */
    public ConcurrentTypeList(final String json) {
        this(JsonDecoder.listOf(json));
    }

    /**
     * Constructs a new {@link ConcurrentTypeList} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public ConcurrentTypeList(final Collection<?> map) {
        ofNullable(map).ifPresent(super::addAll);
    }

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link TypeList} instance for chaining.
     */
    @Override
    public ConcurrentTypeList addR(final int index, final Object value) {
        if (index >= 0 && index < this.size()) {
            super.add(index, value);
        } else {
            super.add(value);
        }
        return this;
    }

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link ConcurrentTypeList} instance for chaining.
     */
    public ConcurrentTypeList addR(final Object index, final Object value) {
        if (index == null) {
            super.add(value);
        } else if (index instanceof Number) {
            this.addR(((Number) index).intValue(), value);
        }
        return this;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     */
    @Override
    public Object get(final int index) {
        return index >= 0 && index < this.size() ? super.get(index) : null;
    }
}
