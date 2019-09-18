package android.icu.impl.number;

import android.icu.text.NumberFormat;

public class ConstantAffixModifier implements Modifier {
    public static final ConstantAffixModifier EMPTY = new ConstantAffixModifier();
    private final NumberFormat.Field field;
    private final String prefix;
    private final boolean strong;
    private final String suffix;

    public ConstantAffixModifier(String prefix2, String suffix2, NumberFormat.Field field2, boolean strong2) {
        this.prefix = prefix2 == null ? "" : prefix2;
        this.suffix = suffix2 == null ? "" : suffix2;
        this.field = field2;
        this.strong = strong2;
    }

    public ConstantAffixModifier() {
        this.prefix = "";
        this.suffix = "";
        this.field = null;
        this.strong = false;
    }

    public int apply(NumberStringBuilder output, int leftIndex, int rightIndex) {
        return output.insert(rightIndex, (CharSequence) this.suffix, this.field) + output.insert(leftIndex, (CharSequence) this.prefix, this.field);
    }

    public int getPrefixLength() {
        return this.prefix.length();
    }

    public int getCodePointCount() {
        return this.prefix.codePointCount(0, this.prefix.length()) + this.suffix.codePointCount(0, this.suffix.length());
    }

    public boolean isStrong() {
        return this.strong;
    }

    public String toString() {
        return String.format("<ConstantAffixModifier prefix:'%s' suffix:'%s'>", new Object[]{this.prefix, this.suffix});
    }
}
