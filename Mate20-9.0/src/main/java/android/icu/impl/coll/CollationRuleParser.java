package android.icu.impl.coll;

import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.Normalizer2;
import android.icu.text.PluralRules;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

public final class CollationRuleParser {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
    private static final String[] gSpecialReorderCodes = {"space", "punct", "symbol", "currency", "digit"};
    private static final String[] positions = {"first tertiary ignorable", "last tertiary ignorable", "first secondary ignorable", "last secondary ignorable", "first primary ignorable", "last primary ignorable", "first variable", "last variable", "first regular", "last regular", "first implicit", "last implicit", "first trailing", "last trailing"};
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

    static abstract class Sink {
        /* access modifiers changed from: package-private */
        public abstract void addRelation(int i, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3);

        /* access modifiers changed from: package-private */
        public abstract void addReset(int i, CharSequence charSequence);

        Sink() {
        }

        /* access modifiers changed from: package-private */
        public void suppressContractions(UnicodeSet set) {
        }

        /* access modifiers changed from: package-private */
        public void optimize(UnicodeSet set) {
        }
    }

    CollationRuleParser(CollationData base) {
        this.baseData = base;
    }

    /* access modifiers changed from: package-private */
    public void setSink(Sink sinkAlias) {
        this.sink = sinkAlias;
    }

    /* access modifiers changed from: package-private */
    public void setImporter(Importer importerAlias) {
        this.importer = importerAlias;
    }

    /* access modifiers changed from: package-private */
    public void parse(String ruleString, CollationSettings outSettings) throws ParseException {
        this.settings = outSettings;
        parse(ruleString);
    }

