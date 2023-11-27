package berlin.yuna.time;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static berlin.yuna.time.Main.DATE_SCORES;
import static berlin.yuna.time.Main.TIME_SCORES;
import static berlin.yuna.time.Score.TIMEZONE_ISO_8601;
import static berlin.yuna.time.Score.TIMEZONE_RFC_822;
import static java.util.Arrays.asList;

public class ContextItem {
    final Item[] items;
    final Item item;
    final int index;

    protected static final List<String> DAY_INDICATORS = asList("st", "nd", "rd", "th");

    public static void scoreInContext(final Item[] items, final int index, final Item item) {
        // 'T' == DATE + 'T' + TIME
        if (item.isDelimiter() && "T".equals(item.content())) {
            markGroup(
                items,
                index,
                prevItem -> increase(DATE_SCORES, item),
                nextItem -> increase(TIME_SCORES, item)
            );
        }

        scoreOffsetTime(items, index, item);

        // 'st', 'nd', 'rd', 'th' == DAY
        if (item.isLetter() && item.length() == 2 && DAY_INDICATORS.contains(item.content())) {
            final Item prevItem = previous(items, index, 1, itm -> !itm.isDelimiter());
            if (prevItem != null) {
                item.increase(Score.DAY);
                decrease(DATE_SCORES, Score.DAY, previous(items, index, 1, itm -> !itm.isDelimiter() && itm != prevItem), next(items, index, 1, itm -> !itm.isDelimiter() && itm != prevItem));
            }
        }

        // SET 12 HOURS format
        if (item.hasScore(Score.AM) || item.hasScore(Score.PM)) {
            final Item hour = previous(items, index, 1, itm -> itm.isDigit() && (itm.hasScore(Score.HOURS_12) || itm.hasScore(Score.HOURS_24)));
            if (hour != null) {
                hour.increase(Score.HOURS_12).decrease(Score.HOURS_24);
            }
        }
    }

    public static void scoreOnPosition(final Item[] items, final int index, final Item item) {
        // if highest score is unclear
//        final List<Score> topScores = item.topScpores();
//        if (topScores.size() > 1) {
//            for (Score score : item.scores().keySet()) {
//                //TODO: try score exclusion / decrease score based on previous items and next items
//            }
//        }
    }

    public static void scoreOffsetTime(final Item[] items, final int index, final Item item) {
        // TIMEZONE_RFC_822, TIMEZONE_ISO_8601
        if (item.isDelimiter() && "+".equals(item.content()) && "-".equals(item.content())) {
            final Item next = next(items, index, 1);
            final Item nextItem = next(items, index, 1, itm -> !itm.isDelimiter);
            if (next != null
                && nextItem != null
                && item.isDigit()
                && item.length() == 2
                && nextItem.isDigit()
                && nextItem.length() == 2
                && next.isDelimiter()
                && next.content().equals(":")
            ) {
                // -05:00 [TIMEZONE_ISO_8601]
                item.increase(TIMEZONE_ISO_8601);
                next.increase(TIMEZONE_ISO_8601);
                nextItem.increase(TIMEZONE_ISO_8601);
            } else if (next != null && !next.isDelimiter() && next.isDigit()) {
                if (next.length() == 2) {
                    // -05 [TIMEZONE_ISO_8601]
                    item.increase(TIMEZONE_ISO_8601);
                    next.increase(TIMEZONE_ISO_8601);
                } else if (next.length() == 4) {
                    // -0500 [TIMEZONE_RFC_822]
                    item.increase(TIMEZONE_RFC_822);
                    next.increase(TIMEZONE_RFC_822);
                }
            }
        }
    }

    public static Item previous(final Item[] items, final int from, final int to) {
        return from - to >= 0 ? items[from - to] : null;
    }

    public static Item next(final Item[] items, final int from, final int to) {
        return from + to < items.length ? items[from + to] : null;
    }

    public static Item previous(final Item[] items, final int from, final int to, final Predicate<Item> filter) {
        return pv(items, from, to, filter, false);
    }

    public static Item next(final Item[] items, final int from, final int to, final Predicate<Item> filter) {
        return pv(items, from, to, filter, true);
    }

    //TODO: option to do something instead of null check
    public static Item pv(final Item[] items, final int from, final int target, final Predicate<Item> filter, final boolean next) {
        int match = 1;
        Item result = null;
        for (int i = 1; i < target; i++) {
            if (match == target) {
                break;
            }
            result = next ? next(items, from, i) : previous(items, from, i);
            if (result != null && filter.test(result)) {
                match++;
            }
        }
        return result;
    }

    public static void increase(final Score[] scores, final Item... items) {
        for (final Item item : items) {
            for (final Score score : scores) {
                item.increase(score);
            }
        }
    }

    public static void decrease(final Score[] scores, final Score exclusion, final Item... items) {
        for (final Item item : items) {
            for (final Score score : scores) {
                if (score != exclusion) {
                    item.decrease(score);
                }
            }
        }
    }

    public static void markGroup(final Item[] items, final int index, final Consumer<Item> item) {
        markGroup(items, index, item, item);
    }

    public static void markGroup(final Item[] items, final int index, final Consumer<Item> prevItem, final Consumer<Item> nextItem) {
        int counter = 1;
        Item currItem;
        String separator = null;
        while ((currItem = previous(items, index, counter)) != null) {
            if (currItem.isDelimiter()) {
                if (separator == null) {
                    separator = currItem.content();
                } else if (!separator.equals(currItem.content())) {
                    break;
                }
            } else {
                prevItem.accept(currItem);
            }
            counter++;
        }

        counter = 1;
        separator = null;
        while ((currItem = next(items, index, counter)) != null) {
            if (currItem.isDelimiter()) {
                if (separator == null) {
                    separator = currItem.content();
                } else if (!separator.equals(currItem.content())) {
                    break;
                }
            } else {
                nextItem.accept(currItem);
            }
            counter++;
        }
    }

    public ContextItem(final int index, final Item[] items, final Item item) {
        this.index = index;
        this.items = items;
        this.item = item;
    }

    public Item[] items() {
        return items;
    }

    public Item item() {
        return item;
    }

    public int index() {
        return index;
    }

    public Item previous(final int index) {
        return this.index - index >= 0 ? items[this.index - index] : null;
    }

    public Item previous(final int index, final Predicate<Item> filter) {
        return pv(index, filter, false);
    }

    public Item next(final int index, final Predicate<Item> filter) {
        return pv(index, filter, true);
    }

    public Item next(final int index) {
        return this.index + index < items.length ? items[this.index + index] : null;
    }

    //TODO: option to do something instead of null check
    protected Item pv(final int target, final Predicate<Item> filter, final boolean next) {
        int match = 1;
        Item result = null;
        for (int i = 1; i < target; i++) {
            if (match == target) {
                break;
            }
            result = next ? next(i) : previous(i);
            if (result != null && filter.test(result)) {
                match++;
            }
        }
        return result;
    }
}
