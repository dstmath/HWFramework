package ohos.location.callback;

import ohos.annotation.SystemApi;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

@SystemApi
public interface ISwitchCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "location.ISwitchCallback";
    public static final int RECV_SWITCH_STATUS_EVENT = 1;

    void onSwitchChange(int i) throws RemoteException;
}
