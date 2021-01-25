package ohos.global.icu.number;

import java.text.Format;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.number.ConstantAffixModifier;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.impl.number.MicroProps;
import ohos.global.icu.impl.number.MicroPropsGenerator;
import ohos.global.icu.impl.number.Modifier;
import ohos.global.icu.impl.number.MultiplierProducer;
import ohos.global.icu.impl.number.RoundingUtils;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.Precision;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.text.NumberFormat;

public class ScientificNotation extends Notation implements Cloneable {
    int engineeringInterval;
    NumberFormatter.SignDisplay exponentSignDisplay;
    int minExponentDigits;
    boolean requireMinInt;

    ScientificNotation(int i, boolean z, int i2, NumberFormatter.SignDisplay signDisplay) {
        this.engineeringInterval = i;
        this.requireMinInt = z;
        this.minExponentDigits = i2;
        this.exponentSignDisplay = signDisplay;
    }

    public ScientificNotation withMinExponentDigits(int i) {
        if (i < 1 || i > 999) {
            throw new IllegalArgumentException("Integer digits must be between 1 and 999 (inclusive)");
        }
        ScientificNotation scientificNotation = (ScientificNotation) clone();
        scientificNotation.minExponentDigits = i;
        return scientificNotation;
    }

    public ScientificNotation withExponentSignDisplay(NumberFormatter.SignDisplay signDisplay) {
        ScientificNotation scientificNotation = (ScientificNotation) clone();
        scientificNotation.exponentSignDisplay = signDisplay;
        return scientificNotation;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /* access modifiers changed from: package-private */
    public MicroPropsGenerator withLocaleData(DecimalFormatSymbols decimalFormatSymbols, boolean z, MicroPropsGenerator microPropsGenerator) {
        return new ScientificHandler(decimalFormatSymbols, z, microPropsGenerator);
    }

    /* access modifiers changed from: private */
    public static class ScientificHandler implements MicroPropsGenerator, MultiplierProducer, Modifier {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        int exponent;
        final ScientificNotation notation;
        final MicroPropsGenerator parent;
        final ScientificModifier[] precomputedMods;
        final DecimalFormatSymbols symbols;

        @Override // ohos.global.icu.impl.number.Modifier
        public boolean containsField(Format.Field field) {
            return false;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public int getCodePointCount() {
            return RoundingUtils.MAX_INT_FRAC_SIG;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public Modifier.Parameters getParameters() {
            return null;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public int getPrefixLength() {
            return 0;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public boolean isStrong() {
            return true;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public boolean semanticallyEquivalent(Modifier modifier) {
            return false;
        }

        private ScientificHandler(ScientificNotation scientificNotation, DecimalFormatSymbols decimalFormatSymbols, boolean z, MicroPropsGenerator microPropsGenerator) {
            this.notation = scientificNotation;
            this.symbols = decimalFormatSymbols;
            this.parent = microPropsGenerator;
            if (z) {
                this.precomputedMods = new ScientificModifier[25];
                for (int i = -12; i <= 12; i++) {
                    this.precomputedMods[i + 12] = new ScientificModifier(i, this);
                }
                return;
            }
            this.precomputedMods = null;
        }

        @Override // ohos.global.icu.impl.number.MicroPropsGenerator
        public MicroProps processQuantity(DecimalQuantity decimalQuantity) {
            MicroProps processQuantity = this.parent.processQuantity(decimalQuantity);
            if (decimalQuantity.isInfinite() || decimalQuantity.isNaN()) {
                processQuantity.modInner = ConstantAffixModifier.EMPTY;
                return processQuantity;
            }
            int i = 0;
            if (!decimalQuantity.isZeroish()) {
                i = -processQuantity.rounder.chooseMultiplierAndApply(decimalQuantity, this);
            } else if (!this.notation.requireMinInt || !(processQuantity.rounder instanceof Precision.SignificantRounderImpl)) {
                processQuantity.rounder.apply(decimalQuantity);
            } else {
                ((Precision.SignificantRounderImpl) processQuantity.rounder).apply(decimalQuantity, this.notation.engineeringInterval);
            }
            ScientificModifier[] scientificModifierArr = this.precomputedMods;
            if (scientificModifierArr != null && i >= -12 && i <= 12) {
                processQuantity.modInner = scientificModifierArr[i + 12];
            } else if (this.precomputedMods != null) {
                processQuantity.modInner = new ScientificModifier(i, this);
            } else {
                this.exponent = i;
                processQuantity.modInner = this;
            }
            processQuantity.rounder = Precision.constructPassThrough();
            return processQuantity;
        }

        @Override // ohos.global.icu.impl.number.MultiplierProducer
        public int getMultiplier(int i) {
            int i2 = this.notation.engineeringInterval;
            if (!this.notation.requireMinInt) {
                if (i2 <= 1) {
                    i2 = 1;
                } else {
                    i2 = (((i % i2) + i2) % i2) + 1;
                }
            }
            return (i2 - i) - 1;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public int apply(FormattedStringBuilder formattedStringBuilder, int i, int i2) {
            return doApply(this.exponent, formattedStringBuilder, i2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int doApply(int i, FormattedStringBuilder formattedStringBuilder, int i2) {
            int abs;
            int i3;
            int insert;
            int insert2 = formattedStringBuilder.insert(i2, this.symbols.getExponentSeparator(), NumberFormat.Field.EXPONENT_SYMBOL) + i2;
            if (i >= 0 || this.notation.exponentSignDisplay == NumberFormatter.SignDisplay.NEVER) {
                if (i >= 0 && this.notation.exponentSignDisplay == NumberFormatter.SignDisplay.ALWAYS) {
                    insert = formattedStringBuilder.insert(insert2, this.symbols.getPlusSignString(), NumberFormat.Field.EXPONENT_SIGN);
                }
                abs = Math.abs(i);
                i3 = 0;
                while (true) {
                    if (i3 < this.notation.minExponentDigits && abs <= 0) {
                        return insert2 - i2;
                    }
                    insert2 += formattedStringBuilder.insert(insert2 - i3, this.symbols.getDigitStringsLocal()[abs % 10], NumberFormat.Field.EXPONENT);
                    i3++;
                    abs /= 10;
                }
            } else {
                insert = formattedStringBuilder.insert(insert2, this.symbols.getMinusSignString(), NumberFormat.Field.EXPONENT_SIGN);
            }
            insert2 += insert;
            abs = Math.abs(i);
            i3 = 0;
            while (true) {
                if (i3 < this.notation.minExponentDigits) {
                }
                insert2 += formattedStringBuilder.insert(insert2 - i3, this.symbols.getDigitStringsLocal()[abs % 10], NumberFormat.Field.EXPONENT);
                i3++;
                abs /= 10;
            }
        }
    }

    private static class ScientificModifier implements Modifier {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        final int exponent;
        final ScientificHandler handler;

        @Override // ohos.global.icu.impl.number.Modifier
        public boolean containsField(Format.Field field) {
            return false;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public int getCodePointCount() {
            return RoundingUtils.MAX_INT_FRAC_SIG;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public Modifier.Parameters getParameters() {
            return null;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public int getPrefixLength() {
            return 0;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public boolean isStrong() {
            return true;
        }

        ScientificModifier(int i, ScientificHandler scientificHandler) {
            this.exponent = i;
            this.handler = scientificHandler;
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public int apply(FormattedStringBuilder formattedStringBuilder, int i, int i2) {
            return this.handler.doApply(this.exponent, formattedStringBuilder, i2);
        }

        @Override // ohos.global.icu.impl.number.Modifier
        public boolean semanticallyEquivalent(Modifier modifier) {
            if ((modifier instanceof ScientificModifier) && this.exponent == ((ScientificModifier) modifier).exponent) {
                return true;
            }
            return false;
        }
    }
}
