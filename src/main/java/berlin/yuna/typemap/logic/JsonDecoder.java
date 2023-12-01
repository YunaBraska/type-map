package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.TypeList;

import static berlin.yuna.typemap.logic.JsonEncoder.unescapeJson;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static java.util.Collections.singletonList;

public class JsonDecoder {

    /**
     * Converts a JSON string to a LinkedTypeMap.
     * This method first converts the JSON string into an appropriate Java object (Map, List, or other types)
     * using the objectOf method. If the result is a LinkedTypeMap, it returns the map directly.
     * Otherwise, it creates a new LinkedTypeMap with a single entry where the key is an empty string
     * and the value is the result object. If the JSON string represents an array or a non-map object,
     * it's encapsulated within this single-entry map.
     *
     * @param json The JSON string to convert.
     * @return A LinkedTypeMap representing the JSON object, or an empty LinkedTypeMap if the JSON string is null or invalid.
     */
    public static LinkedTypeMap jsonMapOf(final String json) {
        final Object result = jsonOf(json);
        if (result instanceof LinkedTypeMap) {
            return (LinkedTypeMap) result;
        } else if (result != null) {
            return new LinkedTypeMap().putt("", result);
        }
        return new LinkedTypeMap();
    }

    /**
     * Converts a JSON string to a List of objects.
     * This method first converts the JSON string into an appropriate Java object (Map, List, or other types)
     * using the objectOf method. If the result is a List, it is cast and returned directly.
     * If the result is a LinkedTypeMap, it creates a new List containing this map as a single element.
     * This method is useful for ensuring that JSON arrays are converted to Lists, but it can also encapsulate
     * non-array JSON objects within a List.
     *
     * @param json The JSON string to convert.
     * @return A List of objects representing the JSON array or containing the JSON object, or an empty List if the JSON string is null or invalid.
     */
    public static TypeList jsonListOf(final String json) {
        final Object result = jsonOf(json);
        if (result instanceof TypeList) {
            return (TypeList) result;
        } else if (result instanceof LinkedTypeMap) {
            final TypeList list = new TypeList();
            list.add(result);
            return list;
        } else if (result != null) {
            return new TypeList(singletonList(result));
        }
        return new TypeList();
    }

    /**
     * Converts a JSON string to a Map, List, or Object.
     * Handles basic structures of JSON including nested objects and arrays.
     * Note: This implementation is simplified and may not handle all edge cases or complex JSON structures.
     *
     * @param json The JSON string to convert.
     * @return A Map, List, or Object representing the JSON structure.
     */
    public static Object jsonOf(final String json) {
        final String input = json == null ? null : json.trim();
        if (json == null || json.equals("{}")) {
            return null;
        } else if (input.startsWith("{") && input.endsWith("}")) {
            final LinkedTypeMap map = toMap(removeWrapper(input).trim());
            return map.size() == 1 && map.containsKey("") ? map.get("") : map;
        } else if (input.startsWith("[") && input.endsWith("]")) {
            return toList(removeWrapper(input).trim());
        } else if (input.startsWith("\"") && input.endsWith("\"")) {
            return unescapeJson(input.substring(1, input.length() - 1));
        } else {
            return convertToPrimitive(input);
        }
    }

    private static Object convertToPrimitive(final String value) {
        if ("null".equals(value)) {
            return null;
        } else if ("true".equals(value) || "false".equals(value)) {
            return Boolean.parseBoolean(value);
        } else {
            final Object result;
            if (value.contains(".") || value.contains("e") || value.contains("E")) {
                result = convertObj(value, Double.class);
            } else {
                result = convertObj(value, Long.class);
            }
            return result == null ? unescapeJson(value) : result;
        }
    }

    private static String removeWrapper(final String input) {
        return input.substring(1, input.length() - 1);
    }

    private static LinkedTypeMap toMap(final String json) {
        final LinkedTypeMap map = new LinkedTypeMap();
        for (final String pair : splitJson(json)) {
            final String[] keyValue = splitFirstBy(pair, ':');
            if (keyValue.length < 2) {
                map.put("", jsonOf(pair));
            } else {
                final String key = unquote(keyValue[0].trim());
                final Object value = jsonOf(keyValue[1].trim());
                map.put(key, value);
            }
        }
        return map;
    }

    @SuppressWarnings("java:S3776")
    private static String[] splitJson(final String json) {
        final TypeList parts = new TypeList();
        int braceCount = 0;
        int bracketCount = 0;
        boolean inString = false;
        final StringBuilder currentPart = new StringBuilder();

        for (int i = 0; i < json.length(); i++) {
            final char c = json.charAt(i);

            // Toggle the inString flag if we encounter a non-escaped quote
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            }

            if (!inString) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                if (c == '[') bracketCount++;
                if (c == ']') bracketCount--;

                if (c == ',' && braceCount == 0 && bracketCount == 0) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                    continue;
                }
            }

            currentPart.append(c);
        }

        if (currentPart.length() > 0) {
            parts.add(currentPart.toString());
        }

        return parts.toArray(new String[0]);
    }

    @SuppressWarnings("SameParameterValue")
    private static String[] splitFirstBy(final String string, final char c) {
        final int colonIndex = firstNonEscapedCar(string, c);
        return colonIndex == -1 ? new String[0] : new String[]{
            string.substring(0, colonIndex).trim(),
            string.substring(colonIndex + 1).trim()
        };
    }

    private static int firstNonEscapedCar(final String str, final char c) {
        boolean inString = false;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '"' && (i == 0 || str.charAt(i - 1) != '\\')) inString = !inString;
            if (str.charAt(i) == c && !inString) return i;
        }
        return -1;
    }

    private static String unquote(final String str) {
        return str.startsWith("\"") && str.endsWith("\"") ? removeWrapper(str) : str;
    }

    private static TypeList toList(final String json) {
        final TypeList list = new TypeList();
        for (final String element : splitJson(json)) {
            list.add(jsonOf(element.trim())); // Recursively parse each element
        }
        return list;
    }

    private JsonDecoder() {
        // static util class
    }
}
