package berlin.yuna.typemap.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static berlin.yuna.typemap.logic.TypeConverter.collectionOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.TypeMapTest.TEST_TIME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@SuppressWarnings("all")
class TypeListTest {

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
        typeList.addR(null, myTime).add(null);

        // VALIDATIONS
        assertThat(typeList.typeListOpt().value()).isNotNull();
        assertThat(typeList.typeMapOpt().value()).isNull();

        // TREE MAP
        assertThat(typeList.asOpt(Instant.class, 0)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.asOpt(LocalTime.class, 0)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.asOpt(OffsetDateTime.class, 1).value()).isNull();
        assertThat(typeList.get(0)).isEqualTo(myTime);

        // KEY MAP
        assertThat(typeList.asOpt(Instant.class, 0)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.asOpt(LocalTime.class, 0)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.asOpt(OffsetDateTime.class, 1).value()).isNull();
        assertThat(typeList.get(0)).isEqualTo(myTime);
        assertThat(typeList.get(1)).isNull();

        // ADD AT INDEX
        typeList.addR(0, "true");
        assertThat(typeList.get(Boolean.class, 0)).isTrue();
        assertThat(typeList.asOpt(Instant.class, 1)).contains(Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.asOpt(LocalTime.class, 1)).contains(LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), ZoneId.systemDefault()).toLocalTime());
        assertThat(typeList.asOpt(OffsetDateTime.class, 2).value()).isNull();


        if (typeList instanceof TypeSet || typeList instanceof ConcurrentTypeSet) {
            typeList.add(myTime);
            typeList.add(-1, myTime);
            typeList.addR(-1, myTime);
            typeList.addAllR(asList(myTime, myTime));
            assertThat(typeList.addR(-1, myTime)).hasSize(3);
            assertThat(typeList.addR((Object) 0, myTime)).hasSize(3);
        } else {
            assertThat(typeList.addR(-1, myTime)).hasSize(4);
            assertThat(typeList.addR((Object) 0, myTime)).hasSize(5);
        }
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void enumConvertTest(final String mapName, final TypeListI<?> typeList) {
        typeList.add("BB");
        assertThat(typeList.asOpt(TestEnum.class, 0)).contains(TestEnum.BB);
    }

    @ParameterizedTest(name = "[{index}] [{0}]")
    @MethodSource("typeMapProvider")
    void collectionConvertTest(final String mapName, final TypeListI<?> typeList) {
        final String myTime = new Date(TEST_TIME).toString();
        typeList.addR(myTime).add(new String[]{"1", "2", "3"});

        // TREE MAP
        final List<Instant> instantList1 = typeList.asList(ArrayList::new, Instant.class, 0);
        final List<Integer> integerList1 = typeList.asList(ArrayList::new, Integer.class, 1);
        final List<Float> floatList1 = typeList.asList(ArrayList::new, Float.class, 1);
        final Double[] doubleArray1 = typeList.asArray(new Double[0], Double.class, 1);
        final Long[] longArray1 = typeList.asArray(Long[]::new, Long.class, 1);

        assertThat(instantList1).isNotEmpty();
        assertThat(integerList1).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList1).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray1).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray1).isNotEmpty().containsExactly(1L, 2L, 3L);
        assertThat(typeList.asList(1)).isNotEmpty().containsExactly("1", "2", "3");
        assertThat(typeList.asList(Integer.class, 1)).isNotEmpty().containsExactly(1, 2, 3);

        // KEY MAP
        final List<Instant> instantList2 = typeList.asList(ArrayList::new, Instant.class, 0);
        final List<Integer> integerList2 = typeList.asList(ArrayList::new, Integer.class, 1);
        final List<Float> floatList2 = typeList.asList(ArrayList::new, Float.class, 1);
        final Double[] doubleArray2 = typeList.asArray(new Double[0], Double.class, 1);
        final Long[] longArray2 = typeList.asArray(Long[]::new, Long.class, 1);
        assertThat(typeList.asList(Integer.class, 1)).isNotEmpty().containsExactly(1, 2, 3);

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
        assertThat(typeList.asMap(0)).containsEntry(6, new Date(TEST_TIME));
        assertThat(typeList.asMap(Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.asMap(() -> new HashMap<>(), Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.asMap(() -> new HashMap<>(), Long.class, Instant.class, 1)).isEmpty();

        // KEY MAP
        assertThat(typeList.asMap(Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.asMap(() -> new HashMap<>(), Long.class, Instant.class, 0)).containsEntry(6L, Instant.ofEpochMilli(TEST_TIME));
        assertThat(typeList.asMap(() -> new HashMap<>(), Long.class, Instant.class, 1)).isEmpty();
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
        assertThat(typeList.asMap(Integer.class, value -> value)).isEmpty();
        assertThat(typeList.asMap(Integer.class, value -> value, 0)).containsOnlyKeys(1, 2, 3);
        assertThat(typeList.asMap(key -> convertObj(key, Integer.class), value -> value, 0)).containsOnlyKeys(1, 2, 3);
        assertThat(typeList.asMap(key -> convertObj(key, Integer.class), value -> collectionOf(value, String.class), 0)).containsExactly(
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

        assertThat(typeList.asOpt(Object.class)).contains(typeList);
        assertThat(typeList.asOpt(Object.class, (Object) null).value()).isNull();
        assertThat(typeList.asOpt(Object.class, new Object[]{null}).value()).isNull();
        assertThat(typeList.asOpt(Object.class, 0)).contains(innerMap);
        assertThat(typeList.asOpt(Object.class, 0, "BB")).contains(asList("11", "22"));
        assertThat(typeList.asOpt(Object.class, 0, "CC").value()).isEqualTo(new Object[]{"33", "44"});
        assertThat(typeList.asOpt(Object.class, 0, "BB", 0)).contains("11");
        assertThat(typeList.asOpt(Object.class, 0, "BB", 1)).contains("22");
        assertThat(typeList.asOpt(Object.class, 0, "CC", 0)).contains("33");
        assertThat(typeList.asOpt(Object.class, 0, "CC", 1)).contains("44");
        assertThat(typeList.asOpt(UnknownClass.class, 0, "DD")).contains(anObject);
        assertThat(typeList.asOpt(UnknownClass.class, 0, "DD", anObject).value()).isNull();
    }

    @Test
    void testDefaultMapMethods() {
        final String myJson = new Date(TEST_TIME).toString();

        // Broken json
        assertThat(new TypeList("{ broken json")).containsExactly("{ broken json");
        assertThat(new TypeSet("{ broken json")).containsExactly("{ broken json");
        assertThat(new ConcurrentTypeSet("{ broken json")).containsExactly("{ broken json");
        assertThat(new ConcurrentTypeList("{ broken json")).containsExactly("{ broken json");

        final TypeList list1 = new TypeList().addAllR(singletonList(myJson));
        final TypeSet list2 = new TypeSet().addAllR(singletonList(myJson));
        final ConcurrentTypeSet list3 = new ConcurrentTypeSet().addAllR(singletonList(myJson));
        final ConcurrentTypeList list4 = new ConcurrentTypeList().addAllR(singletonList(myJson));

        //for coverage, for some reason it doesn't detect this earlier
        list2.add(-1, "AA");
        list3.add(-1, "AA");

        // Get
        assertThat(list1.get(0)).isEqualTo(myJson);
        assertThat(list2.get(0)).isEqualTo(myJson);
        assertThat(list3.get(0)).isEqualTo(myJson);
        assertThat(list4.get(0)).isEqualTo(myJson);
    }

    @Test
    void shouldSupportJsonEntryPoints() throws Exception {
        final TypeList jsonList = TypeList.fromJson("[\"alpha\", {\"beta\":2}]");
        assertThat(jsonList.toJson()).contains("\"alpha\"").contains("\"beta\"");

        assertThat(TypeList.fromJson((CharSequence) "[9]")).hasSize(1);

        final Path path = Files.createTempFile("typelist-json", ".json");
        Files.writeString(path, "[1,2,3]");
        try (InputStream stream = Files.newInputStream(path)) {
            assertThat(TypeList.fromJson(stream)).hasSize(3);
        }
    }

    @Test
    void shouldSupportXmlEntryPoints() throws Exception {
        final String xml = "<root><child>v</child></root>";
        final Path xmlPath = Files.createTempFile("typelist-xml", ".xml");
        Files.writeString(xmlPath, xml);

        assertThat(TypeList.fromXml((CharSequence) xml).toXML()).contains("<root>").contains("</root>");
        assertThat(TypeList.fromXml(xml).toXML()).contains("<root>").contains("</root>");
        try (InputStream stream = Files.newInputStream(xmlPath)) {
            assertThat(TypeList.fromXml(stream).toXML()).contains("<root>").contains("</root>");
        }
    }

    @Test
    void shouldSupportArgsEntryPoints() {
        final String[] args = new String[]{"--name=neo", "-v"};
        final TypeMapI<?> argsMap = (TypeMapI<?>) TypeList.fromArgs(args).get(0);
        assertThat(argsMap.get("name")).isInstanceOf(TypeSet.class);
        assertThat((TypeSet) argsMap.get("name")).contains("neo");
        assertThat(TypeList.fromArgs("--name=neo").get(0)).isInstanceOf(Map.class);
        assertThat(TypeList.fromArgs((CharSequence) "--name=neo").get(0)).isInstanceOf(Map.class);
    }

    @Test
    void shouldSupportArgsFileAndStream() throws Exception {
        final Path argsPath = Files.createTempFile("typelist-args", ".txt");
        Files.writeString(argsPath, "--name=trinity -count=3");
        final TypeMapI<?> mapFromPath = (TypeMapI<?>) TypeList.fromArgs(argsPath).get(0);
        assertThat((TypeSet) mapFromPath.get("name")).contains("trinity");

        try (InputStream stream = Files.newInputStream(argsPath)) {
            final TypeMapI<?> mapFromStream = (TypeMapI<?>) TypeList.fromArgs(stream).get(0);
            assertThat((TypeSet) mapFromStream.get("count")).contains("3");
        }
    }

    @Test
    void shouldHandleEmptyOrBrokenInputs() {
        assertThat(TypeList.fromXml((String) null)).isEmpty();
        assertThat(TypeList.fromArgs((String) null)).isEmpty();
        try (InputStream stream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))) {
            assertThat(TypeList.fromJson(stream)).isEmpty();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
