package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayManagerInternal.DisplayPowerCallbacks;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.hardware.display.DisplayManagerInternal.DisplayTransactionListener;
import android.hardware.display.DisplayViewport;
import android.hardware.display.IDisplayManager.Stub;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.IVirtualDisplayCallback;
import android.hardware.display.WifiDisplayStatus;
import android.hardware.input.InputManagerInternal;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager.BacklightBrightness;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.WindowManagerInternal;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.DisplayThread;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.am.ProcessList;
import com.android.server.display.DisplayAdapter.Listener;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DisplayManagerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final String FORCE_WIFI_DISPLAY_ENABLE = "persist.debug.wfd.enable";
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final boolean HWFLOW = false;
    private static final boolean IS_DEBUG_VERSION = false;
    private static final int MSG_DELIVER_DISPLAY_EVENT = 3;
    private static final int MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS = 2;
    private static final int MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER = 1;
    private static final int MSG_REQUEST_TRAVERSAL = 4;
    private static final int MSG_UPDATE_VIEWPORT = 5;
    private static final String TAG = "DisplayManagerService";
    private static final long WAIT_FOR_DEFAULT_DISPLAY_TIMEOUT = 10000;
    public final SparseArray<CallbackRecord> mCallbacks;
    private final Context mContext;
    private HwCustDisplayManagerService mCust;
    private final DisplayViewport mDefaultViewport;
    private final DisplayAdapterListener mDisplayAdapterListener;
    private final ArrayList<DisplayAdapter> mDisplayAdapters;
    private final ArrayList<DisplayDevice> mDisplayDevices;
    private DisplayPowerController mDisplayPowerController;
    private final CopyOnWriteArrayList<DisplayTransactionListener> mDisplayTransactionListeners;
    private final DisplayViewport mExternalTouchViewport;
    private int mGlobalDisplayBrightness;
    private int mGlobalDisplayState;
    private final DisplayManagerHandler mHandler;
    private InputManagerInternal mInputManagerInternal;
    private boolean mIsHighPrecision;
    private LocalDisplayAdapter mLocalDisplayAdapter;
    private final SparseArray<LogicalDisplay> mLogicalDisplays;
    private int mNextNonDefaultDisplayId;
    public boolean mOnlyCore;
    private boolean mPendingTraversal;
    private final PersistentDataStore mPersistentDataStore;
    private IMediaProjectionManager mProjectionService;
    public boolean mSafeMode;
    private final boolean mSingleDisplayDemoMode;
    private final SyncRoot mSyncRoot;
    private final ArrayList<CallbackRecord> mTempCallbacks;
    private final DisplayViewport mTempDefaultViewport;
    private final DisplayInfo mTempDisplayInfo;
    private final ArrayList<Runnable> mTempDisplayStateWorkQueue;
    private final DisplayViewport mTempExternalTouchViewport;
    private final Handler mUiHandler;
    private VirtualDisplayAdapter mVirtualDisplayAdapter;
    private WifiDisplayAdapter mWifiDisplayAdapter;
    private int mWifiDisplayScanRequestCount;
    private WindowManagerInternal mWindowManagerInternal;

    private final class BinderService extends Stub {
        private BinderService() {
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayInfo -wrap2 = DisplayManagerService.this.getDisplayInfoInternal(displayId, callingUid);
                return -wrap2;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setLowPowerDisplayLevel(int level) {
            DisplayManagerService.this.setLowPowerDisplayLevelservice(level);
        }

        public int getLowPowerDisplayLevel() {
            return DisplayManagerService.this.getLowPowerDisplayLevelservice();
        }

        public int[] getDisplayIds() {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int[] -wrap3 = DisplayManagerService.this.getDisplayIdsInternal(callingUid);
                return -wrap3;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void registerCallback(IDisplayManagerCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            int callingPid = Binder.getCallingPid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.registerCallbackInternal(callback, callingPid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void startWifiDisplayScan() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to start wifi display scans");
            int callingPid = Binder.getCallingPid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.startWifiDisplayScanInternal(callingPid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void stopWifiDisplayScan() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to stop wifi display scans");
            int callingPid = Binder.getCallingPid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.stopWifiDisplayScanInternal(callingPid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void connectWifiDisplay(String address) {
            if (address == null) {
                throw new IllegalArgumentException("address must not be null");
            }
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to connect to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.connectWifiDisplayInternal(address);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void disconnectWifiDisplay() {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.disconnectWifiDisplayInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void renameWifiDisplay(String address, String alias) {
            if (address == null) {
                throw new IllegalArgumentException("address must not be null");
            }
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to rename to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.renameWifiDisplayInternal(address, alias);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void forgetWifiDisplay(String address) {
            if (address == null) {
                throw new IllegalArgumentException("address must not be null");
            }
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to forget to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.forgetWifiDisplayInternal(address);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void pauseWifiDisplay() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to pause a wifi display session");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.pauseWifiDisplayInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void resumeWifiDisplay() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to resume a wifi display session");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.resumeWifiDisplayInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public WifiDisplayStatus getWifiDisplayStatus() {
            long token = Binder.clearCallingIdentity();
            try {
                WifiDisplayStatus -wrap0 = DisplayManagerService.this.getWifiDisplayStatusInternal();
                return -wrap0;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void requestColorTransform(int displayId, int colorTransformId) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_COLOR_TRANSFORM", "Permission required to change the display color transform");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.requestColorTransformInternal(displayId, colorTransformId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public int createVirtualDisplay(IVirtualDisplayCallback callback, IMediaProjection projection, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags) {
            int callingUid = Binder.getCallingUid();
            if (!validatePackageName(callingUid, packageName)) {
                throw new SecurityException("packageName must match the calling uid");
            } else if (callback == null) {
                throw new IllegalArgumentException("appToken must not be null");
            } else if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("name must be non-null and non-empty");
            } else if (width <= 0 || height <= 0 || densityDpi <= 0) {
                throw new IllegalArgumentException("width, height, and densityDpi must be greater than 0");
            } else {
                if ((flags & DisplayManagerService.MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) != 0) {
                    flags |= 16;
                }
                if ((flags & 8) != 0) {
                    flags &= -17;
                }
                if (projection != null) {
                    try {
                        if (DisplayManagerService.this.getProjectionService().isValidMediaProjection(projection)) {
                            flags = projection.applyVirtualDisplayFlags(flags);
                        } else {
                            throw new SecurityException("Invalid media projection");
                        }
                    } catch (RemoteException e) {
                        throw new SecurityException("unable to validate media projection or flags");
                    }
                }
                if (callingUid != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE && (flags & 16) != 0 && !canProjectVideo(projection)) {
                    throw new SecurityException("Requires CAPTURE_VIDEO_OUTPUT or CAPTURE_SECURE_VIDEO_OUTPUT permission, or an appropriate MediaProjection token in order to create a screen sharing virtual display.");
                } else if ((flags & DisplayManagerService.MSG_REQUEST_TRAVERSAL) == 0 || canProjectSecureVideo(projection)) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        int -wrap4 = DisplayManagerService.this.createVirtualDisplayInternal(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags);
                        return -wrap4;
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                } else {
                    throw new SecurityException("Requires CAPTURE_SECURE_VIDEO_OUTPUT or an appropriate MediaProjection token to create a secure virtual display.");
                }
            }
        }

        public void resizeVirtualDisplay(IVirtualDisplayCallback callback, int width, int height, int densityDpi) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.resizeVirtualDisplayInternal(callback.asBinder(), width, height, densityDpi);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setVirtualDisplaySurface(IVirtualDisplayCallback callback, Surface surface) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.setVirtualDisplaySurfaceInternal(callback.asBinder(), surface);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void releaseVirtualDisplay(IVirtualDisplayCallback callback) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.releaseVirtualDisplayInternal(callback.asBinder());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DisplayManagerService.this.mContext == null || DisplayManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump DisplayManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.dumpInternal(pw);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private boolean validatePackageName(int uid, String packageName) {
            if (packageName != null) {
                String[] packageNames = DisplayManagerService.this.mContext.getPackageManager().getPackagesForUid(uid);
                if (packageNames != null) {
                    int length = packageNames.length;
                    for (int i = 0; i < length; i += DisplayManagerService.MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                        if (packageNames[i].equals(packageName)) {
                            return true;
                        }
                    }
                }
            }
            return DisplayManagerService.IS_DEBUG_VERSION;
        }

        private boolean canProjectVideo(IMediaProjection projection) {
            if (projection != null) {
                try {
                    if (projection.canProjectVideo()) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Slog.e(DisplayManagerService.TAG, "Unable to query projection service for permissions", e);
                }
            }
            if (DisplayManagerService.this.mContext.checkCallingPermission("android.permission.CAPTURE_VIDEO_OUTPUT") == 0) {
                return true;
            }
            return canProjectSecureVideo(projection);
        }

        private boolean canProjectSecureVideo(IMediaProjection projection) {
            boolean z = true;
            if (projection != null) {
                try {
                    if (projection.canProjectSecureVideo()) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Slog.e(DisplayManagerService.TAG, "Unable to query projection service for permissions", e);
                }
            }
            if (DisplayManagerService.this.mContext.checkCallingPermission("android.permission.CAPTURE_SECURE_VIDEO_OUTPUT") != 0) {
                z = DisplayManagerService.IS_DEBUG_VERSION;
            }
            return z;
        }
    }

    private final class CallbackRecord implements DeathRecipient {
        private final IDisplayManagerCallback mCallback;
        public final int mPid;
        public boolean mWifiDisplayScanRequested;

        public CallbackRecord(int pid, IDisplayManagerCallback callback) {
            this.mPid = pid;
            this.mCallback = callback;
        }

        public void binderDied() {
            DisplayManagerService.this.onCallbackDied(this);
        }

        public void notifyDisplayEventAsync(int displayId, int event) {
            try {
                this.mCallback.onDisplayEvent(displayId, event);
            } catch (RemoteException ex) {
                Slog.w(DisplayManagerService.TAG, "Failed to notify process " + this.mPid + " that displays changed, assuming it died.", ex);
                if (DisplayManagerService.IS_DEBUG_VERSION) {
                    ArrayMap<String, Object> params = new ArrayMap();
                    params.put("checkType", "DisplayEventLostScene");
                    params.put("context", DisplayManagerService.this.mContext);
                    params.put("looper", DisplayThread.get().getLooper());
                    params.put("pid", Integer.valueOf(this.mPid));
                    if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                        HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
                    }
                }
                binderDied();
            }
        }
    }

    private final class DisplayAdapterListener implements Listener {
        private DisplayAdapterListener() {
        }

        public void onDisplayDeviceEvent(DisplayDevice device, int event) {
            switch (event) {
                case DisplayManagerService.MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER /*1*/:
                    DisplayManagerService.this.handleDisplayDeviceAdded(device);
                case DisplayManagerService.MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS /*2*/:
                    DisplayManagerService.this.handleDisplayDeviceChanged(device);
                case DisplayManagerService.MSG_DELIVER_DISPLAY_EVENT /*3*/:
                    DisplayManagerService.this.handleDisplayDeviceRemoved(device);
                default:
            }
        }

        public void onTraversalRequested() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.scheduleTraversalLocked(DisplayManagerService.IS_DEBUG_VERSION);
            }
        }
    }

    private final class DisplayManagerHandler extends Handler {
        public DisplayManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DisplayManagerService.MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER /*1*/:
                    DisplayManagerService.this.registerDefaultDisplayAdapter();
                case DisplayManagerService.MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS /*2*/:
                    DisplayManagerService.this.registerAdditionalDisplayAdapters();
                case DisplayManagerService.MSG_DELIVER_DISPLAY_EVENT /*3*/:
                    DisplayManagerService.this.deliverDisplayEvent(msg.arg1, msg.arg2);
                case DisplayManagerService.MSG_REQUEST_TRAVERSAL /*4*/:
                    DisplayManagerService.this.mWindowManagerInternal.requestTraversalFromDisplayManager();
                case DisplayManagerService.MSG_UPDATE_VIEWPORT /*5*/:
                    synchronized (DisplayManagerService.this.mSyncRoot) {
                        DisplayManagerService.this.mTempDefaultViewport.copyFrom(DisplayManagerService.this.mDefaultViewport);
                        DisplayManagerService.this.mTempExternalTouchViewport.copyFrom(DisplayManagerService.this.mExternalTouchViewport);
                        break;
                    }
                    DisplayManagerService.this.mInputManagerInternal.setDisplayViewports(DisplayManagerService.this.mTempDefaultViewport, DisplayManagerService.this.mTempExternalTouchViewport);
                default:
            }
        }
    }

    private final class LocalService extends DisplayManagerInternal {

        /* renamed from: com.android.server.display.DisplayManagerService.LocalService.1 */
        class AnonymousClass1 implements DisplayBlanker {
            final /* synthetic */ DisplayPowerCallbacks val$callbacks;

            AnonymousClass1(DisplayPowerCallbacks val$callbacks) {
                this.val$callbacks = val$callbacks;
            }

            public void requestDisplayState(int state, int brightness) {
                if (state == DisplayManagerService.MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                    DisplayManagerService.this.requestGlobalDisplayStateInternal(state, brightness);
                }
                this.val$callbacks.onDisplayStateChange(state);
                if (state != DisplayManagerService.MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                    DisplayManagerService.this.requestGlobalDisplayStateInternal(state, brightness);
                }
            }
        }

        private LocalService() {
        }

        public void initPowerManagement(DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager) {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.mDisplayPowerController = new DisplayPowerController(DisplayManagerService.this.mContext, callbacks, handler, sensorManager, new AnonymousClass1(callbacks));
            }
        }

        public boolean requestPowerState(DisplayPowerRequest request, boolean waitForNegativeProximity) {
            return DisplayManagerService.this.mDisplayPowerController.requestPowerState(request, waitForNegativeProximity);
        }

        public void forceDisplayState(int screenState, int screenBrightness) {
            DisplayManagerService.this.requestGlobalDisplayStateInternal(screenState, screenBrightness);
        }

        public void setBacklightBrightness(BacklightBrightness backlightBrightness) {
            DisplayManagerService.this.mDisplayPowerController.setBacklightBrightness(backlightBrightness);
        }

        public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
            DisplayManagerService.this.mDisplayPowerController.updateAutoBrightnessAdjustFactor(adjustFactor);
        }

        public int getMaxBrightnessForSeekbar() {
            return DisplayManagerService.this.mDisplayPowerController.getMaxBrightnessForSeekbar();
        }

        public boolean isProximitySensorAvailable() {
            return DisplayManagerService.this.mDisplayPowerController.isProximitySensorAvailable();
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            return DisplayManagerService.this.getDisplayInfoInternal(displayId, Process.myUid());
        }

        public void registerDisplayTransactionListener(DisplayTransactionListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            DisplayManagerService.this.registerDisplayTransactionListenerInternal(listener);
        }

        public void unregisterDisplayTransactionListener(DisplayTransactionListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            DisplayManagerService.this.unregisterDisplayTransactionListenerInternal(listener);
        }

        public void setDisplayInfoOverrideFromWindowManager(int displayId, DisplayInfo info) {
            DisplayManagerService.this.setDisplayInfoOverrideFromWindowManagerInternal(displayId, info);
        }

        public void performTraversalInTransactionFromWindowManager() {
            DisplayManagerService.this.performTraversalInTransactionFromWindowManagerInternal();
        }

        public void setDisplayProperties(int displayId, boolean hasContent, float requestedRefreshRate, int requestedMode, boolean inTraversal) {
            DisplayManagerService.this.setDisplayPropertiesInternal(displayId, hasContent, requestedRefreshRate, requestedMode, inTraversal);
        }

        public void setDisplayOffsets(int displayId, int x, int y) {
            DisplayManagerService.this.setDisplayOffsetsInternal(displayId, x, y);
        }
    }

    public static final class SyncRoot {
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.DisplayManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.DisplayManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.DisplayManagerService.<clinit>():void");
    }

    public DisplayManagerService(Context context) {
        super(context);
        this.mCust = (HwCustDisplayManagerService) HwCustUtils.createObj(HwCustDisplayManagerService.class, new Object[0]);
        this.mSyncRoot = new SyncRoot();
        this.mCallbacks = new SparseArray();
        this.mDisplayAdapters = new ArrayList();
        this.mDisplayDevices = new ArrayList();
        this.mLogicalDisplays = new SparseArray();
        this.mNextNonDefaultDisplayId = MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER;
        this.mDisplayTransactionListeners = new CopyOnWriteArrayList();
        this.mGlobalDisplayState = MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS;
        this.mGlobalDisplayBrightness = -1;
        this.mDefaultViewport = new DisplayViewport();
        this.mExternalTouchViewport = new DisplayViewport();
        this.mPersistentDataStore = new PersistentDataStore();
        this.mTempCallbacks = new ArrayList();
        this.mTempDisplayInfo = new DisplayInfo();
        this.mTempDefaultViewport = new DisplayViewport();
        this.mTempExternalTouchViewport = new DisplayViewport();
        this.mTempDisplayStateWorkQueue = new ArrayList();
        this.mIsHighPrecision = IS_DEBUG_VERSION;
        this.mContext = context;
        this.mHandler = new DisplayManagerHandler(DisplayThread.get().getLooper());
        this.mUiHandler = UiThread.getHandler();
        this.mDisplayAdapterListener = new DisplayAdapterListener();
        this.mSingleDisplayDemoMode = SystemProperties.getBoolean("persist.demo.singledisplay", IS_DEBUG_VERSION);
        this.mIsHighPrecision = true;
    }

    public void onStart() {
        this.mHandler.sendEmptyMessage(MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER);
        publishBinderService("display", new BinderService(), true);
        publishLocalService(DisplayManagerInternal.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == 100) {
            synchronized (this.mSyncRoot) {
                long timeout = SystemClock.uptimeMillis() + WAIT_FOR_DEFAULT_DISPLAY_TIMEOUT;
                while (this.mLogicalDisplays.get(0) == null) {
                    long delay = timeout - SystemClock.uptimeMillis();
                    if (delay <= 0) {
                        throw new RuntimeException("Timeout waiting for default display to be initialized.");
                    }
                    try {
                        this.mSyncRoot.wait(delay);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public void windowManagerAndInputReady() {
        synchronized (this.mSyncRoot) {
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            scheduleTraversalLocked(IS_DEBUG_VERSION);
        }
    }

    public void systemReady(boolean safeMode, boolean onlyCore) {
        synchronized (this.mSyncRoot) {
            this.mSafeMode = safeMode;
            this.mOnlyCore = onlyCore;
        }
        if (this.mLocalDisplayAdapter != null) {
            this.mLocalDisplayAdapter.registerContentObserver(this.mContext, this.mHandler);
        }
        this.mHandler.sendEmptyMessage(MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS);
    }

    private void registerDisplayTransactionListenerInternal(DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.add(listener);
    }

    private void unregisterDisplayTransactionListenerInternal(DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.remove(listener);
    }

    private void setDisplayInfoOverrideFromWindowManagerInternal(int displayId, DisplayInfo info) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display != null && display.setDisplayInfoOverrideFromWindowManagerLocked(info)) {
                sendDisplayEventLocked(displayId, MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS);
                scheduleTraversalLocked(IS_DEBUG_VERSION);
            }
        }
    }

    private void performTraversalInTransactionFromWindowManagerInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mPendingTraversal) {
                this.mPendingTraversal = IS_DEBUG_VERSION;
                performTraversalInTransactionLocked();
                for (DisplayTransactionListener listener : this.mDisplayTransactionListeners) {
                    listener.onDisplayTransaction();
                }
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestGlobalDisplayStateInternal(int state, int brightness) {
        if (state == 0) {
            state = MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS;
        }
        if (!this.mIsHighPrecision) {
            if (state == MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                brightness = 0;
            } else if (brightness < 0) {
                brightness = -1;
            } else if (brightness > DEFAULT_MAX_BRIGHTNESS) {
                brightness = DEFAULT_MAX_BRIGHTNESS;
            }
        }
        synchronized (this.mTempDisplayStateWorkQueue) {
            try {
                synchronized (this.mSyncRoot) {
                    if (this.mGlobalDisplayState == state && this.mGlobalDisplayBrightness == brightness) {
                    } else {
                        Trace.traceBegin(131072, "requestGlobalDisplayState(" + Display.stateToString(state) + ", brightness=" + brightness + ")");
                        this.mGlobalDisplayState = state;
                        this.mGlobalDisplayBrightness = brightness;
                        applyGlobalDisplayStateLocked(this.mTempDisplayStateWorkQueue);
                        for (int i = 0; i < this.mTempDisplayStateWorkQueue.size(); i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                            ((Runnable) this.mTempDisplayStateWorkQueue.get(i)).run();
                        }
                        Trace.traceEnd(131072);
                        this.mTempDisplayStateWorkQueue.clear();
                        return;
                    }
                }
            } finally {
                this.mTempDisplayStateWorkQueue.clear();
            }
        }
    }

    private DisplayInfo getDisplayInfoInternal(int displayId, int callingUid) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display != null) {
                DisplayInfo info = display.getDisplayInfoLocked();
                if (info.hasAccess(callingUid)) {
                    return info;
                }
            }
            return null;
        }
    }

    private int[] getDisplayIdsInternal(int callingUid) {
        int[] displayIds;
        synchronized (this.mSyncRoot) {
            int count = this.mLogicalDisplays.size();
            displayIds = new int[count];
            int i = 0;
            int n = 0;
            while (i < count) {
                int n2;
                if (((LogicalDisplay) this.mLogicalDisplays.valueAt(i)).getDisplayInfoLocked().hasAccess(callingUid)) {
                    n2 = n + MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER;
                    displayIds[n] = this.mLogicalDisplays.keyAt(i);
                } else {
                    n2 = n;
                }
                i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER;
                n = n2;
            }
            if (n != count) {
                displayIds = Arrays.copyOfRange(displayIds, 0, n);
            }
        }
        return displayIds;
    }

    private void registerCallbackInternal(IDisplayManagerCallback callback, int callingPid) {
        synchronized (this.mSyncRoot) {
            if (this.mCallbacks.get(callingPid) != null) {
                throw new SecurityException("The calling process has already registered an IDisplayManagerCallback.");
            }
            CallbackRecord record = new CallbackRecord(callingPid, callback);
            try {
                callback.asBinder().linkToDeath(record, 0);
                this.mCallbacks.put(callingPid, record);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void onCallbackDied(CallbackRecord record) {
        synchronized (this.mSyncRoot) {
            this.mCallbacks.remove(record.mPid);
            stopWifiDisplayScanLocked(record);
        }
    }

    private void startWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = (CallbackRecord) this.mCallbacks.get(callingPid);
            if (record == null) {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
            startWifiDisplayScanLocked(record);
        }
    }

    private void startWifiDisplayScanLocked(CallbackRecord record) {
        if (HWFLOW) {
            Slog.i(TAG, "startWifiDisplayScanLocked mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
        }
        if (HWFLOW) {
            Slog.i(TAG, "startWifiDisplayScanLocked record.mWifiDisplayScanRequested=" + record.mWifiDisplayScanRequested);
        }
        if (!record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = true;
            int i = this.mWifiDisplayScanRequestCount;
            this.mWifiDisplayScanRequestCount = i + MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER;
            if (i == 0 && this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestStartScanLocked();
            }
        }
    }

    private void stopWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = (CallbackRecord) this.mCallbacks.get(callingPid);
            if (record == null) {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
            stopWifiDisplayScanLocked(record);
        }
    }

    private void stopWifiDisplayScanLocked(CallbackRecord record) {
        if (record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = IS_DEBUG_VERSION;
            int i = this.mWifiDisplayScanRequestCount - 1;
            this.mWifiDisplayScanRequestCount = i;
            if (i == 0) {
                if (this.mWifiDisplayAdapter != null) {
                    this.mWifiDisplayAdapter.requestStopScanLocked();
                }
            } else if (this.mWifiDisplayScanRequestCount < 0) {
                Slog.wtf(TAG, "mWifiDisplayScanRequestCount became negative: " + this.mWifiDisplayScanRequestCount);
                this.mWifiDisplayScanRequestCount = 0;
            }
        }
        if (HWFLOW) {
            Slog.i(TAG, "stopWifiDisplayScanLocked record.mWifiDisplayScanRequested=" + record.mWifiDisplayScanRequested);
        }
        if (HWFLOW) {
            Slog.i(TAG, "stopWifiDisplayScanLocked mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
        }
    }

    private void connectWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestConnectLocked(address);
            }
        }
    }

    private void pauseWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestPauseLocked();
            }
        }
    }

    private void resumeWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestResumeLocked();
            }
        }
    }

    private void disconnectWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestDisconnectLocked();
            }
        }
    }

    private void renameWifiDisplayInternal(String address, String alias) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestRenameLocked(address, alias);
            }
        }
    }

    private void forgetWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestForgetLocked(address);
            }
        }
    }

    private WifiDisplayStatus getWifiDisplayStatusInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                WifiDisplayStatus wifiDisplayStatusLocked = this.mWifiDisplayAdapter.getWifiDisplayStatusLocked();
                return wifiDisplayStatusLocked;
            }
            wifiDisplayStatusLocked = new WifiDisplayStatus();
            return wifiDisplayStatusLocked;
        }
    }

    private void requestColorTransformInternal(int displayId, int colorTransformId) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (!(display == null || display.getRequestedColorTransformIdLocked() == colorTransformId)) {
                display.setRequestedColorTransformIdLocked(colorTransformId);
                scheduleTraversalLocked(IS_DEBUG_VERSION);
            }
        }
    }

    private int createVirtualDisplayInternal(IVirtualDisplayCallback callback, IMediaProjection projection, int callingUid, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                Slog.w(TAG, "Rejecting request to create private virtual display because the virtual display adapter is not available.");
                return -1;
            }
            DisplayDevice device = this.mVirtualDisplayAdapter.createVirtualDisplayLocked(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags);
            if (device == null) {
                return -1;
            }
            handleDisplayDeviceAddedLocked(device);
            LogicalDisplay display = findLogicalDisplayForDeviceLocked(device);
            if (display != null) {
                int displayIdLocked = display.getDisplayIdLocked();
                return displayIdLocked;
            }
            Slog.w(TAG, "Rejecting request to create virtual display because the logical display was not created.");
            this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(callback.asBinder());
            handleDisplayDeviceRemovedLocked(device);
            return -1;
        }
    }

    private void resizeVirtualDisplayInternal(IBinder appToken, int width, int height, int densityDpi) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                return;
            }
            this.mVirtualDisplayAdapter.resizeVirtualDisplayLocked(appToken, width, height, densityDpi);
        }
    }

    private void setVirtualDisplaySurfaceInternal(IBinder appToken, Surface surface) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                return;
            }
            this.mVirtualDisplayAdapter.setVirtualDisplaySurfaceLocked(appToken, surface);
        }
    }

    private void releaseVirtualDisplayInternal(IBinder appToken) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                return;
            }
            DisplayDevice device = this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(appToken);
            if (device != null) {
                handleDisplayDeviceRemovedLocked(device);
            }
        }
    }

    public int getLowPowerDisplayLevelservice() {
        if (this.mCust != null) {
            return this.mCust.getLowPowerDisplayLevel();
        }
        return 0;
    }

    public void setLowPowerDisplayLevelservice(int level) {
        if (this.mCust != null) {
            this.mCust.setLowPowerDisplayLevel(level);
        }
    }

    private void registerDefaultDisplayAdapter() {
        synchronized (this.mSyncRoot) {
            this.mLocalDisplayAdapter = new LocalDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener);
            registerDisplayAdapterLocked(this.mLocalDisplayAdapter);
        }
    }

    private void registerAdditionalDisplayAdapters() {
        synchronized (this.mSyncRoot) {
            if (shouldRegisterNonEssentialDisplayAdaptersLocked()) {
                registerOverlayDisplayAdapterLocked();
                registerWifiDisplayAdapterLocked();
                registerVirtualDisplayAdapterLocked();
            }
        }
    }

    private void registerOverlayDisplayAdapterLocked() {
        registerDisplayAdapterLocked(new OverlayDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mUiHandler));
    }

    private void registerWifiDisplayAdapterLocked() {
        if (this.mContext.getResources().getBoolean(17956985) || SystemProperties.getInt(FORCE_WIFI_DISPLAY_ENABLE, -1) == MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
            this.mWifiDisplayAdapter = new WifiDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mPersistentDataStore);
            registerDisplayAdapterLocked(this.mWifiDisplayAdapter);
        }
    }

    private void registerVirtualDisplayAdapterLocked() {
        this.mVirtualDisplayAdapter = new VirtualDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener);
        registerDisplayAdapterLocked(this.mVirtualDisplayAdapter);
    }

    private boolean shouldRegisterNonEssentialDisplayAdaptersLocked() {
        return (this.mSafeMode || this.mOnlyCore) ? IS_DEBUG_VERSION : true;
    }

    private void registerDisplayAdapterLocked(DisplayAdapter adapter) {
        this.mDisplayAdapters.add(adapter);
        adapter.registerLocked();
    }

    private void handleDisplayDeviceAdded(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            handleDisplayDeviceAddedLocked(device);
        }
    }

    private void handleDisplayDeviceAddedLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (this.mDisplayDevices.contains(device)) {
            Slog.w(TAG, "Attempted to add already added display device: " + info);
            return;
        }
        Slog.i(TAG, "Display device added: " + info);
        device.mDebugLastLoggedDeviceInfo = info;
        this.mDisplayDevices.add(device);
        addLogicalDisplayLocked(device);
        Runnable work = updateDisplayStateLocked(device);
        if (work != null) {
            work.run();
        }
        scheduleTraversalLocked(IS_DEBUG_VERSION);
    }

    private void handleDisplayDeviceChanged(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
            if (this.mDisplayDevices.contains(device)) {
                int diff = device.mDebugLastLoggedDeviceInfo.diff(info);
                if (diff == MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                    Slog.i(TAG, "Display device changed state: \"" + info.name + "\", " + Display.stateToString(info.state));
                } else if (diff != 0) {
                    Slog.i(TAG, "Display device changed: " + info);
                }
                device.mDebugLastLoggedDeviceInfo = info;
                device.applyPendingDisplayDeviceInfoChangesLocked();
                if (updateLogicalDisplaysLocked()) {
                    scheduleTraversalLocked(IS_DEBUG_VERSION);
                }
                return;
            }
            Slog.w(TAG, "Attempted to change non-existent display device: " + info);
        }
    }

    private void handleDisplayDeviceRemoved(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            handleDisplayDeviceRemovedLocked(device);
        }
    }

    private void handleDisplayDeviceRemovedLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (this.mDisplayDevices.remove(device)) {
            Slog.i(TAG, "Display device removed: " + info);
            device.mDebugLastLoggedDeviceInfo = info;
            updateLogicalDisplaysLocked();
            scheduleTraversalLocked(IS_DEBUG_VERSION);
            return;
        }
        Slog.w(TAG, "Attempted to remove non-existent display device: " + info);
    }

    private void applyGlobalDisplayStateLocked(List<Runnable> workQueue) {
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
            Runnable runnable = updateDisplayStateLocked((DisplayDevice) this.mDisplayDevices.get(i));
            if (runnable != null) {
                workQueue.add(runnable);
            }
        }
    }

    private Runnable updateDisplayStateLocked(DisplayDevice device) {
        if ((device.getDisplayDeviceInfoLocked().flags & 32) == 0) {
            return device.requestDisplayStateLocked(this.mGlobalDisplayState, this.mGlobalDisplayBrightness);
        }
        return null;
    }

    private void addLogicalDisplayLocked(DisplayDevice device) {
        DisplayDeviceInfo deviceInfo = device.getDisplayDeviceInfoLocked();
        boolean isDefault = (deviceInfo.flags & MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) != 0 ? true : IS_DEBUG_VERSION;
        if (isDefault && this.mLogicalDisplays.get(0) != null) {
            Slog.w(TAG, "Ignoring attempt to add a second default display: " + deviceInfo);
            isDefault = IS_DEBUG_VERSION;
        }
        if (isDefault || !this.mSingleDisplayDemoMode) {
            int displayId = assignDisplayIdLocked(isDefault);
            LogicalDisplay display = new LogicalDisplay(displayId, assignLayerStackLocked(displayId), device);
            display.updateLocked(this.mDisplayDevices);
            if (display.isValidLocked()) {
                this.mLogicalDisplays.put(displayId, display);
                if (isDefault) {
                    this.mSyncRoot.notifyAll();
                }
                sendDisplayEventLocked(displayId, MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER);
                return;
            }
            Slog.w(TAG, "Ignoring display device because the logical display created from it was not considered valid: " + deviceInfo);
            return;
        }
        Slog.i(TAG, "Not creating a logical display for a secondary display  because single display demo mode is enabled: " + deviceInfo);
    }

    private int assignDisplayIdLocked(boolean isDefault) {
        if (isDefault) {
            return 0;
        }
        int i = this.mNextNonDefaultDisplayId;
        this.mNextNonDefaultDisplayId = i + MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER;
        return i;
    }

    private int assignLayerStackLocked(int displayId) {
        return displayId;
    }

    private boolean updateLogicalDisplaysLocked() {
        boolean changed = IS_DEBUG_VERSION;
        int i = this.mLogicalDisplays.size();
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return changed;
            }
            int displayId = this.mLogicalDisplays.keyAt(i2);
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.valueAt(i2);
            this.mTempDisplayInfo.copyFrom(display.getDisplayInfoLocked());
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                this.mLogicalDisplays.removeAt(i2);
                sendDisplayEventLocked(displayId, MSG_DELIVER_DISPLAY_EVENT);
                changed = true;
            } else if (!this.mTempDisplayInfo.equals(display.getDisplayInfoLocked())) {
                sendDisplayEventLocked(displayId, MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS);
                changed = true;
            }
            i = i2;
        }
    }

    private void performTraversalInTransactionLocked() {
        clearViewportsLocked();
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
            DisplayDevice device = (DisplayDevice) this.mDisplayDevices.get(i);
            configureDisplayInTransactionLocked(device);
            device.performTraversalInTransactionLocked();
        }
        if (this.mInputManagerInternal != null) {
            this.mHandler.sendEmptyMessage(MSG_UPDATE_VIEWPORT);
        }
    }

    private void setDisplayPropertiesInternal(int displayId, boolean hasContent, float requestedRefreshRate, int requestedModeId, boolean inTraversal) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display == null) {
                return;
            }
            if (display.hasContentLocked() != hasContent) {
                display.setHasContentLocked(hasContent);
                scheduleTraversalLocked(inTraversal);
            }
            if (requestedModeId == 0 && requestedRefreshRate != 0.0f) {
                requestedModeId = display.getDisplayInfoLocked().findDefaultModeByRefreshRate(requestedRefreshRate);
            }
            if (display.getRequestedModeIdLocked() != requestedModeId) {
                display.setRequestedModeIdLocked(requestedModeId);
                scheduleTraversalLocked(inTraversal);
            }
        }
    }

    private void setDisplayOffsetsInternal(int displayId, int x, int y) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display == null) {
                return;
            }
            if (!(display.getDisplayOffsetXLocked() == x && display.getDisplayOffsetYLocked() == y)) {
                display.setDisplayOffsetsLocked(x, y);
                scheduleTraversalLocked(IS_DEBUG_VERSION);
            }
        }
    }

    private void clearViewportsLocked() {
        this.mDefaultViewport.valid = IS_DEBUG_VERSION;
        this.mExternalTouchViewport.valid = IS_DEBUG_VERSION;
    }

    private void configureDisplayInTransactionLocked(DisplayDevice device) {
        boolean z = true;
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        boolean ownContent = (info.flags & DumpState.DUMP_PACKAGES) != 0 ? true : IS_DEBUG_VERSION;
        LogicalDisplay findLogicalDisplayForDeviceLocked = findLogicalDisplayForDeviceLocked(device);
        if (!ownContent) {
            if (!(findLogicalDisplayForDeviceLocked == null || findLogicalDisplayForDeviceLocked.hasContentLocked())) {
                findLogicalDisplayForDeviceLocked = null;
            }
            if (findLogicalDisplayForDeviceLocked == null) {
                findLogicalDisplayForDeviceLocked = (LogicalDisplay) this.mLogicalDisplays.get(0);
            }
        }
        if (findLogicalDisplayForDeviceLocked == null) {
            Slog.w(TAG, "Missing logical display to use for physical display device: " + device.getDisplayDeviceInfoLocked());
            return;
        }
        if (info.state != MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
            z = IS_DEBUG_VERSION;
        }
        findLogicalDisplayForDeviceLocked.configureDisplayInTransactionLocked(device, z);
        if (!(this.mDefaultViewport.valid || (info.flags & MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) == 0)) {
            setViewportLocked(this.mDefaultViewport, findLogicalDisplayForDeviceLocked, device);
        }
        if (!this.mExternalTouchViewport.valid && info.touch == MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS) {
            setViewportLocked(this.mExternalTouchViewport, findLogicalDisplayForDeviceLocked, device);
        }
    }

    private static void setViewportLocked(DisplayViewport viewport, LogicalDisplay display, DisplayDevice device) {
        viewport.valid = true;
        viewport.displayId = display.getDisplayIdLocked();
        device.populateViewportLocked(viewport);
    }

    private LogicalDisplay findLogicalDisplayForDeviceLocked(DisplayDevice device) {
        int count = this.mLogicalDisplays.size();
        for (int i = 0; i < count; i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.valueAt(i);
            if (display.getPrimaryDisplayDeviceLocked() == device) {
                return display;
            }
        }
        return null;
    }

    private void sendDisplayEventLocked(int displayId, int event) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DELIVER_DISPLAY_EVENT, displayId, event));
    }

    private void scheduleTraversalLocked(boolean inTraversal) {
        if (!this.mPendingTraversal && this.mWindowManagerInternal != null) {
            this.mPendingTraversal = true;
            if (!inTraversal) {
                this.mHandler.sendEmptyMessage(MSG_REQUEST_TRAVERSAL);
            }
        }
    }

    private void deliverDisplayEvent(int displayId, int event) {
        synchronized (this.mSyncRoot) {
            int i;
            int count = this.mCallbacks.size();
            this.mTempCallbacks.clear();
            for (i = 0; i < count; i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                this.mTempCallbacks.add((CallbackRecord) this.mCallbacks.valueAt(i));
            }
        }
        for (i = 0; i < count; i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
            ((CallbackRecord) this.mTempCallbacks.get(i)).notifyDisplayEventAsync(displayId, event);
        }
        this.mTempCallbacks.clear();
    }

    private IMediaProjectionManager getProjectionService() {
        if (this.mProjectionService == null) {
            this.mProjectionService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        }
        return this.mProjectionService;
    }

    private void dumpInternal(PrintWriter pw) {
        pw.println("DISPLAY MANAGER (dumpsys display)");
        synchronized (this.mSyncRoot) {
            int i;
            pw.println("  mOnlyCode=" + this.mOnlyCore);
            pw.println("  mSafeMode=" + this.mSafeMode);
            pw.println("  mPendingTraversal=" + this.mPendingTraversal);
            pw.println("  mGlobalDisplayState=" + Display.stateToString(this.mGlobalDisplayState));
            pw.println("  mNextNonDefaultDisplayId=" + this.mNextNonDefaultDisplayId);
            pw.println("  mDefaultViewport=" + this.mDefaultViewport);
            pw.println("  mExternalTouchViewport=" + this.mExternalTouchViewport);
            pw.println("  mSingleDisplayDemoMode=" + this.mSingleDisplayDemoMode);
            pw.println("  mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
            IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "    ");
            ipw.increaseIndent();
            pw.println();
            pw.println("Display Adapters: size=" + this.mDisplayAdapters.size());
            for (DisplayAdapter adapter : this.mDisplayAdapters) {
                pw.println("  " + adapter.getName());
                adapter.dumpLocked(ipw);
            }
            pw.println();
            pw.println("Display Devices: size=" + this.mDisplayDevices.size());
            for (DisplayDevice device : this.mDisplayDevices) {
                pw.println("  " + device.getDisplayDeviceInfoLocked());
                device.dumpLocked(ipw);
            }
            int logicalDisplayCount = this.mLogicalDisplays.size();
            pw.println();
            pw.println("Logical Displays: size=" + logicalDisplayCount);
            for (i = 0; i < logicalDisplayCount; i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.valueAt(i);
                pw.println("  Display " + this.mLogicalDisplays.keyAt(i) + ":");
                display.dumpLocked(ipw);
            }
            int callbackCount = this.mCallbacks.size();
            pw.println();
            pw.println("Callbacks: size=" + callbackCount);
            for (i = 0; i < callbackCount; i += MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER) {
                CallbackRecord callback = (CallbackRecord) this.mCallbacks.valueAt(i);
                pw.println("  " + i + ": mPid=" + callback.mPid + ", mWifiDisplayScanRequested=" + callback.mWifiDisplayScanRequested);
            }
            if (this.mDisplayPowerController != null) {
                this.mDisplayPowerController.dump(pw);
            }
        }
    }
}
