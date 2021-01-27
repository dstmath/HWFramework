package ohos.ai.engine.cloudstrategy;

import java.util.Map;
import java.util.Optional;
import ohos.ai.engine.cloudstrategy.grs.IGrsCallback;
import ohos.ai.engine.pluginbridge.CoreServiceSkeleton;
import ohos.ai.engine.pluginbridge.ICoreService;
import ohos.ai.engine.utils.HiAILog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class CloudStrategyManager {
    private static final String TAG = CloudStrategyManager.class.getSimpleName();
    private static volatile CloudStrategyManager instance = null;
    private IRemoteObject coreService;

    private CloudStrategyManager() {
    }

    public static CloudStrategyManager getInstance() {
        if (instance == null) {
            synchronized (CloudStrategyManager.class) {
                if (instance == null) {
                    instance = new CloudStrategyManager();
                }
            }
        }
        return instance;
    }

    public void setCoreService(IRemoteObject iRemoteObject) {
        this.coreService = iRemoteObject;
    }

    public Optional<ICloudStrategy> getCloudStrategy() {
        ICoreService orElse = CoreServiceSkeleton.asInterface(this.coreService).orElse(null);
        if (orElse != null) {
            try {
                return CloudStrategySkeleton.asInterface(orElse.getCloudStrategyRemoteObject());
            } catch (RemoteException e) {
                String str = TAG;
                HiAILog.error(str, "getReportCore " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public void grsAsyncProcess(String str, String str2, IGrsCallback iGrsCallback) {
        HiAILog.info(TAG, "grsAsyncProcess");
        ICloudStrategy orElse = getCloudStrategy().orElse(null);
        if (orElse != null) {
            try {
                orElse.grsInit();
                orElse.grsAsyncQueryUrl(str, str2, iGrsCallback);
                HiAILog.info(TAG, "grsAsyncProcess ok");
            } catch (RemoteException e) {
                String str3 = TAG;
                HiAILog.error(str3, "grsAsyncProcess " + e.getMessage());
            }
        }
    }

    public String grsSyncProcess(String str, String str2) {
        HiAILog.info(TAG, "grsSynProcess");
        ICloudStrategy orElse = getCloudStrategy().orElse(null);
        if (orElse == null) {
            return "";
        }
        try {
            orElse.grsInit();
            String grsSyncQueryUrl = orElse.grsSyncQueryUrl(str, str2);
            HiAILog.info(TAG, "grsSynProcess ok");
            return grsSyncQueryUrl;
        } catch (RemoteException e) {
            String str3 = TAG;
            HiAILog.error(str3, "grsSyncProcess " + e.getMessage());
            return "";
        }
    }

    public void grsInit() {
        HiAILog.info(TAG, "grsInit");
        ICloudStrategy orElse = getCloudStrategy().orElse(null);
        if (orElse != null) {
            try {
                orElse.grsInit();
            } catch (RemoteException e) {
                String str = TAG;
                HiAILog.error(str, "grsInit " + e.getMessage());
            }
        }
    }

    public void grsClear() {
        HiAILog.info(TAG, "grsClear");
        ICloudStrategy orElse = getCloudStrategy().orElse(null);
        if (orElse != null) {
            try {
                orElse.grsClear();
            } catch (RemoteException e) {
                String str = TAG;
                HiAILog.error(str, "grsClear " + e.getMessage());
            }
        }
    }

    public void resetOkHttpClient() {
        HiAILog.info(TAG, "resetOkHttpClient");
        ICloudStrategy orElse = getCloudStrategy().orElse(null);
        if (orElse != null) {
            try {
                orElse.resetOkHttpClient();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "resetOkHttpClient ");
            }
        }
    }

    public Optional<String> post(String str, String str2) {
        HiAILog.info(TAG, "post");
        ICloudStrategy orElse = getCloudStrategy().orElse(null);
        if (orElse != null) {
            try {
                return Optional.of(orElse.post(str, str2));
            } catch (RemoteException e) {
                String str3 = TAG;
                HiAILog.error(str3, "post " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public Optional<String> postContainsMap(String str, String str2, Map<String, String> map) {
        HiAILog.info(TAG, "postContainsMap");
        ICloudStrategy orElse = getCloudStrategy().orElse(null);
        if (orElse != null) {
            try {
                return Optional.of(orElse.postContainsMap(str, str2, map));
            } catch (RemoteException e) {
                String str3 = TAG;
                HiAILog.error(str3, "postContainsMap " + e.getMessage());
            }
        }
        return Optional.empty();
    }
}
