package android.cover;

import android.content.Context;
import android.content.Intent;
import android.cover.ICoverManager.Stub;
import android.os.Debug;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.rms.iaware.AppTypeInfo;
import android.util.Flog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.huawei.hsm.permission.StubController;

public class CoverManager implements IHwCoverManager {
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
    private static final String KEYGUARD_PERMISSION = "android.permission.CONTROL_KEYGUARD";
    private static final String TAG = "CoverManger";
    private static final Object mInstanceSync = new Object();
    private static volatile CoverManager sSelf = null;
    private Context mContext;
    private LayoutParams mCoverItemparams;

    private static ICoverManager getCoverManagerService() {
        return Stub.asInterface(ServiceManager.getService(COVER_SERVICE));
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
            Flog.w(AppTypeInfo.PG_APP_TYPE_BROWSER, "CoverManagerService not started yet");
            return true;
        } catch (RemoteException e) {
            Flog.w(AppTypeInfo.PG_APP_TYPE_BROWSER, "isCoverOpen got RemoteException:", e);
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
            Flog.w(AppTypeInfo.PG_APP_TYPE_BROWSER, "setCoverForbiddened got RemoteException:", e);
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
        if (1 == Global.getInt(this.mContext.getContentResolver(), "cover_enabled", 1)) {
            Flog.i(AppTypeInfo.PG_APP_TYPE_BROWSER, "addCoverItemView isNeed = " + isNeed + " mDisablePower:" + mDisablePower + " view:" + view);
            WindowManager wm = (WindowManager) this.mContext.getSystemService(FreezeScreenScene.WINDOW_PARAM);
            this.mCoverItemparams = new LayoutParams(2101);
            this.mCoverItemparams.height = -1;
            this.mCoverItemparams.width = -1;
            this.mCoverItemparams.setTitle(COVER_NAME_PREFIX + this.mContext.getPackageName());
            LayoutParams layoutParams = this.mCoverItemparams;
            layoutParams.privateFlags |= -2147483632;
            layoutParams = this.mCoverItemparams;
            layoutParams.flags |= 768;
            if (activTime != 0) {
                this.mCoverItemparams.userActivityTimeout = (long) activTime;
            }
            if (mDisablePower) {
                layoutParams = this.mCoverItemparams;
                layoutParams.hwFlags |= AppTypeInfo.APP_ATTRIBUTE_OVERSEA;
            }
            if (isNeed) {
                layoutParams = this.mCoverItemparams;
                layoutParams.flags |= StubController.PERMISSION_CALENDAR;
            } else {
                layoutParams = this.mCoverItemparams;
                layoutParams.flags |= 1024;
            }
            layoutParams = this.mCoverItemparams;
            layoutParams.flags |= StubController.PERMISSION_CONTACTS_DELETE;
            layoutParams = this.mCoverItemparams;
            layoutParams.flags |= StubController.RMD_PERMISSION_CODE;
            this.mCoverItemparams.isEmuiStyle = 1;
            view.setSystemUiVisibility(StubController.PERMISSION_SMSLOG_WRITE);
            if (view.getParent() == null) {
                wm.addView(view, this.mCoverItemparams);
            }
            Intent intent = new Intent(ACTION_COVER_CHANGED);
            intent.putExtra(ACTION_TYPE_PARAM, 1);
            intent.putExtra(ACTION_PACKAGE, this.mContext.getPackageName());
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, ACTION_PERM_COVER);
            return;
        }
        Log.d(TAG, "return from addCoverItemView because the button is closed");
    }

    public void removeCoverItemView(View view) {
        Flog.i(AppTypeInfo.PG_APP_TYPE_BROWSER, "removeCoverItemView view = " + view);
        if (!(view == null || view.getParent() == null)) {
            ((WindowManager) this.mContext.getSystemService(FreezeScreenScene.WINDOW_PARAM)).removeViewImmediate(view);
        }
        if (this.mContext != null) {
            Intent intent = new Intent(ACTION_COVER_CHANGED);
            intent.putExtra(ACTION_TYPE_PARAM, 0);
            intent.putExtra(ACTION_PACKAGE, this.mContext.getPackageName());
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, ACTION_PERM_COVER);
        }
    }

    public void setCoverViewBinder(IBinder binder, Context context) {
        Flog.i(AppTypeInfo.PG_APP_TYPE_BROWSER, "setCoverBinder, binder = " + binder);
        if (context.checkCallingOrSelfPermission(KEYGUARD_PERMISSION) != 0) {
            Flog.w(AppTypeInfo.PG_APP_TYPE_BROWSER, "Caller needs permission 'android.permission.CONTROL_KEYGUARD' to call " + Debug.getCaller());
            throw new SecurityException("must have permission android.permission.CONTROL_KEYGUARD");
        }
        try {
            getCoverManagerService().setCoverViewBinder(binder);
        } catch (RemoteException e) {
            Flog.w(AppTypeInfo.PG_APP_TYPE_BROWSER, "setCoverBinder failed:", e);
        }
    }
}
