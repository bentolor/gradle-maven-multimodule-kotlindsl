package de.bentolor.toolbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Utility class to analyze, present and enhance user exception expierience.
 *
 * @author Benjamin Schmid, @bentolor
 */
public final class ExceptionTools {

    private static final String[] EMPTY_STRINGARRAY = {};
    private static final int DEFAULT_SURROUND_LINE_COUNT = 5;
    private static final String ASCRII_SEPARATORLINE = "--------";
    private static final String STYLE_DECLARATION = "<style type=\"text/css\">\n" +
            "    .selfdescribingex {\n" +
            "        font-family: Segoe UI, Tahoma, sans-serif;\n" +
            "        margin: 0px;\n" +
            "        padding: 0px;\n" +
            "    }\n" +
            "    .selfdescribingex h1 {\n" +
            "        margin: 10px 0px 8px 0px;\n" +
            "        padding: 0px;\n" +
            "    }\n" +
            "    .exheader {\n" +
            "        background-color: #fcc;\n" +
            "        padding: 5px 15px 15px 15px;\n" +
            "    }\n" +
            "    .footer {\n" +
            "        background-color: #eee;\n" +
            "        padding: 5px 15px 15px 15px;\n" +
            "        font-size:small;\n" +
            "    }\n" +
            "    .codepointer {\n" +
            "        background-color: #f6f6f6;\n" +
            "        padding: 20px 15px;\n" +
            "        border-top: 2px solid #ccc;\n" +
            "        border-bottom: 2px solid #ccc;\n" +
            "    }\n" +
            "    .codestack {\n" +
            "        font-size: 0.85em;\n" +
            "        margin: 0px;\n" +
            "        padding: 0px;\n" +
            "    }\n" +
            "    .location  {\n" +
            "        font-weight: 400;\n" +
            "        color: #666666;\n" +
            "        margin-bottom: 6px;\n" +
            "    }\n" +
            "    .linenum {\n" +
            "        float: left;\n" +
            "        text-align: right;\n" +
            "        width: 50px;\n" +
            "        padding: 0px 0px;\n" +
            "        background-color: #ddf;\n" +
            "        border-right: 1px solid #999;\n" +
            "    }\n" +
            "    .codeline {\n" +
            "        font-family: monospace;\n" +
            "        padding: 1px 6px;\n" +
            "        white-space: pre;\n" +
            "    }\n" +
            "    .active {\n" +
            "        font-weight: bolder;\n" +
            "        color: #090;\n" +
            "    }\n" +
            "</style>";

    private static final String PROLOG = "<div class=\"selfdescribingex\">\n" +
            "    <div class=\"exheader\">\n" +
            "        <h1>Java Exception occured</h1>\n" +
            "        MESSAGE\n" +
            "    </div>\n" +
            "    <div class=\"codepointer\">\n" +
            "        <div class=\"location\">LOCATION</div>\n" +
            "        <div class=\"codestack\">";
    private static final String CODELINE = "<div class=\"codeline\"><div class=\"linenum\">LINENUM:</div><span class=\"codeline\">LINECONTENT</span></div>\n";
    private static final String CODELINE_ACTIVE = "<div class=\"codeline active\"><div class=\"linenum\">LINENUM:</div><span class=\"codeline\">LINECONTENT</span></div>\n";
    private static final String EPILOGUE = "        </div>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"footer\">\n" +
            "        This problem occurred on DATE<!-- and has been logged with id ID-->.\n" +
            "    </div>\n" +
            "</div>";

    /**
     * Apache jakarta commons logger instance sepcially for logging exceptions for later analysis purposes. The Logger name is
     * <code>EXCEPTIONS</code>.
     */
    public static final Log LOG = LogFactory.getLog("EXCEPTIONS"); // NOSONAR

    private ExceptionTools() {
    }

    /**
     * Returns a parameter object describing code details about the passed exception. T
     *
     * @param codeSearchPaths         a list of directories to look for adressed source code files. May be empty or <code>null</code>
     * @param exception               The throwable describe
     * @param acceptedPackagePrefixes A String array of valid package prefixes
     * @return A String array guaranteed to be non-empty and non-null.
     */
    public static ProblemDetails describeThrowable(File[] codeSearchPaths, Throwable exception, String... acceptedPackagePrefixes) {
        if (exception != null) {
            StackTraceElement pickedProblemSpot = pickCodePointerByPackage(exception.getStackTrace(), acceptedPackagePrefixes);
            return retrieveProblemDetails(exception, pickedProblemSpot, codeSearchPaths, DEFAULT_SURROUND_LINE_COUNT);
        } else {
            return null;
        }
    }

