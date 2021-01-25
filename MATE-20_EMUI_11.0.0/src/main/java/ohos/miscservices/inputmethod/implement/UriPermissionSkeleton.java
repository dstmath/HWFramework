package ohos.miscservices.inputmethod.implement;

import ohos.miscservices.inputmethod.IUriPermission;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class UriPermissionSkeleton extends RemoteObject implements IUriPermission {
    private static final int COMMAND_RELEASE = 2;
    private static final int COMMAND_TAKE = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.interfaces.IUriPermission";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public UriPermissionSkeleton(String str) {
        super(str);
    }

    public static IUriPermission asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new UriPermissionProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IUriPermission) {
            return (IUriPermission) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i == 1) {
            try {
                take();
                messageParcel2.writeInt(0);
                return true;
            } catch (RemoteException unused) {
                messageParcel2.writeInt(-1);
                return true;
            }
        } else if (i != 2) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            try {
                release();
                messageParcel2.writeInt(0);
                return true;
            } catch (RemoteException unused2) {
                messageParcel2.writeInt(-1);
                return true;
            }
        }
    }
}
