package ohos.miscservices.inputmethod.internal;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface IAgentCallback extends IRemoteBroker {
    void agentCreated(IRemoteObject iRemoteObject) throws RemoteException;
}
