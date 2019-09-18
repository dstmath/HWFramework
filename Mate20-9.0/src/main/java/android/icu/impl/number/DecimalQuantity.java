package android.icu.impl.number;

import android.icu.impl.StandardPlural;
import android.icu.text.PluralRules;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.FieldPosition;

public interface DecimalQuantity extends PluralRules.IFixedDecimal {
    void adjustMagnitude(int i);

    void copyFrom(DecimalQuantity decimalQuantity);

    DecimalQuantity createCopy();

    byte getDigit(int i);

    int getLowerDisplayMagnitude();

    int getMagnitude() throws ArithmeticException;

    long getPositionFingerprint();

    StandardPlural getStandardPlural(PluralRules pluralRules);

    int getUpperDisplayMagnitude();

    boolean isInfinite();

    boolean isNaN();

    boolean isNegative();

    boolean isZero();

    int maxRepresentableDigits();

    void multiplyBy(BigDecimal bigDecimal);

    void populateUFieldPosition(FieldPosition fieldPosition);

    void roundToIncrement(BigDecimal bigDecimal, MathContext mathContext);

    void roundToInfinity();

    void roundToMagnitude(int i, MathContext mathContext);

    void setFractionLength(int i, int i2);

    void setIntegerLength(int i, int i2);

    void setToBigDecimal(BigDecimal bigDecimal);

    BigDecimal toBigDecimal();

    double toDouble();

    String toPlainString();
}
