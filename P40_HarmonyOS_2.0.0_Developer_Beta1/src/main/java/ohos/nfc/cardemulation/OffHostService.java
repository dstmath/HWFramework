package ohos.nfc.cardemulation;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.rpc.IRemoteObject;

public abstract class OffHostService extends Ability {
    public static final String META_DATA_NAME = "ohos.nfc.cardemulation.data.off_host_service";
    public static final String SERVICE_NAME = "ohos.nfc.cardemulation.action.OFF_HOST_SERVICE";

    public abstract IRemoteObject onConnect(Intent intent);
}
