package ohos.miscservices.inputmethod.adapter;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class InputMethodManagerAdapterControlProxy implements IInputMethodManagerAdapterControl {
    private static final int COMMAND_NOTIFY_CLIENT_DISCONNECT = 1;
    private static final int ERR_OK = 0;
    private final IRemoteObject remote;

    public InputMethodManagerAdapterControlProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.adapter.IInputMethodManagerAdapterControl
    public boolean notifyClientDisconnect() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        boolean z = false;
        try {
            this.remote.sendRequest(1, obtain, obtain2, new MessageOption(0));
            if (obtain2.readInt() == 0) {
                if (obtain2.readInt() == 1) {
                    z = true;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
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
