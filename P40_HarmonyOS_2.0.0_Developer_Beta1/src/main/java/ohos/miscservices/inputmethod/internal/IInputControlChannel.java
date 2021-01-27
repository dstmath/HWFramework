package ohos.miscservices.inputmethod.internal;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;
import ohos.utils.net.Uri;

public interface IInputControlChannel extends IRemoteBroker {
    IUriPermission createUriPermission(Uri uri, String str) throws RemoteException;

    void hideKeyboardSelf(int i) throws RemoteException;

    void reportScreenMode(int i) throws RemoteException;

    boolean toNextInputMethod() throws RemoteException;
}
