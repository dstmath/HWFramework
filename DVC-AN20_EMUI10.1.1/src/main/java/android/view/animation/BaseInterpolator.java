package android.view.animation;

public abstract class BaseInterpolator implements Interpolator {
    private int mChangingConfiguration;

    public int getChangingConfiguration() {
        return this.mChangingConfiguration;
    }

    /* access modifiers changed from: package-private */
    public void setChangingConfiguration(int changingConfiguration) {
        this.mChangingConfiguration = changingConfiguration;
    }
}
