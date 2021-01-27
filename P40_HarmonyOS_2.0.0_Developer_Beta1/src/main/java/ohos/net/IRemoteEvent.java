package ohos.net;

import ohos.rpc.IRemoteBroker;

public interface IRemoteEvent extends IRemoteBroker {
    public static final String DESCRIPTOR = "ipc.msg";
    public static final int MSG_TRANSACTION_REQUEST = 1;
}
