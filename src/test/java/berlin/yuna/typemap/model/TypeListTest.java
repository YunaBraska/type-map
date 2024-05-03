package berlin.yuna.typemap.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

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
            Arguments.of(TypeSet.class.getSimpleName(), new TypeSet()),
            Arguments.of(ConcurrentTypeSet.class.getSimpleName(), new ConcurrentTypeSet()),
            Arguments.of(ConcurrentTypeList.class.getSimpleName(), new ConcurrentTypeList())
        );
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void simpleConvertTest(final String mapName, final TypeListI<?> typeList) {
        final String myTime = new Date(TEST_TIME).toString();
        typeList.addReturn(null, myTime).add(null);

        // VALIDATIONS
        assertThat(typeList.typeListOpt()).isPresent();
        assertThat(typeList.typeMapOpt()).isEmpty();

        // TREE MAP
        assertThat(typeList.getOpt(Instant.class, 0)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getOpt(LocalTime.class, 0)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.getOpt(OffsetDateTime.class, 1)).isEmpty();
        assertThat(typeList.get(0)).isEqualTo(myTime);

        // KEY MAP
        assertThat(typeList.getOpt(Instant.class, 0)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getOpt(LocalTime.class, 0)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.getOpt(OffsetDateTime.class, 1)).isEmpty();
        assertThat(typeList.get(0)).isEqualTo(myTime);
        assertThat(typeList.get(1)).isNull();

        // ADD AT INDEX
        typeList.addReturn(0, "true");
        assertThat(typeList.get(Boolean.class, 0)).isTrue();
        assertThat(typeList.getOpt(Instant.class, 1)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getOpt(LocalTime.class, 1)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.getOpt(OffsetDateTime.class, 2)).isEmpty();


        if (typeList instanceof TypeSet || typeList instanceof ConcurrentTypeSet) {
            typeList.add(myTime);
            typeList.add(-1, myTime);
            typeList.addReturn(-1, myTime);
            typeList.addAllReturn(asList(myTime, myTime));
            assertThat(typeList.addReturn(-1, myTime)).hasSize(3);
            assertThat(typeList.addReturn((Object) 0, myTime)).hasSize(3);
        } else {
            assertThat(typeList.addReturn(-1, myTime)).hasSize(4);
            assertThat(typeList.addReturn((Object) 0, myTime)).hasSize(5);
        }
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void enumConvertTest(final String mapName, final TypeListI<?> typeList) {
        typeList.add("BB");
        assertThat(typeList.getOpt(TestEnum.class, 0)).contains(TestEnum.BB);
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void collectionConvertTest(final String mapName, final TypeListI<?> typeList) {
        final String myTime = new Date(TEST_TIME).toString();
        typeList.addReturn(myTime).add(new String[]{"1", "2", "3"});

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
        final List<Instant> instantList2 = typeList.getList(ArrayList::new, Instant.class, 0);
        final List<Integer> integerList2 = typeList.getList(ArrayList::new, Integer.class, 1);
        final List<Float> floatList2 = typeList.getList(ArrayList::new, Float.class, 1);
        final Double[] doubleArray2 = typeList.getArray(new Double[0], Double.class, 1);
        final Long[] longArray2 = typeList.getArray(Long[]::new, Long.class, 1);
        assertThat(typeList.getList(Integer.class, 1)).isNotEmpty().containsExactly(1, 2, 3);

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
        assertThat(typeList.getMap(Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getMap(() -> new HashMap<>(), Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.getMap(() -> new HashMap<>(), Long.class, Instant.class, 1)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void mapFunctionalConvertTest(final String mapName, final TypeListI<?> typeList) {
        final Map<Object, Object> map = new LinkedHashMap<>();
        map.put("1", "AA");
        map.put("2", Arrays.asList("BB", "CC"));
        map.put("3", new String[]{"DD", "EE"});
        typeList.add(map);

        // TREE MAP
        assertThat(typeList.getMap(Integer.class, value -> value)).isEmpty();
        assertThat(typeList.getMap(Integer.class, value -> value, 0)).containsOnlyKeys(1, 2, 3);
        assertThat(typeList.getMap(key -> convertObj(key, Integer.class), value -> value, 0)).containsOnlyKeys(1, 2, 3);
        assertThat(typeList.getMap(key -> convertObj(key, Integer.class), value -> collectionOf(value, String.class), 0)).containsExactly(
            entry(1, singletonList("AA")),
            entry(2, Arrays.asList("BB", "CC")),
            entry(3, Arrays.asList("DD", "EE"))
        );
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

        assertThat(typeList.getOpt(Object.class)).contains(typeList);
        assertThat(typeList.getOpt(Object.class, (Object) null)).isEmpty();
        assertThat(typeList.getOpt(Object.class, new Object[]{null})).isEmpty();
        assertThat(typeList.getOpt(Object.class, 0)).contains(innerMap);
        assertThat(typeList.getOpt(Object.class, 0, "BB")).contains(asList("11", "22"));
        assertThat(typeList.getOpt(Object.class, 0, "CC")).contains(new Object[]{"33", "44"});
        assertThat(typeList.getOpt(Object.class, 0, "BB", 0)).contains("11");
        assertThat(typeList.getOpt(Object.class, 0, "BB", 1)).contains("22");
        assertThat(typeList.getOpt(Object.class, 0, "CC", 0)).contains("33");
        assertThat(typeList.getOpt(Object.class, 0, "CC", 1)).contains("44");
        assertThat(typeList.getOpt(UnknownClass.class, 0, "DD")).contains(anObject);
        assertThat(typeList.getOpt(UnknownClass.class, 0, "DD", anObject)).isEmpty();
    }

    @Test
    void testDefaultMapMethods() {
        final String myJson = new Date(TEST_TIME).toString();

        // Broken json
        assertThat(new TypeList("{ broken json")).containsExactly("{ broken json");
        assertThat(new TypeSet("{ broken json")).containsExactly("{ broken json");
        assertThat(new ConcurrentTypeSet("{ broken json")).containsExactly("{ broken json");
        assertThat(new ConcurrentTypeList("{ broken json")).containsExactly("{ broken json");

        final TypeList list1 = new TypeList().addAllReturn(singletonList(myJson));
        final TypeSet list2 = new TypeSet().addAllReturn(singletonList(myJson));
        final ConcurrentTypeSet list3 = new ConcurrentTypeSet().addAllReturn(singletonList(myJson));
        final ConcurrentTypeList list4 = new ConcurrentTypeList().addAllReturn(singletonList(myJson));

        //for coverage, for some reason it doesn't detect this earlier
        list2.add(-1, "AA");
        list3.add(-1, "AA");

        // Get
        assertThat(list1.get(0)).isEqualTo(myJson);
        assertThat(list2.get(0)).isEqualTo(myJson);
        assertThat(list3.get(0)).isEqualTo(myJson);
        assertThat(list4.get(0)).isEqualTo(myJson);
    }
}
