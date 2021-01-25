package com.android.server.statusbar;

import android.graphics.Rect;
import android.os.Bundle;
import com.android.server.notification.NotificationDelegate;

public interface StatusBarManagerInternal {
    void appTransitionCancelled(int i);

    void appTransitionFinished(int i);

    void appTransitionPending(int i);

    void appTransitionStarting(int i, long j, long j2);

    void cancelPreloadRecentApps();

    void dismissKeyboardShortcutsMenu();

    void hideRecentApps(boolean z, boolean z2);

    void onCameraLaunchGestureDetected(int i);

    void onDisplayReady(int i);

    void onProposedRotationChanged(int i, boolean z);

    void onRecentsAnimationStateChanged(boolean z);

    void preloadRecentApps();

    void setCurrentUser(int i);

    void setNotificationDelegate(NotificationDelegate notificationDelegate);

    void setSystemUiVisibility(int i, int i2, int i3, int i4, int i5, Rect rect, Rect rect2, boolean z, String str);

    void setTopAppHidesStatusBar(boolean z);

    void setWindowState(int i, int i2, int i3);

    void showAssistDisclosure();

    void showChargingAnimation(int i);

    void showPictureInPictureMenu();

    void showRecentApps(boolean z);

    void showScreenPinningRequest(int i);

    boolean showShutdownUi(boolean z, String str);

    void startAssist(Bundle bundle);

    void toggleKeyboardShortcutsMenu(int i);

    void toggleRecentApps();

    void toggleSplitScreen();

    void topAppWindowChanged(int i, boolean z);
}
