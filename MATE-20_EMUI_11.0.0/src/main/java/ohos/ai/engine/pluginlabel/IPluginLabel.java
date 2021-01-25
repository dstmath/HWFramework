package ohos.ai.engine.pluginlabel;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IPluginLabel extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.ai.engine.pluginlabel.IPluginLabel";
    public static final int GET_CAMERA_LABEL = 5;
    public static final int GET_COMPUTATIONAL_RESOURCE_LABEL = 2;
    public static final int GET_DISTANCE_LABEL = 4;
    public static final int GET_PLUGIN_LABEL_INFO = 6;
    public static final int GET_REGION_LABEL = 1;
    public static final int GET_XPU_LABEL = 3;

    String getCameraLabel() throws RemoteException;

    String getComputationalResourceLabel() throws RemoteException;

    String getDistanceLabel() throws RemoteException;

    PluginLabelInfo getPluginLabelInfo() throws RemoteException;

    String getRegionLabel() throws RemoteException;

    String getXpuLabel() throws RemoteException;
}
