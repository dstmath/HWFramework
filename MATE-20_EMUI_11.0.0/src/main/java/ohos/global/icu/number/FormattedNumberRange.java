package ohos.global.icu.number;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.util.Arrays;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.FormattedValueStringBuilderImpl;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.number.NumberRangeFormatter;
import ohos.global.icu.text.ConstrainedFieldPosition;
import ohos.global.icu.text.FormattedValue;
import ohos.global.icu.util.ICUUncheckedIOException;

public class FormattedNumberRange implements FormattedValue {
    final NumberRangeFormatter.RangeIdentityResult identityResult;
    final DecimalQuantity quantity1;
    final DecimalQuantity quantity2;
    final FormattedStringBuilder string;

    FormattedNumberRange(FormattedStringBuilder formattedStringBuilder, DecimalQuantity decimalQuantity, DecimalQuantity decimalQuantity2, NumberRangeFormatter.RangeIdentityResult rangeIdentityResult) {
        this.string = formattedStringBuilder;
        this.quantity1 = decimalQuantity;
        this.quantity2 = decimalQuantity2;
        this.identityResult = rangeIdentityResult;
    }

    @Override // ohos.global.icu.text.FormattedValue, java.lang.CharSequence, java.lang.Object
    public String toString() {
        return this.string.toString();
    }

    @Override // ohos.global.icu.text.FormattedValue
    public <A extends Appendable> A appendTo(A a) {
        try {
            a.append(this.string);
            return a;
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    @Override // java.lang.CharSequence
    public char charAt(int i) {
        return this.string.charAt(i);
    }

    @Override // java.lang.CharSequence
    public int length() {
        return this.string.length();
    }

    @Override // java.lang.CharSequence
    public CharSequence subSequence(int i, int i2) {
        return this.string.subString(i, i2);
    }

    @Override // ohos.global.icu.text.FormattedValue
    public boolean nextPosition(ConstrainedFieldPosition constrainedFieldPosition) {
        return FormattedValueStringBuilderImpl.nextPosition(this.string, constrainedFieldPosition, null);
    }

    public boolean nextFieldPosition(FieldPosition fieldPosition) {
        return FormattedValueStringBuilderImpl.nextFieldPosition(this.string, fieldPosition);
    }

    @Override // ohos.global.icu.text.FormattedValue
    public AttributedCharacterIterator toCharacterIterator() {
        return FormattedValueStringBuilderImpl.toCharacterIterator(this.string, null);
    }

    public BigDecimal getFirstBigDecimal() {
        return this.quantity1.toBigDecimal();
    }

    public BigDecimal getSecondBigDecimal() {
        return this.quantity2.toBigDecimal();
    }

    public NumberRangeFormatter.RangeIdentityResult getIdentityResult() {
        return this.identityResult;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.quantity2.toBigDecimal().hashCode() ^ ((Arrays.hashCode(this.string.toCharArray()) ^ Arrays.hashCode(this.string.toFieldArray())) ^ this.quantity1.toBigDecimal().hashCode());
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof FormattedNumberRange)) {
            return false;
        }
        FormattedNumberRange formattedNumberRange = (FormattedNumberRange) obj;
        return Arrays.equals(this.string.toCharArray(), formattedNumberRange.string.toCharArray()) && Arrays.equals(this.string.toFieldArray(), formattedNumberRange.string.toFieldArray()) && this.quantity1.toBigDecimal().equals(formattedNumberRange.quantity1.toBigDecimal()) && this.quantity2.toBigDecimal().equals(formattedNumberRange.quantity2.toBigDecimal());
    }
}
