package berlin.yuna.typemap.model;

import berlin.yuna.typemap.logic.XmlEncoder;

import java.util.Map;
import java.util.Optional;

import static berlin.yuna.typemap.model.Type.empty;
import static berlin.yuna.typemap.model.Type.typeOf;

public interface TypeMapI<C extends TypeMapI<C>> extends Map<Object, Object>, TypeInfo<C> {

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated TypeMap instance for chaining.
     */
    @SuppressWarnings("unchecked")
    default C putR(final Object key, final Object value) {
        put(key, value);
        return (C) this;
    }

    /**
     * Fluent typecheck if the current {@link TypeInfo} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    default Type<TypeMapI<?>> typeMapOpt() {
        return typeOf(this);
    }

    /**
     * Fluent typecheck if the current {@link TypeInfo} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    default Type<TypeListI<?>> typeListOpt() {
        return empty();
    }

    default String toXML() {
        return XmlEncoder.toXmlMap(this);
    }
}
