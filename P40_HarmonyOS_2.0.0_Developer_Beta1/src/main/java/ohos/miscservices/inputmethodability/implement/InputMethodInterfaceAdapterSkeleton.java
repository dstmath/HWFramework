package ohos.miscservices.inputmethodability.implement;

import ohos.miscservices.inputmethodability.interfaces.IInputMethodInterfaceAdapter;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InputMethodInterfaceAdapterSkeleton extends RemoteObject implements IInputMethodInterfaceAdapter {
    private static final int COMMAND_ON_ABILITY_CONNECTED = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethodability.interfaces.IInputMethodInterfaceAdapter";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static InputMethodInterfaceAdapterProxy sProxy;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public InputMethodInterfaceAdapterSkeleton(String str) {
        super(str);
    }

    public static IInputMethodInterfaceAdapter asInterface(IRemoteObject iRemoteObject) {
        InputMethodInterfaceAdapterProxy inputMethodInterfaceAdapterProxy;
        if (iRemoteObject == null) {
            synchronized (IInputMethodInterfaceAdapter.class) {
                if (sProxy == null) {
                    sProxy = new InputMethodInterfaceAdapterProxy(null);
                }
                inputMethodInterfaceAdapterProxy = sProxy;
            }
            return inputMethodInterfaceAdapterProxy;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new InputMethodInterfaceAdapterProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IInputMethodInterfaceAdapter) {
            return (IInputMethodInterfaceAdapter) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 1) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        messageParcel.readRemoteObject();
        try {
            IRemoteObject onAbilityConnected = onAbilityConnected(null, null);
            messageParcel2.writeInt(0);
            messageParcel2.writeRemoteObject(onAbilityConnected);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }
}
