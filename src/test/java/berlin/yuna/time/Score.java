package berlin.yuna.time;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Score {

    //TODO: order for weight
    //TODO: try to get out a full date and a full time
    MONTH(
        "07",
        item -> item.digitLength(1, 2),
        item -> addChar("M", item.length() - item.digitLength() + 1)),
    MONTH_NAME(
        "July; Jul",
        item -> item.isLetter() && item.length() >= 3 && Arrays.stream(Month.values()).anyMatch(name -> name.getDisplayName(TextStyle.SHORT, Locale.getDefault()).equalsIgnoreCase(item.content()) || name.getDisplayName(TextStyle.FULL, Locale.getDefault()).equalsIgnoreCase(item.content())),
        item -> item.length() < 4 ? "MMM" : "MMMM"),
    DAY(
        "10",
        item -> item.digitRange(1, 31),
        item -> addChar("d", item.length() - item.digitLength() + 1)
    ),
    YEAR(
        "1996; 96",
        item -> item.digitLength(2, 4),
        item -> item.length() < 4 ? "YY" : "YYYY"
    ),
    WEEK_IN_MONTH(
        "2",
        item -> item.digitRange(1, 4),
        item -> addChar("W", item.length() - item.digitLength() + 1)
    ),
    WEEK_NUMBER(
        "27",
        item -> item.digitRange(0, 52),
        item -> addChar("w", item.length() - item.digitLength() + 1)),
    WEEK_NUMBER_W1(
        "W51",
        item -> item.isLetterAndDigit() && item.length() > 1 && item.removePrefix('W') && item.digitRange(0, 52),
        item -> addChar("W", item.length() - item.digitLength() + 1)),
    WEEK_NUMBER_W2(
        "w51",
        item -> item.isLetterAndDigit() && item.length() > 1 && item.removePrefix('w') && item.digitRange(0, 52),
        item -> addChar("w", item.length() - item.digitLength() + 1)),
    DAY_OF_YEAR(
        "189",
        item -> item.digitRange(1, 356),
        item -> addChar("D", item.length() - item.digitLength() + 1)
    ),
    DAY_OF_WEEK_IN_MONTH(
        "2",
        item -> item.digitRange(1, 5),
        item -> addChar("F", item.length() - item.digitLength() + 1)
    ),
    DAY_NAME_ORDINAL(
        "1; 7",
        item -> item.digitRange(1, 7),
        item -> addChar("u", item.length() - item.digitLength() + 1)
    ),
    HOURS_24(
        "0; 24",
        item -> item.digitRange(0, 24),
        item -> addChar(item.digit == 24 ? "k" : "H", item.length() - item.digitLength() + 1)
    ),
    HOURS_12(
        "0; 24",
        item -> item.digitRange(0, 12),
        item -> addChar(item.digit == 12 ? "h" : "K", item.length() - item.digitLength() + 1)
    ),
    MINUTES(
        "0; 45",
        item -> item.digitRange(0, 60),
        item -> addChar("m", item.length() - item.digitLength() + 1)
    ),
    SECONDS(
        "0; 60",
        item -> item.digitRange(0, 60),
        item -> addChar("s", item.length() - item.digitLength() + 1)
    ),
    MILLISECONDS(
        "0; 60",
        item -> item.digitRange(0, 999),
        item -> addChar("S", item.length() - item.digitLength() + 1)
    ),

    // ########## String Matching == always 100% Score ##########
    DELIMITER(
        "#",
        item -> false,
        item -> item.isLetter() ? "'" + item.content() + "'" : item.content()
    ),
    ERA(
        "AD; BC",
        item -> item.isLetter() && ("AD".equalsIgnoreCase(item.content()) && "BC".equalsIgnoreCase(item.content())),
        item -> "G"
    ),
    AM(
        "AM",
        item -> item.isLetter() && "AM".equalsIgnoreCase(item.content()),
        item -> "a"
    ),
    PM(
        "PM",
        item -> item.isLetter() && "PM".equalsIgnoreCase(item.content()),
        item -> "a"
    ),
    DAY_NAME(
        "Tuesday; Tue",
        item -> item.isLetter() && item.length() > 1 && Arrays.stream(DayOfWeek.values()).anyMatch(name -> name.getDisplayName(TextStyle.SHORT, Locale.getDefault()).equalsIgnoreCase(item.content()) || name.getDisplayName(TextStyle.FULL, Locale.getDefault()).equalsIgnoreCase(item.content())),
        item -> addChar("F", item.length())
    ),
    TIMEZONE_GENERAL(
        "PST; GMT",
        // CAREFUL: "Sat" == "Saturday" && "South African Time" - therefore exclusivity score check is needed
        item -> item.isLetter() && item.length() > 1 && !item.hasExclusivityScore() && ZoneId.getAvailableZoneIds().stream().anyMatch(name ->
            name.equalsIgnoreCase(item.content()) || ZoneId.of(name).getDisplayName(TextStyle.SHORT, Locale.getDefault()).equalsIgnoreCase(item.content()) || ZoneId.of(name).getDisplayName(TextStyle.FULL, Locale.getDefault()).equalsIgnoreCase(item.content())
        ),
        item -> addChar("z", item.length())
    ),
    TIMEZONE_RFC_822(
        "-0800",
        item -> false,
        item -> "Z"
    ),
    //TODO: special generation in case of '08:00'
    TIMEZONE_ISO_8601(
        "-05; -0500; -05:00",
        item -> false,
        item -> addChar("X", item.length())
    ),
    ;

    private final String example;
    private final Predicate<Item> match;
    private final Function<Item, String> pattern;

    Score(final String example, final Predicate<Item> match, final Function<Item, String> pattern) {
        this.example = example;
        this.match = match;
        this.pattern = pattern;
    }

    public String example() {
        return example;
    }

    public boolean match(final Item item) {
        return match.test(item);
    }

    public String pattern(final Item item) {
        return pattern.apply(item);
    }

    public static String addChar(final String string, final int times) {
        //java 17: String.repeat
        return Stream.generate(() -> string).limit(times).collect(Collectors.joining());
    }
}
