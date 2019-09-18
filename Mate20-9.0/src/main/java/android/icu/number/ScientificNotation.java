package android.icu.number;

import android.icu.impl.number.DecimalQuantity;
import android.icu.impl.number.MicroProps;
import android.icu.impl.number.MicroPropsGenerator;
import android.icu.impl.number.Modifier;
import android.icu.impl.number.MultiplierProducer;
import android.icu.impl.number.NumberStringBuilder;
import android.icu.number.NumberFormatter;
import android.icu.number.Rounder;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;

public class ScientificNotation extends Notation implements Cloneable {
    int engineeringInterval;
    NumberFormatter.SignDisplay exponentSignDisplay;
    int minExponentDigits;
    boolean requireMinInt;

    private static class ScientificHandler implements MicroPropsGenerator, MultiplierProducer, Modifier {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        int exponent;
        final ScientificNotation notation;
        final MicroPropsGenerator parent;
        final ScientificModifier[] precomputedMods;
        final DecimalFormatSymbols symbols;

        static {
            Class<ScientificNotation> cls = ScientificNotation.class;
        }

        private ScientificHandler(ScientificNotation notation2, DecimalFormatSymbols symbols2, boolean safe, MicroPropsGenerator parent2) {
            this.notation = notation2;
            this.symbols = symbols2;
            this.parent = parent2;
            if (safe) {
                this.precomputedMods = new ScientificModifier[25];
                for (int i = -12; i <= 12; i++) {
                    this.precomputedMods[i + 12] = new ScientificModifier(i, this);
                }
                return;
            }
            this.precomputedMods = null;
        }

        public MicroProps processQuantity(DecimalQuantity quantity) {
            int exponent2;
            MicroProps micros = this.parent.processQuantity(quantity);
            if (!quantity.isZero()) {
                exponent2 = -micros.rounding.chooseMultiplierAndApply(quantity, this);
            } else if (!this.notation.requireMinInt || !(micros.rounding instanceof Rounder.SignificantRounderImpl)) {
                micros.rounding.apply(quantity);
                exponent2 = 0;
            } else {
                ((Rounder.SignificantRounderImpl) micros.rounding).apply(quantity, this.notation.engineeringInterval);
                exponent2 = 0;
            }
            if (this.precomputedMods != null && exponent2 >= -12 && exponent2 <= 12) {
                micros.modInner = this.precomputedMods[exponent2 + 12];
            } else if (this.precomputedMods != null) {
                micros.modInner = new ScientificModifier(exponent2, this);
            } else {
                this.exponent = exponent2;
                micros.modInner = this;
            }
            micros.rounding = Rounder.constructPassThrough();
            return micros;
        }

        public int getMultiplier(int magnitude) {
            int digitsShown;
            int interval = this.notation.engineeringInterval;
            if (this.notation.requireMinInt) {
                digitsShown = interval;
            } else if (interval <= 1) {
                digitsShown = 1;
            } else {
                digitsShown = (((magnitude % interval) + interval) % interval) + 1;
            }
            return (digitsShown - magnitude) - 1;
        }

        public int getPrefixLength() {
            return 0;
        }

        public int getCodePointCount() {
            throw new AssertionError();
        }

        public boolean isStrong() {
            return true;
        }

        public int apply(NumberStringBuilder output, int leftIndex, int rightIndex) {
            return doApply(this.exponent, output, rightIndex);
        }

        /* access modifiers changed from: private */
        public int doApply(int exponent2, NumberStringBuilder output, int rightIndex) {
            int i = rightIndex;
            int i2 = i + output.insert(i, (CharSequence) this.symbols.getExponentSeparator(), NumberFormat.Field.EXPONENT_SYMBOL);
            if (exponent2 < 0 && this.notation.exponentSignDisplay != NumberFormatter.SignDisplay.NEVER) {
                i2 += output.insert(i2, (CharSequence) this.symbols.getMinusSignString(), NumberFormat.Field.EXPONENT_SIGN);
            } else if (exponent2 >= 0 && this.notation.exponentSignDisplay == NumberFormatter.SignDisplay.ALWAYS) {
                i2 += output.insert(i2, (CharSequence) this.symbols.getPlusSignString(), NumberFormat.Field.EXPONENT_SIGN);
            }
            int disp = Math.abs(exponent2);
            int j = 0;
            while (true) {
                if (j >= this.notation.minExponentDigits && disp <= 0) {
                    return i2 - rightIndex;
                }
                int i3 = i2 - j;
                i2 += output.insert(i3, (CharSequence) this.symbols.getDigitStringsLocal()[disp % 10], NumberFormat.Field.EXPONENT);
                j++;
                disp /= 10;
            }
        }
    }

    private static class ScientificModifier implements Modifier {
        final int exponent;
        final ScientificHandler handler;

        ScientificModifier(int exponent2, ScientificHandler handler2) {
            this.exponent = exponent2;
            this.handler = handler2;
        }

        public int apply(NumberStringBuilder output, int leftIndex, int rightIndex) {
            return this.handler.doApply(this.exponent, output, rightIndex);
        }

        public int getPrefixLength() {
            return 0;
        }

        public int getCodePointCount() {
            throw new AssertionError();
        }

        public boolean isStrong() {
            return true;
        }
    }

    ScientificNotation(int engineeringInterval2, boolean requireMinInt2, int minExponentDigits2, NumberFormatter.SignDisplay exponentSignDisplay2) {
        this.engineeringInterval = engineeringInterval2;
        this.requireMinInt = requireMinInt2;
        this.minExponentDigits = minExponentDigits2;
        this.exponentSignDisplay = exponentSignDisplay2;
    }

    public ScientificNotation withMinExponentDigits(int minExponentDigits2) {
        if (minExponentDigits2 < 0 || minExponentDigits2 >= 100) {
            throw new IllegalArgumentException("Integer digits must be between 0 and 100");
        }
        ScientificNotation other = (ScientificNotation) clone();
        other.minExponentDigits = minExponentDigits2;
        return other;
    }

    public ScientificNotation withExponentSignDisplay(NumberFormatter.SignDisplay exponentSignDisplay2) {
        ScientificNotation other = (ScientificNotation) clone();
        other.exponentSignDisplay = exponentSignDisplay2;
        return other;
    }

    @Deprecated
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /* access modifiers changed from: package-private */
    public MicroPropsGenerator withLocaleData(DecimalFormatSymbols symbols, boolean build, MicroPropsGenerator parent) {
        ScientificHandler scientificHandler = new ScientificHandler(symbols, build, parent);
        return scientificHandler;
    }
}
