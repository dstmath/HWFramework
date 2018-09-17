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
import android.os.Handler;
import android.os.PowerManager;
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
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.hwutil.CommandLineUtil;
import huawei.android.app.IEasyWakeUpManager.Stub;
import huawei.com.android.server.policy.HwGlobalActionsData;
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

public class EasyWakeUpManager extends Stub implements EasyWakeUpCallback, SensorEventListener {
    private static final String[] APP_START_LATTER = null;
    private static final String CAMERA_PACKAGE_NAME = "com.huawei.camera";
    private static final String CONSTANTS_USER = "root";
    private static int COVER_SCREEN_KEYCODE = 0;
    public static final boolean DEBUG = false;
    public static final boolean DEBUG_POWER = false;
    private static int DOUBLE_TOUCH_KEYCODE = 0;
    private static final int D_INT = 4;
    private static final String EASYWAKEUP = "easywakeup";
    private static final String EASYWAKEUP_SHOWNAVIBAR_ACTION = "com.huawei.android.easywakeup.SHOWNAVIBAR";
    private static String EASYWAKE_DATA_PATH = null;
    private static final String EASYWAKE_ENABLE_FLAG = "persist.sys.easyflag";
    private static final String EASYWAKE_ENABLE_SURPPORT_FLAG = "persist.sys.surpport.easyflag";
    private static int FLICK_DOWN_KEYCODE = 0;
    private static int FLICK_LEFT_KEYCODE = 0;
    private static int FLICK_RIGHT_KEYCODE = 0;
    private static int FLICK_UP_KEYCODE = 0;
    private static final Intent INSECURE_CAMERA_INTENT = null;
    private static final String KEY_EASYWAKE_CONTROL = "com.huawei.easywakeup.control";
    private static final String KEY_EASYWAKE_GESTURE = "com.huawei.easywakeup.gesture";
    private static final String KEY_EASYWAKE_POSITION = "com.huawei.easywakeup.position";
    private static final String[] KEY_WAKEUP = null;
    private static int LETTER_C_KEYCODE = 0;
    private static int LETTER_E_KEYCODE = 0;
    private static int LETTER_M_KEYCODE = 0;
    private static int LETTER_W_KEYCODE = 0;
    static final int MAX_ANIMATETIME = 10000;
    private static int MAX_KEYCODE = 0;
    private static int MAX_TIMES_CHECK_KEYGUARD = 0;
    private static int MIN_KEYCODE = 0;
    private static final String NAME_APP_BROWSER = "com.android.browser;com.android.browser.BrowserActivity";
    private static final String NAME_APP_CAMERA = "com.huawei.camera;com.huawei.camera";
    private static final String NAME_APP_FLASHLIGHT = "com.android.systemui;com.android.systemui.flashlight.FlashlightActivity";
    private static String NODE_EASYWAKE_CONTROL = null;
    private static final Intent SECURE_CAMERA_INTENT = null;
    private static int SENSOR_CHECK_TIMES = 0;
    private static final int SENSOR_DELAY_SECOND = 1000000;
    private static float SENSOR_FAR = 0.0f;
    private static float SENSOR_NEAR = 0.0f;
    private static long SENSOR_WATCH_TIME = 0;
    private static final String STARTFLG = "startflg";
    private static String TAG = null;
    private static final String TP_ENABLE = "1";
    private static final String TP_STOP = "0";
    private static String WAKEUP_GESTURE_ENABLE_PATH;
    private static String WAKEUP_GESTURE_STATUS_PATH;
    private static EasyWakeUpManager mEasywakeupmanager;
    private Runnable mAnimateRunable;
    private final BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private CoverManager mCoverManager;
    private boolean mCoverOpen;
    private EasyWakeUpAnimationCallback mEasyWakeUpAnimationCallback;
    private EasyWakeUpView mEasyWakeUpView;
    private Handler mHandler;
    private boolean mIsAnimate;
    private KeyguardServiceDelegate mKeyguardDelegate;
    private PowerManager mPowerManager;
    private boolean mPowerOptimizeSwitchOn;
    private SensorManager mSensorManager;
    private boolean mSensorUnRegisted;
    private float mSensorVaule;
    private boolean mSensorforHandleKey;
    private boolean mSensorforHandleTp;
    private boolean mSuccessProcessEasyWakeUp;
    private String mTPWakeupGestureStatus;
    private Vibrator mVibrator;
    private boolean mVibratorFirs;
    private boolean mViewActive;
    private int mWakeIndex;
    private WindowManager mWindowManager;
    private LockPatternUtils mlockpatternutils;

