package berlin.yuna.typemap.model;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static berlin.yuna.typemap.model.TypeMap.treeGet;

public class Type<T> implements Iterable<T>, TypeInfo<Type<T>> {

    private T value;

    private static final Type<?> EMPTY = new Type<>(null);

    public static <T> Type<T> typeOf(final T value) {
        return new Type<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Type<T> empty() {
        return (Type<T>) EMPTY;
    }

    public Type(final T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    public Type<T> value(final T value) {
        this.value = value;
        return this;
    }

    /**
     * Use {@link Type#value(Object)} instead
     *
     * @param key   sets the key of the current {@link Type}
     * @param value sets the key of the current {@link Type} if `key` is null
     * @return the current instance {@link Type}
     */
    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public Type<T> addR(final Object key, final Object value) {
        this.value = (T) (key != null ? key : value);
        return this;
    }

    /**
     * If a value is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return !isEmpty();
    }

    /**
     * If a value is  not present, returns {@code true}, otherwise
     * {@code false}.
     *
     * @return {@code true} if a value is not present, otherwise {@code false}
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Fluent type-check if the current {@link TypeInfo} is a {@link TypeMapI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeMapI}, else returns self.
     */
    @Override
    public Type<TypeMapI<?>> typeMapOpt() {
        return value instanceof TypeMapI ? typeOf((TypeMapI<?>) value) : empty();
    }

    /**
     * Fluent type-check if the current {@link TypeInfo} is a {@link TypeListI}
     *
     * @return {@link Optional#empty()} if current object is not a {@link TypeListI}, else returns self.
     */
    @Override
    public Type<TypeListI<?>> typeListOpt() {
        return value instanceof TypeListI ? typeOf((TypeListI<?>) value) : empty();
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise does nothing.
     *
     * @param action the action to be performed, if a value is present
     * @return The current instance for method chaining.
     */
    public Type<T> ifPresent(final Consumer<T> action) {
        ifPresentOrElse(action, null);
        return this;
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action the action to be performed, if a value is present
     * @param orElse the empty-based action to be performed, if no value is present
     * @return The current instance for method chaining.
     */
    public Type<T> ifPresentOrElse(final Consumer<T> action, final Runnable orElse) {
        if (value != null) {
            if(action != null)
                action.accept(value);
        } else if (orElse != null) {
            orElse.run();
        }
        return this;
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@link Type} describing the value, otherwise returns an
     * empty {@link Type}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@link Type} describing the value of this
     * {@link Type}, if a value is present and the value matches the
     * given predicate, otherwise an empty {@link Type}
     */
    public Type<T> filter(final Predicate<? super T> predicate) {
        return (value != null && predicate != null && predicate.test(value)) ? this : new Type<>(null);
    }

    /**
     * If a value is present, returns an {@link Type} the result of applying the given mapping function to
     * the value, otherwise returns an empty {@link Type}.
     *
     * <p>If the mapping function returns a {@code null} result then this method
     * returns an empty {@link Type}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return an {@link Type} describing the result of applying a mapping
     */
    public <R> Type<R> map(final Function<? super T, ? extends R> mapper) {
        return (value != null && mapper != null) ? new Type<>(mapper.apply(value)) : new Type<>(null);
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@link Type}-bearing mapping function to the value, otherwise returns
     * an empty {@link Type}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@link Type}-bearing mapping
     * function to the value of this {@link Type}, if a value is
     * present, otherwise an empty {@link Type}
     */
    public <R> Type<R> flatMap(final Function<? super T, ? extends Type<? extends R>> mapper) {
        if (value != null && mapper != null) {
            @SuppressWarnings("unchecked") final Type<R> result = (Type<R>) mapper.apply(value);
            return result != null ? result : new Type<>(null);
        } else {
            return new Type<>(null);
        }
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@link Type}-bearing mapping function to the value, otherwise returns
     * an empty {@link Type}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@link Type}-bearing mapping
     * function to the value of this {@link Type}, if a value is
     * present, otherwise an empty {@link Type}
     */
    @SuppressWarnings({"unchecked", "java:S2789", "java:S4968"})
    public <R> Type<R> flatOpt(final Function<? super T, ? extends Optional<? extends R>> mapper) {
        if (value != null && mapper != null) {
            final Optional<R> result = (Optional<R>) mapper.apply(value);
            return result != null && result.isPresent() ? new Type<>(result.get()) : new Type<>(null);
        } else {
            return new Type<>(null);
        }
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing
     * only that value or in case of {@link Iterable} returns values.
     *
     * @return the value(s) as a {@link Stream}
     */
    public Stream<T> stream() {
        return value != null ? Stream.of(value) : Stream.empty();
    }

    /**
     * If a value is present, returns the value, otherwise returns {@code other}.
     *
     * @param other the value to be returned, if no value is present.  May be {@code null}.
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(final T other) {
        return value != null ? value : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     * supplying function
     */
    public T orElseGet(final Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Optional}
     */
    public T orElseThrow() {
        return orElseThrow(() -> new NoSuchElementException("No value present"));
    }

    /**
     * If a value is present, returns the value, otherwise throws exception
     *
     * @param exceptionSupplier the supplying function that produces an exception to be thrown
     * @return the non-{@code null} value described by this {@code Optional}
     */
    public <X extends Throwable> T orElseThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Type)) return false;
        final Type<?> type = (Type<?>) o;
        return Objects.equals(value, type.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return toJson(value);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return Stream.of(value).iterator();
    }
}
