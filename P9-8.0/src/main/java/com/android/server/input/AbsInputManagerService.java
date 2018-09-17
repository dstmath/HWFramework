package com.android.server.input;

import android.hardware.input.IInputManager.Stub;
import com.android.server.wm.WindowState;

public abstract class AbsInputManagerService extends Stub {
    public void setDisplayWidthAndHeight(int width, int height) {
    }

    public void setCurFocusWindow(WindowState focus) {
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
    }

    public void setImmersiveMode(boolean mode) {
    }

    public void notifyNativeEvent(int eventType, int eventValue, int keyAction, int pid, int uid) {
    }
}
