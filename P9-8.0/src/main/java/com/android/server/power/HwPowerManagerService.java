package com.android.server.power;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManagerInternal;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.DisplayEffectMonitor.MonitorModule;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.Utils;
import com.android.server.lights.LightsService;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.pg.PGManagerInternal;
import com.android.server.policy.PickUpWakeScreenManager;
import com.android.server.power.PowerManagerService.UidState;
import com.android.server.power.PowerManagerService.WakeLock;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.huawei.IntelliServer.intellilib.IIntelliListener;
import com.huawei.IntelliServer.intellilib.IIntelliService;
import com.huawei.IntelliServer.intellilib.IIntelliService.Stub;
import com.huawei.IntelliServer.intellilib.IntelliAlgoResult;
import com.huawei.displayengine.DisplayEngineManager;
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
    private static final int INTELLI_DETECT_FACE_PRESENCE = 1;
    private static final int MAXINUM_TEMPERATURE = 255;
    private static final int MODE_COLOR_TEMP_3_DIMENSION = 1;
    private static final String SERVICE_ACTION = "com.huawei.intelliServer.intelliServer";
    private static final String SERVICE_CLASS = "com.huawei.intelliServer.intelliServer.IntelliService";
    private static final String SERVICE_PACKAGE = "com.huawei.intelliServer.intelliServer";
    private static final String TAG = "HwPowerManagerService";
    private static final int TYPE_FACE_STAY_LIT = 3;
    private static boolean mLoadLibraryFailed;
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (HwPowerManagerService.this.mIntelliServiceBound) {
                if (HwPowerManagerService.DEBUG) {
                    Slog.d(HwPowerManagerService.TAG, "IntelliService Connected, mShouldFaceDetectLater=" + HwPowerManagerService.this.mShouldFaceDetectLater);
                }
                HwPowerManagerService.this.mIRemote = Stub.asInterface(iBinder);
                if (HwPowerManagerService.this.mShouldFaceDetectLater) {
                    HwPowerManagerService.this.mShouldFaceDetectLater = false;
                    HwPowerManagerService.this.registerFaceDetect();
                }
                return;
            }
            Slog.w(HwPowerManagerService.TAG, "IntelliService not bound, ignore.");
        }

        public void onServiceDisconnected(ComponentName componentName) {
            if (HwPowerManagerService.DEBUG) {
                Slog.d(HwPowerManagerService.TAG, "IntelliService Disconnected, mIntelliServiceBound=" + HwPowerManagerService.this.mIntelliServiceBound);
            }
            if (HwPowerManagerService.this.mIntelliServiceBound) {
                HwPowerManagerService.this.resetFaceDetect();
            }
        }

        public void onBindingDied(ComponentName name) {
            Slog.w(HwPowerManagerService.TAG, "IntelliService binding died, mIntelliServiceBound=" + HwPowerManagerService.this.mIntelliServiceBound);
            if (HwPowerManagerService.this.mIntelliServiceBound) {
                HwPowerManagerService.this.resetFaceDetect();
            }
        }
    };
    private final Context mContext;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private DisplayEngineManager mDisplayEngineManager;
    private int mEyesProtectionMode;
    private boolean mFaceDetecting;
    private FingerSenseObserver mFingerSenseObserver;
    private final ArrayList<WakeLock> mForceReleasedWakeLocks = new ArrayList();
    private IIntelliService mIRemote;
    private Intent mIntelliIntent;
    private IIntelliListener mIntelliListener = new IIntelliListener.Stub() {
        public void onEvent(IntelliAlgoResult intelliAlgoResult) throws RemoteException {
            int result = intelliAlgoResult != null ? intelliAlgoResult.getPrecenseStatus() : 0;
            if (HwPowerManagerService.DEBUG) {
                Slog.i(HwPowerManagerService.TAG, "onEvent result=" + result + ",mFaceDetecting=" + HwPowerManagerService.this.mFaceDetecting);
            }
            if (result == 1 && HwPowerManagerService.this.mFaceDetecting) {
                long ident = Binder.clearCallingIdentity();
                try {
                    HwPowerManagerService.this.userActivityInternal(SystemClock.uptimeMillis(), 0, 0, 1000);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void onErr(int i) throws RemoteException {
        }
    };
    private boolean mIntelliServiceBound;
    private PGManagerInternal mPGManagerInternal;
    private WindowManagerPolicy mPolicy;
    private final ArrayList<ProxyWLProcessInfo> mProxyWLProcessList = new ArrayList();
    private final ArrayList<WakeLock> mProxyedWakeLocks = new ArrayList();
    private SettingsObserver mSettingsObserver;
    private boolean mShouldFaceDetectLater;
    private boolean mSupportDisplayEngine3DColorTemperature;
    private boolean mSystemReady = false;
    private int mWaitBrightTimeout = 3000;
    private WindowManagerInternal mWindowManagerInternal;

    private final class BluetoothReceiver extends BroadcastReceiver {
        /* synthetic */ BluetoothReceiver(HwPowerManagerService this$0, BluetoothReceiver -this1) {
            this();
        }

        private BluetoothReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            long now = SystemClock.uptimeMillis();
            BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int newState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
            int oldState = intent.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", 0);
            Slog.d(HwPowerManagerService.TAG, "BluetoothReceiver,btDevice:" + btDevice + ",newState:" + newState + ",oldState:" + oldState + ",intent:" + intent.getAction());
            boolean needWakeUp = false;
            if (btDevice != null && newState == 2 && oldState == 1) {
                BluetoothClass btClass = btDevice.getBluetoothClass();
                Slog.d(HwPowerManagerService.TAG, "BluetoothReceiver btClass.getDeviceClass():" + btClass);
                if (btClass != null) {
                    switch (btClass.getDeviceClass()) {
                        case 1024:
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
                            if (HwPowerManagerService.this.wakeUpNoUpdateLocked(now, "bluetooth.connected", 1000, HwPowerManagerService.this.mContext.getOpPackageName(), 1000) || HwPowerManagerService.this.userActivityNoUpdateLocked(now, 0, 0, 1000)) {
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
            if (Global.getInt(this.resolver, "fingersense_enabled", 1) != 1) {
                z = false;
            }
            HwPowerManagerService.nativeSetFsEnable(z);
        }
    }

    private final class HeadsetReceiver extends BroadcastReceiver {
        /* synthetic */ HeadsetReceiver(HwPowerManagerService this$0, HeadsetReceiver -this1) {
            this();
        }

        private HeadsetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (HwPowerManagerService.this.mLock) {
                long now = SystemClock.uptimeMillis();
                Slog.d(HwPowerManagerService.TAG, "HeadsetReceiver,state:" + intent.getIntExtra("state", 0));
                if (intent.getIntExtra("state", 0) == 1 && (HwPowerManagerService.this.wakeUpNoUpdateLocked(now, "headset.connected", 1000, HwPowerManagerService.this.mContext.getOpPackageName(), 1000) || HwPowerManagerService.this.userActivityNoUpdateLocked(now, 0, 0, 1000))) {
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

    private static native int nativeGetDisplayPanelType();

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
        this.mContext = context;
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(this.mContext);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        if (!mLoadLibraryFailed) {
            init_native();
        }
        this.mDisplayEngineManager = new DisplayEngineManager();
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

    private void setColorRGB(float red, float green, float blue) {
        if (red < 0.0f || red > 1.0f || green < 0.0f || green > 1.0f || blue < 0.0f || blue > 1.0f) {
            Slog.w(TAG, "Parameters invalid: red=" + red + ", green=" + green + ", blue=" + blue);
            return;
        }
        try {
            Class clazz = Class.forName("com.huawei.android.os.PowerManagerCustEx");
            clazz.getMethod("updateRgbGamma", new Class[]{Float.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE, Integer.TYPE}).invoke(clazz, new Object[]{Float.valueOf(red), Float.valueOf(green), Float.valueOf(blue), Integer.valueOf(18), Integer.valueOf(7)});
            Log.i(TAG, "setColorTemperatureAccordingToSetting and setColorRGB sucessfully:red=" + red + ", green=" + green + ", blue=" + blue);
        } catch (RuntimeException e) {
            Log.e(TAG, ": reflection exception is " + e.getMessage());
        } catch (Exception ex) {
            Log.e(TAG, ": Exception happend when setColorRGB. Message is: " + ex.getMessage());
        }
    }

    protected void setColorTemperatureAccordingToSetting() {
        Slog.d(TAG, "setColorTemperatureAccordingToSetting");
        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;
        String ctNewRGB;
        List<String> rgbarryList;
        int operation;
        if (this.mSupportDisplayEngine3DColorTemperature) {
            Slog.i(TAG, "setColorTemperatureAccordingToSetting new from displayengine.");
            try {
                this.mEyesProtectionMode = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYES_PROTECTION, 0, -2);
                if (this.mEyesProtectionMode != 1) {
                    ctNewRGB = System.getStringForUser(this.mContext.getContentResolver(), "color_temperature_rgb", -2);
                    if (ctNewRGB != null) {
                        rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
                        red = Float.valueOf((String) rgbarryList.get(0)).floatValue();
                        green = Float.valueOf((String) rgbarryList.get(1)).floatValue();
                        blue = Float.valueOf((String) rgbarryList.get(2)).floatValue();
                    } else {
                        Slog.w(TAG, "ColorTemperature read from setting failed, and set default values");
                    }
                    setColorRGB(red, green, blue);
                }
            } catch (IndexOutOfBoundsException e) {
                Slog.e(TAG, "IndexOutOfBoundsException:" + e);
            } finally {
                setColorRGB(1.0f, 1.0f, 1.0f);
            }
        } else if (isDisplayFeatureSupported(1)) {
            Slog.d(TAG, "setColorTemperatureAccordingToSetting new.");
            try {
                ctNewRGB = System.getStringForUser(this.mContext.getContentResolver(), "color_temperature_rgb", -2);
                if (ctNewRGB != null) {
                    rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
                    red = Float.valueOf((String) rgbarryList.get(0)).floatValue();
                    green = Float.valueOf((String) rgbarryList.get(1)).floatValue();
                    blue = Float.valueOf((String) rgbarryList.get(2)).floatValue();
                    Slog.d(TAG, "ColorTemperature read from setting:" + ctNewRGB + red + green + blue);
                    updateRgbGammaInternal(red, green, blue);
                } else {
                    operation = System.getIntForUser(this.mContext.getContentResolver(), "color_temperature", 128, -2);
                    Slog.d(TAG, "ColorTemperature read from old setting:" + operation);
                    setColorTemperatureInternal(operation);
                }
            } catch (UnsatisfiedLinkError e2) {
                Slog.d(TAG, "ColorTemperature read from setting exception!");
                updateRgbGammaInternal(1.0f, 1.0f, 1.0f);
            }
        } else {
            operation = System.getIntForUser(this.mContext.getContentResolver(), "color_temperature", 128, -2);
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
            int phoneCount = TelephonyManager.getDefault().getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
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
        boolean z;
        super.systemReady(appOps);
        this.mPolicy = (WindowManagerPolicy) getLocalService(WindowManagerPolicy.class);
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mSystemReady = true;
        if (this.mDisplayEngineManager.getSupported(18) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mSupportDisplayEngine3DColorTemperature = z;
        Slog.d(TAG, "systemReady mSupportDisplayEngine3DColorTemperature=" + this.mSupportDisplayEngine3DColorTemperature);
        setColorTemperatureAccordingToSetting();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.mSupportDisplayEngine3DColorTemperature || isDisplayFeatureSupported(1)) {
            resolver.registerContentObserver(System.getUriFor("color_temperature_rgb"), false, this.mSettingsObserver, -1);
        } else {
            resolver.registerContentObserver(System.getUriFor("color_temperature"), false, this.mSettingsObserver, -1);
        }
        this.mFingerSenseObserver = new FingerSenseObserver(this.mHandler, resolver);
        this.mFingerSenseObserver.observe();
        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction("android.intent.action.HEADSET_PLUG");
        this.mContext.registerReceiver(new HeadsetReceiver(this, null), headsetFilter, null, this.mHandler);
        headsetFilter = new IntentFilter();
        headsetFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        headsetFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        this.mContext.registerReceiver(new BluetoothReceiver(this, null), headsetFilter, null, this.mHandler);
        this.mIntelliIntent = new Intent("com.huawei.intelliServer.intelliServer");
        this.mIntelliIntent.setClassName("com.huawei.intelliServer.intelliServer", SERVICE_CLASS);
    }

    public int getAdjustedMaxTimeout(int oldtimeout, int maxv) {
        if (this.mWindowManagerInternal == null || this.mPolicy == null || (this.mWindowManagerInternal.isCoverOpen() ^ 1) == 0 || (this.mPolicy.isKeyguardLocked() ^ 1) == 0 || (isPhoneInCall() ^ 1) == 0) {
            return 0;
        }
        return 10000;
    }

    private void restoreProxyWakeLockLocked(int pid, int uid) {
        for (int i = this.mProxyedWakeLocks.size() - 1; i >= 0; i--) {
            WakeLock wakelock = (WakeLock) this.mProxyedWakeLocks.get(i);
            if (((wakelock.mOwnerUid == uid || -1 == uid) && (wakelock.mOwnerPid == pid || -1 == uid)) || (wakelock.mWorkSource != null && wakelock.mWorkSource.get(0) == uid)) {
                acquireWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mWorkSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid);
                this.mProxyedWakeLocks.remove(i);
            }
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

    protected boolean acquireProxyWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        if (this.mSystemReady) {
            int iPid = pid;
            int iUid = uid;
            if (ws != null) {
                iPid = -1;
                iUid = ws.get(0);
            }
            synchronized (this.mLock) {
                int listSize = this.mProxyWLProcessList.size();
                for (int i = 0; i < listSize; i++) {
                    if (((ProxyWLProcessInfo) this.mProxyWLProcessList.get(i)).isSameProcess(iPid, iUid)) {
                        if (DEBUG_SPEW) {
                            Log.d(TAG, "acquire pxy wl, pid: " + pid + ", uid: " + uid + ", ws: " + ws + ", packageName: " + packageName + ", tag: " + tag);
                        }
                        this.mProxyedWakeLocks.add(new WakeLock(this, lock, flags, tag, packageName, ws, historyTag, uid, pid, new UidState(uid)));
                        return true;
                    }
                }
                return false;
            }
        }
        Log.w(TAG, "acquireProxyWakeLock, mSystemReady is false.");
        return false;
    }

    private int findProxyWakeLockIndexLocked(IBinder lock) {
        int count = this.mProxyedWakeLocks.size();
        for (int i = 0; i < count; i++) {
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
        if ((flags & 1) != 0) {
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
                wakeLock = new WakeLock(this, lock, flags, tag, packageName, ws, historyTag, uid, pid, new UidState(uid));
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
            this.mDirty |= 1;
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
                    if (1 == length) {
                        if (wakelock.mWorkSource.get(0) == uid) {
                            if (DEBUG_SPEW) {
                                Log.d(TAG, "forceReleaseWakeLockByPidUid, last one, wakelock: " + wakelock);
                            }
                            this.mForceReleasedWakeLocks.add(wakelock);
                            releaseWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags);
                        }
                    } else if (length > 1) {
                        for (int j = 0; j < length; j++) {
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
                                this.mForceReleasedWakeLocks.add(new WakeLock(this, wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, workSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid, new UidState(uid)));
                                WorkSource workSource2 = new WorkSource(wakelock.mWorkSource);
                                workSource2.remove(workSource);
                                notifyWakeLockChangingLocked(wakelock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mOwnerUid, wakelock.mOwnerPid, workSource2, wakelock.mHistoryTag);
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

    /* JADX WARNING: Missing block: B:18:0x0031, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    }
                } else if (wl.mWorkSource != null) {
                    int size = wl.mWorkSource.size();
                    for (int i = 0; i < size; i++) {
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
            this.mSkipWaitKeyguardDismiss = false;
            this.mDirty |= 16384;
            Message msg = this.mHandler.obtainMessage(101);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, (long) this.mWaitBrightTimeout);
        }
        this.mAuthSucceeded = false;
    }

    protected void disableBrightnessWaitLocked(boolean enableBright) {
        disableBrightnessWaitLocked(enableBright, false);
    }

    protected void disableBrightnessWaitLocked(boolean enableBright, boolean skipWaitKeyguardDismiss) {
        if (this.mBrightnessWaitModeEnabled) {
            this.mHandler.removeMessages(101);
            this.mBrightnessWaitModeEnabled = false;
            this.mBrightnessWaitRet = enableBright;
            this.mSkipWaitKeyguardDismiss = skipWaitKeyguardDismiss;
            this.mDirty |= 16384;
        }
        this.mAuthSucceeded = false;
    }

    protected void setAuthSucceededInternal() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "setAuthSucceededInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled);
            }
            this.mAuthSucceeded = true;
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

    public int getDisplayPanelTypeInternal() {
        try {
            if (!mLoadLibraryFailed) {
                return nativeGetDisplayPanelType();
            }
            Slog.d(TAG, "nativeGetDisplayPanelType failed because of library not found!");
            return -1;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativeGetDisplayPanelType not found!");
            return -1;
        }
    }

    protected void stopPickupTrunOff() {
        if (PickUpWakeScreenManager.isPickupSensorSupport(this.mContext) && PickUpWakeScreenManager.getInstance() != null) {
            PickUpWakeScreenManager.getInstance().stopTrunOffScrren();
        }
    }

    protected void resetWaitBrightTimeoutLocked() {
        this.mHandler.removeMessages(101);
        Message msg = this.mHandler.obtainMessage(101);
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
                    this.mLastWakeTime = eventTime;
                    setWakefulnessLocked(1, 0);
                    userActivityNoUpdateLocked(eventTime, 0, 0, uid);
                    disableBrightnessWaitLocked(true, "com.android.systemui".equals(opPackageName) ^ 1);
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
        if (eventTime < this.mLastSleepTime || this.mWakefulness == 1 || (this.mBootCompleted ^ 1) != 0 || (this.mSystemReady ^ 1) != 0 || this.mProximityPositive) {
            notifyWakeupResult(false);
            return false;
        }
        if (mSupportFaceDetect) {
            startIntelliService();
        }
        Trace.traceBegin(131072, "wakeUpWithoutInteractive");
        try {
            switch (this.mWakefulness) {
                case 0:
                    Slog.i(TAG, "Waking up from sleep (uid " + uid + ")...");
                    Jlog.d(5, "JL_PMS_WAKEFULNESS_ASLEEP");
                    break;
                case 2:
                    Slog.i(TAG, "Waking up from dream (uid " + uid + ")...");
                    Jlog.d(6, "JL_PMS_WAKEFULNESS_DREAMING");
                    break;
                case 3:
                    Slog.i(TAG, "Waking up from dozing (uid " + uid + ")...");
                    Jlog.d(7, "JL_PMS_WAKEFULNESS_NAPPING");
                    break;
            }
            this.mLastWakeTime = eventTime;
            this.mDirty |= 2;
            this.mWakefulness = 1;
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
                if (!goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 8, 0, 1000)) {
                    disableBrightnessWaitLocked(false);
                }
                updatePowerStateLocked();
            }
        }
    }

    public boolean isAppWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
        if (this.mPGManagerInternal == null) {
            this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        }
        if (this.mPGManagerInternal != null) {
            return this.mPGManagerInternal.isGmsWakeLockFilterTag(flags, packageName, ws);
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
                    for (int i = 0; i < size; i++) {
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
                params.put(MonitorModule.PARAM_TYPE, paramType);
                params.put("brightness", Integer.valueOf(brightness));
                params.put(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName[0]);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    protected void sendBrightnessModeToMonitor(boolean manualMode, String packageName) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put(MonitorModule.PARAM_TYPE, "brightnessMode");
            params.put("brightnessMode", manualMode ? "MANUAL" : "AUTO");
            params.put(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName);
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    protected void sendManualBrightnessToMonitor(int brightness, String packageName) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put(MonitorModule.PARAM_TYPE, "manualBrightness");
            params.put("brightness", Integer.valueOf(brightness));
            params.put(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName);
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    protected void notifyWakeupResult(boolean isWakenupThisTime) {
        AwareFakeActivityRecg.self().notifyWakeupResult(isWakenupThisTime);
    }

    protected void startIntelliService() {
        if (DEBUG) {
            Slog.d(TAG, "bind IntelliService, mIntelliServiceBound=" + this.mIntelliServiceBound);
        }
        if (!this.mIntelliServiceBound) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    try {
                        HwPowerManagerService.this.mIntelliServiceBound = true;
                        boolean success = HwPowerManagerService.this.mContext.bindServiceAsUser(HwPowerManagerService.this.mIntelliIntent, HwPowerManagerService.this.connection, 1, UserHandle.CURRENT);
                        if (HwPowerManagerService.DEBUG) {
                            Slog.d(HwPowerManagerService.TAG, "bind IntelliService, success=" + success);
                        }
                        if (!success) {
                            HwPowerManagerService.this.resetFaceDetect();
                        }
                    } catch (SecurityException e) {
                        HwPowerManagerService.this.resetFaceDetect();
                        Slog.e(HwPowerManagerService.TAG, "unable to start intelli service: " + HwPowerManagerService.this.mIntelliIntent, e);
                    } catch (Exception e2) {
                        HwPowerManagerService.this.resetFaceDetect();
                        Slog.e(HwPowerManagerService.TAG, "unable to start intelli service: " + HwPowerManagerService.this.mIntelliIntent, e2);
                    }
                }
            });
        }
    }

    protected void stopIntelliService() {
        if (DEBUG) {
            Slog.d(TAG, "unbind IntelliService, mIntelliServiceBound=" + this.mIntelliServiceBound);
        }
        if (this.mIntelliServiceBound) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    try {
                        HwPowerManagerService.this.mContext.unbindService(HwPowerManagerService.this.connection);
                    } catch (Exception e) {
                        Slog.e(HwPowerManagerService.TAG, "unbindService fail: ", e);
                    }
                }
            });
            resetFaceDetect();
        }
    }

    protected int registerFaceDetect() {
        if (!this.mIntelliServiceBound) {
            Slog.i(TAG, "IntelliService not started, face detect later");
            this.mShouldFaceDetectLater = true;
            startIntelliService();
            return -1;
        } else if (this.mIRemote == null || this.mFaceDetecting) {
            Slog.e(TAG, "register err, mFaceDetecting=" + this.mFaceDetecting);
            return -1;
        } else {
            try {
                int result = this.mIRemote.registListener(3, this.mIntelliListener);
                if (DEBUG) {
                    Slog.d(TAG, "registListener, result=" + result);
                }
                if (result != -1) {
                    this.mFaceDetecting = true;
                }
                return result;
            } catch (RemoteException e) {
                Slog.e(TAG, "registListener exption: ", e);
                return -1;
            } catch (Exception e2) {
                Slog.e(TAG, "registListener exption: ", e2);
                return -1;
            }
        }
    }

    protected int unregisterFaceDetect() {
        if (!this.mFaceDetecting) {
            return -1;
        }
        if (this.mIRemote == null) {
            Slog.e(TAG, "unregister err mIRemote is null!!");
            return -1;
        }
        try {
            int result = this.mIRemote.unregistListener(3, this.mIntelliListener);
            if (DEBUG) {
                Slog.d(TAG, "unregistListener, result=" + result);
            }
            if (result != -1) {
                this.mFaceDetecting = false;
            }
            return result;
        } catch (RemoteException e) {
            Slog.e(TAG, "unregistListener exption: ", e);
            return -1;
        } catch (Exception e2) {
            Slog.e(TAG, "unregistListener exption: ", e2);
            return -1;
        }
    }

    private void resetFaceDetect() {
        this.mIntelliServiceBound = false;
        this.mFaceDetecting = false;
        this.mShouldFaceDetectLater = false;
        this.mIRemote = null;
    }
}
