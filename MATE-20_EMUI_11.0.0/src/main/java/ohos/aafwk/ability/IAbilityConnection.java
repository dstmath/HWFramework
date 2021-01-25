package ohos.aafwk.ability;

import ohos.bundle.ElementName;
import ohos.rpc.IRemoteObject;

public interface IAbilityConnection {
    void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i);

    void onAbilityDisconnectDone(ElementName elementName, int i);
}
