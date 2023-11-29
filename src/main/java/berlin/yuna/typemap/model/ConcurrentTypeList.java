package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.JsonDecoder;
import berlin.yuna.typemap.logic.JsonEncoder;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.TypeMap.convertAndMap;
import static berlin.yuna.typemap.model.TypeMap.treeGet;
import static java.util.Optional.ofNullable;

/**
 * {@link ConcurrentTypeList} is a specialized implementation of {@link HashMap} that offers enhanced
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
        this(JsonDecoder.jsonListOf(json));
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
     * @param value the value to be added
     * @return the updated TypeList instance for chaining.
     */
    @Override
    public ConcurrentTypeList addd(final Object value) {
        super.add(value);
        return this;
    }

    /**
     * Adds the specified value
     *
     * @param value the value to be added
     * @return the updated {@link TypeListI} instance for chaining.
     */
    @Override
    public ConcurrentTypeList addd(final int index, final Object value) {
        super.add(index, value);
        return this;
    }

    /**
     * Adds all entries to this specified List
     *
     * @param collection which provides all entries to add
     * @return the updated {@link TypeListI} instance for chaining.
     */
    @Override
    public ConcurrentTypeList adddAll(final Collection<?> collection) {
        super.addAll(collection);
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

    /**
     * Retrieves the value to which the specified index, and attempts to
     * convert it to the specified type.
     *
     * @param <T>   The target type for conversion.
     * @param index the index whose associated value is to be returned.
     * @param type  the Class object of the type to convert to.
     * @return value if present and convertible, else null.
     */
    public <T> T get(final int index, final Class<T> type) {
        return gett(index, type).orElse(null);
    }

    /**
     * Retrieves the value to which the specified index, and attempts to
     * convert it to the specified type.
     *
     * @param <T>   The target type for conversion.
     * @param index the index whose associated value is to be returned.
     * @param type  the Class object of the type to convert to.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    @Override
    public <T> Optional<T> gett(final int index, final Class<T> type) {
        return ofNullable(treeGet(this, index)).map(object -> convertObj(object, type));
    }

    /**
     * Retrieves the value to which the specified key is mapped, and attempts to
     * convert it to the specified type.
     *
     * @param <T>  The target type for conversion.
     * @param path the key whose associated value is to be returned.
     * @param type the Class object of the type to convert to.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    @Override
    public <T> Optional<T> gett(final Class<T> type, final Object... path) {
        return ofNullable(treeGet(this, path)).map(object -> convertObj(object, type));
    }

    /**
     * This method converts the retrieved map to a list of the specified key and value types.
     *
     * @param path The key whose associated value is to be returned.
     * @return a {@link LinkedTypeMap} of the specified key and value types.
     */
    public TypeList getList(final Object... path) {
        return getList(TypeList::new, Object.class, path);
    }

    /**
     * Retrieves a collection associated at the specified index and converts it to
     * the specified element type.
     *
     * @param <E>      The type of elements in the collection.
     * @param path     The index whose associated value is to be returned.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    public <E> List<E> getList(final Class<E> itemType, final Object... path) {
        return getList(ArrayList::new, itemType, path);
    }

    /**
     * Retrieves a collection associated at the specified index and converts it to
     * the specified element type.
     *
     * @param <E>      The type of elements in the collection.
     * @param index    The index whose associated value is to be returned.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    public <E> List<E> getList(final int index, final Class<E> itemType) {
        return getList(ArrayList::new, itemType, index);
    }

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
    @Override
    public <T extends Collection<E>, E> T getList(final int index, final Supplier<? extends T> output, final Class<E> itemType) {
        return collectionOf(super.get(index), output, itemType);
    }

    /**
     * Retrieves a collection associated with the specified key and converts it to
     * the specified collection type and element type.
     *
     * @param <T>      The type of the collection to be returned.
     * @param <E>      The type of elements in the collection.
     * @param path     The key whose associated value is to be returned.
     * @param output   The supplier providing a new collection instance.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    @Override
    public <T extends Collection<E>, E> T getList(final Supplier<? extends T> output, final Class<E> itemType, final Object... path) {
        return collectionOf(treeGet(this, path), output, itemType);
    }

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
    public <E> E[] getArray(final int index, final E[] typeIndicator, final Class<E> componentType) {
        final ArrayList<E> result = getList(index, ArrayList::new, componentType);
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
    }

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method is useful for cases where the type indicator is an array instance.
     *
     * @param <E>           The component type of the array.
     * @param path          The key whose associated value is to be returned.
     * @param typeIndicator An array instance indicating the type of array to return.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    @Override
    public <E> E[] getArray(final E[] typeIndicator, final Class<E> componentType, final Object... path) {
        final ArrayList<E> result = getList(ArrayList::new, componentType, path);
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
    }

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
    @Override
    public <E> E[] getArray(final int index, final IntFunction<E[]> generator, final Class<E> componentType) {
        return getList(index, ArrayList::new, componentType).stream().toArray(generator);
    }

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method allows for custom array generation using a generator function.
     *
     * @param <E>           The component type of the array.
     * @param path          The key whose associated value is to be returned.
     * @param generator     A function to generate the array of the required size.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    @Override
    public <E> E[] getArray(final IntFunction<E[]> generator, final Class<E> componentType, final Object... path) {
        return getList(ArrayList::new, componentType, path).stream().toArray(generator);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param path The key whose associated value is to be returned.
     * @return a map of the specified key and value types.
     */
    @Override
    public LinkedTypeMap getMap(final Object... path) {
        return getMap(LinkedTypeMap::new, Object.class, Object.class, path);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param path      The key whose associated value is to be returned.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    public <K, V> Map<K, V> getMap(final Class<K> keyType, final Class<V> valueType, final Object... path){
        return convertAndMap(treeGet(this, path), LinkedHashMap::new, keyType, valueType);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param index      The key whose associated value is to be returned.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    public <K, V> Map<K, V> getMap(final int index, final Class<K> keyType, final Class<V> valueType) {
        return convertAndMap(treeGet(this, index), LinkedHashMap::new, keyType, valueType);
    }

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
    @Override
    public <K, V, M extends Map<K, V>> M getMap(final int index, final Supplier<M> output, final Class<K> keyType, final Class<V> valueType) {
        return convertAndMap(treeGet(this, index), output, keyType, valueType);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param <M>       The type of the map to be returned.
     * @param path      The key whose associated value is to be returned.
     * @param output    A supplier providing a new map instance.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    @Override
    public <K, V, M extends Map<K, V>> M getMap(final Supplier<M> output, final Class<K> keyType, final Class<V> valueType, final Object... path) {
        return convertAndMap(treeGet(this, path), output, keyType, valueType);
    }

    /**
     * Converts any object to its JSON representation.
     * This method intelligently dispatches the conversion task based on the type of the object,
     * handling Maps, Collections, Arrays (both primitive and object types), and other objects.
     *
     * @param path The key whose associated value is to be returned.
     * @return A JSON representation of the key value as a String.
     */
    @Override
    public String toJson(final Object... path) {
        return JsonEncoder.toJson(treeGet(this, path));
    }

    /**
     * Converts any object to its JSON representation.
     * This method intelligently dispatches the conversion task based on the type of the object,
     * handling Maps, Collections, Arrays (both primitive and object types), and other objects.
     *
     * @return A JSON representation of itself as a String.
     */
    @Override
    public String toJson() {
        return JsonEncoder.toJson(this);
    }
}
