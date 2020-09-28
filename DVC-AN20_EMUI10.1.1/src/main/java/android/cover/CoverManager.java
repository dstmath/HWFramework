package android.cover;

import android.content.Context;
import android.content.Intent;
import android.cover.ICoverManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.huawei.android.os.DebugEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.view.ViewEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.bd.Reporter;

public class CoverManager extends DefaultCoverManager {
    private static final String ACTION_COVER_CHANGED = "com.huawei.action.coverview_window_changed";
    private static final String ACTION_PACKAGE = "package";
    private static final String ACTION_PERM_COVER = "com.android.keyguard.permission.RECEIVE_COVER_STATE";
    private static final String ACTION_TYPE_PARAM = "change_type";
    public static final String COVER_COVER_CLOCK_ACTION = "com.huawei.android.start.CoverClock";
    public static final String COVER_NAME_PREFIX = "Cover:";
    public static final String COVER_SERVICE = "cover";
    public static final String COVER_STATE = "coverOpen";
    public static final String COVER_STATE_CHANGED_ACTION = "com.huawei.android.cover.STATE";
    public static final int DEFAULT_COLOR = -16777216;
    public static final String HALL_STATE_RECEIVER_ASSOCIATED = "associated";
    public static final String HALL_STATE_RECEIVER_AUDIO = "audioserver";
    public static final String HALL_STATE_RECEIVER_CAMERA = "cameraserver";
    public static final String HALL_STATE_RECEIVER_DEFINE = "android";
    public static final String HALL_STATE_RECEIVER_FACE = "facerecognize";
    public static final String HALL_STATE_RECEIVER_GETSTATE = "getstate";
    public static final String HALL_STATE_RECEIVER_PHONE = "com.android.phone";
    private static final String KEYGUARD_PERMISSION = "android.permission.CONTROL_KEYGUARD";
    private static final String TAG = "CoverManger";
    private static final Object mInstanceSync = new Object();
    private static volatile CoverManager sSelf = null;
    private Context mContext;
    private WindowManager.LayoutParams mCoverItemparams;

    private static ICoverManager getCoverManagerService() {
        return ICoverManager.Stub.asInterface(ServiceManagerEx.getService(COVER_SERVICE));
    }

    public static CoverManager getDefault() {
        if (sSelf == null) {
            sSelf = new CoverManager();
        }
        return sSelf;
    }

    public boolean isCoverOpen() {
        try {
            ICoverManager ic = getCoverManagerService();
            if (ic != null) {
                return ic.isCoverOpen();
            }
            Flog.w(306, "CoverManagerService not started yet");
            return true;
        } catch (RemoteException e) {
            Flog.w(306, "isCoverOpen got RemoteException:", e);
            return true;
        }
    }

    public boolean setCoverForbiddened(boolean forbiddened) {
        try {
            ICoverManager ic = getCoverManagerService();
            if (ic == null) {
                return false;
            }
            return ic.setCoverForbiddened(forbiddened);
        } catch (RemoteException e) {
            Flog.w(306, "setCoverForbiddened got RemoteException:", e);
            return false;
        }
    }

    public int getHallState(int hallType) {
        try {
            ICoverManager ic = getCoverManagerService();
            if (ic != null) {
                return ic.getHallState(hallType);
            }
            Flog.w(306, "getHallState get CoverManagerService fail");
            return -1;
        } catch (RemoteException e) {
            Flog.w(306, "getHallState get RemoteException:", e);
            return -1;
        }
    }

    public boolean registerHallCallback(String receiverName, int hallType, IHallCallback callback) {
        try {
            ICoverManager ic = getCoverManagerService();
            if (ic != null) {
                return ic.registerHallCallback(receiverName, hallType, callback);
            }
            Flog.w(306, "registerHallCallback get CoverManagerService fail");
            return false;
        } catch (RemoteException e) {
            Flog.w(306, "registerHallCallback get RemoteException:", e);
            return false;
        }
    }

    public boolean unRegisterHallCallback(String receiverName, int hallType) {
        try {
            ICoverManager ic = getCoverManagerService();
            if (ic != null) {
                return ic.unRegisterHallCallback(receiverName, hallType);
            }
            Flog.w(306, "unRegisterHallCallback get CoverManagerService fail");
            return false;
        } catch (RemoteException e) {
            Flog.w(306, "unRegisterHallCallback get RemoteException:", e);
            return false;
        }
    }

