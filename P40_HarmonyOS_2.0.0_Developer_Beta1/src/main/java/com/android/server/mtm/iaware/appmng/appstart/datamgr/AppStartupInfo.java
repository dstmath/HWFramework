package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCacheExt;
import com.huawei.server.wm.WindowProcessControllerEx;

public class AppStartupInfo {
    public String action;
    public ApplicationInfo applicationInfo;
    public WindowProcessControllerEx callerApp;
    public int callerPid;
    public int callerUid;
    public String compName;
    public int[] fgFlags;
    public boolean fromRecent;
    public Intent intent;
    public boolean isActivityComp = false;
    public boolean isAppStop;
    public boolean isCallerBtMediaBrowser;
    public boolean isCallerNotifyListener;
    public boolean isSysApp;
    public int policyType;
    public String procName;
    public AppMngConstant.AppStartSource requestSource;
    public StringBuilder sbReason;
    public HwAppStartupSetting startupSetting;
    public AwareAppStartStatusCacheExt statusCacheExt;
    public WindowProcessControllerEx targetApp;
    public int unPercetibleAlarm;

    public AppStartupInfo setApplicationInfo(ApplicationInfo applicationInfo2) {
        this.applicationInfo = applicationInfo2;
        return this;
    }

    public AppStartupInfo setAction(String action2) {
        this.action = action2;
        return this;
    }

    public AppStartupInfo setCallerPid(int callerPid2) {
        this.callerPid = callerPid2;
        return this;
    }

    public AppStartupInfo setCallerUid(int callerUid2) {
        this.callerUid = callerUid2;
        return this;
    }

    public AppStartupInfo setCallerApp(WindowProcessControllerEx callerApp2) {
        this.callerApp = callerApp2;
        return this;
    }

    public AppStartupInfo setRequestSource(AppMngConstant.AppStartSource requestSource2) {
        this.requestSource = requestSource2;
        return this;
    }

    public AppStartupInfo setIsAppStop(boolean isAppStop2) {
        this.isAppStop = isAppStop2;
        return this;
    }

    public AppStartupInfo setIsSysApp(boolean isSysApp2) {
        this.isSysApp = isSysApp2;
        return this;
    }

    public AppStartupInfo setIsCallerBtMediaBrowser(boolean isCallerBtMediaBrowser2) {
        this.isCallerBtMediaBrowser = isCallerBtMediaBrowser2;
        return this;
    }

    public AppStartupInfo setIsCallerNotifyListener(boolean isCallerNotifyListener2) {
        this.isCallerNotifyListener = isCallerNotifyListener2;
        return this;
    }

    public AppStartupInfo setUnPercetibleAlarm(int unPercetibleAlarm2) {
        this.unPercetibleAlarm = unPercetibleAlarm2;
        return this;
    }

    public AppStartupInfo setFromRecent(boolean fromRecent2) {
        this.fromRecent = fromRecent2;
        return this;
    }

    public AppStartupInfo setStatusCacheExt(AwareAppStartStatusCacheExt statusCacheExt2) {
        this.statusCacheExt = statusCacheExt2;
        return this;
    }

    public AppStartupInfo setIntent(Intent intent2) {
        this.intent = intent2;
        return this;
    }

    public AppStartupInfo setPolicyType(int policyType2) {
        this.policyType = policyType2;
        return this;
    }

    public AppStartupInfo setCompName(String compName2) {
        this.compName = compName2;
        return this;
    }

    public AppStartupInfo setReason(StringBuilder sbReason2) {
        this.sbReason = sbReason2;
        return this;
    }

    public AppStartupInfo setFgFlags(int[] fgFlags2) {
        this.fgFlags = fgFlags2;
        return this;
    }

    public AppStartupInfo setHwAppStartupSetting(HwAppStartupSetting startupSetting2) {
        this.startupSetting = startupSetting2;
        return this;
    }

    public AppStartupInfo setProcName(String procName2) {
        this.procName = procName2;
        return this;
    }

    public AppStartupInfo setTargetApp(WindowProcessControllerEx targetApp2) {
        this.targetApp = targetApp2;
        return this;
    }

    public AppStartupInfo setIsActivityComp(boolean isActivityComp2) {
        this.isActivityComp = isActivityComp2;
        return this;
    }
}
