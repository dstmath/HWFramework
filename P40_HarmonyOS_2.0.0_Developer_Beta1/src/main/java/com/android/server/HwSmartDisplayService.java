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
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import huawei.android.hwsmartdisplay.IHwSmartDisplayService;

public class HwSmartDisplayService extends IHwSmartDisplayService.Stub {
    private static final boolean DEBUG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
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
    private int mAnimationFlag = 0;
    private int mAnimationTimes = 0;
    private int mAutoComfortMode = 0;
    private int mColorEnhancementSupportLevel = 0;
    private ContentObserver mColorModeObserver = null;
    private ContentObserver mComfortModeObserver = null;
    private Context mContext;
    private int mCurrentColorEnhancementValue = 0;
    private int mCurrentComfortValue = 0;
    private SmartDisplayHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsComfortSupported = true;
    private boolean mIsShowToast = true;
    private int mLastSceneFlag = 0;
    private int mLastSceneMode = 0;
    private String mLastSceneValue;
    private int mValueAnimationTarget = 0;
    private int mValueBeforeAnimation = 0;
    private IWindowManager mWindowManager;

    private static native void finalize_native();

    private static native void init_native();

    private static native int nativeGetDisplayEffectSupported(int i);

    private static native int nativeGetFeatureSupported(int i);

    private static native int nativeSetDisplayEffectParam(int i, int[] iArr, int i2);

    private static native int nativeSetDisplayEffectScene(int i);

    /* access modifiers changed from: private */
    public static native void nativeSetSmartDisplay(int i, int i2);

