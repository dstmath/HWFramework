package ohos.interwork.eventhandler;

import ohos.eventhandler.Courier;
import ohos.eventhandler.InnerEvent;
import ohos.interwork.eventhandler.ICourierEx;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class CourierEx extends Courier {
    public static final Sequenceable.Producer<CourierEx> PRODUCER = $$Lambda$CourierEx$9RJRi5QfrmVaOUM_iKUm1gp5g.INSTANCE;
    private ICourierEx courierEx;

    static /* synthetic */ CourierEx lambda$static$0(Parcel parcel) {
        CourierEx courierEx2 = new CourierEx();
        courierEx2.unmarshalling(parcel);
        return courierEx2;
    }

    public CourierEx(EventHandlerEx eventHandlerEx) {
        if (eventHandlerEx != null) {
            this.courierEx = eventHandlerEx.getICourierEx();
        }
    }

    CourierEx(IRemoteObject iRemoteObject) {
        this.courierEx = new ICourierEx.Proxy(iRemoteObject);
    }

    CourierEx() {
    }

    public void send(InnerEvent innerEvent) throws RemoteException {
        this.courierEx.send(innerEvent);
    }

    public IRemoteObject getRemoteObject() {
        return this.courierEx.asObject();
    }

    public boolean marshalling(Parcel parcel) {
        ICourierEx iCourierEx = this.courierEx;
        IRemoteObject asObject = iCourierEx != null ? iCourierEx.asObject() : null;
        if (asObject == null || !(parcel instanceof MessageParcel)) {
            return false;
        }
        return ((MessageParcel) parcel).writeRemoteObject(asObject);
    }

    public boolean unmarshalling(Parcel parcel) {
        if (!(parcel instanceof MessageParcel)) {
            return false;
        }
        IRemoteObject readRemoteObject = ((MessageParcel) parcel).readRemoteObject();
        if (readRemoteObject == null) {
            return true;
        }
        this.courierEx = new ICourierEx.Proxy(readRemoteObject);
        return true;
    }
}
