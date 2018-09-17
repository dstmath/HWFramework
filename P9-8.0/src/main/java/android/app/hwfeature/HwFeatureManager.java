package android.app.hwfeature;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.hwfeature.IHwFeatureManagerService.Stub;
import android.content.Context;
import android.os.Binder;
import android.os.FreezeScreenScene;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.List;

public class HwFeatureManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwFeatureManager";
    private static HwFeatureManager sInstance = null;
    private static final Object sLock = new Object();
    private IHwFeatureManagerService mService;

    private HwFeatureManager() {
        IHwFeatureManagerService service = getService();
        if (service == null) {
            Log.e(TAG, "featuremanager service is null in constructor");
        }
        this.mService = service;
    }

    public static HwFeatureManager getInstance() {
        HwFeatureManager hwFeatureManager;
        synchronized (sLock) {
            if (sInstance == null) {
                if (Log.HWINFO) {
                    Log.i(TAG, "first time to initialize HwFeatureManager, this log should not appear again!");
                }
                sInstance = new HwFeatureManager();
                if (sInstance.mService == null) {
                    Log.w(TAG, "get featuremanger is null because service is null!");
                    sInstance = null;
                }
            }
            hwFeatureManager = sInstance;
        }
        return hwFeatureManager;
    }

    private IHwFeatureManagerService getService() {
        return Stub.asInterface(ServiceManager.getService("hwfeaturemanager"));
    }

    public boolean requestPermission(Context context) {
        boolean result = false;
        IHwFeatureManagerService service = getService();
        if (service == null || context == null) {
            if (Log.HWINFO) {
                Log.w(TAG, "checkAuthentication service is null");
            }
            return result;
        }
        try {
            result = service.requestPermission(getPackageNameByUidAndPid(context, Binder.getCallingUid(), Binder.getCallingPid()));
        } catch (RemoteException e) {
            Log.e(TAG, "checkAuthentication catch RemoteException");
        }
        return result;
    }

    private String getPackageNameByUidAndPid(Context context, int uid, int pid) {
        if (context == null) {
            Log.w(TAG, "failed to get packageName！");
            return "";
        }
        String[] pkgs = context.getPackageManager().getPackagesForUid(uid);
        if (pkgs == null || pkgs.length == 0) {
            Log.w(TAG, "packageName is null！");
            return "";
        } else if (1 == pkgs.length) {
            return pkgs[0];
        } else {
            String pkgName = "";
            List<RunningAppProcessInfo> list = ((ActivityManager) context.getSystemService(FreezeScreenScene.ACTIVITY_PARAM)).getRunningAppProcesses();
            if (list == null) {
                Log.w(TAG, "list is null");
                return "";
            }
            for (RunningAppProcessInfo runningInfo : list) {
                int runningPid = runningInfo.pid;
                int runningUid = runningInfo.uid;
                if (runningPid == pid && runningUid == uid) {
                    String[] pkgNameList = runningInfo.pkgList;
                    if (pkgNameList != null && pkgNameList.length > 0) {
                        pkgName = pkgNameList[0];
                    }
                    return pkgName;
                }
            }
            return pkgName;
        }
    }
}
