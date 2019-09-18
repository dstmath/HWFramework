package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.IWindowManager;
import android.widget.Toast;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import huawei.android.hwsmartdisplay.IHwSmartDisplayService;

public class HwSmartDisplayService extends IHwSmartDisplayService.Stub {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (1 == SystemProperties.getInt("ro.debuggable", 0));
    private static final String KEY_AUTO_EYES_PROTECTION = "auto_eyes_protection";
    private static final String KEY_COLOR_MODE_SWITCH = "color_mode_switch";
    private static final int LEVEL_COLOR_ENHANCEMENT_SUPPORT_HIGH = 2;
    private static final int LEVEL_COLOR_ENHANCEMENT_SUPPORT_LOW = 1;
    private static final int LEVEL_COLOR_ENHANCEMENT_SUPPORT_NONE = 0;
    private static final int MODE_COLOR_ENHANCEMENT = 2;
    private static final int MODE_COMFORT = 1;
    private static final int MSG_SET_COLOR_VALUE = 3;
    private static final int MSG_SET_COMFORT_VALUE = 2;
    private static final int MSG_SHOW_COMFORT_TOAST = 1;
    private static final String TAG = "HwSmartDisplayService";
    private static final int VALUE_ANIMATION_MSG_INTERVAL = 40;
    private static final int VALUE_ANIMATION_MSG_TIMES = 10;
    private static final int VALUE_COLOR_ENHANCEMENT_BRIGHT = 1;
    private static final int VALUE_COLOR_ENHANCEMENT_NATURE = 0;
    private static final int VALUE_COLOR_ENHANCEMENT_SRGB = 2;
    private static final int VALUE_COMFORT_DEFAULT = 26;
    private static final int VALUE_SET_COLOR_MODE_DELAYED = 300;
    private static final int VALUE_SHOW_TOAST_DELAYED = 300;
    private static boolean mLoadLibraryFailed;
    /* access modifiers changed from: private */
    public int mAnimationFlag = 0;
    /* access modifiers changed from: private */
    public int mAnimationTimes = 0;
    /* access modifiers changed from: private */
    public int mAutoComfortMode = 0;
    /* access modifiers changed from: private */
    public int mColorEnhancementSupportLevel = 0;
    private ContentObserver mColorModeObserver = null;
    private ContentObserver mComfortModeObserver = null;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentColorEnhancementValue = 0;
    /* access modifiers changed from: private */
    public int mCurrentComfortValue = 0;
    private HDREnginePGPClient mHDREnginePGPClient;
    /* access modifiers changed from: private */
    public SmartDisplayHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsComfortSupported = true;
    /* access modifiers changed from: private */
    public boolean mIsShowToast = true;
    /* access modifiers changed from: private */
    public int mLastSceneFlag = 0;
    /* access modifiers changed from: private */
    public int mLastSceneMode = 0;
    /* access modifiers changed from: private */
    public String mLastSceneValue;
    /* access modifiers changed from: private */
    public int mValueAnimationTarget = 0;
    /* access modifiers changed from: private */
    public int mValueBeforeAnimation = 0;
    private IWindowManager mWindowManager;

    private final class HDREnginePGPClient implements IPGPlugCallbacks {
        private PGPlug mPGPlug = new PGPlug(this, HwSmartDisplayService.TAG);

        public HDREnginePGPClient() {
            new Thread(this.mPGPlug, HwSmartDisplayService.TAG).start();
        }

        public void onDaemonConnected() {
            Slog.i(HwSmartDisplayService.TAG, "HDREnginePGPClient connected success!");
        }

        public boolean onEvent(int actionID, String msg) {
            HwSmartDisplayService.this.setDisplayEffectScene(actionID);
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(HwSmartDisplayService.TAG, "Client connect timeout!");
        }
    }

    private final class IPGPClient implements IPGPlugCallbacks {
        private PGPlug mPGPlug = new PGPlug(this, HwSmartDisplayService.TAG);

        public IPGPClient() {
            new Thread(this.mPGPlug, HwSmartDisplayService.TAG).start();
        }

        public void onDaemonConnected() {
            Slog.i(HwSmartDisplayService.TAG, "IPGPClient connected success!");
        }

