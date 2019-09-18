package com.android.server.display;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Xml;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.HwBrightnessSceneRecognition;
import com.android.server.display.HwLightSensorController;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.displayengine.DElog;
import com.huawei.displayengine.DisplayEngineDBManager;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DisplayEngineService extends IDisplayEngineServiceEx.Stub implements HwLightSensorController.LightSensorCallbacks {
    private static final int BINDER_REBUILD_COUNT_MAX = 10;
    private static final String COLOR_MODE_SWITCH_PERMISSION = "com.huawei.android.permission.MANAGE_USERS";
    private static final int COLOR_TEMP_VALID_LUX_THRESHOLD = 50;
    private static final String DISPLAY_ENGINE_PERMISSION = "com.huawei.permission.ACCESS_DISPLAY_ENGINE";
    private static final String KEY_COLOR_MODE_SWITCH = "color_mode_switch";
    private static final String KEY_NATURAL_TONE_SWITCH = "hw_natural_tone_display_switch";
    private static final String KEY_READING_MODE_SWITCH = "hw_reading_mode_display_switch";
    private static final String KEY_USER_PREFERENCE_TRAINING_TIMESTAMP = "hw_brightness_training_timestamp";
    private static final int LIGHT_SENSOR_RATE_MILLS = 300;
    private static final String NATURAL_TONE_SWITCH_PERMISSION = "com.huawei.android.permission.MANAGE_USERS";
    private static final int READING_TYPE = 6;
    private static final int RETURN_PARAMETER_INVALID = -2;
    private static final String SR_CONTROL_XML_FILE = "/display/effect/displayengine/SR_control.xml";
    private static final String TAG = "DE J DisplayEngineService";
    private static final Set<Integer> mSetDataCheckPermissionList = new HashSet<Integer>() {
        {
            add(5);
            add(4);
            add(10);
            add(9);
            add(11);
            add(7);
        }
    };
    private static final Set<Integer> mSetSceneCheckPermissionList = new HashSet<Integer>() {
        {
            add(0);
            add(10);
            add(13);
            add(15);
            add(11);
            add(30);
            add(32);
            add(34);
            add(24);
            add(12);
            add(33);
            add(39);
            add(40);
            add(17);
            add(18);
            add(20);
            add(21);
            add(26);
            add(28);
            add(29);
            add(31);
            add(35);
            add(36);
            add(38);
            add(25);
            add(41);
            add(42);
        }
    };
    private ContentObserver mAutoBrightnessAdjObserver = null;
    private AutoBrightnessAdjSwitchedReceiver mAutoBrightnessAdjSwitchedReceiver = null;
    private int mBinderRebuildCount = 0;
    /* access modifiers changed from: private */
    public boolean mBootComplete = false;
    /* access modifiers changed from: private */
    public int mChargeLevelThreshold = 50;
    private ChargingStateReceiver mChargingStateReceiver = null;
    private ContentObserver mColorModeObserver = null;
    private ColorModeSwitchedReceiver mColorModeSwitchedReceiver = null;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public DcBrightnessDimmingObserver mDcBrightnessDimmingObserver = null;
    /* access modifiers changed from: private */
    public int mDefaultColorModeValue = 1;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    /* access modifiers changed from: private */
    public DisplayEngineManager mDisplayManager = null;
    private final DisplayEngineHandler mHandler;
    private final HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public final HwBrightnessSceneRecognition mHwBrightnessSceneRecognition;
    private volatile boolean mIsBinderBuilding = false;
    /* access modifiers changed from: private */
    public volatile boolean mIsBrightnessTrainingAborting = false;
    /* access modifiers changed from: private */
    public volatile boolean mIsBrightnessTrainingRunning = false;
    /* access modifiers changed from: private */
    public volatile boolean mIsTrainingTriggeredSinceLastScreenOff = false;
    private long mLastAmbientColorTempToMonitorTime;
    private final HwLightSensorController mLightSensorController;
    private boolean mLightSensorEnable = false;
    private Object mLockBinderBuilding;
    private Object mLockService;
    /* access modifiers changed from: private */
    public long mMinimumTrainingIntervalMillis = 57600000;
    private final MotionActionReceiver mMotionActionReceiver;
    private volatile IDisplayEngineService mNativeService;
    private volatile boolean mNativeServiceInitialized = false;
    private ContentObserver mNaturalToneObserver = null;
    private NaturalToneSwitchedReceiver mNaturalToneSwitchedReceiver = null;
    /* access modifiers changed from: private */
    public boolean mNeedPkgNameFromPG = false;
    /* access modifiers changed from: private */
    public int mNewDragNumThreshold = 1;
    /* access modifiers changed from: private */
    public boolean mPGEnable = false;
    private PGSdk mPGSdk = null;
    /* access modifiers changed from: private */
    public volatile boolean mScreenOn = false;
    private final ScreenStateReceiver mScreenStateReceiver;
    private PGSdk.Sink mStateRecognitionListener = null;
    private final ThpListener mThpListener;

    private class AutoBrightnessAdjSwitchedReceiver extends BroadcastReceiver {
        private AutoBrightnessAdjSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid AutoBrightnessAdjSwitchedReceiver invalid action!");
                return;
            }
            char c = 65535;
            if (action.hashCode() == 959232034 && action.equals("android.intent.action.USER_SWITCHED")) {
                c = 0;
            }
            if (c == 0) {
                if (DisplayEngineService.this.mHwBrightnessSceneRecognition != null && DisplayEngineService.this.mHwBrightnessSceneRecognition.isEnable()) {
                    DisplayEngineService.this.mHwBrightnessSceneRecognition.notifyUserChange(ActivityManager.getCurrentUser());
                }
                DisplayEngineService.this.initAutoBrightnessAdjContentObserver();
            }
        }
    }

    private class ChargingStateReceiver extends BroadcastReceiver {
        public ChargingStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_CHANGED");
            DisplayEngineService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            Intent intent2 = intent;
            if (context == null || intent2 == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            DElog.d(DisplayEngineService.TAG, "BroadcastReceiver.onReceive() action:" + action);
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                boolean chargeStatus = false;
                int mLevel = (int) ((100.0f * ((float) intent2.getIntExtra(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, 0))) / ((float) intent2.getIntExtra("scale", 100)));
                int status = intent2.getIntExtra("status", 1);
                if (status == 5 || status == 2) {
                    chargeStatus = true;
                }
                long lastTrainingProcessTimeMillis = Settings.System.getLongForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_USER_PREFERENCE_TRAINING_TIMESTAMP, 0, -2);
                final long elapseTime = System.currentTimeMillis() - lastTrainingProcessTimeMillis;
                DElog.d(DisplayEngineService.TAG, "lastTraining elapseTimeMillis = " + elapseTime);
                if (DisplayEngineService.this.mDisplayManager == null) {
                    DElog.w(DisplayEngineService.TAG, "ChargingStateReceiver on recieve, mDisplayManager is null! returned.");
                    return;
                }
                DisplayEngineDBManager dbManager = DisplayEngineDBManager.getInstance(DisplayEngineService.this.mContext);
                if (dbManager == null) {
                    DElog.w(DisplayEngineService.TAG, "ChargingStateReceiver on recieve, dbManager is null! returned.");
                    return;
                }
                Bundle info = new Bundle();
                info.putInt("NumberLimit", DisplayEngineService.this.mNewDragNumThreshold);
                ArrayList<Bundle> items = dbManager.getAllRecords("DragInfo", info);
                if (items == null) {
                    int i = status;
                } else if (items.size() < DisplayEngineService.this.mNewDragNumThreshold) {
                    String str = action;
                    int i2 = status;
                } else {
                    Bundle data = items.get(DisplayEngineService.this.mNewDragNumThreshold - 1);
                    if (data == null) {
                        DElog.i(DisplayEngineService.TAG, "ChargingStateReceiver on recieve, data is null! returned.");
                        return;
                    }
                    String str2 = action;
                    long newestDragTimeMillis = data.getLong("TimeStamp", 0);
                    if (DisplayEngineService.this.mIsTrainingTriggeredSinceLastScreenOff || newestDragTimeMillis <= lastTrainingProcessTimeMillis || DisplayEngineService.this.mScreenOn || mLevel <= DisplayEngineService.this.mChargeLevelThreshold || !chargeStatus || elapseTime <= DisplayEngineService.this.mMinimumTrainingIntervalMillis || !DisplayEngineService.this.mBootComplete) {
                        DElog.d(DisplayEngineService.TAG, "-----------No Tigger Training Reason Start-----------");
                        StringBuilder sb = new StringBuilder();
                        int i3 = status;
                        sb.append("newestDragTime / lastTrainingProcessTime:     ");
                        sb.append(newestDragTimeMillis);
                        sb.append(" / ");
                        sb.append(lastTrainingProcessTimeMillis);
                        DElog.d(DisplayEngineService.TAG, sb.toString());
                        DElog.d(DisplayEngineService.TAG, "mChargeLevel / mChargeLevelThreshold:         " + mLevel + " / " + DisplayEngineService.this.mChargeLevelThreshold);
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("chargeStatus:                                 ");
                        sb2.append(chargeStatus);
                        DElog.d(DisplayEngineService.TAG, sb2.toString());
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("elapsedTime / mMinimumTrainingIntervalMillis: ");
                        sb3.append(elapseTime);
                        sb3.append(" / ");
                        long j = newestDragTimeMillis;
                        sb3.append(DisplayEngineService.this.mMinimumTrainingIntervalMillis);
                        DElog.d(DisplayEngineService.TAG, sb3.toString());
                        DElog.d(DisplayEngineService.TAG, "mScreenOn:                                    " + DisplayEngineService.this.mScreenOn);
                        DElog.d(DisplayEngineService.TAG, "mBootComplete:                                " + DisplayEngineService.this.mBootComplete);
                        DElog.d(DisplayEngineService.TAG, "-----------No Tigger Training Reason Ended-----------");
                    } else {
                        boolean unused = DisplayEngineService.this.mIsTrainingTriggeredSinceLastScreenOff = true;
                        if (!DisplayEngineService.this.mIsBrightnessTrainingRunning) {
                            boolean unused2 = DisplayEngineService.this.mIsBrightnessTrainingRunning = true;
                            new Thread(new Runnable() {
                                public void run() {
                                    DElog.i(DisplayEngineService.TAG, "mDisplayManager.brightnessTrainingProcess start... ");
                                    if (DisplayEngineService.this.mDisplayManager.brightnessTrainingProcess() == 0) {
                                        long curTime = System.currentTimeMillis();
                                        DElog.i(DisplayEngineService.TAG, "Elapsed Time since last training: " + elapseTime + " > mMinimumTrainingIntervalMillis: " + DisplayEngineService.this.mMinimumTrainingIntervalMillis + ", training successfully done.");
                                        Settings.System.putLongForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_USER_PREFERENCE_TRAINING_TIMESTAMP, curTime, -2);
                                    }
                                    DElog.i(DisplayEngineService.TAG, "mDisplayManager.brightnessTrainingProcess finished.");
                                    boolean unused = DisplayEngineService.this.mIsBrightnessTrainingRunning = false;
                                }
                            }).start();
                        } else {
                            DElog.w(DisplayEngineService.TAG, "Trigger training failed, mIsBrightnessTrainingRunning == true");
                        }
                    }
                }
                DElog.i(DisplayEngineService.TAG, "ChargingStateReceiver on recieve, items is null || items.size < 1! returned.");
                return;
            }
        }
    }

    private class ColorModeSwitchedReceiver extends BroadcastReceiver {
        private ColorModeSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                DElog.w(DisplayEngineService.TAG, "ColorModeSwitched receiver.getAction() is null!");
                return;
            }
            char c = 65535;
            if (action.hashCode() == 959232034 && action.equals("android.intent.action.USER_SWITCHED")) {
                c = 0;
            }
            if (c == 0) {
                if (Settings.System.getIntForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_COLOR_MODE_SWITCH, DisplayEngineService.this.mDefaultColorModeValue, -2) == 0) {
                    DisplayEngineService.this.setScene(13, 16);
                } else {
                    DisplayEngineService.this.setScene(13, 17);
                }
            }
            DisplayEngineService.this.initColorContentObserver();
        }
    }

    private class DcBrightnessDimmingObserver extends ContentObserver {
        private static final String KEY_DC_BRIGHTNESS_DIMMING_SWITCH = "hw_dc_brightness_dimming_switch";
        private static final int REGISTER_OBSERVER_COUNT_MAX = 10;
        private static final int REGISTER_OBSERVER_DELAY_MS = 2000;

        public DcBrightnessDimmingObserver(Handler handler) {
            super(handler);
            DElog.i(DisplayEngineService.TAG, "DcBrightnessDimmingObserver");
            updateDcBrightnessDimmingStatus();
            if (!registerObserver()) {
                registerObserverDelayed();
            }
        }

        public void onChange(boolean selfChange) {
            updateDcBrightnessDimmingStatus();
        }

        /* access modifiers changed from: private */
        public boolean registerObserver() {
            ContentResolver resolver = DisplayEngineService.this.mContext.getContentResolver();
            if (resolver == null) {
                DElog.i(DisplayEngineService.TAG, "register content observer delayed, will try 10 times, with delay 2000ms.");
                return false;
            }
            resolver.registerContentObserver(Settings.System.getUriFor(KEY_DC_BRIGHTNESS_DIMMING_SWITCH), true, this, -1);
            DElog.i(DisplayEngineService.TAG, "register content observer successfully.");
            return true;
        }

        private void registerObserverDelayed() {
            new Thread(new Runnable() {
                public void run() {
                    int retryCount = 0;
                    boolean isDone = false;
                    while (!isDone && retryCount < 10) {
                        retryCount++;
                        DElog.i(DisplayEngineService.TAG, "registerObserverDelayed, retryCount = " + retryCount);
                        SystemClock.sleep(2000);
                        isDone = DcBrightnessDimmingObserver.this.registerObserver();
                    }
                    if (!isDone) {
                        DElog.e(DisplayEngineService.TAG, "registerObserverDelayed failed!, retryCount =" + retryCount);
                    }
                }
            }, "RegisterObserverDelayedThread").start();
        }

        public final void updateDcBrightnessDimmingStatus() {
            if (Settings.System.getIntForUser(DisplayEngineService.this.mContext.getContentResolver(), KEY_DC_BRIGHTNESS_DIMMING_SWITCH, 0, -2) == 1) {
                DisplayEngineService.this.setScene(41, 16);
            } else {
                DisplayEngineService.this.setScene(41, 17);
            }
        }
    }

    private final class DisplayEngineHandler extends Handler {
        public static final int BEGIN_POSITION = 1;
        public static final int END_POSITION = -1;
        public static final int IMAGE_EXIT = 1004;
        public static final int IMAGE_FULLSCREEN_VIEW = 1002;
        public static final int IMAGE_THUMBNAIL = 1003;
        public static final int VIDEO_FULLSCREEN_EXIT = 1001;
        public static final int VIDEO_FULLSCREEN_START = 1000;
        boolean mAlreadyHandle = false;
        int mSceneState = 0;
        Stack mVideoStack = new Stack();

        public DisplayEngineHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                DElog.e(DisplayEngineService.TAG, "msg is null!");
                return;
            }
            if (msg.what != 2) {
                DElog.e(DisplayEngineService.TAG, "Invalid message");
            } else {
                Bundle B = msg.getData();
                String name = (String) B.get("SurfaceName");
                String attachWin = (String) B.get("AttachWinName");
                if (attachWin != null && WhiteList.ATTACH_IMAGE_LIST.contains(attachWin) && name.startsWith("PopupWindow")) {
                    setImageScene(B);
                } else if (WhiteList.IMAGE_LIST.contains(name)) {
                    setImageScene(B);
                } else if (WhiteList.VIDEO_LIST.contains(name)) {
                    setVideoScene(B);
                } else if (WhiteList.VO_LIST.contains(name)) {
                    setVoScene(B);
                }
            }
        }

        private void getTimer() {
            DElog.d(DisplayEngineService.TAG, "getTimer step in!");
            if (!this.mAlreadyHandle) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        DisplayEngineHandler.this.handleVideoStack();
                    }
                }, (long) 100);
                this.mAlreadyHandle = true;
            }
        }

        private void pushActionToStack(int action) {
            this.mVideoStack.push(Integer.valueOf(action));
            getTimer();
        }

        private void setVideoScene(Bundle B) {
            int frameLeft = B.getInt("FrameLeft");
            int frameRight = B.getInt("FrameRight");
            int frameTop = B.getInt("FrameTop");
            int frameBottom = B.getInt("FrameBottom");
            int displayWidth = B.getInt("DisplayWidth");
            int displayHeight = B.getInt("DisplayHeight");
            int position = B.getInt("Position");
            if (position == -1) {
                pushActionToStack(1001);
            } else if (position != 1) {
            } else {
                if (WhiteList.isFullScreen(frameLeft, frameTop, frameRight, frameBottom, displayWidth, displayHeight)) {
                    pushActionToStack(1000);
                } else {
                    pushActionToStack(1001);
                }
            }
        }

        private void setImageScene(Bundle B) {
            int frameLeft = B.getInt("FrameLeft");
            int frameRight = B.getInt("FrameRight");
            int frameTop = B.getInt("FrameTop");
            int frameBottom = B.getInt("FrameBottom");
            int displayWidth = B.getInt("DisplayWidth");
            int displayHeight = B.getInt("DisplayHeight");
            int position = B.getInt("Position");
            if (position == -1) {
                pushActionToStack(1004);
            } else if (position != 1) {
            } else {
                if (WhiteList.isFullScreen(frameLeft, frameTop, frameRight, frameBottom, displayWidth, displayHeight)) {
                    pushActionToStack(1002);
                } else {
                    pushActionToStack(1003);
                }
            }
        }

        private void setVoScene(Bundle B) {
            int position = B.getInt("Position");
            if (position == -1) {
                pushActionToStack(1001);
            } else if (position == 1) {
                pushActionToStack(1000);
            } else {
                DElog.e(DisplayEngineService.TAG, "[leilei] setVoScene position ERROR !");
            }
        }

        /* access modifiers changed from: private */
        public void handleVideoStack() {
            DElog.d(DisplayEngineService.TAG, "handleVideoStack step in!");
            if (this.mVideoStack.empty()) {
                this.mAlreadyHandle = false;
                return;
            }
            if (this.mSceneState != ((Integer) this.mVideoStack.peek()).intValue()) {
                this.mSceneState = ((Integer) this.mVideoStack.peek()).intValue();
                sendToNativeScene(this.mSceneState);
            }
            while (!this.mVideoStack.empty()) {
                this.mVideoStack.pop();
            }
            this.mAlreadyHandle = false;
        }

        private int sendToNativeScene(int sceneState) {
            switch (sceneState) {
                case 1000:
                    return DisplayEngineService.this.setScene(1, 4);
                case 1001:
                    int ret = DisplayEngineService.this.setScene(1, 8);
                    if (DisplayEngineService.this.mPGEnable) {
                        return DisplayEngineService.this.setDetailPGscene();
                    }
                    return DisplayEngineService.this.setDetailIawareScene();
                case 1002:
                    return DisplayEngineService.this.setScene(3, 12);
                case 1003:
                    return DisplayEngineService.this.setScene(3, 12);
                case 1004:
                    int ret2 = DisplayEngineService.this.setScene(3, 13);
                    if (DisplayEngineService.this.mPGEnable) {
                        return DisplayEngineService.this.setDetailPGscene();
                    }
                    return DisplayEngineService.this.setDetailIawareScene();
                default:
                    return -1;
            }
        }
    }

    private class MotionActionReceiver extends BroadcastReceiver {
        private static final String ACTION_MOTION = "com.huawei.motion.change.noification";
        private static final String EXTRA_KEY = "category";
        private static final String EXTRA_MOTION_START = "start_motion";
        private static final String EXTRA_MOTION_STOP_BACK_APP_NOCHANGE = "back_application_nochange";
        private static final String EXTRA_MOTION_STOP_BACK_APP_TRANSATION = "back_application_transation";
        private static final String EXTRA_MOTION_STOP_HOME = "return_home";
        private static final String EXTRA_MOTION_STOP_RECENT = "enter_recent";
        private static final String PERMISSION_MOTION = "com.huawei.android.launcher.permission.HW_MOTION";

        public MotionActionReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_MOTION);
            filter.setPriority(1000);
            DisplayEngineService.this.mContext.registerReceiver(this, filter, PERMISSION_MOTION, null);
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] MotionActionReceiver.onRecive() Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                DElog.w(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] MotionActionReceiver.getAction() is null!");
                return;
            }
            DElog.d(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] MotionActionReceiver.onReceive() action:" + action);
            if (action.equals(ACTION_MOTION)) {
                String stringExtra = intent.getStringExtra("category");
                if (stringExtra != null) {
                    DElog.i(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] MotionActionReceiver.onReceive() extra:" + stringExtra);
                    char c = 65535;
                    switch (stringExtra.hashCode()) {
                        case -1486606706:
                            if (stringExtra.equals(EXTRA_MOTION_STOP_HOME)) {
                                c = 3;
                                break;
                            }
                            break;
                        case -543270494:
                            if (stringExtra.equals(EXTRA_MOTION_STOP_RECENT)) {
                                c = 4;
                                break;
                            }
                            break;
                        case -158947789:
                            if (stringExtra.equals(EXTRA_MOTION_START)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 1472278296:
                            if (stringExtra.equals(EXTRA_MOTION_STOP_BACK_APP_NOCHANGE)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 1851839988:
                            if (stringExtra.equals(EXTRA_MOTION_STOP_BACK_APP_TRANSATION)) {
                                c = 2;
                                break;
                            }
                            break;
                    }
                    switch (c) {
                        case 0:
                            DisplayEngineService.this.setScene(38, 24);
                            DElog.d(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] Motion start");
                            break;
                        case 1:
                        case 2:
                            DisplayEngineService.this.setScene(38, 22);
                            DElog.d(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] Motion app");
                            break;
                        case 3:
                            DElog.d(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] Motion home");
                            DisplayEngineService.this.setScene(38, 21);
                            break;
                        case 4:
                            DisplayEngineService.this.setScene(38, 23);
                            DElog.d(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] Motion recent");
                            break;
                    }
                } else {
                    DElog.w(DisplayEngineService.TAG, "[MOTION_NOTIFICATION] MotionActionReceiver.getStringExtra() is null!");
                }
            }
        }
    }

    private class NaturalToneSwitchedReceiver extends BroadcastReceiver {
        private NaturalToneSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid action!");
                return;
            }
            char c = 65535;
            if (action.hashCode() == 959232034 && action.equals("android.intent.action.USER_SWITCHED")) {
                c = 0;
            }
            if (c == 0) {
                if (1 == Settings.System.getIntForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_NATURAL_TONE_SWITCH, 0, -2)) {
                    DisplayEngineService.this.setScene(25, 16);
                    DElog.v(DisplayEngineService.TAG, "NaturalToneSwitchedReceiver setScene, DE_ACTION_MODE_ON");
                } else {
                    DisplayEngineService.this.setScene(25, 17);
                    DElog.v(DisplayEngineService.TAG, "NaturalToneSwitchedReceiver setScene, DE_ACTION_MODE_OFF");
                }
            }
            DisplayEngineService.this.initNaturalToneContentObserver();
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.addAction("android.intent.action.USER_SWITCHED");
            filter.setPriority(1000);
            DisplayEngineService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                DElog.w(DisplayEngineService.TAG, "BroadcastReceiver.getAction() is null!");
                return;
            }
            DElog.i(DisplayEngineService.TAG, "BroadcastReceiver.onReceive() action:" + action);
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -2128145023) {
                if (hashCode != -1454123155) {
                    if (hashCode != 798292259) {
                        if (hashCode == 959232034 && action.equals("android.intent.action.USER_SWITCHED")) {
                            c = 3;
                        }
                    } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        c = 2;
                    }
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    c = 1;
                }
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                c = 0;
            }
            switch (c) {
                case 0:
                    DisplayEngineService.this.setScene(10, 17);
                    boolean unused = DisplayEngineService.this.mIsTrainingTriggeredSinceLastScreenOff = false;
                    boolean unused2 = DisplayEngineService.this.mScreenOn = false;
                    break;
                case 1:
                    DisplayEngineService.this.setScene(10, 16);
                    if (!DisplayEngineService.this.mIsBrightnessTrainingRunning || DisplayEngineService.this.mIsBrightnessTrainingAborting || DisplayEngineService.this.mDisplayManager == null) {
                        DElog.d(DisplayEngineService.TAG, "Trigger training abort failed, training is NOT running or is already aborting.");
                    } else {
                        boolean unused3 = DisplayEngineService.this.mIsBrightnessTrainingAborting = true;
                        new Thread(new Runnable() {
                            public void run() {
                                DElog.i(DisplayEngineService.TAG, "mDisplayManager.brightnessTrainingAbort start... ");
                                DisplayEngineService.this.mDisplayManager.brightnessTrainingAbort();
                                DElog.i(DisplayEngineService.TAG, "mDisplayManager.brightnessTrainingAbort finished.");
                                boolean unused = DisplayEngineService.this.mIsBrightnessTrainingAborting = false;
                            }
                        }).start();
                    }
                    boolean unused4 = DisplayEngineService.this.mScreenOn = true;
                    break;
                case 2:
                    if (DisplayEngineService.this.mPGEnable || DisplayEngineService.this.mNeedPkgNameFromPG) {
                        DisplayEngineService.this.registerPGSdk();
                    }
                    DisplayEngineService.this.initColorModeSwitch();
                    DisplayEngineService.this.initNaturalToneSwitch();
                    DisplayEngineService.this.initAutoBrightnessAdjSwitch();
                    DisplayEngineService.this.initAutoBrightnessAdjContentObserver();
                    DisplayEngineService.this.setScene(18, 16);
                    DisplayEngineService.this.mHwBrightnessSceneRecognition.initBootCompleteValues();
                    boolean unused5 = DisplayEngineService.this.mBootComplete = true;
                    boolean unused6 = DisplayEngineService.this.mScreenOn = true;
                    break;
                case 3:
                    if (DisplayEngineService.this.mDcBrightnessDimmingObserver != null) {
                        DisplayEngineService.this.mDcBrightnessDimmingObserver.updateDcBrightnessDimmingStatus();
                        break;
                    }
                    break;
            }
        }
    }

    static final class WhiteList {
        static final List<String> ATTACH_IMAGE_LIST = new ArrayList<String>() {
            {
                add("com.tencent.mm/com.tencent.mm.plugin.profile.ui.ContactInfoUI");
            }
        };
        static final List<String> IMAGE_LIST = new ArrayList<String>() {
            {
                add("com.tencent.mm/com.tencent.mm.ui.chatting.gallery.ImageGalleryUI");
                add("com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsBrowseUI");
                add("com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsGalleryUI");
                add("com.tencent.mm/com.tencent.mm.plugin.subapp.ui.gallery.GestureGalleryUI");
                add("com.tencent.mm/com.tencent.mm.plugin.gallery.ui.ImagePreviewUI");
                add("com.tencent.mm/com.tencent.mm.plugin.setting.ui.setting.PreviewHdHeadImg");
                add("com.tencent.mobileqq/com.tencent.mobileqq.activity.aio.photo.AIOGalleryActivity");
                add("com.tencent.mobileqq/cooperation.qzone.QzonePicturePluginProxyActivity");
                add("com.tencent.mobileqq/com.tencent.mobileqq.activity.photo.PhotoPreviewActivity");
                add("com.tencent.mobileqq/com.tencent.mobileqq.activity.FriendProfileImageActivity");
                add("com.baidu.tieba/com.baidu.tieba.image.ImageViewerActivity");
                add("com.sina.weibo/com.sina.weibo.imageviewer.ImageViewer");
            }
        };
        static final List<String> VIDEO_LIST = new ArrayList<String>() {
            {
                add("SurfaceView - air.tv.douyu.android/tv.douyu.view.activity.PlayerActivity");
                add("SurfaceView - air.tv.douyu.android/tv.douyu.view.activity.VideoPlayerActivity");
                add("SurfaceView - air.tv.douyu.android/tv.douyu.view.activity.MobilePlayerActivity");
                add("SurfaceView - com.panda.videoliveplatform/com.panda.videoliveplatform.activity.LiveRoomActivity");
                add("SurfaceView - com.meelive.ingkee/com.meelive.ingkee.game.activity.RoomPlayerActivity");
                add("SurfaceView - com.meelive.ingkee/com.meelive.ingkee.ui.room.activity.RoomActivity");
                add("SurfaceView - com.duowan.kiwi/com.duowan.kiwi.channelpage.ChannelPage");
                add("SurfaceView - com.duowan.kiwi/com.duowan.kiwi.mobileliving.PortraitAwesomeLivingActivity");
                add("SurfaceView - com.duowan.kiwi/com.duowan.kiwi.recordervedio.VideoShowDetailActivity");
            }
        };
        static final List<String> VO_LIST = new ArrayList<String>() {
            {
                add("SurfaceView - com.tencent.mm/com.tencent.mm.plugin.voip.ui.VideoActivity");
                add("SurfaceView - com.tencent.mobileqq/com.tencent.av.ui.AVActivity");
            }
        };

        WhiteList() {
        }

        static boolean isFullScreen(int frameLeft, int frameTop, int frameRight, int frameBottom, int displayWidth, int displayHeight) {
            int l = Math.abs(frameLeft);
            int t = Math.abs(frameTop);
            int r = Math.abs(frameRight);
            int b = Math.abs(frameBottom);
            boolean isLandscape = l + r >= displayHeight + -100 && t + b >= displayWidth + -100;
            boolean isPortrait = l + r >= displayWidth + -100 && t + b >= displayHeight + -100;
            if (isLandscape || isPortrait) {
                return true;
            }
            return false;
        }
    }

    public DisplayEngineService(Context context) {
        this.mContext = context;
        HwLightSensorController controller = null;
        SensorManager manager = (SensorManager) this.mContext.getSystemService("sensor");
        if (manager == null) {
            DElog.e(TAG, "Failed to get SensorManager:sensor");
        } else {
            controller = new HwLightSensorController(this.mContext, this, manager, 300);
        }
        this.mLightSensorController = controller;
        this.mHwBrightnessSceneRecognition = new HwBrightnessSceneRecognition(this.mContext);
        this.mScreenStateReceiver = new ScreenStateReceiver();
        this.mMotionActionReceiver = new MotionActionReceiver();
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new DisplayEngineHandler(this.mHandlerThread.getLooper());
        this.mNativeService = null;
        this.mLockService = new Object();
        this.mLockBinderBuilding = new Object();
        getConfigParam();
        sendUIScene();
        setDefaultColorModeValue();
        initColorModeValue();
        initNaturalToneValue();
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mHwBrightnessSceneRecognition.isFeatureEnable(HwBrightnessSceneRecognition.FeatureTag.TAG_PERSONALIZED_CURVE)) {
            this.mChargingStateReceiver = new ChargingStateReceiver();
            this.mDisplayManager = new DisplayEngineManager(this.mContext);
        }
        this.mThpListener = new ThpListener(this.mContext);
        if (getSupported(30) != 0) {
            this.mDcBrightnessDimmingObserver = new DcBrightnessDimmingObserver(new Handler());
        }
    }

    private IDisplayEngineService getNativeService() throws RemoteException {
        if (this.mNativeService == null && !this.mNativeServiceInitialized) {
            synchronized (this.mLockService) {
                if (this.mNativeService == null && !this.mNativeServiceInitialized) {
                    buildBinder();
                    if (this.mNativeService != null) {
                        this.mNativeServiceInitialized = true;
                    }
                }
            }
        }
        if (this.mNativeService != null || !this.mNativeServiceInitialized) {
            return this.mNativeService;
        }
        if (this.mBinderRebuildCount < 10) {
            throw new RemoteException("Try to rebuild binder " + this.mBinderRebuildCount + " times.");
        }
        throw new RemoteException("binder rebuilding failed!");
    }

    private void buildBinder() {
        IBinder binder = ServiceManager.getService("DisplayEngineService");
        if (binder != null) {
            this.mNativeService = IDisplayEngineService.Stub.asInterface(binder);
            if (this.mNativeService == null) {
                DElog.w(TAG, "service is null!");
                return;
            }
            return;
        }
        this.mNativeService = null;
        DElog.w(TAG, "binder is null!");
    }

    /* access modifiers changed from: private */
    public void rebuildBinder() {
        DElog.i(TAG, "wait 800ms to rebuild binder...");
        SystemClock.sleep(800);
        DElog.i(TAG, "rebuild binder...");
        synchronized (this.mLockService) {
            buildBinder();
            if (this.mNativeService != null) {
                DElog.i(TAG, "rebuild binder success.");
                if (this.mScreenOn) {
                    setScene(10, 16);
                }
            } else {
                DElog.i(TAG, "rebuild binder failed!");
                this.mBinderRebuildCount++;
            }
        }
        synchronized (this.mLockBinderBuilding) {
            if (this.mBinderRebuildCount < 10) {
                this.mIsBinderBuilding = false;
            }
        }
    }

    private void rebuildBinderDelayed() {
        if (!this.mIsBinderBuilding) {
            synchronized (this.mLockBinderBuilding) {
                if (!this.mIsBinderBuilding) {
                    new Thread(new Runnable() {
                        public void run() {
                            DisplayEngineService.this.rebuildBinder();
                        }
                    }).start();
                    this.mIsBinderBuilding = true;
                }
            }
        }
    }

    public final int getSupported(int feature) {
        try {
            IDisplayEngineService service = getNativeService();
            if (service != null) {
                return service.getSupported(feature);
            }
            return 0;
        } catch (RemoteException e) {
            DElog.e(TAG, "getSupported(" + feature + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
            return 0;
        }
    }

    public int setScene(int scene, int action) {
        int ret = -1;
        try {
            IDisplayEngineService service = getNativeService();
            if (service == null || !setSceneHasPermission(scene, action)) {
                return -1;
            }
            ret = service.setScene(scene, action);
            if (scene == 24 && this.mHwBrightnessSceneRecognition != null && this.mHwBrightnessSceneRecognition.isEnable()) {
                this.mHwBrightnessSceneRecognition.notifyScreenStatus(action == 16);
            }
            return ret;
        } catch (RemoteException e) {
            DElog.e(TAG, "setScene(" + scene + ", " + action + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
        }
    }

    private boolean setSceneHasPermission(int scene, int action) {
        if (!mSetSceneCheckPermissionList.contains(Integer.valueOf(scene))) {
            return true;
        }
        int uid = Binder.getCallingUid();
        if (uid == 1000 || this.mContext.checkCallingOrSelfPermission(DISPLAY_ENGINE_PERMISSION) == 0) {
            return true;
        }
        DElog.w(TAG, "setScene requires SYSTEM_UID or com.huawei.permission.ACCESS_DISPLAY_ENGINE, scene=" + scene + ", action=" + action + ", uid=" + uid + ", pid=" + Binder.getCallingPid());
        return false;
    }

    public int setData(int type, PersistableBundle data) {
        int ret = -1;
        try {
            IDisplayEngineService service = getNativeService();
            if (service == null || !setDataHasPermission(type)) {
                return -1;
            }
            if (data == null) {
                DElog.e(TAG, "setData(" + type + ", data): data is null!");
                ret = -2;
            } else if (this.mPGEnable && type == 10) {
                DElog.d(TAG, "setData(" + type + ", data): mPGEnable is true!");
                return -1;
            } else {
                ret = service.setData(type, data);
                if (type == 10) {
                    handleIawareSpecialScene(type, data);
                    handleIawareEbookScene(type, data);
                }
            }
            return ret;
        } catch (RemoteException e) {
            DElog.e(TAG, "setData(" + type + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
        }
    }

    private boolean setDataHasPermission(int type) {
        if (!mSetDataCheckPermissionList.contains(Integer.valueOf(type))) {
            return true;
        }
        int uid = Binder.getCallingUid();
        if (uid == 1000 || this.mContext.checkCallingOrSelfPermission(DISPLAY_ENGINE_PERMISSION) == 0) {
            return true;
        }
        DElog.w(TAG, "setData requires SYSTEM_UID or com.huawei.permission.ACCESS_DISPLAY_ENGINE, type=" + type + ", uid=" + uid + ", pid=" + Binder.getCallingPid());
        return false;
    }

    private void handleIawareEbookScene(int type, PersistableBundle data) {
        if (type == 10) {
            int scene = data.getInt("Scene");
            DElog.d(TAG, "Scene is " + scene);
            if (scene == 6) {
                String pkgName = getCurrentTopAppName();
                if (pkgName != null) {
                    DElog.d(TAG, "pkgName is " + pkgName);
                }
                Settings.System.putIntForUser(this.mContext.getContentResolver(), KEY_READING_MODE_SWITCH, 1, -2);
            } else {
                Settings.System.putIntForUser(this.mContext.getContentResolver(), KEY_READING_MODE_SWITCH, 0, -2);
            }
        }
    }

    public int sendMessage(int messageID, Bundle data) {
        int ret = 0;
        if (data == null) {
            DElog.e(TAG, "sendMessage(" + messageID + ", data): data is null!");
            ret = -2;
        } else if (!sendMessageHasPermission(messageID)) {
            return 0;
        } else {
            Message msg = this.mHandler.obtainMessage(messageID);
            msg.setData(data);
            this.mHandler.sendMessage(msg);
        }
        return ret;
    }

    private boolean sendMessageHasPermission(int messageID) {
        int uid = Binder.getCallingUid();
        if (uid == 1000 || this.mContext.checkCallingOrSelfPermission(DISPLAY_ENGINE_PERMISSION) == 0) {
            return true;
        }
        DElog.w(TAG, "sendMessage requires SYSTEM_UID or com.huawei.permission.ACCESS_DISPLAY_ENGINE, messageID=" + messageID + ", uid=" + uid + ", pid=" + Binder.getCallingPid());
        return false;
    }

    public int getEffect(int feature, int type, byte[] status, int length) {
        int ret = -1;
        try {
            IDisplayEngineService service = getNativeService();
            if (service == null || !getEffectHasPermission(feature, type)) {
                return -1;
            }
            if (status == null || status.length != length) {
                DElog.e(TAG, "getEffect(" + feature + ", " + type + ", status, " + length + "): data is null or status.length != length!");
                ret = -2;
                return ret;
            }
            ret = service.getEffect(feature, type, status, length);
            return ret;
        } catch (RemoteException e) {
            DElog.e(TAG, "getEffect(" + feature + ", " + type + ", " + length + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
        }
    }

    private boolean getEffectNeedCheckPermission(int feature, int type) {
        return feature == 14 || feature == 25;
    }

    private boolean getEffectHasPermission(int feature, int type) {
        if (!getEffectNeedCheckPermission(feature, type)) {
            return true;
        }
        int uid = Binder.getCallingUid();
        if (uid == 1000 || this.mContext.checkCallingOrSelfPermission(DISPLAY_ENGINE_PERMISSION) == 0) {
            return true;
        }
        DElog.w(TAG, "getEffect requires SYSTEM_UID or com.huawei.permission.ACCESS_DISPLAY_ENGINE, feature=" + feature + ", type=" + type + ", uid=" + uid + ", pid=" + Binder.getCallingPid());
        return false;
    }

    public int setEffect(int feature, int mode, PersistableBundle data) {
        try {
            IDisplayEngineService service = getNativeService();
            if (service == null) {
                return -1;
            }
            if (data != null) {
                return service.setEffect(feature, mode, data);
            }
            DElog.e(TAG, "setEffect(" + feature + ", " + mode + ", data): data is null!");
            return -2;
        } catch (RemoteException e) {
            DElog.e(TAG, "setEffect(" + feature + ", " + mode + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
            return -1;
        }
    }

    public void updateLightSensorState(boolean sensorEnable) {
        int uid = Binder.getCallingUid();
        if (uid != 1000) {
            DElog.w(TAG, "updateLightSensorState requires SYSTEM_UID, uid=" + uid + ", pid=" + Binder.getCallingPid());
            return;
        }
        enableLightSensor(sensorEnable);
        DElog.i(TAG, "LightSensorEnable=" + sensorEnable);
    }

    public List<Bundle> getAllRecords(String name, Bundle info) {
        int uid = Binder.getCallingUid();
        if (uid == 1000) {
            return DisplayEngineDBManager.getInstance(this.mContext).getAllRecords(name, info);
        }
        DElog.w(TAG, "getAllRecords requires SYSTEM_UID, uid=" + uid + ", pid=" + Binder.getCallingPid());
        return null;
    }

    public void processSensorData(long timeInMs, int lux, int cct) {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putIntArray("Buffer", new int[]{lux, cct});
        bundle.putInt("BufferLength", 8);
        int ret = setData(9, bundle);
        if (ret != 0) {
            DElog.i(TAG, "processSensorData set Data Error: ret =" + ret);
        }
        sendAmbientColorTempToMonitor(timeInMs, lux, cct);
    }

    private void enableLightSensor(boolean enable) {
        if (this.mLightSensorEnable != enable && this.mLightSensorController != null) {
            this.mLightSensorEnable = enable;
            if (this.mLightSensorEnable) {
                this.mLightSensorController.enableSensor();
                this.mLastAmbientColorTempToMonitorTime = 0;
                return;
            }
            this.mLightSensorController.disableSensor();
        }
    }

    /* access modifiers changed from: private */
    public void initColorContentObserver() {
        if (this.mColorModeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mColorModeObserver);
        }
        this.mColorModeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                if (Settings.System.getIntForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_COLOR_MODE_SWITCH, DisplayEngineService.this.mDefaultColorModeValue, -2) == 0) {
                    DisplayEngineService.this.setScene(13, 16);
                } else {
                    DisplayEngineService.this.setScene(13, 17);
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_COLOR_MODE_SWITCH), true, this.mColorModeObserver, -2);
    }

    /* access modifiers changed from: private */
    public void initColorModeSwitch() {
        if (this.mColorModeSwitchedReceiver == null) {
            this.mColorModeSwitchedReceiver = new ColorModeSwitchedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiver(this.mColorModeSwitchedReceiver, filter, "com.huawei.android.permission.MANAGE_USERS", new Handler());
        }
        initColorContentObserver();
    }

    private void initColorModeValue() {
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_COLOR_MODE_SWITCH, this.mDefaultColorModeValue, -2) == 0) {
            setScene(13, 16);
        } else {
            setScene(13, 17);
        }
    }

    private void setDefaultColorModeValue() {
        byte[] status = new byte[1];
        if (getEffect(11, 0, status, 1) == 0) {
            this.mDefaultColorModeValue = status[0];
            DElog.i(TAG, "[effect] getEffect(DE_FEATURE_COLORMODE):" + this.mDefaultColorModeValue);
            return;
        }
        DElog.e(TAG, "[effect] getEffect(DE_FEATURE_COLORMODE):" + this.mDefaultColorModeValue);
    }

    /* access modifiers changed from: private */
    public void initNaturalToneContentObserver() {
        if (this.mNaturalToneObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mNaturalToneObserver);
        }
        this.mNaturalToneObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                if (1 == Settings.System.getIntForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_NATURAL_TONE_SWITCH, 0, -2)) {
                    DisplayEngineService.this.setScene(25, 16);
                    DElog.v(DisplayEngineService.TAG, "ContentObserver setScene, DE_ACTION_MODE_ON");
                    return;
                }
                DisplayEngineService.this.setScene(25, 17);
                DElog.v(DisplayEngineService.TAG, " ContentObserver setScene, DE_ACTION_MODE_OFF");
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_NATURAL_TONE_SWITCH), true, this.mNaturalToneObserver, -2);
    }

    /* access modifiers changed from: private */
    public void initNaturalToneSwitch() {
        if (this.mNaturalToneSwitchedReceiver == null) {
            this.mNaturalToneSwitchedReceiver = new NaturalToneSwitchedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiver(this.mNaturalToneSwitchedReceiver, filter, "com.huawei.android.permission.MANAGE_USERS", new Handler());
        }
        initNaturalToneContentObserver();
    }

    private void initNaturalToneValue() {
        if (1 == Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_NATURAL_TONE_SWITCH, 0, -2)) {
            setScene(25, 16);
            DElog.v(TAG, "initNaturalToneValue setScene, DE_ACTION_MODE_ON");
            return;
        }
        setScene(25, 17);
        DElog.v(TAG, "initNaturalToneValue setScene, DE_ACTION_MODE_OFF");
    }

    /* access modifiers changed from: private */
    public void initAutoBrightnessAdjContentObserver() {
        if (this.mAutoBrightnessAdjObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mAutoBrightnessAdjObserver);
        }
        this.mAutoBrightnessAdjObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                if (DisplayEngineService.this.mHwBrightnessSceneRecognition != null && DisplayEngineService.this.mHwBrightnessSceneRecognition.isEnable()) {
                    DisplayEngineService.this.mHwBrightnessSceneRecognition.notifyAutoBrightnessAdj();
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("hw_screen_auto_brightness_adj"), true, this.mAutoBrightnessAdjObserver, -2);
        DElog.i(TAG, "initAutoBrightnessAdjContentObserver");
    }

    /* access modifiers changed from: private */
    public void initAutoBrightnessAdjSwitch() {
        if (this.mAutoBrightnessAdjSwitchedReceiver == null) {
            this.mAutoBrightnessAdjSwitchedReceiver = new AutoBrightnessAdjSwitchedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiver(this.mAutoBrightnessAdjSwitchedReceiver, filter, "com.huawei.android.permission.MANAGE_USERS", new Handler());
        }
        DElog.i(TAG, "initAutoBrightnessAdjSwitch");
    }

    /* access modifiers changed from: private */
    public int setDetailPGscene(String pkg) {
        String pkgName = pkg;
        int scene = 0;
        if (pkg == null) {
            DElog.i(TAG, "pkg is null");
            return -1;
        } else if (!this.mPGEnable) {
            DElog.i(TAG, "mPGEnable false");
            return -1;
        } else if (this.mPGSdk == null) {
            DElog.i(TAG, "mPGSdk is null");
            return -1;
        } else {
            try {
                DElog.d(TAG, "getPkgType, pkgName: " + pkgName);
                scene = this.mPGSdk.getPkgType(this.mContext, pkgName);
            } catch (RemoteException ex) {
                DElog.e(TAG, "getPkgType", ex);
            }
            DElog.d(TAG, "PGSdk getPkgType, scene result:" + scene);
            return setScene(17, scene);
        }
    }

    /* access modifiers changed from: private */
    public int setDetailPGscene() {
        String pkgName = getCurrentTopAppName();
        int scene = 0;
        if (pkgName == null) {
            DElog.i(TAG, "getCurrentTopAppName is null");
            return -1;
        } else if (!this.mPGEnable) {
            DElog.i(TAG, "mPGEnable false");
            return -1;
        } else if (this.mPGSdk == null) {
            DElog.i(TAG, "mPGSdk is null");
            return -1;
        } else {
            try {
                DElog.d(TAG, "getPkgType,pkgName: " + pkgName);
                scene = this.mPGSdk.getPkgType(this.mContext, pkgName);
            } catch (RemoteException ex) {
                DElog.e(TAG, "getPkgType", ex);
            }
            DElog.d(TAG, "PGSdk getPkgType, scene result:" + scene);
            return setScene(17, scene);
        }
    }

    /* access modifiers changed from: private */
    public int setDetailIawareScene() {
        String pkgName = getCurrentTopAppName();
        if (pkgName == null) {
            DElog.i(TAG, "getCurrentTopAppName is null");
            return -1;
        } else if (this.mPGEnable) {
            DElog.i(TAG, "mPGEnable true");
            return -1;
        } else {
            int scene = DevSchedFeatureRT.getAppTypeForLCD(pkgName);
            DElog.d(TAG, "getFrom iaware ,pkgName:" + pkgName + ",getAppType: " + scene);
            PersistableBundle data = new PersistableBundle();
            data.putInt("Scene", scene);
            data.putInt("PowerLevel", -1);
            return setData(10, data);
        }
    }

    private void handleIawareSpecialScene(int type, PersistableBundle data) {
        if (type == 10) {
            int scene = data.getInt("Scene");
            DElog.d(TAG, " Scene is " + scene);
            if (scene == 255) {
                String pkgName = getCurrentTopAppName();
                if (pkgName == null) {
                    DElog.i(TAG, "getCurrentTopAppName is null");
                    return;
                }
                DElog.d(TAG, "pkgName is " + pkgName);
                if (pkgName.equals("com.huawei.mmitest")) {
                    setScene(35, 16);
                    DElog.d(TAG, "setScene (35,16) OK!");
                }
            } else if (scene == 1002) {
                if (this.mHwBrightnessSceneRecognition != null && this.mHwBrightnessSceneRecognition.isEnable()) {
                    this.mHwBrightnessSceneRecognition.setVideoPlayStatus(true);
                    DElog.d(TAG, "video start.");
                }
            } else if (scene == 1003 && this.mHwBrightnessSceneRecognition != null && this.mHwBrightnessSceneRecognition.isEnable()) {
                this.mHwBrightnessSceneRecognition.setVideoPlayStatus(false);
                DElog.d(TAG, "video end.");
            }
        }
    }

    private String getCurrentTopAppName() {
        try {
            List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (runningTasks != null) {
                if (!runningTasks.isEmpty()) {
                    return runningTasks.get(0).topActivity.getPackageName();
                }
            }
            return null;
        } catch (SecurityException e) {
            DElog.e(TAG, "getCurrentTopAppName() failed to get topActivity PackageName " + e);
            return null;
        }
    }

    private void initPGSdkState() {
        if (!this.mPGEnable && !this.mNeedPkgNameFromPG) {
            DElog.i(TAG, "mPGEnable false");
        } else if (this.mPGSdk == null) {
            DElog.i(TAG, "mPGSdk is null");
        } else if (this.mStateRecognitionListener == null) {
            DElog.i(TAG, "mStateRecognitionListener is null");
        } else {
            try {
                DElog.d(TAG, "enableStateEvent step in!");
                Integer num = 10000;
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, num.intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_INPUT_START).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_INPUT_END).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_MMS_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_VIDEO_START).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_VIDEO_END).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_CAMERA_END).intValue());
            } catch (RemoteException ex) {
                DElog.e(TAG, "enableStateEvent", ex);
            }
        }
    }

    /* access modifiers changed from: private */
    public void registerPGSdk() {
        if (this.mPGEnable || this.mNeedPkgNameFromPG) {
            if (this.mPGSdk == null) {
                DElog.d(TAG, "mPGSdk constructor ok");
                this.mPGSdk = PGSdk.getInstance();
            }
            if (this.mStateRecognitionListener == null) {
                DElog.d(TAG, "mStateRecognitionListener constructor ok");
                this.mStateRecognitionListener = new PGSdk.Sink() {
                    public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                        DElog.d(DisplayEngineService.TAG, "state type: " + stateType + " eventType:" + eventType + " pid:" + pid + " pkd:" + pkg + " uid:" + uid);
                        if (DisplayEngineService.this.mHwBrightnessSceneRecognition != null && DisplayEngineService.this.mHwBrightnessSceneRecognition.isEnable() && DisplayEngineService.this.mNeedPkgNameFromPG && eventType == 1) {
                            if (pkg != null && pkg.length() > 0) {
                                DisplayEngineService.this.mHwBrightnessSceneRecognition.notifyTopApkChange(pkg);
                            }
                            DElog.i(DisplayEngineService.TAG, "PG pkg:" + pkg);
                        }
                        if (!DisplayEngineService.this.mPGEnable) {
                            return;
                        }
                        if (stateType == 10000) {
                            int unused = DisplayEngineService.this.setDetailPGscene(pkg);
                        } else if (stateType == 10016 || stateType == 10017) {
                            DisplayEngineService.this.setScene(0, stateType);
                            int unused2 = DisplayEngineService.this.setDetailPGscene(pkg);
                        } else {
                            DisplayEngineService.this.setScene(0, stateType);
                        }
                    }
                };
            }
            initPGSdkState();
            return;
        }
        DElog.i(TAG, "mPGEnable false");
    }

    private void sendUIScene() {
        setScene(10, 16);
        this.mScreenOn = true;
    }

    private void setDefaultConfigValue() {
        this.mPGEnable = true;
        this.mNeedPkgNameFromPG = false;
    }

    private void getConfigParam() {
        try {
            if (!getConfig()) {
                DElog.e(TAG, "getConfig failed!");
                setDefaultConfigValue();
            }
        } catch (IOException e) {
            DElog.e(TAG, "getConfig failed setDefaultConfigValue!");
            setDefaultConfigValue();
        }
        DElog.d(TAG, "mPGEnable :" + this.mPGEnable);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0046, code lost:
        if (r2 == null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0048, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004d, code lost:
        if (r2 == null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0051, code lost:
        if (r2 == null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0054, code lost:
        return false;
     */
    private boolean getConfig() throws IOException {
        DElog.i(TAG, "getConfig");
        File xmlFile = HwCfgFilePolicy.getCfgFile(SR_CONTROL_XML_FILE, 0);
        if (xmlFile == null) {
            DElog.w(TAG, "get xmlFile :/display/effect/displayengine/SR_control.xml failed!");
            return false;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(xmlFile);
            if (!getConfigFromXML(inputStream)) {
                DElog.i(TAG, "get xmlFile error");
                inputStream.close();
                inputStream.close();
                return false;
            }
            inputStream.close();
            inputStream.close();
            return true;
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
        } catch (Exception e3) {
        } catch (Throwable th) {
            if (inputStream != null) {
                inputStream.close();
            }
            throw th;
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        DElog.i(TAG, "getConfigFromeXML");
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType != 1) {
                    switch (eventType) {
                        case 2:
                            String name = parser.getName();
                            if (!name.equals("SRControl")) {
                                if (!name.equals("PGEnable")) {
                                    if (!name.equals("NeedPkgNameFromPG")) {
                                        if (!name.equals("MinimumTrainingIntervalMinutes")) {
                                            if (!name.equals("ChargeLevelThreshold")) {
                                                if (name.equals("NewDragNumThreshold")) {
                                                    this.mNewDragNumThreshold = Integer.parseInt(parser.nextText());
                                                    DElog.i(TAG, "mNewDragNumThreshold = " + this.mNewDragNumThreshold);
                                                    break;
                                                }
                                            } else {
                                                this.mChargeLevelThreshold = Integer.parseInt(parser.nextText());
                                                DElog.i(TAG, "mChargeLevelThreshold = " + this.mChargeLevelThreshold);
                                                break;
                                            }
                                        } else {
                                            this.mMinimumTrainingIntervalMillis = AppHibernateCst.DELAY_ONE_MINS * ((long) Integer.parseInt(parser.nextText()));
                                            DElog.i(TAG, "mMinimumTrainingIntervalMillis = " + this.mMinimumTrainingIntervalMillis);
                                            break;
                                        }
                                    } else {
                                        this.mNeedPkgNameFromPG = Boolean.parseBoolean(parser.nextText());
                                        break;
                                    }
                                } else {
                                    this.mPGEnable = Boolean.parseBoolean(parser.nextText());
                                    break;
                                }
                            } else {
                                configGroupLoadStarted = true;
                                break;
                            }
                            break;
                        case 3:
                            if (parser.getName().equals("SRControl") && configGroupLoadStarted) {
                                loadFinished = true;
                                configGroupLoadStarted = false;
                                break;
                            }
                    }
                    if (!loadFinished) {
                        eventType = parser.next();
                    }
                }
            }
            if (loadFinished) {
                DElog.i(TAG, "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            DElog.e(TAG, "get xmlFile error: " + e);
        } catch (IOException e2) {
            DElog.e(TAG, "get xmlFile error: " + e2);
        } catch (NumberFormatException e3) {
            DElog.e(TAG, "get xmlFile error: " + e3);
        } catch (Exception e4) {
            DElog.e(TAG, "get xmlFile error: " + e4);
        }
        DElog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    private void sendAmbientColorTempToMonitor(long time, int lux, int colorTemp) {
        if (this.mDisplayEffectMonitor != null) {
            if (this.mLastAmbientColorTempToMonitorTime == 0 || time <= this.mLastAmbientColorTempToMonitorTime) {
                this.mLastAmbientColorTempToMonitorTime = time;
                return;
            }
            int durationInMs = (int) (time - this.mLastAmbientColorTempToMonitorTime);
            this.mLastAmbientColorTempToMonitorTime = time;
            if (colorTemp > 0 && lux > 50) {
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "ambientColorTempCollection");
                params.put("colorTempValue", Integer.valueOf(colorTemp));
                params.put("durationInMs", Integer.valueOf(durationInMs));
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }
}
