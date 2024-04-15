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
        assertThat(new Pair<>(111, "AA")).hasToString("Pair{key=111, value=AA}");
        assertThat(new Pair<>(111, "AA").hashCode()).isNotZero();
    }
}
