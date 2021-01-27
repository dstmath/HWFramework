package ohos.wifi.p2p;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.Sequenceable;

interface IWifiP2pController extends IRemoteBroker {
    void deletePersistentGroup(int i, IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, int i2, int i3, String str) throws RemoteException;

    IRemoteObject init(IRemoteObject iRemoteObject, String str) throws RemoteException;

    void sendP2pRequest(int i, IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, Sequenceable sequenceable, int i2, String str) throws RemoteException;

    void setDeviceName(int i, IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, String str, int i2, String str2) throws RemoteException;
}
