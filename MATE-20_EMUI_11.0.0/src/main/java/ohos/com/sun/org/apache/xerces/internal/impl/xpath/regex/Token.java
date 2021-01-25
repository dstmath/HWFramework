package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import ohos.agp.styles.attributes.ViewAttrsConstants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.data.search.model.SearchParameter;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.text.SymbolTable;

/* access modifiers changed from: package-private */
public class Token implements Serializable {
    static final int ANCHOR = 8;
    static final int BACKREFERENCE = 12;
    static final int CHAR = 0;
    static final int CHAR_FINAL_QUOTE = 30;
    static final int CHAR_INIT_QUOTE = 29;
    static final int CHAR_LETTER = 31;
    static final int CHAR_MARK = 32;
    static final int CHAR_NUMBER = 33;
    static final int CHAR_OTHER = 35;
    static final int CHAR_PUNCTUATION = 36;
    static final int CHAR_SEPARATOR = 34;
    static final int CHAR_SYMBOL = 37;
    static final int CLOSURE = 3;
    static final int CONCAT = 1;
    static final int CONDITION = 26;
    static final boolean COUNTTOKENS = true;
    static final int DOT = 11;
    static final int EMPTY = 7;
    static final int FC_ANY = 2;
    static final int FC_CONTINUE = 0;
    static final int FC_TERMINAL = 1;
    static final int INDEPENDENT = 24;
    static final int LOOKAHEAD = 20;
    static final int LOOKBEHIND = 22;
    static final int MODIFIERGROUP = 25;
    static final int NEGATIVELOOKAHEAD = 21;
    static final int NEGATIVELOOKBEHIND = 23;
    private static final int NONBMP_BLOCK_START = 84;
    static final int NONGREEDYCLOSURE = 9;
    static final int NRANGE = 5;
    static final int PAREN = 6;
    static final int RANGE = 4;
    static final int STRING = 10;
    static final int UNION = 2;
    static final int UTF16_MAX = 1114111;
    private static final String[] blockNames = {"Basic Latin", "Latin-1 Supplement", "Latin Extended-A", "Latin Extended-B", "IPA Extensions", "Spacing Modifier Letters", "Combining Diacritical Marks", "Greek", "Cyrillic", "Armenian", "Hebrew", "Arabic", "Syriac", "Thaana", "Devanagari", "Bengali", "Gurmukhi", "Gujarati", "Oriya", "Tamil", "Telugu", "Kannada", "Malayalam", "Sinhala", "Thai", "Lao", "Tibetan", "Myanmar", "Georgian", "Hangul Jamo", "Ethiopic", "Cherokee", "Unified Canadian Aboriginal Syllabics", "Ogham", "Runic", "Khmer", "Mongolian", "Latin Extended Additional", "Greek Extended", "General Punctuation", "Superscripts and Subscripts", "Currency Symbols", "Combining Marks for Symbols", "Letterlike Symbols", "Number Forms", "Arrows", "Mathematical Operators", "Miscellaneous Technical", "Control Pictures", "Optical Character Recognition", "Enclosed Alphanumerics", "Box Drawing", "Block Elements", "Geometric Shapes", "Miscellaneous Symbols", "Dingbats", "Braille Patterns", "CJK Radicals Supplement", "Kangxi Radicals", "Ideographic Description Characters", "CJK Symbols and Punctuation", "Hiragana", "Katakana", "Bopomofo", "Hangul Compatibility Jamo", "Kanbun", "Bopomofo Extended", "Enclosed CJK Letters and Months", "CJK Compatibility", "CJK Unified Ideographs Extension A", "CJK Unified Ideographs", "Yi Syllables", "Yi Radicals", "Hangul Syllables", "Private Use", "CJK Compatibility Ideographs", "Alphabetic Presentation Forms", "Arabic Presentation Forms-A", "Combining Half Marks", "CJK Compatibility Forms", "Small Form Variants", "Arabic Presentation Forms-B", "Specials", "Halfwidth and Fullwidth Forms", "Old Italic", "Gothic", "Deseret", "Byzantine Musical Symbols", "Musical Symbols", "Mathematical Alphanumeric Symbols", "CJK Unified Ideographs Extension B", "CJK Compatibility Ideographs Supplement", "Tags"};
    static final String blockRanges = "\u0000ÿĀſƀɏɐʯʰ˿̀ͯͰϿЀӿ԰֏֐׿؀ۿ܀ݏހ޿ऀॿঀ৿਀੿઀૿଀୿஀௿ఀ౿ಀ೿ഀൿ඀෿฀๿຀໿ༀ࿿က႟Ⴀჿᄀᇿሀ፿Ꭰ᏿᐀ᙿ ᚟ᚠ᛿ក៿᠀᢯Ḁỿἀ῿ ⁯⁰₟₠⃏⃐⃿℀⅏⅐↏←⇿∀⋿⌀⏿␀␿⑀⑟①⓿─╿▀▟■◿☀⛿✀➿⠀⣿⺀⻿⼀⿟⿰⿿　〿぀ゟ゠ヿ㄀ㄯ㄰㆏㆐㆟ㆠㆿ㈀㋿㌀㏿㐀䶵一鿿ꀀ꒏꒐꓏가힣豈﫿ﬀﭏﭐ﷿︠︯︰﹏﹐﹯ﹰ﻾﻿﻿＀￯";
    private static final Map<String, Token> categories = new HashMap();
    private static final Map<String, Token> categories2 = new HashMap();
    private static final String[] categoryNames = {"Cn", "Lu", "Ll", "Lt", "Lm", "Lo", "Mn", "Me", "Mc", "Nd", "Nl", "No", "Zs", "Zl", "Zp", "Cc", "Cf", null, "Co", "Cs", "Pd", "Ps", "Pe", "Pc", "Po", "Sm", "Sc", "Sk", "So", "Pi", "Pf", "L", DateFormat.NUM_MONTH, "N", Constants.HASIDCALL_INDEX_SIG, "C", "P", "S"};
    static final int[] nonBMPBlockRanges = {66304, 66351, 66352, 66383, 66560, 66639, 118784, 119039, 119040, 119295, 119808, 120831, 131072, 173782, 194560, 195103, 917504, 917631};
    static final Set<String> nonxs = Collections.synchronizedSet(new HashSet());
    private static final long serialVersionUID = 8484976002585487481L;
    static Token token_0to9 = createRange();
    private static Token token_ccs = null;
    static Token token_dot = new Token(11);
    static Token token_empty = new Token(7);
    private static Token token_grapheme = null;
    static Token token_linebeginning = createAnchor(94);
    static Token token_linebeginning2 = createAnchor(64);
    static Token token_lineend = createAnchor(36);
    static Token token_not_0to9 = complementRanges(token_0to9);
    static Token token_not_spaces = complementRanges(token_spaces);
    static Token token_not_wordchars = complementRanges(token_wordchars);
    static Token token_not_wordedge = createAnchor(66);
    static Token token_spaces = createRange();
    static Token token_stringbeginning = createAnchor(65);
    static Token token_stringend = createAnchor(122);
    static Token token_stringend2 = createAnchor(90);
    static Token token_wordbeginning = createAnchor(60);
    static Token token_wordchars = createRange();
    static Token token_wordedge = createAnchor(98);
    static Token token_wordend = createAnchor(62);
    static int tokens = 0;
    static final String viramaString = "्্੍્୍்్್്ฺ྄";
    final int type;

