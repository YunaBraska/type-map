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
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.Type.typeOf;
import static berlin.yuna.typemap.model.TypeMap.convertAndMap;
import static berlin.yuna.typemap.model.TypeMap.treeGet;

/**
 * Hold common methods between {@link TypeMapI} and {@link TypeListI}
 *
 * @param <C> {@link TypeMapI} or {@link TypeListI}
 */
@SuppressWarnings({"unused", "java:S1452"})
public interface TypeInfo<C extends TypeInfo<C>> {

    /**
     * Adds the specified value
     *
     * @param key or index the index whose associated value is to be returned.
     * @return the updated {@link TypeListI} instance for chaining.
     */
    C addR(final Object key, final Object value);

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
    default Type<String> asStringOpt(final Object... path) {
        return typeOf(asString(path));
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty.
     */
    default Long asLong(final Object... path) {
        return get(Long.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty.
     */
    default Type<Long> asLongOpt(final Object... path) {
        return typeOf(asLong(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Integer> asIntOpt(final Object... path) {
        return typeOf(asInt(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Double> asDoubleOpt(final Object... path) {
        return typeOf(asDouble(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Float> asFloatOpt(final Object... path) {
        return typeOf(asFloat(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Short> asShortOpt(final Object... path) {
        return typeOf(asShort(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Byte> asByteOpt(final Object... path) {
        return typeOf(asByte(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<BigInteger> asBigIntegerOpt(final Object... path) {
        return typeOf(asBigInteger(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<BigDecimal> asBigDecimalOpt(final Object... path) {
        return typeOf(asBigDecimal(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Number> asNumberOpt(final Object... path) {
        return typeOf(asNumber(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<AtomicInteger> asAtomicIntegerOpt(final Object... path) {
        return typeOf(asAtomicInteger(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<AtomicLong> asAtomicLongOpt(final Object... path) {
        return typeOf(asAtomicLong(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<AtomicBoolean> asAtomicBooleanOpt(final Object... path) {
        return typeOf(asAtomicBoolean(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<UUID> asUUIDOpt(final Object... path) {
        return typeOf(asUUID(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Character> asCharacterOpt(final Object... path) {
        return typeOf(asCharacter(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Boolean> asBooleanOpt(final Object... path) {
        return typeOf(asBoolean(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Throwable> asThrowableOpt(final Object... path) {
        return typeOf(asThrowable(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Charset> asCharsetOpt(final Object... path) {
        return typeOf(asCharset(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<File> asFileOpt(final Object... path) {
        return typeOf(asFile(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Path> asPathOpt(final Object... path) {
        return typeOf(asPath(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<URI> asURIOpt(final Object... path) {
        return typeOf(asURI(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<URL> asURLOpt(final Object... path) {
        return typeOf(asURL(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<InetAddress> asInetAddressOpt(final Object... path) {
        return typeOf(asInetAddress(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Inet4Address> asInet4AddressOpt(final Object... path) {
        return typeOf(asInet4Address(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Inet6Address> asInet6AddressOpt(final Object... path) {
        return typeOf(asInet6Address(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Date> asDateOpt(final Object... path) {
        return typeOf(asDate(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Instant> asInstantOpt(final Object... path) {
        return typeOf(asInstant(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Calendar> asCalendarOpt(final Object... path) {
        return typeOf(asCalendar(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<LocalDateTime> asLocalDateTimeOpt(final Object... path) {
        return typeOf(asLocalDateTime(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<LocalDate> asLocalDateOpt(final Object... path) {
        return typeOf(asLocalDate(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<LocalTime> asLocalTimeOpt(final Object... path) {
        return typeOf(asLocalTime(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<OffsetDateTime> asOffsetDateTimeOpt(final Object... path) {
        return typeOf(asOffsetDateTime(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<ZonedDateTime> asZonedDateTimeOpt(final Object... path) {
        return typeOf(asZonedDateTime(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<java.sql.Date> asSqlDateOpt(final Object... path) {
        return typeOf(asSqlDate(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Time> asTimeOpt(final Object... path) {
        return typeOf(asTime(path));
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
     * @return the value if present and convertible, else empty.
     */
    default Type<Timestamp> asTimestampOpt(final Object... path) {
        return typeOf(asTimestamp(path));
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<String> asStrings(final Object... path) {
        return asList(String.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Long> asLongs(final Object... path) {
        return asList(Long.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Integer> asInts(final Object... path) {
        return asList(Integer.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Double> asDoubles(final Object... path) {
        return asList(Double.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Float> asFloats(final Object... path) {
        return asList(Float.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Short> asShorts(final Object... path) {
        return asList(Short.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Byte> asBytes(final Object... path) {
        return asList(Byte.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Number> asNumbers(final Object... path) {
        return asList(Number.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<UUID> asUUIDs(final Object... path) {
        return asList(UUID.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Character> asCharacters(final Object... path) {
        return asList(Character.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Boolean> asBooleans(final Object... path) {
        return asList(Boolean.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Throwable> asThrowables(final Object... path) {
        return asList(Throwable.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Charset> asCharsets(final Object... path) {
        return asList(Charset.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<File> asFiles(final Object... path) {
        return asList(File.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Path> asPaths(final Object... path) {
        return asList(Path.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<URI> asURIs(final Object... path) {
        return asList(URI.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<URL> asURLs(final Object... path) {
        return asList(URL.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<InetAddress> asInetAddresses(final Object... path) {
        return asList(InetAddress.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Inet4Address> asInet4Addresses(final Object... path) {
        return asList(Inet4Address.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Inet6Address> asInet6Addresses(final Object... path) {
        return asList(Inet6Address.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Date> asDates(final Object... path) {
        return asList(Date.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Instant> asInstants(final Object... path) {
        return asList(Instant.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Calendar> asCalendars(final Object... path) {
        return asList(Calendar.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<LocalDateTime> asLocalDateTimes(final Object... path) {
        return asList(LocalDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<LocalDate> asLocalDates(final Object... path) {
        return asList(LocalDate.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<LocalTime> asLocalTimes(final Object... path) {
        return asList(LocalTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<OffsetDateTime> asOffsetDateTimes(final Object... path) {
        return asList(OffsetDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<ZonedDateTime> asZonedDateTimes(final Object... path) {
        return asList(ZonedDateTime.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<java.sql.Date> asSqlDates(final Object... path) {
        return asList(java.sql.Date.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Time> asTimes(final Object... path) {
        return asList(Time.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped
     *
     * @param path the key whose associated value is to be returned.
     * @return the value if present and convertible, else empty list.
     */
    default List<Timestamp> asTimestamps(final Object... path) {
        return asList(Timestamp.class, path);
    }

    /**
     * Retrieves the value to which the specified key is mapped, and attempts to
     * convert it to the specified type.
     *
     * @param <R>  The target type for conversion.
     * @param path the key whose associated value is to be returned.
     * @param type the Class object of the type to convert to.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    default <R> R as(final Class<R> type, final Object... path) {
        return asOpt(type, path).orElse(null);
    }

    /**
     * Fluent type-check if the current {@link TypeInfo} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    Type<TypeMapI<?>> typeMapOpt();

    /**
     * Fluent type-check if the current {@link TypeInfo} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    Type<TypeListI<?>> typeListOpt();

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
        return asOpt(type, path).orElse(null);
    }

    /**
     * Retrieves the value to which the specified key is mapped, and attempts to
     * convert it to the specified type.
     *
     * @param path the key whose associated value is to be returned.
     * @return an Optional containing the value if present and convertible, else empty.
     */
    default Type<?> asOpt(final Object... path) {
        return typeOf(treeGet(this, path));
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
    default <T> Type<T> asOpt(final Class<T> type, final Object... path) {
        return asOpt(path).map(object -> convertObj(object, type));
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
        return asList(ArrayList::new, componentType, path).toArray(generator);
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

    /**
     * Adds a value at a specified path. If the path refers to a collection, the value is appended.
     * This method may throw an exception if the target collection is immutable or has fixed types.
     * It is recommended to use TypeLists or TypeSets instead of other collections when adding collections.
     *
     * @param pathAndValue An array where the path elements precede the value to add.
     * @return The current instance for method chaining.
     */
    default TypeInfo<C> addPathR(final Object... pathAndValue) {
        addPath(pathAndValue);
        return this;
    }

    /**
     * Sets a value at a specified path, replacing any existing value at that position.
     * This method may throw an exception if the target collection is immutable or has fixed types.
     * It is recommended to use TypeLists or TypeSets instead of other collections when adding collections.
     *
     * @param pathAndValue An array where the path elements precede the value to set.
     * @return The current instance for method chaining.
     */
    default TypeInfo<C> setPathR(final Object... pathAndValue) {
        setPath(pathAndValue);
        return this;

    }

    /**
     * Inserts or updates a value at a specified path, where the last path element is treated as a key.
     * This method may throw an exception if the target map is immutable or has fixed types.
     * It is recommended to use TypeMaps when adding maps.
     *
     * @param pathAndValue An array where the path elements precede the key and value to insert or update.
     * @return The current instance for method chaining.
     */
    default TypeInfo<C> putPathR(final Object... pathAndValue) {
        putPath(pathAndValue);
        return this;
    }

    /**
     * Adds a value at a specified path. If the path refers to a collection, the value is appended.
     * This method may throw an exception if the target collection is immutable or has fixed types.
     * It is recommended to use TypeLists or TypeSets instead of other collections when adding collections.
     *
     * @param pathAndValue An array where the path elements precede the value to add.
     * @return {@code true} if the value was added successfully, {@code false} otherwise.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default boolean addPath(final Object... pathAndValue) {
        if (pathAndValue == null || pathAndValue.length < 1)
            return false;
        final Object list = treeGet(this, Arrays.copyOf(pathAndValue, pathAndValue.length - 1));
        if (!(list instanceof List<?>))
            return putPath(pathAndValue);

        ((List) list).add(pathAndValue[pathAndValue.length - 1]);
        return true;
    }

    /**
     * Sets a value at a specified path, replacing any existing value at that position.
     * This method may throw an exception if the target collection is immutable or has fixed types.
     * It is recommended to use TypeLists or TypeSets instead of other collections when adding collections.
     *
     * @param pathAndValue An array where the path elements precede the value to set.
     * @return {@code true} if the value was added successfully, {@code false} otherwise.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default boolean setPath(final Object... pathAndValue) {
        if (pathAndValue == null || pathAndValue.length < 2)
            return false;

        final Object list = treeGet(this, Arrays.copyOf(pathAndValue, pathAndValue.length - 2));
        if (!(list instanceof List<?>))
            return putPath(pathAndValue);

        final Object key = pathAndValue[pathAndValue.length - 2];
        final int index = !(key instanceof final Number num) ? ((List<?>) list).indexOf(key) : num.intValue();
        if (index > -1 && index < ((List<?>) list).size()) {
            ((List) list).set(index, pathAndValue[pathAndValue.length - 1]);
            return true;
        }
        return false;
    }

    /**
     * Inserts or updates a value at a specified path, where the last path element is treated as a key.
     * This method may throw an exception if the target map is immutable or has fixed types.
     * It is recommended to use TypeMaps when adding maps.
     *
     * @param pathAndValue An array where the path elements precede the key and value to insert or update.
     * @return {@code true} if the value was added successfully, {@code false} otherwise.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default boolean putPath(final Object... pathAndValue) {
        if (pathAndValue == null || pathAndValue.length < 2)
            return false;
        final boolean isEntry = pathAndValue[pathAndValue.length - 1] instanceof Map.Entry;
        final Object obj = treeGet(this, Arrays.copyOf(pathAndValue, pathAndValue.length - (isEntry ? 1 : 2)));
        if (obj instanceof final Map map) {
            map.put(
                isEntry ? ((Map.Entry<?, ?>) pathAndValue[pathAndValue.length - 1]).getKey() : pathAndValue[pathAndValue.length - 2],
                isEntry ? ((Map.Entry<?, ?>) pathAndValue[pathAndValue.length - 1]).getValue() : pathAndValue[pathAndValue.length - 1]
            );
            return true;
        }
        return false;
    }

    /**
     * Checks if a specified path contains a given value or key.
     *
     * @param pathAndValue An array where the path elements precede the value or key to check.
     * @return {@code true} if the path contains the value or key, {@code false} otherwise.
     */
    default boolean containsPath(final Object... pathAndValue) {
        if (pathAndValue == null || pathAndValue.length < 1)
            return false;
        final Object obj = treeGet(this, Arrays.copyOf(pathAndValue, pathAndValue.length - 1));
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).containsKey(pathAndValue[pathAndValue.length - 1]);
        } else if (obj instanceof List) {
            return ((Collection<?>) obj).contains(pathAndValue[pathAndValue.length - 1]);
        }
        return false;
    }

    /**
     * If a value is present, returns {@code true}, otherwise {@code false}.
     *
     * @param path The key whose associated value
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    default boolean isPresent(final Object... path) {
        return !isEmpty(path);
    }

    /**
     * If a value is  not present, returns {@code true}, otherwise
     * {@code false}.
     *
     * @param path The key whose associated value
     * @return {@code true} if a value is not present, otherwise {@code false}
     */
    default boolean isEmpty(final Object... path) {
        return treeGet(this, path) == null;
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise does nothing.
     *
     * @param action the action to be performed, if a value is present
     * @param path   The key whose associated value
     * @return The current instance for method chaining.
     */
    default TypeInfo<C> ifPresent(final Consumer<? super Type<?>> action, final Object... path) {
        ifPresentOrElse(action, null, path);
        return this;
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action the action to be performed, if a value is present
     * @param orElse the empty-based action to be performed, if no value is present
     * @param path   The key whose associated value
     * @return The current instance for method chaining.
     */
    default TypeInfo<C> ifPresentOrElse(final Consumer<? super Type<?>> action, final Runnable orElse, final Object... path) {
        final Object value = treeGet(this, path);
        if (value != null) {
            if(action != null)
                action.accept(new Type<>(value));
        } else if (orElse != null) {
            orElse.run();
        }
        return this;
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@link Type} describing the value, otherwise returns an
     * empty {@link Type}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @param path      The key whose associated value
     * @return an {@link Type} describing the value of this
     * {@link Type}, if a value is present and the value matches the
     * given predicate, otherwise an empty {@link Type}
     */
    default Type<?> filter(final Predicate<Type<?>> predicate, final Object... path) {
        final Object value = treeGet(this, path);
        return (value != null && predicate != null && predicate.test(new Type<>(value))) ? new Type<>(value) : new Type<>(null);
    }

    /**
     * If a value is present, returns an {@link Type} the result of applying the given mapping function to
     * the value, otherwise returns an empty {@link Type}.
     *
     * <p>If the mapping function returns a {@code null} result then this method
     * returns an empty {@link Type}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @param path   The key whose associated value
     * @return an {@link Type} describing the result of applying a mapping
     */
    default <R> Type<R> map(final Function<Type<?>, ? extends R> mapper, final Object... path) {
        final Object value = treeGet(this, path);
        return (value != null && mapper != null) ? new Type<>(mapper.apply(new Type<>(value))) : new Type<>(null);
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@link Type}-bearing mapping function to the value, otherwise returns
     * an empty {@link Type}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @param path   The key whose associated value
     * @return the result of applying an {@link Type}-bearing mapping
     * function to the value of this {@link Type}, if a value is
     * present, otherwise an empty {@link Type}
     */
    default <R> Type<R> flatMap(final Function<Type<?>, ? extends Type<? extends R>> mapper, final Object... path) {
        final Object value = treeGet(this, path);
        if (value != null && mapper != null) {
            @SuppressWarnings("unchecked") final Type<R> result = (Type<R>) mapper.apply(new Type<>(value));
            return result != null ? result : new Type<>(null);
        } else {
            return new Type<>(null);
        }
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@link Type}-bearing mapping function to the value, otherwise returns
     * an empty {@link Type}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @param path   The key whose associated value
     * @return the result of applying an {@link Type}-bearing mapping
     * function to the value of this {@link Type}, if a value is
     * present, otherwise an empty {@link Type}
     */
    @SuppressWarnings({"unchecked", "java:S2789", "java:S4968"})
    default <R> Type<R> flatOpt(final Function<Type<?>, ? extends Optional<? extends R>> mapper, final Object... path) {
        final Object value = treeGet(this, path);
        if (value != null && mapper != null) {
            @SuppressWarnings("unchecked") final Optional<R> result = (Optional<R>) mapper.apply(new Type<>(value));
            return result != null && result.isPresent() ? new Type<>(result.orElse(null)) : new Type<>(null);
        } else {
            return new Type<>(null);
        }
    }

    /**
     * If a value is present, returns an {@link Type} describing the value,
     * otherwise returns an {@link Type} produced by the supplying function.
     * It's recommended to use {@link #asOpt(Object...)} then {@link Type#map(Function)} first and then do the {@code or} instead.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @param path     The key whose associated value
     * @return returns an {@link Type} describing the value of this
     * {@link Type}, if a value is present, otherwise an
     * {@link Type} produced by the supplying function.
     */
    @SuppressWarnings("unchecked")
    default <R> Type<R> or(final Supplier<? extends R> supplier, final Object... path) {
        final Object value = treeGet(this, path);
        return new Type<>((R) ((value != null || supplier == null) ? value : supplier.get()));
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing
     * only that value or in case of {@link Iterable} returns values.
     *
     * @param path The key whose associated value
     * @return the value(s) as a {@link Stream}
     */
    @SuppressWarnings("unchecked")
    default Stream<Type<?>> stream(final Object... path) {
        final Object value = treeGet(this, path);
        final Stream<Object> result;
        if (value != null) {
            if (value instanceof Iterable) {
                result = StreamSupport.stream(((Iterable<Object>) value).spliterator(), false);
            } else if (value.getClass().isArray()) {
                result = Arrays.stream((Object[]) value);
            } else {
                result = Stream.of(value);
            }
            return result.map(TypeInfo::toType);
        } else {
            return Stream.empty();
        }
    }

    /**
     * If a value is present, returns the value, otherwise returns {@code other}.
     *
     * @param other the value to be returned, if no value is present.  May be {@code null}.
     * @param path  The key whose associated value
     * @return the value, if present, otherwise {@code other}
     */
    default Object orElse(final Object other, final Object... path) {
        final Object value = treeGet(this, path);
        return value != null ? value : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @param path     The key whose associated value
     * @return the value, if present, otherwise the result produced by the
     * supplying function
     */
    default Object orElseGet(final Supplier<Object> supplier, final Object... path) {
        final Object value = treeGet(this, path);
        return value != null ? value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws  {@code NoSuchElementException}.
     *
     * @param path The key whose associated value
     * @return the non-{@code null} value described by this {@code Optional}
     */
    default Object orElseThrow(final Object... path) {
        return orElseThrow(() -> new NoSuchElementException("No value present [" + (path == null ? "null" : Arrays.stream(path).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(".")) + "]")), path);
    }

    /**
     * If a value is present, returns the value, otherwise throws exception
     *
     * @param exceptionSupplier the supplying function that produces an exception to be thrown
     * @param path              The key whose associated value
     * @return the non-{@code null} value described by this {@code Optional}
     */
    default <X extends Throwable> Object orElseThrow(final Supplier<? extends X> exceptionSupplier, final Object... path) throws X {
        final Object value = treeGet(this, path);
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    static Type<?> toType(final Object o) {
        if (o instanceof Type) {
            return (Type<?>) o;
        } else if (o instanceof Optional) {
            return new Type<>(((Optional<?>) o).orElse(null));
        } else {
            return new Type<>(o);
        }
    }
}
