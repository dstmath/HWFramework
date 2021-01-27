package ohos.ai.engine.cloudstrategy.grs;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IGrsCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.ai.engine.cloudstrategy.grs.IGrsCallback";
    public static final int TRANSACTION_ON_GRS_RESULT = 1;

    void onGrsResult(int i, String str) throws RemoteException;
}
