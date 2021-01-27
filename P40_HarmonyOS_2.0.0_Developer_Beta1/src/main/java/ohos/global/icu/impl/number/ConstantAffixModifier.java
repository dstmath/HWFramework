package ohos.global.icu.impl.number;

import java.text.Format;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.number.Modifier;

public class ConstantAffixModifier implements Modifier {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final ConstantAffixModifier EMPTY = new ConstantAffixModifier();
    private final Format.Field field;
    private final String prefix;
    private final boolean strong;
    private final String suffix;

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean containsField(Format.Field field2) {
        return false;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public Modifier.Parameters getParameters() {
        return null;
    }

    public ConstantAffixModifier(String str, String str2, Format.Field field2, boolean z) {
        this.prefix = str == null ? "" : str;
        this.suffix = str2 == null ? "" : str2;
        this.field = field2;
        this.strong = z;
    }

    public ConstantAffixModifier() {
        this.prefix = "";
        this.suffix = "";
        this.field = null;
        this.strong = false;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int apply(FormattedStringBuilder formattedStringBuilder, int i, int i2) {
        return formattedStringBuilder.insert(i2, this.suffix, this.field) + formattedStringBuilder.insert(i, this.prefix, this.field);
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int getPrefixLength() {
        return this.prefix.length();
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int getCodePointCount() {
        String str = this.prefix;
        int codePointCount = str.codePointCount(0, str.length());
        String str2 = this.suffix;
        return codePointCount + str2.codePointCount(0, str2.length());
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean isStrong() {
        return this.strong;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean semanticallyEquivalent(Modifier modifier) {
        if (!(modifier instanceof ConstantAffixModifier)) {
            return false;
        }
        ConstantAffixModifier constantAffixModifier = (ConstantAffixModifier) modifier;
        if (!this.prefix.equals(constantAffixModifier.prefix) || !this.suffix.equals(constantAffixModifier.suffix) || this.field != constantAffixModifier.field || this.strong != constantAffixModifier.strong) {
            return false;
        }
        return true;
    }

    public String toString() {
        return String.format("<ConstantAffixModifier prefix:'%s' suffix:'%s'>", this.prefix, this.suffix);
    }
}