    private void parse(String ruleString) throws ParseException {
        this.rules = ruleString;
        this.ruleIndex = 0;
        while (this.ruleIndex < this.rules.length()) {
            char c = this.rules.charAt(this.ruleIndex);
            if (PatternProps.isWhiteSpace(c)) {
                this.ruleIndex++;
            } else if (c == '!') {
                this.ruleIndex++;
            } else if (c == '#') {
                this.ruleIndex = skipComment(this.ruleIndex + 1);
            } else if (c == '&') {
                parseRuleChain();
            } else if (c == '@') {
                this.settings.setFlag(2048, true);
                this.ruleIndex++;
            } else if (c != '[') {
                setParseError("expected a reset or setting or comment");
            } else {
                parseSetting();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x002e  */
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

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x007d  */
    private int parseResetAndPosition() throws ParseException {
        int resetStrength;
        int i;
        int i2 = skipWhiteSpace(this.ruleIndex + 1);
        if (this.rules.regionMatches(i2, BEFORE, 0, BEFORE.length())) {
            int length = BEFORE.length() + i2;
            int j = length;
            if (length < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(j))) {
                int skipWhiteSpace = skipWhiteSpace(j + 1);
                int j2 = skipWhiteSpace;
                if (skipWhiteSpace + 1 < this.rules.length()) {
                    char charAt = this.rules.charAt(j2);
                    char c = charAt;
                    if ('1' <= charAt && c <= '3' && this.rules.charAt(j2 + 1) == ']') {
                        resetStrength = 0 + (c - '1');
                        i2 = skipWhiteSpace(j2 + 2);
                        int resetStrength2 = resetStrength;
                        if (i2 < this.rules.length()) {
                            setParseError("reset without position");
                            return -1;
                        }
                        if (this.rules.charAt(i2) == '[') {
                            i = parseSpecialPosition(i2, this.rawBuilder);
                        } else {
                            i = parseTailoringString(i2, this.rawBuilder);
                        }
                        try {
                            this.sink.addReset(resetStrength2, this.rawBuilder);
                            this.ruleIndex = i;
                            return resetStrength2;
                        } catch (Exception e) {
                            setParseError("adding reset failed", e);
                            return -1;
                        }
                    }
                }
            }
        }
        resetStrength = 15;
        int resetStrength22 = resetStrength;
        if (i2 < this.rules.length()) {
        }
    }

    private int parseRelationOperator() {
        int strength;
        this.ruleIndex = skipWhiteSpace(this.ruleIndex);
        if (this.ruleIndex >= this.rules.length()) {
            return -1;
        }
        int i = this.ruleIndex;
        int i2 = i + 1;
        int i3 = this.rules.charAt(i);
        if (i3 != 44) {
            switch (i3) {
                case 59:
                    strength = 1;
                    break;
                case 60:
                    if (i2 >= this.rules.length() || this.rules.charAt(i2) != '<') {
                        strength = 0;
                    } else {
                        i2++;
                        if (i2 >= this.rules.length() || this.rules.charAt(i2) != '<') {
                            strength = 1;
                        } else {
                            i2++;
                            if (i2 >= this.rules.length() || this.rules.charAt(i2) != '<') {
                                strength = 2;
                            } else {
                                i2++;
                                strength = 3;
                            }
                        }
                    }
                    if (i2 < this.rules.length() && this.rules.charAt(i2) == '*') {
                        i2++;
                        strength |= 16;
                        break;
                    }
                    break;
                case 61:
                    strength = 15;
                    if (i2 < this.rules.length() && this.rules.charAt(i2) == '*') {
                        i2++;
                        strength = 15 | 16;
                        break;
                    }
                default:
                    return -1;
            }
        } else {
            strength = 2;
        }
        return ((i2 - this.ruleIndex) << 8) | strength;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: java.lang.StringBuilder} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void parseRelationStrings(int strength, int i) throws ParseException {
        String prefix = "";
        CharSequence extension = "";
        int i2 = parseTailoringString(i, this.rawBuilder);
        char next = i2 < this.rules.length() ? this.rules.charAt(i2) : 0;
        if (next == '|') {
            prefix = this.rawBuilder.toString();
            i2 = parseTailoringString(i2 + 1, this.rawBuilder);
            next = i2 < this.rules.length() ? this.rules.charAt(i2) : 0;
        }
        if (next == '/') {
            StringBuilder extBuilder = new StringBuilder();
            i2 = parseTailoringString(i2 + 1, extBuilder);
            extension = extBuilder;
        }
        if (prefix.length() != 0) {
            int prefix0 = prefix.codePointAt(0);
            int c = this.rawBuilder.codePointAt(0);
            if (!this.nfc.hasBoundaryBefore(prefix0) || !this.nfc.hasBoundaryBefore(c)) {
                setParseError("in 'prefix|str', prefix and str must each start with an NFC boundary");
                return;
            }
        }
        try {
            this.sink.addRelation(strength, prefix, this.rawBuilder, extension);
            this.ruleIndex = i2;
        } catch (Exception e) {
            setParseError("adding relation failed", e);
        }
    }

    private void parseStarredCharacters(int strength, int i) throws ParseException {
        int i2 = parseString(skipWhiteSpace(i), this.rawBuilder);
        if (this.rawBuilder.length() == 0) {
            setParseError("missing starred-relation string");
            return;
        }
        int prev = -1;
        int i3 = i2;
        int j = 0;
        while (true) {
            if (j >= this.rawBuilder.length()) {
                if (i3 < this.rules.length() && this.rules.charAt(i3) == '-') {
                    if (prev >= 0) {
                        i3 = parseString(i3 + 1, this.rawBuilder);
                        if (this.rawBuilder.length() != 0) {
                            int c = this.rawBuilder.codePointAt(0);
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
                                    } else if (65533 > prev || prev > 65535) {
                                        try {
                                            this.sink.addRelation(strength, "", UTF16.valueOf(prev), "");
                                        } catch (Exception e) {
                                            setParseError("adding relation failed", e);
                                            return;
                                        }
                                    } else {
                                        setParseError("starred-relation string range contains U+FFFD, U+FFFE or U+FFFF");
                                        return;
                                    }
                                }
                            } else {
                                setParseError("range start greater than end in starred-relation string");
                                return;
                            }
                        } else {
                            setParseError("range without end in starred-relation string");
                            return;
                        }
                    } else {
                        setParseError("range without start in starred-relation string");
                        return;
                    }
                } else {
                    this.ruleIndex = skipWhiteSpace(i3);
                }
            } else {
                int c2 = this.rawBuilder.codePointAt(j);
                if (!this.nfd.isInert(c2)) {
                    setParseError("starred-relation string is not all NFD-inert");
                    return;
                }
                try {
                    this.sink.addRelation(strength, "", UTF16.valueOf(c2), "");
                    j += Character.charCount(c2);
                    prev = c2;
                } catch (Exception e2) {
                    setParseError("adding relation failed", e2);
                    return;
                }
            }
        }
        this.ruleIndex = skipWhiteSpace(i3);
    }

    private int parseTailoringString(int i, StringBuilder raw) throws ParseException {
        int i2 = parseString(skipWhiteSpace(i), raw);
        if (raw.length() == 0) {
            setParseError("missing relation string");
        }
        return skipWhiteSpace(i2);
    }

    private int parseString(int i, StringBuilder raw) throws ParseException {
        int i2;
        int c = 0;
        raw.setLength(0);
        while (true) {
            if (i >= this.rules.length()) {
                i2 = i;
                break;
            }
            int i3 = i + 1;
            char c2 = this.rules.charAt(i);
            if (isSyntaxChar(c2)) {
                if (c2 != '\'') {
                    if (c2 != '\\') {
                        i2 = i3 - 1;
                        break;
                    } else if (i3 == this.rules.length()) {
                        setParseError("backslash escape at the end of the rule string");
                        return i3;
                    } else {
                        int cp = this.rules.codePointAt(i3);
                        raw.appendCodePoint(cp);
                        i3 += Character.charCount(cp);
                    }
                } else if (i3 >= this.rules.length() || this.rules.charAt(i3) != '\'') {
                    while (i3 != this.rules.length()) {
                        int i4 = i3 + 1;
                        char c3 = this.rules.charAt(i3);
                        if (c3 == '\'') {
                            if (i4 >= this.rules.length() || this.rules.charAt(i4) != '\'') {
                                i = i4;
                            } else {
                                i4++;
                            }
                        }
                        i3 = i4;
                        raw.append(c3);
                    }
                    setParseError("quoted literal text missing terminating apostrophe");
                    return i3;
                } else {
                    raw.append(PatternTokenizer.SINGLE_QUOTE);
                    i = i3 + 1;
                }
            } else if (PatternProps.isWhiteSpace(c2) != 0) {
                i2 = i3 - 1;
                break;
            } else {
                raw.append(c2);
            }
            i = i3;
        }
        while (true) {
            int i5 = c;
            if (i5 >= raw.length()) {
                return i2;
            }
            int c4 = raw.codePointAt(i5);
            if (isSurrogate(c4)) {
                setParseError("string contains an unpaired surrogate");
                return i2;
            } else if (65533 > c4 || c4 > 65535) {
                c = i5 + Character.charCount(c4);
            } else {
                setParseError("string contains U+FFFD, U+FFFE or U+FFFF");
                return i2;
            }
        }
    }

    private static final boolean isSurrogate(int c) {
        return (c & -2048) == 55296;
    }

    private int parseSpecialPosition(int i, StringBuilder str) throws ParseException {
        int j = readWords(i + 1, this.rawBuilder);
        if (j > i && this.rules.charAt(j) == ']' && this.rawBuilder.length() != 0) {
            int j2 = j + 1;
            String raw = this.rawBuilder.toString();
            str.setLength(0);
            for (int pos = 0; pos < positions.length; pos++) {
                if (raw.equals(positions[pos])) {
                    str.append(POS_LEAD);
                    str.append((char) (10240 + pos));
                    return j2;
                }
            }
            if (raw.equals("top")) {
                str.append(POS_LEAD);
                str.append((char) (10240 + Position.LAST_REGULAR.ordinal()));
                return j2;
            } else if (raw.equals("variable top")) {
                str.append(POS_LEAD);
                str.append((char) (10240 + Position.LAST_VARIABLE.ordinal()));
                return j2;
            }
        }
        setParseError("not a valid special reset position");
        return i;
    }

    private void parseSetting() throws ParseException {
        String v;
        String str;
        boolean z = true;
        int i = this.ruleIndex + 1;
        int j = readWords(i, this.rawBuilder);
        if (j <= i || this.rawBuilder.length() == 0) {
            setParseError("expected a setting/option at '['");
        }
        String raw = this.rawBuilder.toString();
        if (this.rules.charAt(j) == ']') {
            int j2 = j + 1;
            if (raw.startsWith("reorder") && (raw.length() == 7 || raw.charAt(7) == ' ')) {
                parseReordering(raw);
                this.ruleIndex = j2;
                return;
            } else if (raw.equals("backwards 2")) {
                this.settings.setFlag(2048, true);
                this.ruleIndex = j2;
                return;
            } else {
                int valueIndex = raw.lastIndexOf(32);
                boolean z2 = false;
                if (valueIndex >= 0) {
                    v = raw.substring(valueIndex + 1);
                    raw = raw.substring(0, valueIndex);
                } else {
                    v = "";
                }
                if (raw.equals("strength") && v.length() == 1) {
                    int value = -1;
                    char c = v.charAt(0);
                    if ('1' <= c && c <= '4') {
                        value = 0 + (c - '1');
                    } else if (c == 'I') {
                        value = 15;
                    }
                    if (value != -1) {
                        this.settings.setStrength(value);
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("alternate")) {
                    int value2 = -1;
                    if (v.equals("non-ignorable")) {
                        value2 = 0;
                    } else if (v.equals("shifted")) {
                        value2 = 1;
                    }
                    if (value2 != -1) {
                        CollationSettings collationSettings = this.settings;
                        if (value2 <= 0) {
                            z = false;
                        }
                        collationSettings.setAlternateHandlingShifted(z);
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("maxVariable")) {
                    int value3 = -1;
                    if (v.equals("space")) {
                        value3 = 0;
                    } else if (v.equals("punct")) {
                        value3 = 1;
                    } else if (v.equals("symbol")) {
                        value3 = 2;
                    } else if (v.equals("currency")) {
                        value3 = 3;
                    }
                    if (value3 != -1) {
                        this.settings.setMaxVariable(value3, 0);
                        this.settings.variableTop = this.baseData.getLastPrimaryForGroup(4096 + value3);
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("caseFirst")) {
                    int value4 = -1;
                    if (v.equals("off")) {
                        value4 = 0;
                    } else if (v.equals("lower")) {
                        value4 = 512;
                    } else if (v.equals("upper")) {
                        value4 = CollationSettings.CASE_FIRST_AND_UPPER_MASK;
                    }
                    if (value4 != -1) {
                        this.settings.setCaseFirst(value4);
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("caseLevel")) {
                    int value5 = getOnOffValue(v);
                    if (value5 != -1) {
                        CollationSettings collationSettings2 = this.settings;
                        if (value5 <= 0) {
                            z = false;
                        }
                        collationSettings2.setFlag(1024, z);
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("normalization")) {
                    int value6 = getOnOffValue(v);
                    if (value6 != -1) {
                        CollationSettings collationSettings3 = this.settings;
                        if (value6 > 0) {
                            z2 = true;
                        }
                        collationSettings3.setFlag(1, z2);
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("numericOrdering")) {
                    int value7 = getOnOffValue(v);
                    if (value7 != -1) {
                        CollationSettings collationSettings4 = this.settings;
                        if (value7 <= 0) {
                            z = false;
                        }
                        collationSettings4.setFlag(2, z);
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("hiraganaQ")) {
                    int value8 = getOnOffValue(v);
                    if (value8 != -1) {
                        if (value8 == 1) {
                            setParseError("[hiraganaQ on] is not supported");
                        }
                        this.ruleIndex = j2;
                        return;
                    }
                } else if (raw.equals("import")) {
                    try {
                        ULocale localeID = new ULocale.Builder().setLanguageTag(v).build();
                        String baseID = localeID.getBaseName();
                        String collationType = localeID.getKeywordValue("collation");
                        if (this.importer == null) {
                            setParseError("[import langTag] is not supported");
                        } else {
                            try {
                                Importer importer2 = this.importer;
                                if (collationType != null) {
                                    str = collationType;
                                } else {
                                    str = "standard";
                                }
                                String importedRules = importer2.getRules(baseID, str);
                                String outerRules = this.rules;
                                int outerRuleIndex = this.ruleIndex;
                                try {
                                    parse(importedRules);
                                } catch (Exception e) {
                                    this.ruleIndex = outerRuleIndex;
                                    setParseError("parsing imported rules failed", e);
                                }
                                this.rules = outerRules;
                                this.ruleIndex = j2;
                            } catch (Exception e2) {
                                setParseError("[import langTag] failed", e2);
                                return;
                            }
                        }
                        return;
                    } catch (Exception e3) {
                        setParseError("expected language tag in [import langTag]", e3);
                        return;
                    }
                }
            }
        } else if (this.rules.charAt(j) == '[') {
            UnicodeSet set = new UnicodeSet();
            int j3 = parseUnicodeSet(j, set);
            if (raw.equals("optimize")) {
                try {
                    this.sink.optimize(set);
                } catch (Exception e4) {
                    setParseError("[optimize set] failed", e4);
                }
                this.ruleIndex = j3;
                return;
            } else if (raw.equals("suppressContractions")) {
                try {
                    this.sink.suppressContractions(set);
                } catch (Exception e5) {
                    setParseError("[suppressContractions set] failed", e5);
                }
                this.ruleIndex = j3;
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
        ArrayList<Integer> reorderCodes = new ArrayList<>();
        while (i < raw.length()) {
            int i2 = i + 1;
            int limit = i2;
            while (limit < raw.length() && raw.charAt(limit) != ' ') {
                limit++;
            }
            int code = getReorderCode(raw.subSequence(i2, limit).toString());
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
            Iterator<Integer> it = reorderCodes.iterator();
            while (it.hasNext()) {
                codes[j] = it.next().intValue();
                j++;
            }
            this.settings.setReordering(this.baseData, codes);
        }
    }

    public static int getReorderCode(String word) {
        for (int i = 0; i < gSpecialReorderCodes.length; i++) {
            if (word.equalsIgnoreCase(gSpecialReorderCodes[i])) {
                return 4096 + i;
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
        int level2 = i;
        while (level2 != this.rules.length()) {
            int j = level2 + 1;
            char c = this.rules.charAt(level2);
            if (c == '[') {
                level++;
            } else if (c == ']') {
                level--;
                if (level == 0) {
                    try {
                        set.applyPattern(this.rules.substring(i, j));
                    } catch (Exception e) {
                        setParseError("not a valid UnicodeSet pattern: " + e.getMessage());
                    }
                    int j2 = skipWhiteSpace(j);
                    if (j2 != this.rules.length() && this.rules.charAt(j2) == ']') {
                        return j2 + 1;
                    }
                    setParseError("missing option-terminating ']' after UnicodeSet pattern");
                    return j2;
                }
            } else {
                continue;
            }
            level2 = j;
        }
        setParseError("unbalanced UnicodeSet pattern brackets");
        return level2;
    }

    private int readWords(int i, StringBuilder raw) {
        raw.setLength(0);
        int i2 = skipWhiteSpace(i);
        while (i2 < this.rules.length()) {
            char c = this.rules.charAt(i2);
            if (!isSyntaxChar(c) || c == '-' || c == '_') {
                if (PatternProps.isWhiteSpace(c)) {
                    raw.append(' ');
                    i2 = skipWhiteSpace(i2 + 1);
                } else {
                    raw.append(c);
                    i2++;
                }
            } else if (raw.length() == 0) {
                return i2;
            } else {
                int lastIndex = raw.length() - 1;
                if (raw.charAt(lastIndex) == ' ') {
                    raw.setLength(lastIndex);
                }
                return i2;
            }
        }
        return 0;
    }

    private int skipComment(int i) {
        while (i < this.rules.length()) {
            int i2 = i + 1;
            int i3 = this.rules.charAt(i);
            if (i3 == 10 || i3 == 12 || i3 == 13 || i3 == 133 || i3 == 8232 || i3 == 8233) {
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
        msg.append(" at index ");
        msg.append(this.ruleIndex);
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
                length = 15 - 1;
            }
        }
        msg.append(this.rules, this.ruleIndex, this.ruleIndex + length);
        msg.append('\"');
        return msg.toString();
    }

    private static boolean isSyntaxChar(int c) {
        return 33 <= c && c <= 126 && (c <= 47 || ((58 <= c && c <= 64) || ((91 <= c && c <= 96) || 123 <= c)));
    }

    private int skipWhiteSpace(int i) {
        while (i < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(i))) {
            i++;
        }
        return i;
    }
}
