package berlin.yuna.typemap.model;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public interface TypeListI<C extends TypeListI<C>> extends List<Object>, TypeContainer<C> {

    /**
     * Adds the specified value
     *
     * @param value the value to be added
     * @return the updated TypeList instance for chaining.
     */
    C addd(final Object value);

    /**
     * Adds the specified value
     *
     * @param value the value to be added
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C addd(final int index, final Object value);

    /**
     * Adds all entries to this specified List
     *
     * @param collection which provides all entries to add
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C adddAll(final Collection<?> collection);

    /**
     * Retrieves the value to which the specified index, and attempts to
     * convert it to the specified type.
     *
     * @param <T>   The target type for conversion.
     * @param index the index whose associated value is to be returned.
     * @param type  the Class object of the type to convert to.
     * @return value if present and convertible, else null.
     */
    <T> T get(final int index, final Class<T> type);

    /**
     * Retrieves the value to which the specified index, and attempts to
     * convert it to the specified type.
     *
     * @param <T>   The target type for conversion.
     * @param index the index whose associated value is to be returned.
     * @param type  the Class object of the type to convert to.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    <T> Optional<T> gett(final int index, final Class<T> type);

    /**
     * Retrieves a collection associated at the specified index and converts it to
     * the specified element type.
     *
     * @param <E>      The type of elements in the collection.
     * @param index    The index whose associated value is to be returned.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    <E> List<E> getList(final int index, final Class<E> itemType);

    /**
     * Retrieves a collection associated at the specified index and converts it to
     * the specified collection type and element type.
     *
     * @param <T>      The type of the collection to be returned.
     * @param <E>      The type of elements in the collection.
     * @param index    The index whose associated value is to be returned.
     * @param output   The supplier providing a new collection instance.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    <T extends Collection<E>, E> T getList(final int index, final Supplier<? extends T> output, final Class<E> itemType);

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param index     The key whose associated value is to be returned.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    <K, V> Map<K, V> getMap(final int index, final Class<K> keyType, final Class<V> valueType);

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param <M>       The type of the map to be returned.
     * @param index     The key whose associated value is to be returned.
     * @param output    A supplier providing a new map instance.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    <K, V, M extends Map<K, V>> M getMap(final int index, final Supplier<M> output, final Class<K> keyType, final Class<V> valueType);

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method is useful for cases where the type indicator is an array instance.
     *
     * @param <E>           The component type of the array.
     * @param index         The index whose associated value is to be returned.
     * @param typeIndicator An array instance indicating the type of array to return.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    <E> E[] getArray(final int index, final E[] typeIndicator, final Class<E> componentType);

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method allows for custom array generation using a generator function.
     *
     * @param <E>           The component type of the array.
     * @param index         The key whose associated value is to be returned.
     * @param generator     A function to generate the array of the required size.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    <E> E[] getArray(final int index, final IntFunction<E[]> generator, final Class<E> componentType);
}
