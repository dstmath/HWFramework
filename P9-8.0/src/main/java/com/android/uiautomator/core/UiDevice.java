package com.android.uiautomator.core;

import android.app.UiAutomation.AccessibilityEventFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Deprecated
public class UiDevice {
    private static final long KEY_PRESS_EVENT_TIMEOUT = 1000;
    private static final String LOG_TAG = UiDevice.class.getSimpleName();
    private static UiDevice sDevice;
    private boolean mInWatcherContext = false;
    private UiAutomatorBridge mUiAutomationBridge;
    private final HashMap<String, UiWatcher> mWatchers = new HashMap();
    private final List<String> mWatchersTriggers = new ArrayList();

    private UiDevice() {
    }

    public void initialize(UiAutomatorBridge uiAutomatorBridge) {
        this.mUiAutomationBridge = uiAutomatorBridge;
    }

    boolean isInWatcherContext() {
        return this.mInWatcherContext;
    }

    UiAutomatorBridge getAutomatorBridge() {
        if (this.mUiAutomationBridge != null) {
            return this.mUiAutomationBridge;
        }
        throw new RuntimeException("UiDevice not initialized");
    }

    public void setCompressedLayoutHeirarchy(boolean compressed) {
        getAutomatorBridge().setCompressedLayoutHierarchy(compressed);
    }

    public static UiDevice getInstance() {
        if (sDevice == null) {
            sDevice = new UiDevice();
        }
        return sDevice;
    }

    public Point getDisplaySizeDp() {
        Tracer.trace(new Object[0]);
        Display display = getAutomatorBridge().getDefaultDisplay();
        Point p = new Point();
        display.getRealSize(p);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        float dpy = ((float) p.y) / metrics.density;
        p.x = Math.round(((float) p.x) / metrics.density);
        p.y = Math.round(dpy);
        return p;
    }

    public String getProductName() {
        Tracer.trace(new Object[0]);
        return Build.PRODUCT;
    }

    public String getLastTraversedText() {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getQueryController().getLastTraversedText();
    }

    public void clearLastTraversedText() {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getQueryController().clearLastTraversedText();
    }

