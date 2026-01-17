package berlin.yuna.typemap.model;


import berlin.yuna.typemap.config.TypeConversionRegister;
import berlin.yuna.typemap.logic.JsonDecoder;
import berlin.yuna.typemap.logic.XmlDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import berlin.yuna.typemap.model.Type;

import static berlin.yuna.typemap.logic.JsonDecoder.jsonTypeOf;
import static berlin.yuna.typemap.logic.JsonDecoder.streamJsonObject;
import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.logic.XmlDecoder.xmlTypeOf;
import static berlin.yuna.typemap.model.ConcurrentTypeMap.concurrentMapOf;
import static berlin.yuna.typemap.model.LinkedTypeMap.linkedMapOf;
import static berlin.yuna.typemap.model.Type.typeOf;
import static berlin.yuna.typemap.model.TypeMap.treeGet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("all")
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
        typeMap.addR("myKey", myTime);

        // VALIDATIONS
        assertThat(typeMap.typeListOpt().value()).isNull();
        assertThat(typeMap.typeMapOpt().value()).isNotNull();

        // TREE MAP
        assertThat(typeMap.asOpt(Instant.class, "myKey")).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.asOpt(LocalTime.class, "myKey")).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeMap.get(OffsetDateTime.class, "myKey")).isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));

        // KEY MAP
        assertThat(typeMap.asOpt(Instant.class, "myKey")).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.asOpt(LocalTime.class, "myKey")).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
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
        assertThat(typeMap.asOpt(TestEnum.class, "key1")).contains(TestEnum.BB);
        assertThat(typeMap.asOpt(TestEnum.class, "key2").value()).isNull();
        assertThat(typeMap.asOpt(TestEnum.class, "key3")).contains(TestEnum.AA);
        assertThat(typeMap.asOpt(TestEnum.class, "key4")).contains(TestEnum.BB);
        assertThat(typeMap.asOpt(TestEnum.class, "key5").value()).isNull();
        assertThat(typeMap.asOpt(TestEnum.class, "key6")).contains(TestEnum.AA);
        assertThat(typeMap.asOpt(TestEnum.class, "key7").value()).isNull();
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void collectionConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date(TEST_TIME).toString();
        typeMap.putR("myKey1", myTime);
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

    @Test
    void shouldMapOfOverloads() throws Exception {
        final String json = "{\"name\":\"neo\",\"tags\":[\"a\"]}";
        final Path file = Files.createTempFile("typemap-mapof", ".json");
        Files.writeString(file, json, StandardCharsets.UTF_8);
        final File asFile = file.toFile();
        final URI uri = file.toUri();
        final URL url = uri.toURL();
        final LinkedTypeMap expected = JsonDecoder.mapOf(json);

        final List<Supplier<LinkedTypeMap>> suppliers = List.of(
            () -> TypeMap.mapOf(json),
            () -> TypeMap.mapOf(new StringBuilder(json)),
            () -> TypeMap.mapOf(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))),
            () -> TypeMap.mapOf(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8),
            () -> TypeMap.mapOf(file),
            () -> TypeMap.mapOf(file, StandardCharsets.UTF_8),
            () -> TypeMap.mapOf(asFile),
            () -> TypeMap.mapOf(asFile, StandardCharsets.UTF_8),
            () -> TypeMap.mapOf(uri),
            () -> TypeMap.mapOf(uri, StandardCharsets.UTF_8),
            () -> TypeMap.mapOf((URL) url),
            () -> TypeMap.mapOf(url, StandardCharsets.UTF_8)
        );

        suppliers.forEach(supplier -> assertThat(supplier.get()).isEqualTo(expected));
    }

    @Test
    void shouldMapOfXml() {
        final String xml = "<root><item>1</item><item>2</item></root>";
        final TypeList xmlList = XmlDecoder.xmlTypeOf(xml);
        final LinkedTypeMap expected = new LinkedTypeMap().putR("", xmlList);
        assertThat(TypeMap.mapOf(xml)).isEqualTo(expected);
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

        assertThat(typeMap.asOpt(Object.class)).contains(typeMap);
        assertThat(typeMap.asOpt(Object.class, (Object) null).value()).isNull();
        assertThat(typeMap.asOpt(Object.class, new Object[]{null}).value()).isNull();
        assertThat(typeMap.asOpt(Object.class, "AA")).contains(innerMap);
        assertThat(typeMap.asOpt(Object.class, "AA", "BB")).contains(asList("11", "22"));
        assertThat(typeMap.asOpt(Object.class, "AA", "CC").value()).isEqualTo(new Object[]{"33", "44"});
        assertThat(typeMap.asOpt(Object.class, "AA", "BB", 0)).contains("11");
        assertThat(typeMap.asOpt(Object.class, "AA", "BB", 1)).contains("22");
        assertThat(typeMap.asOpt(Object.class, "AA", "CC", 0)).contains("33");
        assertThat(typeMap.asOpt(Object.class, "AA", "CC", 1)).contains("44");
        assertThat(typeMap.asOpt(UnknownClass.class, "AA", "DD")).contains(anObject);
        assertThat(typeMap.asOpt(UnknownClass.class, "AA", "DD", anObject).value()).isNull();
    }

    @Test
    void testDefaultMapMethods() {
        final String myTime = new Date(TEST_TIME).toString();

        // Broken json
        assertThat(new TypeMap("{ broken json")).containsEntry("", "{ broken json");
        assertThat(new LinkedTypeMap("{ broken json")).containsEntry("", "{ broken json");
        assertThat(new ConcurrentTypeMap("{ broken json")).containsEntry("", "{ broken json");

        final TypeMap map1 = new TypeMap().putR("myKey", myTime);
        final LinkedTypeMap map2 = new LinkedTypeMap().putR("myKey", myTime);
        final ConcurrentTypeMap map3 = new ConcurrentTypeMap().putR("myKey", myTime);

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

        assertThat(typeMap.putR(testKey, "testString").as(String.class, testKey)).isEqualTo("testString");
        assertThat(typeMap.putR(testKey, "testString").asString(testKey)).isEqualTo("testString");
        assertThat(typeMap.putR(testKey, "testString").asStrings(testKey)).contains("testString");
        assertThat(typeMap.putR(testKey, "testString").asStringOpt(testKey)).contains("testString");
        assertThat(typeMap.putR(testKey, "123456789").asLong(testKey)).isEqualTo(123456789L);
        assertThat(typeMap.putR(testKey, "123456789").asLongs(testKey)).contains(123456789L);
        assertThat(typeMap.putR(testKey, "123456789").asLongOpt(testKey)).contains(123456789L);
        assertThat(typeMap.putR(testKey, "123456789").asNumber(testKey)).isEqualTo(123456789d);
        assertThat(typeMap.putR(testKey, "123456789").asNumbers(testKey)).contains(123456789d);
        assertThat(typeMap.putR(testKey, "123456789").asNumberOpt(testKey)).contains(123456789d);
        assertThat(typeMap.putR(testKey, "42").asInt(testKey)).isEqualTo(42);
        assertThat(typeMap.putR(testKey, "42").asInts(testKey)).contains(42);
        assertThat(typeMap.putR(testKey, "42").asIntOpt(testKey)).contains(42);
        assertThat(typeMap.putR(testKey, "42.42").asDouble(testKey)).isEqualTo(42.42);
        assertThat(typeMap.putR(testKey, "42.42").asDoubles(testKey)).contains(42.42);
        assertThat(typeMap.putR(testKey, "42.42").asDoubleOpt(testKey)).contains(42.42);
        assertThat(typeMap.putR(testKey, "42.42").asFloat(testKey)).isEqualTo(42.42f);
        assertThat(typeMap.putR(testKey, "42.42").asFloats(testKey)).contains(42.42f);
        assertThat(typeMap.putR(testKey, "42.42").asFloatOpt(testKey)).contains(42.42f);
        assertThat(typeMap.putR(testKey, "32000").asShort(testKey)).isEqualTo((short) 32000);
        assertThat(typeMap.putR(testKey, "32000").asShorts(testKey)).contains((short) 32000);
        assertThat(typeMap.putR(testKey, "32000").asShortOpt(testKey)).contains((short) 32000);
        assertThat(typeMap.putR(testKey, "127").asByte(testKey)).isEqualTo((byte) 127);
        assertThat(typeMap.putR(testKey, "127").asBytes(testKey)).contains((byte) 127);
        assertThat(typeMap.putR(testKey, "127").asByteOpt(testKey)).contains((byte) 127);
        assertThat(typeMap.putR(testKey, "12345678901234567890").asBigInteger(testKey)).isEqualTo(new BigInteger("12345678901234567890"));
        assertThat(typeMap.putR(testKey, "12345678901234567890").asBigIntegerOpt(testKey)).contains(new BigInteger("12345678901234567890"));
        assertThat(typeMap.putR(testKey, "12345.67890").asBigDecimal(testKey)).isEqualTo(new BigDecimal("12345.67890"));
        assertThat(typeMap.putR(testKey, "12345.67890").asBigDecimalOpt(testKey)).contains(new BigDecimal("12345.67890"));
        assertThat(typeMap.putR(testKey, "42").asAtomicInteger(testKey)).hasValue(42);
        assertThat(typeMap.putR(testKey, "42").asAtomicIntegerOpt(testKey)).isNotEmpty();
        assertThat(typeMap.putR(testKey, "123456789").asAtomicLong(testKey)).hasValue(123456789L);
        assertThat(typeMap.putR(testKey, "123456789").asAtomicLongOpt(testKey)).isNotEmpty();
        assertThat(typeMap.putR(testKey, "true").asAtomicBoolean(testKey)).isTrue();
        assertThat(typeMap.putR(testKey, "true").asAtomicBooleanOpt(testKey)).isNotEmpty();
        assertThat(typeMap.putR(testKey, "1").asAtomicBoolean(testKey)).isTrue();
        assertThat(typeMap.putR(testKey, "false").asAtomicBoolean(testKey)).isFalse();
        assertThat(typeMap.putR(testKey, "0").asAtomicBoolean(testKey)).isFalse();
        assertThat(typeMap.putR(testKey, UUID.randomUUID().toString()).asUUID(testKey)).isNotNull();
        assertThat(typeMap.putR(testKey, UUID.randomUUID().toString()).asUUIDs(testKey)).isNotNull().hasSize(1);
        assertThat(typeMap.putR(testKey, UUID.randomUUID().toString()).asUUIDOpt(testKey)).isNotEmpty();
        assertThat(typeMap.putR(testKey, "A").asCharacter(testKey)).isEqualTo('A');
        assertThat(typeMap.putR(testKey, "A").asCharacters(testKey)).contains('A');
        assertThat(typeMap.putR(testKey, "A").asCharacterOpt(testKey)).contains('A');
        assertThat(typeMap.putR(testKey, "true").asBoolean(testKey)).isTrue();
        assertThat(typeMap.putR(testKey, "true").asBooleans(testKey)).contains(true);
        assertThat(typeMap.putR(testKey, "true").asBooleanOpt(testKey)).contains(true);
        assertThat(typeMap.putR(testKey, "false").asBoolean(testKey)).isFalse();
        assertThat(typeMap.putR(testKey, "false").asBooleans(testKey)).contains(false);
        assertThat(typeMap.putR(testKey, "false").asBooleanOpt(testKey)).contains(false);
        assertThat(typeMap.putR(testKey, "1").asBoolean(testKey)).isTrue();
        assertThat(typeMap.putR(testKey, "1").asBooleans(testKey)).contains(true);
        assertThat(typeMap.putR(testKey, "1").asBooleanOpt(testKey)).contains(true);
        assertThat(typeMap.putR(testKey, "0").asBoolean(testKey)).isFalse();
        assertThat(typeMap.putR(testKey, "0").asBooleans(testKey)).contains(false);
        assertThat(typeMap.putR(testKey, "0").asBooleanOpt(testKey)).contains(false);
        assertThat(typeMap.putR(testKey, new IllegalStateException("Test Throwable")).asThrowable(testKey)).isInstanceOf(Throwable.class).hasMessage("Test Throwable");
        assertThat(typeMap.putR(testKey, new IllegalStateException("Test Throwable")).asThrowables(testKey)).hasSize(1);
        assertThat(typeMap.putR(testKey, new IllegalStateException("Test Throwable")).asThrowableOpt(testKey)).isNotEmpty();
        assertThat(typeMap.putR(testKey, "UTF-8").asCharset(testKey)).isEqualTo(UTF_8);
        assertThat(typeMap.putR(testKey, "UTF-8").asCharsets(testKey)).contains(UTF_8);
        assertThat(typeMap.putR(testKey, "UTF-8").asCharsetOpt(testKey)).contains(UTF_8);
        assertThat(typeMap.putR(testKey, testPath).asFile(testKey)).isEqualTo(new File(testPath));
        assertThat(typeMap.putR(testKey, testPath).asFiles(testKey)).contains(new File(testPath));
        assertThat(typeMap.putR(testKey, testPath).asFileOpt(testKey)).contains(new File(testPath));
        assertThat(typeMap.putR(testKey, testPath).asPath(testKey)).isEqualTo(Paths.get(testPath));
        assertThat(typeMap.putR(testKey, testPath).asPaths(testKey)).contains(Paths.get(testPath));
        assertThat(typeMap.putR(testKey, testPath).asPathOpt(testKey)).contains(Paths.get(testPath));
        assertThat(typeMap.putR(testKey, testUr).asURI(testKey)).isEqualTo(URI.create(testUr));
        assertThat(typeMap.putR(testKey, testUr).asURIs(testKey)).contains(URI.create(testUr));
        assertThat(typeMap.putR(testKey, testUr).asURIOpt(testKey)).contains(URI.create(testUr));
        assertThat(typeMap.putR(testKey, testUr).asURL(testKey)).isEqualTo(new URL(testUr));
        assertThat(typeMap.putR(testKey, testUr).asURLs(testKey)).contains(new URL(testUr));
        assertThat(typeMap.putR(testKey, testUr).asURLOpt(testKey)).contains(new URL(testUr));
        assertThat(typeMap.putR(testKey, "localhost").asInetAddress(testKey)).isEqualTo(InetAddress.getByName("localhost"));
        assertThat(typeMap.putR(testKey, "localhost").asInetAddresses(testKey)).contains(InetAddress.getByName("localhost"));
        assertThat(typeMap.putR(testKey, "localhost").asInetAddressOpt(testKey)).contains(InetAddress.getByName("localhost"));
        assertThat(typeMap.putR(testKey, "127.0.0.1").asInet4Address(testKey)).isEqualTo(Inet4Address.getByName("127.0.0.1"));
        assertThat(typeMap.putR(testKey, "127.0.0.1").asInet4Addresses(testKey)).contains((Inet4Address) Inet4Address.getByName("127.0.0.1"));
        assertThat(typeMap.putR(testKey, "127.0.0.1").asInet4AddressOpt(testKey)).contains((Inet4Address) Inet4Address.getByName("127.0.0.1"));
        assertThat(typeMap.putR(testKey, "::1").asInet6Address(testKey)).isEqualTo(Inet6Address.getByName("::1"));
        assertThat(typeMap.putR(testKey, "::1").asInet6Addresses(testKey)).contains((Inet6Address) Inet6Address.getByName("::1"));
        assertThat(typeMap.putR(testKey, "::1").asInet6AddressOpt(testKey)).contains((Inet6Address) Inet6Address.getByName("::1"));
        assertThat(typeMap.putR(testKey, String.valueOf(TEST_TIME)).asDate(testKey)).isEqualTo(new Date(TEST_TIME));
        assertThat(typeMap.putR(testKey, String.valueOf(TEST_TIME)).asDates(testKey)).contains(new Date(TEST_TIME));
        assertThat(typeMap.putR(testKey, String.valueOf(TEST_TIME)).asDateOpt(testKey)).contains(new Date(TEST_TIME));
        assertThat(typeMap.putR(testKey, String.valueOf(TEST_TIME)).asInstant(testKey)).isEqualTo(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.putR(testKey, String.valueOf(TEST_TIME)).asInstants(testKey)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.putR(testKey, String.valueOf(TEST_TIME)).asInstantOpt(testKey)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.putR(testKey, calendar).asCalendar(testKey)).isEqualTo(calendar);
        assertThat(typeMap.putR(testKey, calendar).asCalendars(testKey)).hasSize(1);
        assertThat(typeMap.putR(testKey, calendar).asCalendarOpt(testKey)).isNotEmpty();
        assertThat(typeMap.putR(testKey, calendar.getTime().toString()).asCalendar(testKey)).isEqualByComparingTo(calendar);
        assertThat(typeMap.putR(testKey, calendar.getTime().toString()).asCalendars(testKey)).hasSize(1);
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalDateTime(testKey)).isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalDateTimes(testKey)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalDateTimeOpt(testKey)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalDate(testKey)).isEqualTo(LocalDate.ofEpochDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() / (24 * 60 * 60 * 1000)));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalDates(testKey)).contains(LocalDate.ofEpochDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() / (24 * 60 * 60 * 1000)));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalDateOpt(testKey)).contains(LocalDate.ofEpochDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() / (24 * 60 * 60 * 1000)));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalTime(testKey)).isEqualTo(LocalTime.ofNanoOfDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() % (24 * 60 * 60 * 1000) * 1000000));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalTimes(testKey)).contains(LocalTime.ofNanoOfDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() % (24 * 60 * 60 * 1000) * 1000000));
        assertThat(typeMap.putR(testKey, TEST_TIME).asLocalTimeOpt(testKey)).contains(LocalTime.ofNanoOfDay(Instant.ofEpochMilli(TEST_TIME).toEpochMilli() % (24 * 60 * 60 * 1000) * 1000000));
        assertThat(typeMap.putR(testKey, TEST_TIME).asOffsetDateTime(testKey)).isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asOffsetDateTimes(testKey)).contains(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asOffsetDateTimeOpt(testKey)).contains(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asZonedDateTime(testKey)).isEqualTo(ZonedDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asZonedDateTimes(testKey)).contains(ZonedDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asZonedDateTimeOpt(testKey)).contains(ZonedDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
        assertThat(typeMap.putR(testKey, TEST_TIME).asSqlDate(testKey)).isEqualTo(new java.sql.Date(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asSqlDates(testKey)).contains(new java.sql.Date(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asSqlDateOpt(testKey)).contains(new java.sql.Date(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asTime(testKey)).isEqualTo(new Time(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asTimes(testKey)).contains(new Time(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asTimeOpt(testKey)).contains(new Time(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asTimestamp(testKey)).isEqualTo(new Timestamp(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asTimestamps(testKey)).contains(new Timestamp(TEST_TIME));
        assertThat(typeMap.putR(testKey, TEST_TIME).asTimestampOpt(testKey)).contains(new Timestamp(TEST_TIME));
    }

    @Test
    void mapOfTest() {
        assertThat(TypeMap.mapOf((String) null)).isEmpty();
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
    void addTest() {
        final TypeInfo<?> jsonMap = jsonTypeOf("{\n"
            + "  \"outerMap\": {\n"
            + "    \"times\": {\n"
            + "      \"timestamp1\": 1800000000000,\n"
            + "      \"timestamp2\": 1800000000,\n"
            + "      \"date\": \"Fri Jan 15 08:00:00 UTC 2027\",\n"
            + "    },\n"
            + "    \"myList\": [\"BB\",1,true,null,1.2]\n"
            + "  }\n"
            + "}");

        assertThat(jsonMap.addPath()).isFalse();
        assertThat(jsonMap.addPathR("outerMap", 1, 2).asMap("outerMap")).contains(entry(1, 2)).hasSize(3);
        assertThat(jsonMap.addPathR("outerMap", "times", 3, 4).asMap("outerMap", "times")).contains(entry(3, 4)).hasSize(4);
        assertThat(jsonMap.addPathR("outerMap", "myList", 200888).asList("outerMap", "myList")).contains(200888).hasSize(5);

    }

    @Test
    void setTest() {
        final TypeInfo<?> jsonMap = jsonTypeOf("{\n"
            + "  \"outerMap\": {\n"
            + "    \"times\": {\n"
            + "      \"timestamp1\": 1800000000000,\n"
            + "      \"timestamp2\": 1800000000,\n"
            + "      \"date\": \"Fri Jan 15 08:00:00 UTC 2027\",\n"
            + "    },\n"
            + "    \"myList\": [\"BB\",1,true,null,1.2]\n"
            + "  }\n"
            + "}");

        assertThat(jsonMap.setPath()).isFalse();
        assertThat(jsonMap.setPathR("outerMap", "myList", 2, 4).asList("outerMap", "myList")).isEqualTo(asList("BB", 1L, 4, 1.2));
        assertThat(jsonMap.setPathR("outerMap", "myList", 3, "three").asList("outerMap", "myList")).isEqualTo(asList("BB", 1L, 4, "three", 1.2));
        assertThat(jsonMap.setPath("outerMap", "myList", 5, "invalid")).isFalse();
        assertThat(jsonMap.setPathR("outerMap", "myList", 5, "invalid").asList("outerMap", "myList")).isEqualTo(asList("BB", 1L, 4, "three", 1.2));
        assertThat(jsonMap.setPathR("outerMap", "myList", asList("AA", "BB")).asList("outerMap", "myList")).isEqualTo(asList("AA", "BB"));
        assertThat(jsonMap.setPathR("outerMap", 5).asMap()).contains(entry("outerMap", 5)).hasSize(1);
    }

    @Test
    void putTest() {
        final TypeInfo<?> jsonMap = jsonTypeOf("{\n"
            + "  \"outerMap\": {\n"
            + "    \"times\": {\n"
            + "      \"timestamp1\": 1800000000000,\n"
            + "      \"timestamp2\": 1800000000,\n"
            + "      \"date\": \"Fri Jan 15 08:00:00 UTC 2027\",\n"
            + "    },\n"
            + "    \"myList\": [\"BB\",1,true,null,1.2]\n"
            + "  }\n"
            + "}");

        assertThat(jsonMap.putPath()).isFalse(); // can't append with just on value it needs key and value
        assertThat(jsonMap.putPath(1)).isFalse(); // can't append with just on value it needs key and value
        assertThat(jsonMap.putPath("outerMap", "myList", "BB", "CC")).isFalse(); // can't append to non map item
        assertThat(jsonMap.putPathR("outerMap", "myList", asList("AA", "BB")).asList("outerMap", "myList")).isEqualTo(asList("AA", "BB"));
        assertThat(jsonMap.putPathR(3, 4).asMap()).containsEntry(3, 4).hasSize(2);
        assertThat(jsonMap.putPathR("outerMap", "times", 1, 2).asMap("outerMap", "times")).containsEntry(1, 2).hasSize(4);
        assertThat(jsonMap.putPathR(5, 6).asMap()).containsEntry(5, 6).hasSize(3);
        assertThat(jsonMap.putPathR("outerMap", new Pair<>("EE", "FF")).putPathR("outerMap", "EE", "GG").asMap("outerMap")).containsEntry("EE", "GG").hasSize(3);
    }

    @Test
    void containsTest() {
        final TypeInfo<?> jsonMap = jsonTypeOf("{\n"
            + "  \"outerMap\": {\n"
            + "    \"times\": {\n"
            + "      \"timestamp1\": 1800000000000,\n"
            + "      \"timestamp2\": 1800000000,\n"
            + "      \"date\": \"Fri Jan 15 08:00:00 UTC 2027\",\n"
            + "    },\n"
            + "    \"myList\": [\"BB\",1,true,null,1.2]\n"
            + "  }\n"
            + "}");

        assertThat(jsonMap.containsPath()).isFalse();
        assertThat(jsonMap.containsPath("outerMap")).isTrue();
        assertThat(jsonMap.containsPath("invalid")).isFalse();
        assertThat(jsonMap.containsPath("outerMap", "myList", "BB", "CC")).isFalse();
        assertThat(jsonMap.containsPath("outerMap", "myList")).isTrue();
        assertThat(jsonMap.containsPath("outerMap", "myList", 1.2d)).isTrue();
        assertThat(jsonMap.containsPath("outerMap", "myList", 42)).isFalse();
    }

    @Test
    void showCaseTest() {

        // Converter
        final Date date = convertObj("Fri Jan 15 08:00:00 UTC 2027", Date.class);

        // TypeMap
        final TypeMap map = new TypeMap();
        map.put("key", new Date(TEST_TIME));
        final Type<Calendar> calendar = map.asOpt(Calendar.class, "key");
        final Type<LocalDateTime> localDateTime = map.asOpt(LocalDateTime.class, "key");
        final Type<ZonedDateTime> zonedDateTime = map.asOpt(ZonedDateTime.class, "key");

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

        final Type<Date> myDate1 = jsonMap.asOpt(Date.class, "outerMap", "times", "timestamp1");
        final Type<Date> myDate2 = jsonMap.asOpt(Date.class, "outerMap", "times", "timestamp2");
        final Type<Date> myDate3 = jsonMap.asOpt(Date.class, "outerMap", "times", "date");
        final Type<Long> myTimestamp = jsonMap.asOpt(Long.class, "outerMap", "times", "timestamp1");
        final Type<TestEnum> myEnum = jsonMap.asOpt(TestEnum.class, "outerMap", "myList", 0);
        final Type<Boolean> myBoolean = jsonMap.asOpt(Boolean.class, "outerMap", "myList", 2);

        final String myJson = jsonMap.toJson();

        // Assertions
        assertThat(calendar.value()).isNotNull();
        assertThat(localDateTime.value()).isNotNull();
        assertThat(zonedDateTime.value()).isNotNull();
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
    void istPresentTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.isPresent("key")).isTrue();
        assertThat(typeMap.isPresent("invalid")).isFalse();
        assertThat(typeMap.isPresent("key", 0)).isTrue();
        assertThat(typeMap.isPresent("key", 1)).isTrue();
        assertThat(typeMap.isPresent("key", 2)).isFalse();
        assertThat(typeMap.isPresent("key", "value1")).isTrue();
    }

    @Test
    void isEmptyTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.isEmpty()).isFalse();
        assertThat(typeMap.isEmpty("key")).isFalse();
        assertThat(typeMap.isEmpty("invalid")).isTrue();
        assertThat(typeMap.isEmpty("key", 0)).isFalse();
        assertThat(typeMap.isEmpty("key", 1)).isFalse();
        assertThat(typeMap.isEmpty("key", 2)).isTrue();
        assertThat(typeMap.isEmpty("key", "value1")).isFalse();
    }

    @Test
    void ifPresentTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        typeMap.ifPresent(type -> assertThat(type).isNotNull().isNotEmpty(), "key");
        typeMap.ifPresent(type -> fail("Invalid key"), "invalid");
        typeMap.ifPresent(type -> assertThat(type).isNotNull().isNotEmpty(), "key", 0);
        typeMap.ifPresent(type -> assertThat(type).isNotNull().isNotEmpty(), "key", 1);
        typeMap.ifPresent(type -> fail("Invalid key"), "key", 2);
        typeMap.ifPresent(type -> assertThat(type).isNotNull().isNotEmpty(), "key", "value1");
        typeMap.ifPresent(type -> assertThat(type).isNotNull().isNotEmpty(), "key", "value2");
        typeMap.ifPresent(type -> fail("Invalid key"), "key", "value3");
    }

    @Test
    void ifPresentOrElseTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        typeMap.ifPresentOrElse(type -> assertThat(type).isNotNull().isNotEmpty(), () -> fail("Invalid key"), "key");
        typeMap.ifPresentOrElse(type -> fail("Invalid key"), () -> assertThat(typeMap).isNotNull().isNotEmpty(), "invalid");
        typeMap.ifPresentOrElse(type -> assertThat(type).isNotNull().isNotEmpty(), () -> fail("Invalid key"), "key", 0);
        typeMap.ifPresentOrElse(type -> assertThat(type).isNotNull().isNotEmpty(), () -> fail("Invalid key"), "key", 1);
        typeMap.ifPresentOrElse(type -> fail("Invalid key"), () -> assertThat(typeMap).isNotNull().isNotEmpty(), "key", 2);
        typeMap.ifPresentOrElse(type -> assertThat(type).isNotNull().isNotEmpty(), () -> fail("Invalid key"), "key", "value1");
        typeMap.ifPresentOrElse(type -> assertThat(type).isNotNull().isNotEmpty(), () -> fail("Invalid key"), "key", "value2");
        typeMap.ifPresentOrElse(type -> fail("Invalid key"), () -> assertThat(typeMap).isNotNull().isNotEmpty(), "key", "value3");
    }

    @Test
    void filterTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.filter(null).value()).isNull();
        assertThat(typeMap.filter(type -> false).value()).isNull();
        assertThat(typeMap.filter(type -> true)).isNotNull().isNotEmpty();
        assertThat(typeMap.filter(type -> type.isPresent(), "key")).isNotNull().isNotEmpty();
        assertThat(typeMap.filter(type -> type.isPresent(), "key", 0)).isNotNull().isNotEmpty();
        assertThat(typeMap.filter(type -> type.isPresent(), "key", 1)).isNotNull().isNotEmpty();
        assertThat(typeMap.filter(type -> type.isPresent(), "key", 2).value()).isNull();
        assertThat(typeMap.filter(type -> type.isPresent(), "key", "value1")).isNotNull().isNotEmpty();
        assertThat(typeMap.filter(type -> type.isPresent(), "key", "value2")).isNotNull().isNotEmpty();
        assertThat(typeMap.filter(type -> type.isPresent(), "key", "value3").value()).isNull();
    }

    @Test
    void mapTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.map(null).value()).isNull();
        assertThat(typeMap.map(type -> type)).isNotNull().isNotEmpty();
        assertThat(typeMap.map(type -> type, "key")).isNotNull().isNotEmpty();
        assertThat(typeMap.map(type -> type, "key", 0)).isNotNull().isNotEmpty();
        assertThat(typeMap.map(type -> type, "key", 1)).isNotNull().isNotEmpty();
        assertThat(typeMap.map(type -> type, "key", 2).value()).isNull();
        assertThat(typeMap.map(type -> type, "key", "value1")).isNotNull().isNotEmpty();
        assertThat(typeMap.map(type -> type, "key", "value2")).isNotNull().isNotEmpty();
        assertThat(typeMap.map(type -> type, "key", "value3").value()).isNull();
        assertThat(typeMap.map(type -> 200888, "key", "value2").value()).isEqualTo(200888);
    }

    @Test
    void flatMapTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.flatMap(null).value()).isNull();
        assertThat(typeMap.flatMap(type -> type)).isNotNull().isNotEmpty();
        assertThat(typeMap.flatMap(type -> type, "key")).isNotNull().isNotEmpty();
        assertThat(typeMap.flatMap(type -> type, "key", 0)).isNotNull().isNotEmpty();
        assertThat(typeMap.flatMap(type -> type, "key", 1)).isNotNull().isNotEmpty();
        assertThat(typeMap.flatMap(type -> type, "key", 2).value()).isNull();
        assertThat(typeMap.flatMap(type -> type, "key", "value1")).isNotNull().isNotEmpty();
        assertThat(typeMap.flatMap(type -> type, "key", "value2")).isNotNull().isNotEmpty();
        assertThat(typeMap.flatMap(type -> type, "key", "value3").value()).isNull();
        assertThat(typeMap.flatMap(type -> new Type<>(200888), "key", "value2").value()).isEqualTo(200888);
    }

    @Test
    void flatOptTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.flatOpt(null).value()).isNull();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()))).isNotNull().isNotEmpty();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()), "key")).isNotNull().isNotEmpty();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()), "key", 0)).isNotNull().isNotEmpty();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()), "key", 1)).isNotNull().isNotEmpty();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()), "key", 2).value()).isNull();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()), "key", "value1")).isNotNull().isNotEmpty();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()), "key", "value2")).isNotNull().isNotEmpty();
        assertThat(typeMap.flatOpt(type -> Optional.of(type.value()), "key", "value3").value()).isNull();
        assertThat(typeMap.flatOpt(type -> ofNullable(200888), "key", "value2").value()).isEqualTo(200888);
    }

    @Test
    void orTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.or((Supplier<?>) null)).isEqualTo(typeOf(typeMap));
        assertThat(typeMap.or(() -> new TypeMap())).isNotNull().isNotEmpty();
        assertThat(typeMap.or(() -> new TypeList().addR("OR"), "key")).isNotNull().isNotEmpty();
        assertThat(typeMap.or(() -> "OR", "key", 0)).isNotNull().isNotEmpty();
        assertThat(typeMap.or(() -> "OR", "key", 1)).isNotNull().isNotEmpty();
        assertThat(typeMap.or(() -> new TypeMap().putR("OR", "OR"), "key", 2)).isEqualTo(typeOf(new TypeMap().putR("OR", "OR")));
        assertThat(typeMap.or(() -> "OR", "key", "value1")).isNotNull().isNotEmpty();
        assertThat(typeMap.or(() -> "OR", "key", "value2")).isNotNull().isNotEmpty();
        assertThat(typeMap.or(() -> "OR", "key", "value3")).isEqualTo(typeOf("OR"));
        typeMap.asOpt("key", "value3").or(() -> "OR");
    }

    @Test
    void streamTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.stream()).isNotNull().containsExactly(typeOf(typeMap));
        assertThat(typeMap.stream("key")).containsExactly(typeOf("value1"), typeOf("value2"));
        assertThat(typeMap.stream("key", 0)).containsExactly(typeOf("value1"));
        assertThat(typeMap.stream("key", 1)).containsExactly(typeOf("value2"));
        assertThat(typeMap.stream("key", 2)).isEmpty();
        assertThat(typeMap.stream("key", "value1")).containsExactly(typeOf("value1"));
        assertThat(typeMap.stream("key", "value2")).containsExactly(typeOf("value2"));
        assertThat(typeMap.stream("key", "value3")).isEmpty();
        assertThat(new TypeMap().putR("key", new String[]{"value1", "value2"}).stream("key")).containsExactly(typeOf("value1"), typeOf("value2"));
    }

    @Test
    void orElse() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.orElse(null)).isEqualTo(typeMap);
        assertThat(typeMap.orElse(new TypeMap())).isEqualTo(typeMap);
        assertThat(typeMap.orElse(new TypeList().addR("OR"), "key")).isEqualTo(new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.orElse("OR", "key", 0)).isEqualTo("value1");
        assertThat(typeMap.orElse("OR", "key", 1)).isEqualTo("value2");
        assertThat(typeMap.orElse(new TypeMap().putR("OR", "OR"), "key", 2)).isEqualTo(new TypeMap().putR("OR", "OR"));
        assertThat(typeMap.orElse("OR", "key", "value1")).isEqualTo("value1");
        assertThat(typeMap.orElse("OR", "key", "value2")).isEqualTo("value2");
        assertThat(typeMap.orElse("OR", "key", "value3")).isEqualTo("OR");
        typeMap.asOpt("key", "value3").orElse("OR");
    }

    @Test
    void orElseGetTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.orElseGet(null)).isEqualTo(typeMap);
        assertThat(typeMap.orElseGet(() -> new TypeMap())).isEqualTo(typeMap);
        assertThat(typeMap.orElseGet(() -> new TypeList().addR("OR"), "key")).isEqualTo(new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.orElseGet(() -> "OR", "key", 0)).isEqualTo("value1");
        assertThat(typeMap.orElseGet(() -> "OR", "key", 1)).isEqualTo("value2");
        assertThat(typeMap.orElseGet(() -> new TypeMap().putR("OR", "OR"), "key", 2)).isEqualTo(new TypeMap().putR("OR", "OR"));
        assertThat(typeMap.orElseGet(() -> "OR", "key", "value1")).isEqualTo("value1");
        assertThat(typeMap.orElseGet(() -> "OR", "key", "value2")).isEqualTo("value2");
        assertThat(typeMap.orElseGet(() -> "OR", "key", "value3")).isEqualTo("OR");
        typeMap.asOpt("key", "value3").orElseGet(() -> "OR");
    }

    @Test
    void orElseThrowTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.orElseThrow(null)).isEqualTo(typeMap);
        assertThat(typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"))).isEqualTo(typeMap);
        assertThat(typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"), "key")).isEqualTo(new TypeList().addR("value1").addR("value2"));
        assertThat(typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"), "key", 0)).isEqualTo("value1");
        assertThat(typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"), "key", 1)).isEqualTo("value2");
        assertThat(typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"), "key", "value1")).isEqualTo("value1");
        assertThat(typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"), "key", "value2")).isEqualTo("value2");

        assertThatThrownBy(() -> typeMap.orElseThrow("key", 2)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"), "key", 2)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> typeMap.orElseThrow("key", "value3")).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> typeMap.orElseThrow(() -> new IllegalStateException("Invalid key"), "key", "value3")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void toTypeTest() {
        assertThat(TypeInfo.toType(null)).isEqualTo(typeOf(null));
        assertThat(TypeInfo.toType("")).isEqualTo(typeOf(""));
        assertThat(TypeInfo.toType(1)).isEqualTo(typeOf(1));
        assertThat(TypeInfo.toType(typeOf(1L))).isEqualTo(typeOf(1L));
        assertThat(TypeInfo.toType(Optional.of(1L))).isEqualTo(typeOf(1L));
    }

    @Test
    void treeGetTest() {
        final TypeMap typeMap = new TypeMap();
        assertThat(treeGet(typeMap.putR("key", "value"), "key")).isEqualTo("value");
        assertThat(treeGet(typeMap.putR("key", "value"), "invalid")).isNull();
        assertThat(treeGet(typeMap.putR("key", new String[]{"value1", "value2"}), "key")).isEqualTo(new String[]{"value1", "value2"});
        assertThat(treeGet(typeMap.putR("key", new String[]{"value1", "value2"}), "key", 1)).isEqualTo("value2");
        assertThat(treeGet(typeMap.putR("key", new String[]{"value1", "value2"}), "key", 2)).isNull();
        assertThat(treeGet(typeMap.putR("key", new String[]{"value1", "value2"}), "key", "value1")).isEqualTo("value1");
        assertThat(treeGet(typeMap.putR("key", asList("value1", "value2")), "key")).isEqualTo(asList("value1", "value2"));
        assertThat(treeGet(typeMap.putR("key", asList("value1", "value2")), "key", 1)).isEqualTo("value2");
        assertThat(treeGet(typeMap.putR("key", asList("value1", "value2")), "key", 2)).isNull();
        assertThat(treeGet(typeMap.putR("key", asList("value1", "value2")), "key", "value1")).isEqualTo("value1");
        assertThat(treeGet(typeMap.putR("key", Optional.of("AA")), "key", "AA")).isEqualTo("AA");
        assertThat(treeGet(typeMap.putR("key", typeOf("BB")), "key", "BB")).isEqualTo("BB");
        assertThat(treeGet(typeMap.putR("key", new Pair<>("AA", "BB")), "key", "AA")).isEqualTo("BB");
        assertThat(treeGet(typeMap.putR("key", new Pair<>("AA", "BB")), "key", "CC")).isNull();
    }

    @Test
    void shouldReadJsonFromStringPathAndStream() throws Exception {
        final Path jsonPath = Files.createTempFile("typemap-json", ".json");
        Files.writeString(jsonPath, "{\"name\":\"alpha\",\"number\":7}");

        final TypeMapI<?> fromJsonString = TypeMap.fromJson("{\"name\":\"alpha\",\"number\":7}");
        final TypeMapI<?> fromJsonCharSeq = TypeMap.fromJson((CharSequence) "{\"name\":\"char\",\"number\":8}");
        final TypeMapI<?> fromJsonPath = TypeMap.fromJson(jsonPath);
        try (InputStream stream = Files.newInputStream(jsonPath)) {
            assertThat(TypeMap.fromJson(stream).asString("name")).isEqualTo("alpha");
        }
        assertThat(fromJsonString.asInt("number")).isEqualTo(7);
        assertThat(fromJsonCharSeq.asInt("number")).isEqualTo(8);
        assertThat(fromJsonPath.toJson()).contains("\"name\":\"alpha\"");
    }

    @Test
    void shouldStreamJsonObjectEntries() throws Exception {
        final String json = "{\"a\":1,\"b\":\"c\"}";
        try (Stream<Pair<String, Object>> stream = streamJsonObject(json)) {
            final LinkedHashMap<String, Object> map = stream.collect(LinkedHashMap::new, (m, p) -> m.put(p.getKey(), p.getValue()), Map::putAll);
            assertThat(map).containsExactly(entry("a", 1L), entry("b", "c"));
        }
        try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
             Stream<Pair<String, Object>> s = streamJsonObject(stream)) {
            final LinkedHashMap<String, Object> map = s.collect(LinkedHashMap::new, (m, p) -> m.put(p.getKey(), p.getValue()), Map::putAll);
            assertThat(map).containsExactly(entry("a", 1L), entry("b", "c"));
        }
    }

    @Test
    void shouldExposeTypeInfoConvenienceOnStreamedPairs() throws Exception {
        final String json = "{\"num\":\"7\",\"flag\":\"true\"}";
        try (Stream<Pair<String, Object>> stream = streamJsonObject(json)) {
            final LinkedHashMap<String, Object> converted = stream.collect(LinkedHashMap::new, (m, p) -> {
                if ("num".equals(p.key())) {
                    m.put(p.key(), p.valueType().asInt());
                } else if ("flag".equals(p.key())) {
                    m.put(p.key(), p.valueInfo().asBoolean());
                }
            }, Map::putAll);
            assertThat(converted).containsExactly(entry("num", 7), entry("flag", true));
        }
    }

    @Test
    void shouldStreamPairsFromTypeMapInstance() {
        final TypeMap map = new TypeMap();
        map.put("a", 1);
        map.put("b", "two");
        assertThat(map.streamPairs().toList())
            .extracting(Pair::key, Pair::value)
            .containsExactlyInAnyOrder(tuple("a", 1), tuple("b", "two"));
        assertThat(map.streamPairs(Integer.class).toList())
            .extracting(Pair::value)
            .containsExactlyInAnyOrder(1, null);
        assertThat(map.streamPairs("missing")).isEmpty();
    }

    @Test
    void shouldStreamNestedPairsFromXml() {
        final String xml = """
            <root>
              <user id="7">
                <name>neo</name>
                <roles>
                  <role>admin</role>
                  <role>ops</role>
                </roles>
              </user>
              <active>true</active>
            </root>
            """;
        final TypeMapI<?> map = TypeMap.fromXml(xml);

        assertThat(map.streamPairs().toList())
            .extracting(Pair::key)
            .containsExactly("root");

        final List<Pair<String, Object>> rootChildren = map.streamPairs("root")
            .map(p -> new Pair<>(String.valueOf(p.key()), p.value()))
            .toList();
        assertThat(rootChildren).hasSize(2);
        final List<Pair<?, ?>> rootPairs = rootChildren.stream()
            .map(pair -> (Pair<?, ?>) pair.value())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        final TypeList userContent = (TypeList) rootPairs.stream()
            .filter(p -> "user".equals(p.getKey()))
            .findFirst()
            .map(Pair::getValue)
            .orElseThrow();
        final Object activeValue = rootPairs.stream()
            .filter(p -> "active".equals(p.getKey()))
            .findFirst()
            .map(Pair::getValue)
            .orElse(null);

        assertThat(activeValue).isInstanceOf(TypeList.class);
        assertThat((TypeList) activeValue).contains("true");
        final List<Pair<?, ?>> userEntries = userContent.streamPairs()
            .map(pair -> (Pair<?, ?>) pair.value())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        final List<String> userKeys = userEntries.stream()
            .map(entry -> String.valueOf(entry.getKey()))
            .collect(Collectors.toList());
        assertThat(userKeys).containsExactlyInAnyOrder("name", "roles", "@id");

        final TypeList roles = (TypeList) userEntries.stream()
            .filter(p -> "roles".equals(p.getKey()))
            .findFirst()
            .map(Pair::getValue)
            .orElseThrow();

        final List<String> roleValues = roles.streamPairs()
            .map(Pair::value)
            .filter(Objects::nonNull)
            .map(val -> val instanceof Pair<?, ?> pair ? pair.getValue() : val)
            .flatMap(val -> val instanceof TypeList list ? list.stream().map(String::valueOf) : Stream.of(String.valueOf(val)))
            .collect(Collectors.toList());
        assertThat(roleValues).containsExactly("admin", "ops");
    }

    @Test
    void shouldReadXmlFromStringPathAndStream() throws Exception {
        final String xml = "<root><item id=\"9\">value</item></root>";
        final Path xmlPath = Files.createTempFile("typemap-xml", ".xml");
        Files.writeString(xmlPath, xml);

        final TypeMapI<?> fromString = TypeMap.fromXml(xml);
        final TypeMapI<?> fromCharSeq = TypeMap.fromXml((CharSequence) xml);
        final TypeMapI<?> fromPath = TypeMap.fromXml(xmlPath);
        try (InputStream stream = Files.newInputStream(xmlPath)) {
            assertThat(TypeMap.fromXml(stream).toXML()).satisfies(out -> assertThat(normalizeXml(out)).isEqualTo(normalizeXml(xml)));
        }
        assertThat(normalizeXml(fromString.toXML())).isEqualTo(normalizeXml(xml));
        assertThat(normalizeXml(fromCharSeq.toXML())).isEqualTo(normalizeXml(xml));
        assertThat(normalizeXml(fromPath.toXML())).isEqualTo(normalizeXml(xml));
        assertThat(fromString.get("root")).isInstanceOf(TypeList.class);
    }

    @Test
    void shouldReadArgsFromStringArrayPathAndStream() throws Exception {
        final String argsString = "--name=neo -v";
        final String[] argsArray = new String[]{"--name=neo", "-v"};
        final Path argsPath = Files.createTempFile("typemap-args", ".txt");
        Files.writeString(argsPath, "--name=trinity -count=3");

        assertThat((TypeSet) TypeMap.fromArgs(argsString).get("name")).contains("neo");
        assertThat((TypeSet) TypeMap.fromArgs((CharSequence) argsString).get("name")).contains("neo");
        assertThat((TypeSet) TypeMap.fromArgs(argsArray).get("name")).contains("neo");
        assertThat((TypeSet) TypeMap.fromArgs(argsPath).get("count")).contains("3");
        try (InputStream stream = Files.newInputStream(argsPath)) {
            assertThat((TypeSet) TypeMap.fromArgs(stream).get("name")).contains("trinity");
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "perf", matches = "true")
    void benchmarkStreamPairsFromXml() {
        final String xml = largeXml(3000);
        final TypeMapI<?> map = TypeMap.fromXml(xml);
        final long memBefore = usedMemoryKb();
        final long start = System.nanoTime();
        final AtomicInteger itemCount = new AtomicInteger();

        map.streamPairs("root")
            .map(p -> (Pair<?, ?>) p.value())
            .filter(Objects::nonNull)
            .filter(p -> "item".equals(p.getKey()))
            .forEach(p -> itemCount.incrementAndGet());

        final long durationMs = (System.nanoTime() - start) / 1_000_000;
        final long memDelta = usedMemoryKb() - memBefore;
        System.out.printf("benchmarkStreamPairsFromXml items=%d took=%dms memDelta=%dKB%n", itemCount.get(), durationMs, memDelta);
        assertThat(itemCount.get()).isEqualTo(3000);
    }

    @Test
    void shouldHandleBrokenOrEmptyInputsGracefully() {
        assertThat(TypeMap.fromJson("borken")).isNotNull();
        assertThat(TypeMap.fromJson((String) null)).isEmpty();
        assertThat(TypeMap.fromXml("<<<not-xml>>")).isEmpty();
        assertThat(TypeMap.fromArgs((String) null)).isEmpty();
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

    private static String normalizeXml(final String xml) {
        return xml == null ? "" : xml.replaceAll(">\\s+<", "><").trim();
    }

    private static String largeXml(final int size) {
        final StringBuilder builder = new StringBuilder(size * 48).append("<root>");
        for (int i = 0; i < size; i++) {
            builder.append("<item id=\"").append(i).append("\"><name>n").append(i).append("</name><value>").append(i * 2).append("</value></item>");
        }
        builder.append("</root>");
        return builder.toString();
    }

    private static long usedMemoryKb() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }
}
