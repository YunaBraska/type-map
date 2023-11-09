package berlin.yuna.typemap.model;

import java.util.function.Function;

@FunctionalInterface
public interface FunctionOrNull<S, T> {

    @SuppressWarnings("java:S112")
    T applyWithException(S source) throws Exception;

    default T apply(final S source) {
        try {
            return applyWithException(source);
        } catch (final Exception ignored) {
            return null;
        }
    }

    static <S, T> FunctionOrNull<S, T> of(final Function<S, T> function) {
        return source -> {
            try {
                return function.apply(source);
            } catch (final Exception ignored) {
                return null;
            }
        };
    }
}
