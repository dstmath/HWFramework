package ohos.msg;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

class MessengerStub extends RemoteObject implements IMessenger {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "MessengerStub");
    private IMessengerHandler mMessengerHandler = null;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public MessengerStub(String str) {
        super(str);
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        HiLog.debug(TAG, "call TestServiceStub's onTransact", new Object[0]);
        if (i == 1) {
            return messageParcel2.writeInt(sendMessage(this.mMessengerHandler.unmarshalling(messageParcel)));
        }
        try {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } catch (MessengerException unused) {
            HiLog.error(TAG, "fail to receive remote request in transcation", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setMessageHandler(IMessengerHandler iMessengerHandler) {
        this.mMessengerHandler = iMessengerHandler;
    }

    @Override // ohos.msg.IMessenger
    public int sendMessage(Message message) throws MessengerException {
        HiLog.debug(TAG, "MessengerStub::SendMessage entry", new Object[0]);
        if (message != null) {
            IMessengerHandler iMessengerHandler = this.mMessengerHandler;
            if (iMessengerHandler != null) {
                return iMessengerHandler.onReceiveMessage(message);
            }
            throw new MessengerException("mMessengerHandler is null", -1);
        }
        throw new MessengerException("msg is null when SendMessage.", -1);
    }
}
