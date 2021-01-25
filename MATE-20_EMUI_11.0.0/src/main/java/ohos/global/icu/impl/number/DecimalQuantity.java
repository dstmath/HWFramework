package ohos.global.icu.impl.number;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.FieldPosition;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.text.PluralRules;

public interface DecimalQuantity extends PluralRules.IFixedDecimal {
    void adjustMagnitude(int i);

    void applyMaxInteger(int i);

    void copyFrom(DecimalQuantity decimalQuantity);

    DecimalQuantity createCopy();

    byte getDigit(int i);

    int getLowerDisplayMagnitude();

    int getMagnitude() throws ArithmeticException;

    long getPositionFingerprint();

    StandardPlural getStandardPlural(PluralRules pluralRules);

    int getUpperDisplayMagnitude();

    @Override // ohos.global.icu.text.PluralRules.IFixedDecimal
    boolean isInfinite();

    @Override // ohos.global.icu.text.PluralRules.IFixedDecimal
    boolean isNaN();

    boolean isNegative();

    boolean isZeroish();

    int maxRepresentableDigits();

    void multiplyBy(BigDecimal bigDecimal);

    void negate();

    void populateUFieldPosition(FieldPosition fieldPosition);

    void roundToIncrement(BigDecimal bigDecimal, MathContext mathContext);

    void roundToInfinity();

    void roundToMagnitude(int i, MathContext mathContext);

    void roundToNickel(int i, MathContext mathContext);

    void setMinFraction(int i);

    void setMinInteger(int i);

    void setToBigDecimal(BigDecimal bigDecimal);

    int signum();

    BigDecimal toBigDecimal();

    double toDouble();

    String toPlainString();
}