    public boolean pressMenu() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKeyAndWaitForEvent(82, 0, 2048, KEY_PRESS_EVENT_TIMEOUT);
    }

    public boolean pressBack() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKeyAndWaitForEvent(4, 0, 2048, KEY_PRESS_EVENT_TIMEOUT);
    }

    public boolean pressHome() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKeyAndWaitForEvent(3, 0, 2048, KEY_PRESS_EVENT_TIMEOUT);
    }

    public boolean pressSearch() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(84);
    }

    public boolean pressDPadCenter() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(23);
    }

    public boolean pressDPadDown() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(20);
    }

    public boolean pressDPadUp() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(19);
    }

    public boolean pressDPadLeft() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(21);
    }

    public boolean pressDPadRight() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(22);
    }

    public boolean pressDelete() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(67);
    }

    public boolean pressEnter() {
        Tracer.trace(new Object[0]);
        return pressKeyCode(66);
    }

    public boolean pressKeyCode(int keyCode) {
        Tracer.trace(Integer.valueOf(keyCode));
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKey(keyCode, 0);
    }

    public boolean pressKeyCode(int keyCode, int metaState) {
        Tracer.trace(Integer.valueOf(keyCode), Integer.valueOf(metaState));
        waitForIdle();
        return getAutomatorBridge().getInteractionController().sendKey(keyCode, metaState);
    }

    public boolean pressRecentApps() throws RemoteException {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().toggleRecentApps();
    }

    public boolean openNotification() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().openNotification();
    }

    public boolean openQuickSettings() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getInteractionController().openQuickSettings();
    }

    public int getDisplayWidth() {
        Tracer.trace(new Object[0]);
        Display display = getAutomatorBridge().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        return p.x;
    }

    public int getDisplayHeight() {
        Tracer.trace(new Object[0]);
        Display display = getAutomatorBridge().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        return p.y;
    }

    public boolean click(int x, int y) {
        Tracer.trace(Integer.valueOf(x), Integer.valueOf(y));
        if (x >= getDisplayWidth() || y >= getDisplayHeight()) {
            return false;
        }
        return getAutomatorBridge().getInteractionController().clickNoSync(x, y);
    }

    public boolean swipe(int startX, int startY, int endX, int endY, int steps) {
        Tracer.trace(Integer.valueOf(startX), Integer.valueOf(startY), Integer.valueOf(endX), Integer.valueOf(endY), Integer.valueOf(steps));
        return getAutomatorBridge().getInteractionController().swipe(startX, startY, endX, endY, steps);
    }

    public boolean drag(int startX, int startY, int endX, int endY, int steps) {
        Tracer.trace(Integer.valueOf(startX), Integer.valueOf(startY), Integer.valueOf(endX), Integer.valueOf(endY), Integer.valueOf(steps));
        return getAutomatorBridge().getInteractionController().swipe(startX, startY, endX, endY, steps, true);
    }

    public boolean swipe(Point[] segments, int segmentSteps) {
        Tracer.trace(segments, Integer.valueOf(segmentSteps));
        return getAutomatorBridge().getInteractionController().swipe(segments, segmentSteps);
    }

    public void waitForIdle() {
        Tracer.trace(new Object[0]);
        waitForIdle(Configurator.getInstance().getWaitForIdleTimeout());
    }

    public void waitForIdle(long timeout) {
        Tracer.trace(Long.valueOf(timeout));
        getAutomatorBridge().waitForIdle(timeout);
    }

    @Deprecated
    public String getCurrentActivityName() {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getQueryController().getCurrentActivityName();
    }

    public String getCurrentPackageName() {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getQueryController().getCurrentPackageName();
    }

    public void registerWatcher(String name, UiWatcher watcher) {
        Tracer.trace(name, watcher);
        if (this.mInWatcherContext) {
            throw new IllegalStateException("Cannot register new watcher from within another");
        }
        this.mWatchers.put(name, watcher);
    }

    public void removeWatcher(String name) {
        Tracer.trace(name);
        if (this.mInWatcherContext) {
            throw new IllegalStateException("Cannot remove a watcher from within another");
        }
        this.mWatchers.remove(name);
    }

    public void runWatchers() {
        Tracer.trace(new Object[0]);
        if (!this.mInWatcherContext) {
            for (String watcherName : this.mWatchers.keySet()) {
                UiWatcher watcher = (UiWatcher) this.mWatchers.get(watcherName);
                if (watcher != null) {
                    try {
                        this.mInWatcherContext = true;
                        if (watcher.checkForCondition()) {
                            setWatcherTriggered(watcherName);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exceuting watcher: " + watcherName, e);
                    } catch (Throwable th) {
                        this.mInWatcherContext = false;
                    }
                    this.mInWatcherContext = false;
                }
            }
        }
    }

    public void resetWatcherTriggers() {
        Tracer.trace(new Object[0]);
        this.mWatchersTriggers.clear();
    }

    public boolean hasWatcherTriggered(String watcherName) {
        Tracer.trace(watcherName);
        return this.mWatchersTriggers.contains(watcherName);
    }

    public boolean hasAnyWatcherTriggered() {
        Tracer.trace(new Object[0]);
        if (this.mWatchersTriggers.size() > 0) {
            return true;
        }
        return false;
    }

    private void setWatcherTriggered(String watcherName) {
        Tracer.trace(watcherName);
        if (!hasWatcherTriggered(watcherName)) {
            this.mWatchersTriggers.add(watcherName);
        }
    }

    public boolean isNaturalOrientation() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        int ret = getAutomatorBridge().getRotation();
        if (ret == 0 || ret == 2) {
            return true;
        }
        return false;
    }

    public int getDisplayRotation() {
        Tracer.trace(new Object[0]);
        waitForIdle();
        return getAutomatorBridge().getRotation();
    }

    public void freezeRotation() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().freezeRotation();
    }

    public void unfreezeRotation() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().unfreezeRotation();
    }

    public void setOrientationLeft() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().setRotationLeft();
        waitForIdle();
    }

    public void setOrientationRight() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().setRotationRight();
        waitForIdle();
    }

    public void setOrientationNatural() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().setRotationNatural();
        waitForIdle();
    }

    public void wakeUp() throws RemoteException {
        Tracer.trace(new Object[0]);
        if (getAutomatorBridge().getInteractionController().wakeDevice()) {
            SystemClock.sleep(500);
        }
    }

    public boolean isScreenOn() throws RemoteException {
        Tracer.trace(new Object[0]);
        return getAutomatorBridge().getInteractionController().isScreenOn();
    }

    public void sleep() throws RemoteException {
        Tracer.trace(new Object[0]);
        getAutomatorBridge().getInteractionController().sleepDevice();
    }

    public void dumpWindowHierarchy(String fileName) {
        Tracer.trace(fileName);
        AccessibilityNodeInfo root = getAutomatorBridge().getQueryController().getAccessibilityRootNode();
        if (root != null) {
            Display display = getAutomatorBridge().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            AccessibilityNodeInfoDumper.dumpWindowToFile(root, new File(new File(Environment.getDataDirectory(), "local/tmp"), fileName), display.getRotation(), size.x, size.y);
        }
    }

    public boolean waitForWindowUpdate(final String packageName, long timeout) {
        Tracer.trace(packageName, Long.valueOf(timeout));
        if (packageName != null && !packageName.equals(getCurrentPackageName())) {
            return false;
        }
        try {
            getAutomatorBridge().executeCommandAndWaitForAccessibilityEvent(new Runnable() {
                public void run() {
                }
            }, new AccessibilityEventFilter() {
                public boolean accept(AccessibilityEvent t) {
                    if (t.getEventType() != 2048) {
                        return false;
                    }
                    return packageName != null ? packageName.equals(t.getPackageName()) : true;
                }
            }, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (Exception e2) {
            Log.e(LOG_TAG, "waitForWindowUpdate: general exception from bridge", e2);
            return false;
        }
    }

    public boolean takeScreenshot(File storePath) {
        Tracer.trace(storePath);
        return takeScreenshot(storePath, 1.0f, 90);
    }

    public boolean takeScreenshot(File storePath, float scale, int quality) {
        Tracer.trace(storePath, Float.valueOf(scale), Integer.valueOf(quality));
        return getAutomatorBridge().takeScreenshot(storePath, quality);
    }
}
