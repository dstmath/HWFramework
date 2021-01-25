package com.android.server.imm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import android.view.inputmethod.CursorAnchorInfo;
import com.android.internal.annotations.GuardedBy;
import com.android.server.gesture.DefaultGestureNavConst;
import com.huawei.android.inputmethod.IHwInputContentListener;
import com.huawei.android.inputmethod.IHwInputMethodListener;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwInputMethodManagerServiceEx implements IHwInputMethodManagerServiceEx {
    private static int AFT_EVENT = 1;
    private static int AFT_EVENT_ID = 991310177;
    private static final int BAIDU_CHANGER_IME = 19;
    private static String BAIDU_INPUT_PACKAGE_NAME = "com.baidu.input_huawei";
    private static final String BAI_DU_IME = "com.baidu.input_huawei/.ImeService";
    private static int BETA_VERSION_IN_CHINA_AREA = 3;
    private static final int FOCUS_IN_VIEW = 18;
    private static String PHONE_BEGIN_TIME = "phone_begin_time";
    private static final int PRESS_VOICE_INPUT_KEY = 16;
    private static final int RAISE_VOICE_INPUT_KEY = 17;
    private static int SEND_HIEVENT_FAIL = -1;
    private static int SEND_HIEVENT_SUCCESS = 1;
    static final String TAG = "HwInputMethodManagerServiceEx";
    private static String USER_TYPE = "ro.logsystem.usertype";
    private Context mContext;
    private IHwInputContentListener mHwInputContentListener;
    private IHwInputMethodListener mHwInputMethodListener;
    IHwInputMethodManagerInner mIImsInner = null;
    private boolean mIsChangeDefaultIme = false;
    private boolean mIsChangeLastIme = false;

    public HwInputMethodManagerServiceEx(IHwInputMethodManagerInner iims, Context context) {
        this.mIImsInner = iims;
        this.mContext = context;
        if (isChinaBetaVersion() && TextUtils.isEmpty(Settings.Secure.getString(this.mContext.getContentResolver(), PHONE_BEGIN_TIME))) {
            Settings.Secure.putString(this.mContext.getContentResolver(), PHONE_BEGIN_TIME, Long.toString(System.currentTimeMillis()));
        }
    }

    public boolean isTriNavigationBar(Context context) {
        boolean isEnableNavBar = Settings.System.getIntForUser(context.getContentResolver(), "enable_navbar", getNaviBarEnabledDefValue(), -2) != 0;
        boolean isGestureNavigation = Settings.Secure.getIntForUser(context.getContentResolver(), DefaultGestureNavConst.KEY_SECURE_GESTURE_NAVIGATION, 0, -2) != 0;
        Slog.i(TAG, "--- show navigation bar status: isEnableNavBar = " + isEnableNavBar + " ,isGestureNavigation = " + isGestureNavigation);
        if (!isEnableNavBar) {
            return false;
        }
        return !isGestureNavigation;
    }

    @GuardedBy({"mHwInputMethodListener"})
    public void registerInputMethodListener(IHwInputMethodListener listener) {
        this.mHwInputMethodListener = listener;
    }

    @GuardedBy({"mHwInputMethodListener"})
    public void unregisterInputMethodListener() {
        this.mHwInputMethodListener = null;
    }

    @GuardedBy({"mHwInputMethodListener"})
    public void onStartInput() {
        IHwInputMethodListener iHwInputMethodListener = this.mHwInputMethodListener;
        if (iHwInputMethodListener != null) {
            try {
                iHwInputMethodListener.onStartInput();
            } catch (RemoteException e) {
                Slog.e(TAG, "onInputStart RemoteException");
            }
        }
    }

    @GuardedBy({"mHwInputMethodListener"})
    public void onFinishInput() {
        IHwInputMethodListener iHwInputMethodListener = this.mHwInputMethodListener;
        if (iHwInputMethodListener != null) {
            try {
                iHwInputMethodListener.onFinishInput();
            } catch (RemoteException e) {
                Slog.e(TAG, "onFinishInput RemoteException");
            }
        }
    }

    @GuardedBy({"mHwInputMethodListener"})
    public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
        IHwInputMethodListener iHwInputMethodListener = this.mHwInputMethodListener;
        if (iHwInputMethodListener != null) {
            try {
                iHwInputMethodListener.onUpdateCursorAnchorInfo(cursorAnchorInfo);
            } catch (RemoteException e) {
                Slog.e(TAG, "onUpdateCursorAnchorInfo RemoteException");
            }
        }
    }

    @GuardedBy({"mHwInputContentListener"})
    public void registerInputContentListener(IHwInputContentListener listener) {
        Slog.d(TAG, "registerInputContentListener");
        this.mHwInputContentListener = listener;
    }

    @GuardedBy({"mHwInputContentListener"})
    public void unregisterInputContentListener() {
        Slog.d(TAG, "unregisterInputContentListener");
        this.mHwInputContentListener = null;
    }

    @GuardedBy({"mHwInputContentListener"})
    public void onReceivedInputContent(String content) {
        IHwInputContentListener iHwInputContentListener = this.mHwInputContentListener;
        if (iHwInputContentListener != null) {
            try {
                iHwInputContentListener.onReceivedInputContent(content);
            } catch (RemoteException e) {
                Slog.e(TAG, "onReceivedInputContent RemoteException");
            }
        } else {
            Slog.w(TAG, "null input content listener");
        }
    }

    @GuardedBy({"mHwInputMethodListener"})
    public void onShowInputRequested() {
        IHwInputMethodListener iHwInputMethodListener = this.mHwInputMethodListener;
        if (iHwInputMethodListener != null) {
            try {
                iHwInputMethodListener.onShowInputRequested();
            } catch (RemoteException e) {
                Slog.e(TAG, "onShowInputRequested RemoteException");
            }
        }
    }

    @GuardedBy({"mHwInputMethodListener"})
    public void onContentChanged(String text) {
        IHwInputMethodListener iHwInputMethodListener = this.mHwInputMethodListener;
        if (iHwInputMethodListener != null) {
            try {
                iHwInputMethodListener.onContentChanged(text);
            } catch (RemoteException e) {
                Slog.e(TAG, "onContentChanged RemoteException");
            }
        }
    }

    @GuardedBy({"mHwInputContentListener"})
    public void onReceivedComposingText(String content) {
        IHwInputContentListener iHwInputContentListener = this.mHwInputContentListener;
        if (iHwInputContentListener != null) {
            try {
                iHwInputContentListener.onReceivedComposingText(content);
            } catch (RemoteException e) {
                Slog.e(TAG, "onReceivedComposingText RemoteException");
            }
        } else {
            Slog.w(TAG, "null input content listener");
        }
    }

    private int getNaviBarEnabledDefValue() {
        int defValue;
        boolean frontFingerprintNavigation = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
        int frontFingerprintNavigationTrikey = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
        if (!frontFingerprintNavigation) {
            defValue = 1;
        } else {
            boolean isTrikeyExist = isTrikeyExist();
            if (frontFingerprintNavigationTrikey == 1 && isTrikeyExist) {
                defValue = 0;
            } else if (SystemProperties.get("ro.config.hw_optb", "0").equals("156")) {
                defValue = 0;
            } else {
                defValue = 1;
            }
        }
        Slog.i(TAG, "NaviBar defValue = " + defValue);
        return defValue;
    }

    private boolean isTrikeyExist() {
        try {
            Class clazz = Class.forName("huawei.android.os.HwGeneralManager");
            return ((Boolean) clazz.getDeclaredMethod("isSupportTrikey", new Class[0]).invoke(clazz.getDeclaredMethod("getInstance", new Class[0]).invoke(clazz, new Object[0]), new Object[0])).booleanValue();
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
            Slog.e(TAG, "isTrikeyExist, reflect method handle, and has exception: " + e);
            return false;
        } catch (Exception ex) {
            Slog.e(TAG, "isTrikeyExist, other exception: " + ex);
            return false;
        }
    }

    public synchronized int sendEventData(int dataType, String str) {
        if (!isChinaBetaVersion()) {
            return SEND_HIEVENT_FAIL;
        }
        int uid = Binder.getCallingUid();
        try {
            if (uid != this.mContext.getPackageManager().getPackageUid(BAIDU_INPUT_PACKAGE_NAME, 0)) {
                Slog.e(TAG, uid + " has no permssion to use");
                return SEND_HIEVENT_FAIL;
            } else if (dataType == AFT_EVENT) {
                return handleAFTData(str);
            } else {
                return SEND_HIEVENT_FAIL;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.i(TAG, "the package is not exist.");
            return SEND_HIEVENT_FAIL;
        } catch (Exception e2) {
            Slog.i(TAG, "get package uid fail.");
            return SEND_HIEVENT_FAIL;
        }
    }

    private int handleAFTData(String str) {
        HiEvent event = new HiEvent(AFT_EVENT_ID);
        try {
            JSONObject json = new JSONObject(str);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key != null) {
                    if (isIntValue(key)) {
                        event.putInt(key, json.getInt(key));
                    } else if (isBooleanValue(key)) {
                        event.putBool(key, json.getBoolean(key));
                    } else {
                        event.putString(key, json.getString(key));
                    }
                }
            }
        } catch (JSONException e) {
            Slog.e(TAG, "json exception");
        }
        event.putInt("nav", getNavigationBarState());
        event.putString("useTime", Settings.Secure.getString(this.mContext.getContentResolver(), PHONE_BEGIN_TIME));
        HiView.report(event);
        return SEND_HIEVENT_SUCCESS;
    }

    private int getNavigationBarState() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), DefaultGestureNavConst.KEY_SECURE_GESTURE_NAVIGATION, 0);
    }

    private boolean isChinaBetaVersion() {
        if (SystemProperties.getInt(USER_TYPE, 0) != BETA_VERSION_IN_CHINA_AREA) {
            return false;
        }
        return true;
    }

    private boolean isIntValue(String key) {
        if (key.equals("hand")) {
            return true;
        }
        return false;
    }

    private boolean isBooleanValue(String key) {
        if (key.equals("upChar") || key.equals("panelSplit") || key.equals("orientation")) {
            return true;
        }
        return false;
    }

    public void handleChangeInputMsg(int changeInputReason) {
        switch (changeInputReason) {
            case 16:
                if (!this.mIsChangeDefaultIme) {
                    Slog.i(TAG, "change baidu inputmethod");
                    this.mIsChangeDefaultIme = true;
                    this.mIsChangeLastIme = false;
                    this.mIImsInner.changeInputMethod(BAI_DU_IME);
                    return;
                }
                return;
            case 17:
                if (this.mIsChangeDefaultIme) {
                    if (this.mIsChangeLastIme) {
                        Slog.i(TAG, "press key change default inputmethod");
                        this.mIImsInner.changeInputMethod("");
                        this.mIsChangeDefaultIme = false;
                    }
                    this.mIsChangeLastIme = true;
                    return;
                }
                return;
            case 18:
                if (this.mIsChangeDefaultIme) {
                    this.mIImsInner.changeInputMethod("");
                    Slog.i(TAG, "focus in change default inputmethod");
                    this.mIsChangeDefaultIme = false;
                    return;
                }
                return;
            case 19:
                if (this.mIsChangeDefaultIme) {
                    Slog.i(TAG, "baidu change default inputmethod");
                    this.mIImsInner.changeInputMethod("");
                    this.mIsChangeDefaultIme = false;
                    return;
                }
                return;
            default:
                return;
        }
    }
}
