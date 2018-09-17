package android.icu.impl;

import android.icu.text.PluralRules;
import android.icu.util.AnnualTimeZoneRule;

public final class SimplePatternFormatter {
    private static final int ARG_NUM_LIMIT = 256;
    private static final int MAX_SEGMENT_LENGTH = 65279;
    private static final char SEGMENT_LENGTH_PLACEHOLDER_CHAR = '\uffff';
    private final String compiledPattern;

    private SimplePatternFormatter(String compiledPattern) {
        this.compiledPattern = compiledPattern;
    }

    public static SimplePatternFormatter compile(CharSequence pattern) {
        return compileMinMaxPlaceholders(pattern, 0, AnnualTimeZoneRule.MAX_YEAR);
    }

    public static SimplePatternFormatter compileMinMaxPlaceholders(CharSequence pattern, int min, int max) {
        return new SimplePatternFormatter(compileToStringMinMaxPlaceholders(pattern, new StringBuilder(), min, max));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String compileToStringMinMaxPlaceholders(CharSequence pattern, StringBuilder sb, int min, int max) {
        int patternLength = pattern.length();
        sb.ensureCapacity(patternLength);
        sb.setLength(1);
        int textLength = 0;
        int maxArg = -1;
        boolean inQuote = false;
        int i = 0;
        while (i < patternLength) {
            int i2 = i + 1;
            char c = pattern.charAt(i);
            if (c == PatternTokenizer.SINGLE_QUOTE) {
                if (i2 < patternLength) {
                    c = pattern.charAt(i2);
                    if (c == PatternTokenizer.SINGLE_QUOTE) {
                        i2++;
                    }
                }
                if (inQuote) {
                    inQuote = false;
                    i = i2;
                } else if (c == '{' || c == '}') {
                    i2++;
                    inQuote = true;
                } else {
                    c = PatternTokenizer.SINGLE_QUOTE;
                }
            } else if (!inQuote && c == '{') {
                int argNumber;
                if (textLength > 0) {
                    sb.setCharAt((sb.length() - textLength) - 1, (char) (textLength + ARG_NUM_LIMIT));
                    textLength = 0;
                }
                if (i2 + 1 < patternLength) {
                    argNumber = pattern.charAt(i2) - 48;
                    if (argNumber >= 0 && argNumber <= 9 && pattern.charAt(i2 + 1) == '}') {
                        i2 += 2;
                        if (argNumber > maxArg) {
                            maxArg = argNumber;
                        }
                        sb.append((char) argNumber);
                        i = i2;
                    }
                }
                int argStart = i2 - 1;
                argNumber = -1;
                if (i2 < patternLength) {
                    i = i2 + 1;
                    c = pattern.charAt(i2);
                    if ('1' > c || c > '9') {
                        i2 = i;
                    } else {
                        argNumber = c - 48;
                        while (i < patternLength) {
                            i2 = i + 1;
                            c = pattern.charAt(i);
                            if ('0' <= c && c <= '9') {
                                argNumber = (argNumber * 10) + (c - 48);
                                if (argNumber >= ARG_NUM_LIMIT) {
                                    break;
                                }
                                i = i2;
                            }
                        }
                        i2 = i;
                    }
                }
                if (argNumber < 0 || c != '}') {
                    throw new IllegalArgumentException("Argument syntax error in pattern \"" + pattern + "\" at index " + argStart + PluralRules.KEYWORD_RULE_SEPARATOR + pattern.subSequence(argStart, i2));
                }
                if (argNumber > maxArg) {
                    maxArg = argNumber;
                }
                sb.append((char) argNumber);
                i = i2;
            }
            if (textLength == 0) {
                sb.append(SEGMENT_LENGTH_PLACEHOLDER_CHAR);
            }
            sb.append(c);
            textLength++;
            if (textLength == MAX_SEGMENT_LENGTH) {
                textLength = 0;
            }
            i = i2;
        }
        if (textLength > 0) {
            sb.setCharAt((sb.length() - textLength) - 1, (char) (textLength + ARG_NUM_LIMIT));
        }
        int argCount = maxArg + 1;
        if (argCount < min) {
            throw new IllegalArgumentException("Fewer than minimum " + min + " placeholders in pattern \"" + pattern + "\"");
        } else if (argCount > max) {
            throw new IllegalArgumentException("More than maximum " + max + " placeholders in pattern \"" + pattern + "\"");
        } else {
            sb.setCharAt(0, (char) argCount);
            return sb.toString();
        }
    }

    public int getPlaceholderCount() {
        return getPlaceholderCount(this.compiledPattern);
    }

    public static int getPlaceholderCount(String compiledPattern) {
        return compiledPattern.charAt(0);
    }

    public String format(CharSequence... values) {
        return formatCompiledPattern(this.compiledPattern, values);
    }

    public static String formatCompiledPattern(String compiledPattern, CharSequence... values) {
        return formatAndAppend(compiledPattern, new StringBuilder(), null, values).toString();
    }

    public StringBuilder formatAndAppend(StringBuilder appendTo, int[] offsets, CharSequence... values) {
        return formatAndAppend(this.compiledPattern, appendTo, offsets, values);
    }

    public static StringBuilder formatAndAppend(String compiledPattern, StringBuilder appendTo, int[] offsets, CharSequence... values) {
        if ((values != null ? values.length : 0) >= getPlaceholderCount(compiledPattern)) {
            return format(compiledPattern, values, appendTo, null, true, offsets);
        }
        throw new IllegalArgumentException("Too few values.");
    }

    public StringBuilder formatAndReplace(StringBuilder result, int[] offsets, CharSequence... values) {
        return formatAndReplace(this.compiledPattern, result, offsets, values);
    }

    public static StringBuilder formatAndReplace(String compiledPattern, StringBuilder result, int[] offsets, CharSequence... values) {
        if ((values != null ? values.length : 0) < getPlaceholderCount(compiledPattern)) {
            throw new IllegalArgumentException("Too few values.");
        }
        int firstArg = -1;
        String str = null;
        if (getPlaceholderCount(compiledPattern) > 0) {
            int i = 1;
            while (i < compiledPattern.length()) {
                int i2 = i + 1;
                int n = compiledPattern.charAt(i);
                if (n < ARG_NUM_LIMIT) {
                    if (values[n] == result) {
                        if (i2 == 2) {
                            firstArg = n;
                            i = i2;
                        } else if (str == null) {
                            str = result.toString();
                            i = i2;
                        }
                    }
                    i = i2;
                } else {
                    i = i2 + (n - 256);
                }
            }
        }
        if (firstArg < 0) {
            result.setLength(0);
        }
        return format(compiledPattern, values, result, str, false, offsets);
    }

    public String toString() {
        String[] values = new String[getPlaceholderCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = String.format("{%d}", new Object[]{Integer.valueOf(i)});
        }
        return formatAndAppend(new StringBuilder(), null, values).toString();
    }

    public String getTextWithNoPlaceholders() {
        return getTextWithNoPlaceholders(this.compiledPattern);
    }

    public static String getTextWithNoPlaceholders(String compiledPattern) {
        StringBuilder sb = new StringBuilder((compiledPattern.length() - 1) - getPlaceholderCount(compiledPattern));
        int i = 1;
        while (i < compiledPattern.length()) {
            int i2 = i + 1;
            int segmentLength = compiledPattern.charAt(i) - 256;
            if (segmentLength > 0) {
                int limit = i2 + segmentLength;
                sb.append(compiledPattern, i2, limit);
                i = limit;
            } else {
                i = i2;
            }
        }
        return sb.toString();
    }

    private static StringBuilder format(String compiledPattern, CharSequence[] values, StringBuilder result, String resultCopy, boolean forbidResultAsValue, int[] offsets) {
        int offsetsLength;
        int i;
        if (offsets == null) {
            offsetsLength = 0;
        } else {
            offsetsLength = offsets.length;
            for (i = 0; i < offsetsLength; i++) {
                offsets[i] = -1;
            }
        }
        i = 1;
        while (i < compiledPattern.length()) {
            int i2 = i + 1;
            int n = compiledPattern.charAt(i);
            if (n < ARG_NUM_LIMIT) {
                StringBuilder value = values[n];
                if (value != result) {
                    if (n < offsetsLength) {
                        offsets[n] = result.length();
                    }
                    result.append(value);
                    i = i2;
                } else if (forbidResultAsValue) {
                    throw new IllegalArgumentException("Value must not be same object as result");
                } else if (i2 != 2) {
                    if (n < offsetsLength) {
                        offsets[n] = result.length();
                    }
                    result.append(resultCopy);
                    i = i2;
                } else if (n < offsetsLength) {
                    offsets[n] = 0;
                    i = i2;
                } else {
                    i = i2;
                }
            } else {
                int limit = i2 + (n - 256);
                result.append(compiledPattern, i2, limit);
                i = limit;
            }
        }
        return result;
    }
}
