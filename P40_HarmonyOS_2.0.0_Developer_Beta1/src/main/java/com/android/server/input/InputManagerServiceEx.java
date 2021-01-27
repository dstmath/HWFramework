package com.android.server.input;

import android.content.Context;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.PointerIcon;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.view.InputFilterEx;

public class InputManagerServiceEx {
    protected static final boolean DEBUG_HWFLOW = InputManagerService.DEBUG_HWFLOW;
    protected static final String TAG = "InputManager";
    private InputManagerServiceBridge mBridge;

    public InputManagerServiceEx(Context context) {
        this.mBridge = new InputManagerServiceBridge(context);
        this.mBridge.setInputManagerServiceEx(this);
    }

    public InputManagerService getInputManagerService() {
        return this.mBridge;
    }

    public void start() {
    }

    public void systemRunning() {
    }

    public void setDisplayWidthAndHeight(int width, int height) {
    }

    public void setCurFocusWindow(WindowStateEx focus) {
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
    }

    public void setImmersiveMode(boolean isMode) {
    }

    public void setInputEventStrategy(boolean isStartInputEventControl) {
    }

    /* access modifiers changed from: protected */
    public Context getExternalContext() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void deliverInputDevicesChanged(InputDevice[] oldInputDevices) {
    }

    /* access modifiers changed from: protected */
    public boolean filterInputEvent(InputEvent event, int policyFlags) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean filterInputEventEx(InputEvent event, int policyFlags) {
        return this.mBridge.filterInputEventEx(event, policyFlags);
    }

    public void setInputFilter(InputFilterEx filterEx) {
    }

    public void setPointerIconType(int iconId) {
    }

    public void setCustomPointerIcon(PointerIcon icon) {
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
    }

    public void updateFingerprintSlideSwitchValue() {
    }

    public void setIawareGameMode(int gameMode) {
    }

    public void responseTouchEvent(boolean isNeedResponseStatus) {
    }

    public void setIawareGameModeAccurate(int gameMode) {
    }

    public void onConfigurationChanged() {
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        return this.mBridge.injectInputEvent(event, mode);
    }

    public void setPointerIconTypeEx(int iconId) {
        this.mBridge.setPointerIconTypeEx(iconId);
    }

    public void setCustomPointerIconEx(PointerIcon icon) {
        this.mBridge.setCustomPointerIconEx(icon);
    }

    /* access modifiers changed from: protected */
    public boolean injectInputEventInternal(InputEvent event, int mode) {
        return this.mBridge.injectInputEventInternal(event, mode);
    }

    /* access modifiers changed from: protected */
    public boolean injectInputEventInternal(InputEvent event, int mode, int appendPolicyFlag) {
        return this.mBridge.injectInputEventInternal(event, mode, appendPolicyFlag);
    }

    public Object getInputFilterLock() {
        return this.mBridge.mInputFilterLock;
    }

    public long getPtr() {
        return this.mBridge.mPtr;
    }

    public InputFilterEx getInputFilter() {
        if (this.mBridge.mInputFilter == null) {
            return null;
        }
        InputFilterEx filterEx = new InputFilterEx();
        filterEx.setIInputFilter(this.mBridge.mInputFilter);
        return filterEx;
    }

    /* access modifiers changed from: protected */
    public void nativeSetInputFilterEnabled(long ptr, boolean isEnable) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeSetInputFilterEnabled(ptr, isEnable);
    }

    /* access modifiers changed from: protected */
    public void nativeResponseTouchEvent(long ptr, boolean isStatus) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeResponseTouchEvent(ptr, isStatus);
    }

    /* access modifiers changed from: protected */
    public void nativeSetInputScaleConfig(long ptr, float xScale, float yScale, int scaleSide, int scaleType) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeSetInputScaleConfig(ptr, xScale, yScale, scaleSide, scaleType);
    }

    /* access modifiers changed from: protected */
    public void nativeSetKeyguardState(long ptr, boolean isShowing) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeSetKeyguardState(ptr, isShowing);
    }

    /* access modifiers changed from: protected */
    public void nativeSetMirrorLinkInputStatus(long ptr, boolean isStatus) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeSetMirrorLinkInputStatus(ptr, isStatus);
    }

    /* access modifiers changed from: protected */
    public void nativeReloadPointerIcons(long ptr, Context context) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeReloadPointerIcons(ptr, context);
    }

    /* access modifiers changed from: protected */
    public void nativeSetIawareGameMode(long ptr, int gameMode) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeSetIawareGameMode(ptr, gameMode);
    }

    /* access modifiers changed from: protected */
    public void nativeSetIawareGameModeAccurate(long ptr, int gameMode) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeSetIawareGameModeAccurate(ptr, gameMode);
    }

    /* access modifiers changed from: protected */
    public void nativeSetDispatchDisplayInfo(long ptr, int displayId, int displayWidth, int displayHeight, int rotation) {
        InputManagerServiceBridge inputManagerServiceBridge = this.mBridge;
        InputManagerServiceBridge.nativeSetDispatchDisplayInfo(ptr, displayId, displayWidth, displayHeight, rotation);
    }

    public class DefaultHwInputManagerLocalService {
        public DefaultHwInputManagerLocalService() {
        }

        public boolean injectInputEvent(InputEvent event, int mode) {
            return false;
        }

        public boolean injectInputEvent(InputEvent event, int mode, int appendPolicyFlag) {
            return false;
        }

        public void setExternalDisplayContext(Context context) {
        }

        public void setPointerIconTypeAndKeep(int iconId, boolean isKeep) {
        }

        public void setCustomPointerIconAndKeep(PointerIcon icon, boolean isKeep) {
        }

        public void setMirrorLinkInputStatus(boolean isMirrorLinkStatus) {
        }

        public void setKeyguardState(boolean isShowing) {
        }

        public void setInputScaleConfig(float xScale, float yScale, int scaleSide, int scaleType) {
        }
    }
}
