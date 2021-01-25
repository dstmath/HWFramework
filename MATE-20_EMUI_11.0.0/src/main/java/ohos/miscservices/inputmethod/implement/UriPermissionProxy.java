package ohos.miscservices.inputmethod.implement;

import ohos.miscservices.inputmethod.IUriPermission;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class UriPermissionProxy implements IUriPermission {
    private static final int COMMAND_RELEASE = 2;
    private static final int COMMAND_TAKE = 1;
    private static final int ERR_OK = 0;
    private final IRemoteObject remote;

    public UriPermissionProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.IUriPermission
    public void take() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.remote.sendRequest(1, obtain, obtain2, new MessageOption(0));
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

    @Override // ohos.miscservices.inputmethod.IUriPermission
    public void release() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.remote.sendRequest(2, obtain, obtain2, new MessageOption(0));
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
