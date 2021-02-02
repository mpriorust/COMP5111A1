package comp5111.assignment.cut;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static comp5111.assignment.cut.ToolBox.StringTools.EMPTY;

public class ToolBox {
    /**
     * <p>Operations to assist when working with a {@link Locale}.</p>
     *
     * <p>This class tries to handle {@code null} input gracefully.
     * An exception will not be thrown for a {@code null} input.
     * Each method documents its behavior in more detail.</p>
     */
    public static class LocaleTools {
        /**
         * Checks whether the given String is a ISO 3166 alpha-2 country code.
         *
         * @param str the String to check
         * @return true, is the given String is a ISO 3166 compliant country code.
         */
        private static boolean isISO3166CountryCode(final String str) {
            return StringTools.isAllUpperCase(str) && str.length() == 2;
        }

        /**
         * Checks whether the given String is a ISO 639 compliant language code.
         *
         * @param str the String to check.
         * @return true, if the given String is a ISO 639 compliant language code.
         */
        private static boolean isISO639LanguageCode(final String str) {
            return StringTools.isAllLowerCase(str) && (str.length() == 2 || str.length() == 3);
        }

        /**
         * Checks whether the given String is a UN M.49 numeric area code.
         *
         * @param str the String to check
         * @return true, is the given String is a UN M.49 numeric area code.
         */
        private static boolean isNumericAreaCode(final String str) {
            return StringTools.isNumeric(str) && str.length() == 3;
        }

        /**
         * Tries to parse a locale from the given String.
         *
         * @param str the String to parse a locale from.
         * @return a Locale instance parsed from the given String.
         * @throws IllegalArgumentException if the given String can not be parsed.
         */
        private static Locale parseLocale(final String str) {
            if (isISO639LanguageCode(str)) {
                return new Locale(str);
            }

            final String[] segments = str.split("_", -1);
            final String language = segments[0];
            if (segments.length == 2) {
                final String country = segments[1];
                if (isISO639LanguageCode(language) && isISO3166CountryCode(country) ||
                    isNumericAreaCode(country)) {
                    return new Locale(language, country);
                }
            } else if (segments.length == 3) {
                final String country = segments[1];
                final String variant = segments[2];
                if (isISO639LanguageCode(language) &&
                    (country.isEmpty() || isISO3166CountryCode(country) || isNumericAreaCode(country)) &&
                    !variant.isEmpty()) {
                    return new Locale(language, country, variant);
                }
            }
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }

