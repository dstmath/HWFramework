package ohos.ai.engine.aimodel;

import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IModelCore extends IRemoteBroker {
    public static final int CONNECT = 2;
    public static final String DESCRIPTOR = "ohos.ai.engine.aimodel.IModelCore";
    public static final int GET_RESOURCE_VERSION_CODE = 4;
    public static final int INSERT_RESOURCE_INFORMATION = 3;
    public static final int IS_CONNECT = 1;
    public static final int IS_SUPPORT_MODEL_MANAGEMENT = 9;
    public static final int REQUEST_MODELS_BY_BUSI_DOMAIN = 7;
    public static final int REQUEST_MODEL_BYTES = 6;
    public static final int REQUEST_MODEL_PATH = 5;
    public static final int SUBSCRIBE_MODEL = 10;
    public static final int SYNC_MODEL = 8;
    public static final int UNSUBSCRIBE_MODEL = 11;

    void connect() throws RemoteException;

    long getResourceVersionCode(String str) throws RemoteException;

    boolean insertResourceInformation(String str) throws RemoteException;

    boolean isConnect() throws RemoteException;

    boolean isSupportModelManagement() throws RemoteException;

    byte[] requestModelBytes(long j) throws RemoteException;

    String requestModelPath(long j) throws RemoteException;

    List<AiModelBean> requestModelsByBusiDomain(String str) throws RemoteException;

    void subscribeModel(ModelUpInfo modelUpInfo, IRecordObserverCallback iRecordObserverCallback) throws RemoteException;

    boolean syncModel(String str, long j) throws RemoteException;

    void unsubscribeModel(ModelUpInfo modelUpInfo) throws RemoteException;
}
