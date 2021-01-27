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

    public String toString() {
        return this.string.toString();
    }

    public int length() {
        return this.string.length();
    }

    public char charAt(int i) {
        return this.string.charAt(i);
    }

    public CharSequence subSequence(int i, int i2) {
        return this.string.subString(i, i2);
    }

    public <A extends Appendable> A appendTo(A a) {
        return (A) Utility.appendTo(this.string, a);
    }

    public boolean nextPosition(ConstrainedFieldPosition constrainedFieldPosition) {
        return FormattedValueStringBuilderImpl.nextPosition(this.string, constrainedFieldPosition, null);
    }

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

    public int hashCode() {
        return this.fq.toBigDecimal().hashCode() ^ (Arrays.hashCode(this.string.toCharArray()) ^ Arrays.hashCode(this.string.toFieldArray()));
    }

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
