package com.android.server.input;

import android.content.Context;
import android.view.IInputFilter;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.PointerIcon;
import com.android.server.wm.WindowState;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.view.InputFilterEx;

public class InputManagerServiceBridge extends InputManagerService {
    private InputFilterEx filterEx;
    private InputManagerServiceEx mInputManagerServiceEx;

    public InputManagerServiceBridge(Context context) {
        super(context);
    }

    public void setInputManagerServiceEx(InputManagerServiceEx inputManagerServiceEx) {
        this.mInputManagerServiceEx = inputManagerServiceEx;
    }

    public void start() {
        InputManagerServiceBridge.super.start();
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.start();
        }
    }

    public void systemRunning() {
        InputManagerServiceBridge.super.systemRunning();
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.systemRunning();
        }
    }

    public Context getExternalContext() {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            return inputManagerServiceEx.getExternalContext();
        }
        return null;
    }

    public void setCustomPointerIcon(PointerIcon icon) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.setCustomPointerIcon(icon);
        } else {
            InputManagerServiceBridge.super.setCustomPointerIcon(icon);
        }
    }

    public void setPointerIconType(int iconId) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.setPointerIconType(iconId);
        } else {
            InputManagerServiceBridge.super.setPointerIconType(iconId);
        }
    }

    /* access modifiers changed from: protected */
    public void setPointerIconTypeEx(int iconId) {
        InputManagerServiceBridge.super.setPointerIconType(iconId);
    }

    /* access modifiers changed from: protected */
    public void setCustomPointerIconEx(PointerIcon icon) {
        InputManagerServiceBridge.super.setCustomPointerIcon(icon);
    }

    public void setInputFilter(IInputFilter filter) {
        InputManagerServiceBridge.super.setInputFilter(filter);
        if (this.mInputManagerServiceEx != null) {
            InputFilterEx filterEx2 = null;
            if (filter != null) {
                filterEx2 = new InputFilterEx();
                filterEx2.setIInputFilter(filter);
            }
            this.mInputManagerServiceEx.setInputFilter(filterEx2);
        }
    }

    /* access modifiers changed from: protected */
    public void deliverInputDevicesChanged(InputDevice[] oldInputDevices) {
        InputManagerServiceBridge.super.deliverInputDevicesChanged(oldInputDevices);
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.deliverInputDevicesChanged(oldInputDevices);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean filterInputEvent(InputEvent event, int policyFlags) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            return inputManagerServiceEx.filterInputEvent(event, policyFlags);
        }
        return InputManagerServiceBridge.super.filterInputEvent(event, policyFlags);
    }

    /* access modifiers changed from: protected */
    public boolean filterInputEventEx(InputEvent event, int policyFlags) {
        return InputManagerServiceBridge.super.filterInputEvent(event, policyFlags);
    }

    public void setDisplayWidthAndHeight(int width, int height) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.setDisplayWidthAndHeight(width, height);
        }
    }

    public void setCurFocusWindow(WindowState focus) {
        if (this.mInputManagerServiceEx != null) {
            WindowStateEx stateEx = null;
            if (focus != null) {
                stateEx = new WindowStateEx();
                stateEx.setWindowState(focus);
            }
            this.mInputManagerServiceEx.setCurFocusWindow(stateEx);
        }
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.setIsTopFullScreen(isTopFullScreen);
        }
    }

    public void setImmersiveMode(boolean isMode) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.setImmersiveMode(isMode);
        }
    }

    public void setInputEventStrategy(boolean isStartInputEventControl) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.setInputEventStrategy(isStartInputEventControl);
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.setCurrentUser(newUserId, currentProfileIds);
        }
    }

    public final void updateFingerprintSlideSwitchValue() {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.updateFingerprintSlideSwitchValue();
        }
    }

    public void onConfigurationChanged() {
        InputManagerServiceEx inputManagerServiceEx = this.mInputManagerServiceEx;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.onConfigurationChanged();
        }
    }
}
