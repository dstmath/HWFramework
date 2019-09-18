package android.icu.number;

import android.icu.impl.Utility;
import android.icu.impl.number.DecimalQuantity;
import android.icu.impl.number.DecimalQuantity_DualStorageBCD;
import android.icu.impl.number.MacroProps;
import android.icu.impl.number.MicroProps;
import android.icu.impl.number.NumberStringBuilder;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class LocalizedNumberFormatter extends NumberFormatterSettings<LocalizedNumberFormatter> {
    static final AtomicLongFieldUpdater<LocalizedNumberFormatter> callCount = AtomicLongFieldUpdater.newUpdater(LocalizedNumberFormatter.class, "callCountInternal");
    volatile long callCountInternal;
    volatile NumberFormatterImpl compiled;
    volatile LocalizedNumberFormatter savedWithUnit;

    LocalizedNumberFormatter(NumberFormatterSettings<?> parent, int key, Object value) {
        super(parent, key, value);
    }

    public FormattedNumber format(long input) {
        return format((DecimalQuantity) new DecimalQuantity_DualStorageBCD(input));
    }

    public FormattedNumber format(double input) {
        return format((DecimalQuantity) new DecimalQuantity_DualStorageBCD(input));
    }

    public FormattedNumber format(Number input) {
        return format((DecimalQuantity) new DecimalQuantity_DualStorageBCD(input));
    }

    public FormattedNumber format(Measure input) {
        MeasureUnit unit = input.getUnit();
        Number number = input.getNumber();
        if (Utility.equals(resolve().unit, unit)) {
            return format(number);
        }
        LocalizedNumberFormatter withUnit = this.savedWithUnit;
        if (withUnit == null || !Utility.equals(withUnit.resolve().unit, unit)) {
            withUnit = new LocalizedNumberFormatter(this, 3, unit);
            this.savedWithUnit = withUnit;
        }
        return withUnit.format(number);
    }

    @Deprecated
    public FormattedNumber format(DecimalQuantity fq) {
        MicroProps micros;
        MacroProps macros = resolve();
        long currentCount = callCount.incrementAndGet(this);
        NumberStringBuilder string = new NumberStringBuilder();
        if (currentCount == macros.threshold.longValue()) {
            this.compiled = NumberFormatterImpl.fromMacros(macros);
            micros = this.compiled.apply(fq, string);
        } else if (this.compiled != null) {
            micros = this.compiled.apply(fq, string);
        } else {
            micros = NumberFormatterImpl.applyStatic(macros, fq, string);
        }
        return new FormattedNumber(string, fq, micros);
    }

    /* access modifiers changed from: package-private */
    public LocalizedNumberFormatter create(int key, Object value) {
        return new LocalizedNumberFormatter(this, key, value);
    }
}
