package com.android.server.policy;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.cover.CoverManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.policy.EasyWakeUpView.EasyWakeUpCallback;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.android.app.IEasyWakeUpManager.Stub;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class EasyWakeUpManager extends Stub implements EasyWakeUpCallback, SensorEventListener {
    private static final String[] APP_START_LATTER = new String[]{NAME_APP_BROWSER, NAME_APP_FLASHLIGHT};
    private static final String CAMERA_PACKAGE_NAME = "com.huawei.camera";
    private static final String CONSTANTS_USER = "root";
    private static int COVER_SCREEN_KEYCODE = -1;
    public static final boolean DEBUG = false;
    public static final boolean DEBUG_POWER = false;
    private static int DOUBLE_TOUCH_KEYCODE = -1;
    private static final int D_INT = 4;
    private static final String EASYWAKEUP = "easywakeup";
    private static final String EASYWAKEUP_SHOWNAVIBAR_ACTION = "com.huawei.android.easywakeup.SHOWNAVIBAR";
    private static String EASYWAKE_DATA_PATH = "/sys/devices/platform/huawei_touch/touch_gesture_wakeup_position";
    private static final String EASYWAKE_ENABLE_FLAG = "persist.sys.easyflag";
    private static final String EASYWAKE_ENABLE_SURPPORT_FLAG = "persist.sys.surpport.easyflag";
    private static int FLICK_DOWN_KEYCODE = -1;
    private static int FLICK_LEFT_KEYCODE = -1;
    private static int FLICK_RIGHT_KEYCODE = -1;
    private static int FLICK_UP_KEYCODE = -1;
    private static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA").addFlags(268435456).addFlags(536870912).addFlags(67108864).setPackage("com.huawei.camera");
    private static final String KEY_EASYWAKE_GESTURE = "com.huawei.easywakeup.gesture";
    private static final String KEY_EASYWAKE_POSITION = "com.huawei.easywakeup.position";
    private static final String[] KEY_WAKEUP = new String[]{"persist.sys.easywakeup.up", "persist.sys.easywakeup.down", "persist.sys.easywakeup.left", "persist.sys.easywakeup.right", "persist.sys.easywakeup.o", "persist.sys.easywakeup.c", "persist.sys.easywakeup.e", "persist.sys.easywakeup.m", "persist.sys.easywakeup.w", "persist.sys.easywakeup.v", "persist.sys.easywakeup.s"};
    private static int LETTER_C_KEYCODE = -1;
    private static int LETTER_E_KEYCODE = -1;
    private static int LETTER_M_KEYCODE = -1;
    private static int LETTER_W_KEYCODE = -1;
    static final int MAX_ANIMATETIME = 10000;
    private static int MAX_KEYCODE = -1;
    private static int MAX_TIMES_CHECK_KEYGUARD = 10;
    private static int MIN_KEYCODE = -1;
    private static final String NAME_APP_BROWSER = "com.android.browser;com.android.browser.BrowserActivity";
    private static final String NAME_APP_CAMERA = "com.huawei.camera;com.huawei.camera";
    private static final String NAME_APP_FLASHLIGHT = "com.android.systemui;com.android.systemui.flashlight.FlashlightActivity";
    private static final Intent SECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608).addFlags(268435456).addFlags(536870912).addFlags(67108864).setPackage("com.huawei.camera");
    private static int SENSOR_CHECK_TIMES = 6;
    private static final int SENSOR_DELAY_SECOND = 1000000;
    private static float SENSOR_FAR = 5.0f;
    private static float SENSOR_NEAR = 0.0f;
    private static long SENSOR_WATCH_TIME = 50;
    private static final String STARTFLG = "startflg";
    private static String TAG = "EasyWakeUpManager";
    private static final int TP_ENABLE = 1;
    private static final int TP_HAL_DEATH_COOKIE = 1001;
    private static final int TP_STOP = 0;
    private static String WAKEUP_GESTURE_STATUS_PATH = "/sys/devices/platform/huawei_touch/easy_wakeup_gesture";
    private static EasyWakeUpManager mEasywakeupmanager;
    private Runnable mAnimateRunable = new Runnable() {
        public void run() {
            EasyWakeUpManager.this.mIsAnimate = false;
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w(EasyWakeUpManager.TAG, "onReceive, the intent is null!");
                return;
            }
            if ("com.huawei.android.cover.STATE".equals(intent.getAction())) {
                EasyWakeUpManager.this.mCoverOpen = intent.getBooleanExtra("coverOpen", true);
                if (!EasyWakeUpManager.this.mCoverOpen) {
                    EasyWakeUpManager.this.turnOffSensorListener();
                }
            }
        }
    };
    private Context mContext;
    private CoverManager mCoverManager;
    private boolean mCoverOpen = true;
    private EasyWakeUpAnimationCallback mEasyWakeUpAnimationCallback;
    private EasyWakeUpView mEasyWakeUpView = null;
    private Handler mHandler;
    private boolean mIsAnimate = false;
    private KeyguardServiceDelegate mKeyguardDelegate;
    private final Object mLock = new Object();
    private PowerManager mPowerManager = null;
    private boolean mPowerOptimizeSwitchOn = false;
    private ITouchscreen mProxy = null;
    private SensorManager mSensorManager = null;
    private boolean mSensorUnRegisted = false;
    private float mSensorVaule = -1.0f;
    private boolean mSensorforHandleKey = false;
    private boolean mSensorforHandleTp = false;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private boolean mSuccessProcessEasyWakeUp = true;
    private int mTPWakeupGestureStatus = 0;
    private Vibrator mVibrator;
    private boolean mVibratorFirs = true;
    private boolean mViewActive = false;
    private int mWakeIndex = 0;
    private WindowManager mWindowManager = null;
    private LockPatternUtils mlockpatternutils;

    public interface EasyWakeUpAnimationCallback {
        void afterTrackAnimation();
    }

    final class DeathRecipient implements android.os.IHwBinder.DeathRecipient {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1001) {
                Log.e(EasyWakeUpManager.TAG, "tp hal service died cookie: " + cookie);
                synchronized (EasyWakeUpManager.this.mLock) {
                    EasyWakeUpManager.this.mProxy = null;
                }
            }
        }
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Log.e(EasyWakeUpManager.TAG, "tp hal service started " + fqName + " " + name);
            EasyWakeUpManager.this.connectToProxy();
        }
    }

    public static EasyWakeUpManager getInstance(Context context, Handler handler, KeyguardServiceDelegate mKeyguardDelegate) {
        EasyWakeUpManager easyWakeUpManager;
        synchronized (EasyWakeUpManager.class) {
            if (mEasywakeupmanager == null) {
                mEasywakeupmanager = new EasyWakeUpManager(context, handler, mKeyguardDelegate);
            }
            easyWakeUpManager = mEasywakeupmanager;
        }
        return easyWakeUpManager;
    }

    public EasyWakeUpManager(Context context, Handler handler, KeyguardServiceDelegate keyguardDelegate) {
        this.mContext = context;
        this.mKeyguardDelegate = keyguardDelegate;
        this.mHandler = handler;
        this.mlockpatternutils = new LockPatternUtils(context);
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mCoverManager = new CoverManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.android.cover.STATE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        try {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
            this.mWindowManager = (WindowManager) context.getSystemService("window");
            this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        } catch (Exception e) {
            Log.e(TAG, TAG + " Error when constructor ");
        }
        try {
            if (!IServiceManager.getService().registerForNotifications(ITouchscreen.kInterfaceName, "", this.mServiceNotification)) {
                Log.e(TAG, "Failed to register service start notification");
            }
            connectToProxy();
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to register service start notification", e2);
        }
    }

    private void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                Log.i(TAG, "mProxy has registered, donnot regitster again");
                return;
            }
            try {
                this.mProxy = ITouchscreen.getService();
                if (this.mProxy != null) {
                    this.mProxy.linkToDeath(new DeathRecipient(), 1001);
                }
            } catch (NoSuchElementException e) {
                Log.e(TAG, "connectToProxy: tp hal service not found. Did the service fail to start?", e);
            } catch (RemoteException e2) {
                Log.e(TAG, "connectToProxy: tp hal service not responding", e2);
            }
        }
    }

    private void setEasyWeakupGestureReportEnableHal(boolean enable) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.e(TAG, "mProxy is null, return");
                return;
            }
            try {
                boolean hwTsSetEasyWeakupGestureReportEnable = this.mProxy.hwTsSetEasyWeakupGestureReportEnable(enable);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set glove mode:", e);
            }
        }
    }

    private void setEasyWakeupGestureHal(int status) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.e(TAG, "mProxy is null, return");
                return;
            }
            try {
                boolean hwTsSetEasyWeakupGesture = this.mProxy.hwTsSetEasyWeakupGesture(status);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set glove mode:", e);
            }
        }
    }

    private int getWakeupIndex(int keyCode) {
        if (keyCode == FLICK_UP_KEYCODE) {
            return 0;
        }
        if (keyCode == FLICK_DOWN_KEYCODE) {
            return 1;
        }
        if (keyCode == FLICK_LEFT_KEYCODE) {
            return 2;
        }
        if (keyCode == FLICK_RIGHT_KEYCODE) {
            return 3;
        }
        if (keyCode == DOUBLE_TOUCH_KEYCODE) {
            return -1;
        }
        if (keyCode == LETTER_C_KEYCODE) {
            return 5;
        }
        if (keyCode == LETTER_E_KEYCODE) {
            return 6;
        }
        if (keyCode == LETTER_M_KEYCODE) {
            return 7;
        }
        if (keyCode == LETTER_W_KEYCODE) {
            return 8;
        }
        return -2;
    }

    private void processCoverScreenKeyCode(boolean hapticFeedback) {
        if (this.mPowerManager != null && this.mPowerManager.isScreenOn() && this.mVibratorFirs) {
            if (this.mVibrator != null && hapticFeedback) {
                this.mVibrator.vibrate(50);
            }
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (EasyWakeUpManager.this.mPowerManager.isScreenOn()) {
                        EasyWakeUpManager.this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
                    }
                    EasyWakeUpManager.this.mVibratorFirs = true;
                }
            }, 100);
            this.mVibratorFirs = false;
        }
    }

    public boolean processEasyWakeUp(int keyCode) {
        Log.v(TAG, TAG + " processEasyWakeUp and the keyCode from driver is : " + keyCode);
        boolean hapticFeedback = System.getInt(this.mContext.getContentResolver(), "haptic_feedback_enabled", 1) != 0;
        if (COVER_SCREEN_KEYCODE == keyCode) {
            processCoverScreenKeyCode(hapticFeedback);
            return false;
        }
        int wakeIndex = getWakeupIndex(keyCode);
        if (wakeIndex == -2) {
            return false;
        }
        if (this.mViewActive || (this.mPowerManager != null && this.mPowerManager.isScreenOn())) {
            Log.v(TAG, TAG + " processEasyWakeUp return false for mViewActive is : " + this.mViewActive);
            return false;
        }
        if (this.mPowerManager != null) {
            this.mPowerManager.newWakeLock(1, "").acquire(1000);
        }
        if (this.mVibrator != null && hapticFeedback) {
            this.mVibrator.vibrate(50);
        }
        this.mWakeIndex = wakeIndex;
        if (wakeIndex == -1) {
            Log.v(TAG, TAG + " processEasyWakeUp and wakeup for double click screen");
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
            return true;
        }
        unLockScreen(this.mlockpatternutils.isLockScreenDisabled(0), false);
        Log.v(TAG, TAG + " easywakeup  processEasyWakeUp and startTrackAnimation start");
        startTrackAnimation(wakeIndex);
        String startInfo = System.getString(this.mContext.getContentResolver(), KEY_WAKEUP[this.mWakeIndex]);
        if (!(startInfo == null || (checkAppNeedStart(startInfo) ^ 1) == 0)) {
            startActivity(startInfo);
        }
        return true;
    }

    private boolean checkAppNeedStart(String startInfo) {
        for (String equals : APP_START_LATTER) {
            if (equals.equals(startInfo)) {
                return true;
            }
        }
        return false;
    }

    public boolean setEasyWakeUpFlag(int flag) {
        this.mContext.enforceCallingPermission("android.permission.EASY_WAKE_UP", "set EasyWakeUp Flag");
        setGestureValue(EasyWakeUpXmlParse.getDriverGesturePath(), flag);
        return true;
    }

    private void unLockScreen(boolean keyguardable, boolean keyguardsecure) {
        if (!keyguardable) {
            Log.w(TAG, "EasyWakeUpManager dismiss keyguard");
            this.mKeyguardDelegate.dismiss(null);
        }
    }

    private Intent getCameraIntent() {
        return INSECURE_CAMERA_INTENT;
    }

    private boolean checkAppNeedStartCamera(String startInfo) {
        return startInfo.equals(NAME_APP_CAMERA);
    }

    private void startActivity(String startInfo) {
        String[] startInfos = startInfo.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        if (startInfos != null && startInfos.length == 2) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setClassName(startInfos[0], startInfos[1]);
            intent.putExtra(STARTFLG, EASYWAKEUP);
            intent.addFlags(805306368);
            try {
                if (checkAppNeedStartCamera(startInfo)) {
                    this.mContext.startActivity(getCameraIntent());
                } else {
                    this.mContext.startActivity(intent);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void startEasyWakeUpActivity() {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
            int times = 0;
            if (keyguardManager != null) {
                while (keyguardManager.inKeyguardRestrictedInputMode() && times < MAX_TIMES_CHECK_KEYGUARD) {
                    Thread.sleep(50);
                    times++;
                }
                if (keyguardManager.inKeyguardRestrictedInputMode()) {
                    this.mKeyguardDelegate.dismiss(null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, TAG + " Error on Thread sleep");
        }
        if (this.mWakeIndex >= 0) {
            String startInfo = System.getString(this.mContext.getContentResolver(), KEY_WAKEUP[this.mWakeIndex]);
            if (startInfo != null && checkAppNeedStart(startInfo)) {
                startActivity(startInfo);
            }
            this.mViewActive = false;
        }
    }

    private ArrayList<Point> getFlickTouchPointData(int wakeIndex) {
        ArrayList<Point> dataList = new ArrayList();
        int disX = this.mContext.getResources().getDisplayMetrics().widthPixels;
        int disY = this.mContext.getResources().getDisplayMetrics().heightPixels;
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            int tmp = disX;
            disX = disY;
            disY = tmp;
        }
        switch (wakeIndex) {
            case 0:
                dataList.add(new Point(disX / 2, disY));
                dataList.add(new Point(disX / 2, 0));
                break;
            case 1:
                dataList.add(new Point(disX / 2, 0));
                dataList.add(new Point(disX / 2, disY));
                break;
            case 2:
                dataList.add(new Point(disX, disY / 2));
                dataList.add(new Point(0, disY / 2));
                break;
            case 3:
                dataList.add(new Point(0, disY / 2));
                dataList.add(new Point(disX, disY / 2));
                break;
        }
        return dataList;
    }

    private boolean startTrackAnimation(final int wakeIndex) {
        this.mHandler.post(new Runnable() {
            public void run() {
                EasyWakeUpManager.this.mPowerManager.newWakeLock(1, "").acquire(MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                ArrayList<Point> pointList = EasyWakeUpManager.this.getTouchPointData(wakeIndex);
                if (!checkPointList(pointList, wakeIndex)) {
                    if (wakeIndex < 4) {
                        pointList = EasyWakeUpManager.this.getFlickTouchPointData(wakeIndex);
                        if (pointList == null || pointList.size() < 2) {
                            EasyWakeUpManager.this.mSuccessProcessEasyWakeUp = false;
                            return;
                        }
                    }
                    EasyWakeUpManager.this.mIsAnimate = false;
                    return;
                }
                LayoutParams lp = new LayoutParams(-1, -1);
                lp.type = 2010;
                lp.flags = 1280;
                lp.privateFlags |= Integer.MIN_VALUE;
                lp.format = -1;
                lp.setTitle("EasyWakeUp");
                lp.screenOrientation = 1;
                if (!(EasyWakeUpManager.this.mViewActive || EasyWakeUpManager.this.mWindowManager == null)) {
                    if (EasyWakeUpManager.this.mEasyWakeUpView == null) {
                        EasyWakeUpManager.this.mEasyWakeUpView = new EasyWakeUpView(EasyWakeUpManager.this.mContext, EasyWakeUpManager.this.mWakeIndex);
                        EasyWakeUpManager.this.mEasyWakeUpView.setEasyWakeUpCallback(EasyWakeUpManager.this);
                        if (EasyWakeUpManager.this.mPowerManager != null) {
                            EasyWakeUpManager.this.mEasyWakeUpView.setPowerManager(EasyWakeUpManager.this.mPowerManager);
                        }
                    }
                    try {
                        EasyWakeUpManager.this.mWindowManager.addView(EasyWakeUpManager.this.mEasyWakeUpView, lp);
                        EasyWakeUpManager.this.mViewActive = true;
                    } catch (Exception e) {
                        Log.e(EasyWakeUpManager.TAG, "windoe add err = " + e);
                        EasyWakeUpManager.this.mSuccessProcessEasyWakeUp = false;
                        EasyWakeUpManager.this.mIsAnimate = false;
                        return;
                    }
                }
                EasyWakeUpManager.this.mEasyWakeUpView.startTrackAnimation(pointList, wakeIndex);
            }

            private boolean checkPointList(ArrayList<Point> pointList, int wakeIndex) {
                if (pointList == null) {
                    return false;
                }
                boolean checkResult;
                if (wakeIndex >= 4 && pointList.size() == 6) {
                    checkResult = true;
                } else if (wakeIndex >= 4 || pointList.size() != 2) {
                    checkResult = false;
                } else {
                    checkResult = true;
                }
                return checkResult;
            }
        });
        return true;
    }

    private void sendBroadcast() {
        Log.d(TAG, "sendBroadcast showNaviBar");
        this.mContext.sendBroadcastAsUser(new Intent(EASYWAKEUP_SHOWNAVIBAR_ACTION), UserHandle.ALL, "com.huawei.easywakeup.permission.RECV_EASYWAKEUP_SHOWNAVIBAR");
    }

    public void disappearTrackAnimation() {
        this.mHandler.post(new Runnable() {
            public void run() {
                EasyWakeUpManager.this.removeEasyWakeUpView();
                if (EasyWakeUpManager.this.mEasyWakeUpAnimationCallback != null) {
                    EasyWakeUpManager.this.mEasyWakeUpAnimationCallback.afterTrackAnimation();
                }
                EasyWakeUpManager.this.startEasyWakeUpActivity();
                EasyWakeUpManager.this.sendBroadcast();
            }
        });
    }

    private void removeEasyWakeUpView() {
        if (this.mViewActive && this.mEasyWakeUpView != null && this.mWindowManager != null) {
            this.mWindowManager.removeView(this.mEasyWakeUpView);
            this.mEasyWakeUpView = null;
            this.mViewActive = false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ba A:{SYNTHETIC, Splitter: B:29:0x00ba} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0103 A:{SYNTHETIC, Splitter: B:37:0x0103} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<Point> getTouchPointData(int wakeIndex) {
        Exception e;
        Throwable th;
        if (wakeIndex < 0 || wakeIndex > 8) {
            return null;
        }
        int len = 2;
        if (wakeIndex >= 4) {
            len = 6;
        }
        RandomAccessFile indexFile = null;
        ArrayList<Point> dataList = new ArrayList();
        try {
            RandomAccessFile indexFile2 = new RandomAccessFile(EASYWAKE_DATA_PATH, "r");
            try {
                String data = indexFile2.readLine();
                int index = 0;
                for (int i = 0; i < len; i++) {
                    index += 4;
                    index += 4;
                    dataList.add(new Point(Integer.parseInt(data.substring(index, index + 4), 16), Integer.parseInt(data.substring(index, index + 4), 16)));
                }
                if (indexFile2 != null) {
                    try {
                        indexFile2.close();
                    } catch (FileNotFoundException e2) {
                        Log.d(TAG, TAG + " getTouchPointData  the file(NODE for driver) is not be found while close file");
                        e2.printStackTrace();
                    } catch (IOException e3) {
                        Log.d(TAG, TAG + " getTouchPointData Exception for IOException while close file");
                        e3.printStackTrace();
                    }
                }
                indexFile = indexFile2;
            } catch (Exception e4) {
                e = e4;
                indexFile = indexFile2;
                try {
                    Log.d(TAG, TAG + " getTouchPointData Exception for IOException while read file");
                    e.printStackTrace();
                    if (indexFile != null) {
                        try {
                            indexFile.close();
                        } catch (FileNotFoundException e22) {
                            Log.d(TAG, TAG + " getTouchPointData  the file(NODE for driver) is not be found while close file");
                            e22.printStackTrace();
                        } catch (IOException e32) {
                            Log.d(TAG, TAG + " getTouchPointData Exception for IOException while close file");
                            e32.printStackTrace();
                        }
                    }
                    return dataList;
                } catch (Throwable th2) {
                    th = th2;
                    if (indexFile != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                indexFile = indexFile2;
                if (indexFile != null) {
                    try {
                        indexFile.close();
                    } catch (FileNotFoundException e222) {
                        Log.d(TAG, TAG + " getTouchPointData  the file(NODE for driver) is not be found while close file");
                        e222.printStackTrace();
                    } catch (IOException e322) {
                        Log.d(TAG, TAG + " getTouchPointData Exception for IOException while close file");
                        e322.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            Log.d(TAG, TAG + " getTouchPointData Exception for IOException while read file");
            e.printStackTrace();
            if (indexFile != null) {
            }
            return dataList;
        }
        return dataList;
    }

    public void saveTouchPointNodePath() {
        Secure.putInt(this.mContext.getContentResolver(), EASYWAKE_ENABLE_SURPPORT_FLAG, EasyWakeUpXmlParse.getDefaultSupportValueFromCust());
        Secure.putString(this.mContext.getContentResolver(), KEY_EASYWAKE_POSITION, EasyWakeUpXmlParse.getDriverPostionPath());
        String gesturePath = EasyWakeUpXmlParse.getDriverGesturePath();
        Secure.putString(this.mContext.getContentResolver(), KEY_EASYWAKE_GESTURE, gesturePath);
        saveParsedItemsToDb();
        int flag = getFlagValue();
        if (flag != 0) {
            setGestureValue(gesturePath, setCemwValuesForDriver(flag));
        } else {
            setFlagValue(EasyWakeUpXmlParse.getDefaultValueFromCust());
            saveParsedItemsToDb();
        }
        readDBItems();
    }

    private int getIndexFrmoDB(String str) {
        int result = -1;
        try {
            return Secure.getInt(this.mContext.getContentResolver(), str);
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    private int setCemwValuesForDriver(int allValue) {
        int value = allValue;
        int allIndex = getIndexFrmoDB("EasyWakeUp_Letter_ALL_index");
        int cIndex = getIndexFrmoDB("EasyWakeUp_Letter_C_index");
        int eIndex = getIndexFrmoDB("EasyWakeUp_Letter_E_index");
        int mIndex = getIndexFrmoDB("EasyWakeUp_Letter_M_index");
        int wIndex = getIndexFrmoDB("EasyWakeUp_Letter_W_index");
        int flickAllIndex = getIndexFrmoDB("EasyWakeUp_Flick_ALL_index");
        int upIndex = getIndexFrmoDB("EasyWakeUp_Flick_Up_index");
        int downIndex = getIndexFrmoDB("EasyWakeUp_Flick_Down_index");
        int leftIndex = getIndexFrmoDB("EasyWakeUp_Flick_left_index");
        int rightIndex = getIndexFrmoDB("EasyWakeUp_Flick_Right_index");
        if (((1 << allIndex) & allValue) == 0) {
            value = (~((((1 << cIndex) | (1 << eIndex)) | (1 << mIndex)) | (1 << wIndex))) & allValue;
        }
        if (((1 << flickAllIndex) & allValue) != 0) {
            return value;
        }
        return value & (~((((1 << upIndex) | (1 << downIndex)) | (1 << leftIndex)) | (1 << rightIndex)));
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0039 A:{SYNTHETIC, Splitter: B:15:0x0039} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a A:{SYNTHETIC, Splitter: B:21:0x004a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setGestureValue(String path, int flag) {
        Exception e;
        Throwable th;
        FileOutputStream file = null;
        try {
            FileOutputStream file2 = new FileOutputStream(new File(path));
            try {
                file2.write(String.valueOf(flag).getBytes(Charset.forName("UTF-8")));
                if (file2 != null) {
                    try {
                        file2.close();
                    } catch (IOException e2) {
                        Log.i(TAG, "Closing outputstream: ", e2);
                    }
                }
                file = file2;
            } catch (Exception e3) {
                e = e3;
                file = file2;
                try {
                    Log.i(TAG, "set gesture to file: error", e);
                    if (file == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (file != null) {
                        try {
                            file.close();
                        } catch (IOException e22) {
                            Log.i(TAG, "Closing outputstream: ", e22);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                file = file2;
                if (file != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Log.i(TAG, "set gesture to file: error", e);
            if (file == null) {
                try {
                    file.close();
                } catch (IOException e222) {
                    Log.i(TAG, "Closing outputstream: ", e222);
                }
            }
        }
    }

    private void setFlagValue(int flag) {
        try {
            Secure.putInt(this.mContext.getContentResolver(), EASYWAKE_ENABLE_FLAG, flag);
        } catch (Exception e) {
            Log.i(TAG, "set flag in settings: error");
        }
    }

    private int getFlagValue() {
        try {
            return Secure.getInt(this.mContext.getContentResolver(), EASYWAKE_ENABLE_FLAG);
        } catch (Exception e) {
            Log.i(TAG, "get flag from settings: error");
            return 0;
        }
    }

    private boolean isCalling() {
        boolean z = false;
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null) {
            return false;
        }
        if (telephonyManager.getCallState() != 0) {
            z = true;
        }
        return z;
    }

    private boolean processSensorChange() {
        turnOffSensorListener();
        this.mSensorforHandleKey = true;
        this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(8), 0);
        this.mSensorUnRegisted = false;
        int i = 0;
        while (i < SENSOR_CHECK_TIMES) {
            try {
                Thread.sleep(SENSOR_WATCH_TIME);
                if (this.mSensorVaule != -1.0f) {
                    break;
                }
                i++;
            } catch (InterruptedException e) {
                Log.i(TAG, "thread sleep InterruptedException");
            }
        }
        this.mSensorManager.unregisterListener(this);
        this.mSensorforHandleKey = false;
        this.mSensorUnRegisted = true;
        if (this.mSensorVaule < SENSOR_FAR) {
            Log.e(TAG, "do nothing for easywakeup because of PROXIMITY is " + this.mSensorVaule);
            turnOnSensorListener();
            setEasyWeakupGestureReportEnableHal(true);
            this.mSensorVaule = -1.0f;
            return true;
        }
        this.mSensorVaule = -1.0f;
        return false;
    }

    private void handleWakeUpKeyInternal(int code) {
        if (this.mSensorManager == null || code == COVER_SCREEN_KEYCODE || !processSensorChange()) {
            if (code != DOUBLE_TOUCH_KEYCODE) {
                if (isCalling()) {
                    Log.e(TAG, " do nothing because Calling !");
                    setEasyWeakupGestureReportEnableHal(true);
                    return;
                } else if (code != COVER_SCREEN_KEYCODE) {
                    this.mIsAnimate = true;
                    this.mHandler.postDelayed(this.mAnimateRunable, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                }
            }
            this.mEasyWakeUpAnimationCallback = new EasyWakeUpAnimationCallback() {
                public void afterTrackAnimation() {
                    if (EasyWakeUpManager.this.mHandler.hasCallbacks(EasyWakeUpManager.this.mAnimateRunable)) {
                        EasyWakeUpManager.this.mHandler.removeCallbacks(EasyWakeUpManager.this.mAnimateRunable);
                    }
                    EasyWakeUpManager.this.mIsAnimate = false;
                }
            };
            this.mSuccessProcessEasyWakeUp = true;
            if (processEasyWakeUp(code) && !this.mSuccessProcessEasyWakeUp) {
                Log.i(TAG, " write flick node 1 to driver because of fail EasyWakeUp");
                setEasyWeakupGestureReportEnableHal(true);
            }
        }
    }

    public boolean handleWakeUpKey(KeyEvent event, int mScreenOffReason) {
        if (!this.mIsAnimate || this.mEasyWakeUpView == null) {
            int code = event.getKeyCode();
            if (event.getAction() != 0 || code < MIN_KEYCODE || code > MAX_KEYCODE) {
                return false;
            }
            if (code != COVER_SCREEN_KEYCODE && this.mPowerManager != null && this.mPowerManager.isScreenOn()) {
                return false;
            }
            if (mScreenOffReason == 6) {
                setEasyWeakupGestureReportEnableHal(true);
                Log.e(TAG, " Off screen beacuse sensor !");
                return true;
            }
            handleWakeUpKeyInternal(code);
            return true;
        }
        Log.e(TAG, "easywakeup is animate !");
        return true;
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent arg0) {
        float[] its = arg0.values;
        if (its != null && arg0.sensor.getType() == 8 && its.length > 0) {
            this.mSensorVaule = its[0];
        }
        if (this.mSensorforHandleTp && !this.mSensorforHandleKey) {
            int tmpWakeupEnable;
            if (this.mSensorVaule < SENSOR_FAR) {
                tmpWakeupEnable = 0;
            } else {
                tmpWakeupEnable = 1;
            }
            if (this.mTPWakeupGestureStatus != tmpWakeupEnable) {
                this.mTPWakeupGestureStatus = tmpWakeupEnable;
                setEasyWakeupGestureHal(this.mTPWakeupGestureStatus);
            }
        }
    }

    private void readDBItems() {
        boolean z = true;
        COVER_SCREEN_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "Cover_Screen_Keycode", -1);
        DOUBLE_TOUCH_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "Double_Touch_Keycode", -1);
        FLICK_UP_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Flick_Up_Keycode", -1);
        FLICK_DOWN_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Flick_Down_Keycode", -1);
        FLICK_LEFT_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Flick_left_Keycode", -1);
        FLICK_RIGHT_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Flick_Right_Keycode", -1);
        LETTER_C_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Letter_C_Keycode", -1);
        LETTER_E_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Letter_E_Keycode", -1);
        LETTER_M_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Letter_M_Keycode", -1);
        LETTER_W_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "EasyWakeUp_Letter_W_Keycode", -1);
        MAX_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "maxKeycode", -1);
        MIN_KEYCODE = Secure.getInt(this.mContext.getContentResolver(), "minKeyCode", -1);
        EASYWAKE_DATA_PATH = Secure.getString(this.mContext.getContentResolver(), KEY_EASYWAKE_POSITION);
        SENSOR_NEAR = Secure.getFloat(this.mContext.getContentResolver(), "sensor_near", 0.0f);
        SENSOR_FAR = Secure.getFloat(this.mContext.getContentResolver(), "sensor_far", 5.0f);
        SENSOR_WATCH_TIME = Secure.getLong(this.mContext.getContentResolver(), "sensor_watch_time", 50);
        SENSOR_CHECK_TIMES = Secure.getInt(this.mContext.getContentResolver(), "sensor_check_times", 6);
        if (Secure.getInt(this.mContext.getContentResolver(), "power_optimize", 0) != 1) {
            z = false;
        }
        this.mPowerOptimizeSwitchOn = z;
    }

    private void saveParsedItemsToDb() {
        setDBValue("Cover_Screen_index", EasyWakeUpXmlParse.getCoverScreenIndex());
        setDBValue("Double_Touch_index", EasyWakeUpXmlParse.getDoubleTouchIndex());
        setDBValue("EasyWakeUp_Flick_ALL_index", EasyWakeUpXmlParse.getFlickAllIndex());
        setDBValue("EasyWakeUp_Flick_Up_index", EasyWakeUpXmlParse.getFlickUpIndex());
        setDBValue("EasyWakeUp_Flick_Down_index", EasyWakeUpXmlParse.getFlickDownEIndex());
        setDBValue("EasyWakeUp_Flick_left_index", EasyWakeUpXmlParse.getFlickLeftIndex());
        setDBValue("EasyWakeUp_Flick_Right_index", EasyWakeUpXmlParse.getFlickRightIndex());
        setDBValue("EasyWakeUp_Letter_ALL_index", EasyWakeUpXmlParse.getLetterAllIndex());
        setDBValue("EasyWakeUp_Letter_C_index", EasyWakeUpXmlParse.getLetterCIndex());
        setDBValue("EasyWakeUp_Letter_E_index", EasyWakeUpXmlParse.getLetterEIndex());
        setDBValue("EasyWakeUp_Letter_M_index", EasyWakeUpXmlParse.getLetterMIndex());
        setDBValue("EasyWakeUp_Letter_W_index", EasyWakeUpXmlParse.getLetterWIndex());
        setDBValue("Cover_Screen_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.Cover_Screen));
        setDBValue("Double_Touch_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.Double_Touch));
        setDBValue("EasyWakeUp_Flick_Up_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Flick_UP));
        setDBValue("EasyWakeUp_Flick_Down_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Flick_DOWN));
        setDBValue("EasyWakeUp_Flick_left_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Flick_LEFT));
        setDBValue("EasyWakeUp_Flick_Right_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Flick_RIGHT));
        setDBValue("EasyWakeUp_Letter_C_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Letter_C));
        setDBValue("EasyWakeUp_Letter_E_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Letter_E));
        setDBValue("EasyWakeUp_Letter_M_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Letter_M));
        setDBValue("EasyWakeUp_Letter_W_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EasyWakeUp_Letter_W));
        setDBValue("maxKeycode", EasyWakeUpXmlParse.getKeyCodeByString("maxKeyCode"));
        setDBValue("minKeyCode", EasyWakeUpXmlParse.getKeyCodeByString("minKeyCode"));
        setDBValue("DriverFileLength", EasyWakeUpXmlParse.getDriverFileLength());
        Secure.putFloat(this.mContext.getContentResolver(), "sensor_near", EasyWakeUpXmlParse.getSensorNearValue());
        Secure.putFloat(this.mContext.getContentResolver(), "sensor_far", EasyWakeUpXmlParse.getSensorFarValue());
        Secure.putLong(this.mContext.getContentResolver(), "sensor_watch_time", EasyWakeUpXmlParse.getSensorWatchTime());
        setDBValue("sensor_check_times", EasyWakeUpXmlParse.getSensorCheckTimes());
        setDBValue("power_optimize", EasyWakeUpXmlParse.getPowerOptimizeState());
    }

    private void setDBValue(String str, int value) {
        try {
            Secure.putInt(this.mContext.getContentResolver(), str, value);
        } catch (Exception e) {
            Log.i(TAG, "set flag in settings: error str= " + str);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0070 A:{SYNTHETIC, Splitter: B:29:0x0070} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0061 A:{SYNTHETIC, Splitter: B:21:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x007c A:{SYNTHETIC, Splitter: B:35:0x007c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isEasyWakeupEnabledByKernel() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedReader reader = null;
        boolean result = false;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(WAKEUP_GESTURE_STATUS_PATH), Charset.defaultCharset()));
            try {
                String line = reader2.readLine();
                if (!(line == null || Integer.parseInt(line.trim().replaceAll("^0[x|X]", ""), 16) == 0)) {
                    result = true;
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e4) {
                e = e4;
                reader = reader2;
                e.printStackTrace();
                if (reader != null) {
                }
                Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
                return result;
            } catch (IOException e5) {
                e2 = e5;
                reader = reader2;
                try {
                    e2.printStackTrace();
                    if (reader != null) {
                    }
                    Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e = e6;
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e322) {
                    e322.printStackTrace();
                }
            }
            Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
            return result;
        } catch (IOException e7) {
            e2 = e7;
            e2.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e3222) {
                    e3222.printStackTrace();
                }
            }
            Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
            return result;
        }
        Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
        return result;
    }

    public void turnOnSensorListener() {
        if (this.mCoverManager != null) {
            this.mCoverOpen = this.mCoverManager.isCoverOpen();
        }
        boolean tpEnabledByKernel = isEasyWakeupEnabledByKernel();
        if (this.mCoverOpen && tpEnabledByKernel && this.mPowerOptimizeSwitchOn) {
            this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(8), SENSOR_DELAY_SECOND);
            this.mSensorforHandleTp = true;
        } else {
            this.mSensorforHandleTp = false;
        }
        this.mSensorUnRegisted = false;
        this.mTPWakeupGestureStatus = 0;
    }

    public void turnOffSensorListener() {
        if (!this.mSensorUnRegisted && (this.mSensorforHandleTp || this.mSensorforHandleKey)) {
            this.mSensorManager.unregisterListener(this);
        }
        this.mSensorforHandleTp = false;
        this.mSensorUnRegisted = true;
    }
}
