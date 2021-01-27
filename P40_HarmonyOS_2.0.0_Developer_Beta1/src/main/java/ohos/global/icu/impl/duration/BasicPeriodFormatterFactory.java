package ohos.global.icu.impl.duration;

import java.util.Locale;
import ohos.global.icu.impl.duration.impl.PeriodFormatterData;
import ohos.global.icu.impl.duration.impl.PeriodFormatterDataService;

public class BasicPeriodFormatterFactory implements PeriodFormatterFactory {
    private Customizations customizations = new Customizations();
    private boolean customizationsInUse;
    private PeriodFormatterData data;
    private final PeriodFormatterDataService ds;
    private String localeName = Locale.getDefault().toString();

    BasicPeriodFormatterFactory(PeriodFormatterDataService periodFormatterDataService) {
        this.ds = periodFormatterDataService;
    }

    public static BasicPeriodFormatterFactory getDefault() {
        return (BasicPeriodFormatterFactory) BasicPeriodFormatterService.getInstance().newPeriodFormatterFactory();
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterFactory
    public PeriodFormatterFactory setLocale(String str) {
        this.data = null;
        this.localeName = str;
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterFactory
    public PeriodFormatterFactory setDisplayLimit(boolean z) {
        updateCustomizations().displayLimit = z;
        return this;
    }

    public boolean getDisplayLimit() {
        return this.customizations.displayLimit;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterFactory
    public PeriodFormatterFactory setDisplayPastFuture(boolean z) {
        updateCustomizations().displayDirection = z;
        return this;
    }

    public boolean getDisplayPastFuture() {
        return this.customizations.displayDirection;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterFactory
    public PeriodFormatterFactory setSeparatorVariant(int i) {
        updateCustomizations().separatorVariant = (byte) i;
        return this;
    }

    public int getSeparatorVariant() {
        return this.customizations.separatorVariant;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterFactory
    public PeriodFormatterFactory setUnitVariant(int i) {
        updateCustomizations().unitVariant = (byte) i;
        return this;
    }

    public int getUnitVariant() {
        return this.customizations.unitVariant;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterFactory
    public PeriodFormatterFactory setCountVariant(int i) {
        updateCustomizations().countVariant = (byte) i;
        return this;
    }

    public int getCountVariant() {
        return this.customizations.countVariant;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterFactory
    public PeriodFormatter getFormatter() {
        this.customizationsInUse = true;
        return new BasicPeriodFormatter(this, this.localeName, getData(), this.customizations);
    }

    private Customizations updateCustomizations() {
        if (this.customizationsInUse) {
            this.customizations = this.customizations.copy();
            this.customizationsInUse = false;
        }
        return this.customizations;
    }

    /* access modifiers changed from: package-private */
    public PeriodFormatterData getData() {
        if (this.data == null) {
            this.data = this.ds.get(this.localeName);
        }
        return this.data;
    }

    /* access modifiers changed from: package-private */
    public PeriodFormatterData getData(String str) {
        return this.ds.get(str);
    }

    /* access modifiers changed from: package-private */
    public static class Customizations {
        byte countVariant = 0;
        boolean displayDirection = true;
        boolean displayLimit = true;
        byte separatorVariant = 2;
        byte unitVariant = 0;

        Customizations() {
        }

        public Customizations copy() {
            Customizations customizations = new Customizations();
            customizations.displayLimit = this.displayLimit;
            customizations.displayDirection = this.displayDirection;
            customizations.separatorVariant = this.separatorVariant;
            customizations.unitVariant = this.unitVariant;
            customizations.countVariant = this.countVariant;
            return customizations;
        }
    }
}
