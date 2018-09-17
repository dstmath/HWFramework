package android.icu.impl.duration;

import android.icu.impl.duration.impl.PeriodFormatterData;
import android.icu.impl.duration.impl.PeriodFormatterDataService;
import java.util.Locale;

public class BasicPeriodFormatterFactory implements PeriodFormatterFactory {
    private Customizations customizations = new Customizations();
    private boolean customizationsInUse;
    private PeriodFormatterData data;
    private final PeriodFormatterDataService ds;
    private String localeName = Locale.getDefault().toString();

    static class Customizations {
        byte countVariant = (byte) 0;
        boolean displayDirection = true;
        boolean displayLimit = true;
        byte separatorVariant = (byte) 2;
        byte unitVariant = (byte) 0;

        Customizations() {
        }

        public Customizations copy() {
            Customizations result = new Customizations();
            result.displayLimit = this.displayLimit;
            result.displayDirection = this.displayDirection;
            result.separatorVariant = this.separatorVariant;
            result.unitVariant = this.unitVariant;
            result.countVariant = this.countVariant;
            return result;
        }
    }

    BasicPeriodFormatterFactory(PeriodFormatterDataService ds) {
        this.ds = ds;
    }

    public static BasicPeriodFormatterFactory getDefault() {
        return (BasicPeriodFormatterFactory) BasicPeriodFormatterService.getInstance().newPeriodFormatterFactory();
    }

    public PeriodFormatterFactory setLocale(String localeName) {
        this.data = null;
        this.localeName = localeName;
        return this;
    }

    public PeriodFormatterFactory setDisplayLimit(boolean display) {
        updateCustomizations().displayLimit = display;
        return this;
    }

    public boolean getDisplayLimit() {
        return this.customizations.displayLimit;
    }

    public PeriodFormatterFactory setDisplayPastFuture(boolean display) {
        updateCustomizations().displayDirection = display;
        return this;
    }

    public boolean getDisplayPastFuture() {
        return this.customizations.displayDirection;
    }

    public PeriodFormatterFactory setSeparatorVariant(int variant) {
        updateCustomizations().separatorVariant = (byte) variant;
        return this;
    }

    public int getSeparatorVariant() {
        return this.customizations.separatorVariant;
    }

    public PeriodFormatterFactory setUnitVariant(int variant) {
        updateCustomizations().unitVariant = (byte) variant;
        return this;
    }

    public int getUnitVariant() {
        return this.customizations.unitVariant;
    }

    public PeriodFormatterFactory setCountVariant(int variant) {
        updateCustomizations().countVariant = (byte) variant;
        return this;
    }

    public int getCountVariant() {
        return this.customizations.countVariant;
    }

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

    PeriodFormatterData getData() {
        if (this.data == null) {
            this.data = this.ds.get(this.localeName);
        }
        return this.data;
    }

    PeriodFormatterData getData(String locName) {
        return this.ds.get(locName);
    }
}
