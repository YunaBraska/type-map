package berlin.yuna.typemap.config;


import berlin.yuna.typemap.model.FunctionOrNull;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Manages type conversions between different classes.
 * This class acts as a registry for conversion functions that can convert
 * between different data types. The class supports registering and applying
 * conversion functions to transform data from a source type to a target type.
 */
public class TypeConversionRegister {

    /**
     * A map where the key is the target type and the value is another map.
     * The nested map's key is the source type, and the value is the conversion
     * function that can convert from the source type to the target type.
     */
    @SuppressWarnings({"rawtypes", "java:S2386"})
    public static final Map<Class<?>, Map<Class<?>, FunctionOrNull>> TYPE_CONVERSIONS = new ConcurrentHashMap<>();

    /**
     * Registers a conversion function that can convert an object of the source type
     * to the target type.
     *
     * @param <S>        The source type.
     * @param <T>        The target type.
     * @param sourceType The class of the source type.
     * @param targetType The class of the target type.
     * @param conversion The function that will perform the conversion.
     */
    public static <S, T> void registerTypeConvert(final Class<S> sourceType, final Class<T> targetType, final FunctionOrNull<S, T> conversion) {
        TYPE_CONVERSIONS.computeIfAbsent(targetType, k -> new HashMap<>()).put(sourceType, conversion);
    }

    /**
     * An array of {@link DateTimeFormatter}s that can be used to parse and format dates.
     * The formatters are listed in a specific order to handle the most complex formats first,
     * eventually falling back to simpler formats. This order ensures the highest accuracy when
     * parsing date strings that may be in various formats.
     */
    @SuppressWarnings("java:S2386")
    public static final DateTimeFormatter[] DATE_TIME_FORMATTERS = {
        DateTimeFormatter.ISO_ZONED_DATE_TIME,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ISO_ORDINAL_DATE,
        DateTimeFormatter.RFC_1123_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_OFFSET_DATE,
        DateTimeFormatter.ISO_LOCAL_TIME,
        DateTimeFormatter.ISO_OFFSET_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.BASIC_ISO_DATE,
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ISO_INSTANT,
        DateTimeFormatter.ISO_DATE,
        DateTimeFormatter.ISO_TIME,
        DateTimeFormatter.ISO_WEEK_DATE,
        // String.valueOf(new Date())
        new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("EEE MMM dd HH:mm:ss ").appendZoneText(TextStyle.SHORT).appendPattern(" yyyy").toFormatter(Locale.ENGLISH),
    };

    public static final String LINE_SEPARATOR = System.lineSeparator();

