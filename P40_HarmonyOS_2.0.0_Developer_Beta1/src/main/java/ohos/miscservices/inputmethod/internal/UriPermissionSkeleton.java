package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class UriPermissionSkeleton extends RemoteObject implements IUriPermission {
    private static final int COMMAND_RELEASE = 2;
    private static final int COMMAND_TAKE = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IUriPermission";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static final int READ_MSG_ERROR = -3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "UriPermissionSkeleton");

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
        if (i != 1) {
            if (i != 2) {
                return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
            if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
                HiLog.error(TAG, "release readInterfaceToken failed.", new Object[0]);
                messageParcel2.writeInt(-3);
                return false;
            }
            try {
                release();
                messageParcel2.writeInt(0);
                return true;
            } catch (RemoteException unused) {
                messageParcel2.writeInt(-1);
                return true;
            }
        } else if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "take readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        } else {
            try {
                take();
                messageParcel2.writeInt(0);
                return true;
            } catch (RemoteException unused2) {
                messageParcel2.writeInt(-1);
                return true;
            }
        }
    }
}
