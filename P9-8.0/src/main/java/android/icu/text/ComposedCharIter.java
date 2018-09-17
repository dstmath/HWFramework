package android.icu.text;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;

@Deprecated
public final class ComposedCharIter {
    @Deprecated
    public static final char DONE = '￿';
    private int curChar;
    private String decompBuf;
    private final Normalizer2Impl n2impl;
    private int nextChar;

    @Deprecated
    public ComposedCharIter() {
        this(false, 0);
    }

    @Deprecated
    public ComposedCharIter(boolean compat, int options) {
        this.curChar = 0;
        this.nextChar = -1;
        if (compat) {
            this.n2impl = Norm2AllModes.getNFKCInstance().impl;
        } else {
            this.n2impl = Norm2AllModes.getNFCInstance().impl;
        }
    }

    @Deprecated
    public boolean hasNext() {
        if (this.nextChar == -1) {
            findNextChar();
        }
        return this.nextChar != -1;
    }

    @Deprecated
    public char next() {
        if (this.nextChar == -1) {
            findNextChar();
        }
        this.curChar = this.nextChar;
        this.nextChar = -1;
        return (char) this.curChar;
    }

    @Deprecated
    public String decomposition() {
        if (this.decompBuf != null) {
            return this.decompBuf;
        }
        return "";
    }

    private void findNextChar() {
        int c = this.curChar + 1;
        this.decompBuf = null;
        while (c < DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            this.decompBuf = this.n2impl.getDecomposition(c);
            if (this.decompBuf != null) {
                break;
            }
            c++;
        }
        c = -1;
        this.nextChar = c;
    }
}