    static {
        // NUMBERS
        registerTypeConvert(Number.class, Long.class, Number::longValue);
        registerTypeConvert(Number.class, Integer.class, Number::intValue);
        registerTypeConvert(Number.class, Float.class, Number::floatValue);
        registerTypeConvert(Number.class, Double.class, Number::doubleValue);
        registerTypeConvert(Number.class, Short.class, Number::shortValue);
        registerTypeConvert(Number.class, Byte.class, Number::byteValue);
        registerTypeConvert(Number.class, BigInteger.class, number -> BigInteger.valueOf(number.longValue()));
        registerTypeConvert(Number.class, BigDecimal.class, number -> BigDecimal.valueOf(number.doubleValue()));
        registerTypeConvert(Number.class, Number.class, number -> number);
        registerTypeConvert(Number.class, Boolean.class, number -> number.intValue() == 1);

        // ATOMIC TYPES
        registerTypeConvert(AtomicInteger.class, Integer.class, AtomicInteger::get);
        registerTypeConvert(Integer.class, AtomicInteger.class, AtomicInteger::new);
        registerTypeConvert(AtomicLong.class, Long.class, AtomicLong::get);
        registerTypeConvert(Long.class, AtomicLong.class, AtomicLong::new);
        registerTypeConvert(AtomicBoolean.class, Boolean.class, AtomicBoolean::get);
        registerTypeConvert(Boolean.class, AtomicBoolean.class, AtomicBoolean::new);

        // PRIMITIVES TO WRAPPERS
        registerTypeConvert(int.class, Integer.class, Integer::valueOf);
        registerTypeConvert(long.class, Long.class, Long::valueOf);
        registerTypeConvert(short.class, Short.class, Short::valueOf);
        registerTypeConvert(byte.class, Byte.class, Byte::valueOf);
        registerTypeConvert(float.class, Float.class, Float::valueOf);
        registerTypeConvert(double.class, Double.class, Double::valueOf);
        registerTypeConvert(char.class, Character.class, Character::valueOf);
        registerTypeConvert(boolean.class, Boolean.class, Boolean::valueOf);
        registerTypeConvert(Integer.class, int.class, Integer::intValue);
        registerTypeConvert(Long.class, long.class, Long::longValue);
        registerTypeConvert(Short.class, short.class, Short::shortValue);
        registerTypeConvert(Byte.class, byte.class, Byte::byteValue);
        registerTypeConvert(Float.class, float.class, Float::floatValue);
        registerTypeConvert(Double.class, double.class, Double::doubleValue);
        registerTypeConvert(Character.class, char.class, Character::charValue);
        registerTypeConvert(Boolean.class, boolean.class, Boolean::booleanValue);

        // STRINGS
        registerTypeConvert(String.class, Character.class, string -> string.charAt(0));
        registerTypeConvert(Character.class, String.class, Object::toString);
        registerTypeConvert(UUID.class, String.class, UUID::toString);
        registerTypeConvert(String.class, UUID.class, UUID::fromString);
        registerTypeConvert(String.class, Boolean.class, Boolean::valueOf);
        registerTypeConvert(Enum.class, String.class, Enum::name);
        registerTypeConvert(Throwable.class, String.class, TypeConversionRegister::stringOf);
        registerTypeConvert(String.class, Long.class, Long::valueOf);
        registerTypeConvert(String.class, Integer.class, Integer::valueOf);
        registerTypeConvert(String.class, Float.class, Float::valueOf);
        registerTypeConvert(String.class, Double.class, Double::valueOf);
        registerTypeConvert(String.class, Short.class, Short::valueOf);
        registerTypeConvert(String.class, Byte.class, Byte::valueOf);
        registerTypeConvert(String.class, BigInteger.class, BigInteger::new);
        registerTypeConvert(String.class, BigDecimal.class, BigDecimal::new);
        registerTypeConvert(String.class, Number.class, Double::valueOf);

        // PATH
        registerTypeConvert(Path.class, File.class, Path::toFile);
        registerTypeConvert(Path.class, URI.class, Path::toUri);
        registerTypeConvert(Path.class, URL.class, path -> path.toUri().toURL());
        registerTypeConvert(Path.class, String.class, Path::toString);
        registerTypeConvert(File.class, Path.class, File::toPath);
        registerTypeConvert(File.class, URI.class, File::toURI);
        registerTypeConvert(File.class, URL.class, file -> file.toURI().toURL());
        registerTypeConvert(File.class, String.class, File::toString);
        registerTypeConvert(URI.class, File.class, File::new);
        registerTypeConvert(URI.class, Path.class, Paths::get);
        registerTypeConvert(URI.class, String.class, URI::toString);
        registerTypeConvert(URI.class, URL.class, URI::toURL);
        registerTypeConvert(URL.class, File.class, url -> new File(url.toURI()));
        registerTypeConvert(URL.class, Path.class, url -> Paths.get(url.toURI()));
        registerTypeConvert(URL.class, String.class, URL::toString);
        registerTypeConvert(URL.class, URI.class, URL::toURI);
        registerTypeConvert(String.class, Path.class, Paths::get);
        registerTypeConvert(String.class, File.class, File::new);
        registerTypeConvert(String.class, URL.class, string -> new URI(string).toURL());
        registerTypeConvert(String.class, URI.class, URI::new);

        // NET
        registerTypeConvert(StringBuilder.class, String.class, StringBuilder::toString);
        registerTypeConvert(String.class, StringBuilder.class, StringBuilder::new);
        registerTypeConvert(InetAddress.class, String.class, InetAddress::toString);
        registerTypeConvert(String.class, InetAddress.class, TypeConversionRegister::inetAddressOf);

        // DATE
        registerTypeConvert(Date.class, Instant.class, Date::toInstant);
        registerTypeConvert(Date.class, Calendar.class, TypeConversionRegister::calendarOf);
        registerTypeConvert(Date.class, LocalDateTime.class, date -> LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        registerTypeConvert(Date.class, LocalDate.class, date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        registerTypeConvert(Date.class, LocalTime.class, date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
        registerTypeConvert(Date.class, OffsetDateTime.class, date -> date.toInstant().atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now())));
        registerTypeConvert(Date.class, ZonedDateTime.class, date -> date.toInstant().atZone(ZoneId.systemDefault()));
        registerTypeConvert(Date.class, java.sql.Date.class, date -> new java.sql.Date(date.getTime()));
        registerTypeConvert(Date.class, Time.class, date -> new Time(date.getTime()));
        registerTypeConvert(Date.class, Timestamp.class, date -> new Timestamp(date.getTime()));

        // INSTANT
        registerTypeConvert(Instant.class, Date.class, Date::from);
        registerTypeConvert(Instant.class, Calendar.class, instant -> calendarOf(instant.toEpochMilli()));
        registerTypeConvert(Instant.class, LocalDateTime.class, instant -> instant.atZone(ZoneId.systemDefault()).toLocalDateTime());
        registerTypeConvert(Instant.class, LocalDate.class, instant -> instant.atZone(ZoneId.systemDefault()).toLocalDate());
        registerTypeConvert(Instant.class, LocalTime.class, instant -> instant.atZone(ZoneId.systemDefault()).toLocalTime());
        registerTypeConvert(Instant.class, OffsetDateTime.class, instant -> instant.atOffset(ZoneId.systemDefault().getRules().getOffset(instant)));
        registerTypeConvert(Instant.class, ZonedDateTime.class, instant -> instant.atZone(ZoneId.systemDefault()));
        registerTypeConvert(Instant.class, java.sql.Date.class, instant -> new java.sql.Date(instant.toEpochMilli()));
        registerTypeConvert(Instant.class, Time.class, instant -> new Time(instant.toEpochMilli()));
        registerTypeConvert(Instant.class, Timestamp.class, Timestamp::from);

        // CALENDAR
        registerTypeConvert(Calendar.class, Instant.class, Calendar::toInstant);
        registerTypeConvert(Calendar.class, Date.class, Calendar::getTime);
        registerTypeConvert(Calendar.class, LocalDateTime.class, calendar -> LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()));
        registerTypeConvert(Calendar.class, LocalDate.class, calendar -> LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()).toLocalDate());
        registerTypeConvert(Calendar.class, LocalTime.class, calendar -> LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()).toLocalTime());
        registerTypeConvert(Calendar.class, OffsetDateTime.class, calendar -> OffsetDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()));
        registerTypeConvert(Calendar.class, ZonedDateTime.class, calendar -> ZonedDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()));
        registerTypeConvert(Calendar.class, java.sql.Date.class, calendar -> new java.sql.Date(calendar.getTimeInMillis()));
        registerTypeConvert(Calendar.class, Time.class, calendar -> new Time(calendar.getTimeInMillis()));
        registerTypeConvert(Calendar.class, Timestamp.class, calendar -> new Timestamp(calendar.getTimeInMillis()));

        // LOCAL DATE TIME
        registerTypeConvert(LocalDateTime.class, Instant.class, ldt -> ldt.atZone(ZoneId.systemDefault()).toInstant());
        registerTypeConvert(LocalDateTime.class, Calendar.class, ldt -> calendarOf(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())));
        registerTypeConvert(LocalDateTime.class, Date.class, ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        registerTypeConvert(LocalDateTime.class, LocalDate.class, LocalDateTime::toLocalDate);
        registerTypeConvert(LocalDateTime.class, LocalTime.class, LocalDateTime::toLocalTime);
        registerTypeConvert(LocalDateTime.class, OffsetDateTime.class, ldt -> ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime());
        registerTypeConvert(LocalDateTime.class, ZonedDateTime.class, ldt -> ldt.atZone(ZoneId.systemDefault()));
        registerTypeConvert(LocalDateTime.class, java.sql.Date.class, ldt -> java.sql.Date.valueOf(ldt.toLocalDate()));
        registerTypeConvert(LocalDateTime.class, Time.class, ldt -> Time.valueOf(ldt.toLocalTime()));
        registerTypeConvert(LocalDateTime.class, Timestamp.class, Timestamp::valueOf);


        // LOCAL DATE
        registerTypeConvert(LocalDate.class, Instant.class, ld -> ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        registerTypeConvert(LocalDate.class, Calendar.class, ld -> calendarOf(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant())));
        registerTypeConvert(LocalDate.class, LocalDateTime.class, LocalDate::atStartOfDay);
        registerTypeConvert(LocalDate.class, Date.class, ld -> Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        registerTypeConvert(LocalDate.class, LocalTime.class, ld -> null);
        registerTypeConvert(LocalDate.class, OffsetDateTime.class, ld -> ld.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime());
        registerTypeConvert(LocalDate.class, ZonedDateTime.class, ld -> ld.atStartOfDay(ZoneId.systemDefault()));
        registerTypeConvert(LocalDate.class, java.sql.Date.class, java.sql.Date::valueOf);
        registerTypeConvert(LocalDate.class, Time.class, ld -> null);
        registerTypeConvert(LocalDate.class, Timestamp.class, ld -> Timestamp.valueOf(ld.atStartOfDay()));

        // LOCAL TIME
        registerTypeConvert(LocalTime.class, Instant.class, lt -> null);
        registerTypeConvert(LocalTime.class, Calendar.class, TypeConversionRegister::calendarOf);
        registerTypeConvert(LocalTime.class, LocalDateTime.class, lt -> lt.atDate(LocalDate.now()));
        registerTypeConvert(LocalTime.class, LocalDate.class, lt -> null);
        registerTypeConvert(LocalTime.class, Date.class, lt -> calendarOf(lt).getTime());
        registerTypeConvert(LocalTime.class, OffsetDateTime.class, lt -> OffsetDateTime.of(LocalDate.MIN, lt, ZoneId.systemDefault().getRules().getOffset(Instant.now())));
        registerTypeConvert(LocalTime.class, ZonedDateTime.class, lt -> ZonedDateTime.of(LocalDate.MIN, lt, ZoneId.systemDefault()));
        registerTypeConvert(LocalTime.class, java.sql.Date.class, lt -> java.sql.Date.valueOf(LocalDate.MIN));
        registerTypeConvert(LocalTime.class, Time.class, Time::valueOf);
        registerTypeConvert(LocalTime.class, Timestamp.class, lt -> new Timestamp(calendarOf(lt).getTimeInMillis()));

        // OFFSET DATE TIME
        registerTypeConvert(OffsetDateTime.class, Instant.class, OffsetDateTime::toInstant);
        registerTypeConvert(OffsetDateTime.class, Calendar.class, odt -> calendarOf(Date.from(odt.toInstant())));
        registerTypeConvert(OffsetDateTime.class, LocalDateTime.class, OffsetDateTime::toLocalDateTime);
        registerTypeConvert(OffsetDateTime.class, LocalDate.class, OffsetDateTime::toLocalDate);
        registerTypeConvert(OffsetDateTime.class, LocalTime.class, OffsetDateTime::toLocalTime);
        registerTypeConvert(OffsetDateTime.class, Date.class, odt -> Date.from(odt.toInstant()));
        registerTypeConvert(OffsetDateTime.class, ZonedDateTime.class, OffsetDateTime::toZonedDateTime);
        registerTypeConvert(OffsetDateTime.class, java.sql.Date.class, odt -> java.sql.Date.valueOf(odt.toLocalDate()));
        registerTypeConvert(OffsetDateTime.class, Time.class, odt -> Time.valueOf(odt.toLocalTime()));
        registerTypeConvert(OffsetDateTime.class, Timestamp.class, odt -> Timestamp.from(odt.toInstant()));

        // ZONED DATE TIME
        registerTypeConvert(ZonedDateTime.class, Instant.class, ChronoZonedDateTime::toInstant);
        registerTypeConvert(ZonedDateTime.class, Calendar.class, zdt -> calendarOf(Date.from(zdt.toInstant())));
        registerTypeConvert(ZonedDateTime.class, LocalDateTime.class, ZonedDateTime::toLocalDateTime);
        registerTypeConvert(ZonedDateTime.class, LocalDate.class, ZonedDateTime::toLocalDate);
        registerTypeConvert(ZonedDateTime.class, LocalTime.class, ZonedDateTime::toLocalTime);
        registerTypeConvert(ZonedDateTime.class, OffsetDateTime.class, ZonedDateTime::toOffsetDateTime);
        registerTypeConvert(ZonedDateTime.class, Date.class, zdt -> Date.from(zdt.toInstant()));
        registerTypeConvert(ZonedDateTime.class, java.sql.Date.class, zdt -> java.sql.Date.valueOf(zdt.toLocalDate()));
        registerTypeConvert(ZonedDateTime.class, Time.class, zdt -> Time.valueOf(zdt.toLocalTime()));
        registerTypeConvert(ZonedDateTime.class, Timestamp.class, zdt -> Timestamp.from(zdt.toInstant()));

        // SQL DATE
        registerTypeConvert(java.sql.Date.class, Instant.class, date -> Instant.ofEpochMilli(date.getTime()));
        registerTypeConvert(java.sql.Date.class, Calendar.class, TypeConversionRegister::calendarOf);
        registerTypeConvert(java.sql.Date.class, LocalDateTime.class, date -> date.toLocalDate().atStartOfDay());
        registerTypeConvert(java.sql.Date.class, LocalDate.class, java.sql.Date::toLocalDate);
        registerTypeConvert(java.sql.Date.class, LocalTime.class, date -> null);
        registerTypeConvert(java.sql.Date.class, OffsetDateTime.class, date -> date.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        registerTypeConvert(java.sql.Date.class, ZonedDateTime.class, date -> date.toLocalDate().atStartOfDay(ZoneId.systemDefault()));
        registerTypeConvert(java.sql.Date.class, Date.class, date -> new Date(date.getTime()));
        registerTypeConvert(java.sql.Date.class, Time.class, date -> null);
        registerTypeConvert(java.sql.Date.class, Timestamp.class, date -> new Timestamp(date.getTime()));

        // SQL TIME
        registerTypeConvert(Time.class, Instant.class, time -> Instant.ofEpochMilli(time.getTime()));
        registerTypeConvert(Time.class, Calendar.class, time -> calendarOf(time.getTime()));
        registerTypeConvert(Time.class, LocalDateTime.class, time -> {
            final LocalDate date = LocalDate.ofEpochDay(0);
            final LocalTime localTime = time.toLocalTime();
            return LocalDateTime.of(date, localTime);
        });
        registerTypeConvert(Time.class, LocalDate.class, time -> LocalDate.ofEpochDay(0));
        registerTypeConvert(Time.class, LocalTime.class, Time::toLocalTime);
        registerTypeConvert(Time.class, OffsetDateTime.class, time -> {
            final LocalDate date = LocalDate.ofEpochDay(0);
            final LocalTime localTime = time.toLocalTime();
            return LocalDateTime.of(date, localTime).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerTypeConvert(Time.class, ZonedDateTime.class, time -> {
            final LocalDate date = LocalDate.ofEpochDay(0);
            final LocalTime localTime = time.toLocalTime();
            return LocalDateTime.of(date, localTime).atZone(ZoneId.systemDefault());
        });
        registerTypeConvert(Time.class, java.sql.Date.class, time -> java.sql.Date.valueOf(LocalDate.ofEpochDay(0)));
        registerTypeConvert(Time.class, Date.class, time -> new Date(time.getTime()));
        registerTypeConvert(Time.class, Timestamp.class, time -> Timestamp.valueOf(time.toLocalTime().atDate(LocalDate.ofEpochDay(0))));

        // SQL Timestamp
        registerTypeConvert(Timestamp.class, Instant.class, Timestamp::toInstant);
        registerTypeConvert(Timestamp.class, Calendar.class, timestamp -> calendarOf(timestamp.getTime()));
        registerTypeConvert(Timestamp.class, LocalDateTime.class, Timestamp::toLocalDateTime);
        registerTypeConvert(Timestamp.class, LocalDate.class, timestamp -> timestamp.toLocalDateTime().toLocalDate());
        registerTypeConvert(Timestamp.class, LocalTime.class, timestamp -> timestamp.toLocalDateTime().toLocalTime());
        registerTypeConvert(Timestamp.class, OffsetDateTime.class, timestamp -> timestamp.toLocalDateTime().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        registerTypeConvert(Timestamp.class, ZonedDateTime.class, timestamp -> timestamp.toLocalDateTime().atZone(ZoneId.systemDefault()));
        registerTypeConvert(Timestamp.class, java.sql.Date.class, timestamp -> new java.sql.Date(timestamp.getTime()));
        registerTypeConvert(Timestamp.class, Time.class, timestamp -> new Time(timestamp.getTime()));
        registerTypeConvert(Timestamp.class, Date.class, timestamp -> new Date(timestamp.getTime()));

        // STRING TIME
        registerTypeConvert(String.class, Date.class, string -> temporalOf(string).map(time -> Date.from(Instant.from(time))).orElse(null));
        registerTypeConvert(String.class, Instant.class, string -> temporalOf(string).map(Instant::from).orElse(null));
        registerTypeConvert(String.class, Calendar.class, string -> temporalOf(string).map(time -> GregorianCalendar.from(ZonedDateTime.from(time))).orElse(null));
        registerTypeConvert(String.class, LocalDateTime.class, string -> temporalOf(string).map(LocalDateTime::from).orElse(null));
        registerTypeConvert(String.class, LocalDate.class, string -> temporalOf(string).map(LocalDate::from).orElse(null));
        registerTypeConvert(String.class, LocalTime.class, string -> temporalOf(string).map(LocalTime::from).orElse(null));
        registerTypeConvert(String.class, OffsetDateTime.class, string -> temporalOf(string).map(OffsetDateTime::from).orElse(null));
        registerTypeConvert(String.class, ZonedDateTime.class, string -> temporalOf(string).map(ZonedDateTime::from).orElse(null));
        registerTypeConvert(String.class, java.sql.Date.class, string -> temporalOf(string).map(time -> java.sql.Date.valueOf(LocalDate.from(time))).orElse(null));
        registerTypeConvert(String.class, Time.class, string -> temporalOf(string).map(time -> Time.valueOf(LocalTime.from(time))).orElse(null));
        registerTypeConvert(String.class, Timestamp.class, string -> temporalOf(string).map(time -> Timestamp.from(Instant.from(time))).orElse(null));
    }

    public static Optional<TemporalAccessor> temporalOf(final String string) {
        for (final DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return Optional.of(formatter.parse(string));
            } catch (final DateTimeParseException ignored) {
                // ignored
            }
        }
        return Optional.empty();
    }

    public static InetAddress inetAddressOf(final String string) {
        try {
            return InetAddress.getByName(string);
        } catch (final Exception ignored) {
            return null;
        }
    }

    public static Calendar calendarOf(final long timeInMs) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMs);
        return calendar;
    }

    public static Calendar calendarOf(final Date date) {
        final Calendar cal = Calendar.getInstance();
        Calendar.getInstance().setTime(date);
        return cal;
    }

    public static Calendar calendarOf(final LocalTime localTime) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, localTime.getHour());
        cal.set(Calendar.MINUTE, localTime.getMinute());
        cal.set(Calendar.SECOND, localTime.getSecond());
        cal.set(Calendar.MILLISECOND, localTime.getNano() / 1_000_000);
        return cal;
    }

    public static String stringOf(final Throwable throwable) {
        final StringBuilder sb = new StringBuilder();

        for (final StackTraceElement ste : throwable.getStackTrace()) {
            // Check if the class name does not start with 'java.' or 'javax.'
            if (!ste.getClassName().startsWith("java.") && !ste.getClassName().startsWith("javax.")) {
                sb.append("\tat ").append(ste).append(LINE_SEPARATOR);
            }
        }

        if (sb.length() == 0) {
            for (final StackTraceElement ste : throwable.getStackTrace()) {
                sb.append("\tat ").append(ste).append(LINE_SEPARATOR);
            }
        }

        // Include cause exceptions in the output
        Throwable cause = throwable.getCause();
        while (cause != null) {
            sb.append("Caused by: ").append(cause).append(LINE_SEPARATOR);
            for (final StackTraceElement ste : cause.getStackTrace()) {
                if (!ste.getClassName().startsWith("java.") && !ste.getClassName().startsWith("javax.")) {
                    sb.append("\tat ").append(ste).append(LINE_SEPARATOR);
                }
            }
            cause = cause.getCause();
        }

        return sb.toString();
    }

    private TypeConversionRegister() {
        // static util class
    }
}
