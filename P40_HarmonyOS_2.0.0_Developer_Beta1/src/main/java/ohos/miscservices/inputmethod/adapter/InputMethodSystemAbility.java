package ohos.miscservices.inputmethod.adapter;

import java.util.List;
import ohos.miscservices.inputmethod.InputMethodProperty;
import ohos.miscservices.inputmethod.KeyboardType;
import ohos.miscservices.inputmethod.implement.InputMethodSystemAbilitySkeleton;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class InputMethodSystemAbility extends InputMethodSystemAbilitySkeleton {
    private static volatile InputMethodSystemAbility sInstance;
    private final InputMethodManagerAdapter mAdapter = InputMethodManagerAdapter.getInstance();

    private InputMethodSystemAbility() {
    }

    public static InputMethodSystemAbility getInstance() {
        if (sInstance == null) {
            synchronized (InputMethodSystemAbility.class) {
                if (sInstance == null) {
                    sInstance = new InputMethodSystemAbility();
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean startInput(int i) throws RemoteException {
        return this.mAdapter.startInput(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean restartInput(int i) throws RemoteException {
        return this.mAdapter.restartInput(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean startRemoteInput(int i, int i2) throws RemoteException {
        return this.mAdapter.startRemoteInput(i, i2);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean stopInput(int i) throws RemoteException {
        return this.mAdapter.stopInput(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public int getScreenMode() throws RemoteException {
        return this.mAdapter.getScreenMode();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean isAvailable() throws RemoteException {
        return this.mAdapter.isAvailable();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public int getKeyboardWindowHeight() throws RemoteException {
        return this.mAdapter.getKeyboardWindowHeight();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public KeyboardType getCurrentKeyboardType() throws RemoteException {
        return this.mAdapter.getCurrentKeyboardType();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public List<InputMethodProperty> listInputMethodEnabled() throws RemoteException {
        return this.mAdapter.listInputMethodEnabled();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public List<KeyboardType> listKeyboardType(InputMethodProperty inputMethodProperty) throws RemoteException {
        return this.mAdapter.listKeyboardType(inputMethodProperty);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public List<InputMethodProperty> listInputMethod() throws RemoteException {
        return this.mAdapter.listInputMethod();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public void displayOptionalInputMethod() throws RemoteException {
        this.mAdapter.displayOptionalInputMethod();
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public void setLocalInputDataChannel(IRemoteObject iRemoteObject) throws RemoteException {
        this.mAdapter.setLocalInputDataChannel(iRemoteObject);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public void setCursorCoordinateNotifyMode(int i) throws RemoteException {
        this.mAdapter.setCursorCoordinateNotifyMode(i);
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility
    public boolean isCursorCoordinateSubscribed() throws RemoteException {
        return this.mAdapter.isCursorCoordinateSubscribed();
    }
}
