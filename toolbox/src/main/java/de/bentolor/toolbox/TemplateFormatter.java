package de.bentolor.toolbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Formats text message templates with values derived out of a set of named objects. object properties and methods and beanshell
 * output. <ul> <li>An object is referenced by its name in as <code>${objectname}</code></li> <li>An field of an object is
 * referenced inside the format string as <code>${object.fieldname}</code></li> <li>An bean value of an object is referenced
 * inside the format string as <code>${object.beanproperty}</code></li> <li>An method result of an object is referenced inside the
 * format string as <code>${object.methodname}</code></li> </ul>
 * <p>
 * It is also possible to execute bean shell scripts inside a format string. Therefore you <b>must provde and deploy the Bean
 * library</b> on your own. It's not bundled by default. Bean shell scripts are enclosed in {@link #SCRIPT_START_TAG} and {@link
 * #SCRIPT_END_TAG}. Inside a script the objects are referenced with names
 * <p>
 * Example:
 * <pre>
 * public static void main(String[] args) {
 *     TextFormat formatter = new TextFormat();
 *     Map map = new HashMap();
 *     map.put("foo", "bar");
 *     System.out.println(formatter.format("Uppercase: ${foo.toUpperCase}", map));
 *     System.out.println(formatter.format("Bewertung: Laenge " +
 *                                         "&lt;bsh&gt;" + " return foo.length()" + "&lt;/bsh&gt;\n" +
 *                                         "&lt;bsh&gt;" +  "if ( foo.length()&gt;10 )" +
 *                                         "  return 'lang'; else  return 'kurz';" + "&lt;/bsh&gt;", map));
 * }
 * </pre>
 */
public class TemplateFormatter {
    /**
     * Tag for opening a script section ({@value})
     */
    public static final String SCRIPT_START_TAG = "<bsh>";
    /**
     * Tag for closing a script section ({@value})
     */
    public static final String SCRIPT_END_TAG = "</bsh>";
    /**
     * Tag to declare and start a property reference (methods/field)
     */
    public static final String PROPERTY_START_TAG = "${";
    /**
     * Tag to end a property reference.
     */
    public static final String PROPERTY_CLOSE_TAG = "}";
    /**
     * Bean access possible?
     */
    public static final Boolean DEFAULT_BEANACCESS_ENABLED = true; // Originally a config facility was used here
    /**
     * Method access possible?
     */
    public static final Boolean DEFAULT_METHODS_ENABLED = true; // Originally a config facility was used here
    /**
     * Apache jakarta commons logger
     */
    private static final Log LOG = LogFactory.getLog(TemplateFormatter.class);
    /**
     * Default String representation for <code>null</code>
     */
    public static String DEFAULT_NULL_STRING = "<null>";
    /**
     * BeanShell access possible?
     */
    public static Boolean DEFAULT_BSH_ENABLED = false; // Originally a config facility was used here
    /**
     * Field access possible?
     */
    public static Boolean DEFAULT_FIELDS_ENABLED = true; // Originally a config facility was used here
    /**
     * @see #setFieldAccessEnabled
     * @see #isFormatFields
     */
    private boolean formatFields = TemplateFormatter.DEFAULT_FIELDS_ENABLED;

    /**
     * @see #setMethodAccessEnabled
     * @see #isFormatMethods
     */
    private boolean formatMethods = TemplateFormatter.DEFAULT_METHODS_ENABLED;

    /**
     * @see #setScriptingEnabled
     * @see #isFormatScripts
     */
    private boolean formatScripts = TemplateFormatter.DEFAULT_BSH_ENABLED;

    /**
     * @see #setFormatBeanValues(boolean)
     */
    private boolean formatBeanValues = TemplateFormatter.DEFAULT_BEANACCESS_ENABLED;

    /**
     * String representation of <code>null</code>
     */
    private String nullString = TemplateFormatter.DEFAULT_NULL_STRING;

    /**
     * Creates a new <code>TextFormat</code> instance.
     */
    public TemplateFormatter() {
    }

    /**
     * Returns the String representation of <code>null</code>.
     *
     * @return a <code>String</code> value
     * @see #setNullString
     * @see #DEFAULT_NULL_STRING
     */
    public final String getNullString() {
        return nullString;
    }

    /**
     * Sets the String representation of <code>null</code>. Default is {@link #DEFAULT_NULL_STRING}
     *
     * @param nullString a <code>String</code> value
     * @see #DEFAULT_NULL_STRING
     */
    public final void setNullString(String nullString) {
        this.nullString = nullString;
    }

    /**
     * Toogle access for <b>public</b> object field values.
     *
     * @param enabled <code>true</code> if enabled
     */
    public final void setFieldAccessEnabled(boolean enabled) {
        formatFields = enabled;
    }

    public final boolean isFormatFields() {
        return formatFields;
    }

    /**
     * Toggle access for <b>public</b> method return values.
     *
     * @param enabled <code>true</code> if enabled
     */
    public final void setMethodAccessEnabled(boolean enabled) {
        formatMethods = enabled;
    }

