package android.os;

import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;
import java.util.Arrays;

public class PatternMatcher implements Parcelable {
    public static final Parcelable.Creator<PatternMatcher> CREATOR = new Parcelable.Creator<PatternMatcher>() {
        /* class android.os.PatternMatcher.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PatternMatcher createFromParcel(Parcel source) {
            return new PatternMatcher(source);
        }

        @Override // android.os.Parcelable.Creator
        public PatternMatcher[] newArray(int size) {
            return new PatternMatcher[size];
        }
    };
    private static final int MAX_PATTERN_STORAGE = 2048;
    private static final int NO_MATCH = -1;
    private static final int PARSED_MODIFIER_ONE_OR_MORE = -8;
    private static final int PARSED_MODIFIER_RANGE_START = -5;
    private static final int PARSED_MODIFIER_RANGE_STOP = -6;
    private static final int PARSED_MODIFIER_ZERO_OR_MORE = -7;
    private static final int PARSED_TOKEN_CHAR_ANY = -4;
    private static final int PARSED_TOKEN_CHAR_SET_INVERSE_START = -2;
    private static final int PARSED_TOKEN_CHAR_SET_START = -1;
    private static final int PARSED_TOKEN_CHAR_SET_STOP = -3;
    public static final int PATTERN_ADVANCED_GLOB = 3;
    public static final int PATTERN_LITERAL = 0;
    public static final int PATTERN_PREFIX = 1;
    public static final int PATTERN_SIMPLE_GLOB = 2;
    private static final String TAG = "PatternMatcher";
    private static final int TOKEN_TYPE_ANY = 1;
    private static final int TOKEN_TYPE_INVERSE_SET = 3;
    private static final int TOKEN_TYPE_LITERAL = 0;
    private static final int TOKEN_TYPE_SET = 2;
    private static final int[] sParsedPatternScratch = new int[2048];
    private final int[] mParsedPattern;
    private final String mPattern;
    private final int mType;

    public PatternMatcher(String pattern, int type) {
        this.mPattern = pattern;
        this.mType = type;
        if (this.mType == 3) {
            this.mParsedPattern = parseAndVerifyAdvancedPattern(pattern);
        } else {
            this.mParsedPattern = null;
        }
    }

    public final String getPath() {
        return this.mPattern;
    }

    public final int getType() {
        return this.mType;
    }

    public boolean match(String str) {
        return matchPattern(str, this.mPattern, this.mParsedPattern, this.mType);
    }

    public String toString() {
        String type = "? ";
        int i = this.mType;
        if (i == 0) {
            type = "LITERAL: ";
        } else if (i == 1) {
            type = "PREFIX: ";
        } else if (i == 2) {
            type = "GLOB: ";
        } else if (i == 3) {
            type = "ADVANCED: ";
        }
        return "PatternMatcher{" + type + this.mPattern + "}";
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mPattern);
        proto.write(1159641169922L, this.mType);
        proto.end(token);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPattern);
        dest.writeInt(this.mType);
        dest.writeIntArray(this.mParsedPattern);
    }

    public PatternMatcher(Parcel src) {
        this.mPattern = src.readString();
        this.mType = src.readInt();
        this.mParsedPattern = src.createIntArray();
    }

    static boolean matchPattern(String match, String pattern, int[] parsedPattern, int type) {
        if (match == null) {
            return false;
        }
        if (type == 0) {
            return pattern.equals(match);
        }
        if (type == 1) {
            return match.startsWith(pattern);
        }
        if (type == 2) {
            return matchGlobPattern(pattern, match);
        }
        if (type == 3) {
            return matchAdvancedPattern(parsedPattern, match);
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0084  */
    static boolean matchGlobPattern(String pattern, String match) {
        int NP = pattern.length();
        if (NP <= 0) {
            return match.length() <= 0;
        }
        int NM = match.length();
        int ip = 0;
        int im = 0;
        char nextChar = pattern.charAt(0);
        while (ip < NP && im < NM) {
            char c = nextChar;
            ip++;
            nextChar = ip < NP ? pattern.charAt(ip) : 0;
            boolean escaped = c == '\\';
            if (escaped) {
                c = nextChar;
                ip++;
                nextChar = ip < NP ? pattern.charAt(ip) : 0;
            }
            if (nextChar == '*') {
                if (escaped || c != '.') {
                    while (match.charAt(im) == c && (im = im + 1) < NM) {
                        while (match.charAt(im) == c) {
                            while (match.charAt(im) == c) {
                            }
                        }
                    }
                    ip++;
                    nextChar = ip < NP ? pattern.charAt(ip) : 0;
                } else if (ip >= NP - 1) {
                    return true;
                } else {
                    int ip2 = ip + 1;
                    char nextChar2 = pattern.charAt(ip2);
                    if (nextChar2 == '\\') {
                        ip2++;
                        nextChar2 = ip2 < NP ? pattern.charAt(ip2) : 0;
                    }
                    while (match.charAt(im) != nextChar2 && (im = im + 1) < NM) {
                        while (match.charAt(im) != nextChar2) {
                            while (match.charAt(im) != nextChar2) {
                            }
                        }
                    }
                    if (im == NM) {
                        return false;
                    }
                    ip = ip2 + 1;
                    nextChar = ip < NP ? pattern.charAt(ip) : 0;
                    im++;
                }
            } else if (c != '.' && match.charAt(im) != c) {
                return false;
            } else {
                im++;
            }
        }
        if (ip < NP || im < NM) {
            return ip == NP + -2 && pattern.charAt(ip) == '.' && pattern.charAt(ip + 1) == '*';
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x0121  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0160  */
    static synchronized int[] parseAndVerifyAdvancedPattern(String pattern) {
        int[] copyOf;
        int it;
        int it2;
        int it3;
        int rangeMax;
        int parsedRange;
        int it4;
        synchronized (PatternMatcher.class) {
            int ip = 0;
            int LP = pattern.length();
            int it5 = 0;
            boolean inCharClass = false;
            boolean inRange = false;
            boolean inCharClass2 = false;
            while (ip < LP) {
                if (it5 <= 2045) {
                    int charAt = pattern.charAt(ip);
                    boolean addToParsedPattern = false;
                    if (charAt != 42) {
                        if (charAt != 43) {
                            if (charAt != 46) {
                                if (charAt != 123) {
                                    if (charAt != 125) {
                                        switch (charAt) {
                                            case 91:
                                                if (inCharClass) {
                                                    addToParsedPattern = true;
                                                    it = it5;
                                                    it2 = ip;
                                                    break;
                                                } else {
                                                    if (pattern.charAt(ip + 1) == '^') {
                                                        sParsedPatternScratch[it5] = -2;
                                                        ip++;
                                                        it5++;
                                                    } else {
                                                        sParsedPatternScratch[it5] = -1;
                                                        it5++;
                                                    }
                                                    ip++;
                                                    inCharClass = true;
                                                    continue;
                                                }
                                            case 92:
                                                if (ip + 1 < LP) {
                                                    int ip2 = ip + 1;
                                                    charAt = pattern.charAt(ip2);
                                                    addToParsedPattern = true;
                                                    it = it5;
                                                    it2 = ip2;
                                                    break;
                                                } else {
                                                    throw new IllegalArgumentException("Escape found at end of pattern!");
                                                }
                                            case 93:
                                                if (inCharClass) {
                                                    int parsedToken = sParsedPatternScratch[it5 - 1];
                                                    if (parsedToken != -1 && parsedToken != -2) {
                                                        it = it5 + 1;
                                                        sParsedPatternScratch[it5] = -3;
                                                        inCharClass2 = false;
                                                        inCharClass = false;
                                                        it2 = ip;
                                                        break;
                                                    } else {
                                                        throw new IllegalArgumentException("You must define characters in a set.");
                                                    }
                                                } else {
                                                    addToParsedPattern = true;
                                                    it = it5;
                                                    it2 = ip;
                                                    break;
                                                }
                                                break;
                                            default:
                                                addToParsedPattern = true;
                                                it = it5;
                                                it2 = ip;
                                                break;
                                        }
                                    } else if (inRange) {
                                        it = it5 + 1;
                                        sParsedPatternScratch[it5] = -6;
                                        inRange = false;
                                        it2 = ip;
                                    }
                                    if (!inCharClass) {
                                        if (inCharClass2) {
                                            it3 = it + 1;
                                            sParsedPatternScratch[it] = charAt;
                                            inCharClass2 = false;
                                        } else if (it2 + 2 >= LP || pattern.charAt(it2 + 1) != '-' || pattern.charAt(it2 + 2) == ']') {
                                            int it6 = it + 1;
                                            sParsedPatternScratch[it] = charAt;
                                            sParsedPatternScratch[it6] = charAt;
                                            it3 = it6 + 1;
                                        } else {
                                            it3 = it + 1;
                                            sParsedPatternScratch[it] = charAt;
                                            it2++;
                                            inCharClass2 = true;
                                        }
                                    } else if (inRange) {
                                        int endOfSet = pattern.indexOf(125, it2);
                                        if (endOfSet >= 0) {
                                            String rangeString = pattern.substring(it2, endOfSet);
                                            int commaIndex = rangeString.indexOf(44);
                                            if (commaIndex < 0) {
                                                try {
                                                    parsedRange = Integer.parseInt(rangeString);
                                                    rangeMax = parsedRange;
                                                } catch (NumberFormatException e) {
                                                    e = e;
                                                    throw new IllegalArgumentException("Range number format incorrect", e);
                                                }
                                            } else {
                                                parsedRange = Integer.parseInt(rangeString.substring(0, commaIndex));
                                                if (commaIndex == rangeString.length() - 1) {
                                                    rangeMax = Integer.MAX_VALUE;
                                                } else {
                                                    rangeMax = Integer.parseInt(rangeString.substring(commaIndex + 1));
                                                }
                                            }
                                            if (parsedRange <= rangeMax) {
                                                int it7 = it + 1;
                                                try {
                                                    sParsedPatternScratch[it] = parsedRange;
                                                    it4 = it7 + 1;
                                                } catch (NumberFormatException e2) {
                                                    e = e2;
                                                    throw new IllegalArgumentException("Range number format incorrect", e);
                                                }
                                                try {
                                                    sParsedPatternScratch[it7] = rangeMax;
                                                    ip = endOfSet;
                                                    it5 = it4;
                                                } catch (NumberFormatException e3) {
                                                    e = e3;
                                                    throw new IllegalArgumentException("Range number format incorrect", e);
                                                }
                                            } else {
                                                throw new IllegalArgumentException("Range quantifier minimum is greater than maximum");
                                            }
                                        } else {
                                            throw new IllegalArgumentException("Range not ended with '}'");
                                        }
                                    } else if (addToParsedPattern) {
                                        it3 = it + 1;
                                        sParsedPatternScratch[it] = charAt;
                                    } else {
                                        it3 = it;
                                    }
                                    ip = it2 + 1;
                                    it5 = it3;
                                } else if (!inCharClass) {
                                    if (it5 == 0 || isParsedModifier(sParsedPatternScratch[it5 - 1])) {
                                        throw new IllegalArgumentException("Modifier must follow a token.");
                                    }
                                    it = it5 + 1;
                                    sParsedPatternScratch[it5] = -5;
                                    inRange = true;
                                    it2 = ip + 1;
                                    if (!inCharClass) {
                                    }
                                    ip = it2 + 1;
                                    it5 = it3;
                                }
                            } else if (!inCharClass) {
                                it = it5 + 1;
                                sParsedPatternScratch[it5] = -4;
                                it2 = ip;
                                if (!inCharClass) {
                                }
                                ip = it2 + 1;
                                it5 = it3;
                            }
                        } else if (!inCharClass) {
                            if (it5 == 0 || isParsedModifier(sParsedPatternScratch[it5 - 1])) {
                                throw new IllegalArgumentException("Modifier must follow a token.");
                            }
                            it = it5 + 1;
                            sParsedPatternScratch[it5] = -8;
                            it2 = ip;
                            if (!inCharClass) {
                            }
                            ip = it2 + 1;
                            it5 = it3;
                        }
                    } else if (!inCharClass) {
                        if (it5 == 0 || isParsedModifier(sParsedPatternScratch[it5 - 1])) {
                            throw new IllegalArgumentException("Modifier must follow a token.");
                        }
                        it = it5 + 1;
                        sParsedPatternScratch[it5] = -7;
                        it2 = ip;
                        if (!inCharClass) {
                        }
                        ip = it2 + 1;
                        it5 = it3;
                    }
                    it = it5;
                    it2 = ip;
                    if (!inCharClass) {
                    }
                    ip = it2 + 1;
                    it5 = it3;
                } else {
                    throw new IllegalArgumentException("Pattern is too large!");
                }
            }
            if (!inCharClass) {
                copyOf = Arrays.copyOf(sParsedPatternScratch, it5);
            } else {
                throw new IllegalArgumentException("Set was not terminated!");
            }
        }
        return copyOf;
    }

    private static boolean isParsedModifier(int parsedChar) {
        return parsedChar == -8 || parsedChar == -7 || parsedChar == -6 || parsedChar == -5;
    }

    static boolean matchAdvancedPattern(int[] parsedPattern, String match) {
        int charSetEnd;
        int charSetStart;
        int tokenType;
        int ip;
        int ip2;
        int minRepetition;
        int maxRepetition;
        int matched;
        int tokenType2;
        int ip3 = 0;
        int LP = parsedPattern.length;
        int LM = match.length();
        int charSetStart2 = 0;
        int charSetEnd2 = 0;
        int im = 0;
        while (ip3 < LP) {
            int patternChar = parsedPattern[ip3];
            if (patternChar == -4) {
                ip = ip3 + 1;
                charSetStart = charSetStart2;
                charSetEnd = charSetEnd2;
                tokenType = 1;
            } else if (patternChar == -2 || patternChar == -1) {
                if (patternChar == -1) {
                    tokenType2 = 2;
                } else {
                    tokenType2 = 3;
                }
                int charSetStart3 = ip3 + 1;
                do {
                    ip3++;
                    if (ip3 >= LP) {
                        break;
                    }
                } while (parsedPattern[ip3] != -3);
                int charSetEnd3 = ip3 - 1;
                ip = ip3 + 1;
                charSetStart = charSetStart3;
                charSetEnd = charSetEnd3;
                tokenType = tokenType2;
            } else {
                ip = ip3 + 1;
                charSetStart = ip3;
                charSetEnd = charSetEnd2;
                tokenType = 0;
            }
            if (ip >= LP) {
                ip2 = ip;
                minRepetition = 1;
                maxRepetition = 1;
            } else {
                int patternChar2 = parsedPattern[ip];
                if (patternChar2 == -8) {
                    ip2 = ip + 1;
                    minRepetition = 1;
                    maxRepetition = Integer.MAX_VALUE;
                } else if (patternChar2 == -7) {
                    ip2 = ip + 1;
                    minRepetition = 0;
                    maxRepetition = Integer.MAX_VALUE;
                } else if (patternChar2 != -5) {
                    ip2 = ip;
                    minRepetition = 1;
                    maxRepetition = 1;
                } else {
                    int ip4 = ip + 1;
                    int minRepetition2 = parsedPattern[ip4];
                    int ip5 = ip4 + 1;
                    ip2 = ip5 + 2;
                    maxRepetition = parsedPattern[ip5];
                    minRepetition = minRepetition2;
                }
            }
            if (minRepetition > maxRepetition || (matched = matchChars(match, im, LM, tokenType, minRepetition, maxRepetition, parsedPattern, charSetStart, charSetEnd)) == -1) {
                return false;
            }
            im += matched;
            charSetStart2 = charSetStart;
            charSetEnd2 = charSetEnd;
            ip3 = ip2;
        }
        return ip3 >= LP && im >= LM;
    }

    private static int matchChars(String match, int im, int lm, int tokenType, int minRepetition, int maxRepetition, int[] parsedPattern, int tokenStart, int tokenEnd) {
        int matched = 0;
        while (matched < maxRepetition && matchChar(match, im + matched, lm, tokenType, parsedPattern, tokenStart, tokenEnd)) {
            matched++;
        }
        if (matched < minRepetition) {
            return -1;
        }
        return matched;
    }

    private static boolean matchChar(String match, int im, int lm, int tokenType, int[] parsedPattern, int tokenStart, int tokenEnd) {
        if (im >= lm) {
            return false;
        }
        if (tokenType != 0) {
            if (tokenType == 1) {
                return true;
            }
            if (tokenType == 2) {
                for (int i = tokenStart; i < tokenEnd; i += 2) {
                    char matchChar = match.charAt(im);
                    if (matchChar >= parsedPattern[i] && matchChar <= parsedPattern[i + 1]) {
                        return true;
                    }
                }
                return false;
            } else if (tokenType != 3) {
                return false;
            } else {
                for (int i2 = tokenStart; i2 < tokenEnd; i2 += 2) {
                    char matchChar2 = match.charAt(im);
                    if (matchChar2 >= parsedPattern[i2] && matchChar2 <= parsedPattern[i2 + 1]) {
                        return false;
                    }
                }
                return true;
            }
        } else if (match.charAt(im) == parsedPattern[tokenStart]) {
            return true;
        } else {
            return false;
        }
    }
}
