package ohos.global.icu.impl.coll;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import ohos.agp.render.opengl.GLES20;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.data.search.model.SearchParameter;
import ohos.global.icu.impl.IllegalIcuArgumentException;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.lang.UProperty;
import ohos.global.icu.text.Normalizer2;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ULocale;

public final class CollationRuleParser {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String BEFORE = "[before";
    private static final int OFFSET_SHIFT = 8;
    static final Position[] POSITION_VALUES = Position.values();
    static final char POS_BASE = 10240;
    static final char POS_LEAD = 65534;
    private static final int STARRED_FLAG = 16;
    private static final int STRENGTH_MASK = 15;
    private static final int UCOL_DEFAULT = -1;
    private static final int UCOL_OFF = 0;
    private static final int UCOL_ON = 1;
    private static final int U_PARSE_CONTEXT_LEN = 16;
    private static final String[] gSpecialReorderCodes = {"space", "punct", "symbol", "currency", Constants.ATTRNAME_DIGIT};
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

    /* access modifiers changed from: package-private */
    public interface Importer {
        String getRules(String str, String str2);
    }

    /* access modifiers changed from: package-private */
    public enum Position {
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

    private static final boolean isSurrogate(int i) {
        return (i & -2048) == 55296;
    }

    private static boolean isSyntaxChar(int i) {
        return 33 <= i && i <= 126 && (i <= 47 || ((58 <= i && i <= 64) || ((91 <= i && i <= 96) || 123 <= i)));
    }

    /* access modifiers changed from: package-private */
    public static abstract class Sink {
        /* access modifiers changed from: package-private */
        public abstract void addRelation(int i, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3);

        /* access modifiers changed from: package-private */
        public abstract void addReset(int i, CharSequence charSequence);

        /* access modifiers changed from: package-private */
        public void optimize(UnicodeSet unicodeSet) {
        }

        /* access modifiers changed from: package-private */
        public void suppressContractions(UnicodeSet unicodeSet) {
        }

        Sink() {
        }
    }

    CollationRuleParser(CollationData collationData) {
        this.baseData = collationData;
    }

    /* access modifiers changed from: package-private */
    public void setSink(Sink sink2) {
        this.sink = sink2;
    }

    /* access modifiers changed from: package-private */
    public void setImporter(Importer importer2) {
        this.importer = importer2;
    }

    /* access modifiers changed from: package-private */
    public void parse(String str, CollationSettings collationSettings) throws ParseException {
        this.settings = collationSettings;
        parse(str);
    }

    private void parse(String str) throws ParseException {
        this.rules = str;
        this.ruleIndex = 0;
        while (this.ruleIndex < this.rules.length()) {
            char charAt = this.rules.charAt(this.ruleIndex);
            if (PatternProps.isWhiteSpace(charAt)) {
                this.ruleIndex++;
            } else if (charAt == '!') {
                this.ruleIndex++;
            } else if (charAt == '#') {
                this.ruleIndex = skipComment(this.ruleIndex + 1);
            } else if (charAt == '&') {
                parseRuleChain();
            } else if (charAt == '@') {
                this.settings.setFlag(2048, true);
                this.ruleIndex++;
            } else if (charAt != '[') {
                setParseError("expected a reset or setting or comment");
            } else {
                parseSetting();
            }
        }
    }

    private void parseRuleChain() throws ParseException {
        int parseResetAndPosition = parseResetAndPosition();
        boolean z = true;
        while (true) {
            int parseRelationOperator = parseRelationOperator();
            if (parseRelationOperator >= 0) {
                int i = parseRelationOperator & 15;
                if (parseResetAndPosition < 15) {
                    if (z) {
                        if (i != parseResetAndPosition) {
                            setParseError("reset-before strength differs from its first relation");
                            return;
                        }
                    } else if (i < parseResetAndPosition) {
                        setParseError("reset-before strength followed by a stronger relation");
                        return;
                    }
                }
                int i2 = this.ruleIndex + (parseRelationOperator >> 8);
                if ((parseRelationOperator & 16) == 0) {
                    parseRelationStrings(i, i2);
                } else {
                    parseStarredCharacters(i, i2);
                }
                z = false;
            } else if (this.ruleIndex >= this.rules.length() || this.rules.charAt(this.ruleIndex) != '#') {
                break;
            } else {
                this.ruleIndex = skipComment(this.ruleIndex + 1);
            }
        }
        if (z) {
            setParseError("reset not followed by a relation");
        }
    }

