package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.JsonDecoder;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Optional.ofNullable;

/**
 * {@link ConcurrentTypeSet} is a specialized implementation of {@link CopyOnWriteArrayList} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link ConcurrentTypeSet}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class ConcurrentTypeSet extends CopyOnWriteArrayList<Object> implements TypeListI<ConcurrentTypeSet> {

    /**
     * Default constructor for creating an empty {@link ConcurrentTypeSet}.
     */
    public ConcurrentTypeSet() {
        this((Collection<?>) null);
    }

    /**
     * Constructs a new {@link ConcurrentTypeSet} of the specified json.
     */
    public ConcurrentTypeSet(final String json) {
        this(JsonDecoder.jsonListOf(json));
    }

    /**
     * Constructs a new {@link ConcurrentTypeSet} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public ConcurrentTypeSet(final Collection<?> map) {
        ofNullable(map).ifPresent(super::addAll);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element <tt>e</tt> to this set if
     * this set contains no element <tt>e2</tt> such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.
     *
     * @param value element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     */
    @Override
    public boolean add(final Object value) {
        if (!this.contains(value)) {
            return super.add(value);
        }
        return false;
    }

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     */
    @Override
    public void add(final int index, final Object value) {
        if (!this.contains(value)) {
            if (index >= 0 && index < this.size()) {
                super.add(index, value);
            } else {
                super.add(value);
            }
        }
    }

    /**
     * Adds all the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param collection collection containing elements to be added to this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(Object)
     */
    @Override
    public boolean addAll(final Collection<?> collection) {
        boolean modified = false;
        for (final Object value : collection) {
            if (add(value)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link ConcurrentTypeSet} instance for chaining.
     */
    @Override
    public ConcurrentTypeSet addReturn(final int index, final Object value) {
        this.add(index, value);
        return this;
    }

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link ConcurrentTypeSet} instance for chaining.
     */
    public ConcurrentTypeSet addReturn(final Object index, final Object value) {
        if (index == null) {
            super.add(value);
        } else if (index instanceof Number) {
            this.addReturn(((Number) index).intValue(), value);
        }
        return this;
    }
}
