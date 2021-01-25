package ohos.security.privacyability;

import ohos.rpc.IRemoteBroker;

public interface IIDAnonymizationManager extends IRemoteBroker {
    String getCFID(String str, String str2);

    String getCUID();

    int resetCUID();
}
