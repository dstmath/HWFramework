package ohos.global.icu.impl.number;

import java.math.RoundingMode;
import java.util.Objects;
import ohos.global.icu.number.IntegerWidth;
import ohos.global.icu.number.Notation;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.Precision;
import ohos.global.icu.number.Scale;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.MeasureUnit;
import ohos.global.icu.util.ULocale;

public class MacroProps implements Cloneable {
    public AffixPatternProvider affixProvider;
    public NumberFormatter.DecimalSeparatorDisplay decimal;
    public Object grouping;
    public IntegerWidth integerWidth;
    public ULocale loc;
    public Notation notation;
    public Padder padder;
    public MeasureUnit perUnit;
    public Precision precision;
    public RoundingMode roundingMode;
    public PluralRules rules;
    public Scale scale;
    public NumberFormatter.SignDisplay sign;
    public Object symbols;
    public Long threshold;
    public MeasureUnit unit;
    public NumberFormatter.UnitWidth unitWidth;

    public void fallback(MacroProps macroProps) {
        if (this.notation == null) {
            this.notation = macroProps.notation;
        }
        if (this.unit == null) {
            this.unit = macroProps.unit;
        }
        if (this.perUnit == null) {
            this.perUnit = macroProps.perUnit;
        }
        if (this.precision == null) {
            this.precision = macroProps.precision;
        }
        if (this.roundingMode == null) {
            this.roundingMode = macroProps.roundingMode;
        }
        if (this.grouping == null) {
            this.grouping = macroProps.grouping;
        }
        if (this.padder == null) {
            this.padder = macroProps.padder;
        }
        if (this.integerWidth == null) {
            this.integerWidth = macroProps.integerWidth;
        }
        if (this.symbols == null) {
            this.symbols = macroProps.symbols;
        }
        if (this.unitWidth == null) {
            this.unitWidth = macroProps.unitWidth;
        }
        if (this.sign == null) {
            this.sign = macroProps.sign;
        }
        if (this.decimal == null) {
            this.decimal = macroProps.decimal;
        }
        if (this.affixProvider == null) {
            this.affixProvider = macroProps.affixProvider;
        }
        if (this.scale == null) {
            this.scale = macroProps.scale;
        }
        if (this.rules == null) {
            this.rules = macroProps.rules;
        }
        if (this.loc == null) {
            this.loc = macroProps.loc;
        }
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.notation, this.unit, this.perUnit, this.precision, this.roundingMode, this.grouping, this.padder, this.integerWidth, this.symbols, this.unitWidth, this.sign, this.decimal, this.affixProvider, this.scale, this.rules, this.loc);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MacroProps)) {
            return false;
        }
        MacroProps macroProps = (MacroProps) obj;
        return Objects.equals(this.notation, macroProps.notation) && Objects.equals(this.unit, macroProps.unit) && Objects.equals(this.perUnit, macroProps.perUnit) && Objects.equals(this.precision, macroProps.precision) && Objects.equals(this.roundingMode, macroProps.roundingMode) && Objects.equals(this.grouping, macroProps.grouping) && Objects.equals(this.padder, macroProps.padder) && Objects.equals(this.integerWidth, macroProps.integerWidth) && Objects.equals(this.symbols, macroProps.symbols) && Objects.equals(this.unitWidth, macroProps.unitWidth) && Objects.equals(this.sign, macroProps.sign) && Objects.equals(this.decimal, macroProps.decimal) && Objects.equals(this.affixProvider, macroProps.affixProvider) && Objects.equals(this.scale, macroProps.scale) && Objects.equals(this.rules, macroProps.rules) && Objects.equals(this.loc, macroProps.loc);
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
