package ohos.miscservices.inputmethod;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class RemoteInputMethodAgentSkeleton extends RemoteObject implements IRemoteInputMethodAgent {
    private static final int COMMAND_SET_REMOTE_OBJECT = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.IRemoteInputMethodAgent";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public RemoteInputMethodAgentSkeleton(String str) {
        super(str);
    }

    public static IRemoteInputMethodAgent asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new RemoteInputMethodAgentProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IRemoteInputMethodAgent) {
            return (IRemoteInputMethodAgent) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 1) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        try {
            setRemoteObject(messageParcel.readRemoteObject(), messageParcel.readRemoteObject(), messageParcel.readString());
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }
}
