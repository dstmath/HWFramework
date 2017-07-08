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
    private static final String LOG_TAG = null;
    public static final int ROTATION_FREEZE_0 = 0;
    public static final int ROTATION_FREEZE_180 = 2;
    public static final int ROTATION_FREEZE_270 = 3;
    public static final int ROTATION_FREEZE_90 = 1;
    public static final int ROTATION_FREEZE_CURRENT = -1;
    public static final int ROTATION_UNFREEZE = -2;
    private final IAccessibilityServiceClient mClient;
    private int mConnectionId;
    private final ArrayList<AccessibilityEvent> mEventQueue;
    private int mFlags;
    private boolean mIsConnecting;
    private boolean mIsDestroyed;
    private long mLastEventTimeMillis;
    private final Object mLock;
    private OnAccessibilityEventListener mOnAccessibilityEventListener;
    private final IUiAutomationConnection mUiAutomationConnection;
    private boolean mWaitingForEventDelivery;

    public interface AccessibilityEventFilter {
        boolean accept(AccessibilityEvent accessibilityEvent);
    }

    private class IAccessibilityServiceClientImpl extends IAccessibilityServiceClientWrapper {

        /* renamed from: android.app.UiAutomation.IAccessibilityServiceClientImpl.1 */
        static class AnonymousClass1 implements Callbacks {
            final /* synthetic */ UiAutomation val$this$0;

            AnonymousClass1(UiAutomation val$this$0) {
                this.val$this$0 = val$this$0;
            }

            public void init(int connectionId, IBinder windowToken) {
                synchronized (this.val$this$0.mLock) {
                    this.val$this$0.mConnectionId = connectionId;
                    this.val$this$0.mLock.notifyAll();
                }
            }

            public void onServiceConnected() {
            }

            public void onInterrupt() {
            }

            public boolean onGesture(int gestureId) {
                return UiAutomation.DEBUG;
            }

            public void onAccessibilityEvent(AccessibilityEvent event) {
                synchronized (this.val$this$0.mLock) {
                    this.val$this$0.mLastEventTimeMillis = event.getEventTime();
                    if (this.val$this$0.mWaitingForEventDelivery) {
                        this.val$this$0.mEventQueue.add(AccessibilityEvent.obtain(event));
                    }
                    this.val$this$0.mLock.notifyAll();
                }
                OnAccessibilityEventListener listener = this.val$this$0.mOnAccessibilityEventListener;
                if (listener != null) {
                    listener.onAccessibilityEvent(AccessibilityEvent.obtain(event));
                }
            }

            public boolean onKeyEvent(KeyEvent event) {
                return UiAutomation.DEBUG;
            }

            public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) {
            }

            public void onSoftKeyboardShowModeChanged(int showMode) {
            }

            public void onPerformGestureResult(int sequence, boolean completedSuccessfully) {
            }
        }

        public IAccessibilityServiceClientImpl(Looper looper) {
            super(null, looper, new AnonymousClass1(UiAutomation.this));
        }
    }

    public interface OnAccessibilityEventListener {
        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.UiAutomation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.UiAutomation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.UiAutomation.<clinit>():void");
    }

    public UiAutomation(Looper looper, IUiAutomationConnection connection) {
        this.mLock = new Object();
        this.mEventQueue = new ArrayList();
        this.mConnectionId = ROTATION_FREEZE_CURRENT;
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
        connect(ROTATION_FREEZE_0);
    }

    public void connect(int flags) {
        synchronized (this.mLock) {
            throwIfConnectedLocked();
            if (this.mIsConnecting) {
                return;
            }
            this.mIsConnecting = true;
            try {
                this.mUiAutomationConnection.connect(this.mClient, flags);
                this.mFlags = flags;
                synchronized (this.mLock) {
                    long startTimeMillis = SystemClock.uptimeMillis();
                    while (!isConnectedLocked()) {
                        try {
                            long remainingTimeMillis = CONNECT_TIMEOUT_MILLIS - (SystemClock.uptimeMillis() - startTimeMillis);
                            if (remainingTimeMillis <= 0) {
                                throw new RuntimeException("Error while connecting UiAutomation");
                            }
                            try {
                                this.mLock.wait(remainingTimeMillis);
                            } catch (InterruptedException e) {
                            }
                        } catch (Throwable th) {
                            this.mIsConnecting = DEBUG;
                        }
                    }
                    this.mIsConnecting = DEBUG;
                }
            } catch (RemoteException re) {
                throw new RuntimeException("Error while connecting UiAutomation", re);
            }
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
            this.mConnectionId = ROTATION_FREEZE_CURRENT;
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
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                return connection.performGlobalAction(action);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while calling performGlobalAction", re);
            }
        }
        return DEBUG;
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, ROTATION_UNFREEZE, AccessibilityNodeInfo.ROOT_NODE_ID, focus);
    }

    public final AccessibilityServiceInfo getServiceInfo() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
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
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            AccessibilityInteractionClient.getInstance().clearCache();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
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
            return DEBUG;
        }
    }

    public boolean setRotation(int rotation) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        switch (rotation) {
            case ROTATION_UNFREEZE /*-2*/:
            case ROTATION_FREEZE_CURRENT /*-1*/:
            case ROTATION_FREEZE_0 /*0*/:
            case ROTATION_FREEZE_90 /*1*/:
            case ROTATION_FREEZE_180 /*2*/:
            case ROTATION_FREEZE_270 /*3*/:
                try {
                    this.mUiAutomationConnection.setRotation(rotation);
                    return true;
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while setting rotation!", re);
                    return DEBUG;
                }
            default:
                throw new IllegalArgumentException("Invalid rotation.");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AccessibilityEvent executeAndWaitForEvent(Runnable command, AccessibilityEventFilter filter, long timeoutMillis) throws TimeoutException {
        AccessibilityEvent event;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            this.mEventQueue.clear();
            this.mWaitingForEventDelivery = true;
        }
        long executionStartTimeMillis = SystemClock.uptimeMillis();
        command.run();
        synchronized (this.mLock) {
            long startTimeMillis = SystemClock.uptimeMillis();
            while (true) {
                if (this.mEventQueue.isEmpty()) {
                    long remainingTimeMillis = timeoutMillis - (SystemClock.uptimeMillis() - startTimeMillis);
                    if (remainingTimeMillis <= 0) {
                        break;
                    }
                    try {
                        this.mLock.wait(remainingTimeMillis);
                    } catch (InterruptedException e) {
                    }
                } else {
                    event = (AccessibilityEvent) this.mEventQueue.remove(ROTATION_FREEZE_0);
                    if (event.getEventTime() < executionStartTimeMillis) {
                        continue;
                    } else if (filter.accept(event)) {
                        break;
                    } else {
                        try {
                            event.recycle();
                        } catch (Throwable th) {
                            this.mWaitingForEventDelivery = DEBUG;
                            this.mEventQueue.clear();
                            this.mLock.notifyAll();
                        }
                    }
                }
            }
            throw new TimeoutException("Expected event not received within: " + timeoutMillis + " ms.");
        }
        return event;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    break;
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
            throw new TimeoutException("No idle state with idle timeout: " + idleTimeoutMillis + " within global timeout: " + globalTimeoutMillis);
        }
    }

    public Bitmap takeScreenshot() {
        float screenshotWidth;
        float screenshotHeight;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(ROTATION_FREEZE_0);
        Point displaySize = new Point();
        display.getRealSize(displaySize);
        int displayWidth = displaySize.x;
        int displayHeight = displaySize.y;
        int rotation = display.getRotation();
        switch (rotation) {
            case ROTATION_FREEZE_0 /*0*/:
                screenshotWidth = (float) displayWidth;
                screenshotHeight = (float) displayHeight;
                break;
            case ROTATION_FREEZE_90 /*1*/:
                screenshotWidth = (float) displayHeight;
                screenshotHeight = (float) displayWidth;
                break;
            case ROTATION_FREEZE_180 /*2*/:
                screenshotWidth = (float) displayWidth;
                screenshotHeight = (float) displayHeight;
                break;
            case ROTATION_FREEZE_270 /*3*/:
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
                canvas.translate((float) (unrotatedScreenShot.getWidth() / ROTATION_FREEZE_180), (float) (unrotatedScreenShot.getHeight() / ROTATION_FREEZE_180));
                canvas.rotate(getDegreesForRotation(rotation));
                canvas.translate((-screenshotWidth) / 2.0f, (-screenshotHeight) / 2.0f);
                canvas.drawBitmap(screenShot, 0.0f, 0.0f, null);
                canvas.setBitmap(null);
                screenShot.recycle();
                screenShot = unrotatedScreenShot;
            }
            screenShot.setHasAlpha(DEBUG);
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
            ActivityManagerNative.getDefault().setUserIsMonkey(enable);
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
            return DEBUG;
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
            return DEBUG;
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
            return DEBUG;
        }
    }

    public ParcelFileDescriptor executeShellCommand(String command) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        ParcelFileDescriptor parcelFileDescriptor = null;
        AutoCloseable autoCloseable = null;
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            parcelFileDescriptor = pipe[ROTATION_FREEZE_0];
            autoCloseable = pipe[ROTATION_FREEZE_90];
            this.mUiAutomationConnection.executeShellCommand(command, autoCloseable);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error executing shell command!", ioe);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing shell command!", re);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
        return parcelFileDescriptor;
    }

    private static float getDegreesForRotation(int value) {
        switch (value) {
            case ROTATION_FREEZE_90 /*1*/:
                return 270.0f;
            case ROTATION_FREEZE_180 /*2*/:
                return 180.0f;
            case ROTATION_FREEZE_270 /*3*/:
                return 90.0f;
            default:
                return 0.0f;
        }
    }

    private boolean isConnectedLocked() {
        return this.mConnectionId != ROTATION_FREEZE_CURRENT ? true : DEBUG;
    }

    private void throwIfConnectedLocked() {
        if (this.mConnectionId != ROTATION_FREEZE_CURRENT) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }

    private void throwIfNotConnectedLocked() {
        if (!isConnectedLocked()) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }
}
