package com.android.server.wm;

import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.os.IBinder;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.HwMwUtils;
import android.view.IApplicationToken;
import com.huawei.server.HwPartIawareUtil;

public class HwAppWindowTokenImpl extends HwAppWindowTokenBridgeEx {
    private static final int RETURN_CONTINUE_STARTWINDOW = 0;
    private static final int RETURN_CONTINUE_STARTWINDOW_AND_UPDATE_TRANSFROM = 1;
    private static final int RETURN_NOT_CONTINUE_STARTWINDOW = -1;
    private static final String TAG = "HwAppWindowTokenImpl";

    public boolean isHwStartWindowEnabled(String pkg, CompatibilityInfo compatInfo) {
        int appType;
        if (pkg == null || compatInfo == null) {
            return false;
        }
        if (HwPartIawareUtil.isStartWindowEnable() && compatInfo.mAppInfo != null) {
            if ((!compatInfo.mAppInfo.isSystemApp() && !compatInfo.mAppInfo.isPrivilegedApp() && !compatInfo.mAppInfo.isUpdatedSystemApp()) && (appType = AppTypeRecoManager.getInstance().getAppType(pkg)) != -1 && appType != 13 && appType != 28 && appType != 301 && appType != 309) {
                return true;
            }
        }
        return false;
    }

    public int continueHwStartWindow(ApplicationInfo appInfo, IBinder transferFrom, IApplicationToken token, boolean[] windowArgs) {
        if (appInfo == null || windowArgs == null || windowArgs.length != 7 || windowArgs[1] || token == null) {
            return -1;
        }
        boolean windowIsTranslucent = windowArgs[2];
        boolean windowDisableStarting = windowArgs[3];
        AwareLog.i(TAG, "addHwStartingWindow Translucent=" + windowIsTranslucent + " DisableStarting=" + windowDisableStarting + " processRunning=" + windowArgs[0]);
        IBinder binder = token.asBinder();
        boolean taskSwitch = windowArgs[4];
        boolean windowShowWallpaper = windowArgs[5];
        boolean fromRecents = windowArgs[6];
        if (!HwStartWindowRecord.getInstance().isStartWindowApp(Integer.valueOf(appInfo.uid))) {
            if (taskSwitch && (windowDisableStarting || windowIsTranslucent || windowShowWallpaper) && (HwStartWindowRecord.getInstance().getStartFromMainAction(Integer.valueOf(appInfo.uid)) || fromRecents)) {
                HwStartWindowRecord.getInstance().updateStartWindowApp(Integer.valueOf(appInfo.uid), binder);
                return 0;
            }
            HwStartWindowRecord.getInstance().resetStartWindowApp(Integer.valueOf(appInfo.uid));
        }
        if (HwStartWindowRecord.getInstance().checkStartWindowApp(Integer.valueOf(appInfo.uid))) {
            if (transferFrom == null) {
                return 1;
            }
        } else if (windowIsTranslucent || windowDisableStarting) {
            return -1;
        }
        return 0;
    }

    public boolean isHwMwAnimationBelowStack(AppWindowTokenExt appWindowToken) {
        if (HwMwUtils.ENABLED && appWindowToken != null && !appWindowToken.isAppWindowTokenNull() && appWindowToken.isHwActivityRecord() && appWindowToken.inHwMagicWindowingMode()) {
            boolean isAniRunningBelow = appWindowToken.isHwActivityAniRunningBelow();
            boolean isExitInActivityOpen = !appWindowToken.getEnteringAnimation() && appWindowToken.getTransit() == 6;
            boolean isEnterInActivityClose = appWindowToken.getEnteringAnimation() && appWindowToken.getTransit() == 7;
            if (isAniRunningBelow || isExitInActivityOpen || isEnterInActivityClose) {
                return true;
            }
        }
        return false;
    }

    public IBinder getTransferFrom(ApplicationInfo appInfo) {
        if (appInfo == null) {
            return null;
        }
        return HwStartWindowRecord.getInstance().getTransferFromStartWindowApp(Integer.valueOf(appInfo.uid));
    }

    public void cancelInputMethodRetractAnimation(WindowStateEx inputMethodWindow) {
        if (!inputMethodWindow.isEntranceAnimation()) {
            inputMethodWindow.cancelAnimation();
        }
    }
}
