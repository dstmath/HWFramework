package android.icu.number;

import android.icu.impl.number.MacroProps;
import android.icu.impl.number.Padder;
import android.icu.number.NumberFormatter;
import android.icu.number.NumberFormatterSettings;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberingSystem;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;

public abstract class NumberFormatterSettings<T extends NumberFormatterSettings<?>> {
    static final int KEY_DECIMAL = 11;
    static final int KEY_GROUPER = 5;
    static final int KEY_INTEGER = 7;
    static final int KEY_LOCALE = 1;
    static final int KEY_MACROS = 0;
    static final int KEY_MAX = 13;
    static final int KEY_NOTATION = 2;
    static final int KEY_PADDER = 6;
    static final int KEY_ROUNDER = 4;
    static final int KEY_SIGN = 10;
    static final int KEY_SYMBOLS = 8;
    static final int KEY_THRESHOLD = 12;
    static final int KEY_UNIT = 3;
    static final int KEY_UNIT_WIDTH = 9;
    final int key;
    final NumberFormatterSettings<?> parent;
    volatile MacroProps resolvedMacros;
    final Object value;

    /* access modifiers changed from: package-private */
    public abstract T create(int i, Object obj);

    NumberFormatterSettings(NumberFormatterSettings<?> parent2, int key2, Object value2) {
        this.parent = parent2;
        this.key = key2;
        this.value = value2;
    }

    public T notation(Notation notation) {
        return create(2, notation);
    }

    public T unit(MeasureUnit unit) {
        return create(3, unit);
    }

    public T rounding(Rounder rounder) {
        return create(4, rounder);
    }

    @Deprecated
    public T grouping(Grouper grouper) {
        return create(5, grouper);
    }

    public T integerWidth(IntegerWidth style) {
        return create(7, style);
    }

    public T symbols(DecimalFormatSymbols symbols) {
        return create(8, (DecimalFormatSymbols) symbols.clone());
    }

    public T symbols(NumberingSystem ns) {
        return create(8, ns);
    }

    public T unitWidth(NumberFormatter.UnitWidth style) {
        return create(9, style);
    }

    public T sign(NumberFormatter.SignDisplay style) {
        return create(10, style);
    }

    public T decimal(NumberFormatter.DecimalSeparatorDisplay style) {
        return create(11, style);
    }

    @Deprecated
    public T macros(MacroProps macros) {
        return create(0, macros);
    }

    @Deprecated
    public T padding(Padder padder) {
        return create(6, padder);
    }

    @Deprecated
    public T threshold(Long threshold) {
        return create(12, threshold);
    }

    /* access modifiers changed from: package-private */
    public MacroProps resolve() {
        if (this.resolvedMacros != null) {
            return this.resolvedMacros;
        }
        MacroProps macros = new MacroProps();
        for (NumberFormatterSettings numberFormatterSettings = this; numberFormatterSettings != null; numberFormatterSettings = numberFormatterSettings.parent) {
            switch (numberFormatterSettings.key) {
                case 0:
                    macros.fallback((MacroProps) numberFormatterSettings.value);
                    break;
                case 1:
                    if (macros.loc != null) {
                        break;
                    } else {
                        macros.loc = (ULocale) numberFormatterSettings.value;
                        break;
                    }
                case 2:
                    if (macros.notation != null) {
                        break;
                    } else {
                        macros.notation = (Notation) numberFormatterSettings.value;
                        break;
                    }
                case 3:
                    if (macros.unit != null) {
                        break;
                    } else {
                        macros.unit = (MeasureUnit) numberFormatterSettings.value;
                        break;
                    }
                case 4:
                    if (macros.rounder != null) {
                        break;
                    } else {
                        macros.rounder = (Rounder) numberFormatterSettings.value;
                        break;
                    }
                case 5:
                    if (macros.grouper != null) {
                        break;
                    } else {
                        macros.grouper = (Grouper) numberFormatterSettings.value;
                        break;
                    }
                case 6:
                    if (macros.padder != null) {
                        break;
                    } else {
                        macros.padder = (Padder) numberFormatterSettings.value;
                        break;
                    }
                case 7:
                    if (macros.integerWidth != null) {
                        break;
                    } else {
                        macros.integerWidth = (IntegerWidth) numberFormatterSettings.value;
                        break;
                    }
                case 8:
                    if (macros.symbols != null) {
                        break;
                    } else {
                        macros.symbols = numberFormatterSettings.value;
                        break;
                    }
                case 9:
                    if (macros.unitWidth != null) {
                        break;
                    } else {
                        macros.unitWidth = (NumberFormatter.UnitWidth) numberFormatterSettings.value;
                        break;
                    }
                case 10:
                    if (macros.sign != null) {
                        break;
                    } else {
                        macros.sign = (NumberFormatter.SignDisplay) numberFormatterSettings.value;
                        break;
                    }
                case 11:
                    if (macros.decimal != null) {
                        break;
                    } else {
                        macros.decimal = (NumberFormatter.DecimalSeparatorDisplay) numberFormatterSettings.value;
                        break;
                    }
                case 12:
                    if (macros.threshold != null) {
                        break;
                    } else {
                        macros.threshold = (Long) numberFormatterSettings.value;
                        break;
                    }
                default:
                    throw new AssertionError("Unknown key: " + numberFormatterSettings.key);
            }
        }
        this.resolvedMacros = macros;
        return macros;
    }

    public int hashCode() {
        return resolve().hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other != null && (other instanceof NumberFormatterSettings)) {
            return resolve().equals(((NumberFormatterSettings) other).resolve());
        }
        return false;
    }
}
