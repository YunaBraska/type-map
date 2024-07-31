package berlin.yuna.typemap.model;

import berlin.yuna.typemap.logic.JsonEncoder;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.TypeMap.convertAndMap;
import static berlin.yuna.typemap.model.TypeMap.treeGet;
import static java.util.Optional.ofNullable;

/**
 * Hold common methods between {@link TypeMapI} and {@link TypeListI}
 *
 * @param <C> {@link TypeMapI} or {@link TypeListI}
 */
@SuppressWarnings("unused")
public interface TypeInfo<C extends TypeInfo<C>> {

    /**
     * Adds the specified value
     *
     * @param key or index the index whose associated value is to be returned.
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C addReturn(final Object key, final Object value);

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default String asString(final Object... path) {
        return get(String.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Long asLong(final Object... path) {
        return get(Long.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Integer asInt(final Object... path) {
        return get(Integer.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Double asDouble(final Object... path) {
        return get(Double.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Float asFloat(final Object... path) {
        return get(Float.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Short asShort(final Object... path) {
        return get(Short.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Byte asByte(final Object... path) {
        return get(Byte.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default BigInteger asBigInteger(final Object... path) {
        return get(BigInteger.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default BigDecimal asBigDecimal(final Object... path) {
        return get(BigDecimal.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Number asNumber(final Object... path) {
        return get(Number.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default AtomicInteger asAtomicInteger(final Object... path) {
        return get(AtomicInteger.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default AtomicLong asAtomicLong(final Object... path) {
        return get(AtomicLong.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default AtomicBoolean asAtomicBoolean(final Object... path) {
        return get(AtomicBoolean.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default UUID asUUID(final Object... path) {
        return get(UUID.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Character asCharacter(final Object... path) {
        return get(Character.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Boolean asBoolean(final Object... path) {
        return get(Boolean.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Throwable asThrowable(final Object... path) {
        return get(Throwable.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Charset asCharset(final Object... path) {
        return get(Charset.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default File asFile(final Object... path) {
        return get(File.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Path asPath(final Object... path) {
        return get(Path.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default URI asURI(final Object... path) {
        return get(URI.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default URL asURL(final Object... path) {
        return get(URL.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default InetAddress asInetAddress(final Object... path) {
        return get(InetAddress.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Inet4Address asInet4Address(final Object... path) {
        return get(Inet4Address.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Inet6Address asInet6Address(final Object... path) {
        return get(Inet6Address.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Date asDate(final Object... path) {
        return get(Date.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Instant asInstant(final Object... path) {
        return get(Instant.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Calendar asCalendar(final Object... path) {
        return get(Calendar.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default LocalDateTime asLocalDateTime(final Object... path) {
        return get(LocalDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default LocalDate asLocalDate(final Object... path) {
        return get(LocalDate.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default LocalTime asLocalTime(final Object... path) {
        return get(LocalTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default OffsetDateTime asOffsetDateTime(final Object... path) {
        return get(OffsetDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default ZonedDateTime asZonedDateTime(final Object... path) {
        return get(ZonedDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default java.sql.Date asSqlDate(final Object... path) {
        return get(java.sql.Date.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Time asTime(final Object... path) {
        return get(Time.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default Timestamp asTimestamp(final Object... path) {
        return get(Timestamp.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<String> asStrings(final Object... path) {
        return asList(String.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Long> asLongs(final Object... path) {
        return asList(Long.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Integer> asInts(final Object... path) {
        return asList(Integer.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Double> asDoubles(final Object... path) {
        return asList(Double.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Float> asFloats(final Object... path) {
        return asList(Float.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Short> asShorts(final Object... path) {
        return asList(Short.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Byte> asBytes(final Object... path) {
        return asList(Byte.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Number> asNumbers(final Object... path) {
        return asList(Number.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<UUID> asUUIDs(final Object... path) {
        return asList(UUID.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Character> asCharacters(final Object... path) {
        return asList(Character.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Boolean> asBooleans(final Object... path) {
        return asList(Boolean.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Throwable> asThrowables(final Object... path) {
        return asList(Throwable.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Charset> asCharsets(final Object... path) {
        return asList(Charset.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<File> asFiles(final Object... path) {
        return asList(File.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Path> asPaths(final Object... path) {
        return asList(Path.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<URI> asURIs(final Object... path) {
        return asList(URI.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<URL> asURLs(final Object... path) {
        return asList(URL.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<InetAddress> asInetAddresses(final Object... path) {
        return asList(InetAddress.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Inet4Address> asInet4Addresses(final Object... path) {
        return asList(Inet4Address.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Inet6Address> asInet6Addresses(final Object... path) {
        return asList(Inet6Address.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Date> asDates(final Object... path) {
        return asList(Date.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Instant> asInstants(final Object... path) {
        return asList(Instant.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Calendar> asCalendars(final Object... path) {
        return asList(Calendar.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<LocalDateTime> asLocalDateTimes(final Object... path) {
        return asList(LocalDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<LocalDate> asLocalDates(final Object... path) {
        return asList(LocalDate.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<LocalTime> asLocalTimes(final Object... path) {
        return asList(LocalTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<OffsetDateTime> asOffsetDateTimes(final Object... path) {
        return asList(OffsetDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<ZonedDateTime> asZonedDateTimes(final Object... path) {
        return asList(ZonedDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<java.sql.Date> asSqlDates(final Object... path) {
        return asList(java.sql.Date.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Time> asTimes(final Object... path) {
        return asList(Time.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else null.
     */
    default List<Timestamp> asTimestamps(final Object... path) {
        return asList(Timestamp.class, path);
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
    default <T> T as(final Class<T> type, final Object... path) {
        return getOpt(type, path).orElse(null);
    }

    /**
     * Fluent type-check if the current {@link TypeInfo} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    Optional<TypeMapI<?>> typeMapOpt();

    /**
     * Fluent type-check if the current {@link TypeInfo} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    Optional<TypeListI<?>> typeListOpt();


    /**
     * Retrieves the value to which the specified key is mapped, and attempts to
     * convert it to the specified type.
     *
     * @param <T>  The target type for conversion.
     * @param path the key whose associated value is to be returned.
     * @param type the Class object of the type to convert to.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    default <T> T get(final Class<T> type, final Object... path) {
        return getOpt(type, path).orElse(null);
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
    default <T> Optional<T> getOpt(final Class<T> type, final Object... path) {
        return ofNullable(treeGet(this, path)).map(object -> convertObj(object, type));
    }

    /**
     * This method converts the retrieved map to a list of the specified key and value types.
     *
     * @param path The key whose associated value is to be returned.
     * @return a {@link TypeList} of the specified key and value types.
     */
    default TypeList asList(final Object... path) {
        return asList(TypeList::new, Object.class, path);
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
    default <E> List<E> asList(final Class<E> itemType, final Object... path) {
        return asList(ArrayList::new, itemType, path);
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
    default <T extends Collection<E>, E> T asList(final Supplier<? extends T> output, final Class<E> itemType, final Object... path) {
        return collectionOf(treeGet(this, path), output, itemType);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param path The key whose associated value is to be returned.
     * @return a map of the specified key and value types.
     */
    default LinkedTypeMap asMap(final Object... path) {
        return asMap(LinkedTypeMap::new, Object.class, Object.class, path);
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
    default <K, V> Map<K, V> asMap(final Class<K> keyType, final Class<V> valueType, final Object... path) {
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
    default <K, V, M extends Map<K, V>> M asMap(final Supplier<M> output, final Class<K> keyType, final Class<V> valueType, final Object... path) {
        return convertAndMap(treeGet(this, path), output, keyType, valueType);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>         The type of keys in the returned map.
     * @param <V>         The type of values in the returned map.
     * @param path        The key whose associated value is to be returned.
     * @param keyType     The class of the map's key type.
     * @param valueMapper A function that maps the input values to the desired value type.
     * @return a map of the specified key and value types.
     */
    default <K, V> Map<K, V> asMap(final Class<K> keyType, final Function<Object, V> valueMapper, final Object... path) {
        return asMap(key -> convertObj(key, keyType), valueMapper, path);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>         The type of keys in the returned map.
     * @param <V>         The type of values in the returned map.
     * @param path        The key whose associated value is to be returned.
     * @param keyMapper   A function that maps the input keys to the desired key type.
     * @param valueMapper A function that maps the input values to the desired value type.
     * @return a map of the specified key and value types.
     */
    default <K, V> Map<K, V> asMap(final Function<Object, K> keyMapper, final Function<Object, V> valueMapper, final Object... path) {
        return asMap(LinkedHashMap::new, keyMapper, valueMapper, path);
    }

    /**
     * Retrieves a map of a specific type associated with the specified key.
     * This method converts the retrieved map to a map of the specified key and value types.
     *
     * @param <K>         The type of keys in the returned map.
     * @param <V>         The type of values in the returned map.
     * @param path        The key whose associated value is to be returned.
     * @param output      A supplier providing a new map instance.
     * @param keyMapper   A function that maps the input keys to the desired key type.
     * @param valueMapper A function that maps the input values to the desired value type.
     * @return a map of the specified key and value types.
     */
    default <K, V, M extends Map<K, V>> M asMap(final Supplier<M> output, final Function<Object, K> keyMapper, final Function<Object, V> valueMapper, final Object... path) {
        return convertAndMap(treeGet(this, path), output, keyMapper, valueMapper);
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
    default <E> E[] asArray(final E[] typeIndicator, final Class<E> componentType, final Object... path) {
        final ArrayList<E> result = asList(ArrayList::new, componentType, path);
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
    default <E> E[] asArray(final IntFunction<E[]> generator, final Class<E> componentType, final Object... path) {
        return asList(ArrayList::new, componentType, path).stream().toArray(generator);
    }

    /**
     * Converts any object to its JSON representation.
     * This method intelligently dispatches the conversion task based on the type of the object,
     * handling Maps, Collections, Arrays (both primitive and object types), and other objects.
     *
     * @param path The key whose associated value is to be returned.
     * @return A JSON representation of the key value as a String.
     */
    default String toJson(final Object... path) {
        return JsonEncoder.toJson(treeGet(this, path));
    }

    /**
     * Converts any object to its JSON representation.
     * This method intelligently dispatches the conversion task based on the type of the object,
     * handling Maps, Collections, Arrays (both primitive and object types), and other objects.
     *
     * @return A JSON representation of itself as a String.
     */
    default String toJson() {
        return JsonEncoder.toJson(this);
    }
}
