package de.bentolor.toolbox;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for wrap utility class.
 *
 * @author Benjamin Schmid, @bentolor
 */
public class WrapTest {

    @Test
    public void testIntoList() throws Exception {
        ArrayList<String> orig = new ArrayList<String>();
        orig.add("a");
        orig.add("b");
        orig.add("c");

        assertEquals(orig, Wrap.intoList("a", "b", "c"));

        List<CharSequence> actual = Wrap.intoList("a", "b", "c");
        assertEquals(orig, actual);

        orig.clear();
        orig.add(null);
        orig.add(null);

        assertEquals(orig, Wrap.<String>intoList(null, null));

        assertEquals(0, Wrap.intoList((String[]) null).size());
    }

    @Test
    public void testIntoSet() throws Exception {
        HashSet<String> orig = new HashSet<String>();
        orig.add("b");
        orig.add("a");
        orig.add("c");

        assertEquals(orig, Wrap.intoSet("a", "b", "c"));

        orig.add(null);
        Assert.assertFalse(orig.equals(Wrap.intoSet("a", "b", "c")));

        assertEquals(0, Wrap.intoSet((String[]) null).size());
    }

    @Test
    public void testIntoListEnumeration() throws Exception {
        ArrayList<String> orig = new ArrayList<String>();
        orig.add("a");
        orig.add("b");
        orig.add("c");

        assertEquals(orig, Wrap.iterIntoList(orig.iterator()));

        assertNotNull(Wrap.iterIntoList((Iterator<String>) null));
        assertTrue(Wrap.iterIntoList((Iterator<String>) null).isEmpty());
    }

    @Test
    public void testIntoMap() throws Exception {
        Map<String, Object> actual = Wrap.intoMap("name", "Gromit", "likes", "cheese", "id", 1234, "defines", null);
        Map<String, Object> orig = new HashMap<String, Object>();
        orig.put("name", "Gromit");
        orig.put("likes", "cheese");
        orig.put("id", 1234);
        orig.put("defines", null);

        assertEquals(orig, actual);

        assertEquals(0, Wrap.<String, String>intoMap((Object[]) null).size());

        try {
            Wrap.intoMap("name", "Gromit", "likes", "cheese", "id", 1234, "defines");
            fail("Non-even map argument count.");
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

    @Test
    public void testVarargsWarning() {
        Foo<Integer> foo = new Foo<Integer>();
        foo.testSignature(1, 2);
    }

    private static class Foo<T> {

        private List<T> testList = new ArrayList<>();

        public Foo() {

        }

        public void testSignature(T element1, T element2) {
            testList.addAll(Wrap.intoList(element1, element2));
        }

    }

}
