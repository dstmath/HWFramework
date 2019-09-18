package android.icu.impl.number;

import android.icu.text.NumberFormat;

public class SimpleModifier implements Modifier {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ARG_NUM_LIMIT = 256;
    private final String compiledPattern;
    private final NumberFormat.Field field;
    private final int prefixLength;
    private final boolean strong;
    private final int suffixLength;
    private final int suffixOffset;

    public SimpleModifier(String compiledPattern2, NumberFormat.Field field2, boolean strong2) {
        this.compiledPattern = compiledPattern2;
        this.field = field2;
        this.strong = strong2;
        if (compiledPattern2.charAt(1) != 0) {
            this.prefixLength = compiledPattern2.charAt(1) - 256;
            this.suffixOffset = this.prefixLength + 3;
        } else {
            this.prefixLength = 0;
            this.suffixOffset = 2;
        }
        if (3 + this.prefixLength < compiledPattern2.length()) {
            this.suffixLength = compiledPattern2.charAt(this.suffixOffset) - 256;
        } else {
            this.suffixLength = 0;
        }
    }

    public int apply(NumberStringBuilder output, int leftIndex, int rightIndex) {
        return formatAsPrefixSuffix(output, leftIndex, rightIndex, this.field);
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public int getCodePointCount() {
        int count = 0;
        if (this.prefixLength > 0) {
            count = 0 + Character.codePointCount(this.compiledPattern, 2, this.prefixLength + 2);
        }
        if (this.suffixLength > 0) {
            return count + Character.codePointCount(this.compiledPattern, this.suffixOffset + 1, 1 + this.suffixOffset + this.suffixLength);
        }
        return count;
    }

    public boolean isStrong() {
        return this.strong;
    }

    public int formatAsPrefixSuffix(NumberStringBuilder result, int startIndex, int endIndex, NumberFormat.Field field2) {
        if (this.prefixLength > 0) {
            result.insert(startIndex, this.compiledPattern, 2, 2 + this.prefixLength, field2);
        }
        if (this.suffixLength > 0) {
            result.insert(endIndex + this.prefixLength, this.compiledPattern, 1 + this.suffixOffset, 1 + this.suffixOffset + this.suffixLength, field2);
        }
        return this.prefixLength + this.suffixLength;
    }
}
