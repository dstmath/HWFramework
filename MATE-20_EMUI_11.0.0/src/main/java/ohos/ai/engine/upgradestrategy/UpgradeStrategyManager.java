package ohos.ai.engine.upgradestrategy;

import java.util.Optional;
import ohos.ai.engine.pluginbridge.CoreServiceSkeleton;
import ohos.ai.engine.pluginbridge.ICoreService;
import ohos.ai.engine.utils.HiAILog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class UpgradeStrategyManager {
    private static final String TAG = UpgradeStrategyManager.class.getSimpleName();
    private static volatile UpgradeStrategyManager instance = null;
    private IRemoteObject remoteCoreService;

    private UpgradeStrategyManager() {
    }

    public static UpgradeStrategyManager getInstance() {
        if (instance == null) {
            synchronized (UpgradeStrategyManager.class) {
                if (instance == null) {
                    instance = new UpgradeStrategyManager();
                }
            }
        }
        return instance;
    }

    public void setCoreService(IRemoteObject iRemoteObject) {
        this.remoteCoreService = iRemoteObject;
    }

    public Optional<IUpgradeStrategy> getUpgradeStrategy() {
        ICoreService orElse = CoreServiceSkeleton.asInterface(this.remoteCoreService).orElse(null);
        if (orElse != null) {
            try {
                return UpgradeStrategySkeleton.asInterface(orElse.getUpgradeStrategyRemoteObject());
            } catch (RemoteException e) {
                String str = TAG;
                HiAILog.error(str, "getUpgradeStrategy " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public void checkHiAiAppUpdate(IUpgradeIndicator iUpgradeIndicator) {
        HiAILog.info(TAG, "checkHiAiAppUpdate");
        if (iUpgradeIndicator == null) {
            HiAILog.error(TAG, "indicator is null");
            return;
        }
        IUpgradeStrategy orElse = getUpgradeStrategy().orElse(null);
        if (orElse != null) {
            try {
                orElse.checkHiAiAppUpdate(iUpgradeIndicator);
            } catch (RemoteException e) {
                String str = TAG;
                HiAILog.error(str, "checkHiAiAppUpdate " + e.getMessage());
            }
        }
    }

    public void updateHiAiApp() {
        HiAILog.info(TAG, "updateHiAiApp");
        IUpgradeStrategy orElse = getUpgradeStrategy().orElse(null);
        if (orElse != null) {
            try {
                orElse.updateHiAiApp();
            } catch (RemoteException e) {
                String str = TAG;
                HiAILog.error(str, "updateHiAiApp " + e.getMessage());
            }
        }
    }
}
