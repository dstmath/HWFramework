package ohos.global.icu.number;

import java.math.BigDecimal;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.util.Arrays;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.FormattedValueStringBuilderImpl;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.text.ConstrainedFieldPosition;
import ohos.global.icu.text.FormattedValue;
import ohos.global.icu.text.PluralRules;

public class FormattedNumber implements FormattedValue {
    final DecimalQuantity fq;
    final FormattedStringBuilder string;

    FormattedNumber(FormattedStringBuilder formattedStringBuilder, DecimalQuantity decimalQuantity) {
        this.string = formattedStringBuilder;
        this.fq = decimalQuantity;
    }

    @Override // ohos.global.icu.text.FormattedValue, java.lang.CharSequence, java.lang.Object
    public String toString() {
        return this.string.toString();
    }

    @Override // java.lang.CharSequence
    public int length() {
        return this.string.length();
    }

    @Override // java.lang.CharSequence
    public char charAt(int i) {
        return this.string.charAt(i);
    }

    @Override // java.lang.CharSequence
    public CharSequence subSequence(int i, int i2) {
        return this.string.subString(i, i2);
    }

    @Override // ohos.global.icu.text.FormattedValue
    public <A extends Appendable> A appendTo(A a) {
        return (A) Utility.appendTo(this.string, a);
    }

    @Override // ohos.global.icu.text.FormattedValue
    public boolean nextPosition(ConstrainedFieldPosition constrainedFieldPosition) {
        return FormattedValueStringBuilderImpl.nextPosition(this.string, constrainedFieldPosition, null);
    }

    @Override // ohos.global.icu.text.FormattedValue
    public AttributedCharacterIterator toCharacterIterator() {
        return FormattedValueStringBuilderImpl.toCharacterIterator(this.string, null);
    }

    public boolean nextFieldPosition(FieldPosition fieldPosition) {
        this.fq.populateUFieldPosition(fieldPosition);
        return FormattedValueStringBuilderImpl.nextFieldPosition(this.string, fieldPosition);
    }

    public BigDecimal toBigDecimal() {
        return this.fq.toBigDecimal();
    }

    @Deprecated
    public PluralRules.IFixedDecimal getFixedDecimal() {
        return this.fq;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.fq.toBigDecimal().hashCode() ^ (Arrays.hashCode(this.string.toCharArray()) ^ Arrays.hashCode(this.string.toFieldArray()));
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof FormattedNumber)) {
            return false;
        }
        FormattedNumber formattedNumber = (FormattedNumber) obj;
        return Arrays.equals(this.string.toCharArray(), formattedNumber.string.toCharArray()) && Arrays.equals(this.string.toFieldArray(), formattedNumber.string.toFieldArray()) && this.fq.toBigDecimal().equals(formattedNumber.fq.toBigDecimal());
    }
}
