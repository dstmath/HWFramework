package ohos.ai.engine.cloudstrategy;

import java.util.Map;
import ohos.ai.engine.cloudstrategy.grs.IGrsCallback;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ICloudStrategy extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.ai.engine.cloudstrategy.ICloudStrategy";
    public static final int TRANSACTION_GRS_ASYNC_QUERY_URL = 2;
    public static final int TRANSACTION_GRS_CLEAR = 4;
    public static final int TRANSACTION_GRS_INIT = 1;
    public static final int TRANSACTION_GRS_SYNC_QUERY_URL = 3;
    public static final int TRANSACTION_POST = 7;
    public static final int TRANSACTION_POST_CONTAINS_MAP = 6;
    public static final int TRANSACTION_RESET_OKHTTP_CLIENT = 5;

    void grsAsyncQueryUrl(String str, String str2, IGrsCallback iGrsCallback) throws RemoteException;

    void grsClear() throws RemoteException;

    void grsInit() throws RemoteException;

    String grsSyncQueryUrl(String str, String str2) throws RemoteException;

    String post(String str, String str2) throws RemoteException;

    String postContainsMap(String str, String str2, Map<String, String> map) throws RemoteException;

    void resetOkHttpClient() throws RemoteException;
}
