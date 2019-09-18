package com.android.uiautomator.core;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class UiAutomatorBridge {
    private static final String LOG_TAG = UiAutomatorBridge.class.getSimpleName();
    private static final long QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE = 500;
    private static final long TOTAL_TIME_TO_WAIT_FOR_IDLE_STATE = 10000;
    private final InteractionController mInteractionController = new InteractionController(this);
    private final QueryController mQueryController = new QueryController(this);
    private final UiAutomation mUiAutomation;

    public abstract Display getDefaultDisplay();

    public abstract int getRotation();

    public abstract long getSystemLongPressTime();

    public abstract boolean isScreenOn();

    UiAutomatorBridge(UiAutomation uiAutomation) {
        this.mUiAutomation = uiAutomation;
    }

    /* access modifiers changed from: package-private */
    public InteractionController getInteractionController() {
        return this.mInteractionController;
    }

    /* access modifiers changed from: package-private */
    public QueryController getQueryController() {
        return this.mQueryController;
    }

    public void setOnAccessibilityEventListener(UiAutomation.OnAccessibilityEventListener listener) {
        this.mUiAutomation.setOnAccessibilityEventListener(listener);
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        return this.mUiAutomation.getRootInActiveWindow();
    }

    public boolean injectInputEvent(InputEvent event, boolean sync) {
        return this.mUiAutomation.injectInputEvent(event, sync);
    }

    public boolean setRotation(int rotation) {
        return this.mUiAutomation.setRotation(rotation);
    }

    public void setCompressedLayoutHierarchy(boolean compressed) {
        AccessibilityServiceInfo info = this.mUiAutomation.getServiceInfo();
        if (compressed) {
            info.flags &= -3;
        } else {
            info.flags |= 2;
        }
        this.mUiAutomation.setServiceInfo(info);
    }

    public void waitForIdle() {
        waitForIdle(TOTAL_TIME_TO_WAIT_FOR_IDLE_STATE);
    }

    public void waitForIdle(long timeout) {
        try {
            this.mUiAutomation.waitForIdle(QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE, timeout);
        } catch (TimeoutException te) {
            Log.w(LOG_TAG, "Could not detect idle state.", te);
        }
    }

    public AccessibilityEvent executeCommandAndWaitForAccessibilityEvent(Runnable command, UiAutomation.AccessibilityEventFilter filter, long timeoutMillis) throws TimeoutException {
        return this.mUiAutomation.executeAndWaitForEvent(command, filter, timeoutMillis);
    }

    public boolean takeScreenshot(File storePath, int quality) {
        Bitmap screenshot = this.mUiAutomation.takeScreenshot();
        if (screenshot == null) {
            return false;
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(storePath));
            screenshot.compress(Bitmap.CompressFormat.PNG, quality, bos);
            bos.flush();
            try {
                bos.close();
            } catch (IOException e) {
            }
            screenshot.recycle();
            return true;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "failed to save screen shot to file", ioe);
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e2) {
                }
            }
            screenshot.recycle();
            return false;
        } catch (Throwable th) {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e3) {
                }
            }
            screenshot.recycle();
            throw th;
        }
    }

    public boolean performGlobalAction(int action) {
        return this.mUiAutomation.performGlobalAction(action);
    }
}
