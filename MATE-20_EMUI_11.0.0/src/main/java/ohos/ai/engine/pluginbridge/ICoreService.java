package ohos.ai.engine.pluginbridge;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface ICoreService extends IRemoteBroker {
    public static final String DESCRIPTOR = "com.huawei.hiai.pdk.pluginbridge.ICoreService";
    public static final int GET_CLOUD_STRATEGY_REMOTE_OBJECT = 3;
    public static final int GET_HEALTH_CORE_REMOTE_OBJECT = 2;
    public static final int GET_MODEL_CORE_REMOTE_OBJECT = 5;
    public static final int GET_PLUGIN_LABEL_REMOTE_OBJECT = 14;
    public static final int GET_REPORT_CORE_REMOTE_OBJECT = 1;
    public static final int GET_SYSTEM_CORE_REMOTE_OBJECT = 15;
    public static final int GET_UPGRADE_STRATEGY_REMOTE_OBJECT = 4;
    public static final int IS_OPEN = 13;

    IRemoteObject getCloudStrategyRemoteObject() throws RemoteException;

    IRemoteObject getHealthCoreRemoteObject() throws RemoteException;

    IRemoteObject getModelCoreRemoteObject() throws RemoteException;

    IRemoteObject getPluginLabelRemoteObject() throws RemoteException;

    IRemoteObject getReportCoreRemoteObject() throws RemoteException;

    IRemoteObject getSystemCoreRemoteObject() throws RemoteException;

    IRemoteObject getUpgradeStrategyRemoteObject() throws RemoteException;

    boolean isOpen(int i) throws RemoteException;
}
