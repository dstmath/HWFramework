package android.os;

import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;
import java.util.Arrays;

public class PatternMatcher implements Parcelable {
    public static final Parcelable.Creator<PatternMatcher> CREATOR = new Parcelable.Creator<PatternMatcher>() {
        public PatternMatcher createFromParcel(Parcel source) {
            return new PatternMatcher(source);
        }

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
        switch (this.mType) {
            case 0:
                type = "LITERAL: ";
                break;
            case 1:
                type = "PREFIX: ";
                break;
            case 2:
                type = "GLOB: ";
                break;
            case 3:
                type = "ADVANCED: ";
                break;
        }
        return "PatternMatcher{" + type + this.mPattern + "}";
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mPattern);
        proto.write(1159641169922L, this.mType);
        proto.end(token);
    }

    public int describeContents() {
        return 0;
    }

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

    static boolean matchGlobPattern(String pattern, String match) {
        int NP = pattern.length();
        boolean z = false;
        if (NP <= 0) {
            if (match.length() <= 0) {
                z = true;
            }
            return z;
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
                    while (match.charAt(im) == c) {
                        im++;
                        if (im >= NM) {
                            break;
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
                    while (match.charAt(im) != nextChar2) {
                        im++;
                        if (im >= NM) {
                            break;
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x014e  */
    static synchronized int[] parseAndVerifyAdvancedPattern(String pattern) {
        int[] copyOf;
        int it;
        int it2;
        int rangeMax;
        int parsedRange;
        int it3;
        String str = pattern;
        synchronized (PatternMatcher.class) {
            int LP = pattern.length();
            int inRange = 0;
            boolean inSet = false;
            int it4 = 0;
            int ip = 0;
            boolean inCharClass = false;
            while (ip < LP) {
                if (it4 <= 2045) {
                    int charAt = str.charAt(ip);
                    boolean addToParsedPattern = false;
                    if (charAt != 46) {
                        if (charAt != 123) {
                            if (charAt != 125) {
                                switch (charAt) {
                                    case 42:
                                        if (!inSet) {
                                            if (it4 != 0 && !isParsedModifier(sParsedPatternScratch[it4 - 1])) {
                                                it = it4 + 1;
                                                sParsedPatternScratch[it4] = -7;
                                                break;
                                            } else {
                                                throw new IllegalArgumentException("Modifier must follow a token.");
                                            }
                                        }
                                        break;
                                    case 43:
                                        if (!inSet) {
                                            if (it4 != 0 && !isParsedModifier(sParsedPatternScratch[it4 - 1])) {
                                                it = it4 + 1;
                                                sParsedPatternScratch[it4] = -8;
                                                break;
                                            } else {
                                                throw new IllegalArgumentException("Modifier must follow a token.");
                                            }
                                        }
                                        break;
                                    default:
                                        switch (charAt) {
                                            case 91:
                                                if (inSet) {
                                                    addToParsedPattern = true;
                                                } else {
                                                    if (str.charAt(ip + 1) == '^') {
                                                        sParsedPatternScratch[it4] = -2;
                                                        ip++;
                                                        it4++;
                                                    } else {
                                                        sParsedPatternScratch[it4] = -1;
                                                        it4++;
                                                    }
                                                    ip++;
                                                    inSet = true;
                                                    continue;
                                                    continue;
                                                }
                                            case 92:
                                                if (ip + 1 < LP) {
                                                    ip++;
                                                    charAt = str.charAt(ip);
                                                    addToParsedPattern = true;
                                                } else {
                                                    throw new IllegalArgumentException("Escape found at end of pattern!");
                                                }
                                            case 93:
                                                if (inSet) {
                                                    int parsedToken = sParsedPatternScratch[it4 - 1];
                                                    if (parsedToken != -1 && parsedToken != -2) {
                                                        it = it4 + 1;
                                                        sParsedPatternScratch[it4] = -3;
                                                        inCharClass = false;
                                                        inSet = false;
                                                        break;
                                                    } else {
                                                        throw new IllegalArgumentException("You must define characters in a set.");
                                                    }
                                                } else {
                                                    addToParsedPattern = true;
                                                }
                                                break;
                                            default:
                                                addToParsedPattern = true;
                                        }
                                        break;
                                }
                            } else if (inRange != 0) {
                                it = it4 + 1;
                                sParsedPatternScratch[it4] = -6;
                                it3 = 0;
                            }
                        } else if (!inSet) {
                            if (it4 == 0 || isParsedModifier(sParsedPatternScratch[it4 - 1])) {
                                throw new IllegalArgumentException("Modifier must follow a token.");
                            }
                            it = it4 + 1;
                            sParsedPatternScratch[it4] = -5;
                            ip++;
                            it3 = 1;
                        }
                        inRange = it3;
                        int ip2 = ip;
                        boolean inCharClass2 = inCharClass;
                        if (inSet) {
                            if (inCharClass2) {
                                it2 = it + 1;
                                sParsedPatternScratch[it] = charAt;
                                inCharClass = false;
                            } else if (ip2 + 2 >= LP || str.charAt(ip2 + 1) != '-' || str.charAt(ip2 + 2) == ']') {
                                int it5 = it + 1;
                                sParsedPatternScratch[it] = charAt;
                                sParsedPatternScratch[it5] = charAt;
                                inCharClass = inCharClass2;
                                it2 = it5 + 1;
                            } else {
                                inCharClass = true;
                                it2 = it + 1;
                                sParsedPatternScratch[it] = charAt;
                                ip2++;
                            }
                        } else if (inRange != 0) {
                            int endOfSet = str.indexOf(125, ip2);
                            if (endOfSet >= 0) {
                                String rangeString = str.substring(ip2, endOfSet);
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
                                    int it6 = it + 1;
                                    try {
                                        sParsedPatternScratch[it] = parsedRange;
                                        int it7 = it6 + 1;
                                        try {
                                            sParsedPatternScratch[it6] = rangeMax;
                                            it4 = it7;
                                            boolean z = inCharClass2;
                                            ip = endOfSet;
                                            inCharClass = z;
                                        } catch (NumberFormatException e2) {
                                            e = e2;
                                            int i = it7;
                                            throw new IllegalArgumentException("Range number format incorrect", e);
                                        }
                                    } catch (NumberFormatException e3) {
                                        e = e3;
                                        int i2 = it6;
                                        throw new IllegalArgumentException("Range number format incorrect", e);
                                    }
                                } else {
                                    int i3 = parsedRange;
                                    throw new IllegalArgumentException("Range quantifier minimum is greater than maximum");
                                }
                            } else {
                                throw new IllegalArgumentException("Range not ended with '}'");
                            }
                        } else if (addToParsedPattern) {
                            it2 = it + 1;
                            sParsedPatternScratch[it] = charAt;
                            inCharClass = inCharClass2;
                        } else {
                            inCharClass = inCharClass2;
                            it2 = it;
                        }
                        ip = ip2 + 1;
                        it4 = it2;
                    } else if (!inSet) {
                        it = it4 + 1;
                        sParsedPatternScratch[it4] = -4;
                        int ip22 = ip;
                        boolean inCharClass22 = inCharClass;
                        if (inSet) {
                        }
                        ip = ip22 + 1;
                        it4 = it2;
                    }
                    it = it4;
                    int ip222 = ip;
                    boolean inCharClass222 = inCharClass;
                    if (inSet) {
                    }
                    ip = ip222 + 1;
                    it4 = it2;
                } else {
                    throw new IllegalArgumentException("Pattern is too large!");
                }
            }
            if (!inSet) {
                copyOf = Arrays.copyOf(sParsedPatternScratch, it4);
            } else {
                throw new IllegalArgumentException("Set was not terminated!");
            }
        }
        return copyOf;
    }

    private static boolean isParsedModifier(int parsedChar) {
        return parsedChar == -8 || parsedChar == -7 || parsedChar == -6 || parsedChar == -5;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0071 A[SYNTHETIC] */
    static boolean matchAdvancedPattern(int[] parsedPattern, String match) {
        int tokenType;
        int ip;
        int ip2;
        int maxRepetition;
        int i;
        int minRepetition;
        int[] iArr = parsedPattern;
        int LP = iArr.length;
        int LM = match.length();
        int charSetStart = 0;
        boolean z = false;
        int im = 0;
        int ip3 = 0;
        int charSetEnd = 0;
        while (true) {
            int minRepetition2 = 1;
            if (ip3 < LP) {
                int patternChar = iArr[ip3];
                if (patternChar != -4) {
                    switch (patternChar) {
                        case -2:
                        case -1:
                            tokenType = patternChar == -1 ? 2 : 3;
                            charSetStart = ip3 + 1;
                            do {
                                ip3++;
                                if (ip3 < LP) {
                                }
                                charSetEnd = ip3 - 1;
                                ip = ip3 + 1;
                                break;
                            } while (iArr[ip3] != -3);
                            charSetEnd = ip3 - 1;
                            ip = ip3 + 1;
                        default:
                            charSetStart = ip3;
                            tokenType = 0;
                            ip = ip3 + 1;
                            break;
                    }
                } else {
                    tokenType = 1;
                    ip = ip3 + 1;
                }
                int charSetEnd2 = charSetEnd;
                int charSetStart2 = charSetStart;
                int tokenType2 = tokenType;
                if (ip >= LP) {
                    i = 1;
                } else {
                    patternChar = iArr[ip];
                    if (patternChar != -5) {
                        switch (patternChar) {
                            case -8:
                                minRepetition2 = 1;
                                i = Integer.MAX_VALUE;
                                ip++;
                                break;
                            case -7:
                                minRepetition2 = 0;
                                i = Integer.MAX_VALUE;
                                ip++;
                                break;
                            default:
                                i = 1;
                                break;
                        }
                    } else {
                        int ip4 = ip + 1;
                        int minRepetition3 = iArr[ip4];
                        int ip5 = ip4 + 1;
                        minRepetition2 = minRepetition3;
                        ip2 = ip5 + 2;
                        maxRepetition = iArr[ip5];
                        minRepetition = minRepetition2;
                        if (minRepetition <= maxRepetition) {
                            return false;
                        }
                        int i2 = minRepetition;
                        int i3 = maxRepetition;
                        int matched = matchChars(match, im, LM, tokenType2, minRepetition, maxRepetition, iArr, charSetStart2, charSetEnd2);
                        if (matched == -1) {
                            return false;
                        }
                        im += matched;
                        charSetStart = charSetStart2;
                        charSetEnd = charSetEnd2;
                        ip3 = ip2;
                    }
                }
                maxRepetition = i;
                ip2 = ip;
                minRepetition = minRepetition2;
                if (minRepetition <= maxRepetition) {
                }
            } else {
                if (ip3 >= LP && im >= LM) {
                    z = true;
                }
                return z;
            }
        }
    }

    private static int matchChars(String match, int im, int lm, int tokenType, int minRepetition, int maxRepetition, int[] parsedPattern, int tokenStart, int tokenEnd) {
        int matched = 0;
        while (matched < maxRepetition) {
            if (!matchChar(match, im + matched, lm, tokenType, parsedPattern, tokenStart, tokenEnd)) {
                break;
            }
            matched++;
        }
        if (matched < minRepetition) {
            return -1;
        }
        return matched;
    }

    private static boolean matchChar(String match, int im, int lm, int tokenType, int[] parsedPattern, int tokenStart, int tokenEnd) {
        boolean z = false;
        if (im >= lm) {
            return false;
        }
        switch (tokenType) {
            case 0:
                if (match.charAt(im) == parsedPattern[tokenStart]) {
                    z = true;
                }
                return z;
            case 1:
                return true;
            case 2:
                for (int i = tokenStart; i < tokenEnd; i += 2) {
                    char matchChar = match.charAt(im);
                    if (matchChar >= parsedPattern[i] && matchChar <= parsedPattern[i + 1]) {
                        return true;
                    }
                }
                return false;
            case 3:
                for (int i2 = tokenStart; i2 < tokenEnd; i2 += 2) {
                    char matchChar2 = match.charAt(im);
                    if (matchChar2 >= parsedPattern[i2] && matchChar2 <= parsedPattern[i2 + 1]) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }
}
