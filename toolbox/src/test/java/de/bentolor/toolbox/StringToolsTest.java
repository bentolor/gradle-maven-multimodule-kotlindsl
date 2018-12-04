package de.bentolor.toolbox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benjamin Schmid, @bentolor
 */
public class StringToolsTest {
    @Test
    public void testAbbreviate() throws Exception {
        assertEquals("n..", StringTools.abbreviate(3, null));
        assertEquals("Hallo", StringTools.abbreviate(5, "Hallo"));
        assertEquals("Ha..", StringTools.abbreviate(4, "Hallo"));
    }
}
