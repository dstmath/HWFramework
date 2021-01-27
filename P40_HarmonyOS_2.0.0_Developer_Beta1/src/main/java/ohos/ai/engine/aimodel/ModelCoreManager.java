package ohos.ai.engine.aimodel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.ai.engine.pluginbridge.CoreServiceSkeleton;
import ohos.ai.engine.pluginbridge.ICoreService;
import ohos.ai.engine.utils.HiAILog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class ModelCoreManager {
    private static final long INVALID_VERSION_CODE = -1;
    private static final String TAG = ModelCoreManager.class.getSimpleName();
    private static volatile ModelCoreManager instance = null;
    private IRemoteObject coreService;

    private ModelCoreManager() {
    }

    public static ModelCoreManager getInstance() {
        if (instance == null) {
            synchronized (ModelCoreManager.class) {
                if (instance == null) {
                    instance = new ModelCoreManager();
                }
            }
        }
        return instance;
    }

    public void setCoreService(IRemoteObject iRemoteObject) {
        this.coreService = iRemoteObject;
    }

    public Optional<IModelCore> getModelCore() {
        ICoreService orElse = CoreServiceSkeleton.asInterface(this.coreService).orElse(null);
        if (orElse == null) {
            HiAILog.info(TAG, "iCoreService is null");
            return Optional.empty();
        }
        try {
            return ModelCoreSkeleton.asInterface(orElse.getModelCoreRemoteObject());
        } catch (RemoteException e) {
            String str = TAG;
            HiAILog.error(str, "getModelCore " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isConnect() {
        HiAILog.info(TAG, "isConnect");
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[isConnect] modelCore is null");
            return false;
        }
        try {
            return orElse.isConnect();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[isConnect] RemoteException e");
            return false;
        }
    }

    public void connect() {
        HiAILog.info(TAG, "connect");
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[connect] modelCore is null");
            return;
        }
        try {
            orElse.connect();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[connect] RemoteException e");
        }
    }

    public boolean insertResourceInformation(String str) {
        String str2 = TAG;
        HiAILog.info(str2, "insertResourceInformation " + str);
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[insertResourceInformation] modelCore is null");
            return false;
        }
        try {
            return orElse.insertResourceInformation(str);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[insertResourceInformation] RemoteException e");
            return false;
        }
    }

    public void subscribeModel(ModelUpInfo modelUpInfo, IRecordObserverCallback iRecordObserverCallback) {
        HiAILog.info(TAG, "subscribeModel ");
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[subscribeModel] modelCore is null");
            return;
        }
        try {
            orElse.subscribeModel(modelUpInfo, iRecordObserverCallback);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[subscribeModel] RemoteException e");
        }
    }

    public void unsubscribeModel(ModelUpInfo modelUpInfo) {
        HiAILog.info(TAG, "unsubscribeModel ");
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[unsubscribeModel] modelCore is null");
            return;
        }
        try {
            orElse.unsubscribeModel(modelUpInfo);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[unsubscribeModel] RemoteException e");
        }
    }

    public long getResourceVersionCode(String str) {
        String str2 = TAG;
        HiAILog.info(str2, "getResourceVersionCode " + str);
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[getResourceVersionCode] modelCore is null");
            return -1;
        }
        try {
            return orElse.getResourceVersionCode(str);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[getResourceVersionCode] RemoteException e");
            return -1;
        }
    }

    public Optional<String> requestModelPath(long j) {
        String str = TAG;
        HiAILog.info(str, "requestModelPath " + j);
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[requestModelPath] modelCore is null");
            return Optional.empty();
        }
        try {
            return Optional.of(orElse.requestModelPath(j));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[requestModelPath] RemoteException e");
            return Optional.empty();
        }
    }

    public byte[] requestModelBytes(long j) {
        String str = TAG;
        HiAILog.info(str, "requestModelBytes " + j);
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[requestModelBytes] modelCore is null");
            return new byte[0];
        }
        try {
            return orElse.requestModelBytes(j);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[requestModelBytes] RemoteException e");
            return new byte[0];
        }
    }

    public Optional<ByteBuffer> requestModelBuffer(long j) {
        String str = TAG;
        HiAILog.info(str, "requestModelBuffer " + j);
        byte[] requestModelBytes = requestModelBytes(j);
        if (requestModelBytes != null) {
            return Optional.of(ByteBuffer.wrap(requestModelBytes).asReadOnlyBuffer());
        }
        HiAILog.error(TAG, "bytes is null");
        return Optional.empty();
    }

    public List<AiModelBean> requestModelsByBusiDomain(String str) {
        String str2 = TAG;
        HiAILog.info(str2, "requestModelsByBusiDomain " + str);
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[requestModelsByBusiDomain] modelCore is null");
            return new ArrayList();
        }
        try {
            return orElse.requestModelsByBusiDomain(str);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[requestModelsByBusiDomain] RemoteException e");
            return new ArrayList();
        }
    }

    public boolean syncModel(String str, long j) {
        String str2 = TAG;
        HiAILog.info(str2, "syncModel " + str + j);
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[syncModel] modelCore is null");
            return false;
        }
        try {
            return orElse.syncModel(str, j);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[syncModel] RemoteException e");
            return false;
        }
    }

    public boolean isSupportModelManagement() {
        HiAILog.info(TAG, "isSupportModelManagement");
        IModelCore orElse = getModelCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[isSupportModelManagement] modelCore is null");
            return false;
        }
        try {
            return orElse.isSupportModelManagement();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[isSupportModelManagement] RemoteException e");
            return false;
        }
    }
}
