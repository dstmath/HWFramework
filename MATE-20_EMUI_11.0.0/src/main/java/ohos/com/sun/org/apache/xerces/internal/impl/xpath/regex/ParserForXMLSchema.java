package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token;

/* access modifiers changed from: package-private */
public class ParserForXMLSchema extends RegexParser {
    private static final String DIGITS = "09٠٩۰۹०९০৯੦੯૦૯୦୯௧௯౦౯೦೯൦൯๐๙໐໙༠༩၀၉፩፱០៩᠐᠙０９";
    private static final int[] DIGITS_INT = {120782, 120831};
    private static final String LETTERS = "AZazÀÖØöøıĴľŁňŊžƀǰǴǵǺȗɐʨʻˁʰˑΆΆΈΊΌΌΎΡΣώϐϖϚϚϜϜϞϞϠϠϢϳЁЌЎяёќўҁҐӄӇӈӋӌӐӫӮӵӸӹԱՖՙՙաֆאתװײءغفيٱڷںھۀێېۓەەۥۦअहऽऽक़ॡঅঌএঐওনপরললশহড়ঢ়য়ৡৰৱਅਊਏਐਓਨਪਰਲਲ਼ਵਸ਼ਸਹਖ਼ੜਫ਼ਫ਼ੲੴઅઋઍઍએઑઓનપરલળવહઽઽૠૠଅଌଏଐଓନପରଲଳଶହଽଽଡ଼ଢ଼ୟୡஅஊஎஐஒகஙசஜஜஞடணதநபமவஷஹఅఌఎఐఒనపళవహౠౡಅಌಎಐಒನಪಳವಹೞೞೠೡഅഌഎഐഒനപഹൠൡกฮะะาำเๅກຂຄຄງຈຊຊຍຍດທນຟມຣລລວວສຫອຮະະາຳຽຽເໄཀཇཉཀྵႠჅაჶᄀᄀᄂᄃᄅᄇᄉᄉᄋᄌᄎᄒᄼᄼᄾᄾᅀᅀᅌᅌᅎᅎᅐᅐᅔᅕᅙᅙᅟᅡᅣᅣᅥᅥᅧᅧᅩᅩᅭᅮᅲᅳᅵᅵᆞᆞᆨᆨᆫᆫᆮᆯᆷᆸᆺᆺᆼᇂᇫᇫᇰᇰᇹᇹḀẛẠỹἀἕἘἝἠὅὈὍὐὗὙὙὛὛὝὝὟώᾀᾴᾶᾼιιῂῄῆῌῐΐῖΊῠῬῲῴῶῼΩΩKÅ℮℮ↀↂ〇〇〡〩ぁゔァヺㄅㄬ一龥가힣ｦﾟ";
    private static final int[] LETTERS_INT = {120720, 120744, 120746, 120777, 195099, 195101};
    private static final String NAMECHARS = "-.0:AZ__az··ÀÖØöøıĴľŁňŊžƀǃǍǰǴǵǺȗɐʨʻˁːˑ̀͠͡ͅΆΊΌΌΎΡΣώϐϖϚϚϜϜϞϞϠϠϢϳЁЌЎяёќўҁ҃҆ҐӄӇӈӋӌӐӫӮӵӸӹԱՖՙՙաֆֹֻֽֿֿׁׂ֑֣֡ׄׄאתװײءغـْ٠٩ٰڷںھۀێېۓە۪ۭۨ۰۹ँःअह़्॑॔क़ॣ०९ঁঃঅঌএঐওনপরললশহ়়াৄেৈো্ৗৗড়ঢ়য়ৣ০ৱਂਂਅਊਏਐਓਨਪਰਲਲ਼ਵਸ਼ਸਹ਼਼ਾੂੇੈੋ੍ਖ਼ੜਫ਼ਫ਼੦ੴઁઃઅઋઍઍએઑઓનપરલળવહ઼ૅેૉો્ૠૠ૦૯ଁଃଅଌଏଐଓନପରଲଳଶହ଼ୃେୈୋ୍ୖୗଡ଼ଢ଼ୟୡ୦୯ஂஃஅஊஎஐஒகஙசஜஜஞடணதநபமவஷஹாூெைொ்ௗௗ௧௯ఁఃఅఌఎఐఒనపళవహాౄెైొ్ౕౖౠౡ౦౯ಂಃಅಌಎಐಒನಪಳವಹಾೄೆೈೊ್ೕೖೞೞೠೡ೦೯ംഃഅഌഎഐഒനപഹാൃെൈൊ്ൗൗൠൡ൦൯กฮะฺเ๎๐๙ກຂຄຄງຈຊຊຍຍດທນຟມຣລລວວສຫອຮະູົຽເໄໆໆ່ໍ໐໙༘༙༠༩༹༹༵༵༷༷༾ཇཉཀྵ྄ཱ྆ྋྐྕྗྗྙྭྱྷྐྵྐྵႠჅაჶᄀᄀᄂᄃᄅᄇᄉᄉᄋᄌᄎᄒᄼᄼᄾᄾᅀᅀᅌᅌᅎᅎᅐᅐᅔᅕᅙᅙᅟᅡᅣᅣᅥᅥᅧᅧᅩᅩᅭᅮᅲᅳᅵᅵᆞᆞᆨᆨᆫᆫᆮᆯᆷᆸᆺᆺᆼᇂᇫᇫᇰᇰᇹᇹḀẛẠỹἀἕἘἝἠὅὈὍὐὗὙὙὛὛὝὝὟώᾀᾴᾶᾼιιῂῄῆῌῐΐῖΊῠῬῲῴῶῼ⃐⃜⃡⃡ΩΩKÅ℮℮ↀↂ々々〇〇〡〯〱〵ぁゔ゙゚ゝゞァヺーヾㄅㄬ一龥가힣";
    private static final String SPACES = "\t\n\r\r  ";
    private static Map<String, Token> ranges;
    private static Map<String, Token> ranges2;

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public boolean checkQuestion(int i) {
        return false;
    }

