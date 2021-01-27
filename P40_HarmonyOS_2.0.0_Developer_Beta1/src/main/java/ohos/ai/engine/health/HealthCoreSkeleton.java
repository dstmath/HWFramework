package ohos.ai.engine.health;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;

public abstract class HealthCoreSkeleton extends RemoteObject implements IHealthCore {
    public IRemoteObject asObject() {
        return this;
    }

    public HealthCoreSkeleton() {
        super(IHealthCore.DESCRIPTOR);
    }

    public static Optional<IHealthCore> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IHealthCore queryLocalInterface = iRemoteObject.queryLocalInterface(IHealthCore.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IHealthCore)) {
            return Optional.of(new HealthCoreProxy(iRemoteObject));
        }
        return Optional.of(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        PacMap pacMap = new PacMap();
        if (i == 1) {
            int readInt = messageParcel.readInt();
            String readString = messageParcel.readString();
            messageParcel.readSequenceable(pacMap);
            return messageParcel2.writeInt(requestRunning(readInt, readString, pacMap));
        } else if (i == 2) {
            int readInt2 = messageParcel.readInt();
            String readString2 = messageParcel.readString();
            messageParcel.readSequenceable(pacMap);
            return messageParcel2.writeInt(reportCompleted(readInt2, readString2, pacMap));
        } else if (i == 3) {
            return messageParcel2.writeBoolean(call(messageParcel.readString(), messageParcel.readString()));
        } else {
            if (i != 4) {
                return HealthCoreSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
            return messageParcel2.writeInt(getProcessPriority(messageParcel.readString()));
        }
    }
}