    public final boolean isFormatMethods() {
        return formatMethods;
    }

    public boolean isFormatBeanValues() {
        return formatBeanValues;
    }

    /**
     * Toggle access for bean property values.
     */
    public void setFormatBeanValues(final boolean formatBeanValues) {
        this.formatBeanValues = formatBeanValues;
    }

    /**
     * Switch on/off formatting scripts.
     *
     * @param enabled true if scripting is enabled
     */
    public final void setScriptingEnabled(boolean enabled) {
        formatScripts = enabled;
    }


    public final boolean isFormatScripts() {
        return formatScripts;
    }


    @SuppressWarnings({"OverlyLongMethod", "OverlyNestedMethod"})
    public final String formatProperties(String formatString, Map<String, Object> parameters) { // NOSONAR [bschmid] I know -- its long.

        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        try {
            int nextOpenToken = formatString.indexOf(TemplateFormatter.PROPERTY_START_TAG);
            int nextCloseToken = formatString.indexOf(TemplateFormatter.PROPERTY_CLOSE_TAG, nextOpenToken > 0 ? nextOpenToken : 0);
            int nextDot = formatString.indexOf('.', nextOpenToken > 0 ? nextOpenToken : 0);
            if (nextDot > nextCloseToken) {
                // The dot has to appear between the open and close token
                nextDot = -1;
            }

            while (nextOpenToken >= 0) {
                if (nextCloseToken > nextOpenToken + 2) {
                    if (nextDot < 0) {
                        // trivial case: ${objectname}
                        String objectName = formatString.substring(nextOpenToken + 2, nextCloseToken);
                        if (parameters.containsKey(objectName)) {
                            Object objectValue = parameters.get(objectName);
                            formatString = formatString.replace("${" + objectName + "}",
                                    valueToString(objectName, objectValue));
                        }
                    } else if (nextDot > nextOpenToken + 2 && nextDot < nextCloseToken - 1) {
                        // case ${objectname.name}
                        String objectName = formatString.substring(nextOpenToken + 2, nextDot);
                        Object objectValue = parameters.get(objectName);
                        String fullTokenName = formatString.substring(nextDot + 1, nextCloseToken);

                        Object replacement = objectValue;
                        boolean replacementValid = true;
                        int tokenNameIdx = 0;
                        while (tokenNameIdx < fullTokenName.length()) {
                            int nextSubTokenIdx = fullTokenName.indexOf('.', tokenNameIdx + 1);
                            if (nextSubTokenIdx < 0) {
                                nextSubTokenIdx = fullTokenName.length();
                            }
                            String tokenName = fullTokenName.substring(tokenNameIdx, nextSubTokenIdx);

                            tokenNameIdx = nextSubTokenIdx + 1;
                            if (replacement != null) {
                                final Class<?> objectClass = replacement.getClass();
                                final Field[] fieldList = objectClass.getFields();
                                final Method[] methodList = objectClass.getMethods();

                                // search for fitting fields
                                if (formatFields) { // NOSONAR [bschmid] Deeply nested ifs.
                                    Field field = null;
                                    for (Field fieldIter : fieldList) {
                                        if (fieldIter.getName().equals(tokenName)) {
                                            field = fieldIter;
                                            break;
                                        }
                                    }
                                    if (field != null) {
                                        replacement = field.get(replacement);
                                        continue;
                                    }
                                }

                                if (formatBeanValues) {// NOSONAR [bschmid] Deeply nested ifs.
                                    Method method = null;
                                    final String baseName = (Character.toUpperCase(tokenName.charAt(0))) + tokenName.substring(1);
                                    final String getterName = "get" + baseName;
                                    final String isName = "is" + baseName;
                                    for (Method method1 : methodList) {
                                        // We need to get the method without return arguments
                                        final String methodname = method1.getName();
                                        if ((methodname.equals(getterName) || methodname.equals(isName))
                                                && method1.getParameterTypes().length == 0) {
                                            method = method1;
                                            break;
                                        }
                                    }
                                    if (method != null) {
                                        replacement = method.invoke(replacement);
                                        continue;
                                    }
                                }

                                if (formatMethods) {// NOSONAR [bschmid] Deeply nested ifs.
                                    Method method = null;
                                    for (Method method1 : methodList) {
                                        // We need to get the method without return arguments
                                        if (method1.getName().equals(tokenName) && method1.getParameterTypes().length == 0) {
                                            method = method1;
                                            break;
                                        }
                                    }
                                    if (method != null) {
                                        replacement = method.invoke(replacement);
                                        continue;
                                    }
                                }

                                replacementValid = false; // no replacement strategy worked.
                            }
                        }
                        if (replacement != null && replacementValid) {
                            formatString = formatString.replace("${" + objectName + "." + fullTokenName + "}",
                                    valueToString(fullTokenName, replacement));
                        } else if (replacement == null && replacementValid) {
                            formatString = formatString.replace("${" + objectName + "." + fullTokenName + "}", "");
                        }
                    }
                }
                nextOpenToken = formatString.indexOf(TemplateFormatter.PROPERTY_START_TAG, nextOpenToken + 1);
                nextCloseToken = formatString.indexOf(TemplateFormatter.PROPERTY_CLOSE_TAG, nextOpenToken + 3);
                nextDot = formatString.indexOf(".", nextOpenToken + 3);
                if (nextDot > nextCloseToken) {
                    // The dot has to appear between the open and close token
                    nextDot = -1;
                }
            }
        } catch (IllegalAccessException e) {
            if (TemplateFormatter.LOG.isTraceEnabled()) {
                TemplateFormatter.LOG.trace("Exception during TemplateFormatter: " + e + ". Cause is: ", e.getCause());
            } else if (TemplateFormatter.LOG.isDebugEnabled()) {
                TemplateFormatter.LOG.debug("Exception during TemplateFormatter: " + e + ". Cause is: " + e.getCause());
            }
        } catch (InvocationTargetException e) {
            if (TemplateFormatter.LOG.isTraceEnabled()) {
                TemplateFormatter.LOG.trace("Exception during TemplateFormatter: " + e + ". Cause is: ", e.getCause());
            } else if (TemplateFormatter.LOG.isDebugEnabled()) {
                TemplateFormatter.LOG.debug("Exception during TemplateFormatter: " + e + ". Cause is: " + e.getCause());
            }
        }

        return formatString;
    }


