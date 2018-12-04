package de.bentolor.toolbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TemplateFormatTest {
    /**
     * Apache jakarta commons logger
     */
    private static final Log LOG = LogFactory.getLog(TemplateFormatTest.class);

    private TemplateFormatter formatter;

    @Before
    public void init() {
        formatter = new TemplateFormatter();
    }

    @Test
    public void formatString() {
        // just to be sure
        formatter.setFieldAccessEnabled(true);
        formatter.setMethodAccessEnabled(true);
        formatter.setScriptingEnabled(true);

        final Dimension dimension = new Dimension(11, 12);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");
        map.put("size", dimension);

        assertEquals("bar", formatter.format("${foo}", map));
        assertEquals("BAR", formatter.format("${foo.toUpperCase}", map));
        assertEquals("11x12", formatter.format("${size.width}x${size.height}", map));
        assertEquals("bean access of Dimension.getSize() failed", dimension.toString(), formatter.format("${size.size}", map));

        if (formatter.isBeanShellAvailable()) {
            String script = "test:<bsh>return foo.length()</bsh><bsh>if ( foo.length()>10 ) return \"-lang\"; else return \"-kurz\";</bsh>";
            assertEquals("test:3-kurz", formatter.format(script, map));
        } else {
            TemplateFormatTest.LOG.info("Skipping beanshell tests. No bsh-xx.jar on classpath");
        }

        map.clear();
        map.put("s", new StringBuffer("value"));
        assertEquals("value", formatter.format("${s.toString}", map));
    }

    @Test
    public void dotAtTheEndOfString() {
        Map<String, Object> args = new HashMap<String, Object>();
        String text = "abc";
        args.put("text", text);
        args.put("stringLength", new Integer(4));
        assertEquals("Der Text " + text + " überschreitet die Maximallänge 4.", formatter.format("Der Text ${text} überschreitet die Maximallänge ${stringLength}.", args));
    }

    @Test
    public void formatStringWithStrangeBracesAndDots() {
        String s = "{{.{ala } } {gigi}";
        assertEquals(s, formatter.format(s, null));
        s = "${a${b}}${${c}}{$f.}";
        assertEquals(s, formatter.format(s, null));
    }

    @Test
    public void formatWithMissingParameters() {
        String s = "${missing} in ${action}.";
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("missing", "missing");
        assertEquals("missing in ${action}.", formatter.format(s, m));
    }

    @Test
    public void formatWithUnexistingMethod() {
        String s = "${a.b}. for {all}";
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("a", "a");
        assertEquals(s, formatter.format(s, m));
    }

    @Test
    public void formatWithExceptionThrowingMethod() {
        String s = "${a.b}";
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("a", new BMethod());
        assertEquals("${a.b}", formatter.format(s, m));
    }

    @Test
    public void formatWithVoidReturningMethod() {
        String s = "${a.b}";
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("a", new VoidMethod());
        assertEquals("${a.b}", formatter.format(s, m));

    }

    @Test
    public void formatWithNullValueParameter() {
        String s = "a ${v} b";
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("v", null);
        assertEquals("a " + TemplateFormatter.DEFAULT_NULL_STRING + " b", formatter.format(s, m));
        s = "a ${v.c} b";
        assertEquals("a  b", formatter.format(s, m));
    }

    private static final class BMethod {
        public String b() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class VoidMethod {
        public void v() {
        }
    }
}