package android.app.mtm;

import android.app.mtm.IMultiTaskManagerService;
import android.app.mtm.iaware.SceneData;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.os.Bundle;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.util.LogEx;
import java.util.List;

public class MultiTaskManager {
    private static final boolean DEBUG = false;
    private static final boolean ENABLE_IAWARE = SystemPropertiesEx.getBoolean("ro.config.enable_iaware", false);
    private static final Object LOCK = new Object[0];
    private static final String TAG = "MultiTaskManager";
    private static MultiTaskManager instance = null;
    private IMultiTaskManagerService mService;

    private MultiTaskManager() {
        IMultiTaskManagerService service = getService();
        if (service == null) {
            SlogEx.e(TAG, "multi task service is null in constructor");
        }
        this.mService = service;
    }

    public static MultiTaskManager getInstance() {
        synchronized (LOCK) {
            if (!ENABLE_IAWARE) {
                SlogEx.e(TAG, "multitask service is not running because prop is false, so getInstance return null");
                return null;
            }
            if (instance == null) {
                if (LogEx.getLogHWInfo()) {
                    SlogEx.i(TAG, "first time to initialize MultiTaskManager, this log should not appear again!");
                }
                instance = new MultiTaskManager();
                if (instance.mService == null) {
                    instance = null;
                }
            }
            return instance;
        }
    }

    public void notifyResourceStatusOverload(int resourceType, String resourceExtend, int resourceStatus, Bundle args) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyResourceStatusOverload(resourceType, resourceExtend, resourceStatus, args);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.registerObserver(observer);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.unregisterObserver(observer);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
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
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
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
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
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
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public boolean killProcess(int pid, boolean restartService) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.killProcess(pid, restartService);
            }
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
            return false;
        }
    }

    public boolean forcestopApps(int pid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.forcestopApps(pid);
            }
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to MultiTaskManagerService");
            return false;
        }
    }

    public boolean reportScene(int featureId, SceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.reportScene(featureId, scene);
            }
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "reportScene can not connect to MultiTaskManagerService");
            return false;
        }
    }

    public RPolicyData acquirePolicyData(int featureId, SceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.acquirePolicyData(featureId, scene);
            }
            return null;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "acquirePolicyData can not connect to MultiTaskManagerService");
            return null;
        }
    }

    private IMultiTaskManagerService getService() {
        return IMultiTaskManagerService.Stub.asInterface(ServiceManagerEx.getService("multi_task"));
    }

    public void requestAppCleanFromPG(List<String> pkgName, List<Integer> userId, int level, final String reason) {
        requestAppCleanWithCallback(new AppCleanParam.Builder(AppMngConstant.AppCleanSource.POWER_GENIE.ordinal()).level(level).stringList(pkgName).intList(userId).build(), new IAppCleanCallback.Stub() {
            /* class android.app.mtm.MultiTaskManager.AnonymousClass1 */

            public void onCleanFinish(AppCleanParam result) {
                AwareLog.i(MultiTaskManager.TAG, "onCleanFinish:" + result + ", reason = " + reason);
            }
        });
    }

    public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) {
        IMultiTaskManagerService service = getService();
        if (service != null) {
            try {
                service.requestAppCleanWithCallback(param, callback);
            } catch (RemoteException e) {
                if (LogEx.getLogHWInfo()) {
                    Log.i(TAG, "requestAppCleanWithCallback catch RemoteException");
                }
            }
        } else if (LogEx.getLogHWInfo()) {
            Log.i(TAG, "MultiTaskManager service is null ");
        }
    }

    public void executeMultiAppClean(List<AppCleanParam.AppCleanInfo> appCleanInfoList, IAppCleanCallback callback) {
        requestAppCleanWithCallback(new AppCleanParam.Builder(AppMngConstant.AppCleanSource.SYSTEM_MANAGER.ordinal()).action(0).appCleanInfoList(appCleanInfoList).build(), callback);
    }

    public void getAppListForUserClean(IAppCleanCallback callback) {
        requestAppCleanWithCallback(new AppCleanParam.Builder(AppMngConstant.AppCleanSource.SYSTEM_MANAGER.ordinal()).action(1).build(), callback);
    }

    public void requestAppClean(List<String> pkgName, List<Integer> userId, int level, String reason, int source) {
        requestAppCleanWithCallback(new AppCleanParam.Builder(source).level(level).stringList(pkgName).intList(userId).build(), null);
    }
}
