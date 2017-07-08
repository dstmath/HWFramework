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
import android.provider.Settings.System;
import android.util.Slog;
import android.view.IWindowManager;
import android.widget.Toast;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.IOTController;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import huawei.android.hwsmartdisplay.IHwSmartDisplayService.Stub;

public class HwSmartDisplayService extends Stub {
    private static final boolean DEBUG = false;
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
    private int mAnimationFlag;
    private int mAnimationTimes;
    private int mAutoComfortMode;
    private int mColorEnhancementSupportLevel;
    private ContentObserver mColorModeObserver;
    private ContentObserver mComfortModeObserver;
    private Context mContext;
    private int mCurrentColorEnhancementValue;
    private int mCurrentComfortValue;
    private HDREnginePGPClient mHDREnginePGPClient;
    private SmartDisplayHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsComfortSupported;
    private boolean mIsShowToast;
    private int mLastSceneFlag;
    private int mLastSceneMode;
    private String mLastSceneValue;
    private int mValueAnimationTarget;
    private int mValueBeforeAnimation;
    private IWindowManager mWindowManager;

    /* renamed from: com.android.server.HwSmartDisplayService.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            if (System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_COLOR_MODE_SWITCH, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, -2) == 0) {
                Slog.i(HwSmartDisplayService.TAG, "content observer onChange 0");
                HwSmartDisplayService.this.setColorEnhancementValue(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
                return;
            }
            Slog.i(HwSmartDisplayService.TAG, "content observer onChange 1");
            HwSmartDisplayService.this.setColorEnhancementValue(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
        }
    }

    /* renamed from: com.android.server.HwSmartDisplayService.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwSmartDisplayService.this.mAutoComfortMode = System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_AUTO_EYES_PROTECTION, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
            Slog.i(HwSmartDisplayService.TAG, "Comfort mode in Settings changed to = " + HwSmartDisplayService.this.mAutoComfortMode);
            if (HwSmartDisplayService.this.mAutoComfortMode != 0) {
                HwSmartDisplayService.this.animationTo(HwSmartDisplayService.VALUE_COMFORT_DEFAULT, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
            } else {
                HwSmartDisplayService.this.animationTo(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
            }
        }
    }

    private final class HDREnginePGPClient implements IPGPlugCallbacks {
        private PGPlug mPGPlug;

        public HDREnginePGPClient() {
            this.mPGPlug = new PGPlug(this, HwSmartDisplayService.TAG);
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
        private PGPlug mPGPlug;

        public IPGPClient() {
            this.mPGPlug = new PGPlug(this, HwSmartDisplayService.TAG);
            new Thread(this.mPGPlug, HwSmartDisplayService.TAG).start();
        }

        public void onDaemonConnected() {
            Slog.i(HwSmartDisplayService.TAG, "IPGPClient connected success!");
        }

        public boolean onEvent(int actionID, String value) {
            int mode = HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE;
            if (HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT != PGAction.checkActionType(actionID)) {
                if (HwSmartDisplayService.DEBUG) {
                    Slog.i(HwSmartDisplayService.TAG, "Filter application event id : " + actionID);
                }
                return true;
            }
            int subFlag = PGAction.checkActionFlag(actionID);
            if (HwSmartDisplayService.DEBUG) {
                Slog.i(HwSmartDisplayService.TAG, "IPGP onEvent actionID=" + actionID + ", value=" + value + ",  subFlag=" + subFlag);
            }
            if (subFlag == HwSmartDisplayService.MSG_SET_COLOR_VALUE || actionID == 10017 || actionID == 10007) {
                if (HwSmartDisplayService.DEBUG) {
                    String mFront = MemoryConstant.MEM_SCENE_DEFAULT;
                    switch (actionID) {
                        case 10001:
                            mFront = "Browser";
                            Slog.i(HwSmartDisplayService.TAG, "Browser");
                            break;
                        case 10002:
                            mFront = "3D Game";
                            Slog.i(HwSmartDisplayService.TAG, "3D Game");
                            break;
                        case 10003:
                            mFront = "Ebook";
                            Slog.i(HwSmartDisplayService.TAG, "Ebook");
                            break;
                        case 10004:
                            mFront = "Gallery";
                            Slog.i(HwSmartDisplayService.TAG, "Gallery");
                            break;
                        case 10007:
                            mFront = "Camera";
                            Slog.i(HwSmartDisplayService.TAG, "Camera");
                            break;
                        case 10008:
                            mFront = "Office";
                            Slog.i(HwSmartDisplayService.TAG, "Office");
                            break;
                        case 10009:
                            mFront = "Video";
                            Slog.i(HwSmartDisplayService.TAG, "Video");
                            break;
                        case 10010:
                            mFront = "Launcher";
                            Slog.i(HwSmartDisplayService.TAG, "Launcher");
                            break;
                        case 10011:
                            mFront = "2DGame";
                            Slog.i(HwSmartDisplayService.TAG, "2DGame");
                            break;
                        case 10013:
                            mFront = "MMS";
                            Slog.i(HwSmartDisplayService.TAG, "MMS");
                            break;
                        default:
                            mFront = MemoryConstant.MEM_SCENE_DEFAULT;
                            Slog.i(HwSmartDisplayService.TAG, MemoryConstant.MEM_SCENE_DEFAULT);
                            break;
                    }
                }
                switch (actionID) {
                    case 10003:
                    case 10008:
                        mode = HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT;
                        break;
                    case 10007:
                        if (HwSmartDisplayService.this.mColorEnhancementSupportLevel == HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT) {
                            HwSmartDisplayService.this.setColorEnhancementValue(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_SHOW_TOAST_DELAYED);
                            break;
                        }
                        break;
                    case 10017:
                        if (HwSmartDisplayService.this.mColorEnhancementSupportLevel == HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT) {
                            HwSmartDisplayService.this.setColorEnhancementValue(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT, HwSmartDisplayService.VALUE_SHOW_TOAST_DELAYED);
                            break;
                        }
                        break;
                }
                if (subFlag == HwSmartDisplayService.MSG_SET_COLOR_VALUE && subFlag == HwSmartDisplayService.this.mLastSceneFlag && value.equals(HwSmartDisplayService.this.mLastSceneValue)) {
                    mode |= HwSmartDisplayService.this.mLastSceneMode;
                }
                HwSmartDisplayService.this.mLastSceneFlag = subFlag;
                HwSmartDisplayService.this.mLastSceneValue = value;
                if (mode == HwSmartDisplayService.this.mLastSceneMode) {
                    if (HwSmartDisplayService.DEBUG) {
                        Slog.i(HwSmartDisplayService.TAG, "the current scene is the same as the last one,mode:" + mode);
                    }
                    return true;
                }
                HwSmartDisplayService.this.mLastSceneMode = mode;
                return true;
            }
            HwSmartDisplayService.this.mLastSceneFlag = subFlag;
            HwSmartDisplayService.this.mLastSceneValue = value;
            if (HwSmartDisplayService.DEBUG) {
                Slog.i(HwSmartDisplayService.TAG, "Not used non-parent scene , ignore it");
            }
            return true;
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
            filter.setPriority(IOTController.TYPE_MASTER);
            HwSmartDisplayService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (HwSmartDisplayService.DEBUG) {
                Slog.i(HwSmartDisplayService.TAG, "Receiver broadcast action=" + intent.getAction());
            }
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction()) && HwSmartDisplayService.this.mAutoComfortMode != 0) {
                HwSmartDisplayService.this.animationTo(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
                HwSmartDisplayService.this.mLastSceneMode = HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE;
                HwSmartDisplayService.this.mLastSceneFlag = HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE;
                HwSmartDisplayService.this.mIsShowToast = HwSmartDisplayService.DEBUG;
                HwSmartDisplayService.this.mLastSceneValue = null;
            }
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                HwSmartDisplayService.this.mAutoComfortMode = System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_AUTO_EYES_PROTECTION, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
                Slog.i(HwSmartDisplayService.TAG, "Comfort mode in Settings changed to = " + HwSmartDisplayService.this.mAutoComfortMode);
                if (HwSmartDisplayService.this.mAutoComfortMode != 0) {
                    HwSmartDisplayService.this.animationTo(HwSmartDisplayService.VALUE_COMFORT_DEFAULT, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
                }
            }
        }
    }

    private final class SmartDisplayHandler extends Handler {
        public SmartDisplayHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int value = msg.arg1;
            switch (msg.what) {
                case HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT /*1*/:
                    Toast.makeText(HwSmartDisplayService.this.mContext, 33685779, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE).show();
                case HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_SRGB /*2*/:
                    if (msg.arg2 > 0) {
                        if (msg.arg2 != HwSmartDisplayService.this.mAnimationFlag) {
                            Slog.i(HwSmartDisplayService.TAG, "drop the old animation msg when the new one is coming");
                            return;
                        }
                        if (HwSmartDisplayService.this.mValueAnimationTarget > 0) {
                            value = (HwSmartDisplayService.this.mValueAnimationTarget * HwSmartDisplayService.this.mAnimationTimes) / HwSmartDisplayService.VALUE_ANIMATION_MSG_TIMES;
                        } else {
                            value = (HwSmartDisplayService.this.mValueBeforeAnimation * (10 - HwSmartDisplayService.this.mAnimationTimes)) / HwSmartDisplayService.VALUE_ANIMATION_MSG_TIMES;
                        }
                        HwSmartDisplayService hwSmartDisplayService = HwSmartDisplayService.this;
                        hwSmartDisplayService.mAnimationTimes = hwSmartDisplayService.mAnimationTimes + HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT;
                    }
                    HwSmartDisplayService.this.mCurrentComfortValue = value;
                    HwSmartDisplayService.nativeSetSmartDisplay(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT, HwSmartDisplayService.this.mCurrentComfortValue);
                    if (HwSmartDisplayService.DEBUG) {
                        Slog.i(HwSmartDisplayService.TAG, "Process comfort msg value =" + HwSmartDisplayService.this.mCurrentComfortValue);
                    }
                    if (msg.arg2 > 0 && HwSmartDisplayService.this.mAnimationTimes <= HwSmartDisplayService.VALUE_ANIMATION_MSG_TIMES) {
                        HwSmartDisplayService.this.mHandler.sendMessageDelayed(HwSmartDisplayService.this.mHandler.obtainMessage(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_SRGB, value, msg.arg2), 40);
                    } else if (HwSmartDisplayService.this.mCurrentComfortValue > 0 && HwSmartDisplayService.this.mIsShowToast) {
                        HwSmartDisplayService.this.mHandler.sendMessageDelayed(HwSmartDisplayService.this.mHandler.obtainMessage(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE), 300);
                        HwSmartDisplayService.this.mIsShowToast = true;
                    }
                case HwSmartDisplayService.MSG_SET_COLOR_VALUE /*3*/:
                    HwSmartDisplayService.this.mCurrentColorEnhancementValue = value;
                    HwSmartDisplayService.nativeSetSmartDisplay(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_SRGB, value);
                    Slog.i(HwSmartDisplayService.TAG, "Process colorEnhancement msg value =" + value);
                default:
                    Slog.e(HwSmartDisplayService.TAG, "Invalid message");
            }
        }
    }

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        private UserSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (System.getIntForUser(HwSmartDisplayService.this.mContext.getContentResolver(), HwSmartDisplayService.KEY_COLOR_MODE_SWITCH, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, -2) == 0) {
                HwSmartDisplayService.this.setColorEnhancementValue(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
            } else {
                HwSmartDisplayService.this.setColorEnhancementValue(HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_BRIGHT, HwSmartDisplayService.VALUE_COLOR_ENHANCEMENT_NATURE);
            }
            HwSmartDisplayService.this.initColorContentObserver();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwSmartDisplayService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwSmartDisplayService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwSmartDisplayService.<clinit>():void");
    }

    private static native void finalize_native();

    private static native void init_native();

    private static native int nativeGetDisplayEffectSupported(int i);

    private static native int nativeGetFeatureSupported(int i);

    private static native int nativeSetDisplayEffectParam(int i, int[] iArr, int i2);

    private static native int nativeSetDisplayEffectScene(int i);

    private static native void nativeSetSmartDisplay(int i, int i2);

    private void initColorContentObserver() {
        if (this.mColorModeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mColorModeObserver);
        }
        this.mColorModeObserver = new AnonymousClass1(new Handler());
        Slog.i(TAG, "mColorModeObserver this " + this.mColorModeObserver);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_COLOR_MODE_SWITCH), true, this.mColorModeObserver, -2);
    }

    private void initComfortContentObserver(int id) {
        this.mComfortModeObserver = new AnonymousClass2(new Handler());
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_AUTO_EYES_PROTECTION), true, this.mComfortModeObserver, id);
    }

    public HwSmartDisplayService(Context context) {
        this.mLastSceneMode = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mLastSceneFlag = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mAutoComfortMode = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mCurrentComfortValue = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mCurrentColorEnhancementValue = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mColorEnhancementSupportLevel = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mAnimationFlag = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mAnimationTimes = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mValueAnimationTarget = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mValueBeforeAnimation = VALUE_COLOR_ENHANCEMENT_NATURE;
        this.mIsShowToast = true;
        this.mIsComfortSupported = true;
        this.mColorModeObserver = null;
        this.mComfortModeObserver = null;
        this.mContext = context;
        this.mCurrentColorEnhancementValue = VALUE_COLOR_ENHANCEMENT_NATURE;
        if (!mLoadLibraryFailed) {
            init_native();
            this.mIsComfortSupported = isFeatureSupported(VALUE_COLOR_ENHANCEMENT_BRIGHT);
            this.mColorEnhancementSupportLevel = nativeGetFeatureSupported(VALUE_COLOR_ENHANCEMENT_SRGB);
            Slog.i(TAG, "comfort support = " + this.mIsComfortSupported);
            if ("true".equals(SystemProperties.get("ro.config.eyesprotect_support", "false")) && this.mIsComfortSupported) {
                this.mAutoComfortMode = System.getIntForUser(this.mContext.getContentResolver(), KEY_AUTO_EYES_PROTECTION, VALUE_COLOR_ENHANCEMENT_NATURE, VALUE_COLOR_ENHANCEMENT_NATURE);
                initComfortContentObserver(VALUE_COLOR_ENHANCEMENT_NATURE);
            }
            this.mHDREnginePGPClient = new HDREnginePGPClient();
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mHandler = new SmartDisplayHandler(this.mHandlerThread.getLooper());
            if (this.mColorEnhancementSupportLevel == VALUE_COLOR_ENHANCEMENT_SRGB) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.USER_SWITCHED");
                this.mContext.registerReceiver(new UserSwitchedReceiver(), filter, null, this.mHandler);
                if (System.getIntForUser(this.mContext.getContentResolver(), KEY_COLOR_MODE_SWITCH, VALUE_COLOR_ENHANCEMENT_NATURE, -2) == 0) {
                    setColorEnhancementValue(VALUE_COLOR_ENHANCEMENT_NATURE, VALUE_COLOR_ENHANCEMENT_NATURE);
                } else {
                    setColorEnhancementValue(VALUE_COLOR_ENHANCEMENT_BRIGHT, VALUE_COLOR_ENHANCEMENT_NATURE);
                }
                initColorContentObserver();
            }
            this.mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (this.mAutoComfortMode != 0) {
                animationTo(VALUE_COMFORT_DEFAULT, VALUE_COLOR_ENHANCEMENT_NATURE);
            }
        }
    }

    protected void finalize() {
        finalize_native();
        try {
            super.finalize();
        } catch (Throwable th) {
        }
    }

    private void setColorEnhancementValue(int value, int delayed) {
        Slog.i(TAG, "set color enhancement value to: " + value + ",delayed=" + delayed);
        Message msg = this.mHandler.obtainMessage(MSG_SET_COLOR_VALUE, value, VALUE_COLOR_ENHANCEMENT_NATURE);
        if (delayed > 0) {
            this.mHandler.sendMessageDelayed(msg, (long) delayed);
        } else {
            this.mHandler.sendMessage(msg);
        }
    }

    public boolean isFeatureSupported(int feature) {
        boolean z = true;
        if (mLoadLibraryFailed) {
            Slog.e(TAG, "Comfort feature not supported because of library not found");
            return DEBUG;
        } else if (VALUE_COLOR_ENHANCEMENT_SRGB == feature) {
            if (VALUE_COLOR_ENHANCEMENT_SRGB != nativeGetFeatureSupported(feature)) {
                z = DEBUG;
            }
            return z;
        } else {
            if (nativeGetFeatureSupported(feature) == 0) {
                z = DEBUG;
            }
            return z;
        }
    }

    public int setDisplayEffectScene(int scene) {
        try {
            if (!mLoadLibraryFailed) {
                return nativeSetDisplayEffectScene(scene);
            }
            Slog.d(TAG, "nativeSetDisplayEffectScene not valid!");
            return VALUE_COLOR_ENHANCEMENT_NATURE;
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
            return VALUE_COLOR_ENHANCEMENT_NATURE;
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
            return VALUE_COLOR_ENHANCEMENT_NATURE;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativesetDisplayEffetSupported not found!");
            return VALUE_COLOR_ENHANCEMENT_NATURE;
        }
    }

    private void animationTo(int target, int delayed) {
        Slog.i(TAG, "animationTo target = " + target + ", delayed time = " + delayed);
        this.mHandler.removeMessages(VALUE_COLOR_ENHANCEMENT_SRGB);
        if (this.mAnimationTimes < VALUE_ANIMATION_MSG_TIMES) {
            this.mAnimationFlag += VALUE_COLOR_ENHANCEMENT_BRIGHT;
        } else {
            this.mAnimationFlag = VALUE_COLOR_ENHANCEMENT_BRIGHT;
        }
        this.mAnimationTimes = VALUE_COLOR_ENHANCEMENT_BRIGHT;
        this.mValueBeforeAnimation = this.mCurrentComfortValue;
        this.mValueAnimationTarget = target;
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(VALUE_COLOR_ENHANCEMENT_SRGB, this.mValueAnimationTarget, this.mAnimationFlag), (long) delayed);
    }
}
