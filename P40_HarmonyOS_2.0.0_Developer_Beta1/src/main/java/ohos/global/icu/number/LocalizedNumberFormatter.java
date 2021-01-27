package ohos.global.icu.number;

import java.text.Format;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.impl.number.DecimalQuantity_DualStorageBCD;
import ohos.global.icu.impl.number.LocalizedNumberFormatterAsFormat;
import ohos.global.icu.impl.number.MacroProps;
import ohos.global.icu.util.Measure;
import ohos.global.icu.util.MeasureUnit;

public class LocalizedNumberFormatter extends NumberFormatterSettings<LocalizedNumberFormatter> {
    static final AtomicLongFieldUpdater<LocalizedNumberFormatter> callCount = AtomicLongFieldUpdater.newUpdater(LocalizedNumberFormatter.class, "callCountInternal");
    volatile long callCountInternal;
    volatile NumberFormatterImpl compiled;
    volatile LocalizedNumberFormatter savedWithUnit;

    LocalizedNumberFormatter(NumberFormatterSettings<?> numberFormatterSettings, int i, Object obj) {
        super(numberFormatterSettings, i, obj);
    }

    public FormattedNumber format(long j) {
        return format(new DecimalQuantity_DualStorageBCD(j));
    }

    public FormattedNumber format(double d) {
        return format(new DecimalQuantity_DualStorageBCD(d));
    }

    public FormattedNumber format(Number number) {
        return format(new DecimalQuantity_DualStorageBCD(number));
    }

    public FormattedNumber format(Measure measure) {
        MeasureUnit unit = measure.getUnit();
        Number number = measure.getNumber();
        if (Objects.equals(resolve().unit, unit)) {
            return format(number);
        }
        LocalizedNumberFormatter localizedNumberFormatter = this.savedWithUnit;
        if (localizedNumberFormatter == null || !Objects.equals(localizedNumberFormatter.resolve().unit, unit)) {
            localizedNumberFormatter = new LocalizedNumberFormatter(this, 3, unit);
            this.savedWithUnit = localizedNumberFormatter;
        }
        return localizedNumberFormatter.format(number);
    }

    public Format toFormat() {
        return new LocalizedNumberFormatterAsFormat(this, resolve().loc);
    }

    private FormattedNumber format(DecimalQuantity decimalQuantity) {
        FormattedStringBuilder formattedStringBuilder = new FormattedStringBuilder();
        formatImpl(decimalQuantity, formattedStringBuilder);
        return new FormattedNumber(formattedStringBuilder, decimalQuantity);
    }

    @Deprecated
    public void formatImpl(DecimalQuantity decimalQuantity, FormattedStringBuilder formattedStringBuilder) {
        if (computeCompiled()) {
            this.compiled.format(decimalQuantity, formattedStringBuilder);
        } else {
            NumberFormatterImpl.formatStatic(resolve(), decimalQuantity, formattedStringBuilder);
        }
    }

    @Deprecated
    public String getAffixImpl(boolean z, boolean z2) {
        int i;
        FormattedStringBuilder formattedStringBuilder = new FormattedStringBuilder();
        byte b = (byte) (z2 ? -1 : 1);
        StandardPlural standardPlural = StandardPlural.OTHER;
        if (computeCompiled()) {
            i = this.compiled.getPrefixSuffix(b, standardPlural, formattedStringBuilder);
        } else {
            i = NumberFormatterImpl.getPrefixSuffixStatic(resolve(), b, standardPlural, formattedStringBuilder);
        }
        if (z) {
            return formattedStringBuilder.subSequence(0, i).toString();
        }
        return formattedStringBuilder.subSequence(i, formattedStringBuilder.length()).toString();
    }

    private boolean computeCompiled() {
        MacroProps resolve = resolve();
        if (callCount.incrementAndGet(this) == resolve.threshold.longValue()) {
            this.compiled = new NumberFormatterImpl(resolve);
            return true;
        } else if (this.compiled != null) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.number.NumberFormatterSettings
    public LocalizedNumberFormatter create(int i, Object obj) {
        return new LocalizedNumberFormatter(this, i, obj);
    }
}
