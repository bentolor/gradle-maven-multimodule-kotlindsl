package de.bentolor.sampleproject.module1;

import de.bentolor.toolbox.StringTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A sample class depending on content of toolbox Module.
 */
public final class HelloModule1 {

    /* Apache jakarta commons logger instance. */
    private static final Log LOG = LogFactory.getLog(HelloModule1.class);

    private HelloModule1() {
    }

    public static String myFunction(String parameter) {
        LOG.debug("Shorting '" + parameter + '\'');
        return StringTools.abbreviate(5, parameter);
    }
}