        /**
         * <p>Converts a String to a Locale.</p>
         *
         * <p>This method takes the string format of a locale and creates the
         * locale object from it.</p>
         *
         * <pre>
         *   LocaleUtils.toLocale("")           = new Locale("", "")
         *   LocaleUtils.toLocale("en")         = new Locale("en", "")
         *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
         *   LocaleUtils.toLocale("en_001")     = new Locale("en", "001")
         *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
         * </pre>
         *
         * <p>(#) The behavior of the JDK variant constructor changed between JDK1.3 and JDK1.4.
         * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
         * Thus, the result from getVariant() may vary depending on your JDK.</p>
         *
         * <p>This method validates the input strictly.
         * The language code must be lowercase.
         * The country code must be uppercase.
         * The separator must be an underscore.
         * The length must be correct.
         * </p>
         *
         * @param str the locale String to convert, null returns null
         * @return a Locale, null if null input
         * @throws IllegalArgumentException if the string is an invalid format
         * @see Locale#forLanguageTag(String)
         */
        public static Locale toLocale(final String str) {
            if (str == null) {
                return null;
            }
            if (str.isEmpty()) { // LANG-941 - JDK 8 introduced an empty locale where all fields are blank
                return new Locale(EMPTY, EMPTY);
            }
            if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            final int len = str.length();
            if (len < 2) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            final char ch0 = str.charAt(0);
            if (ch0 == '_') {
                if (len < 3) {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                if (len == 3) {
                    return new Locale(EMPTY, str.substring(1, 3));
                }
                if (len < 5) {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                if (str.charAt(3) != '_') {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                return new Locale(EMPTY, str.substring(1, 3), str.substring(4));
            }

            return parseLocale(str);
        }

    }

    public static class ArrayTools {
        /**
         * An empty immutable {@code char} array.
         */
        public static final char[] EMPTY_CHAR_ARRAY = new char[0];


        /**
         * The index value when an element is not found in a list or array: {@code -1}.
         * This value is returned by methods in this class and can also be used in comparisons with values returned by
         * various method from {@link List}.
         */
        public static final int INDEX_NOT_FOUND = -1;


        /**
         * <p>Checks if an array of primitive booleans is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final boolean[] array) {
            return getLength(array) == 0;
        }

        /**
         * <p>Checks if an array of primitive bytes is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final byte[] array) {
            return getLength(array) == 0;
        }

        // IndexOf search
        // ----------------------------------------------------------------------

        /**
         * <p>Checks if an array of primitive chars is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final char[] array) {
            return getLength(array) == 0;
        }

        /**
         * <p>Checks if an array of primitive doubles is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final double[] array) {
            return getLength(array) == 0;
        }

        /**
         * <p>Checks if an array of primitive floats is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final float[] array) {
            return getLength(array) == 0;
        }

        /**
         * <p>Checks if an array of primitive ints is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final int[] array) {
            return getLength(array) == 0;
        }


        /**
         * <p>Checks if an array of primitive longs is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final long[] array) {
            return getLength(array) == 0;
        }

        // ----------------------------------------------------------------------

        /**
         * <p>Checks if an array of Objects is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final Object[] array) {
            return getLength(array) == 0;
        }

        /**
         * <p>Checks if an array of primitive shorts is empty or {@code null}.
         *
         * @param array the array to test
         * @return {@code true} if the array is empty or {@code null}
         */
        public static boolean isEmpty(final short[] array) {
            return getLength(array) == 0;
        }

        //-----------------------------------------------------------------------

        /**
         * <p>Returns the length of the specified array.
         * This method can deal with {@code Object} arrays and with primitive arrays.
         *
         * <p>If the input array is {@code null}, {@code 0} is returned.
         *
         * <pre>
         * ArrayUtils.getLength(null)            = 0
         * ArrayUtils.getLength([])              = 0
         * ArrayUtils.getLength([null])          = 1
         * ArrayUtils.getLength([true, false])   = 2
         * ArrayUtils.getLength([1, 2, 3])       = 3
         * ArrayUtils.getLength(["a", "b", "c"]) = 3
         * </pre>
         *
         * @param array the array to retrieve the length from, may be null
         * @return The length of the array, or {@code 0} if the array is {@code null}
         * @throws IllegalArgumentException if the object argument is not an array.
         */
        public static int getLength(final Object array) {
            if (array == null) {
                return 0;
            }
            return Array.getLength(array);
        }
    }

    public static class CharSequenceTools {
        static final int TO_STRING_LIMIT = 16;
        //-----------------------------------------------------------------------
        private static final int NOT_FOUND = -1;

        /**
         * Returns the index within {@code cs} of the first occurrence of the
         * specified character, starting the search at the specified index.
         * <p>
         * If a character with value {@code searchChar} occurs in the
         * character sequence represented by the {@code cs}
         * object at an index no smaller than {@code start}, then
         * the index of the first such occurrence is returned. For values
         * of {@code searchChar} in the range from 0 to 0xFFFF (inclusive),
         * this is the smallest value <i>k</i> such that:
         * <blockquote><pre>
         * (this.charAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &gt;= start)
         * </pre></blockquote>
         * is true. For other values of {@code searchChar}, it is the
         * smallest value <i>k</i> such that:
         * <blockquote><pre>
         * (this.codePointAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &gt;= start)
         * </pre></blockquote>
         * is true. In either case, if no such character occurs inm {@code cs}
         * at or after position {@code start}, then
         * {@code -1} is returned.
         *
         * <p>
         * There is no restriction on the value of {@code start}. If it
         * is negative, it has the same effect as if it were zero: the entire
         * {@code CharSequence} may be searched. If it is greater than
         * the length of {@code cs}, it has the same effect as if it were
         * equal to the length of {@code cs}: {@code -1} is returned.
         *
         * <p>All indices are specified in {@code char} values
         * (Unicode code units).
         *
         * @param cs         the {@code CharSequence} to be processed, not null
         * @param searchChar the char to be searched for
         * @param start      the start index, negative starts at the string start
         * @return the index where the search char was found, -1 if not found
         */
        static int indexOf(final CharSequence cs, final int searchChar, int start) {
            if (cs instanceof String) {
                return ((String) cs).indexOf(searchChar, start);
            }
            final int sz = cs.length();
            if (start < 0) {
                start = 0;
            }
            if (searchChar < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                for (int i = start; i < sz; i++) {
                    if (cs.charAt(i) == searchChar) {
                        return i;
                    }
                }
                return NOT_FOUND;
            }
            //supplementary characters (LANG1300)
            if (searchChar <= Character.MAX_CODE_POINT) {
                final char[] chars = Character.toChars(searchChar);
                for (int i = start; i < sz - 1; i++) {
                    final char high = cs.charAt(i);
                    final char low = cs.charAt(i + 1);
                    if (high == chars[0] && low == chars[1]) {
                        return i;
                    }
                }
            }
            return NOT_FOUND;
        }

        /**
         * Used by the indexOf(CharSequence methods) as a green implementation of indexOf.
         *
         * @param cs         the {@code CharSequence} to be processed
         * @param searchChar the {@code CharSequence} to be searched for
         * @param start      the start index
         * @return the index where the search sequence was found
         */
        static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
            if (cs instanceof String) {
                return ((String) cs).indexOf(searchChar.toString(), start);
            } else if (cs instanceof StringBuilder) {
                return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
            } else if (cs instanceof StringBuffer) {
                return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
            }
            return cs.toString().indexOf(searchChar.toString(), start);
        }

        /**
         * Converts the given CharSequence to a char[].
         *
         * @param source the {@code CharSequence} to be processed.
         * @return the resulting char array, never null.
         */
        public static char[] toCharArray(final CharSequence source) {
            final int len = StringTools.length(source);
            if (len == 0) {
                return ArrayTools.EMPTY_CHAR_ARRAY;
            }
            if (source instanceof String) {
                return ((String) source).toCharArray();
            }
            final char[] array = new char[len];
            for (int i = 0; i < len; i++) {
                array[i] = source.charAt(i);
            }
            return array;
        }

        /**
         * Green implementation of regionMatches.
         *
         * @param cs         the {@code CharSequence} to be processed
         * @param ignoreCase whether or not to be case insensitive
         * @param thisStart  the index to start on the {@code cs} CharSequence
         * @param substring  the {@code CharSequence} to be looked for
         * @param start      the index to start on the {@code substring} CharSequence
         * @param length     character length of the region
         * @return whether the region matched
         */
        static boolean regionMatches(final CharSequence cs, final boolean ignoreCase, final int thisStart,
                                     final CharSequence substring, final int start, final int length) {
            if (cs instanceof String && substring instanceof String) {
                return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
            }
            int index1 = thisStart;
            int index2 = start;
            int tmpLen = length;

            // Extract these first so we detect NPEs the same as the java.lang.String version
            final int srcLen = cs.length() - thisStart;
            final int otherLen = substring.length() - start;

            // Check for invalid parameters
            if (thisStart < 0 || start < 0 || length < 0) {
                return false;
            }

            // Check that the regions are long enough
            if (srcLen < length || otherLen < length) {
                return false;
            }

            while (tmpLen-- > 0) {
                final char c1 = cs.charAt(index1++);
                final char c2 = substring.charAt(index2++);

                if (c1 == c2) {
                    continue;
                }

                if (!ignoreCase) {
                    return false;
                }

                // The real same check as in String.regionMatches():
                final char u1 = Character.toUpperCase(c1);
                final char u2 = Character.toUpperCase(c2);
                if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Returns the index within {@code cs} of the last occurrence of
         * the specified character, searching backward starting at the
         * specified index. For values of {@code searchChar} in the range
         * from 0 to 0xFFFF (inclusive), the index returned is the largest
         * value <i>k</i> such that:
         * <blockquote><pre>
         * (this.charAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &lt;= start)
         * </pre></blockquote>
         * is true. For other values of {@code searchChar}, it is the
         * largest value <i>k</i> such that:
         * <blockquote><pre>
         * (this.codePointAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &lt;= start)
         * </pre></blockquote>
         * is true. In either case, if no such character occurs in {@code cs}
         * at or before position {@code start}, then {@code -1} is returned.
         *
         * <p>All indices are specified in {@code char} values
         * (Unicode code units).
         *
         * @param cs         the {@code CharSequence} to be processed
         * @param searchChar the char to be searched for
         * @param start      the start index, negative returns -1, beyond length starts at end
         * @return the index where the search char was found, -1 if not found
         */
        static int lastIndexOf(final CharSequence cs, final int searchChar, int start) {
            if (cs instanceof String) {
                return ((String) cs).lastIndexOf(searchChar, start);
            }
            final int sz = cs.length();
            if (start < 0) {
                return NOT_FOUND;
            }
            if (start >= sz) {
                start = sz - 1;
            }
            if (searchChar < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                for (int i = start; i >= 0; --i) {
                    if (cs.charAt(i) == searchChar) {
                        return i;
                    }
                }
                return NOT_FOUND;
            }
            //supplementary characters (LANG1300)
            //NOTE - we must do a forward traversal for this to avoid duplicating code points
            if (searchChar <= Character.MAX_CODE_POINT) {
                final char[] chars = Character.toChars(searchChar);
                //make sure it's not the last index
                if (start == sz - 1) {
                    return NOT_FOUND;
                }
                for (int i = start; i >= 0; i--) {
                    final char high = cs.charAt(i);
                    final char low = cs.charAt(i + 1);
                    if (chars[0] == high && chars[1] == low) {
                        return i;
                    }
                }
            }
            return NOT_FOUND;
        }

        /**
         * Used by the lastIndexOf(CharSequence methods) as a green implementation of lastIndexOf
         *
         * @param cs         the {@code CharSequence} to be processed
         * @param searchChar the {@code CharSequence} to find
         * @param start      the start index
         * @return the index where the search sequence was found
         */
        static int lastIndexOf(final CharSequence cs, final CharSequence searchChar, int start) {
            if (searchChar == null || cs == null) {
                return NOT_FOUND;
            }
            if (searchChar instanceof String) {
                if (cs instanceof String) {
                    return ((String) cs).lastIndexOf((String) searchChar, start);
                } else if (cs instanceof StringBuilder) {
                    return ((StringBuilder) cs).lastIndexOf((String) searchChar, start);
                } else if (cs instanceof StringBuffer) {
                    return ((StringBuffer) cs).lastIndexOf((String) searchChar, start);
                }
            }

            final int len1 = cs.length();
            final int len2 = searchChar.length();

            if (start > len1) {
                start = len1;
            }

            if (start < 0 || len2 < 0 || len2 > len1) {
                return NOT_FOUND;
            }

            if (len2 == 0) {
                return start;
            }

            if (len2 <= TO_STRING_LIMIT) {
                if (cs instanceof String) {
                    return ((String) cs).lastIndexOf(searchChar.toString(), start);
                } else if (cs instanceof StringBuilder) {
                    return ((StringBuilder) cs).lastIndexOf(searchChar.toString(), start);
                } else if (cs instanceof StringBuffer) {
                    return ((StringBuffer) cs).lastIndexOf(searchChar.toString(), start);
                }
            }

            if (start + len2 > len1) {
                start = len1 - len2;
            }

            final char char0 = searchChar.charAt(0);

            int i = start;
            while (true) {
                while (cs.charAt(i) != char0) {
                    i--;
                    if (i < 0) {
                        return NOT_FOUND;
                    }
                }
                if (checkLaterThan1(cs, searchChar, len2, i)) {
                    return i;
                }
                i--;
                if (i < 0) {
                    return NOT_FOUND;
                }
            }
        }

        private static boolean checkLaterThan1(final CharSequence cs, final CharSequence searchChar, final int len2, final int start1) {
            for (int i = 1, j = len2 - 1; i <= j; i++, j--) {
                if (cs.charAt(start1 + i) != searchChar.charAt(i)
                    ||
                    cs.charAt(start1 + j) != searchChar.charAt(j)
                ) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class CharTools {
        /**
         * Linefeed character LF ({@code '\n'}, Unicode 000a).
         *
         * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
         * for Character and String Literals</a>
         */
        public static final char LF = '\n';
        /**
         * Carriage return characterf CR ('\r', Unicode 000d).
         *
         * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
         * for Character and String Literals</a>
         */
        public static final char CR = '\r';
        /**
         * {@code \u0000} null control character ('\0'), abbreviated NUL.
         */
        public static final char NUL = '\0';
        private static final String[] CHAR_STRING_ARRAY = new String[128];

        static {
            for (char c = 0; c < CHAR_STRING_ARRAY.length; c++) {
                CHAR_STRING_ARRAY[c] = String.valueOf(c);
            }
        }
    }

    /**
     * <p>Helpers to process Strings using regular expressions.</p>
     *
     * @see Pattern
     */
    public static class RegExTools {


        /**
         * <p>Replaces each substring of the text String that matches the given regular expression pattern with the given replacement.</p>
         * <p>
         * This method is a {@code null} safe equivalent to:
         * <ul>
         *  <li>{@code pattern.matcher(text).replaceAll(replacement)}</li>
         * </ul>
         *
         * <p>A {@code null} reference passed to this method is a no-op.</p>
         *
         * <pre>
         * StringTools.replaceAll(null, *, *)       = null
         * StringTools.replaceAll("any", (Pattern) null, *)   = "any"
         * StringTools.replaceAll("any", *, null)   = "any"
         * StringTools.replaceAll("", Pattern.compile(""), "zzz")    = "zzz"
         * StringTools.replaceAll("", Pattern.compile(".*"), "zzz")  = "zzz"
         * StringTools.replaceAll("", Pattern.compile(".+"), "zzz")  = ""
         * StringTools.replaceAll("abc", Pattern.compile(""), "ZZ")  = "ZZaZZbZZcZZ"
         * StringTools.replaceAll("&lt;__&gt;\n&lt;__&gt;", Pattern.compile("&lt;.*&gt;"), "z")                 = "z\nz"
         * StringTools.replaceAll("&lt;__&gt;\n&lt;__&gt;", Pattern.compile("&lt;.*&gt;", Pattern.DOTALL), "z") = "z"
         * StringTools.replaceAll("&lt;__&gt;\n&lt;__&gt;", Pattern.compile("(?s)&lt;.*&gt;"), "z")             = "z"
         * StringTools.replaceAll("ABCabc123", Pattern.compile("[a-z]"), "_")       = "ABC___123"
         * StringTools.replaceAll("ABCabc123", Pattern.compile("[^A-Z0-9]+"), "_")  = "ABC_123"
         * StringTools.replaceAll("ABCabc123", Pattern.compile("[^A-Z0-9]+"), "")   = "ABC123"
         * StringTools.replaceAll("Lorem ipsum  dolor   sit", Pattern.compile("( +)([a-z]+)"), "_$2")  = "Lorem_ipsum_dolor_sit"
         * </pre>
         *
         * @param text        text to search and replace in, may be null
         * @param regex       the regular expression pattern to which this string is to be matched
         * @param replacement the string to be substituted for each match
         * @return the text with any replacements processed,
         * {@code null} if null String input
         * @see java.util.regex.Matcher#replaceAll(String)
         * @see Pattern
         */
        public static String replaceAll(final String text, final Pattern regex, final String replacement) {
            if (text == null || regex == null || replacement == null) {
                return text;
            }
            return regex.matcher(text).replaceAll(replacement);
        }

        /**
         * <p>Replaces each substring of the text String that matches the given regular expression
         * with the given replacement.</p>
         * <p>
         * This method is a {@code null} safe equivalent to:
         * <ul>
         *  <li>{@code text.replaceAll(regex, replacement)}</li>
         *  <li>{@code Pattern.compile(regex).matcher(text).replaceAll(replacement)}</li>
         * </ul>
         *
         * <p>A {@code null} reference passed to this method is a no-op.</p>
         *
         * <p>Unlike in the {@link #replacePattern(String, String, String)} method, the {@link Pattern#DOTALL} option
         * is NOT automatically added.
         * To use the DOTALL option prepend {@code "(?s)"} to the regex.
         * DOTALL is also known as single-line mode in Perl.</p>
         *
         * <pre>
         * StringTools.replaceAll(null, *, *)       = null
         * StringTools.replaceAll("any", (String) null, *)   = "any"
         * StringTools.replaceAll("any", *, null)   = "any"
         * StringTools.replaceAll("", "", "zzz")    = "zzz"
         * StringTools.replaceAll("", ".*", "zzz")  = "zzz"
         * StringTools.replaceAll("", ".+", "zzz")  = ""
         * StringTools.replaceAll("abc", "", "ZZ")  = "ZZaZZbZZcZZ"
         * StringTools.replaceAll("&lt;__&gt;\n&lt;__&gt;", "&lt;.*&gt;", "z")      = "z\nz"
         * StringTools.replaceAll("&lt;__&gt;\n&lt;__&gt;", "(?s)&lt;.*&gt;", "z")  = "z"
         * StringTools.replaceAll("ABCabc123", "[a-z]", "_")       = "ABC___123"
         * StringTools.replaceAll("ABCabc123", "[^A-Z0-9]+", "_")  = "ABC_123"
         * StringTools.replaceAll("ABCabc123", "[^A-Z0-9]+", "")   = "ABC123"
         * StringTools.replaceAll("Lorem ipsum  dolor   sit", "( +)([a-z]+)", "_$2")  = "Lorem_ipsum_dolor_sit"
         * </pre>
         *
         * @param text        text to search and replace in, may be null
         * @param regex       the regular expression to which this string is to be matched
         * @param replacement the string to be substituted for each match
         * @return the text with any replacements processed,
         * {@code null} if null String input
         * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
         * @see #replacePattern(String, String, String)
         * @see String#replaceAll(String, String)
         * @see Pattern
         * @see Pattern#DOTALL
         */
        public static String replaceAll(final String text, final String regex, final String replacement) {
            if (text == null || regex == null || replacement == null) {
                return text;
            }
            return text.replaceAll(regex, replacement);
        }

        /**
         * <p>Replaces the first substring of the text string that matches the given regular expression pattern
         * with the given replacement.</p>
         * <p>
         * This method is a {@code null} safe equivalent to:
         * <ul>
         *  <li>{@code pattern.matcher(text).replaceFirst(replacement)}</li>
         * </ul>
         *
         * <p>A {@code null} reference passed to this method is a no-op.</p>
         *
         * <pre>
         * StringTools.replaceFirst(null, *, *)       = null
         * StringTools.replaceFirst("any", (Pattern) null, *)   = "any"
         * StringTools.replaceFirst("any", *, null)   = "any"
         * StringTools.replaceFirst("", Pattern.compile(""), "zzz")    = "zzz"
         * StringTools.replaceFirst("", Pattern.compile(".*"), "zzz")  = "zzz"
         * StringTools.replaceFirst("", Pattern.compile(".+"), "zzz")  = ""
         * StringTools.replaceFirst("abc", Pattern.compile(""), "ZZ")  = "ZZabc"
         * StringTools.replaceFirst("&lt;__&gt;\n&lt;__&gt;", Pattern.compile("&lt;.*&gt;"), "z")      = "z\n&lt;__&gt;"
         * StringTools.replaceFirst("&lt;__&gt;\n&lt;__&gt;", Pattern.compile("(?s)&lt;.*&gt;"), "z")  = "z"
         * StringTools.replaceFirst("ABCabc123", Pattern.compile("[a-z]"), "_")          = "ABC_bc123"
         * StringTools.replaceFirst("ABCabc123abc", Pattern.compile("[^A-Z0-9]+"), "_")  = "ABC_123abc"
         * StringTools.replaceFirst("ABCabc123abc", Pattern.compile("[^A-Z0-9]+"), "")   = "ABC123abc"
         * StringTools.replaceFirst("Lorem ipsum  dolor   sit", Pattern.compile("( +)([a-z]+)"), "_$2")  = "Lorem_ipsum  dolor   sit"
         * </pre>
         *
         * @param text        text to search and replace in, may be null
         * @param regex       the regular expression pattern to which this string is to be matched
         * @param replacement the string to be substituted for the first match
         * @return the text with the first replacement processed,
         * {@code null} if null String input
         * @see java.util.regex.Matcher#replaceFirst(String)
         * @see Pattern
         */
        public static String replaceFirst(final String text, final Pattern regex, final String replacement) {
            if (text == null || regex == null || replacement == null) {
                return text;
            }
            return regex.matcher(text).replaceFirst(replacement);
        }

        /**
         * <p>Replaces the first substring of the text string that matches the given regular expression
         * with the given replacement.</p>
         * <p>
         * This method is a {@code null} safe equivalent to:
         * <ul>
         *  <li>{@code text.replaceFirst(regex, replacement)}</li>
         *  <li>{@code Pattern.compile(regex).matcher(text).replaceFirst(replacement)}</li>
         * </ul>
         *
         * <p>A {@code null} reference passed to this method is a no-op.</p>
         *
         * <p>The {@link Pattern#DOTALL} option is NOT automatically added.
         * To use the DOTALL option prepend {@code "(?s)"} to the regex.
         * DOTALL is also known as single-line mode in Perl.</p>
         *
         * <pre>
         * StringTools.replaceFirst(null, *, *)       = null
         * StringTools.replaceFirst("any", (String) null, *)   = "any"
         * StringTools.replaceFirst("any", *, null)   = "any"
         * StringTools.replaceFirst("", "", "zzz")    = "zzz"
         * StringTools.replaceFirst("", ".*", "zzz")  = "zzz"
         * StringTools.replaceFirst("", ".+", "zzz")  = ""
         * StringTools.replaceFirst("abc", "", "ZZ")  = "ZZabc"
         * StringTools.replaceFirst("&lt;__&gt;\n&lt;__&gt;", "&lt;.*&gt;", "z")      = "z\n&lt;__&gt;"
         * StringTools.replaceFirst("&lt;__&gt;\n&lt;__&gt;", "(?s)&lt;.*&gt;", "z")  = "z"
         * StringTools.replaceFirst("ABCabc123", "[a-z]", "_")          = "ABC_bc123"
         * StringTools.replaceFirst("ABCabc123abc", "[^A-Z0-9]+", "_")  = "ABC_123abc"
         * StringTools.replaceFirst("ABCabc123abc", "[^A-Z0-9]+", "")   = "ABC123abc"
         * StringTools.replaceFirst("Lorem ipsum  dolor   sit", "( +)([a-z]+)", "_$2")  = "Lorem_ipsum  dolor   sit"
         * </pre>
         *
         * @param text        text to search and replace in, may be null
         * @param regex       the regular expression to which this string is to be matched
         * @param replacement the string to be substituted for the first match
         * @return the text with the first replacement processed,
         * {@code null} if null String input
         * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
         * @see String#replaceFirst(String, String)
         * @see Pattern
         * @see Pattern#DOTALL
         */
        public static String replaceFirst(final String text, final String regex, final String replacement) {
            if (text == null || regex == null || replacement == null) {
                return text;
            }
            return text.replaceFirst(regex, replacement);
        }

        /**
         * <p>Replaces each substring of the source String that matches the given regular expression with the given
         * replacement using the {@link Pattern#DOTALL} option. DOTALL is also known as single-line mode in Perl.</p>
         * <p>
         * This call is a {@code null} safe equivalent to:
         * <ul>
         * <li>{@code text.replaceAll(&quot;(?s)&quot; + regex, replacement)}</li>
         * <li>{@code Pattern.compile(regex, Pattern.DOTALL).matcher(text).replaceAll(replacement)}</li>
         * </ul>
         *
         * <p>A {@code null} reference passed to this method is a no-op.</p>
         *
         * <pre>
         * StringTools.replacePattern(null, *, *)       = null
         * StringTools.replacePattern("any", (String) null, *)   = "any"
         * StringTools.replacePattern("any", *, null)   = "any"
         * StringTools.replacePattern("", "", "zzz")    = "zzz"
         * StringTools.replacePattern("", ".*", "zzz")  = "zzz"
         * StringTools.replacePattern("", ".+", "zzz")  = ""
         * StringTools.replacePattern("&lt;__&gt;\n&lt;__&gt;", "&lt;.*&gt;", "z")       = "z"
         * StringTools.replacePattern("ABCabc123", "[a-z]", "_")       = "ABC___123"
         * StringTools.replacePattern("ABCabc123", "[^A-Z0-9]+", "_")  = "ABC_123"
         * StringTools.replacePattern("ABCabc123", "[^A-Z0-9]+", "")   = "ABC123"
         * StringTools.replacePattern("Lorem ipsum  dolor   sit", "( +)([a-z]+)", "_$2")  = "Lorem_ipsum_dolor_sit"
         * </pre>
         *
         * @param text        the source string
         * @param regex       the regular expression to which this string is to be matched
         * @param replacement the string to be substituted for each match
         * @return The resulting {@code String}
         * @see #replaceAll(String, String, String)
         * @see String#replaceAll(String, String)
         * @see Pattern#DOTALL
         */
        public static String replacePattern(final String text, final String regex, final String replacement) {
            if (text == null || regex == null || replacement == null) {
                return text;
            }
            return Pattern.compile(regex, Pattern.DOTALL).matcher(text).replaceAll(replacement);
        }

    }

    /**
     * <p>Operations on {@link String} that are
     * {@code null} safe.</p>
     *
     * <ul>
     *  <li><b>IsEmpty/IsBlank</b>
     *      - checks if a String contains text</li>
     *  <li><b>Trim/Strip</b>
     *      - removes leading and trailing whitespace</li>
     *  <li><b>Equals/Compare</b>
     *      - compares two strings in a null-safe manner</li>
     *  <li><b>startsWith</b>
     *      - check if a String starts with a prefix in a null-safe manner</li>
     *  <li><b>endsWith</b>
     *      - check if a String ends with a suffix in a null-safe manner</li>
     *  <li><b>IndexOf/LastIndexOf/Contains</b>
     *      - null-safe index-of checks
     *  <li><b>IndexOfAny/LastIndexOfAny/IndexOfAnyBut/LastIndexOfAnyBut</b>
     *      - index-of any of a set of Strings</li>
     *  <li><b>ContainsOnly/ContainsNone/ContainsAny</b>
     *      - checks if String contains only/none/any of these characters</li>
     *  <li><b>Substring/Left/Right/Mid</b>
     *      - null-safe substring extractions</li>
     *  <li><b>SubstringBefore/SubstringAfter/SubstringBetween</b>
     *      - substring extraction relative to other strings</li>
     *  <li><b>Split/Join</b>
     *      - splits a String into an array of substrings and vice versa</li>
     *  <li><b>Remove/Delete</b>
     *      - removes part of a String</li>
     *  <li><b>Replace/Overlay</b>
     *      - Searches a String and replaces one String with another</li>
     *  <li><b>Chomp/Chop</b>
     *      - removes the last part of a String</li>
     *  <li><b>AppendIfMissing</b>
     *      - appends a suffix to the end of the String if not present</li>
     *  <li><b>PrependIfMissing</b>
     *      - prepends a prefix to the start of the String if not present</li>
     *  <li><b>LeftPad/RightPad/Center/Repeat</b>
     *      - pads a String</li>
     *  <li><b>UpperCase/LowerCase/SwapCase/Capitalize/Uncapitalize</b>
     *      - changes the case of a String</li>
     *  <li><b>CountMatches</b>
     *      - counts the number of occurrences of one String in another</li>
     *  <li><b>IsAlpha/IsNumeric/IsWhitespace/IsAsciiPrintable</b>
     *      - checks the characters in a String</li>
     *  <li><b>DefaultString</b>
     *      - protects against a null input String</li>
     *  <li><b>Rotate</b>
     *      - rotate (circular shift) a String</li>
     *  <li><b>Reverse/ReverseDelimited</b>
     *      - reverses a String</li>
     *  <li><b>Abbreviate</b>
     *      - abbreviates a string using ellipses or another given String</li>
     *  <li><b>Difference</b>
     *      - compares Strings and reports on their differences</li>
     *  <li><b>LevenshteinDistance</b>
     *      - the number of changes needed to change one String into another</li>
     * </ul>
     *
     * <p>The {@code StringTools} class defines certain words related to
     * String handling.</p>
     *
     * <ul>
     *  <li>null - {@code null}</li>
     *  <li>empty - a zero-length string ({@code ""})</li>
     *  <li>space - the space character ({@code ' '}, char 32)</li>
     *  <li>whitespace - the characters defined by {@link Character#isWhitespace(char)}</li>
     *  <li>trim - the characters &lt;= 32 as in {@link String#trim()}</li>
     * </ul>
     *
     * <p>{@code StringTools} handles {@code null} input Strings quietly.
     * That is to say that a {@code null} input will return {@code null}.
     * Where a {@code boolean} or {@code int} is being returned
     * details vary by method.</p>
     *
     * <p>A side effect of the {@code null} handling is that a
     * {@code NullPointerException} should be considered a bug in
     * {@code StringTools}.</p>
     *
     * <p>Methods in this class include sample code in their Javadoc comments to explain their operation.
     * The symbol {@code *} is used to indicate any input including {@code null}.</p>
     *
     * <p>#ThreadSafe#</p>
     */
    //@Immutable
    public static class StringTools {

        /**
         * A String for a space character.
         */
        public static final String SPACE = " ";
        /**
         * The empty String {@code ""}.
         */
        public static final String EMPTY = "";

        // Performance testing notes (JDK 1.4, Jul03, scolebourne)
        // Whitespace:
        // Character.isWhitespace() is faster than WHITESPACE.indexOf()
        // where WHITESPACE is a string of all whitespace characters
        //
        // Character access:
        // String.charAt(n) versus toCharArray(), then array[n]
        // String.charAt(n) is about 15% worse for a 10K string
        // They are about equal for a length 50 string
        // String.charAt(n) is about 4 times better for a length 3 string
        // String.charAt(n) is best bet overall
        //
        // Append:
        // String.concat about twice as fast as StringBuffer.append
        // (not sure who tested this)
        /**
         * A String for linefeed LF ("\n").
         * <p>
         * <p>
         * for Character and String Literals</a>
         */
        public static final String LF = "\n";
        /**
         * A String for carriage return CR ("\r").
         * <p>
         * <p>
         * for Character and String Literals</a>
         */
        public static final String CR = "\r";
        /**
         * Represents a failed index search.
         */
        public static final int INDEX_NOT_FOUND = -1;

        /**
         * Gets a CharSequence length or {@code 0} if the CharSequence is
         * {@code null}.
         *
         * @param cs a CharSequence or {@code null}
         * @return CharSequence length or {@code 0} if the CharSequence is
         * {@code null}.
         * @since 2.4
         * @since 3.0 Changed signature from length(String) to length(CharSequence)
         */
        public static int length(final CharSequence cs) {
            return cs == null ? 0 : cs.length();
        }

        /**
         * <p>Compare two Strings lexicographically, as per {@link String#compareTo(String)}, returning :</p>
         * <ul>
         *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
         *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
         *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
         * </ul>
         *
         * <p>This is a {@code null} safe version of :</p>
         * <blockquote><pre>str1.compareTo(str2)</pre></blockquote>
         *
         * <p>{@code null} inputs are handled according to the {@code nullIsLess} parameter.
         * Two {@code null} references are considered equal.</p>
         *
         * <pre>
         * StringTools.compare(null, null, *)     = 0
         * StringTools.compare(null , "a", true)  &lt; 0
         * StringTools.compare(null , "a", false) &gt; 0
         * StringTools.compare("a", null, true)   &gt; 0
         * StringTools.compare("a", null, false)  &lt; 0
         * StringTools.compare("abc", "abc", *)   = 0
         * StringTools.compare("a", "b", *)       &lt; 0
         * StringTools.compare("b", "a", *)       &gt; 0
         * StringTools.compare("a", "B", *)       &gt; 0
         * StringTools.compare("ab", "abc", *)    &lt; 0
         * </pre>
         *
         * @param str1       the String to compare from
         * @param str2       the String to compare to
         * @param nullIsLess whether consider {@code null} value less than non-{@code null} value
         * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2}
         */
        public static int compare(final String str1, final String str2, final boolean nullIsLess) {
            if (str1 == str2) { // NOSONARLINT this intentionally uses == to allow for both null
                return 0;
            }
            if (str1 == null) {
                return nullIsLess ? -1 : 1;
            }
            if (str2 == null) {
                return nullIsLess ? 1 : -1;
            }
            return str1.compareTo(str2);
        }

        /**
         * <p>Compare two Strings lexicographically, ignoring case differences,
         * as per {@link String#compareToIgnoreCase(String)}, returning :</p>
         * <ul>
         *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
         *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
         *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
         * </ul>
         *
         * <p>This is a {@code null} safe version of :</p>
         * <blockquote><pre>str1.compareToIgnoreCase(str2)</pre></blockquote>
         *
         * <p>{@code null} inputs are handled according to the {@code nullIsLess} parameter.
         * Two {@code null} references are considered equal.
         * Comparison is case insensitive.</p>
         *
         * <pre>
         * StringTools.compareIgnoreCase(null, null, *)     = 0
         * StringTools.compareIgnoreCase(null , "a", true)  &lt; 0
         * StringTools.compareIgnoreCase(null , "a", false) &gt; 0
         * StringTools.compareIgnoreCase("a", null, true)   &gt; 0
         * StringTools.compareIgnoreCase("a", null, false)  &lt; 0
         * StringTools.compareIgnoreCase("abc", "abc", *)   = 0
         * StringTools.compareIgnoreCase("abc", "ABC", *)   = 0
         * StringTools.compareIgnoreCase("a", "b", *)       &lt; 0
         * StringTools.compareIgnoreCase("b", "a", *)       &gt; 0
         * StringTools.compareIgnoreCase("a", "B", *)       &lt; 0
         * StringTools.compareIgnoreCase("A", "b", *)       &lt; 0
         * StringTools.compareIgnoreCase("ab", "abc", *)    &lt; 0
         * </pre>
         *
         * @param str1       the String to compare from
         * @param str2       the String to compare to
         * @param nullIsLess whether consider {@code null} value less than non-{@code null} value
         * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2},
         * ignoring case differences.
         */
        public static int compareIgnoreCase(final String str1, final String str2, final boolean nullIsLess) {
            if (str1 == str2) { // NOSONARLINT this intentionally uses == to allow for both null
                return 0;
            }
            if (str1 == null) {
                return nullIsLess ? -1 : 1;
            }
            if (str2 == null) {
                return nullIsLess ? 1 : -1;
            }
            return str1.compareToIgnoreCase(str2);
        }

        /**
         * <p>Checks if CharSequence contains a search character, handling {@code null}.
         * This method uses {@link String#indexOf(int)} if possible.</p>
         *
         * <p>A {@code null} or empty ("") CharSequence will return {@code false}.</p>
         *
         * <pre>
         * StringTools.contains(null, *)    = false
         * StringTools.contains("", *)      = false
         * StringTools.contains("abc", 'a') = true
         * StringTools.contains("abc", 'z') = false
         * </pre>
         *
         * @param seq        the CharSequence to check, may be null
         * @param searchChar the character to find
         * @return true if the CharSequence contains the search character,
         * false if not or {@code null} string input
         */
        public static boolean contains(final CharSequence seq, final int searchChar) {
            if (isEmpty(seq)) {
                return false;
            }
            return CharSequenceTools.indexOf(seq, searchChar, 0) >= 0;
        }

        /**
         * <p>Checks if the CharSequence contains any character in the given
         * set of characters.</p>
         *
         * <p>A {@code null} CharSequence will return {@code false}.
         * A {@code null} or zero length search array will return {@code false}.</p>
         *
         * <pre>
         * StringTools.containsAny(null, *)                  = false
         * StringTools.containsAny("", *)                    = false
         * StringTools.containsAny(*, null)                  = false
         * StringTools.containsAny(*, [])                    = false
         * StringTools.containsAny("zzabyycdxx", ['z', 'a']) = true
         * StringTools.containsAny("zzabyycdxx", ['b', 'y']) = true
         * StringTools.containsAny("zzabyycdxx", ['z', 'y']) = true
         * StringTools.containsAny("aba", ['z'])             = false
         * </pre>
         *
         * @param cs          the CharSequence to check, may be null
         * @param searchChars the chars to search for, may be null
         * @return the {@code true} if any of the chars are found,
         * {@code false} if no match or null input
         */
        public static boolean containsAny(final CharSequence cs, final char... searchChars) {
            if (isEmpty(cs) || ArrayTools.isEmpty(searchChars)) {
                return false;
            }
            final int csLength = cs.length();
            final int searchLength = searchChars.length;
            final int csLast = csLength - 1;
            final int searchLast = searchLength - 1;
            for (int i = 0; i < csLength; i++) {
                final char ch = cs.charAt(i);
                for (int j = 0; j < searchLength; j++) {
                    if (searchChars[j] == ch) {
                        if (Character.isHighSurrogate(ch)) {
                            if (j == searchLast) {
                                // missing low surrogate, fine, like String.indexOf(String)
                                return true;
                            }
                            if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                                return true;
                            }
                        } else {
                            // ch is in the Basic Multilingual Plane
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * <p>
         * Checks if the CharSequence contains any character in the given set of characters.
         * </p>
         *
         * <p>
         * A {@code null} CharSequence will return {@code false}. A {@code null} search CharSequence will return
         * {@code false}.
         * </p>
         *
         * <pre>
         * StringTools.containsAny(null, *)               = false
         * StringTools.containsAny("", *)                 = false
         * StringTools.containsAny(*, null)               = false
         * StringTools.containsAny(*, "")                 = false
         * StringTools.containsAny("zzabyycdxx", "za")    = true
         * StringTools.containsAny("zzabyycdxx", "by")    = true
         * StringTools.containsAny("zzabyycdxx", "zy")    = true
         * StringTools.containsAny("zzabyycdxx", "\tx")   = true
         * StringTools.containsAny("zzabyycdxx", "$.#yF") = true
         * StringTools.containsAny("aba", "z")            = false
         * </pre>
         *
         * @param cs          the CharSequence to check, may be null
         * @param searchChars the chars to search for, may be null
         * @return the {@code true} if any of the chars are found, {@code false} if no match or null input
         */
        public static boolean containsAny(final CharSequence cs, final CharSequence searchChars) {
            if (searchChars == null) {
                return false;
            }
            return containsAny(cs, CharSequenceTools.toCharArray(searchChars));
        }

        /**
         * <p>Checks that the CharSequence does not contain certain characters.</p>
         *
         * <p>A {@code null} CharSequence will return {@code true}.
         * A {@code null} invalid character array will return {@code true}.
         * An empty CharSequence (length()=0) always returns true.</p>
         *
         * <pre>
         * StringTools.containsNone(null, *)       = true
         * StringTools.containsNone(*, null)       = true
         * StringTools.containsNone("", *)         = true
         * StringTools.containsNone("ab", '')      = true
         * StringTools.containsNone("abab", 'xyz') = true
         * StringTools.containsNone("ab1", 'xyz')  = true
         * StringTools.containsNone("abz", 'xyz')  = false
         * </pre>
         *
         * @param cs          the CharSequence to check, may be null
         * @param searchChars an array of invalid chars, may be null
         * @return true if it contains none of the invalid chars, or is null
         */
        public static boolean containsNone(final CharSequence cs, final char... searchChars) {
            if (cs == null || searchChars == null) {
                return true;
            }
            final int csLen = cs.length();
            final int csLast = csLen - 1;
            final int searchLen = searchChars.length;
            final int searchLast = searchLen - 1;
            for (int i = 0; i < csLen; i++) {
                final char ch = cs.charAt(i);
                for (int j = 0; j < searchLen; j++) {
                    if (searchChars[j] == ch) {
                        if (Character.isHighSurrogate(ch)) {
                            if (j == searchLast) {
                                // missing low surrogate, fine, like String.indexOf(String)
                                return false;
                            }
                            if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                                return false;
                            }
                        } else {
                            // ch is in the Basic Multilingual Plane
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        /**
         * <p>Compares two CharSequences, returning {@code true} if they represent
         * equal sequences of characters.</p>
         *
         * <p>{@code null}s are handled without exceptions. Two {@code null}
         * references are considered to be equal. The comparison is <strong>case sensitive</strong>.</p>
         *
         * <pre>
         * StringTools.equals(null, null)   = true
         * StringTools.equals(null, "abc")  = false
         * StringTools.equals("abc", null)  = false
         * StringTools.equals("abc", "abc") = true
         * StringTools.equals("abc", "ABC") = false
         * </pre>
         *
         * @param cs1 the first CharSequence, may be {@code null}
         * @param cs2 the second CharSequence, may be {@code null}
         * @return {@code true} if the CharSequences are equal (case-sensitive), or both {@code null}
         */
        public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
            if (cs1 == cs2) {
                return true;
            }
            if (cs1 == null || cs2 == null) {
                return false;
            }
            if (cs1.length() != cs2.length()) {
                return false;
            }
            if (cs1 instanceof String && cs2 instanceof String) {
                return cs1.equals(cs2);
            }
            // Step-wise comparison
            final int length = cs1.length();
            for (int i = 0; i < length; i++) {
                if (cs1.charAt(i) != cs2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * <p>Compares all Strings in an array and returns the initial sequence of
         * characters that is common to all of them.</p>
         *
         * <p>For example,
         * {@code getCommonPrefix(new String[] {"i am a machine", "i am a robot"}) -&gt; "i am a "}</p>
         *
         * <pre>
         * StringTools.getCommonPrefix(null) = ""
         * StringTools.getCommonPrefix(new String[] {}) = ""
         * StringTools.getCommonPrefix(new String[] {"abc"}) = "abc"
         * StringTools.getCommonPrefix(new String[] {null, null}) = ""
         * StringTools.getCommonPrefix(new String[] {"", ""}) = ""
         * StringTools.getCommonPrefix(new String[] {"", null}) = ""
         * StringTools.getCommonPrefix(new String[] {"abc", null, null}) = ""
         * StringTools.getCommonPrefix(new String[] {null, null, "abc"}) = ""
         * StringTools.getCommonPrefix(new String[] {"", "abc"}) = ""
         * StringTools.getCommonPrefix(new String[] {"abc", ""}) = ""
         * StringTools.getCommonPrefix(new String[] {"abc", "abc"}) = "abc"
         * StringTools.getCommonPrefix(new String[] {"abc", "a"}) = "a"
         * StringTools.getCommonPrefix(new String[] {"ab", "abxyz"}) = "ab"
         * StringTools.getCommonPrefix(new String[] {"abcde", "abxyz"}) = "ab"
         * StringTools.getCommonPrefix(new String[] {"abcde", "xyz"}) = ""
         * StringTools.getCommonPrefix(new String[] {"xyz", "abcde"}) = ""
         * StringTools.getCommonPrefix(new String[] {"i am a machine", "i am a robot"}) = "i am a "
         * </pre>
         *
         * @param strs array of String objects, entries may be null
         * @return the initial sequence of characters that are common to all Strings
         * in the array; empty String if the array is null, the elements are all null
         * or if there is no common prefix.
         */
        public static String getCommonPrefix(final String... strs) {
            if (ArrayTools.isEmpty(strs)) {
                return EMPTY;
            }
            final int smallestIndexOfDiff = indexOfDifference(strs);
            if (smallestIndexOfDiff == INDEX_NOT_FOUND) {
                // all strings were identical
                if (strs[0] == null) {
                    return EMPTY;
                }
                return strs[0];
            } else if (smallestIndexOfDiff == 0) {
                // there were no common initial characters
                return EMPTY;
            } else {
                // we found a common initial character sequence
                return strs[0].substring(0, smallestIndexOfDiff);
            }
        }

        /**
         * <p>Returns either the passed in CharSequence, or if the CharSequence is
         * empty or {@code null}, the value supplied by {@code defaultStrSupplier}.</p>
         *
         * <p>Caller responsible for thread-safety and exception handling of default value supplier</p>
         *
         * <pre>
         * {@code
         * StringTools.getIfEmpty(null, () -> "NULL")    = "NULL"
         * StringTools.getIfEmpty("", () -> "NULL")      = "NULL"
         * StringTools.getIfEmpty(" ", () -> "NULL")     = " "
         * StringTools.getIfEmpty("bat", () -> "NULL")   = "bat"
         * StringTools.getIfEmpty("", () -> null)        = null
         * StringTools.getIfEmpty("", null)              = null
         * }
         * </pre>
         *
         * @param <T>             the specific kind of CharSequence
         * @param str             the CharSequence to check, may be null
         * @param defaultSupplier the supplier of default CharSequence to return
         *                        if the input is empty ("") or {@code null}, may be null
         * @return the passed in CharSequence, or the default
         */
        public static <T extends CharSequence> T getIfEmpty(final T str, final Supplier<T> defaultSupplier) {
            return isEmpty(str) ? defaultSupplier == null ? null : defaultSupplier.get() : str;
        }

        /**
         * <p>Search a CharSequence to find the first index of any
         * character in the given set of characters.</p>
         *
         * <p>A {@code null} String will return {@code -1}.
         * A {@code null} or zero length search array will return {@code -1}.</p>
         *
         * <pre>
         * StringTools.indexOfAny(null, *)                  = -1
         * StringTools.indexOfAny("", *)                    = -1
         * StringTools.indexOfAny(*, null)                  = -1
         * StringTools.indexOfAny(*, [])                    = -1
         * StringTools.indexOfAny("zzabyycdxx", ['z', 'a']) = 0
         * StringTools.indexOfAny("zzabyycdxx", ['b', 'y']) = 3
         * StringTools.indexOfAny("aba", ['z'])             = -1
         * </pre>
         *
         * @param cs          the CharSequence to check, may be null
         * @param searchChars the chars to search for, may be null
         * @return the index of any of the chars, -1 if no match or null input
         */
        public static int indexOfAny(final CharSequence cs, final char... searchChars) {
            if (isEmpty(cs) || ArrayTools.isEmpty(searchChars)) {
                return INDEX_NOT_FOUND;
            }
            final int csLen = cs.length();
            final int csLast = csLen - 1;
            final int searchLen = searchChars.length;
            final int searchLast = searchLen - 1;
            for (int i = 0; i < csLen; i++) {
                final char ch = cs.charAt(i);
                for (int j = 0; j < searchLen; j++) {
                    if (searchChars[j] == ch) {
                        if (i < csLast && j < searchLast || Character.isHighSurrogate(ch)) {
                            // ch is a supplementary character
                            if (searchChars[j + 1] == cs.charAt(i + 1)) {
                                return i;
                            }
                        } else {
                            return i;
                        }
                    }
                }
            }
            return INDEX_NOT_FOUND;
        }

        /**
         * <p>Searches a CharSequence to find the first index of any
         * character not in the given set of characters.</p>
         *
         * <p>A {@code null} CharSequence will return {@code -1}.
         * A {@code null} or zero length search array will return {@code -1}.</p>
         *
         * <pre>
         * StringTools.indexOfAnyBut(null, *)                              = -1
         * StringTools.indexOfAnyBut("", *)                                = -1
         * StringTools.indexOfAnyBut(*, null)                              = -1
         * StringTools.indexOfAnyBut(*, [])                                = -1
         * StringTools.indexOfAnyBut("zzabyycdxx", new char[] {'z', 'a'} ) = 3
         * StringTools.indexOfAnyBut("aba", new char[] {'z'} )             = 0
         * StringTools.indexOfAnyBut("aba", new char[] {'a', 'b'} )        = -1
         *
         * </pre>
         *
         * @param cs          the CharSequence to check, may be null
         * @param searchChars the chars to search for, may be null
         * @return the index of any of the chars, -1 if no match or null input
         */
        public static int indexOfAnyBut(final CharSequence cs, final char... searchChars) {
            if (isEmpty(cs) || ArrayTools.isEmpty(searchChars)) {
                return INDEX_NOT_FOUND;
            }
            final int csLen = cs.length();
            final int csLast = csLen - 1;
            final int searchLen = searchChars.length;
            final int searchLast = searchLen - 1;
            outer:
            for (int i = 0; i < csLen; i++) {
                final char ch = cs.charAt(i);
                for (int j = 0; j < searchLen; j++) {
                    if (searchChars[j] == ch) {
                        if (i < csLast && j < searchLast && Character.isHighSurrogate(ch)) {
                            if (searchChars[j + 1] == cs.charAt(i + 1)) {
                                continue outer;
                            }
                        } else {
                            continue outer;
                        }
                    }
                }
                return i;
            }
            return INDEX_NOT_FOUND;
        }

        /**
         * <p>Search a CharSequence to find the first index of any
         * character not in the given set of characters.</p>
         *
         * <p>A {@code null} CharSequence will return {@code -1}.
         * A {@code null} or empty search string will return {@code -1}.</p>
         *
         * <pre>
         * StringTools.indexOfAnyBut(null, *)            = -1
         * StringTools.indexOfAnyBut("", *)              = -1
         * StringTools.indexOfAnyBut(*, null)            = -1
         * StringTools.indexOfAnyBut(*, "")              = -1
         * StringTools.indexOfAnyBut("zzabyycdxx", "za") = 3
         * StringTools.indexOfAnyBut("zzabyycdxx", "")   = -1
         * StringTools.indexOfAnyBut("aba", "ab")        = -1
         * </pre>
         *
         * @param seq         the CharSequence to check, may be null
         * @param searchChars the chars to search for, may be null
         * @return the index of any of the chars, -1 if no match or null input
         */
        public static int indexOfAnyBut(final CharSequence seq, final CharSequence searchChars) {
            if (isEmpty(seq) || isEmpty(searchChars)) {
                return INDEX_NOT_FOUND;
            }
            final int strLen = seq.length();
            for (int i = 0; i < strLen; i++) {
                final char ch = seq.charAt(i);
                final boolean chFound = CharSequenceTools.indexOf(searchChars, ch, 0) >= 0;
                if (i + 1 < strLen && Character.isHighSurrogate(ch)) {
                    final char ch2 = seq.charAt(i + 1);
                    if (chFound && CharSequenceTools.indexOf(searchChars, ch2, 0) < 0) {
                        return i;
                    }
                } else {
                    if (!chFound) {
                        return i;
                    }
                }
            }
            return INDEX_NOT_FOUND;
        }

        /**
         * <p>Compares all CharSequences in an array and returns the index at which the
         * CharSequences begin to differ.</p>
         *
         * <p>For example,
         * {@code indexOfDifference(new String[] {"i am a machine", "i am a robot"}) -> 7}</p>
         *
         * <pre>
         * StringTools.indexOfDifference(null) = -1
         * StringTools.indexOfDifference(new String[] {}) = -1
         * StringTools.indexOfDifference(new String[] {"abc"}) = -1
         * StringTools.indexOfDifference(new String[] {null, null}) = -1
         * StringTools.indexOfDifference(new String[] {"", ""}) = -1
         * StringTools.indexOfDifference(new String[] {"", null}) = 0
         * StringTools.indexOfDifference(new String[] {"abc", null, null}) = 0
         * StringTools.indexOfDifference(new String[] {null, null, "abc"}) = 0
         * StringTools.indexOfDifference(new String[] {"", "abc"}) = 0
         * StringTools.indexOfDifference(new String[] {"abc", ""}) = 0
         * StringTools.indexOfDifference(new String[] {"abc", "abc"}) = -1
         * StringTools.indexOfDifference(new String[] {"abc", "a"}) = 1
         * StringTools.indexOfDifference(new String[] {"ab", "abxyz"}) = 2
         * StringTools.indexOfDifference(new String[] {"abcde", "abxyz"}) = 2
         * StringTools.indexOfDifference(new String[] {"abcde", "xyz"}) = 0
         * StringTools.indexOfDifference(new String[] {"xyz", "abcde"}) = 0
         * StringTools.indexOfDifference(new String[] {"i am a machine", "i am a robot"}) = 7
         * </pre>
         *
         * @param css array of CharSequences, entries may be null
         * @return the index where the strings begin to differ; -1 if they are all equal
         */
        public static int indexOfDifference(final CharSequence... css) {
            if (ArrayTools.getLength(css) <= 1) {
                return INDEX_NOT_FOUND;
            }
            boolean anyStringNull = false;
            boolean allStringsNull = true;
            final int arrayLen = css.length - 1;
            int shortestStrLen = Integer.MAX_VALUE;
            int longestStrLen = 0;

            // find the min and max string lengths; this avoids checking to make
            // sure we are not exceeding the length of the string each time through
            // the bottom loop.
            for (final CharSequence cs : css) {
                if (cs == null) {
                    anyStringNull = true;
                    shortestStrLen = 0;
                } else {
                    allStringsNull = false;
                    shortestStrLen = Math.min(cs.length(), shortestStrLen);
                    longestStrLen = Math.max(cs.length(), longestStrLen);
                }
            }

            // handle lists containing all nulls or all empty strings
            if (allStringsNull || longestStrLen == 0 && !anyStringNull) {
                return INDEX_NOT_FOUND;
            }

            // handle lists containing some nulls or some empty strings
            if (shortestStrLen == 0) {
                return 0;
            }

            // find the position with the first difference across all strings
            int firstDiff = -1;
            for (int stringPos = 0; stringPos < shortestStrLen; stringPos++) {
                final char comparisonChar = css[0].charAt(stringPos);
                for (int arrayPos = 1; arrayPos < arrayLen; arrayPos++) {
                    if (css[arrayPos].charAt(stringPos) != comparisonChar) {
                        firstDiff = stringPos;
                        break;
                    }
                }
                if (firstDiff != -1) {
                    break;
                }
            }

            if (firstDiff == -1 && shortestStrLen != longestStrLen) {
                // we compared all of the characters up to the length of the
                // shortest string and didn't find a match, but the string lengths
                // vary, so return the length of the shortest string.
                return shortestStrLen;
            }
            return firstDiff;
        }

        /**
         * <p>Checks if the CharSequence contains only Unicode digits.
         * A decimal point is not a Unicode digit and returns false.</p>
         *
         * <p>{@code null} will return {@code false}.
         * An empty CharSequence (length()=0) will return {@code false}.</p>
         *
         * <p>Note that the method does not allow for a leading sign, either positive or negative.
         * Also, if a String passes the numeric test, it may still generate a NumberFormatException
         * when parsed by Integer.parseInt or Long.parseLong, e.g. if the value is outside the range
         * for int or long respectively.</p>
         *
         * <pre>
         * StringUtils.isNumeric(null)   = false
         * StringUtils.isNumeric("")     = false
         * StringUtils.isNumeric("  ")   = false
         * StringUtils.isNumeric("123")  = true
         * StringUtils.isNumeric("\u0967\u0968\u0969")  = true
         * StringUtils.isNumeric("12 3") = false
         * StringUtils.isNumeric("ab2c") = false
         * StringUtils.isNumeric("12-3") = false
         * StringUtils.isNumeric("12.3") = false
         * StringUtils.isNumeric("-123") = false
         * StringUtils.isNumeric("+123") = false
         * </pre>
         *
         * @param cs the CharSequence to check, may be null
         * @return {@code true} if only contains digits, and is non-null
         * @since 3.0 Changed signature from isNumeric(String) to isNumeric(CharSequence)
         * @since 3.0 Changed "" to return false and not true
         */
        public static boolean isNumeric(final CharSequence cs) {
            if (isEmpty(cs)) {
                return false;
            }
            final int sz = cs.length();
            for (int i = 0; i < sz; i++) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * <p>Checks if the CharSequence contains only lowercase characters.</p>
         *
         * <p>{@code null} will return {@code false}.
         * An empty CharSequence (length()=0) will return {@code false}.</p>
         *
         * <pre>
         * StringTools.isAllLowerCase(null)   = false
         * StringTools.isAllLowerCase("")     = false
         * StringTools.isAllLowerCase("  ")   = false
         * StringTools.isAllLowerCase("abc")  = true
         * StringTools.isAllLowerCase("abC")  = false
         * StringTools.isAllLowerCase("ab c") = false
         * StringTools.isAllLowerCase("ab1c") = false
         * StringTools.isAllLowerCase("ab/c") = false
         * </pre>
         *
         * @param cs the CharSequence to check, may be null
         * @return {@code true} if only contains lowercase characters, and is non-null
         */
        public static boolean isAllLowerCase(final CharSequence cs) {
            if (isEmpty(cs)) {
                return false;
            }
            final int sz = cs.length();
            for (int i = 0; i < sz; i++) {
                if (!Character.isLowerCase(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * <p>Checks if the CharSequence contains only uppercase characters.</p>
         *
         * <p>{@code null} will return {@code false}.
         * An empty String (length()=0) will return {@code false}.</p>
         *
         * <pre>
         * StringTools.isAllUpperCase(null)   = false
         * StringTools.isAllUpperCase("")     = false
         * StringTools.isAllUpperCase("  ")   = false
         * StringTools.isAllUpperCase("ABC")  = true
         * StringTools.isAllUpperCase("aBC")  = false
         * StringTools.isAllUpperCase("A C")  = false
         * StringTools.isAllUpperCase("A1C")  = false
         * StringTools.isAllUpperCase("A/C")  = false
         * </pre>
         *
         * @param cs the CharSequence to check, may be null
         * @return {@code true} if only contains uppercase characters, and is non-null
         */
        public static boolean isAllUpperCase(final CharSequence cs) {
            if (isEmpty(cs)) {
                return false;
            }
            final int sz = cs.length();
            for (int i = 0; i < sz; i++) {
                if (!Character.isUpperCase(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * <p>Checks if a CharSequence is empty ("") or null.</p>
         *
         * <pre>
         * StringTools.isEmpty(null)      = true
         * StringTools.isEmpty("")        = true
         * StringTools.isEmpty(" ")       = false
         * StringTools.isEmpty("bob")     = false
         * StringTools.isEmpty("  bob  ") = false
         * </pre>
         * <p>
         * <p>
         * It no longer trims the CharSequence.
         * That functionality is available in isBlank().</p>
         *
         * @param cs the CharSequence to check, may be null
         * @return {@code true} if the CharSequence is empty or null
         */
        public static boolean isEmpty(final CharSequence cs) {
            return cs == null || cs.length() == 0;
        }

        /**
         * <p>Checks if the CharSequence contains mixed casing of both uppercase and lowercase characters.</p>
         *
         * <p>{@code null} will return {@code false}. An empty CharSequence ({@code length()=0}) will return
         * {@code false}.</p>
         *
         * <pre>
         * StringTools.isMixedCase(null)    = false
         * StringTools.isMixedCase("")      = false
         * StringTools.isMixedCase("ABC")   = false
         * StringTools.isMixedCase("abc")   = false
         * StringTools.isMixedCase("aBc")   = true
         * StringTools.isMixedCase("A c")   = true
         * StringTools.isMixedCase("A1c")   = true
         * StringTools.isMixedCase("a/C")   = true
         * StringTools.isMixedCase("aC\t")  = true
         * </pre>
         *
         * @param cs the CharSequence to check, may be null
         * @return {@code true} if the CharSequence contains both uppercase and lowercase characters
         */
        public static boolean isMixedCase(final CharSequence cs) {
            if (isEmpty(cs) || cs.length() == 1) {
                return false;
            }
            boolean containsUppercase = false;
            boolean containsLowercase = false;
            final int sz = cs.length();
            for (int i = 0; i < sz; i++) {
                if (containsUppercase && containsLowercase) {
                    return true;
                } else if (Character.isUpperCase(cs.charAt(i))) {
                    containsUppercase = true;
                } else if (Character.isLowerCase(cs.charAt(i))) {
                    containsLowercase = true;
                }
            }
            return containsUppercase && containsLowercase;
        }

        /**
         * <p>Checks whether the String a valid Java number.</p>
         *
         * <p>Valid numbers include hexadecimal marked with the {@code 0x} or
         * {@code 0X} qualifier, octal numbers, scientific notation and
         * numbers marked with a type qualifier (e.g. 123L).</p>
         *
         * <p>Non-hexadecimal strings beginning with a leading zero are
         * treated as octal values. Thus the string {@code 09} will return
         * {@code false}, since {@code 9} is not a valid octal value.
         * However, numbers beginning with {@code 0.} are treated as decimal.</p>
         *
         * <p>{@code null} and empty/blank {@code String} will return
         * {@code false}.</p>
         *
         * @param str the {@code String} to check
         * @return {@code true} if the string is a correctly formatted number
         * @since 3.5
         */
        public static boolean isCreatable(final String str) {
            if (StringTools.isEmpty(str)) {
                return false;
            }
            final char[] chars = str.toCharArray();
            int sz = chars.length;
            boolean hasExp = false;
            boolean hasDecPoint = false;
            boolean allowSigns = false;
            boolean foundDigit = false;
            // deal with any possible sign up front
            final int start = chars[0] == '-' || chars[0] == '+' ? 1 : 1;
            if (sz > start + 1 && chars[start] == '0' && !StringTools.contains(str, '.')) { // leading 0, skip if is a
                // decimal number
                if (chars[start + 1] == 'x' || chars[start + 1] == 'X') { // leading 0x/0X
                    int i = start + 2;
                    if (i == sz) {
                        return false; // str == "0x"
                    }
                    // checking hex (it can't be anything else)
                    for (; i < chars.length; i++) {
                        if ((chars[i] < '0' || chars[i] > '9')
                            && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                            return false;
                        }
                    }
                    return true;
                } else if (Character.isDigit(chars[start + 1])) {
                    // leading 0, but not hex, must be octal
                    int i = start + 1;
                    for (; i < chars.length; i++) {
                        if (chars[i] < '0' || chars[i] > '7') {
                            return false;
                        }
                    }
                    return true;
                }
            }
            sz--; // don't want to loop to the last char, check it afterwords
            // for type qualifiers
            int i = start;
            // loop to the next to last char or to the last char if we need another digit to
            // make a valid number (e.g. chars[0..5] = "1234E")
            while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
                if (chars[i] >= '0' && chars[i] <= '9') {
                    foundDigit = true;
                    allowSigns = false;

                } else if (chars[i] == '.') {
                    if (hasDecPoint || hasExp) {
                        // two decimal points or dec in exponent
                        return false;
                    }
                    hasDecPoint = true;
                } else if (chars[i] == 'e' || chars[i] == 'E') {
                    // we've already taken care of hex.
                    if (hasExp) {
                        // two E's
                        return false;
                    }
                    if (!foundDigit) {
                        return false;
                    }
                    hasExp = true;
                    allowSigns = true;
                } else if (chars[i] == '+' || chars[i] == '-') {
                    if (!allowSigns) {
                        return false;
                    }
                    allowSigns = false;
                    foundDigit = false; // we need a digit after the E
                } else {
                    return false;
                }
                i++;
            }
            if (i < chars.length) {
                if (chars[i] >= '0' && chars[i] <= '9') {
                    // no type qualifier, OK
                    return true;
                }
                if (chars[i] == 'e' || chars[i] == 'E') {
                    // can't have an E at the last byte
                    return false;
                }
                if (chars[i] == '.') {
                    if (hasDecPoint || hasExp) {
                        // two decimal points or dec in exponent
                        return false;
                    }
                    // single trailing decimal point after non-exponent is ok
                    return foundDigit;
                }
                if (!allowSigns
                    && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                    return foundDigit;
                }
                if (chars[i] == 'l'
                    || chars[i] == 'L') {
                    // not allowing L with an exponent or decimal point
                    return foundDigit && !hasExp && !hasDecPoint;
                }
                // last character is illegal
                return false;
            }
            // allowSigns is true iff the val ends in 'E'
            // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
            return !allowSigns && foundDigit;
        }
    }
}
