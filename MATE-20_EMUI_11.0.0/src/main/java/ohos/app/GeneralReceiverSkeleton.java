package ohos.app;

import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;

public abstract class GeneralReceiverSkeleton extends RemoteObject implements IGeneralReceiver {
    public IRemoteObject asObject() {
        return this;
    }

    @Override // ohos.app.IGeneralReceiver
    public abstract void sendResult(int i, PacMap pacMap);

    public GeneralReceiverSkeleton() {
        super(IGeneralReceiver.DESCRIPTOR);
    }

    public static IGeneralReceiver asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IGeneralReceiver queryLocalInterface = iRemoteObject.queryLocalInterface(IGeneralReceiver.DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new GeneralReceiverProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IGeneralReceiver) {
            return queryLocalInterface;
        }
        return null;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null) {
            return false;
        }
        if (!IGeneralReceiver.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            AppLog.e("onRemoteRequest:: token is invalid.", new Object[0]);
            return false;
        } else if (i != 1) {
            return GeneralReceiverSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            AppLog.d("GeneralReceiverSkeleton::onRemoteRequest receive ON_SEND_RESULT", new Object[0]);
            int readInt = messageParcel.readInt();
            PacMap pacMap = new PacMap();
            if (!messageParcel.readSequenceable(pacMap)) {
                pacMap = null;
            }
            sendResult(readInt, pacMap);
            return true;
        }
    }
}
