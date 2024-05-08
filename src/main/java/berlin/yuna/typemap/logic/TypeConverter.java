package berlin.yuna.typemap.logic;


import berlin.yuna.typemap.model.FunctionOrNull;
import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;
import berlin.yuna.typemap.model.TypeMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static berlin.yuna.typemap.config.TypeConversionRegister.TYPE_CONVERSIONS;

@SuppressWarnings("java:S1168")
public class TypeConverter {

    /**
     * Safely converts an object to the specified target typeId.
     * <p>
     * This method provides a way to convert between common types such as String, Boolean, LocalDateTime, Numbers, Collections, and Maps.
     * If the value is already of the target typeId, it will simply be cast and returned.
     * Otherwise, the method attempts to safely convert the value to the desired typeId.
     * </p>
     * <ul>
     *   <li>If the target typeId is {@code String}, the method converts the value using {@code toString()}.</li>
     *   <li>If the target typeId is {@code Boolean} and the value is a string representation of a boolean, the method converts it using {@code Boolean.valueOf()}.</li>
     *   <li>If the target typeId is {@code LocalDateTime} and the value is a {@code Date}, it converts it to {@code LocalDateTime}.</li>
     *   <li>If the target typeId is {@code Byte} and the value is a number, it casts it to byte.</li>
     *   <li>If the target typeId is a subclass of {@code Number} and the value is a number, it invokes {@code numberOf} to convert it.</li>
     *   <li>If the target typeId is a {@code Collection} or {@code Map}, and the value is already of that typeId, it will simply be cast and returned.</li>
     * </ul>
     *
     * @param <T>        The target typeId to convert the value to.
     * @param value      The object value to convert.
     * @param targetType The class of the target typeId.
     * @return The converted object of typeId {@code T}, or {@code null} if the conversion is not supported.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T convertObj(final Object value, final Class<T> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }

        // Handle non-empty arrays, collections, map
        final Object firstValue = getFirstItem(value);
        if (firstValue != null) {
            return convertObj(firstValue, targetType);
        }

        // Enums
        if (targetType.isEnum()) {
            final Enum anEnum = enumOf(value, (Class<Enum>) targetType);
            if (anEnum != null) {
                return (T) anEnum;
            }
        }

        final Class<?> sourceType = value.getClass();
        final Map<Class<?>, FunctionOrNull> conversions = TYPE_CONVERSIONS.getOrDefault(targetType, Collections.emptyMap());

        // First try to find exact match
        final FunctionOrNull exactMatch = conversions.get(sourceType);
        if (exactMatch != null) {
            return targetType.cast(exactMatch.apply(value));
        }

        // Fallback to more general converters
        for (final Map.Entry<Class<?>, FunctionOrNull> entry : conversions.entrySet()) {
            if (entry.getKey().isAssignableFrom(sourceType)) {
                return targetType.cast(entry.getValue().apply(value));
            }
        }

        // Fallback to string convert
        if (!String.class.equals(sourceType)) {
            return convertObj(String.valueOf(value), targetType);
        }
        return null;
    }

    /**
     * Creates a new map of typeId {@code M} from the given {@code input} map. The keys and values are converted
     * to types {@code K} and {@code V} respectively.
     *
     * @param input The input map containing keys and values to be converted.
     * @return A new map of typeId {@code M} with keys and values converted to types {@code K} and {@code V}. Returns {@code null} if the output map is {@code null}.
     */
    public static TypeMap mapOf(final Map<?, ?> input) {
        return mapOf(input, TypeMap::new, Object.class, Object.class);
    }

