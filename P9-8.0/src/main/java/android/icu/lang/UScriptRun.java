package android.icu.lang;

import android.icu.text.UTF16;

@Deprecated
public final class UScriptRun {
    private static int PAREN_STACK_DEPTH = 32;
    private static int pairedCharExtra = (pairedChars.length - pairedCharPower);
    private static int pairedCharPower = (1 << highBit(pairedChars.length));
    private static int[] pairedChars = new int[]{40, 41, 60, 62, 91, 93, 123, 125, 171, 187, 8216, 8217, 8220, 8221, 8249, 8250, 12296, 12297, 12298, 12299, 12300, 12301, 12302, 12303, 12304, 12305, 12308, 12309, 12310, 12311, 12312, 12313, 12314, 12315};
    private static ParenStackEntry[] parenStack = new ParenStackEntry[PAREN_STACK_DEPTH];
    private char[] emptyCharArray;
    private int fixupCount;
    private int parenSP;
    private int pushCount;
    private int scriptCode;
    private int scriptLimit;
    private int scriptStart;
    private char[] text;
    private int textIndex;
    private int textLimit;
    private int textStart;

    private static final class ParenStackEntry {
        int pairIndex;
        int scriptCode;

        public ParenStackEntry(int thePairIndex, int theScriptCode) {
            this.pairIndex = thePairIndex;
            this.scriptCode = theScriptCode;
        }
    }

    @Deprecated
    public UScriptRun() {
        this.emptyCharArray = new char[0];
        this.parenSP = -1;
        this.pushCount = 0;
        this.fixupCount = 0;
        reset(null, 0, 0);
    }

    @Deprecated
    public UScriptRun(String text) {
        this.emptyCharArray = new char[0];
        this.parenSP = -1;
        this.pushCount = 0;
        this.fixupCount = 0;
        reset(text);
    }

    @Deprecated
    public UScriptRun(String text, int start, int count) {
        this.emptyCharArray = new char[0];
        this.parenSP = -1;
        this.pushCount = 0;
        this.fixupCount = 0;
        reset(text, start, count);
    }

    @Deprecated
    public UScriptRun(char[] chars) {
        this.emptyCharArray = new char[0];
        this.parenSP = -1;
        this.pushCount = 0;
        this.fixupCount = 0;
        reset(chars);
    }

    @Deprecated
    public UScriptRun(char[] chars, int start, int count) {
        this.emptyCharArray = new char[0];
        this.parenSP = -1;
        this.pushCount = 0;
        this.fixupCount = 0;
        reset(chars, start, count);
    }

    @Deprecated
    public final void reset() {
        while (stackIsNotEmpty()) {
            pop();
        }
        this.scriptStart = this.textStart;
        this.scriptLimit = this.textStart;
        this.scriptCode = -1;
        this.parenSP = -1;
        this.pushCount = 0;
        this.fixupCount = 0;
        this.textIndex = this.textStart;
    }

    @Deprecated
    public final void reset(int start, int count) throws IllegalArgumentException {
        int len = 0;
        if (this.text != null) {
            len = this.text.length;
        }
        if (start < 0 || count < 0 || start > len - count) {
            throw new IllegalArgumentException();
        }
        this.textStart = start;
        this.textLimit = start + count;
        reset();
    }

    @Deprecated
    public final void reset(char[] chars, int start, int count) {
        if (chars == null) {
            chars = this.emptyCharArray;
        }
        this.text = chars;
        reset(start, count);
    }

    @Deprecated
    public final void reset(char[] chars) {
        int length = 0;
        if (chars != null) {
            length = chars.length;
        }
        reset(chars, 0, length);
    }

    @Deprecated
    public final void reset(String str, int start, int count) {
        char[] chars = null;
        if (str != null) {
            chars = str.toCharArray();
        }
        reset(chars, start, count);
    }

    @Deprecated
    public final void reset(String str) {
        int length = 0;
        if (str != null) {
            length = str.length();
        }
        reset(str, 0, length);
    }

    @Deprecated
    public final int getScriptStart() {
        return this.scriptStart;
    }

    @Deprecated
    public final int getScriptLimit() {
        return this.scriptLimit;
    }

    @Deprecated
    public final int getScriptCode() {
        return this.scriptCode;
    }

