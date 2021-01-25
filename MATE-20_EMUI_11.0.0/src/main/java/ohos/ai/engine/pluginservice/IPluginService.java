package ohos.ai.engine.pluginservice;

import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public interface IPluginService extends IRemoteBroker {
    public static final int CHECK_PLUGIN_INSTALLED = 1;
    public static final String DESCRIPTOR = "com.huawei.hiai.pdk.pluginservice.IPluginService";
    public static final int GET_HOST_REMOTE_OBJECT = 4;
    public static final int GET_PLUGIN_NAME = 5;
    public static final int GET_PLUGIN_NAMES = 6;
    public static final int GET_SPLIT_REMOTE_OBJECT = 3;
    public static final int GET_SPLIT_REMOTE_OBJECT_NAME = 7;
    public static final int GET_SPLIT_REMOTE_OBJECT_NAMES = 8;
    public static final int IS_OPEN = 9;
    public static final int PROCESS = 10;
    public static final int START_INSTALL_PLUGIN = 2;

    int checkPluginInstalled(List<PluginRequest> list) throws RemoteException;

    IRemoteObject getHostRemoteObject() throws RemoteException;

    String getPluginName(int i) throws RemoteException;

    List<String> getPluginNames(int[] iArr) throws RemoteException;

    IRemoteObject getSplitRemoteObject(int i) throws RemoteException;

    String getSplitRemoteObjectName(int i) throws RemoteException;

    List<String> getSplitRemoteObjectNames(int[] iArr) throws RemoteException;

    boolean isOpen(int i) throws RemoteException;

    PacMap process(PacMap pacMap) throws RemoteException;

    void startInstallPlugin(List<PluginRequest> list, String str, ILoadPluginCallback iLoadPluginCallback) throws RemoteException;
}
