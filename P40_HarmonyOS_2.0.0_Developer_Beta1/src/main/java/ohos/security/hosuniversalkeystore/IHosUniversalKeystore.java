package ohos.security.hosuniversalkeystore;

import java.security.cert.X509Certificate;
import ohos.aafwk.ability.Ability;
import ohos.rpc.IRemoteBroker;

public interface IHosUniversalKeystore extends IRemoteBroker {
    public static final String DESCRIPTOR = "IHosUniversalKeystore";

    X509Certificate[] attestDeviceIds(Ability ability, int[] iArr, byte[] bArr);

    void install();
}
