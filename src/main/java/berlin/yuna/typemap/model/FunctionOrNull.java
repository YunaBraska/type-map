package berlin.yuna.typemap.model;

/**
 * A functional interface similar to {@link java.util.function.Function}, but
 * with the ability to ignore exceptions. The interface provides a method to
 * apply a function to a given input, with the provision to throw an exception.
 * If an exception occurs, the default implementation returns null.
 *
 * @param <S> the type of the input to the function.
 * @param <T> the type of the result of the function.
 */
@FunctionalInterface
public interface FunctionOrNull<S, T> {

    /**
     * Applies this function to the given argument, allowing exceptions to be thrown.
     *
     * @param source the function argument.
     * @return the function result.
     * @throws Exception if an error occurs during function application.
     */
    @SuppressWarnings("java:S112")
    T applyWithException(S source) throws Exception;

    /**
     * Applies this function to the given argument and handles any exceptions.
     * If an exception occurs, this method returns null.
     *
     * @param source the function argument.
     * @return the function result or null if an exception occurs.
     */
    default T apply(final S source) {
        try {
            return applyWithException(source);
        } catch (final Exception ignored) {
            return null;
        }
    }
}
