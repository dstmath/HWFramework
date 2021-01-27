package ohos.security.privacyability;

import ohos.sysability.samgr.SysAbilityManager;

public class IDAnonymizationManager {
    private static final int SA_ID = 3599;
    private static volatile IDAnonymizationManager sInstance;
    private IIDAnonymizationManager mProxy = new IDAnonymizationManagerProxy(SysAbilityManager.getSysAbility(SA_ID));

    private IDAnonymizationManager() {
    }

    public static IDAnonymizationManager getInstance() {
        if (sInstance == null) {
            synchronized (IDAnonymizationManager.class) {
                if (sInstance == null) {
                    sInstance = new IDAnonymizationManager();
                }
            }
        }
        return sInstance;
    }

    public String getCUID() {
        return this.mProxy.getCUID();
    }

    public String getCFID(String str, String str2) {
        return (str == null || str2 == null) ? "" : this.mProxy.getCFID(str, str2);
    }

    public int resetCUID() {
        return this.mProxy.resetCUID();
    }
}
