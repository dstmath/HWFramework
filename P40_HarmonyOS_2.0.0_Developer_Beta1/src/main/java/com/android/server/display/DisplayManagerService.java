package com.android.server.display;

import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorSpace;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.hardware.display.AmbientBrightnessDayStats;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.Curve;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayViewport;
import android.hardware.display.DisplayedContentSample;
import android.hardware.display.DisplayedContentSamplingAttributes;
import android.hardware.display.HwFoldScreenState;
import android.hardware.display.IDisplayManager;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.IVirtualDisplayCallback;
import android.hardware.display.WifiDisplayStatus;
import android.hardware.input.InputManagerInternal;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.IntArray;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Spline;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.vrsystem.DisplayInfoExt;
import android.vrsystem.IVRSystemServiceManager;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.AnimationThread;
import com.android.server.DisplayThread;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayModeDirector;
import com.android.server.display.color.DisplayTransformManager;
import com.android.server.wm.SurfaceAnimationThread;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;
import com.huawei.android.hardware.display.IHwDisplayManager;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DisplayManagerService extends SystemService implements IHwDisplayManagerInner {
    private static final boolean DEBUG = false;
    private static final String FORCE_WIFI_DISPLAY_ENABLE = "persist.debug.wfd.enable";
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean IS_FOLDABEL;
    private static final int MSG_DELIVER_DISPLAY_EVENT = 3;
    private static final int MSG_FOLD_UPDATE_CONFIGURATION = 8;
    private static final int MSG_LOAD_BRIGHTNESS_CONFIGURATION = 6;
    private static final int MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS = 2;
    private static final int MSG_REGISTER_DEFAULT_DISPLAY_ADAPTERS = 1;
    private static final int MSG_REQUEST_TRAVERSAL = 4;
    private static final int MSG_UPDATE_VIEWPORT = 5;
    private static final long NEED_DELAY_TIME_COORDINATION = 1900;
    private static final long NEED_DELAY_TIME_DEFAULT = (HwFoldScreenState.isOutFoldDevice() ? NEED_DELAY_TIME_COORDINATION : 200);
    private static final String PROP_DEFAULT_DISPLAY_TOP_INSET = "persist.sys.displayinset.top";
    private static final String TAG = "DisplayManagerService";
    private static final long WAIT_FOR_DEFAULT_DISPLAY_TIMEOUT = 10000;
    public final SparseArray<CallbackRecord> mCallbacks;
    private final Context mContext;
    private int mCurrentUserId;
    private final int mDefaultDisplayDefaultColorMode;
    private int mDefaultDisplayTopInset;
    private final SparseArray<IntArray> mDisplayAccessUIDs;
    private final DisplayAdapterListener mDisplayAdapterListener;
    private final ArrayList<DisplayAdapter> mDisplayAdapters;
    private final ArrayList<DisplayDevice> mDisplayDevices;
    private int mDisplayMode;
    private final DisplayModeDirector mDisplayModeDirector;
    private DisplayPowerController mDisplayPowerController;
    private final CopyOnWriteArrayList<DisplayManagerInternal.DisplayTransactionListener> mDisplayTransactionListeners;
    private int mGlobalAlpmState;
    private int mGlobalDisplayBrightness;
    private int mGlobalDisplayState;
    private final DisplayManagerHandler mHandler;
    IHwDisplayManagerServiceEx mHwDMSEx;
    HwInnerDisplayManagerService mHwInnerService;
    private final Injector mInjector;
    private InputManagerInternal mInputManagerInternal;
    private boolean mIsDisplayNeedDelaydefault;
    private boolean mIsDisplayRequest;
    private boolean mIsForceUpdateDisplayInfo;
    private LocalDisplayAdapter mLocalDisplayAdapter;
    private final SparseArray<LogicalDisplay> mLogicalDisplays;
    private final Curve mMinimumBrightnessCurve;
    private final Spline mMinimumBrightnessSpline;
    private long mNeedDelayTime;
    private boolean mNeedHandleDisplayMode;
    private boolean mNeedHandleDisplayRequest;
    private int mNextNonDefaultDisplayId;
    public boolean mOnlyCore;
    private boolean mPendingTraversal;
    private final PersistentDataStore mPersistentDataStore;
    private PowerManagerInternal mPowerManagerInternal;
    private IMediaProjectionManager mProjectionService;
    public boolean mSafeMode;
    private final boolean mSingleDisplayDemoMode;
    private Point mStableDisplaySize;
    private final SyncRoot mSyncRoot;
    private boolean mSystemReady;
    private final ArrayList<CallbackRecord> mTempCallbacks;
    private final DisplayInfo mTempDisplayInfo;
    private final ArrayList<Runnable> mTempDisplayStateWorkQueue;
    private final ArrayList<DisplayViewport> mTempViewports;
    private int mTemporaryScreenBrightnessSettingOverride;
    private SurfaceControl.Transaction mTransaction;
    private final Handler mUiHandler;
    @GuardedBy({"mSyncRoot"})
    private final ArrayList<DisplayViewport> mViewports;
    private VirtualDisplayAdapter mVirtualDisplayAdapter;
    private IVRSystemServiceManager mVrMananger;
    private final ColorSpace mWideColorSpace;
    private WifiDisplayAdapter mWifiDisplayAdapter;
    private int mWifiDisplayScanRequestCount;
    private WindowManagerInternal mWindowManagerInternal;

    public static final class SyncRoot {
    }

    static {
        boolean z = false;
        if (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get("persist.sys.fold.disp.size").isEmpty()) {
            z = true;
        }
        IS_FOLDABEL = z;
    }

    public DisplayManagerService(Context context) {
        this(context, new Injector());
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        this.mHwDMSEx = HwServiceExFactory.getHwDisplayManagerServiceEx(this, context);
    }

    @VisibleForTesting
    DisplayManagerService(Context context, Injector injector) {
        super(context);
        this.mSyncRoot = new SyncRoot();
        this.mCallbacks = new SparseArray<>();
        this.mDisplayAdapters = new ArrayList<>();
        this.mDisplayDevices = new ArrayList<>();
        this.mLogicalDisplays = new SparseArray<>();
        this.mNextNonDefaultDisplayId = 1;
        this.mDisplayTransactionListeners = new CopyOnWriteArrayList<>();
        this.mGlobalDisplayState = 2;
        this.mGlobalDisplayBrightness = -1;
        this.mStableDisplaySize = new Point();
        this.mViewports = new ArrayList<>();
        this.mPersistentDataStore = new PersistentDataStore();
        this.mTempCallbacks = new ArrayList<>();
        this.mTempDisplayInfo = new DisplayInfo();
        this.mTempViewports = new ArrayList<>();
        this.mTempDisplayStateWorkQueue = new ArrayList<>();
        this.mDisplayAccessUIDs = new SparseArray<>();
        this.mGlobalAlpmState = -1;
        this.mTemporaryScreenBrightnessSettingOverride = -1;
        this.mNeedHandleDisplayRequest = false;
        this.mIsDisplayRequest = false;
        this.mIsDisplayNeedDelaydefault = false;
        this.mNeedHandleDisplayMode = false;
        this.mIsForceUpdateDisplayInfo = false;
        this.mDisplayMode = 1;
        this.mTransaction = null;
        this.mNeedDelayTime = NEED_DELAY_TIME_DEFAULT;
        this.mHwDMSEx = null;
        this.mHwInnerService = new HwInnerDisplayManagerService(this);
        this.mInjector = injector;
        this.mContext = context;
        this.mHandler = new DisplayManagerHandler(DisplayThread.get().getLooper());
        this.mUiHandler = UiThread.getHandler();
        this.mDisplayAdapterListener = new DisplayAdapterListener();
        this.mDisplayModeDirector = new DisplayModeDirector(context, this.mHandler);
        this.mSingleDisplayDemoMode = SystemProperties.getBoolean("persist.demo.singledisplay", false);
        Resources resources = this.mContext.getResources();
        this.mDefaultDisplayDefaultColorMode = this.mContext.getResources().getInteger(17694771);
        this.mDefaultDisplayTopInset = SystemProperties.getInt(PROP_DEFAULT_DISPLAY_TOP_INSET, -1);
        float[] lux = getFloatArray(resources.obtainTypedArray(17236036));
        float[] nits = getFloatArray(resources.obtainTypedArray(17236037));
        this.mMinimumBrightnessCurve = new Curve(lux, nits);
        this.mMinimumBrightnessSpline = Spline.createSpline(lux, nits);
        this.mCurrentUserId = 0;
        this.mWideColorSpace = SurfaceControl.getCompositionColorSpaces()[1];
        this.mHwDMSEx = HwServiceExFactory.getHwDisplayManagerServiceEx(this, context);
        this.mSystemReady = false;
    }

    public void setupSchedulerPolicies() {
        Process.setThreadGroupAndCpuset(DisplayThread.get().getThreadId(), 5);
        Process.setThreadGroupAndCpuset(AnimationThread.get().getThreadId(), 5);
        Process.setThreadGroupAndCpuset(SurfaceAnimationThread.get().getThreadId(), 5);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.display.DisplayManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.display.DisplayManagerService$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        synchronized (this.mSyncRoot) {
            this.mPersistentDataStore.loadIfNeeded();
            loadStableDisplayValuesLocked();
        }
        this.mHandler.sendEmptyMessage(1);
        publishBinderService("display", new BinderService(), true);
        publishLocalService(DisplayManagerInternal.class, new LocalService());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 100) {
            synchronized (this.mSyncRoot) {
                long timeout = SystemClock.uptimeMillis() + this.mInjector.getDefaultDisplayDelayTimeout();
                while (true) {
                    if (this.mLogicalDisplays.get(0) != null) {
                        if (this.mVirtualDisplayAdapter == null) {
                        }
                    }
                    long delay = timeout - SystemClock.uptimeMillis();
                    if (delay > 0) {
                        try {
                            this.mSyncRoot.wait(delay);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        throw new RuntimeException("Timeout waiting for default display to be initialized. DefaultDisplay=" + this.mLogicalDisplays.get(0) + ", mVirtualDisplayAdapter=" + this.mVirtualDisplayAdapter);
                    }
                }
            }
        }
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int newUserId) {
        int userSerial = getUserManager().getUserSerialNumber(newUserId);
        synchronized (this.mSyncRoot) {
            if (this.mCurrentUserId != newUserId) {
                this.mCurrentUserId = newUserId;
                this.mDisplayPowerController.setBrightnessConfiguration(this.mPersistentDataStore.getBrightnessConfiguration(userSerial));
            }
            this.mDisplayPowerController.onSwitchUser(newUserId);
        }
    }

    public void windowManagerAndInputReady() {
        synchronized (this.mSyncRoot) {
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            scheduleTraversalLocked(false);
        }
    }

    public void pcDisplayChangeService(boolean connected) {
        LocalDisplayAdapter localDisplayAdapter = this.mLocalDisplayAdapter;
        if (localDisplayAdapter != null) {
            localDisplayAdapter.pcDisplayChangeService(connected);
        }
    }

    public void systemReady(boolean safeMode, boolean onlyCore) {
        synchronized (this.mSyncRoot) {
            this.mSafeMode = safeMode;
            this.mOnlyCore = onlyCore;
            this.mSystemReady = true;
            recordTopInsetLocked(this.mLogicalDisplays.get(0));
        }
        LocalDisplayAdapter localDisplayAdapter = this.mLocalDisplayAdapter;
        if (localDisplayAdapter != null) {
            localDisplayAdapter.registerContentObserver(this.mContext, this.mHandler);
        }
        this.mDisplayModeDirector.setListener(new AllowedDisplayModeObserver());
        this.mDisplayModeDirector.start();
        this.mHandler.sendEmptyMessage(2);
        this.mPowerManagerInternal = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Handler getDisplayHandler() {
        return this.mHandler;
    }

    private void loadStableDisplayValuesLocked() {
        Point size = this.mPersistentDataStore.getStableDisplaySize();
        if (size.x <= 0 || size.y <= 0) {
            Resources res = this.mContext.getResources();
            int width = res.getInteger(17694897);
            int height = res.getInteger(17694896);
            if (width > 0 && height > 0) {
                setStableDisplaySizeLocked(width, height);
                return;
            }
            return;
        }
        this.mStableDisplaySize.set(size.x, size.y);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Point getStableDisplaySizeInternal() {
        Point r = new Point();
        synchronized (this.mSyncRoot) {
            if (this.mStableDisplaySize.x > 0 && this.mStableDisplaySize.y > 0) {
                r.set(this.mStableDisplaySize.x, this.mStableDisplaySize.y);
            }
        }
        return r;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerDisplayTransactionListenerInternal(DisplayManagerInternal.DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.add(listener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterDisplayTransactionListenerInternal(DisplayManagerInternal.DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.remove(listener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayInfoOverrideFromWindowManagerInternal(int displayId, DisplayInfo info) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null && (display.setDisplayInfoOverrideFromWindowManagerLocked(info) || this.mIsForceUpdateDisplayInfo)) {
                handleLogicalDisplayChanged(displayId, display);
                scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCutoutInfoForRogInternal(int displayId) {
        synchronized (this.mSyncRoot) {
            DisplayDevice device = this.mDisplayDevices.get(displayId);
            device.updateDesityforRog();
            handleDisplayDeviceChanged(device);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getNonOverrideDisplayInfoInternal(int displayId, DisplayInfo outInfo) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                display.getNonOverrideDisplayInfoLocked(outInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetDisplayDelayImpl() {
        this.mIsDisplayNeedDelaydefault = false;
        this.mIsDisplayRequest = false;
        this.mNeedHandleDisplayMode = false;
        this.mNeedHandleDisplayRequest = false;
        this.mIsForceUpdateDisplayInfo = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDisplayNeedDelay(boolean wakeup, int newDisplaymode) {
        int i = this.mDisplayMode;
        if (newDisplaymode == i) {
            Slog.i(TAG, "isDisplayNeedDelay return.");
            return false;
        }
        if ((newDisplaymode == 3 && i == 2) || (this.mDisplayMode == 3 && newDisplaymode == 2)) {
            Slog.i(TAG, "Mian to Sub must delay");
            wakeup = false;
        }
        if (!wakeup || !HwFoldScreenState.isOutFoldDevice()) {
            Slog.i(TAG, "isDisplayNeedDelay newDisplaymode = " + newDisplaymode);
            if (HwFoldScreenState.isOutFoldDevice()) {
                this.mIsForceUpdateDisplayInfo = true;
            }
            return true;
        }
        Slog.i(TAG, "wakeup no need delay.");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performTraversalNow(SurfaceControl.Transaction t) {
        WindowManagerInternal windowManagerInternal;
        Slog.i(TAG, "performTraversalNow done, mIsDisplayNeedDelaydefault = " + this.mIsDisplayNeedDelaydefault + " mIsDisplayRequest " + this.mIsDisplayRequest);
        performTraversalInternal(t, false);
        if ((this.mIsDisplayNeedDelaydefault || this.mIsDisplayRequest) && (windowManagerInternal = this.mWindowManagerInternal) != null) {
            windowManagerInternal.transactionApply(t);
        }
        resetDisplayDelayImpl();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performTraversalNeedDelay(SurfaceControl.Transaction t, long timeOld) {
        this.mTransaction = t;
        if (this.mIsDisplayNeedDelaydefault || this.mIsDisplayRequest) {
            Slog.i(TAG, "performTraversalNeedDelay repeat!");
        } else if (this.mNeedHandleDisplayMode) {
            this.mIsDisplayNeedDelaydefault = true;
            this.mNeedHandleDisplayMode = false;
            long time = this.mNeedDelayTime;
            Message msg = Message.obtain(this.mHandler, 8);
            this.mHandler.removeMessages(msg.what);
            this.mHandler.sendMessageDelayed(msg, time);
            Slog.i(TAG, "Default performTraversalNeedDelay 1000 ms, or other time:" + time);
        } else if (this.mNeedHandleDisplayRequest) {
            Message msg2 = Message.obtain(this.mHandler, 8);
            this.mHandler.removeMessages(msg2.what);
            this.mHandler.sendMessageDelayed(msg2, timeOld);
            Slog.i(TAG, "performTraversalNeedDelay:time = " + timeOld + " ms.");
            this.mIsDisplayRequest = true;
            this.mNeedHandleDisplayRequest = false;
        } else {
            performTraversalNow(t);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void performTraversalInternal(SurfaceControl.Transaction t, boolean forceTraversal) {
        synchronized (this.mSyncRoot) {
            if (this.mPendingTraversal || forceTraversal) {
                this.mPendingTraversal = false;
                performTraversalLocked(t);
            } else {
                return;
            }
        }
        Iterator<DisplayManagerInternal.DisplayTransactionListener> it = this.mDisplayTransactionListeners.iterator();
        while (it.hasNext()) {
            it.next().onDisplayTransaction(t);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestGlobalDisplayStateInternal(int state, int brightness) {
        IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
        if (iZrHung != null) {
            ZrHungData arg = new ZrHungData();
            arg.putString("addScreenOnInfo", "Power: state=" + state + ", brightness=" + brightness);
            iZrHung.addInfo(arg);
            if (state == 2 && brightness > 0) {
                iZrHung.stop((ZrHungData) null);
            }
        }
        if (state == 0) {
            state = 2;
        }
        if (state == 1) {
            brightness = 0;
        } else if (brightness < 0) {
            brightness = -1;
        }
        synchronized (this.mTempDisplayStateWorkQueue) {
            try {
                synchronized (this.mSyncRoot) {
                    if (this.mGlobalAlpmState == 0) {
                        brightness = 0;
                        Slog.d(TAG, "mGlobalAlpmState == 0(in AOD mode), set brightbess = 0 ");
                    }
                    if (this.mGlobalDisplayState != state || this.mGlobalDisplayBrightness != brightness) {
                        Trace.traceBegin(131072, "requestGlobalDisplayState(" + Display.stateToString(state) + ", brightness=" + brightness + ")");
                        this.mGlobalDisplayState = state;
                        this.mGlobalDisplayBrightness = brightness;
                        applyGlobalDisplayStateLocked(this.mTempDisplayStateWorkQueue);
                    } else {
                        return;
                    }
                }
                for (int i = 0; i < this.mTempDisplayStateWorkQueue.size(); i++) {
                    this.mTempDisplayStateWorkQueue.get(i).run();
                }
                Trace.traceEnd(131072);
                this.mTempDisplayStateWorkQueue.clear();
            } finally {
                this.mTempDisplayStateWorkQueue.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DisplayInfo getDisplayInfoInternal(int displayId, int callingUid) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                DisplayInfo info = display.getDisplayInfoLocked();
                if (info.hasAccess(callingUid) || isUidPresentOnDisplayInternal(callingUid, displayId)) {
                    return info;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int[] getDisplayIdsInternal(int callingUid) {
        int[] displayIds;
        synchronized (this.mSyncRoot) {
            int count = this.mLogicalDisplays.size();
            displayIds = new int[count];
            int n = 0;
            for (int i = 0; i < count; i++) {
                if (this.mLogicalDisplays.valueAt(i).getDisplayInfoLocked().hasAccess(callingUid)) {
                    displayIds[n] = this.mLogicalDisplays.keyAt(i);
                    n++;
                }
            }
            if (n != count) {
                displayIds = Arrays.copyOfRange(displayIds, 0, n);
            }
        }
        return displayIds;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerCallbackInternal(IDisplayManagerCallback callback, int callingPid) {
        synchronized (this.mSyncRoot) {
            if (this.mCallbacks.get(callingPid) == null) {
                CallbackRecord record = new CallbackRecord(callingPid, callback);
                try {
                    callback.asBinder().linkToDeath(record, 0);
                    this.mCallbacks.put(callingPid, record);
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                throw new SecurityException("The calling process has already registered an IDisplayManagerCallback.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCallbackDied(CallbackRecord record) {
        synchronized (this.mSyncRoot) {
            this.mCallbacks.remove(record.mPid);
            stopWifiDisplayScanLocked(record);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = this.mCallbacks.get(callingPid);
            if (record != null) {
                startWifiDisplayScanLocked(record);
            } else {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
        }
    }

    private void startWifiDisplayScanLocked(CallbackRecord record) {
        WifiDisplayAdapter wifiDisplayAdapter;
        if (!record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = true;
            int i = this.mWifiDisplayScanRequestCount;
            this.mWifiDisplayScanRequestCount = i + 1;
            if (i == 0 && (wifiDisplayAdapter = this.mWifiDisplayAdapter) != null) {
                wifiDisplayAdapter.requestStartScanLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = this.mCallbacks.get(callingPid);
            if (record != null) {
                stopWifiDisplayScanLocked(record);
            } else {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
        }
    }

    private void stopWifiDisplayScanLocked(CallbackRecord record) {
        if (record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = false;
            int i = this.mWifiDisplayScanRequestCount - 1;
            this.mWifiDisplayScanRequestCount = i;
            if (i == 0) {
                WifiDisplayAdapter wifiDisplayAdapter = this.mWifiDisplayAdapter;
                if (wifiDisplayAdapter != null) {
                    wifiDisplayAdapter.requestStopScanLocked();
                }
            } else if (this.mWifiDisplayScanRequestCount < 0) {
                Slog.wtf(TAG, "mWifiDisplayScanRequestCount became negative: " + this.mWifiDisplayScanRequestCount);
                this.mWifiDisplayScanRequestCount = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestConnectLocked(address);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pauseWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestPauseLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resumeWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestResumeLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnectWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestDisconnectLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void renameWifiDisplayInternal(String address, String alias) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestRenameLocked(address, alias);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void forgetWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestForgetLocked(address);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiDisplayStatus getWifiDisplayStatusInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                return this.mWifiDisplayAdapter.getWifiDisplayStatusLocked();
            }
            return new WifiDisplayStatus();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestColorModeInternal(int displayId, int colorMode) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (!(display == null || display.getRequestedColorModeLocked() == colorMode)) {
                display.setRequestedColorModeLocked(colorMode);
                scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSaturationLevelInternal(float level) {
        if (level < 0.0f || level > 1.0f) {
            throw new IllegalArgumentException("Saturation level must be between 0 and 1");
        }
        float[] matrix = level == 1.0f ? null : computeSaturationMatrix(level);
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        if (dtm != null) {
            dtm.setColorMatrix(150, matrix);
        }
    }

    private static float[] computeSaturationMatrix(float saturation) {
        float desaturation = 1.0f - saturation;
        float[] luminance = {0.231f * desaturation, 0.715f * desaturation, 0.072f * desaturation};
        return new float[]{luminance[0] + saturation, luminance[0], luminance[0], 0.0f, luminance[1], luminance[1] + saturation, luminance[1], 0.0f, luminance[2], luminance[2], luminance[2] + saturation, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int createVirtualDisplayInternal(IVirtualDisplayCallback callback, IMediaProjection projection, int callingUid, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                Slog.w(TAG, "Rejecting request to create private virtual display because the virtual display adapter is not available.");
                return -1;
            }
            DisplayDevice device = this.mVirtualDisplayAdapter.createVirtualDisplayLocked(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags, uniqueId);
            if (device == null) {
                return -1;
            }
            handleDisplayDeviceAddedLocked(device);
            LogicalDisplay display = findLogicalDisplayForDeviceLocked(device);
            if (display != null) {
                return display.getDisplayIdLocked();
            }
            Slog.w(TAG, "Rejecting request to create virtual display because the logical display was not created.");
            this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(callback.asBinder());
            handleDisplayDeviceRemovedLocked(device);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resizeVirtualDisplayInternal(IBinder appToken, int width, int height, int densityDpi) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.resizeVirtualDisplayLocked(appToken, width, height, densityDpi);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVirtualDisplaySurfaceInternal(IBinder appToken, Surface surface) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.setVirtualDisplaySurfaceLocked(appToken, surface);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseVirtualDisplayInternal(IBinder appToken) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                DisplayDevice device = this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(appToken);
                if (device != null) {
                    handleDisplayDeviceRemovedLocked(device);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVirtualDisplayStateInternal(IBinder appToken, boolean isOn) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.setVirtualDisplayStateLocked(appToken, isOn);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerDefaultDisplayAdapters() {
        synchronized (this.mSyncRoot) {
            this.mLocalDisplayAdapter = new LocalDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener);
            registerDisplayAdapterLocked(this.mLocalDisplayAdapter);
            this.mVirtualDisplayAdapter = this.mInjector.getVirtualDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener);
            if (this.mVirtualDisplayAdapter != null) {
                registerDisplayAdapterLocked(this.mVirtualDisplayAdapter);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerAdditionalDisplayAdapters() {
        synchronized (this.mSyncRoot) {
            if (shouldRegisterNonEssentialDisplayAdaptersLocked()) {
                registerOverlayDisplayAdapterLocked();
                registerWifiDisplayAdapterLocked();
            }
            if (this.mHwDMSEx != null) {
                this.mHwDMSEx.registerHwVrDisplayAdapterIfNeedLocked(this.mDisplayAdapters, this.mHandler, this.mDisplayAdapterListener, this.mUiHandler);
            }
        }
    }

    private void registerOverlayDisplayAdapterLocked() {
        registerDisplayAdapterLocked(new OverlayDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mUiHandler));
    }

    private void registerWifiDisplayAdapterLocked() {
        if (this.mContext.getResources().getBoolean(17891454) || SystemProperties.getInt(FORCE_WIFI_DISPLAY_ENABLE, -1) == 1) {
            this.mWifiDisplayAdapter = new WifiDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mPersistentDataStore);
            registerDisplayAdapterLocked(this.mWifiDisplayAdapter);
        }
    }

    private boolean shouldRegisterNonEssentialDisplayAdaptersLocked() {
        return !this.mSafeMode && !this.mOnlyCore;
    }

    private void registerDisplayAdapterLocked(DisplayAdapter adapter) {
        this.mDisplayAdapters.add(adapter);
        adapter.registerLocked();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        if ("com.hpplay.happycast".equals(info.ownerPackageName)) {
            info.densityDpi = 240;
            info.xDpi = 240.0f;
            info.yDpi = 240.0f;
        }
        Slog.i(TAG, "Display device added: " + info);
        device.mDebugLastLoggedDeviceInfo = info;
        this.mDisplayDevices.add(device);
        LogicalDisplay display = addLogicalDisplayLocked(device);
        Runnable work = updateDisplayStateLocked(device);
        if (display != null) {
            DisplayInfoExt displayInfoExt = new DisplayInfoExt();
            displayInfoExt.setDisplayInfo(display.getDisplayInfoLocked());
            this.mVrMananger.setVrDisplayInfo(displayInfoExt, display.getDisplayIdLocked());
        }
        if (work != null) {
            work.run();
        }
        scheduleTraversalLocked(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDisplayDeviceChanged(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
            if (!this.mDisplayDevices.contains(device)) {
                Slog.w(TAG, "Attempted to change non-existent display device: " + info);
                return;
            }
            int diff = device.mDebugLastLoggedDeviceInfo.diff(info);
            if (diff == 1) {
                Slog.i(TAG, "Display device changed state: \"" + info.name + "\", " + Display.stateToString(info.state));
            } else if (diff != 0) {
                Slog.i(TAG, "Display device changed: " + info);
            }
            if ((diff & 4) != 0) {
                try {
                    this.mPersistentDataStore.setColorMode(device, info.colorMode);
                } finally {
                    this.mPersistentDataStore.saveIfNeeded();
                }
            }
            device.mDebugLastLoggedDeviceInfo = info;
            device.applyPendingDisplayDeviceInfoChangesLocked();
            if (updateLogicalDisplaysLocked()) {
                scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDisplayDeviceRemoved(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            handleDisplayDeviceRemovedLocked(device);
        }
    }

    private void handleDisplayDeviceRemovedLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (!this.mDisplayDevices.remove(device)) {
            Slog.w(TAG, "Attempted to remove non-existent display device: " + info);
            return;
        }
        Slog.i(TAG, "Display device removed: " + info);
        device.mDebugLastLoggedDeviceInfo = info;
        updateLogicalDisplaysLocked();
        scheduleTraversalLocked(false);
    }

    private void handleLogicalDisplayChanged(int displayId, LogicalDisplay display) {
        if (displayId == 0) {
            recordTopInsetLocked(display);
        }
        sendDisplayEventLocked(displayId, 2);
    }

    private void applyGlobalDisplayStateLocked(List<Runnable> workQueue) {
        Runnable runnable;
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i++) {
            DisplayDevice device = this.mDisplayDevices.get(i);
            if ((!HwPCUtils.isPcCastModeInServer() || device.getDisplayDeviceInfoLocked().type != 2 || this.mPowerManagerInternal.shouldUpdatePCScreenState()) && (runnable = updateDisplayStateLocked(device)) != null) {
                workQueue.add(runnable);
            }
        }
    }

    private Runnable updateDisplayStateLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (this.mVrMananger.isVRDeviceConnected() && info.type == 1) {
            Slog.w(TAG, "Ignoring updateDisplayState to " + this.mGlobalDisplayState + " on build-in display in VR mode");
            return null;
        } else if ((info.flags & 32) == 0) {
            return device.requestDisplayStateLocked(this.mGlobalDisplayState, this.mGlobalDisplayBrightness);
        } else {
            return null;
        }
    }

    private LogicalDisplay addLogicalDisplayLocked(DisplayDevice device) {
        int displayId;
        DisplayDeviceInfo deviceInfo = device.getDisplayDeviceInfoLocked();
        boolean isDefault = (deviceInfo.flags & 1) != 0;
        if (isDefault && this.mLogicalDisplays.get(0) != null) {
            Slog.w(TAG, "Ignoring attempt to add a second default display: " + deviceInfo);
            isDefault = false;
        }
        if (isDefault || !this.mSingleDisplayDemoMode) {
            if (this.mVrMananger.isVRDeviceConnected()) {
                displayId = this.mVrMananger.assignVrDisplayIdIfNeeded(isDefault, assignDisplayIdLocked(isDefault), deviceInfo.name);
            } else {
                displayId = assignDisplayIdLocked(isDefault);
            }
            LogicalDisplay display = new LogicalDisplay(displayId, assignLayerStackLocked(displayId), device);
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                Slog.w(TAG, "Ignoring display device because the logical display created from it was not considered valid: " + deviceInfo);
                return null;
            }
            configureColorModeLocked(display, device);
            if (isDefault) {
                recordStableDisplayStatsIfNeededLocked(display);
                recordTopInsetLocked(display);
            }
            this.mLogicalDisplays.put(displayId, display);
            if (isDefault) {
                this.mSyncRoot.notifyAll();
            }
            sendDisplayEventLocked(displayId, 1);
            return display;
        }
        Slog.i(TAG, "Not creating a logical display for a secondary display  because single display demo mode is enabled: " + deviceInfo);
        return null;
    }

    private int assignDisplayIdLocked(boolean isDefault) {
        if (isDefault) {
            return 0;
        }
        int i = this.mNextNonDefaultDisplayId;
        this.mNextNonDefaultDisplayId = i + 1;
        return i;
    }

    private int assignLayerStackLocked(int displayId) {
        return displayId;
    }

    private void configureColorModeLocked(LogicalDisplay display, DisplayDevice device) {
        if (display.getPrimaryDisplayDeviceLocked() == device) {
            int colorMode = this.mPersistentDataStore.getColorMode(device);
            if (colorMode == -1) {
                if ((device.getDisplayDeviceInfoLocked().flags & 1) != 0) {
                    colorMode = this.mDefaultDisplayDefaultColorMode;
                } else {
                    colorMode = 0;
                }
            }
            display.setRequestedColorModeLocked(colorMode);
        }
    }

    private void recordStableDisplayStatsIfNeededLocked(LogicalDisplay d) {
        if (this.mStableDisplaySize.x <= 0 && this.mStableDisplaySize.y <= 0) {
            DisplayInfo info = d.getDisplayInfoLocked();
            setStableDisplaySizeLocked(info.getNaturalWidth(), info.getNaturalHeight());
        }
    }

    private void recordTopInsetLocked(LogicalDisplay d) {
        int topInset;
        if (this.mSystemReady && d != null && (topInset = d.getInsets().top) != this.mDefaultDisplayTopInset) {
            this.mDefaultDisplayTopInset = topInset;
            SystemProperties.set(PROP_DEFAULT_DISPLAY_TOP_INSET, Integer.toString(topInset));
        }
    }

    private void setStableDisplaySizeLocked(int width, int height) {
        this.mStableDisplaySize = new Point(width, height);
        try {
            this.mPersistentDataStore.setStableDisplaySize(this.mStableDisplaySize);
        } finally {
            this.mPersistentDataStore.saveIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Curve getMinimumBrightnessCurveInternal() {
        return this.mMinimumBrightnessCurve;
    }

    /* access modifiers changed from: package-private */
    public int getPreferredWideGamutColorSpaceIdInternal() {
        return this.mWideColorSpace.getId();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBrightnessConfigurationForUserInternal(BrightnessConfiguration c, int userId, String packageName) {
        validateBrightnessConfiguration(c);
        int userSerial = getUserManager().getUserSerialNumber(userId);
        synchronized (this.mSyncRoot) {
            try {
                this.mPersistentDataStore.setBrightnessConfigurationForUser(c, userSerial, packageName);
                this.mPersistentDataStore.saveIfNeeded();
                if (userId == this.mCurrentUserId) {
                    this.mDisplayPowerController.setBrightnessConfiguration(c);
                }
            } catch (Throwable th) {
                this.mPersistentDataStore.saveIfNeeded();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void validateBrightnessConfiguration(BrightnessConfiguration config) {
        if (config != null && isBrightnessConfigurationTooDark(config)) {
            throw new IllegalArgumentException("brightness curve is too dark");
        }
    }

    private boolean isBrightnessConfigurationTooDark(BrightnessConfiguration config) {
        Pair<float[], float[]> curve = config.getCurve();
        float[] lux = (float[]) curve.first;
        float[] nits = (float[]) curve.second;
        for (int i = 0; i < lux.length; i++) {
            if (nits[i] < this.mMinimumBrightnessSpline.interpolate(lux[i])) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadBrightnessConfiguration() {
        synchronized (this.mSyncRoot) {
            this.mDisplayPowerController.setBrightnessConfiguration(this.mPersistentDataStore.getBrightnessConfiguration(getUserManager().getUserSerialNumber(this.mCurrentUserId)));
        }
    }

    private boolean updateLogicalDisplaysLocked() {
        boolean changed = false;
        int displayId = this.mLogicalDisplays.size();
        while (true) {
            int i = displayId - 1;
            if (displayId <= 0) {
                return changed;
            }
            int displayId2 = this.mLogicalDisplays.keyAt(i);
            LogicalDisplay display = this.mLogicalDisplays.valueAt(i);
            this.mTempDisplayInfo.copyFrom(display.getDisplayInfoLocked());
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                this.mLogicalDisplays.removeAt(i);
                sendDisplayEventLocked(displayId2, 3);
                changed = true;
            } else if (!this.mTempDisplayInfo.equals(display.getDisplayInfoLocked())) {
                handleLogicalDisplayChanged(displayId2, display);
                changed = true;
            }
            displayId = i;
        }
    }

    private void performTraversalLocked(SurfaceControl.Transaction t) {
        clearViewportsLocked();
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i++) {
            DisplayDevice device = this.mDisplayDevices.get(i);
            configureDisplayLocked(t, device);
            device.performTraversalLocked(t);
        }
        if (this.mInputManagerInternal != null) {
            this.mHandler.sendEmptyMessage(5);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayPropertiesInternal(int displayId, boolean hasContent, float requestedRefreshRate, int requestedModeId, boolean inTraversal) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                if (display.hasContentLocked() != hasContent) {
                    display.setHasContentLocked(hasContent);
                    scheduleTraversalLocked(inTraversal);
                }
                if (requestedModeId == 0 && requestedRefreshRate != 0.0f) {
                    requestedModeId = display.getDisplayInfoLocked().findDefaultModeByRefreshRate(requestedRefreshRate);
                }
                this.mDisplayModeDirector.getAppRequestObserver().setAppRequestedMode(displayId, requestedModeId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayOffsetsInternal(int displayId, int x, int y) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                if (!(display.getDisplayOffsetXLocked() == x && display.getDisplayOffsetYLocked() == y)) {
                    display.setDisplayOffsetsLocked(x, y);
                    scheduleTraversalLocked(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayScalingDisabledInternal(int displayId, boolean disable) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                if (display.isDisplayScalingDisabled() != disable) {
                    display.setDisplayScalingDisabledLocked(disable);
                    scheduleTraversalLocked(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayAccessUIDsInternal(SparseArray<IntArray> newDisplayAccessUIDs) {
        synchronized (this.mSyncRoot) {
            this.mDisplayAccessUIDs.clear();
            for (int i = newDisplayAccessUIDs.size() - 1; i >= 0; i--) {
                this.mDisplayAccessUIDs.append(newDisplayAccessUIDs.keyAt(i), newDisplayAccessUIDs.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isUidPresentOnDisplayInternal(int uid, int displayId) {
        synchronized (this.mSyncRoot) {
            boolean z = true;
            if (this.mHwDMSEx.checkPermissionForHwMultiDisplay(uid)) {
                return true;
            }
            IntArray displayUIDs = this.mDisplayAccessUIDs.get(displayId);
            if (displayUIDs == null || displayUIDs.indexOf(uid) == -1) {
                z = false;
            }
            return z;
        }
    }

    private IBinder getDisplayToken(int displayId) {
        DisplayDevice device;
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display == null || (device = display.getPrimaryDisplayDeviceLocked()) == null) {
                return null;
            }
            return device.getDisplayTokenLocked();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SurfaceControl.ScreenshotGraphicBuffer screenshotInternal(int displayId) {
        synchronized (this.mSyncRoot) {
            IBinder token = getDisplayToken(displayId);
            if (token == null) {
                return null;
            }
            LogicalDisplay logicalDisplay = this.mLogicalDisplays.get(displayId);
            if (logicalDisplay == null) {
                return null;
            }
            DisplayInfo displayInfo = logicalDisplay.getDisplayInfoLocked();
            return SurfaceControl.screenshotToBufferWithSecureLayersUnsafe(token, new Rect(), displayInfo.getNaturalWidth(), displayInfo.getNaturalHeight(), false, 0);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public DisplayedContentSamplingAttributes getDisplayedContentSamplingAttributesInternal(int displayId) {
        IBinder token = getDisplayToken(displayId);
        if (token == null) {
            return null;
        }
        return SurfaceControl.getDisplayedContentSamplingAttributes(token);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean setDisplayedContentSamplingEnabledInternal(int displayId, boolean enable, int componentMask, int maxFrames) {
        IBinder token = getDisplayToken(displayId);
        if (token == null) {
            return false;
        }
        return SurfaceControl.setDisplayedContentSamplingEnabled(token, enable, componentMask, maxFrames);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public DisplayedContentSample getDisplayedContentSampleInternal(int displayId, long maxFrames, long timestamp) {
        IBinder token = getDisplayToken(displayId);
        if (token == null) {
            return null;
        }
        return SurfaceControl.getDisplayedContentSample(token, maxFrames, timestamp);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAllowedDisplayModesChangedInternal() {
        boolean changed = false;
        synchronized (this.mSyncRoot) {
            int count = this.mLogicalDisplays.size();
            for (int i = 0; i < count; i++) {
                LogicalDisplay display = this.mLogicalDisplays.valueAt(i);
                int[] allowedModes = this.mDisplayModeDirector.getAllowedModes(this.mLogicalDisplays.keyAt(i));
                if (!Arrays.equals(allowedModes, display.getAllowedDisplayModesLocked())) {
                    display.setAllowedDisplayModesLocked(allowedModes);
                    changed = true;
                }
            }
            if (changed) {
                scheduleTraversalLocked(false);
            }
        }
    }

    private void clearViewportsLocked() {
        this.mViewports.clear();
    }

    private void configureDisplayLocked(SurfaceControl.Transaction t, DisplayDevice device) {
        IHwDisplayManagerServiceEx iHwDisplayManagerServiceEx;
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        boolean z = false;
        boolean ownContent = (info.flags & 128) != 0;
        LogicalDisplay display = findLogicalDisplayForDeviceLocked(device);
        if (!ownContent) {
            if (display != null && !display.hasContentLocked()) {
                int displayID = display.getDisplayIdLocked();
                boolean isPCID = HwPCUtils.isValidExtDisplayId(displayID) && HwPCUtils.isPcCastModeInServerEarly();
                boolean isVrDisplayId = HwFrameworkFactory.getVRSystemServiceManager().isVrCaredDisplay(displayID);
                if (!isPCID && !isVrDisplayId) {
                    Slog.i(TAG, "do not mirror the default display content in pc or vr mode");
                    display = null;
                }
            }
            if (display == null) {
                if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServerEarly()) {
                    display = this.mLogicalDisplays.get(0);
                } else {
                    int displayPC = HwPCUtils.getPCDisplayID();
                    display = this.mLogicalDisplays.get(displayPC);
                    HwPCUtils.log(TAG, "mLogicalDisplays displayPC " + displayPC + " display " + display);
                }
            }
        }
        String displayName = info.name;
        if (this.mVrMananger.isVRDeviceConnected()) {
            IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
            StringBuilder sb = new StringBuilder();
            sb.append(displayName);
            IVRSystemServiceManager iVRSystemServiceManager2 = this.mVrMananger;
            sb.append("-Src");
            if (iVRSystemServiceManager.isVrVirtualDisplay(sb.toString()) && (iHwDisplayManagerServiceEx = this.mHwDMSEx) != null) {
                display = iHwDisplayManagerServiceEx.getVrVirtualDisplayIfNeed(this.mLogicalDisplays, displayName, display);
            }
        }
        if (display == null) {
            Slog.w(TAG, "Missing logical display to use for physical display device: " + device.getDisplayDeviceInfoLocked());
            return;
        }
        if (info.state == 1) {
            z = true;
        }
        display.configureDisplayLocked(t, device, z);
        DisplayViewport internalViewport = getInternalViewportLocked();
        if ((1 & info.flags) != 0) {
            populateViewportLocked(internalViewport, display, device);
        }
        DisplayViewport externalViewport = getExternalViewportLocked();
        setExternalTouchViewport(device, info, display, externalViewport);
        if (info.touch == 2 && !HwPCUtils.isValidExtDisplayId(externalViewport.displayId)) {
            populateViewportLocked(externalViewport, display, device);
        } else if (!externalViewport.valid) {
            externalViewport.copyFrom(internalViewport);
            externalViewport.type = 2;
        }
        if (info.touch == 3 && !TextUtils.isEmpty(info.uniqueId)) {
            populateViewportLocked(getVirtualViewportLocked(info.uniqueId), display, device);
        }
    }

    private void setExternalTouchViewport(DisplayDevice device, DisplayDeviceInfo info, LogicalDisplay display, DisplayViewport externalViewport) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return;
        }
        if (HwPCUtils.isValidExtDisplayId(display.getDisplayIdLocked())) {
            try {
                if (!HwPCUtils.isPcCastMode()) {
                    return;
                }
                if (!HwPCUtils.enabledInPad()) {
                    populateViewportLocked(externalViewport, display, device);
                    externalViewport.logicalFrame.set(0, 0, info.width, info.height);
                    externalViewport.deviceHeight = info.height;
                    externalViewport.deviceWidth = info.width;
                    externalViewport.displayId = display.getDisplayIdLocked();
                    externalViewport.valid = true;
                } else if ("HUAWEI PAD PC Display".equals(info.name)) {
                    populateViewportLocked(externalViewport, display, device);
                    externalViewport.logicalFrame.set(0, 0, info.height, info.width);
                    externalViewport.deviceHeight = info.width;
                    externalViewport.deviceWidth = info.height;
                    externalViewport.displayId = display.getDisplayIdLocked();
                    externalViewport.valid = true;
                } else {
                    populateViewportLocked(externalViewport, display, device);
                    externalViewport.logicalFrame.set(0, 0, getInternalViewportLocked().deviceWidth, getInternalViewportLocked().deviceHeight);
                    externalViewport.deviceHeight = getInternalViewportLocked().deviceHeight;
                    externalViewport.deviceWidth = getInternalViewportLocked().deviceWidth;
                    externalViewport.displayId = display.getDisplayIdLocked();
                    externalViewport.valid = true;
                }
            } catch (Exception e) {
                Slog.w(TAG, "when set external touch view port", e);
            }
        } else if (HwPCUtils.isValidExtDisplayId(externalViewport.displayId)) {
            externalViewport.valid = true;
        }
    }

    private DisplayViewport getVirtualViewportLocked(String uniqueId) {
        int count = this.mViewports.size();
        for (int i = 0; i < count; i++) {
            DisplayViewport viewport = this.mViewports.get(i);
            if (uniqueId.equals(viewport.uniqueId)) {
                if (viewport.type == 3) {
                    return viewport;
                }
                Slog.wtf(TAG, "Found a viewport with uniqueId '" + uniqueId + "' but it has type " + DisplayViewport.typeToString(viewport.type) + " (expected VIRTUAL)");
            }
        }
        DisplayViewport viewport2 = new DisplayViewport();
        viewport2.uniqueId = uniqueId;
        viewport2.type = 3;
        this.mViewports.add(viewport2);
        return viewport2;
    }

    private DisplayViewport getInternalViewportLocked() {
        return getViewportByTypeLocked(1);
    }

    private DisplayViewport getExternalViewportLocked() {
        return getViewportByTypeLocked(2);
    }

    private DisplayViewport getViewportByTypeLocked(int viewportType) {
        if (viewportType == 1 || viewportType == 2) {
            int count = this.mViewports.size();
            for (int i = 0; i < count; i++) {
                DisplayViewport viewport = this.mViewports.get(i);
                if (viewport.type == viewportType) {
                    return viewport;
                }
            }
            DisplayViewport viewport2 = new DisplayViewport();
            viewport2.type = viewportType;
            this.mViewports.add(viewport2);
            return viewport2;
        }
        Slog.wtf(TAG, "Cannot call getViewportByTypeLocked for type " + DisplayViewport.typeToString(viewportType));
        return null;
    }

    private static void populateViewportLocked(DisplayViewport viewport, LogicalDisplay display, DisplayDevice device) {
        viewport.valid = true;
        viewport.displayId = display.getDisplayIdLocked();
        device.populateViewportLocked(viewport);
    }

    private LogicalDisplay findLogicalDisplayForDeviceLocked(DisplayDevice device) {
        int count = this.mLogicalDisplays.size();
        for (int i = 0; i < count; i++) {
            LogicalDisplay display = this.mLogicalDisplays.valueAt(i);
            if (display.getPrimaryDisplayDeviceLocked() == device) {
                return display;
            }
        }
        return null;
    }

    private void sendDisplayEventLocked(int displayId, int event) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, displayId, event));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleTraversalLocked(boolean inTraversal) {
        if (!this.mPendingTraversal && this.mWindowManagerInternal != null) {
            this.mPendingTraversal = true;
            if (!inTraversal) {
                this.mHandler.sendEmptyMessage(4);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deliverDisplayEvent(int displayId, int event) {
        int count;
        synchronized (this.mSyncRoot) {
            count = this.mCallbacks.size();
            this.mTempCallbacks.clear();
            for (int i = 0; i < count; i++) {
                this.mTempCallbacks.add(this.mCallbacks.valueAt(i));
            }
        }
        for (int i2 = 0; i2 < count; i2++) {
            this.mTempCallbacks.get(i2).notifyDisplayEventAsync(displayId, event);
        }
        this.mTempCallbacks.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IMediaProjectionManager getProjectionService() {
        if (this.mProjectionService == null) {
            this.mProjectionService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        }
        return this.mProjectionService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserManager getUserManager() {
        return (UserManager) this.mContext.getSystemService(UserManager.class);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(PrintWriter pw) {
        pw.println("DISPLAY MANAGER (dumpsys display)");
        synchronized (this.mSyncRoot) {
            pw.println("  mOnlyCode=" + this.mOnlyCore);
            pw.println("  mSafeMode=" + this.mSafeMode);
            pw.println("  mPendingTraversal=" + this.mPendingTraversal);
            pw.println("  mGlobalDisplayState=" + Display.stateToString(this.mGlobalDisplayState));
            pw.println("  mNextNonDefaultDisplayId=" + this.mNextNonDefaultDisplayId);
            pw.println("  mViewports=" + this.mViewports);
            pw.println("  mDefaultDisplayDefaultColorMode=" + this.mDefaultDisplayDefaultColorMode);
            pw.println("  mSingleDisplayDemoMode=" + this.mSingleDisplayDemoMode);
            pw.println("  mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
            pw.println("  mStableDisplaySize=" + this.mStableDisplaySize);
            pw.println("  mMinimumBrightnessCurve=" + this.mMinimumBrightnessCurve);
            IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "    ");
            ipw.increaseIndent();
            pw.println();
            pw.println("Display Adapters: size=" + this.mDisplayAdapters.size());
            Iterator<DisplayAdapter> it = this.mDisplayAdapters.iterator();
            while (it.hasNext()) {
                DisplayAdapter adapter = it.next();
                pw.println("  " + adapter.getName());
                adapter.dumpLocked(ipw);
            }
            pw.println();
            pw.println("Display Devices: size=" + this.mDisplayDevices.size());
            Iterator<DisplayDevice> it2 = this.mDisplayDevices.iterator();
            while (it2.hasNext()) {
                DisplayDevice device = it2.next();
                pw.println("  " + device.getDisplayDeviceInfoLocked());
                device.dumpLocked(ipw);
            }
            int logicalDisplayCount = this.mLogicalDisplays.size();
            pw.println();
            pw.println("Logical Displays: size=" + logicalDisplayCount);
            for (int i = 0; i < logicalDisplayCount; i++) {
                int displayId = this.mLogicalDisplays.keyAt(i);
                pw.println("  Display " + displayId + ":");
                this.mLogicalDisplays.valueAt(i).dumpLocked(ipw);
            }
            pw.println();
            this.mDisplayModeDirector.dump(pw);
            int callbackCount = this.mCallbacks.size();
            pw.println();
            pw.println("Callbacks: size=" + callbackCount);
            for (int i2 = 0; i2 < callbackCount; i2++) {
                CallbackRecord callback = this.mCallbacks.valueAt(i2);
                pw.println("  " + i2 + ": mPid=" + callback.mPid + ", mWifiDisplayScanRequested=" + callback.mWifiDisplayScanRequested);
            }
            if (this.mDisplayPowerController != null) {
                this.mDisplayPowerController.dump(pw);
            }
            pw.println();
            this.mPersistentDataStore.dump(pw);
        }
    }

    private static float[] getFloatArray(TypedArray array) {
        int length = array.length();
        float[] floatArray = new float[length];
        for (int i = 0; i < length; i++) {
            floatArray[i] = array.getFloat(i, Float.NaN);
        }
        array.recycle();
        return floatArray;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class Injector {
        Injector() {
        }

        /* access modifiers changed from: package-private */
        public VirtualDisplayAdapter getVirtualDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener displayAdapterListener) {
            return new VirtualDisplayAdapter(syncRoot, context, handler, displayAdapterListener);
        }

        /* access modifiers changed from: package-private */
        public long getDefaultDisplayDelayTimeout() {
            return 10000;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public DisplayDeviceInfo getDisplayDeviceInfoInternal(int displayId) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display == null) {
                return null;
            }
            return display.getPrimaryDisplayDeviceLocked().getDisplayDeviceInfoLocked();
        }
    }

    /* access modifiers changed from: private */
    public final class DisplayManagerHandler extends Handler {
        public DisplayManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean changed;
            switch (msg.what) {
                case 1:
                    DisplayManagerService.this.registerDefaultDisplayAdapters();
                    return;
                case 2:
                    DisplayManagerService.this.registerAdditionalDisplayAdapters();
                    return;
                case 3:
                    DisplayManagerService.this.deliverDisplayEvent(msg.arg1, msg.arg2);
                    return;
                case 4:
                    DisplayManagerService.this.mWindowManagerInternal.requestTraversalFromDisplayManager();
                    return;
                case 5:
                    synchronized (DisplayManagerService.this.mSyncRoot) {
                        changed = !DisplayManagerService.this.mTempViewports.equals(DisplayManagerService.this.mViewports);
                        if (changed) {
                            DisplayManagerService.this.mTempViewports.clear();
                            Iterator it = DisplayManagerService.this.mViewports.iterator();
                            while (it.hasNext()) {
                                DisplayManagerService.this.mTempViewports.add(((DisplayViewport) it.next()).makeCopy());
                            }
                        }
                    }
                    if (changed) {
                        DisplayManagerService.this.mInputManagerInternal.setDisplayViewports(DisplayManagerService.this.mTempViewports);
                        return;
                    }
                    return;
                case 6:
                    DisplayManagerService.this.loadBrightnessConfiguration();
                    return;
                case 7:
                default:
                    return;
                case 8:
                    if (DisplayManagerService.this.mTransaction == null) {
                        Slog.i(DisplayManagerService.TAG, "setDisplayModeChangeDelay Transaction is null, return");
                        return;
                    }
                    DisplayManagerService displayManagerService = DisplayManagerService.this;
                    displayManagerService.performTraversalNow(displayManagerService.mTransaction);
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class DisplayAdapterListener implements DisplayAdapter.Listener {
        private DisplayAdapterListener() {
        }

        @Override // com.android.server.display.DisplayAdapter.Listener
        public void onDisplayDeviceEvent(DisplayDevice device, int event) {
            if (event == 1) {
                DisplayManagerService.this.handleDisplayDeviceAdded(device);
            } else if (event == 2) {
                DisplayManagerService.this.handleDisplayDeviceChanged(device);
            } else if (event == 3) {
                DisplayManagerService.this.handleDisplayDeviceRemoved(device);
            }
        }

        @Override // com.android.server.display.DisplayAdapter.Listener
        public void onTraversalRequested() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class CallbackRecord implements IBinder.DeathRecipient {
        private final IDisplayManagerCallback mCallback;
        public final int mPid;
        public boolean mWifiDisplayScanRequested;

        public CallbackRecord(int pid, IDisplayManagerCallback callback) {
            this.mPid = pid;
            this.mCallback = callback;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            DisplayManagerService.this.onCallbackDied(this);
        }

        public void notifyDisplayEventAsync(int displayId, int event) {
            try {
                this.mCallback.onDisplayEvent(displayId, event);
            } catch (RemoteException ex) {
                Slog.w(DisplayManagerService.TAG, "Failed to notify process " + this.mPid + " that displays changed, assuming it died.", ex);
                binderDied();
            }
        }
    }

    @VisibleForTesting
    final class BinderService extends IDisplayManager.Stub {
        BinderService() {
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getDisplayInfoInternal(displayId, callingUid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public int[] getDisplayIds() {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getDisplayIdsInternal(callingUid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isUidPresentOnDisplay(int uid, int displayId) {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.isUidPresentOnDisplayInternal(uid, displayId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public Point getStableDisplaySize() {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getStableDisplaySizeInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void registerCallback(IDisplayManagerCallback callback) {
            if (callback != null) {
                int callingPid = Binder.getCallingPid();
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.registerCallbackInternal(callback, callingPid);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("listener must not be null");
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
            if (address != null) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to connect to a wifi display");
                long token = Binder.clearCallingIdentity();
                try {
                    if (DisplayManagerService.this.mHwDMSEx != null) {
                        DisplayManagerService.this.mHwDMSEx.setHwWifiDisplayParameters(null);
                    }
                    DisplayManagerService.this.connectWifiDisplayInternal(address);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("address must not be null");
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
            if (address != null) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to rename to a wifi display");
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.renameWifiDisplayInternal(address, alias);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("address must not be null");
            }
        }

        public void forgetWifiDisplay(String address) {
            if (address != null) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to forget to a wifi display");
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.forgetWifiDisplayInternal(address);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("address must not be null");
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
                return DisplayManagerService.this.getWifiDisplayStatusInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void requestColorMode(int displayId, int colorMode) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_COLOR_MODE", "Permission required to change the display color mode");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.requestColorModeInternal(displayId, colorMode);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setSaturationLevelEx(float level) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_DISPLAY_SATURATION", "Permission required to set display saturation level");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.setSaturationLevelInternal(level);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public int createVirtualDisplay(IVirtualDisplayCallback callback, IMediaProjection projection, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) {
            int flags2;
            int flags3;
            int flags4;
            Throwable th;
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            if (!validatePackageName(callingUid, packageName)) {
                throw new SecurityException("packageName must match the calling uid");
            } else if (callback == null) {
                throw new IllegalArgumentException("appToken must not be null");
            } else if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("name must be non-null and non-empty");
            } else if (width <= 0 || height <= 0 || densityDpi <= 0) {
                throw new IllegalArgumentException("width, height, and densityDpi must be greater than 0");
            } else if (surface == null || !surface.isSingleBuffered()) {
                if ((flags & 1) != 0) {
                    flags2 = flags | 16;
                    if ((flags2 & 32) != 0) {
                        throw new IllegalArgumentException("Public display must not be marked as SHOW_WHEN_LOCKED_INSECURE");
                    }
                } else {
                    flags2 = flags;
                }
                if ((flags2 & 8) != 0) {
                    flags3 = flags2 & -17;
                } else {
                    flags3 = flags2;
                }
                if (projection != null) {
                    try {
                        if (DisplayManagerService.this.getProjectionService().isValidMediaProjection(projection)) {
                            flags4 = projection.applyVirtualDisplayFlags(flags3);
                        } else {
                            throw new SecurityException("Invalid media projection");
                        }
                    } catch (RemoteException e) {
                        throw new SecurityException("unable to validate media projection or flags");
                    }
                } else {
                    flags4 = flags3;
                }
                if (callingUid != 1000 && (flags4 & 16) != 0 && !canProjectVideo(projection)) {
                    throw new SecurityException("Requires CAPTURE_VIDEO_OUTPUT or CAPTURE_SECURE_VIDEO_OUTPUT permission, or an appropriate MediaProjection token in order to create a screen sharing virtual display.");
                } else if (callingUid == 1000 || (flags4 & 4) == 0 || canProjectSecureVideo(projection)) {
                    HwActivityManager.reportScreenRecord(callingUid, callingPid, 1);
                    LogPower.push(204, String.valueOf(callingPid), String.valueOf(callingUid), String.valueOf(2));
                    if (callingUid == 1000 || (flags4 & 512) == 0 || checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "createVirtualDisplay()")) {
                        long token = Binder.clearCallingIdentity();
                        try {
                            try {
                                int createVirtualDisplayInternal = DisplayManagerService.this.createVirtualDisplayInternal(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags4, uniqueId);
                                Binder.restoreCallingIdentity(token);
                                return createVirtualDisplayInternal;
                            } catch (Throwable th2) {
                                th = th2;
                                Binder.restoreCallingIdentity(token);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                    } else {
                        throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
                    }
                } else {
                    throw new SecurityException("Requires CAPTURE_SECURE_VIDEO_OUTPUT or an appropriate MediaProjection token to create a secure virtual display.");
                }
            } else {
                throw new IllegalArgumentException("Surface can't be single-buffered");
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
            if (surface == null || !surface.isSingleBuffered()) {
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.setVirtualDisplaySurfaceInternal(callback.asBinder(), surface);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("Surface can't be single-buffered");
            }
        }

        public void releaseVirtualDisplay(IVirtualDisplayCallback callback) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long token = Binder.clearCallingIdentity();
            HwActivityManager.reportScreenRecord(callingUid, callingPid, 0);
            LogPower.push(205, String.valueOf(callingPid), String.valueOf(callingUid), String.valueOf(2));
            try {
                DisplayManagerService.this.releaseVirtualDisplayInternal(callback.asBinder());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setVirtualDisplayState(IVirtualDisplayCallback callback, boolean isOn) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.setVirtualDisplayStateInternal(callback.asBinder(), isOn);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(DisplayManagerService.this.mContext, DisplayManagerService.TAG, pw)) {
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.dumpInternal(pw);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        public ParceledListSlice<BrightnessChangeEvent> getBrightnessEvents(String callingPackage) {
            ParceledListSlice<BrightnessChangeEvent> brightnessEvents;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BRIGHTNESS_SLIDER_USAGE", "Permission to read brightness events.");
            int callingUid = Binder.getCallingUid();
            int mode = ((AppOpsManager) DisplayManagerService.this.mContext.getSystemService(AppOpsManager.class)).noteOp(43, callingUid, callingPackage);
            boolean hasUsageStats = true;
            if (mode == 3) {
                if (DisplayManagerService.this.mContext.checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
                    hasUsageStats = false;
                }
            } else if (mode != 0) {
                hasUsageStats = false;
            }
            int userId = UserHandle.getUserId(callingUid);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    brightnessEvents = DisplayManagerService.this.mDisplayPowerController.getBrightnessEvents(userId, hasUsageStats);
                }
                return brightnessEvents;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public ParceledListSlice<AmbientBrightnessDayStats> getAmbientBrightnessStats() {
            ParceledListSlice<AmbientBrightnessDayStats> ambientBrightnessStats;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_AMBIENT_LIGHT_STATS", "Permission required to to access ambient light stats.");
            int userId = UserHandle.getUserId(Binder.getCallingUid());
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    ambientBrightnessStats = DisplayManagerService.this.mDisplayPowerController.getAmbientBrightnessStats(userId);
                }
                return ambientBrightnessStats;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setBrightnessConfigurationForUser(BrightnessConfiguration c, int userId, String packageName) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_BRIGHTNESS", "Permission required to change the display's brightness configuration");
            if (userId != UserHandle.getCallingUserId()) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Permission required to change the display brightness configuration of another user");
            }
            if (packageName != null && !validatePackageName(getCallingUid(), packageName)) {
                packageName = null;
            }
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.setBrightnessConfigurationForUserInternal(c, userId, packageName);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public BrightnessConfiguration getBrightnessConfigurationForUser(int userId) {
            BrightnessConfiguration config;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_BRIGHTNESS", "Permission required to read the display's brightness configuration");
            if (userId != UserHandle.getCallingUserId()) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Permission required to read the display brightness configuration of another user");
            }
            long token = Binder.clearCallingIdentity();
            try {
                int userSerial = DisplayManagerService.this.getUserManager().getUserSerialNumber(userId);
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    config = DisplayManagerService.this.mPersistentDataStore.getBrightnessConfiguration(userSerial);
                    if (config == null) {
                        config = DisplayManagerService.this.mDisplayPowerController.getDefaultBrightnessConfiguration();
                    }
                }
                return config;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public BrightnessConfiguration getDefaultBrightnessConfiguration() {
            BrightnessConfiguration defaultBrightnessConfiguration;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_BRIGHTNESS", "Permission required to read the display's default brightness configuration");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    defaultBrightnessConfiguration = DisplayManagerService.this.mDisplayPowerController.getDefaultBrightnessConfiguration();
                }
                return defaultBrightnessConfiguration;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setTemporaryBrightness(int brightness) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_DISPLAY_BRIGHTNESS", "Permission required to set the display's brightness");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setTemporaryBrightness(brightness);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_DISPLAY_BRIGHTNESS", "Permission required to set the display's auto brightness adjustment");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setTemporaryAutoBrightnessAdjustment(adjustment);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r12v0, resolved type: com.android.server.display.DisplayManagerService$BinderService */
        /* JADX WARN: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            Throwable th;
            long token = Binder.clearCallingIdentity();
            try {
                try {
                    new DisplayManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public Curve getMinimumBrightnessCurve() {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getMinimumBrightnessCurveInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* JADX WARN: Type inference failed for: r0v1, types: [android.os.IBinder, com.android.server.display.DisplayManagerService$HwInnerDisplayManagerService] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public IBinder getHwInnerService() {
            return DisplayManagerService.this.mHwInnerService;
        }

        public int getPreferredWideGamutColorSpaceId() {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getPreferredWideGamutColorSpaceIdInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* access modifiers changed from: package-private */
        public void setBrightness(int brightness) {
            Settings.System.putIntForUser(DisplayManagerService.this.mContext.getContentResolver(), "screen_brightness", brightness, -2);
        }

        /* access modifiers changed from: package-private */
        public void resetBrightnessConfiguration() {
            DisplayManagerService displayManagerService = DisplayManagerService.this;
            displayManagerService.setBrightnessConfigurationForUserInternal(null, displayManagerService.mContext.getUserId(), DisplayManagerService.this.mContext.getPackageName());
        }

        /* access modifiers changed from: package-private */
        public void setAutoBrightnessLoggingEnabled(boolean enabled) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setAutoBrightnessLoggingEnabled(enabled);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setDisplayWhiteBalanceLoggingEnabled(boolean enabled) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setDisplayWhiteBalanceLoggingEnabled(enabled);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setAmbientColorTemperatureOverride(float cct) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setAmbientColorTemperatureOverride(cct);
                }
            }
        }

        private boolean validatePackageName(int uid, String packageName) {
            String[] packageNames;
            if (!(packageName == null || (packageNames = DisplayManagerService.this.mContext.getPackageManager().getPackagesForUid(uid)) == null)) {
                for (String n : packageNames) {
                    if (n.equals(packageName)) {
                        return true;
                    }
                }
            }
            return false;
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
            if (checkCallingPermission("android.permission.CAPTURE_VIDEO_OUTPUT", "canProjectVideo()")) {
                return true;
            }
            return canProjectSecureVideo(projection);
        }

        private boolean canProjectSecureVideo(IMediaProjection projection) {
            if (projection != null) {
                try {
                    if (projection.canProjectSecureVideo()) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Slog.e(DisplayManagerService.TAG, "Unable to query projection service for permissions", e);
                }
            }
            return checkCallingPermission("android.permission.CAPTURE_SECURE_VIDEO_OUTPUT", "canProjectSecureVideo()");
        }

        private boolean checkCallingPermission(String permission, String func) {
            if (DisplayManagerService.this.mContext.checkCallingPermission(permission) == 0) {
                return true;
            }
            Slog.w(DisplayManagerService.TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
            return false;
        }
    }

    private final class LocalService extends DisplayManagerInternal {
        private LocalService() {
        }

        public void initPowerManagement(final DisplayManagerInternal.DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager) {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayBlanker blanker = new DisplayBlanker() {
                    /* class com.android.server.display.DisplayManagerService.LocalService.AnonymousClass1 */

                    @Override // com.android.server.display.DisplayBlanker
                    public void requestDisplayState(int state, int brightness) {
                        if (state == 1) {
                            DisplayManagerService.this.requestGlobalDisplayStateInternal(state, brightness);
                        }
                        callbacks.onDisplayStateChange(state);
                        if (state != 1) {
                            DisplayManagerService.this.requestGlobalDisplayStateInternal(state, brightness);
                        }
                    }
                };
                DisplayManagerService.this.mDisplayPowerController = new DisplayPowerController(DisplayManagerService.this.mContext, callbacks, handler, sensorManager, blanker);
            }
            DisplayManagerService.this.mHandler.sendEmptyMessage(6);
        }

        public boolean requestPowerState(DisplayManagerInternal.DisplayPowerRequest request, boolean waitForNegativeProximity) {
            boolean requestPowerState;
            synchronized (DisplayManagerService.this.mSyncRoot) {
                requestPowerState = DisplayManagerService.this.mDisplayPowerController.requestPowerState(request, waitForNegativeProximity);
            }
            return requestPowerState;
        }

        public void pcDisplayChange(boolean connected) {
            DisplayManagerService.this.pcDisplayChangeService(connected);
        }

        public void forceDisplayState(int screenState, int screenBrightness) {
            DisplayManagerService.this.requestGlobalDisplayStateInternal(screenState, screenBrightness);
        }

        public void setBacklightBrightness(PowerManager.BacklightBrightness backlightBrightness) {
            DisplayManagerService.this.mDisplayPowerController.setBacklightBrightness(backlightBrightness);
        }

        public void setCameraModeBrightnessLineEnable(boolean cameraModeBrightnessLineEnable) {
            DisplayManagerService.this.mDisplayPowerController.setCameraModeBrightnessLineEnable(cameraModeBrightnessLineEnable);
        }

        public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
            DisplayManagerService.this.mDisplayPowerController.updateAutoBrightnessAdjustFactor(adjustFactor);
        }

        public int getMaxBrightnessForSeekbar() {
            return DisplayManagerService.this.mDisplayPowerController.getMaxBrightnessForSeekbar();
        }

        public boolean getRebootAutoModeEnable() {
            return DisplayManagerService.this.mDisplayPowerController.getRebootAutoModeEnable();
        }

        public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
            DisplayManagerService.this.mDisplayPowerController.setBrightnessAnimationTime(animationEnabled, millisecond);
        }

        public int getCoverModeBrightnessFromLastScreenBrightness() {
            return DisplayManagerService.this.mDisplayPowerController.getCoverModeBrightnessFromLastScreenBrightness();
        }

        public void setMaxBrightnessFromThermal(int brightness) {
            DisplayManagerService.this.mDisplayPowerController.setMaxBrightnessFromThermal(brightness);
        }

        public void setPoweroffModeChangeAutoEnable(boolean enable) {
            DisplayManagerService.this.mDisplayPowerController.setPoweroffModeChangeAutoEnable(enable);
        }

        public void setKeyguardLockedStatus(boolean isLocked) {
            DisplayManagerService.this.mDisplayPowerController.setKeyguardLockedStatus(isLocked);
        }

        public void setAodAlpmState(int globalState) {
            DisplayManagerService.this.mGlobalAlpmState = globalState;
            DisplayManagerService.this.mDisplayPowerController.setAodAlpmState(globalState);
        }

        public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
            return DisplayManagerService.this.mDisplayPowerController.setScreenBrightnessMappingtoIndoorMax(brightness);
        }

        public void setBrightnessNoLimit(int brightness, int time) {
            DisplayManagerService.this.mDisplayPowerController.setBrightnessNoLimit(brightness, time);
        }

        public void setModeToAutoNoClearOffsetEnable(boolean enable) {
            DisplayManagerService.this.mDisplayPowerController.setModeToAutoNoClearOffsetEnable(enable);
        }

        public void setTemporaryScreenBrightnessSettingOverride(int brightness) {
            if (DisplayManagerService.this.mTemporaryScreenBrightnessSettingOverride != brightness) {
                DisplayManagerService.this.mDisplayPowerController.setTemporaryBrightness(brightness);
                DisplayManagerService.this.mTemporaryScreenBrightnessSettingOverride = brightness;
            }
        }

        public boolean setHwBrightnessData(String name, Bundle data, int[] result) {
            return DisplayManagerService.this.mDisplayPowerController.setHwBrightnessData(name, data, result);
        }

        public boolean getHwBrightnessData(String name, Bundle data, int[] result) {
            return DisplayManagerService.this.mDisplayPowerController.getHwBrightnessData(name, data, result);
        }

        public IBinder getDisplayToken(int displayId) {
            LogicalDisplay logicalDisplay = (LogicalDisplay) DisplayManagerService.this.mLogicalDisplays.get(displayId);
            if (logicalDisplay == null) {
                return null;
            }
            return logicalDisplay.getPrimaryDisplayDeviceLocked().getDisplayTokenLocked();
        }

        public boolean isProximitySensorAvailable() {
            boolean isProximitySensorAvailable;
            synchronized (DisplayManagerService.this.mSyncRoot) {
                isProximitySensorAvailable = DisplayManagerService.this.mDisplayPowerController.isProximitySensorAvailable();
            }
            return isProximitySensorAvailable;
        }

        public SurfaceControl.ScreenshotGraphicBuffer screenshot(int displayId) {
            return DisplayManagerService.this.screenshotInternal(displayId);
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            return DisplayManagerService.this.getDisplayInfoInternal(displayId, Process.myUid());
        }

        public void registerDisplayTransactionListener(DisplayManagerInternal.DisplayTransactionListener listener) {
            if (listener != null) {
                DisplayManagerService.this.registerDisplayTransactionListenerInternal(listener);
                return;
            }
            throw new IllegalArgumentException("listener must not be null");
        }

        public void unregisterDisplayTransactionListener(DisplayManagerInternal.DisplayTransactionListener listener) {
            if (listener != null) {
                DisplayManagerService.this.unregisterDisplayTransactionListenerInternal(listener);
                return;
            }
            throw new IllegalArgumentException("listener must not be null");
        }

        public void setDisplayInfoOverrideFromWindowManager(int displayId, DisplayInfo info) {
            DisplayManagerService.this.setDisplayInfoOverrideFromWindowManagerInternal(displayId, info);
        }

        public void updateCutoutInfoForRog(int displayId) {
            DisplayManagerService.this.updateCutoutInfoForRogInternal(displayId);
        }

        public void getNonOverrideDisplayInfo(int displayId, DisplayInfo outInfo) {
            DisplayManagerService.this.getNonOverrideDisplayInfoInternal(displayId, outInfo);
        }

        public void performTraversal(SurfaceControl.Transaction t) {
            if (HwFoldScreenState.isOutFoldDevice()) {
                DisplayManagerService.this.performTraversalNeedDelay(t, 0);
            } else {
                DisplayManagerService.this.performTraversalInternal(t, false);
            }
        }

        public void performTraversal(SurfaceControl.Transaction t, boolean forceTraversal) {
            DisplayManagerService.this.performTraversalInternal(t, forceTraversal);
        }

        public void setDisplayProperties(int displayId, boolean hasContent, float requestedRefreshRate, int requestedMode, boolean inTraversal) {
            DisplayManagerService.this.setDisplayPropertiesInternal(displayId, hasContent, requestedRefreshRate, requestedMode, inTraversal);
        }

        public void setDisplayOffsets(int displayId, int x, int y) {
            DisplayManagerService.this.setDisplayOffsetsInternal(displayId, x, y);
        }

        public void setDisplayScalingDisabled(int displayId, boolean disableScaling) {
            DisplayManagerService.this.setDisplayScalingDisabledInternal(displayId, disableScaling);
        }

        public void setDisplayAccessUIDs(SparseArray<IntArray> newDisplayAccessUIDs) {
            DisplayManagerService.this.setDisplayAccessUIDsInternal(newDisplayAccessUIDs);
        }

        public void persistBrightnessTrackerState() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.mDisplayPowerController.persistBrightnessTrackerState();
            }
        }

        public void onOverlayChanged() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                for (int i = 0; i < DisplayManagerService.this.mDisplayDevices.size(); i++) {
                    ((DisplayDevice) DisplayManagerService.this.mDisplayDevices.get(i)).onOverlayChangedLocked();
                }
            }
        }

        public DisplayedContentSamplingAttributes getDisplayedContentSamplingAttributes(int displayId) {
            return DisplayManagerService.this.getDisplayedContentSamplingAttributesInternal(displayId);
        }

        public boolean setDisplayedContentSamplingEnabled(int displayId, boolean enable, int componentMask, int maxFrames) {
            return DisplayManagerService.this.setDisplayedContentSamplingEnabledInternal(displayId, enable, componentMask, maxFrames);
        }

        public DisplayedContentSample getDisplayedContentSample(int displayId, long maxFrames, long timestamp) {
            return DisplayManagerService.this.getDisplayedContentSampleInternal(displayId, maxFrames, timestamp);
        }

        public void requestScreenState() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.mDisplayPowerController.requestScreenState();
            }
        }

        public int setDisplayMode(int mode, int state, boolean wakeup) {
            DisplayManagerService.this.resetDisplayDelayImpl();
            if (HwFoldScreenState.isInwardFoldDevice() && DisplayManagerService.this.mDisplayMode == 1 && mode == 2) {
                if (DisplayManagerService.this.mDisplayPowerController != null) {
                    DisplayManagerService.this.mDisplayPowerController.updateDisplayMode(mode);
                } else {
                    Slog.e(DisplayManagerService.TAG, "mDisplayPowerController is null");
                }
            }
            if (DisplayManagerService.this.isDisplayNeedDelay(wakeup, mode)) {
                DisplayManagerService.this.mNeedHandleDisplayMode = true;
            }
            if (4 == DisplayManagerService.this.mDisplayMode || 4 == mode) {
                DisplayManagerService.this.mNeedDelayTime = DisplayManagerService.NEED_DELAY_TIME_COORDINATION;
            } else {
                DisplayManagerService.this.mNeedDelayTime = DisplayManagerService.NEED_DELAY_TIME_DEFAULT;
            }
            DisplayManagerService.this.mDisplayMode = mode;
            return ((DisplayDevice) DisplayManagerService.this.mDisplayDevices.get(0)).setDisplayState(mode, state);
        }

        public void resetDisplayDelay() {
            DisplayManagerService.this.resetDisplayDelayImpl();
        }

        public int getDisplayMode() {
            return ((DisplayDevice) DisplayManagerService.this.mDisplayDevices.get(0)).getDisplayState();
        }

        public void wakeupDisplayModeChange(boolean change) {
            if (DisplayManagerService.this.mTransaction != null) {
                Slog.d(DisplayManagerService.TAG, "wakeupDisplayModeChange is called! " + change);
                DisplayManagerService.this.mHandler.removeMessages(Message.obtain(DisplayManagerService.this.mHandler, 8).what);
                DisplayManagerService displayManagerService = DisplayManagerService.this;
                displayManagerService.performTraversalNow(displayManagerService.mTransaction);
            }
        }

        public void setDisplayModeChangeDelay(SurfaceControl.Transaction t, long time) {
            DisplayManagerService.this.mTransaction = t;
            DisplayManagerService.this.mNeedHandleDisplayRequest = true;
            DisplayManagerService.this.mIsForceUpdateDisplayInfo = true;
            Slog.i(DisplayManagerService.TAG, "setDisplayModeChangeDelay time = " + time);
            if (t == null) {
                Slog.i(DisplayManagerService.TAG, "setDisplayModeChangeDelay Transaction is null, return");
                return;
            }
            if (time != 0) {
                DisplayManagerService.this.mIsDisplayNeedDelaydefault = false;
            }
            DisplayManagerService.this.performTraversalNeedDelay(t, time);
        }

        public void setHwWifiDisplayParameters(HwWifiDisplayParameters parameters) {
            if (DisplayManagerService.this.mHwDMSEx != null) {
                DisplayManagerService.this.mHwDMSEx.setHwWifiDisplayParameters(parameters);
            }
        }

        public void setBiometricDetectState(int state) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                DisplayManagerService.this.mDisplayPowerController.setBiometricDetectState(state);
            }
        }

        public void startDawnAnimation() {
            Slog.d(DisplayManagerService.TAG, "dms startDawnAnimation");
            if (DisplayManagerService.this.mDisplayPowerController == null) {
                Slog.e(DisplayManagerService.TAG, "mDisplayPowerController is null");
            } else if (DisplayManagerService.this.mDisplayMode == 1) {
                DisplayManagerService.this.mDisplayPowerController.startDawnAnimation();
            }
        }

        public boolean registerScreenOnUnBlockerCallback(HwFoldScreenManagerInternal.ScreenOnUnblockerCallback callback) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                return DisplayManagerService.this.mDisplayPowerController.registerScreenOnUnBlockerCallback(callback);
            }
            Slog.e(DisplayManagerService.TAG, "mDisplayPowerController is null");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public class AllowedDisplayModeObserver implements DisplayModeDirector.Listener {
        AllowedDisplayModeObserver() {
        }

        @Override // com.android.server.display.DisplayModeDirector.Listener
        public void onAllowedDisplayModesChanged() {
            DisplayManagerService.this.onAllowedDisplayModesChangedInternal();
        }
    }

    @Override // com.android.server.display.IHwDisplayManagerInner
    public SyncRoot getLock() {
        return this.mSyncRoot;
    }

    @Override // com.android.server.display.IHwDisplayManagerInner
    public WifiDisplayAdapter getWifiDisplayAdapter() {
        return this.mWifiDisplayAdapter;
    }

    @Override // com.android.server.display.IHwDisplayManagerInner
    public void startWifiDisplayScanInner(int callingPid, int channelID) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = this.mCallbacks.get(callingPid);
            if (record != null) {
                startWifiDisplayScanLocked(record, channelID);
            } else {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
        }
    }

    private void startWifiDisplayScanLocked(CallbackRecord record, int channelID) {
        WifiDisplayAdapter wifiDisplayAdapter;
        if (HWFLOW) {
            Slog.i(TAG, "startWifiDisplayScanLocked mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
        }
        if (HWFLOW) {
            Slog.i(TAG, "startWifiDisplayScanLocked record.mWifiDisplayScanRequested=" + record.mWifiDisplayScanRequested);
        }
        if (!record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = true;
            int i = this.mWifiDisplayScanRequestCount;
            this.mWifiDisplayScanRequestCount = i + 1;
            if (i == 0 && (wifiDisplayAdapter = this.mWifiDisplayAdapter) != null) {
                wifiDisplayAdapter.requestStartScanLocked(channelID);
            }
        }
    }

    public class HwInnerDisplayManagerService extends IHwDisplayManager.Stub {
        private static final String WFD_ENHANCE_PERMISSION = "com.huawei.permission.WFD_ENHANCE_API";
        DisplayManagerService mDMS;

        HwInnerDisplayManagerService(DisplayManagerService dms) {
            this.mDMS = dms;
        }

        public void startWifiDisplayScan(int channelId) {
            if (!checkCallingPermission(WFD_ENHANCE_PERMISSION, "startWifiDisplayScan")) {
                throw new SecurityException("Requires wifidisplay permission");
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                DisplayManagerService.this.mHwDMSEx.startWifiDisplayScan(channelId);
            }
        }

        public void connectWifiDisplay(String address, HwWifiDisplayParameters parameters) {
            if (!checkCallingPermission(WFD_ENHANCE_PERMISSION, "connectWifiDisplay")) {
                throw new SecurityException("Requires wifidisplay permission");
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                DisplayManagerService.this.mHwDMSEx.setHwWifiDisplayParameters(parameters);
                DisplayManagerService.this.mHwDMSEx.connectWifiDisplay(address, parameters);
            }
        }

        public void checkVerificationResult(boolean isRight) {
            if (!checkCallingPermission(WFD_ENHANCE_PERMISSION, "checkVerificationResult")) {
                throw new SecurityException("Requires wifidisplay permission");
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                DisplayManagerService.this.mHwDMSEx.checkVerificationResult(isRight);
            }
        }

        public boolean sendWifiDisplayAction(String action) {
            if (!checkCallingPermission(WFD_ENHANCE_PERMISSION, "sendWifiDisplayAction")) {
                throw new SecurityException("Requires wifidisplay permission");
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                return DisplayManagerService.this.mHwDMSEx.sendWifiDisplayAction(action);
            } else {
                return false;
            }
        }

        public HwWifiDisplayParameters getHwWifiDisplayParameters() {
            if (!checkCallingPermission(WFD_ENHANCE_PERMISSION, "getHwWifiDisplayParameters")) {
                throw new SecurityException("Requires wifidisplay permission");
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                return DisplayManagerService.this.mHwDMSEx.getHwWifiDisplayParameters();
            } else {
                return new HwWifiDisplayParameters();
            }
        }

        private boolean checkCallingPermission(String permission, String describe) {
            if (Binder.getCallingUid() == 1000 || DisplayManagerService.this.mContext.checkCallingPermission(permission) == 0) {
                return true;
            }
            Slog.w(DisplayManagerService.TAG, "Permission Denial: " + describe + ".");
            return false;
        }

        public boolean createVrDisplay(String displayName, int[] displayParams) {
            if (Binder.getCallingUid() != 1000) {
                Slog.e(DisplayManagerService.TAG, "not allowed to call createVrDisplay");
                return false;
            } else if (displayName == null || displayParams == null) {
                Slog.e(DisplayManagerService.TAG, "parameter is invalid in createVrDisplay");
                return false;
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                return DisplayManagerService.this.mHwDMSEx.createVrDisplay(displayName, displayParams);
            } else {
                return false;
            }
        }

        public boolean destroyVrDisplay(String displayName) {
            if (Binder.getCallingUid() != 1000) {
                Slog.e(DisplayManagerService.TAG, "not allowed to call destroyVrDisplay");
                return false;
            } else if (displayName == null) {
                Slog.e(DisplayManagerService.TAG, "parameter is null in destroyVrDisplay");
                return false;
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                return DisplayManagerService.this.mHwDMSEx.destroyVrDisplay(displayName);
            } else {
                return false;
            }
        }

        public boolean destroyAllVrDisplay() {
            if (Binder.getCallingUid() != 1000) {
                Slog.e(DisplayManagerService.TAG, "not allowed to call destroyAllVrDisplay");
                return false;
            } else if (DisplayManagerService.this.mHwDMSEx != null) {
                return DisplayManagerService.this.mHwDMSEx.destroyAllVrDisplay();
            } else {
                return false;
            }
        }
    }
}
