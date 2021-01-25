package com.android.server.statusbar;

import android.view.KeyEvent;

public interface IHwStatusBarManagerServiceEx {
    void init(IHwStatusBarKeyEventListener iHwStatusBarKeyEventListener);

    void notifyKeyEvent(KeyEvent keyEvent);
}
