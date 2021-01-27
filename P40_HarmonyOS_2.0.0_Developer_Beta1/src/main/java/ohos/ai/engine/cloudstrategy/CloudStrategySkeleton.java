package ohos.ai.engine.cloudstrategy;

import java.util.HashMap;
import java.util.Optional;
import ohos.ai.engine.cloudstrategy.grs.GrsCallbackSkeleton;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class CloudStrategySkeleton extends RemoteObject implements ICloudStrategy {
    public IRemoteObject asObject() {
        return this;
    }

    public CloudStrategySkeleton() {
        super(ICloudStrategy.DESCRIPTOR);
    }

    public static Optional<ICloudStrategy> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        ICloudStrategy queryLocalInterface = iRemoteObject.queryLocalInterface(ICloudStrategy.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof ICloudStrategy)) {
            return Optional.of(new CloudStrategyProxy(iRemoteObject));
        }
        return Optional.of(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        HashMap hashMap = null;
        switch (i) {
            case 1:
                grsInit();
                return true;
            case 2:
                grsAsyncQueryUrl(messageParcel.readString(), messageParcel.readString(), GrsCallbackSkeleton.asInterface(messageParcel.readRemoteObject()).orElse(null));
                return true;
            case 3:
                return messageParcel2.writeString(grsSyncQueryUrl(messageParcel.readString(), messageParcel.readString()));
            case 4:
                grsClear();
                return true;
            case 5:
                resetOkHttpClient();
                return true;
            case 6:
                String readString = messageParcel.readString();
                String readString2 = messageParcel.readString();
                int readInt = messageParcel.readInt();
                if (readInt >= 0 && readInt <= 500) {
                    hashMap = new HashMap(readInt);
                    while (readInt > 0) {
                        hashMap.put(messageParcel.readString(), messageParcel.readString());
                        readInt--;
                    }
                }
                return messageParcel2.writeString(postContainsMap(readString, readString2, hashMap));
            case 7:
                return messageParcel2.writeString(post(messageParcel.readString(), messageParcel.readString()));
            default:
                return CloudStrategySkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }
}
