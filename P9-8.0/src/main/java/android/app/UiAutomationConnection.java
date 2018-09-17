package android.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.app.IUiAutomationConnection.Stub;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.graphics.Bitmap;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.IWindowManager;
import android.view.InputEvent;
import android.view.SurfaceControl;
import android.view.WindowAnimationFrameStats;
import android.view.WindowContentFrameStats;
import android.view.accessibility.IAccessibilityManager;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import libcore.io.IoUtils;

public final class UiAutomationConnection extends Stub {
    private static final int INITIAL_FROZEN_ROTATION_UNSPECIFIED = -1;
    private final IAccessibilityManager mAccessibilityManager = IAccessibilityManager.Stub.asInterface(ServiceManager.getService(Context.ACCESSIBILITY_SERVICE));
    private IAccessibilityServiceClient mClient;
    private int mInitialFrozenRotation = -1;
    private boolean mIsShutdown;
    private final Object mLock = new Object();
    private int mOwningUid;
    private final IPackageManager mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    private final Binder mToken = new Binder();
    private final IWindowManager mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));

    public void connect(IAccessibilityServiceClient client, int flags) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null!");
        }
        synchronized (this.mLock) {
            throwIfShutdownLocked();
            if (isConnectedLocked()) {
                throw new IllegalStateException("Already connected.");
            }
            this.mOwningUid = Binder.getCallingUid();
            registerUiTestAutomationServiceLocked(client, flags);
            storeRotationStateLocked();
        }
    }

    public void disconnect() {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            if (isConnectedLocked()) {
                this.mOwningUid = -1;
                unregisterUiTestAutomationServiceLocked();
                restoreRotationStateLocked();
            } else {
                throw new IllegalStateException("Already disconnected.");
            }
        }
    }

    public boolean injectInputEvent(InputEvent event, boolean sync) {
        int mode;
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        if (sync) {
            mode = 2;
        } else {
            mode = 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            boolean injectInputEvent = InputManager.getInstance().injectInputEvent(event, mode);
            return injectInputEvent;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean setRotation(int rotation) {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        long identity = Binder.clearCallingIdentity();
        if (rotation == -2) {
            try {
                this.mWindowManager.thawRotation();
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(identity);
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            this.mWindowManager.freezeRotation(rotation);
        }
        Binder.restoreCallingIdentity(identity);
        return true;
    }

    public Bitmap takeScreenshot(int width, int height) {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Bitmap screenshot = SurfaceControl.screenshot(width, height);
            return screenshot;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean clearWindowContentFrameStats(int windowId) throws RemoteException {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        int callingUserId = UserHandle.getCallingUserId();
        long identity = Binder.clearCallingIdentity();
        try {
            IBinder token = this.mAccessibilityManager.getWindowToken(windowId, callingUserId);
            if (token == null) {
                return false;
            }
            boolean clearWindowContentFrameStats = this.mWindowManager.clearWindowContentFrameStats(token);
            Binder.restoreCallingIdentity(identity);
            return clearWindowContentFrameStats;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public WindowContentFrameStats getWindowContentFrameStats(int windowId) throws RemoteException {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        int callingUserId = UserHandle.getCallingUserId();
        long identity = Binder.clearCallingIdentity();
        try {
            IBinder token = this.mAccessibilityManager.getWindowToken(windowId, callingUserId);
            if (token == null) {
                return null;
            }
            WindowContentFrameStats windowContentFrameStats = this.mWindowManager.getWindowContentFrameStats(token);
            Binder.restoreCallingIdentity(identity);
            return windowContentFrameStats;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void clearWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        long identity = Binder.clearCallingIdentity();
        try {
            SurfaceControl.clearAnimationFrameStats();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public WindowAnimationFrameStats getWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        long identity = Binder.clearCallingIdentity();
        try {
            WindowAnimationFrameStats stats = new WindowAnimationFrameStats();
            SurfaceControl.getAnimationFrameStats(stats);
            return stats;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void grantRuntimePermission(String packageName, String permission, int userId) throws RemoteException {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        long identity = Binder.clearCallingIdentity();
        try {
            this.mPackageManager.grantRuntimePermission(packageName, permission, userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void revokeRuntimePermission(String packageName, String permission, int userId) throws RemoteException {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        long identity = Binder.clearCallingIdentity();
        try {
            this.mPackageManager.revokeRuntimePermission(packageName, permission, userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void executeShellCommand(final String command, final ParcelFileDescriptor sink) throws RemoteException {
        synchronized (this.mLock) {
            throwIfCalledByNotTrustedUidLocked();
            throwIfShutdownLocked();
            throwIfNotConnectedLocked();
        }
        new Thread() {
            public void run() {
                IOException ioe;
                Throwable th;
                AutoCloseable out = null;
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(command);
                    InputStream in = process.getInputStream();
                    OutputStream out2 = new FileOutputStream(sink.getFileDescriptor());
                    try {
                        byte[] buffer = new byte[8192];
                        while (true) {
                            int readByteCount = in.read(buffer);
                            if (readByteCount < 0) {
                                if (process != null) {
                                    process.destroy();
                                }
                                IoUtils.closeQuietly(out2);
                                IoUtils.closeQuietly(sink);
                                return;
                            }
                            out2.write(buffer, 0, readByteCount);
                        }
                    } catch (IOException e) {
                        ioe = e;
                        out = out2;
                    } catch (Throwable th2) {
                        th = th2;
                        Object out3 = out2;
                    }
                } catch (IOException e2) {
                    ioe = e2;
                    try {
                        throw new RuntimeException("Error running shell command", ioe);
                    } catch (Throwable th3) {
                        th = th3;
                        if (process != null) {
                            process.destroy();
                        }
                        IoUtils.closeQuietly(out);
                        IoUtils.closeQuietly(sink);
                        throw th;
                    }
                }
            }
        }.start();
    }

    public void shutdown() {
        synchronized (this.mLock) {
            if (isConnectedLocked()) {
                throwIfCalledByNotTrustedUidLocked();
            }
            throwIfShutdownLocked();
            this.mIsShutdown = true;
            if (isConnectedLocked()) {
                disconnect();
            }
        }
    }

    private void registerUiTestAutomationServiceLocked(IAccessibilityServiceClient client, int flags) {
        IAccessibilityManager manager = IAccessibilityManager.Stub.asInterface(ServiceManager.getService(Context.ACCESSIBILITY_SERVICE));
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = -1;
        info.feedbackType = 16;
        info.flags |= 65554;
        info.setCapabilities(15);
        try {
            manager.registerUiTestAutomationService(this.mToken, client, info, flags);
            this.mClient = client;
        } catch (RemoteException re) {
            throw new IllegalStateException("Error while registering UiTestAutomationService.", re);
        }
    }

    private void unregisterUiTestAutomationServiceLocked() {
        try {
            IAccessibilityManager.Stub.asInterface(ServiceManager.getService(Context.ACCESSIBILITY_SERVICE)).unregisterUiTestAutomationService(this.mClient);
            this.mClient = null;
        } catch (RemoteException re) {
            throw new IllegalStateException("Error while unregistering UiTestAutomationService", re);
        }
    }

    private void storeRotationStateLocked() {
        try {
            if (this.mWindowManager.isRotationFrozen()) {
                this.mInitialFrozenRotation = this.mWindowManager.getDefaultDisplayRotation();
            }
        } catch (RemoteException e) {
        }
    }

    private void restoreRotationStateLocked() {
        try {
            if (this.mInitialFrozenRotation != -1) {
                this.mWindowManager.freezeRotation(this.mInitialFrozenRotation);
            } else {
                this.mWindowManager.thawRotation();
            }
        } catch (RemoteException e) {
        }
    }

    private boolean isConnectedLocked() {
        return this.mClient != null;
    }

    private void throwIfShutdownLocked() {
        if (this.mIsShutdown) {
            throw new IllegalStateException("Connection shutdown!");
        }
    }

    private void throwIfNotConnectedLocked() {
        if (!isConnectedLocked()) {
            throw new IllegalStateException("Not connected!");
        }
    }

    private void throwIfCalledByNotTrustedUidLocked() {
        int callingUid = Binder.getCallingUid();
        if (callingUid != this.mOwningUid && this.mOwningUid != 1000 && callingUid != 0) {
            throw new SecurityException("Calling from not trusted UID!");
        }
    }
}
