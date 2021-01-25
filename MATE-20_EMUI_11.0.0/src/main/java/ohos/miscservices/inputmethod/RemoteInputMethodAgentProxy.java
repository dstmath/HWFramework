package ohos.miscservices.inputmethod;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class RemoteInputMethodAgentProxy implements IRemoteInputMethodAgent {
    private static final int COMMAND_SET_REMOTE_OBJECT = 1;
    private static final int ERR_OK = 0;
    private final IRemoteObject remote;

    public RemoteInputMethodAgentProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.IRemoteInputMethodAgent
    public void setRemoteObject(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2, String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeRemoteObject(iRemoteObject);
        obtain.writeRemoteObject(iRemoteObject2);
        obtain.writeString(str);
        try {
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                obtain.reclaim();
                obtain2.reclaim();
                return;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }
}
