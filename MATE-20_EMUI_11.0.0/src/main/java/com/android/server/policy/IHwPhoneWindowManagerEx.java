package com.android.server.policy;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowState;

public interface IHwPhoneWindowManagerEx {
    void cancelWalletSwipe(Handler handler);

    boolean getFPAuthState();

    WindowState getTopFullscreenWindow();

    void handleHicarExtraKeys(KeyEvent keyEvent, WindowManagerPolicy.WindowState windowState, WindowManagerInternal windowManagerInternal);

    boolean isIntersectCutoutForNotch(DisplayFrames displayFrames, boolean z);

    boolean isNeedWaitForAuthenticate();

    boolean isPowerFpForbidGotoSleep();

    void launchWalletSwipe(Handler handler, long j);

    void putViewToCache(String str, View view, WindowManager.LayoutParams layoutParams);

    void removeFreeFormStackIfNeed(WindowManagerInternal windowManagerInternal);

    void sendPowerKeyToFingerprint(int i, boolean z, boolean z2);

    void setFPAuthState(boolean z);

    void setIntersectCutoutForNotch(boolean z);

    void showKeyguard(WindowManagerPolicy windowManagerPolicy);

    View tryAddViewFromCache(String str, IBinder iBinder, Configuration configuration);
}
