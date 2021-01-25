package ohos.msg;

import ohos.rpc.IRemoteBroker;

public interface IMessenger extends IRemoteBroker {
    public static final String DESCRIPTOR = "ipc.msg";
    public static final int MSG_TRANSACTION_REQUEST = 1;

    int sendMessage(Message message) throws MessengerException;
}
