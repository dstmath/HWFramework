package ohos.ai.engine.pluginservice;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ILoadPluginCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "com.huawei.hiai.pdk.pluginservice.ILoadPluginCallback";
    public static final int ON_PROGRESS = 2;
    public static final int ON_RESULT = 1;

    void onProgress(int i) throws RemoteException;

    void onResult(int i) throws RemoteException;
}
