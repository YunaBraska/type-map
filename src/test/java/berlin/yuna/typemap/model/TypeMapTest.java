package berlin.yuna.typemap.model;


import berlin.yuna.typemap.config.TypeConversionRegister;
import berlin.yuna.typemap.logic.TypeConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class TypeMapTest {

    static Stream<Arguments> typeMapProvider() {
        return Stream.of(
            Arguments.of("TypeMap", new TypeMap()),
            Arguments.of("LinkedTypeMap", new LinkedTypeMap()),
            Arguments.of("ConcurrentTypeMap", new ConcurrentTypeMap())
        );
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void simpleConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date().toString();
        typeMap.put("myKey", myTime);

        // TREE MAP
        assertThat(typeMap.get(Instant.class, "myKey")).isPresent();
        assertThat(typeMap.get(LocalTime.class, "myKey")).isPresent();

        // KEY MAP
        assertThat(typeMap.get("myKey", Instant.class)).isPresent();
        assertThat(typeMap.get("myKey", LocalTime.class)).isPresent();
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void enumConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        typeMap.put("myKey", "BB");
        final TestEnum testEnum = typeMap.get(TestEnum.class, "myKey").orElse(null);
        assertThat(testEnum).isEqualTo(TestEnum.BB);
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void collectionConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date().toString();
        typeMap.put("myKey1", myTime);
        typeMap.put("myKey2", new String[]{"1", "2", "3"});

        // TREE MAP
        final List<Instant> instantList1 = typeMap.get(ArrayList::new, Instant.class, "myKey1");
        final List<Integer> integerList1 = typeMap.get(ArrayList::new, Integer.class, "myKey2");
        final List<Float> floatList1 = typeMap.get(ArrayList::new, Float.class, "myKey2");
        final Double[] doubleArray1 = typeMap.getArray(new Double[0], Double.class, "myKey2");
        final Long[] longArray1 = typeMap.getArray(Long[]::new, Long.class, "myKey2");

        assertThat(instantList1).isNotEmpty();
        assertThat(integerList1).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList1).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray1).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray1).isNotEmpty().containsExactly(1L, 2L, 3L);

        // KEY MAP
        final List<Instant> instantList2 = typeMap.get("myKey1", ArrayList::new, Instant.class);
        final List<Integer> integerList2 = typeMap.get("myKey2", ArrayList::new, Integer.class);
        final List<Float> floatList2 = typeMap.get("myKey2", ArrayList::new, Float.class);
        final Double[] doubleArray2 = typeMap.getArray("myKey2", new Double[0], Double.class);
        final Long[] longArray2 = typeMap.getArray("myKey2", Long[]::new, Long.class);

        assertThat(instantList2).isNotEmpty();
        assertThat(integerList2).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList2).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray2).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray2).isNotEmpty().containsExactly(1L, 2L, 3L);
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void mapConvertTest(final String mapName, final TypeMapI<?> typeMap) {
        final Map<Integer, Date> input = new HashMap<>();
        input.put(6, new Date());
        typeMap.put("myKey", input);

        // TREE MAP
        assertThat(typeMap.get(() -> new HashMap<>(), Long.class, Instant.class, "myKey")).isNotEmpty();
        assertThat(typeMap.get(() -> new HashMap<>(), Long.class, Instant.class, "myKey2")).isEmpty();

        // KEY MAP
        assertThat(typeMap.get("myKey", () -> new HashMap<>(), Long.class, Instant.class)).isNotEmpty();
        assertThat(typeMap.get("myKey2", () -> new HashMap<>(), Long.class, Instant.class)).isEmpty();
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

        assertThat(typeMap.get(Object.class)).isEmpty();
        assertThat(typeMap.get(Object.class, (Object) null)).isEmpty();
        assertThat(typeMap.get(Object.class, new Object[]{null})).isEmpty();
        assertThat(typeMap.get(Object.class, "AA")).contains(innerMap);
        assertThat(typeMap.get(Object.class, "AA", "BB")).contains(asList("11", "22"));
        assertThat(typeMap.get(Object.class, "AA", "CC")).contains(new Object[]{"33", "44"});
        assertThat(typeMap.get(Object.class, "AA", "BB", 0)).contains("11");
        assertThat(typeMap.get(Object.class, "AA", "BB", 1)).contains("22");
        assertThat(typeMap.get(Object.class, "AA", "CC", 0)).contains("33");
        assertThat(typeMap.get(Object.class, "AA", "CC", 1)).contains("44");
        assertThat(typeMap.get(UnknownClass.class, "AA", "DD")).contains(anObject);
        assertThat(typeMap.get(UnknownClass.class, "AA", "DD", anObject)).isEmpty();
    }

    @Test
    void brokenJsonTest() {
        assertThat(new TypeMap("{ broken json")).containsEntry("", "{ broken json");
    }

    @Test
    void showCaseTest() {

        // Converter
        final Date date = TypeConverter.convertObj("Sat Nov 11 16:12:29 CET 2023", Date.class);

        // TypeMap
        final TypeMap map = new TypeMap();
        map.put("key", new Date());
        final Optional<Calendar> calendar = map.get("key", Calendar.class);
        final Optional<LocalDateTime> localDateTime = map.get("key", LocalDateTime.class);
        final Optional<ZonedDateTime> zonedDateTime = map.get("key", ZonedDateTime.class);

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
        final Optional<Date> myDate = jsonMap.get(Date.class, "outerMap", "innerMap", "timestamp");
        final Optional<Long> myTimestamp = jsonMap.get(Long.class, "outerMap", "innerMap", "timestamp");
        final Optional<TestEnum> myEnum = jsonMap.get(TestEnum.class, "outerMap", "myList", 0);
        final Optional<Boolean> myBoolean = jsonMap.get(Boolean.class, "outerMap", "myList", 2);
        final String myJson = jsonMap.toJson();


        System.out.println(date + calendar.toString() + localDateTime.toString() + zonedDateTime.toString());
        System.out.println(jsonMap.toString() + myDate + myTimestamp + myEnum + myBoolean + myJson);
    }
}
