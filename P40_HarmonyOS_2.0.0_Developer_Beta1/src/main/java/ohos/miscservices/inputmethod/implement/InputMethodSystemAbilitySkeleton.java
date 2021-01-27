package ohos.miscservices.inputmethod.implement;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InputMethodSystemAbilitySkeleton extends RemoteObject implements IInputMethodSystemAbility {
    private static final int COMMAND_START_INPUT = 1;
    private static final int COMMAND_STOP_INPUT = 2;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.implement.IInputMethodSystemAbility";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodSystemAbilitySkeleton");
    private static InputMethodSystemAbilityProxy sProxy;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public InputMethodSystemAbilitySkeleton() {
        super(DESCRIPTOR);
    }

    public static IInputMethodSystemAbility asInterface(IRemoteObject iRemoteObject) {
        InputMethodSystemAbilityProxy inputMethodSystemAbilityProxy;
        if (iRemoteObject == null) {
            synchronized (IInputMethodSystemAbility.class) {
                if (sProxy == null) {
                    sProxy = new InputMethodSystemAbilityProxy(null);
                }
                inputMethodSystemAbilityProxy = sProxy;
            }
            return inputMethodSystemAbilityProxy;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new InputMethodSystemAbilityProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IInputMethodSystemAbility) {
            return (IInputMethodSystemAbility) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i == 1) {
            HiLog.info(TAG, "startInput is implemented", new Object[0]);
            return true;
        } else if (i != 2) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            HiLog.info(TAG, "stopInput is implemented", new Object[0]);
            return true;
        }
    }
}
