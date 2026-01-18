package berlin.yuna.typemap.model;

import berlin.yuna.typemap.logic.JsonEncoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static berlin.yuna.typemap.logic.TypeConverter.convertObj;
import static berlin.yuna.typemap.model.Type.typeOf;

public class Pair<K, V> implements Map.Entry<K, V>, TypeInfo<Pair<K, V>> {

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

    /**
     * Converts key and value into a new {@link Pair} using the provided target types.
     */
    public <C, R> Pair<C, R> to(final Class<? extends C> keyType, final Class<? extends R> valueType) {
        return new Pair<>(convertObj(key, keyType), convertObj(value, valueType));
    }

    /**
     * Wraps the key in a {@link Type} to access conversion helpers such as {@code asInt()}.
     */
    public Type<K> keyType() {
        return typeOf(key);
    }

    /**
     * Wraps the value in a {@link Type} to access conversion helpers such as {@code asBoolean()}.
     */
    public Type<V> valueType() {
        return typeOf(value);
    }

    /**
     * Alias for {@link #valueType()} to highlight {@link TypeInfo}-style access to the value.
     */
    public Type<V> valueInfo() {
        return valueType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Pair<K, V> addR(final Object key, final Object value) {
        if (key != null) {
            this.key = (K) key;
        } else {
            this.value = (V) value;
        }
        return this;
    }

    @Override
    public Type<TypeMapI<?>> typeMapOpt() {
        return value instanceof TypeMapI ? typeOf((TypeMapI<?>) value) : typeOf(null);
    }

    @Override
    public Type<TypeListI<?>> typeListOpt() {
        return value instanceof TypeListI ? typeOf((TypeListI<?>) value) : typeOf(null);
    }

    @Override
    public Type<?> asOpt(final Object... path) {
        if (path == null || path.length == 0)
            return typeOf(value);
        final Object selector = path[0];
        if ("key".equals(selector) || (selector instanceof final Number num && num.intValue() == 0))
            return typeOf(key);
        if ("value".equals(selector) || (selector instanceof final Number num && num.intValue() == 1))
            return typeOf(value);
        return typeOf(null);
    }

    public <T> T keyAs(final Class<T> type) {
        return keyType().get(type);
    }

    public <T> T valueAs(final Class<T> type) {
        return valueType().get(type);
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
        final Map<Object, Object> map = new LinkedHashMap<>(1);
        map.put(key, value);
        return JsonEncoder.toJson(map);
    }
}
