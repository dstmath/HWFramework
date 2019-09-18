package com.android.server.policy;

import android.os.Handler;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.WindowManagerInternal;

public interface IHwPhoneWindowManagerEx {
    void cancelWalletSwipe(Handler handler);

    boolean getFPAuthState();

    boolean getNaviBarFlag();

    boolean isIntersectCutoutForNotch(DisplayFrames displayFrames, boolean z);

    boolean isNeedWaitForAuthenticate();

    boolean isPowerFpForbidGotoSleep();

    void launchWalletSwipe(Handler handler, long j);

    void removeFreeFormStackIfNeed(WindowManagerInternal windowManagerInternal);

    void sendPowerKeyToFingerprint(int i, boolean z, boolean z2);

    void setFPAuthState(boolean z);

    void setIntersectCutoutForNotch(boolean z);

    void setNaviBarFlag(boolean z);

    void updateNavigationBar(boolean z);
}
