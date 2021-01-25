package ohos.miscservices.inputmethod.adapter;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IInputMethodManagerAdapterControl extends IRemoteBroker {
    boolean notifyClientDisconnect() throws RemoteException;
}
