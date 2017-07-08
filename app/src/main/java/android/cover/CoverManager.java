package android.cover;

import android.content.Context;
import android.content.pm.ActivityInfoEx;
import android.cover.ICoverManager.Stub;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.attestation.HwAttestationStatus;

public class CoverManager implements IHwCoverManager {
    public static final String COVER_COVER_CLOCK_ACTION = "com.huawei.android.start.CoverClock";
    public static final String COVER_NAME_PREFIX = "Cover:";
    public static final String COVER_SERVICE = "cover";
    public static final String COVER_STATE = "coverOpen";
    public static final String COVER_STATE_CHANGED_ACTION = "com.huawei.android.cover.STATE";
    public static final int DEFAULT_COLOR = -16777216;
    private static final String KEYGUARD_PERMISSION = "android.permission.CONTROL_KEYGUARD";
    private static final String TAG = "CoverManger";
    private static final Object mInstanceSync;
    private static ICoverManager sCoverManagerService;
    private static volatile CoverManager sSelf;
    private Context mContext;
    private LayoutParams mCoverItemparams;

    static {
        mInstanceSync = new Object();
        sSelf = null;
    }

    private static ICoverManager getCoverManagerService() {
        synchronized (mInstanceSync) {
            if (sCoverManagerService != null) {
                ICoverManager iCoverManager = sCoverManagerService;
                return iCoverManager;
            }
            sCoverManagerService = Stub.asInterface(ServiceManager.getService(COVER_SERVICE));
            iCoverManager = sCoverManagerService;
            return iCoverManager;
        }
    }

    public static CoverManager getDefault() {
        if (sSelf == null) {
            sSelf = new CoverManager();
        }
        return sSelf;
    }

    public boolean isCoverOpen() {
        try {
            return getCoverManagerService().isCoverOpen();
        } catch (RemoteException e) {
            return true;
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
        if (view != null) {
            this.mContext = view.getContext();
            WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
            this.mCoverItemparams = new LayoutParams(2101);
            this.mCoverItemparams.height = -1;
            this.mCoverItemparams.width = -1;
            this.mCoverItemparams.setTitle(COVER_NAME_PREFIX + this.mContext.getPackageName());
            LayoutParams layoutParams = this.mCoverItemparams;
            layoutParams.privateFlags |= -2147483632;
            layoutParams = this.mCoverItemparams;
            layoutParams.flags |= 525056;
            if (activTime != 0) {
                this.mCoverItemparams.userActivityTimeout = (long) activTime;
            }
            if (mDisablePower) {
                layoutParams = this.mCoverItemparams;
                layoutParams.hwFlags |= AppOpsManagerEx.TYPE_NET;
            }
            if (isNeed) {
                layoutParams = this.mCoverItemparams;
                layoutParams.flags |= HwAttestationStatus.CERT_MAX_LENGTH;
            } else {
                layoutParams = this.mCoverItemparams;
                layoutParams.flags |= AppOpsManagerEx.TYPE_CAMERA;
            }
            view.setSystemUiVisibility(ActivityInfoEx.CONFIG_SIMPLEUI);
            if (view.getParent() == null) {
                wm.addView(view, this.mCoverItemparams);
            }
        }
    }

    public void removeCoverItemView(View view) {
        if (view != null && view.getParent() != null) {
            ((WindowManager) this.mContext.getSystemService("window")).removeViewImmediate(view);
        }
    }

    public void setCoverViewBinder(IBinder binder, Context context) {
        Log.d(TAG, "setCoverBinder, binder = " + binder);
        if (context.checkCallingOrSelfPermission(KEYGUARD_PERMISSION) != 0) {
            Log.w(TAG, "Caller needs permission 'android.permission.CONTROL_KEYGUARD' to call " + Debug.getCaller());
            throw new SecurityException("must have permission android.permission.CONTROL_KEYGUARD");
        }
        try {
            getCoverManagerService().setCoverViewBinder(binder);
        } catch (RemoteException e) {
            Log.w(TAG, "setCoverBinder failed:", e);
        }
    }
}
