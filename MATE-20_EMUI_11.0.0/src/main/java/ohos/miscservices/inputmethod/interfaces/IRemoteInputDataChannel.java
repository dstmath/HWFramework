package ohos.miscservices.inputmethod.interfaces;

import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RichContent;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public interface IRemoteInputDataChannel extends IRemoteBroker {
    void close() throws RemoteException;

    boolean deleteBackward(int i) throws RemoteException;

    boolean deleteForward(int i) throws RemoteException;

    String getBackward(int i) throws RemoteException;

    String getForward(int i) throws RemoteException;

    boolean insertRichContent(RichContent richContent) throws RemoteException;

    boolean insertText(String str) throws RemoteException;

    boolean markText(int i, int i2) throws RemoteException;

    boolean replaceMarkedText(String str) throws RemoteException;

    boolean selectText(int i, int i2) throws RemoteException;

    boolean sendCustomizedData(String str, PacMap pacMap) throws RemoteException;

    boolean sendKeyEvent(KeyEvent keyEvent) throws RemoteException;

    boolean sendKeyFunction(int i) throws RemoteException;

    EditingText subscribeEditingText(EditingCapability editingCapability) throws RemoteException;

    boolean unmarkText() throws RemoteException;
}
