package ohos.security.securitycenter;

import java.util.List;
import ohos.sysability.samgr.SysAbilityManager;

public class AntiMaliciousManager {
    private static final int SA_ID = 3599;
    public static final int WIFI_STATUS_CURRENT_SSID_IS_NOT_SECURE = 2;
    public static final int WIFI_STATUS_CURRENT_SSID_IS_SECURE = 1;
    public static final int WIFI_STATUS_DETECT_ERROR = -1;
    public static final int WIFI_STATUS_NO_SSID = 0;
    private static volatile AntiMaliciousManager sInstance;
    private final IAntiMaliciousManager mProxy = new AntiMaliciousManagerProxy(SysAbilityManager.getSysAbility(SA_ID));

    private AntiMaliciousManager() {
    }

    public static AntiMaliciousManager getInstance() {
        if (sInstance == null) {
            synchronized (AntiMaliciousManager.class) {
                if (sInstance == null) {
                    sInstance = new AntiMaliciousManager();
                }
            }
        }
        return sInstance;
    }

    public List<VirusAppInfo> getVirusAppList() throws AntiMaliciousException {
        return this.mProxy.getVirusAppList();
    }

    public int getWifiThreatDetectStatus() throws AntiMaliciousException {
        return this.mProxy.getWifiThreatDetectStatus();
    }
}
