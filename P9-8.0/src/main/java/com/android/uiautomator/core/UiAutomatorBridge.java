package com.android.uiautomator.core;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.app.UiAutomation.AccessibilityEventFilter;
import android.app.UiAutomation.OnAccessibilityEventListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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

    InteractionController getInteractionController() {
        return this.mInteractionController;
    }

    QueryController getQueryController() {
        return this.mQueryController;
    }

    public void setOnAccessibilityEventListener(OnAccessibilityEventListener listener) {
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

    public AccessibilityEvent executeCommandAndWaitForAccessibilityEvent(Runnable command, AccessibilityEventFilter filter, long timeoutMillis) throws TimeoutException {
        return this.mUiAutomation.executeAndWaitForEvent(command, filter, timeoutMillis);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0036 A:{SYNTHETIC, Splitter: B:19:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0042 A:{SYNTHETIC, Splitter: B:26:0x0042} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean takeScreenshot(File storePath, int quality) {
        IOException ioe;
        Throwable th;
        Bitmap screenshot = this.mUiAutomation.takeScreenshot();
        if (screenshot == null) {
            return false;
        }
        BufferedOutputStream bos = null;
        try {
            BufferedOutputStream bos2 = new BufferedOutputStream(new FileOutputStream(storePath));
            if (bos2 != null) {
                try {
                    screenshot.compress(CompressFormat.PNG, quality, bos2);
                    bos2.flush();
                } catch (IOException e) {
                    ioe = e;
                    bos = bos2;
                    try {
                        Log.e(LOG_TAG, "failed to save screen shot to file", ioe);
                        if (bos != null) {
                        }
                        screenshot.recycle();
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bos != null) {
                            try {
                                bos.close();
                            } catch (IOException e2) {
                            }
                        }
                        screenshot.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bos = bos2;
                    if (bos != null) {
                    }
                    screenshot.recycle();
                    throw th;
                }
            }
            if (bos2 != null) {
                try {
                    bos2.close();
                } catch (IOException e3) {
                }
            }
            screenshot.recycle();
            return true;
        } catch (IOException e4) {
            ioe = e4;
            Log.e(LOG_TAG, "failed to save screen shot to file", ioe);
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e5) {
                }
            }
            screenshot.recycle();
            return false;
        }
    }

    public boolean performGlobalAction(int action) {
        return this.mUiAutomation.performGlobalAction(action);
    }
}
