package ohos.global.icu.impl.number;

import ohos.global.icu.number.IntegerWidth;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.Precision;
import ohos.global.icu.text.DecimalFormatSymbols;

public class MicroProps implements Cloneable, MicroPropsGenerator {
    public NumberFormatter.DecimalSeparatorDisplay decimal;
    private volatile boolean exhausted;
    public Grouper grouping;
    private final boolean immutable;
    public IntegerWidth integerWidth;
    public Modifier modInner;
    public Modifier modMiddle;
    public Modifier modOuter;
    public String nsName;
    public Padder padding;
    public Precision rounder;
    public NumberFormatter.SignDisplay sign;
    public DecimalFormatSymbols symbols;
    public boolean useCurrency;

    public MicroProps(boolean z) {
        this.immutable = z;
    }

    @Override // ohos.global.icu.impl.number.MicroPropsGenerator
    public MicroProps processQuantity(DecimalQuantity decimalQuantity) {
        if (this.immutable) {
            return (MicroProps) clone();
        }
        if (!this.exhausted) {
            this.exhausted = true;
            return this;
        }
        throw new AssertionError("Cannot re-use a mutable MicroProps in the quantity chain");
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
