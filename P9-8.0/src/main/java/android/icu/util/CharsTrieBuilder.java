package android.icu.util;

import android.icu.util.StringTrieBuilder.Option;
import java.nio.CharBuffer;

public final class CharsTrieBuilder extends StringTrieBuilder {
    static final /* synthetic */ boolean -assertionsDisabled = (CharsTrieBuilder.class.desiredAssertionStatus() ^ 1);
    private char[] chars;
    private int charsLength;
    private final char[] intUnits = new char[3];

    public CharsTrieBuilder add(CharSequence s, int value) {
        addImpl(s, value);
        return this;
    }

    public CharsTrie build(Option buildOption) {
        return new CharsTrie(buildCharSequence(buildOption), 0);
    }

    public CharSequence buildCharSequence(Option buildOption) {
        buildChars(buildOption);
        return CharBuffer.wrap(this.chars, this.chars.length - this.charsLength, this.charsLength);
    }

    private void buildChars(Option buildOption) {
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

    @Deprecated
    protected boolean matchNodesCanHaveValues() {
        return true;
    }

    @Deprecated
    protected int getMaxBranchLinearSubNodeLength() {
        return 5;
    }

    @Deprecated
    protected int getMinLinearMatch() {
        return 48;
    }

    @Deprecated
    protected int getMaxLinearMatchLength() {
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

    @Deprecated
    protected int write(int unit) {
        int newLength = this.charsLength + 1;
        ensureCapacity(newLength);
        this.charsLength = newLength;
        this.chars[this.chars.length - this.charsLength] = (char) unit;
        return this.charsLength;
    }

    @Deprecated
    protected int write(int offset, int length) {
        int newLength = this.charsLength + length;
        ensureCapacity(newLength);
        this.charsLength = newLength;
        int charsOffset = this.chars.length - this.charsLength;
        int offset2 = offset;
        while (length > 0) {
            int charsOffset2 = charsOffset + 1;
            offset = offset2 + 1;
            this.chars[charsOffset] = this.strings.charAt(offset2);
            length--;
            charsOffset = charsOffset2;
            offset2 = offset;
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

    @Deprecated
    protected int writeValueAndFinal(int i, boolean isFinal) {
        int i2 = 32768;
        if (i < 0 || i > 16383) {
            int length;
            if (i < 0 || i > 1073676287) {
                this.intUnits[0] = 32767;
                this.intUnits[1] = (char) (i >> 16);
                this.intUnits[2] = (char) i;
                length = 3;
            } else {
                this.intUnits[0] = (char) ((i >> 16) + 16384);
                this.intUnits[1] = (char) i;
                length = 2;
            }
            char[] cArr = this.intUnits;
            char c = this.intUnits[0];
            if (!isFinal) {
                i2 = 0;
            }
            cArr[0] = (char) (i2 | c);
            return write(this.intUnits, length);
        }
        if (!isFinal) {
            i2 = 0;
        }
        return write(i2 | i);
    }

    @Deprecated
    protected int writeValueAndType(boolean hasValue, int value, int node) {
        if (!hasValue) {
            return write(node);
        }
        int length;
        if (value < 0 || value > 16646143) {
            this.intUnits[0] = 32704;
            this.intUnits[1] = (char) (value >> 16);
            this.intUnits[2] = (char) value;
            length = 3;
        } else if (value <= 255) {
            this.intUnits[0] = (char) ((value + 1) << 6);
            length = 1;
        } else {
            this.intUnits[0] = (char) (((value >> 10) & 32704) + 16448);
            this.intUnits[1] = (char) value;
            length = 2;
        }
        char[] cArr = this.intUnits;
        cArr[0] = (char) (cArr[0] | ((char) node));
        return write(this.intUnits, length);
    }

    @Deprecated
    protected int writeDeltaTo(int jumpTarget) {
        int i = this.charsLength - jumpTarget;
        if (!-assertionsDisabled && i < 0) {
            throw new AssertionError();
        } else if (i <= 64511) {
            return write(i);
        } else {
            int length;
            if (i <= 67043327) {
                this.intUnits[0] = (char) ((i >> 16) + 64512);
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
}
