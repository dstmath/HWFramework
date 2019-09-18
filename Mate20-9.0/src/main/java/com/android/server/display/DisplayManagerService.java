package com.android.server.display;

import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.hardware.display.AmbientBrightnessDayStats;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.Curve;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayViewport;
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
import android.os.HwBrightnessProcessor;
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
import android.util.ArrayMap;
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
import android.vrsystem.IVRSystemServiceManager;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.AnimationThread;
import com.android.server.DisplayThread;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.display.DisplayAdapter;
import com.android.server.wm.SurfaceAnimationThread;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.zrhung.IZRHungService;
import com.huawei.android.app.HwActivityManager;
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
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static boolean ESD_ENABLE = SystemProperties.getBoolean("ro.product.esdenable", false);
    private static final String FORCE_WIFI_DISPLAY_ENABLE = "persist.debug.wfd.enable";
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    /* access modifiers changed from: private */
    public static final boolean IS_DEBUG_VERSION = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    private static final int MSG_DELIVER_DISPLAY_EVENT = 3;
    private static final int MSG_LOAD_BRIGHTNESS_CONFIGURATION = 7;
    private static final int MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS = 2;
    private static final int MSG_REGISTER_BRIGHTNESS_TRACKER = 6;
    private static final int MSG_REGISTER_DEFAULT_DISPLAY_ADAPTERS = 1;
    private static final int MSG_REQUEST_TRAVERSAL = 4;
    private static final int MSG_UPDATE_VIEWPORT = 5;
    private static final String TAG = "DisplayManagerService";
    private static final long WAIT_FOR_DEFAULT_DISPLAY_TIMEOUT = 10000;
    /* access modifiers changed from: private */
    public static final boolean mIsFoldable;
    private int mBrightnessBeforeSuspend;
    public final SparseArray<CallbackRecord> mCallbacks;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCurrentUserId;
    private final int mDefaultDisplayDefaultColorMode;
    /* access modifiers changed from: private */
    public final DisplayViewport mDefaultViewport;
    private final SparseArray<IntArray> mDisplayAccessUIDs;
    private final DisplayAdapterListener mDisplayAdapterListener;
    private final ArrayList<DisplayAdapter> mDisplayAdapters;
    /* access modifiers changed from: private */
    public final ArrayList<DisplayDevice> mDisplayDevices;
    /* access modifiers changed from: private */
    public DisplayPowerController mDisplayPowerController;
    private final CopyOnWriteArrayList<DisplayManagerInternal.DisplayTransactionListener> mDisplayTransactionListeners;
    /* access modifiers changed from: private */
    public final DisplayViewport mExternalTouchViewport;
    /* access modifiers changed from: private */
    public int mGlobalAlpmState;
    private int mGlobalDisplayBrightness;
    private int mGlobalDisplayState;
    /* access modifiers changed from: private */
    public final DisplayManagerHandler mHandler;
    IHwDisplayManagerServiceEx mHwDMSEx;
    HwInnerDisplayManagerService mHwInnerService;
    private final Injector mInjector;
    /* access modifiers changed from: private */
    public InputManagerInternal mInputManagerInternal;
    private boolean mIsHighPrecision;
    private LocalDisplayAdapter mLocalDisplayAdapter;
    /* access modifiers changed from: private */
    public final SparseArray<LogicalDisplay> mLogicalDisplays;
    private final Curve mMinimumBrightnessCurve;
    private final Spline mMinimumBrightnessSpline;
    private int mNextNonDefaultDisplayId;
    public boolean mOnlyCore;
    private boolean mPendingTraversal;
    /* access modifiers changed from: private */
    public final PersistentDataStore mPersistentDataStore;
    private PowerManagerInternal mPowerManagerInternal;
    /* access modifiers changed from: private */
    public final ArrayMap<String, HwBrightnessProcessor> mPowerProcessors;
    private IMediaProjectionManager mProjectionService;
    public boolean mSafeMode;
    private final boolean mSingleDisplayDemoMode;
    private Point mStableDisplaySize;
    private int mStateBeforeSuspend;
    private boolean mSuspendDisplay;
    /* access modifiers changed from: private */
    public final SyncRoot mSyncRoot;
    private final ArrayList<CallbackRecord> mTempCallbacks;
    /* access modifiers changed from: private */
    public final DisplayViewport mTempDefaultViewport;
    private final DisplayInfo mTempDisplayInfo;
    private final ArrayList<Runnable> mTempDisplayStateWorkQueue;
    /* access modifiers changed from: private */
    public final DisplayViewport mTempExternalTouchViewport;
    /* access modifiers changed from: private */
    public final ArrayList<DisplayViewport> mTempVirtualTouchViewports;
    /* access modifiers changed from: private */
    public int mTemporaryScreenBrightnessSettingOverride;
    private final Handler mUiHandler;
    private VirtualDisplayAdapter mVirtualDisplayAdapter;
    /* access modifiers changed from: private */
    public final ArrayList<DisplayViewport> mVirtualTouchViewports;
    private IVRSystemServiceManager mVrMananger;
    private WifiDisplayAdapter mWifiDisplayAdapter;
    private int mWifiDisplayScanRequestCount;
    /* access modifiers changed from: private */
    public WindowManagerInternal mWindowManagerInternal;

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

        public void setSaturationLevel(float level) {
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
            long token;
            IMediaProjection iMediaProjection = projection;
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            String str = packageName;
            if (!validatePackageName(callingUid, str)) {
                int i = callingUid;
                throw new SecurityException("packageName must match the calling uid");
            } else if (callback == null) {
                int i2 = callingUid;
                throw new IllegalArgumentException("appToken must not be null");
            } else if (TextUtils.isEmpty(name)) {
                int i3 = callingUid;
                throw new IllegalArgumentException("name must be non-null and non-empty");
            } else if (width <= 0 || height <= 0 || densityDpi <= 0) {
                int i4 = callingUid;
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
                    flags2 &= -17;
                }
                int flags4 = flags2;
                if (iMediaProjection != null) {
                    try {
                        if (DisplayManagerService.this.getProjectionService().isValidMediaProjection(iMediaProjection)) {
                            flags3 = iMediaProjection.applyVirtualDisplayFlags(flags4);
                        } else {
                            throw new SecurityException("Invalid media projection");
                        }
                    } catch (RemoteException e) {
                        throw new SecurityException("unable to validate media projection or flags");
                    }
                } else {
                    flags3 = flags4;
                }
                if (callingUid != 1000 && (flags3 & 16) != 0 && !canProjectVideo(iMediaProjection)) {
                    throw new SecurityException("Requires CAPTURE_VIDEO_OUTPUT or CAPTURE_SECURE_VIDEO_OUTPUT permission, or an appropriate MediaProjection token in order to create a screen sharing virtual display.");
                } else if ((flags3 & 4) == 0 || canProjectSecureVideo(iMediaProjection)) {
                    HwActivityManager.reportScreenRecord(callingUid, callingPid, 1);
                    LogPower.push(204, String.valueOf(callingPid), String.valueOf(callingUid), String.valueOf(2));
                    long token2 = Binder.clearCallingIdentity();
                    try {
                        IMediaProjection iMediaProjection2 = iMediaProjection;
                        int i5 = callingUid;
                        token = token2;
                        int i6 = callingPid;
                        try {
                            int access$3900 = DisplayManagerService.this.createVirtualDisplayInternal(callback, iMediaProjection2, callingUid, str, name, width, height, densityDpi, surface, flags3, uniqueId);
                            Binder.restoreCallingIdentity(token);
                            return access$3900;
                        } catch (Throwable th) {
                            th = th;
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        int i7 = callingPid;
                        int i8 = callingUid;
                        token = token2;
                        Binder.restoreCallingIdentity(token);
                        throw th;
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
            boolean hasUsageStats = false;
            if (mode == 3) {
                if (DisplayManagerService.this.mContext.checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") == 0) {
                    hasUsageStats = true;
                }
            } else if (mode == 0) {
                hasUsageStats = true;
            }
            int userId = UserHandle.getUserId(callingUid);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    brightnessEvents = DisplayManagerService.this.mDisplayPowerController.getBrightnessEvents(userId, hasUsageStats);
                }
                Binder.restoreCallingIdentity(token);
                return brightnessEvents;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
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
                Binder.restoreCallingIdentity(token);
                return ambientBrightnessStats;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
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
                Binder.restoreCallingIdentity(token);
                return config;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
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
                Binder.restoreCallingIdentity(token);
                return defaultBrightnessConfiguration;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setTemporaryBrightness(int brightness) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_DISPLAY_BRIGHTNESS", "Permission required to set the display's brightness");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setTemporaryBrightness(brightness);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_DISPLAY_BRIGHTNESS", "Permission required to set the display's auto brightness adjustment");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setTemporaryAutoBrightnessAdjustment(adjustment);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        /* JADX WARNING: type inference failed for: r4v0, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            long token = Binder.clearCallingIdentity();
            try {
                try {
                    new DisplayManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    th = th;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
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

        /* JADX WARNING: type inference failed for: r0v1, types: [android.os.IBinder, com.android.server.display.DisplayManagerService$HwInnerDisplayManagerService] */
        public IBinder getHwInnerService() {
            return DisplayManagerService.this.mHwInnerService;
        }

        /* access modifiers changed from: package-private */
        public void setBrightness(int brightness) {
            Settings.System.putIntForUser(DisplayManagerService.this.mContext.getContentResolver(), "screen_brightness", brightness, -2);
        }

        /* access modifiers changed from: package-private */
        public void resetBrightnessConfiguration() {
            DisplayManagerService.this.setBrightnessConfigurationForUserInternal(null, DisplayManagerService.this.mContext.getUserId(), DisplayManagerService.this.mContext.getPackageName());
        }

        private boolean validatePackageName(int uid, String packageName) {
            if (packageName != null) {
                String[] packageNames = DisplayManagerService.this.mContext.getPackageManager().getPackagesForUid(uid);
                if (packageNames != null) {
                    for (String n : packageNames) {
                        if (n.equals(packageName)) {
                            return true;
                        }
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
                z = false;
            }
            return z;
        }
    }

    private final class CallbackRecord implements IBinder.DeathRecipient {
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
                    ArrayMap<String, Object> params = new ArrayMap<>();
                    params.put("checkType", "DisplayEventLostScene");
                    params.put("context", DisplayManagerService.this.mContext);
                    params.put("looper", DisplayThread.get().getLooper());
                    params.put(IZRHungService.PARAM_PID, Integer.valueOf(this.mPid));
                    if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                        HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
                    }
                }
                binderDied();
            }
        }
    }

    private final class DisplayAdapterListener implements DisplayAdapter.Listener {
        private DisplayAdapterListener() {
        }

        public void onDisplayDeviceEvent(DisplayDevice device, int event) {
            switch (event) {
                case 1:
                    DisplayManagerService.this.handleDisplayDeviceAdded(device);
                    return;
                case 2:
                    DisplayManagerService.this.handleDisplayDeviceChanged(device);
                    return;
                case 3:
                    DisplayManagerService.this.handleDisplayDeviceRemoved(device);
                    return;
                default:
                    return;
            }
        }

        public void onTraversalRequested() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.scheduleTraversalLocked(false);
            }
        }
    }

    private final class DisplayManagerHandler extends Handler {
        public DisplayManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 7) {
                switch (i) {
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
                            DisplayManagerService.this.mTempDefaultViewport.copyFrom(DisplayManagerService.this.mDefaultViewport);
                            DisplayManagerService.this.mTempExternalTouchViewport.copyFrom(DisplayManagerService.this.mExternalTouchViewport);
                            if (!DisplayManagerService.this.mTempVirtualTouchViewports.equals(DisplayManagerService.this.mVirtualTouchViewports) || DisplayManagerService.mIsFoldable) {
                                DisplayManagerService.this.mTempVirtualTouchViewports.clear();
                                Iterator it = DisplayManagerService.this.mVirtualTouchViewports.iterator();
                                while (it.hasNext()) {
                                    DisplayManagerService.this.mTempVirtualTouchViewports.add(((DisplayViewport) it.next()).makeCopy());
                                }
                            }
                        }
                        DisplayManagerService.this.mInputManagerInternal.setDisplayViewports(DisplayManagerService.this.mTempDefaultViewport, DisplayManagerService.this.mTempExternalTouchViewport, DisplayManagerService.this.mTempVirtualTouchViewports);
                        return;
                    default:
                        return;
                }
            } else {
                DisplayManagerService.this.loadBrightnessConfiguration();
            }
        }
    }

    public class HwInnerDisplayManagerService extends IHwDisplayManager.Stub {
        DisplayManagerService mDMS;

        HwInnerDisplayManagerService(DisplayManagerService dms) {
            this.mDMS = dms;
        }

        public void startWifiDisplayScan(int channelId) {
            DisplayManagerService.this.mHwDMSEx.startWifiDisplayScan(channelId);
        }

        public void connectWifiDisplay(String address, String verificaitonCode) {
            DisplayManagerService.this.mHwDMSEx.connectWifiDisplay(address, verificaitonCode);
        }

        public void checkVerificationResult(boolean isRight) {
            DisplayManagerService.this.mHwDMSEx.checkVerificationResult(isRight);
        }

        public boolean sendWifiDisplayAction(String action) {
            return DisplayManagerService.this.mHwDMSEx.sendWifiDisplayAction(action);
        }
    }

    @VisibleForTesting
    static class Injector {
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

    private final class LocalService extends DisplayManagerInternal {
        private LocalService() {
        }

        public void initPowerManagement(final DisplayManagerInternal.DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager) {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayBlanker blanker = new DisplayBlanker() {
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
                DisplayManagerService displayManagerService = DisplayManagerService.this;
                DisplayPowerController displayPowerController = new DisplayPowerController(DisplayManagerService.this.mContext, callbacks, handler, sensorManager, blanker);
                DisplayPowerController unused = displayManagerService.mDisplayPowerController = displayPowerController;
            }
            DisplayManagerService.this.mHandler.sendEmptyMessage(7);
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

        public void suspendSystem(boolean suspend, boolean forceUpdate) {
            DisplayManagerService.this.suspendSystemInternal(suspend, forceUpdate);
        }

        public boolean isSystemSuspending() {
            return DisplayManagerService.this.isSystemSuspendingInternal();
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
            int unused = DisplayManagerService.this.mGlobalAlpmState = globalState;
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
                int unused = DisplayManagerService.this.mTemporaryScreenBrightnessSettingOverride = brightness;
            }
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
            DisplayManagerService.this.performTraversalInternal(t);
        }

        public void setDisplayProperties(int displayId, boolean hasContent, float requestedRefreshRate, int requestedMode, boolean inTraversal) {
            DisplayManagerService.this.setDisplayPropertiesInternal(displayId, hasContent, requestedRefreshRate, requestedMode, inTraversal);
        }

        public void setDisplayOffsets(int displayId, int x, int y) {
            DisplayManagerService.this.setDisplayOffsetsInternal(displayId, x, y);
        }

        public void setDisplayAccessUIDs(SparseArray<IntArray> newDisplayAccessUIDs) {
            DisplayManagerService.this.setDisplayAccessUIDsInternal(newDisplayAccessUIDs);
        }

        public boolean isUidPresentOnDisplay(int uid, int displayId) {
            return DisplayManagerService.this.isUidPresentOnDisplayInternal(uid, displayId);
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

        public boolean hwBrightnessSetData(String name, Bundle data, int[] result) {
            boolean ret = DisplayManagerService.this.mDisplayPowerController.hwBrightnessSetData(name, data, result);
            if (ret) {
                return ret;
            }
            HwBrightnessProcessor processor = (HwBrightnessProcessor) DisplayManagerService.this.mPowerProcessors.get(name);
            if (processor != null) {
                return processor.setData(data, result);
            }
            return ret;
        }

        public boolean hwBrightnessGetData(String name, Bundle data, int[] result) {
            boolean ret = DisplayManagerService.this.mDisplayPowerController.hwBrightnessGetData(name, data, result);
            if (ret) {
                return ret;
            }
            HwBrightnessProcessor processor = (HwBrightnessProcessor) DisplayManagerService.this.mPowerProcessors.get(name);
            if (processor != null) {
                return processor.getData(data, result);
            }
            return ret;
        }

        public int setDisplayMode(int mode) {
            return ((DisplayDevice) DisplayManagerService.this.mDisplayDevices.get(0)).setDisplayState(mode);
        }

        public int getDisplayMode() {
            return ((DisplayDevice) DisplayManagerService.this.mDisplayDevices.get(0)).getDisplayState();
        }
    }

    public static final class SyncRoot {
    }

    static {
        boolean z = true;
        if (SystemProperties.get("ro.config.hw_fold_disp").isEmpty() && SystemProperties.get("persist.sys.fold.disp.size").isEmpty()) {
            z = false;
        }
        mIsFoldable = z;
    }

    public DisplayManagerService(Context context) {
        this(context, new Injector());
        loadHwBrightnessProcessors();
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
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
        this.mDefaultViewport = new DisplayViewport();
        this.mExternalTouchViewport = new DisplayViewport();
        this.mVirtualTouchViewports = new ArrayList<>();
        this.mPersistentDataStore = new PersistentDataStore();
        this.mTempCallbacks = new ArrayList<>();
        this.mTempDisplayInfo = new DisplayInfo();
        this.mTempDefaultViewport = new DisplayViewport();
        this.mTempExternalTouchViewport = new DisplayViewport();
        this.mTempVirtualTouchViewports = new ArrayList<>();
        this.mTempDisplayStateWorkQueue = new ArrayList<>();
        this.mDisplayAccessUIDs = new SparseArray<>();
        this.mIsHighPrecision = false;
        this.mGlobalAlpmState = -1;
        this.mTemporaryScreenBrightnessSettingOverride = -1;
        this.mPowerProcessors = new ArrayMap<>();
        this.mSuspendDisplay = false;
        this.mStateBeforeSuspend = 2;
        this.mBrightnessBeforeSuspend = -1;
        this.mHwDMSEx = null;
        this.mHwInnerService = new HwInnerDisplayManagerService(this);
        this.mInjector = injector;
        this.mContext = context;
        this.mHandler = new DisplayManagerHandler(DisplayThread.get().getLooper());
        this.mUiHandler = UiThread.getHandler();
        this.mDisplayAdapterListener = new DisplayAdapterListener();
        this.mSingleDisplayDemoMode = SystemProperties.getBoolean("persist.demo.singledisplay", false);
        Resources resources = this.mContext.getResources();
        this.mDefaultDisplayDefaultColorMode = this.mContext.getResources().getInteger(17694763);
        float[] lux = getFloatArray(resources.obtainTypedArray(17236017));
        float[] nits = getFloatArray(resources.obtainTypedArray(17236018));
        this.mMinimumBrightnessCurve = new Curve(lux, nits);
        this.mMinimumBrightnessSpline = Spline.createSpline(lux, nits);
        this.mIsHighPrecision = true;
        this.mCurrentUserId = 0;
        this.mHwDMSEx = HwServiceExFactory.getHwDisplayManagerServiceEx(this, context);
    }

    public void setupSchedulerPolicies() {
        Process.setThreadGroupAndCpuset(DisplayThread.get().getThreadId(), 5);
        Process.setThreadGroupAndCpuset(AnimationThread.get().getThreadId(), 5);
        Process.setThreadGroupAndCpuset(SurfaceAnimationThread.get().getThreadId(), 5);
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [com.android.server.display.DisplayManagerService$BinderService, android.os.IBinder] */
    public void onStart() {
        synchronized (this.mSyncRoot) {
            this.mPersistentDataStore.loadIfNeeded();
            loadStableDisplayValuesLocked();
        }
        this.mHandler.sendEmptyMessage(1);
        publishBinderService("display", new BinderService(), true);
        publishLocalService(DisplayManagerInternal.class, new LocalService());
        publishLocalService(DisplayTransformManager.class, new DisplayTransformManager());
    }

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
        if (this.mLocalDisplayAdapter != null) {
            this.mLocalDisplayAdapter.pcDisplayChangeService(connected);
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
        this.mHandler.sendEmptyMessage(2);
        this.mHandler.sendEmptyMessage(6);
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
            int width = res.getInteger(17694869);
            int height = res.getInteger(17694868);
            if (width > 0 && height > 0) {
                setStableDisplaySizeLocked(width, height);
                return;
            }
            return;
        }
        this.mStableDisplaySize.set(size.x, size.y);
    }

    /* access modifiers changed from: private */
    public Point getStableDisplaySizeInternal() {
        Point r = new Point();
        synchronized (this.mSyncRoot) {
            if (this.mStableDisplaySize.x > 0 && this.mStableDisplaySize.y > 0) {
                r.set(this.mStableDisplaySize.x, this.mStableDisplaySize.y);
            }
        }
        return r;
    }

    /* access modifiers changed from: private */
    public void registerDisplayTransactionListenerInternal(DisplayManagerInternal.DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.add(listener);
    }

    /* access modifiers changed from: private */
    public void unregisterDisplayTransactionListenerInternal(DisplayManagerInternal.DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.remove(listener);
    }

    /* access modifiers changed from: private */
    public void setDisplayInfoOverrideFromWindowManagerInternal(int displayId, DisplayInfo info) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null && display.setDisplayInfoOverrideFromWindowManagerLocked(info)) {
                sendDisplayEventLocked(displayId, 2);
                scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateCutoutInfoForRogInternal(int displayId) {
        synchronized (this.mSyncRoot) {
            DisplayDevice device = this.mDisplayDevices.get(displayId);
            device.updateDesityforRog();
            handleDisplayDeviceChanged(device);
        }
    }

    /* access modifiers changed from: private */
    public void getNonOverrideDisplayInfoInternal(int displayId, DisplayInfo outInfo) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                display.getNonOverrideDisplayInfoLocked(outInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        if (r0.hasNext() == false) goto L_0x0026;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0.next().onDisplayTransaction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        r0 = r2.mDisplayTransactionListeners.iterator();
     */
    @VisibleForTesting
    public void performTraversalInternal(SurfaceControl.Transaction t) {
        synchronized (this.mSyncRoot) {
            if (this.mPendingTraversal) {
                this.mPendingTraversal = false;
                performTraversalLocked(t);
            }
        }
    }

    public void suspendSystemInternal(boolean suspend, boolean forceUpdate) {
        if (!ESD_ENABLE) {
            if (HWFLOW) {
                Slog.i(TAG, "ESD_ENABLE false, do not use this method");
            }
        } else if (this.mSuspendDisplay == suspend) {
            if (HWFLOW) {
                Slog.i(TAG, "suspendSystemInternal mSuspendDisplay=" + suspend);
            }
        } else {
            this.mSuspendDisplay = suspend;
            if (this.mSuspendDisplay) {
                this.mStateBeforeSuspend = this.mGlobalDisplayState;
                this.mBrightnessBeforeSuspend = this.mGlobalDisplayBrightness;
                if (HWFLOW) {
                    Slog.i(TAG, "suspendSystem requestGlobalDisplayStateInternal off, forceUpdate=" + forceUpdate);
                }
                if (forceUpdate) {
                    requestGlobalDisplayStateInternal(1, 0);
                }
            } else {
                if (HWFLOW) {
                    Slog.i(TAG, "suspendSystem requestGlobalDisplayStateInternal on, state=" + this.mStateBeforeSuspend + ",mBrightnessBeforeSuspend=" + this.mBrightnessBeforeSuspend + ",forceUpdate=" + forceUpdate);
                }
                if (forceUpdate) {
                    requestGlobalDisplayStateInternal(this.mStateBeforeSuspend, this.mBrightnessBeforeSuspend);
                }
            }
        }
    }

    public boolean isSystemSuspendingInternal() {
        if (ESD_ENABLE) {
            return this.mSuspendDisplay;
        }
        if (HWFLOW) {
            Slog.i(TAG, "ESD_ENABLE false, do not use this method");
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00cd, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d4, code lost:
        if (r2 >= r6.mTempDisplayStateWorkQueue.size()) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d6, code lost:
        r6.mTempDisplayStateWorkQueue.get(r2).run();
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00e4, code lost:
        android.os.Trace.traceEnd(131072);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        r6.mTempDisplayStateWorkQueue.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ee, code lost:
        return;
     */
    public void requestGlobalDisplayStateInternal(int state, int brightness) {
        IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
        if (iZrHung != null) {
            ZrHungData arg = new ZrHungData();
            arg.putString("addScreenOnInfo", "Power: state=" + state + ", brightness=" + brightness);
            iZrHung.addInfo(arg);
            if (state == 2 && brightness > 0) {
                iZrHung.stop(null);
            }
        }
        if (state == 0) {
            state = 2;
        }
        if (ESD_ENABLE && this.mSuspendDisplay) {
            Slog.i(TAG, "system suspending, change request state:" + Display.stateToString(state) + "->Display.STATE_OFF");
            state = 1;
        }
        if (!this.mIsHighPrecision) {
            if (state == 1) {
                brightness = 0;
            } else if (brightness < 0) {
                brightness = -1;
            } else if (brightness > 255) {
                brightness = 255;
            }
        }
        synchronized (this.mTempDisplayStateWorkQueue) {
            try {
                synchronized (this.mSyncRoot) {
                    if (this.mGlobalAlpmState == 0) {
                        brightness = 0;
                        Slog.d(TAG, "mGlobalAlpmState == 0(in AOD mode), set brightbess = 0 ");
                    }
                    if (this.mGlobalDisplayState == state && this.mGlobalDisplayBrightness == brightness) {
                        this.mTempDisplayStateWorkQueue.clear();
                        return;
                    }
                    Trace.traceBegin(131072, "requestGlobalDisplayState(" + Display.stateToString(state) + ", brightness=" + brightness + ")");
                    this.mGlobalDisplayState = state;
                    this.mGlobalDisplayBrightness = brightness;
                    applyGlobalDisplayStateLocked(this.mTempDisplayStateWorkQueue);
                }
            } catch (Throwable th) {
                this.mTempDisplayStateWorkQueue.clear();
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        return r2;
     */
    public DisplayInfo getDisplayInfoInternal(int displayId, int callingUid) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                DisplayInfo info = display.getDisplayInfoLocked();
                if (info.hasAccess(callingUid) || isUidPresentOnDisplayInternal(callingUid, displayId)) {
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public int[] getDisplayIdsInternal(int callingUid) {
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
    public void registerCallbackInternal(IDisplayManagerCallback callback, int callingPid) {
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
    public void onCallbackDied(CallbackRecord record) {
        synchronized (this.mSyncRoot) {
            this.mCallbacks.remove(record.mPid);
            stopWifiDisplayScanLocked(record);
        }
    }

    /* access modifiers changed from: private */
    public void startWifiDisplayScanInternal(int callingPid) {
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
            if (i == 0 && this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestStartScanLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void stopWifiDisplayScanInternal(int callingPid) {
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

    /* access modifiers changed from: private */
    public void connectWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestConnectLocked(address);
            }
        }
    }

    /* access modifiers changed from: private */
    public void pauseWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestPauseLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void resumeWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestResumeLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void disconnectWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestDisconnectLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void renameWifiDisplayInternal(String address, String alias) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestRenameLocked(address, alias);
            }
        }
    }

    /* access modifiers changed from: private */
    public void forgetWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestForgetLocked(address);
            }
        }
    }

    /* access modifiers changed from: private */
    public WifiDisplayStatus getWifiDisplayStatusInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                WifiDisplayStatus wifiDisplayStatusLocked = this.mWifiDisplayAdapter.getWifiDisplayStatusLocked();
                return wifiDisplayStatusLocked;
            }
            WifiDisplayStatus wifiDisplayStatus = new WifiDisplayStatus();
            return wifiDisplayStatus;
        }
    }

    /* access modifiers changed from: private */
    public void requestColorModeInternal(int displayId, int colorMode) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (!(display == null || display.getRequestedColorModeLocked() == colorMode)) {
                display.setRequestedColorModeLocked(colorMode);
                scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setSaturationLevelInternal(float level) {
        if (level < 0.0f || level > 1.0f) {
            throw new IllegalArgumentException("Saturation level must be between 0 and 1");
        }
        ((DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class)).setColorMatrix(150, level == 1.0f ? null : computeSaturationMatrix(level));
    }

    private static float[] computeSaturationMatrix(float saturation) {
        float desaturation = 1.0f - saturation;
        float[] luminance = {0.231f * desaturation, 0.715f * desaturation, 0.072f * desaturation};
        return new float[]{luminance[0] + saturation, luminance[0], luminance[0], 0.0f, luminance[1], luminance[1] + saturation, luminance[1], 0.0f, luminance[2], luminance[2], luminance[2] + saturation, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    }

    /* access modifiers changed from: private */
    public int createVirtualDisplayInternal(IVirtualDisplayCallback callback, IMediaProjection projection, int callingUid, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) {
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
                int displayIdLocked = display.getDisplayIdLocked();
                return displayIdLocked;
            }
            Slog.w(TAG, "Rejecting request to create virtual display because the logical display was not created.");
            this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(callback.asBinder());
            handleDisplayDeviceRemovedLocked(device);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public void resizeVirtualDisplayInternal(IBinder appToken, int width, int height, int densityDpi) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.resizeVirtualDisplayLocked(appToken, width, height, densityDpi);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setVirtualDisplaySurfaceInternal(IBinder appToken, Surface surface) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.setVirtualDisplaySurfaceLocked(appToken, surface);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        return;
     */
    public void releaseVirtualDisplayInternal(IBinder appToken) {
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
    public void registerDefaultDisplayAdapters() {
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
    public void registerAdditionalDisplayAdapters() {
        synchronized (this.mSyncRoot) {
            if (shouldRegisterNonEssentialDisplayAdaptersLocked()) {
                registerOverlayDisplayAdapterLocked();
                registerWifiDisplayAdapterLocked();
            }
        }
    }

    private void registerOverlayDisplayAdapterLocked() {
        OverlayDisplayAdapter overlayDisplayAdapter = new OverlayDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mUiHandler);
        registerDisplayAdapterLocked(overlayDisplayAdapter);
    }

    private void registerWifiDisplayAdapterLocked() {
        if (this.mContext.getResources().getBoolean(17956969) || SystemProperties.getInt(FORCE_WIFI_DISPLAY_ENABLE, -1) == 1) {
            WifiDisplayAdapter wifiDisplayAdapter = new WifiDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mPersistentDataStore);
            this.mWifiDisplayAdapter = wifiDisplayAdapter;
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
    public void handleDisplayDeviceAdded(DisplayDevice device) {
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
        if (work != null) {
            work.run();
        }
        this.mVrMananger.setVRDispalyInfo(display.getDisplayInfoLocked(), display.getDisplayIdLocked());
        scheduleTraversalLocked(false);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0097, code lost:
        return;
     */
    public void handleDisplayDeviceChanged(DisplayDevice device) {
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
                    this.mPersistentDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mPersistentDataStore.saveIfNeeded();
                    throw th;
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
    public void handleDisplayDeviceRemoved(DisplayDevice device) {
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

    private void applyGlobalDisplayStateLocked(List<Runnable> workQueue) {
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i++) {
            DisplayDevice device = this.mDisplayDevices.get(i);
            if (!HwPCUtils.isPcCastModeInServer() || device.getDisplayDeviceInfoLocked().type != 2 || this.mPowerManagerInternal.shouldUpdatePCScreenState()) {
                Runnable runnable = updateDisplayStateLocked(device);
                if (runnable != null) {
                    workQueue.add(runnable);
                }
            }
        }
    }

    private Runnable updateDisplayStateLocked(DisplayDevice device) {
        if ((device.getDisplayDeviceInfoLocked().flags & 32) == 0) {
            return device.requestDisplayStateLocked(this.mGlobalDisplayState, this.mGlobalDisplayBrightness);
        }
        return null;
    }

    private LogicalDisplay addLogicalDisplayLocked(DisplayDevice device) {
        DisplayDeviceInfo deviceInfo = device.getDisplayDeviceInfoLocked();
        boolean isDefault = (deviceInfo.flags & 1) != 0;
        if (isDefault && this.mLogicalDisplays.get(0) != null) {
            Slog.w(TAG, "Ignoring attempt to add a second default display: " + deviceInfo);
            isDefault = false;
        }
        if (isDefault || !this.mSingleDisplayDemoMode) {
            int displayId = assignDisplayIdLocked(isDefault);
            LogicalDisplay display = new LogicalDisplay(displayId, assignLayerStackLocked(displayId), device);
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                Slog.w(TAG, "Ignoring display device because the logical display created from it was not considered valid: " + deviceInfo);
                return null;
            }
            configureColorModeLocked(display, device);
            if (isDefault) {
                recordStableDisplayStatsIfNeededLocked(display);
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

    /* access modifiers changed from: private */
    public void setBrightnessConfigurationForUserInternal(BrightnessConfiguration c, int userId, String packageName) {
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
    public void loadBrightnessConfiguration() {
        synchronized (this.mSyncRoot) {
            this.mDisplayPowerController.setBrightnessConfiguration(this.mPersistentDataStore.getBrightnessConfiguration(getUserManager().getUserSerialNumber(this.mCurrentUserId)));
        }
    }

    private boolean updateLogicalDisplaysLocked() {
        boolean changed = false;
        int i = this.mLogicalDisplays.size();
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return changed;
            }
            int displayId = this.mLogicalDisplays.keyAt(i2);
            LogicalDisplay display = this.mLogicalDisplays.valueAt(i2);
            this.mTempDisplayInfo.copyFrom(display.getDisplayInfoLocked());
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                this.mLogicalDisplays.removeAt(i2);
                if (this.mVrMananger.isValidVRDisplayId(displayId)) {
                    Slog.i(TAG, "disconnect vr display");
                    this.mVrMananger.setVRDisplayConnected(false);
                }
                sendDisplayEventLocked(displayId, 3);
                changed = true;
            } else if (!this.mTempDisplayInfo.equals(display.getDisplayInfoLocked())) {
                sendDisplayEventLocked(displayId, 2);
                changed = true;
            }
            i = i2;
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
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0038, code lost:
        return;
     */
    public void setDisplayPropertiesInternal(int displayId, boolean hasContent, float requestedRefreshRate, int requestedModeId, boolean inTraversal) {
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
                if (display.getRequestedModeIdLocked() != requestedModeId) {
                    display.setRequestedModeIdLocked(requestedModeId);
                    scheduleTraversalLocked(inTraversal);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        return;
     */
    public void setDisplayOffsetsInternal(int displayId, int x, int y) {
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
    public void setDisplayAccessUIDsInternal(SparseArray<IntArray> newDisplayAccessUIDs) {
        synchronized (this.mSyncRoot) {
            this.mDisplayAccessUIDs.clear();
            for (int i = newDisplayAccessUIDs.size() - 1; i >= 0; i--) {
                this.mDisplayAccessUIDs.append(newDisplayAccessUIDs.keyAt(i), newDisplayAccessUIDs.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isUidPresentOnDisplayInternal(int uid, int displayId) {
        boolean z;
        synchronized (this.mSyncRoot) {
            IntArray displayUIDs = this.mDisplayAccessUIDs.get(displayId);
            z = (displayUIDs == null || displayUIDs.indexOf(uid) == -1) ? false : true;
        }
        return z;
    }

    private void clearViewportsLocked() {
        this.mDefaultViewport.valid = false;
        this.mExternalTouchViewport.valid = false;
        this.mVirtualTouchViewports.clear();
    }

    private void configureDisplayLocked(SurfaceControl.Transaction t, DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        boolean z = false;
        boolean ownContent = (info.flags & 128) != 0;
        LogicalDisplay display = findLogicalDisplayForDeviceLocked(device);
        if (!ownContent) {
            if (display != null && !display.hasContentLocked()) {
                int displayID = display.getDisplayIdLocked();
                boolean isPCID = HwPCUtils.isValidExtDisplayId(displayID) && HwPCUtils.isPcCastModeInServerEarly();
                boolean isVRID = this.mVrMananger.isValidVRDisplayId(displayID);
                if (!isPCID && !isVRID) {
                    Slog.i(TAG, "do not mirror the default display content in pc or vr mode");
                    display = null;
                }
            }
            if (display == null && this.mVrMananger.isVRDeviceConnected() && this.mVrMananger.isVRDisplayConnected() && this.mVrMananger.isVRMode()) {
                display = this.mLogicalDisplays.get(this.mVrMananger.getVRDisplayID());
            } else if (display == null) {
                Slog.i(TAG, "Mirror the default display, device = " + device);
                display = this.mLogicalDisplays.get(0);
            }
            IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
            if ("HW-VR-Virtual-Screen".equals(info.name)) {
                display = this.mLogicalDisplays.get(0);
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
        if (!this.mDefaultViewport.valid && (info.flags & 1) != 0) {
            setViewportLocked(this.mDefaultViewport, display, device);
        }
        setExternalTouchViewport(device, info, display);
        if (!this.mExternalTouchViewport.valid && info.touch == 2 && !HwPCUtils.isPcCastModeInServer()) {
            setViewportLocked(this.mExternalTouchViewport, display, device);
        }
        if (info.touch == 3 && !TextUtils.isEmpty(info.uniqueId)) {
            setViewportLocked(getVirtualTouchViewportLocked(info.uniqueId), display, device);
        }
    }

    private void setExternalTouchViewport(DisplayDevice device, DisplayDeviceInfo info, LogicalDisplay display) {
        if (HwPCUtils.isPcCastModeInServer() && display != null) {
            HwPCUtils.log(TAG, "setExternalTouchViewport display:" + display.getDisplayIdLocked() + ", valid:" + this.mExternalTouchViewport.valid + ", flags:" + info.flags + ", isValidId:" + HwPCUtils.isValidExtDisplayId(display.getDisplayIdLocked()));
        }
        boolean isValidExternalTouchViewport = this.mExternalTouchViewport.valid;
        if (HwPCUtils.enabledInPad() && isValidExternalTouchViewport && !HwPCUtils.isValidExtDisplayId(this.mExternalTouchViewport.displayId)) {
            Slog.d(TAG, "setExternalTouchViewport isInValid displayId in PAD PC mode");
            isValidExternalTouchViewport = false;
        }
        if (HwPCUtils.isPcCastModeInServer() && !isValidExternalTouchViewport && (info.flags & 1) == 0 && display != null && HwPCUtils.isValidExtDisplayId(display.getDisplayIdLocked())) {
            try {
                if (!HwPCUtils.isPcCastMode()) {
                    return;
                }
                if (HwPCUtils.enabledInPad()) {
                    setViewportLocked(this.mExternalTouchViewport, display, device);
                    this.mExternalTouchViewport.logicalFrame.set(0, 0, info.height, info.width);
                    this.mExternalTouchViewport.deviceHeight = info.width;
                    this.mExternalTouchViewport.deviceWidth = info.height;
                    this.mExternalTouchViewport.displayId = display.getDisplayIdLocked();
                    this.mExternalTouchViewport.valid = true;
                    return;
                }
                setViewportLocked(this.mExternalTouchViewport, display, device);
                this.mExternalTouchViewport.logicalFrame.set(0, 0, info.width, info.height);
                this.mExternalTouchViewport.deviceHeight = info.height;
                this.mExternalTouchViewport.deviceWidth = info.width;
                this.mExternalTouchViewport.displayId = display.getDisplayIdLocked();
                this.mExternalTouchViewport.valid = true;
            } catch (Exception e) {
                Slog.w(TAG, "when set external touch view port", e);
            }
        }
    }

    private void loadHwBrightnessProcessors() {
    }

    private DisplayViewport getVirtualTouchViewportLocked(String uniqueId) {
        int count = this.mVirtualTouchViewports.size();
        for (int i = 0; i < count; i++) {
            DisplayViewport viewport = this.mVirtualTouchViewports.get(i);
            if (uniqueId.equals(viewport.uniqueId)) {
                return viewport;
            }
        }
        DisplayViewport viewport2 = new DisplayViewport();
        viewport2.uniqueId = uniqueId;
        this.mVirtualTouchViewports.add(viewport2);
        return viewport2;
    }

    private static void setViewportLocked(DisplayViewport viewport, LogicalDisplay display, DisplayDevice device) {
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
    public void scheduleTraversalLocked(boolean inTraversal) {
        if (!this.mPendingTraversal && this.mWindowManagerInternal != null) {
            this.mPendingTraversal = true;
            if (!inTraversal) {
                this.mHandler.sendEmptyMessage(4);
            }
        }
    }

    /* access modifiers changed from: private */
    public void deliverDisplayEvent(int displayId, int event) {
        int count;
        int i;
        synchronized (this.mSyncRoot) {
            count = this.mCallbacks.size();
            this.mTempCallbacks.clear();
            i = 0;
            for (int i2 = 0; i2 < count; i2++) {
                this.mTempCallbacks.add(this.mCallbacks.valueAt(i2));
            }
        }
        int count2 = count;
        while (true) {
            int i3 = i;
            if (i3 < count2) {
                this.mTempCallbacks.get(i3).notifyDisplayEventAsync(displayId, event);
                i = i3 + 1;
            } else {
                this.mTempCallbacks.clear();
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public IMediaProjectionManager getProjectionService() {
        if (this.mProjectionService == null) {
            this.mProjectionService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        }
        return this.mProjectionService;
    }

    /* access modifiers changed from: private */
    public UserManager getUserManager() {
        return (UserManager) this.mContext.getSystemService(UserManager.class);
    }

    /* access modifiers changed from: private */
    public void dumpInternal(PrintWriter pw) {
        pw.println("DISPLAY MANAGER (dumpsys display)");
        synchronized (this.mSyncRoot) {
            pw.println("  mOnlyCode=" + this.mOnlyCore);
            pw.println("  mSafeMode=" + this.mSafeMode);
            pw.println("  mPendingTraversal=" + this.mPendingTraversal);
            pw.println("  mGlobalDisplayState=" + Display.stateToString(this.mGlobalDisplayState));
            pw.println("  mNextNonDefaultDisplayId=" + this.mNextNonDefaultDisplayId);
            pw.println("  mDefaultViewport=" + this.mDefaultViewport);
            pw.println("  mExternalTouchViewport=" + this.mExternalTouchViewport);
            pw.println("  mVirtualTouchViewports=" + this.mVirtualTouchViewports);
            pw.println("  mDefaultDisplayDefaultColorMode=" + this.mDefaultDisplayDefaultColorMode);
            pw.println("  mSingleDisplayDemoMode=" + this.mSingleDisplayDemoMode);
            pw.println("  mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
            pw.println("  mStableDisplaySize=" + this.mStableDisplaySize);
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
    public DisplayDeviceInfo getDisplayDeviceInfoInternal(int displayId) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display == null) {
                return null;
            }
            DisplayDeviceInfo displayDeviceInfoLocked = display.getPrimaryDisplayDeviceLocked().getDisplayDeviceInfoLocked();
            return displayDeviceInfoLocked;
        }
    }

    public SyncRoot getLock() {
        return this.mSyncRoot;
    }

    public WifiDisplayAdapter getWifiDisplayAdapter() {
        return this.mWifiDisplayAdapter;
    }

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
            if (i == 0 && this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestStartScanLocked(channelID);
            }
        }
    }
}
