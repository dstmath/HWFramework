package android.os;

import android.os.Parcelable.Creator;
import java.util.Arrays;

public class PatternMatcher implements Parcelable {
    public static final Creator<PatternMatcher> CREATOR = new Creator<PatternMatcher>() {
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
        boolean z = true;
        int NP = pattern.length();
        if (NP <= 0) {
            if (match.length() > 0) {
                z = false;
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
                    ip++;
                    nextChar = pattern.charAt(ip);
                    if (nextChar == '\\') {
                        ip++;
                        nextChar = ip < NP ? pattern.charAt(ip) : 0;
                    }
                    while (match.charAt(im) != nextChar) {
                        im++;
                        if (im >= NM) {
                            break;
                        }
                    }
                    if (im == NM) {
                        return false;
                    }
                    ip++;
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
        } else {
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:81:0x0195  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0195  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0195  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0195  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static synchronized int[] parseAndVerifyAdvancedPattern(String pattern) {
        NumberFormatException e;
        int[] copyOf;
        synchronized (PatternMatcher.class) {
            int ip = 0;
            int LP = pattern.length();
            boolean inSet = false;
            boolean inRange = false;
            boolean inCharClass = false;
            int it = 0;
            while (ip < LP) {
                if (it > 2045) {
                    throw new IllegalArgumentException("Pattern is too large!");
                }
                int it2;
                char c = pattern.charAt(ip);
                boolean addToParsedPattern = false;
                switch (c) {
                    case '*':
                        if (inSet) {
                            it2 = it;
                        } else if (it == 0 || isParsedModifier(sParsedPatternScratch[it - 1])) {
                            throw new IllegalArgumentException("Modifier must follow a token.");
                        } else {
                            it2 = it + 1;
                            sParsedPatternScratch[it] = -7;
                        }
                        it = it2;
                        break;
                    case '+':
                        if (inSet) {
                            it2 = it;
                        } else if (it == 0 || isParsedModifier(sParsedPatternScratch[it - 1])) {
                            throw new IllegalArgumentException("Modifier must follow a token.");
                        } else {
                            it2 = it + 1;
                            sParsedPatternScratch[it] = -8;
                        }
                        it = it2;
                        break;
                    case '.':
                        if (inSet) {
                            it2 = it;
                        } else {
                            it2 = it + 1;
                            sParsedPatternScratch[it] = -4;
                        }
                        it = it2;
                    case '[':
                        if (inSet) {
                            addToParsedPattern = true;
                        } else {
                            if (pattern.charAt(ip + 1) == '^') {
                                it2 = it + 1;
                                sParsedPatternScratch[it] = -2;
                                ip++;
                            } else {
                                it2 = it + 1;
                                sParsedPatternScratch[it] = -1;
                            }
                            ip++;
                            inSet = true;
                            it = it2;
                            continue;
                        }
                    case '\\':
                        if (ip + 1 < LP) {
                            ip++;
                            c = pattern.charAt(ip);
                            addToParsedPattern = true;
                            if (inSet) {
                                if (inCharClass) {
                                    it2 = it + 1;
                                    sParsedPatternScratch[it] = c;
                                    inCharClass = false;
                                } else if (ip + 2 >= LP || pattern.charAt(ip + 1) != '-' || pattern.charAt(ip + 2) == ']') {
                                    it2 = it + 1;
                                    sParsedPatternScratch[it] = c;
                                    it = it2 + 1;
                                    sParsedPatternScratch[it2] = c;
                                    it2 = it;
                                } else {
                                    inCharClass = true;
                                    it2 = it + 1;
                                    sParsedPatternScratch[it] = c;
                                    ip++;
                                }
                            } else if (inRange) {
                                int endOfSet = pattern.indexOf(125, ip);
                                if (endOfSet >= 0) {
                                    int rangeMax;
                                    int rangeMin;
                                    String rangeString = pattern.substring(ip, endOfSet);
                                    int commaIndex = rangeString.indexOf(44);
                                    if (commaIndex < 0) {
                                        try {
                                            int parsedRange = Integer.parseInt(rangeString);
                                            rangeMax = parsedRange;
                                            rangeMin = parsedRange;
                                        } catch (NumberFormatException e2) {
                                            e = e2;
                                            it2 = it;
                                            break;
                                        }
                                    }
                                    rangeMin = Integer.parseInt(rangeString.substring(0, commaIndex));
                                    if (commaIndex == rangeString.length() - 1) {
                                        rangeMax = Integer.MAX_VALUE;
                                    } else {
                                        rangeMax = Integer.parseInt(rangeString.substring(commaIndex + 1));
                                    }
                                    if (rangeMin <= rangeMax) {
                                        it2 = it + 1;
                                        try {
                                            sParsedPatternScratch[it] = rangeMin;
                                            it = it2 + 1;
                                            sParsedPatternScratch[it2] = rangeMax;
                                            ip = endOfSet;
                                            break;
                                        } catch (NumberFormatException e3) {
                                            e = e3;
                                            break;
                                        }
                                    }
                                    throw new IllegalArgumentException("Range quantifier minimum is greater than maximum");
                                }
                                throw new IllegalArgumentException("Range not ended with '}'");
                            } else if (addToParsedPattern) {
                                it2 = it + 1;
                                sParsedPatternScratch[it] = c;
                            } else {
                                it2 = it;
                            }
                            ip++;
                            it = it2;
                            break;
                        }
                        throw new IllegalArgumentException("Escape found at end of pattern!");
                    case ']':
                        if (inSet) {
                            int parsedToken = sParsedPatternScratch[it - 1];
                            if (parsedToken == -1 || parsedToken == -2) {
                                throw new IllegalArgumentException("You must define characters in a set.");
                            }
                            it2 = it + 1;
                            sParsedPatternScratch[it] = -3;
                            inSet = false;
                            inCharClass = false;
                        } else {
                            addToParsedPattern = true;
                            it2 = it;
                        }
                        it = it2;
                        if (inSet) {
                        }
                        ip++;
                        it = it2;
                        break;
                    case '{':
                        if (inSet) {
                            it2 = it;
                        } else if (it == 0 || isParsedModifier(sParsedPatternScratch[it - 1])) {
                            throw new IllegalArgumentException("Modifier must follow a token.");
                        } else {
                            it2 = it + 1;
                            sParsedPatternScratch[it] = -5;
                            ip++;
                            inRange = true;
                        }
                        it = it2;
                        if (inSet) {
                        }
                        ip++;
                        it = it2;
                        break;
                    case '}':
                        if (inRange) {
                            it2 = it + 1;
                            sParsedPatternScratch[it] = -6;
                            inRange = false;
                        } else {
                            it2 = it;
                        }
                        it = it2;
                        if (inSet) {
                        }
                        ip++;
                        it = it2;
                        break;
                    default:
                        addToParsedPattern = true;
                }
                if (inSet) {
                }
                ip++;
                it = it2;
            }
            if (inSet) {
                throw new IllegalArgumentException("Set was not terminated!");
            }
            copyOf = Arrays.copyOf(sParsedPatternScratch, it);
        }
        return copyOf;
        throw new IllegalArgumentException("Range number format incorrect", e);
    }

    private static boolean isParsedModifier(int parsedChar) {
        if (parsedChar == -8 || parsedChar == -7 || parsedChar == -6 || parsedChar == -5) {
            return true;
        }
        return false;
    }

    static boolean matchAdvancedPattern(int[] parsedPattern, String match) {
        int ip = 0;
        int im = 0;
        int LP = parsedPattern.length;
        int LM = match.length();
        int charSetStart = 0;
        int charSetEnd = 0;
        while (ip < LP) {
            int tokenType;
            int minRepetition;
            int maxRepetition;
            int patternChar = parsedPattern[ip];
            switch (patternChar) {
                case -4:
                    tokenType = 1;
                    ip++;
                    break;
                case -2:
                case -1:
                    if (patternChar == -1) {
                        tokenType = 2;
                    } else {
                        tokenType = 3;
                    }
                    charSetStart = ip + 1;
                    do {
                        ip++;
                        if (ip < LP) {
                        }
                        charSetEnd = ip - 1;
                        ip++;
                        break;
                    } while (parsedPattern[ip] != -3);
                    charSetEnd = ip - 1;
                    ip++;
                default:
                    charSetStart = ip;
                    tokenType = 0;
                    ip++;
                    break;
            }
            if (ip < LP) {
                switch (parsedPattern[ip]) {
                    case -8:
                        minRepetition = 1;
                        maxRepetition = Integer.MAX_VALUE;
                        ip++;
                        break;
                    case -7:
                        minRepetition = 0;
                        maxRepetition = Integer.MAX_VALUE;
                        ip++;
                        break;
                    case -5:
                        ip++;
                        minRepetition = parsedPattern[ip];
                        ip++;
                        maxRepetition = parsedPattern[ip];
                        ip += 2;
                        break;
                    default:
                        maxRepetition = 1;
                        minRepetition = 1;
                        break;
                }
            }
            maxRepetition = 1;
            minRepetition = 1;
            if (minRepetition > maxRepetition) {
                return false;
            }
            int matched = matchChars(match, im, LM, tokenType, minRepetition, maxRepetition, parsedPattern, charSetStart, charSetEnd);
            if (matched == -1) {
                return false;
            }
            im += matched;
        }
        boolean z = ip >= LP && im >= LM;
        return z;
    }

    private static int matchChars(String match, int im, int lm, int tokenType, int minRepetition, int maxRepetition, int[] parsedPattern, int tokenStart, int tokenEnd) {
        int matched = 0;
        while (matched < maxRepetition) {
            if (!matchChar(match, im + matched, lm, tokenType, parsedPattern, tokenStart, tokenEnd)) {
                break;
            }
            matched++;
        }
        return matched < minRepetition ? -1 : matched;
    }

    private static boolean matchChar(String match, int im, int lm, int tokenType, int[] parsedPattern, int tokenStart, int tokenEnd) {
        boolean z = true;
        if (im >= lm) {
            return false;
        }
        int i;
        char matchChar;
        switch (tokenType) {
            case 0:
                if (match.charAt(im) != parsedPattern[tokenStart]) {
                    z = false;
                }
                return z;
            case 1:
                return true;
            case 2:
                i = tokenStart;
                while (i < tokenEnd) {
                    matchChar = match.charAt(im);
                    if (matchChar >= parsedPattern[i] && matchChar <= parsedPattern[i + 1]) {
                        return true;
                    }
                    i += 2;
                }
                return false;
            case 3:
                i = tokenStart;
                while (i < tokenEnd) {
                    matchChar = match.charAt(im);
                    if (matchChar >= parsedPattern[i] && matchChar <= parsedPattern[i + 1]) {
                        return false;
                    }
                    i += 2;
                }
                return true;
            default:
                return false;
        }
    }
}
