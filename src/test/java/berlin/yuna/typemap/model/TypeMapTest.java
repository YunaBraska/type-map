package berlin.yuna.typemap.model;


import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class TypeMapTest {

    @Test
    void simpleConvert() {
        final String myTime = new Date().toString();
        final TypeMap typeMap = new TypeMap();
        typeMap.put("myKey", myTime);
        final Instant instant = typeMap.get("myKey", Instant.class).orElse(null);
        final LocalTime localTime = typeMap.get("myKey", LocalTime.class).orElse(null);

        assertThat(instant).isNotNull();
        assertThat(localTime).isNotNull();
    }

    @Test
    void enumConvert() {
        final TypeMap typeMap = new TypeMap();
        typeMap.put("myKey", "BB");
        final TestEnum testEnum = typeMap.get("myKey", TestEnum.class).orElse(null);
        assertThat(testEnum).isEqualTo(TestEnum.BB);
    }

    @Test
    void collectionConvert() {
        final String myTime = new Date().toString();
        final TypeMap typeMap = new TypeMap();
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

    @Test
    void mapConvert() {
        final TypeMap typeMap = new TypeMap();
        final Map<Integer, Date> input = new HashMap<>();
        input.put(6, new Date());
        typeMap.put("myKey", input);
        final Map<Long, Instant> instantMap = typeMap.get("myKey", HashMap::new, Long.class, Instant.class);

        assertThat(instantMap).isNotEmpty();
    }
}