    public boolean unRegisterHallCallbackEx(int hallType, IHallCallback callback) {
        try {
            ICoverManager ic = getCoverManagerService();
            if (ic != null) {
                return ic.unRegisterHallCallbackEx(hallType, callback);
            }
            Flog.w(306, "unRegisterHallCallbackEx get CoverManagerService fail");
            return false;
        } catch (RemoteException e) {
            Flog.w(306, "unRegisterHallCallbackEx get RemoteException:", e);
            return false;
        }
    }

    public void addCoverItemView(View view, boolean isNeed) {
        addCoverItemView(view, isNeed, false, 0);
    }

    public void addCoverItemView(View view, boolean isNeed, int activTime) {
        addCoverItemView(view, isNeed, false, activTime);
    }

    public void addCoverItemView(View view, boolean isNeed, boolean mDisablePower) {
        addCoverItemView(view, isNeed, mDisablePower, 0);
    }

    public void addCoverItemView(View view, boolean isNeed, boolean mDisablePower, int activTime) {
        if (view == null) {
            Log.d(TAG, "return from addCoverItemView because view is null");
            return;
        }
        this.mContext = view.getContext();
        if (!(1 == Settings.Global.getInt(this.mContext.getContentResolver(), "cover_enabled", 1))) {
            Log.d(TAG, "return from addCoverItemView because the button is closed");
            return;
        }
        Flog.i(306, "addCoverItemView isNeed = " + isNeed + " mDisablePower:" + mDisablePower + " view:" + view);
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        this.mCoverItemparams = new WindowManager.LayoutParams(2101);
        WindowManager.LayoutParams layoutParams = this.mCoverItemparams;
        layoutParams.height = -1;
        layoutParams.width = -1;
        layoutParams.setTitle(COVER_NAME_PREFIX + this.mContext.getPackageName());
        WindowManagerEx.LayoutParamsEx layoutParamsEx = new WindowManagerEx.LayoutParamsEx(this.mCoverItemparams);
        layoutParamsEx.addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers() | WindowManagerEx.LayoutParamsEx.getPrivateFlagHideNaviBar());
        this.mCoverItemparams.flags |= 768;
        if (activTime != 0) {
            WindowManagerEx.LayoutParamsEx.setUserActivityTimeout(this.mCoverItemparams, activTime);
        }
        if (mDisablePower) {
            layoutParamsEx.addHwFlags(Integer.MIN_VALUE);
        }
        if (isNeed) {
            this.mCoverItemparams.flags |= 2048;
        } else {
            this.mCoverItemparams.flags |= Reporter.MAX_CONTENT_SIZE;
        }
        this.mCoverItemparams.flags |= 131072;
        this.mCoverItemparams.flags |= 67108864;
        layoutParamsEx.setIsEmuiStyle(1);
        view.setSystemUiVisibility(ViewEx.getStatusBarFlag(0));
        if (view.getParent() == null) {
            wm.addView(view, this.mCoverItemparams);
        }
        Intent intent = new Intent(ACTION_COVER_CHANGED);
        intent.putExtra(ACTION_TYPE_PARAM, 1);
        intent.putExtra(ACTION_PACKAGE, this.mContext.getPackageName());
        this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, ACTION_PERM_COVER);
    }

    public void removeCoverItemView(View view) {
        Flog.i(306, "removeCoverItemView view = " + view);
        if (!(view == null || view.getParent() == null)) {
            ((WindowManager) this.mContext.getSystemService("window")).removeViewImmediate(view);
        }
        if (this.mContext != null) {
            Intent intent = new Intent(ACTION_COVER_CHANGED);
            intent.putExtra(ACTION_TYPE_PARAM, 0);
            intent.putExtra(ACTION_PACKAGE, this.mContext.getPackageName());
            this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, ACTION_PERM_COVER);
        }
    }

    public void setCoverViewBinder(IBinder binder, Context context) {
        Flog.i(306, "setCoverBinder, binder = " + binder);
        if (context.checkCallingOrSelfPermission(KEYGUARD_PERMISSION) == 0) {
            try {
                ICoverManager iCoverManager = getCoverManagerService();
                if (iCoverManager != null) {
                    iCoverManager.setCoverViewBinder(binder);
                }
            } catch (RemoteException e) {
                Flog.w(306, "setCoverBinder failed:", e);
            }
        } else {
            Flog.w(306, "Caller needs permission 'android.permission.CONTROL_KEYGUARD' to call " + DebugEx.getCaller());
            throw new SecurityException("must have permission android.permission.CONTROL_KEYGUARD");
        }
    }
}