        /* JADX WARNING: Removed duplicated region for block: B:53:0x014d  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x016b  */
        public boolean onEvent(int actionID, String value) {
            int mode = 0;
            if (1 != PGAction.checkActionType(actionID)) {
                if (HwSmartDisplayService.DEBUG) {
                    Slog.i(HwSmartDisplayService.TAG, "Filter application event id : " + actionID);
                }
                return true;
            }
            int subFlag = PGAction.checkActionFlag(actionID);
            if (HwSmartDisplayService.DEBUG) {
                Slog.i(HwSmartDisplayService.TAG, "IPGP onEvent actionID=" + actionID + ", value=" + value + ",  subFlag=" + subFlag);
            }
            if (subFlag == 3 || actionID == 10017 || actionID == 10007) {
                if (HwSmartDisplayService.DEBUG) {
                    switch (actionID) {
                        case IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT /*10001*/:
                            Slog.i(HwSmartDisplayService.TAG, "Browser");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT /*10002*/:
                            Slog.i(HwSmartDisplayService.TAG, "3D Game");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT /*10003*/:
                            Slog.i(HwSmartDisplayService.TAG, "Ebook");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT /*10004*/:
                            Slog.i(HwSmartDisplayService.TAG, "Gallery");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT /*10007*/:
                            Slog.i(HwSmartDisplayService.TAG, "Camera");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT /*10008*/:
                            Slog.i(HwSmartDisplayService.TAG, "Office");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT /*10009*/:
                            Slog.i(HwSmartDisplayService.TAG, "Video");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT /*10010*/:
                            Slog.i(HwSmartDisplayService.TAG, "Launcher");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT /*10011*/:
                            Slog.i(HwSmartDisplayService.TAG, "2DGame");
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_MMS_FRONT /*10013*/:
                            Slog.i(HwSmartDisplayService.TAG, "MMS");
                            break;
                        default:
                            Slog.i(HwSmartDisplayService.TAG, MemoryConstant.MEM_SCENE_DEFAULT);
                            break;
                    }
                }
                if (actionID != 10003) {
                    if (actionID != 10017) {
                        switch (actionID) {
                            case IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT /*10007*/:
                                if (HwSmartDisplayService.this.mColorEnhancementSupportLevel == 1) {
                                    HwSmartDisplayService.this.setColorEnhancementValue(0, 300);
                                    break;
                                }
                                break;
                            case IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT /*10008*/:
                                break;
                        }
                    } else if (HwSmartDisplayService.this.mColorEnhancementSupportLevel == 1) {
                        HwSmartDisplayService.this.setColorEnhancementValue(1, 300);
                    }
                    if (subFlag == 3 && subFlag == HwSmartDisplayService.this.mLastSceneFlag && value.equals(HwSmartDisplayService.this.mLastSceneValue)) {
                        mode |= HwSmartDisplayService.this.mLastSceneMode;
                    }
                    int unused = HwSmartDisplayService.this.mLastSceneFlag = subFlag;
                    String unused2 = HwSmartDisplayService.this.mLastSceneValue = value;
                    if (mode != HwSmartDisplayService.this.mLastSceneMode) {
                        if (HwSmartDisplayService.DEBUG) {
                            Slog.i(HwSmartDisplayService.TAG, "the current scene is the same as the last one,mode:" + mode);
                        }
                        return true;
                    }
                    int unused3 = HwSmartDisplayService.this.mLastSceneMode = mode;
                    return true;
                }
                mode = 0 | 1;
                mode |= HwSmartDisplayService.this.mLastSceneMode;
                int unused4 = HwSmartDisplayService.this.mLastSceneFlag = subFlag;
                String unused5 = HwSmartDisplayService.this.mLastSceneValue = value;
                if (mode != HwSmartDisplayService.this.mLastSceneMode) {
                }
            } else {
                int unused6 = HwSmartDisplayService.this.mLastSceneFlag = subFlag;
                String unused7 = HwSmartDisplayService.this.mLastSceneValue = value;
                if (HwSmartDisplayService.DEBUG) {
                    Slog.i(HwSmartDisplayService.TAG, "Not used non-parent scene , ignore it");
                }
                return true;
            }
        }

