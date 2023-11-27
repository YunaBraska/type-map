package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.JsonDecoder;
import berlin.yuna.typemap.logic.JsonEncoder;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.TypeMap.getMap;
import static berlin.yuna.typemap.model.TypeMap.treeGet;
import static java.util.Optional.ofNullable;

/**
 * {@link LinkedTypeMap} is a specialized implementation of {@link LinkedHashMap} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link LinkedTypeMap}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class LinkedTypeMap extends LinkedHashMap<Object, Object> implements TypeMapI<LinkedTypeMap> {

    /**
     * Default constructor for creating an empty {@link LinkedTypeMap}.
     */
    public LinkedTypeMap() {
        this((Map<?, ?>) null);
    }

    /**
     * Constructs a new {@link LinkedTypeMap} of the specified json.
     */
    public LinkedTypeMap(final String json) {
        this(JsonDecoder.jsonMapOf(json));
    }

    /**
     * Constructs a new {@link LinkedTypeMap} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public LinkedTypeMap(final Map<?, ?> map) {
        ofNullable(map).ifPresent(super::putAll);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated {@link LinkedTypeMap} instance for chaining.
     */
    @Override
    public LinkedTypeMap put(final Object key, final Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * Retrieves the value to which the specified key is mapped, and attempts to
     * convert it to the specified type.
     *
     * @param <T>  The target type for conversion.
     * @param key  the key whose associated value is to be returned.
     * @param type the Class object of the type to convert to.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    public <T> Optional<T> get(final Class<T> type, final Object... key) {
        return ofNullable(treeGet(this, key)).map(object -> convertObj(object, type));
    }

    /**
     * Retrieves the value to which the specified key is mapped, and attempts to
     * convert it to the specified type.
     *
     * @param <T>  The target type for conversion.
     * @param key  the key whose associated value is to be returned.
     * @param type the Class object of the type to convert to.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    public <T> Optional<T> get(final Object key, final Class<T> type) {
        return ofNullable(super.get(key)).map(object -> convertObj(object, type));
    }

    /**
     * Retrieves a collection associated with the specified key and converts it to
     * the specified collection type and element type.
     *
     * @param <T>      The type of the collection to be returned.
     * @param <E>      The type of elements in the collection.
     * @param key      The key whose associated value is to be returned.
     * @param output   The supplier providing a new collection instance.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    public <T extends Collection<E>, E> T get(final Supplier<? extends T> output, final Class<E> itemType, final Object... key) {
        return collectionOf(treeGet(this, key), output, itemType);
    }

    /**
     * Retrieves a collection associated with the specified key and converts it to
     * the specified collection type and element type.
     *
     * @param <T>      The type of the collection to be returned.
     * @param <E>      The type of elements in the collection.
     * @param key      The key whose associated value is to be returned.
     * @param output   The supplier providing a new collection instance.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    public <T extends Collection<E>, E> T get(final Object key, final Supplier<? extends T> output, final Class<E> itemType) {
        return collectionOf(super.get(key), output, itemType);
    }

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method is useful for cases where the type indicator is an array instance.
     *
     * @param <E>           The component type of the array.
     * @param key           The key whose associated value is to be returned.
     * @param typeIndicator An array instance indicating the type of array to return.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public <E> E[] getArray(final E[] typeIndicator, final Class<E> componentType, final Object... key) {
        final ArrayList<E> result = get(ArrayList::new, componentType, key);
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
    }

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method is useful for cases where the type indicator is an array instance.
     *
     * @param <E>           The component type of the array.
     * @param key           The key whose associated value is to be returned.
     * @param typeIndicator An array instance indicating the type of array to return.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public <E> E[] getArray(final Object key, final E[] typeIndicator, final Class<E> componentType) {
        final ArrayList<E> result = get(key, ArrayList::new, componentType);
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
    }

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method allows for custom array generation using a generator function.
     *
     * @param <E>           The component type of the array.
     * @param key           The key whose associated value is to be returned.
     * @param generator     A function to generate the array of the required size.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public <E> E[] getArray(final IntFunction<E[]> generator, final Class<E> componentType, final Object... key) {
        return get(ArrayList::new, componentType, key).stream().toArray(generator);
    }

    /**
     * Retrieves an array of a specific type associated with the specified key.
     * This method allows for custom array generation using a generator function.
     *
     * @param <E>           The component type of the array.
     * @param key           The key whose associated value is to be returned.
     * @param generator     A function to generate the array of the required size.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public <E> E[] getArray(final Object key, final IntFunction<E[]> generator, final Class<E> componentType) {
        return get(key, ArrayList::new, componentType).stream().toArray(generator);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param <M>       The type of the map to be returned.
     * @param key       The key whose associated value is to be returned.
     * @param output    A supplier providing a new map instance.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    public <K, V, M extends Map<K, V>> M get(final Supplier<M> output, final Class<K> keyType, final Class<V> valueType, final Object... key) {
        return getMap(treeGet(this, key), output, keyType, valueType);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param <M>       The type of the map to be returned.
     * @param key       The key whose associated value is to be returned.
     * @param output    A supplier providing a new map instance.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    public <K, V, M extends Map<K, V>> M get(final Object key, final Supplier<M> output, final Class<K> keyType, final Class<V> valueType) {
        return getMap(super.get(key), output, keyType, valueType);
    }

    /**
     * Converts any object to its JSON representation.
     * This method intelligently dispatches the conversion task based on the type of the object,
     * handling Maps, Collections, Arrays (both primitive and object types), and other objects.
     *
     * @param key The key whose associated value is to be returned.
     * @return A JSON representation of the key value as a String.
     */
    public String toJson(final Object... key) {
        return JsonEncoder.toJson(treeGet(this, key));
    }

    /**
     * Converts any object to its JSON representation.
     * This method intelligently dispatches the conversion task based on the type of the object,
     * handling Maps, Collections, Arrays (both primitive and object types), and other objects.
     *
     * @return A JSON representation of itself as a String.
     */
    public String toJson() {
        return JsonEncoder.toJson(this);
    }
}
