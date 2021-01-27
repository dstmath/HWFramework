package ohos.net;

import ohos.eventhandler.EventHandler;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public class NetRemoteEvent extends RemoteObject implements IRemoteEvent {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NetEvent");
    private EventHandler mEventHandler = null;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public NetRemoteEvent(String str) {
        super(str);
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 1) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        parseDescriptor(messageParcel);
        if (messageParcel.readInt() == 0) {
            messageParcel2.writeInt(0);
            return true;
        }
        sendRemoteEvent(cvtMessageToEvent(messageParcel));
        messageParcel2.writeInt(0);
        return true;
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.mEventHandler = eventHandler;
    }

    private void sendRemoteEvent(InnerEvent innerEvent) {
        EventHandler eventHandler;
        if (innerEvent == null || (eventHandler = this.mEventHandler) == null) {
            HiLog.warn(LABEL, "drop event", new Object[0]);
        } else {
            eventHandler.sendEvent(innerEvent);
        }
    }

    private void parseDescriptor(MessageParcel messageParcel) {
        messageParcel.readInt();
        messageParcel.readInt();
        messageParcel.readString();
    }

    private InnerEvent cvtMessageToEvent(MessageParcel messageParcel) {
        InnerEvent innerEvent = InnerEvent.get();
        innerEvent.eventId = messageParcel.readInt();
        innerEvent.param = (long) messageParcel.readInt();
        messageParcel.readInt();
        HiLog.warn(LABEL, "event Id: %{public}d", Integer.valueOf(innerEvent.eventId));
        if (messageParcel.readInt() != 0) {
            HiLog.debug(LABEL, "class name: %{public}s", messageParcel.readString());
        }
        messageParcel.readLong();
        if (!EventObject.isValidData(messageParcel)) {
            return innerEvent;
        }
        EventObject eventObject = new EventObject();
        eventObject.unmarshalling(messageParcel);
        innerEvent.object = eventObject;
        return innerEvent;
    }
}
