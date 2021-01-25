package android.view;

public interface IHwBlurWindowManager {
    boolean getBlurCacheEnabled();

    boolean getBlurEnabled();

    int getBlurMode();

    float getBlurProgress();

    void performDrawBlurLayer(ViewRootImpl viewRootImpl, View view);

    boolean setBlurCacheEnabled(boolean z);

    boolean setBlurEnabled(boolean z);

    boolean setBlurMode(int i);

    boolean setBlurProgress(float f);

    void updateWindowBlurDrawOp(ViewRootImpl viewRootImpl, boolean z);

    void updateWindowBlurParams(ViewRootImpl viewRootImpl, boolean z);
}