    /**
     * Creates a new map of typeId {@code M} from the given {@code input} map. The keys and values are converted
     * to types {@code K} and {@code V} respectively.
     *
     * @param <K>       The typeId of the keys in the output map.
     * @param <V>       The typeId of the values in the output map.
     * @param input     The input map containing keys and values to be converted.
     * @param keyType   The {@code Class} object representing the typeId of key to convert to.
     * @param valueType The {@code Class} object representing the typeId of value to convert to.
     * @return A new map of typeId {@code M} with keys and values converted to types {@code K} and {@code V}. Returns {@code null} if the output map is {@code null}.
     */
    public static <K, V> Map<K, V> mapOf(final Map<?, ?> input, final Class<K> keyType, final Class<V> valueType) {
        return mapOf(input, LinkedHashMap::new, keyType, valueType);
    }

    /**
     * Creates a new map of typeId {@code M} from the given {@code input} map. The keys and values are converted
     * to types {@code K} and {@code V} respectively.
     *
     * @param <K>       The typeId of the keys in the output map.
     * @param <V>       The typeId of the values in the output map.
     * @param <M>       The typeId of the output map, which must be a subclass of {@code Map<K, V>}.
     * @param input     The input map containing keys and values to be converted.
     * @param output    A {@code Supplier} for the output map of typeId {@code M}.
     * @param keyType   The {@code Class} object representing the typeId of key to convert to.
     * @param valueType The {@code Class} object representing the typeId of value to convert to.
     * @return A new map of typeId {@code M} with keys and values converted to types {@code K} and {@code V}. Returns {@code null} if the output map is {@code null}.
     */
    public static <K, V, M extends Map<K, V>> M mapOf(final Map<?, ?> input, final Supplier<M> output, final Class<K> keyType, final Class<V> valueType) {
        if (output == null) {
            return null;
        } else if (input == null || keyType == null || valueType == null) {
            return output.get();
        }

        final M result = output.get();
        if (result == null) {
            return null;
        }
        input.forEach((key, value) -> {
            final K convertedKey = convertObj(key, keyType);
            final V convertedValue = convertObj(value, valueType);
            if (convertedKey != null && convertedValue != null) {
                result.put(convertedKey, convertedValue);
            }
        });

        return result;
    }

    /**
     * Creates a collection of a specific typeId containing elements of a specific typeId based on the given input.
     * The function handles three types of input:
     * - A Collection
     * - An Array
     * - A single Object
     *
     * @param input The input object, which can be an array or a collection.
     * @return A collection of typeId T containing elements of typeId E, or null if conversion is not possible.
     *
     * <p>
     * The method handles three scenarios:
     * 1. If the input is already a collection, it converts it to a collection of typeId T containing elements of typeId E.
     * 2. If the input is an array, it converts it to a collection of typeId T containing elements of typeId E.
     * 3. If the input is a single object, it converts it to typeId E and returns a collection of typeId T containing that single element.
     * </p>
     */
    public static TypeList collectionOf(final Object input) {
        return collectionOf(input, TypeList::new, Object.class);
    }

    /**
     * Creates a collection of a specific typeId containing elements of a specific typeId based on the given input.
     * The function handles three types of input:
     * - A Collection
     * - An Array
     * - A single Object
     *
     * @param <E>      Type of the elements in the target collection.
     * @param input    The input object, which can be an array or a collection.
     * @param itemType The class typeId of the elements to be contained in the output collection.
     * @return A collection of typeId T containing elements of typeId E, or null if conversion is not possible.
     *
     * <p>
     * The method handles three scenarios:
     * 1. If the input is already a collection, it converts it to a collection of typeId T containing elements of typeId E.
     * 2. If the input is an array, it converts it to a collection of typeId T containing elements of typeId E.
     * 3. If the input is a single object, it converts it to typeId E and returns a collection of typeId T containing that single element.
     * </p>
     */
    public static <E> List<E> collectionOf(final Object input, final Class<E> itemType) {
        return collectionOf(input, ArrayList::new, itemType);
    }

