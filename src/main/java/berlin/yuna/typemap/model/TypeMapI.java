package berlin.yuna.typemap.model;

import java.util.Map;

public interface TypeMapI<C extends TypeMapI<C>> extends Map<Object, Object>, TypeInfo<C> {

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated TypeMap instance for chaining.
     */
    C putReturn(final Object key, final Object value);
}