    private int parseResetAndPosition() throws ParseException {
        int i;
        int i2;
        int i3;
        int skipWhiteSpace;
        int skipWhiteSpace2;
        char charAt;
        int skipWhiteSpace3 = skipWhiteSpace(this.ruleIndex + 1);
        if (!this.rules.regionMatches(skipWhiteSpace3, BEFORE, 0, 7) || (i3 = skipWhiteSpace3 + 7) >= this.rules.length() || !PatternProps.isWhiteSpace(this.rules.charAt(i3)) || (skipWhiteSpace2 = (skipWhiteSpace = skipWhiteSpace(i3 + 1)) + 1) >= this.rules.length() || '1' > (charAt = this.rules.charAt(skipWhiteSpace)) || charAt > '3' || this.rules.charAt(skipWhiteSpace2) != ']') {
            i = 15;
        } else {
            i = (charAt - '1') + 0;
            skipWhiteSpace3 = skipWhiteSpace(skipWhiteSpace + 2);
        }
        if (skipWhiteSpace3 >= this.rules.length()) {
            setParseError("reset without position");
            return -1;
        }
        if (this.rules.charAt(skipWhiteSpace3) == '[') {
            i2 = parseSpecialPosition(skipWhiteSpace3, this.rawBuilder);
        } else {
            i2 = parseTailoringString(skipWhiteSpace3, this.rawBuilder);
        }
        try {
            this.sink.addReset(i, this.rawBuilder);
            this.ruleIndex = i2;
            return i;
        } catch (Exception e) {
            setParseError("adding reset failed", e);
            return -1;
        }
    }

    private int parseRelationOperator() {
        int i;
        this.ruleIndex = skipWhiteSpace(this.ruleIndex);
        if (this.ruleIndex >= this.rules.length()) {
            return -1;
        }
        int i2 = this.ruleIndex;
        int i3 = i2 + 1;
        char charAt = this.rules.charAt(i2);
        int i4 = 2;
        if (charAt != ',') {
            switch (charAt) {
                case ';':
                    i4 = 1;
                    break;
                case '<':
                    if (i3 >= this.rules.length() || this.rules.charAt(i3) != '<') {
                        i = 0;
                    } else {
                        i3++;
                        if (i3 >= this.rules.length() || this.rules.charAt(i3) != '<') {
                            i4 = 1;
                            if (i3 < this.rules.length() && this.rules.charAt(i3) == '*') {
                                i3++;
                                i4 |= 16;
                                break;
                            }
                        } else {
                            i3++;
                            if (i3 < this.rules.length() && this.rules.charAt(i3) == '<') {
                                i3++;
                                i = 3;
                            }
                            i3++;
                            i4 |= 16;
                        }
                    }
                    i4 = i;
                    i3++;
                    i4 |= 16;
                    break;
                case '=':
                    i4 = 15;
                    if (i3 < this.rules.length() && this.rules.charAt(i3) == '*') {
                        i3++;
                        i4 = 31;
                        break;
                    }
                default:
                    return -1;
            }
        }
        return ((i3 - this.ruleIndex) << 8) | i4;
    }

    private void parseRelationStrings(int i, int i2) throws ParseException {
        String str;
        int parseTailoringString = parseTailoringString(i2, this.rawBuilder);
        char charAt = parseTailoringString < this.rules.length() ? this.rules.charAt(parseTailoringString) : 0;
        String str2 = "";
        if (charAt == '|') {
            String sb = this.rawBuilder.toString();
            parseTailoringString = parseTailoringString(parseTailoringString + 1, this.rawBuilder);
            str = sb;
            charAt = parseTailoringString < this.rules.length() ? this.rules.charAt(parseTailoringString) : 0;
        } else {
            str = str2;
        }
        String str3 = str2;
        if (charAt == '/') {
            StringBuilder sb2 = new StringBuilder();
            parseTailoringString = parseTailoringString(parseTailoringString + 1, sb2);
            str3 = sb2;
        }
        if (str.length() != 0) {
            int codePointAt = str.codePointAt(0);
            int codePointAt2 = this.rawBuilder.codePointAt(0);
            if (!this.nfc.hasBoundaryBefore(codePointAt) || !this.nfc.hasBoundaryBefore(codePointAt2)) {
                setParseError("in 'prefix|str', prefix and str must each start with an NFC boundary");
                return;
            }
        }
        try {
            this.sink.addRelation(i, str, this.rawBuilder, str3);
            this.ruleIndex = parseTailoringString;
        } catch (Exception e) {
            setParseError("adding relation failed", e);
        }
    }

