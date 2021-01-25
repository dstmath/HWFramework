package ohos.backgroundtaskmgr;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class ExpiredCallbackStub extends RemoteObject implements IExpiredCallback {
    private static final int ERR_OK = 0;
    private static final int LOG_DOMAIN = 218109696;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final String TAG = "ExpiredCallback";

    public IRemoteObject asObject() {
        return this;
    }

    static {
        try {
            System.loadLibrary("ipc_core.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LOG_LABEL, "Could not load ipc_core.z.so.", new Object[0]);
        }
    }

    public ExpiredCallbackStub(String str) {
        super(str);
    }

    public ExpiredCallbackStub() {
        this(IExpiredCallback.DESCRIPTOR);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        HiLog.info(LOG_LABEL, "onRemoteRequest code: %{public}d", new Object[]{Integer.valueOf(i)});
        if (!messageParcel.readInterfaceToken().equals(IExpiredCallback.DESCRIPTOR)) {
            HiLog.error(LOG_LABEL, "descriptor not match", new Object[0]);
            return false;
        } else if (i != 1) {
            return ExpiredCallbackStub.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            onExpired();
            return messageParcel2.writeInt(0);
        }
    }
}