    public ParserForXMLSchema() {
    }

    public ParserForXMLSchema(Locale locale) {
        super(locale);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processCaret() throws ParseException {
        next();
        return Token.createChar(94);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processDollar() throws ParseException {
        next();
        return Token.createChar(36);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processLookahead() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processNegativelookahead() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processLookbehind() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processNegativelookbehind() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_A() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_Z() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_z() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_b() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_B() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_lt() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_gt() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processStar(Token token) throws ParseException {
        next();
        return Token.createClosure(token);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processPlus(Token token) throws ParseException {
        next();
        return Token.createConcat(token, Token.createClosure(token));
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processQuestion(Token token) throws ParseException {
        next();
        Token.UnionToken createUnion = Token.createUnion();
        createUnion.addChild(token);
        createUnion.addChild(Token.createEmpty());
        return createUnion;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processParen() throws ParseException {
        next();
        Token.ParenToken createParen = Token.createParen(parseRegex(), 0);
        if (read() == 7) {
            next();
            return createParen;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processParen2() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processCondition() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processModifiers() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processIndependent() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_c() throws ParseException {
        next();
        return getTokenForShorthand(99);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_C() throws ParseException {
        next();
        return getTokenForShorthand(67);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_i() throws ParseException {
        next();
        return getTokenForShorthand(105);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_I() throws ParseException {
        next();
        return getTokenForShorthand(73);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_g() throws ParseException {
        throw ex("parser.process.1", this.offset - 2);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBacksolidus_X() throws ParseException {
        throw ex("parser.process.1", this.offset - 2);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token processBackreference() throws ParseException {
        throw ex("parser.process.1", this.offset - 4);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public int processCIinCharacterClass(RangeToken rangeToken, int i) {
        rangeToken.mergeRanges(getTokenForShorthand(i));
        return -1;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00b0, code lost:
        if (r10 < 0) goto L_0x0095;
     */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x01c3 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00e6  */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public RangeToken parseCharacterClass(boolean z) throws ParseException {
        RangeToken rangeToken;
        RangeToken rangeToken2;
        boolean z2;
        int i;
        boolean z3;
        setContext(1);
        next();
        boolean z4 = false;
        if (read() == 0 && this.chardata == 94) {
            next();
            RangeToken createRange = Token.createRange();
            createRange.addRange(0, 1114111);
            rangeToken2 = Token.createRange();
            rangeToken = createRange;
            z2 = true;
        } else {
            rangeToken2 = Token.createRange();
            rangeToken = null;
            z2 = false;
        }
        boolean z5 = true;
        while (true) {
            int read = read();
            if (read == 1) {
                break;
            } else if (read != 0 || this.chardata != 93 || z5) {
                int i2 = this.chardata;
                if (read == 10) {
                    if (i2 != 45) {
                        if (i2 != 73) {
                            if (i2 != 80) {
                                if (!(i2 == 83 || i2 == 87)) {
                                    if (i2 != 105) {
                                        if (i2 != 112) {
                                            if (!(i2 == 115 || i2 == 119)) {
                                                if (i2 != 67) {
                                                    if (i2 != 68) {
                                                        if (i2 != 99) {
                                                            if (i2 != 100) {
                                                                i2 = decodeEscaped();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                rangeToken2.mergeRanges(getTokenForShorthand(i2));
                                i = i2;
                                z3 = true;
                                next();
                                if (!z3) {
                                    if (read == 0) {
                                        if (i == 91) {
                                            throw ex("parser.cc.6", this.offset - 2);
                                        } else if (i == 93) {
                                            throw ex("parser.cc.7", this.offset - 2);
                                        } else if (i == 45 && this.chardata != 93 && !z5) {
                                            throw ex("parser.cc.8", this.offset - 2);
                                        }
                                    }
                                    if (read() == 0 && this.chardata == 45 && (i != 45 || !z5)) {
                                        next();
                                        int read2 = read();
                                        if (read2 == 1) {
                                            throw ex("parser.cc.2", this.offset);
                                        } else if (read2 == 0 && this.chardata == 93) {
                                            if (!isSet(2) || i > 65535) {
                                                rangeToken2.addRange(i, i);
                                            } else {
                                                addCaseInsensitiveChar(rangeToken2, i);
                                            }
                                            rangeToken2.addRange(45, 45);
                                        } else if (read2 != 24) {
                                            int i3 = this.chardata;
                                            if (read2 == 0) {
                                                if (i3 == 91) {
                                                    throw ex("parser.cc.6", this.offset - 1);
                                                } else if (i3 == 93) {
                                                    throw ex("parser.cc.7", this.offset - 1);
                                                } else if (i3 == 45) {
                                                    throw ex("parser.cc.8", this.offset - 2);
                                                }
                                            } else if (read2 == 10) {
                                                i3 = decodeEscaped();
                                            }
                                            next();
                                            if (i > i3) {
                                                throw ex("parser.ope.3", this.offset - 1);
                                            } else if (!isSet(2) || (i > 65535 && i3 > 65535)) {
                                                rangeToken2.addRange(i, i3);
                                            } else {
                                                addCaseInsensitiveCharRange(rangeToken2, i, i3);
                                            }
                                        } else {
                                            throw ex("parser.cc.8", this.offset - 1);
                                        }
                                    } else if (!isSet(2) || i > 65535) {
                                        rangeToken2.addRange(i, i);
                                    } else {
                                        addCaseInsensitiveChar(rangeToken2, i);
                                    }
                                }
                                z4 = false;
                                z5 = false;
                            }
                            int i4 = this.offset;
                            RangeToken processBacksolidus_pP = processBacksolidus_pP(i2);
                            if (processBacksolidus_pP != null) {
                                rangeToken2.mergeRanges(processBacksolidus_pP);
                                i = i2;
                                z3 = true;
                                next();
                                if (!z3) {
                                }
                                z4 = false;
                                z5 = false;
                            } else {
                                throw ex("parser.atom.5", i4);
                            }
                        }
                        i2 = processCIinCharacterClass(rangeToken2, i2);
                    } else {
                        i2 = decodeEscaped();
                    }
                } else if (read == 24 && !z5) {
                    if (z2) {
                        rangeToken.subtractRanges(rangeToken2);
                        rangeToken2 = rangeToken;
                    }
                    rangeToken2.subtractRanges(parseCharacterClass(z4));
                    if (read() != 0 || this.chardata != 93) {
                        throw ex("parser.cc.5", this.offset);
                    }
                }
                i = i2;
                z3 = z4;
                next();
                if (!z3) {
                }
                z4 = false;
                z5 = false;
            } else if (z2) {
                rangeToken.subtractRanges(rangeToken2);
                rangeToken2 = rangeToken;
            }
        }
        if (read() != 1) {
            rangeToken2.sortRanges();
            rangeToken2.compactRanges();
            setContext(0);
            next();
            return rangeToken2;
        }
        throw ex("parser.cc.2", this.offset);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public RangeToken parseSetOperations() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public Token getTokenForShorthand(int i) {
        if (i == 67) {
            return getRange("xml:isNameChar", false);
        }
        if (i == 68) {
            return getRange("xml:isDigit", false);
        }
        if (i == 73) {
            return getRange("xml:isInitialNameChar", false);
        }
        if (i == 83) {
            return getRange("xml:isSpace", false);
        }
        if (i == 87) {
            return getRange("xml:isWord", false);
        }
        if (i == 105) {
            return getRange("xml:isInitialNameChar", true);
        }
        if (i == 115) {
            return getRange("xml:isSpace", true);
        }
        if (i == 119) {
            return getRange("xml:isWord", true);
        }
        if (i == 99) {
            return getRange("xml:isNameChar", true);
        }
        if (i == 100) {
            return getRange("xml:isDigit", true);
        }
        throw new RuntimeException("Internal Error: shorthands: \\u" + Integer.toString(i, 16));
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegexParser
    public int decodeEscaped() throws ParseException {
        if (read() == 10) {
            int i = this.chardata;
            if (!(i == 45 || i == 46 || i == 63)) {
                if (i == 110) {
                    return 10;
                }
                if (i == 114) {
                    return 13;
                }
                if (i == 116) {
                    return 9;
                }
                switch (i) {
                    case 40:
                    case 41:
                    case 42:
                    case 43:
                        break;
                    default:
                        switch (i) {
                            case 91:
                            case 92:
                            case 93:
                            case 94:
                                break;
                            default:
                                switch (i) {
                                    case 123:
                                    case 124:
                                    case 125:
                                        break;
                                    default:
                                        throw ex("parser.process.1", this.offset - 2);
                                }
                        }
                }
            }
            return i;
        }
        throw ex("parser.next.1", this.offset - 1);
    }

    protected static synchronized RangeToken getRange(String str, boolean z) {
        RangeToken rangeToken;
        synchronized (ParserForXMLSchema.class) {
            if (ranges == null) {
                ranges = new HashMap();
                ranges2 = new HashMap();
                RangeToken createRange = Token.createRange();
                setupRange(createRange, SPACES);
                ranges.put("xml:isSpace", createRange);
                ranges2.put("xml:isSpace", Token.complementRanges(createRange));
                RangeToken createRange2 = Token.createRange();
                setupRange(createRange2, DIGITS);
                setupRange(createRange2, DIGITS_INT);
                ranges.put("xml:isDigit", createRange2);
                ranges2.put("xml:isDigit", Token.complementRanges(createRange2));
                RangeToken createRange3 = Token.createRange();
                setupRange(createRange3, LETTERS);
                setupRange(createRange3, LETTERS_INT);
                createRange3.mergeRanges(ranges.get("xml:isDigit"));
                ranges.put("xml:isWord", createRange3);
                ranges2.put("xml:isWord", Token.complementRanges(createRange3));
                RangeToken createRange4 = Token.createRange();
                setupRange(createRange4, NAMECHARS);
                ranges.put("xml:isNameChar", createRange4);
                ranges2.put("xml:isNameChar", Token.complementRanges(createRange4));
                RangeToken createRange5 = Token.createRange();
                setupRange(createRange5, LETTERS);
                createRange5.addRange(95, 95);
                createRange5.addRange(58, 58);
                ranges.put("xml:isInitialNameChar", createRange5);
                ranges2.put("xml:isInitialNameChar", Token.complementRanges(createRange5));
            }
            if (z) {
                rangeToken = (RangeToken) ranges.get(str);
            } else {
                rangeToken = (RangeToken) ranges2.get(str);
            }
        }
        return rangeToken;
    }

    static void setupRange(Token token, String str) {
        int length = str.length();
        for (int i = 0; i < length; i += 2) {
            token.addRange(str.charAt(i), str.charAt(i + 1));
        }
    }

    static void setupRange(Token token, int[] iArr) {
        int length = iArr.length;
        for (int i = 0; i < length; i += 2) {
            token.addRange(iArr[i], iArr[i + 1]);
        }
    }
}
