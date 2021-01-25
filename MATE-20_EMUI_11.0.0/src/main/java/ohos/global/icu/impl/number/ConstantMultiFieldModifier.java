package ohos.global.icu.impl.number;

import java.text.Format;
import java.util.Arrays;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.number.Modifier;

public class ConstantMultiFieldModifier implements Modifier {
    private final boolean overwrite;
    private final Modifier.Parameters parameters;
    protected final char[] prefixChars;
    protected final Format.Field[] prefixFields;
    private final boolean strong;
    protected final char[] suffixChars;
    protected final Format.Field[] suffixFields;

    public ConstantMultiFieldModifier(FormattedStringBuilder formattedStringBuilder, FormattedStringBuilder formattedStringBuilder2, boolean z, boolean z2) {
        this(formattedStringBuilder, formattedStringBuilder2, z, z2, null);
    }

    public ConstantMultiFieldModifier(FormattedStringBuilder formattedStringBuilder, FormattedStringBuilder formattedStringBuilder2, boolean z, boolean z2, Modifier.Parameters parameters2) {
        this.prefixChars = formattedStringBuilder.toCharArray();
        this.suffixChars = formattedStringBuilder2.toCharArray();
        this.prefixFields = formattedStringBuilder.toFieldArray();
        this.suffixFields = formattedStringBuilder2.toFieldArray();
        this.overwrite = z;
        this.strong = z2;
        this.parameters = parameters2;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int apply(FormattedStringBuilder formattedStringBuilder, int i, int i2) {
        int insert = formattedStringBuilder.insert(i, this.prefixChars, this.prefixFields);
        if (this.overwrite) {
            insert += formattedStringBuilder.splice(i + insert, i2 + insert, "", 0, 0, null);
        }
        return insert + formattedStringBuilder.insert(i2 + insert, this.suffixChars, this.suffixFields);
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int getPrefixLength() {
        return this.prefixChars.length;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int getCodePointCount() {
        char[] cArr = this.prefixChars;
        int codePointCount = Character.codePointCount(cArr, 0, cArr.length);
        char[] cArr2 = this.suffixChars;
        return codePointCount + Character.codePointCount(cArr2, 0, cArr2.length);
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean isStrong() {
        return this.strong;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean containsField(Format.Field field) {
        int i = 0;
        while (true) {
            Format.Field[] fieldArr = this.prefixFields;
            if (i >= fieldArr.length) {
                int i2 = 0;
                while (true) {
                    Format.Field[] fieldArr2 = this.suffixFields;
                    if (i2 >= fieldArr2.length) {
                        return false;
                    }
                    if (fieldArr2[i2] == field) {
                        return true;
                    }
                    i2++;
                }
            } else if (fieldArr[i] == field) {
                return true;
            } else {
                i++;
            }
        }
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public Modifier.Parameters getParameters() {
        return this.parameters;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean semanticallyEquivalent(Modifier modifier) {
        if (!(modifier instanceof ConstantMultiFieldModifier)) {
            return false;
        }
        ConstantMultiFieldModifier constantMultiFieldModifier = (ConstantMultiFieldModifier) modifier;
        Modifier.Parameters parameters2 = this.parameters;
        if (parameters2 != null && constantMultiFieldModifier.parameters != null && parameters2.obj == constantMultiFieldModifier.parameters.obj) {
            return true;
        }
        if (!Arrays.equals(this.prefixChars, constantMultiFieldModifier.prefixChars) || !Arrays.equals(this.prefixFields, constantMultiFieldModifier.prefixFields) || !Arrays.equals(this.suffixChars, constantMultiFieldModifier.suffixChars) || !Arrays.equals(this.suffixFields, constantMultiFieldModifier.suffixFields) || this.overwrite != constantMultiFieldModifier.overwrite || this.strong != constantMultiFieldModifier.strong) {
            return false;
        }
        return true;
    }

    public String toString() {
        FormattedStringBuilder formattedStringBuilder = new FormattedStringBuilder();
        apply(formattedStringBuilder, 0, 0);
        int prefixLength = getPrefixLength();
        return String.format("<ConstantMultiFieldModifier prefix:'%s' suffix:'%s'>", formattedStringBuilder.subSequence(0, prefixLength), formattedStringBuilder.subSequence(prefixLength, formattedStringBuilder.length()));
    }
}
