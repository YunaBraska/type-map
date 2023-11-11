package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.TestEnum;
import berlin.yuna.typemap.model.UnknownClass;
import berlin.yuna.typemap.model.UnknownNumber;
import org.junit.jupiter.api.Test;

import java.util.*;

import static berlin.yuna.typemap.logic.TypeConverter.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class TypeConverterTest {

    @Test
    void convertNull() {
        assertThat(convertObj(null, String.class)).isNull();
    }

    @Test
    void convertSameType() {
        final StringBuilder sb = new StringBuilder("AA");
        assertThat(convertObj(sb, StringBuilder.class)).isEqualTo(sb);
    }

    @Test
    void convertFirstItem() {
        assertThat(convertObj(asList("123", "456"), Integer.class)).isEqualTo(123);
    }

    @Test
    void convertEnum() {
        assertThat(convertObj("BB", TestEnum.class)).isEqualTo(TestEnum.BB);
        assertThat(convertObj("ZZ", TestEnum.class)).isNull();
        assertThat(enumOf("BB", TestEnum.class)).isEqualTo(TestEnum.BB);
        assertThat(enumOf("ZZ", TestEnum.class)).isNull();
    }

    @Test
    void convertPatentAndExactMatch() {
        assertThat(convertObj("123", Long.class)).isEqualTo(123L);
        assertThat(convertObj("123", Number.class)).isEqualTo(123d);
        assertThat(convertObj(123, Long.class)).isEqualTo(123L);
        assertThat(convertObj(123, Number.class)).isEqualTo(123);
    }

    @Test
    void convertFallBackToString() {
        assertThat(convertObj(new UnknownNumber(), Long.class)).isEqualTo(123L);
    }

    @Test
    void convertUnknownIsNull() {
        assertThat(convertObj(new UnknownClass(), Long.class)).isNull();
        assertThat(convertObj("AA", UnknownClass.class)).isNull();
    }

    @Test
    void convertMap() {
        final Map<String, String> input = new HashMap<>();
        input.put("12", "34");
        input.put("56", "78");

        final Map<Integer, Long> output = new TreeMap<>();
        output.put(12, 34L);
        output.put(56, 78L);
        assertThat(convertObj(input, String.class)).isEqualTo("12=34");
        assertThat(mapOf(input, () -> new HashMap<>(), String.class, String.class)).isEqualTo(input);
        assertThat(mapOf(input, () -> new TreeMap<>(), Integer.class, Long.class)).containsExactlyInAnyOrderEntriesOf(output);
        assertThat(mapOf(input, () -> new TreeMap<>(), null, Long.class)).isEmpty();
        final Object nullMap1 = mapOf(input, () -> null, Integer.class, Long.class);
        final Object nullMap2 = mapOf(input, null, Integer.class, Long.class);
        assertThat(nullMap1).isNull();
        assertThat(nullMap2).isNull();
    }

    @Test
    void convertCollection() {
        assertThat(convertObj(asList("123", "456"), String.class)).isEqualTo("123");
        assertThat(collectionOf(asList("123", "456"), () -> new HashSet<>(), String.class)).containsExactlyInAnyOrder("123", "456");
        assertThat(collectionOf(new String[]{"123", "456"}, () -> new HashSet<>(), Integer.class)).containsExactlyInAnyOrder(123, 456);
        assertThat(collectionOf("123", () -> new ArrayList<>(), Integer.class)).containsExactly(123);
        assertThat(collectionOf(null, () -> new ArrayList<>(), Integer.class)).isEmpty();
        assertThat(collectionOf("123", () -> new ArrayList<>(), null)).isEmpty();
        final Object nullCollection1 = collectionOf("123", () -> null, Integer.class);
        final Object nullCollection2 = collectionOf("123", null, Integer.class);
        assertThat(nullCollection1).isNull();
        assertThat(nullCollection2).isNull();
    }

    @Test
    void convertArray() {
        assertThat(convertObj(new String[]{"123", "456"}, String.class)).isEqualTo("123");
        assertThat(arrayOf(new String[]{"123", "456"}, new Integer[0], Integer.class)).containsExactly(123, 456);
        assertThat(arrayOf(asList("123", "456"), new Integer[0], Integer.class)).containsExactly(123, 456);
        assertThat(arrayOf("123", new Integer[0], Integer.class)).containsExactly(123);
        assertThat(arrayOf(new String[]{"123", "456"}, Integer[]::new , Integer.class)).containsExactly(123, 456);
        assertThat(arrayOf(asList("123", "456"), Integer[]::new, Integer.class)).containsExactly(123, 456);
        assertThat(arrayOf("123", Integer[]::new, Integer.class)).containsExactly(123);
        assertThat(arrayOf("123", Integer[]::new, Integer.class)).containsExactly(123);
    }
}
