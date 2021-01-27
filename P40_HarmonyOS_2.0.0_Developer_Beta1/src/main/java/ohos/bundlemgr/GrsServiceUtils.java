package ohos.bundlemgr;

import android.content.Context;
import com.huawei.networkit.grs.GrsBaseInfo;
import com.huawei.networkit.grs.GrsClient;
import ohos.ai.engine.pluginlabel.PluginLabelConstants;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class GrsServiceUtils {
    private static final HiLogLabel GRS_SERVICE_LABEL = new HiLogLabel(3, 218108160, "GrsServiceUtils");
    private static final Object INSTANCE_LOCK = new Object();
    private static volatile GrsServiceUtils instance = null;
    private String hagBaseUrl;
    private boolean initSuccess = false;
    private String serCountry;

    private GrsServiceUtils() {
    }

    public static GrsServiceUtils getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new GrsServiceUtils();
                }
            }
        }
        return instance;
    }

    public boolean initGrsClient(Context context) {
        AppLog.i(GRS_SERVICE_LABEL, "grs start : get base url", new Object[0]);
        String serCountryCode = getSerCountryCode();
        if (serCountryCode.isEmpty()) {
            AppLog.i(GRS_SERVICE_LABEL, "grs end : country code is null", new Object[0]);
        } else if (!this.initSuccess || !serCountryCode.equals(getSerCountry())) {
            GrsBaseInfo grsBaseInfo = new GrsBaseInfo();
            grsBaseInfo.setAppName(FoundationConstants.APP_NAME);
            grsBaseInfo.setSerCountry(serCountryCode);
            if (context != null) {
                String synGetGrsUrl = new GrsClient(context, grsBaseInfo).synGetGrsUrl(FoundationConstants.APPLICATION_ID, FoundationConstants.ROOT);
                if (synGetGrsUrl == null || synGetGrsUrl.isEmpty()) {
                    AppLog.i(GRS_SERVICE_LABEL, "grs end : main or analytics base url is null", new Object[0]);
                } else {
                    AppLog.i(GRS_SERVICE_LABEL, "grs end : get base url success, %{private}s", synGetGrsUrl);
                    this.initSuccess = true;
                    this.serCountry = serCountryCode;
                    this.hagBaseUrl = synGetGrsUrl;
                    return true;
                }
            } else {
                AppLog.i(GRS_SERVICE_LABEL, "grs end : application context is null", new Object[0]);
            }
        } else {
            AppLog.i(GRS_SERVICE_LABEL, "grs end : base url is already set", new Object[0]);
            return true;
        }
        resetGrsProperty();
        return false;
    }

    private void resetGrsProperty() {
        AppLog.i(GRS_SERVICE_LABEL, "grs reset", new Object[0]);
        this.initSuccess = false;
        this.hagBaseUrl = null;
        this.serCountry = null;
    }

    private String getSerCountryCode() {
        String countryCode = new CountryCodeUtils(false).getCountryCode();
        return (countryCode == null || countryCode.equals(PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT)) ? "" : countryCode;
    }

    private String getSerCountry() {
        return this.serCountry;
    }

    public String getHagBaseUrl() {
        return this.hagBaseUrl;
    }
}
