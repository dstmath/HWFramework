package ohos.ai.engine.health;

import java.util.Optional;
import ohos.ai.engine.pluginbridge.CoreServiceSkeleton;
import ohos.ai.engine.pluginbridge.ICoreService;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class HealthCoreManager {
    private static final String TAG = HealthCoreManager.class.getSimpleName();
    private static volatile HealthCoreManager instance = null;
    private IRemoteObject coreService;

    public int getCallProcessPriority(Context context) {
        return 0;
    }

    private HealthCoreManager() {
    }

    public static HealthCoreManager getInstance() {
        if (instance == null) {
            synchronized (HealthCoreManager.class) {
                if (instance == null) {
                    instance = new HealthCoreManager();
                }
            }
        }
        return instance;
    }

    public void setCoreService(IRemoteObject iRemoteObject) {
        this.coreService = iRemoteObject;
    }

    public Optional<IHealthCore> getHealthCore() {
        ICoreService orElse = CoreServiceSkeleton.asInterface(this.coreService).orElse(null);
        if (orElse != null) {
            try {
                return HealthCoreSkeleton.asInterface(orElse.getHealthCoreRemoteObject());
            } catch (RemoteException e) {
                String str = TAG;
                HiAILog.error(str, "getHealthCore " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public int requestRunning(int i, String str, PacMap pacMap) {
        String str2 = TAG;
        HiAILog.info(str2, "requestRunning " + i + " " + str);
        IHealthCore orElse = getHealthCore().orElse(null);
        if (orElse != null) {
            try {
                return orElse.requestRunning(i, str, pacMap);
            } catch (RemoteException e) {
                String str3 = TAG;
                HiAILog.error(str3, "requestRunning " + e.getMessage());
                return 0;
            }
        } else {
            HiAILog.error(TAG, "[requestRunning] HealthCore is null,check setCoreService had been called ");
            return 0;
        }
    }

    public int reportCompleted(int i, String str, PacMap pacMap) {
        String str2 = TAG;
        HiAILog.info(str2, "reportCompleted " + i + " " + str);
        IHealthCore orElse = getHealthCore().orElse(null);
        if (orElse != null) {
            try {
                return orElse.reportCompleted(i, str, pacMap);
            } catch (RemoteException e) {
                String str3 = TAG;
                HiAILog.error(str3, "reportCompleted " + e.getMessage());
                return 0;
            }
        } else {
            HiAILog.error(TAG, "[reportCompleted] HealthCore is null,check setCoreService had been called ");
            return 0;
        }
    }

    public boolean call(Context context, String str) {
        String str2 = TAG;
        HiAILog.debug(str2, "call apiName " + str);
        return true;
    }

    public int getProcessPriority(String str) {
        IHealthCore orElse = getHealthCore().orElse(null);
        if (orElse != null) {
            try {
                return orElse.getProcessPriority(str);
            } catch (RemoteException e) {
                String str2 = TAG;
                HiAILog.error(str2, "health getProcessPriority error" + e.getMessage());
                return 0;
            }
        } else {
            HiAILog.error(TAG, "[getProcessPriority] HealthCore is null,check setCoreService had been called ");
            return 0;
        }
    }
}
