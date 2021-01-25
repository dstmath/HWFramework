package ohos.msg;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

class MessengerProxy implements IMessenger {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "MessengerProxy");
    private final IRemoteObject mRemoteObject;

    MessengerProxy(IRemoteObject iRemoteObject) {
        this.mRemoteObject = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemoteObject;
    }

    @Override // ohos.msg.IMessenger
    public int sendMessage(Message message) throws MessengerException {
        HiLog.debug(TAG, "MessengerProxy::SendMessage entry", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            message.marshalling(obtain);
            HiLog.info(TAG, "Message transact start", new Object[0]);
            this.mRemoteObject.sendRequest(1, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 0) {
                HiLog.info(TAG, "transaction replyData is %{public}d", Integer.valueOf(readInt));
                obtain2.reclaim();
                obtain.reclaim();
                return readInt;
            }
            HiLog.info(TAG, "transaction failed in replydata", new Object[0]);
            throw new MessengerException("transaction failed in replydata", -1);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "fail to sendMessage in transcation", new Object[0]);
            throw new MessengerException("SendMessage failed in transcat", -1);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }
}
