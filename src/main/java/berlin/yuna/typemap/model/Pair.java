package berlin.yuna.typemap.model;

import java.util.Objects;

import static berlin.yuna.typemap.logic.TypeConverter.convertObj;

public class Pair<K, V> {

    final K key;
    final V value;

    public Pair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public K key() {
        return key;
    }

    public V value() {
        return value;
    }

    public <T> T key(final Class<T> type) {
        return convertObj(key, type);
    }

    public <T> T value(final Class<T> type) {
        return convertObj(value, type);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
            "key=" + key +
            ", value=" + value +
            '}';
    }
}

