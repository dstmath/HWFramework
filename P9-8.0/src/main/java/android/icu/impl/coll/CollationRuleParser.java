package android.icu.impl.coll;

import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.Normalizer2;
import android.icu.text.PluralRules;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Builder;
import java.text.ParseException;
import java.util.ArrayList;

public final class CollationRuleParser {
    static final /* synthetic */ boolean -assertionsDisabled = (CollationRuleParser.class.desiredAssertionStatus() ^ 1);
    private static final String BEFORE = "[before";
    private static final int OFFSET_SHIFT = 8;
    static final Position[] POSITION_VALUES = Position.values();
    static final char POS_BASE = '⠀';
    static final char POS_LEAD = '￾';
    private static final int STARRED_FLAG = 16;
    private static final int STRENGTH_MASK = 15;
    private static final int UCOL_DEFAULT = -1;
    private static final int UCOL_OFF = 0;
    private static final int UCOL_ON = 1;
    private static final int U_PARSE_CONTEXT_LEN = 16;
    private static final String[] gSpecialReorderCodes = new String[]{"space", "punct", "symbol", "currency", "digit"};
    private static final String[] positions = new String[]{"first tertiary ignorable", "last tertiary ignorable", "first secondary ignorable", "last secondary ignorable", "first primary ignorable", "last primary ignorable", "first variable", "last variable", "first regular", "last regular", "first implicit", "last implicit", "first trailing", "last trailing"};
    private final CollationData baseData;
    private Importer importer;
    private Normalizer2 nfc = Normalizer2.getNFCInstance();
    private Normalizer2 nfd = Normalizer2.getNFDInstance();
    private final StringBuilder rawBuilder = new StringBuilder();
    private int ruleIndex;
    private String rules;
    private CollationSettings settings;
    private Sink sink;

    interface Importer {
        String getRules(String str, String str2);
    }

    static abstract class Sink {
        abstract void addRelation(int i, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3);

        abstract void addReset(int i, CharSequence charSequence);

        Sink() {
        }

        void suppressContractions(UnicodeSet set) {
        }

        void optimize(UnicodeSet set) {
        }
    }

    enum Position {
        FIRST_TERTIARY_IGNORABLE,
        LAST_TERTIARY_IGNORABLE,
        FIRST_SECONDARY_IGNORABLE,
        LAST_SECONDARY_IGNORABLE,
        FIRST_PRIMARY_IGNORABLE,
        LAST_PRIMARY_IGNORABLE,
        FIRST_VARIABLE,
        LAST_VARIABLE,
        FIRST_REGULAR,
        LAST_REGULAR,
        FIRST_IMPLICIT,
        LAST_IMPLICIT,
        FIRST_TRAILING,
        LAST_TRAILING
    }

    CollationRuleParser(CollationData base) {
        this.baseData = base;
    }

    void setSink(Sink sinkAlias) {
        this.sink = sinkAlias;
    }

    void setImporter(Importer importerAlias) {
        this.importer = importerAlias;
    }

    void parse(String ruleString, CollationSettings outSettings) throws ParseException {
        this.settings = outSettings;
        parse(ruleString);
    }

