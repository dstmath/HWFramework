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
import android.hardware.display.HwFoldScreenState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBrightnessCallback;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.EsdDetection;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.DisplayManagerService;
import com.android.server.gesture.GestureNavConst;
import com.android.server.lights.LightsService;
import com.android.server.pg.PGManagerInternal;
import com.android.server.policy.PickUpWakeScreenManager;
import com.android.server.policy.TurnOnWakeScreenManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.PowerManagerService;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.IntelliServer.intellilib.IIntelliListener;
import com.huawei.IntelliServer.intellilib.IIntelliService;
import com.huawei.IntelliServer.intellilib.IntelliAlgoResult;
import com.huawei.displayengine.DisplayEngineManager;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

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
    public static final int SUBTYPE_DROP_WAKELOCK = 1;
    public static final int SUBTYPE_PROXY_NO_WORKSOURCE = 2;
    public static final int SUBTYPE_RELEASE_NO_WORKSOURCE = 3;
    private static final String TAG = "HwPowerManagerService";
    private static final int TYPE_FACE_STAY_LIT = 3;
    private static boolean mLoadLibraryFailed;
    /* access modifiers changed from: private */
    public ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (!HwPowerManagerService.this.mIntelliServiceBound) {
                Slog.w(HwPowerManagerService.TAG, "IntelliService not bound, ignore.");
                return;
            }
            if (PowerManagerService.DEBUG) {
                Slog.d(HwPowerManagerService.TAG, "IntelliService Connected, mShouldFaceDetectLater=" + HwPowerManagerService.this.mShouldFaceDetectLater);
            }
            IIntelliService unused = HwPowerManagerService.this.mIRemote = IIntelliService.Stub.asInterface(iBinder);
            if (HwPowerManagerService.this.mShouldFaceDetectLater) {
                boolean unused2 = HwPowerManagerService.this.mShouldFaceDetectLater = false;
                HwPowerManagerService.this.registerFaceDetect();
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            if (PowerManagerService.DEBUG) {
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
    /* access modifiers changed from: private */
    public final Context mContext;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private DisplayEngineManager mDisplayEngineManager;
    private int mEyesProtectionMode;
    /* access modifiers changed from: private */
    public boolean mFaceDetecting;
    private FingerSenseObserver mFingerSenseObserver;
    private final ArrayList<PowerManagerService.WakeLock> mForceReleasedWakeLocks = new ArrayList<>();
    private boolean mHadSendBrightnessModeToMonitor;
    private boolean mHadSendManualBrightnessToMonitor;
    private final ArrayList<HwBrightnessCallbackData> mHwBrightnessCallbacks = new ArrayList<>();
    private final ArrayMap<String, HwBrightnessInnerProcessor> mHwBrightnessInnerProcessors = new ArrayMap<>();
    /* access modifiers changed from: private */
    public IIntelliService mIRemote;
    /* access modifiers changed from: private */
    public Intent mIntelliIntent;
    private IIntelliListener mIntelliListener = new IIntelliListener.Stub() {
        public void onEvent(IntelliAlgoResult intelliAlgoResult) throws RemoteException {
            int result = intelliAlgoResult != null ? intelliAlgoResult.getPrecenseStatus() : 0;
            if (PowerManagerService.DEBUG) {
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
    /* access modifiers changed from: private */
    public boolean mIntelliServiceBound;
    private boolean mIsGoogleEBS = false;
    private final Object mLockHwBrightnessCallbacks = new Object();
    private PGManagerInternal mPGManagerInternal;
    private WindowManagerPolicy mPolicy;
    private final ArrayList<ProxyWLProcessInfo> mProxyWLProcessList = new ArrayList<>();
    private final ArrayList<PowerManagerService.WakeLock> mProxyedWakeLocks = new ArrayList<>();
    private SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public boolean mShouldFaceDetectLater;
    private boolean mSupportDisplayEngine3DColorTemperature;
    private boolean mSystemReady = false;
    private int mWaitBrightTimeout = 3000;
    private WindowManagerInternal mWindowManagerInternal;

    private final class BluetoothReceiver extends BroadcastReceiver {
        private BluetoothReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Object obj;
            Intent intent2 = intent;
            long now = SystemClock.uptimeMillis();
            BluetoothDevice btDevice = (BluetoothDevice) intent2.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int newState = intent2.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
            int oldState = intent2.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", 0);
            if (PowerManagerService.DEBUG_SPEW) {
                Slog.d(HwPowerManagerService.TAG, "BluetoothReceiver,btDevice:" + btDevice + ",newState:" + newState + ",oldState:" + oldState + ",intent:" + intent.getAction());
            }
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
                    boolean needWakeUp2 = needWakeUp;
                    if (needWakeUp2) {
                        Object obj2 = HwPowerManagerService.this.mLock;
                        synchronized (obj2) {
                            try {
                                if (HwFoldScreenState.isFoldScreenDevice()) {
                                    HwPowerManagerService.this.mHwPowerEx.prepareWakeupEx(0, 1000, HwPowerManagerService.this.mContext.getOpPackageName(), "bluetooth.connected");
                                    obj = obj2;
                                } else {
                                    obj = obj2;
                                    if (HwPowerManagerService.this.wakeUpNoUpdateLocked(now, "bluetooth.connected", 1000, HwPowerManagerService.this.mContext.getOpPackageName(), 1000) || HwPowerManagerService.this.userActivityNoUpdateLocked(now, 0, 0, 1000)) {
                                        HwPowerManagerService.this.updatePowerStateLocked();
                                    }
                                }
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                    }
                    boolean z = needWakeUp2;
                }
            }
        }
    }

    private static final class FingerSenseObserver extends ContentObserver {
        private ContentResolver resolver;

        public FingerSenseObserver(Handler handler, ContentResolver resolver2) {
            super(handler);
            this.resolver = resolver2;
        }

        public void observe() {
            this.resolver.registerContentObserver(Settings.Global.getUriFor("fingersense_enabled"), false, this, -1);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            if (Settings.Global.getInt(this.resolver, "fingersense_enabled", 1) != 1) {
                z = false;
            }
            PowerManagerService.nativeSetFsEnable(z);
        }
    }

    private final class HeadsetReceiver extends BroadcastReceiver {
        private HeadsetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (HwPowerManagerService.this.mLock) {
                long now = SystemClock.uptimeMillis();
                Slog.d(HwPowerManagerService.TAG, "HeadsetReceiver,state:" + intent.getIntExtra("state", 0));
                if (intent.getIntExtra("state", 0) == 1) {
                    if (HwFoldScreenState.isFoldScreenDevice()) {
                        HwPowerManagerService.this.mHwPowerEx.prepareWakeupEx(0, 1000, HwPowerManagerService.this.mContext.getOpPackageName(), "headset.connected");
                    } else if (HwPowerManagerService.this.wakeUpNoUpdateLocked(now, "headset.connected", 1000, HwPowerManagerService.this.mContext.getOpPackageName(), 1000) || HwPowerManagerService.this.userActivityNoUpdateLocked(now, 0, 0, 1000)) {
                        HwPowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }
        }
    }

    private static final class HwBrightnessCallbackData {
        private IHwBrightnessCallback mCB;
        private List<String> mFilter = null;

        public HwBrightnessCallbackData(IHwBrightnessCallback cb, List<String> filter) {
            if (filter != null) {
                this.mFilter = new ArrayList(filter);
            }
            this.mCB = cb;
        }

        public List<String> getFilter() {
            return this.mFilter;
        }

        public IHwBrightnessCallback getCB() {
            return this.mCB;
        }
    }

    public static final class HwBrightnessInnerProcessor {
        public int setData(Bundle data) {
            Slog.w(HwPowerManagerService.TAG, "Forget to override setData()? Now in default setData(), nothing to do!");
            return -1;
        }

        public int getData(Bundle data) {
            Slog.w(HwPowerManagerService.TAG, "Forget to override getData()? Now in default getData(), nothing to do!");
            return -1;
        }
    }

    private static final class ProxyWLProcessInfo {
        public int mPid;
        public boolean mProxyWS;
        public int mUid;

        public ProxyWLProcessInfo(int pid, int uid, boolean proxyWS) {
            this.mPid = pid;
            this.mUid = uid;
            this.mProxyWS = proxyWS;
        }

        public boolean isSameProcess(int pid, int uid) {
            return (this.mPid == pid || -1 == pid) && (this.mUid == uid || -1 == uid);
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

    private static native void init_SurfaceComposerClient();

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
        loadHwBrightnessInnerProcessors();
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        if (!mLoadLibraryFailed) {
            finalize_native();
        }
        try {
            HwPowerManagerService.super.finalize();
        } catch (Throwable th) {
        }
    }

    public boolean setGoogleEBS(boolean isGoogleEBS) {
        Slog.d(TAG, "setGoogleEBS:" + isGoogleEBS);
        this.mIsGoogleEBS = isGoogleEBS;
        return true;
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
        Slog.d(TAG, "isDisplayFeatureSupported feature:" + feature);
        boolean z = false;
        try {
            if (!mLoadLibraryFailed) {
                if (nativeGetDisplayFeatureSupported(feature) != 0) {
                    z = true;
                }
                return z;
            }
            Slog.d(TAG, "Display feature not supported because of library not found!");
            return false;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "Display feature not supported because of exception!");
            return false;
        }
    }

    private void setColorRGB(float red, float green, float blue) {
        if (red < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || red > 1.0f || green < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || green > 1.0f || blue < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || blue > 1.0f) {
            Slog.w(TAG, "Parameters invalid: red=" + red + ", green=" + green + ", blue=" + blue);
            return;
        }
        try {
            Class clazz = Class.forName("com.huawei.android.os.PowerManagerCustEx");
            clazz.getMethod("updateRgbGamma", new Class[]{Float.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE, Integer.TYPE}).invoke(clazz, new Object[]{Float.valueOf(red), Float.valueOf(green), Float.valueOf(blue), 18, 7});
            Log.i(TAG, "setColorTemperatureAccordingToSetting and setColorRGB sucessfully:red=" + red + ", green=" + green + ", blue=" + blue);
        } catch (RuntimeException e) {
            Log.e(TAG, ": reflection exception is " + e.getMessage());
        } catch (Exception ex) {
            Log.e(TAG, ": Exception happend when setColorRGB. Message is: " + ex.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void setColorTemperatureAccordingToSetting() {
        Slog.d(TAG, "setColorTemperatureAccordingToSetting");
        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;
        if (this.mSupportDisplayEngine3DColorTemperature) {
            Slog.i(TAG, "setColorTemperatureAccordingToSetting new from displayengine.");
            try {
                this.mEyesProtectionMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, -2);
                if (this.mEyesProtectionMode == 1) {
                    setColorRGB(1.0f, 1.0f, 1.0f);
                    return;
                }
                String ctNewRGB = Settings.System.getStringForUser(this.mContext.getContentResolver(), "color_temperature_rgb", -2);
                if (ctNewRGB != null) {
                    List<String> rgbarryList = new ArrayList<>(Arrays.asList(ctNewRGB.split(",")));
                    red = Float.valueOf(rgbarryList.get(0)).floatValue();
                    green = Float.valueOf(rgbarryList.get(1)).floatValue();
                    blue = Float.valueOf(rgbarryList.get(2)).floatValue();
                } else {
                    Slog.w(TAG, "ColorTemperature read from setting failed, and set default values");
                }
                setColorRGB(red, green, blue);
            } catch (IndexOutOfBoundsException e) {
                Slog.e(TAG, "IndexOutOfBoundsException:" + e);
            } catch (Throwable th) {
                setColorRGB(1.0f, 1.0f, 1.0f);
                throw th;
            }
        } else if (isDisplayFeatureSupported(1)) {
            Slog.d(TAG, "setColorTemperatureAccordingToSetting new.");
            try {
                String ctNewRGB2 = Settings.System.getStringForUser(this.mContext.getContentResolver(), "color_temperature_rgb", -2);
                if (ctNewRGB2 != null) {
                    List<String> rgbarryList2 = new ArrayList<>(Arrays.asList(ctNewRGB2.split(",")));
                    float red2 = Float.valueOf(rgbarryList2.get(0)).floatValue();
                    float green2 = Float.valueOf(rgbarryList2.get(1)).floatValue();
                    float blue2 = Float.valueOf(rgbarryList2.get(2)).floatValue();
                    Slog.d(TAG, "ColorTemperature read from setting:" + ctNewRGB2 + red2 + green2 + blue2);
                    updateRgbGammaInternal(red2, green2, blue2);
                } else {
                    int operation = Settings.System.getIntForUser(this.mContext.getContentResolver(), "color_temperature", 128, -2);
                    Slog.d(TAG, "ColorTemperature read from old setting:" + operation);
                    setColorTemperatureInternal(operation);
                }
            } catch (UnsatisfiedLinkError e2) {
                Slog.d(TAG, "ColorTemperature read from setting exception!");
                updateRgbGammaInternal(1.0f, 1.0f, 1.0f);
            }
        } else {
            int operation2 = Settings.System.getIntForUser(this.mContext.getContentResolver(), "color_temperature", 128, -2);
            Slog.d(TAG, "setColorTemperatureAccordingToSetting old:" + operation2);
            setColorTemperatureInternal(operation2);
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
        HwPowerManagerService.super.systemReady(appOps);
        init_SurfaceComposerClient();
        this.mPolicy = (WindowManagerPolicy) getLocalService(WindowManagerPolicy.class);
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mSystemReady = true;
        this.mSupportDisplayEngine3DColorTemperature = this.mDisplayEngineManager.getSupported(18) == 1;
        Slog.d(TAG, "systemReady mSupportDisplayEngine3DColorTemperature=" + this.mSupportDisplayEngine3DColorTemperature);
        setColorTemperatureAccordingToSetting();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.mSupportDisplayEngine3DColorTemperature || isDisplayFeatureSupported(1)) {
            resolver.registerContentObserver(Settings.System.getUriFor("color_temperature_rgb"), false, this.mSettingsObserver, -1);
        } else {
            resolver.registerContentObserver(Settings.System.getUriFor("color_temperature"), false, this.mSettingsObserver, -1);
        }
        this.mFingerSenseObserver = new FingerSenseObserver(this.mHandler, resolver);
        this.mFingerSenseObserver.observe();
        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction("android.intent.action.HEADSET_PLUG");
        this.mContext.registerReceiver(new HeadsetReceiver(), headsetFilter, null, this.mHandler);
        IntentFilter headsetFilter2 = new IntentFilter();
        headsetFilter2.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        headsetFilter2.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        this.mContext.registerReceiver(new BluetoothReceiver(), headsetFilter2, null, this.mHandler);
        this.mIntelliIntent = new Intent("com.huawei.intelliServer.intelliServer");
        this.mIntelliIntent.setClassName("com.huawei.intelliServer.intelliServer", SERVICE_CLASS);
    }

    public int getAdjustedMaxTimeout(int oldtimeout, int maxv) {
        if (this.mWindowManagerInternal == null || this.mPolicy == null || this.mWindowManagerInternal.isCoverOpen() || this.mPolicy.isKeyguardLocked() || isPhoneInCall()) {
            return 0;
        }
        return 10000;
    }

    public void handleWakeLockDeath(PowerManagerService.WakeLock wakeLock) {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleWakeLockDeath: lock=" + Objects.hashCode(wakeLock.mLock) + " [" + wakeLock.mTag + "]");
            }
            for (int i = this.mForceReleasedWakeLocks.size() - 1; i >= 0; i--) {
                if (this.mForceReleasedWakeLocks.get(i).mLock == wakeLock.mLock) {
                    Log.d(TAG, "remove from forceReleased wl: " + wakeLock + " mForceReleasedWakeLocks:" + wl);
                    this.mForceReleasedWakeLocks.remove(i);
                }
            }
            for (int i2 = this.mProxyedWakeLocks.size() - 1; i2 >= 0; i2--) {
                if (this.mProxyedWakeLocks.get(i2).mLock == wakeLock.mLock) {
                    Log.d(TAG, "remove from proxyed wl: " + wakeLock + " mProxyedWakeLocks:" + wl);
                    this.mProxyedWakeLocks.remove(i2);
                }
            }
            HwPowerManagerService.super.handleWakeLockDeath(wakeLock);
        }
    }

    private void restoreProxyWakeLockLocked(int pid, int uid) {
        for (int i = this.mProxyedWakeLocks.size() - 1; i >= 0; i--) {
            PowerManagerService.WakeLock wakelock = this.mProxyedWakeLocks.get(i);
            if (((wakelock.mOwnerUid == uid || -1 == uid) && (wakelock.mOwnerPid == pid || -1 == uid)) || (wakelock.mWorkSource != null && getWorkSourceUid(wakelock.mWorkSource) == uid)) {
                acquireWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mWorkSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid);
                this.mProxyedWakeLocks.remove(i);
            }
        }
    }

    private void removeProxyWakeLockProcessLocked(int pid, int uid) {
        for (int i = this.mProxyWLProcessList.size() - 1; i >= 0; i--) {
            if (this.mProxyWLProcessList.get(i).isSameProcess(pid, uid)) {
                if (DEBUG_SPEW) {
                    Log.d(TAG, "remove pxy wl, pid: " + pid + ", uid: " + uid + " from pxy process list.");
                }
                this.mProxyWLProcessList.remove(i);
            }
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        proxyWakeLockByPidUid(pid, uid, proxy, true);
    }

    private void proxyWakeLockByPidUid(int pid, int uid, boolean proxy, boolean proxyWS) {
        synchronized (this.mLock) {
            if (true == proxy) {
                try {
                    this.mProxyWLProcessList.add(new ProxyWLProcessInfo(pid, uid, proxyWS));
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                restoreProxyWakeLockLocked(pid, uid);
                removeProxyWakeLockProcessLocked(pid, uid);
            }
        }
    }

    private int getWorkSourceUid(WorkSource ws) {
        int uid = -1;
        if (ws == null) {
            return -1;
        }
        if (ws.size() > 0) {
            uid = ws.get(0);
        } else {
            ArrayList<WorkSource.WorkChain> workChains = ws.getWorkChains();
            if (workChains == null || workChains.size() <= 0) {
                Log.w(TAG, "getWorkSourceUid, workChains is empty.");
            } else {
                uid = workChains.get(0).getAttributionUid();
            }
        }
        return uid;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0049, code lost:
        if (isScreenOrProximityLock(r25) != false) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004d, code lost:
        if (DEBUG_SPEW == false) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0053, code lost:
        if (dropLogs(r13, r15) != false) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
        r2 = r27;
        r3 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005b, code lost:
        android.util.Log.d(TAG, "acquire pxy wl : lock=" + r31 + ", uid: " + r15 + ", ws: " + r14 + ", packageName: " + r27 + ", tag: " + r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x009c, code lost:
        r1 = r1;
        r16 = r4;
        r17 = r5;
        r18 = r6;
        r19 = r7;
        r20 = r8;
        r21 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r1 = new com.android.server.power.PowerManagerService.WakeLock(r12, r24, r25, r13, r27, r14, r29, r15, r31, new com.android.server.power.PowerManagerService.UidState(r15));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00bb, code lost:
        r1 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r24.linkToDeath(r1, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r12.mProxyedWakeLocks.add(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c7, code lost:
        monitor-exit(r21);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c9, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ca, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00cb, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d3, code lost:
        throw new java.lang.IllegalArgumentException("HW Wake lock is already dead.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00d5, code lost:
        r2 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00f5, code lost:
        r0 = th;
     */
    public boolean acquireProxyWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        String str = tag;
        WorkSource workSource = ws;
        int i = uid;
        if (!this.mSystemReady) {
            Log.w(TAG, "acquireProxyWakeLock, mSystemReady is false.");
            return false;
        }
        Object obj = this.mLock;
        synchronized (obj) {
            try {
                int listSize = this.mProxyWLProcessList.size();
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 < listSize) {
                        int iPid = pid;
                        int iUid = i;
                        ProxyWLProcessInfo pwi = this.mProxyWLProcessList.get(i3);
                        if (pwi.mProxyWS && workSource != null) {
                            iPid = -1;
                            int workSourceUid = getWorkSourceUid(workSource);
                            if (workSourceUid >= 0) {
                                iUid = workSourceUid;
                            }
                        }
                        int iPid2 = iPid;
                        int iUid2 = iUid;
                        if (pwi.isSameProcess(iPid2, iUid2)) {
                            break;
                        }
                        IBinder iBinder = lock;
                        int i4 = listSize;
                        Object obj2 = obj;
                        i2 = i3 + 1;
                        str = tag;
                    } else {
                        IBinder iBinder2 = lock;
                    }
                }
            } catch (Throwable th) {
                ex = th;
                IBinder iBinder3 = lock;
                Object obj3 = obj;
                throw ex;
            }
        }
        return false;
    }

    private int findProxyWakeLockIndexLocked(IBinder lock) {
        int count = this.mProxyedWakeLocks.size();
        for (int i = 0; i < count; i++) {
            if (this.mProxyedWakeLocks.get(i).mLock == lock) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean updateProxyWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        synchronized (this.mLock) {
            int index = findProxyWakeLockIndexLocked(lock);
            if (index < 0) {
                return false;
            }
            PowerManagerService.WakeLock wakeLock = this.mProxyedWakeLocks.get(index);
            if (DEBUG_SPEW) {
                Slog.d(TAG, "update ws pxy wl : lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], ws=" + ws + ", curr.ws: " + wakeLock.mWorkSource);
            }
            if (!wakeLock.hasSameWorkSource(ws)) {
                wakeLock.mHistoryTag = historyTag;
                wakeLock.updateWorkSource(ws);
            }
            return true;
        }
    }

    private boolean releaseWakeLockFromListLocked(IBinder lock, ArrayList<PowerManagerService.WakeLock> list) {
        boolean ret = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            PowerManagerService.WakeLock wakelock = list.get(i);
            if (wakelock.mLock == lock) {
                if (DEBUG_SPEW && !dropLogs(wakelock.mTag, wakelock.mOwnerUid)) {
                    Log.d(TAG, "release ws pxy wl : lock= wl:" + wakelock + " from list, length: " + length);
                }
                try {
                    lock.unlinkToDeath(wakelock, 0);
                } catch (NoSuchElementException e) {
                    Log.d(TAG, "release ws pxy wl, no such Element");
                }
                list.remove(i);
                ret = true;
            }
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public boolean releaseProxyWakeLock(IBinder lock) {
        boolean ret;
        if (!this.mSystemReady) {
            Log.w(TAG, "releaseProxyWakeLock, mSystemReady is false.");
            return false;
        }
        synchronized (this.mLock) {
            ret = false | releaseWakeLockFromListLocked(lock, this.mProxyedWakeLocks) | releaseWakeLockFromListLocked(lock, this.mForceReleasedWakeLocks);
        }
        return ret;
    }

    private void releaseWakeLockInternalLocked(IBinder lock, int flags) {
        int index = findWakeLockIndexLocked(lock);
        if (index < 0) {
            Slog.w(TAG, "releaseWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + " [not found], flags=0x" + Integer.toHexString(flags));
            return;
        }
        PowerManagerService.WakeLock wakeLock = (PowerManagerService.WakeLock) this.mWakeLocks.get(index);
        if (DEBUG_SPEW && !dropLogs(wakeLock.mTag, wakeLock.mOwnerUid)) {
            Slog.d(TAG, "releaseWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + wakeLock.mTag + "\", packageName=" + wakeLock.mPackageName + "\", ws=" + wakeLock.mWorkSource + ", uid=" + wakeLock.mOwnerUid + ", pid=" + wakeLock.mOwnerPid);
        }
        if ((flags & 1) != 0) {
            this.mRequestWaitForNegativeProximity = true;
        }
        removeWakeLockLocked(wakeLock, index);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v1, resolved type: com.android.server.power.PowerManagerService$WakeLock} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void acquireWakeLockInternalLocked(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        int i;
        PowerManagerService.WakeLock wakeLock;
        PowerManagerService.WakeLock wakeLock2;
        String str = tag;
        String str2 = packageName;
        WorkSource workSource = ws;
        int i2 = uid;
        int i3 = pid;
        boolean dropLogs = dropLogs(str, i2);
        if (DEBUG_SPEW && !dropLogs) {
            Slog.d(TAG, "acquireWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + str + "\", packageName=" + str2 + "\", ws=" + workSource + ", uid=" + i2 + ", pid=" + i3);
        }
        if (!lock.isBinderAlive()) {
            Slog.w(TAG, "lock:" + Objects.hashCode(lock) + " is already dead, tag=\"" + str + "\", packageName=" + str2 + ", ws=" + workSource + ", uid=" + i2 + ", pid=" + i3);
            return;
        }
        int index = findWakeLockIndexLocked(lock);
        if (index >= 0) {
            if (DEBUG_SPEW && !dropLogs) {
                Slog.d(TAG, "acquireWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", existing wakelock");
            }
            PowerManagerService.WakeLock wakeLock3 = this.mWakeLocks.get(index);
            if (!wakeLock3.hasSameProperties(flags, str, workSource, i2, i3)) {
                int i4 = index;
                notifyWakeLockChangingLocked(wakeLock3, flags, str, str2, i2, i3, workSource, historyTag);
                wakeLock3.updateProperties(flags, str, str2, workSource, historyTag, i2, i3);
            }
            wakeLock2 = null;
            IBinder iBinder = lock;
            i = i2;
            wakeLock = wakeLock3;
        } else {
            i = i2;
            PowerManagerService.WakeLock wakeLock4 = new PowerManagerService.WakeLock(this, lock, flags, str, str2, workSource, historyTag, i2, pid, new PowerManagerService.UidState(i2));
            PowerManagerService.WakeLock wakeLock5 = wakeLock4;
            try {
                lock.linkToDeath(wakeLock5, 0);
                this.mWakeLocks.add(wakeLock5);
                setWakeLockDisabledStateLocked(wakeLock5);
                wakeLock = wakeLock5;
                wakeLock2 = 1;
            } catch (RemoteException e) {
                RemoteException remoteException = e;
                throw new IllegalArgumentException("Wake lock is already dead.");
            }
        }
        applyWakeLockFlagsOnAcquireLocked(wakeLock, i);
        this.mDirty = 1 | this.mDirty;
        updatePowerStateLocked();
        if (wakeLock2 != null) {
            notifyWakeLockAcquiredLocked(wakeLock);
        }
    }

    public boolean proxyedWakeLock(int subType, List<String> value) {
        if (value == null || value.size() != 2) {
            Log.w(TAG, "invaild para for :" + subType + " value:" + value);
            return false;
        }
        boolean ret = true;
        try {
            int pid = Integer.parseInt(value.get(0));
            int uid = Integer.parseInt(value.get(1));
            switch (subType) {
                case 1:
                    dropProxyedWakeLock(pid, uid);
                    break;
                case 2:
                    proxyWakeLockByPidUid(pid, uid, true, false);
                    break;
                case 3:
                    forceReleaseWakeLockByPidUid(pid, uid, false);
                    break;
                default:
                    ret = false;
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, "proxyedWakeLock Exception !", ex);
            ret = false;
        }
        return ret;
    }

    private void dropProxyedWakeLock(int pid, int uid) {
        synchronized (this.mLock) {
            for (int i = this.mForceReleasedWakeLocks.size() - 1; i >= 0; i--) {
                PowerManagerService.WakeLock wl = this.mForceReleasedWakeLocks.get(i);
                if (((wl.mOwnerUid == uid || -1 == uid) && (wl.mOwnerPid == pid || -1 == pid)) || (-1 == pid && wl.mWorkSource != null && getWorkSourceUid(wl.mWorkSource) == uid)) {
                    Log.d(TAG, "drop from forceReleased wl: " + wl + " mForceReleasedWakeLocks:" + wl);
                    this.mForceReleasedWakeLocks.remove(i);
                    wl.mLock.unlinkToDeath(wl, 0);
                }
            }
            for (int i2 = this.mProxyedWakeLocks.size() - 1; i2 >= 0; i2--) {
                PowerManagerService.WakeLock wl2 = this.mProxyedWakeLocks.get(i2);
                if ((-1 == uid && -1 == pid) || ((wl2.mOwnerUid == uid && (wl2.mOwnerPid == pid || -1 == pid)) || (-1 == pid && wl2.mWorkSource != null && getWorkSourceUid(wl2.mWorkSource) == uid))) {
                    Log.d(TAG, "drop from proxyed wl: " + wl2 + " mProxyedWakeLocks:" + wl2);
                    this.mProxyedWakeLocks.remove(i2);
                    wl2.mLock.unlinkToDeath(wl2, 0);
                }
            }
        }
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
        forceReleaseWakeLockByPidUid(pid, uid, true);
    }

    private void forceReleaseWakeLockByPidUid(int pid, int uid, boolean releaseWS) {
        int i;
        int size;
        int i2;
        int i3;
        int j;
        int i4;
        int size2;
        int length;
        PowerManagerService.WakeLock wakelock;
        WorkSource workSource;
        int length2 = pid;
        int i5 = uid;
        synchronized (this.mLock) {
            try {
                int size3 = this.mWakeLocks.size();
                int i6 = size3 - 1;
                while (true) {
                    int i7 = i6;
                    if (i7 >= 0) {
                        PowerManagerService.WakeLock wakelock2 = (PowerManagerService.WakeLock) this.mWakeLocks.get(i7);
                        if (!releaseWS || wakelock2.mWorkSource == null) {
                            size = size3;
                            PowerManagerService.WakeLock wakelock3 = wakelock2;
                            i = i7;
                            if (wakelock3.mWorkSource == null) {
                                i3 = pid;
                                if (wakelock3.mOwnerPid == i3) {
                                    try {
                                        i2 = uid;
                                        if (wakelock3.mOwnerUid == i2) {
                                            if (DEBUG_SPEW) {
                                                Log.d(TAG, "forceReleaseWakeLockByPidUid, ws null, pid: " + i3 + ", uid: " + i2 + ", wakelock: " + wakelock3);
                                            }
                                            this.mForceReleasedWakeLocks.add(wakelock3);
                                            releaseWakeLockInternalLocked(wakelock3.mLock, wakelock3.mFlags);
                                        }
                                        i6 = i - 1;
                                        length2 = i3;
                                        i5 = i2;
                                        size3 = size;
                                    } catch (Throwable th) {
                                        wakelock = th;
                                        throw wakelock;
                                    }
                                }
                                i2 = uid;
                                i6 = i - 1;
                                length2 = i3;
                                i5 = i2;
                                size3 = size;
                            }
                        } else {
                            int length3 = wakelock2.mWorkSource.size();
                            int j2 = 0;
                            if (1 == length3) {
                                if (wakelock2.mWorkSource.get(0) == i5) {
                                    if (DEBUG_SPEW) {
                                        Log.d(TAG, "forceReleaseWakeLockByPidUid, last one, wakelock: " + wakelock2);
                                    }
                                    this.mForceReleasedWakeLocks.add(wakelock2);
                                    releaseWakeLockInternalLocked(wakelock2.mLock, wakelock2.mFlags);
                                }
                                size = size3;
                                PowerManagerService.WakeLock wakeLock = wakelock2;
                                i = i7;
                            } else if (length3 > 1) {
                                while (true) {
                                    int j3 = j2;
                                    if (j3 >= length3) {
                                        break;
                                    }
                                    if (wakelock2.mWorkSource.get(j3) == i5) {
                                        if (DEBUG_SPEW) {
                                            Log.d(TAG, "forceReleaseWakeLockByPidUid, more than one, wakelock: " + wakelock2);
                                        }
                                        String name = wakelock2.mWorkSource.getName(j3);
                                        if (name == null) {
                                            workSource = new WorkSource(i5);
                                        } else {
                                            workSource = new WorkSource(i5, name);
                                        }
                                        WorkSource workSource2 = workSource;
                                        PowerManagerService.UidState state = new PowerManagerService.UidState(i5);
                                        IBinder iBinder = wakelock2.mLock;
                                        int i8 = wakelock2.mFlags;
                                        String str = wakelock2.mTag;
                                        String str2 = wakelock2.mPackageName;
                                        size2 = size3;
                                        i4 = i7;
                                        try {
                                            String str3 = str2;
                                            r1 = r1;
                                            WorkSource workSource3 = workSource2;
                                            String str4 = name;
                                            String name2 = str3;
                                            j = j3;
                                            length = length3;
                                            wakelock = wakelock2;
                                            PowerManagerService.WakeLock wakeLock2 = new PowerManagerService.WakeLock(this, iBinder, i8, str, name2, workSource3, wakelock2.mHistoryTag, wakelock2.mOwnerUid, wakelock2.mOwnerPid, state);
                                            PowerManagerService.WakeLock cacheWakelock = wakeLock2;
                                            this.mForceReleasedWakeLocks.add(cacheWakelock);
                                            WorkSource newWorkSource = new WorkSource(wakelock.mWorkSource);
                                            WorkSource workSource4 = workSource3;
                                            newWorkSource.remove(workSource4);
                                            PowerManagerService.WakeLock wakeLock3 = cacheWakelock;
                                            notifyWakeLockChangingLocked(wakelock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mOwnerUid, wakelock.mOwnerPid, newWorkSource, wakelock.mHistoryTag);
                                            wakelock.mWorkSource.remove(workSource4);
                                        } catch (Throwable th2) {
                                            wakelock = th2;
                                            int i9 = pid;
                                            int i10 = uid;
                                            throw wakelock;
                                        }
                                    } else {
                                        size2 = size3;
                                        j = j3;
                                        length = length3;
                                        wakelock = wakelock2;
                                        i4 = i7;
                                    }
                                    j2 = j + 1;
                                    wakelock2 = wakelock;
                                    length3 = length;
                                    size3 = size2;
                                    i7 = i4;
                                    length2 = pid;
                                    i5 = uid;
                                }
                                size = size3;
                                int i11 = length3;
                                PowerManagerService.WakeLock wakeLock4 = wakelock2;
                                i = i7;
                            } else {
                                size = size3;
                                PowerManagerService.WakeLock wakeLock5 = wakelock2;
                                i = i7;
                                Log.e(TAG, "forceReleaseWakeLockByPidUid, length invalid: " + length3);
                            }
                        }
                        i3 = pid;
                        i2 = uid;
                        i6 = i - 1;
                        length2 = i3;
                        i5 = i2;
                        size3 = size;
                    } else {
                        int i12 = length2;
                        int i13 = i5;
                        return;
                    }
                }
            } catch (Throwable th3) {
                wakelock = th3;
                int i14 = length2;
                int i15 = i5;
                throw wakelock;
            }
        }
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
        int size;
        int i = pid;
        int i2 = uid;
        synchronized (this.mLock) {
            int size2 = this.mForceReleasedWakeLocks.size();
            int i3 = size2 - 1;
            while (true) {
                int i4 = i3;
                if (i4 >= 0) {
                    PowerManagerService.WakeLock wakelock = this.mForceReleasedWakeLocks.get(i4);
                    if (wakelock.mWorkSource == null) {
                        if (wakelock.mOwnerPid != i) {
                            if (-1 == i) {
                            }
                        }
                        if (wakelock.mOwnerUid == i2 || -1 == i2) {
                            if (DEBUG_SPEW) {
                                Log.d(TAG, "forceRestoreWakeLockByPidUid, WorkSource == null, wakelock: " + wakelock);
                            }
                            acquireWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mWorkSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid);
                            this.mForceReleasedWakeLocks.remove(i4);
                        }
                    } else if (getWorkSourceUid(wakelock.mWorkSource) == i2 || -1 == i2) {
                        int index = findWakeLockIndexLocked(wakelock.mLock);
                        if (index < 0) {
                            if (DEBUG_SPEW) {
                                Log.d(TAG, "forceRestoreWakeLockByPidUid, not found base, wakelock: " + wakelock);
                            }
                            size = size2;
                            int size3 = index;
                            acquireWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mWorkSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid);
                        } else {
                            size = size2;
                            int size4 = index;
                            if (DEBUG_SPEW) {
                                Log.d(TAG, "forceRestoreWakeLockByPidUid, update exist, wakelock: " + wakelock);
                            }
                            WorkSource newWorkSource = new WorkSource(((PowerManagerService.WakeLock) this.mWakeLocks.get(size4)).mWorkSource);
                            newWorkSource.add(wakelock.mWorkSource);
                            WorkSource workSource = newWorkSource;
                            notifyWakeLockChangingLocked((PowerManagerService.WakeLock) this.mWakeLocks.get(size4), wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mOwnerUid, wakelock.mOwnerPid, newWorkSource, wakelock.mHistoryTag);
                            ((PowerManagerService.WakeLock) this.mWakeLocks.get(size4)).mWorkSource.add(wakelock.mWorkSource);
                        }
                        this.mForceReleasedWakeLocks.remove(i4);
                        i3 = i4 - 1;
                        size2 = size;
                    }
                    size = size2;
                    i3 = i4 - 1;
                    size2 = size;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
        return true;
     */
    public boolean getWakeLockByUid(int uid, int wakeflag) {
        synchronized (this.mLock) {
            if (this.mWakeLocks.size() <= 0) {
                return false;
            }
            Iterator<PowerManagerService.WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                PowerManagerService.WakeLock wl = it.next();
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

    /* access modifiers changed from: protected */
    public void disableBrightnessWaitLocked(boolean enableBright) {
        disableBrightnessWaitLocked(enableBright, false);
    }

    /* access modifiers changed from: protected */
    public void disableBrightnessWaitLocked(boolean enableBright, boolean skipWaitKeyguardDismiss) {
        if (this.mBrightnessWaitModeEnabled) {
            this.mHandler.removeMessages(101);
            this.mBrightnessWaitModeEnabled = false;
            this.mBrightnessWaitRet = enableBright;
            this.mSkipWaitKeyguardDismiss = skipWaitKeyguardDismiss;
            this.mDirty |= 16384;
        }
        this.mAuthSucceeded = false;
    }

    /* access modifiers changed from: protected */
    public void setAuthSucceededInternal() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "setAuthSucceededInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled);
            }
            this.mAuthSucceeded = true;
        }
    }

    /* access modifiers changed from: protected */
    public void startWakeUpReadyInternal(long eventTime, int uid, String opPackageName) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "UL_Power startWakeUpReadyInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled);
            }
            if (!this.mBrightnessWaitModeEnabled) {
                if (!FingerViewController.PKGNAME_OF_KEYGUARD.equals(opPackageName)) {
                    this.mPolicy.setPickUpFlag();
                    this.mPolicy.setSyncPowerStateFlag();
                }
                if (wakeUpNoUpdateWithoutInteractiveLocked(eventTime, "startWakeUpReady", uid, opPackageName)) {
                    enableBrightnessWaitLocked();
                    updatePowerStateLocked();
                }
            } else {
                resetWaitBrightTimeoutLocked();
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

    /* access modifiers changed from: protected */
    public int hwBrightnessSetDataInternal(String name, Bundle data) {
        int[] result = {0};
        if (this.mDisplayManagerInternal.hwBrightnessSetData(name, data, result)) {
            return result[0];
        }
        HwBrightnessInnerProcessor processor = this.mHwBrightnessInnerProcessors.get(name);
        if (processor != null) {
            return processor.setData(data);
        }
        Slog.w(TAG, "There is no process to deal with setData(" + name + ")");
        return -1;
    }

    /* access modifiers changed from: protected */
    public int hwBrightnessGetDataInternal(String name, Bundle data) {
        int[] result = {0};
        if (this.mDisplayManagerInternal.hwBrightnessGetData(name, data, result)) {
            return result[0];
        }
        HwBrightnessInnerProcessor processor = this.mHwBrightnessInnerProcessors.get(name);
        if (processor != null) {
            return processor.getData(data);
        }
        Slog.w(TAG, "There is no process to deal with getData(" + name + ")");
        return -1;
    }

    /* access modifiers changed from: protected */
    public int hwBrightnessRegisterCallbackInternal(IHwBrightnessCallback cb, List<String> filter) {
        synchronized (this.mLockHwBrightnessCallbacks) {
            this.mHwBrightnessCallbacks.add(new HwBrightnessCallbackData(cb, filter));
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int hwBrightnessUnregisterCallbackInternal(IHwBrightnessCallback cb) {
        synchronized (this.mLockHwBrightnessCallbacks) {
            int size = this.mHwBrightnessCallbacks.size();
            for (int i = 0; i < size; i++) {
                if (this.mHwBrightnessCallbacks.get(i).getCB() == cb) {
                    this.mHwBrightnessCallbacks.remove(i);
                    return 0;
                }
            }
            Slog.i(TAG, "Unknown callback!");
            return -1;
        }
    }

    private boolean needHwBrightnessNotify(String what, List<String> filter) {
        if (filter == null) {
            return true;
        }
        int size = filter.size();
        for (int i = 0; i < size; i++) {
            if (what.equals(filter.get(i))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void notifyHwBrightnessCallbacks(String what, int arg1, int arg2, Bundle data) {
        synchronized (this.mLockHwBrightnessCallbacks) {
            try {
                int size = this.mHwBrightnessCallbacks.size();
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 < size) {
                        HwBrightnessCallbackData d = this.mHwBrightnessCallbacks.get(i2);
                        String str = what;
                        if (needHwBrightnessNotify(str, d.getFilter())) {
                            final HwBrightnessCallbackData hwBrightnessCallbackData = d;
                            final String str2 = str;
                            final int i3 = arg1;
                            final int i4 = arg2;
                            final Bundle bundle = data;
                            AnonymousClass1 r1 = new Runnable() {
                                public void run() {
                                    try {
                                        hwBrightnessCallbackData.getCB().onStatusChanged(str2, i3, i4, bundle);
                                    } catch (RemoteException e) {
                                        Slog.w(HwPowerManagerService.TAG, "Failed to notify callback! Error:" + e.getMessage());
                                    }
                                }
                            };
                            new Thread(r1).start();
                        }
                        i = i2 + 1;
                    } else {
                        String str3 = what;
                        return;
                    }
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private void loadHwBrightnessInnerProcessors() {
    }

    /* access modifiers changed from: protected */
    public void stopPickupTrunOff() {
        if (PickUpWakeScreenManager.isPickupSensorSupport(this.mContext) && PickUpWakeScreenManager.getInstance() != null) {
            PickUpWakeScreenManager.getInstance().stopTrunOffScrren();
        }
    }

    /* access modifiers changed from: protected */
    public void stopWakeLockedSensor(boolean trunOffScreen) {
        EsdDetection.getInstance(this.mContext);
        if (!EsdDetection.isEsdEnabled()) {
            TurnOnWakeScreenManager turnOnWakeScreenManager = TurnOnWakeScreenManager.getInstance(this.mContext);
            if (turnOnWakeScreenManager != null && turnOnWakeScreenManager.isTurnOnSensorSupport()) {
                turnOnWakeScreenManager.turnOffAllSensor();
                if (trunOffScreen) {
                    turnOnWakeScreenManager.turnOffScreen();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetWaitBrightTimeoutLocked() {
        this.mHandler.removeMessages(101);
        Message msg = this.mHandler.obtainMessage(101);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, (long) this.mWaitBrightTimeout);
    }

    /* access modifiers changed from: protected */
    public void stopWakeUpReadyInternal(long eventTime, int uid, boolean enableBright, String opPackageName) {
        if (DEBUG) {
            Slog.d(TAG, "UL_Powerbegin wait mLock, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled + " enableBright = " + enableBright);
        }
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "UL_Power stopWakeUpReadyInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled + " enableBright = " + enableBright);
            }
            if (this.mBrightnessWaitModeEnabled) {
                if (!FingerViewController.PKGNAME_OF_KEYGUARD.equals(opPackageName)) {
                    this.mPolicy.setSyncPowerStateFlag();
                }
                if (enableBright) {
                    this.mLastWakeTime = eventTime;
                    setWakefulnessLocked(1, 0);
                    userActivityNoUpdateLocked(eventTime, 0, 0, uid);
                    disableBrightnessWaitLocked(true, !FingerViewController.PKGNAME_OF_KEYGUARD.equals(opPackageName));
                    updatePowerStateLocked();
                } else {
                    goToSleepNoUpdateLocked(eventTime, 0, 0, uid);
                    updatePowerStateLocked();
                }
            } else if (enableBright) {
                if (DEBUG) {
                    Slog.d(TAG, "UL_Power stopWakeUpReadyInternal, brightness wait timeout.");
                }
                if (wakeUpNoUpdateLocked(eventTime, "BrightnessWaitTimeout", uid, opPackageName, uid)) {
                    updatePowerStateLocked();
                }
            }
        }
    }

    private boolean wakeUpNoUpdateWithoutInteractiveLocked(long eventTime, String reason, int uid, String opPackageName) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "UL_Power wakeUpNoUpdateWithoutInteractiveLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastSleepTime || this.mWakefulness == 1 || !this.mBootCompleted || !this.mSystemReady || this.mProximityPositive) {
            notifyWakeupResult(false);
            return false;
        }
        if (mSupportFaceDetect) {
            startIntelliService();
        }
        Trace.traceBegin(131072, "wakeUpWithoutInteractive");
        try {
            int i = this.mWakefulness;
            if (i != 0) {
                switch (i) {
                    case 2:
                        Slog.i(TAG, "UL_Power Waking up from dream (uid " + uid + ")...");
                        Jlog.d(6, "JL_PMS_WAKEFULNESS_DREAMING");
                        break;
                    case 3:
                        Slog.i(TAG, "UL_Power Waking up from dozing (uid " + uid + ")...");
                        Jlog.d(7, "JL_PMS_WAKEFULNESS_NAPPING");
                        break;
                }
            } else {
                Slog.i(TAG, "UL_Power Waking up from sleep (uid " + uid + ")...");
                Jlog.d(5, "JL_PMS_WAKEFULNESS_ASLEEP");
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

    /* access modifiers changed from: protected */
    public void handleWaitBrightTimeout() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "UL_Power handleWaitBrightTimeout mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled + " mWakefulness = " + this.mWakefulness);
            }
            if (this.mBrightnessWaitModeEnabled) {
                if (!goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 101, 0, 1000)) {
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
            if (tag == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (this.mWakeLocks.size() <= 0) {
                return false;
            } else {
                Iterator<PowerManagerService.WakeLock> it = this.mWakeLocks.iterator();
                while (it.hasNext()) {
                    PowerManagerService.WakeLock wl = it.next();
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
    }

    public void dumpInternal(PrintWriter pw) {
        HwPowerManagerService.super.dumpInternal(pw);
        pw.println("Proxyed WakeLocks State : ");
        synchronized (this.mLock) {
            Iterator<PowerManagerService.WakeLock> it = this.mProxyedWakeLocks.iterator();
            while (it.hasNext()) {
                pw.println(" Proxyed WakeLocks :" + it.next());
            }
            Iterator<PowerManagerService.WakeLock> it2 = this.mForceReleasedWakeLocks.iterator();
            while (it2.hasNext()) {
                pw.println(" Force Released WakeLocks :" + it2.next());
            }
            Iterator<ProxyWLProcessInfo> it3 = this.mProxyWLProcessList.iterator();
            while (it3.hasNext()) {
                ProxyWLProcessInfo pi = it3.next();
                pw.println(" ProxyWLProcess pid:" + pi.mPid + ", uid: " + pi.mUid + ", proxyWs: " + pi.mProxyWS);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendTempBrightnessToMonitor(String paramType, int brightness) {
        if (this.mDisplayEffectMonitor != null) {
            String[] packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()).split(":");
            if (packageName.length != 0) {
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, paramType);
                params.put("brightness", Integer.valueOf(brightness));
                params.put("packageName", packageName[0]);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendBrightnessModeToMonitor(boolean manualMode, String packageName) {
        if (this.mDisplayEffectMonitor != null) {
            if (!this.mHadSendBrightnessModeToMonitor) {
                packageName = "android";
                this.mHadSendBrightnessModeToMonitor = true;
            }
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "brightnessMode");
            params.put("brightnessMode", manualMode ? "MANUAL" : "AUTO");
            params.put("packageName", packageName);
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void sendManualBrightnessToMonitor(int brightness, String packageName) {
        if (this.mDisplayEffectMonitor != null) {
            if (!this.mHadSendManualBrightnessToMonitor) {
                packageName = "android";
                this.mHadSendManualBrightnessToMonitor = true;
            }
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "manualBrightness");
            params.put("brightness", Integer.valueOf(brightness));
            params.put("packageName", packageName);
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void sendBootCompletedToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "bootCompleted");
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWakeupResult(boolean isWakenupThisTime) {
        AwareFakeActivityRecg.self().notifyWakeupResult(isWakenupThisTime);
    }

    /* access modifiers changed from: protected */
    public void startIntelliService() {
        if (DEBUG) {
            Slog.d(TAG, "bind IntelliService, mIntelliServiceBound=" + this.mIntelliServiceBound);
        }
        if (!this.mIntelliServiceBound) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    try {
                        boolean unused = HwPowerManagerService.this.mIntelliServiceBound = true;
                        boolean success = HwPowerManagerService.this.mContext.bindServiceAsUser(HwPowerManagerService.this.mIntelliIntent, HwPowerManagerService.this.connection, 1, UserHandle.CURRENT);
                        if (PowerManagerService.DEBUG) {
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

    /* access modifiers changed from: protected */
    public void stopIntelliService() {
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

    /* access modifiers changed from: protected */
    public int registerFaceDetect() {
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

    /* access modifiers changed from: protected */
    public int unregisterFaceDetect() {
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

    /* access modifiers changed from: private */
    public void resetFaceDetect() {
        this.mIntelliServiceBound = false;
        this.mFaceDetecting = false;
        this.mShouldFaceDetectLater = false;
        this.mIRemote = null;
    }

    private boolean dropLogs(String tag, int uid) {
        if (!"RILJ".equals(tag) || 1001 != uid) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void notifyWakeLockToIAware(int uid, int pid, String packageName, String Tag) {
        SysLoadManager.getInstance().notifyWakeLock(uid, pid, packageName, Tag);
    }

    /* access modifiers changed from: protected */
    public void notifyWakeLockReleaseToIAware(int uid, int pid, String packageName, String Tag) {
        SysLoadManager.getInstance().notifyWakeLockRelease(uid, pid, packageName, Tag);
    }
}