    /* renamed from: com.android.server.policy.EasyWakeUpManager.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ int val$wakeIndex;

        AnonymousClass4(int val$wakeIndex) {
            this.val$wakeIndex = val$wakeIndex;
        }

        public void run() {
            EasyWakeUpManager.this.mPowerManager.newWakeLock(1, AppHibernateCst.INVALID_PKG).acquire(MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            ArrayList<Point> pointList = EasyWakeUpManager.this.getTouchPointData(this.val$wakeIndex);
            if (!checkPointList(pointList, this.val$wakeIndex)) {
                if (this.val$wakeIndex < EasyWakeUpManager.D_INT) {
                    pointList = EasyWakeUpManager.this.getFlickTouchPointData(this.val$wakeIndex);
                    if (pointList == null || pointList.size() < 2) {
                        EasyWakeUpManager.this.mSuccessProcessEasyWakeUp = EasyWakeUpManager.DEBUG_POWER;
                        return;
                    }
                }
                EasyWakeUpManager.this.mIsAnimate = EasyWakeUpManager.DEBUG_POWER;
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
                    EasyWakeUpManager.this.mSuccessProcessEasyWakeUp = EasyWakeUpManager.DEBUG_POWER;
                    EasyWakeUpManager.this.mIsAnimate = EasyWakeUpManager.DEBUG_POWER;
                    return;
                }
            }
            EasyWakeUpManager.this.mEasyWakeUpView.startTrackAnimation(pointList, this.val$wakeIndex);
        }

        private boolean checkPointList(ArrayList<Point> pointList, int wakeIndex) {
            if (pointList == null) {
                return EasyWakeUpManager.DEBUG_POWER;
            }
            boolean checkResult;
            if (wakeIndex >= EasyWakeUpManager.D_INT && pointList.size() == 6) {
                checkResult = true;
            } else if (wakeIndex >= EasyWakeUpManager.D_INT || pointList.size() != 2) {
                checkResult = EasyWakeUpManager.DEBUG_POWER;
            } else {
                checkResult = true;
            }
            return checkResult;
        }
    }

    public interface EasyWakeUpAnimationCallback {
        void afterTrackAnimation();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.EasyWakeUpManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.EasyWakeUpManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.EasyWakeUpManager.<clinit>():void");
    }

    private int setCemwValuesForDriver(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.EasyWakeUpManager.setCemwValuesForDriver(int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.EasyWakeUpManager.setCemwValuesForDriver(int):int");
    }

    public static EasyWakeUpManager getInstance(Context context, Handler handler, KeyguardServiceDelegate mKeyguardDelegate) {
        if (mEasywakeupmanager == null) {
            mEasywakeupmanager = new EasyWakeUpManager(context, handler, mKeyguardDelegate);
        }
        return mEasywakeupmanager;
    }

    public EasyWakeUpManager(Context context, Handler handler, KeyguardServiceDelegate keyguardDelegate) {
        this.mSensorManager = null;
        this.mWindowManager = null;
        this.mEasyWakeUpView = null;
        this.mPowerManager = null;
        this.mWakeIndex = 0;
        this.mViewActive = DEBUG_POWER;
        this.mVibratorFirs = true;
        this.mSuccessProcessEasyWakeUp = true;
        this.mIsAnimate = DEBUG_POWER;
        this.mSensorVaule = -1.0f;
        this.mAnimateRunable = new Runnable() {
            public void run() {
                EasyWakeUpManager.this.mIsAnimate = EasyWakeUpManager.DEBUG_POWER;
            }
        };
        this.mSensorforHandleTp = DEBUG_POWER;
        this.mSensorforHandleKey = DEBUG_POWER;
        this.mSensorUnRegisted = DEBUG_POWER;
        this.mTPWakeupGestureStatus = AppHibernateCst.INVALID_PKG;
        this.mCoverOpen = true;
        this.mPowerOptimizeSwitchOn = DEBUG_POWER;
        this.mBroadcastReceiver = new BroadcastReceiver() {
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
    }

    public boolean processEasyWakeUp(int keyCode) {
        int wakeIndex;
        Log.v(TAG, TAG + " processEasyWakeUp and the keyCode from driver is : " + keyCode);
        boolean hapticFeedback = System.getInt(this.mContext.getContentResolver(), "haptic_feedback_enabled", 1) != 0 ? true : DEBUG_POWER;
        if (FLICK_UP_KEYCODE == keyCode) {
            wakeIndex = 0;
        } else if (FLICK_DOWN_KEYCODE == keyCode) {
            wakeIndex = 1;
        } else if (FLICK_LEFT_KEYCODE == keyCode) {
            wakeIndex = 2;
        } else if (FLICK_RIGHT_KEYCODE == keyCode) {
            wakeIndex = 3;
        } else if (DOUBLE_TOUCH_KEYCODE == keyCode) {
            wakeIndex = -1;
        } else if (LETTER_C_KEYCODE == keyCode) {
            wakeIndex = 5;
        } else if (LETTER_E_KEYCODE == keyCode) {
            wakeIndex = 6;
        } else if (LETTER_M_KEYCODE == keyCode) {
            wakeIndex = 7;
        } else if (LETTER_W_KEYCODE == keyCode) {
            wakeIndex = 8;
        } else {
            if (COVER_SCREEN_KEYCODE == keyCode && this.mPowerManager != null && this.mPowerManager.isScreenOn() && this.mVibratorFirs) {
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
                this.mVibratorFirs = DEBUG_POWER;
            }
            return DEBUG_POWER;
        }
        if (this.mViewActive || (this.mPowerManager != null && this.mPowerManager.isScreenOn())) {
            Log.v(TAG, TAG + " processEasyWakeUp return false for mViewActive is : " + this.mViewActive);
            return DEBUG_POWER;
        }
        if (this.mPowerManager != null) {
            this.mPowerManager.newWakeLock(1, AppHibernateCst.INVALID_PKG).acquire(1000);
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
        unLockScreen(this.mlockpatternutils.isLockScreenDisabled(0), DEBUG_POWER);
        Log.v(TAG, TAG + " easywakeup  processEasyWakeUp and startTrackAnimation start");
        startTrackAnimation(wakeIndex);
        String startInfo = System.getString(this.mContext.getContentResolver(), KEY_WAKEUP[this.mWakeIndex]);
        if (!(startInfo == null || checkAppNeedStart(startInfo))) {
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
        return DEBUG_POWER;
    }

    public boolean setEasyWakeUpFlag(int flag) {
        this.mContext.enforceCallingPermission("android.permission.EASY_WAKE_UP", "set EasyWakeUp Flag");
        setGestureValue(EasyWakeUpXmlParse.getDriverGesturePath(), flag);
        return true;
    }

    private void unLockScreen(boolean keyguardable, boolean keyguardsecure) {
        if (!keyguardable) {
            if (keyguardsecure) {
                this.mKeyguardDelegate.dismiss();
            } else {
                this.mKeyguardDelegate.keyguardDone(DEBUG_POWER, true);
            }
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
                    this.mKeyguardDelegate.dismiss();
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
            this.mViewActive = DEBUG_POWER;
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
            case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                dataList.add(new Point(disX / 2, disY));
                dataList.add(new Point(disX / 2, 0));
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                dataList.add(new Point(disX / 2, 0));
                dataList.add(new Point(disX / 2, disY));
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                dataList.add(new Point(disX, disY / 2));
                dataList.add(new Point(0, disY / 2));
                break;
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                dataList.add(new Point(0, disY / 2));
                dataList.add(new Point(disX, disY / 2));
                break;
        }
        return dataList;
    }

    private boolean startTrackAnimation(int wakeIndex) {
        this.mHandler.post(new AnonymousClass4(wakeIndex));
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
            this.mViewActive = DEBUG_POWER;
        }
    }

    private ArrayList<Point> getTouchPointData(int wakeIndex) {
        Exception e;
        Throwable th;
        if (wakeIndex < 0 || wakeIndex > 8) {
            return null;
        }
        int len = 2;
        if (wakeIndex >= D_INT) {
            len = 6;
        }
        RandomAccessFile randomAccessFile = null;
        ArrayList<Point> dataList = new ArrayList();
        try {
            RandomAccessFile indexFile = new RandomAccessFile(EASYWAKE_DATA_PATH, "r");
            try {
                String data = indexFile.readLine();
                int index = 0;
                for (int i = 0; i < len; i++) {
                    index += D_INT;
                    index += D_INT;
                    dataList.add(new Point(Integer.parseInt(data.substring(index, index + D_INT), 16), Integer.parseInt(data.substring(index, index + D_INT), 16)));
                }
                if (indexFile != null) {
                    try {
                        indexFile.close();
                    } catch (FileNotFoundException e2) {
                        Log.d(TAG, TAG + " getTouchPointData  the file(NODE for driver) is not be found while close file");
                        e2.printStackTrace();
                    } catch (IOException e3) {
                        Log.d(TAG, TAG + " getTouchPointData Exception for IOException while close file");
                        e3.printStackTrace();
                    }
                }
                randomAccessFile = indexFile;
            } catch (Exception e4) {
                e = e4;
                randomAccessFile = indexFile;
                try {
                    Log.d(TAG, TAG + " getTouchPointData Exception for IOException while read file");
                    e.printStackTrace();
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
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
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
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
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = indexFile;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            Log.d(TAG, TAG + " getTouchPointData Exception for IOException while read file");
            e.printStackTrace();
            if (randomAccessFile != null) {
                randomAccessFile.close();
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
            result = Secure.getInt(this.mContext.getContentResolver(), str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setGestureValue(String path, int flag) {
        Exception e;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream file = new FileOutputStream(new File(path));
            try {
                file.write(String.valueOf(flag).getBytes(Charset.forName("UTF-8")));
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e2) {
                        Log.i(TAG, "Closing outputstream: ", e2);
                    }
                }
                fileOutputStream = file;
            } catch (Exception e3) {
                e = e3;
                fileOutputStream = file;
                try {
                    Log.i(TAG, "set gesture to file: error", e);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e22) {
                            Log.i(TAG, "Closing outputstream: ", e22);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e222) {
                            Log.i(TAG, "Closing outputstream: ", e222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = file;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Log.i(TAG, "set gesture to file: error", e);
            if (fileOutputStream != null) {
                fileOutputStream.close();
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
        boolean z = DEBUG_POWER;
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null) {
            return DEBUG_POWER;
        }
        if (telephonyManager.getCallState() != 0) {
            z = true;
        }
        return z;
    }

    public boolean handleWakeUpKey(KeyEvent event, int mScreenOffReason) {
        if (!this.mIsAnimate || this.mEasyWakeUpView == null) {
            int code = event.getKeyCode();
            if (event.getAction() != 0 || code < MIN_KEYCODE || code > MAX_KEYCODE || (code != COVER_SCREEN_KEYCODE && this.mPowerManager != null && this.mPowerManager.isScreenOn())) {
                return DEBUG_POWER;
            }
            if (mScreenOffReason == 6) {
                if (NODE_EASYWAKE_CONTROL == null || NODE_EASYWAKE_CONTROL.isEmpty()) {
                    NODE_EASYWAKE_CONTROL = Secure.getString(this.mContext.getContentResolver(), KEY_EASYWAKE_CONTROL);
                }
                setGestureValue(NODE_EASYWAKE_CONTROL, 1);
                Log.e(TAG, " Off screen beacuse sensor !");
                return true;
            }
            if (!(this.mSensorManager == null || code == COVER_SCREEN_KEYCODE)) {
                turnOffSensorListener();
                this.mSensorforHandleKey = true;
                this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(8), 0);
                this.mSensorUnRegisted = DEBUG_POWER;
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
                this.mSensorforHandleKey = DEBUG_POWER;
                this.mSensorUnRegisted = true;
                if (this.mSensorVaule < SENSOR_FAR) {
                    Log.e(TAG, "do nothing for easywakeup because of PROXIMITY is " + this.mSensorVaule);
                    turnOnSensorListener();
                    if (NODE_EASYWAKE_CONTROL == null || NODE_EASYWAKE_CONTROL.isEmpty()) {
                        NODE_EASYWAKE_CONTROL = Secure.getString(this.mContext.getContentResolver(), KEY_EASYWAKE_CONTROL);
                    }
                    setGestureValue(NODE_EASYWAKE_CONTROL, 1);
                    this.mSensorVaule = -1.0f;
                    return true;
                }
                this.mSensorVaule = -1.0f;
            }
            if (code != DOUBLE_TOUCH_KEYCODE) {
                if (isCalling()) {
                    Log.e(TAG, " do nothing because Calling !");
                    setGestureValue(NODE_EASYWAKE_CONTROL, 1);
                    return true;
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
                    EasyWakeUpManager.this.mIsAnimate = EasyWakeUpManager.DEBUG_POWER;
                }
            };
            this.mSuccessProcessEasyWakeUp = true;
            if (processEasyWakeUp(code)) {
                if (!this.mSuccessProcessEasyWakeUp) {
                    Log.i(TAG, " write flick node 1 to driver because of fail EasyWakeUp");
                    if (NODE_EASYWAKE_CONTROL == null || NODE_EASYWAKE_CONTROL.isEmpty()) {
                        NODE_EASYWAKE_CONTROL = Secure.getString(this.mContext.getContentResolver(), KEY_EASYWAKE_CONTROL);
                    }
                    setGestureValue(NODE_EASYWAKE_CONTROL, 1);
                }
                return true;
            }
            return DEBUG_POWER;
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
            String tmpWakeupEnable = AppHibernateCst.INVALID_PKG;
            if (this.mSensorVaule < SENSOR_FAR) {
                tmpWakeupEnable = TP_STOP;
            } else {
                tmpWakeupEnable = TP_ENABLE;
            }
            if (this.mTPWakeupGestureStatus != null && !this.mTPWakeupGestureStatus.equals(tmpWakeupEnable)) {
                this.mTPWakeupGestureStatus = tmpWakeupEnable;
                CommandLineUtil.echo(CONSTANTS_USER, this.mTPWakeupGestureStatus, WAKEUP_GESTURE_ENABLE_PATH);
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
        NODE_EASYWAKE_CONTROL = Secure.getString(this.mContext.getContentResolver(), KEY_EASYWAKE_CONTROL);
        SENSOR_NEAR = Secure.getFloat(this.mContext.getContentResolver(), "sensor_near", 0.0f);
        SENSOR_FAR = Secure.getFloat(this.mContext.getContentResolver(), "sensor_far", 5.0f);
        SENSOR_WATCH_TIME = Secure.getLong(this.mContext.getContentResolver(), "sensor_watch_time", 50);
        SENSOR_CHECK_TIMES = Secure.getInt(this.mContext.getContentResolver(), "sensor_check_times", 6);
        if (Secure.getInt(this.mContext.getContentResolver(), "power_optimize", 0) != 1) {
            z = DEBUG_POWER;
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
        Secure.putString(this.mContext.getContentResolver(), KEY_EASYWAKE_CONTROL, EasyWakeUpXmlParse.getDriverControlPath());
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

    private boolean isEasyWakeupEnabledByKernel() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedReader bufferedReader = null;
        boolean result = DEBUG_POWER;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(WAKEUP_GESTURE_STATUS_PATH), Charset.defaultCharset()));
            try {
                String line = reader.readLine();
                if (!(line == null || Integer.parseInt(line.trim().replaceAll("^0[x|X]", AppHibernateCst.INVALID_PKG), 16) == 0)) {
                    result = true;
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e4) {
                e = e4;
                bufferedReader = reader;
                e.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e32) {
                        e32.printStackTrace();
                    }
                }
                Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
                return result;
            } catch (IOException e5) {
                e2 = e5;
                bufferedReader = reader;
                try {
                    e2.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e322) {
                            e322.printStackTrace();
                        }
                    }
                    Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e = e6;
            e.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            Log.e(TAG, "isEasyWakeupEnabledByKernel " + result);
            return result;
        } catch (IOException e7) {
            e2 = e7;
            e2.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
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
            this.mSensorforHandleTp = DEBUG_POWER;
        }
        this.mSensorUnRegisted = DEBUG_POWER;
        this.mTPWakeupGestureStatus = AppHibernateCst.INVALID_PKG;
    }

    public void turnOffSensorListener() {
        if (!this.mSensorUnRegisted && (this.mSensorforHandleTp || this.mSensorforHandleKey)) {
            this.mSensorManager.unregisterListener(this);
        }
        this.mSensorforHandleTp = DEBUG_POWER;
        this.mSensorUnRegisted = true;
    }
}
