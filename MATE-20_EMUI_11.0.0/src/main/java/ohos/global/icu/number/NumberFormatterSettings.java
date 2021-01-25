package ohos.global.icu.number;

import java.math.RoundingMode;
import ohos.global.icu.impl.number.MacroProps;
import ohos.global.icu.impl.number.Padder;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.NumberFormatterSettings;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.text.NumberingSystem;
import ohos.global.icu.util.MeasureUnit;
import ohos.global.icu.util.ULocale;

public abstract class NumberFormatterSettings<T extends NumberFormatterSettings<?>> {
    static final int KEY_DECIMAL = 12;
    static final int KEY_GROUPING = 6;
    static final int KEY_INTEGER = 8;
    static final int KEY_LOCALE = 1;
    static final int KEY_MACROS = 0;
    static final int KEY_MAX = 16;
    static final int KEY_NOTATION = 2;
    static final int KEY_PADDER = 7;
    static final int KEY_PER_UNIT = 15;
    static final int KEY_PRECISION = 4;
    static final int KEY_ROUNDING_MODE = 5;
    static final int KEY_SCALE = 13;
    static final int KEY_SIGN = 11;
    static final int KEY_SYMBOLS = 9;
    static final int KEY_THRESHOLD = 14;
    static final int KEY_UNIT = 3;
    static final int KEY_UNIT_WIDTH = 10;
    private final int key;
    private final NumberFormatterSettings<?> parent;
    private volatile MacroProps resolvedMacros;
    private final Object value;

    /* access modifiers changed from: package-private */
    public abstract T create(int i, Object obj);

    NumberFormatterSettings(NumberFormatterSettings<?> numberFormatterSettings, int i, Object obj) {
        this.parent = numberFormatterSettings;
        this.key = i;
        this.value = obj;
    }

    public T notation(Notation notation) {
        return create(2, notation);
    }

    public T unit(MeasureUnit measureUnit) {
        return create(3, measureUnit);
    }

    public T perUnit(MeasureUnit measureUnit) {
        return create(15, measureUnit);
    }

    public T precision(Precision precision) {
        return create(4, precision);
    }

    public T roundingMode(RoundingMode roundingMode) {
        return create(5, roundingMode);
    }

    public T grouping(NumberFormatter.GroupingStrategy groupingStrategy) {
        return create(6, groupingStrategy);
    }

    public T integerWidth(IntegerWidth integerWidth) {
        return create(8, integerWidth);
    }

    public T symbols(DecimalFormatSymbols decimalFormatSymbols) {
        return create(9, (DecimalFormatSymbols) decimalFormatSymbols.clone());
    }

    public T symbols(NumberingSystem numberingSystem) {
        return create(9, numberingSystem);
    }

    public T unitWidth(NumberFormatter.UnitWidth unitWidth) {
        return create(10, unitWidth);
    }

    public T sign(NumberFormatter.SignDisplay signDisplay) {
        return create(11, signDisplay);
    }

    public T decimal(NumberFormatter.DecimalSeparatorDisplay decimalSeparatorDisplay) {
        return create(12, decimalSeparatorDisplay);
    }

    public T scale(Scale scale) {
        return create(13, scale);
    }

    @Deprecated
    public T macros(MacroProps macroProps) {
        return create(0, macroProps);
    }

    @Deprecated
    public T padding(Padder padder) {
        return create(7, padder);
    }

    @Deprecated
    public T threshold(Long l) {
        return create(14, l);
    }

    public String toSkeleton() {
        return NumberSkeletonImpl.generate(resolve());
    }

    /* access modifiers changed from: package-private */
    public MacroProps resolve() {
        if (this.resolvedMacros != null) {
            return this.resolvedMacros;
        }
        MacroProps macroProps = new MacroProps();
        for (NumberFormatterSettings numberFormatterSettings = this; numberFormatterSettings != null; numberFormatterSettings = numberFormatterSettings.parent) {
            switch (numberFormatterSettings.key) {
                case 0:
                    macroProps.fallback((MacroProps) numberFormatterSettings.value);
                    break;
                case 1:
                    if (macroProps.loc == null) {
                        macroProps.loc = (ULocale) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 2:
                    if (macroProps.notation == null) {
                        macroProps.notation = (Notation) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 3:
                    if (macroProps.unit == null) {
                        macroProps.unit = (MeasureUnit) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 4:
                    if (macroProps.precision == null) {
                        macroProps.precision = (Precision) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 5:
                    if (macroProps.roundingMode == null) {
                        macroProps.roundingMode = (RoundingMode) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 6:
                    if (macroProps.grouping == null) {
                        macroProps.grouping = numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 7:
                    if (macroProps.padder == null) {
                        macroProps.padder = (Padder) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 8:
                    if (macroProps.integerWidth == null) {
                        macroProps.integerWidth = (IntegerWidth) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 9:
                    if (macroProps.symbols == null) {
                        macroProps.symbols = numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 10:
                    if (macroProps.unitWidth == null) {
                        macroProps.unitWidth = (NumberFormatter.UnitWidth) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 11:
                    if (macroProps.sign == null) {
                        macroProps.sign = (NumberFormatter.SignDisplay) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 12:
                    if (macroProps.decimal == null) {
                        macroProps.decimal = (NumberFormatter.DecimalSeparatorDisplay) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 13:
                    if (macroProps.scale == null) {
                        macroProps.scale = (Scale) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 14:
                    if (macroProps.threshold == null) {
                        macroProps.threshold = (Long) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                case 15:
                    if (macroProps.perUnit == null) {
                        macroProps.perUnit = (MeasureUnit) numberFormatterSettings.value;
                        break;
                    } else {
                        break;
                    }
                default:
                    throw new AssertionError("Unknown key: " + numberFormatterSettings.key);
            }
        }
        this.resolvedMacros = macroProps;
        return macroProps;
    }

    public int hashCode() {
        return resolve().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof NumberFormatterSettings)) {
            return resolve().equals(((NumberFormatterSettings) obj).resolve());
        }
        return false;
    }
}