    /**
     * Creates a collection of a specific typeId containing elements of a specific typeId based on the given input.
     * The function handles three types of input:
     * - A Collection
     * - An Array
     * - A single Object
     *
     * @param <T>      Type of the target collection.
     * @param <E>      Type of the elements in the target collection.
     * @param input    The input object, which can be an array or a collection.
     * @param output   A supplier for creating an empty collection of typeId T.
     * @param itemType The class typeId of the elements to be contained in the output collection.
     * @return A collection of typeId T containing elements of typeId E, or null if conversion is not possible.
     *
     * <p>
     * The method handles three scenarios:
     * 1. If the input is already a collection, it converts it to a collection of typeId T containing elements of typeId E.
     * 2. If the input is an array, it converts it to a collection of typeId T containing elements of typeId E.
     * 3. If the input is a single object, it converts it to typeId E and returns a collection of typeId T containing that single element.
     * </p>
     */
    public static <T extends Collection<E>, E> T collectionOf(final Object input, final Supplier<? extends T> output, final Class<E> itemType) {
        if (output == null) {
            return null;
        } else if (input != null && itemType != null) {
            if (input instanceof Collection<?>) {
                final Collection<?> noTypeCollection = (Collection<?>) input;
                return collectionOf(noTypeCollection, output, itemType);
            } else if (input.getClass().isArray()) {
                final Collection<Object> noTypeCollection = new ArrayList<>();
                iterateOverArray(input, item -> noTypeCollection.add(convertObj(item, itemType)));
                return collectionOf(noTypeCollection, output, itemType);
            } else {
                final E converted = convertObj(input, itemType);
                if (converted != null) {
                    final T result = output.get();
                    if (result == null) {
                        return null;
                    }
                    result.add(converted);
                    return result;
                }
            }
        }
        return output.get();
    }

    /**
     * Converts an object to an array of a specified type. If the object is a collection,
     * each element is converted to the component type of the array.
     *
     * @param <E>           The component type of the array.
     * @param object        The object to be converted.
     * @param typeIndicator An array instance indicating the type of array to return.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public static <E> E[] arrayOf(final Object object, final E[] typeIndicator, final Class<E> componentType) {
        ArrayList<E> result = collectionOf(object, ArrayList::new, componentType);
        result = result == null ? new ArrayList<>() : result;
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
    }

    /**
     * Converts an object to an array of a specified type using a generator function.
     * If the object is a collection, each element is converted to the component type of the array.
     *
     * @param <E>           The component type of the array.
     * @param object        The object to be converted.
     * @param generator     A function to generate the array of the required size.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public static <E> E[] arrayOf(final Object object, final IntFunction<E[]> generator, final Class<E> componentType) {
        ArrayList<E> result = collectionOf(object, ArrayList::new, componentType);
        result = result == null ? new ArrayList<>() : result;
        return result.stream().toArray(generator);
    }

    /**
     * Converts an array of objects to an array of a specified type.
     *
     * @param <E>           The component type of the target array.
     * @param array         The array to be converted.
     * @param typeIndicator An array instance indicating the type of array to return.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public static <E> E[] arrayOf(final Object[] array, final E[] typeIndicator, final Class<E> componentType) {
        return arrayOf(Arrays.stream(array).collect(Collectors.toList()), typeIndicator, componentType);
    }

    /**
     * Converts an array of objects to an array of a specified type using a generator function.
     *
     * @param <E>           The component type of the target array.
     * @param array         The array to be converted.
     * @param generator     A function to generate the array of the required size.
     * @param componentType The class of the array's component type.
     * @return an array of the specified component type.
     */
    public static <E> E[] arrayOf(final Object[] array, final IntFunction<E[]> generator, final Class<E> componentType) {
        return arrayOf(Arrays.stream(array).collect(Collectors.toList()), generator, componentType);
    }

