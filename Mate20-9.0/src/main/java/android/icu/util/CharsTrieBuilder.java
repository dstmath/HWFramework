package android.icu.util;

import android.icu.impl.Normalizer2Impl;
import android.icu.util.StringTrieBuilder;
import java.nio.CharBuffer;

public final class CharsTrieBuilder extends StringTrieBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private char[] chars;
    private int charsLength;
    private final char[] intUnits = new char[3];

    public CharsTrieBuilder add(CharSequence s, int value) {
        addImpl(s, value);
        return this;
    }

    public CharsTrie build(StringTrieBuilder.Option buildOption) {
        return new CharsTrie(buildCharSequence(buildOption), 0);
    }

    public CharSequence buildCharSequence(StringTrieBuilder.Option buildOption) {
        buildChars(buildOption);
        return CharBuffer.wrap(this.chars, this.chars.length - this.charsLength, this.charsLength);
    }

    private void buildChars(StringTrieBuilder.Option buildOption) {
        if (this.chars == null) {
            this.chars = new char[1024];
        }
        buildImpl(buildOption);
    }

    public CharsTrieBuilder clear() {
        clearImpl();
        this.chars = null;
        this.charsLength = 0;
        return this;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean matchNodesCanHaveValues() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int getMaxBranchLinearSubNodeLength() {
        return 5;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int getMinLinearMatch() {
        return 48;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int getMaxLinearMatchLength() {
        return 16;
    }

    private void ensureCapacity(int length) {
        if (length > this.chars.length) {
            int newCapacity = this.chars.length;
            do {
                newCapacity *= 2;
            } while (newCapacity <= length);
            char[] newChars = new char[newCapacity];
            System.arraycopy(this.chars, this.chars.length - this.charsLength, newChars, newChars.length - this.charsLength, this.charsLength);
            this.chars = newChars;
        }
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int write(int unit) {
        int newLength = this.charsLength + 1;
        ensureCapacity(newLength);
        this.charsLength = newLength;
        this.chars[this.chars.length - this.charsLength] = (char) unit;
        return this.charsLength;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int write(int offset, int length) {
        int newLength = this.charsLength + length;
        ensureCapacity(newLength);
        this.charsLength = newLength;
        int charsOffset = this.chars.length - this.charsLength;
        while (length > 0) {
            this.chars[charsOffset] = this.strings.charAt(offset);
            length--;
            charsOffset++;
            offset++;
        }
        return this.charsLength;
    }

    private int write(char[] s, int length) {
        int newLength = this.charsLength + length;
        ensureCapacity(newLength);
        this.charsLength = newLength;
        System.arraycopy(s, 0, this.chars, this.chars.length - this.charsLength, length);
        return this.charsLength;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int writeValueAndFinal(int i, boolean isFinal) {
        char c = 32768;
        if (i < 0 || i > 16383) {
            int length = 2;
            if (i < 0 || i > 1073676287) {
                this.intUnits[0] = 32767;
                this.intUnits[1] = (char) (i >> 16);
                this.intUnits[2] = (char) i;
                length = 3;
            } else {
                this.intUnits[0] = (char) (16384 + (i >> 16));
                this.intUnits[1] = (char) i;
            }
            char[] cArr = this.intUnits;
            char c2 = this.intUnits[0];
            if (!isFinal) {
                c = 0;
            }
            cArr[0] = (char) (c | c2);
            return write(this.intUnits, length);
        }
        if (!isFinal) {
            c = 0;
        }
        return write(c | i);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int writeValueAndType(boolean hasValue, int value, int node) {
        if (!hasValue) {
            return write(node);
        }
        int length = 2;
        if (value < 0 || value > 16646143) {
            this.intUnits[0] = 32704;
            this.intUnits[1] = (char) (value >> 16);
            this.intUnits[2] = (char) value;
            length = 3;
        } else if (value <= 255) {
            this.intUnits[0] = (char) ((value + 1) << 6);
            length = 1;
        } else {
            this.intUnits[0] = (char) (16448 + (32704 & (value >> 10)));
            this.intUnits[1] = (char) value;
        }
        char[] cArr = this.intUnits;
        cArr[0] = (char) (cArr[0] | ((char) node));
        return write(this.intUnits, length);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int writeDeltaTo(int jumpTarget) {
        int length;
        int i = this.charsLength - jumpTarget;
        if (i <= 64511) {
            return write(i);
        }
        if (i <= 67043327) {
            this.intUnits[0] = (char) (Normalizer2Impl.MIN_NORMAL_MAYBE_YES + (i >> 16));
            length = 1;
        } else {
            this.intUnits[0] = 65535;
            this.intUnits[1] = (char) (i >> 16);
            length = 2;
        }
        int length2 = length + 1;
        this.intUnits[length] = (char) i;
        return write(this.intUnits, length2);
    }
}
