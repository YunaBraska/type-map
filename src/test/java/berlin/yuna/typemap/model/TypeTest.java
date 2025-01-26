package berlin.yuna.typemap.model;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static berlin.yuna.typemap.model.Type.empty;
import static berlin.yuna.typemap.model.Type.typeOf;
import static berlin.yuna.typemap.model.TypeMapTest.TEST_TIME;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("all")
class TypeTest {

    @Test
    void testType() {
        final Type<Long> nonEmpty = typeOf(200888L);
        assertThat(nonEmpty).contains(200888L);
        assertThat(nonEmpty.addR(1L, null)).contains(1L);
        assertThat(nonEmpty.addR(null, 2L)).contains(2L);
        assertThat(nonEmpty.addR(null, null).value()).isNull();
        assertThat(nonEmpty.value(TEST_TIME)).contains(TEST_TIME);
        assertThat(nonEmpty.typeListOpt().value()).isNull();
        assertThat(nonEmpty.typeMapOpt().value()).isNull();

        final Type<String> emptyType = empty();
        assertThat(emptyType.value()).isNull();
        assertThat(emptyType.value("AA")).contains("AA");
        assertThat(emptyType.addR("BB", null)).contains("BB");
        assertThat(emptyType.addR(null, "CC")).contains("CC");
        assertThat(emptyType.addR(null, null).value()).isNull();
        assertThat(emptyType.typeListOpt().value()).isNull();
        assertThat(emptyType.typeMapOpt().value()).isNull();
    }

    @Test
    void filterTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeOf(typeMap).filter(map -> map.containsKey("key")).value()).isEqualTo(typeMap);
        assertThat(typeOf(typeMap).filter(null).value()).isNull();
        assertThat(typeOf(null).filter(null).value()).isNull();
        assertThat(typeOf(typeMap).filter(map -> map.containsKey("invalid")).value()).isNull();
    }

    @Test
    void mapTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeOf(typeMap).map(map -> map.get("key")).value()).isEqualTo(new TypeList().addR("value1").addR("value2"));
        assertThat(typeOf(typeMap).map(null).value()).isNull();
        assertThat(typeOf(null).map(null).value()).isNull();
        assertThat(typeOf(typeMap).map(map -> map.get("invalid")).value()).isNull();
    }

    @Test
    void flatMapTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeOf(typeMap).flatMap(map -> typeOf(map.get("key"))).value()).isEqualTo(new TypeList().addR("value1").addR("value2"));
        assertThat(typeOf(typeMap).flatMap(null).value()).isNull();
        assertThat(typeOf(null).flatMap(null).value()).isNull();
        assertThat(typeOf(null).flatMap(map -> null).value()).isNull();
        assertThat(typeOf(typeMap).flatMap(map -> typeOf(map.get("invalid"))).value()).isNull();
    }

    @Test
    void flatOptTest() {
        final TypeMap typeMap = new TypeMap().putR("key", new TypeList().addR("value1").addR("value2"));
        assertThat(typeOf(typeMap).flatOpt(map -> ofNullable(map.get("key"))).value()).isEqualTo(new TypeList().addR("value1").addR("value2"));
        assertThat(typeOf(typeMap).flatOpt(null).value()).isNull();
        assertThat(typeOf(null).flatOpt(null).value()).isNull();
        assertThat(typeOf(null).flatOpt(map -> null).value()).isNull();
        assertThat(typeOf(null).flatOpt(map -> Optional.empty()).value()).isNull();
        assertThat(typeOf(typeMap).flatOpt(map -> ofNullable(map.get("invalid"))).value()).isNull();
    }

    @Test
    void streamTest() {
        assertThat(typeOf("AA").stream().count()).isEqualTo(1);
        assertThat(typeOf("BB").stream().findFirst()).contains("BB");
        assertThat(typeOf(null).stream().count()).isZero();
    }

    @Test
    void orElseTest() {
        assertThat(typeOf("AA").orElse("BB")).isEqualTo("AA");
        assertThat(typeOf(null).orElse("BB")).isEqualTo("BB");
    }

    @Test
    void orElseGetTest() {
        assertThat(typeOf("AA").orElseGet(() -> "BB")).isEqualTo("AA");
        assertThat(typeOf(null).orElseGet(() -> "BB")).isEqualTo("BB");
    }

    @Test
    void orElseThrowTest() {
        assertThatThrownBy(() -> typeOf(null).orElseThrow()).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> typeOf(null).orElseThrow(IllegalArgumentException::new)).isInstanceOf(IllegalArgumentException.class);
        assertThat(typeOf("AA").orElseThrow()).isEqualTo("AA");
        assertThat(typeOf("AA").orElseThrow(IllegalArgumentException::new)).isEqualTo("AA");
    }

    @Test
    void equalsTest() {
        assertThat(typeOf("AA").equals(typeOf("BB"))).isFalse();
        assertThat(typeOf("BB").equals(typeOf("AA"))).isFalse();
        assertThat(typeOf("AA").equals(typeOf("AA"))).isTrue();
        assertThat(typeOf("AA").equals("AA")).isFalse();
    }

    @Test
    void generalTest() {
        assertThat(typeOf(new TypeMap()).typeMapOpt().value()).isNotNull();
        assertThat(typeOf(new TypeList()).typeListOpt().value()).isNotNull();
        assertThat(typeOf("AA").hashCode()).isNotZero();
        assertThat(typeOf("AA").toString()).isEqualTo("{\"AA\"}");
        assertThat(typeOf("AA").iterator().next()).isEqualTo("AA");
    }
}
