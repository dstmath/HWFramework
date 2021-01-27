package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class AgentCallbackProxy implements IAgentCallback {
    private static final int AGENT_CREATED = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IAgentCallback";
    private static final int ERR_OK = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "AgentCallbackProxy");
    private static final int WRITE_MSG_ERROR = -2;
    private final IRemoteObject remote;

    public AgentCallbackProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.internal.IAgentCallback
    public void agentCreated(IRemoteObject iRemoteObject) throws RemoteException {
        HiLog.info(TAG, "agentCreated", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "agentCreated writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeRemoteObject(iRemoteObject);
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
