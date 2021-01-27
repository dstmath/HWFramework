package ohos.location.callback;

import ohos.annotation.SystemApi;
import ohos.location.Location;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

@SystemApi
public interface ILocatorCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.locationkits.callback.ILocatorCallback";
    public static final int RECV_LOCATION_REPORT_EVENT = 1;
    public static final int RECV_STATUS_CHANGE_EVENT = 2;
    public static final int RECV_STATUS_ERROR_REPORT = 3;

    void onErrorReport(int i) throws RemoteException;

    void onLocationReport(Location location) throws RemoteException;

    void onStatusChanged(int i) throws RemoteException;
}
