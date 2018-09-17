package android.content.res;

import android.content.res.Resources.Theme;

public abstract class ComplexColor {
    private int mChangingConfigurations;

    public abstract boolean canApplyTheme();

    public abstract ConstantState<ComplexColor> getConstantState();

    public abstract int getDefaultColor();

    public abstract ComplexColor obtainForTheme(Theme theme);

    public boolean isStateful() {
        return false;
    }

    final void setBaseChangingConfigurations(int changingConfigurations) {
        this.mChangingConfigurations = changingConfigurations;
    }

    public int getChangingConfigurations() {
        return this.mChangingConfigurations;
    }
}
