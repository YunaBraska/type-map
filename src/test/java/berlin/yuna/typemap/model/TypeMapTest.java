package berlin.yuna.typemap.model;


import berlin.yuna.typemap.config.TypeConversionRegister;
import berlin.yuna.typemap.logic.TypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class TypeMapTest {

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
        typeMap.put("myKey", myTime);

        // TREE MAP
        assertThat(typeMap.gett(Instant.class, "myKey")).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.gett(LocalTime.class, "myKey")).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeMap.get(OffsetDateTime.class, "myKey")).isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));

        // KEY MAP
        assertThat(typeMap.gett("myKey", Instant.class)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.gett("myKey", LocalTime.class)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeMap.get( "myKey", OffsetDateTime.class)).isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()));
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void enumConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        typeMap.put("myKey", "BB");
        assertThat(typeMap.gett(TestEnum.class, "myKey")).contains(TestEnum.BB);
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void collectionConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date(TEST_TIME).toString();
        typeMap.putt("myKey1", myTime);
        typeMap.put("myKey2", new String[]{"1", "2", "3"});

        // TREE MAP
        final List<Instant> instantList1 = typeMap.getList(ArrayList::new, Instant.class, "myKey1");
        final List<Integer> integerList1 = typeMap.getList(ArrayList::new, Integer.class, "myKey2");
        final List<Float> floatList1 = typeMap.getList(ArrayList::new, Float.class, "myKey2");
        final Double[] doubleArray1 = typeMap.getArray(new Double[0], Double.class, "myKey2");
        final Long[] longArray1 = typeMap.getArray(Long[]::new, Long.class, "myKey2");

        assertThat(instantList1).isNotEmpty();
        assertThat(integerList1).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList1).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray1).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray1).isNotEmpty().containsExactly(1L, 2L, 3L);
        assertThat(typeMap.getList("myKey2")).isNotEmpty().containsExactly("1", "2", "3");
        assertThat(typeMap.getList(Integer.class, "myKey2")).isNotEmpty().containsExactly(1, 2, 3);

        // KEY MAP
        final List<Instant> instantList2 = typeMap.getList("myKey1", ArrayList::new, Instant.class);
        final List<Integer> integerList2 = typeMap.getList("myKey2", ArrayList::new, Integer.class);
        final List<Float> floatList2 = typeMap.getList("myKey2", ArrayList::new, Float.class);
        final Double[] doubleArray2 = typeMap.getArray("myKey2", new Double[0], Double.class);
        final Long[] longArray2 = typeMap.getArray("myKey2", Long[]::new, Long.class);

        assertThat(instantList2).isNotEmpty();
        assertThat(integerList2).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList2).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray2).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray2).isNotEmpty().containsExactly(1L, 2L, 3L);
        assertThat(typeMap.getList("myKey2", Integer.class)).isNotEmpty().containsExactly(1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void mapConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final Map<Integer, Date> input = new HashMap<>();
        input.put(6, new Date(TEST_TIME));
        typeMap.put("myKey", input);

        // TREE MAP
        assertThat(typeMap.getMap( "myKey")).containsEntry(6, new Date(TEST_TIME));
        assertThat(typeMap.getMap(Long.class, Instant.class, "myKey")).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.getMap(() -> new HashMap<>(), Long.class, Instant.class, "myKey")).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.getMap(() -> new HashMap<>(), Long.class, Instant.class, "myKey2")).isEmpty();

        // KEY MAP
        assertThat(typeMap.getMap("myKey", () -> new HashMap<>(), Long.class, Instant.class)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.getMap("myKey", Long.class, Instant.class)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeMap.getMap("myKey2", () -> new HashMap<>(), Long.class, Instant.class)).isEmpty();
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

        assertThat(typeMap.gett(Object.class)).isEmpty();
        assertThat(typeMap.gett(Object.class, (Object) null)).isEmpty();
        assertThat(typeMap.gett(Object.class, new Object[]{null})).isEmpty();
        assertThat(typeMap.gett(Object.class, "AA")).contains(innerMap);
        assertThat(typeMap.gett(Object.class, "AA", "BB")).contains(asList("11", "22"));
        assertThat(typeMap.gett(Object.class, "AA", "CC")).contains(new Object[]{"33", "44"});
        assertThat(typeMap.gett(Object.class, "AA", "BB", 0)).contains("11");
        assertThat(typeMap.gett(Object.class, "AA", "BB", 1)).contains("22");
        assertThat(typeMap.gett(Object.class, "AA", "CC", 0)).contains("33");
        assertThat(typeMap.gett(Object.class, "AA", "CC", 1)).contains("44");
        assertThat(typeMap.gett(UnknownClass.class, "AA", "DD")).contains(anObject);
        assertThat(typeMap.gett(UnknownClass.class, "AA", "DD", anObject)).isEmpty();
    }

    @Test
    void testDefaultMapMethods() {
        final String myTime = new Date(TEST_TIME).toString();

        // Broken json
        assertThat(new TypeMap("{ broken json")).containsEntry("", "{ broken json");
        assertThat(new LinkedTypeMap("{ broken json")).containsEntry("", "{ broken json");
        assertThat(new ConcurrentTypeMap("{ broken json")).containsEntry("", "{ broken json");

        final TypeMap map1 = new TypeMap().putt("myKey", myTime);
        final LinkedTypeMap map2 = new LinkedTypeMap().putt("myKey", myTime);
        final ConcurrentTypeMap map3 = new ConcurrentTypeMap().putt("myKey", myTime);

        // Get
        assertThat(map1.get("myKey")).isNotNull();
        assertThat(map2.get("myKey")).isNotNull();
        assertThat(map3.get("myKey")).isNotNull();
    }

    @Test
    void showCaseTest() {

        // Converter
        final Date date = TypeConverter.convertObj("Sat Nov 11 16:12:29 CET 2023", Date.class);

        // TypeMap
        final TypeMap map = new TypeMap();
        map.put("key", new Date(TEST_TIME));
        final Optional<Calendar> calendar = map.gett("key", Calendar.class);
        final Optional<LocalDateTime> localDateTime = map.gett("key", LocalDateTime.class);
        final Optional<ZonedDateTime> zonedDateTime = map.gett("key", ZonedDateTime.class);

        // Register custom conversion
        TypeConversionRegister.registerTypeConvert(UnknownClass.class, Double.class, source -> 99d);

        // JSON - Encode/Decode & Convert
        final String jsonString = "{\n"
            + "  \"outerMap\": {\n"
            + "    \"innerMap\": {\n"
            + "      \"timestamp\": 1800000000000,\n"
            + "    },\n"
            + "    \"myList\": [\"BB\",1,true,null,1.2]\n"
            + "  }\n"
            + "}";

        final TypeMap jsonMap = new TypeMap(jsonString);
        final LinkedTypeMap map1 = jsonMap.getMap( "outerMap", "innerMap");
        final TestEnum testEnum = jsonMap.getList("outerMap", "myList").get(0, TestEnum.class);


        final Optional<Date> myDate = jsonMap.gett(Date.class, "outerMap", "innerMap", "timestamp");
        final Optional<Long> myTimestamp = jsonMap.gett(Long.class, "outerMap", "innerMap", "timestamp");
        final Optional<TestEnum> myEnum = jsonMap.gett(TestEnum.class, "outerMap", "myList", 0);
        final Optional<Boolean> myBoolean = jsonMap.gett(Boolean.class, "outerMap", "myList", 2);

        final String myJson = jsonMap.toJson();


        System.out.println(date + calendar.toString() + localDateTime.toString() + zonedDateTime.toString());
        System.out.println(jsonMap.toString() + myDate + myTimestamp + myEnum + myBoolean + myJson + map1 + testEnum);
    }
}
