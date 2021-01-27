package ohos.miscservices.inputmethod.internal;

import ohos.miscservices.inputmethod.EditingText;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IInputDataChannelCallback extends IRemoteBroker {
    void notifyEditingText(EditingText editingText) throws RemoteException;

    void notifyInsertRichContentResult(boolean z) throws RemoteException;

    void notifySubscribeCaretContextResult(boolean z) throws RemoteException;

    void setAutoCapitalizeMode(int i) throws RemoteException;

    void setBackward(String str) throws RemoteException;

    void setForward(String str) throws RemoteException;

    void setSelectedText(String str) throws RemoteException;
}
