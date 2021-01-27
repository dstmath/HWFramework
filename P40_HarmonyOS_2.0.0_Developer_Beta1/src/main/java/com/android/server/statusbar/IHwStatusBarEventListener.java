package com.android.server.statusbar;

import android.view.KeyEvent;

public interface IHwStatusBarEventListener {
    void onKeyEvent(KeyEvent keyEvent);

    void onLauncherShadowStateChange(boolean z);
}
