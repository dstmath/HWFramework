package android.view;

import android.content.Context;

public interface IHwNsdImpl {
    boolean StopSdrForSpecial(String str, int i);

    void adaptPowerSave(Context context, MotionEvent motionEvent);

    boolean checkAdBlock(View view, String str);

    boolean checkIs2DSDRCase(Context context, ViewRootImpl viewRootImpl);

    float computeSDRRatio(Context context, View view, View view2, float f, float f2, int i);

    int computeSDRRatioBase(Context context, View view, View view2);

    void createEventAnalyzed();

    boolean doProcessDrawSLB(long j, boolean z, boolean z2);

    String[] getCustAppList(int i);

    int getCustScreenDimDurationLocked(int i);

    void initAPS(Context context, int i, int i2);

    boolean isAPSReady();

    boolean isGameProcess(String str);

    boolean isSLBSwitchOn(String str);

    boolean isSupportAPSEventAnalysis();

    boolean isSupportAps();

    void powerCtroll();

    void setAPSOnPause();

    void setContext(Context context);

    void setFrameScheduledSLB();

    void setPlayingVideoSLB(boolean z);
}
