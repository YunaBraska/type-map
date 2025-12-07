package berlin.yuna.typemap.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PairTest {

    @Test
    void testConstructor() {
        final Pair<Integer, String> pair = new Pair<>(111, "222");
        assertThat(pair.key()).isEqualTo(111);
        assertThat(pair.value()).isEqualTo("222");
        assertThat(pair.key(String.class)).isEqualTo("111");
        assertThat(pair.value(Integer.class)).isEqualTo(222);
        assertThat(pair.getKey()).isEqualTo(111);
        assertThat(pair.getValue()).isEqualTo("222");
        assertThat(pair.value("AA").value()).isEqualTo("AA");
        assertThat(pair.setValue("BB")).isEqualTo("AA");
        assertThat(pair.getValue()).isEqualTo("BB");
        assertThat(pair.key(222).key()).isEqualTo(222);
    }

    @Test
    void shouldExposeTypeInfoConvenience() {
        final Pair<String, Object> numberPair = new Pair<>("num", "42");
        final Pair<String, Object> flagPair = new Pair<>("flag", "true");
        assertThat(numberPair.asInt()).isEqualTo(42);
        assertThat(numberPair.valueType().asLong()).isEqualTo(42L);
        assertThat(flagPair.asBoolean()).isTrue();
        assertThat(flagPair.asBoolean("value")).isTrue();
    }

    @Test
    void TestEquals() {
        final Pair<Integer, String> pair1 = new Pair<>(111, "AA");
        final Pair<Integer, String> pair2 = new Pair<>(111, "BB");
        final Pair<Integer, String> pair3 = new Pair<>(222, "AA");
        final Pair<Integer, String> pair4 = new Pair<>(111, "AA");
        assertThat(pair1.equals(pair2)).isFalse();
        assertThat(pair1.equals(pair3)).isFalse();
        assertThat(pair1.equals(pair4)).isTrue();
        assertThat(pair2.equals(pair3)).isFalse();
        assertThat(pair2.equals(pair4)).isFalse();
        assertThat(pair3.equals(pair4)).isFalse();
    }

    @Test
    void TestToString() {
        assertThat(new Pair<>(111, "AA")).hasToString("{111:\"AA\"}");
        assertThat(new Pair<>(111, "AA").hashCode()).isNotZero();
    }
}
