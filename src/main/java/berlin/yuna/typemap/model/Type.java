package berlin.yuna.typemap.model;

import java.util.Optional;

public class Type implements TypeI<Type> {

    final Object object;

    public static Type typeOf(final Object object) {
        return new Type(object);
    }

    public Type(final Object object) {
        this.object = object;
    }

    @Override
    public Type addReturn(final Object key, final Object value) {
        return null;
    }

    @Override
    public Optional<TypeMapI<?>> typeMapOpt() {
        return Optional.empty();
    }

    @Override
    public Optional<TypeListI<?>> typeListOpt() {
        return Optional.empty();
    }
}
