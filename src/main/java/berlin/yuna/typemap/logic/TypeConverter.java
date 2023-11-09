package berlin.yuna.typemap.logic;


import berlin.yuna.typemap.model.FunctionOrNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static berlin.yuna.typemap.config.TypeConversionRegister.TYPE_CONVERSIONS;
import static java.util.Optional.ofNullable;

@SuppressWarnings({"UnusedReturnValue", "java:S6548", "unused"})
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
            return safeCast(value, targetType);
        }

        // Handle non-empty arrays, collections, map
        final Object firstValue = getFirstItem(value);
        if (firstValue != null) {
            return convertObj(firstValue, targetType);
        }

        // Enums
        if (targetType.isEnum()) {
            return (T) enumOf(String.valueOf(value), (Class<Enum>) targetType);
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
        return ofNullable(input).map(nonNull -> keyType).map(nonNull -> valueType).map(nonNull -> output).map(Supplier::get).map(result -> {
            input.forEach((key, value) -> {
                final K convertedKey = convertObj(key, keyType);
                final V convertedValue = convertObj(value, valueType);
                if (convertedKey != null && convertedValue != null) {
                    result.put(convertedKey, convertedValue);
                }
            });
            return result;
        }).orElse(ofNullable(output).map(Supplier::get).orElse(null));
    }

    /**
     * Creates a new collection of typeId {@code T} from the given {@code input} collection.
     * The elements are converted to typeId {@code E} and added to the resulting collection.
     *
     * @param <T>      The typeId of the resulting collection, which must be a subclass of {@code Collection<E>}.
     * @param <E>      The typeId of elements in the resulting collection.
     * @param input    The input collection containing elements to be converted.
     * @param output   A {@code Supplier} for the output collection of typeId {@code T}.
     * @param itemType The {@code Class} object representing the typeId of elements to convert to.
     * @return A new collection of typeId {@code T} with elements converted to typeId {@code E}.
     * Returns {@code null} if the output collection is {@code null}.
     */
    public static <T extends Collection<E>, E> T collectionOf(final Collection<?> input, final Supplier<T> output, final Class<E> itemType) {
        return ofNullable(input).map(nonNull -> itemType).map(nonNull -> output).map(Supplier::get).map(result -> {
            input.stream()
                .map(item -> convertObj(item, itemType))
                .filter(Objects::nonNull)
                .forEach(result::add);
            return result;
        }).orElseGet(() -> ofNullable(output).map(Supplier::get).orElse(null));
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
        if (input != null && output != null && itemType != null) {
            if (input instanceof Collection<?>) {
                final Collection<?> noTypeCollection = (Collection<?>) input;
                return collectionOf(noTypeCollection, output, itemType);
            } else if (input.getClass().isArray() && input instanceof Object[]) {
                final Collection<Object> noTypeCollection = new ArrayList<>();
                for (final Object item : (Object[]) input) {
                    noTypeCollection.add(convertObj(item, itemType));
                }
                return collectionOf(noTypeCollection, output, itemType);
            } else {
                final T result = output.get();
                ofNullable(convertObj(input, itemType)).ifPresent(result::add);
                return result;
            }
        }
        return ofNullable(output).map(Supplier::get).orElse(null);
    }

    public <E> E[] arrayOf(final Object object, final E[] typeIndicator, final Class<E> componentType) {
        ArrayList<E> result = collectionOf(object, ArrayList::new, componentType);
        result = result == null ? new ArrayList<>() : result;
        return result.toArray(Arrays.copyOf(typeIndicator, result.size()));
    }

    public <E> E[] arrayOf(final Object object, final IntFunction<E[]> generator, final Class<E> componentType) {
        ArrayList<E> result = collectionOf(object, ArrayList::new, componentType);
        result = result == null ? new ArrayList<>() : result;
        return result.stream().toArray(generator);
    }

    public <E> E[] arrayOf(final Object[] array, final E[] typeIndicator, final Class<E> componentType) {
        return arrayOf(Arrays.stream(array).collect(Collectors.toList()), typeIndicator, componentType);
    }

    public <E> E[] arrayOf(final Object[] array, final IntFunction<E[]> generator, final Class<E> componentType) {
        return arrayOf(Arrays.stream(array).collect(Collectors.toList()), generator, componentType);
    }

    public static <T extends Enum<T>> T enumOf(final String value, final Class<T> enumType) {
        try {
            return value != null && enumType != null && enumType.isEnum() ? Enum.valueOf(enumType, value) : null;
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    protected static <T> T safeCast(final Object value, final Class<T> targetType) {
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }
        throw new ClassCastException("Failed to cast " + value + " to " + targetType.getName());
    }

    private static Object getFirstItem(final Object value) {
        if (value instanceof Collection<?>) {
            final Collection<?> collection = (Collection<?>) value;
            return collection.isEmpty() ? null : collection.iterator().next();
        } else if (value.getClass().isArray() && value instanceof Object[]) {
            final Object[] array = (Object[]) value;
            return array.length == 0 ? null : array[0];
        } else if (value instanceof Map<?, ?>) {
            final Map<?, ?> map = (Map<?, ?>) value;
            return map.isEmpty() ? null : map.entrySet().iterator().next();
        }
        return null;
    }

    private TypeConverter() {
        // static util class
    }

}
