package ohos.miscservices.inputmethod.implement;

import java.util.List;
import ohos.miscservices.inputmethod.InputMethodProperty;
import ohos.miscservices.inputmethod.KeyboardType;
import ohos.miscservices.inputmethod.adapter.InputMethodSystemAbility;
import ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class InputMethodSystemAbilityProxy implements IInputMethodSystemAbility {
    private static final int COMMAND_START_INPUT = 1;
    private static final int COMMAND_STOP_INPUT = 2;
    private static final int ERR_OK = 0;
    private final IRemoteObject mRemote;
    private final InputMethodSystemAbility mSA = InputMethodSystemAbility.getInstance();

    public InputMethodSystemAbilityProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean startInput(int i) throws RemoteException {
        return this.mSA.startInput(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean restartInput(int i) throws RemoteException {
        return this.mSA.restartInput(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean startRemoteInput(int i, int i2) throws RemoteException {
        return this.mSA.startRemoteInput(i, i2);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean stopInput(int i) throws RemoteException {
        return this.mSA.stopInput(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public int getScreenMode() throws RemoteException {
        return this.mSA.getScreenMode();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean isAvailable() throws RemoteException {
        return this.mSA.isAvailable();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public int getKeyboardWindowHeight() throws RemoteException {
        return this.mSA.getKeyboardWindowHeight();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public KeyboardType getCurrentKeyboardType() throws RemoteException {
        return this.mSA.getCurrentKeyboardType();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public List<InputMethodProperty> listInputMethodEnabled() throws RemoteException {
        return this.mSA.listInputMethodEnabled();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public List<KeyboardType> listKeyboardType(InputMethodProperty inputMethodProperty) throws RemoteException {
        return this.mSA.listKeyboardType(inputMethodProperty);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public List<InputMethodProperty> listInputMethod() throws RemoteException {
        return this.mSA.listInputMethod();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public void displayOptionalInputMethod() throws RemoteException {
        this.mSA.displayOptionalInputMethod();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public void setLocalInputDataChannel(IRemoteObject iRemoteObject) throws RemoteException {
        this.mSA.setLocalInputDataChannel(iRemoteObject);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public void setCursorCoordinateNotifyMode(int i) throws RemoteException {
        this.mSA.setCursorCoordinateNotifyMode(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean isCursorCoordinateSubscribed() throws RemoteException {
        return this.mSA.isCursorCoordinateSubscribed();
    }
}
