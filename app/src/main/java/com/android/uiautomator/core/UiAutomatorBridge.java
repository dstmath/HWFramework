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
    private static final String LOG_TAG = null;
    private static final long QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE = 500;
    private static final long TOTAL_TIME_TO_WAIT_FOR_IDLE_STATE = 10000;
    private final InteractionController mInteractionController;
    private final QueryController mQueryController;
    private final UiAutomation mUiAutomation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.uiautomator.core.UiAutomatorBridge.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.uiautomator.core.UiAutomatorBridge.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.uiautomator.core.UiAutomatorBridge.<clinit>():void");
    }

    public abstract Display getDefaultDisplay();

    public abstract int getRotation();

    public abstract long getSystemLongPressTime();

    public abstract boolean isScreenOn();

    UiAutomatorBridge(UiAutomation uiAutomation) {
        this.mUiAutomation = uiAutomation;
        this.mInteractionController = new InteractionController(this);
        this.mQueryController = new QueryController(this);
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
                            try {
                                bos.close();
                            } catch (IOException e2) {
                            }
                        }
                        screenshot.recycle();
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bos != null) {
                            try {
                                bos.close();
                            } catch (IOException e3) {
                            }
                        }
                        screenshot.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bos = bos2;
                    if (bos != null) {
                        bos.close();
                    }
                    screenshot.recycle();
                    throw th;
                }
            }
            if (bos2 != null) {
                try {
                    bos2.close();
                } catch (IOException e4) {
                }
            }
            screenshot.recycle();
            return true;
        } catch (IOException e5) {
            ioe = e5;
            Log.e(LOG_TAG, "failed to save screen shot to file", ioe);
            if (bos != null) {
                bos.close();
            }
            screenshot.recycle();
            return false;
        }
    }

    public boolean performGlobalAction(int action) {
        return this.mUiAutomation.performGlobalAction(action);
    }
}
