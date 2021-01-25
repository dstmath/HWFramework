package ohos.eventhandler;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public interface ICourier extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.appexecfwk.ICourier";
    public static final int SEND = 100;

    void send(InnerEvent innerEvent) throws RemoteException;

    public static class Proxy implements ICourier {
        private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218108208, "ICourier");
        private final IRemoteObject remote;

        Proxy(IRemoteObject iRemoteObject) {
            this.remote = iRemoteObject;
        }

        public IRemoteObject asObject() {
            return this.remote;
        }

        @Override // ohos.eventhandler.ICourier
        public void send(InnerEvent innerEvent) throws RemoteException {
            HiLog.debug(LOG_LABEL, "Proxy::send called", new Object[0]);
            if (this.remote == null) {
                HiLog.error(LOG_LABEL, "Proxy::send remote is null", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                if (obtain.writeInterfaceToken(ICourier.DESCRIPTOR)) {
                    obtain.writeSequenceable(innerEvent);
                    this.remote.sendRequest(100, obtain, obtain2, messageOption);
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    public static abstract class Stub extends RemoteObject implements ICourier {
        private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218108208, "ICourier");

        Stub() {
            super(ICourier.DESCRIPTOR);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (messageParcel == null || messageParcel2 == null) {
                HiLog.debug(LOG_LABEL, "Stub::onRemoteRequest data or reply is null", new Object[0]);
                return false;
            } else if (!ICourier.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
                HiLog.error(LOG_LABEL, "Stub::onRemoteRequest token is invalid.", new Object[0]);
                return false;
            } else if (i != 100) {
                return ICourier.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                InnerEvent innerEvent = InnerEvent.get();
                messageParcel.readSequenceable(innerEvent);
                send(innerEvent);
                HiLog.debug(LOG_LABEL, "Stub::onRemoteRequest send success", new Object[0]);
                return true;
            }
        }

        public IRemoteObject asObject() {
            HiLog.debug(LOG_LABEL, "Stub::asObject called", new Object[0]);
            return this;
        }
    }
}
