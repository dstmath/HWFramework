package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

import java.io.Serializable;
import java.text.CharacterIterator;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Op;
import ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public class RegularExpression implements Serializable {
    static final int CARRIAGE_RETURN = 13;
    static final boolean DEBUG = false;
    static final int EXTENDED_COMMENT = 16;
    static final int IGNORE_CASE = 2;
    static final int LINE_FEED = 10;
    static final int LINE_SEPARATOR = 8232;
    static final int MULTIPLE_LINES = 8;
    static final int PARAGRAPH_SEPARATOR = 8233;
    static final int PROHIBIT_FIXED_STRING_OPTIMIZATION = 256;
    static final int PROHIBIT_HEAD_CHARACTER_OPTIMIZATION = 128;
    static final int SINGLE_LINE = 4;
    static final int SPECIAL_COMMA = 1024;
    static final int UNICODE_WORD_BOUNDARY = 64;
    static final int USE_UNICODE_CATEGORY = 32;
    private static final int WT_IGNORE = 0;
    private static final int WT_LETTER = 1;
    private static final int WT_OTHER = 2;
    static final int XMLSCHEMA_MODE = 512;
    private static final long serialVersionUID = 6242499334195006401L;
    transient Context context;
    transient RangeToken firstChar;
    transient String fixedString;
    transient boolean fixedStringOnly;
    transient int fixedStringOptions;
    transient BMPattern fixedStringTable;
    boolean hasBackReferences;
    transient int minlength;
    int nofparen;
    transient int numberOfClosures;
    transient Op operations;
    int options;
    String regex;
    Token tokentree;

    private static final boolean isEOLChar(int i) {
        return i == 10 || i == 13 || i == LINE_SEPARATOR || i == PARAGRAPH_SEPARATOR;
    }

    private static final boolean isSet(int i, int i2) {
        return (i & i2) == i2;
    }

    private static final boolean isWordChar(int i) {
        if (i == 95) {
            return true;
        }
        if (i < 48 || i > 122) {
            return false;
        }
        if (i <= 57) {
            return true;
        }
        if (i < 65) {
            return false;
        }
        return i <= 90 || i >= 97;
    }

    private synchronized void compile(Token token) {
        if (this.operations == null) {
            this.numberOfClosures = 0;
            this.operations = compile(token, null, false);
        }
    }

    private Op compile(Token token, Op op, boolean z) {
        Op op2;
        Op op3;
        Op.ChildOp childOp;
        int i = token.type;
        int i2 = 0;
        switch (i) {
            case 0:
                op2 = Op.createChar(token.getChar());
                op2.next = op;
                break;
            case 1:
                if (!z) {
                    for (int size = token.size() - 1; size >= 0; size--) {
                        op = compile(token.getChild(size), op, false);
                    }
                    return op;
                }
                while (i2 < token.size()) {
                    op = compile(token.getChild(i2), op, true);
                    i2++;
                }
                return op;
            case 2:
                Op.UnionOp createUnion = Op.createUnion(token.size());
                while (i2 < token.size()) {
                    createUnion.addElement(compile(token.getChild(i2), op, z));
                    i2++;
                }
                return createUnion;
            case 3:
            case 9:
                Token child = token.getChild(0);
                int min = token.getMin();
                int max = token.getMax();
                if (min < 0 || min != max) {
                    if (min > 0 && max > 0) {
                        max -= min;
                    }
                    if (max > 0) {
                        Op op4 = op;
                        int i3 = 0;
                        while (i3 < max) {
                            Op.ChildOp createQuestion = Op.createQuestion(token.type == 9);
                            createQuestion.next = op;
                            createQuestion.setChild(compile(child, op4, z));
                            i3++;
                            op4 = createQuestion;
                        }
                        op3 = op4;
                    } else {
                        if (token.type == 9) {
                            childOp = Op.createNonGreedyClosure();
                        } else {
                            int i4 = this.numberOfClosures;
                            this.numberOfClosures = i4 + 1;
                            childOp = Op.createClosure(i4);
                        }
                        childOp.next = op;
                        childOp.setChild(compile(child, childOp, z));
                        op3 = childOp;
                    }
                    if (min <= 0) {
                        return op3;
                    }
                    while (i2 < min) {
                        op3 = compile(child, op3, z);
                        i2++;
                    }
                    return op3;
                }
                while (i2 < min) {
                    op = compile(child, op, z);
                    i2++;
                }
                return op;
            case 4:
            case 5:
                op2 = Op.createRange(token);
                op2.next = op;
                break;
            case 6:
                if (token.getParenNumber() == 0) {
                    return compile(token.getChild(0), op, z);
                }
                if (z) {
                    return Op.createCapture(-token.getParenNumber(), compile(token.getChild(0), Op.createCapture(token.getParenNumber(), op), z));
                }
                return Op.createCapture(token.getParenNumber(), compile(token.getChild(0), Op.createCapture(-token.getParenNumber(), op), z));
            case 7:
                return op;
            case 8:
                op2 = Op.createAnchor(token.getChar());
                op2.next = op;
                break;
            case 10:
                op2 = Op.createString(token.getString());
                op2.next = op;
                break;
            case 11:
                op2 = Op.createDot();
                op2.next = op;
                break;
            case 12:
                op2 = Op.createBackReference(token.getReferenceNumber());
                op2.next = op;
                break;
            default:
                Op op5 = null;
                switch (i) {
                    case 20:
                        return Op.createLook(20, op, compile(token.getChild(0), null, false));
                    case 21:
                        return Op.createLook(21, op, compile(token.getChild(0), null, false));
                    case 22:
                        return Op.createLook(22, op, compile(token.getChild(0), null, true));
                    case 23:
                        return Op.createLook(23, op, compile(token.getChild(0), null, true));
                    case 24:
                        return Op.createIndependent(op, compile(token.getChild(0), null, z));
                    case 25:
                        Token.ModifierToken modifierToken = (Token.ModifierToken) token;
                        return Op.createModifier(op, compile(token.getChild(0), null, z), modifierToken.getOptions(), modifierToken.getOptionsMask());
                    case 26:
                        Token.ConditionToken conditionToken = (Token.ConditionToken) token;
                        int i5 = conditionToken.refNumber;
                        Op compile = conditionToken.condition == null ? null : compile(conditionToken.condition, null, z);
                        Op compile2 = compile(conditionToken.yes, op, z);
                        if (conditionToken.no != null) {
                            op5 = compile(conditionToken.no, op, z);
                        }
                        return Op.createCondition(op, i5, compile, compile2, op5);
                    default:
                        throw new RuntimeException("Unknown token type: " + token.type);
                }
        }
        return op2;
    }

    public boolean matches(char[] cArr) {
        return matches(cArr, 0, cArr.length, (Match) null);
    }

    public boolean matches(char[] cArr, int i, int i2) {
        return matches(cArr, i, i2, (Match) null);
    }

    public boolean matches(char[] cArr, Match match) {
        return matches(cArr, 0, cArr.length, match);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:108:0x010f */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0168  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x017a  */
    public boolean matches(char[] cArr, int i, int i2, Match match) {
        Context context2;
        int i3;
        int i4;
        int i5;
        boolean z;
        synchronized (this) {
            if (this.operations == null) {
                prepare();
            }
            if (this.context == null) {
                this.context = new Context();
            }
        }
        synchronized (this.context) {
            context2 = this.context.inuse ? new Context() : this.context;
            context2.reset(cArr, i, i2, this.numberOfClosures);
        }
        if (match != null) {
            match.setNumberOfGroups(this.nofparen);
            match.setSource(cArr);
        } else if (this.hasBackReferences) {
            match = new Match();
            match.setNumberOfGroups(this.nofparen);
        }
        context2.match = match;
        if (isSet(this.options, 512)) {
            int match2 = match(context2, this.operations, context2.start, 1, this.options);
            if (match2 != context2.limit) {
                return false;
            }
            if (context2.match != null) {
                context2.match.setBeginning(0, context2.start);
                context2.match.setEnd(0, match2);
            }
            context2.setInUse(false);
            return true;
        } else if (this.fixedStringOnly) {
            int matches = this.fixedStringTable.matches(cArr, context2.start, context2.limit);
            if (matches >= 0) {
                if (context2.match != null) {
                    context2.match.setBeginning(0, matches);
                    context2.match.setEnd(0, matches + this.fixedString.length());
                }
                context2.setInUse(false);
                return true;
            }
            context2.setInUse(false);
            return false;
        } else if (this.fixedString == null || this.fixedStringTable.matches(cArr, context2.start, context2.limit) >= 0) {
            int i6 = context2.limit - this.minlength;
            int i7 = -1;
            Op op = this.operations;
            if (op == null || op.type != 7 || this.operations.getChild().type != 0) {
                RangeToken rangeToken = this.firstChar;
                if (rangeToken != null) {
                    int i8 = -1;
                    i5 = context2.start;
                    while (i5 <= i6) {
                        char c = cArr[i5];
                        boolean isHighSurrogate = REUtil.isHighSurrogate(c);
                        int i9 = c;
                        if (isHighSurrogate) {
                            int i10 = i5 + 1;
                            i9 = c;
                            if (i10 < context2.limit) {
                                i9 = REUtil.composeFromSurrogates(c, cArr[i10]);
                            }
                        }
                        if (rangeToken.match(i9 == 1 ? 1 : 0) && (i8 = match(context2, this.operations, i5, 1, this.options)) >= 0) {
                            break;
                        }
                        i5++;
                    }
                    i4 = i8;
                } else {
                    i3 = context2.start;
                    while (i3 <= i6) {
                        i7 = match(context2, this.operations, i3, 1, this.options);
                        if (i7 >= 0) {
                            break;
                        }
                        i3++;
                    }
                    i4 = i7;
                    if (i4 >= 0) {
                        if (context2.match != null) {
                            context2.match.setBeginning(0, i3);
                            context2.match.setEnd(0, i4);
                        }
                        context2.setInUse(false);
                        return true;
                    }
                    context2.setInUse(false);
                    return false;
                }
            } else if (isSet(this.options, 4)) {
                i3 = context2.start;
                i4 = match(context2, this.operations, context2.start, 1, this.options);
                if (i4 >= 0) {
                }
            } else {
                int i11 = -1;
                i5 = context2.start;
                boolean z2 = true;
                while (i5 <= i6) {
                    if (!isEOLChar(cArr[i5])) {
                        if (z2 && (i11 = match(context2, this.operations, i5, 1, this.options)) >= 0) {
                            break;
                        }
                        z = false;
                    } else {
                        z = true;
                    }
                    i5++;
                    z2 = z;
                }
                i4 = i11;
            }
            i3 = i5;
            if (i4 >= 0) {
            }
        } else {
            context2.setInUse(false);
            return false;
        }
    }

    public boolean matches(String str) {
        return matches(str, 0, str.length(), (Match) null);
    }

    public boolean matches(String str, int i, int i2) {
        return matches(str, i, i2, (Match) null);
    }

    public boolean matches(String str, Match match) {
        return matches(str, 0, str.length(), match);
    }

    /* JADX WARNING: Removed duplicated region for block: B:93:0x016e  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0180  */
    public boolean matches(String str, int i, int i2, Match match) {
        Context context2;
        int i3;
        int i4;
        int i5;
        int i6;
        synchronized (this) {
            if (this.operations == null) {
                prepare();
            }
            if (this.context == null) {
                this.context = new Context();
            }
        }
        synchronized (this.context) {
            context2 = this.context.inuse ? new Context() : this.context;
            context2.reset(str, i, i2, this.numberOfClosures);
        }
        if (match != null) {
            match.setNumberOfGroups(this.nofparen);
            match.setSource(str);
        } else if (this.hasBackReferences) {
            match = new Match();
            match.setNumberOfGroups(this.nofparen);
        }
        context2.match = match;
        if (isSet(this.options, 512)) {
            int match2 = match(context2, this.operations, context2.start, 1, this.options);
            if (match2 != context2.limit) {
                return false;
            }
            if (context2.match != null) {
                context2.match.setBeginning(0, context2.start);
                context2.match.setEnd(0, match2);
            }
            context2.setInUse(false);
            return true;
        } else if (this.fixedStringOnly) {
            int matches = this.fixedStringTable.matches(str, context2.start, context2.limit);
            if (matches >= 0) {
                if (context2.match != null) {
                    context2.match.setBeginning(0, matches);
                    context2.match.setEnd(0, matches + this.fixedString.length());
                }
                context2.setInUse(false);
                return true;
            }
            context2.setInUse(false);
            return false;
        } else if (this.fixedString == null || this.fixedStringTable.matches(str, context2.start, context2.limit) >= 0) {
            int i7 = context2.limit - this.minlength;
            int i8 = -1;
            Op op = this.operations;
            if (op == null || op.type != 7 || this.operations.getChild().type != 0) {
                RangeToken rangeToken = this.firstChar;
                if (rangeToken != null) {
                    int i9 = -1;
                    i5 = context2.start;
                    while (i5 <= i7) {
                        int charAt = str.charAt(i5);
                        if (REUtil.isHighSurrogate(charAt) && (i6 = i5 + 1) < context2.limit) {
                            charAt = REUtil.composeFromSurrogates(charAt, str.charAt(i6));
                        }
                        if (rangeToken.match(charAt) && (i9 = match(context2, this.operations, i5, 1, this.options)) >= 0) {
                            break;
                        }
                        i5++;
                    }
                    i4 = i9;
                } else {
                    i3 = context2.start;
                    while (i3 <= i7) {
                        i8 = match(context2, this.operations, i3, 1, this.options);
                        if (i8 >= 0) {
                            break;
                        }
                        i3++;
                    }
                    i4 = i8;
                    if (i4 >= 0) {
                        if (context2.match != null) {
                            context2.match.setBeginning(0, i3);
                            context2.match.setEnd(0, i4);
                        }
                        context2.setInUse(false);
                        return true;
                    }
                    context2.setInUse(false);
                    return false;
                }
            } else if (isSet(this.options, 4)) {
                i3 = context2.start;
                i4 = match(context2, this.operations, context2.start, 1, this.options);
                if (i4 >= 0) {
                }
            } else {
                int i10 = -1;
                i5 = context2.start;
                boolean z = true;
                while (i5 <= i7) {
                    if (!isEOLChar(str.charAt(i5))) {
                        if (z && (i10 = match(context2, this.operations, i5, 1, this.options)) >= 0) {
                            break;
                        }
                        z = false;
                    } else {
                        z = true;
                    }
                    i5++;
                }
                i4 = i10;
            }
            i3 = i5;
            if (i4 >= 0) {
            }
        } else {
            context2.setInUse(false);
            return false;
        }
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x0327  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x035b  */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x0325 A[SYNTHETIC] */
    private int match(ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.Context r25, ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Op r26, int r27, int r28, int r29) {
        /*
        // Method dump skipped, instructions count: 1030
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.match(ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression$Context, ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Op, int, int, int):int");
    }

    private boolean matchChar(int i, int i2, boolean z) {
        if (z) {
            return matchIgnoreCase(i, i2);
        }
        return i == i2;
    }

    /* access modifiers changed from: package-private */
    public boolean matchAnchor(ExpressionTarget expressionTarget, Op op, Context context2, int i, int i2) {
        int i3;
        int i4;
        int wordType;
        boolean z;
        int wordType2;
        int data = op.getData();
        if (data != 36) {
            if (data != 60) {
                if (data != 62) {
                    if (data != 90) {
                        if (data != 94) {
                            if (data != 98) {
                                if (data != 122) {
                                    switch (data) {
                                        case 64:
                                            if (i != context2.start && (i <= context2.start || !isEOLChar(expressionTarget.charAt(i - 1)))) {
                                                return false;
                                            }
                                            break;
                                        case 65:
                                            if (i != context2.start) {
                                                return false;
                                            }
                                            break;
                                        case 66:
                                            if (context2.length == 0 || (wordType2 = getWordType(expressionTarget, context2.start, context2.limit, i, i2)) == 0 || wordType2 == getPreviousWordType(expressionTarget, context2.start, context2.limit, i, i2)) {
                                                z = true;
                                            } else {
                                                z = false;
                                            }
                                            if (!z) {
                                                return false;
                                            }
                                            break;
                                    }
                                } else if (i != context2.limit) {
                                    return false;
                                }
                            } else if (context2.length == 0 || (wordType = getWordType(expressionTarget, context2.start, context2.limit, i, i2)) == 0 || wordType == getPreviousWordType(expressionTarget, context2.start, context2.limit, i, i2)) {
                                return false;
                            }
                        } else if (isSet(i2, 8)) {
                            if (i != context2.start && (i <= context2.start || i >= context2.limit || !isEOLChar(expressionTarget.charAt(i - 1)))) {
                                return false;
                            }
                        } else if (i != context2.start) {
                            return false;
                        }
                    } else if (i != context2.limit && (((i4 = i + 1) != context2.limit || !isEOLChar(expressionTarget.charAt(i))) && (i + 2 != context2.limit || expressionTarget.charAt(i) != '\r' || expressionTarget.charAt(i4) != '\n'))) {
                        return false;
                    }
                } else if (context2.length == 0 || i == context2.start || getWordType(expressionTarget, context2.start, context2.limit, i, i2) != 2 || getPreviousWordType(expressionTarget, context2.start, context2.limit, i, i2) != 1) {
                    return false;
                }
            } else if (context2.length == 0 || i == context2.limit || getWordType(expressionTarget, context2.start, context2.limit, i, i2) != 1 || getPreviousWordType(expressionTarget, context2.start, context2.limit, i, i2) != 2) {
                return false;
            }
        } else if (isSet(i2, 8)) {
            if (i != context2.limit && (i >= context2.limit || !isEOLChar(expressionTarget.charAt(i)))) {
                return false;
            }
        } else if (i != context2.limit && (((i3 = i + 1) != context2.limit || !isEOLChar(expressionTarget.charAt(i))) && (i + 2 != context2.limit || expressionTarget.charAt(i) != '\r' || expressionTarget.charAt(i3) != '\n'))) {
            return false;
        }
        return true;
    }

    private static final int getPreviousWordType(ExpressionTarget expressionTarget, int i, int i2, int i3, int i4) {
        int i5 = i3 - 1;
        int wordType = getWordType(expressionTarget, i, i2, i5, i4);
        while (wordType == 0) {
            i5--;
            wordType = getWordType(expressionTarget, i, i2, i5, i4);
        }
        return wordType;
    }

    private static final int getWordType(ExpressionTarget expressionTarget, int i, int i2, int i3, int i4) {
        if (i3 < i || i3 >= i2) {
            return 2;
        }
        return getWordType0(expressionTarget.charAt(i3), i4);
    }

    public boolean matches(CharacterIterator characterIterator) {
        return matches(characterIterator, (Match) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:94:0x0176  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0188  */
    public boolean matches(CharacterIterator characterIterator, Match match) {
        Context context2;
        int i;
        int i2;
        int i3;
        int i4;
        int beginIndex = characterIterator.getBeginIndex();
        int endIndex = characterIterator.getEndIndex();
        synchronized (this) {
            if (this.operations == null) {
                prepare();
            }
            if (this.context == null) {
                this.context = new Context();
            }
        }
        synchronized (this.context) {
            context2 = this.context.inuse ? new Context() : this.context;
            context2.reset(characterIterator, beginIndex, endIndex, this.numberOfClosures);
        }
        if (match != null) {
            match.setNumberOfGroups(this.nofparen);
            match.setSource(characterIterator);
        } else if (this.hasBackReferences) {
            match = new Match();
            match.setNumberOfGroups(this.nofparen);
        }
        context2.match = match;
        if (isSet(this.options, 512)) {
            int match2 = match(context2, this.operations, context2.start, 1, this.options);
            if (match2 != context2.limit) {
                return false;
            }
            if (context2.match != null) {
                context2.match.setBeginning(0, context2.start);
                context2.match.setEnd(0, match2);
            }
            context2.setInUse(false);
            return true;
        } else if (this.fixedStringOnly) {
            int matches = this.fixedStringTable.matches(characterIterator, context2.start, context2.limit);
            if (matches >= 0) {
                if (context2.match != null) {
                    context2.match.setBeginning(0, matches);
                    context2.match.setEnd(0, matches + this.fixedString.length());
                }
                context2.setInUse(false);
                return true;
            }
            context2.setInUse(false);
            return false;
        } else if (this.fixedString == null || this.fixedStringTable.matches(characterIterator, context2.start, context2.limit) >= 0) {
            int i5 = context2.limit - this.minlength;
            int i6 = -1;
            Op op = this.operations;
            if (op == null || op.type != 7 || this.operations.getChild().type != 0) {
                RangeToken rangeToken = this.firstChar;
                if (rangeToken != null) {
                    int i7 = -1;
                    i3 = context2.start;
                    while (i3 <= i5) {
                        int index = characterIterator.setIndex(i3);
                        if (REUtil.isHighSurrogate(index) && (i4 = i3 + 1) < context2.limit) {
                            index = REUtil.composeFromSurrogates(index, characterIterator.setIndex(i4));
                        }
                        if (rangeToken.match(index) && (i7 = match(context2, this.operations, i3, 1, this.options)) >= 0) {
                            break;
                        }
                        i3++;
                    }
                    i2 = i7;
                } else {
                    i = context2.start;
                    while (i <= i5) {
                        i6 = match(context2, this.operations, i, 1, this.options);
                        if (i6 >= 0) {
                            break;
                        }
                        i++;
                    }
                    i2 = i6;
                    if (i2 >= 0) {
                        if (context2.match != null) {
                            context2.match.setBeginning(0, i);
                            context2.match.setEnd(0, i2);
                        }
                        context2.setInUse(false);
                        return true;
                    }
                    context2.setInUse(false);
                    return false;
                }
            } else if (isSet(this.options, 4)) {
                i = context2.start;
                i2 = match(context2, this.operations, context2.start, 1, this.options);
                if (i2 >= 0) {
                }
            } else {
                int i8 = -1;
                i3 = context2.start;
                boolean z = true;
                while (i3 <= i5) {
                    if (!isEOLChar(characterIterator.setIndex(i3))) {
                        if (z && (i8 = match(context2, this.operations, i3, 1, this.options)) >= 0) {
                            break;
                        }
                        z = false;
                    } else {
                        z = true;
                    }
                    i3++;
                }
                i2 = i8;
            }
            i = i3;
            if (i2 >= 0) {
            }
        } else {
            context2.setInUse(false);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class ExpressionTarget {
        /* access modifiers changed from: package-private */
        public abstract char charAt(int i);

        /* access modifiers changed from: package-private */
        public abstract boolean regionMatches(boolean z, int i, int i2, int i3, int i4);

        /* access modifiers changed from: package-private */
        public abstract boolean regionMatches(boolean z, int i, int i2, String str, int i3);

        ExpressionTarget() {
        }
    }

    /* access modifiers changed from: package-private */
    public static final class StringTarget extends ExpressionTarget {
        private String target;

        StringTarget(String str) {
            this.target = str;
        }

        /* access modifiers changed from: package-private */
        public final void resetTarget(String str) {
            this.target = str;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final char charAt(int i) {
            return this.target.charAt(i);
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final boolean regionMatches(boolean z, int i, int i2, String str, int i3) {
            if (i2 - i < i3) {
                return false;
            }
            return z ? this.target.regionMatches(true, i, str, 0, i3) : this.target.regionMatches(i, str, 0, i3);
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final boolean regionMatches(boolean z, int i, int i2, int i3, int i4) {
            if (i2 - i < i4) {
                return false;
            }
            if (z) {
                String str = this.target;
                return str.regionMatches(true, i, str, i3, i4);
            }
            String str2 = this.target;
            return str2.regionMatches(i, str2, i3, i4);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class CharArrayTarget extends ExpressionTarget {
        char[] target;

        CharArrayTarget(char[] cArr) {
            this.target = cArr;
        }

        /* access modifiers changed from: package-private */
        public final void resetTarget(char[] cArr) {
            this.target = cArr;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public char charAt(int i) {
            return this.target[i];
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final boolean regionMatches(boolean z, int i, int i2, String str, int i3) {
            if (i < 0 || i2 - i < i3) {
                return false;
            }
            if (z) {
                return regionMatchesIgnoreCase(i, i2, str, i3);
            }
            return regionMatches(i, i2, str, i3);
        }

        private final boolean regionMatches(int i, int i2, String str, int i3) {
            int i4 = 0;
            while (true) {
                int i5 = i3 - 1;
                if (i3 <= 0) {
                    return true;
                }
                int i6 = i + 1;
                int i7 = i4 + 1;
                if (this.target[i] != str.charAt(i4)) {
                    return false;
                }
                i4 = i7;
                i3 = i5;
                i = i6;
            }
        }

        private final boolean regionMatchesIgnoreCase(int i, int i2, String str, int i3) {
            char upperCase;
            char upperCase2;
            int i4 = 0;
            while (true) {
                int i5 = i3 - 1;
                if (i3 <= 0) {
                    return true;
                }
                int i6 = i + 1;
                char c = this.target[i];
                int i7 = i4 + 1;
                char charAt = str.charAt(i4);
                if (c != charAt && (upperCase = Character.toUpperCase(c)) != (upperCase2 = Character.toUpperCase(charAt)) && Character.toLowerCase(upperCase) != Character.toLowerCase(upperCase2)) {
                    return false;
                }
                i4 = i7;
                i3 = i5;
                i = i6;
            }
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final boolean regionMatches(boolean z, int i, int i2, int i3, int i4) {
            if (i < 0 || i2 - i < i4) {
                return false;
            }
            if (z) {
                return regionMatchesIgnoreCase(i, i2, i3, i4);
            }
            return regionMatches(i, i2, i3, i4);
        }

        private final boolean regionMatches(int i, int i2, int i3, int i4) {
            while (true) {
                int i5 = i4 - 1;
                if (i4 <= 0) {
                    return true;
                }
                char[] cArr = this.target;
                int i6 = i + 1;
                int i7 = i3 + 1;
                if (cArr[i] != cArr[i3]) {
                    return false;
                }
                i4 = i5;
                i = i6;
                i3 = i7;
            }
        }

        private final boolean regionMatchesIgnoreCase(int i, int i2, int i3, int i4) {
            char upperCase;
            char upperCase2;
            while (true) {
                int i5 = i4 - 1;
                if (i4 <= 0) {
                    return true;
                }
                char[] cArr = this.target;
                int i6 = i + 1;
                char c = cArr[i];
                int i7 = i3 + 1;
                char c2 = cArr[i3];
                if (c != c2 && (upperCase = Character.toUpperCase(c)) != (upperCase2 = Character.toUpperCase(c2)) && Character.toLowerCase(upperCase) != Character.toLowerCase(upperCase2)) {
                    return false;
                }
                i4 = i5;
                i = i6;
                i3 = i7;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class CharacterIteratorTarget extends ExpressionTarget {
        CharacterIterator target;

        CharacterIteratorTarget(CharacterIterator characterIterator) {
            this.target = characterIterator;
        }

        /* access modifiers changed from: package-private */
        public final void resetTarget(CharacterIterator characterIterator) {
            this.target = characterIterator;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final char charAt(int i) {
            return this.target.setIndex(i);
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final boolean regionMatches(boolean z, int i, int i2, String str, int i3) {
            if (i < 0 || i2 - i < i3) {
                return false;
            }
            if (z) {
                return regionMatchesIgnoreCase(i, i2, str, i3);
            }
            return regionMatches(i, i2, str, i3);
        }

        private final boolean regionMatches(int i, int i2, String str, int i3) {
            int i4 = 0;
            while (true) {
                int i5 = i3 - 1;
                if (i3 <= 0) {
                    return true;
                }
                int i6 = i + 1;
                int i7 = i4 + 1;
                if (this.target.setIndex(i) != str.charAt(i4)) {
                    return false;
                }
                i4 = i7;
                i3 = i5;
                i = i6;
            }
        }

        private final boolean regionMatchesIgnoreCase(int i, int i2, String str, int i3) {
            char upperCase;
            char upperCase2;
            int i4 = 0;
            while (true) {
                int i5 = i3 - 1;
                if (i3 <= 0) {
                    return true;
                }
                int i6 = i + 1;
                char index = this.target.setIndex(i);
                int i7 = i4 + 1;
                char charAt = str.charAt(i4);
                if (index != charAt && (upperCase = Character.toUpperCase(index)) != (upperCase2 = Character.toUpperCase(charAt)) && Character.toLowerCase(upperCase) != Character.toLowerCase(upperCase2)) {
                    return false;
                }
                i4 = i7;
                i3 = i5;
                i = i6;
            }
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.ExpressionTarget
        public final boolean regionMatches(boolean z, int i, int i2, int i3, int i4) {
            if (i < 0 || i2 - i < i4) {
                return false;
            }
            if (z) {
                return regionMatchesIgnoreCase(i, i2, i3, i4);
            }
            return regionMatches(i, i2, i3, i4);
        }

        private final boolean regionMatches(int i, int i2, int i3, int i4) {
            while (true) {
                int i5 = i4 - 1;
                if (i4 <= 0) {
                    return true;
                }
                int i6 = i + 1;
                int i7 = i3 + 1;
                if (this.target.setIndex(i) != this.target.setIndex(i3)) {
                    return false;
                }
                i4 = i5;
                i = i6;
                i3 = i7;
            }
        }

        private final boolean regionMatchesIgnoreCase(int i, int i2, int i3, int i4) {
            char upperCase;
            char upperCase2;
            while (true) {
                int i5 = i4 - 1;
                if (i4 <= 0) {
                    return true;
                }
                int i6 = i + 1;
                char index = this.target.setIndex(i);
                int i7 = i3 + 1;
                char index2 = this.target.setIndex(i3);
                if (index != index2 && (upperCase = Character.toUpperCase(index)) != (upperCase2 = Character.toUpperCase(index2)) && Character.toLowerCase(upperCase) != Character.toLowerCase(upperCase2)) {
                    return false;
                }
                i4 = i5;
                i = i6;
                i3 = i7;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ClosureContext {
        int currentIndex = 0;
        int[] offsets = new int[4];

        ClosureContext() {
        }

        /* access modifiers changed from: package-private */
        public boolean contains(int i) {
            for (int i2 = 0; i2 < this.currentIndex; i2++) {
                if (this.offsets[i2] == i) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.currentIndex = 0;
        }

        /* access modifiers changed from: package-private */
        public void addOffset(int i) {
            if (this.currentIndex == this.offsets.length) {
                this.offsets = expandOffsets();
            }
            int[] iArr = this.offsets;
            int i2 = this.currentIndex;
            this.currentIndex = i2 + 1;
            iArr[i2] = i;
        }

        private int[] expandOffsets() {
            int[] iArr = this.offsets;
            int[] iArr2 = new int[(iArr.length << 1)];
            System.arraycopy(iArr, 0, iArr2, 0, this.currentIndex);
            return iArr2;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Context {
        private CharArrayTarget charArrayTarget;
        private CharacterIteratorTarget characterIteratorTarget;
        ClosureContext[] closureContexts;
        boolean inuse = false;
        int length;
        int limit;
        Match match;
        int start;
        private StringTarget stringTarget;
        ExpressionTarget target;

        Context() {
        }

        private void resetCommon(int i) {
            this.length = this.limit - this.start;
            setInUse(true);
            this.match = null;
            ClosureContext[] closureContextArr = this.closureContexts;
            if (closureContextArr == null || closureContextArr.length != i) {
                this.closureContexts = new ClosureContext[i];
            }
            for (int i2 = 0; i2 < i; i2++) {
                ClosureContext[] closureContextArr2 = this.closureContexts;
                if (closureContextArr2[i2] == null) {
                    closureContextArr2[i2] = new ClosureContext();
                } else {
                    closureContextArr2[i2].reset();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void reset(CharacterIterator characterIterator, int i, int i2, int i3) {
            CharacterIteratorTarget characterIteratorTarget2 = this.characterIteratorTarget;
            if (characterIteratorTarget2 == null) {
                this.characterIteratorTarget = new CharacterIteratorTarget(characterIterator);
            } else {
                characterIteratorTarget2.resetTarget(characterIterator);
            }
            this.target = this.characterIteratorTarget;
            this.start = i;
            this.limit = i2;
            resetCommon(i3);
        }

        /* access modifiers changed from: package-private */
        public void reset(String str, int i, int i2, int i3) {
            StringTarget stringTarget2 = this.stringTarget;
            if (stringTarget2 == null) {
                this.stringTarget = new StringTarget(str);
            } else {
                stringTarget2.resetTarget(str);
            }
            this.target = this.stringTarget;
            this.start = i;
            this.limit = i2;
            resetCommon(i3);
        }

        /* access modifiers changed from: package-private */
        public void reset(char[] cArr, int i, int i2, int i3) {
            CharArrayTarget charArrayTarget2 = this.charArrayTarget;
            if (charArrayTarget2 == null) {
                this.charArrayTarget = new CharArrayTarget(cArr);
            } else {
                charArrayTarget2.resetTarget(cArr);
            }
            this.target = this.charArrayTarget;
            this.start = i;
            this.limit = i2;
            resetCommon(i3);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setInUse(boolean z) {
            this.inuse = z;
        }
    }

    /* access modifiers changed from: package-private */
    public void prepare() {
        compile(this.tokentree);
        this.minlength = this.tokentree.getMinLength();
        this.firstChar = null;
        if (!isSet(this.options, 128) && !isSet(this.options, 512)) {
            RangeToken createRange = Token.createRange();
            if (this.tokentree.analyzeFirstCharacter(createRange, this.options) == 1) {
                createRange.compactRanges();
                this.firstChar = createRange;
            }
        }
        Op op = this.operations;
        if (op != null && ((op.type == 6 || this.operations.type == 1) && this.operations.next == null)) {
            this.fixedStringOnly = true;
            if (this.operations.type == 6) {
                this.fixedString = this.operations.getString();
            } else if (this.operations.getData() >= 65536) {
                this.fixedString = REUtil.decomposeToSurrogates(this.operations.getData());
            } else {
                this.fixedString = new String(new char[]{(char) this.operations.getData()});
            }
            this.fixedStringOptions = this.options;
            this.fixedStringTable = new BMPattern(this.fixedString, 256, isSet(this.fixedStringOptions, 2));
        } else if (!isSet(this.options, 256) && !isSet(this.options, 512)) {
            Token.FixedStringContainer fixedStringContainer = new Token.FixedStringContainer();
            this.tokentree.findFixedString(fixedStringContainer, this.options);
            this.fixedString = fixedStringContainer.token == null ? null : fixedStringContainer.token.getString();
            this.fixedStringOptions = fixedStringContainer.options;
            String str = this.fixedString;
            if (str != null && str.length() < 2) {
                this.fixedString = null;
            }
            String str2 = this.fixedString;
            if (str2 != null) {
                this.fixedStringTable = new BMPattern(str2, 256, isSet(this.fixedStringOptions, 2));
            }
        }
    }

    public RegularExpression(String str) throws ParseException {
        this(str, null);
    }

    public RegularExpression(String str, String str2) throws ParseException {
        this.hasBackReferences = false;
        this.operations = null;
        this.context = null;
        this.firstChar = null;
        this.fixedString = null;
        this.fixedStringTable = null;
        this.fixedStringOnly = false;
        setPattern(str, str2);
    }

    public RegularExpression(String str, String str2, Locale locale) throws ParseException {
        this.hasBackReferences = false;
        this.operations = null;
        this.context = null;
        this.firstChar = null;
        this.fixedString = null;
        this.fixedStringTable = null;
        this.fixedStringOnly = false;
        setPattern(str, str2, locale);
    }

    RegularExpression(String str, Token token, int i, boolean z, int i2) {
        this.hasBackReferences = false;
        this.operations = null;
        this.context = null;
        this.firstChar = null;
        this.fixedString = null;
        this.fixedStringTable = null;
        this.fixedStringOnly = false;
        this.regex = str;
        this.tokentree = token;
        this.nofparen = i;
        this.options = i2;
        this.hasBackReferences = z;
    }

    public void setPattern(String str) throws ParseException {
        setPattern(str, Locale.getDefault());
    }

    public void setPattern(String str, Locale locale) throws ParseException {
        setPattern(str, this.options, locale);
    }

    private void setPattern(String str, int i, Locale locale) throws ParseException {
        this.regex = str;
        this.options = i;
        RegexParser parserForXMLSchema = isSet(this.options, 512) ? new ParserForXMLSchema(locale) : new RegexParser(locale);
        this.tokentree = parserForXMLSchema.parse(this.regex, this.options);
        this.nofparen = parserForXMLSchema.parennumber;
        this.hasBackReferences = parserForXMLSchema.hasBackReferences;
        this.operations = null;
        this.context = null;
    }

    public void setPattern(String str, String str2) throws ParseException {
        setPattern(str, str2, Locale.getDefault());
    }

    public void setPattern(String str, String str2, Locale locale) throws ParseException {
        setPattern(str, REUtil.parseOptions(str2), locale);
    }

    public String getPattern() {
        return this.regex;
    }

    @Override // java.lang.Object
    public String toString() {
        return this.tokentree.toString(this.options);
    }

    public String getOptions() {
        return REUtil.createOptionString(this.options);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RegularExpression)) {
            return false;
        }
        RegularExpression regularExpression = (RegularExpression) obj;
        if (!this.regex.equals(regularExpression.regex) || this.options != regularExpression.options) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean equals(String str, int i) {
        return this.regex.equals(str) && this.options == i;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (this.regex + PsuedoNames.PSEUDONAME_ROOT + getOptions()).hashCode();
    }

    public int getNumberOfGroups() {
        return this.nofparen;
    }

    private static final int getWordType0(char c, int i) {
        if (!isSet(i, 64)) {
            return isSet(i, 32) ? Token.getRange("IsWord", true).match(c) ? 1 : 2 : isWordChar(c) ? 1 : 2;
        }
        int type = Character.getType(c);
        if (type != 15) {
            if (type != 16) {
                switch (type) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                        return 1;
                    case 6:
                    case 7:
                        break;
                    default:
                        return 2;
                }
            }
            return 0;
        }
        switch (c) {
            case '\t':
            case '\n':
            case 11:
            case '\f':
            case '\r':
                return 2;
            default:
                return 0;
        }
    }

    private static final boolean matchIgnoreCase(int i, int i2) {
        if (i == i2) {
            return true;
        }
        if (i > 65535 || i2 > 65535) {
            return false;
        }
        char upperCase = Character.toUpperCase((char) i);
        char upperCase2 = Character.toUpperCase((char) i2);
        return upperCase == upperCase2 || Character.toLowerCase(upperCase) == Character.toLowerCase(upperCase2);
    }
}
