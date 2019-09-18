package com.android.server.camera;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.media.AudioManagerEx;
import com.huawei.android.media.IAudioModeCallback;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.TagID;

public class HwCameraAutoImpl {
    private static final String ACTION_CAMERA_DESCEND = "com.huawei.camera.action.CAMERA_DESCEND";
    private static final String ACTION_CAMERA_RISE = "com.huawei.camera.action.CAMERA_RISE";
    private static final String ACTIVITY_NOTIFY_COMPONENTNAME = "comp";
    private static final String ACTIVITY_NOTIFY_ONPAUSE = "onPause";
    private static final String ACTIVITY_NOTIFY_ONRESUME = "onResume";
    private static final String ACTIVITY_NOTIFY_REASON = "activityLifeState";
    private static final String ACTIVITY_NOTIFY_STATE = "state";
    private static final String AUTO_CAMERA_STATUS_PERMISSION = "com.huawei.camera.permission.AUTO_CAMERA_STATUS";
    private static final int CAMERA_LIFT_SOUNDS_DISABLED = 0;
    private static final int CAMERA_LIFT_SOUNDS_ENABLED = 1;
    private static final String CAMERA_LIFT_SOUNDS_KEY = "camera_lift_sounds_enabled";
    private static final String CAMERA_LIFT_SOUND_FILE = "/product/media/audio/camera/camera_lift.ogg";
    private static final int CAMERA_STATE_ENTER_GALLERY = 100;
    private static final int DELAY_TIME = 5000;
    private static final int DELAY_TIME_DEFAULT = 0;
    private static final int DELAY_TIME_THIRDAPP = 220;
    private static final int DP_MARGIN_TOP = 40;
    private static final String FALSE = "false";
    private static final Object HANDLER_THREAD_SYNC_OBJ = new Object();
    private static final String HW_CAMERA_MOCK = "HW.Camera.Mock";
    private static final String HW_CAMERA_NAME = "com.huawei.camera";
    private static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final int LENGTH = 2;
    private static final int MEDIUM = 160;
    private static final String MOTION_THREAD_NAME = "motionThread";
    private static final int MOTOR_CONTROL_DESCEND = 1;
    private static final int MOTOR_CONTROL_RISE = 0;
    private static final int RES_CLOSE_EMERGENCY = 109;
    private static final int RES_CLOSE_EMERGENCY_FAIL = 106;
    private static final int RES_CLOSE_EMERGENCY_SUCCESS = 105;
    private static final int RES_CLOSE_FAIL = 104;
    private static final int RES_CLOSE_SUCCESS = 103;
    private static final int RES_EVENT_PRESS = 107;
    private static final int RES_OPEN_FAIL = 102;
    private static final int RES_OPEN_SUCCESS = 101;
    private static final int RES_REQUEST_OVER_FREQ = 108;
    private static final int RES_START_CLOSE = 111;
    private static final int RES_START_OPEN = 110;
    private static final int RES_UNKNOWN = 100;
    private static final String STRATEGY_ACTIVITY_AND_NO_CLOSE = "2";
    private static final String STRATEGY_ACTIVITY_AND_ORIGIN = "1";
    private static final String STRATEGY_DELAY_CLOSE = "5";
    private static final String STRATEGY_NO_HANDLE = "0";
    private static final String STRATEGY_VIDEO_CALL = "3";
    private static final String STRATEGY_VIDEO_PHONE = "4";
    private static final String STRING_VALUE_DEFAULT = "";
    private static final String TAG = "HwCameraAutoImpl";
    private static final String TEST_FILE_NAME = "/data/cameratest.txt";
    private static final int TOAST_TEXT_SIZE = 13;
    private static final String TRUE = "true";
    private static final String WHITELIST_FILE_NAME = "/product/etc/camera/camera_whitelist.json";
    private BroadcastReceiver coverStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.w(HwCameraAutoImpl.TAG, "onReceive, the intent is null!");
                return;
            }
            Slog.w(HwCameraAutoImpl.TAG, "onReceive srceen off or home");
            HwCameraAutoImpl.this.dismissSlidedownTip();
            HwCameraAutoImpl.this.dismissCoverDialog();
            if (HwCameraAutoImpl.this.isEnterGallery) {
                HwCameraAutoImpl.this.unRegisterPopDeviceListener();
                HwCameraAutoImpl.this.registerPopDeviceListener(1, 0);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean isEnterGallery = false;
    /* access modifiers changed from: private */
    public boolean isFirstCoverPopup = true;
    /* access modifiers changed from: private */
    public boolean isFrontCameraOpen = false;
    private boolean isScreenOffBroadcastRegisterd = false;
    private boolean isShutDownBroadcastRegisterd = false;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        public void call(Bundle extras) {
            if (extras == null) {
                Slog.e(HwCameraAutoImpl.TAG, "AMS callback, extras is null");
                return;
            }
            ComponentName componentName = (ComponentName) extras.getParcelable(HwCameraAutoImpl.ACTIVITY_NOTIFY_COMPONENTNAME);
            String className = componentName != null ? componentName.getClassName() : "";
            if (componentName != null) {
                String packageName = componentName.getPackageName();
            }
            String state = extras.getString(HwCameraAutoImpl.ACTIVITY_NOTIFY_STATE);
            if (HwCameraAutoImpl.this.mFrontCameraActivity.equals(className)) {
                String strategy = HwCameraAutoImpl.this.getStrategyByActivity(className);
                if (!"1".equals(strategy) && !"2".equals(strategy)) {
                    return;
                }
                if (HwCameraAutoImpl.ACTIVITY_NOTIFY_ONPAUSE.equals(state)) {
                    HwCameraAutoImpl.this.registerPopDeviceListener(1, 220);
                    HwCameraAutoImpl.this.dismissSlidedownTip();
                    HwCameraAutoImpl.this.dismissCoverDialog();
                    return;
                }
                if (HwCameraAutoImpl.this.isFrontCameraOpen && HwCameraAutoImpl.ACTIVITY_NOTIFY_ONRESUME.equals(state)) {
                    HwCameraAutoImpl.this.unRegisterPopDeviceListener();
                    HwCameraAutoImpl.this.registerPopDeviceListener(0, 220);
                }
            }
        }
    };
    private AudioManager mAudioManager = null;
    private AudioManagerEx mAudioManagerEx = null;
    private IAudioModeCallback mAudiocallback = new IAudioModeCallback() {
        public void onAudioModeChanged(int mode) {
            if (mode == 3) {
                Slog.w(HwCameraAutoImpl.TAG, "currentMode is in communication");
                HwCameraAutoImpl.this.unRegisterPopDeviceListener();
                HwCameraAutoImpl.this.registerPopDeviceListener(0, 0);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mCameraLiftSoundId = 0;
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public Dialog mCoverDialog = null;
    private Map<String, Integer> mDelayTimeMap = null;
    /* access modifiers changed from: private */
    public int mDensityDPI = 0;
    /* access modifiers changed from: private */
    public Dialog mDialog = null;
    /* access modifiers changed from: private */
    public String mFrontCameraActivity = "";
    private HWExtDeviceEventListener mHWEDListener = new HWExtDeviceEventListener() {
        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            float[] deviceValues = hwextDeviceEvent.getDeviceValues();
            if (deviceValues == null || deviceValues.length < 1) {
                Slog.w(HwCameraAutoImpl.TAG, "onDeviceDataChanged deviceValues is null");
            } else {
                HwCameraAutoImpl.this.handleMotionResult(deviceValues);
            }
        }
    };
    private HWExtDeviceManager mHWEDManager = null;
    private HWExtMotion mHWExtMotion = null;
    private Handler mHandler = null;
    private IntentFilter mIntentFilter = null;
    private Handler mMotionHandler = null;
    private HandlerThread mMotionThread = null;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(Looper.getMainLooper()) {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == 2) {
                Slog.w(HwCameraAutoImpl.TAG, "callstate is offhook");
                HwCameraAutoImpl.this.unRegisterPopDeviceListener();
                HwCameraAutoImpl.this.registerPopDeviceListener(1, 0);
            }
        }
    };
    private IntentFilter mShutDownIntentFilter = null;
    /* access modifiers changed from: private */
    public SoundPool mSoundPool = new SoundPool(1, 1, 0);
    private Map<String, List<String>> mStrategyMap = null;
    private TelephonyManager mTelephonyManager = null;
    /* access modifiers changed from: private */
    public Toast mToast = null;
    /* access modifiers changed from: private */
    public DialogInterface.OnClickListener onCoverClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -2:
                    HwCameraAutoImpl.this.dismissCoverDialog();
                    return;
                case -1:
                    boolean unused = HwCameraAutoImpl.this.isFirstCoverPopup = false;
                    HwCameraAutoImpl.this.unRegisterPopDeviceListener();
                    HwCameraAutoImpl.this.registerPopDeviceListener(0, 0);
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public DialogInterface.OnClickListener onTtyClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -2:
                    HwCameraAutoImpl.this.backToHome();
                    HwCameraAutoImpl.this.dismissSlidedownTip();
                    return;
                case -1:
                    HwCameraAutoImpl.this.unRegisterPopDeviceListener();
                    HwCameraAutoImpl.this.registerPopDeviceListener(0, 0);
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver shutDownStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.w(HwCameraAutoImpl.TAG, "onReceive, the intent is null!");
                return;
            }
            Slog.w(HwCameraAutoImpl.TAG, "onReceive shutdown");
            HwCameraAutoImpl.this.unRegisterPopDeviceListener();
            HwCameraAutoImpl.this.registerPopDeviceListener(1, 0);
        }
    };

    public HwCameraAutoImpl(Context context) {
        if (context == null) {
            Slog.e(TAG, "context is null");
            return;
        }
        this.mContext = context;
        this.mCameraLiftSoundId = this.mSoundPool.load(CAMERA_LIFT_SOUND_FILE, 1);
        this.mHWEDManager = HWExtDeviceManager.getInstance(this.mContext);
        this.mHWExtMotion = new HWExtMotion(3100);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mAudioManagerEx = new AudioManagerEx();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mDensityDPI = this.mContext.getResources().getDisplayMetrics().densityDpi;
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mIntentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mShutDownIntentFilter = new IntentFilter();
        this.mShutDownIntentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast unused = HwCameraAutoImpl.this.mToast = new Toast(HwCameraAutoImpl.this.mContext.getApplicationContext());
                HwCameraAutoImpl.this.initToast(HwCameraAutoImpl.this.mDensityDPI);
                HwCameraAutoImpl.this.mToast.setDuration(0);
            }
        });
        initWhiteList();
        registerActivityNotifier();
    }

    private void initWhiteList() {
        int i = 0;
        String content = new ReadFileInfo(WHITELIST_FILE_NAME, false).readFileToString();
        if (!content.isEmpty()) {
            try {
                JSONArray strategyArray = new JSONArray(content);
                int strategyNumber = strategyArray.length();
                this.mStrategyMap = new HashMap(strategyNumber);
                JSONArray delayTimeArray = null;
                int i2 = 0;
                while (i2 < strategyNumber) {
                    JSONObject strategyObject = strategyArray.getJSONObject(i2);
                    String strategyId = strategyObject.getString(HwDevicePolicyManagerServiceUtil.EXCHANGE_ID);
                    JSONArray activityArray = strategyObject.getJSONArray("activity");
                    int activityNumber = activityArray == null ? i : activityArray.length();
                    if ("5".equals(strategyId)) {
                        delayTimeArray = strategyObject.optJSONArray("delaytime");
                        this.mDelayTimeMap = new HashMap(activityNumber);
                    }
                    List<String> whiteList = new ArrayList<>(activityNumber);
                    for (int j = i; j < activityNumber; j++) {
                        whiteList.add(activityArray.getString(j));
                        if (!(this.mDelayTimeMap == null || delayTimeArray == null)) {
                            this.mDelayTimeMap.put(activityArray.getString(j), Integer.valueOf(delayTimeArray.optInt(j)));
                        }
                    }
                    this.mStrategyMap.put(strategyId, whiteList);
                    i2++;
                    i = 0;
                }
            } catch (JSONException e) {
                Slog.e(TAG, "parase camera json throw exception " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    public void initToast(int densityDpi) {
        this.mToast.setView(((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(34013375, null));
        this.mToast.setGravity(49, 0, (densityDpi / 160) * 40);
    }

    private void showToast(final String text) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (HwCameraAutoImpl.this.mToast != null) {
                    int densityDpi = HwCameraAutoImpl.this.mContext.getResources().getDisplayMetrics().densityDpi;
                    if (densityDpi != HwCameraAutoImpl.this.mDensityDPI) {
                        int unused = HwCameraAutoImpl.this.mDensityDPI = densityDpi;
                        HwCameraAutoImpl.this.initToast(HwCameraAutoImpl.this.mDensityDPI);
                    }
                    TextView tv = (TextView) HwCameraAutoImpl.this.mToast.getView().findViewById(34603183);
                    tv.setText(text);
                    tv.setTextSize(13.0f);
                    HwCameraAutoImpl.this.mToast.show();
                }
            }
        });
    }

    private void registerBroadcast() {
        if (!this.isScreenOffBroadcastRegisterd) {
            this.mContext.registerReceiver(this.coverStateReceiver, this.mIntentFilter);
            this.isScreenOffBroadcastRegisterd = true;
        }
        if (!this.isShutDownBroadcastRegisterd) {
            this.mContext.registerReceiver(this.shutDownStateReceiver, this.mShutDownIntentFilter);
            this.isShutDownBroadcastRegisterd = true;
        }
    }

    /* access modifiers changed from: private */
    public void unregisterBroadcast() {
        if (this.isScreenOffBroadcastRegisterd) {
            try {
                this.mContext.unregisterReceiver(this.coverStateReceiver);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "Unregister coverStateReceiver fail");
            }
            this.isScreenOffBroadcastRegisterd = false;
        }
        if (this.isShutDownBroadcastRegisterd) {
            try {
                this.mContext.unregisterReceiver(this.shutDownStateReceiver);
            } catch (IllegalArgumentException e2) {
                Slog.e(TAG, "Unregister shutDownStateReceiver fail");
            }
            this.isShutDownBroadcastRegisterd = false;
        }
    }

    /* access modifiers changed from: private */
    public void backToHome() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(268435456);
        if (this.mContext != null) {
            this.mContext.getApplicationContext().startActivity(intent);
        }
    }

    private void showDialog() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Dialog unused = HwCameraAutoImpl.this.mDialog = new AlertDialog.Builder(HwCameraAutoImpl.this.mContext.getApplicationContext(), 33947691).setMessage(33685977).setPositiveButton(33686060, HwCameraAutoImpl.this.onTtyClickListener).setNegativeButton(33686059, HwCameraAutoImpl.this.onTtyClickListener).create();
                HwCameraAutoImpl.this.mDialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS);
                WindowManager.LayoutParams attrs = HwCameraAutoImpl.this.mDialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                HwCameraAutoImpl.this.mDialog.getWindow().setAttributes(attrs);
                HwCameraAutoImpl.this.mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == 4) {
                            Slog.d(HwCameraAutoImpl.TAG, "onKeyBack");
                            HwCameraAutoImpl.this.backToHome();
                            HwCameraAutoImpl.this.dismissSlidedownTip();
                        }
                        return false;
                    }
                });
                if (HwCameraAutoImpl.this.mDialog != null && HwCameraAutoImpl.this.mDialog.isShowing()) {
                    HwCameraAutoImpl.this.mDialog.dismiss();
                }
                HwCameraAutoImpl.this.mDialog.show();
            }
        });
    }

    /* access modifiers changed from: private */
    public void dismissSlidedownTip() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (HwCameraAutoImpl.this.mDialog != null && HwCameraAutoImpl.this.mDialog.isShowing()) {
                    Slog.w(HwCameraAutoImpl.TAG, "dismiss mDialog");
                    HwCameraAutoImpl.this.mDialog.dismiss();
                    HwCameraAutoImpl.this.unregisterBroadcast();
                }
            }
        });
    }

    private void showCoverDialog() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Dialog unused = HwCameraAutoImpl.this.mCoverDialog = new AlertDialog.Builder(HwCameraAutoImpl.this.mContext.getApplicationContext(), 33947691).setMessage(33686003).setPositiveButton(33686009, HwCameraAutoImpl.this.onCoverClickListener).setNegativeButton(33686059, HwCameraAutoImpl.this.onCoverClickListener).create();
                HwCameraAutoImpl.this.mCoverDialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_UNBIND_SUCCESS);
                WindowManager.LayoutParams attrs = HwCameraAutoImpl.this.mCoverDialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                HwCameraAutoImpl.this.mCoverDialog.getWindow().setAttributes(attrs);
                HwCameraAutoImpl.this.mCoverDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == 4) {
                            HwCameraAutoImpl.this.backToHome();
                            HwCameraAutoImpl.this.dismissCoverDialog();
                        }
                        return false;
                    }
                });
                if (HwCameraAutoImpl.this.mCoverDialog != null && HwCameraAutoImpl.this.mCoverDialog.isShowing()) {
                    HwCameraAutoImpl.this.mCoverDialog.dismiss();
                }
                HwCameraAutoImpl.this.mCoverDialog.show();
            }
        });
    }

    /* access modifiers changed from: private */
    public void dismissCoverDialog() {
        this.isFirstCoverPopup = true;
        this.mHandler.post(new Runnable() {
            public void run() {
                if (HwCameraAutoImpl.this.mCoverDialog != null && HwCameraAutoImpl.this.mCoverDialog.isShowing()) {
                    Slog.w(HwCameraAutoImpl.TAG, "dismiss mCoverDialog");
                    HwCameraAutoImpl.this.mCoverDialog.dismiss();
                    HwCameraAutoImpl.this.unregisterBroadcast();
                }
            }
        });
    }

    private void sendDeviceStatusBroadcast(String flag) {
        Slog.d(TAG, "sendDeviceStatusBroadcast " + flag);
        Intent intent = new Intent();
        intent.setAction(flag);
        intent.setPackage("com.huawei.camera");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, AUTO_CAMERA_STATUS_PERMISSION);
    }

    /* access modifiers changed from: private */
    public void handleMotionResult(float[] deviceValues) {
        int result = (int) deviceValues[0];
        Slog.d(TAG, "Motion Result = " + result);
        switch (result) {
            case 101:
                sendDeviceStatusBroadcast(ACTION_CAMERA_RISE);
                return;
            case 102:
                if (!this.isFirstCoverPopup) {
                    showToast(this.mContext.getResources().getString(33685978));
                    break;
                } else {
                    showCoverDialog();
                    break;
                }
            case 103:
                unregisterBroadcast();
                break;
            case 104:
                showToast(this.mContext.getResources().getString(33685976));
                break;
            case 107:
                playRing();
                backToHome();
                break;
            case 108:
                if (deviceValues.length >= 2) {
                    int timeSecond = (int) deviceValues[1];
                    showToast(this.mContext.getResources().getQuantityString(34406410, timeSecond, new Object[]{Integer.valueOf(timeSecond)}));
                    break;
                }
                break;
            case 109:
                playRing();
                showDialog();
                break;
            case 110:
            case 111:
                playRing();
                break;
        }
        sendDeviceStatusBroadcast(ACTION_CAMERA_DESCEND);
    }

    private void testHandleResult() {
        String mockValueStr = new ReadFileInfo(TEST_FILE_NAME, true).readFileToString();
        if (!mockValueStr.isEmpty()) {
            float[] deviceValuesTest = new float[2];
            try {
                String[] temps = mockValueStr.split(",");
                if (temps.length == 1) {
                    deviceValuesTest[0] = Float.parseFloat(temps[0]);
                } else if (temps.length == 2) {
                    deviceValuesTest[0] = Float.parseFloat(temps[0]);
                    deviceValuesTest[1] = Float.parseFloat(temps[1]);
                } else {
                    return;
                }
                handleMotionResult(deviceValuesTest);
            } catch (NumberFormatException e) {
                Slog.e(TAG, "NumberFormatException when read the mock file");
            }
        }
    }

    private void initMotionHandler() {
        synchronized (HANDLER_THREAD_SYNC_OBJ) {
            if (this.mMotionThread == null) {
                this.mMotionThread = new HandlerThread(MOTION_THREAD_NAME);
                this.mMotionThread.start();
                this.mMotionHandler = new Handler(this.mMotionThread.getLooper());
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private String getForegroundActivityName() {
        long identityToken = Binder.clearCallingIdentity();
        try {
            ActivityInfo info = ActivityManagerEx.getLastResumedActivity();
            Binder.restoreCallingIdentity(identityToken);
            String activityName = "";
            if (info != null) {
                activityName = info.name;
            }
            Slog.d(TAG, "foreground activity name is " + activityName);
            return activityName;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    private void listenPhoneState(int state) {
        long identityToken = Binder.clearCallingIdentity();
        try {
            this.mTelephonyManager.listen(this.mPhoneStateListener, state);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void registerActivityNotifier() {
        long identityToken = Binder.clearCallingIdentity();
        try {
            ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, ACTIVITY_NOTIFY_REASON);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    /* access modifiers changed from: private */
    public void registerPopDeviceListener(int cmd, int time) {
        if (TRUE.equals(SystemProperties.get(HW_CAMERA_MOCK, FALSE))) {
            testHandleResult();
        } else if (this.mHWEDManager == null || this.mHWExtMotion == null) {
            Slog.w(TAG, "mHWEDManager or mHWExtMotion is null,register return");
        } else {
            if (cmd == 0) {
                registerBroadcast();
            }
            this.mHWExtMotion.setHWExtDeviceAction(cmd);
            this.mHWExtMotion.setHWExtDeviceDelay(time);
            initMotionHandler();
            boolean isRegistered = this.mHWEDManager.registerDeviceListener(this.mHWEDListener, this.mHWExtMotion, this.mMotionHandler);
            if (HW_DEBUG) {
                Slog.d(TAG, "registerPopDeviceListener cmd = " + cmd + " result = " + isRegistered + " isEnterGallery = " + this.isEnterGallery + " delaytime = " + time);
            }
        }
    }

    /* access modifiers changed from: private */
    public void unRegisterPopDeviceListener() {
        if (this.mHWEDManager == null || this.mHWExtMotion == null) {
            Slog.w(TAG, "mHWEDManager or mHWExtMotion is null, unregister return");
            return;
        }
        this.mHWEDManager.unregisterDeviceListener(this.mHWEDListener, this.mHWExtMotion);
        this.isEnterGallery = false;
        Slog.d(TAG, "unRegisterPopDeviceListener: unregisterDeviceListener");
    }

    /* access modifiers changed from: private */
    public String getStrategyByActivity(String activity) {
        if (this.mStrategyMap == null || this.mStrategyMap.size() == 0) {
            Slog.w(TAG, "getStrategyByActivity failed, mStrategyMap is null or size is 0");
            return "";
        }
        for (String strategy : this.mStrategyMap.keySet()) {
            List<String> whiteList = this.mStrategyMap.get(strategy);
            if (whiteList != null && whiteList.contains(activity)) {
                return strategy;
            }
        }
        return "";
    }

    private int getDelayTimeByActivity(String activity) {
        if (this.mDelayTimeMap == null || this.mDelayTimeMap.size() == 0) {
            Slog.w(TAG, "getDelayTimeByActivity failed, mDelayTimeMap is null or size is 0");
            return 220;
        }
        int delayTime = this.mDelayTimeMap.get(activity).intValue();
        if (delayTime <= 0 || delayTime > DELAY_TIME) {
            delayTime = 220;
        }
        return delayTime;
    }

    private void runPopupCmd(int newCameraState, int facing, int delaytime) {
        if (facing == 1) {
            if (newCameraState == 0) {
                unRegisterPopDeviceListener();
                registerPopDeviceListener(0, delaytime);
            } else if (newCameraState == 3) {
                registerPopDeviceListener(1, this.isEnterGallery ? DELAY_TIME : delaytime);
                try {
                    this.mAudioManagerEx.unregisterAudioModeCallback(this.mAudiocallback);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Unregister AudioModeCallback fail");
                }
            }
        }
    }

    private void handleCameraState(int newCameraState, int facing, String clientName, String strategy, String activity) {
        if (newCameraState == 0 || newCameraState == 3) {
            int i = 0;
            int delayTime = "com.huawei.camera".equals(clientName) ? 0 : 220;
            boolean isRunPopupCmd = true;
            char c = 65535;
            switch (strategy.hashCode()) {
                case TagID.TAG_S3_SATURATION /*49*/:
                    if (strategy.equals("1")) {
                        c = 0;
                        break;
                    }
                    break;
                case 50:
                    if (strategy.equals("2")) {
                        c = 1;
                        break;
                    }
                    break;
                case TagID.TAG_S3_SKIN_GAIN /*51*/:
                    if (strategy.equals("3")) {
                        c = 2;
                        break;
                    }
                    break;
                case TagID.TAG_HBM_PARAMETER /*52*/:
                    if (strategy.equals("4")) {
                        c = 3;
                        break;
                    }
                    break;
                case TagID.TAG_COUNT /*53*/:
                    if (strategy.equals("5")) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 1:
                    isRunPopupCmd = this.isFrontCameraOpen;
                    if (newCameraState == 0 && facing != 1) {
                        registerPopDeviceListener(1, 220);
                        isRunPopupCmd = false;
                        break;
                    }
                case 2:
                    if (this.isFrontCameraOpen) {
                        if (this.mAudioManager.getMode() != 3) {
                            this.mAudioManagerEx.registerAudioModeCallback(this.mAudiocallback, null);
                            isRunPopupCmd = false;
                            break;
                        } else {
                            Slog.w(TAG, "CurrentMode is already in communication");
                            break;
                        }
                    }
                    break;
                case 3:
                    if (this.isFrontCameraOpen) {
                        i = 32;
                    }
                    listenPhoneState(i);
                    break;
                case 4:
                    delayTime = this.isFrontCameraOpen ? delayTime : getDelayTimeByActivity(activity);
                    break;
            }
            if (isRunPopupCmd) {
                runPopupCmd(newCameraState, facing, delayTime);
            }
        }
    }

    public void updateActivityCount(String cameraId, int newCameraState, int facing, String clientName) {
        if (HW_DEBUG) {
            Slog.d(TAG, "clientName is " + clientName + " camerastate is " + newCameraState + " facing is " + facing);
        }
        String currentActivity = getForegroundActivityName();
        String strategy = getStrategyByActivity(currentActivity);
        if (HW_DEBUG) {
            Slog.d(TAG, "updateActivityCount strategy is " + strategy);
        }
        if (!"0".equals(strategy)) {
            if (facing == 1) {
                if (newCameraState == 0) {
                    this.isFrontCameraOpen = true;
                    this.mFrontCameraActivity = currentActivity;
                } else if (newCameraState == 3) {
                    this.isFrontCameraOpen = false;
                    dismissSlidedownTip();
                    dismissCoverDialog();
                } else if (newCameraState == 100) {
                    this.isEnterGallery = true;
                }
            }
            handleCameraState(newCameraState, facing, clientName, strategy, currentActivity);
        }
    }

    /* access modifiers changed from: protected */
    public void playRing() {
        this.mHandler.post(new Runnable() {
            public void run() {
                int currentUserId = ActivityManager.getCurrentUser();
                Slog.d(HwCameraAutoImpl.TAG, "playSlideSound currentUser = " + currentUserId + " soundId = " + HwCameraAutoImpl.this.mCameraLiftSoundId);
                if (Settings.System.getIntForUser(HwCameraAutoImpl.this.mContext.getContentResolver(), HwCameraAutoImpl.CAMERA_LIFT_SOUNDS_KEY, 0, currentUserId) == 1) {
                    HwCameraAutoImpl.this.mSoundPool.play(HwCameraAutoImpl.this.mCameraLiftSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                }
            }
        });
    }
}
