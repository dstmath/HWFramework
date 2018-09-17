package android.view.animation;

public abstract class BaseInterpolator implements Interpolator {
    private int mChangingConfiguration;

    public int getChangingConfiguration() {
        return this.mChangingConfiguration;
    }

    void setChangingConfiguration(int changingConfiguration) {
        this.mChangingConfiguration = changingConfiguration;
    }
}
