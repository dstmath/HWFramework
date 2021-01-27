package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class AgentCallbackSkeleton extends RemoteObject implements IAgentCallback {
    private static final int AGENT_CREATED = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IAgentCallback";
    private static final int ERR_OK = 0;
    private static final int READ_MSG_ERROR = -3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "AgentCallbackSkeleton");

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public AgentCallbackSkeleton(String str) {
        super(str);
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 1) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        HiLog.info(TAG, "AGENT_CREATED", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "agentCreated readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        agentCreated(messageParcel.readRemoteObject());
        messageParcel2.writeInt(0);
        return true;
    }

    public static IAgentCallback asInterface(IRemoteObject iRemoteObject) {
        HiLog.info(TAG, "asInterface.", new Object[0]);
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new AgentCallbackProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IAgentCallback) {
            return (IAgentCallback) queryLocalInterface;
        }
        return null;
    }
}
