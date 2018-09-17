package com.android.uiautomator.core;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.IActivityController.Stub;
import android.app.IActivityManager;
import android.app.UiAutomation;
import android.app.UiAutomationConnection;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.RemoteException;

public class UiAutomationShellWrapper {
    private static final String HANDLER_THREAD_NAME = "UiAutomatorHandlerThread";
    private final HandlerThread mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
    private UiAutomation mUiAutomation;

    private class DummyActivityController extends Stub {
        /* synthetic */ DummyActivityController(UiAutomationShellWrapper this$0, DummyActivityController -this1) {
            this();
        }

        private DummyActivityController() {
        }

        public boolean activityStarting(Intent intent, String pkg) throws RemoteException {
            return true;
        }

        public boolean activityResuming(String pkg) throws RemoteException {
            return true;
        }

        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
            return true;
        }

        public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
            return 0;
        }

        public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
            return 0;
        }

        public int systemNotResponding(String message) throws RemoteException {
            return 0;
        }
    }

    public void connect() {
        if (this.mHandlerThread.isAlive()) {
            throw new IllegalStateException("Already connected!");
        }
        this.mHandlerThread.start();
        this.mUiAutomation = new UiAutomation(this.mHandlerThread.getLooper(), new UiAutomationConnection());
        this.mUiAutomation.connect();
    }

    public void setRunAsMonkey(boolean isSet) {
        IActivityManager am = ActivityManager.getService();
        if (am == null) {
            throw new RuntimeException("Can't manage monkey status; is the system running?");
        } else if (isSet) {
            try {
                am.setActivityController(new DummyActivityController(this, null), true);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            am.setActivityController(null, true);
        }
    }

    public void disconnect() {
        if (this.mHandlerThread.isAlive()) {
            this.mUiAutomation.disconnect();
            this.mHandlerThread.quit();
            return;
        }
        throw new IllegalStateException("Already disconnected!");
    }

    public UiAutomation getUiAutomation() {
        return this.mUiAutomation;
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
}
