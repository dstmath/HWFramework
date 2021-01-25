package ohos.miscservices.inputmethod;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface IRemoteInputMethodAgent extends IRemoteBroker {
    void setRemoteObject(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, String str) throws RemoteException;
}
