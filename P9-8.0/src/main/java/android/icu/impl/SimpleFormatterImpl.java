package android.icu.impl;

import android.icu.text.PluralRules;

public final class SimpleFormatterImpl {
    static final /* synthetic */ boolean -assertionsDisabled = (SimpleFormatterImpl.class.desiredAssertionStatus() ^ 1);
    private static final int ARG_NUM_LIMIT = 256;
    private static final String[][] COMMON_PATTERNS;
    private static final char LEN1_CHAR = 'ā';
    private static final char LEN2_CHAR = 'Ă';
    private static final char LEN3_CHAR = 'ă';
    private static final int MAX_SEGMENT_LENGTH = 65279;
    private static final char SEGMENT_LENGTH_ARGUMENT_CHAR = '￿';

    static {
        r0 = new String[4][];
        r0[0] = new String[]{"{0} {1}", "\u0002\u0000ā \u0001"};
        r0[1] = new String[]{"{0} ({1})", "\u0002\u0000Ă (\u0001ā)"};
        r0[2] = new String[]{"{0}, {1}", "\u0002\u0000Ă, \u0001"};
        r0[3] = new String[]{"{0} – {1}", "\u0002\u0000ă – \u0001"};
        COMMON_PATTERNS = r0;
    }

    private SimpleFormatterImpl() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x00cd  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String compileToStringMinMaxArguments(CharSequence pattern, StringBuilder sb, int min, int max) {
        if (min <= 2 && 2 <= max) {
            String[][] strArr = COMMON_PATTERNS;
            int i = 0;
            int length = strArr.length;
            while (i < length) {
                String[] pair = strArr[i];
                if (!pair[0].contentEquals(pattern)) {
                    i++;
                } else if (-assertionsDisabled || pair[1].charAt(0) == 2) {
                    return pair[1];
                } else {
                    throw new AssertionError();
                }
            }
        }
        int patternLength = pattern.length();
        sb.ensureCapacity(patternLength);
        sb.setLength(1);
        int textLength = 0;
        int maxArg = -1;
        boolean inQuote = false;
        int i2 = 0;
        while (i2 < patternLength) {
            int i3 = i2 + 1;
            char c = pattern.charAt(i2);
            if (c == PatternTokenizer.SINGLE_QUOTE) {
                if (i3 < patternLength) {
                    c = pattern.charAt(i3);
                    if (c == PatternTokenizer.SINGLE_QUOTE) {
                        i3++;
                    }
                }
                if (inQuote) {
                    inQuote = false;
                    i2 = i3;
                } else if (c == '{' || c == '}') {
                    i3++;
                    inQuote = true;
                } else {
                    c = PatternTokenizer.SINGLE_QUOTE;
                }
            } else if (!inQuote && c == '{') {
                int argNumber;
                if (textLength > 0) {
                    sb.setCharAt((sb.length() - textLength) - 1, (char) (textLength + 256));
                    textLength = 0;
                }
                if (i3 + 1 < patternLength) {
                    argNumber = pattern.charAt(i3) - 48;
                    if (argNumber >= 0 && argNumber <= 9) {
                        if (pattern.charAt(i3 + 1) == '}') {
                            i3 += 2;
                            if (argNumber > maxArg) {
                                maxArg = argNumber;
                            }
                            sb.append((char) argNumber);
                            i2 = i3;
                        }
                    }
                }
                int argStart = i3 - 1;
                argNumber = -1;
                if (i3 < patternLength) {
                    i2 = i3 + 1;
                    c = pattern.charAt(i3);
                    if ('1' > c || c > '9') {
                        i3 = i2;
                    } else {
                        argNumber = c - 48;
                        while (i2 < patternLength) {
                            i3 = i2 + 1;
                            c = pattern.charAt(i2);
                            if ('0' > c || c > '9') {
                                break;
                            }
                            argNumber = (argNumber * 10) + (c - 48);
                            if (argNumber >= 256) {
                                break;
                            }
                            i2 = i3;
                        }
                        i3 = i2;
                    }
                }
                if (argNumber < 0 || c != '}') {
                    throw new IllegalArgumentException("Argument syntax error in pattern \"" + pattern + "\" at index " + argStart + PluralRules.KEYWORD_RULE_SEPARATOR + pattern.subSequence(argStart, i3));
                }
                if (argNumber > maxArg) {
                }
                sb.append((char) argNumber);
                i2 = i3;
            }
            if (textLength == 0) {
                sb.append(65535);
            }
            sb.append(c);
            textLength++;
            if (textLength == MAX_SEGMENT_LENGTH) {
                textLength = 0;
            }
            i2 = i3;
        }
        if (textLength > 0) {
            sb.setCharAt((sb.length() - textLength) - 1, (char) (textLength + 256));
        }
        int argCount = maxArg + 1;
        if (argCount < min) {
            throw new IllegalArgumentException("Fewer than minimum " + min + " arguments in pattern \"" + pattern + "\"");
        } else if (argCount > max) {
            throw new IllegalArgumentException("More than maximum " + max + " arguments in pattern \"" + pattern + "\"");
        } else {
            sb.setCharAt(0, (char) argCount);
            return sb.toString();
        }
    }

    public static int getArgumentLimit(String compiledPattern) {
        return compiledPattern.charAt(0);
    }

    public static String formatCompiledPattern(String compiledPattern, CharSequence... values) {
        return formatAndAppend(compiledPattern, new StringBuilder(), null, values).toString();
    }

    public static String formatRawPattern(String pattern, int min, int max, CharSequence... values) {
        StringBuilder sb = new StringBuilder();
        String compiledPattern = compileToStringMinMaxArguments(pattern, sb, min, max);
        sb.setLength(0);
        return formatAndAppend(compiledPattern, sb, null, values).toString();
    }

    public static StringBuilder formatAndAppend(String compiledPattern, StringBuilder appendTo, int[] offsets, CharSequence... values) {
        if ((values != null ? values.length : 0) >= getArgumentLimit(compiledPattern)) {
            return format(compiledPattern, values, appendTo, null, true, offsets);
        }
        throw new IllegalArgumentException("Too few values.");
    }

    public static StringBuilder formatAndReplace(String compiledPattern, StringBuilder result, int[] offsets, CharSequence... values) {
        if ((values != null ? values.length : 0) < getArgumentLimit(compiledPattern)) {
            throw new IllegalArgumentException("Too few values.");
        }
        int firstArg = -1;
        String resultCopy = null;
        if (getArgumentLimit(compiledPattern) > 0) {
            int i = 1;
            while (i < compiledPattern.length()) {
                int i2 = i + 1;
                int n = compiledPattern.charAt(i);
                if (n < 256) {
                    if (values[n] == result) {
                        if (i2 == 2) {
                            firstArg = n;
                            i = i2;
                        } else if (resultCopy == null) {
                            resultCopy = result.toString();
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
        return format(compiledPattern, values, result, resultCopy, false, offsets);
    }

    public static String getTextWithNoArguments(String compiledPattern) {
        StringBuilder sb = new StringBuilder((compiledPattern.length() - 1) - getArgumentLimit(compiledPattern));
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
            if (n < 256) {
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
