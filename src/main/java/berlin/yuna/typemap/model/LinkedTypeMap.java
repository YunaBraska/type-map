package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.ArgsDecoder;
import berlin.yuna.typemap.logic.JsonDecoder;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

/**
 * {@link LinkedTypeMap} is a specialized implementation of {@link LinkedHashMap} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link LinkedTypeMap}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class LinkedTypeMap extends LinkedHashMap<Object, Object> implements TypeMapI<LinkedTypeMap> {

    /**
     * Default constructor for creating an empty {@link LinkedTypeMap}.
     */
    public LinkedTypeMap() {
        this((Map<?, ?>) null);
    }

    /**
     * Constructs a new {@link LinkedTypeMap} of the specified json.
     */
    public LinkedTypeMap(final String json) {
        this(JsonDecoder.jsonMapOf(json));
    }

    /**
     * Constructs a new {@link LinkedTypeMap} of the specified command line arguments.
     */
    public LinkedTypeMap(final String[] cliArgs) {
        this(ArgsDecoder.argsOf(String.join(" ", cliArgs)));
    }

    /**
     * Constructs a new {@link LinkedTypeMap} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public LinkedTypeMap(final Map<?, ?> map) {
        ofNullable(map).ifPresent(super::putAll);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the updated {@link LinkedTypeMap} instance for chaining.
     */
    public LinkedTypeMap addR(final Object key, final Object value) {
        return putR(key, value);
    }

    /**
     * Returns a {@link LinkedTypeMap} containing mappings.
     *
     * @param input key and value pairs
     * @return a new {@link LinkedTypeMap} containing the specified mappings.
     */
    public static LinkedTypeMap linkedMapOf(final Object... input) {
        if (input == null)
            return new LinkedTypeMap();
        if ((input.length & 1) != 0)
            throw new InternalError("length is odd");

        final LinkedTypeMap result = new LinkedTypeMap();
        for (int i = 0; i < input.length; i += 2) {
            result.put(input[i], input[i + 1]);
        }
        return result;
    }
}
