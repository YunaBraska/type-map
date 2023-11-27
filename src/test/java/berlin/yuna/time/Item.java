package berlin.yuna.time;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static berlin.yuna.time.Main.EXCLUSIVITY_SCORES;
import static java.lang.Long.parseLong;

public class Item {
    final Map<Score, Integer> scores = new EnumMap<>(Score.class);
    long digit = -1;
    int digitLength = -1;
    boolean isDigit = false;
    boolean isLetter = false;
    boolean isDelimiter = false;
    String content;

    public Item add(final StringBuilder content, final Collection<Item> items) {
        final String string = content.toString();
        if (content.length() != 0) {
            content(string);
            items.add(this);
        }
        content.setLength(0);
        return new Item();
    }

    public Integer score(final Score score) {
        return scores.getOrDefault(score, -1);
    }

    public boolean hasScore(final Score score) {
        return scores.containsKey(score);
    }

    public boolean hasExclusivityScore() {
        return EXCLUSIVITY_SCORES.stream().anyMatch(scores::containsKey);
    }

    public Item increase(final Score score) {
        scores.put(score, scores.getOrDefault(score, 0) + 1);
        handleMutualExclusivity();
        return this;
    }

    public Item increase(final Score score, final Predicate<Item> when) {
        if (when.test(this)) {
            increase(score);
        }
        return this;
    }

    public Item decrease(final Score score) {
        final Integer i = scores.get(score);
        if (i == null || i <= 1) {
            scores.remove(score);
        }
        return this;
    }

    private Score maxScore() {
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public int length() {
        return content.length();
    }

    public int digitLength() {
        if (digitLength == -1 && isDigit()) {
            final String string = removePrefixes(content, '0');
            digit = parseLong(string);
            digitLength = isDigit() && !isLetter() ? string.length() : content.length();
        }
        return digitLength;
    }

    public boolean digitLength(final int min, final int max) {
        return digitLength >= min && digitLength <= max;
    }

    public boolean digitRange(final int min, final int max) {
        return digit() >= min && digit() <= max;
    }

    public long digit() {
        return digit;
    }

    public Map<Score, Integer> scores() {
        return scores;
    }

    public Item length(final int length) {
        this.digitLength = length;
        return this;
    }

    public boolean isLetterAndDigit() {
        return isDigit && isLetter;
    }

    public boolean isDigit() {
        return isDigit && !isLetter;
    }

    public Item isDigit(final boolean digit) {
        isDigit = digit;
        return this;
    }

    public boolean isLetter() {
        return isLetter && !isDigit;
    }

    public Item isLetter(final boolean letter) {
        isLetter = letter;
        return this;
    }

    public boolean isDelimiter() {
        return isDelimiter;
    }

    public Item isDelimiter(final boolean delimiter) {
        isDelimiter = delimiter;
        return this;
    }

    public String content() {
        return content;
    }

    public Item content(final String content) {
        this.content = content;
        for (final char c : content().toCharArray()) {
            isDigit(Character.isDigit(c) || isDigit);
            isLetter(Character.isLetter(c) || isLetter);
        }
        digitLength = -1;
        digitLength();
        return this;
    }

    protected void handleMutualExclusivity() {
        // Mutual Exclusivity - If a certain item strongly indicates one component (like a month name), reduce the scores for unrelated components (like hours or minutes) for that item.
        for (final Score score : EXCLUSIVITY_SCORES) {
            if (scores.containsKey(score)) {
                scores.keySet().forEach(key -> {
                    if (!EXCLUSIVITY_SCORES.contains(key)) {
                        scores.remove(key);
                    }
                });
            }
        }
    }

    public boolean removePrefix(final char removePrefix) {
        if (length() > 1 && content.charAt(0) == removePrefix) {
            content(content.substring(1));
            return true;
        }
        return false;
    }

    public static String removePrefixes(final String string, final char c) {
        //JAVA 17 Function<String, Integer> lengthWithoutLeadingZeros = s -> s.length() - (int) s.chars().takeWhile(c -> c == '0').count();
        final char[] chars = string.toCharArray();
        final StringBuilder sb = new StringBuilder();
        boolean isPrefix = true;
        for (final char cc : chars) {
            if (isPrefix) {
                if (cc != c) {
                    isPrefix = false;
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
            .add("digit=" + digit())
            .add("content='" + content + "'")
            .toString();
    }
}
