package ohos.miscservices.inputmethodability.interfaces;

import ohos.app.Context;
import ohos.miscservices.inputmethodability.InputMethodEngine;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface IInputMethodInterfaceAdapter extends IRemoteBroker {
    IRemoteObject onAbilityConnected(Context context, InputMethodEngine inputMethodEngine) throws RemoteException;
}
