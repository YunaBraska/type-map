package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.LinkedTypeMap;
import berlin.yuna.typemap.model.TypeList;
import berlin.yuna.typemap.model.TypeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;


public class ArgsDecoder {

    public static LinkedTypeMap argsOf(final String input) {
        final LinkedTypeMap result = new LinkedTypeMap();
        final List<Integer[]> keyRanges = getKeyRanges(input);

        for (int i = 0; i < keyRanges.size(); i++) {
            final Integer[] range = keyRanges.get(i);
            final int offsetEnd = i + 1 < keyRanges.size() ? keyRanges.get(i + 1)[0] : input.length();
            final String key = input.substring(range[0], range[1]);
            final String cleanKey = key.substring(key.startsWith("--") ? 2 : 1).strip();
            final String value = input.substring(range[1] + 1, offsetEnd);
            final TypeSet values = (TypeSet) result.computeIfAbsent(cleanKey, v -> new TypeSet());
            values.addAll((hasText(value) ? handleValue(value.strip()) : singletonList(true)));
            result.put(cleanKey, values);
        }
        return result;
    }

    public static boolean hasText(final String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    public static TypeList handleValue(final String value) {
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            return new TypeList().addR(convertToType(value.substring(1, value.length() - 1)));
        }
        return new TypeList().addAllR(Arrays.stream(value.split("\\s+", -1)).map(ArgsDecoder::convertToType).toList());
    }

    private static Object convertToType(final String string) {
        return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false") ? string.equalsIgnoreCase("true") : string;
    }

    private static List<Integer[]> getKeyRanges(final String input) {
        final List<Integer[]> keyRanges = new ArrayList<>();
        int keyIndex = -1;
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == '"' || c == '\'') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && keyIndex == -1 && c == '-') {
                keyIndex = i;
            } else if (!inQuotes && keyIndex != -1 && ((c == '=') || c == ' ')) {
                keyRanges.add(new Integer[]{keyIndex, i});
                keyIndex = -1;
            }
        }
        return keyRanges;
    }

    private static boolean containsText(final CharSequence str) {
        final int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private ArgsDecoder() {
        // static util class
    }
}
