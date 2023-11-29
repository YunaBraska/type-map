package berlin.yuna.typemap.model;

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

class TypeListTest {

    public static final long TEST_TIME = 1800000000000L;

    @BeforeEach
    void setUp() {
        System.getProperties().setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);
    }

    static Stream<Arguments> typeMapProvider() {
        return Stream.of(
            Arguments.of(TypeList.class.getSimpleName(), new TypeList()),
            Arguments.of(ConcurrentTypeList.class.getSimpleName(), new ConcurrentTypeList())
        );
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void simpleConvertTest(final String mapName, final TypeListI<?> typeList) {
        final String myTime = new Date(TEST_TIME).toString();
        typeList.addd(myTime).add(null);

        // TREE MAP
        assertThat(typeList.gett(Instant.class, 0)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.gett(LocalTime.class, 0)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.gett(OffsetDateTime.class, 1)).isEmpty();
        assertThat(typeList.get(0)).isEqualTo(myTime);

        // KEY MAP
        assertThat(typeList.gett(0, Instant.class)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.gett(0, LocalTime.class)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.gett(1, OffsetDateTime.class)).isEmpty();
        assertThat(typeList.get(0)).isEqualTo(myTime);
        assertThat(typeList.get(1)).isNull();

        // ADD AT INDEX
        typeList.addd(0, "true");
        assertThat(typeList.get(0, Boolean.class)).isTrue();
        assertThat(typeList.gett(1, Instant.class)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.gett(1, LocalTime.class)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.gett(2, OffsetDateTime.class)).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void enumConvertTest(final String mapName, final TypeListI<?> typeList) {
        typeList.add("BB");
        assertThat(typeList.gett(TestEnum.class, 0)).contains(TestEnum.BB);
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void collectionConvertTest(final String mapName, final TypeListI<?> typeList) {
        final String myTime = new Date(TEST_TIME).toString();
        typeList.addd(myTime).add(new String[]{"1", "2", "3"});

        // TREE MAP
        final List<Instant> instantList1 = typeList.getList(ArrayList::new, Instant.class, 0);
        final List<Integer> integerList1 = typeList.getList(ArrayList::new, Integer.class, 1);
        final List<Float> floatList1 = typeList.getList(ArrayList::new, Float.class, 1);
        final Double[] doubleArray1 = typeList.getArray(new Double[0], Double.class, 1);
        final Long[] longArray1 = typeList.getArray(Long[]::new, Long.class, 1);

        assertThat(instantList1).isNotEmpty();
        assertThat(integerList1).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList1).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray1).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray1).isNotEmpty().containsExactly(1L, 2L, 3L);
        assertThat(typeList.getList(1)).isNotEmpty().containsExactly("1", "2", "3");
        assertThat(typeList.getList(Integer.class, 1)).isNotEmpty().containsExactly(1, 2, 3);

        // KEY MAP
        final List<Instant> instantList2 = typeList.getList(0, ArrayList::new, Instant.class);
        final List<Integer> integerList2 = typeList.getList(1, ArrayList::new, Integer.class);
        final List<Float> floatList2 = typeList.getList(1, ArrayList::new, Float.class);
        final Double[] doubleArray2 = typeList.getArray(1, new Double[0], Double.class);
        final Long[] longArray2 = typeList.getArray(1, Long[]::new, Long.class);
        assertThat(typeList.getList(1, Integer.class)).isNotEmpty().containsExactly(1, 2, 3);

        assertThat(instantList2).isNotEmpty();
        assertThat(integerList2).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList2).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray2).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray2).isNotEmpty().containsExactly(1L, 2L, 3L);
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void mapConvertTest(final String mapName, final TypeListI<?> typeList) {
        final Map<Integer, Date> input = new HashMap<>();
        input.put(6, new Date(TEST_TIME));
        typeList.add(input);

        // TREE MAP
        assertThat(typeList.getMap(0)).containsEntry(6, new Date(TEST_TIME));
        assertThat(typeList.getMap(Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getMap(() -> new HashMap<>(), Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getMap(() -> new HashMap<>(), Long.class, Instant.class, 1)).isEmpty();

        // KEY MAP
        assertThat(typeList.getMap(0, Long.class, Instant.class)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getMap(0, () -> new HashMap<>(), Long.class, Instant.class)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getMap(1, () -> new HashMap<>(), Long.class, Instant.class)).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void jsonConvertTest(final String mapName, final TypeListI<?> typeList) {
        final Map<String, Object> input = new HashMap<>();
        final Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("FF", asList("GG", 2, true));
        input.put("AA", asList("BB", 1, true, null));
        input.put("CC", new long[]{4L, 5L, 6L});
        input.put("DD", innerMap);
        input.put("EE", "HH,II,\n");
        typeList.add(input);

        assertThat(typeList.toJson("invalidKey")).isEqualTo("{}");
        assertThat(typeList.toJson(0)).isEqualTo("{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}");
        assertThat(typeList.toJson()).isEqualTo("[{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}]");

        // Encode & Decode
        assertThat(new TypeList(typeList.toJson("invalidKey")).toJson()).isEqualTo("[]");
        assertThat(new TypeList(typeList.toJson(0)).toJson()).isEqualTo("[{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}]");
        assertThat(new TypeList(typeList.toJson()).toJson()).isEqualTo("[{\"AA\":[\"BB\",1,true,null],\"CC\":[4,5,6],\"DD\":{\"FF\":[\"GG\",2,true]},\"EE\":\"HH,II,\\n\"}]");
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void nestedKeysTest(final String mapName, final TypeListI<?> typeList) {
        final Map<String, Object> innerMap = new HashMap<>();
        final UnknownClass anObject = new UnknownClass();
        innerMap.put("BB", asList("11", "22"));
        innerMap.put("CC", new Object[]{"33", "44"});
        innerMap.put("DD", anObject);
        innerMap.put("EE", singletonList(anObject));
        innerMap.put("FF", new Object[]{anObject});
        typeList.add(innerMap);

        assertThat(typeList.gett(Object.class)).isEmpty();
        assertThat(typeList.gett(Object.class, (Object) null)).isEmpty();
        assertThat(typeList.gett(Object.class, new Object[]{null})).isEmpty();
        assertThat(typeList.gett(Object.class, 0)).contains(innerMap);
        assertThat(typeList.gett(Object.class, 0, "BB")).contains(asList("11", "22"));
        assertThat(typeList.gett(Object.class, 0, "CC")).contains(new Object[]{"33", "44"});
        assertThat(typeList.gett(Object.class, 0, "BB", 0)).contains("11");
        assertThat(typeList.gett(Object.class, 0, "BB", 1)).contains("22");
        assertThat(typeList.gett(Object.class, 0, "CC", 0)).contains("33");
        assertThat(typeList.gett(Object.class, 0, "CC", 1)).contains("44");
        assertThat(typeList.gett(UnknownClass.class, 0, "DD")).contains(anObject);
        assertThat(typeList.gett(UnknownClass.class, 0, "DD", anObject)).isEmpty();
    }

    @Test
    void testDefaultMapMethods() {
        final String myTime = new Date(TEST_TIME).toString();

        // Broken json
        assertThat(new TypeList("{ broken json")).containsExactly("{ broken json");
        assertThat(new ConcurrentTypeList("{ broken json")).containsExactly("{ broken json");

        final TypeList list1 = new TypeList().adddAll(singletonList(myTime));
        final ConcurrentTypeList list2 = new ConcurrentTypeList().adddAll(singletonList(myTime));

        // Get
        assertThat(list1.get(0)).isEqualTo(myTime);
        assertThat(list2.get(0)).isEqualTo(myTime);
    }
}
