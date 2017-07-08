package com.android.server.power;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.server.BatteryService;
import com.android.server.HwInputMethodManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.DisplayManagerService;
import com.android.server.input.HwCircleAnimation;
import com.android.server.lights.LightsService;
import com.android.server.pfw.HwPFWService;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.power.PowerManagerService.WakeLock;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import libcore.util.Objects;

public class HwPowerManagerService extends PowerManagerService {
    private static final String COLOR_TEMPERATURE = "color_temperature";
    private static final int COLOR_TEMPERATURE_DEFAULT = 128;
    private static final String COLOR_TEMPERATURE_RGB = "color_temperature_rgb";
    private static final int MAXINUM_TEMPERATURE = 255;
    private static final int MODE_COLOR_TEMP_3_DIMENSION = 1;
    private static final String TAG = "HwPowerManagerService";
    private static boolean mLoadLibraryFailed;
    CoverManager mCm;
    private final Context mContext;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private FingerSenseObserver mFingerSenseObserver;
    private final ArrayList<WakeLock> mForceReleasedWakeLocks;
    private WindowManagerPolicy mPolicy;
    private final ArrayList<ProxyWLProcessInfo> mProxyWLProcessList;
    private final ArrayList<WakeLock> mProxyedWakeLocks;
    private SettingsObserver mSettingsObserver;
    private boolean mSystemReady;
    private int mWaitBrightTimeout;

