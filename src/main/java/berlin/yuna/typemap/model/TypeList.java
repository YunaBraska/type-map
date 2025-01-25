package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.JsonDecoder;
import berlin.yuna.typemap.logic.XmlDecoder;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Optional.ofNullable;

/**
 * {@link TypeList} is a specialized implementation of {@link ArrayList} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link TypeList}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class TypeList extends ArrayList<Object> implements TypeListI<TypeList> {

    /**
     * Default constructor for creating an empty {@link TypeList}.
     */
    public TypeList() {
        this((Collection<?>) null);
    }

    /**
     * Constructs a new {@link TypeList} of the specified json.
     */
    public TypeList(final String jsonOrXml) {
        this(jsonOrXml != null && jsonOrXml.startsWith("<") ? XmlDecoder.xmlTypeOf(jsonOrXml) : JsonDecoder.jsonListOf(jsonOrXml));
    }

    /**
     * Constructs a new {@link TypeList} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public TypeList(final Collection<?> map) {
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
    public TypeList addR(final int index, final Object value) {
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
     * @return the updated {@link TypeList} instance for chaining.
     */
    public TypeList addR(final Object index, final Object value) {
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
