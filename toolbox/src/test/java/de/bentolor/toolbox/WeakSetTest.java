package de.bentolor.toolbox;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


public class WeakSetTest {

    private WeakSet<Object> set;

    public WeakSetTest() {
    }

    @Before
    public void init() {
        set = new WeakSet<Object>();
    }

    @Test
    public void addElements() {
        Object o1 = new Object();
        Object o2 = new Object();
        Set<Object> other = new HashSet<Object>();
        other.add(o1);
        other.add(o2);
        set.add(o1);
        set.add(o2);
        assertEquals(2, set.size());
        for (Object o : set) {
            assertTrue(other.contains(o));
        }
    }

    @SuppressWarnings("CallToSystemGC")
    @Test(timeout = 25000)
    public void testGarbageCollection() throws InterruptedException {
        Object o1 = new Object();
        set.add(o1);
        int collectableElements = 1000;
        Set<Object> referenceHolder = new HashSet<Object>();
        for (int i = 0; i < collectableElements; i++) {
            Object obj = new Object();
            referenceHolder.add(obj);
            set.add(obj);
        }
        assertEquals(collectableElements + 1, set.size());
        // We shall remove the elements from the referenceHolder
        // so that gc can clean the weak references up (at least some of them).
        referenceHolder.clear();
        while (true) {
            System.gc();
            if (set.size() < collectableElements + 1) {
                break;
            }
            Thread.sleep(50);
        }
        // This is a stupid statement, but we need to hold the reference to o1.
        // Otherwise it would have been collected....
        assertNotNull(o1);
    }

}
