package android.app;

import android.accessibilityservice.AccessibilityService.Callbacks;
import android.accessibilityservice.AccessibilityService.IAccessibilityServiceClientWrapper;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.accessibilityservice.IAccessibilityServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Region;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.display.DisplayManagerGlobal;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.WindowAnimationFrameStats;
import android.view.WindowContentFrameStats;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import libcore.io.IoUtils;

public final class UiAutomation {
    private static final int CONNECTION_ID_UNDEFINED = -1;
    private static final long CONNECT_TIMEOUT_MILLIS = 5000;
    private static final boolean DEBUG = false;
    public static final int FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES = 1;
    private static final String LOG_TAG = UiAutomation.class.getSimpleName();
    public static final int ROTATION_FREEZE_0 = 0;
    public static final int ROTATION_FREEZE_180 = 2;
    public static final int ROTATION_FREEZE_270 = 3;
    public static final int ROTATION_FREEZE_90 = 1;
    public static final int ROTATION_FREEZE_CURRENT = -1;
    public static final int ROTATION_UNFREEZE = -2;
    private final IAccessibilityServiceClient mClient;
    private int mConnectionId = -1;
    private final ArrayList<AccessibilityEvent> mEventQueue = new ArrayList();
    private int mFlags;
    private boolean mIsConnecting;
    private boolean mIsDestroyed;
    private long mLastEventTimeMillis;
    private final Object mLock = new Object();
    private OnAccessibilityEventListener mOnAccessibilityEventListener;
    private final IUiAutomationConnection mUiAutomationConnection;
    private boolean mWaitingForEventDelivery;

    public interface AccessibilityEventFilter {
        boolean accept(AccessibilityEvent accessibilityEvent);
    }