        public void onConnectedTimeout() {
            Slog.e(HwSmartDisplayService.TAG, "Client connect timeout!");
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.setPriority(1000);
            HwSmartDisplayService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (HwSmartDisplayService.DEBUG) {
                Slog.i(HwSmartDisplayService.TAG, "Receiver broadcast action=" + intent.getAction());
            }
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction()) && HwSmartDisplayService.this.mAutoComfortMode != 0) {
                HwSmartDisplayService.this.animationTo(0, 0);
                int unused = HwSmartDisplayService.this.mLastSceneMode = 0;
                int unused2 = HwSmartDisplayService.this.mLastSceneFlag = 0;
                boolean unused3 = HwSmartDisplayService.this.mIsShowToast = false;
                String unused4 = HwSmartDisplayService.this.mLastSceneValue = null;
            }
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                int unused5 = HwSmartDisplayService.this.mAutoComfortMode = Settings.System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_AUTO_EYES_PROTECTION, 0, 0);
                Slog.i(HwSmartDisplayService.TAG, "Comfort mode in Settings changed to = " + HwSmartDisplayService.this.mAutoComfortMode);
                if (HwSmartDisplayService.this.mAutoComfortMode != 0) {
                    HwSmartDisplayService.this.animationTo(26, 0);
                }
            }
        }
    }

    private final class SmartDisplayHandler extends Handler {
        public SmartDisplayHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int value;
            int value2 = msg.arg1;
            switch (msg.what) {
                case 1:
                    Toast.makeText(HwSmartDisplayService.this.mContext, 33685786, 0).show();
                    return;
                case 2:
                    if (msg.arg2 > 0) {
                        if (msg.arg2 != HwSmartDisplayService.this.mAnimationFlag) {
                            Slog.i(HwSmartDisplayService.TAG, "drop the old animation msg when the new one is coming");
                            return;
                        }
                        if (HwSmartDisplayService.this.mValueAnimationTarget > 0) {
                            value = (HwSmartDisplayService.this.mValueAnimationTarget * HwSmartDisplayService.this.mAnimationTimes) / 10;
                        } else {
                            value = (HwSmartDisplayService.this.mValueBeforeAnimation * (10 - HwSmartDisplayService.this.mAnimationTimes)) / 10;
                        }
                        value2 = value;
                        int unused = HwSmartDisplayService.this.mAnimationTimes = HwSmartDisplayService.this.mAnimationTimes + 1;
                    }
                    int unused2 = HwSmartDisplayService.this.mCurrentComfortValue = value2;
                    HwSmartDisplayService.nativeSetSmartDisplay(1, HwSmartDisplayService.this.mCurrentComfortValue);
                    if (HwSmartDisplayService.DEBUG) {
                        Slog.i(HwSmartDisplayService.TAG, "Process comfort msg value =" + HwSmartDisplayService.this.mCurrentComfortValue);
                    }
                    if (msg.arg2 > 0 && HwSmartDisplayService.this.mAnimationTimes <= 10) {
                        HwSmartDisplayService.this.mHandler.sendMessageDelayed(HwSmartDisplayService.this.mHandler.obtainMessage(2, value2, msg.arg2), 40);
                        return;
                    } else if (HwSmartDisplayService.this.mCurrentComfortValue > 0 && HwSmartDisplayService.this.mIsShowToast) {
                        HwSmartDisplayService.this.mHandler.sendMessageDelayed(HwSmartDisplayService.this.mHandler.obtainMessage(1, 0, 0), 300);
                        boolean unused3 = HwSmartDisplayService.this.mIsShowToast = true;
                        return;
                    } else {
                        return;
                    }
                case 3:
                    int unused4 = HwSmartDisplayService.this.mCurrentColorEnhancementValue = value2;
                    HwSmartDisplayService.nativeSetSmartDisplay(2, value2);
                    Slog.i(HwSmartDisplayService.TAG, "Process colorEnhancement msg value =" + value2);
                    return;
                default:
                    Slog.e(HwSmartDisplayService.TAG, "Invalid message");
                    return;
            }
        }
    }

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        private UserSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (Settings.System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_COLOR_MODE_SWITCH, 0, -2) == 0) {
                HwSmartDisplayService.this.setColorEnhancementValue(0, 0);
            } else {
                HwSmartDisplayService.this.setColorEnhancementValue(1, 0);
            }
            HwSmartDisplayService.this.initColorContentObserver();
        }
    }

    private static native void finalize_native();

    private static native void init_native();

    private static native int nativeGetDisplayEffectSupported(int i);

    private static native int nativeGetFeatureSupported(int i);

    private static native int nativeSetDisplayEffectParam(int i, int[] iArr, int i2);

    private static native int nativeSetDisplayEffectScene(int i);

    /* access modifiers changed from: private */
    public static native void nativeSetSmartDisplay(int i, int i2);

    static {
        mLoadLibraryFailed = false;
        try {
            System.loadLibrary("hwsmartdisplay_jni");
        } catch (UnsatisfiedLinkError e) {
            mLoadLibraryFailed = true;
            Slog.d(TAG, "hwsmartdisplay_jni library not found!");
        }
    }

    /* access modifiers changed from: private */
    public void initColorContentObserver() {
        if (this.mColorModeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mColorModeObserver);
        }
        this.mColorModeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                if (Settings.System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_COLOR_MODE_SWITCH, 0, -2) == 0) {
                    Slog.i(HwSmartDisplayService.TAG, "content observer onChange 0");
                    HwSmartDisplayService.this.setColorEnhancementValue(0, 0);
                    return;
                }
                Slog.i(HwSmartDisplayService.TAG, "content observer onChange 1");
                HwSmartDisplayService.this.setColorEnhancementValue(1, 0);
            }
        };
        Slog.i(TAG, "mColorModeObserver this " + this.mColorModeObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_COLOR_MODE_SWITCH), true, this.mColorModeObserver, -2);
    }

    private void initComfortContentObserver(int id) {
        this.mComfortModeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                int unused = HwSmartDisplayService.this.mAutoComfortMode = Settings.System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_AUTO_EYES_PROTECTION, 0, 0);
                Slog.i(HwSmartDisplayService.TAG, "Comfort mode in Settings changed to = " + HwSmartDisplayService.this.mAutoComfortMode);
                if (HwSmartDisplayService.this.mAutoComfortMode != 0) {
                    HwSmartDisplayService.this.animationTo(26, 0);
                } else {
                    HwSmartDisplayService.this.animationTo(0, 0);
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_AUTO_EYES_PROTECTION), true, this.mComfortModeObserver, id);
    }

    public HwSmartDisplayService(Context context) {
        this.mContext = context;
        this.mCurrentColorEnhancementValue = 0;
        if (!mLoadLibraryFailed) {
            init_native();
            this.mIsComfortSupported = isFeatureSupported(1);
            this.mColorEnhancementSupportLevel = nativeGetFeatureSupported(2);
            Slog.i(TAG, "comfort support = " + this.mIsComfortSupported);
            if ("true".equals(SystemProperties.get("ro.config.eyesprotect_support", "false")) && this.mIsComfortSupported) {
                this.mAutoComfortMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_AUTO_EYES_PROTECTION, 0, 0);
                initComfortContentObserver(0);
            }
            this.mHDREnginePGPClient = new HDREnginePGPClient();
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mHandler = new SmartDisplayHandler(this.mHandlerThread.getLooper());
            if (this.mColorEnhancementSupportLevel == 2) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.USER_SWITCHED");
                this.mContext.registerReceiver(new UserSwitchedReceiver(), filter, null, this.mHandler);
                if (Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_COLOR_MODE_SWITCH, 0, -2) == 0) {
                    setColorEnhancementValue(0, 0);
                } else {
                    setColorEnhancementValue(1, 0);
                }
                initColorContentObserver();
            }
            this.mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (this.mAutoComfortMode != 0) {
                animationTo(26, 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        finalize_native();
        try {
            HwSmartDisplayService.super.finalize();
        } catch (Throwable th) {
        }
    }

    /* access modifiers changed from: private */
    public void setColorEnhancementValue(int value, int delayed) {
        Slog.i(TAG, "set color enhancement value to: " + value + ",delayed=" + delayed);
        Message msg = this.mHandler.obtainMessage(3, value, 0);
        if (delayed > 0) {
            this.mHandler.sendMessageDelayed(msg, (long) delayed);
        } else {
            this.mHandler.sendMessage(msg);
        }
    }

    public boolean isFeatureSupported(int feature) {
        if (!mLoadLibraryFailed) {
            boolean z = true;
            if (2 == feature) {
                if (2 != nativeGetFeatureSupported(feature)) {
                    z = false;
                }
                return z;
            }
            if (nativeGetFeatureSupported(feature) == 0) {
                z = false;
            }
            return z;
        }
        Slog.e(TAG, "Comfort feature not supported because of library not found");
        return false;
    }

    public int setDisplayEffectScene(int scene) {
        try {
            if (!mLoadLibraryFailed) {
                return nativeSetDisplayEffectScene(scene);
            }
            Slog.d(TAG, "nativeSetDisplayEffectScene not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativeSetDisplayEffectScene not found!");
            return -1;
        }
    }

    public int setDisplayEffectParam(int type, int[] buffer, int length) {
        try {
            if (!mLoadLibraryFailed) {
                return nativeSetDisplayEffectParam(type, buffer, length);
            }
            Slog.d(TAG, "nativesetDisplayEffetParam not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativesetDisplayEffetParam not found!");
            return -1;
        }
    }

    public int getDisplayEffectSupported(int type) {
        try {
            if (!mLoadLibraryFailed) {
                return nativeGetDisplayEffectSupported(type);
            }
            Slog.d(TAG, "nativesetDisplayEffectSupported not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativesetDisplayEffetSupported not found!");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public void animationTo(int target, int delayed) {
        Slog.i(TAG, "animationTo target = " + target + ", delayed time = " + delayed);
        this.mHandler.removeMessages(2);
        if (this.mAnimationTimes < 10) {
            this.mAnimationFlag++;
        } else {
            this.mAnimationFlag = 1;
        }
        this.mAnimationTimes = 1;
        this.mValueBeforeAnimation = this.mCurrentComfortValue;
        this.mValueAnimationTarget = target;
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, this.mValueAnimationTarget, this.mAnimationFlag), (long) delayed);
    }
}
