package ohos.interwork.eventhandler;

import ohos.eventhandler.ICourier;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.utils.PacMapEx;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public interface ICourierEx extends ICourier {
    public static final int DOMAIN = 218108208;
    public static final String MESSENGER_DESCRIPTOR = "android.os.IMessenger";
    public static final int MESSENGER_SEND_CODE = 1;

    public static class Proxy implements ICourierEx {
        private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, ICourierEx.DOMAIN, "ICourierEx");
        private final CourierExProxyAdapter adapter;
        private final IRemoteObject remote;

        Proxy(IRemoteObject iRemoteObject) {
            this.remote = iRemoteObject;
            this.adapter = new CourierExProxyAdapter(iRemoteObject);
        }

        public IRemoteObject asObject() {
            return this.remote;
        }

        public void send(InnerEvent innerEvent) throws RemoteException {
            this.adapter.send(innerEvent);
        }
    }

    public static abstract class Stub extends RemoteObject implements ICourierEx {
        private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, ICourierEx.DOMAIN, "ICourierEx");

        public IRemoteObject asObject() {
            return this;
        }

        Stub() {
            super("ohos.appexecfwk.ICourier");
        }

        @Override // ohos.rpc.RemoteObject
        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (messageParcel == null || messageParcel2 == null) {
                HiLog.debug(LOG_LABEL, "Stub::onRemoteRequest data or reply is null", new Object[0]);
                return false;
            }
            String readInterfaceToken = messageParcel.readInterfaceToken();
            if ("ohos.appexecfwk.ICourier".equals(readInterfaceToken) || ICourierEx.MESSENGER_DESCRIPTOR.equals(readInterfaceToken)) {
                HiLog.debug(LOG_LABEL, "Stub::onRemoteRequest,code:%{public}d", Integer.valueOf(i));
                if (i == 1) {
                    return handleMessengerRequest(messageParcel);
                }
                if (i != 100) {
                    return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
                }
                return handleCourierRequest(messageParcel);
            }
            HiLog.error(LOG_LABEL, "Stub::onRemoteRequest invalid token:%{public}s", readInterfaceToken);
            return false;
        }

        private boolean handleCourierRequest(MessageParcel messageParcel) throws RemoteException {
            InnerEvent innerEvent = InnerEvent.get();
            messageParcel.readSequenceable(innerEvent);
            send(innerEvent);
            return true;
        }

        private boolean handleMessengerRequest(MessageParcel messageParcel) throws RemoteException {
            if (messageParcel.readInt() == 0) {
                HiLog.error(LOG_LABEL, "Stub::onRemoteRequest Message is null.", new Object[0]);
                return false;
            }
            int readInt = messageParcel.readInt();
            int readInt2 = messageParcel.readInt();
            int readInt3 = messageParcel.readInt();
            if (messageParcel.readInt() != 0) {
                HiLog.error(LOG_LABEL, "Stub::onRemoteRequest do not support parcelable", new Object[0]);
                return false;
            }
            messageParcel.readLong();
            PacMapEx pacMapEx = new PacMapEx();
            messageParcel.readPacMapEx(pacMapEx);
            IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
            messageParcel.readInt();
            InnerEventExInfo innerEventExInfo = new InnerEventExInfo();
            innerEventExInfo.arg1 = readInt2;
            innerEventExInfo.arg2 = readInt3;
            innerEventExInfo.pacMapEx = pacMapEx;
            InnerEvent innerEvent = InnerEvent.get(readInt, innerEventExInfo);
            if (readRemoteObject != null) {
                innerEvent.replyTo = new CourierEx(readRemoteObject);
            }
            send(innerEvent);
            return true;
        }
    }
}
