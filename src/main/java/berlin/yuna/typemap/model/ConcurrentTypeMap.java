package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.ArgsDecoder;
import berlin.yuna.typemap.logic.JsonDecoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

/**
 * {@link ConcurrentTypeMap} is a specialized implementation of {@link ConcurrentHashMap} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link ConcurrentTypeMap}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class ConcurrentTypeMap extends ConcurrentHashMap<Object, Object> implements TypeMapI<ConcurrentTypeMap> {

    /**
     * Default constructor for creating an empty TypeMap.
     */
    public ConcurrentTypeMap() {
        this((Map<?, ?>) null);
    }

    /**
     * Constructs a new {@link ConcurrentTypeMap} of the specified json.
     */
    public ConcurrentTypeMap(final String json) {
        this(JsonDecoder.jsonMapOf(json));
    }

    /**
     * Constructs a new {@link ConcurrentTypeMap} of the specified command line arguments.
     */
    public ConcurrentTypeMap(final String[] cliArgs) {
        this(ArgsDecoder.argsOf(String.join(" ", cliArgs)));
    }

    /**
     * Constructs a new {@link ConcurrentTypeMap} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public ConcurrentTypeMap(final Map<?, ?> map) {
        ofNullable(map).ifPresent(super::putAll);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated {@link ConcurrentTypeMap} instance for chaining.
     */
    public ConcurrentTypeMap addReturn(final Object key, final Object value) {
        return putReturn(key, value);
    }
}
