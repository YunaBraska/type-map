package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.ArgsDecoder;
import berlin.yuna.typemap.logic.JsonDecoder;
import berlin.yuna.typemap.logic.JsonEncoder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.TypeMap.convertAndMap;
import static berlin.yuna.typemap.model.TypeMap.treeGet;
import static java.util.Optional.ofNullable;

/**
 * {@link ConcurrentTypeMap} is a specialized implementation of {@link ConcurrentHashMap} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link ConcurrentTypeMap}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class ConcurrentTypeMap extends ConcurrentHashMap<Object, Object> implements TypeMapI<ConcurrentTypeMap> {

    /**
     * Default constructor for creating an empty TypeMap.
     */
    public ConcurrentTypeMap() {
        this((Map<?, ?>) null);
    }

    /**
     * Constructs a new {@link ConcurrentTypeMap} of the specified json.
     */
    public ConcurrentTypeMap(final String json) {
        this(JsonDecoder.jsonMapOf(json));
    }

    /**
     * Constructs a new {@link ConcurrentTypeMap} of the specified command line arguments.
     */
    public ConcurrentTypeMap(final String[] cliArgs) {
        this(ArgsDecoder.argsOf(String.join(" ", cliArgs)));
    }

    /**
     * Constructs a new {@link ConcurrentTypeMap} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public ConcurrentTypeMap(final Map<?, ?> map) {
        ofNullable(map).ifPresent(super::putAll);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated {@link ConcurrentTypeMap} instance for chaining.
     */
    public ConcurrentTypeMap putt(final Object key, final Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated {@link ConcurrentTypeMap} instance for chaining.
     */
    public ConcurrentTypeMap addd(final Object key, final Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * Retrieves the value to which the specified key is mapped, and attempts to
     * convert it to the specified type.
     *
     * @param <T>  The target type for conversion.
     * @param path the key whose associated value is to be returned.
     * @param type the Class object of the type to convert to.
     * @return the value if present and convertible, else null.
     */
    public <T> T get(final Class<T> type, final Object... path) {
        return gett(type, path).orElse(null);
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
    public <T> Optional<T> gett(final Class<T> type, final Object... path) {
        return ofNullable(treeGet(this, path)).map(object -> convertObj(object, type));
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
    public <T> T get(final Object key, final Class<T> type) {
        return gett(key, type).orElse(null);
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
    public <T> Optional<T> gett(final Object key, final Class<T> type) {
        return ofNullable(super.get(key)).map(object -> convertObj(object, type));
    }

    /**
     * This method converts the retrieved map to a list of the specified key and value types.
     *
     * @param path The key whose associated value is to be returned.
     * @return a {@link TypeList} of the specified key and value types.
     */
    public TypeList getList(final Object... path) {
        return getList(TypeList::new, Object.class, path);
    }

    /**
     * Retrieves a collection associated at the specified index and converts it to
     * the specified element type.
     *
     * @param <E>      The type of elements in the collection.
     * @param key      The index whose associated value is to be returned.
     * @param itemType The class of the items in the collection.
     * @return a collection of the specified type and element type.
     */
    public <E> List<E> getList(final Object key, final Class<E> itemType) {
        return getList(ArrayList::new, itemType, key);
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
    public <T extends Collection<E>, E> T getList(final Supplier<? extends T> output, final Class<E> itemType, final Object... path) {
        return collectionOf(treeGet(this, path), output, itemType);
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
    public <T extends Collection<E>, E> T getList(final Object key, final Supplier<? extends T> output, final Class<E> itemType) {
        return collectionOf(super.get(key), output, itemType);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param path The key whose associated value is to be returned.
     * @return a {@link LinkedTypeMap} of the specified key and value types.
     */
    public LinkedTypeMap getMap(final Object... path) {
        return getMap(LinkedTypeMap::new, Object.class, Object.class, path);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>       The type of keys in the returned map.
     * @param <V>       The type of values in the returned map.
     * @param key       The key whose associated value is to be returned.
     * @param keyType   The class of the map's key type.
     * @param valueType The class of the map's value type.
     * @return a map of the specified key and value types.
     */
    public <K, V> Map<K, V> getMap(final Object key, final Class<K> keyType, final Class<V> valueType) {
        return convertAndMap(treeGet(this, key), LinkedHashMap::new, keyType, valueType);
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
    public <K, V> Map<K, V> getMap(final Class<K> keyType, final Class<V> valueType, final Object... path) {
        return convertAndMap(treeGet(this, path), LinkedHashMap::new, keyType, valueType);
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
    public <K, V, M extends Map<K, V>> M getMap(final Supplier<M> output, final Class<K> keyType, final Class<V> valueType, final Object... path) {
        return convertAndMap(treeGet(this, path), output, keyType, valueType);
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
    public <K, V, M extends Map<K, V>> M getMap(final Object key, final Supplier<M> output, final Class<K> keyType, final Class<V> valueType) {
        return convertAndMap(super.get(key), output, keyType, valueType);
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
    public <E> E[] getArray(final E[] typeIndicator, final Class<E> componentType, final Object... path) {
        final ArrayList<E> result = getList(ArrayList::new, componentType, path);
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
        final ArrayList<E> result = getList(key, ArrayList::new, componentType);
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
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
    public <E> E[] getArray(final IntFunction<E[]> generator, final Class<E> componentType, final Object... path) {
        return getList(ArrayList::new, componentType, path).stream().toArray(generator);
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
        return getList(key, ArrayList::new, componentType).stream().toArray(generator);
    }

    /**
     * Fluent typecheck if the current {@link TypeContainer} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    public Optional<TypeMapI<?>> typeMap() {
        return Optional.of(this);
    }

    /**
     * Fluent typecheck if the current {@link TypeContainer} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    public Optional<TypeListI<?>> typeList() {
        return Optional.empty();
    }

    /**
     * Converts any object to its JSON representation.
     * This method intelligently dispatches the conversion task based on the type of the object,
     * handling Maps, Collections, Arrays (both primitive and object types), and other objects.
     *
     * @param path The key whose associated value is to be returned.
     * @return A JSON representation of the key value as a String.
     */
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
    public String toJson() {
        return JsonEncoder.toJson(this);
    }
}
