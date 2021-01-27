package ohos.eventhandler;

import ohos.eventhandler.ICourier;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class Courier implements Sequenceable {
    public static final Sequenceable.Producer<Courier> PRODUCER = $$Lambda$Courier$t_w3BHZjjQZSEQPqFVGMwhSgb80.INSTANCE;
    private ICourier courier;

    static /* synthetic */ Courier lambda$static$0(Parcel parcel) {
        Courier courier2 = new Courier();
        courier2.unmarshalling(parcel);
        return courier2;
    }

    public Courier(EventHandler eventHandler) {
        if (eventHandler != null) {
            this.courier = eventHandler.getICourier();
        }
    }

    public Courier(IRemoteObject iRemoteObject) {
        this.courier = new ICourier.Proxy(iRemoteObject);
    }

    public Courier() {
    }

    public void send(InnerEvent innerEvent) throws RemoteException {
        this.courier.send(innerEvent);
    }

    public IRemoteObject getRemoteObject() {
        return this.courier.asObject();
    }

    public boolean marshalling(Parcel parcel) {
        ICourier iCourier = this.courier;
        IRemoteObject asObject = iCourier != null ? iCourier.asObject() : null;
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
        this.courier = new ICourier.Proxy(readRemoteObject);
        return true;
    }
}
