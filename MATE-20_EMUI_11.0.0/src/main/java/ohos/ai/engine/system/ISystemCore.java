package ohos.ai.engine.system;

import java.util.Optional;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ISystemCore extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.ai.engine.system.ISystemCore";
    public static final int GET_DEVICE_BRAND = 6;
    public static final int GET_PROP = 1;
    public static final int GET_SERIAL_NUMBER = 3;
    public static final int GET_SYSTEM_MODEL = 5;
    public static final int GET_SYSTEM_VERSION = 4;
    public static final int GET_UDID = 2;

    Optional<String> getDeviceBrand() throws RemoteException;

    Optional<String> getProp(String str, String str2) throws RemoteException;

    Optional<String> getSerialNumber() throws RemoteException;

    Optional<String> getSystemModel() throws RemoteException;

    Optional<String> getSystemVersion() throws RemoteException;

    Optional<String> getUdid() throws RemoteException;
}
