package ohos.security.privacyability;

import ohos.sysability.samgr.SysAbilityManager;

public class DiffPrivacyManager {
    private static final int SA_ID = 3599;
    private static volatile DiffPrivacyManager sInstance;
    private final IDiffPrivacyManager mProxy = new DiffPrivacyManagerProxy(SysAbilityManager.getSysAbility(SA_ID));

    private DiffPrivacyManager() {
    }

    public static DiffPrivacyManager getInstance() {
        if (sInstance == null) {
            synchronized (DiffPrivacyManager.class) {
                if (sInstance == null) {
                    sInstance = new DiffPrivacyManager();
                }
            }
        }
        return sInstance;
    }

    public String getDiffPrivacyBloomFilter(String str, String str2) {
        return this.mProxy.getDiffPrivacyBloomFilter(str, str2);
    }

    public String getDiffPrivacyBitsHistogram(int[] iArr, String str) {
        return this.mProxy.getDiffPrivacyBitsHistogram(iArr, str);
    }

    public String getDiffPrivacyCountSketch(String str, String str2) {
        return this.mProxy.getDiffPrivacyCountSketch(str, str2);
    }
}
