package ohos.miscservices.inputmethod.interfaces;

import java.util.List;
import ohos.miscservices.inputmethod.InputMethodProperty;
import ohos.miscservices.inputmethod.KeyboardType;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface IInputMethodSystemAbility extends IRemoteBroker {
    void displayOptionalInputMethod() throws RemoteException;

    KeyboardType getCurrentKeyboardType() throws RemoteException;

    int getKeyboardWindowHeight() throws RemoteException;

    int getScreenMode() throws RemoteException;

    boolean isAvailable() throws RemoteException;

    boolean isCaretCoordinateSubscribed() throws RemoteException;

    List<InputMethodProperty> listInputMethod() throws RemoteException;

    List<InputMethodProperty> listInputMethodEnabled() throws RemoteException;

    List<KeyboardType> listKeyboardType(InputMethodProperty inputMethodProperty) throws RemoteException;

    boolean restartInput(int i) throws RemoteException;

    void setCaretCoordinateNotifyMode(int i) throws RemoteException;

    void setLocalInputDataChannel(IRemoteObject iRemoteObject) throws RemoteException;

    boolean startInput(int i) throws RemoteException;

    boolean startRemoteInput(int i, int i2) throws RemoteException;

    boolean stopInput(int i) throws RemoteException;
}
