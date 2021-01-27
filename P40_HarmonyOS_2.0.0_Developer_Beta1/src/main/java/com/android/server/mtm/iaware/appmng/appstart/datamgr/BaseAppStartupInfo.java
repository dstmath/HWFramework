package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.content.Intent;
import com.huawei.server.wm.WindowProcessControllerEx;

public class BaseAppStartupInfo {
    public WindowProcessControllerEx callerApp;
    public int callerPid;
    public int callerUid;
    public Intent intent;
    public WindowProcessControllerEx targetApp;

    public BaseAppStartupInfo(int pid, int uid, WindowProcessControllerEx callerWinApp, WindowProcessControllerEx targetWinApp, Intent service) {
        this.callerPid = pid;
        this.callerUid = uid;
        this.callerApp = callerWinApp;
        this.targetApp = targetWinApp;
        this.intent = service;
    }
}