    private static final boolean isSet(int i, int i2) {
        return (i & i2) == i2;
    }

    /* access modifiers changed from: package-private */
    public int getChar() {
        return -1;
    }

    /* access modifiers changed from: package-private */
    public Token getChild(int i) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getMax() {
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int getMin() {
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int getParenNumber() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getReferenceNumber() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public String getString() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setMax(int i) {
    }

    /* access modifiers changed from: package-private */
    public void setMin(int i) {
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return 0;
    }

    static {
        token_0to9.addRange(48, 57);
        token_wordchars.addRange(48, 57);
        token_wordchars.addRange(65, 90);
        token_wordchars.addRange(95, 95);
        token_wordchars.addRange(97, 122);
        token_spaces.addRange(9, 9);
        token_spaces.addRange(10, 10);
        token_spaces.addRange(12, 12);
        token_spaces.addRange(13, 13);
        token_spaces.addRange(32, 32);
    }

    static ParenToken createLook(int i, Token token) {
        tokens++;
        return new ParenToken(i, token, 0);
    }

    static ParenToken createParen(Token token, int i) {
        tokens++;
        return new ParenToken(6, token, i);
    }

    static ClosureToken createClosure(Token token) {
        tokens++;
        return new ClosureToken(3, token);
    }

    static ClosureToken createNGClosure(Token token) {
        tokens++;
        return new ClosureToken(9, token);
    }

    static ConcatToken createConcat(Token token, Token token2) {
        tokens++;
        return new ConcatToken(token, token2);
    }

    static UnionToken createConcat() {
        tokens++;
        return new UnionToken(1);
    }

    static UnionToken createUnion() {
        tokens++;
        return new UnionToken(2);
    }

    static Token createEmpty() {
        return token_empty;
    }

    static RangeToken createRange() {
        tokens++;
        return new RangeToken(4);
    }

    static RangeToken createNRange() {
        tokens++;
        return new RangeToken(5);
    }

    static CharToken createChar(int i) {
        tokens++;
        return new CharToken(0, i);
    }

    private static CharToken createAnchor(int i) {
        tokens++;
        return new CharToken(8, i);
    }

    static StringToken createBackReference(int i) {
        tokens++;
        return new StringToken(12, null, i);
    }

    static StringToken createString(String str) {
        tokens++;
        return new StringToken(10, str, 0);
    }

    static ModifierToken createModifierGroup(Token token, int i, int i2) {
        tokens++;
        return new ModifierToken(token, i, i2);
    }

    static ConditionToken createCondition(int i, Token token, Token token2, Token token3) {
        tokens++;
        return new ConditionToken(i, token, token2, token3);
    }

    protected Token(int i) {
        this.type = i;
    }

    /* access modifiers changed from: package-private */
    public void addChild(Token token) {
        throw new RuntimeException("Not supported.");
    }

    /* access modifiers changed from: protected */
    public void addRange(int i, int i2) {
        throw new RuntimeException("Not supported.");
    }

    /* access modifiers changed from: protected */
    public void sortRanges() {
        throw new RuntimeException("Not supported.");
    }

    /* access modifiers changed from: protected */
    public void compactRanges() {
        throw new RuntimeException("Not supported.");
    }

    /* access modifiers changed from: protected */
    public void mergeRanges(Token token) {
        throw new RuntimeException("Not supported.");
    }

    /* access modifiers changed from: protected */
    public void subtractRanges(Token token) {
        throw new RuntimeException("Not supported.");
    }

    /* access modifiers changed from: protected */
    public void intersectRanges(Token token) {
        throw new RuntimeException("Not supported.");
    }

    static Token complementRanges(Token token) {
        return RangeToken.complementRanges(token);
    }

    @Override // java.lang.Object
    public String toString() {
        return toString(0);
    }

    public String toString(int i) {
        return this.type == 11 ? "." : "";
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0023 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002e  */
    public final int getMinLength() {
        int i = this.type;
        switch (i) {
            case 0:
            case 4:
            case 5:
            case 11:
                return 1;
            case 1:
                int i2 = 0;
                for (int i3 = 0; i3 < size(); i3++) {
                    i2 += getChild(i3).getMinLength();
                }
                return i2;
            case 2:
                if (size() == 0) {
                    return 0;
                }
                int minLength = getChild(0).getMinLength();
                for (int i4 = 1; i4 < size(); i4++) {
                    int minLength2 = getChild(i4).getMinLength();
                    if (minLength2 < minLength) {
                        minLength = minLength2;
                    }
                }
                return minLength;
            case 3:
            case 9:
                if (getMin() >= 0) {
                    return getMin() * getChild(0).getMinLength();
                }
                return 0;
            case 6:
                return getChild(0).getMinLength();
            case 7:
            case 8:
                return 0;
            case 10:
                return getString().length();
            case 12:
                return 0;
            default:
                switch (i) {
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        break;
                    case 24:
                    case 25:
                        break;
                    case 26:
                        break;
                    default:
                        throw new RuntimeException("Token#getMinLength(): Invalid Type: " + this.type);
                }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0050  */
    public final int getMaxLength() {
        int i = this.type;
        int i2 = 1;
        switch (i) {
            case 0:
                return 1;
            case 1:
                int i3 = 0;
                for (int i4 = 0; i4 < size(); i4++) {
                    int maxLength = getChild(i4).getMaxLength();
                    if (maxLength < 0) {
                        return -1;
                    }
                    i3 += maxLength;
                }
                return i3;
            case 2:
                if (size() == 0) {
                    return 0;
                }
                int maxLength2 = getChild(0).getMaxLength();
                while (maxLength2 >= 0 && i2 < size()) {
                    int maxLength3 = getChild(i2).getMaxLength();
                    if (maxLength3 < 0) {
                        return -1;
                    }
                    if (maxLength3 > maxLength2) {
                        maxLength2 = maxLength3;
                    }
                    i2++;
                }
                return maxLength2;
            case 3:
            case 9:
                if (getMax() >= 0) {
                    return getMax() * getChild(0).getMaxLength();
                }
                return -1;
            case 4:
            case 5:
            case 11:
                return 2;
            case 6:
                return getChild(0).getMaxLength();
            case 7:
            case 8:
                return 0;
            case 10:
                return getString().length();
            case 12:
                return -1;
            default:
                switch (i) {
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        return 0;
                    case 24:
                    case 25:
                        break;
                    case 26:
                        break;
                    default:
                        throw new RuntimeException("Token#getMaxLength(): Invalid Type: " + this.type);
                }
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Failed to find switch 'out' block
        	at jadx.core.dex.visitors.regions.RegionMaker.processSwitch(RegionMaker.java:821)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:157)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
        	at jadx.core.dex.visitors.regions.RegionMaker.processSwitch(RegionMaker.java:860)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:157)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:50)
        */
    final int analyzeFirstCharacter(ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RangeToken r8, int r9) {
        /*
        // Method dump skipped, instructions count: 344
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token.analyzeFirstCharacter(ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RangeToken, int):int");
    }

    private final boolean isShorterThan(Token token) {
        if (token == null) {
            return false;
        }
        if (this.type == 10) {
            int length = getString().length();
            if (token.type != 10) {
                throw new RuntimeException("Internal Error: Illegal type: " + token.type);
            } else if (length < token.getString().length()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new RuntimeException("Internal Error: Illegal type: " + this.type);
        }
    }

    /* access modifiers changed from: package-private */
    public static class FixedStringContainer {
        int options = 0;
        Token token = null;

        FixedStringContainer() {
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x003e  */
    public final void findFixedString(FixedStringContainer fixedStringContainer, int i) {
        int i2 = this.type;
        Token token = null;
        switch (i2) {
            case 0:
                fixedStringContainer.token = null;
                return;
            case 1:
                int i3 = 0;
                for (int i4 = 0; i4 < size(); i4++) {
                    getChild(i4).findFixedString(fixedStringContainer, i);
                    if (token == null || token.isShorterThan(fixedStringContainer.token)) {
                        token = fixedStringContainer.token;
                        i3 = fixedStringContainer.options;
                    }
                }
                fixedStringContainer.token = token;
                fixedStringContainer.options = i3;
                return;
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
            case 8:
            case 9:
            case 11:
            case 12:
                fixedStringContainer.token = null;
                return;
            case 6:
                getChild(0).findFixedString(fixedStringContainer, i);
                return;
            case 10:
                fixedStringContainer.token = this;
                fixedStringContainer.options = i;
                return;
            default:
                switch (i2) {
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 26:
                        break;
                    case 24:
                        break;
                    case 25:
                        ModifierToken modifierToken = (ModifierToken) this;
                        getChild(0).findFixedString(fixedStringContainer, (i | modifierToken.getOptions()) & (~modifierToken.getOptionsMask()));
                        return;
                    default:
                        throw new RuntimeException("Token#findFixedString(): Invalid Type: " + this.type);
                }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean match(int i) {
        throw new RuntimeException("NFAArrow#match(): Internal error: " + this.type);
    }

    protected static RangeToken getRange(String str, boolean z) {
        if (categories.size() == 0) {
            synchronized (categories) {
                Token[] tokenArr = new Token[categoryNames.length];
                for (int i = 0; i < tokenArr.length; i++) {
                    tokenArr[i] = createRange();
                }
                int i2 = 0;
                while (true) {
                    char c = '\"';
                    if (i2 < 65536) {
                        int type2 = Character.getType((char) i2);
                        if (type2 == 21 || type2 == 22) {
                            if (i2 == 171 || i2 == 8216 || i2 == 8219 || i2 == 8220 || i2 == 8223 || i2 == 8249) {
                                type2 = 29;
                            }
                            if (i2 == 187 || i2 == 8217 || i2 == 8221 || i2 == 8250) {
                                type2 = 30;
                            }
                        }
                        tokenArr[type2].addRange(i2, i2);
                        switch (type2) {
                            case 0:
                            case 15:
                            case 16:
                            case 18:
                            case 19:
                                c = '#';
                                break;
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                                c = 31;
                                break;
                            case 6:
                            case 7:
                            case 8:
                                c = ' ';
                                break;
                            case 9:
                            case 10:
                            case 11:
                                c = '!';
                                break;
                            case 12:
                            case 13:
                            case 14:
                                break;
                            case 17:
                            default:
                                throw new RuntimeException("org.apache.xerces.utils.regex.Token#getRange(): Unknown Unicode category: " + type2);
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 29:
                            case 30:
                                c = SymbolTable.SYMBOL_REF;
                                break;
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                                c = '%';
                                break;
                        }
                        tokenArr[c].addRange(i2, i2);
                        i2++;
                    } else {
                        tokenArr[0].addRange(65536, 1114111);
                        for (int i3 = 0; i3 < tokenArr.length; i3++) {
                            if (categoryNames[i3] != null) {
                                if (i3 == 0) {
                                    tokenArr[i3].addRange(65536, 1114111);
                                }
                                categories.put(categoryNames[i3], tokenArr[i3]);
                                categories2.put(categoryNames[i3], complementRanges(tokenArr[i3]));
                            }
                        }
                        StringBuilder sb = new StringBuilder(50);
                        for (int i4 = 0; i4 < blockNames.length; i4++) {
                            RangeToken createRange = createRange();
                            if (i4 < 84) {
                                int i5 = i4 * 2;
                                createRange.addRange(blockRanges.charAt(i5), blockRanges.charAt(i5 + 1));
                            } else {
                                int i6 = (i4 - 84) * 2;
                                createRange.addRange(nonBMPBlockRanges[i6], nonBMPBlockRanges[i6 + 1]);
                            }
                            String str2 = blockNames[i4];
                            if (str2.equals("Specials")) {
                                createRange.addRange(65520, UCharacter.REPLACEMENT_CHAR);
                            }
                            if (str2.equals("Private Use")) {
                                createRange.addRange(983040, 1048573);
                                createRange.addRange(1048576, 1114109);
                            }
                            categories.put(str2, createRange);
                            categories2.put(str2, complementRanges(createRange));
                            sb.setLength(0);
                            sb.append("Is");
                            if (str2.indexOf(32) >= 0) {
                                for (int i7 = 0; i7 < str2.length(); i7++) {
                                    if (str2.charAt(i7) != ' ') {
                                        sb.append(str2.charAt(i7));
                                    }
                                }
                            } else {
                                sb.append(str2);
                            }
                            setAlias(sb.toString(), str2, true);
                        }
                        setAlias("ASSIGNED", "Cn", false);
                        setAlias("UNASSIGNED", "Cn", true);
                        RangeToken createRange2 = createRange();
                        createRange2.addRange(0, 1114111);
                        categories.put("ALL", createRange2);
                        categories2.put("ALL", complementRanges(createRange2));
                        registerNonXS("ASSIGNED");
                        registerNonXS("UNASSIGNED");
                        registerNonXS("ALL");
                        RangeToken createRange3 = createRange();
                        createRange3.mergeRanges(tokenArr[1]);
                        createRange3.mergeRanges(tokenArr[2]);
                        createRange3.mergeRanges(tokenArr[5]);
                        categories.put("IsAlpha", createRange3);
                        categories2.put("IsAlpha", complementRanges(createRange3));
                        registerNonXS("IsAlpha");
                        RangeToken createRange4 = createRange();
                        createRange4.mergeRanges(createRange3);
                        createRange4.mergeRanges(tokenArr[9]);
                        categories.put("IsAlnum", createRange4);
                        categories2.put("IsAlnum", complementRanges(createRange4));
                        registerNonXS("IsAlnum");
                        RangeToken createRange5 = createRange();
                        createRange5.mergeRanges(token_spaces);
                        createRange5.mergeRanges(tokenArr[34]);
                        categories.put("IsSpace", createRange5);
                        categories2.put("IsSpace", complementRanges(createRange5));
                        registerNonXS("IsSpace");
                        RangeToken createRange6 = createRange();
                        createRange6.mergeRanges(createRange4);
                        createRange6.addRange(95, 95);
                        categories.put("IsWord", createRange6);
                        categories2.put("IsWord", complementRanges(createRange6));
                        registerNonXS("IsWord");
                        RangeToken createRange7 = createRange();
                        createRange7.addRange(0, 127);
                        categories.put("IsASCII", createRange7);
                        categories2.put("IsASCII", complementRanges(createRange7));
                        registerNonXS("IsASCII");
                        RangeToken createRange8 = createRange();
                        createRange8.mergeRanges(tokenArr[35]);
                        createRange8.addRange(32, 32);
                        categories.put("IsGraph", complementRanges(createRange8));
                        categories2.put("IsGraph", createRange8);
                        registerNonXS("IsGraph");
                        RangeToken createRange9 = createRange();
                        createRange9.addRange(48, 57);
                        createRange9.addRange(65, 70);
                        createRange9.addRange(97, 102);
                        categories.put("IsXDigit", complementRanges(createRange9));
                        categories2.put("IsXDigit", createRange9);
                        registerNonXS("IsXDigit");
                        setAlias("IsDigit", "Nd", true);
                        setAlias("IsUpper", "Lu", true);
                        setAlias("IsLower", "Ll", true);
                        setAlias("IsCntrl", "C", true);
                        setAlias("IsPrint", "C", false);
                        setAlias("IsPunct", "P", true);
                        registerNonXS("IsDigit");
                        registerNonXS("IsUpper");
                        registerNonXS("IsLower");
                        registerNonXS("IsCntrl");
                        registerNonXS("IsPrint");
                        registerNonXS("IsPunct");
                        setAlias(ViewAttrsConstants.ALPHA, "IsAlpha", true);
                        setAlias("alnum", "IsAlnum", true);
                        setAlias("ascii", "IsASCII", true);
                        setAlias("cntrl", "IsCntrl", true);
                        setAlias(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_DIGIT, "IsDigit", true);
                        setAlias("graph", "IsGraph", true);
                        setAlias(SearchParameter.LOWER, "IsLower", true);
                        setAlias("print", "IsPrint", true);
                        setAlias("punct", "IsPunct", true);
                        setAlias("space", "IsSpace", true);
                        setAlias(SearchParameter.UPPER, "IsUpper", true);
                        setAlias("word", "IsWord", true);
                        setAlias("xdigit", "IsXDigit", true);
                        registerNonXS(ViewAttrsConstants.ALPHA);
                        registerNonXS("alnum");
                        registerNonXS("ascii");
                        registerNonXS("cntrl");
                        registerNonXS(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_DIGIT);
                        registerNonXS("graph");
                        registerNonXS(SearchParameter.LOWER);
                        registerNonXS("print");
                        registerNonXS("punct");
                        registerNonXS("space");
                        registerNonXS(SearchParameter.UPPER);
                        registerNonXS("word");
                        registerNonXS("xdigit");
                    }
                }
            }
        }
        if (z) {
            return (RangeToken) categories.get(str);
        }
        return (RangeToken) categories2.get(str);
    }

    protected static RangeToken getRange(String str, boolean z, boolean z2) {
        RangeToken range = getRange(str, z);
        if (!z2 || range == null || !isRegisterNonXS(str)) {
            return range;
        }
        return null;
    }

    protected static void registerNonXS(String str) {
        nonxs.add(str);
    }

    protected static boolean isRegisterNonXS(String str) {
        return nonxs.contains(str);
    }

    private static void setAlias(String str, String str2, boolean z) {
        Token token = categories.get(str2);
        Token token2 = categories2.get(str2);
        if (z) {
            categories.put(str, token);
            categories2.put(str, token2);
            return;
        }
        categories2.put(str, token);
        categories.put(str, token2);
    }

    static synchronized Token getGraphemePattern() {
        synchronized (Token.class) {
            if (token_grapheme != null) {
                return token_grapheme;
            }
            RangeToken createRange = createRange();
            createRange.mergeRanges(getRange("ASSIGNED", true));
            createRange.subtractRanges(getRange(DateFormat.NUM_MONTH, true));
            createRange.subtractRanges(getRange("C", true));
            RangeToken createRange2 = createRange();
            for (int i = 0; i < 11; i++) {
                createRange2.addRange(i, i);
            }
            RangeToken createRange3 = createRange();
            createRange3.mergeRanges(getRange(DateFormat.NUM_MONTH, true));
            createRange3.addRange(4448, 4607);
            createRange3.addRange(65438, 65439);
            UnionToken createUnion = createUnion();
            createUnion.addChild(createRange);
            createUnion.addChild(token_empty);
            UnionToken createUnion2 = createUnion();
            createUnion2.addChild(createConcat(createRange2, getRange("L", true)));
            createUnion2.addChild(createRange3);
            token_grapheme = createConcat(createUnion, createClosure(createUnion2));
            return token_grapheme;
        }
    }

    static synchronized Token getCombiningCharacterSequence() {
        synchronized (Token.class) {
            if (token_ccs != null) {
                return token_ccs;
            }
            token_ccs = createConcat(getRange(DateFormat.NUM_MONTH, false), createClosure(getRange(DateFormat.NUM_MONTH, true)));
            return token_ccs;
        }
    }

    /* access modifiers changed from: package-private */
    public static class StringToken extends Token implements Serializable {
        private static final long serialVersionUID = -4614366944218504172L;
        final int refNumber;
        String string;

        StringToken(int i, String str, int i2) {
            super(i);
            this.string = str;
            this.refNumber = i2;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int getReferenceNumber() {
            return this.refNumber;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String getString() {
            return this.string;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            if (this.type != 12) {
                return REUtil.quoteMeta(this.string);
            }
            return "\\" + this.refNumber;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ConcatToken extends Token implements Serializable {
        private static final long serialVersionUID = 8717321425541346381L;
        final Token child;
        final Token child2;

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int size() {
            return 2;
        }

        ConcatToken(Token token, Token token2) {
            super(1);
            this.child = token;
            this.child2 = token2;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public Token getChild(int i) {
            return i == 0 ? this.child : this.child2;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            if (this.child2.type == 3 && this.child2.getChild(0) == this.child) {
                return this.child.toString(i) + "+";
            } else if (this.child2.type == 9 && this.child2.getChild(0) == this.child) {
                return this.child.toString(i) + "+?";
            } else {
                return this.child.toString(i) + this.child2.toString(i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class CharToken extends Token implements Serializable {
        private static final long serialVersionUID = -4394272816279496989L;
        final int chardata;

        CharToken(int i, int i2) {
            super(i);
            this.chardata = i2;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int getChar() {
            return this.chardata;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            int i2 = this.type;
            if (i2 == 0) {
                int i3 = this.chardata;
                if (i3 == 9) {
                    return "\\t";
                }
                if (i3 == 10) {
                    return "\\n";
                }
                if (i3 == 12) {
                    return "\\f";
                }
                if (i3 == 13) {
                    return "\\r";
                }
                if (i3 == 27) {
                    return "\\e";
                }
                if (!(i3 == 46 || i3 == 63 || i3 == 91 || i3 == 92 || i3 == 123 || i3 == 124)) {
                    switch (i3) {
                        case 40:
                        case 41:
                        case 42:
                        case 43:
                            break;
                        default:
                            if (i3 >= 65536) {
                                String str = "0" + Integer.toHexString(this.chardata);
                                return "\\v" + str.substring(str.length() - 6, str.length());
                            }
                            return "" + ((char) this.chardata);
                    }
                }
                return "\\" + ((char) this.chardata);
            } else if (i2 != 8) {
                return null;
            } else {
                if (this == Token.token_linebeginning || this == Token.token_lineend) {
                    return "" + ((char) this.chardata);
                }
                return "\\" + ((char) this.chardata);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public boolean match(int i) {
            if (this.type == 0) {
                return i == this.chardata;
            }
            throw new RuntimeException("NFAArrow#match(): Internal error: " + this.type);
        }
    }

    /* access modifiers changed from: package-private */
    public static class ClosureToken extends Token implements Serializable {
        private static final long serialVersionUID = 1308971930673997452L;
        final Token child;
        int max;
        int min;

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int size() {
            return 1;
        }

        ClosureToken(int i, Token token) {
            super(i);
            this.child = token;
            setMin(-1);
            setMax(-1);
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public Token getChild(int i) {
            return this.child;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public final void setMin(int i) {
            this.min = i;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public final void setMax(int i) {
            this.max = i;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public final int getMin() {
            return this.min;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public final int getMax() {
            return this.max;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            if (this.type == 3) {
                if (getMin() < 0 && getMax() < 0) {
                    return this.child.toString(i) + "*";
                } else if (getMin() == getMax()) {
                    return this.child.toString(i) + "{" + getMin() + "}";
                } else if (getMin() >= 0 && getMax() >= 0) {
                    return this.child.toString(i) + "{" + getMin() + "," + getMax() + "}";
                } else if (getMin() < 0 || getMax() >= 0) {
                    throw new RuntimeException("Token#toString(): CLOSURE " + getMin() + ", " + getMax());
                } else {
                    return this.child.toString(i) + "{" + getMin() + ",}";
                }
            } else if (getMin() < 0 && getMax() < 0) {
                return this.child.toString(i) + "*?";
            } else if (getMin() == getMax()) {
                return this.child.toString(i) + "{" + getMin() + "}?";
            } else if (getMin() >= 0 && getMax() >= 0) {
                return this.child.toString(i) + "{" + getMin() + "," + getMax() + "}?";
            } else if (getMin() < 0 || getMax() >= 0) {
                throw new RuntimeException("Token#toString(): NONGREEDYCLOSURE " + getMin() + ", " + getMax());
            } else {
                return this.child.toString(i) + "{" + getMin() + ",}?";
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class ParenToken extends Token implements Serializable {
        private static final long serialVersionUID = -5938014719827987704L;
        final Token child;
        final int parennumber;

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int size() {
            return 1;
        }

        ParenToken(int i, Token token, int i2) {
            super(i);
            this.child = token;
            this.parennumber = i2;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public Token getChild(int i) {
            return this.child;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int getParenNumber() {
            return this.parennumber;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            int i2 = this.type;
            if (i2 != 6) {
                switch (i2) {
                    case 20:
                        return "(?=" + this.child.toString(i) + ")";
                    case 21:
                        return "(?!" + this.child.toString(i) + ")";
                    case 22:
                        return "(?<=" + this.child.toString(i) + ")";
                    case 23:
                        return "(?<!" + this.child.toString(i) + ")";
                    case 24:
                        return "(?>" + this.child.toString(i) + ")";
                    default:
                        return null;
                }
            } else if (this.parennumber == 0) {
                return "(?:" + this.child.toString(i) + ")";
            } else {
                return "(" + this.child.toString(i) + ")";
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class ConditionToken extends Token implements Serializable {
        private static final long serialVersionUID = 4353765277910594411L;
        final Token condition;
        final Token no;
        final int refNumber;
        final Token yes;

        ConditionToken(int i, Token token, Token token2, Token token3) {
            super(26);
            this.refNumber = i;
            this.condition = token;
            this.yes = token2;
            this.no = token3;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int size() {
            return this.no == null ? 1 : 2;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public Token getChild(int i) {
            if (i == 0) {
                return this.yes;
            }
            if (i == 1) {
                return this.no;
            }
            throw new RuntimeException("Internal Error: " + i);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            String str;
            if (this.refNumber > 0) {
                str = "(?(" + this.refNumber + ")";
            } else if (this.condition.type == 8) {
                str = "(?(" + this.condition + ")";
            } else {
                str = "(?" + this.condition;
            }
            if (this.no == null) {
                return str + this.yes + ")";
            }
            return str + this.yes + "|" + this.no + ")";
        }
    }

    /* access modifiers changed from: package-private */
    public static class ModifierToken extends Token implements Serializable {
        private static final long serialVersionUID = -9114536559696480356L;
        final int add;
        final Token child;
        final int mask;

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int size() {
            return 1;
        }

        ModifierToken(Token token, int i, int i2) {
            super(25);
            this.child = token;
            this.add = i;
            this.mask = i2;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public Token getChild(int i) {
            return this.child;
        }

        /* access modifiers changed from: package-private */
        public int getOptions() {
            return this.add;
        }

        /* access modifiers changed from: package-private */
        public int getOptionsMask() {
            return this.mask;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            StringBuilder sb = new StringBuilder();
            sb.append("(?");
            int i2 = this.add;
            String str = "";
            sb.append(i2 == 0 ? str : REUtil.createOptionString(i2));
            int i3 = this.mask;
            if (i3 != 0) {
                str = REUtil.createOptionString(i3);
            }
            sb.append(str);
            sb.append(":");
            sb.append(this.child.toString(i));
            sb.append(")");
            return sb.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public static class UnionToken extends Token implements Serializable {
        private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ELEMNAME_CHILDREN_STRING, Vector.class)};
        private static final long serialVersionUID = -2568843945989489861L;
        List<Token> children;

        UnionToken(int i) {
            super(i);
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public void addChild(Token token) {
            StringBuilder sb;
            if (token != null) {
                if (this.children == null) {
                    this.children = new ArrayList();
                }
                if (this.type == 2) {
                    this.children.add(token);
                } else if (token.type == 1) {
                    for (int i = 0; i < token.size(); i++) {
                        addChild(token.getChild(i));
                    }
                } else {
                    int size = this.children.size();
                    if (size == 0) {
                        this.children.add(token);
                        return;
                    }
                    int i2 = size - 1;
                    Token token2 = this.children.get(i2);
                    if ((token2.type == 0 || token2.type == 10) && (token.type == 0 || token.type == 10)) {
                        int length = token.type == 0 ? 2 : token.getString().length();
                        if (token2.type == 0) {
                            sb = new StringBuilder(length + 2);
                            int i3 = token2.getChar();
                            if (i3 >= 65536) {
                                sb.append(REUtil.decomposeToSurrogates(i3));
                            } else {
                                sb.append((char) i3);
                            }
                            token2 = Token.createString(null);
                            this.children.set(i2, token2);
                        } else {
                            sb = new StringBuilder(token2.getString().length() + length);
                            sb.append(token2.getString());
                        }
                        if (token.type == 0) {
                            int i4 = token.getChar();
                            if (i4 >= 65536) {
                                sb.append(REUtil.decomposeToSurrogates(i4));
                            } else {
                                sb.append((char) i4);
                            }
                        } else {
                            sb.append(token.getString());
                        }
                        ((StringToken) token2).string = new String(sb);
                        return;
                    }
                    this.children.add(token);
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public int size() {
            List<Token> list = this.children;
            if (list == null) {
                return 0;
            }
            return list.size();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public Token getChild(int i) {
            return this.children.get(i);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
        public String toString(int i) {
            if (this.type == 1) {
                if (this.children.size() == 2) {
                    Token child = getChild(0);
                    Token child2 = getChild(1);
                    if (child2.type == 3 && child2.getChild(0) == child) {
                        return child.toString(i) + "+";
                    } else if (child2.type == 9 && child2.getChild(0) == child) {
                        return child.toString(i) + "+?";
                    } else {
                        return child.toString(i) + child2.toString(i);
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i2 = 0; i2 < this.children.size(); i2++) {
                        sb.append(this.children.get(i2).toString(i));
                    }
                    return new String(sb);
                }
            } else if (this.children.size() == 2 && getChild(1).type == 7) {
                return getChild(0).toString(i) + "?";
            } else if (this.children.size() == 2 && getChild(0).type == 7) {
                return getChild(1).toString(i) + "??";
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(this.children.get(0).toString(i));
                for (int i3 = 1; i3 < this.children.size(); i3++) {
                    sb2.append('|');
                    sb2.append(this.children.get(i3).toString(i));
                }
                return new String(sb2);
            }
        }

        private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
            List<Token> list = this.children;
            objectOutputStream.putFields().put(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ELEMNAME_CHILDREN_STRING, list == null ? null : new Vector(list));
            objectOutputStream.writeFields();
        }

        private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
            Vector vector = (Vector) objectInputStream.readFields().get(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ELEMNAME_CHILDREN_STRING, (Object) null);
            if (vector != null) {
                this.children = new ArrayList(vector);
            }
        }
    }
}
