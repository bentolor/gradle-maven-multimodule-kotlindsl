package de.bentolor.toolbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Benjamin Schmid, @bentolor
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public class ExceptionToolsTest {
    /* Apache jakarta commons logger instance. */
    private static final Log LOG = LogFactory.getLog(ExceptionToolsTest.class);

    private static final String SRCDIR_FROM_PROJECTROOT = "core-toolbox/src/main/java";
    private static final String TESTSRCDIR_FROM_PROJECTROOT = "core-toolbox/src/test/java";
    private static final String SRCDIR_FROM_MODULEROOT = "src/main/java";
    private static final String TESTSRCDIR_FROM_MODULEROOT = "src/test/java";

    private File[] codeSearchPaths;
    private StackTraceElement validCodePointerLine1;

    @Before
    public void setUp() throws Exception {
        validCodePointerLine1 = new StackTraceElement(this.getClass().getName(), "foo", null, 1);
        codeSearchPaths = new File[]{
                new File(ExceptionToolsTest.SRCDIR_FROM_PROJECTROOT.replace('/', File.separatorChar)),
                new File(ExceptionToolsTest.TESTSRCDIR_FROM_PROJECTROOT.replace('/', File.separatorChar)),
                new File(ExceptionToolsTest.SRCDIR_FROM_MODULEROOT.replace('/', File.separatorChar)),
                new File(ExceptionToolsTest.TESTSRCDIR_FROM_MODULEROOT.replace('/', File.separatorChar))
        };
    }

    @Test
    public void testRetrieveSurroundingSourceLines() throws Exception {
        assertNull(ExceptionTools.retrieveProblemDetails(null, null, null, 1));
    }

    @Test
    public void testRetrieveSurroundingSourceLinesViaFilePath() throws Exception {
        StackTraceElement fooCodePointer = new StackTraceElement(String.class.getName(), "foo", null, 175);
        Assert.assertNull(ExceptionTools.retrieveProblemDetails(null, fooCodePointer, null, 10));

        ExceptionTools.ProblemDetails line1SurroundCode =
                ExceptionTools.retrieveProblemDetails(null, validCodePointerLine1, codeSearchPaths, 1);
        assertNotNull(line1SurroundCode);
        assertNotNull(line1SurroundCode.getCode());

        assertEquals("", line1SurroundCode.getCode()[0]);
        assertEquals("package " + this.getClass().getPackage().getName() + ";", line1SurroundCode.getCode()[1]);
    }

    @Test
    public void testRetrieveSurroundingSourceLinesViaClassPath() throws Exception {
        StackTraceElement validCodePointer =
                new StackTraceElement("com.sun.org.apache.xml.internal.serializer.Encodings", "foo", "Encodings.properties", 1);
        ExceptionTools.ProblemDetails line1SurroundCode =
                ExceptionTools.retrieveProblemDetails(null, validCodePointer, codeSearchPaths, 0);
        assertNotNull(line1SurroundCode);
        assertNotNull(line1SurroundCode.getCode());
        assertEquals(1, line1SurroundCode.getCode().length);
        assertTrue(line1SurroundCode.getCode()[0].trim().length() > 0);
    }

    @Test
    public void codePointFiltering() {
        StackTraceElement[] stackTrace = new IllegalArgumentException().getStackTrace();
        assertNull(ExceptionTools.pickCodePointerByPackage(stackTrace, "foo.bar"));
        assertNotNull(ExceptionTools.pickCodePointerByPackage(stackTrace, "de.bentolor"));
        assertEquals(ExceptionTools.pickCodePointerByPackage(stackTrace, "de.bentolor"), stackTrace[0]);
    }

    @Test
    public void testAsciiArt() {
        String fooString = "abc";
        Throwable fakeProblem = null;
        try {
            String b = fooString.substring(5, 10);
        } catch (Exception e) {
            fakeProblem = e;
        }

        String[] message = ExceptionTools.describeThrowableAsciiArt(codeSearchPaths, fakeProblem, "de.bentolor");
        assertNotNull(message);
        assertTrue(message.length > 10);
        for (String s : message) {
            ExceptionToolsTest.LOG.info(s);
        }

        message = ExceptionTools.describeThrowableAsciiArt(codeSearchPaths, new IllegalArgumentException(), "de.bentolor");
        assertNotNull(message);
        assertTrue(message.length > 10);
        for (String s : message) {
            ExceptionToolsTest.LOG.info(s);
        }
    }

    @Test
    public void testAsciiHtml() {
        String message = ExceptionTools.describeThrowableHtml(codeSearchPaths, new IllegalArgumentException(), "de.bentolor");
        assertNotNull(message);
        ExceptionToolsTest.LOG.info(message);
    }

    @Test
    public void testIDGeneration() {
        String sampleID = ExceptionTools.generateID();
        ExceptionToolsTest.LOG.info("Sample generated ID: " + sampleID);
        for (int i = 0; i < 1000; i++) {
            sampleID = ExceptionTools.generateID();
            assertNotNull("ID = null?", sampleID);
            assertTrue(sampleID, sampleID.length() > 4);
            assertTrue(sampleID, sampleID.matches("[a-zA-Z0-9]+"));
        }
    }

}
