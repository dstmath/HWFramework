package android.icu.impl;

import android.icu.text.PluralRules;

public final class SimpleFormatterImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ARG_NUM_LIMIT = 256;
    private static final String[][] COMMON_PATTERNS = {new String[]{"{0} {1}", "\u0002\u0000ā \u0001"}, new String[]{"{0} ({1})", "\u0002\u0000Ă (\u0001ā)"}, new String[]{"{0}, {1}", "\u0002\u0000Ă, \u0001"}, new String[]{"{0} – {1}", "\u0002\u0000ă – \u0001"}};
    private static final char LEN1_CHAR = 'ā';
    private static final char LEN2_CHAR = 'Ă';
    private static final char LEN3_CHAR = 'ă';
    private static final int MAX_SEGMENT_LENGTH = 65279;
    private static final char SEGMENT_LENGTH_ARGUMENT_CHAR = '￿';

    private SimpleFormatterImpl() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x00d7  */
    public static String compileToStringMinMaxArguments(CharSequence pattern, StringBuilder sb, int min, int max) {
        int argNumber;
        CharSequence charSequence = pattern;
        StringBuilder sb2 = sb;
        int i = min;
        int i2 = max;
        int i3 = 1;
        if (i <= 2 && 2 <= i2) {
            for (String[] pair : COMMON_PATTERNS) {
                if (pair[0].contentEquals(charSequence)) {
                    return pair[1];
                }
            }
        }
        int patternLength = pattern.length();
        sb2.ensureCapacity(patternLength);
        sb2.setLength(1);
        boolean inQuote = false;
        int maxArg = -1;
        int textLength = 0;
        int i4 = 0;
        while (i4 < patternLength) {
            int i5 = i4 + 1;
            char c = charSequence.charAt(i4);
            if (c == '\'') {
                if (i5 < patternLength) {
                    char charAt = charSequence.charAt(i5);
                    c = charAt;
                    if (charAt == '\'') {
                        i5++;
                    }
                }
                if (inQuote) {
                    inQuote = false;
                    i4 = i5;
                } else if (c == '{' || c == '}') {
                    i5++;
                    inQuote = true;
                } else {
                    c = PatternTokenizer.SINGLE_QUOTE;
                }
            } else if (!inQuote && c == '{') {
                if (textLength > 0) {
                    sb2.setCharAt((sb.length() - textLength) - i3, (char) (256 + textLength));
                    textLength = 0;
                }
                if (i5 + 1 < patternLength) {
                    int charAt2 = charSequence.charAt(i5) - '0';
                    int argNumber2 = charAt2;
                    if (charAt2 >= 0) {
                        argNumber = argNumber2;
                        if (argNumber <= 9 && charSequence.charAt(i5 + 1) == '}') {
                            i5 += 2;
                            if (argNumber > maxArg) {
                                maxArg = argNumber;
                            }
                            sb2.append((char) argNumber);
                            i4 = i5;
                            i3 = 1;
                        }
                    }
                }
                int argStart = i5 - 1;
                int argNumber3 = -1;
                if (i5 < patternLength) {
                    int i6 = i5 + 1;
                    char charAt3 = charSequence.charAt(i5);
                    c = charAt3;
                    if ('1' <= charAt3 && c <= '9') {
                        argNumber3 = c - '0';
                        while (true) {
                            i5 = i6;
                            if (i5 >= patternLength) {
                                break;
                            }
                            i6 = i5 + 1;
                            char charAt4 = charSequence.charAt(i5);
                            c = charAt4;
                            if ('0' > charAt4 || c > '9') {
                                break;
                            }
                            argNumber3 = (argNumber3 * 10) + (c - '0');
                            if (argNumber3 >= 256) {
                                break;
                            }
                        }
                    }
                    i5 = i6;
                }
                int argNumber4 = argNumber3;
                if (argNumber4 < 0 || c != '}') {
                    throw new IllegalArgumentException("Argument syntax error in pattern \"" + charSequence + "\" at index " + argStart + PluralRules.KEYWORD_RULE_SEPARATOR + charSequence.subSequence(argStart, i5));
                }
                argNumber = argNumber4;
                if (argNumber > maxArg) {
                }
                sb2.append((char) argNumber);
                i4 = i5;
                i3 = 1;
            }
            if (textLength == 0) {
                sb2.append(65535);
            }
            sb2.append(c);
            textLength++;
            if (textLength == MAX_SEGMENT_LENGTH) {
                textLength = 0;
            }
            i4 = i5;
            i3 = 1;
        }
        if (textLength > 0) {
            sb2.setCharAt((sb.length() - textLength) - 1, (char) (256 + textLength));
        }
        int argCount = maxArg + 1;
        if (argCount < i) {
            throw new IllegalArgumentException("Fewer than minimum " + i + " arguments in pattern \"" + charSequence + "\"");
        } else if (argCount <= i2) {
            sb2.setCharAt(0, (char) argCount);
            return sb.toString();
        } else {
            throw new IllegalArgumentException("More than maximum " + i2 + " arguments in pattern \"" + charSequence + "\"");
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
        if ((values != null ? values.length : 0) >= getArgumentLimit(compiledPattern)) {
            int firstArg = -1;
            String resultCopy = null;
            if (getArgumentLimit(compiledPattern) > 0) {
                int i = 1;
                while (i < compiledPattern.length()) {
                    int i2 = i + 1;
                    int n = compiledPattern.charAt(i);
                    if (n >= 256) {
                        i2 += n - 256;
                    } else if (values[n] == result) {
                        if (i2 == 2) {
                            firstArg = n;
                        } else if (resultCopy == null) {
                            resultCopy = result.toString();
                        }
                    }
                    i = i2;
                }
            }
            String resultCopy2 = resultCopy;
            if (firstArg < 0) {
                result.setLength(0);
            }
            return format(compiledPattern, values, result, resultCopy2, false, offsets);
        }
        throw new IllegalArgumentException("Too few values.");
    }

    public static String getTextWithNoArguments(String compiledPattern) {
        int segmentLength = 1;
        StringBuilder sb = new StringBuilder((compiledPattern.length() - 1) - getArgumentLimit(compiledPattern));
        while (segmentLength < compiledPattern.length()) {
            int i = segmentLength + 1;
            int i2 = compiledPattern.charAt(segmentLength) - 256;
            if (i2 > 0) {
                int limit = i + i2;
                sb.append(compiledPattern, i, limit);
                segmentLength = limit;
            } else {
                segmentLength = i;
            }
        }
        return sb.toString();
    }

    private static StringBuilder format(String compiledPattern, CharSequence[] values, StringBuilder result, String resultCopy, boolean forbidResultAsValue, int[] offsets) {
        int offsetsLength;
        if (offsets == null) {
            offsetsLength = 0;
        } else {
            offsetsLength = offsets.length;
            for (int i = 0; i < offsetsLength; i++) {
                offsets[i] = -1;
            }
        }
        int n = 1;
        while (n < compiledPattern.length()) {
            int i2 = n + 1;
            int i3 = compiledPattern.charAt(n);
            if (i3 < 256) {
                CharSequence value = values[i3];
                if (value != result) {
                    if (i3 < offsetsLength) {
                        offsets[i3] = result.length();
                    }
                    result.append(value);
                } else if (forbidResultAsValue) {
                    throw new IllegalArgumentException("Value must not be same object as result");
                } else if (i2 != 2) {
                    if (i3 < offsetsLength) {
                        offsets[i3] = result.length();
                    }
                    result.append(resultCopy);
                } else if (i3 < offsetsLength) {
                    offsets[i3] = 0;
                }
                n = i2;
            } else {
                int limit = (i3 - 256) + i2;
                result.append(compiledPattern, i2, limit);
                n = limit;
            }
        }
        return result;
    }
}
