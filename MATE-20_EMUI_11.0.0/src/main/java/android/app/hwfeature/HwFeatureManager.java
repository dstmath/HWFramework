package android.app.hwfeature;

import android.app.ActivityManager;
import android.app.hwfeature.IHwFeatureManagerService;
import android.content.Context;
import android.os.Binder;
import android.os.FreezeScreenScene;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
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
        return IHwFeatureManagerService.Stub.asInterface(ServiceManager.getService("hwfeaturemanager"));
    }

    public boolean requestPermission(Context context) {
        IHwFeatureManagerService service = getService();
        if (service == null || context == null) {
            if (Log.HWINFO) {
                Log.w(TAG, "checkAuthentication service is null");
            }
            return false;
        }
        try {
            return service.requestPermission(getPackageNameByUidAndPid(context, Binder.getCallingUid(), Binder.getCallingPid()));
        } catch (RemoteException e) {
            Log.e(TAG, "checkAuthentication catch RemoteException");
            return false;
        }
    }

    private String getPackageNameByUidAndPid(Context context, int uid, int pid) {
        if (context == null) {
            Log.w(TAG, "failed to get packageName！");
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        String[] pkgs = context.getPackageManager().getPackagesForUid(uid);
        if (pkgs == null || pkgs.length == 0) {
            Log.w(TAG, "packageName is null！");
            return StorageManagerExt.INVALID_KEY_DESC;
        } else if (1 == pkgs.length) {
            return pkgs[0];
        } else {
            List<ActivityManager.RunningAppProcessInfo> list = ((ActivityManager) context.getSystemService(FreezeScreenScene.ACTIVITY_PARAM)).getRunningAppProcesses();
            if (list == null) {
                Log.w(TAG, "list is null");
                return StorageManagerExt.INVALID_KEY_DESC;
            }
            for (ActivityManager.RunningAppProcessInfo runningInfo : list) {
                int runningPid = runningInfo.pid;
                int runningUid = runningInfo.uid;
                if (runningPid == pid && runningUid == uid) {
                    String[] pkgNameList = runningInfo.pkgList;
                    if (pkgNameList == null || pkgNameList.length <= 0) {
                        return StorageManagerExt.INVALID_KEY_DESC;
                    }
                    return pkgNameList[0];
                }
            }
            return StorageManagerExt.INVALID_KEY_DESC;
        }
    }
}
