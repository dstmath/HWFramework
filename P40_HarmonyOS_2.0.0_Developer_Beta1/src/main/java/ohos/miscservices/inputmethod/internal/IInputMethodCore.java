package ohos.miscservices.inputmethod.internal;

import ohos.miscservices.inputmethod.EditorAttribute;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface IInputMethodCore extends IRemoteBroker {
    void createAgent(IRemoteObject iRemoteObject) throws RemoteException;

    boolean hideKeyboard(int i) throws RemoteException;

    void initializeInput(IRemoteObject iRemoteObject, int i, IRemoteObject iRemoteObject2) throws RemoteException;

    boolean showKeyboard(int i) throws RemoteException;

    boolean startInput(IRemoteObject iRemoteObject, EditorAttribute editorAttribute, IRemoteObject iRemoteObject2) throws RemoteException;
}
