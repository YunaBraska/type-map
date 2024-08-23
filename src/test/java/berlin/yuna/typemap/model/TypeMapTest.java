package berlin.yuna.typemap.model;


import berlin.yuna.typemap.config.TypeConversionRegister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Paths;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static berlin.yuna.typemap.logic.JsonDecoder.jsonTypeOf;
import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.logic.XmlDecoder.xmlTypeOf;
import static berlin.yuna.typemap.model.ConcurrentTypeMap.concurrentMapOf;
import static berlin.yuna.typemap.model.LinkedTypeMap.linkedMapOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;

public class TypeMapTest {

    public static final long TEST_TIME = 1800000000000L;

    @BeforeEach
    void setUp() {
        System.getProperties().setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);
    }

    static Stream<Arguments> typeMapProvider() {
        return Stream.of(
            Arguments.of(TypeMap.class.getSimpleName(), new TypeMap()),
            Arguments.of(LinkedTypeMap.class.getSimpleName(), new LinkedTypeMap()),
            Arguments.of(ConcurrentTypeMap.class.getSimpleName(), new ConcurrentTypeMap())
        );
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void simpleConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date(TEST_TIME).toString();
        typeMap.addReturn("myKey", myTime);

        // VALIDATIONS
        assertThat(typeMap.typeListOpt()).isEmpty();
        assertThat(typeMap.typeMapOpt()).isPresent();

        // TREE MAP
        assertThat(typeMap.getOpt(Instant.class, "myKey")).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.getOpt(LocalTime.class, "myKey")).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeMap.get(OffsetDateTime.class, "myKey")).isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));

        // KEY MAP
        assertThat(typeMap.getOpt(Instant.class, "myKey")).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.getOpt(LocalTime.class, "myKey")).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeMap.get(OffsetDateTime.class, "myKey")).isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void enumConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        TypeConversionRegister.registerTypeConvert(Number.class, TestEnum.class, source -> source.intValue() == 5 ? TestEnum.AA : null);
        typeMap.put("key1", "BB");
        typeMap.put("key2", "1");
        typeMap.put("key3", 0);
        typeMap.put("key4", 1);
        typeMap.put("key5", 3);
        typeMap.put("key6", 5D);
        typeMap.put("key7", -1);
        assertThat(typeMap.getOpt(TestEnum.class, "key1")).contains(TestEnum.BB);
        assertThat(typeMap.getOpt(TestEnum.class, "key2")).isEmpty();
        assertThat(typeMap.getOpt(TestEnum.class, "key3")).contains(TestEnum.AA);
        assertThat(typeMap.getOpt(TestEnum.class, "key4")).contains(TestEnum.BB);
        assertThat(typeMap.getOpt(TestEnum.class, "key5")).isEmpty();
        assertThat(typeMap.getOpt(TestEnum.class, "key6")).contains(TestEnum.AA);
        assertThat(typeMap.getOpt(TestEnum.class, "key7")).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void collectionConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date(TEST_TIME).toString();
        typeMap.putReturn("myKey1", myTime);
        typeMap.put("myKey2", new String[]{"1", "2", "3"});

        // TREE MAP
        final List<Instant> instantList1 = typeMap.asList(ArrayList::new, Instant.class, "myKey1");
        final List<Integer> integerList1 = typeMap.asList(ArrayList::new, Integer.class, "myKey2");
        final List<Float> floatList1 = typeMap.asList(ArrayList::new, Float.class, "myKey2");
        final Double[] doubleArray1 = typeMap.asArray(new Double[0], Double.class, "myKey2");
        final Long[] longArray1 = typeMap.asArray(Long[]::new, Long.class, "myKey2");

        assertThat(instantList1).isNotEmpty();
        assertThat(integerList1).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList1).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray1).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray1).isNotEmpty().containsExactly(1L, 2L, 3L);
        assertThat(typeMap.asList("myKey2")).isNotEmpty().containsExactly("1", "2", "3");
        assertThat(typeMap.asList(Integer.class, "myKey2")).isNotEmpty().containsExactly(1, 2, 3);

        // KEY MAP
        final List<Instant> instantList2 = typeMap.asList(ArrayList::new, Instant.class, "myKey1");
        final List<Integer> integerList2 = typeMap.asList(ArrayList::new, Integer.class, "myKey2");
        final List<Float> floatList2 = typeMap.asList(ArrayList::new, Float.class, "myKey2");
        final Double[] doubleArray2 = typeMap.asArray(new Double[0], Double.class, "myKey2");
        final Long[] longArray2 = typeMap.asArray(Long[]::new, Long.class, "myKey2");

        assertThat(instantList2).isNotEmpty();
        assertThat(integerList2).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList2).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray2).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray2).isNotEmpty().containsExactly(1L, 2L, 3L);
        assertThat(typeMap.asList(Integer.class, "myKey2")).isNotEmpty().containsExactly(1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void mapConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final Map<Integer, Date> input = new HashMap<>();
        input.put(6, new Date(TEST_TIME));
        typeMap.put("myKey", input);

        // TREE MAP
        assertThat(typeMap.asMap(String.class, Object.class)).containsOnlyKeys("myKey");
        assertThat(typeMap.asMap("myKey")).containsEntry(6, new Date(TEST_TIME));
        assertThat(typeMap.asMap(Long.class, Instant.class, "myKey")).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.asMap(() -> new HashMap<>(), Long.class, Instant.class, "myKey")).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.asMap(() -> new HashMap<>(), Long.class, Instant.class, "myKey2")).isEmpty();

        // KEY MAP
        assertThat(typeMap.asMap(() -> new HashMap<>(), Long.class, Instant.class, "myKey")).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.asMap(Long.class, Instant.class, "myKey")).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.asMap(() -> new HashMap<>(), Long.class, Instant.class, "myKey2")).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void mapFunctionalConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        typeMap.put("1", "AA");
        typeMap.put("2", Arrays.asList("BB", "CC"));
        typeMap.put("3", new String[]{"DD", "EE"});

        // TREE MAP
        assertThat(typeMap.asMap(Integer.class, value -> value)).containsOnlyKeys(1, 2, 3);
        assertThat(typeMap.asMap(key -> convertObj(key, Integer.class), value -> value)).containsOnlyKeys(1, 2, 3);
        assertThat(typeMap.asMap(key -> convertObj(key, Integer.class), value -> collectionOf(value, String.class))).containsExactly(
            entry(1, singletonList("AA")),
            entry(2, Arrays.asList("BB", "CC")),
            entry(3, Arrays.asList("DD", "EE"))
        );
        assertThat(typeMap.asMap(() -> new LinkedHashMap<>(), key -> convertObj(key, Integer.class), value -> collectionOf(value, String.class))).containsExactly(
            entry(1, singletonList("AA")),
            entry(2, Arrays.asList("BB", "CC")),
            entry(3, Arrays.asList("DD", "EE"))
        );
        assertThat(typeMap.asMap((Supplier<? extends Map<Integer, List<String>>>) null, key -> convertObj(key, Integer.class), value -> collectionOf(value, String.class))).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void jsonConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final Map<String, Object> input = new HashMap<>();
        final Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("FF", asList("GG", 2, true));
        input.put("AA", asList("BB", 1, true, null));
        input.put("CC", new long[]{4L, 5L, 6L});
        input.put("DD", innerMap);
        input.put("EE", "HH,II,\n");
        typeMap.put("myKey", input);

        assertThat(typeMap.toJson("invalidKey")).isEqualTo("{}");
        assertThat(typeMap.toJson("myKey")).isEqualTo("{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}");
        assertThat(typeMap.toJson()).isEqualTo("{\"myKey\":{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}}");

        // Encode & Decode
        assertThat(new TypeMap(typeMap.toJson("invalidKey")).toJson()).isEqualTo("{}");
        assertThat(new ConcurrentTypeMap(typeMap.toJson("myKey")).toJson()).isEqualTo("{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}");
        assertThat(new LinkedTypeMap(typeMap.toJson()).toJson()).isEqualTo("{\"myKey\":{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}}");
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void nestedKeysTest(final String mapName, final TypeMapI<?> typeMap) {
        final Map<String, Object> innerMap = new HashMap<>();
        final UnknownClass anObject = new UnknownClass();
        innerMap.put("BB", asList("11", "22"));
        innerMap.put("CC", new Object[]{"33", "44"});
        innerMap.put("DD", anObject);
        innerMap.put("EE", singletonList(anObject));
        innerMap.put("FF", new Object[]{anObject});
        typeMap.put("AA", innerMap);

        assertThat(typeMap.getOpt(Object.class)).contains(typeMap);
        assertThat(typeMap.getOpt(Object.class, (Object) null)).isEmpty();
        assertThat(typeMap.getOpt(Object.class, new Object[]{null})).isEmpty();
        assertThat(typeMap.getOpt(Object.class, "AA")).contains(innerMap);
        assertThat(typeMap.getOpt(Object.class, "AA", "BB")).contains(asList("11", "22"));
        assertThat(typeMap.getOpt(Object.class, "AA", "CC")).contains(new Object[]{"33", "44"});
        assertThat(typeMap.getOpt(Object.class, "AA", "BB", 0)).contains("11");
        assertThat(typeMap.getOpt(Object.class, "AA", "BB", 1)).contains("22");
        assertThat(typeMap.getOpt(Object.class, "AA", "CC", 0)).contains("33");
        assertThat(typeMap.getOpt(Object.class, "AA", "CC", 1)).contains("44");
        assertThat(typeMap.getOpt(UnknownClass.class, "AA", "DD")).contains(anObject);
        assertThat(typeMap.getOpt(UnknownClass.class, "AA", "DD", anObject)).isEmpty();
    }

    @Test
    void testDefaultMapMethods() {
        final String myTime = new Date(TEST_TIME).toString();

        // Broken json
        assertThat(new TypeMap("{ broken json")).containsEntry("", "{ broken json");
        assertThat(new LinkedTypeMap("{ broken json")).containsEntry("", "{ broken json");
        assertThat(new ConcurrentTypeMap("{ broken json")).containsEntry("", "{ broken json");

        final TypeMap map1 = new TypeMap().putReturn("myKey", myTime);
        final LinkedTypeMap map2 = new LinkedTypeMap().putReturn("myKey", myTime);
        final ConcurrentTypeMap map3 = new ConcurrentTypeMap().putReturn("myKey", myTime);

        // Get
        assertThat(map1.get("myKey")).isNotNull();
        assertThat(map2.get("myKey")).isNotNull();
        assertThat(map3.get("myKey")).isNotNull();
    }

    @Test
    void argsTest() {
        final String[] cliArgs = {" myCommand1    myCommand2 --help  -v2=\"true\" -v=\"true\" -v=\"true\" --verbose=\"true\"   -DArgs=\"true\" -param 42   54   -DArgList=\"item 1\" --DArgList=\"item 2\" -v2=\"false\" --DArgList=\"-item 3\"  "};
        final TypeMap map1 = new TypeMap(cliArgs);
        final LinkedTypeMap map2 = new LinkedTypeMap(cliArgs);
        final ConcurrentTypeMap map3 = new ConcurrentTypeMap(cliArgs);

        for (final TypeMapI<?> map : new TypeMapI<?>[]{map1, map2, map3}) {
            assertThat(map.get(Boolean.class, "help")).isTrue();
            assertThat(map.get(Boolean.class, "v")).isTrue();
            assertThat(map.get(Boolean.class, "v2")).isTrue();
            assertThat(map.get(Boolean.class, "verbose")).isTrue();
            assertThat(map.get(Boolean.class, "DArgs")).isTrue();
            assertThat(map.asList(Integer.class, "param")).containsExactly(42, 54);
            assertThat(map.asList(Boolean.class, "v2")).containsExactly(true, false);
            assertThat(map.asList("DArgList")).containsExactly("item 1", "item 2", "-item 3");
            assertThat(map.asList(String.class, "DArgList")).containsExactly("item 1", "item 2", "-item 3");
        }
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void testDefaultMethods(final String mapName, final TypeMapI<?> typeMap) throws Exception {
        final String testKey = "testKey";
        final String testPath = "/path/to/file";
        final String testUr = "http://example.com";
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(TEST_TIME));

        assertThat(typeMap.putReturn(testKey, "testString").as(String.class, testKey)).isEqualTo("testString");
        assertThat(typeMap.putReturn(testKey, "testString").asString(testKey)).isEqualTo("testString");
        assertThat(typeMap.putReturn(testKey, "testString").asStrings(testKey)).contains("testString");
        assertThat(typeMap.putReturn(testKey, "123456789").asLong(testKey)).isEqualTo(123456789L);
        assertThat(typeMap.putReturn(testKey, "123456789").asLongs(testKey)).contains(123456789L);
        assertThat(typeMap.putReturn(testKey, "123456789").asNumber(testKey)).isEqualTo(123456789d);
        assertThat(typeMap.putReturn(testKey, "123456789").asNumbers(testKey)).contains(123456789d);
        assertThat(typeMap.putReturn(testKey, "42").asInt(testKey)).isEqualTo(42);
        assertThat(typeMap.putReturn(testKey, "42").asInts(testKey)).contains(42);
        assertThat(typeMap.putReturn(testKey, "42.42").asDouble(testKey)).isEqualTo(42.42);
        assertThat(typeMap.putReturn(testKey, "42.42").asDoubles(testKey)).contains(42.42);
        assertThat(typeMap.putReturn(testKey, "42.42").asFloat(testKey)).isEqualTo(42.42f);
        assertThat(typeMap.putReturn(testKey, "42.42").asFloats(testKey)).contains(42.42f);
        assertThat(typeMap.putReturn(testKey, "32000").asShort(testKey)).isEqualTo((short) 32000);
        assertThat(typeMap.putReturn(testKey, "32000").asShorts(testKey)).contains((short) 32000);
        assertThat(typeMap.putReturn(testKey, "127").asByte(testKey)).isEqualTo((byte) 127);
        assertThat(typeMap.putReturn(testKey, "127").asBytes(testKey)).contains((byte) 127);
        assertThat(typeMap.putReturn(testKey, "12345678901234567890").asBigInteger(testKey)).isEqualTo(new BigInteger("12345678901234567890"));
        assertThat(typeMap.putReturn(testKey, "12345.67890").asBigDecimal(testKey)).isEqualTo(new BigDecimal("12345.67890"));
        assertThat(typeMap.putReturn(testKey, "42").asAtomicInteger(testKey)).hasValue(42);
        assertThat(typeMap.putReturn(testKey, "123456789").asAtomicLong(testKey)).hasValue(123456789L);
        assertThat(typeMap.putReturn(testKey, "true").asAtomicBoolean(testKey)).isTrue();
        assertThat(typeMap.putReturn(testKey, "1").asAtomicBoolean(testKey)).isTrue();
        assertThat(typeMap.putReturn(testKey, "false").asAtomicBoolean(testKey)).isFalse();
        assertThat(typeMap.putReturn(testKey, "0").asAtomicBoolean(testKey)).isFalse();
        assertThat(typeMap.putReturn(testKey, UUID.randomUUID().toString()).asUUID(testKey)).isNotNull();
        assertThat(typeMap.putReturn(testKey, UUID.randomUUID().toString()).asUUIDs(testKey)).isNotNull().hasSize(1);
        assertThat(typeMap.putReturn(testKey, "A").asCharacter(testKey)).isEqualTo('A');
        assertThat(typeMap.putReturn(testKey, "A").asCharacters(testKey)).contains('A');
        assertThat(typeMap.putReturn(testKey, "true").asBoolean(testKey)).isTrue();
        assertThat(typeMap.putReturn(testKey, "true").asBooleans(testKey)).contains(true);
        assertThat(typeMap.putReturn(testKey, "false").asBoolean(testKey)).isFalse();
        assertThat(typeMap.putReturn(testKey, "false").asBooleans(testKey)).contains(false);
        assertThat(typeMap.putReturn(testKey, "1").asBoolean(testKey)).isTrue();
        assertThat(typeMap.putReturn(testKey, "1").asBooleans(testKey)).contains(true);
        assertThat(typeMap.putReturn(testKey, "0").asBoolean(testKey)).isFalse();
        assertThat(typeMap.putReturn(testKey, "0").asBooleans(testKey)).contains(false);
        assertThat(typeMap.putReturn(testKey, new IllegalStateException("Test Throwable")).asThrowable(testKey)).isInstanceOf(Throwable.class).hasMessage("Test Throwable");
        assertThat(typeMap.putReturn(testKey, new IllegalStateException("Test Throwable")).asThrowables(testKey)).hasSize(1);
        assertThat(typeMap.putReturn(testKey, "UTF-8").asCharset(testKey)).isEqualTo(UTF_8);
        assertThat(typeMap.putReturn(testKey, "UTF-8").asCharsets(testKey)).contains(UTF_8);
        assertThat(typeMap.putReturn(testKey, testPath).asFile(testKey)).isEqualTo(new File(testPath));
        assertThat(typeMap.putReturn(testKey, testPath).asFiles(testKey)).contains(new File(testPath));
        assertThat(typeMap.putReturn(testKey, testPath).asPath(testKey)).isEqualTo(Paths.get(testPath));
        assertThat(typeMap.putReturn(testKey, testPath).asPaths(testKey)).contains(Paths.get(testPath));
        assertThat(typeMap.putReturn(testKey, testUr).asURI(testKey)).isEqualTo(URI.create(testUr));
        assertThat(typeMap.putReturn(testKey, testUr).asURIs(testKey)).contains(URI.create(testUr));
        assertThat(typeMap.putReturn(testKey, testUr).asURL(testKey)).isEqualTo(new URL(testUr));
        assertThat(typeMap.putReturn(testKey, testUr).asURLs(testKey)).contains(new URL(testUr));
        assertThat(typeMap.putReturn(testKey, "localhost").asInetAddress(testKey)).isEqualTo(InetAddress.getByName("localhost"));
        assertThat(typeMap.putReturn(testKey, "localhost").asInetAddresses(testKey)).contains(InetAddress.getByName("localhost"));
        assertThat(typeMap.putReturn(testKey, "127.0.0.1").asInet4Address(testKey)).isEqualTo(Inet4Address.getByName("127.0.0.1"));
        assertThat(typeMap.putReturn(testKey, "127.0.0.1").asInet4Addresses(testKey)).contains((Inet4Address) Inet4Address.getByName("127.0.0.1"));
        assertThat(typeMap.putReturn(testKey, "::1").asInet6Address(testKey)).isEqualTo(Inet6Address.getByName("::1"));
        assertThat(typeMap.putReturn(testKey, "::1").asInet6Addresses(testKey)).contains((Inet6Address) Inet6Address.getByName("::1"));
        assertThat(typeMap.putReturn(testKey, String.valueOf(TEST_TIME)).asDate(testKey)).isEqualTo(new Date(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, String.valueOf(TEST_TIME)).asDates(testKey)).contains(new Date(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, String.valueOf(TEST_TIME)).asInstant(testKey)).isEqualTo(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, String.valueOf(TEST_TIME)).asInstants(testKey)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, calendar).asCalendar(testKey)).isEqualTo(calendar);
        assertThat(typeMap.putReturn(testKey, calendar).asCalendars(testKey)).hasSize(1);
        assertThat(typeMap.putReturn(testKey, calendar.getTime().toString()).asCalendar(testKey)).isEqualByComparingTo(calendar);
        assertThat(typeMap.putReturn(testKey, calendar.getTime().toString()).asCalendars(testKey)).hasSize(1);
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asLocalDateTime(testKey)).isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asLocalDateTimes(testKey)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asLocalDate(testKey)).isEqualTo(LocalDate.ofEpochDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() / (24 * 60 * 60 * 1000)));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asLocalDates(testKey)).contains(LocalDate.ofEpochDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() / (24 * 60 * 60 * 1000)));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asLocalTime(testKey)).isEqualTo(LocalTime.ofNanoOfDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() % (24 * 60 * 60 * 1000) * 1000000));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asLocalTimes(testKey)).contains(LocalTime.ofNanoOfDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() % (24 * 60 * 60 * 1000) * 1000000));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asOffsetDateTime(testKey)).isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asOffsetDateTimes(testKey)).contains(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asZonedDateTime(testKey)).isEqualTo(ZonedDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asZonedDateTimes(testKey)).contains(ZonedDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asSqlDate(testKey)).isEqualTo(new java.sql.Date(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asSqlDates(testKey)).contains(new java.sql.Date(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asTime(testKey)).isEqualTo(new Time(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asTimes(testKey)).contains(new Time(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asTimestamp(testKey)).isEqualTo(new Timestamp(TEST_TIME));
        assertThat(typeMap.putReturn(testKey, TEST_TIME).asTimestamps(testKey)).contains(new Timestamp(TEST_TIME));
    }

    @Test
    void mapOfTest() {
        assertThat(TypeMap.mapOf(null)).isEmpty();
        assertThat(linkedMapOf(null)).isEmpty();
        assertThat(concurrentMapOf(null)).isEmpty();

        assertThat(TypeMap.mapOf("key1", 1, "key2", true)).containsExactly(entry("key1", 1), entry("key2", true));
        assertThat(linkedMapOf("key1", 1, "key2", true)).containsExactly(entry("key1", 1), entry("key2", true));
        assertThat(concurrentMapOf("key1", 1, "key2", true)).containsExactly(entry("key1", 1), entry("key2", true));

        assertThatThrownBy(() -> TypeMap.mapOf("key1", 1, "key2")).isInstanceOf(InternalError.class).hasMessage("length is odd");
        assertThatThrownBy(() -> linkedMapOf("key1", 1, "key2")).isInstanceOf(InternalError.class).hasMessage("length is odd");
        assertThatThrownBy(() -> concurrentMapOf("key1", 1, "key2")).isInstanceOf(InternalError.class).hasMessage("length is odd");
    }

    @Test
    void showCaseTest() {

        // Converter
        final Date date = convertObj("Fri Jan 15 08:00:00 UTC 2027", Date.class);

        // TypeMap
        final TypeMap map = new TypeMap();
        map.put("key", new Date(TEST_TIME));
        final Optional<Calendar> calendar = map.getOpt(Calendar.class, "key");
        final Optional<LocalDateTime> localDateTime = map.getOpt(LocalDateTime.class, "key");
        final Optional<ZonedDateTime> zonedDateTime = map.getOpt(ZonedDateTime.class, "key");

        // Register custom conversion
        TypeConversionRegister.registerTypeConvert(UnknownClass.class, Double.class, source -> 99d);

        // JSON - Encode/Decode & Convert
        final String jsonInput = "{\n"
            + "  \"outerMap\": {\n"
            + "    \"times\": {\n"
            + "      \"timestamp1\": 1800000000000,\n"
            + "      \"timestamp2\": 1800000000,\n"
            + "      \"date\": \"Fri Jan 15 08:00:00 UTC 2027\",\n"
            + "    },\n"
            + "    \"myList\": [\"BB\",1,true,null,1.2]\n"
            + "  }\n"
            + "}";

        final TypeInfo<?> jsonMap = jsonTypeOf(jsonInput);
        final LinkedTypeMap map1 = jsonMap.asMap("outerMap", "times");
        final TestEnum testEnum = jsonMap.asList("outerMap", "myList").get(TestEnum.class, 0);

        final Optional<Date> myDate1 = jsonMap.getOpt(Date.class, "outerMap", "times", "timestamp1");
        final Optional<Date> myDate2 = jsonMap.getOpt(Date.class, "outerMap", "times", "timestamp2");
        final Optional<Date> myDate3 = jsonMap.getOpt(Date.class, "outerMap", "times", "date");
        final Optional<Long> myTimestamp = jsonMap.getOpt(Long.class, "outerMap", "times", "timestamp1");
        final Optional<TestEnum> myEnum = jsonMap.getOpt(TestEnum.class, "outerMap", "myList", 0);
        final Optional<Boolean> myBoolean = jsonMap.getOpt(Boolean.class, "outerMap", "myList", 2);

        final String myJson = jsonMap.toJson();

        // Assertions
        assertThat(calendar).isPresent();
        assertThat(localDateTime).isPresent();
        assertThat(zonedDateTime).isPresent();
        assertThat(map1).hasSize(3);
        assertThat(testEnum).isEqualTo(TestEnum.BB);
        assertThat(date).isEqualTo(new Date(TEST_TIME));
        assertThat(myDate1).contains(new Date(TEST_TIME));
        assertThat(myDate2).contains(new Date(TEST_TIME));
        assertThat(myDate3).contains(new Date(TEST_TIME));
        assertThat(myTimestamp).contains(TEST_TIME);
        assertThat(myEnum).contains(TestEnum.BB);
        assertThat(myBoolean).contains(true);
        assertThat(myJson).isNotNull();
    }

    @Test
    void showCaseXml() {
        final String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<error>\n"
            + "  <code>red</code>\n"
            + "  <details>\n"
            + "    <http-status>418</http-status>\n"
            + "    <date-time>Fri Jan 15 08:00:00 UTC 2027</date-time>\n"
            + "  </details>\n"
            + "</error>";
        final TypeList xml = xmlTypeOf(xmlString);
        assertThat(xml.get(String.class, "error", "code")).isEqualTo("red");
        assertThat(xml.get(Integer.class, "error", "details", "http-status")).isEqualTo(418);
        assertThat(xml.get(Date.class, "error", "details", "date-time")).isEqualTo(new Date(1800000000000L));
    }
}
