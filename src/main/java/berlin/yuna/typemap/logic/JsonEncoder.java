package berlin.yuna.typemap.logic;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static berlin.yuna.typemap.logic.TypeConverter.arrayOf;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;

public class JsonEncoder {

    @SuppressWarnings({"java:S2386"})
    public static final Map<Character, String> JSON_ESCAPE_SEQUENCES = new HashMap<>();
    @SuppressWarnings({"java:S2386"})
    public static final Map<String, String> JSON_UNESCAPE_SEQUENCES = new HashMap<>();

    static {
        JSON_ESCAPE_SEQUENCES.put('"', "\\\"");
        JSON_ESCAPE_SEQUENCES.put('\\', "\\\\");
        JSON_ESCAPE_SEQUENCES.put('\b', "\\b");
        JSON_ESCAPE_SEQUENCES.put('\f', "\\f");
        JSON_ESCAPE_SEQUENCES.put('\n', "\\n");
        JSON_ESCAPE_SEQUENCES.put('\r', "\\r");
        JSON_ESCAPE_SEQUENCES.put('\t', "\\t");
        JSON_ESCAPE_SEQUENCES.forEach((key, value) -> JSON_UNESCAPE_SEQUENCES.put(value, key.toString()));
    }

    /**
     * Converts any object to its JSON representation.
     * This method dispatches the conversion task based on the type of the object.
     * It handles Maps, Collections, Arrays (both primitive and object types),
     * and other objects. If the object is null, it returns an empty JSON object ({}).
     * Standalone objects are converted to JSON strings and wrapped in curly braces,
     * making them single-property JSON objects.
     *
     * @param object The object to be converted to JSON.
     * @return A JSON representation of the object as a String.
     *         If the object is null, returns "{}".
     */
    public static String toJson(final Object object) {
        if (object == null) {
            return "{}";
        } else if (object instanceof Map) {
            return jsonOf((Map<?, ?>) object);
        } else if (object instanceof Collection) {
            return jsonOf((Collection<?>) object);
        } else if (object.getClass().isArray()) {
            return jsonOfArray(object, Object[]::new, Object.class);
        } else {
            return "{" + jsonify(object) + "}";
        }
    }

    /**
     * Escapes a String for JSON.
     * This method replaces special characters in a String with their corresponding JSON escape sequences.
     *
     * @param str The string to be escaped for JSON.
     * @return The escaped JSON string.
     */
    public static String escapeJsonValue(final String str) {
        return str == null ? null : str.chars()
            .mapToObj(c -> escapeJson((char) c))
            .collect(Collectors.joining());
    }


    /**
     * Escapes a character for JSON.
     * This method returns the JSON escape sequence for a given character, if necessary.
     *
     * @param c The character to be escaped for JSON.
     * @return The escaped JSON character as a String.
     */
    public static String escapeJson(final char c) {
        return JSON_ESCAPE_SEQUENCES.getOrDefault(c, (c < 32 || c >= 127) ? String.format("\\u%04x", (int) c) : String.valueOf(c));
    }

    /**
     * Unescapes a JSON string by replacing JSON escape sequences with their corresponding characters.
     * <p>
     * This method iterates through a predefined set of JSON escape sequences (like \" for double quotes,
     * \\ for backslash, \n for newline, etc.) and replaces them in the input string with the actual characters
     * they represent. The method is designed to process a JSON-encoded string and return a version with
     * standard characters, making it suitable for further processing or display.
     * <p>
     * Note: This method assumes that the input string is a valid JSON string with correct escape sequences.
     * It does not perform JSON validation.
     *
     * @param str The JSON string with escape sequences to be unescaped.
     * @return The unescaped version of the JSON string.
     */
    public static String unescapeJson(final String str) {
        String result = str;
        for (final Map.Entry<String, String> entry : JSON_UNESCAPE_SEQUENCES.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static String jsonOf(final Map<?, ?> map) {
        return map.entrySet().stream()
            .map(entry -> jsonify(entry.getKey()) + ":" + jsonify(entry.getValue()))
            .collect(Collectors.joining(",", "{", "}"));
    }

    private static String jsonOf(final Collection<?> collection) {
        return collection.stream()
            .map(JsonEncoder::jsonify)
            .collect(Collectors.joining(",", "[", "]"));
    }

    @SuppressWarnings("SameParameterValue")
    private static <E> String jsonOfArray(final Object object, final IntFunction<E[]> generator, final Class<E> componentType) {
        return object.getClass().isArray() ? Arrays.stream(arrayOf(object, generator, componentType))
            .map(JsonEncoder::jsonify)
            .collect(Collectors.joining(",", "[", "]")) : "null";
    }

    private static String jsonify(final Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return "\"" + escapeJsonValue((String) obj) + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        } else if (obj instanceof Map) {
            return jsonOf((Map<?, ?>) obj);
        } else if (obj instanceof Collection) {
            return jsonOf((Collection<?>) obj);
        } else if (obj.getClass().isArray()) {
            return jsonOfArray(obj, Object[]::new, Object.class);
        } else {
            final String str = convertObj(obj, String.class);
            return str == null ? "null" : "\"" + escapeJsonValue(str) + "\"";
        }
    }

    private JsonEncoder() {
        // static util class
    }
}
