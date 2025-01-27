package berlin.yuna.typemap.model;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static berlin.yuna.typemap.model.Type.empty;
import static berlin.yuna.typemap.model.Type.typeOf;
import static berlin.yuna.typemap.model.TypeMapTest.TEST_TIME;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("all")
class TypeTest {

    @Test
    void testType() {
        final Type<Long> nonEmpty = typeOf(200888L);
        assertThat(nonEmpty.isEmpty()).isFalse();
        assertThat(nonEmpty.isPresent()).isTrue();
        assertThat(nonEmpty).contains(200888L);
        assertThat(nonEmpty.addR(1L, null)).contains(1L);
        assertThat(nonEmpty.addR(null, 2L)).contains(2L);
        assertThat(nonEmpty.addR(null, null).value()).isNull();
        assertThat(nonEmpty.value(TEST_TIME)).contains(TEST_TIME);
        assertThat(nonEmpty.typeListOpt().value()).isNull();
        assertThat(nonEmpty.typeMapOpt().value()).isNull();
        assertThat(nonEmpty.or(() -> -1L)).contains(TEST_TIME);

        nonEmpty.value(TEST_TIME).ifPresent(value -> assertThat(value).isEqualTo(TEST_TIME));
        assertThat(nonEmpty.ifPresent(null).value()).isEqualTo(TEST_TIME);
        assertThat(nonEmpty.ifPresentOrElse(null, null).value()).isEqualTo(TEST_TIME);
        assertThat(nonEmpty.ifPresentOrElse(value -> assertThat(value).isEqualTo(TEST_TIME), null).value()).isEqualTo(TEST_TIME);
        assertThat(nonEmpty.ifPresentOrElse(value -> assertThat(value).isEqualTo(TEST_TIME), () -> fail("Value should be present")).value()).isEqualTo(TEST_TIME);
        assertThat(nonEmpty.ifPresentOrElse(null, () -> fail("Value should be present")).value()).isEqualTo(TEST_TIME);

        final Type<String> emptyType = empty();
        assertThat(emptyType.isEmpty()).isTrue();
        assertThat(emptyType.isPresent()).isFalse();
        assertThat(emptyType.value()).isNull();
        assertThat(emptyType.value("AA")).contains("AA");
        assertThat(emptyType.addR("BB", null)).contains("BB");
        assertThat(emptyType.addR(null, "CC")).contains("CC");
        assertThat(emptyType.addR(null, null).value()).isNull();
        assertThat(emptyType.typeListOpt().value()).isNull();
        assertThat(emptyType.typeMapOpt().value()).isNull();

        emptyType.value(null).ifPresent(value -> fail("Value should be null"));
        assertThat(emptyType.or(() -> -99L)).contains(-99L);
        assertThat(emptyType.ifPresent(null).value()).isNull();
        assertThat(emptyType.ifPresentOrElse(null, null).value()).isNull();
        assertThat(emptyType.ifPresentOrElse(value -> fail("Value should be null"), null).value()).isNull();
        assertThat(emptyType.ifPresentOrElse(value -> fail("Value should be null"), () -> assertThat(nonEmpty).contains(TEST_TIME)).value()).isNull();
        assertThat(emptyType.ifPresentOrElse(null, () -> assertThat(nonEmpty).contains(TEST_TIME)).value()).isNull();
    }

    @Test
    void chainTest() {
        final TypeMap typeMap = new TypeMap().putR("key1", "value1").putR("key2", "value2");
        assertThat(typeMap.asStringOpt("key1")).contains("value1");
        assertThat(typeMap.asStringOpt("key1").or(() -> typeMap.asString("key2"))).contains("value1");
        assertThat(typeMap.asStringOpt("key1").or(() -> typeMap.asString("key2")).orElseGet(() -> "default")).isEqualTo("value1");
        assertThat(typeMap.asStringOpt("invalid").or(() -> typeMap.asString("key2")).orElseGet(() -> "default")).isEqualTo("value2");
        assertThat(typeMap.asStringOpt("invalid").or(() -> typeMap.asString("invalid")).orElseGet(() -> "default")).isEqualTo("default");
        assertThat(typeMap.asStringOpt("invalid").or(() -> typeMap.asString("invalid")).orElseGet(() -> "default")).isEqualTo("default");
    }

    @Test
    void orTest() {
        assertThat(typeOf("AA").or(() -> "BB")).contains("AA");
        assertThat(typeOf("AA").or(null)).contains("AA");
        assertThat(typeOf("AA").or(() -> null)).contains("AA");
        assertThat(typeOf(null).or(() -> "BB")).contains("BB");
        assertThat(typeOf(null).or(null).value()).isNull();
        assertThat(typeOf(null).or(() -> null).value()).isNull();
        assertThat(typeOf(null).or(() -> typeOf("BB")).value()).isEqualTo(typeOf("BB"));
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