    /**
     * Converts a string value to an enum of a specified type. If the value does not match
     * any enum constants, or if an error occurs, this method returns null.
     *
     * @param <T>      The enum type to which the string is to be converted.
     * @param value    The string value to be converted to an enum constant.
     * @param enumType The class of the enum type.
     * @return the enum constant corresponding to the given string, or null if no match is found.
     */
    public static <T extends Enum<T>> T enumOf(final Object value, final Class<T> enumType) {
        try {
            if(value instanceof Number) {
                final int ordinal = ((Number) value).intValue();
                final T[] enumConstants = enumType.getEnumConstants();
                if (ordinal >= 0 && ordinal < enumConstants.length) {
                    return enumConstants[ordinal];
                }
                return null;
            } else {
                return Enum.valueOf(enumType, String.valueOf(value));
            }
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Retrieves the first item from a given collection, array, or map.
     * <p>
     * This method is designed to abstract the process of obtaining the first element from various data structures,
     * handling Collections, Arrays (both object and primitive types), and Maps. It returns null if the provided
     * data structure is empty or if the input is not a Collection, Array, or Map.
     * <p>
     * Note: For Maps, this method returns the first Map.Entry object.
     *
     * @param value The Collection, Array, or Map from which to retrieve the first element.
     * @return The first element of the provided Collection, Array, or Map, or null if it's empty or not an instance of these types.
     */
    public static Object getFirstItem(final Object value) {
        if (value instanceof Collection<?>) {
            final Collection<?> collection = (Collection<?>) value;
            return collection.isEmpty() ? null : collection.iterator().next();
        } else if (value.getClass().isArray()) {
            return getFirstFromArray(value);
        } else if (value instanceof Map<?, ?>) {
            final Map<?, ?> map = (Map<?, ?>) value;
            if (!map.isEmpty()) {
                final Map.Entry<?, ?> first = map.entrySet().iterator().next();
                return first.getKey() == null || "".equals(first.getKey()) ? first.getValue() : first.getKey();
            }
        } else if (value instanceof Pair<?, ?>) {
            final Pair<?, ?> pair = (Pair<?, ?>) value;
            return pair.key() == null || "".equals(pair.key()) ? pair.value() : pair.key();
        }
        return null;
    }

    /**
     * Iterates over an array of any type, including all primitive arrays, and applies a given Consumer function to each element.
     * <p>
     * This method exists because Java treats primitive arrays differently from object arrays. In Java, primitive arrays (like int[], double[], etc.)
     * and object arrays (like Integer[], String[], etc.) do not have a common interface or superclass that reflects their array nature.
     * Therefore, to handle all possible array types (including primitives) without using reflection, we need to explicitly check and handle
     * each primitive array type. This method provides a unified way to iterate over any array type, applying a Consumer action to each element,
     * regardless of whether it's an object array or a primitive array.
     *
     * @param array    The array to be iterated over. Can be an object array or any primitive array type.
     * @param consumer The Consumer function to apply to each element of the array.
     */
    @SuppressWarnings("java:S3776")
    public static void iterateOverArray(final Object array, final Consumer<Object> consumer) {
        if (array instanceof Object[]) {
            for (final Object item : (Object[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof int[]) {
            for (final int item : (int[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof long[]) {
            for (final long item : (long[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof double[]) {
            for (final double item : (double[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof float[]) {
            for (final float item : (float[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof boolean[]) {
            for (final boolean item : (boolean[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof char[]) {
            for (final char item : (char[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof byte[]) {
            for (final byte item : (byte[]) array) {
                consumer.accept(item);
            }
        } else if (array instanceof short[]) {
            for (final short item : (short[]) array) {
                consumer.accept(item);
            }
        }
    }

    private static Object getFirstFromArray(final Object value) {
        final AtomicBoolean isFirst = new AtomicBoolean(true);
        final AtomicReference<Object> result = new AtomicReference<>(null);
        iterateOverArray(value, item -> {
            if (isFirst.get()) {
                isFirst.set(false);
                result.set(item);
            }
        });
        return result.get();
    }

    private static <T extends Collection<E>, E> T collectionOf(final Collection<?> input, final Supplier<T> output, final Class<E> itemType) {
        final T result = output.get();
        input.stream().map(item -> convertObj(item, itemType)).filter(Objects::nonNull).forEach(result::add);
        return result;
    }

    private TypeConverter() {
        // static util class
    }
}
