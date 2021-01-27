package com.android.server.input;

import android.hardware.input.IInputManager;
import com.android.server.wm.WindowState;

public abstract class AbsInputManagerService extends IInputManager.Stub {
    public void setDisplayWidthAndHeight(int width, int height) {
    }

    public void setCurFocusWindow(WindowState focus) {
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
    }

    public void setImmersiveMode(boolean mode) {
    }

    public void setInputEventStrategy(boolean isStartInputEventControl) {
    }
}
