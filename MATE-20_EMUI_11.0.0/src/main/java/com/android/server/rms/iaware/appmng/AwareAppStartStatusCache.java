package com.android.server.rms.iaware.appmng;

import android.content.pm.ApplicationInfo;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupInfo;

public final class AwareAppStartStatusCache {
    public static final int APPTYPE_DEFAULT = -100;
    public String cacheAction;
    public int cacheAppAttribute;
    public int cacheAppType;
    public int cacheCallerUid;
    public String cacheCompName;
    public int cacheFlags;
    public boolean cacheIsAppStop;
    public boolean cacheIsBtMediaBrowserCaller;
    public boolean cacheIsCallerFg;
    public boolean cacheIsSystemApp;
    public boolean cacheIsTargetFg;
    public boolean cacheNotifyListenerCaller;
    public AwareAppStartStatusCacheExt cacheStatusCacheExt;
    public int cacheUid;
    public int unPercetibleAlarm;

    public AwareAppStartStatusCache(AppStartupInfo appStartupInfo, ApplicationInfo applicationInfo, boolean isCallerFg, boolean isTargetFg, AwareAppStartStatusCacheExt statusCacheExt) {
        if (appStartupInfo != null && applicationInfo != null) {
            this.cacheUid = applicationInfo.uid;
            this.cacheCallerUid = appStartupInfo.callerUid;
            this.cacheIsSystemApp = appStartupInfo.isSysApp;
            this.cacheIsAppStop = appStartupInfo.isAppStop;
            this.cacheCompName = appStartupInfo.compName;
            this.cacheAction = appStartupInfo.action;
            this.cacheIsCallerFg = isCallerFg;
            this.cacheIsTargetFg = isTargetFg;
            this.cacheFlags = applicationInfo.flags;
            this.cacheIsBtMediaBrowserCaller = appStartupInfo.isCallerBtMediaBrowser;
            this.cacheAppType = -100;
            this.cacheAppAttribute = -1;
            this.unPercetibleAlarm = appStartupInfo.unPercetibleAlarm;
            this.cacheNotifyListenerCaller = appStartupInfo.isCallerNotifyListener;
            this.cacheStatusCacheExt = statusCacheExt;
        }
    }
}
