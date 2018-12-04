package de.bentolor.toolbox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benjamin Schmid, @bentolor
 */
public class StringToolsTest {
    @Test
    public void testAbbreviate() throws Exception {
        assertEquals("nu…", StringTools.abbreviate(3, null));
        assertEquals("Hallo", StringTools.abbreviate(5, "Hallo"));
        assertEquals("Hal…", StringTools.abbreviate(4, "Hallo"));
    }
}