    private void parse(String ruleString) throws ParseException {
        this.rules = ruleString;
        this.ruleIndex = 0;
        while (this.ruleIndex < this.rules.length()) {
            char c = this.rules.charAt(this.ruleIndex);
            if (!PatternProps.isWhiteSpace(c)) {
                switch (c) {
                    case '!':
                        this.ruleIndex++;
                        break;
                    case '#':
                        this.ruleIndex = skipComment(this.ruleIndex + 1);
                        break;
                    case '&':
                        parseRuleChain();
                        break;
                    case '@':
                        this.settings.setFlag(2048, true);
                        this.ruleIndex++;
                        break;
                    case '[':
                        parseSetting();
                        break;
                    default:
                        setParseError("expected a reset or setting or comment");
                        break;
                }
            }
            this.ruleIndex++;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x002e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseRuleChain() throws ParseException {
        int resetStrength = parseResetAndPosition();
        boolean isFirstRelation = true;
        while (true) {
            int result = parseRelationOperator();
            if (result >= 0) {
                int strength = result & 15;
                if (resetStrength < 15) {
                    if (isFirstRelation) {
                        if (strength != resetStrength) {
                            setParseError("reset-before strength differs from its first relation");
                            return;
                        }
                    } else if (strength < resetStrength) {
                        setParseError("reset-before strength followed by a stronger relation");
                        return;
                    }
                }
                int i = this.ruleIndex + (result >> 8);
                if ((result & 16) == 0) {
                    parseRelationStrings(strength, i);
                } else {
                    parseStarredCharacters(strength, i);
                }
                isFirstRelation = false;
            } else if (this.ruleIndex < this.rules.length() && this.rules.charAt(this.ruleIndex) == '#') {
                this.ruleIndex = skipComment(this.ruleIndex + 1);
            } else if (isFirstRelation) {
                setParseError("reset not followed by a relation");
            }
        }
        if (isFirstRelation) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0075  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseResetAndPosition() throws ParseException {
        int resetStrength;
        int i = skipWhiteSpace(this.ruleIndex + 1);
        if (this.rules.regionMatches(i, BEFORE, 0, BEFORE.length())) {
            int j = i + BEFORE.length();
            if (j < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(j))) {
                j = skipWhiteSpace(j + 1);
                if (j + 1 < this.rules.length()) {
                    char c = this.rules.charAt(j);
                    if ('1' <= c && c <= '3' && this.rules.charAt(j + 1) == ']') {
                        resetStrength = (c - 49) + 0;
                        i = skipWhiteSpace(j + 2);
                        if (i < this.rules.length()) {
                            setParseError("reset without position");
                            return -1;
                        }
                        if (this.rules.charAt(i) == '[') {
                            i = parseSpecialPosition(i, this.rawBuilder);
                        } else {
                            i = parseTailoringString(i, this.rawBuilder);
                        }
                        try {
                            this.sink.addReset(resetStrength, this.rawBuilder);
                            this.ruleIndex = i;
                            return resetStrength;
                        } catch (Exception e) {
                            setParseError("adding reset failed", e);
                            return -1;
                        }
                    }
                }
            }
        }
        resetStrength = 15;
        if (i < this.rules.length()) {
        }
    }

    private int parseRelationOperator() {
        this.ruleIndex = skipWhiteSpace(this.ruleIndex);
        if (this.ruleIndex >= this.rules.length()) {
            return -1;
        }
        int strength;
        int i = this.ruleIndex;
        int i2 = i + 1;
        switch (this.rules.charAt(i)) {
            case ',':
                strength = 2;
                i = i2;
                break;
            case ';':
                strength = 1;
                i = i2;
                break;
            case '<':
                if (i2 >= this.rules.length() || this.rules.charAt(i2) != '<') {
                    strength = 0;
                    i = i2;
                } else {
                    i = i2 + 1;
                    if (i >= this.rules.length() || this.rules.charAt(i) != '<') {
                        strength = 1;
                    } else {
                        i++;
                        if (i >= this.rules.length() || this.rules.charAt(i) != '<') {
                            strength = 2;
                        } else {
                            i++;
                            strength = 3;
                        }
                    }
                }
                if (i < this.rules.length() && this.rules.charAt(i) == '*') {
                    i++;
                    strength |= 16;
                    break;
                }
                break;
            case '=':
                strength = 15;
                if (i2 < this.rules.length() && this.rules.charAt(i2) == '*') {
                    i = i2 + 1;
                    strength = 31;
                    break;
                }
                i = i2;
                break;
            default:
                return -1;
        }
        return ((i - this.ruleIndex) << 8) | strength;
    }

    private void parseRelationStrings(int strength, int i) throws ParseException {
        String prefix = "";
        CharSequence extension = "";
        i = parseTailoringString(i, this.rawBuilder);
        char next = i < this.rules.length() ? this.rules.charAt(i) : 0;
        if (next == '|') {
            prefix = this.rawBuilder.toString();
            i = parseTailoringString(i + 1, this.rawBuilder);
            next = i < this.rules.length() ? this.rules.charAt(i) : 0;
        }
        if (next == '/') {
            StringBuilder extBuilder = new StringBuilder();
            i = parseTailoringString(i + 1, extBuilder);
            extension = extBuilder;
        }
        if (prefix.length() != 0) {
            int prefix0 = prefix.codePointAt(0);
            int c = this.rawBuilder.codePointAt(0);
            if (!(this.nfc.hasBoundaryBefore(prefix0) && (this.nfc.hasBoundaryBefore(c) ^ 1) == 0)) {
                setParseError("in 'prefix|str', prefix and str must each start with an NFC boundary");
                return;
            }
        }
        try {
            this.sink.addRelation(strength, prefix, this.rawBuilder, extension);
            this.ruleIndex = i;
        } catch (Exception e) {
            setParseError("adding relation failed", e);
        }
    }

    private void parseStarredCharacters(int strength, int i) throws ParseException {
        String empty = "";
        i = parseString(skipWhiteSpace(i), this.rawBuilder);
        if (this.rawBuilder.length() == 0) {
            setParseError("missing starred-relation string");
            return;
        }
        int prev = -1;
        int j = 0;
        while (true) {
            int c;
            if (j >= this.rawBuilder.length()) {
                if (i < this.rules.length() && this.rules.charAt(i) == '-') {
                    if (prev >= 0) {
                        i = parseString(i + 1, this.rawBuilder);
                        if (this.rawBuilder.length() != 0) {
                            c = this.rawBuilder.codePointAt(0);
                            if (c >= prev) {
                                while (true) {
                                    prev++;
                                    if (prev > c) {
                                        prev = -1;
                                        j = Character.charCount(c);
                                        break;
                                    } else if (!this.nfd.isInert(prev)) {
                                        setParseError("starred-relation string range is not all NFD-inert");
                                        return;
                                    } else if (isSurrogate(prev)) {
                                        setParseError("starred-relation string range contains a surrogate");
                                        return;
                                    } else if (UCharacter.REPLACEMENT_CHAR > prev || prev > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                                        try {
                                            this.sink.addRelation(strength, empty, UTF16.valueOf(prev), empty);
                                        } catch (Exception e) {
                                            setParseError("adding relation failed", e);
                                            return;
                                        }
                                    } else {
                                        setParseError("starred-relation string range contains U+FFFD, U+FFFE or U+FFFF");
                                        return;
                                    }
                                }
                            }
                            setParseError("range start greater than end in starred-relation string");
                            return;
                        }
                        setParseError("range without end in starred-relation string");
                        return;
                    }
                    setParseError("range without start in starred-relation string");
                    return;
                }
                this.ruleIndex = skipWhiteSpace(i);
            } else {
                c = this.rawBuilder.codePointAt(j);
                if (this.nfd.isInert(c)) {
                    try {
                        this.sink.addRelation(strength, empty, UTF16.valueOf(c), empty);
                        j += Character.charCount(c);
                        prev = c;
                    } catch (Exception e2) {
                        setParseError("adding relation failed", e2);
                        return;
                    }
                }
                setParseError("starred-relation string is not all NFD-inert");
                return;
            }
        }
        this.ruleIndex = skipWhiteSpace(i);
    }

    private int parseTailoringString(int i, StringBuilder raw) throws ParseException {
        i = parseString(skipWhiteSpace(i), raw);
        if (raw.length() == 0) {
            setParseError("missing relation string");
        }
        return skipWhiteSpace(i);
    }

    private int parseString(int i, StringBuilder raw) throws ParseException {
        raw.setLength(0);
        while (i < this.rules.length()) {
            int i2 = i + 1;
            char c = this.rules.charAt(i);
            if (isSyntaxChar(c)) {
                if (c != PatternTokenizer.SINGLE_QUOTE) {
                    if (c != PatternTokenizer.BACK_SLASH) {
                        i = i2 - 1;
                        break;
                    } else if (i2 == this.rules.length()) {
                        setParseError("backslash escape at the end of the rule string");
                        return i2;
                    } else {
                        int cp = this.rules.codePointAt(i2);
                        raw.appendCodePoint(cp);
                        i = i2 + Character.charCount(cp);
                    }
                } else if (i2 >= this.rules.length() || this.rules.charAt(i2) != PatternTokenizer.SINGLE_QUOTE) {
                    i = i2;
                    while (i != this.rules.length()) {
                        i2 = i + 1;
                        c = this.rules.charAt(i);
                        if (c != PatternTokenizer.SINGLE_QUOTE) {
                            i = i2;
                        } else if (i2 >= this.rules.length() || this.rules.charAt(i2) != PatternTokenizer.SINGLE_QUOTE) {
                            i = i2;
                        } else {
                            i = i2 + 1;
                        }
                        raw.append(c);
                    }
                    setParseError("quoted literal text missing terminating apostrophe");
                    return i;
                } else {
                    raw.append(PatternTokenizer.SINGLE_QUOTE);
                    i = i2 + 1;
                }
            } else if (PatternProps.isWhiteSpace(c)) {
                i = i2 - 1;
                break;
            } else {
                raw.append(c);
                i = i2;
            }
        }
        int j = 0;
        while (j < raw.length()) {
            int c2 = raw.codePointAt(j);
            if (isSurrogate(c2)) {
                setParseError("string contains an unpaired surrogate");
                return i;
            } else if (UCharacter.REPLACEMENT_CHAR > c2 || c2 > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                j += Character.charCount(c2);
            } else {
                setParseError("string contains U+FFFD, U+FFFE or U+FFFF");
                return i;
            }
        }
        return i;
    }

    private static final boolean isSurrogate(int c) {
        return (c & -2048) == 55296;
    }

    private int parseSpecialPosition(int i, StringBuilder str) throws ParseException {
        int j = readWords(i + 1, this.rawBuilder);
        if (j > i && this.rules.charAt(j) == ']' && this.rawBuilder.length() != 0) {
            j++;
            String raw = this.rawBuilder.toString();
            str.setLength(0);
            for (int pos = 0; pos < positions.length; pos++) {
                if (raw.equals(positions[pos])) {
                    str.append(POS_LEAD).append((char) (pos + 10240));
                    return j;
                }
            }
            if (raw.equals("top")) {
                str.append(POS_LEAD).append((char) (Position.LAST_REGULAR.ordinal() + 10240));
                return j;
            } else if (raw.equals("variable top")) {
                str.append(POS_LEAD).append((char) (Position.LAST_VARIABLE.ordinal() + 10240));
                return j;
            }
        }
        setParseError("not a valid special reset position");
        return i;
    }

    private void parseSetting() throws ParseException {
        int i = this.ruleIndex + 1;
        int j = readWords(i, this.rawBuilder);
        if (j <= i || this.rawBuilder.length() == 0) {
            setParseError("expected a setting/option at '['");
        }
        String raw = this.rawBuilder.toString();
        if (this.rules.charAt(j) == ']') {
            j++;
            if (raw.startsWith("reorder") && (raw.length() == 7 || raw.charAt(7) == ' ')) {
                parseReordering(raw);
                this.ruleIndex = j;
                return;
            } else if (raw.equals("backwards 2")) {
                this.settings.setFlag(2048, true);
                this.ruleIndex = j;
                return;
            } else {
                String v;
                int valueIndex = raw.lastIndexOf(32);
                if (valueIndex >= 0) {
                    v = raw.substring(valueIndex + 1);
                    raw = raw.substring(0, valueIndex);
                } else {
                    v = "";
                }
                int value;
                if (raw.equals("strength") && v.length() == 1) {
                    value = -1;
                    char c = v.charAt(0);
                    if ('1' <= c && c <= '4') {
                        value = (c - 49) + 0;
                    } else if (c == 'I') {
                        value = 15;
                    }
                    if (value != -1) {
                        this.settings.setStrength(value);
                        this.ruleIndex = j;
                        return;
                    }
                } else if (raw.equals("alternate")) {
                    value = -1;
                    if (v.equals("non-ignorable")) {
                        value = 0;
                    } else if (v.equals("shifted")) {
                        value = 1;
                    }
                    if (value != -1) {
                        this.settings.setAlternateHandlingShifted(value > 0);
                        this.ruleIndex = j;
                        return;
                    }
                } else if (raw.equals("maxVariable")) {
                    value = -1;
                    if (v.equals("space")) {
                        value = 0;
                    } else if (v.equals("punct")) {
                        value = 1;
                    } else if (v.equals("symbol")) {
                        value = 2;
                    } else if (v.equals("currency")) {
                        value = 3;
                    }
                    if (value != -1) {
                        this.settings.setMaxVariable(value, 0);
                        this.settings.variableTop = this.baseData.getLastPrimaryForGroup(value + 4096);
                        if (-assertionsDisabled || this.settings.variableTop != 0) {
                            this.ruleIndex = j;
                            return;
                        }
                        throw new AssertionError();
                    }
                } else if (raw.equals("caseFirst")) {
                    value = -1;
                    if (v.equals("off")) {
                        value = 0;
                    } else if (v.equals("lower")) {
                        value = 512;
                    } else if (v.equals("upper")) {
                        value = 768;
                    }
                    if (value != -1) {
                        this.settings.setCaseFirst(value);
                        this.ruleIndex = j;
                        return;
                    }
                } else if (raw.equals("caseLevel")) {
                    value = getOnOffValue(v);
                    if (value != -1) {
                        this.settings.setFlag(1024, value > 0);
                        this.ruleIndex = j;
                        return;
                    }
                } else if (raw.equals("normalization")) {
                    value = getOnOffValue(v);
                    if (value != -1) {
                        this.settings.setFlag(1, value > 0);
                        this.ruleIndex = j;
                        return;
                    }
                } else if (raw.equals("numericOrdering")) {
                    value = getOnOffValue(v);
                    if (value != -1) {
                        this.settings.setFlag(2, value > 0);
                        this.ruleIndex = j;
                        return;
                    }
                } else if (raw.equals("hiraganaQ")) {
                    value = getOnOffValue(v);
                    if (value != -1) {
                        if (value == 1) {
                            setParseError("[hiraganaQ on] is not supported");
                        }
                        this.ruleIndex = j;
                        return;
                    }
                } else if (raw.equals("import")) {
                    try {
                        ULocale localeID = new Builder().setLanguageTag(v).build();
                        String baseID = localeID.getBaseName();
                        String collationType = localeID.getKeywordValue("collation");
                        if (this.importer == null) {
                            setParseError("[import langTag] is not supported");
                        } else {
                            try {
                                Importer importer = this.importer;
                                if (collationType == null) {
                                    collationType = "standard";
                                }
                                String importedRules = importer.getRules(baseID, collationType);
                                String outerRules = this.rules;
                                int outerRuleIndex = this.ruleIndex;
                                try {
                                    parse(importedRules);
                                } catch (Exception e) {
                                    this.ruleIndex = outerRuleIndex;
                                    setParseError("parsing imported rules failed", e);
                                }
                                this.rules = outerRules;
                                this.ruleIndex = j;
                            } catch (Exception e2) {
                                setParseError("[import langTag] failed", e2);
                                return;
                            }
                        }
                        return;
                    } catch (Exception e22) {
                        setParseError("expected language tag in [import langTag]", e22);
                        return;
                    }
                }
            }
        } else if (this.rules.charAt(j) == '[') {
            UnicodeSet set = new UnicodeSet();
            j = parseUnicodeSet(j, set);
            if (raw.equals("optimize")) {
                try {
                    this.sink.optimize(set);
                } catch (Exception e222) {
                    setParseError("[optimize set] failed", e222);
                }
                this.ruleIndex = j;
                return;
            } else if (raw.equals("suppressContractions")) {
                try {
                    this.sink.suppressContractions(set);
                } catch (Exception e2222) {
                    setParseError("[suppressContractions set] failed", e2222);
                }
                this.ruleIndex = j;
                return;
            }
        }
        setParseError("not a valid setting/option");
    }

    private void parseReordering(CharSequence raw) throws ParseException {
        int i = 7;
        if (7 == raw.length()) {
            this.settings.resetReordering();
            return;
        }
        ArrayList<Integer> reorderCodes = new ArrayList();
        while (i < raw.length()) {
            i++;
            int limit = i;
            while (limit < raw.length() && raw.charAt(limit) != ' ') {
                limit++;
            }
            int code = getReorderCode(raw.subSequence(i, limit).toString());
            if (code < 0) {
                setParseError("unknown script or reorder code");
                return;
            } else {
                reorderCodes.add(Integer.valueOf(code));
                i = limit;
            }
        }
        if (reorderCodes.isEmpty()) {
            this.settings.resetReordering();
        } else {
            int[] codes = new int[reorderCodes.size()];
            int j = 0;
            for (Integer code2 : reorderCodes) {
                int j2 = j + 1;
                codes[j] = code2.intValue();
                j = j2;
            }
            this.settings.setReordering(this.baseData, codes);
        }
    }

    public static int getReorderCode(String word) {
        for (int i = 0; i < gSpecialReorderCodes.length; i++) {
            if (word.equalsIgnoreCase(gSpecialReorderCodes[i])) {
                return i + 4096;
            }
        }
        try {
            int script = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, word);
            if (script >= 0) {
                return script;
            }
        } catch (IllegalIcuArgumentException e) {
        }
        if (word.equalsIgnoreCase("others")) {
            return 103;
        }
        return -1;
    }

    private static int getOnOffValue(String s) {
        if (s.equals("on")) {
            return 1;
        }
        if (s.equals("off")) {
            return 0;
        }
        return -1;
    }

    private int parseUnicodeSet(int i, UnicodeSet set) throws ParseException {
        int level = 0;
        int j = i;
        while (j != this.rules.length()) {
            int j2 = j + 1;
            char c = this.rules.charAt(j);
            if (c == '[') {
                level++;
            } else if (c == ']') {
                level--;
                if (level == 0) {
                    try {
                        set.applyPattern(this.rules.substring(i, j2));
                    } catch (Exception e) {
                        setParseError("not a valid UnicodeSet pattern: " + e.getMessage());
                    }
                    j = skipWhiteSpace(j2);
                    if (j != this.rules.length() && this.rules.charAt(j) == ']') {
                        return j + 1;
                    }
                    setParseError("missing option-terminating ']' after UnicodeSet pattern");
                    return j;
                }
            } else {
                continue;
            }
            j = j2;
        }
        setParseError("unbalanced UnicodeSet pattern brackets");
        return j;
    }

    private int readWords(int i, StringBuilder raw) {
        raw.setLength(0);
        i = skipWhiteSpace(i);
        while (i < this.rules.length()) {
            char c = this.rules.charAt(i);
            if (!isSyntaxChar(c) || c == '-' || c == '_') {
                if (PatternProps.isWhiteSpace(c)) {
                    raw.append(' ');
                    i = skipWhiteSpace(i + 1);
                } else {
                    raw.append(c);
                    i++;
                }
            } else if (raw.length() == 0) {
                return i;
            } else {
                int lastIndex = raw.length() - 1;
                if (raw.charAt(lastIndex) == ' ') {
                    raw.setLength(lastIndex);
                }
                return i;
            }
        }
        return 0;
    }

    private int skipComment(int i) {
        while (i < this.rules.length()) {
            int i2 = i + 1;
            char c = this.rules.charAt(i);
            if (c == 10 || c == 12 || c == 13 || c == 133 || c == 8232 || c == 8233) {
                return i2;
            }
            i = i2;
        }
        return i;
    }

    private void setParseError(String reason) throws ParseException {
        throw makeParseException(reason);
    }

    private void setParseError(String reason, Exception e) throws ParseException {
        ParseException newExc = makeParseException(reason + PluralRules.KEYWORD_RULE_SEPARATOR + e.getMessage());
        newExc.initCause(e);
        throw newExc;
    }

    private ParseException makeParseException(String reason) {
        return new ParseException(appendErrorContext(reason), this.ruleIndex);
    }

    private String appendErrorContext(String reason) {
        StringBuilder msg = new StringBuilder(reason);
        msg.append(" at index ").append(this.ruleIndex);
        msg.append(" near \"");
        int start = this.ruleIndex - 15;
        if (start < 0) {
            start = 0;
        } else if (start > 0 && Character.isLowSurrogate(this.rules.charAt(start))) {
            start++;
        }
        msg.append(this.rules, start, this.ruleIndex);
        msg.append('!');
        int length = this.rules.length() - this.ruleIndex;
        if (length >= 16) {
            length = 15;
            if (Character.isHighSurrogate(this.rules.charAt((this.ruleIndex + 15) - 1))) {
                length = 14;
            }
        }
        msg.append(this.rules, this.ruleIndex, this.ruleIndex + length);
        return msg.append('\"').toString();
    }

    private static boolean isSyntaxChar(int c) {
        if (33 > c || c > 126) {
            return false;
        }
        if (c <= 47) {
            return true;
        }
        if (58 <= c && c <= 64) {
            return true;
        }
        if ((91 > c || c > 96) && 123 > c) {
            return false;
        }
        return true;
    }

    private int skipWhiteSpace(int i) {
        while (i < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(i))) {
            i++;
        }
        return i;
    }
}
