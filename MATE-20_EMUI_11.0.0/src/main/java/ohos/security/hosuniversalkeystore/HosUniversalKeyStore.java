package ohos.security.hosuniversalkeystore;

import java.security.cert.X509Certificate;
import ohos.aafwk.ability.Ability;
import ohos.sysability.samgr.SysAbilityManager;

public class HosUniversalKeyStore {
    private static final int SA_ID = 3599;
    private static volatile HosUniversalKeyStore sInstance;
    private IHosUniversalKeystore proxy = new HosUniversalKeystoreProxy(SysAbilityManager.getSysAbility(SA_ID));

    public static HosUniversalKeyStore getInstance() {
        if (sInstance == null) {
            synchronized (HosUniversalKeyStore.class) {
                if (sInstance == null) {
                    sInstance = new HosUniversalKeyStore();
                }
            }
        }
        return sInstance;
    }

    public void install() {
        this.proxy.install();
    }

    public X509Certificate[] attestDeviceIds(Ability ability, int[] iArr, byte[] bArr) {
        return this.proxy.attestDeviceIds(ability, iArr, bArr);
    }
}
