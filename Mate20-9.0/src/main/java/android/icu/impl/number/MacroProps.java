package android.icu.impl.number;

import android.icu.impl.Utility;
import android.icu.number.Grouper;
import android.icu.number.IntegerWidth;
import android.icu.number.Notation;
import android.icu.number.NumberFormatter;
import android.icu.number.Rounder;
import android.icu.text.PluralRules;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;

public class MacroProps implements Cloneable {
    public AffixPatternProvider affixProvider;
    public NumberFormatter.DecimalSeparatorDisplay decimal;
    public Grouper grouper;
    public IntegerWidth integerWidth;
    public ULocale loc;
    public MultiplierImpl multiplier;
    public Notation notation;
    public Padder padder;
    public Rounder rounder;
    public PluralRules rules;
    public NumberFormatter.SignDisplay sign;
    public Object symbols;
    public Long threshold;
    public MeasureUnit unit;
    public NumberFormatter.UnitWidth unitWidth;

    public void fallback(MacroProps fallback) {
        if (this.notation == null) {
            this.notation = fallback.notation;
        }
        if (this.unit == null) {
            this.unit = fallback.unit;
        }
        if (this.rounder == null) {
            this.rounder = fallback.rounder;
        }
        if (this.grouper == null) {
            this.grouper = fallback.grouper;
        }
        if (this.padder == null) {
            this.padder = fallback.padder;
        }
        if (this.integerWidth == null) {
            this.integerWidth = fallback.integerWidth;
        }
        if (this.symbols == null) {
            this.symbols = fallback.symbols;
        }
        if (this.unitWidth == null) {
            this.unitWidth = fallback.unitWidth;
        }
        if (this.sign == null) {
            this.sign = fallback.sign;
        }
        if (this.decimal == null) {
            this.decimal = fallback.decimal;
        }
        if (this.affixProvider == null) {
            this.affixProvider = fallback.affixProvider;
        }
        if (this.multiplier == null) {
            this.multiplier = fallback.multiplier;
        }
        if (this.rules == null) {
            this.rules = fallback.rules;
        }
        if (this.loc == null) {
            this.loc = fallback.loc;
        }
    }

    public int hashCode() {
        return Utility.hash(this.notation, this.unit, this.rounder, this.grouper, this.padder, this.integerWidth, this.symbols, this.unitWidth, this.sign, this.decimal, this.affixProvider, this.multiplier, this.rules, this.loc);
    }

    public boolean equals(Object _other) {
        boolean z = false;
        if (_other == null) {
            return false;
        }
        if (this == _other) {
            return true;
        }
        if (!(_other instanceof MacroProps)) {
            return false;
        }
        MacroProps other = (MacroProps) _other;
        if (Utility.equals(this.notation, other.notation) && Utility.equals(this.unit, other.unit) && Utility.equals(this.rounder, other.rounder) && Utility.equals(this.grouper, other.grouper) && Utility.equals(this.padder, other.padder) && Utility.equals(this.integerWidth, other.integerWidth) && Utility.equals(this.symbols, other.symbols) && Utility.equals(this.unitWidth, other.unitWidth) && Utility.equals(this.sign, other.sign) && Utility.equals(this.decimal, other.decimal) && Utility.equals(this.affixProvider, other.affixProvider) && Utility.equals(this.multiplier, other.multiplier) && Utility.equals(this.rules, other.rules) && Utility.equals(this.loc, other.loc)) {
            z = true;
        }
        return z;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