    static /* synthetic */ int access$1308(HwSmartDisplayService x0) {
        int i = x0.mAnimationTimes;
        x0.mAnimationTimes = i + 1;
        return i;
    }

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
    /* access modifiers changed from: public */
    private void initColorContentObserver() {
        if (this.mColorModeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mColorModeObserver);
        }
        this.mColorModeObserver = new ContentObserver(new Handler()) {
            /* class com.android.server.HwSmartDisplayService.AnonymousClass1 */

            @Override // android.database.ContentObserver
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
            /* class com.android.server.HwSmartDisplayService.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwSmartDisplayService hwSmartDisplayService = HwSmartDisplayService.this;
                hwSmartDisplayService.mAutoComfortMode = Settings.System.getIntForUser(hwSmartDisplayService.mContext.getContentResolver(), HwSmartDisplayService.KEY_AUTO_EYES_PROTECTION, 0, 0);
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

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        private UserSwitchedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (Settings.System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_COLOR_MODE_SWITCH, 0, -2) == 0) {
                HwSmartDisplayService.this.setColorEnhancementValue(0, 0);
            } else {
                HwSmartDisplayService.this.setColorEnhancementValue(1, 0);
            }
            HwSmartDisplayService.this.initColorContentObserver();
        }
    }

    public HwSmartDisplayService(Context context) {
        this.mContext = context;
        this.mCurrentColorEnhancementValue = 0;
        if (!mLoadLibraryFailed) {
            init_native();
            this.mIsComfortSupported = isFeatureSupportedInner(1);
            this.mColorEnhancementSupportLevel = nativeGetFeatureSupported(2);
            Slog.i(TAG, "comfort support = " + this.mIsComfortSupported);
            if (AppActConstant.VALUE_TRUE.equals(SystemProperties.get("ro.config.eyesprotect_support", AppActConstant.VALUE_FALSE)) && this.mIsComfortSupported) {
                this.mAutoComfortMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_AUTO_EYES_PROTECTION, 0, 0);
                initComfortContentObserver(0);
            }
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mHandler = new SmartDisplayHandler(this.mHandlerThread.getLooper());
            if (this.mColorEnhancementSupportLevel == 2) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
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
    /* access modifiers changed from: public */
    private void setColorEnhancementValue(int value, int delayed) {
        Slog.i(TAG, "set color enhancement value to: " + value + ",delayed=" + delayed);
        Message msg = this.mHandler.obtainMessage(3, value, 0);
        if (delayed > 0) {
            this.mHandler.sendMessageDelayed(msg, (long) delayed);
        } else {
            this.mHandler.sendMessage(msg);
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
            filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
            filter.setPriority(1000);
            HwSmartDisplayService.this.mContext.registerReceiver(this, filter);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwSmartDisplayService.DEBUG) {
                Slog.i(HwSmartDisplayService.TAG, "Receiver broadcast action=" + intent.getAction());
            }
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(intent.getAction()) && HwSmartDisplayService.this.mAutoComfortMode != 0) {
                HwSmartDisplayService.this.animationTo(0, 0);
                HwSmartDisplayService.this.mLastSceneMode = 0;
                HwSmartDisplayService.this.mLastSceneFlag = 0;
                HwSmartDisplayService.this.mIsShowToast = false;
                HwSmartDisplayService.this.mLastSceneValue = null;
            }
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(intent.getAction())) {
                HwSmartDisplayService hwSmartDisplayService = HwSmartDisplayService.this;
                hwSmartDisplayService.mAutoComfortMode = Settings.System.getIntForUser(hwSmartDisplayService.mContext.getContentResolver(), HwSmartDisplayService.KEY_AUTO_EYES_PROTECTION, 0, 0);
                Slog.i(HwSmartDisplayService.TAG, "Comfort mode in Settings changed to = " + HwSmartDisplayService.this.mAutoComfortMode);
                if (HwSmartDisplayService.this.mAutoComfortMode != 0) {
                    HwSmartDisplayService.this.animationTo(26, 0);
                }
            }
        }
    }

    public boolean isFeatureSupported(int feature) {
        return isFeatureSupportedInner(feature);
    }

    private boolean isFeatureSupportedInner(int feature) {
        if (mLoadLibraryFailed) {
            Slog.e(TAG, "Comfort feature not supported because of library not found");
            return false;
        } else if (feature == 2) {
            if (nativeGetFeatureSupported(feature) == 2) {
                return true;
            }
            return false;
        } else if (nativeGetFeatureSupported(feature) != 0) {
            return true;
        } else {
            return false;
        }
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
    /* access modifiers changed from: public */
    private void animationTo(int target, int delayed) {
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

    /* access modifiers changed from: private */
    public final class SmartDisplayHandler extends Handler {
        public SmartDisplayHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int value = msg.arg1;
            int i = msg.what;
            if (i == 1) {
                Toast.makeText(HwSmartDisplayService.this.mContext, 33685786, 0).show();
            } else if (i == 2) {
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
                    HwSmartDisplayService.access$1308(HwSmartDisplayService.this);
                }
                HwSmartDisplayService.this.mCurrentComfortValue = value;
                HwSmartDisplayService.nativeSetSmartDisplay(1, HwSmartDisplayService.this.mCurrentComfortValue);
                if (HwSmartDisplayService.DEBUG) {
                    Slog.i(HwSmartDisplayService.TAG, "Process comfort msg value =" + HwSmartDisplayService.this.mCurrentComfortValue);
                }
                if (msg.arg2 > 0 && HwSmartDisplayService.this.mAnimationTimes <= 10) {
                    HwSmartDisplayService.this.mHandler.sendMessageDelayed(HwSmartDisplayService.this.mHandler.obtainMessage(2, value, msg.arg2), 40);
                } else if (HwSmartDisplayService.this.mCurrentComfortValue > 0 && HwSmartDisplayService.this.mIsShowToast) {
                    HwSmartDisplayService.this.mHandler.sendMessageDelayed(HwSmartDisplayService.this.mHandler.obtainMessage(1, 0, 0), 300);
                    HwSmartDisplayService.this.mIsShowToast = true;
                }
            } else if (i != 3) {
                Slog.e(HwSmartDisplayService.TAG, "Invalid message");
            } else {
                HwSmartDisplayService.this.mCurrentColorEnhancementValue = value;
                HwSmartDisplayService.nativeSetSmartDisplay(2, value);
                Slog.i(HwSmartDisplayService.TAG, "Process colorEnhancement msg value =" + value);
            }
        }
    }
}