    private void parseStarredCharacters(int i, int i2) throws ParseException {
        int codePointAt;
        int parseString = parseString(skipWhiteSpace(i2), this.rawBuilder);
        if (this.rawBuilder.length() == 0) {
            setParseError("missing starred-relation string");
            return;
        }
        int i3 = parseString;
        int i4 = 0;
        while (true) {
            int i5 = -1;
            while (i4 < this.rawBuilder.length()) {
                i5 = this.rawBuilder.codePointAt(i4);
                if (!this.nfd.isInert(i5)) {
                    setParseError("starred-relation string is not all NFD-inert");
                    return;
                }
                try {
                    this.sink.addRelation(i, "", UTF16.valueOf(i5), "");
                    i4 += Character.charCount(i5);
                } catch (Exception e) {
                    setParseError("adding relation failed", e);
                    return;
                }
            }
            if (i3 < this.rules.length() && this.rules.charAt(i3) == '-') {
                if (i5 >= 0) {
                    i3 = parseString(i3 + 1, this.rawBuilder);
                    if (this.rawBuilder.length() != 0) {
                        codePointAt = this.rawBuilder.codePointAt(0);
                        if (codePointAt >= i5) {
                            while (true) {
                                i5++;
                                if (i5 > codePointAt) {
                                    break;
                                } else if (!this.nfd.isInert(i5)) {
                                    setParseError("starred-relation string range is not all NFD-inert");
                                    return;
                                } else if (isSurrogate(i5)) {
                                    setParseError("starred-relation string range contains a surrogate");
                                    return;
                                } else if (65533 > i5 || i5 > 65535) {
                                    try {
                                        this.sink.addRelation(i, "", UTF16.valueOf(i5), "");
                                    } catch (Exception e2) {
                                        setParseError("adding relation failed", e2);
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
                break;
            }
            i4 = Character.charCount(codePointAt);
        }
        this.ruleIndex = skipWhiteSpace(i3);
    }

    private int parseTailoringString(int i, StringBuilder sb) throws ParseException {
        int parseString = parseString(skipWhiteSpace(i), sb);
        if (sb.length() == 0) {
            setParseError("missing relation string");
        }
        return skipWhiteSpace(parseString);
    }

    private int parseString(int i, StringBuilder sb) throws ParseException {
        int i2;
        int i3 = 0;
        sb.setLength(0);
        while (true) {
            if (i >= this.rules.length()) {
                break;
            }
            i2 = i + 1;
            char charAt = this.rules.charAt(i);
            if (isSyntaxChar(charAt)) {
                if (charAt != '\'') {
                    if (charAt != '\\') {
                        break;
                    } else if (i2 == this.rules.length()) {
                        setParseError("backslash escape at the end of the rule string");
                        return i2;
                    } else {
                        int codePointAt = this.rules.codePointAt(i2);
                        sb.appendCodePoint(codePointAt);
                        i2 += Character.charCount(codePointAt);
                        i = i2;
                    }
                } else if (i2 >= this.rules.length() || this.rules.charAt(i2) != '\'') {
                    while (i2 != this.rules.length()) {
                        int i4 = i2 + 1;
                        char charAt2 = this.rules.charAt(i2);
                        if (charAt2 == '\'') {
                            if (i4 >= this.rules.length() || this.rules.charAt(i4) != '\'') {
                                i = i4;
                            } else {
                                i4++;
                            }
                        }
                        i2 = i4;
                        sb.append(charAt2);
                    }
                    setParseError("quoted literal text missing terminating apostrophe");
                    return i2;
                } else {
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                    i = i2 + 1;
                }
            } else if (PatternProps.isWhiteSpace(charAt)) {
                break;
            } else {
                sb.append(charAt);
                i = i2;
            }
        }
        i = i2 - 1;
        while (i3 < sb.length()) {
            int codePointAt2 = sb.codePointAt(i3);
            if (isSurrogate(codePointAt2)) {
                setParseError("string contains an unpaired surrogate");
                return i;
            } else if (65533 > codePointAt2 || codePointAt2 > 65535) {
                i3 += Character.charCount(codePointAt2);
            } else {
                setParseError("string contains U+FFFD, U+FFFE or U+FFFF");
                return i;
            }
        }
        return i;
    }

    private int parseSpecialPosition(int i, StringBuilder sb) throws ParseException {
        int readWords = readWords(i + 1, this.rawBuilder);
        if (readWords > i && this.rules.charAt(readWords) == ']' && this.rawBuilder.length() != 0) {
            int i2 = readWords + 1;
            String sb2 = this.rawBuilder.toString();
            int i3 = 0;
            sb.setLength(0);
            while (true) {
                String[] strArr = positions;
                if (i3 < strArr.length) {
                    if (sb2.equals(strArr[i3])) {
                        sb.append(POS_LEAD);
                        sb.append((char) (i3 + GLES20.GL_TEXTURE_MAG_FILTER));
                        return i2;
                    }
                    i3++;
                } else if (sb2.equals("top")) {
                    sb.append(POS_LEAD);
                    sb.append((char) (Position.LAST_REGULAR.ordinal() + GLES20.GL_TEXTURE_MAG_FILTER));
                    return i2;
                } else if (sb2.equals("variable top")) {
                    sb.append(POS_LEAD);
                    sb.append((char) (Position.LAST_VARIABLE.ordinal() + GLES20.GL_TEXTURE_MAG_FILTER));
                    return i2;
                }
            }
        }
        setParseError("not a valid special reset position");
        return i;
    }

    private void parseSetting() throws ParseException {
        String str;
        char c;
        boolean z = true;
        boolean z2 = true;
        int i = 1;
        boolean z3 = true;
        int i2 = this.ruleIndex + 1;
        int readWords = readWords(i2, this.rawBuilder);
        if (readWords <= i2 || this.rawBuilder.length() == 0) {
            setParseError("expected a setting/option at '['");
        }
        String sb = this.rawBuilder.toString();
        if (this.rules.charAt(readWords) == ']') {
            int i3 = readWords + 1;
            if (sb.startsWith("reorder") && (sb.length() == 7 || sb.charAt(7) == ' ')) {
                parseReordering(sb);
                this.ruleIndex = i3;
                return;
            } else if (sb.equals("backwards 2")) {
                this.settings.setFlag(2048, true);
                this.ruleIndex = i3;
                return;
            } else {
                int lastIndexOf = sb.lastIndexOf(32);
                boolean z4 = false;
                int i4 = 0;
                if (lastIndexOf >= 0) {
                    str = sb.substring(lastIndexOf + 1);
                    sb = sb.substring(0, lastIndexOf);
                } else {
                    str = "";
                }
                if (sb.equals("strength") && str.length() == 1) {
                    char charAt = str.charAt(0);
                    int i5 = ('1' > charAt || charAt > '4') ? charAt == 'I' ? 15 : -1 : (charAt - '1') + 0;
                    if (i5 != -1) {
                        this.settings.setStrength(i5);
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals("alternate")) {
                    if (str.equals("non-ignorable")) {
                        c = 0;
                    } else {
                        c = str.equals("shifted") ? (char) 1 : 65535;
                    }
                    if (c != 65535) {
                        CollationSettings collationSettings = this.settings;
                        if (c <= 0) {
                            z2 = false;
                        }
                        collationSettings.setAlternateHandlingShifted(z2);
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals("maxVariable")) {
                    if (str.equals("space")) {
                        i = 0;
                    } else if (!str.equals("punct")) {
                        if (str.equals("symbol")) {
                            i = 2;
                        } else {
                            i = str.equals("currency") ? 3 : -1;
                        }
                    }
                    if (i != -1) {
                        this.settings.setMaxVariable(i, 0);
                        this.settings.variableTop = this.baseData.getLastPrimaryForGroup(i + 4096);
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals("caseFirst")) {
                    if (!str.equals("off")) {
                        if (str.equals(SearchParameter.LOWER)) {
                            i4 = 512;
                        } else {
                            i4 = str.equals(SearchParameter.UPPER) ? 768 : -1;
                        }
                    }
                    if (i4 != -1) {
                        this.settings.setCaseFirst(i4);
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals("caseLevel")) {
                    int onOffValue = getOnOffValue(str);
                    if (onOffValue != -1) {
                        CollationSettings collationSettings2 = this.settings;
                        if (onOffValue <= 0) {
                            z3 = false;
                        }
                        collationSettings2.setFlag(1024, z3);
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals("normalization")) {
                    int onOffValue2 = getOnOffValue(str);
                    if (onOffValue2 != -1) {
                        CollationSettings collationSettings3 = this.settings;
                        if (onOffValue2 > 0) {
                            z4 = true;
                        }
                        collationSettings3.setFlag(1, z4);
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals("numericOrdering")) {
                    int onOffValue3 = getOnOffValue(str);
                    if (onOffValue3 != -1) {
                        CollationSettings collationSettings4 = this.settings;
                        if (onOffValue3 <= 0) {
                            z = false;
                        }
                        collationSettings4.setFlag(2, z);
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals("hiraganaQ")) {
                    int onOffValue4 = getOnOffValue(str);
                    if (onOffValue4 != -1) {
                        if (onOffValue4 == 1) {
                            setParseError("[hiraganaQ on] is not supported");
                        }
                        this.ruleIndex = i3;
                        return;
                    }
                } else if (sb.equals(Constants.ELEMNAME_IMPORT_STRING)) {
                    try {
                        ULocale build = new ULocale.Builder().setLanguageTag(str).build();
                        String baseName = build.getBaseName();
                        String keywordValue = build.getKeywordValue("collation");
                        Importer importer2 = this.importer;
                        if (importer2 == null) {
                            setParseError("[import langTag] is not supported");
                            return;
                        }
                        if (keywordValue == null) {
                            keywordValue = "standard";
                        }
                        try {
                            String rules2 = importer2.getRules(baseName, keywordValue);
                            String str2 = this.rules;
                            int i6 = this.ruleIndex;
                            try {
                                parse(rules2);
                            } catch (Exception e) {
                                this.ruleIndex = i6;
                                setParseError("parsing imported rules failed", e);
                            }
                            this.rules = str2;
                            this.ruleIndex = i3;
                            return;
                        } catch (Exception e2) {
                            setParseError("[import langTag] failed", e2);
                            return;
                        }
                    } catch (Exception e3) {
                        setParseError("expected language tag in [import langTag]", e3);
                        return;
                    }
                }
            }
        } else if (this.rules.charAt(readWords) == '[') {
            UnicodeSet unicodeSet = new UnicodeSet();
            int parseUnicodeSet = parseUnicodeSet(readWords, unicodeSet);
            if (sb.equals("optimize")) {
                try {
                    this.sink.optimize(unicodeSet);
                } catch (Exception e4) {
                    setParseError("[optimize set] failed", e4);
                }
                this.ruleIndex = parseUnicodeSet;
                return;
            } else if (sb.equals("suppressContractions")) {
                try {
                    this.sink.suppressContractions(unicodeSet);
                } catch (Exception e5) {
                    setParseError("[suppressContractions set] failed", e5);
                }
                this.ruleIndex = parseUnicodeSet;
                return;
            }
        }
        setParseError("not a valid setting/option");
    }

    private void parseReordering(CharSequence charSequence) throws ParseException {
        int i;
        if (7 == charSequence.length()) {
            this.settings.resetReordering();
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (int i2 = 7; i2 < charSequence.length(); i2 = i) {
            int i3 = i2 + 1;
            i = i3;
            while (i < charSequence.length() && charSequence.charAt(i) != ' ') {
                i++;
            }
            int reorderCode = getReorderCode(charSequence.subSequence(i3, i).toString());
            if (reorderCode < 0) {
                setParseError("unknown script or reorder code");
                return;
            } else {
                arrayList.add(Integer.valueOf(reorderCode));
            }
        }
        if (arrayList.isEmpty()) {
            this.settings.resetReordering();
            return;
        }
        int[] iArr = new int[arrayList.size()];
        int i4 = 0;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            iArr[i4] = ((Integer) it.next()).intValue();
            i4++;
        }
        this.settings.setReordering(this.baseData, iArr);
    }

    public static int getReorderCode(String str) {
        int i = 0;
        while (true) {
            String[] strArr = gSpecialReorderCodes;
            if (i >= strArr.length) {
                try {
                    int propertyValueEnum = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, str);
                    if (propertyValueEnum >= 0) {
                        return propertyValueEnum;
                    }
                } catch (IllegalIcuArgumentException unused) {
                }
                return str.equalsIgnoreCase("others") ? 103 : -1;
            } else if (str.equalsIgnoreCase(strArr[i])) {
                return i + 4096;
            } else {
                i++;
            }
        }
    }

    private static int getOnOffValue(String str) {
        if (str.equals("on")) {
            return 1;
        }
        return str.equals("off") ? 0 : -1;
    }

    private int parseUnicodeSet(int i, UnicodeSet unicodeSet) throws ParseException {
        int i2 = 0;
        int i3 = i;
        while (i3 != this.rules.length()) {
            int i4 = i3 + 1;
            char charAt = this.rules.charAt(i3);
            if (charAt == '[') {
                i2++;
            } else if (charAt == ']' && i2 - 1 == 0) {
                try {
                    unicodeSet.applyPattern(this.rules.substring(i, i4));
                } catch (Exception e) {
                    setParseError("not a valid UnicodeSet pattern: " + e.getMessage());
                }
                int skipWhiteSpace = skipWhiteSpace(i4);
                if (skipWhiteSpace != this.rules.length() && this.rules.charAt(skipWhiteSpace) == ']') {
                    return skipWhiteSpace + 1;
                }
                setParseError("missing option-terminating ']' after UnicodeSet pattern");
                return skipWhiteSpace;
            }
            i3 = i4;
        }
        setParseError("unbalanced UnicodeSet pattern brackets");
        return i3;
    }

    private int readWords(int i, StringBuilder sb) {
        sb.setLength(0);
        int skipWhiteSpace = skipWhiteSpace(i);
        while (skipWhiteSpace < this.rules.length()) {
            char charAt = this.rules.charAt(skipWhiteSpace);
            if (!isSyntaxChar(charAt) || charAt == '-' || charAt == '_') {
                if (PatternProps.isWhiteSpace(charAt)) {
                    sb.append(' ');
                    skipWhiteSpace = skipWhiteSpace(skipWhiteSpace + 1);
                } else {
                    sb.append(charAt);
                    skipWhiteSpace++;
                }
            } else if (sb.length() == 0) {
                return skipWhiteSpace;
            } else {
                int length = sb.length() - 1;
                if (sb.charAt(length) == ' ') {
                    sb.setLength(length);
                }
                return skipWhiteSpace;
            }
        }
        return 0;
    }

    private int skipComment(int i) {
        while (i < this.rules.length()) {
            int i2 = i + 1;
            char charAt = this.rules.charAt(i);
            if (charAt == '\n' || charAt == '\f' || charAt == '\r' || charAt == 133 || charAt == 8232 || charAt == 8233) {
                return i2;
            }
            i = i2;
        }
        return i;
    }

    private void setParseError(String str) throws ParseException {
        throw makeParseException(str);
    }

    private void setParseError(String str, Exception exc) throws ParseException {
        ParseException makeParseException = makeParseException(str + PluralRules.KEYWORD_RULE_SEPARATOR + exc.getMessage());
        makeParseException.initCause(exc);
        throw makeParseException;
    }

    private ParseException makeParseException(String str) {
        return new ParseException(appendErrorContext(str), this.ruleIndex);
    }

    private String appendErrorContext(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.append(" at index ");
        sb.append(this.ruleIndex);
        sb.append(" near \"");
        int i = 15;
        int i2 = this.ruleIndex - 15;
        if (i2 < 0) {
            i2 = 0;
        } else if (i2 > 0 && Character.isLowSurrogate(this.rules.charAt(i2))) {
            i2++;
        }
        sb.append((CharSequence) this.rules, i2, this.ruleIndex);
        sb.append('!');
        int length = this.rules.length();
        int i3 = this.ruleIndex;
        int i4 = length - i3;
        if (i4 < 16) {
            i = i4;
        } else if (Character.isHighSurrogate(this.rules.charAt((i3 + 15) - 1))) {
            i = 14;
        }
        String str2 = this.rules;
        int i5 = this.ruleIndex;
        sb.append((CharSequence) str2, i5, i + i5);
        sb.append('\"');
        return sb.toString();
    }

    private int skipWhiteSpace(int i) {
        while (i < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(i))) {
            i++;
        }
        return i;
    }
}