    private final class BluetoothReceiver extends BroadcastReceiver {
        private BluetoothReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            long now = SystemClock.uptimeMillis();
            BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int newState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
            int oldState = intent.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", 0);
            Slog.d(HwPowerManagerService.TAG, "BluetoothReceiver,btDevice:" + btDevice + ",newState:" + newState + ",oldState:" + oldState + ",intent:" + intent.getAction());
            boolean needWakeUp = false;
            if (btDevice != null && newState == 2 && oldState == HwPowerManagerService.MODE_COLOR_TEMP_3_DIMENSION) {
                BluetoothClass btClass = btDevice.getBluetoothClass();
                Slog.d(HwPowerManagerService.TAG, "BluetoothReceiver btClass.getDeviceClass():" + btClass);
                if (btClass != null) {
                    switch (btClass.getDeviceClass()) {
                        case HwGlobalActionsData.FLAG_SILENTMODE_NORMAL /*1024*/:
                        case 1028:
                        case 1032:
                        case 1040:
                        case 1044:
                        case 1048:
                        case 1052:
                        case 1056:
                        case 1060:
                        case 1064:
                        case 1068:
                        case 1072:
                        case 1076:
                        case 1080:
                        case 1084:
                        case 1088:
                        case 1096:
                            needWakeUp = true;
                            break;
                    }
                    if (needWakeUp) {
                        synchronized (HwPowerManagerService.this.mLock) {
                            if (HwPowerManagerService.this.wakeUpNoUpdateLocked(now, "bluetooth.connected", IOTController.TYPE_MASTER, HwPowerManagerService.this.mContext.getOpPackageName(), IOTController.TYPE_MASTER)) {
                                HwPowerManagerService.this.updatePowerStateLocked();
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class FingerSenseObserver extends ContentObserver {
        private ContentResolver resolver;

        public FingerSenseObserver(Handler handler, ContentResolver resolver) {
            super(handler);
            this.resolver = resolver;
        }

        public void observe() {
            this.resolver.registerContentObserver(Global.getUriFor("fingersense_enabled"), false, this, -1);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            if (Global.getInt(this.resolver, "fingersense_enabled", HwPowerManagerService.MODE_COLOR_TEMP_3_DIMENSION) != HwPowerManagerService.MODE_COLOR_TEMP_3_DIMENSION) {
                z = false;
            }
            HwPowerManagerService.nativeSetFsEnable(z);
        }
    }

    private final class HeadsetReceiver extends BroadcastReceiver {
        private HeadsetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (HwPowerManagerService.this.mLock) {
                long now = SystemClock.uptimeMillis();
                Slog.d(HwPowerManagerService.TAG, "HeadsetReceiver,state:" + intent.getIntExtra("state", 0));
                if (intent.getIntExtra("state", 0) == HwPowerManagerService.MODE_COLOR_TEMP_3_DIMENSION && HwPowerManagerService.this.wakeUpNoUpdateLocked(now, "headset.connected", IOTController.TYPE_MASTER, HwPowerManagerService.this.mContext.getOpPackageName(), IOTController.TYPE_MASTER)) {
                    HwPowerManagerService.this.updatePowerStateLocked();
                }
            }
        }
    }

    private static final class ProxyWLProcessInfo {
        public int mPid;
        public int mUid;

        public ProxyWLProcessInfo(int pid, int uid) {
            this.mPid = pid;
            this.mUid = uid;
        }

        public boolean isSameProcess(int pid, int uid) {
            boolean z = true;
            if (this.mPid != pid && -1 != pid) {
                return false;
            }
            if (!(this.mUid == uid || -1 == uid)) {
                z = false;
            }
            return z;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (HwPowerManagerService.this.mLock) {
                HwPowerManagerService.this.setColorTemperatureAccordingToSetting();
            }
        }
    }

    private static native void finalize_native();

    private static native void init_native();

    private static native int nativeGetDisplayFeatureSupported(int i);

    public static native String nativeReadColorTemperatureNV();

    private native int nativeSetColorTemperature(int i);

    private native int nativeUpdateRgbGamma(float f, float f2, float f3);

    static {
        mLoadLibraryFailed = false;
        try {
            System.loadLibrary("hwpwmanager_jni");
        } catch (UnsatisfiedLinkError e) {
            mLoadLibraryFailed = true;
            Slog.d(TAG, "hwpwmanager_jni library not found!");
        }
    }

    public void init(Context context, LightsService ls, ActivityManagerService am, BatteryService bs, IBatteryStats bss, IAppOpsService appOps, DisplayManagerService dm) {
    }

    public HwPowerManagerService(Context context) {
        super(context);
        this.mWaitBrightTimeout = 3000;
        this.mCm = null;
        this.mSystemReady = false;
        this.mProxyWLProcessList = new ArrayList();
        this.mProxyedWakeLocks = new ArrayList();
        this.mForceReleasedWakeLocks = new ArrayList();
        this.mContext = context;
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(this.mContext);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        if (!mLoadLibraryFailed) {
            init_native();
        }
    }

    protected void finalize() {
        if (!mLoadLibraryFailed) {
            finalize_native();
        }
        try {
            super.finalize();
        } catch (Throwable th) {
        }
    }

    public int setColorTemperatureInternal(int colorTemper) {
        Slog.d(TAG, "setColorTemperature:" + colorTemper);
        try {
            if (!mLoadLibraryFailed) {
                return nativeSetColorTemperature(colorTemper);
            }
            Slog.d(TAG, "nativeSetColorTemperature not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativeSetColorTemperature not found!");
            return -1;
        }
    }

    public boolean isDisplayFeatureSupported(int feature) {
        boolean z = false;
        Slog.d(TAG, "isDisplayFeatureSupported feature:" + feature);
        try {
            if (mLoadLibraryFailed) {
                Slog.d(TAG, "Display feature not supported because of library not found!");
                return false;
            }
            if (nativeGetDisplayFeatureSupported(feature) != 0) {
                z = true;
            }
            return z;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "Display feature not supported because of exception!");
            return false;
        }
    }

    protected void setColorTemperatureAccordingToSetting() {
        Slog.d(TAG, "setColorTemperatureAccordingToSetting");
        int operation;
        if (isDisplayFeatureSupported(MODE_COLOR_TEMP_3_DIMENSION)) {
            Slog.d(TAG, "setColorTemperatureAccordingToSetting new.");
            try {
                String ctNewRGB = System.getStringForUser(this.mContext.getContentResolver(), COLOR_TEMPERATURE_RGB, -2);
                if (ctNewRGB != null) {
                    List<String> rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
                    float red = Float.valueOf((String) rgbarryList.get(0)).floatValue();
                    float green = Float.valueOf((String) rgbarryList.get(MODE_COLOR_TEMP_3_DIMENSION)).floatValue();
                    float blue = Float.valueOf((String) rgbarryList.get(2)).floatValue();
                    Slog.d(TAG, "ColorTemperature read from setting:" + ctNewRGB + red + green + blue);
                    updateRgbGammaInternal(red, green, blue);
                } else {
                    operation = System.getIntForUser(this.mContext.getContentResolver(), COLOR_TEMPERATURE, COLOR_TEMPERATURE_DEFAULT, -2);
                    Slog.d(TAG, "ColorTemperature read from old setting:" + operation);
                    setColorTemperatureInternal(operation);
                }
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "ColorTemperature read from setting exception!");
                updateRgbGammaInternal(HwCircleAnimation.SMALL_ALPHA, HwCircleAnimation.SMALL_ALPHA, HwCircleAnimation.SMALL_ALPHA);
            }
        } else {
            operation = System.getIntForUser(this.mContext.getContentResolver(), COLOR_TEMPERATURE, COLOR_TEMPERATURE_DEFAULT, -2);
            Slog.d(TAG, "setColorTemperatureAccordingToSetting old:" + operation);
            setColorTemperatureInternal(operation);
        }
    }

    public int updateRgbGammaInternal(float red, float green, float blue) {
        Slog.d(TAG, "updateRgbGammaInternal:red=" + red + " green=" + green + " blue=" + blue);
        try {
            if (!mLoadLibraryFailed) {
                return nativeUpdateRgbGamma(red, green, blue);
            }
            Slog.d(TAG, "nativeUpdateRgbGamma not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativeUpdateRgbGamma not found!");
            return -1;
        }
    }

    private static boolean isMultiSimEnabled() {
        return false;
    }

    private boolean isPhoneInCall() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i += MODE_COLOR_TEMP_3_DIMENSION) {
                if (TelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManager.getDefault().getCallState(SubscriptionManager.getDefaultSubscriptionId()) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public void systemReady(IAppOpsService appOps) {
        super.systemReady(appOps);
        this.mPolicy = (WindowManagerPolicy) getLocalService(WindowManagerPolicy.class);
        this.mCm = new CoverManager();
        this.mSystemReady = true;
        setColorTemperatureAccordingToSetting();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        ContentResolver resolver = this.mContext.getContentResolver();
        if (isDisplayFeatureSupported(MODE_COLOR_TEMP_3_DIMENSION)) {
            resolver.registerContentObserver(System.getUriFor(COLOR_TEMPERATURE_RGB), false, this.mSettingsObserver, -1);
        } else {
            resolver.registerContentObserver(System.getUriFor(COLOR_TEMPERATURE), false, this.mSettingsObserver, -1);
        }
        this.mFingerSenseObserver = new FingerSenseObserver(this.mHandler, resolver);
        this.mFingerSenseObserver.observe();
        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction("android.intent.action.HEADSET_PLUG");
        this.mContext.registerReceiver(new HeadsetReceiver(), headsetFilter, null, this.mHandler);
        headsetFilter = new IntentFilter();
        headsetFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        headsetFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        this.mContext.registerReceiver(new BluetoothReceiver(), headsetFilter, null, this.mHandler);
    }

    public int getAdjustedMaxTimeout(int oldtimeout, int maxv) {
        if (this.mCm == null || this.mPolicy == null || this.mCm.isCoverOpen() || this.mPolicy.isKeyguardLocked() || isPhoneInCall()) {
            return 0;
        }
        return LifeCycleStateMachine.TIME_OUT_TIME;
    }

    private void restoreProxyWakeLockLocked(int pid, int uid) {
        for (int i = this.mProxyedWakeLocks.size() - 1; i >= 0; i--) {
            WakeLock wakelock = (WakeLock) this.mProxyedWakeLocks.get(i);
            if (!((wakelock.mOwnerUid == uid || -1 == uid) && (wakelock.mOwnerPid == pid || -1 == uid))) {
                if (wakelock.mWorkSource != null && wakelock.mWorkSource.get(0) == uid) {
                }
            }
            acquireWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mWorkSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid);
            this.mProxyedWakeLocks.remove(i);
        }
    }

    private void removeProxyWakeLockProcessLocked(int pid, int uid) {
        for (int i = this.mProxyWLProcessList.size() - 1; i >= 0; i--) {
            if (((ProxyWLProcessInfo) this.mProxyWLProcessList.get(i)).isSameProcess(pid, uid)) {
                if (DEBUG_SPEW) {
                    Log.d(TAG, "remove pxy wl, pid: " + pid + ", uid: " + uid + " from pxy process list.");
                }
                this.mProxyWLProcessList.remove(i);
            }
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        synchronized (this.mLock) {
            if (proxy) {
                this.mProxyWLProcessList.add(new ProxyWLProcessInfo(pid, uid));
            } else {
                restoreProxyWakeLockLocked(pid, uid);
                removeProxyWakeLockProcessLocked(pid, uid);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean acquireProxyWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        if (this.mSystemReady) {
            int iPid = pid;
            int iUid = uid;
            if (ws != null) {
                iPid = -1;
                iUid = ws.get(0);
            }
            synchronized (this.mLock) {
                int i = 0;
                while (true) {
                    if (i >= this.mProxyWLProcessList.size()) {
                        return false;
                    } else if (((ProxyWLProcessInfo) this.mProxyWLProcessList.get(i)).isSameProcess(iPid, iUid)) {
                        break;
                    } else {
                        i += MODE_COLOR_TEMP_3_DIMENSION;
                    }
                }
                if (DEBUG_SPEW) {
                    Log.d(TAG, "acquire pxy wl, pid: " + pid + ", uid: " + uid + ", ws: " + ws + ", packageName: " + packageName + ", tag: " + tag);
                }
                this.mProxyedWakeLocks.add(new WakeLock(this, lock, flags, tag, packageName, ws, historyTag, uid, pid));
                return true;
            }
        }
        Log.w(TAG, "acquireProxyWakeLock, mSystemReady is false.");
        return false;
    }

    private int findProxyWakeLockIndexLocked(IBinder lock) {
        int count = this.mProxyedWakeLocks.size();
        for (int i = 0; i < count; i += MODE_COLOR_TEMP_3_DIMENSION) {
            if (((WakeLock) this.mProxyedWakeLocks.get(i)).mLock == lock) {
                return i;
            }
        }
        return -1;
    }

    protected boolean updateProxyWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        synchronized (this.mLock) {
            int index = findProxyWakeLockIndexLocked(lock);
            if (index < 0) {
                return false;
            }
            WakeLock wakeLock = (WakeLock) this.mProxyedWakeLocks.get(index);
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateProxyWakeLockWorkSource: lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], ws=" + ws + ", curr.ws: " + wakeLock.mWorkSource);
            }
            if (!wakeLock.hasSameWorkSource(ws)) {
                wakeLock.mHistoryTag = historyTag;
                wakeLock.updateWorkSource(ws);
            }
            return true;
        }
    }

    private boolean releaseWakeLockFromListLocked(IBinder lock, ArrayList<WakeLock> list) {
        int length = list.size();
        boolean ret = false;
        for (int i = length - 1; i >= 0; i--) {
            WakeLock wakelock = (WakeLock) list.get(i);
            if (wakelock.mLock == lock) {
                if (DEBUG_SPEW) {
                    Log.d(TAG, "release pxy wl: " + wakelock + " from list, length: " + length);
                }
                list.remove(i);
                ret = true;
            }
        }
        return ret;
    }

    protected boolean releaseProxyWakeLock(IBinder lock) {
        if (this.mSystemReady) {
            boolean ret;
            synchronized (this.mLock) {
                ret = releaseWakeLockFromListLocked(lock, this.mProxyedWakeLocks) | releaseWakeLockFromListLocked(lock, this.mForceReleasedWakeLocks);
            }
            return ret;
        }
        Log.w(TAG, "releaseProxyWakeLock, mSystemReady is false.");
        return false;
    }

    private void releaseWakeLockInternalLocked(IBinder lock, int flags) {
        int index = findWakeLockIndexLocked(lock);
        if (index < 0) {
            Slog.w(TAG, "releaseWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + " [not found], flags=0x" + Integer.toHexString(flags));
            return;
        }
        WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(index);
        if (DEBUG_SPEW) {
            Slog.d(TAG, "releaseWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + wakeLock.mTag + "\", packageName=" + wakeLock.mPackageName + "\", ws=" + wakeLock.mWorkSource + ", uid=" + wakeLock.mOwnerUid + ", pid=" + wakeLock.mOwnerPid);
        }
        if ((flags & MODE_COLOR_TEMP_3_DIMENSION) != 0) {
            this.mRequestWaitForNegativeProximity = true;
        }
        wakeLock.mLock.unlinkToDeath(wakeLock, 0);
        removeWakeLockLocked(wakeLock, index);
    }

    private void acquireWakeLockInternalLocked(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "acquireWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + tag + "\", packageName=" + packageName + "\", ws=" + ws + ", uid=" + uid + ", pid=" + pid);
        }
        if (lock.isBinderAlive()) {
            WakeLock wakeLock;
            boolean notifyAcquire;
            int index = findWakeLockIndexLocked(lock);
            if (index >= 0) {
                if (DEBUG_SPEW) {
                    Slog.d(TAG, "acquireWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", existing wakelock");
                }
                wakeLock = (WakeLock) this.mWakeLocks.get(index);
                if (!wakeLock.hasSameProperties(flags, tag, ws, uid, pid)) {
                    notifyWakeLockChangingLocked(wakeLock, flags, tag, packageName, uid, pid, ws, historyTag);
                    wakeLock.updateProperties(flags, tag, packageName, ws, historyTag, uid, pid);
                }
                notifyAcquire = false;
            } else {
                wakeLock = new WakeLock(this, lock, flags, tag, packageName, ws, historyTag, uid, pid);
                try {
                    lock.linkToDeath(wakeLock, 0);
                    this.mWakeLocks.add(wakeLock);
                    setWakeLockDisabledStateLocked(wakeLock);
                    notifyAcquire = true;
                } catch (RemoteException e) {
                    throw new IllegalArgumentException("Wake lock is already dead.");
                }
            }
            applyWakeLockFlagsOnAcquireLocked(wakeLock, uid);
            this.mDirty |= MODE_COLOR_TEMP_3_DIMENSION;
            updatePowerStateLocked();
            if (notifyAcquire) {
                notifyWakeLockAcquiredLocked(wakeLock);
            }
            return;
        }
        Slog.w(TAG, "lock:" + Objects.hashCode(lock) + " is already dead, tag=\"" + tag + "\", packageName=" + packageName + ", ws=" + ws + ", uid=" + uid + ", pid=" + pid);
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
        synchronized (this.mLock) {
            for (int i = this.mWakeLocks.size() - 1; i >= 0; i--) {
                WakeLock wakelock = (WakeLock) this.mWakeLocks.get(i);
                if (wakelock.mWorkSource != null) {
                    int length = wakelock.mWorkSource.size();
                    if (MODE_COLOR_TEMP_3_DIMENSION == length) {
                        if (wakelock.mWorkSource.get(0) == uid) {
                            if (DEBUG_SPEW) {
                                Log.d(TAG, "forceReleaseWakeLockByPidUid, last one, wakelock: " + wakelock);
                            }
                            this.mForceReleasedWakeLocks.add(wakelock);
                            releaseWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags);
                        }
                    } else if (length > MODE_COLOR_TEMP_3_DIMENSION) {
                        for (int j = 0; j < length; j += MODE_COLOR_TEMP_3_DIMENSION) {
                            if (wakelock.mWorkSource.get(j) == uid) {
                                WorkSource workSource;
                                if (DEBUG_SPEW) {
                                    Log.d(TAG, "forceReleaseWakeLockByPidUid, more than one, wakelock: " + wakelock);
                                }
                                String name = wakelock.mWorkSource.getName(j);
                                if (name == null) {
                                    workSource = new WorkSource(uid);
                                } else {
                                    workSource = new WorkSource(uid, name);
                                }
                                this.mForceReleasedWakeLocks.add(new WakeLock(this, wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, workSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid));
                                WorkSource workSource2 = new WorkSource(wakelock.mWorkSource);
                                workSource2.remove(workSource);
                                WakeLock wakeLock = wakelock;
                                notifyWakeLockChangingLocked(wakeLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mOwnerUid, wakelock.mOwnerPid, workSource2, wakelock.mHistoryTag);
                                wakelock.mWorkSource.remove(workSource);
                            }
                        }
                    } else {
                        Log.e(TAG, "forceReleaseWakeLockByPidUid, length invalid: " + length);
                    }
                } else if (wakelock.mWorkSource == null && wakelock.mOwnerPid == pid && wakelock.mOwnerUid == uid) {
                    if (DEBUG_SPEW) {
                        Log.d(TAG, "forceReleaseWakeLockByPidUid, ws null, pid: " + pid + ", uid: " + uid + ", wakelock: " + wakelock);
                    }
                    this.mForceReleasedWakeLocks.add(wakelock);
                    releaseWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags);
                }
            }
        }
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
        synchronized (this.mLock) {
            for (int i = this.mForceReleasedWakeLocks.size() - 1; i >= 0; i--) {
                WakeLock wakelock = (WakeLock) this.mForceReleasedWakeLocks.get(i);
                if (wakelock.mWorkSource == null) {
                    if ((wakelock.mOwnerPid == pid || -1 == pid) && (wakelock.mOwnerUid == uid || -1 == uid)) {
                        if (DEBUG_SPEW) {
                            Log.d(TAG, "forceRestoreWakeLockByPidUid, WorkSource == null, wakelock: " + wakelock);
                        }
                        acquireWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mWorkSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid);
                        this.mForceReleasedWakeLocks.remove(i);
                    }
                } else if (wakelock.mWorkSource.get(0) == uid || -1 == uid) {
                    int index = findWakeLockIndexLocked(wakelock.mLock);
                    if (index < 0) {
                        if (DEBUG_SPEW) {
                            Log.d(TAG, "forceRestoreWakeLockByPidUid, not found base, wakelock: " + wakelock);
                        }
                        acquireWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mWorkSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid);
                    } else {
                        if (DEBUG_SPEW) {
                            Log.d(TAG, "forceRestoreWakeLockByPidUid, update exist, wakelock: " + wakelock);
                        }
                        WorkSource newWorkSource = new WorkSource(((WakeLock) this.mWakeLocks.get(index)).mWorkSource);
                        newWorkSource.add(wakelock.mWorkSource);
                        notifyWakeLockChangingLocked((WakeLock) this.mWakeLocks.get(index), wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mOwnerUid, wakelock.mOwnerPid, newWorkSource, wakelock.mHistoryTag);
                        ((WakeLock) this.mWakeLocks.get(index)).mWorkSource.add(wakelock.mWorkSource);
                    }
                    this.mForceReleasedWakeLocks.remove(i);
                }
            }
        }
    }

    public boolean getWakeLockByUid(int uid, int wakeflag) {
        synchronized (this.mLock) {
            if (this.mWakeLocks.size() <= 0) {
                return false;
            }
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                WakeLock wl = (WakeLock) it.next();
                if (wl.mOwnerUid == uid) {
                    if (-1 == wakeflag || (wl.mFlags & 65535) == wakeflag) {
                        return true;
                    }
                } else if (wl.mWorkSource != null) {
                    int size = wl.mWorkSource.size();
                    for (int i = 0; i < size; i += MODE_COLOR_TEMP_3_DIMENSION) {
                        if (uid == wl.mWorkSource.get(i) && (-1 == wakeflag || (wl.mFlags & 65535) == wakeflag)) {
                            Log.d(TAG, "worksource not null, i:" + i + ", size: " + size + ", flags: " + wl.mFlags);
                            return true;
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            }
            return false;
        }
    }

    public void setLcdRatio(int ratio, boolean autoAdjust) {
        this.mLightsManager.getLight(0).setLcdRatio(ratio, autoAdjust);
    }

    public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        this.mLightsManager.getLight(0).configBrightnessRange(ratioMin, ratioMax, autoLimit);
    }

    private void enableBrightnessWaitLocked() {
        if (!this.mBrightnessWaitModeEnabled) {
            this.mBrightnessWaitModeEnabled = true;
            this.mBrightnessWaitRet = false;
            this.mDirty |= HwInputMethodManagerService.SECURE_IME_NO_HIDE_FLAG;
            Message msg = this.mHandler.obtainMessage(4);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, (long) this.mWaitBrightTimeout);
        }
    }

    protected void disableBrightnessWaitLocked(boolean dismissKeyguard) {
        if (this.mBrightnessWaitModeEnabled) {
            this.mHandler.removeMessages(4);
            this.mBrightnessWaitModeEnabled = false;
            this.mBrightnessWaitRet = dismissKeyguard;
            this.mDirty |= HwInputMethodManagerService.SECURE_IME_NO_HIDE_FLAG;
        }
    }

    protected void startWakeUpReadyInternal(long eventTime, int uid, String opPackageName) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "startWakeUpReadyInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled);
            }
            if (this.mBrightnessWaitModeEnabled) {
                resetWaitBrightTimeoutLocked();
            } else {
                if (wakeUpNoUpdateWithoutInteractiveLocked(eventTime, "startWakeUpReady", uid, opPackageName)) {
                    enableBrightnessWaitLocked();
                    updatePowerStateLocked();
                }
            }
        }
    }

    protected void resetWaitBrightTimeoutLocked() {
        this.mHandler.removeMessages(4);
        Message msg = this.mHandler.obtainMessage(4);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, (long) this.mWaitBrightTimeout);
    }

    protected void stopWakeUpReadyInternal(long eventTime, int uid, boolean enableBright, String opPackageName) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "stopWakeUpReadyInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled + " enableBright = " + enableBright);
            }
            if (this.mBrightnessWaitModeEnabled) {
                if (enableBright) {
                    setWakefulnessLocked(MODE_COLOR_TEMP_3_DIMENSION, 0);
                    disableBrightnessWaitLocked(true);
                    updatePowerStateLocked();
                } else {
                    goToSleepNoUpdateLocked(eventTime, 0, 0, uid);
                    updatePowerStateLocked();
                }
            } else if (enableBright) {
                if (DEBUG) {
                    Slog.d(TAG, "stopWakeUpReadyInternal, brightness wait timeout.");
                }
                if (wakeUpNoUpdateLocked(eventTime, "BrightnessWaitTimeout", uid, opPackageName, uid)) {
                    updatePowerStateLocked();
                }
            }
        }
    }

    private boolean wakeUpNoUpdateWithoutInteractiveLocked(long eventTime, String reason, int uid, String opPackageName) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "wakeUpNoUpdateWithoutInteractiveLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastSleepTime || this.mWakefulness == MODE_COLOR_TEMP_3_DIMENSION || !this.mBootCompleted || !this.mSystemReady || this.mProximityPositive) {
            return false;
        }
        Trace.traceBegin(131072, "wakeUpWithoutInteractive");
        try {
            switch (this.mWakefulness) {
                case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                    Slog.i(TAG, "Waking up from sleep (uid " + uid + ")...");
                    Jlog.d(5, "JL_PMS_WAKEFULNESS_ASLEEP");
                    break;
                case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                    Slog.i(TAG, "Waking up from dream (uid " + uid + ")...");
                    Jlog.d(6, "JL_PMS_WAKEFULNESS_DREAMING");
                    break;
                case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                    Slog.i(TAG, "Waking up from dozing (uid " + uid + ")...");
                    Jlog.d(7, "JL_PMS_WAKEFULNESS_NAPPING");
                    break;
            }
            this.mLastWakeTime = eventTime;
            this.mDirty |= 2;
            this.mWakefulness = MODE_COLOR_TEMP_3_DIMENSION;
            this.mNotifier.onWakeUp(reason, uid, opPackageName, uid);
            userActivityNoUpdateLocked(eventTime, 0, 0, uid);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    protected void handleWaitBrightTimeout() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "handleWaitBrightTimeout mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled + " mWakefulness = " + this.mWakefulness);
            }
            if (this.mBrightnessWaitModeEnabled) {
                goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 8, 0, IOTController.TYPE_MASTER);
                updatePowerStateLocked();
            }
        }
    }

    public boolean isAppWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.isAppWakeLockFilterTag(flags, packageName, ws);
        }
        return false;
    }

    public boolean isSkipWakeLockUsing(int uid, String tag) {
        synchronized (this.mLock) {
            if (this.mWakeLocks.size() <= 0) {
                return false;
            }
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                WakeLock wl = (WakeLock) it.next();
                if (wl.mOwnerUid == uid) {
                    if (tag.equals(wl.mTag)) {
                        return true;
                    }
                } else if (wl.mWorkSource != null) {
                    int size = wl.mWorkSource.size();
                    for (int i = 0; i < size; i += MODE_COLOR_TEMP_3_DIMENSION) {
                        if (uid == wl.mWorkSource.get(i) && tag.equals(wl.mTag)) {
                            return true;
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            }
            return false;
        }
    }

    public void dumpInternal(PrintWriter pw) {
        super.dumpInternal(pw);
        pw.println("Proxyed WakeLocks State");
        synchronized (this.mLock) {
            for (WakeLock wl : this.mProxyedWakeLocks) {
                pw.println(" Proxyed WakeLocks :" + wl);
            }
            for (WakeLock wl2 : this.mForceReleasedWakeLocks) {
                pw.println(" Force Released WakeLocks :" + wl2);
            }
        }
    }

    protected void sendTempBrightnessToMonitor(String paramType, int brightness) {
        if (this.mDisplayEffectMonitor != null) {
            String[] packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()).split(":");
            if (packageName.length != 0) {
                ArrayMap<String, Object> params = new ArrayMap();
                params.put("paramType", paramType);
                params.put("brightness", Integer.valueOf(brightness));
                params.put(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName[0]);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    protected void sendBrightnessModeToMonitor(boolean manualMode) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put("paramType", "brightnessMode");
            params.put("brightnessMode", Boolean.valueOf(manualMode));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    protected void sendManualBrightnessToMonitor(int brightness) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put("paramType", "manualBrightness");
            params.put("brightness", Integer.valueOf(brightness));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }
}
