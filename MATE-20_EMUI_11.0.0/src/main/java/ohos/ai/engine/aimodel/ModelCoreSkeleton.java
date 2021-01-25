package ohos.ai.engine.aimodel;

import java.util.List;
import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class ModelCoreSkeleton extends RemoteObject implements IModelCore {
    public IRemoteObject asObject() {
        return this;
    }

    public ModelCoreSkeleton() {
        super(IModelCore.DESCRIPTOR);
    }

    public static Optional<IModelCore> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IModelCore queryLocalInterface = iRemoteObject.queryLocalInterface(IModelCore.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IModelCore)) {
            return Optional.ofNullable(new ModelCoreProxy(iRemoteObject));
        }
        return Optional.ofNullable(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        switch (i) {
            case 1:
                return messageParcel2.writeBoolean(isConnect());
            case 2:
                connect();
                return true;
            case 3:
                return messageParcel2.writeBoolean(insertResourceInformation(messageParcel.readString()));
            case 4:
                return messageParcel2.writeLong(getResourceVersionCode(messageParcel.readString()));
            case 5:
                return messageParcel2.writeString(requestModelPath(messageParcel.readLong()));
            case 6:
                return messageParcel2.writeByteArray(requestModelBytes(messageParcel.readLong()));
            case 7:
                List<AiModelBean> requestModelsByBusiDomain = requestModelsByBusiDomain(messageParcel.readString());
                if (requestModelsByBusiDomain == null) {
                    return messageParcel2.writeInt(-1);
                }
                boolean writeInt = messageParcel2.writeInt(requestModelsByBusiDomain.size());
                for (AiModelBean aiModelBean : requestModelsByBusiDomain) {
                    messageParcel2.writeSequenceable(aiModelBean);
                }
                return writeInt;
            case 8:
                return messageParcel2.writeBoolean(syncModel(messageParcel.readString(), messageParcel.readLong()));
            case 9:
                return messageParcel2.writeBoolean(isSupportModelManagement());
            case 10:
                ModelUpInfo modelUpInfo = new ModelUpInfo();
                messageParcel.readSequenceable(modelUpInfo);
                subscribeModel(modelUpInfo, RecordObserverCallbackSkeleton.asInterface(messageParcel.readRemoteObject()).orElse(null));
                return true;
            case 11:
                ModelUpInfo modelUpInfo2 = new ModelUpInfo();
                messageParcel.readSequenceable(modelUpInfo2);
                unsubscribeModel(modelUpInfo2);
                return true;
            default:
                return ModelCoreSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }
}
