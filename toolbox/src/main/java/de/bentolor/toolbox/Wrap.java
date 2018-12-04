package de.bentolor.toolbox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Very little utility class for easy inline creation and transformation of collections into new, mutable <code>Collection</code>
 * instances. <p>You are exclusive owner of the returned collection objects. So it's safe to modify and use those collections as
 * regulary created ones.</p>
 *
 * @author Benjamin Schmid, @bentolor
 */
public final class Wrap {

    private Wrap() {
    }

    /**
     * Wraps the passed objects into a type-safe List. <p>Example: <br><code>List&lt;CharSequence&gt; l = Wrap.intoList("foo",
     * "bar"); </code> <br><code>List&lt;String&gt; l = Wrap.&lt;String&gt;intoList("foo", "bar"); </code>
     *
     * @param listContents The list of objects of type <code>T</code> to wrap int a list.
     * @return A new, mutable list collection containing the listContents objects.
     */
    @Nonnull
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> intoList(@Nullable T... listContents) {
        if (listContents != null) {
            return new ArrayList<T>(Arrays.asList(listContents));
        } else {
            return new ArrayList<T>();
        }
    }

    /**
     * Wraps the passed objects into a type-safe Set. <p>Example: <br><code>List&lt;CharSequence&gt; l = Wrap.intoList("foo",
     * "bar"); </code>
     *
     * @param setContents The list of objects of type <code>T</code> to wrap int a list.
     * @return A new, mutable list collection  containing the listContents objects.
     */
    @Nonnull
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Set<T> intoSet(@Nullable T... setContents) {
        if (setContents != null) {
            Set<T> set = new HashSet<T>(setContents.length + 16);
            List<T> contents = Arrays.asList(setContents);
            set.addAll(contents);
            return set;
        } else {
            return new HashSet<T>();
        }
    }

    /**
     * Wraps an alternating varagrs list of key/value objects into a new map. <p>Example: <br><code>Map&lt;String, Boolean&gt; l
     * = Wrap.intoMap("foo", true, "bar", false); </code>
     *
     * @param alternatingKeyValueInstances An even-sized vararg list of objects in the form KEY, VALUE, KEY, VALUE, ...
     * @return A new, mutable map filled with the varags as content.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <KEY, VALUE> Map<KEY, VALUE> intoMap(@Nullable Object... alternatingKeyValueInstances) {
        if (alternatingKeyValueInstances == null) {
            //noinspection ZeroLengthArrayAllocation
            alternatingKeyValueInstances = new Object[]{};
        }

        if (alternatingKeyValueInstances.length % 2 != 0) {
            throw new IllegalArgumentException("Only even-lengthed arguments allowed (key, value, key, value, ....)");
        }

        final Map<KEY, VALUE> map = new HashMap<KEY, VALUE>(alternatingKeyValueInstances.length + 16);
        KEY key;
        VALUE value;
        for (int i = 0; i < alternatingKeyValueInstances.length / 2; i++) {
            key = (KEY) alternatingKeyValueInstances[i * 2];
            value = (VALUE) alternatingKeyValueInstances[i * 2 + 1];
            map.put(key, value);
        }

        return map;
    }

    /**
     * Converts a passed enumeration into a List.
     *
     * @param enumeration The enumeration to traverse and copy into a new List object.
     * @return A new, mutable list collection filled with the enumeration contents.
     */
    @Nonnull
    public static <E> List<E> iterIntoList(@Nullable Iterator<E> enumeration) {
        if (enumeration == null) {
            return new ArrayList<E>();
        } else {
            List<E> values = new ArrayList<E>();
            while (enumeration.hasNext()) {
                values.add(enumeration.next());
            }
            return values;
        }
    }
}
