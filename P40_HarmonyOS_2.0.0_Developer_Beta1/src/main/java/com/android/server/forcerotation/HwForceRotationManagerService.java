package com.android.server.forcerotation;

import android.content.Context;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.server.wm.ActivityRecord;
import com.huawei.forcerotation.IHwForceRotationManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwForceRotationManagerService extends IHwForceRotationManager.Stub {
    private static final int MSG_SHOW_TOAST = 1;
    private static final String TAG = "HwForceRotationService";
    private Context mContext;
    private HwForceRotationLayout mFixedLandscapeLayout;
    private List<ForceRotationAppInfo> mForceRotationAppInfos;
    private HwForceRotationConfig mForceRotationConfig;
    private Handler mHandler = new Handler() {
        /* class com.android.server.forcerotation.HwForceRotationManagerService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwForceRotationManagerService.this.showToast();
            }
        }
    };
    private boolean mIsAppInForceRotationWhiteList = false;
    private String mPrvCompontentName = "";
    private String mPrvPackageName = "";
    private String mTmpAppName;
    private Map<String, AppToastInfo> mToastedAppInfos;

    /* access modifiers changed from: protected */
    public void showToast() {
        Toast.makeText(this.mContext, 33685953, 0).show();
    }

    public HwForceRotationManagerService(Context context, Handler uiHandler) {
        this.mContext = context;
        this.mForceRotationAppInfos = new ArrayList();
        this.mFixedLandscapeLayout = new HwForceRotationLayout(this.mContext, uiHandler, this);
        this.mForceRotationConfig = new HwForceRotationConfigLoader().load();
        this.mToastedAppInfos = new HashMap();
    }

    public boolean isForceRotationSwitchOpen() {
        Context context = this.mContext;
        if (context != null && context.getContentResolver() != null && Settings.System.getInt(this.mContext.getContentResolver(), "force_rotation_mode", 0) == 1 && (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer())) {
            return true;
        }
        return false;
    }

    public synchronized boolean isAppInForceRotationWhiteList(String packageName) {
        return this.mForceRotationConfig.isAppSupportForceRotation(packageName);
    }

    public synchronized boolean isAppForceLandRotatable(String packageName, IBinder aToken) {
        if (!this.mForceRotationConfig.isAppSupportForceRotation(packageName)) {
            return false;
        }
        return isAppForceLandRotatable(aToken);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a8, code lost:
        r0 = r3;
     */
    public synchronized boolean isAppForceLandRotatable(IBinder aToken) {
        boolean z;
        ForceRotationAppInfo tmpFRAI;
        ForceRotationAppInfo portaitFRAI = null;
        ForceRotationAppInfo landscapeFRAI = null;
        Iterator<ForceRotationAppInfo> iter = this.mForceRotationAppInfos.iterator();
        while (true) {
            z = true;
            if (!iter.hasNext()) {
                break;
            }
            tmpFRAI = iter.next();
            IBinder tmpToken = tmpFRAI.getmAppToken().get();
            int tmpOrientation = tmpFRAI.getmOrientation();
            if (ActivityRecord.forToken(tmpToken) != null) {
                if (aToken == tmpToken) {
                    if (tmpOrientation != 1 && tmpOrientation != 7 && tmpOrientation != 9) {
                        if (tmpOrientation != 12) {
                            if (tmpOrientation == 0 || tmpOrientation == 6 || tmpOrientation == 8 || tmpOrientation == 11 || tmpOrientation == -1 || tmpOrientation == 4 || tmpOrientation == 5 || tmpOrientation == 2 || tmpOrientation == 13) {
                                break;
                            } else if (tmpOrientation == 10) {
                                break;
                            } else {
                                Slog.d(TAG, "utk:pn=" + tmpFRAI.getmPackageName() + ", o=" + tmpOrientation);
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    continue;
                }
            } else {
                Slog.d(TAG, "ftk:pn=" + tmpFRAI.getmPackageName() + ", o=" + tmpOrientation);
                iter.remove();
            }
        }
        landscapeFRAI = tmpFRAI;
        if (portaitFRAI == null && landscapeFRAI != null) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public synchronized ForceRotationAppInfo queryForceRotationAppInfo(IBinder aToken) {
        ForceRotationAppInfo frai;
        frai = null;
        Iterator<ForceRotationAppInfo> iter = this.mForceRotationAppInfos.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            ForceRotationAppInfo tmpFRAI = iter.next();
            IBinder tmpToken = tmpFRAI.getmAppToken().get();
            if (ActivityRecord.forToken(tmpToken) == null) {
                iter.remove();
            } else if (aToken == tmpToken) {
                frai = tmpFRAI;
                break;
            }
        }
        return frai;
    }

    public synchronized boolean saveOrUpdateForceRotationAppInfo(String packageName, String componentName, IBinder aToken, int reqOrientation) {
        if (!this.mForceRotationConfig.isAppSupportForceRotation(packageName)) {
            if (packageName != null && !this.mPrvPackageName.equals(packageName)) {
                this.mPrvPackageName = packageName;
                Slog.i(TAG, "isAppSupportForceRotation-f,pn = " + packageName);
            }
            return false;
        } else if (!this.mForceRotationConfig.isActivitySupportForceRotation(componentName)) {
            if (componentName != null && !this.mPrvCompontentName.equals(componentName)) {
                this.mPrvCompontentName = componentName;
                Slog.i(TAG, "isActivitySupportForceRotation-t,cn = " + componentName);
            }
            return false;
        } else {
            saveOrUpdateForceRotationAppInfo(packageName, aToken, reqOrientation);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void saveOrUpdateForceRotationAppInfo(String packageName, IBinder aToken, int reqOrientation) {
        ForceRotationAppInfo frai = queryForceRotationAppInfo(aToken);
        if (frai == null) {
            this.mForceRotationAppInfos.add(new ForceRotationAppInfo(packageName, aToken, reqOrientation));
        } else if (reqOrientation != frai.getmOrientation()) {
            frai.setmOrientation(reqOrientation);
        }
    }

    public synchronized void showToastIfNeeded(String packageName, int pid, String processName, IBinder aToken) {
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        if (dm.widthPixels >= dm.heightPixels) {
            if (isAppForceLandRotatable(packageName, aToken)) {
                if (!TextUtils.isEmpty(packageName) && pid > 0) {
                    AppToastInfo tmp = this.mToastedAppInfos.get(packageName);
                    if (tmp == null || (pid != tmp.getmPid() && processName.equals(tmp.getmProcessName()))) {
                        if (tmp == null) {
                            tmp = new AppToastInfo(packageName, processName, pid);
                        } else {
                            tmp.setmPid(pid);
                        }
                        this.mToastedAppInfos.put(packageName, tmp);
                        Message msg = this.mHandler.obtainMessage();
                        msg.what = 1;
                        Slog.v(TAG, "show Toast message in package:" + packageName);
                        this.mHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    public void applyForceRotationLayout(IBinder aToken, Rect vf) {
        Rect dv = null;
        HwForceRotationLayout hwForceRotationLayout = this.mFixedLandscapeLayout;
        if (hwForceRotationLayout != null) {
            dv = hwForceRotationLayout.getForceRotationLayout();
        }
        if (dv != null) {
            vf.set(dv);
        }
    }

    public int recalculateWidthForForceRotation(int width, int height, int logicalHeight, String packageName) {
        if (width <= height || UserHandle.isIsolated(Binder.getCallingUid()) || !isForceRotationSwitchOpen()) {
            return width;
        }
        if (packageName != null && !packageName.equals(this.mTmpAppName)) {
            this.mIsAppInForceRotationWhiteList = isAppInForceRotationWhiteList(packageName);
            this.mTmpAppName = packageName;
        }
        return this.mIsAppInForceRotationWhiteList ? logicalHeight : width;
    }
}
