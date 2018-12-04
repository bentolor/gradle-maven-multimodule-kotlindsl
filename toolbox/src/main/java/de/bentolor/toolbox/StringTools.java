package de.bentolor.toolbox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collection of static utility methods for operations with <code>String</code> objects.
 *
 * @author Benjamin Schmid, @bentolor
 */
public final class StringTools {

    private static final String ABBREVIATION_INDICATOR = "…";

    private StringTools() {
    }

    /**
     * A method which will render the passed Object using the toString() method. this method ensures a Value never exceeds a given
     * maximum string length. If the string length exceeds the passed value, the string will be shortened to the max length by
     * trimming to maxlength minus 2 and appending '..'.
     *
     * @param object The object to describe
     */
    @Nonnull
    public static String abbreviate(int maxLength, @Nullable Object object) {
        String text = String.valueOf(object);
        if (text.length() <= maxLength) {
            return text;
        } else {
            // return abbreviated form.
            return text.substring(0, Math.max(0, maxLength - ABBREVIATION_INDICATOR.length())) + ABBREVIATION_INDICATOR;
        }
    }

}
