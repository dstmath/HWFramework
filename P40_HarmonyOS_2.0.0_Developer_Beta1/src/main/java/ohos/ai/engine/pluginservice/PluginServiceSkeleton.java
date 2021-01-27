package ohos.ai.engine.pluginservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.ai.engine.utils.HiAILog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;

public abstract class PluginServiceSkeleton extends RemoteObject implements IPluginService {
    public IRemoteObject asObject() {
        return this;
    }

    public PluginServiceSkeleton() {
        super(IPluginService.DESCRIPTOR);
    }

    public static Optional<IPluginService> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IPluginService queryLocalInterface = iRemoteObject.queryLocalInterface(IPluginService.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IPluginService)) {
            return Optional.of(new PluginServiceProxy(iRemoteObject));
        }
        return Optional.of(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        PluginRequest pluginRequest;
        PluginRequest pluginRequest2;
        HiAILog.info("PluginServiceSkeleton", "onRemoteRequest");
        messageParcel.readInterfaceToken();
        PacMap pacMap = null;
        switch (i) {
            case 1:
                ArrayList arrayList = new ArrayList();
                int readInt = messageParcel.readInt();
                while (readInt > 0 && readInt <= 128) {
                    if (messageParcel.readInt() != 0) {
                        pluginRequest = new PluginRequest();
                        pluginRequest.unmarshalling(messageParcel);
                    } else {
                        pluginRequest = null;
                    }
                    arrayList.add(pluginRequest);
                    readInt--;
                }
                int checkPluginInstalled = checkPluginInstalled(arrayList);
                messageParcel2.writeInt(0);
                return messageParcel2.writeInt(checkPluginInstalled);
            case 2:
                ArrayList arrayList2 = new ArrayList();
                int readInt2 = messageParcel.readInt();
                while (readInt2 > 0 && readInt2 <= 128) {
                    if (messageParcel.readInt() != 0) {
                        pluginRequest2 = new PluginRequest();
                        pluginRequest2.unmarshalling(messageParcel);
                    } else {
                        pluginRequest2 = null;
                    }
                    arrayList2.add(pluginRequest2);
                    readInt2--;
                }
                startInstallPlugin(arrayList2, messageParcel.readString(), LoadPluginCallbackSkeleton.asInterface(messageParcel.readRemoteObject()).orElse(null));
                messageParcel2.writeInt(0);
                return true;
            case 3:
                IRemoteObject splitRemoteObject = getSplitRemoteObject(messageParcel.readInt());
                messageParcel2.writeInt(0);
                return messageParcel2.writeRemoteObject(splitRemoteObject);
            case 4:
                IRemoteObject hostRemoteObject = getHostRemoteObject();
                messageParcel2.writeInt(0);
                return messageParcel2.writeRemoteObject(hostRemoteObject);
            case 5:
                String pluginName = getPluginName(messageParcel.readInt());
                messageParcel2.writeInt(0);
                return messageParcel2.writeString(pluginName);
            case 6:
                List<String> pluginNames = getPluginNames(messageParcel.readIntArray());
                messageParcel2.writeInt(0);
                if (pluginNames == null) {
                    messageParcel2.writeInt(-1);
                    break;
                } else {
                    boolean writeInt = messageParcel2.writeInt(pluginNames.size());
                    boolean z = writeInt;
                    for (String str : pluginNames) {
                        z = messageParcel2.writeString(str);
                    }
                    return z;
                }
            case 7:
                String splitRemoteObjectName = getSplitRemoteObjectName(messageParcel.readInt());
                messageParcel2.writeInt(0);
                return messageParcel2.writeString(splitRemoteObjectName);
            case 8:
                List<String> splitRemoteObjectNames = getSplitRemoteObjectNames(messageParcel.readIntArray());
                messageParcel2.writeInt(0);
                if (splitRemoteObjectNames == null) {
                    messageParcel2.writeInt(-1);
                    break;
                } else {
                    boolean writeInt2 = messageParcel2.writeInt(splitRemoteObjectNames.size());
                    boolean z2 = writeInt2;
                    for (String str2 : splitRemoteObjectNames) {
                        z2 = messageParcel2.writeString(str2);
                    }
                    return z2;
                }
            case 9:
                boolean isOpen = isOpen(messageParcel.readInt());
                messageParcel2.writeInt(0);
                return messageParcel2.writeInt(isOpen ? 1 : 0);
            case 10:
                if (messageParcel.readInt() != 0) {
                    pacMap = new PacMap();
                    pacMap.unmarshalling(messageParcel);
                }
                PacMap process = process(pacMap);
                messageParcel2.writeInt(0);
                if (process != null) {
                    messageParcel2.writeInt(1);
                    process.marshalling(messageParcel2);
                    return true;
                }
                messageParcel2.writeInt(0);
                return true;
            default:
                return PluginServiceSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        return false;
    }
}
