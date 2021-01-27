package com.android.server.wm;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import com.huawei.android.app.HiViewEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HomePageDetect {
    private static final String TAG = "HWMW_HomePageDetect";
    private Map<String, String> mHosts = new HashMap();
    private HwMagicWinManager mMwManager;
    private SharedParameters mSharedParameters;

    public HomePageDetect(SharedParameters parameters) {
        this.mSharedParameters = parameters;
        this.mMwManager = this.mSharedParameters.getMwWinManager();
    }

    public void getDetectedParam(String packageName, Bundle result) {
        if (packageName != null && result != null) {
            boolean isNeedDetect = isNeedDect(packageName);
            result.putBoolean("NEED_HOST_DETECT", isNeedDetect);
            if (isNeedDetect) {
                result.putInt("VIEW_COUNT", this.mMwManager.getLocalSysCfg().getHostViewThreshold());
            }
        }
    }

    public void putDetectedResult(ActivityRecordEx detectedActivity, Bundle result) {
        long callingId = Binder.clearCallingIdentity();
        String pkgName = Utils.getPackageName(detectedActivity);
        result.putBoolean("IS_RESULT_DETECT", true);
        if (pkgName != null && pkgName.equals(detectedActivity.getProcessName())) {
            SlogEx.i(TAG, "putDetectedResult is host homeActivity =" + detectedActivity);
            String homeName = Utils.getClassName(detectedActivity);
            addDetectedHome(pkgName, homeName);
            Context context = this.mSharedParameters.getContext();
            HiViewEx.report(HiViewEx.byContent(992130004, context, "{app:" + pkgName + ", home_recognized:" + homeName + "}"));
            Binder.restoreCallingIdentity(callingId);
            result.putBoolean("IS_RESULT_DETECT", true);
        }
    }

    public void setDetectedHomepage(ActivityRecordEx detected) {
        addDetectedHome(Utils.getPackageName(detected), Utils.getClassName(detected));
    }

    private boolean isNeedDect(String pkg) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null && container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
            return false;
        }
        if (this.mMwManager.getLocalSysCfg().isSupportVirtualConfig()) {
            return true;
        }
        if (container == null || !container.isNeedDetect(pkg) || isSkipHomeDetect(container, pkg)) {
            return false;
        }
        return true;
    }

    private boolean isSkipHomeDetect(HwMagicContainer container, String packageName) {
        boolean isSkipHome = this.mMwManager.getAmsPolicy().isEnterDoubleWindowIgnoreHome(container, packageName);
        SlogEx.i(TAG, " isSkipHomeDetect " + isSkipHome + " ,pkg= " + packageName);
        return isSkipHome;
    }

    private void addDetectedHome(String pkg, String home) {
        if (!isEmpty(pkg) && !isEmpty(home)) {
            this.mHosts.put(pkg, home);
        }
    }

    public boolean isHomeActivity(HwMagicContainer container, ActivityRecordEx ar) {
        if (container == null || ar == null) {
            return false;
        }
        if (container.isHomePage(Utils.getPackageName(ar), Utils.getClassName(ar)) || isMainActivity(container, ar) || isTheDetectedHomePage(ar)) {
            return true;
        }
        return false;
    }

    private boolean isMainActivity(HwMagicContainer container, ActivityRecordEx activity) {
        List<String> mainActName = container.getConfig().getMainActivity(Utils.getPackageName(activity));
        return mainActName != null && mainActName.contains(Utils.getClassName(activity));
    }

    public void updateDetectHomeAfterActivityFinished(ActivityRecordEx finish) {
        HwMagicContainer container = this.mMwManager.getContainer(finish);
        if (container != null && isTheDetectedHomePage(finish)) {
            String pkgName = Utils.getPackageName(finish);
            if (container.getAppSupportMode(pkgName) != 0 && !isSkipHomeDetect(container, pkgName)) {
                if (isAutoFinish(finish)) {
                    updateDetectHomeForAutoFinish(container, finish);
                } else {
                    updateDetectHome(finish);
                }
            }
        }
    }

    private boolean isAutoFinish(ActivityRecordEx finish) {
        return !Utils.getClassName(finish).equals(Utils.getClassName((ActivityRecordEx) Optional.ofNullable(finish.getActivityStackEx()).map($$Lambda$HomePageDetect$GEjqTSNaEEVeBATXdK3VySgon8.INSTANCE).orElse(null)));
    }

    private void updateDetectHome(ActivityRecordEx ar) {
        HwMagicContainer container = this.mMwManager.getContainer(ar);
        if (container != null) {
            SlogEx.i(TAG, "back key judgeHost " + ar);
            synchronized (this.mSharedParameters.getAms().getActivityTaskManagerEx()) {
                ActivityStackEx stack = ar.getActivityStackEx();
                if (stack != null) {
                    if (stack.getTaskHistory().size() == 1) {
                        TaskRecordEx task = ar.getTaskRecordEx();
                        if (task.getChildCount() > 1) {
                            ActivityRecordEx realHost = task.getChildAt(0);
                            if (realHost != null) {
                                int position = container.getBoundsPosition(realHost.getRequestedOverrideBounds());
                                if (!this.mMwManager.isSlave(ar) || (position != 3 && (!container.isFoldableDevice() || position != 5))) {
                                    ActivityRecordEx next = this.mMwManager.getAmsPolicy().getActivityByPosition(ar, 1, 1);
                                    if (this.mMwManager.isMaster(ar) && next != null) {
                                        realHost = next;
                                    }
                                    SlogEx.i(TAG, "JudgeHost set new host " + realHost);
                                    setDetectedHomepage(realHost);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateDetectHomeForAutoFinish(HwMagicContainer container, ActivityRecordEx ar) {
        SlogEx.i(TAG, "AutoFinish updateDetectHomeForAutoFinish " + ar);
        HwMagicWinAmsPolicy policy = this.mMwManager.getAmsPolicy();
        synchronized (this.mSharedParameters.getAms().getActivityTaskManagerEx()) {
            Iterator<ActivityRecordEx> it = policy.getAllActivities(ar.getActivityStackEx()).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ActivityRecordEx top = it.next();
                if (!policy.isRelatedActivity(container, top)) {
                    SlogEx.i(TAG, "JudgeHostAutoFinish set new host " + top);
                    setDetectedHomepage(top);
                    break;
                }
            }
        }
    }

    private boolean isTheDetectedHomePage(ActivityRecordEx activity) {
        String home;
        String pkgName = Utils.getPackageName(activity);
        String component = Utils.getClassName(activity);
        if (pkgName == null || (home = this.mHosts.get(pkgName)) == null || !home.equals(component)) {
            return false;
        }
        return true;
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
