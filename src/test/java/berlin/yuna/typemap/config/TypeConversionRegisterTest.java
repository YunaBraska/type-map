package berlin.yuna.typemap.config;

import berlin.yuna.typemap.model.TestEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static berlin.yuna.typemap.config.TypeConversionRegister.*;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.TypeMapTest.TEST_TIME;
import static org.assertj.core.api.Assertions.assertThat;

class TypeConversionRegisterTest {

    @BeforeEach
    void setUp() {
        System.getProperties().setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);
    }

    @AfterEach
    void tearDown() {
        registerTypeConvert(String.class, Integer.class, Integer::valueOf);
    }

    @Test
    void registerCustomConversion() {
        assertThat(convertObj("123", Integer.class)).isEqualTo(123);
        registerTypeConvert(String.class, Integer.class, source -> 999);
        assertThat(convertObj("123", Integer.class)).isEqualTo(999);
        conversionFrom(String.class).to(Integer.class).register(source -> 567);
        assertThat(convertObj("123", Integer.class)).isEqualTo(567);
    }

    @Test
    void registerCustomConversionThrowingException() {
        assertThat(convertObj("123", Integer.class)).isEqualTo(123);
        registerTypeConvert(String.class, Integer.class, source -> {
            throw new IllegalStateException("This Exception should be ignored");
        });
        assertThat(convertObj("123", Integer.class)).isNull();
    }

    @Test
    void convertNumbers() {
        assertThat(convertObj(123, Long.class)).isEqualTo(123L);
        assertThat(convertObj(123L, Integer.class)).isEqualTo(123);
        assertThat(convertObj(123, Float.class)).isEqualTo(123f);
        assertThat(convertObj(123, Double.class)).isEqualTo(123d);
        assertThat(convertObj(123, Short.class)).isEqualTo(Short.valueOf("123"));
        assertThat(convertObj(123, Byte.class)).isEqualTo(((Integer) 123).byteValue());
        assertThat(convertObj(123, BigInteger.class)).isEqualTo(BigInteger.valueOf(123));
        assertThat(convertObj(123, BigDecimal.class)).isEqualTo(BigDecimal.valueOf(((Number) 123).doubleValue()));
        assertThat(convertObj(123, Number.class)).isEqualTo(123);
        assertThat(convertObj(123, Boolean.class)).isFalse();
        assertThat(convertObj(1, Boolean.class)).isTrue();
        assertThat(convertObj(0, Boolean.class)).isFalse();
    }

    @Test
    void convertAtomics() {
        assertThat(convertObj(new AtomicInteger(123), Integer.class)).isEqualTo(123);
        assertThat(convertObj(123, AtomicInteger.class).get()).isEqualTo(123);
        assertThat(convertObj(new AtomicLong(123), Long.class)).isEqualTo(123L);
        assertThat(convertObj(123, AtomicLong.class).get()).isEqualTo(123L);
        assertThat(convertObj(new AtomicBoolean(true), Boolean.class)).isTrue();
        assertThat(convertObj(new AtomicBoolean(false), Boolean.class)).isFalse();
        assertThat(convertObj(false, AtomicBoolean.class).get()).isFalse();
        assertThat(convertObj(true, AtomicBoolean.class).get()).isTrue();
    }

    @Test
    void convertStrings() {
        final UUID uuid = UUID.randomUUID();
        assertThat(convertObj("123", Character.class)).isEqualTo('1');
        assertThat(convertObj('1', String.class)).isEqualTo("1");
        assertThat(convertObj(uuid, String.class)).isEqualTo(uuid.toString());
        assertThat(convertObj(uuid.toString(), UUID.class)).isEqualTo(uuid);
        assertThat(convertObj("true", Boolean.class)).isTrue();
        assertThat(convertObj("false", Boolean.class)).isFalse();
        assertThat(convertObj("1", Boolean.class)).isTrue();
        assertThat(convertObj("0", Boolean.class)).isFalse();
        assertThat(convertObj("123", Boolean.class)).isFalse();
        assertThat(convertObj(null, String.class)).isNull();
        assertThat(convertObj(true, String.class)).isEqualTo("true");
        assertThat(convertObj(false, String.class)).isEqualTo("false");
        assertThat(convertObj(TestEnum.BB, String.class)).isEqualTo("BB");
        assertThat(convertObj("BB", TestEnum.class)).isEqualTo(TestEnum.BB);
        assertThat(convertObj("123", Integer.class)).isEqualTo(123);
        assertThat(convertObj("123", Long.class)).isEqualTo(123L);
        assertThat(convertObj("123", Float.class)).isEqualTo(123f);
        assertThat(convertObj("123", Double.class)).isEqualTo(123d);
        assertThat(convertObj("123", Short.class)).isEqualTo(Short.valueOf("123"));
        assertThat(convertObj("123", Byte.class)).isEqualTo(Byte.valueOf("123"));
        assertThat(convertObj("123", BigInteger.class)).isEqualTo(BigInteger.valueOf(123));
        assertThat(convertObj("123", BigDecimal.class)).isEqualTo(BigDecimal.valueOf(123));
        assertThat(convertObj("123", Number.class)).isEqualTo(123d);


    }

    @Test
    void convertThrowable() {
        final RuntimeException value = new RuntimeException("AA", new RuntimeException("BB"));
        final String expected = stringOf(value);
        assertThat(convertObj(value, String.class)).isEqualTo(expected);

        TypeConversionRegister.IGNORED_TRACE_ELEMENTS.add("berlin.yuna");
        assertThat(convertObj(value, String.class)).isNotNull();
        assertThat(convertObj(value, String.class)).isNotEqualTo(expected);
    }

    @Test
    void convertPath() throws MalformedURLException, URISyntaxException {
        final Path path = Paths.get("AA", "BB");
        final File file = path.toFile();
        final URI uri = path.toUri();
        final URL url = uri.toURL();
        // path
        assertThat(convertObj(path, File.class)).isEqualTo(file);
        assertThat(convertObj(path, URI.class)).isEqualTo(uri);
        assertThat(convertObj(path, URL.class)).isEqualTo(url);
        assertThat(convertObj(path, String.class)).isEqualTo(path.toString());
        // file
        assertThat(convertObj(file, Path.class)).isEqualTo(path);
        assertThat(convertObj(file, URI.class)).isEqualTo(uri);
        assertThat(convertObj(file, URL.class)).isEqualTo(url);
        assertThat(convertObj(file, String.class)).isEqualTo(path.toString());
        // uri
        assertThat(convertObj(uri, Path.class)).isEqualTo(Paths.get(uri.getPath()));
        assertThat(convertObj(uri, File.class)).isEqualTo(new File(uri));
        assertThat(convertObj(uri, URL.class)).isEqualTo(url);
        assertThat(convertObj(uri, String.class)).isEqualTo(uri.toString());
        // url
        assertThat(convertObj(url, Path.class)).isEqualTo(Paths.get(uri.getPath()));
        assertThat(convertObj(url, File.class)).isEqualTo(new File(uri));
        assertThat(convertObj(url, URI.class)).isEqualTo(url.toURI());
        assertThat(convertObj(url, String.class)).isEqualTo(url.toString());
        // string
        assertThat(convertObj(path.toString(), Path.class)).isEqualTo(path);
        assertThat(convertObj(path.toString(), File.class)).isEqualTo(file);
        assertThat(convertObj(path.toString(), URI.class)).isEqualTo(new URI(path.toString()));
        assertThat(convertObj(path.toString(), URL.class)).isEqualTo(Paths.get(path.toString()).toUri().toURL());
        // string builder
        assertThat(convertObj(new StringBuilder().append("AA").append("BB"), String.class)).isEqualTo("AABB");
        assertThat(convertObj("AA", StringBuilder.class)).hasToString("AA");
    }

    @Test
    void convertNet() throws UnknownHostException {
        assertThat(convertObj(Inet4Address.getByName("1.2.3.4"), String.class)).isEqualTo("/1.2.3.4");
        assertThat(convertObj(Inet6Address.getByName("2001:0db8:85a3:08d3:1319:8a2e:0370:7344"), String.class)).isEqualTo("/2001:db8:85a3:8d3:1319:8a2e:370:7344");
        assertThat(convertObj("1.2.3.4", InetAddress.class)).isEqualTo(InetAddress.getByName("1.2.3.4"));
        assertThat(convertObj("invalid", InetAddress.class)).isNull();
        assertThat(convertObj("1.2.3.4", Inet4Address.class)).isEqualTo(Inet4Address.getByName("1.2.3.4"));
        assertThat(convertObj("2001:0db8:85a3:08d3:1319:8a2e:0370:7344", Inet6Address.class)).isEqualTo(Inet6Address.getByName("2001:0db8:85a3:08d3:1319:8a2e:0370:7344"));
    }

    @Test
    void convertDate() {
        final Date time = new Date(TEST_TIME);
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertInstant() {
        final Instant time = Instant.now();
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertCalendar() {
        final Calendar time = Calendar.getInstance();
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertLocalDateTime() {
        final LocalDateTime time = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertLocalDate() {
        final LocalDate time = LocalDate.now(ZoneId.systemDefault());
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNull();
        assertThat(convertObj(time, Time.class)).isNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertLocalTime() {
        final LocalTime time = LocalTime.now(ZoneId.systemDefault());
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNull();
        assertThat(convertObj(time, LocalDate.class)).isNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertOffsetDateTime() {
        final OffsetDateTime time = OffsetDateTime.now();
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertZonedDateTime() {
        final ZonedDateTime time = ZonedDateTime.now();
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
    }

    @Test
    void convertSqlDate() {
        final java.sql.Date time = convertObj(new Date(TEST_TIME), java.sql.Date.class);
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNull();
    }

    @Test
    void convertSqlTime() {
        final java.sql.Time time = convertObj(new Date(TEST_TIME), java.sql.Time.class);
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, Timestamp.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNull();
    }

    @Test
    void convertTimestamp() {
        final Timestamp time = Timestamp.from(Instant.now());
        assertThat(convertObj(time, Long.class)).isNotNull();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
    }

    @Test
    void convertStringToTime() {
        final String time = new Date(TEST_TIME).toString();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj("invalid", ZonedDateTime.class)).isNull();
    }

    @Test
    void convertStringTimestampToTime() {
        final String time = "1800000000000";
        final String time2 = "1800000000";
        assertThat(convertObj(time, Date.class)).isEqualTo(convertObj(time2, Date.class));
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj("invalid", ZonedDateTime.class)).isNull();
    }

    @Test
    void convertLongToTime() {
        final Long time = Instant.now().toEpochMilli();
        assertThat(convertObj(time, Date.class)).isNotNull();
        assertThat(convertObj(time, Instant.class)).isNotNull();
        assertThat(convertObj(time, Calendar.class)).isNotNull();
        assertThat(convertObj(time, LocalDate.class)).isNotNull();
        assertThat(convertObj(time, LocalTime.class)).isNotNull();
        assertThat(convertObj(time, LocalDateTime.class)).isNotNull();
        assertThat(convertObj(time, OffsetDateTime.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Date.class)).isNotNull();
        assertThat(convertObj(time, java.sql.Time.class)).isNotNull();
        assertThat(convertObj(time, Time.class)).isNotNull();
        assertThat(convertObj(time, ZonedDateTime.class)).isNotNull();
        assertThat(convertObj(-1L, ZonedDateTime.class)).isNotNull();
    }

    @Test
    void parseCommonDateTimeFormats_resilient() {
        final String[] samples = {
            "2024-11-14 04:54:38",
            "2025-09-04T13:45:30Z",
            "2025-09-04 13:45:30",
            "2025-09-04 13:45",
            "2025-9-4 1:5:6",
            "2025/09/04 13:45",
            "04.09.2025 13:45",
            "04.09.2025 13:45:30.123",
            "09/04/2025 01:45 PM",
            "Thu, 04 Sep 2025 13:45:30 GMT",
            "Thu Aug 22 2024 15:01:02 GMT+0200",
            "2025-09-04T13:45:30+0200",
            "2025-09-04T13:45:30.123456789Z",
            "2025-09-04 at 13:45:30"
        };

        for (final String s : samples) {
            assertThat(convertObj(s, Date.class)).as("Date: " + s).isNotNull();
            assertThat(convertObj(s, Instant.class)).as("Instant: " + s).isNotNull();
            assertThat(convertObj(s, Calendar.class)).as("Calendar: " + s).isNotNull();
            assertThat(convertObj(s, LocalDate.class)).as("LocalDate: " + s).isNotNull();
            assertThat(convertObj(s, LocalTime.class)).as("LocalTime: " + s).isNotNull();
            assertThat(convertObj(s, LocalDateTime.class)).as("LocalDateTime: " + s).isNotNull();
            assertThat(convertObj(s, OffsetDateTime.class)).as("OffsetDateTime: " + s).isNotNull();
            assertThat(convertObj(s, ZonedDateTime.class)).as("ZonedDateTime: " + s).isNotNull();
            assertThat(convertObj(s, java.sql.Date.class)).as("sql.Date: " + s).isNotNull();
            assertThat(convertObj(s, java.sql.Time.class)).as("sql.Time: " + s).isNotNull();
            assertThat(convertObj(s, Time.class)).as("Time: " + s).isNotNull();
        }
    }

    @Test
    void parseCommonDateOnlyFormats_resilient() {
        final String[] samples = {
            "2025-09-04",
            "2025/09/04",
            "04.09.2025",
            "4.9.2025",
            "04/09/2025",
            "09/04/2025",
            "04-09-2025",
            "4-9-2025",
            "4 Sep 2025",
            "4 September 2025",
//            "4. Sep 2025",
            "4. September 2025"
        };

        for (final String s : samples) {
            // With the upgraded temporalOf, these should all promote to start-of-day UTC (per @BeforeEach)
            assertThat(convertObj(s, LocalDate.class)).as("LocalDate: " + s).isNotNull();
            assertThat(convertObj(s, ZonedDateTime.class)).as("ZonedDateTime: " + s).isNotNull();
            assertThat(convertObj(s, Instant.class)).as("Instant: " + s).isNotNull();
            assertThat(convertObj(s, Date.class)).as("Date: " + s).isNotNull();
            assertThat(convertObj(s, java.sql.Date.class)).as("sql.Date: " + s).isNotNull();
        }
    }

    @Test
    void parseTwoDigitYear_US_style_pivotTo2000s() {
        final String s = "9/4/25 13:45";
        final LocalDate ld = convertObj(s, LocalDate.class);
        assertThat(ld).isNotNull();
        assertThat(ld.getYear()).isEqualTo(2025);
    }

    @Test
    void calendarOfDate_isCorrect() {
        final Date d = new Date(123456789L);
        final Calendar cal = TypeConversionRegister.calendarOf(d);
        assertThat(cal.getTimeInMillis()).isEqualTo(123456789L);
    }
}
