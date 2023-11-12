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
    void simpleConvert(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date().toString();
        typeMap.put("myKey", myTime);
        final Instant instant = typeMap.get("myKey", Instant.class).orElse(null);
        final LocalTime localTime = typeMap.get("myKey", LocalTime.class).orElse(null);

        assertThat(instant).isNotNull();
        assertThat(localTime).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void enumConvert(final String mapName, final TypeMapI<?> typeMap) {
        typeMap.put("myKey", "BB");
        final TestEnum testEnum = typeMap.get("myKey", TestEnum.class).orElse(null);
        assertThat(testEnum).isEqualTo(TestEnum.BB);
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void collectionConvert(final String mapName, final TypeMapI<?> typeMap) {
        final String myTime = new Date().toString();
        typeMap.put("myKey1", myTime);
        typeMap.put("myKey2", new String[]{"1", "2", "3"});
        final List<Instant> instantList = typeMap.get("myKey1", ArrayList::new, Instant.class);
        final List<Integer> integerList = typeMap.get("myKey2", ArrayList::new, Integer.class);
        final List<Float> floatList = typeMap.get("myKey2", ArrayList::new, Float.class);
        final Double[] doubleArray = typeMap.getArray("myKey2", new Double[0], Double.class);
        final Long[] longArray = typeMap.getArray("myKey2", Long[]::new, Long.class);


        assertThat(instantList).isNotEmpty();
        assertThat(integerList).isNotEmpty().containsExactly(1, 2, 3);
        assertThat(floatList).isNotEmpty().containsExactly(1f, 2f, 3f);
        assertThat(doubleArray).isNotEmpty().containsExactly(1d, 2d, 3d);
        assertThat(longArray).isNotEmpty().containsExactly(1L, 2L, 3L);
    }

    @ParameterizedTest
    @MethodSource("typeMapProvider")
    void mapConvert(final String mapName, final TypeMapI<?> typeMap) {
        final Map<Integer, Date> input = new HashMap<>();
        input.put(6, new Date());
        typeMap.put("myKey", input);
        final Map<Long, Instant> instantMap = typeMap.get("myKey", HashMap::new, Long.class, Instant.class);
        final Map<Long, Instant> instantMap2 = typeMap.get("myKey2", HashMap::new, Long.class, Instant.class);

        assertThat(instantMap).isNotEmpty();
        assertThat(instantMap2).isEmpty();
    }

    @Test
    void showCase() {

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





        System.out.println(date + calendar.toString() + localDateTime.toString() + zonedDateTime.toString());
    }
}