    @Deprecated
    public final boolean next() {
        if (this.scriptLimit >= this.textLimit) {
            return false;
        }
        this.scriptCode = 0;
        this.scriptStart = this.scriptLimit;
        syncFixup();
        while (this.textIndex < this.textLimit) {
            int ch = UTF16.charAt(this.text, this.textStart, this.textLimit, this.textIndex - this.textStart);
            int codePointCount = UTF16.getCharCount(ch);
            int sc = UScript.getScript(ch);
            int pairIndex = getPairIndex(ch);
            this.textIndex += codePointCount;
            if (pairIndex >= 0) {
                if ((pairIndex & 1) == 0) {
                    push(pairIndex, this.scriptCode);
                } else {
                    int pi = pairIndex & -2;
                    while (stackIsNotEmpty() && top().pairIndex != pi) {
                        pop();
                    }
                    if (stackIsNotEmpty()) {
                        sc = top().scriptCode;
                    }
                }
            }
            if (!sameScript(this.scriptCode, sc)) {
                this.textIndex -= codePointCount;
                break;
            }
            if (this.scriptCode <= 1 && sc > 1) {
                this.scriptCode = sc;
                fixup(this.scriptCode);
            }
            if (pairIndex >= 0 && (pairIndex & 1) != 0) {
                pop();
            }
        }
        this.scriptLimit = this.textIndex;
        return true;
    }

    private static boolean sameScript(int scriptOne, int scriptTwo) {
        return scriptOne <= 1 || scriptTwo <= 1 || scriptOne == scriptTwo;
    }

    private static final int mod(int sp) {
        return sp % PAREN_STACK_DEPTH;
    }

    private static final int inc(int sp, int count) {
        return mod(sp + count);
    }

    private static final int inc(int sp) {
        return inc(sp, 1);
    }

    private static final int dec(int sp, int count) {
        return mod((PAREN_STACK_DEPTH + sp) - count);
    }

    private static final int dec(int sp) {
        return dec(sp, 1);
    }

    private static final int limitInc(int count) {
        if (count < PAREN_STACK_DEPTH) {
            return count + 1;
        }
        return count;
    }

    private final boolean stackIsEmpty() {
        return this.pushCount <= 0;
    }

    private final boolean stackIsNotEmpty() {
        return stackIsEmpty() ^ 1;
    }

    private final void push(int pairIndex, int scrptCode) {
        this.pushCount = limitInc(this.pushCount);
        this.fixupCount = limitInc(this.fixupCount);
        this.parenSP = inc(this.parenSP);
        parenStack[this.parenSP] = new ParenStackEntry(pairIndex, scrptCode);
    }

    private final void pop() {
        if (!stackIsEmpty()) {
            parenStack[this.parenSP] = null;
            if (this.fixupCount > 0) {
                this.fixupCount--;
            }
            this.pushCount--;
            this.parenSP = dec(this.parenSP);
            if (stackIsEmpty()) {
                this.parenSP = -1;
            }
        }
    }

    private final ParenStackEntry top() {
        return parenStack[this.parenSP];
    }

    private final void syncFixup() {
        this.fixupCount = 0;
    }

    private final void fixup(int scrptCode) {
        int fixupSP = dec(this.parenSP, this.fixupCount);
        while (true) {
            int i = this.fixupCount;
            this.fixupCount = i - 1;
            if (i > 0) {
                fixupSP = inc(fixupSP);
                parenStack[fixupSP].scriptCode = scrptCode;
            } else {
                return;
            }
        }
    }

    private static final byte highBit(int n) {
        if (n <= 0) {
            return (byte) -32;
        }
        byte bit = (byte) 0;
        if (n >= 65536) {
            n >>= 16;
            bit = (byte) 16;
        }
        if (n >= 256) {
            n >>= 8;
            bit = (byte) (bit + 8);
        }
        if (n >= 16) {
            n >>= 4;
            bit = (byte) (bit + 4);
        }
        if (n >= 4) {
            n >>= 2;
            bit = (byte) (bit + 2);
        }
        if (n >= 2) {
            n >>= 1;
            bit = (byte) (bit + 1);
        }
        return bit;
    }

    private static int getPairIndex(int ch) {
        int probe = pairedCharPower;
        int index = 0;
        if (ch >= pairedChars[pairedCharExtra]) {
            index = pairedCharExtra;
        }
        while (probe > 1) {
            probe >>= 1;
            if (ch >= pairedChars[index + probe]) {
                index += probe;
            }
        }
        if (pairedChars[index] != ch) {
            return -1;
        }
        return index;
    }
}
