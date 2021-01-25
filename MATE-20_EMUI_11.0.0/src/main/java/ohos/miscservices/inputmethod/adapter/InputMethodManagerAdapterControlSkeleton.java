package ohos.miscservices.inputmethod.adapter;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InputMethodManagerAdapterControlSkeleton extends RemoteObject implements IInputMethodManagerAdapterControl {
    private static final int COMMAND_NOTIFY_CLIENT_DISCONNECT = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.adapter.IInputMethodManagerAdapterControl";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public InputMethodManagerAdapterControlSkeleton(String str) {
        super(str);
    }

    public static IInputMethodManagerAdapterControl asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new InputMethodManagerAdapterControlProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IInputMethodManagerAdapterControl) {
            return (IInputMethodManagerAdapterControl) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 1) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        try {
            boolean notifyClientDisconnect = notifyClientDisconnect();
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(notifyClientDisconnect ? 1 : 0);
            return notifyClientDisconnect;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }
}
