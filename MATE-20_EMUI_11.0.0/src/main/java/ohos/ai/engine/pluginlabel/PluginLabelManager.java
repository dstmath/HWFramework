package ohos.ai.engine.pluginlabel;

import java.util.Optional;
import ohos.ai.engine.pluginbridge.CoreServiceSkeleton;
import ohos.ai.engine.pluginbridge.ICoreService;
import ohos.ai.engine.utils.HiAILog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class PluginLabelManager {
    private static final String TAG = PluginLabelManager.class.getSimpleName();
    private static volatile PluginLabelManager instance = null;
    private IRemoteObject remoteObject;

    private PluginLabelManager() {
    }

    public static PluginLabelManager getInstance() {
        if (instance == null) {
            synchronized (PluginLabelManager.class) {
                if (instance == null) {
                    instance = new PluginLabelManager();
                }
            }
        }
        return instance;
    }

    public void setCoreService(IRemoteObject iRemoteObject) {
        this.remoteObject = iRemoteObject;
    }

    private Optional<IPluginLabel> getPluginLabel() {
        ICoreService orElse = CoreServiceSkeleton.asInterface(this.remoteObject).orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "iCoreService is null");
            return Optional.empty();
        }
        try {
            return PluginLabelSkeleton.asInterface(orElse.getPluginLabelRemoteObject());
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getPluginLabel remoteException");
            return Optional.empty();
        }
    }

    public String getRegionLabel() {
        HiAILog.info(TAG, "getRegionLabel");
        IPluginLabel orElse = getPluginLabel().orElse(null);
        if (orElse != null) {
            try {
                return orElse.getRegionLabel();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "getRegionLabel remoteException");
            }
        }
        return PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    }

    public String getComputationalResourceLabel() {
        HiAILog.info(TAG, "getComputationalResourceLabel");
        IPluginLabel orElse = getPluginLabel().orElse(null);
        if (orElse != null) {
            try {
                return orElse.getComputationalResourceLabel();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "getComputationalResourceLabel remoteException");
            }
        }
        return PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    }

    public String getXpuLabel() {
        HiAILog.info(TAG, "getXpuLabel");
        IPluginLabel orElse = getPluginLabel().orElse(null);
        if (orElse != null) {
            try {
                return orElse.getXpuLabel();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "getXpuLabel remoteException");
            }
        }
        return PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    }

    public String getDistanceLabel() {
        HiAILog.info(TAG, "getDistanceLabel");
        IPluginLabel orElse = getPluginLabel().orElse(null);
        if (orElse != null) {
            try {
                return orElse.getDistanceLabel();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "getDistanceLabel remoteException");
            }
        }
        return PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    }

    public String getCameraLabel() {
        HiAILog.info(TAG, "getCameraLabel");
        IPluginLabel orElse = getPluginLabel().orElse(null);
        if (orElse != null) {
            try {
                return orElse.getCameraLabel();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "getCameraLabel remoteException");
            }
        }
        return PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    }

    public PluginLabelInfo getPluginLabelInfo() {
        HiAILog.info(TAG, "getPluginLabelInfo");
        IPluginLabel orElse = getPluginLabel().orElse(null);
        PluginLabelInfo pluginLabelInfo = new PluginLabelInfo();
        if (orElse == null) {
            return pluginLabelInfo;
        }
        try {
            return orElse.getPluginLabelInfo();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getPluginLabelInfo remoteException");
            return pluginLabelInfo;
        }
    }
}
