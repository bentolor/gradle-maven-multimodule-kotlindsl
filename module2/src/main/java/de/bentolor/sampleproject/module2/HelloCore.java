package de.bentolor.sampleproject.module2;

import de.bentolor.toolbox.StringTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A sample class depending on content of toolbox Module.
 */
public final class HelloCore {

    /* Apache jakarta commons logger instance. */
    private static final Log LOG = LogFactory.getLog(HelloCore.class);

    private HelloCore() {
    }

    public static String myFunction(String parameter) {
        LOG.debug("Shorting '" + parameter + '\'');
        return StringTools.abbreviate(5, parameter);
    }
}
