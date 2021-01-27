package com.android.server.statusbar;

import android.view.KeyEvent;

public interface IHwStatusBarManagerServiceEx {
    void init(IHwStatusBarEventListener iHwStatusBarEventListener);

    void notifyKeyEvent(KeyEvent keyEvent);

    void updateIsEnableLauncherShadow(boolean z);
}