    private class IAccessibilityServiceClientImpl extends IAccessibilityServiceClientWrapper {
        public IAccessibilityServiceClientImpl(Looper looper) {
            super(null, looper, new Callbacks(UiAutomation.this) {
                public void init(int connectionId, IBinder windowToken) {
                    synchronized (this$0.mLock) {
                        this$0.mConnectionId = connectionId;
                        this$0.mLock.notifyAll();
                    }
                }

                public void onServiceConnected() {
                }

                public void onInterrupt() {
                }

                public boolean onGesture(int gestureId) {
                    return false;
                }

                public void onAccessibilityEvent(AccessibilityEvent event) {
                    synchronized (this$0.mLock) {
                        this$0.mLastEventTimeMillis = event.getEventTime();
                        if (this$0.mWaitingForEventDelivery) {
                            this$0.mEventQueue.add(AccessibilityEvent.obtain(event));
                        }
                        this$0.mLock.notifyAll();
                    }
                    OnAccessibilityEventListener listener = this$0.mOnAccessibilityEventListener;
                    if (listener != null) {
                        listener.onAccessibilityEvent(AccessibilityEvent.obtain(event));
                    }
                }

                public boolean onKeyEvent(KeyEvent event) {
                    return false;
                }

                public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) {
                }

                public void onSoftKeyboardShowModeChanged(int showMode) {
                }

                public void onPerformGestureResult(int sequence, boolean completedSuccessfully) {
                }

                public void onFingerprintCapturingGesturesChanged(boolean active) {
                }

                public void onFingerprintGesture(int gesture) {
                }

                public void onAccessibilityButtonClicked() {
                }

                public void onAccessibilityButtonAvailabilityChanged(boolean available) {
                }
            });
        }
    }

    public interface OnAccessibilityEventListener {
        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);
    }

    public UiAutomation(Looper looper, IUiAutomationConnection connection) {
        if (looper == null) {
            throw new IllegalArgumentException("Looper cannot be null!");
        } else if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null!");
        } else {
            this.mUiAutomationConnection = connection;
            this.mClient = new IAccessibilityServiceClientImpl(looper);
        }
    }

    public void connect() {
        connect(0);
    }

    /* JADX WARNING: Missing block: B:12:?, code:
            r12.mUiAutomationConnection.connect(r12.mClient, r13);
            r12.mFlags = r13;
     */
    /* JADX WARNING: Missing block: B:13:0x0019, code:
            r9 = r12.mLock;
     */
    /* JADX WARNING: Missing block: B:14:0x001b, code:
            monitor-enter(r9);
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            r6 = android.os.SystemClock.uptimeMillis();
     */
    /* JADX WARNING: Missing block: B:19:0x0024, code:
            if (isConnectedLocked() == false) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            r12.mIsConnecting = false;
     */
    /* JADX WARNING: Missing block: B:23:0x0029, code:
            monitor-exit(r9);
     */
    /* JADX WARNING: Missing block: B:24:0x002a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:28:0x002e, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:30:0x0037, code:
            throw new java.lang.RuntimeException("Error while connecting UiAutomation", r3);
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            r4 = CONNECT_TIMEOUT_MILLIS - (android.os.SystemClock.uptimeMillis() - r6);
     */
    /* JADX WARNING: Missing block: B:33:0x0046, code:
            if (r4 > 0) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:35:0x0050, code:
            throw new java.lang.RuntimeException("Error while connecting UiAutomation");
     */
    /* JADX WARNING: Missing block: B:39:?, code:
            r12.mIsConnecting = false;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            r12.mLock.wait(r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void connect(int flags) {
        synchronized (this.mLock) {
            throwIfConnectedLocked();
            if (this.mIsConnecting) {
                return;
            }
            this.mIsConnecting = true;
        }
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void disconnect() {
        synchronized (this.mLock) {
            if (this.mIsConnecting) {
                throw new IllegalStateException("Cannot call disconnect() while connecting!");
            }
            throwIfNotConnectedLocked();
            this.mConnectionId = -1;
        }
        try {
            this.mUiAutomationConnection.disconnect();
        } catch (RemoteException re) {
            throw new RuntimeException("Error while disconnecting UiAutomation", re);
        }
    }

    public int getConnectionId() {
        int i;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            i = this.mConnectionId;
        }
        return i;
    }

    public boolean isDestroyed() {
        return this.mIsDestroyed;
    }

    public void setOnAccessibilityEventListener(OnAccessibilityEventListener listener) {
        synchronized (this.mLock) {
            this.mOnAccessibilityEventListener = listener;
        }
    }

    public void destroy() {
        disconnect();
        this.mIsDestroyed = true;
    }

    public final boolean performGlobalAction(int action) {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                return connection.performGlobalAction(action);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while calling performGlobalAction", re);
            }
        }
        return false;
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, -2, AccessibilityNodeInfo.ROOT_NODE_ID, focus);
    }

    public final AccessibilityServiceInfo getServiceInfo() {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                return connection.getServiceInfo();
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while getting AccessibilityServiceInfo", re);
            }
        }
        return null;
    }

    public final void setServiceInfo(AccessibilityServiceInfo info) {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            AccessibilityInteractionClient.getInstance().clearCache();
            connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                connection.setServiceInfo(info);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while setting AccessibilityServiceInfo", re);
            }
        }
    }

    public List<AccessibilityWindowInfo> getWindows() {
        int connectionId;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connectionId = this.mConnectionId;
        }
        return AccessibilityInteractionClient.getInstance().getWindows(connectionId);
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        int connectionId;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connectionId = this.mConnectionId;
        }
        return AccessibilityInteractionClient.getInstance().getRootInActiveWindow(connectionId);
    }

    public boolean injectInputEvent(InputEvent event, boolean sync) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.injectInputEvent(event, sync);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while injecting input event!", re);
            return false;
        }
    }

    public boolean setRotation(int rotation) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        switch (rotation) {
            case -2:
            case -1:
            case 0:
            case 1:
            case 2:
            case 3:
                try {
                    this.mUiAutomationConnection.setRotation(rotation);
                    return true;
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while setting rotation!", re);
                    return false;
                }
            default:
                throw new IllegalArgumentException("Invalid rotation.");
        }
    }

    public AccessibilityEvent executeAndWaitForEvent(Runnable command, AccessibilityEventFilter filter, long timeoutMillis) throws TimeoutException {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            this.mEventQueue.clear();
            this.mWaitingForEventDelivery = true;
        }
        long executionStartTimeMillis = SystemClock.uptimeMillis();
        command.run();
        try {
            long startTimeMillis = SystemClock.uptimeMillis();
            while (true) {
                List<AccessibilityEvent> localEvents = new ArrayList();
                synchronized (this.mLock) {
                    localEvents.addAll(this.mEventQueue);
                    this.mEventQueue.clear();
                }
                while (!localEvents.isEmpty()) {
                    AccessibilityEvent event = (AccessibilityEvent) localEvents.remove(0);
                    if (event.getEventTime() >= executionStartTimeMillis) {
                        if (filter.accept(event)) {
                            synchronized (this.mLock) {
                                this.mWaitingForEventDelivery = false;
                                this.mEventQueue.clear();
                                this.mLock.notifyAll();
                            }
                            return event;
                        }
                        event.recycle();
                    }
                }
                long remainingTimeMillis = timeoutMillis - (SystemClock.uptimeMillis() - startTimeMillis);
                if (remainingTimeMillis <= 0) {
                    throw new TimeoutException("Expected event not received within: " + timeoutMillis + " ms.");
                }
                synchronized (this.mLock) {
                    if (this.mEventQueue.isEmpty()) {
                        try {
                            this.mLock.wait(remainingTimeMillis);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mWaitingForEventDelivery = false;
                this.mEventQueue.clear();
                this.mLock.notifyAll();
            }
        }
    }

    public void waitForIdle(long idleTimeoutMillis, long globalTimeoutMillis) throws TimeoutException {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            long startTimeMillis = SystemClock.uptimeMillis();
            if (this.mLastEventTimeMillis <= 0) {
                this.mLastEventTimeMillis = startTimeMillis;
            }
            while (true) {
                long currentTimeMillis = SystemClock.uptimeMillis();
                if (globalTimeoutMillis - (currentTimeMillis - startTimeMillis) <= 0) {
                    throw new TimeoutException("No idle state with idle timeout: " + idleTimeoutMillis + " within global timeout: " + globalTimeoutMillis);
                }
                long remainingIdleTimeMillis = idleTimeoutMillis - (currentTimeMillis - this.mLastEventTimeMillis);
                if (remainingIdleTimeMillis <= 0) {
                } else {
                    try {
                        this.mLock.wait(remainingIdleTimeMillis);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public Bitmap takeScreenshot() {
        float screenshotWidth;
        float screenshotHeight;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        Point displaySize = new Point();
        display.getRealSize(displaySize);
        int displayWidth = displaySize.x;
        int displayHeight = displaySize.y;
        int rotation = display.getRotation();
        switch (rotation) {
            case 0:
                screenshotWidth = (float) displayWidth;
                screenshotHeight = (float) displayHeight;
                break;
            case 1:
                screenshotWidth = (float) displayHeight;
                screenshotHeight = (float) displayWidth;
                break;
            case 2:
                screenshotWidth = (float) displayWidth;
                screenshotHeight = (float) displayHeight;
                break;
            case 3:
                screenshotWidth = (float) displayHeight;
                screenshotHeight = (float) displayWidth;
                break;
            default:
                throw new IllegalArgumentException("Invalid rotation: " + rotation);
        }
        try {
            Bitmap screenShot = this.mUiAutomationConnection.takeScreenshot((int) screenshotWidth, (int) screenshotHeight);
            if (screenShot == null) {
                return null;
            }
            if (rotation != 0) {
                Bitmap unrotatedScreenShot = Bitmap.createBitmap(displayWidth, displayHeight, Config.ARGB_8888);
                Canvas canvas = new Canvas(unrotatedScreenShot);
                canvas.translate((float) (unrotatedScreenShot.getWidth() / 2), (float) (unrotatedScreenShot.getHeight() / 2));
                canvas.rotate(getDegreesForRotation(rotation));
                canvas.translate((-screenshotWidth) / 2.0f, (-screenshotHeight) / 2.0f);
                canvas.drawBitmap(screenShot, (float) TonemapCurve.LEVEL_BLACK, (float) TonemapCurve.LEVEL_BLACK, null);
                canvas.setBitmap(null);
                screenShot.recycle();
                screenShot = unrotatedScreenShot;
            }
            screenShot.setHasAlpha(false);
            return screenShot;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while taking screnshot!", re);
            return null;
        }
    }

    public void setRunAsMonkey(boolean enable) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            ActivityManager.getService().setUserIsMonkey(enable);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while setting run as monkey!", re);
        }
    }

    public boolean clearWindowContentFrameStats(int windowId) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.clearWindowContentFrameStats(windowId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error clearing window content frame stats!", re);
            return false;
        }
    }

    public WindowContentFrameStats getWindowContentFrameStats(int windowId) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.getWindowContentFrameStats(windowId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error getting window content frame stats!", re);
            return null;
        }
    }

    public void clearWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.clearWindowAnimationFrameStats();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error clearing window animation frame stats!", re);
        }
    }

    public WindowAnimationFrameStats getWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.getWindowAnimationFrameStats();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error getting window animation frame stats!", re);
            return null;
        }
    }

    public boolean grantRuntimePermission(String packageName, String permission, UserHandle userHandle) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.grantRuntimePermission(packageName, permission, userHandle.getIdentifier());
            return true;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error granting runtime permission", re);
            return false;
        }
    }

    public boolean revokeRuntimePermission(String packageName, String permission, UserHandle userHandle) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.revokeRuntimePermission(packageName, permission, userHandle.getIdentifier());
            return true;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error revoking runtime permission", re);
            return false;
        }
    }

    public ParcelFileDescriptor executeShellCommand(String command) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        ParcelFileDescriptor source = null;
        AutoCloseable autoCloseable = null;
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            source = pipe[0];
            autoCloseable = pipe[1];
            this.mUiAutomationConnection.executeShellCommand(command, autoCloseable);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error executing shell command!", ioe);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing shell command!", re);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
        return source;
    }

    private static float getDegreesForRotation(int value) {
        switch (value) {
            case 1:
                return 270.0f;
            case 2:
                return 180.0f;
            case 3:
                return 90.0f;
            default:
                return TonemapCurve.LEVEL_BLACK;
        }
    }

    private boolean isConnectedLocked() {
        return this.mConnectionId != -1;
    }

    private void throwIfConnectedLocked() {
        if (this.mConnectionId != -1) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }

    private void throwIfNotConnectedLocked() {
        if (!isConnectedLocked()) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }
}
