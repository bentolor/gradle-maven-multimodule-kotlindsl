package de.bentolor.toolbox;

import java.util.*;

/**
 * Set implementation which holds its elements wrapped inside a {@link java.lang.ref.WeakReference weak reference}
 * instance, so that they can be garbage collected.
 *
 * @param <TYPE> the type of the elements held by this set.
 */
public class WeakSet<TYPE> implements Set<TYPE> {

    private final Map<TYPE, String> map = new WeakHashMap<TYPE, String>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<TYPE> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public boolean add(TYPE o) {
        if (map.containsKey(o)) {
            return false;
        }
        map.put(o, null);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!map.containsKey(o)) {
            return false;
        }
        map.remove(o);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends TYPE> c) {
        boolean modified = false;
        for (TYPE e : c) {
            boolean added = add(e);
            if (!modified) {
                modified = added;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<TYPE> toRemove = new HashSet<TYPE>(map.keySet());
        boolean removed = false;
        for (Object element : toRemove) {
            if (c.contains(element)) {
                continue;
            }
            remove(element);
            removed = true;
        }
        return removed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object toRemove : c) {
            boolean removed = remove(toRemove);
            if (!modified) {
                modified = removed;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        map.clear();
    }

}
