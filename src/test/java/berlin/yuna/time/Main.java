package berlin.yuna.time;


import java.util.*;
import java.util.stream.Collectors;

import static berlin.yuna.time.ContextItem.scoreInContext;
import static java.util.Arrays.asList;

public class Main {

    protected static final Score[] DATE_SCORES = new Score[]{
        Score.YEAR,
        Score.MONTH,
        Score.DAY,
        Score.WEEK_IN_MONTH,
        Score.WEEK_NUMBER,
        Score.DAY_OF_YEAR,
        Score.DAY_OF_WEEK_IN_MONTH,
        Score.DAY_NAME_ORDINAL
    };

    protected static final Score[] TIME_SCORES = new Score[]{
        Score.HOURS_24,
        Score.HOURS_12,
        Score.MINUTES,
        Score.SECONDS,
        Score.MILLISECONDS
    };

    protected static final List<Score> EXCLUSIVITY_SCORES = asList(
        Score.AM,
        Score.PM,
        Score.ERA,
        Score.DAY_NAME,
        Score.MONTH_NAME,
        Score.WEEK_NUMBER_W1,
        Score.WEEK_NUMBER_W2,
        Score.TIMEZONE_GENERAL,
        Score.TIMEZONE_RFC_822,
        Score.TIMEZONE_ISO_8601

    );


    // TODO: remove all scores of other items when one score is

    // ########## INTERPRETERS (ADDS WEIGHTS IF UNCLEAR) ##########
//        SCORE_HANDLERS.add(si -> {
//            //TODO: YEAR SCORE if its at the end or at the start of the group - In most formats, the year appears at the start or end of the date component.
//            if (si.item().increaseYear() > 0) {
//                si.item().increaseMilliseconds(si.item().milliSecondsScore() + 1);
//            }
//        });
    //TODO: Locale specific if day && month is not clear delimiter is '/' and Locale is english, then it could be MM/DD/YYYY

    public static void main(final String[] args) {
        final String dateString = "Sat Nov 11 16:12:29 CET 2023 Sat Nov 11 16:12:29 CET 2023";
        final Item[] items = splitToItems(dateString);
        // Simple Scoring Item wise
        scoreItemise(items);
        //TODO: generate pattern
        generatePattern(items);
        System.out.println(dateString);
    }

    private static void scoreItemise(final Item[] items) {
        // ADD SIMPLE SCORING
        simpleScoring(items);
        contextScoring(items);
        //TODO: position scoring
        // Proximity Rules: Use the position of items relative to known components to increase the likelihood of related components. For instance, numbers following a month name are more likely to be days, and those following days are more likely to be years.
        // Implement more complex pattern recognition. For example, a sequence of two numbers separated by a colon (:) is likely indicative of time (hours and minutes).
        // TODO: Redundancy Handling (unknown if its useful)
        // Handle multiple occurrences of the same component. For instance, if a year is already detected, subsequent four-digit numbers might not be years.
    }

    private static void simpleScoring(final Item[] items) {
        for (final Item item : items) {
            if (!item.isDelimiter()) {
                for (final Score score : Score.values()) {
                    item.increase(score, score::match);
                }
            }
        }
    }

    private static void contextScoring(final Item[] items) {
        for (int i = 0; i < items.length; i++) {
            final Item item = items[i];
            scoreInContext(items, i, item);
        }
    }

    private static String generatePattern(final Item[] items) {
        // TODO: detect non patterns like 'at', 'th',...

        // Placeholder logic to generate pattern from scored items
        return Arrays.stream(items)
            .map(item -> inferPatternFromItem(items, item))
//            .map(item -> item.isDelimiter() ? item.content() : inferPatternFromItem(items, item))
            .collect(Collectors.joining(" "));
    }

    private static String inferPatternFromItem(final Item[] items, final Item item) {
//        final Score score = Collections.max(item.scores().entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        final List<Map.Entry<Score, Integer>> topScores = item.scores().entrySet().stream()
            .collect(Collectors.groupingBy(Map.Entry::getValue))
            .entrySet().stream()
            .max(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .orElse(Collections.emptyList());
//        System.out.println(score);
        System.out.println("item [" + item.content() + "] scores: " + topScores);
        // Add more conditions as needed for other components like day of the week, month names, etc.
        return "";  // Default case or unrecognized item
    }


    // TODO: add score to each group
    //  numbers only means its timestamp
    //  'T' is used to split date & time
    //  'h', 'AM', 'PM', 'hour' is used means previous group is time if its numbers only
    //  'AM', 'PM', means it is 12h format
    //  'at' is used means next group is time if its numbers only
    //  'Z', '+', '-', indicates timezone
    //  'st', 'nd', 'rd', 'th' indicates previous group to be day
    //  ':' is used prio for time
    //  '/', '-', '.' is used prio for date
    //  'W45' a word followed by numbers is a week indicator
    //  Date: search highest number year (9999), day (31), month (12) && Locale US uses MM/DD/YYYY
    //  Time: search highest number milliseconds (999), seconds (60), minutes (60), hour (12/24)
    //  Time: is usually ordered like hour, minutes, seconds, milliseconds, nano?
    //  three letters before half could indicate time else
    //  List [GMT, CET, UTC,... ] Means its for sure timezone

    public static Item[] splitToItems(final String dateString) {
        final List<Item> result = new ArrayList<>();
        final char[] chars = dateString.toCharArray();
        final StringBuilder content = new StringBuilder();
        Item item = new Item();
        for (final char c : chars) {
            if (Character.isLetterOrDigit(c)) {
                content.append(c);
            } else {
                item = item.add(content, result);
                content.append(c);
                item.isDelimiter(true);
                item = item.add(content, result);
            }
        }
        item.add(content, result);
        return result.toArray(new Item[0]);
    }
}
