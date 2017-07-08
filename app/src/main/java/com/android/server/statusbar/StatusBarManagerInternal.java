package com.android.server.statusbar;

import android.graphics.Rect;
import android.os.Bundle;
import com.android.server.notification.NotificationDelegate;

public interface StatusBarManagerInternal {
    void appTransitionCancelled();

    void appTransitionFinished();

    void appTransitionPending();

    void appTransitionStarting(long j, long j2);

    void buzzBeepBlinked();

    void cancelPreloadRecentApps();

    void dismissKeyboardShortcutsMenu();

    void hideRecentApps(boolean z, boolean z2);

    void notificationLightOff();

    void notificationLightPulse(int i, int i2, int i3);

    void onCameraLaunchGestureDetected(int i);

    void preloadRecentApps();

    void setCurrentUser(int i);

    void setNotificationDelegate(NotificationDelegate notificationDelegate);

    void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2, String str);

    void setWindowState(int i, int i2);

    void showAssistDisclosure();

    void showRecentApps(boolean z, boolean z2);

    void showScreenPinningRequest(int i);

    void showTvPictureInPictureMenu();

    void startAssist(Bundle bundle);

    void toggleKeyboardShortcutsMenu(int i);

    void toggleRecentApps();

    void toggleSplitScreen();

    void topAppWindowChanged(boolean z);
}