    /**
     * Returns a String array pretty-printing and describing the passed exception via ascii art. To be pretty, the content should
     * be printed using a non-proportional font.
     *
     * @param codeSearchPaths         a list of directories to look for adressed source code files. May be empty or <code>null</code>
     * @param exception               The throwable describe
     * @param acceptedPackagePrefixes A String array of valid package prefixes
     * @return A String array guaranteed to be non-empty and non-null.
     */
    public static String[] describeThrowableAsciiArt(File[] codeSearchPaths, Throwable exception, String... acceptedPackagePrefixes) {
        if (exception == null) {
            return EMPTY_STRINGARRAY;
        }

        List<String> result = new ArrayList<String>();
        StackTraceElement causePoint = exception.getStackTrace()[0];

        if (exception.getMessage() != null) {
            result.add("Problem:  " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
            result.add("Location: " + causePoint.getClassName() + " (around line " + causePoint.getLineNumber() + ")");
        } else {
            result.add(exception.getClass().getSimpleName() + " in " + causePoint.getClassName() + " around line " + causePoint.getLineNumber());
        }


        StackTraceElement pickedCodePointer = pickCodePointerByPackage(exception.getStackTrace(), acceptedPackagePrefixes);
        if (pickedCodePointer == null) {
            return result.toArray(new String[result.size()]);
        }

        ProblemDetails details = retrieveProblemDetails(exception, pickedCodePointer, codeSearchPaths, DEFAULT_SURROUND_LINE_COUNT);

        if (!pickedCodePointer.equals(causePoint)) {
            result.add("Showing according call in " + details.getSpotSourcePath() + " (around line " + pickedCodePointer.getLineNumber() + ")");
        }

        result.add(ASCRII_SEPARATORLINE);

        if (details != null && details.getCode() != null) {
            int currentLine = details.getStartLine();
            for (String codeLine : details.getCode()) {
                if (currentLine != pickedCodePointer.getLineNumber()) {
                    result.add(String.format(" % 5d: %s", currentLine, codeLine));
                } else {
                    result.add(String.format("*% 5d: %s", currentLine, codeLine));
                }
                currentLine++;
            }
            result.add(ASCRII_SEPARATORLINE);
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns a String containing a pretty-printing HTML based describtion of the passed exception.
     *
     * @param codeSearchPaths a list of directories to look for adressed source code files. May be empty or <code>null</code>
     * @param exception       The throwable describe
     * @return A String array guaranteed to be non-empty and non-null.
     */
    public static String describeThrowableHtml(File[] codeSearchPaths, Throwable exception, String... acceptedPackages) {
        if (exception == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(STYLE_DECLARATION);
        StackTraceElement causePoint = exception.getStackTrace()[0];
        StackTraceElement pickedCodePointer = pickCodePointerByPackage(exception.getStackTrace(), acceptedPackages);

        String message = "<strong>" + exception.getClass().getSimpleName() + "</strong> occured: " + exception.getMessage();
        String location = "In <strong>" + causePoint.getClassName() + "</strong> (around line " + causePoint.getLineNumber() + ")";

        ProblemDetails details = retrieveProblemDetails(exception, pickedCodePointer, codeSearchPaths, DEFAULT_SURROUND_LINE_COUNT);

        if (pickedCodePointer != null && !pickedCodePointer.equals(causePoint)) {
            location += ("<br/>Showing according call in <strong>" + details.getSpotSourcePath() + "</strong> " +
                    "(around line " + pickedCodePointer.getLineNumber() + ")");
        }

        result.append(PROLOG.replace("MESSAGE", message).replace("LOCATION", location));

        if (pickedCodePointer == null) {
            result.append(EPILOGUE.replace("DATE", new Date().toString()));
            return result.toString();
        }

        if (details != null && details.getCode() != null) {
            int currentLine = details.getStartLine();
            for (String codeLine : details.getCode()) {
                if (currentLine != pickedCodePointer.getLineNumber()) {
                    result.append(CODELINE.replace("LINENUM", String.valueOf(currentLine)).replace("LINECONTENT", codeLine));
                } else {
                    result.append(CODELINE_ACTIVE.replace("LINENUM", String.valueOf(currentLine)).replace("LINECONTENT", codeLine));
                }
                currentLine++;
            }
        }

        result.append(EPILOGUE.replace("DATE", new Date().toString()));
        return result.toString();
    }

    /**
     * Picks the first code pointer out of the passed stacktrace which matches the given list of valid package prefixes.
     *
     * @param acceptedPackagePrefixes A String array of valid package prefixes
     * @return The first code pointer which lies in one of the passed package prefixes or <code>null</code> otherwise.
     */
    public static StackTraceElement pickCodePointerByPackage(StackTraceElement[] stacktrace, String... acceptedPackagePrefixes) {
        if (stacktrace == null || acceptedPackagePrefixes == null) {
            return null;
        }
        for (StackTraceElement codePointer : stacktrace) {
            for (String acceptedPackagePrefix : acceptedPackagePrefixes) {
                if (codePointer.getClassName().startsWith(acceptedPackagePrefix)) {
                    return codePointer;
                }
            }
        }
        return null;
    }

    /**
     * Tries to retrieve the source code lines for the give code pointer by reading the according java files from the classpath.
     * If it fails to retrieve a source file at all, <code>null</code> will be returned. Otherwise a String array with the desired
     * code pointer is returned. The String array is guaranteed to be 1+2*surroundLineCount in size. It's also guaranteed that the
     * tageted code line is exactly the middle line of the code extract. If lines were not available (exceeding source file) they
     * are padded with <code>null</code> values.
     *
     * @param problem           The exception causing this problem.
     * @param pickedProblemLine The code pointer to illustrate by code extract
     * @param codeSearchPaths   a list of directories to look for adressed source code files. May be empty or <code>null</code>
     * @param surroundLineCount amount of lines to show around the code pointer    @return an array ouf source code lines or
     *                          <code>null</code> if unable to retrieve the lines.
     */
    public static ProblemDetails retrieveProblemDetails(Throwable problem, StackTraceElement pickedProblemLine,
                                                        File[] codeSearchPaths, int surroundLineCount) {
        if (pickedProblemLine == null) {
            if (problem != null) {
                pickedProblemLine = problem.getStackTrace()[0];
            } else {
                return null;
            }
        }
        ProblemDetails codePointerDetails = new ProblemDetails(null, pickedProblemLine);
        String sourceFilePath = codePointerDetails.getSpotSourcePath();
        String pkgName = codePointerDetails.getSpotPackageName();
        String sourceFileName = codePointerDetails.getSpotFileName();

        // First try file based approach
        String[] fileContent = loadCodeFromFile(codeSearchPaths, sourceFilePath);

        // No success? Try via classpath
        if (fileContent == null) {
            String sourceResource = pkgName.replace('.', '/') + '/' + sourceFileName;
            fileContent = loadCodeFromClasspath(sourceResource);
        }

        // Hmpfh... No luck today
        if (fileContent == null) {
            return null;
        }

        final int codeLine = pickedProblemLine.getLineNumber() - 1;
        final int startLine = codeLine - surroundLineCount;

        String[] content = new String[2 * surroundLineCount + 1];
        for (int i = startLine; i < startLine + 2 * surroundLineCount + 1; i++) {
            if (i >= 0 && i < fileContent.length) {
                content[i - startLine] = fileContent[i];
            } else {
                content[i - startLine] = "";
            }
        }

        codePointerDetails.startLine = startLine + 1;
        codePointerDetails.code = content;

        return codePointerDetails;
    }

    /**
     * Creates a (semi-) unique short, alphanumeric ID which can be used as token for easier identifaction of exactly one error.
     * Just create one, pass it to the user as reference and log the exception
     *
     * @return A semi-unique, alpahnumeric String.
     */
    public static String generateID() {
        long randomId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        return Long.toString(randomId, Character.MAX_RADIX);
    }

    /**
     * Generates a unique id for the passed exception and logs the passed exception in the logger {@link #LOG} (logger category
     * <code>EXCEPTION</code>) with <i>error</i> severity.
     *
     * @param exception The exception you want to report / log.
     * @return The ID used to identify this exception within the log. Show this to your users as reference.
     * @see #generateID()
     */
    public static String report(Throwable exception) {
        return report(Level.SEVERE, "Exception occurred", exception);
    }

    /**
     * Generates a unique id for the passed exception and logs the passed exception in the logger {@link #LOG} (logger category
     * <code>EXCEPTION</code>) with <i>error</i> severity.
     *
     * @param exception The exception you want to report / log.
     * @param message   An additional message to describe this problem report.
     * @return The ID used to identify this exception within the log. Show this to your users as reference.
     * @see #generateID()
     */
    public static String report(String message, Throwable exception) {
        return report(Level.SEVERE, message, exception);
    }

    /**
     * Generates a unique id for the passed exception and logs the passed exception in the logger {@link #LOG} (logger category
     * <code>EXCEPTION</code>) with <i>error</i> severity.
     *
     * @param exception The exception you want to report / log.
     * @param logLevel  The log level/severity to use.
     * @param message   An additional message to describe this problem report.
     * @return The ID used to identify this exception within the log. Show this to your users as reference.
     * @see #generateID()
     */
    public static String report(Level logLevel, String message, Throwable exception) {
        int level = logLevel.intValue();
        String id = generateID();
        if (message == null) {
            message = '[' + id + "] Exception occurred";
        } else {
            message = '[' + id + "] " + message;
        }

        if (level >= Level.SEVERE.intValue()) {
            LOG.error(message, exception);
        } else if (level >= Level.WARNING.intValue()) {
            LOG.warn(message, exception);
        } else if (level >= Level.CONFIG.intValue()) {
            LOG.info(message, exception);
        } else if (level >= Level.FINE.intValue()) {
            LOG.debug(message, exception);
        } else {
            LOG.trace(message, exception);
        }
        return id;
    }

    private static String[] loadCodeFromClasspath(String sourceResource) {
        try {
            InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourceResource);
            if (resourceStream != null) {
                InputStreamReader streamReader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
                try {
                    return loadFileLines(streamReader);
                } finally {
                    streamReader.close();
                }
            } else {
                return null; // NOSONAR
            }
        } catch (IOException ignore) {
            return null; // NOSONAR
        }

    }

    private static String[] loadCodeFromFile(File[] codeSearchPaths, String filename) { //NOPMD
        if (codeSearchPaths == null) {
            return null; // NOSONAR
        }
        for (File codeSearchPath : codeSearchPaths) {
            File potentialLocation = new File(codeSearchPath, filename);
            try {
                Reader reader = new InputStreamReader(new FileInputStream(potentialLocation), StandardCharsets.UTF_8);
                try {
                    String[] fileContent = loadFileLines(reader);
                    if (fileContent != null) {
                        return fileContent;
                    }
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ignore) {
                        // ignore
                    }
                }
            } catch (IOException ignore) {
                // Yep - do nothing
            }
        }
        return null;  // NOSONAR
    }

    private static String[] loadFileLines(Reader reader) {
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            try {
                List<String> codeLines = new ArrayList<String>();
                while (bufferedReader.ready()) {
                    codeLines.add(bufferedReader.readLine());
                }
                return codeLines.toArray(new String[codeLines.size()]);
            } finally {
                bufferedReader.close();
            }
        } catch (IOException e) {
            return null;  // NOSONAR
        }
    }

    /**
     * A value object containing various details about a thrown exception.
     */
    public static class ProblemDetails {

        private final Throwable problem;
        private final StackTraceElement spot;
        private final String spotFileName;
        private final String spotPackageName;
        private final String spotSourcePath;
        String[] code;
        int startLine;

        public ProblemDetails(Throwable problem, StackTraceElement pickedCodeSpot) {
            this.problem = problem;
            this.spot = pickedCodeSpot;
            String clsFullName = pickedCodeSpot.getClassName();
            String clsName = clsFullName.substring(clsFullName.lastIndexOf('.') + 1);

            this.spotPackageName = clsFullName.substring(0, clsFullName.lastIndexOf('.'));
            this.spotFileName = pickedCodeSpot.getFileName() != null ? pickedCodeSpot.getFileName() : clsName + ".java";

            String pkgPath = spotPackageName.replace('.', File.separatorChar);
            this.spotSourcePath = pkgPath + File.separatorChar + this.spotFileName;
        }

        /**
         * The original problem.
         *
         * @return Throable, may be <code>null</code>
         */
        public Throwable getProblem() {
            return problem;
        }

        /**
         * The file name of the picked code file demonstrated.
         *
         * @return may be <code>null</code>
         */
        public String getSpotFileName() {
            return spotFileName;
        }

        /**
         * The package name of the picked code file demonstrated.
         *
         * @return may be <code>null</code>
         */
        public String getSpotPackageName() {
            return spotPackageName;
        }

        /**
         * The file path of the picked code file demonstrated.
         *
         * @return may be <code>null</code>
         */

        public String getSpotSourcePath() {
            return spotSourcePath;
        }

        /**
         * The code spot we looked for.
         */

        public StackTraceElement getSpot() {
            return spot;
        }

        /**
         * The first line of the code.
         */
        public int getStartLine() {
            return startLine;
        }

        /**
         * The retrieved code extract.
         */
        public String[] getCode() {
            return code != null ? code.clone() : null;
        }
    }
}
