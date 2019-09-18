package com.android.server.wm;

import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.view.IApplicationToken;
import com.android.server.AttributeCache;
import com.android.server.rms.iaware.feature.StartWindowFeature;

public class HwAppWindowContainerControllerImpl implements IHwAppWindowContainerController {
    private static final int RETURN_CONTINUE_STARTWINDOW = 0;
    private static final int RETURN_CONTINUE_STARTWINDOW_AND_UPDATE_TRANSFROM = 1;
    private static final int RETURN_NOT_CONTINUE_STARTWINDOW = -1;
    private static final String TAG = "HwAppWindowContainerControllerImpl";

    public boolean isHwStartWindowEnabled(String pkg) {
        if (StartWindowFeature.isStartWindowEnable()) {
            int appType = AppTypeRecoManager.getInstance().getAppType(pkg);
            if (!(appType == -1 || appType == 13 || appType == 309)) {
                return true;
            }
        }
        return false;
    }

    public int continueHwStartWindow(String pkg, AttributeCache.Entry ent, ApplicationInfo appInfo, boolean processRunning, boolean windowIsFloating, boolean windowIsTranslucent, boolean windowDisableStarting, boolean newTask, boolean taskSwitch, boolean windowShowWallpaper, IBinder transferFrom, IApplicationToken token, RootWindowContainer root, boolean fromRecents) {
        if (windowIsFloating || token == null) {
            ApplicationInfo applicationInfo = appInfo;
            boolean z = windowIsTranslucent;
            return -1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("addHwStartingWindow Translucent=");
        boolean z2 = windowIsTranslucent;
        sb.append(z2);
        sb.append(" DisableStarting=");
        boolean z3 = windowDisableStarting;
        sb.append(z3);
        sb.append(" processRunning=");
        boolean z4 = processRunning;
        sb.append(z4);
        AwareLog.i(TAG, sb.toString());
        return updateStartWindowAppStatus(appInfo.uid, token.asBinder(), newTask, taskSwitch, z4, z3, z2, windowShowWallpaper, transferFrom, root, fromRecents);
    }

    private int updateStartWindowAppStatus(int appUid, IBinder binder, boolean newTask, boolean taskSwitch, boolean processRunning, boolean windowDisableStarting, boolean windowIsTranslucent, boolean windowShowWallpaper, IBinder transferFrom, RootWindowContainer root, boolean fromRecents) {
        boolean notContinueStartWindow = true;
        if (!HwStartWindowRecord.getInstance().isStartWindowApp(Integer.valueOf(appUid))) {
            if (taskSwitch && (windowDisableStarting || windowIsTranslucent || windowShowWallpaper) && (HwStartWindowRecord.getInstance().getStartFromMainAction(Integer.valueOf(appUid)) || fromRecents)) {
                HwStartWindowRecord.getInstance().updateStartWindowApp(Integer.valueOf(appUid), binder);
                return 0;
            }
            IBinder iBinder = binder;
            HwStartWindowRecord.getInstance().resetStartWindowApp(Integer.valueOf(appUid));
        } else {
            IBinder iBinder2 = binder;
        }
        if (!HwStartWindowRecord.getInstance().checkStartWindowApp(Integer.valueOf(appUid))) {
            if (!windowIsTranslucent && !windowDisableStarting) {
                notContinueStartWindow = false;
            }
            if (notContinueStartWindow) {
                return -1;
            }
        } else if (transferFrom == null) {
            return 1;
        }
        return 0;
    }

    public IBinder getTransferFrom(ApplicationInfo appInfo) {
        return HwStartWindowRecord.getInstance().getTransferFromStartWindowApp(Integer.valueOf(appInfo.uid));
    }
}
