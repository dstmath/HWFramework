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
import android.icu.util.ULocale.Builder;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.text.ParseException;
import java.util.ArrayList;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class CollationRuleParser {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final String BEFORE = "[before";
    private static final int OFFSET_SHIFT = 8;
    static final Position[] POSITION_VALUES = null;
    static final char POS_BASE = '\u2800';
    static final char POS_LEAD = '\ufffe';
    private static final int STARRED_FLAG = 16;
    private static final int STRENGTH_MASK = 15;
    private static final int UCOL_DEFAULT = -1;
    private static final int UCOL_OFF = 0;
    private static final int UCOL_ON = 1;
    private static final int U_PARSE_CONTEXT_LEN = 16;
    private static final String[] gSpecialReorderCodes = null;
    private static final String[] positions = null;
    private final CollationData baseData;
    private Importer importer;
    private Normalizer2 nfc;
    private Normalizer2 nfd;
    private final StringBuilder rawBuilder;
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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationRuleParser.Position.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationRuleParser.Position.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationRuleParser.Position.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationRuleParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationRuleParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationRuleParser.<clinit>():void");
    }

    CollationRuleParser(CollationData base) {
        this.rawBuilder = new StringBuilder();
        this.nfd = Normalizer2.getNFDInstance();
        this.nfc = Normalizer2.getNFCInstance();
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
        this.ruleIndex = UCOL_OFF;
        while (this.ruleIndex < this.rules.length()) {
            char c = this.rules.charAt(this.ruleIndex);
            if (!PatternProps.isWhiteSpace(c)) {
                switch (c) {
                    case Opcodes.OP_ARRAY_LENGTH /*33*/:
                        this.ruleIndex += UCOL_ON;
                        break;
                    case Opcodes.OP_NEW_ARRAY /*35*/:
                        this.ruleIndex = skipComment(this.ruleIndex + UCOL_ON);
                        break;
                    case Opcodes.OP_FILL_ARRAY_DATA /*38*/:
                        parseRuleChain();
                        break;
                    case NodeFilter.SHOW_PROCESSING_INSTRUCTION /*64*/:
                        this.settings.setFlag(NodeFilter.SHOW_NOTATION, true);
                        this.ruleIndex += UCOL_ON;
                        break;
                    case Opcodes.OP_IPUT_OBJECT /*91*/:
                        parseSetting();
                        break;
                    default:
                        setParseError("expected a reset or setting or comment");
                        break;
                }
            }
            this.ruleIndex += UCOL_ON;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseRuleChain() throws ParseException {
        int resetStrength = parseResetAndPosition();
        boolean isFirstRelation = true;
        while (true) {
            int result = parseRelationOperator();
            if (result >= 0) {
                int strength = result & STRENGTH_MASK;
                if (resetStrength < STRENGTH_MASK) {
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
                int i = this.ruleIndex + (result >> OFFSET_SHIFT);
                if ((result & U_PARSE_CONTEXT_LEN) == 0) {
                    parseRelationStrings(strength, i);
                } else {
                    parseStarredCharacters(strength, i);
                }
                isFirstRelation = -assertionsDisabled;
            } else if (this.ruleIndex < this.rules.length() && this.rules.charAt(this.ruleIndex) == '#') {
                this.ruleIndex = skipComment(this.ruleIndex + UCOL_ON);
            } else if (isFirstRelation) {
                setParseError("reset not followed by a relation");
            }
        }
        if (isFirstRelation) {
            setParseError("reset not followed by a relation");
        }
    }

    private int parseResetAndPosition() throws ParseException {
        int resetStrength;
        int i = skipWhiteSpace(this.ruleIndex + UCOL_ON);
        if (this.rules.regionMatches(i, BEFORE, UCOL_OFF, BEFORE.length())) {
            int j = i + BEFORE.length();
            if (j < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(j))) {
                j = skipWhiteSpace(j + UCOL_ON);
                if (j + UCOL_ON < this.rules.length()) {
                    char c = this.rules.charAt(j);
                    if ('1' <= c && c <= '3' && this.rules.charAt(j + UCOL_ON) == ']') {
                        resetStrength = (c - 49) + UCOL_OFF;
                        i = skipWhiteSpace(j + 2);
                        if (i < this.rules.length()) {
                            setParseError("reset without position");
                            return UCOL_DEFAULT;
                        }
                        if (this.rules.charAt(i) != '[') {
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
                            return UCOL_DEFAULT;
                        }
                    }
                }
            }
        }
        resetStrength = STRENGTH_MASK;
        if (i < this.rules.length()) {
            if (this.rules.charAt(i) != '[') {
                i = parseTailoringString(i, this.rawBuilder);
            } else {
                i = parseSpecialPosition(i, this.rawBuilder);
            }
            this.sink.addReset(resetStrength, this.rawBuilder);
            this.ruleIndex = i;
            return resetStrength;
        }
        setParseError("reset without position");
        return UCOL_DEFAULT;
    }

    private int parseRelationOperator() {
        this.ruleIndex = skipWhiteSpace(this.ruleIndex);
        if (this.ruleIndex >= this.rules.length()) {
            return UCOL_DEFAULT;
        }
        int strength;
        int i = this.ruleIndex;
        int i2 = i + UCOL_ON;
        switch (this.rules.charAt(i)) {
            case Opcodes.OP_SPARSE_SWITCH /*44*/:
                strength = 2;
                i = i2;
                break;
            case Opcodes.OP_IF_GEZ /*59*/:
                strength = UCOL_ON;
                i = i2;
                break;
            case Opcodes.OP_IF_GTZ /*60*/:
                if (i2 >= this.rules.length() || this.rules.charAt(i2) != '<') {
                    strength = UCOL_OFF;
                    i = i2;
                } else {
                    i = i2 + UCOL_ON;
                    if (i >= this.rules.length() || this.rules.charAt(i) != '<') {
                        strength = UCOL_ON;
                    } else {
                        i += UCOL_ON;
                        if (i >= this.rules.length() || this.rules.charAt(i) != '<') {
                            strength = 2;
                        } else {
                            i += UCOL_ON;
                            strength = 3;
                        }
                    }
                }
                if (i < this.rules.length() && this.rules.charAt(i) == '*') {
                    i += UCOL_ON;
                    strength |= U_PARSE_CONTEXT_LEN;
                    break;
                }
                break;
            case Opcodes.OP_IF_LEZ /*61*/:
                strength = STRENGTH_MASK;
                if (i2 < this.rules.length() && this.rules.charAt(i2) == '*') {
                    i = i2 + UCOL_ON;
                    strength = 31;
                    break;
                }
                i = i2;
                break;
            default:
                return UCOL_DEFAULT;
        }
        return ((i - this.ruleIndex) << OFFSET_SHIFT) | strength;
    }

    private void parseRelationStrings(int strength, int i) throws ParseException {
        char next;
        String prefix = XmlPullParser.NO_NAMESPACE;
        CharSequence extension = XmlPullParser.NO_NAMESPACE;
        i = parseTailoringString(i, this.rawBuilder);
        if (i < this.rules.length()) {
            next = this.rules.charAt(i);
        } else {
            next = '\u0000';
        }
        if (next == '|') {
            prefix = this.rawBuilder.toString();
            i = parseTailoringString(i + UCOL_ON, this.rawBuilder);
            next = i < this.rules.length() ? this.rules.charAt(i) : '\u0000';
        }
        if (next == '/') {
            StringBuilder extBuilder = new StringBuilder();
            i = parseTailoringString(i + UCOL_ON, extBuilder);
            extension = extBuilder;
        }
        if (prefix.length() != 0) {
            int prefix0 = prefix.codePointAt(UCOL_OFF);
            int c = this.rawBuilder.codePointAt(UCOL_OFF);
            if (!(this.nfc.hasBoundaryBefore(prefix0) && this.nfc.hasBoundaryBefore(c))) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseStarredCharacters(int strength, int i) throws ParseException {
        String empty = XmlPullParser.NO_NAMESPACE;
        i = parseString(skipWhiteSpace(i), this.rawBuilder);
        if (this.rawBuilder.length() == 0) {
            setParseError("missing starred-relation string");
            return;
        }
        int prev = UCOL_DEFAULT;
        int j = UCOL_OFF;
        while (true) {
            int c;
            if (j < this.rawBuilder.length()) {
                c = this.rawBuilder.codePointAt(j);
                if (this.nfd.isInert(c)) {
                    try {
                        this.sink.addRelation(strength, empty, UTF16.valueOf(c), empty);
                        j += Character.charCount(c);
                        prev = c;
                    } catch (Exception e) {
                        setParseError("adding relation failed", e);
                        return;
                    }
                }
                setParseError("starred-relation string is not all NFD-inert");
                return;
            } else if (i >= this.rules.length() || this.rules.charAt(i) != '-') {
                this.ruleIndex = skipWhiteSpace(i);
            } else if (prev < 0) {
                setParseError("range without start in starred-relation string");
                return;
            } else {
                i = parseString(i + UCOL_ON, this.rawBuilder);
                if (this.rawBuilder.length() == 0) {
                    setParseError("range without end in starred-relation string");
                    return;
                }
                c = this.rawBuilder.codePointAt(UCOL_OFF);
                if (c < prev) {
                    setParseError("range start greater than end in starred-relation string");
                    return;
                }
                while (true) {
                    prev += UCOL_ON;
                    if (prev > c) {
                        break;
                    } else if (!this.nfd.isInert(prev)) {
                        setParseError("starred-relation string range is not all NFD-inert");
                        return;
                    } else if (isSurrogate(prev)) {
                        setParseError("starred-relation string range contains a surrogate");
                        return;
                    } else if (UCharacter.REPLACEMENT_CHAR > prev || prev > DexFormat.MAX_TYPE_IDX) {
                        try {
                            this.sink.addRelation(strength, empty, UTF16.valueOf(prev), empty);
                        } catch (Exception e2) {
                            setParseError("adding relation failed", e2);
                            return;
                        }
                    } else {
                        setParseError("starred-relation string range contains U+FFFD, U+FFFE or U+FFFF");
                        return;
                    }
                }
                prev = UCOL_DEFAULT;
                j = Character.charCount(c);
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
        raw.setLength(UCOL_OFF);
        while (i < this.rules.length()) {
            int i2 = i + UCOL_ON;
            char c = this.rules.charAt(i);
            if (isSyntaxChar(c)) {
                if (c != PatternTokenizer.SINGLE_QUOTE) {
                    if (c != PatternTokenizer.BACK_SLASH) {
                        i = i2 + UCOL_DEFAULT;
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
                        i2 = i + UCOL_ON;
                        c = this.rules.charAt(i);
                        if (c != PatternTokenizer.SINGLE_QUOTE) {
                            i = i2;
                        } else if (i2 >= this.rules.length() || this.rules.charAt(i2) != PatternTokenizer.SINGLE_QUOTE) {
                            i = i2;
                        } else {
                            i = i2 + UCOL_ON;
                        }
                        raw.append(c);
                    }
                    setParseError("quoted literal text missing terminating apostrophe");
                    return i;
                } else {
                    raw.append(PatternTokenizer.SINGLE_QUOTE);
                    i = i2 + UCOL_ON;
                }
            } else if (PatternProps.isWhiteSpace(c)) {
                i = i2 + UCOL_DEFAULT;
                break;
            } else {
                raw.append(c);
                i = i2;
            }
        }
        int j = UCOL_OFF;
        while (j < raw.length()) {
            int c2 = raw.codePointAt(j);
            if (isSurrogate(c2)) {
                setParseError("string contains an unpaired surrogate");
                return i;
            } else if (UCharacter.REPLACEMENT_CHAR > c2 || c2 > DexFormat.MAX_TYPE_IDX) {
                j += Character.charCount(c2);
            } else {
                setParseError("string contains U+FFFD, U+FFFE or U+FFFF");
                return i;
            }
        }
        return i;
    }

    private static final boolean isSurrogate(int c) {
        return (c & -2048) == UTF16.SURROGATE_MIN_VALUE ? true : -assertionsDisabled;
    }

    private int parseSpecialPosition(int i, StringBuilder str) throws ParseException {
        int j = readWords(i + UCOL_ON, this.rawBuilder);
        if (j > i && this.rules.charAt(j) == ']' && this.rawBuilder.length() != 0) {
            j += UCOL_ON;
            String raw = this.rawBuilder.toString();
            str.setLength(UCOL_OFF);
            for (int pos = UCOL_OFF; pos < positions.length; pos += UCOL_ON) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseSetting() throws ParseException {
        int i = this.ruleIndex + UCOL_ON;
        int j = readWords(i, this.rawBuilder);
        if (j > i) {
        }
        setParseError("expected a setting/option at '['");
        String raw = this.rawBuilder.toString();
        if (this.rules.charAt(j) == ']') {
            j += UCOL_ON;
            if (raw.startsWith("reorder") && (raw.length() == 7 || raw.charAt(7) == ' ')) {
                parseReordering(raw);
                this.ruleIndex = j;
                return;
            }
            if (raw.equals("backwards 2")) {
                this.settings.setFlag(NodeFilter.SHOW_NOTATION, true);
                this.ruleIndex = j;
                return;
            }
            String v;
            int valueIndex = raw.lastIndexOf(32);
            if (valueIndex >= 0) {
                v = raw.substring(valueIndex + UCOL_ON);
                raw = raw.substring(UCOL_OFF, valueIndex);
            } else {
                v = XmlPullParser.NO_NAMESPACE;
            }
            int value;
            if (raw.equals("strength") && v.length() == UCOL_ON) {
                value = UCOL_DEFAULT;
                char c = v.charAt(UCOL_OFF);
                if ('1' <= c && c <= '4') {
                    value = (c - 49) + UCOL_OFF;
                } else if (c == 'I') {
                    value = STRENGTH_MASK;
                }
                if (value != UCOL_DEFAULT) {
                    this.settings.setStrength(value);
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("alternate")) {
                value = UCOL_DEFAULT;
                if (v.equals("non-ignorable")) {
                    value = UCOL_OFF;
                } else {
                    if (v.equals("shifted")) {
                        value = UCOL_ON;
                    }
                }
                if (value != UCOL_DEFAULT) {
                    this.settings.setAlternateHandlingShifted(value > 0 ? true : -assertionsDisabled);
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("maxVariable")) {
                value = UCOL_DEFAULT;
                if (v.equals("space")) {
                    value = UCOL_OFF;
                } else {
                    if (v.equals("punct")) {
                        value = UCOL_ON;
                    } else {
                        if (v.equals("symbol")) {
                            value = 2;
                        } else {
                            if (v.equals("currency")) {
                                value = 3;
                            }
                        }
                    }
                }
                if (value != UCOL_DEFAULT) {
                    this.settings.setMaxVariable(value, UCOL_OFF);
                    this.settings.variableTop = this.baseData.getLastPrimaryForGroup(value + VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS);
                    if (!-assertionsDisabled) {
                        Object obj;
                        if (this.settings.variableTop != 0) {
                            obj = UCOL_ON;
                        } else {
                            obj = null;
                        }
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("caseFirst")) {
                value = UCOL_DEFAULT;
                if (v.equals("off")) {
                    value = UCOL_OFF;
                } else {
                    if (v.equals("lower")) {
                        value = NodeFilter.SHOW_DOCUMENT_TYPE;
                    } else {
                        if (v.equals("upper")) {
                            value = CollationSettings.CASE_FIRST_AND_UPPER_MASK;
                        }
                    }
                }
                if (value != UCOL_DEFAULT) {
                    this.settings.setCaseFirst(value);
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("caseLevel")) {
                value = getOnOffValue(v);
                if (value != UCOL_DEFAULT) {
                    this.settings.setFlag(NodeFilter.SHOW_DOCUMENT_FRAGMENT, value > 0 ? true : -assertionsDisabled);
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("normalization")) {
                value = getOnOffValue(v);
                if (value != UCOL_DEFAULT) {
                    this.settings.setFlag(UCOL_ON, value > 0 ? true : -assertionsDisabled);
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("numericOrdering")) {
                value = getOnOffValue(v);
                if (value != UCOL_DEFAULT) {
                    this.settings.setFlag(2, value > 0 ? true : -assertionsDisabled);
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("hiraganaQ")) {
                value = getOnOffValue(v);
                if (value != UCOL_DEFAULT) {
                    if (value == UCOL_ON) {
                        setParseError("[hiraganaQ on] is not supported");
                    }
                    this.ruleIndex = j;
                    return;
                }
            }
            if (raw.equals("import")) {
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
        if (this.rules.charAt(j) == '[') {
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
            }
            if (raw.equals("suppressContractions")) {
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
            i += UCOL_ON;
            int limit = i;
            while (limit < raw.length() && raw.charAt(limit) != ' ') {
                limit += UCOL_ON;
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
            int j = UCOL_OFF;
            for (Integer code2 : reorderCodes) {
                int j2 = j + UCOL_ON;
                codes[j] = code2.intValue();
                j = j2;
            }
            this.settings.setReordering(this.baseData, codes);
        }
    }

    public static int getReorderCode(String word) {
        for (int i = UCOL_OFF; i < gSpecialReorderCodes.length; i += UCOL_ON) {
            if (word.equalsIgnoreCase(gSpecialReorderCodes[i])) {
                return i + VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS;
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
            return Opcodes.OP_SPUT;
        }
        return UCOL_DEFAULT;
    }

    private static int getOnOffValue(String s) {
        if (s.equals("on")) {
            return UCOL_ON;
        }
        if (s.equals("off")) {
            return UCOL_OFF;
        }
        return UCOL_DEFAULT;
    }

    private int parseUnicodeSet(int i, UnicodeSet set) throws ParseException {
        int level = UCOL_OFF;
        int j = i;
        while (j != this.rules.length()) {
            int j2 = j + UCOL_ON;
            char c = this.rules.charAt(j);
            if (c == '[') {
                level += UCOL_ON;
            } else if (c == ']') {
                level += UCOL_DEFAULT;
                if (level == 0) {
                    try {
                        set.applyPattern(this.rules.substring(i, j2));
                    } catch (Exception e) {
                        setParseError("not a valid UnicodeSet pattern: " + e.getMessage());
                    }
                    j = skipWhiteSpace(j2);
                    if (j != this.rules.length() && this.rules.charAt(j) == ']') {
                        return j + UCOL_ON;
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
        raw.setLength(UCOL_OFF);
        i = skipWhiteSpace(i);
        while (i < this.rules.length()) {
            char c = this.rules.charAt(i);
            if (!isSyntaxChar(c) || c == '-' || c == '_') {
                if (PatternProps.isWhiteSpace(c)) {
                    raw.append(' ');
                    i = skipWhiteSpace(i + UCOL_ON);
                } else {
                    raw.append(c);
                    i += UCOL_ON;
                }
            } else if (raw.length() == 0) {
                return i;
            } else {
                int lastIndex = raw.length() + UCOL_DEFAULT;
                if (raw.charAt(lastIndex) == ' ') {
                    raw.setLength(lastIndex);
                }
                return i;
            }
        }
        return UCOL_OFF;
    }

    private int skipComment(int i) {
        while (i < this.rules.length()) {
            int i2 = i + UCOL_ON;
            char c = this.rules.charAt(i);
            if (c == '\n' || c == '\f' || c == '\r' || c == '\u0085' || c == '\u2028' || c == '\u2029') {
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
            start = UCOL_OFF;
        } else if (start > 0 && Character.isLowSurrogate(this.rules.charAt(start))) {
            start += UCOL_ON;
        }
        msg.append(this.rules, start, this.ruleIndex);
        msg.append('!');
        int length = this.rules.length() - this.ruleIndex;
        if (length >= U_PARSE_CONTEXT_LEN) {
            length = STRENGTH_MASK;
            if (Character.isHighSurrogate(this.rules.charAt((this.ruleIndex + STRENGTH_MASK) + UCOL_DEFAULT))) {
                length = 14;
            }
        }
        msg.append(this.rules, this.ruleIndex, this.ruleIndex + length);
        return msg.append('\"').toString();
    }

    private static boolean isSyntaxChar(int c) {
        if (33 > c || c > Opcodes.OP_NOT_LONG) {
            return -assertionsDisabled;
        }
        if (c <= 47) {
            return true;
        }
        if (58 <= c && c <= 64) {
            return true;
        }
        if ((91 > c || c > 96) && Opcodes.OP_NEG_INT > c) {
            return -assertionsDisabled;
        }
        return true;
    }

    private int skipWhiteSpace(int i) {
        while (i < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(i))) {
            i += UCOL_ON;
        }
        return i;
    }
}
