package com.android.server.power;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IAppOpsService;
import com.android.server.LocalServices;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.lights.Light;
import com.android.server.pg.PGManagerInternal;
import com.android.server.policy.TurnOnWakeScreenManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.PowerManagerService;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.IntelliServer.intellilib.IIntelliListener;
import com.huawei.IntelliServer.intellilib.IIntelliService;
import com.huawei.IntelliServer.intellilib.IntelliAlgoResult;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.HwPartIawareUtil;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class HwPowerManagerService extends PowerManagerService {
    private static final int ANY_PID = -1;
    private static final int ANY_UID = -1;
    private static final int ANY_WAKELOCK_FLAG = -1;
    private static final int BLUE_ORDER = 2;
    private static final String CAR_PACKAGENAME = "com.huawei.vdrive";
    private static final String COLOR_TEMPERATURE_RGB = "color_temperature_rgb";
    private static final int EYE_PROTECTIION_OFF = 0;
    private static final int EYE_PROTECTIION_ON = 1;
    private static final int EYE_PROTECTIION_ON_BY_USER = 3;
    private static final int GREEN_ORDER = 1;
    private static final String INCALLUI_PACKAGENAME = "com.android.incallui";
    private static final int INTELLI_DETECT_FACE_PRESENCE = 1;
    private static final int INVALID_VALUE = -1;
    private static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final int MAX_BRIGHTNESS = 255;
    private static final String MEETIME_PACKAGENAME = "com.huawei.meetime";
    private static final int RED_ORDER = 0;
    private static final String SERVICE_ACTION = "com.huawei.intelliServer.intelliServer";
    private static final String SERVICE_CLASS = "com.huawei.intelliServer.intelliServer.IntelliService";
    private static final String SERVICE_PACKAGE = "com.huawei.intelliServer.intelliServer";
    private static final int SUBTYPE_DROP_PROXYED_WAKELOCK = 1;
    private static final int SUBTYPE_DROP_WAKELOCK = 5;
    private static final int SUBTYPE_PROXY_NO_WORKSOURCE = 2;
    private static final int SUBTYPE_PROXY_SYS_WAKELOCK = 4;
    private static final int SUBTYPE_RELEASE_NO_WORKSOURCE = 3;
    private static final String TAG = "HwPowerManagerService";
    private static final int TYPE_FACE_STAY_LIT = 3;
    private static final int WAIT_BRIGHT_TIMEOUT = 3000;
    private static boolean isLoadLibraryFailed;
    private ServiceConnection connection = new ServiceConnection() {
        /* class com.android.server.power.HwPowerManagerService.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            if (!HwPowerManagerService.this.mIsIntelliServiceBound) {
                Slog.w(HwPowerManagerService.TAG, "IntelliService not bound, ignore.");
                return;
            }
            if (PowerManagerService.DEBUG) {
                Slog.i(HwPowerManagerService.TAG, "IntelliService Connected, mShouldFaceDetectLater=" + HwPowerManagerService.this.mShouldFaceDetectLater);
            }
            HwPowerManagerService.this.mIintelliService = IIntelliService.Stub.asInterface(binder);
            if (HwPowerManagerService.this.mShouldFaceDetectLater) {
                HwPowerManagerService.this.mShouldFaceDetectLater = false;
                HwPowerManagerService.this.registerFaceDetect();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (PowerManagerService.DEBUG) {
                Slog.i(HwPowerManagerService.TAG, "IntelliService Disconnected, mIsIntelliServiceBound=" + HwPowerManagerService.this.mIsIntelliServiceBound);
            }
            if (HwPowerManagerService.this.mIsIntelliServiceBound) {
                HwPowerManagerService.this.resetFaceDetect();
            }
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            Slog.w(HwPowerManagerService.TAG, "IntelliService binding died, mIsIntelliServiceBound =" + HwPowerManagerService.this.mIsIntelliServiceBound);
            if (HwPowerManagerService.this.mIsIntelliServiceBound) {
                HwPowerManagerService.this.resetFaceDetect();
            }
        }
    };
    private boolean isVdriveBackLightMode = false;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.power.HwPowerManagerService.AnonymousClass1 */

        public void call(Bundle extras) {
            if (extras == null) {
                Slog.i(HwPowerManagerService.TAG, "AMS callback , extras=null");
                return;
            }
            int pid = extras.getInt("pid");
            int uid = extras.getInt("uid");
            synchronized (HwPowerManagerService.this.mLock) {
                Slog.i(HwPowerManagerService.TAG, "appdie release wakelock, pid = " + pid + ", uid = " + uid);
                for (int i = HwPowerManagerService.this.mWakeLocks.size() + -1; i >= 0; i--) {
                    PowerManagerService.WakeLock wakelock = (PowerManagerService.WakeLock) HwPowerManagerService.this.mWakeLocks.get(i);
                    if (wakelock.mOwnerPid == pid && wakelock.mOwnerUid == uid) {
                        Slog.i(HwPowerManagerService.TAG, "releaseWakeLockInternal wl: " + wakelock);
                        HwPowerManagerService.this.releaseWakeLockInternal(wakelock.mLock, wakelock.mFlags);
                    }
                }
                for (int i2 = HwPowerManagerService.this.mForceReleasedWakeLocks.size() - 1; i2 >= 0; i2--) {
                    PowerManagerService.WakeLock wl = (PowerManagerService.WakeLock) HwPowerManagerService.this.mForceReleasedWakeLocks.get(i2);
                    if (wl.mOwnerUid == uid && wl.mOwnerPid == pid) {
                        Log.i(HwPowerManagerService.TAG, "drop from ForceReleasedWakeLocks wl: " + wl);
                        HwPowerManagerService.this.mForceReleasedWakeLocks.remove(i2);
                        HwPowerManagerService.this.unlinkToDeath(wl);
                    }
                }
                for (int i3 = HwPowerManagerService.this.mProxyedWakeLocks.size() - 1; i3 >= 0; i3--) {
                    PowerManagerService.WakeLock wl2 = (PowerManagerService.WakeLock) HwPowerManagerService.this.mProxyedWakeLocks.get(i3);
                    if (wl2.mOwnerUid == uid && wl2.mOwnerPid == pid) {
                        Log.i(HwPowerManagerService.TAG, "drop from ProxyedWakeLocks wl: " + wl2);
                        HwPowerManagerService.this.mProxyedWakeLocks.remove(i3);
                        HwPowerManagerService.this.unlinkToDeath(wl2);
                    }
                }
            }
        }
    };
    private Light mBackLight;
    private final Context mContext;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private int mEyesProtectionMode = 0;
    private final List<PowerManagerService.WakeLock> mForceReleasedWakeLocks = new ArrayList();
    private InputManagerServiceEx.DefaultHwInputManagerLocalService mHwInputManagerInternal;
    private SettingsObserver mHwSettingsObserver;
    private IIntelliService mIintelliService;
    private Intent mIntelliIntent;
    private IIntelliListener mIntelliListener = new IIntelliListener.Stub() {
        /* class com.android.server.power.HwPowerManagerService.AnonymousClass3 */

        @Override // com.huawei.IntelliServer.intellilib.IIntelliListener
        public void onEvent(IntelliAlgoResult intelliAlgoResult) throws RemoteException {
            int result = intelliAlgoResult != null ? intelliAlgoResult.getPrecenseStatus() : 0;
            if (PowerManagerService.DEBUG) {
                Slog.i(HwPowerManagerService.TAG, "onEvent result=" + result + ",mIsFaceDetecting=" + HwPowerManagerService.this.mIsFaceDetecting);
            }
            if (result == 1 && HwPowerManagerService.this.mIsFaceDetecting) {
                long ident = Binder.clearCallingIdentity();
                try {
                    HwPowerManagerService.this.userActivityInternal(SystemClock.uptimeMillis(), 0, 0, 1000);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        @Override // com.huawei.IntelliServer.intellilib.IIntelliListener
        public void onErr(int i) throws RemoteException {
        }
    };
    private boolean mIsFaceDetecting;
    private boolean mIsIntelliServiceBound;
    private PGManagerInternal mPGManagerInternal;
    private WindowManagerPolicy mPolicy;
    private final List<ProxyWakeLockProcessInfo> mProxyWakeLockProcessList = new ArrayList();
    private final List<PowerManagerService.WakeLock> mProxyedWakeLocks = new ArrayList();
    private boolean mShouldFaceDetectLater;
    private WindowManagerInternal mWindowManagerInternal;

    private static native void finalizeNative();

    private static native void initNative();

    private static native void initSurfaceComposerClient();

    private static native int nativeGetDisplayPanelType();

    private native int nativeSetColorTemperature(int i);

    private native int nativeUpdateRgbGamma(float f, float f2, float f3);

    static {
        isLoadLibraryFailed = false;
        try {
            System.loadLibrary("hwpwmanager_jni");
        } catch (UnsatisfiedLinkError e) {
            isLoadLibraryFailed = true;
            Slog.e(TAG, "hwpwmanager_jni library not found!");
        }
    }

    public HwPowerManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(this.mContext);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        if (!isLoadLibraryFailed) {
            initNative();
        }
    }

    public void systemReady(IAppOpsService appOps) {
        HwPowerManagerService.super.systemReady(appOps);
        initSurfaceComposerClient();
        this.mPolicy = (WindowManagerPolicy) getLocalService(WindowManagerPolicy.class);
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mHwInputManagerInternal = (InputManagerServiceEx.DefaultHwInputManagerLocalService) getLocalService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        this.mBackLight = this.mLightsManager.getLight(0);
        setColorTemperatureAccordingToSetting();
        this.mHwSettingsObserver = new SettingsObserver(this.mHandler);
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Settings.System.getUriFor(COLOR_TEMPERATURE_RGB), false, this.mHwSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor(KEY_EYES_PROTECTION), false, this.mSettingsObserver, -2);
        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction("android.intent.action.HEADSET_PLUG");
        this.mContext.registerReceiver(new HeadsetReceiver(), headsetFilter, null, this.mHandler);
        this.mIntelliIntent = new Intent("com.huawei.intelliServer.intelliServer");
        this.mIntelliIntent.setClassName("com.huawei.intelliServer.intelliServer", SERVICE_CLASS);
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "appDie");
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            synchronized (HwPowerManagerService.this.mLock) {
                HwPowerManagerService.this.setColorTemperatureAccordingToSetting();
            }
        }
    }

    private final class HeadsetReceiver extends BroadcastReceiver {
        private HeadsetReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (HwPowerManagerService.this.mLock) {
                SystemClock.uptimeMillis();
                Slog.i(HwPowerManagerService.TAG, "HeadsetReceiver, state:" + intent.getIntExtra("state", 0));
                if (intent.getIntExtra("state", 0) == 1) {
                    HwPowerManagerService.this.wakeUpAndUserActiviyLocked(2, "headset.connected");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wakeUpAndUserActiviyLocked(int reason, String details) {
        long now = SystemClock.uptimeMillis();
        if (wakeUpNoUpdateLocked(now, reason, details, 1000, this.mContext.getOpPackageName(), 1000) || userActivityNoUpdateLocked(now, 0, 0, 1000)) {
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (!isLoadLibraryFailed) {
            finalizeNative();
        }
        HwPowerManagerService.super.finalize();
    }

    public int setColorTemperatureInternal(int colorTemper) {
        Slog.i(TAG, "setColorTemperature:" + colorTemper);
        try {
            if (!isLoadLibraryFailed) {
                return nativeSetColorTemperature(colorTemper);
            }
            Slog.i(TAG, "nativeSetColorTemperature not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "nativeSetColorTemperature not found!");
            return -1;
        }
    }

    public int updateRgbGammaInternal(float red, float green, float blue) {
        Slog.i(TAG, "updateRgbGammaInternal:red=" + red + " green=" + green + " blue=" + blue);
        try {
            if (!isLoadLibraryFailed) {
                return nativeUpdateRgbGamma(red, green, blue);
            }
            Slog.i(TAG, "nativeUpdateRgbGamma not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "nativeUpdateRgbGamma not found!");
            return -1;
        }
    }

    public int getDisplayPanelTypeInternal() {
        try {
            if (!isLoadLibraryFailed) {
                return nativeGetDisplayPanelType();
            }
            Slog.i(TAG, "nativeGetDisplayPanelType failed because of library not found!");
            return -1;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "nativeGetDisplayPanelType not found!");
            return -1;
        }
    }

    private void setColorRGB(float red, float green, float blue) {
        boolean isRedInvalid = red < 0.0f || red > 1.0f;
        boolean isGreenInvalid = green < 0.0f || green > 1.0f;
        boolean isBlueInvalid = blue < 0.0f || blue > 1.0f;
        if (isRedInvalid || isGreenInvalid || isBlueInvalid) {
            Slog.w(TAG, "Parameters invalid: red=" + red + ", green=" + green + ", blue=" + blue);
            return;
        }
        try {
            Class clazz = Class.forName("com.huawei.android.os.PowerManagerCustEx");
            clazz.getMethod("updateRgbGamma", Float.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE, Integer.TYPE).invoke(clazz, Float.valueOf(red), Float.valueOf(green), Float.valueOf(blue), 18, 7);
            Log.i(TAG, "setColorTemperatureAccordingToSetting and setColorRGB sucessfully:red = " + red + ", green = " + green + ", blue = " + blue);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException happened when setColorRGB");
        } catch (SecurityException e2) {
            Log.e(TAG, "SecurityException happened when setColorRGB");
        } catch (ClassNotFoundException e3) {
            Log.e(TAG, "ClassNotFoundException happened when setColorRGB");
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "IllegalAccessException happened when setColorRGB");
        } catch (IllegalArgumentException e5) {
            Log.e(TAG, "IllegalArgumentException happened when setColorRGB");
        } catch (InvocationTargetException e6) {
            Log.e(TAG, "InvocationTargetException happened when setColorRGB");
        }
    }

    /* access modifiers changed from: protected */
    public void setColorTemperatureAccordingToSetting() {
        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;
        Slog.i(TAG, "setColorTemperatureAccordingToSetting new from displayengine.");
        try {
            if (this.mEyesProtectionMode == 1) {
                setColorRGB(1.0f, 1.0f, 1.0f);
                return;
            }
            String colorRGB = Settings.System.getStringForUser(this.mContext.getContentResolver(), COLOR_TEMPERATURE_RGB, -2);
            if (colorRGB != null) {
                List<String> rgbarryList = new ArrayList<>(Arrays.asList(colorRGB.split(",")));
                if (rgbarryList.size() <= 2) {
                    setColorRGB(1.0f, 1.0f, 1.0f);
                    setColorRGB(1.0f, 1.0f, 1.0f);
                    return;
                }
                red = Float.valueOf(rgbarryList.get(0)).floatValue();
                green = Float.valueOf(rgbarryList.get(1)).floatValue();
                blue = Float.valueOf(rgbarryList.get(2)).floatValue();
            } else {
                Slog.w(TAG, "ColorTemperature read from setting failed, and set default values");
            }
            setColorRGB(red, green, blue);
        } catch (IndexOutOfBoundsException e) {
            Slog.e(TAG, "IndexOutOfBoundsException happened when setColorTemperatureAccordingToSetting");
        } catch (NumberFormatException e2) {
            Slog.e(TAG, "NumberFormatException happened when setColorTemperatureAccordingToSetting");
        } catch (Throwable th) {
            setColorRGB(1.0f, 1.0f, 1.0f);
            throw th;
        }
    }

    public int getAdjustedMaxTimeout() {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal == null || this.mPolicy == null || windowManagerInternal.isCoverOpen() || this.mPolicy.isKeyguardLocked() || isPhoneInCall()) {
            return 0;
        }
        return 10000;
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

    /* access modifiers changed from: protected */
    public void updateSettingsLocked() {
        HwPowerManagerService.super.updateSettingsLocked();
        this.mEyesProtectionMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_EYES_PROTECTION, 0, -2);
        Slog.i(TAG, "updateSettingsLocked mEyesProtectionMode:" + this.mEyesProtectionMode);
    }

    public void handleWakeLockDeath(PowerManagerService.WakeLock wakeLock) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.i(TAG, "handleWakeLockDeath: lock=" + Objects.hashCode(wakeLock.mLock) + " [" + wakeLock.mTag + "]");
            }
            for (int i = this.mForceReleasedWakeLocks.size() - 1; i >= 0; i--) {
                PowerManagerService.WakeLock wl = this.mForceReleasedWakeLocks.get(i);
                if (wl.mLock == wakeLock.mLock) {
                    Log.i(TAG, "remove from forceReleased list, wl: " + wl);
                    this.mForceReleasedWakeLocks.remove(i);
                }
            }
            for (int i2 = this.mProxyedWakeLocks.size() - 1; i2 >= 0; i2--) {
                PowerManagerService.WakeLock wl2 = this.mProxyedWakeLocks.get(i2);
                if (wl2.mLock == wakeLock.mLock) {
                    Log.i(TAG, "remove from proxyed list, wl: " + wl2);
                    this.mProxyedWakeLocks.remove(i2);
                }
            }
            HwPowerManagerService.super.handleWakeLockDeath(wakeLock);
        }
    }

    /* access modifiers changed from: private */
    public static final class ProxyWakeLockProcessInfo {
        private boolean mIsProxyWorkSource;
        private int mPid;
        private int mUid;

        ProxyWakeLockProcessInfo(int pid, int uid, boolean isProxyWorkSource) {
            this.mPid = pid;
            this.mUid = uid;
            this.mIsProxyWorkSource = isProxyWorkSource;
        }

        public boolean isSameProcess(int pid, int uid) {
            return (this.mPid == pid || pid == -1) && (this.mUid == uid || uid == -1);
        }

        public int getPid() {
            return this.mPid;
        }

        public int getUid() {
            return this.mUid;
        }

        public boolean isProxyWorkSource() {
            return this.mIsProxyWorkSource;
        }
    }

    private void restoreProxyWakeLockLocked(int pid, int uid) {
        for (int i = this.mProxyedWakeLocks.size() - 1; i >= 0; i--) {
            PowerManagerService.WakeLock wakelock = this.mProxyedWakeLocks.get(i);
            if (((wakelock.mOwnerUid == uid || uid == -1) && (wakelock.mOwnerPid == pid || pid == -1)) || (wakelock.mWorkSource != null && getWorkSourceUid(wakelock.mWorkSource) == uid)) {
                acquireWakeLockInternalLocked(wakelock);
                this.mProxyedWakeLocks.remove(i);
            }
        }
    }

    private void removeProxyWakeLockProcessLocked(int pid, int uid) {
        for (int i = this.mProxyWakeLockProcessList.size() - 1; i >= 0; i--) {
            if (this.mProxyWakeLockProcessList.get(i).isSameProcess(pid, uid)) {
                if (DEBUG) {
                    Log.i(TAG, "remove pxy wl, pid: " + pid + " , uid: " + uid + " from pxy process list.");
                }
                this.mProxyWakeLockProcessList.remove(i);
            }
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean isProxy) {
        synchronized (this.mLock) {
            proxyWakeLockByPidUidLocked(pid, uid, isProxy, true);
        }
    }

    private void proxyWakeLockByPidUidLocked(int pid, int uid, boolean isProxy, boolean isProxyWorkSource) {
        if (isProxy) {
            this.mProxyWakeLockProcessList.add(new ProxyWakeLockProcessInfo(pid, uid, isProxyWorkSource));
            return;
        }
        restoreProxyWakeLockLocked(pid, uid);
        removeProxyWakeLockProcessLocked(pid, uid);
    }

    private int getWorkSourceUid(WorkSource ws) {
        if (ws == null) {
            return -1;
        }
        if (ws.size() > 0) {
            return ws.get(0);
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null && workChains.size() > 0) {
            return workChains.get(0).getAttributionUid();
        }
        Log.w(TAG, "getWorkSourceUid, workChains is empty.");
        return -1;
    }

    private boolean isWorkSouceIncludeUid(WorkSource ws, int uid) {
        int wsSize = ws.size();
        for (int j = 0; j < wsSize; j++) {
            if (ws.get(j) == uid) {
                return true;
            }
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains == null) {
            return false;
        }
        for (WorkSource.WorkChain workChainItem : workChains) {
            if (workChainItem != null && workChainItem.getAttributionUid() == uid) {
                return true;
            }
        }
        return false;
    }

    private boolean canProxySysWakeLockLocked(int pid, int uid) {
        for (int i = this.mWakeLocks.size() - 1; i >= 0; i--) {
            PowerManagerService.WakeLock wakelock = (PowerManagerService.WakeLock) this.mWakeLocks.get(i);
            if (wakelock.mWorkSource != null && isWorkSouceIncludeUid(wakelock.mWorkSource, uid)) {
                Log.i(TAG, "can't proxy sys wakelock for uid in ws, wl: " + wakelock);
                return false;
            }
        }
        return true;
    }

    private boolean proxySysWakeLockByPidUidLocked(int pid, int uid) {
        if (!canProxySysWakeLockLocked(pid, uid)) {
            return false;
        }
        proxyWakeLockByPidUidLocked(pid, uid, true, false);
        forceReleaseWakeLockByPidUidLocked(pid, uid, false);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean acquireProxyWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        Object obj;
        int tmpPid;
        int tmpUid;
        if (!this.mSystemReady) {
            Log.w(TAG, "acquireProxyWakeLock, mSystemReady is false.");
            return false;
        }
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                for (ProxyWakeLockProcessInfo pwi : this.mProxyWakeLockProcessList) {
                    if (!pwi.isProxyWorkSource() || ws == null) {
                        tmpPid = pid;
                        tmpUid = uid;
                    } else {
                        int workSourceUid = getWorkSourceUid(ws);
                        tmpPid = -1;
                        tmpUid = workSourceUid >= 0 ? workSourceUid : uid;
                    }
                    if (pwi.isSameProcess(tmpPid, tmpUid)) {
                        Log.i(TAG, "acquire pxy wl : lock=" + Objects.hashCode(lock) + ", pid: " + pid + " , uid: " + uid + ", ws: " + ws + ", packageName: " + packageName + ", tag: " + tag);
                        obj = obj2;
                        try {
                            PowerManagerService.WakeLock wakelock = new PowerManagerService.WakeLock(this, lock, flags, tag, packageName, ws, historyTag, uid, pid, new PowerManagerService.UidState(uid));
                            try {
                                lock.linkToDeath(wakelock, 0);
                                this.mProxyedWakeLocks.add(wakelock);
                                return true;
                            } catch (RemoteException e) {
                                throw new IllegalArgumentException("HW Wake lock is already dead.");
                            } catch (Throwable th) {
                                ex = th;
                                throw ex;
                            }
                        } catch (Throwable th2) {
                            ex = th2;
                            throw ex;
                        }
                    }
                }
            } catch (Throwable th3) {
                ex = th3;
                obj = obj2;
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
            if (DEBUG) {
                Slog.i(TAG, "update ws pxy wl : lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], ws=" + ws + ", curr.ws: " + wakeLock.mWorkSource);
            }
            if (!wakeLock.hasSameWorkSource(ws)) {
                wakeLock.mHistoryTag = historyTag;
                wakeLock.updateWorkSource(ws);
            }
            return true;
        }
    }

    private boolean releaseWakeLockFromListLocked(IBinder lock, List<PowerManagerService.WakeLock> list) {
        int length = list.size();
        boolean isRelease = false;
        for (int i = length - 1; i >= 0; i--) {
            PowerManagerService.WakeLock wakelock = list.get(i);
            if (wakelock.mLock == lock) {
                if (DEBUG && !dropLogs(wakelock.mTag, wakelock.mOwnerUid)) {
                    Log.i(TAG, "release ws pxy wl : lock= wl:" + wakelock + " from list, length: " + length);
                }
                unlinkToDeath(wakelock);
                list.remove(i);
                isRelease = true;
            }
        }
        return isRelease;
    }

    /* access modifiers changed from: protected */
    public boolean releaseProxyWakeLock(IBinder lock) {
        boolean isRelease;
        if (!this.mSystemReady) {
            Log.w(TAG, "releaseProxyWakeLock, mSystemReady is false.");
            return false;
        }
        synchronized (this.mLock) {
            isRelease = false | releaseWakeLockFromListLocked(lock, this.mProxyedWakeLocks) | releaseWakeLockFromListLocked(lock, this.mForceReleasedWakeLocks);
        }
        return isRelease;
    }

    private void releaseWakeLockInternalLocked(IBinder lock, int flags) {
        int index = findWakeLockIndexLocked(lock);
        if (index < 0) {
            Slog.w(TAG, "releaseWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + " [not found], flags=0x" + Integer.toHexString(flags));
            return;
        }
        PowerManagerService.WakeLock wakeLock = (PowerManagerService.WakeLock) this.mWakeLocks.get(index);
        if (DEBUG && !dropLogs(wakeLock.mTag, wakeLock.mOwnerUid)) {
            Slog.i(TAG, "releaseWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + wakeLock.mTag + " \", packageName=" + wakeLock.mPackageName + "\", ws=" + wakeLock.mWorkSource + " , uid=" + wakeLock.mOwnerUid + " , pid=" + wakeLock.mOwnerPid);
        }
        if ((flags & 1) != 0) {
            this.mRequestWaitForNegativeProximity = true;
        }
        removeWakeLockLocked(wakeLock, index);
    }

    private void acquireWakeLockInternalLocked(PowerManagerService.WakeLock wl) {
        int uid;
        PowerManagerService.WakeLock wakeLock;
        boolean isNotifyAcquire;
        IBinder lock = wl.mLock;
        int flags = wl.mFlags;
        String tag = wl.mTag;
        String packageName = wl.mPackageName;
        WorkSource ws = wl.mWorkSource;
        int uid2 = wl.mOwnerUid;
        int pid = wl.mOwnerPid;
        boolean dropLogs = dropLogs(tag, uid2);
        if (DEBUG && !dropLogs) {
            Slog.i(TAG, "acquireWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + tag + "\", packageName=" + packageName + "\", ws=" + ws + " , uid=" + uid2 + ", pid=" + pid);
        }
        if (!lock.isBinderAlive()) {
            Slog.w(TAG, "lock:" + Objects.hashCode(lock) + " is already dead, tag=\"" + tag + "\", packageName=" + packageName + ", ws=" + ws + ", uid=" + uid2 + ", pid=" + pid);
            return;
        }
        int index = findWakeLockIndexLocked(lock);
        if (index >= 0) {
            if (DEBUG && !dropLogs) {
                Slog.i(TAG, "acquireWakeLockInternalLocked: lock=" + Objects.hashCode(lock) + ", existing wakelock");
            }
            PowerManagerService.WakeLock wakeLock2 = (PowerManagerService.WakeLock) this.mWakeLocks.get(index);
            if (!wakeLock2.hasSameProperties(flags, tag, ws, uid2, pid)) {
                String historyTag = wl.mHistoryTag;
                uid = uid2;
                notifyWakeLockChangingLocked(wakeLock2, flags, tag, packageName, uid2, pid, ws, historyTag);
                wakeLock2.updateProperties(flags, tag, packageName, ws, historyTag, uid, pid);
            } else {
                uid = uid2;
            }
            isNotifyAcquire = false;
            wakeLock = wakeLock2;
            if (wl != wakeLock) {
                unlinkToDeath(wl);
            }
        } else {
            uid = uid2;
            this.mWakeLocks.add(wl);
            setWakeLockDisabledStateLocked(wl);
            isNotifyAcquire = true;
            wakeLock = wl;
        }
        applyWakeLockFlagsOnAcquireLocked(wakeLock, uid);
        this.mDirty |= 1;
        updatePowerStateLocked();
        if (isNotifyAcquire) {
            notifyWakeLockAcquiredLocked(wakeLock);
        }
    }

    public boolean proxyedWakeLock(int subType, List<String> value) {
        if (value == null || value.size() != 2) {
            Log.w(TAG, "invaild para for :" + subType + " value:" + value);
            return false;
        }
        boolean isSuccess = true;
        try {
            int pid = Integer.parseInt(value.get(0));
            int uid = Integer.parseInt(value.get(1));
            synchronized (this.mLock) {
                if (subType == 1) {
                    dropProxyedWakeLockLocked(pid, uid);
                } else if (subType == 2) {
                    proxyWakeLockByPidUidLocked(pid, uid, true, false);
                } else if (subType == 3) {
                    forceReleaseWakeLockByPidUidLocked(pid, uid, false);
                } else if (subType == 4) {
                    isSuccess = proxySysWakeLockByPidUidLocked(pid, uid);
                } else if (subType != 5) {
                    isSuccess = false;
                } else {
                    dropWakeLockLocked(pid, uid);
                }
            }
            return isSuccess;
        } catch (NumberFormatException ex) {
            Log.e(TAG, "proxyedWakeLock Exception !", ex);
            return false;
        }
    }

    private void dropProxyedWakeLockLocked(int pid, int uid) {
        for (int i = this.mForceReleasedWakeLocks.size() - 1; i >= 0; i--) {
            PowerManagerService.WakeLock wl = this.mForceReleasedWakeLocks.get(i);
            if (((wl.mOwnerUid == uid || uid == -1) && (wl.mOwnerPid == pid || pid == -1)) || (pid == -1 && wl.mWorkSource != null && getWorkSourceUid(wl.mWorkSource) == uid)) {
                Log.i(TAG, "drop wl from forceReleased list: " + wl);
                this.mForceReleasedWakeLocks.remove(i);
                unlinkToDeath(wl);
            }
        }
        for (int i2 = this.mProxyedWakeLocks.size() - 1; i2 >= 0; i2--) {
            PowerManagerService.WakeLock wl2 = this.mProxyedWakeLocks.get(i2);
            if ((uid == -1 && pid == -1) || ((wl2.mOwnerUid == uid && (wl2.mOwnerPid == pid || pid == -1)) || (pid == -1 && wl2.mWorkSource != null && getWorkSourceUid(wl2.mWorkSource) == uid))) {
                Log.i(TAG, "drop wl from proxyed list: " + wl2);
                this.mProxyedWakeLocks.remove(i2);
                unlinkToDeath(wl2);
            }
        }
    }

    private void dropWakeLockLocked(int pid, int uid) {
        Log.i(TAG, "drop wl, pid: " + pid + " uid: " + uid);
        forceReleaseWakeLockByPidUidLocked(pid, uid, pid < 0);
        dropProxyedWakeLockLocked(pid, uid);
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
        synchronized (this.mLock) {
            forceReleaseWakeLockByPidUidLocked(pid, uid, true);
        }
    }

    private void forceReleaseWakeLockByPidUidLocked(int pid, int uid, boolean isReleaseWorkSource) {
        for (int i = this.mWakeLocks.size() - 1; i >= 0; i--) {
            PowerManagerService.WakeLock wakelock = (PowerManagerService.WakeLock) this.mWakeLocks.get(i);
            if (wakelock.mWorkSource == null) {
                forceReleaseWakeLockWithoutWorkSourceIfMatchPidUidLocked(wakelock, pid, uid);
            } else if (isReleaseWorkSource) {
                forceReleaseWakeLockWithWorkSourceIfMatchPidUidLocked(wakelock, pid, uid);
            }
        }
    }

    private void forceReleaseWakeLockWithWorkSourceIfMatchPidUidLocked(PowerManagerService.WakeLock wakelock, int pid, int uid) {
        int j;
        int length;
        int length2;
        String str;
        WorkSource workSource;
        int i = uid;
        int length3 = wakelock.mWorkSource.size();
        int i2 = 0;
        String str2 = TAG;
        if (length3 == 1) {
            if (wakelock.mWorkSource.get(0) == i) {
                if (DEBUG) {
                    Log.i(str2, "forceReleaseWakeLockByPidUid, last one, wakelock: " + wakelock);
                }
                this.mForceReleasedWakeLocks.add(wakelock);
                releaseWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags);
            }
        } else if (length3 > 1) {
            int j2 = 0;
            while (j2 < length3) {
                if (wakelock.mWorkSource.get(j2) != i) {
                    j = j2;
                    str = str2;
                    length = length3;
                    length2 = i2;
                } else {
                    if (DEBUG) {
                        Log.i(str2, "forceReleaseWakeLockByPidUid, more than one, wakelock: " + wakelock);
                    }
                    String name = wakelock.mWorkSource.getName(j2);
                    if (name == null) {
                        workSource = new WorkSource(i);
                    } else {
                        workSource = new WorkSource(i, name);
                    }
                    j = j2;
                    length = length3;
                    length2 = 0;
                    PowerManagerService.WakeLock cacheWakelock = new PowerManagerService.WakeLock(this, wakelock.mLock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, workSource, wakelock.mHistoryTag, wakelock.mOwnerUid, wakelock.mOwnerPid, new PowerManagerService.UidState(i));
                    try {
                        wakelock.mLock.linkToDeath(cacheWakelock, 0);
                        this.mForceReleasedWakeLocks.add(cacheWakelock);
                        WorkSource newWorkSource = new WorkSource(wakelock.mWorkSource);
                        newWorkSource.remove(workSource);
                        notifyWakeLockChangingLocked(wakelock, wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mOwnerUid, wakelock.mOwnerPid, newWorkSource, wakelock.mHistoryTag);
                        wakelock.mWorkSource.remove(workSource);
                        str = str2;
                    } catch (RemoteException ex) {
                        str = str2;
                        Log.i(str, "forceReleaseWakeLockByPidUid, linkToDeath exception: " + ex);
                    }
                }
                j2 = j + 1;
                i = uid;
                str2 = str;
                i2 = length2;
                length3 = length;
            }
        } else {
            Log.e(str2, "forceReleaseWakeLockByPidUid, length invalid: " + length3);
        }
    }

    private void forceReleaseWakeLockWithoutWorkSourceIfMatchPidUidLocked(PowerManagerService.WakeLock wakelock, int pid, int uid) {
        if ((wakelock.mOwnerPid == pid || pid == -1) && wakelock.mOwnerUid == uid) {
            if (DEBUG) {
                Log.i(TAG, "forceReleaseWakeLockByPidUid, ws null, pid: " + pid + ", uid: " + uid + " , wakelock: " + wakelock);
            }
            this.mForceReleasedWakeLocks.add(wakelock);
            releaseWakeLockInternalLocked(wakelock.mLock, wakelock.mFlags);
        }
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
        boolean isMatchAndRestored;
        synchronized (this.mLock) {
            for (int i = this.mForceReleasedWakeLocks.size() - 1; i >= 0; i--) {
                PowerManagerService.WakeLock wakelock = this.mForceReleasedWakeLocks.get(i);
                if (wakelock.mWorkSource == null) {
                    isMatchAndRestored = forceRestoreWakeLockWithoutWorkSourceIfMatchPidUidLocked(wakelock, pid, uid);
                } else {
                    isMatchAndRestored = forceRestoreWakeLockWithWorkSourceIfMatchPidUidLocked(wakelock, pid, uid);
                }
                if (isMatchAndRestored) {
                    this.mForceReleasedWakeLocks.remove(i);
                }
            }
        }
    }

    private boolean forceRestoreWakeLockWithoutWorkSourceIfMatchPidUidLocked(PowerManagerService.WakeLock wakelock, int pid, int uid) {
        if (wakelock.mOwnerPid != pid && pid != -1) {
            return false;
        }
        if (wakelock.mOwnerUid != uid && uid != -1) {
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "forceRestoreWakeLockByPidUid, WorkSource == null, wakelock:  " + wakelock);
        }
        acquireWakeLockInternalLocked(wakelock);
        return true;
    }

    private boolean forceRestoreWakeLockWithWorkSourceIfMatchPidUidLocked(PowerManagerService.WakeLock wakelock, int pid, int uid) {
        if (uid != -1 && getWorkSourceUid(wakelock.mWorkSource) != uid) {
            return false;
        }
        int index = findWakeLockIndexLocked(wakelock.mLock);
        if (index < 0) {
            if (DEBUG) {
                Log.i(TAG, "forceRestoreWakeLockByPidUid, not found base, wakelock: " + wakelock);
            }
            acquireWakeLockInternalLocked(wakelock);
            return true;
        }
        if (DEBUG) {
            Log.i(TAG, "forceRestoreWakeLockByPidUid, update exist, wakelock: " + wakelock);
        }
        WorkSource newWorkSource = new WorkSource(((PowerManagerService.WakeLock) this.mWakeLocks.get(index)).mWorkSource);
        newWorkSource.add(wakelock.mWorkSource);
        notifyWakeLockChangingLocked((PowerManagerService.WakeLock) this.mWakeLocks.get(index), wakelock.mFlags, wakelock.mTag, wakelock.mPackageName, wakelock.mOwnerUid, wakelock.mOwnerPid, newWorkSource, wakelock.mHistoryTag);
        ((PowerManagerService.WakeLock) this.mWakeLocks.get(index)).mWorkSource.add(wakelock.mWorkSource);
        unlinkToDeath(wakelock);
        return true;
    }

    public boolean isAppWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
        if (this.mPGManagerInternal == null) {
            this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        }
        PGManagerInternal pGManagerInternal = this.mPGManagerInternal;
        if (pGManagerInternal != null) {
            return pGManagerInternal.isGmsWakeLockFilterTag(flags, packageName, ws);
        }
        return false;
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
            for (ProxyWakeLockProcessInfo pi : this.mProxyWakeLockProcessList) {
                pw.println(" ProxyWLProcess pid:" + pi.getPid() + ", uid: " + pi.getUid() + ", proxyWs: " + pi.isProxyWorkSource());
            }
        }
    }

    private boolean isWakeLockWithWorkSourceMatchUidAndFlag(PowerManagerService.WakeLock wl, int uid, int flag) {
        int size = wl.mWorkSource.size();
        for (int i = 0; i < size; i++) {
            if (uid == wl.mWorkSource.get(i) && (flag == -1 || (wl.mFlags & AwarenessConstants.BROADCAST_ACTION_MAX) == flag)) {
                Log.i(TAG, "worksource not null, i: " + i + ", size: " + size + ", flags: " + wl.mFlags);
                return true;
            }
        }
        return false;
    }

    private boolean isWakeLockMatchUidAndFlag(PowerManagerService.WakeLock wl, int uid, int flag) {
        if (wl.mOwnerUid == uid) {
            if (flag == -1 || (wl.mFlags & AwarenessConstants.BROADCAST_ACTION_MAX) == flag) {
                return true;
            }
            return false;
        } else if (wl.mWorkSource == null || !isWakeLockWithWorkSourceMatchUidAndFlag(wl, uid, flag)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean getWakeLockByUid(int uid, int flag) {
        synchronized (this.mLock) {
            if (this.mWakeLocks.size() <= 0) {
                return false;
            }
            Iterator<PowerManagerService.WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                if (isWakeLockMatchUidAndFlag(it.next(), uid, flag)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void sendTempBrightnessToMonitor(String paramType, int brightness) {
        if (this.mDisplayEffectMonitor != null) {
            String[] packageNames = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid()).split(AwarenessInnerConstants.COLON_KEY);
            if (packageNames.length != 0) {
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, paramType);
                params.put("brightness", Integer.valueOf(brightness));
                params.put(AppActConstant.ATTR_PACKAGE_NAME, packageNames[0]);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendBrightnessModeToMonitor(boolean isManualMode) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "brightnessMode");
            params.put("brightnessMode", isManualMode ? "MANUAL" : "AUTO");
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void sendManualBrightnessToMonitor(int brightness) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "manualBrightness");
            params.put("brightness", Integer.valueOf(brightness));
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
        HwPartIawareUtil.notifyWakeupResult(isWakenupThisTime);
    }

    /* access modifiers changed from: protected */
    public void stopPickupTrunOff() {
        HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.PICK_UP_WAKE_SCREEN_PART_FACTORY_IMPL).getPickUpWakeScreenManager().stopTurnOffController();
    }

    /* access modifiers changed from: protected */
    public void stopWakeLockedSensor(boolean isTrunOffScreen) {
        TurnOnWakeScreenManager turnOnWakeScreenManager = TurnOnWakeScreenManager.getInstance(this.mContext);
        if (turnOnWakeScreenManager != null && turnOnWakeScreenManager.isTurnOnSensorSupport()) {
            turnOnWakeScreenManager.turnOffAllSensor();
            if (isTrunOffScreen) {
                turnOnWakeScreenManager.turnOffScreen();
            }
        }
    }

    private void enableBrightnessWaitLocked() {
        if (!this.mBrightnessWaitModeEnabled) {
            this.mBrightnessWaitModeEnabled = true;
            this.mBrightnessWaitRet = false;
            this.mDirty |= 16384;
            Message msg = this.mHandler.obtainMessage(101);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, 3000);
        }
        this.mAuthSucceeded = false;
    }

    /* access modifiers changed from: protected */
    public void disableBrightnessWaitLocked(boolean isBright) {
        if (this.mBrightnessWaitModeEnabled) {
            this.mHandler.removeMessages(101);
            this.mBrightnessWaitModeEnabled = false;
            this.mBrightnessWaitRet = isBright;
            this.mDirty |= 16384;
        }
        this.mAuthSucceeded = false;
    }

    /* access modifiers changed from: protected */
    public void setAuthSucceededInternal() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.i(TAG, "setAuthSucceededInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled);
            }
            this.mAuthSucceeded = true;
        }
    }

    /* access modifiers changed from: protected */
    public void startWakeUpReadyInternal(long eventTime, int uid, String opPackageName) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.i(TAG, "UL_Power startWakeUpReadyInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled);
            }
            if (this.mBrightnessWaitModeEnabled) {
                resetWaitBrightTimeoutLocked();
            } else if (wakeUpNoUpdateWithoutInteractiveLocked(eventTime, "startWakeUpReady", uid, opPackageName)) {
                setWakeUpReasonByPackageName(opPackageName);
                enableBrightnessWaitLocked();
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetWaitBrightTimeoutLocked() {
        this.mHandler.removeMessages(101);
        Message msg = this.mHandler.obtainMessage(101);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, 3000);
    }

    /* access modifiers changed from: protected */
    public void stopWakeUpReadyInternal(long eventTime, int uid, boolean isBright, String opPackageName) {
        synchronized (this.mLock) {
            try {
                if (DEBUG) {
                    Slog.i(TAG, "UL_Power stopWakeUpReadyInternal, mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled + " isBright = " + isBright);
                }
                if (this.mBrightnessWaitModeEnabled) {
                    if (isBright) {
                        this.mLastWakeTime = eventTime;
                        setWakefulnessLocked(1, 0, SystemClock.uptimeMillis());
                        userActivityNoUpdateLocked(eventTime, 0, 0, uid);
                        disableBrightnessWaitLocked(true);
                        updatePowerStateLocked();
                    } else {
                        goToSleepNoUpdateLocked(eventTime, 0, 0, uid);
                        updatePowerStateLocked();
                    }
                } else if (isBright) {
                    if (DEBUG) {
                        Slog.i(TAG, "UL_Power stopWakeUpReadyInternal, brightness wait timeout.");
                    }
                    if (wakeUpNoUpdateLocked(eventTime, 2, "BrightnessWaitTimeout", uid, opPackageName, uid)) {
                        updatePowerStateLocked();
                    }
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private void setWakeUpReasonByPackageName(String opPackageName) {
        Slog.i(TAG, "Finger or PickUp wake up screen opPackageName: " + opPackageName);
        if (opPackageName != null) {
            char c = 65535;
            int hashCode = opPackageName.hashCode();
            if (hashCode != -861391249) {
                if (hashCode == 1698344559 && opPackageName.equals("com.android.systemui")) {
                    c = 0;
                }
            } else if (opPackageName.equals("android")) {
                c = 1;
            }
            if (c == 0) {
                this.mDisplayPowerRequest.mScreenChangeReason = 65637;
                this.mLastWakeReason = 101;
            } else if (c == 1) {
                this.mDisplayPowerRequest.mScreenChangeReason = 65638;
                this.mLastWakeReason = 102;
            }
        }
    }

    private boolean wakeUpNoUpdateWithoutInteractiveLocked(long eventTime, String reason, int uid, String opPackageName) {
        if (DEBUG) {
            Slog.i(TAG, "UL_Power wakeUpNoUpdateWithoutInteractiveLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastSleepTime || ((this.mWakefulness == 1 && !HwPCUtils.isInWindowsCastMode() && !HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) || !this.mBootCompleted || !this.mSystemReady || this.mProximityPositive)) {
            notifyWakeupResult(false);
            return false;
        }
        if (mSupportFaceDetect) {
            startIntelliService();
        }
        Trace.traceBegin(131072, "wakeUpWithoutInteractive");
        try {
            int i = this.mWakefulness;
            if (i == 0) {
                Slog.i(TAG, "UL_Power Waking up from sleep (uid " + uid + ")...");
            } else if (i == 2) {
                Slog.i(TAG, "UL_Power Waking up from dream (uid " + uid + ")...");
            } else if (i == 3) {
                Slog.i(TAG, "UL_Power Waking up from dozing (uid " + uid + ")... ");
            }
            this.mLastWakeTime = eventTime;
            this.mDirty |= 2;
            this.mWakefulness = 1;
            this.mNotifier.onWakeUp(2, reason, uid, opPackageName, uid);
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
                Slog.i(TAG, "UL_Power handleWaitBrightTimeout mBrightnessWaitModeEnabled = " + this.mBrightnessWaitModeEnabled + " mWakefulness = " + this.mWakefulness);
            }
            if (this.mBrightnessWaitModeEnabled) {
                if (!goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 101, 0, 1000)) {
                    disableBrightnessWaitLocked(false);
                }
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startIntelliService() {
        if (DEBUG) {
            Slog.i(TAG, "bind IntelliService, mIsIntelliServiceBound=" + this.mIsIntelliServiceBound);
        }
        if (!this.mIsIntelliServiceBound) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.power.HwPowerManagerService.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        HwPowerManagerService.this.mIsIntelliServiceBound = true;
                        boolean isBind = HwPowerManagerService.this.mContext.bindServiceAsUser(HwPowerManagerService.this.mIntelliIntent, HwPowerManagerService.this.connection, 1, UserHandle.CURRENT);
                        if (PowerManagerService.DEBUG) {
                            Slog.i(HwPowerManagerService.TAG, "bind IntelliService, success=" + isBind);
                        }
                        if (!isBind) {
                            HwPowerManagerService.this.resetFaceDetect();
                        }
                    } catch (SecurityException e) {
                        HwPowerManagerService.this.resetFaceDetect();
                        Slog.e(HwPowerManagerService.TAG, "unable to start intelli service: " + HwPowerManagerService.this.mIntelliIntent, e);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void stopIntelliService() {
        if (DEBUG) {
            Slog.i(TAG, "unbind IntelliService, mIsIntelliServiceBound=" + this.mIsIntelliServiceBound);
        }
        if (this.mIsIntelliServiceBound) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.power.HwPowerManagerService.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        HwPowerManagerService.this.mContext.unbindService(HwPowerManagerService.this.connection);
                    } catch (IllegalArgumentException e) {
                        Log.e(HwPowerManagerService.TAG, "unbindService service IllegalArgumentException");
                    }
                }
            });
            resetFaceDetect();
        }
    }

    /* access modifiers changed from: protected */
    public int registerFaceDetect() {
        if (!this.mIsIntelliServiceBound) {
            Slog.i(TAG, "IntelliService not started, face detect later");
            this.mShouldFaceDetectLater = true;
            startIntelliService();
            return -1;
        }
        IIntelliService iIntelliService = this.mIintelliService;
        if (iIntelliService == null || this.mIsFaceDetecting) {
            Slog.e(TAG, "register err, mIsFaceDetecting=" + this.mIsFaceDetecting);
            return -1;
        }
        try {
            int result = iIntelliService.registListener(3, this.mIntelliListener);
            if (DEBUG) {
                Slog.i(TAG, "registListener, result=" + result);
            }
            if (result != -1) {
                this.mIsFaceDetecting = true;
            }
            return result;
        } catch (RemoteException e) {
            Slog.e(TAG, "registListener exption");
            return -1;
        } catch (Exception e2) {
            Slog.e(TAG, "registListener exption");
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public int unregisterFaceDetect() {
        if (!this.mIsFaceDetecting) {
            return -1;
        }
        IIntelliService iIntelliService = this.mIintelliService;
        if (iIntelliService == null) {
            Slog.e(TAG, "unregister err mIintelliService is null!!");
            return -1;
        }
        try {
            int result = iIntelliService.unregistListener(3, this.mIntelliListener);
            if (DEBUG) {
                Slog.i(TAG, "unregistListener, result=" + result);
            }
            if (result != -1) {
                this.mIsFaceDetecting = false;
            }
            return result;
        } catch (RemoteException e) {
            Slog.e(TAG, "unregistListener exption");
            return -1;
        } catch (Exception e2) {
            Slog.e(TAG, "unregistListener exption");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetFaceDetect() {
        this.mIsIntelliServiceBound = false;
        this.mIsFaceDetecting = false;
        this.mShouldFaceDetectLater = false;
        this.mIintelliService = null;
    }

    /* access modifiers changed from: protected */
    public boolean needFaceDetectLocked(long nextTimeout, long now, boolean isStartWithoutChangeLights) {
        if (!mSupportFaceDetect) {
            return false;
        }
        boolean isActLightValid = (this.mUserActivitySummary == 1 && !((this.mLastUserActivityTimeNoChangeLights > this.mLastWakeTime ? 1 : (this.mLastUserActivityTimeNoChangeLights == this.mLastWakeTime ? 0 : -1)) >= 0 && (this.mLastUserActivityTimeNoChangeLights > this.mLastUserActivityTime ? 1 : (this.mLastUserActivityTimeNoChangeLights == this.mLastUserActivityTime ? 0 : -1)) > 0)) || isStartWithoutChangeLights;
        boolean isStayScreenOn = (this.mWakeLockSummary & 32) == 0 && !this.mStayOn;
        if (!isActLightValid || nextTimeout - now < 1000 || isKeyguardLocked() || !isStayScreenOn) {
            return false;
        }
        return true;
    }

    private boolean isKeyguardLocked() {
        WindowManagerPolicy windowManagerPolicy = this.mPolicy;
        return windowManagerPolicy != null && windowManagerPolicy.isKeyguardLocked();
    }

    private boolean dropLogs(String tag, int uid) {
        if (!"RILJ".equals(tag) || uid != 1001) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setMirrorLinkForVdrive() {
        if (this.isVdriveBackLightMode) {
            this.mBackLight.setMirrorLinkBrightness(255);
            this.isVdriveBackLightMode = false;
            this.mBackLight.setMirrorLinkBrightnessStatus(false);
            InputManagerServiceEx.DefaultHwInputManagerLocalService defaultHwInputManagerLocalService = this.mHwInputManagerInternal;
            if (defaultHwInputManagerLocalService != null) {
                defaultHwInputManagerLocalService.setMirrorLinkInputStatus(false);
            }
        } else {
            this.mBackLight.setMirrorLinkBrightness(0);
            this.isVdriveBackLightMode = true;
            this.mBackLight.setMirrorLinkBrightnessStatus(true);
            InputManagerServiceEx.DefaultHwInputManagerLocalService defaultHwInputManagerLocalService2 = this.mHwInputManagerInternal;
            if (defaultHwInputManagerLocalService2 != null) {
                defaultHwInputManagerLocalService2.setMirrorLinkInputStatus(true);
            }
        }
        Slog.i(TAG, "VCar mode goToSleepInternal inVdriveBackLightMode=" + this.isVdriveBackLightMode);
    }

    /* access modifiers changed from: protected */
    public boolean isVdriveHeldWakeLock() {
        if ((this.mWakeLockSummary & 2) == 0) {
            return false;
        }
        Iterator it = this.mWakeLocks.iterator();
        while (it.hasNext()) {
            PowerManagerService.WakeLock wl = (PowerManagerService.WakeLock) it.next();
            if (CAR_PACKAGENAME.equals(wl.mPackageName) && (wl.mFlags & AwarenessConstants.BROADCAST_ACTION_MAX) == 10) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setMirrorLinkPowerStatusInternal(boolean isStatus) {
        this.mBackLight.setMirrorLinkBrightnessStatus(false);
        InputManagerServiceEx.DefaultHwInputManagerLocalService defaultHwInputManagerLocalService = this.mHwInputManagerInternal;
        if (defaultHwInputManagerLocalService != null) {
            defaultHwInputManagerLocalService.setMirrorLinkInputStatus(false);
        }
        Slog.d(TAG, "setMirrorLinkPowerStatus status" + isStatus);
    }

    /* access modifiers changed from: protected */
    public void bedTimeLog(boolean isKeepAwake, boolean isStayOn, boolean isScreenBrightnessBoostInProgress) {
        if (this.mScreenTimeoutFlag && isKeepAwake) {
            Slog.i(TAG, "UL_Power Screen timeout occured. mStayOn = " + isStayOn + ", mProximityPositive = " + this.mProximityPositive + ", mWakeLockSummary = 0x" + Integer.toHexString(this.mWakeLockSummary) + ", mUserActivitySummary = 0x" + Integer.toHexString(this.mUserActivitySummary) + ", mScreenBrightnessBoostInProgress = " + isScreenBrightnessBoostInProgress);
            if ((this.mWakeLockSummary & 32) != 0) {
                Slog.i(TAG, "Wake Locks: size = " + this.mWakeLocks.size());
                Iterator it = this.mWakeLocks.iterator();
                while (it.hasNext()) {
                    Slog.i(TAG, "WakeLock:" + ((PowerManagerService.WakeLock) it.next()).toString());
                }
            }
        }
        if (mSupportFaceDetect && this.mScreenTimeoutFlag) {
            if ((this.mWakeLockSummary & 32) != 0 || isStayOn) {
                unregisterFaceDetect();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isPhoneHeldWakeLock() {
        if ((this.mWakeLockSummary & 16) == 0) {
            return false;
        }
        Iterator it = this.mWakeLocks.iterator();
        while (it.hasNext()) {
            PowerManagerService.WakeLock wl = (PowerManagerService.WakeLock) it.next();
            if ((INCALLUI_PACKAGENAME.equals(wl.mPackageName) || MEETIME_PACKAGENAME.equals(wl.mPackageName)) && (wl.mFlags & AwarenessConstants.BROADCAST_ACTION_MAX) == 32) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateEyePoetectColorTemperature(int lastScreenState, int newScreenState) {
        int i = this.mEyesProtectionMode;
        boolean isEyeprotectionMode = true;
        if (!(i == 1 || i == 3)) {
            isEyeprotectionMode = false;
        }
        if (newScreenState == 3 && lastScreenState == 0 && !isEyeprotectionMode) {
            setColorTemperatureAccordingToSetting();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unlinkToDeath(PowerManagerService.WakeLock wl) {
        if (wl != null && wl.mLock != null) {
            try {
                wl.mLock.unlinkToDeath(wl, 0);
            } catch (NoSuchElementException e) {
                Log.i(TAG, "unlinkToDeath, no such Element, wl: " + wl);
            }
        }
    }
}
