package ohos.telephony;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ICellularDataStateObserver extends IRemoteBroker {
    void onCellularDataConnectStateUpdated(int i, int i2) throws RemoteException;

    void onCellularDataFlow(int i) throws RemoteException;
}
