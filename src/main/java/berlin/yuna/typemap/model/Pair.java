package berlin.yuna.typemap.model;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static berlin.yuna.typemap.logic.JsonEncoder.toJson;
import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static java.util.stream.Collectors.toMap;

public class Pair<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;

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

    public Pair<K, V> key(final K key) {
        this.key = key;
        return this;
    }

    public Pair<K, V> value(final V value) {
        this.value = value;
        return this;
    }

    public <T> T key(final Class<T> type) {
        return convertObj(key, type);
    }

    public <T> T value(final Class<T> type) {
        return convertObj(value, type);
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(final V value) {
        final V oldValue = this.value;
        this.value = value;
        return oldValue;
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
        return toJson(Stream.of(new AbstractMap.SimpleEntry<>(key, value)).collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}