    public final String formatScript(String script, Map<String, Object> parameters, Object interpreter) {

        try {
            for (Map.Entry<String, Object> stringObjectEntry : parameters.entrySet()) {
                getInterpreterSetMethod().invoke(interpreter, stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }

            Object result = getInterpreterEvalMethod().invoke(interpreter, script);

            // reset interpreter
            for (String objectName : parameters.keySet()) {
                getInterpreterUnsetMethod().invoke(interpreter, objectName);
            }

            return result != null ? result.toString() : nullString;
        } catch (Exception ex) {
            TemplateFormatter.LOG.error("Exception in TemplateFormatter: " + ex);
            return "[Script-Error: " + ex.getClass().getName() + "]";
        }
    }

    protected final String formatScripts(String formatString, Map<String, Object> parameters) {
        try {
            final StringBuilder result = new StringBuilder();
            Object interpreter = null;
            int index = 0;
            int startScript = formatString.indexOf(TemplateFormatter.SCRIPT_START_TAG, index);
            int endScript = formatString.indexOf(TemplateFormatter.SCRIPT_END_TAG, index);
            while (startScript >= 0 && endScript > startScript) {
                // Script prefix
                result.append(formatString, index, startScript);

                // script content
                String script = formatString.substring(startScript + TemplateFormatter.SCRIPT_START_TAG.length(), endScript);
                if (interpreter == null) {
                    interpreter = createInterpreter();
                }
                result.append(formatScript(script, parameters, interpreter));

                // next script?
                index = endScript + TemplateFormatter.SCRIPT_END_TAG.length();
                startScript = formatString.indexOf(TemplateFormatter.SCRIPT_START_TAG, index);
                endScript = formatString.indexOf(TemplateFormatter.SCRIPT_END_TAG, index);
            }

            // append script suffix
            if (index < formatString.length()) {
                result.append(formatString.substring(index));
            }

            return result.toString();
        } catch (Exception e) {
            if (TemplateFormatter.LOG.isDebugEnabled()) {
                TemplateFormatter.LOG.debug("Exception during TemplateFormatter script: " + e + ". Cause is: ", e.getCause());
            } else if (TemplateFormatter.LOG.isWarnEnabled()) {
                TemplateFormatter.LOG.warn("Exception during TemplateFormatter script: " + e + ". Cause is: " + e.getCause());
            }
        }
        return formatString;
    }


    public final String format(String formatString, Map<String, Object> parameters) {
        if (formatFields || formatMethods || formatBeanValues) {
            formatString = formatProperties(formatString, parameters);
        }

        if (formatScripts) {
            formatString = formatScripts(formatString, parameters);
        }

        return formatString;
    }

    public final boolean isBeanShellAvailable() {
        try {
            createInterpreter();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * You can override this method for custom object formatting.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected String valueToString(String fulltoken, Object tokenValue) {
        return tokenValue != null ? tokenValue.toString() : nullString;
    }


    @SuppressWarnings("MethodWithTooExceptionsDeclared")
    protected Object createInterpreter() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> bshInterpreterClass = Class.forName("bsh.Interpreter");
        return bshInterpreterClass.getConstructor().newInstance();
    }

    protected Method getInterpreterSetMethod() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> bshInterpreterClass = Class.forName("bsh.Interpreter");
        return bshInterpreterClass.getMethod("set", String.class, Object.class);
    }

    protected Method getInterpreterUnsetMethod() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> bshInterpreterClass = Class.forName("bsh.Interpreter");
        return bshInterpreterClass.getMethod("unset", String.class);
    }

    protected Method getInterpreterEvalMethod() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> bshInterpreterClass = Class.forName("bsh.Interpreter");
        return bshInterpreterClass.getMethod("eval", String.class);
    }
}














