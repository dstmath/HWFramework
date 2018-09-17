package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.util.ArrayList;
import java.util.List;

public class HwBrightnessPgSceneDetection {
    private static final boolean DEBUG;
    private static final int LOW_LIGHT_MAX = 55;
    private static final int LOW_LIGHT_MIN = 20;
    private static final int LOW_LIGHT_REFERENCE_RATIO = 30;
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int MINDEFAULTBRIGHTNESS = 4;
    private static final int MSG_REG_POWER_MODE_LISTENER = 2;
    private static final int MSG_UPDATE_PGBRIGHTNESS = 1;
    private static final int PG_DEFAULT_MODE = 2;
    private static final String PG_EXTREME_MODE_ACTION = "huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION";
    private static final int PG_POWER_SAVE_MODE = 1;
    private static final float PG_POWER_SAVE_MODE_LIGHT_RATIO = 0.56f;
    private static final int PG_SUPER_POWER_SAVE_MODE = 4;
    private static final String POWERMODE_PROP = "persist.sys.smart_power";
    private static final String POWER_MODE_CHANGED_ACTION = "huawei.intent.action.POWER_MODE_CHANGED_ACTION";
    private static final int POWER_MODE_MIN_LIGHT_VAL = 25;
    private static final int SUPER_MODE_MAX_LIGHT_VAL = 35;
    private static final String TAG = "HwBrightnessPgSceneDetection";
    private int mAppType = -1;
    private List<Integer> mBacklightAppList = null;
    private HwBrightnessPgSceneDetectionCallbacks mCallbacks;
    private final Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwBrightnessPgSceneDetection.this.mCallbacks.updateStateRecognition(HwBrightnessPgSceneDetection.this.mShouldUpdateBLight, HwBrightnessPgSceneDetection.this.mAppType);
                    return;
                case 2:
                    HwBrightnessPgSceneDetection.this.registerPowerModeChangedListener();
                    return;
                default:
                    Slog.e(HwBrightnessPgSceneDetection.TAG, "Invalid message");
                    return;
            }
        }
    };
    private boolean mPGBLListenerRegisted = false;
    private PGSdk mPGSdk = null;
    private BroadcastReceiver mPgModeChangedBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(HwBrightnessPgSceneDetection.TAG, "onReceive() intent is NULL!");
                return;
            }
            String action = intent.getAction();
            if (HwBrightnessPgSceneDetection.POWER_MODE_CHANGED_ACTION.equals(action)) {
                HwBrightnessPgSceneDetection.this.sendUpdatePgBrightness(false);
            } else if ("huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION".equals(action)) {
                HwBrightnessPgSceneDetection.this.sendUpdatePgBrightness(false);
            }
        }
    };
    private int mPgSceneDetectionBrightenDelayTime = HwActivityManagerService.SERVICE_ADJ;
    private int mPgSceneDetectionDarkenDelayTime = 2500;
    private boolean mShouldUpdateBLight;
    private Sink mStateRecognitionListener = new Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            int appType = -1;
            boolean exempStateType = stateType == IDisplayEngineService.DE_ACTION_PG_VIDEO_START;
            if (eventType == 1) {
                try {
                    appType = HwBrightnessPgSceneDetection.this.mPGSdk.getPkgType(HwBrightnessPgSceneDetection.this.mContext, pkg);
                    HwBrightnessPgSceneDetection.this.mAppType = appType;
                } catch (RemoteException e) {
                    Slog.w(HwBrightnessPgSceneDetection.TAG, "getPkgType failed!");
                }
                if (HwBrightnessPgSceneDetection.DEBUG) {
                    Slog.d(HwBrightnessPgSceneDetection.TAG, "in onStateChanged, pkg = " + pkg + ", appType = " + appType);
                }
                if (HwBrightnessPgSceneDetection.this.mBacklightAppList != null) {
                    HwBrightnessPgSceneDetection.this.mShouldUpdateBLight = HwBrightnessPgSceneDetection.this.mBacklightAppList.contains(Integer.valueOf(appType)) ^ 1 ? exempStateType ^ 1 : false;
                    HwBrightnessPgSceneDetection.this.sendUpdatePgBrightness(HwBrightnessPgSceneDetection.this.mShouldUpdateBLight);
                }
            }
        }
    };

    public interface HwBrightnessPgSceneDetectionCallbacks {
        void updateStateRecognition(boolean z, int i);
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwBrightnessPgSceneDetection(HwBrightnessPgSceneDetectionCallbacks callbacks, int pgSceneDetectionDarkenDelayTime, int pgSceneDetectionBrightenDelayTime, Context context) {
        this.mCallbacks = callbacks;
        this.mContext = context;
        parsePowerSavingAppType(SystemProperties.get("ro.blight.exempt_app_type", ""));
        this.mHandler.sendEmptyMessage(2);
        this.mPgSceneDetectionDarkenDelayTime = pgSceneDetectionDarkenDelayTime;
        this.mPgSceneDetectionBrightenDelayTime = pgSceneDetectionBrightenDelayTime;
    }

    private void registerPowerModeChangedListener() {
        IntentFilter intentFilter = new IntentFilter(POWER_MODE_CHANGED_ACTION);
        intentFilter.addAction("huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION");
        this.mContext.registerReceiver(this.mPgModeChangedBroadcastReceiver, intentFilter);
    }

    public static int getAdjsutLightVal(int rawLight) {
        int retVal = rawLight;
        if (rawLight >= 20 && rawLight <= LOW_LIGHT_MAX) {
            return (((rawLight - 20) * 10) / 35) + 20;
        }
        if (rawLight > LOW_LIGHT_MAX) {
            return (int) ((((float) rawLight) * PG_POWER_SAVE_MODE_LIGHT_RATIO) + 0.5f);
        }
        return retVal;
    }

    public float getPgPowerModeRatio() {
        return PG_POWER_SAVE_MODE_LIGHT_RATIO;
    }

    public int getPgPowerMode() {
        return SystemProperties.getInt(POWERMODE_PROP, 2);
    }

    public int getPowerSaveModeLightValFromRaw(int rawLightVal) {
        int retRatioVal = getAdjsutLightVal(rawLightVal);
        if (retRatioVal < 4) {
            retRatioVal = 4;
            Slog.w(TAG, "warning retRatioVal < min,retRatioVal=4");
        }
        if (retRatioVal <= 255) {
            return retRatioVal;
        }
        Slog.w(TAG, "warning retRatioVal > max,retRatioVal=255");
        return 255;
    }

    public int getSuperPowerSaveModeArbLightVal(int rawLightVal) {
        int calRawVal = rawLightVal > 0 ? rawLightVal : 25;
        return calRawVal < 35 ? calRawVal : 35;
    }

    public int getAdjustLightValByPgMode(int rawLightVal) {
        int retVal = rawLightVal;
        int nowPgMode = getPgPowerMode();
        if (nowPgMode == 1) {
            retVal = getPowerSaveModeLightValFromRaw(rawLightVal);
            if (DEBUG) {
                Slog.d(TAG, "PG_POWER_SAVE_MODE,nowPgMode" + nowPgMode + ",rawLightVal=" + rawLightVal + ",retVal=" + retVal);
            }
        } else if (nowPgMode == 4) {
            retVal = getSuperPowerSaveModeArbLightVal(rawLightVal);
            if (DEBUG) {
                Slog.d(TAG, "PG_SUPER_POWER_SAVE_MODE,nowPgMode" + nowPgMode + ",rawLightVal=" + rawLightVal + ",retVal=" + retVal);
            }
        }
        return retVal;
    }

    private void parsePowerSavingAppType(String exempt_app_type) {
        if (exempt_app_type == null || exempt_app_type.length() <= 0) {
            Slog.w(TAG, "exempt_app_type == null");
            return;
        }
        Slog.i(TAG, "PowerSaving exempt_app_type=" + exempt_app_type);
        if (this.mBacklightAppList != null) {
            this.mBacklightAppList.clear();
        } else {
            this.mBacklightAppList = new ArrayList();
        }
        String[] powerSavingAppType = exempt_app_type.split(",");
        int i = 0;
        while (i < powerSavingAppType.length) {
            try {
                this.mBacklightAppList.add(Integer.valueOf(Integer.parseInt(powerSavingAppType[i])));
                i++;
            } catch (NumberFormatException e) {
                this.mBacklightAppList.clear();
                Slog.w(TAG, "parse mBacklightAppList error");
                return;
            }
        }
    }

    private synchronized void sendUpdatePgBrightness(boolean shouldUpdateBackLight) {
        this.mHandler.removeMessages(1);
        if (shouldUpdateBackLight) {
            this.mHandler.sendEmptyMessageDelayed(1, (long) this.mPgSceneDetectionDarkenDelayTime);
        } else {
            this.mHandler.sendEmptyMessageDelayed(1, (long) this.mPgSceneDetectionBrightenDelayTime);
        }
    }

    public boolean registerPgBLightSceneListener(Context context) {
        if (!this.mPGBLListenerRegisted) {
            this.mPGSdk = PGSdk.getInstance();
            if (this.mPGSdk != null) {
                this.mPGBLListenerRegisted = registerPGSdkListener();
            }
        }
        return this.mPGBLListenerRegisted;
    }

    private boolean registerPGSdkListener() {
        boolean retVal = false;
        if (this.mPGSdk == null) {
            return false;
        }
        try {
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10000);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_MMS_FRONT);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
            retVal = true;
        } catch (RemoteException e) {
            this.mPGSdk = null;
            Slog.e(TAG, "mPGSdk registerSink && enableStateEvent happend RemoteException ");
        } catch (NullPointerException e2) {
            this.mPGSdk = null;
            Slog.e(TAG, "mPGSdk registerSink && enableStateEvent happend NullPointerException ");
        }
        return retVal;
    }

    public void unregisterPgBLightSceneListener(Context context) {
        if (this.mPGBLListenerRegisted) {
            this.mPGBLListenerRegisted = unRegisterPGSdkListener() ^ 1;
        }
    }

    private boolean unRegisterPGSdkListener() {
        if (this.mPGSdk == null) {
            return true;
        }
        boolean retVal;
        try {
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 10000);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_MMS_FRONT);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
            this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
            retVal = true;
        } catch (RemoteException e) {
            retVal = false;
            Slog.e(TAG, "callPG unRegisterListener happend RemoteException ");
        } catch (NullPointerException e2) {
            retVal = false;
            Slog.e(TAG, "callPG unRegisterListener happend NullPointerException ");
        }
        return retVal;
    }

    public boolean getPGBLListenerRegisted() {
        return this.mPGBLListenerRegisted;
    }
}
