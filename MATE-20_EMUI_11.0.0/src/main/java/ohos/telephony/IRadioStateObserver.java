package ohos.telephony;

import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IRadioStateObserver extends IRemoteBroker {
    void onCellInfoUpdated(List<CellInformation> list) throws RemoteException;

    void onNetworkStateUpdated(NetworkState networkState) throws RemoteException;

    void onSignalInfoUpdated(List<SignalInformation> list) throws RemoteException;
}
