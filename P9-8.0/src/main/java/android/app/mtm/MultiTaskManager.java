package android.app.mtm;

import android.app.mtm.IMultiTaskManagerService.Stub;
import android.app.mtm.iaware.RSceneData;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppCleanParam.AppCleanInfo;
import android.app.mtm.iaware.appmng.AppCleanParam.Builder;
import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;
import android.util.Log;
import android.util.Slog;
import java.util.List;

public class MultiTaskManager {
    static final boolean DEBUG = false;
    static final String TAG = "MultiTaskManager";
    private static MultiTaskManager instance = null;
    static final Object mLock = new Object[0];
    private IMultiTaskManagerService mService;

    private MultiTaskManager() {
        IMultiTaskManagerService service = getService();
        if (service == null) {
            Slog.e(TAG, "multi task service is null in constructor");
        }
        this.mService = service;
    }

    public static MultiTaskManager getInstance() {
        synchronized (mLock) {
            if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
                if (instance == null) {
                    if (Log.HWINFO) {
                        Slog.i(TAG, "first time to initialize MultiTaskManager, this log should not appear again!");
                    }
                    instance = new MultiTaskManager();
                    if (instance.mService == null) {
                        instance = null;
                    }
                }
                MultiTaskManager multiTaskManager = instance;
                return multiTaskManager;
            }
            Slog.e(TAG, "multitask service is not running because prop is false, so getInstance return null");
            return null;
        }
    }

    public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyResourceStatusOverload(resourcetype, resourceextend, resourcestatus, args);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService != null) {
                this.mService.registerObserver(observer);
                return;
            }
            this.mService = getService();
            if (this.mService != null) {
                this.mService.registerObserver(observer);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService != null) {
                this.mService.unregisterObserver(observer);
                return;
            }
            this.mService = getService();
            if (this.mService != null) {
                this.mService.unregisterObserver(observer);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void notifyProcessGroupChange(int pid, int uid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyProcessGroupChange(pid, uid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyProcessStatusChange(pkg, process, hostingType, pid, uid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void notifyProcessDiedChange(int pid, int uid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyProcessDiedChange(pid, uid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public boolean killProcess(int pid, boolean restartservice) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.killProcess(pid, restartservice);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return false;
    }

    public boolean forcestopApps(int pid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.forcestopApps(pid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return false;
    }

    public boolean reportScene(int featureId, RSceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.reportScene(featureId, scene);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "reportScene can not connect to MultiTaskManagerService");
        }
        return false;
    }

    public RPolicyData acquirePolicyData(int featureId, RSceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.acquirePolicyData(featureId, scene);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "acquirePolicyData can not connect to MultiTaskManagerService");
        }
        return null;
    }

    private IMultiTaskManagerService getService() {
        return Stub.asInterface(ServiceManager.getService("multi_task"));
    }

    public void requestAppCleanFromPG(List<String> pkgName, List<Integer> userId, int level, final String reason) {
        requestAppCleanWithCallback(new Builder(AppCleanSource.POWER_GENIE.ordinal()).level(level).stringList(pkgName).intList(userId).build(), new IAppCleanCallback.Stub() {
            public void onCleanFinish(AppCleanParam result) {
                AwareLog.i(MultiTaskManager.TAG, "onCleanFinish:" + result + ", reason = " + reason);
            }
        });
    }

    public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) {
        IMultiTaskManagerService service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "MultiTaskManager service is null ");
            }
            return;
        }
        try {
            service.requestAppCleanWithCallback(param, callback);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "requestAppCleanWithCallback catch RemoteException");
            }
        }
    }

    public void executeMultiAppClean(List<AppCleanInfo> appCleanInfoList, IAppCleanCallback callback) {
        requestAppCleanWithCallback(new Builder(AppCleanSource.SYSTEM_MANAGER.ordinal()).action(0).appCleanInfoList(appCleanInfoList).build(), callback);
    }

    public void getAppListForUserClean(IAppCleanCallback callback) {
        requestAppCleanWithCallback(new Builder(AppCleanSource.SYSTEM_MANAGER.ordinal()).action(1).build(), callback);
    }
}
