package ohos.bundlemgr;

import com.huawei.networkit.grs.common.SystemPropUtils;
import java.util.Locale;
import ohos.ai.engine.pluginlabel.PluginLabelConstants;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class CountryCodeUtils {
    private static final String ANDRIOD_SYSTEMPROP = "android.os.SystemProperties";
    private static final int COUNTRYCODE_SIZE = 2;
    private static final HiLogLabel COUNTRY_CODE_LABLE = new HiLogLabel(3, 218108160, "CountryCodeUtils");
    private static final String LOCALE_COUNTRYSYSTEMPROP = "ro.product.locale.region";
    private static final String LOG_STRING = "getCountryCode get country code from %{public}s";
    private static final String SPECIAL_COUNTRYCODE_CN = "cn";
    private static final String SPECIAL_COUNTRYCODE_EU = "eu";
    private static final String SPECIAL_COUNTRYCODE_LA = "la";
    private static final String VENDORCOUNTRY_SYSTEMPROP = "ro.hw.country";
    private String countryCode = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    private String countrySource = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;

    public CountryCodeUtils(boolean z) {
        init(z);
        this.countryCode = this.countryCode.toUpperCase(Locale.ENGLISH);
    }

    public String getCountrySource() {
        return this.countrySource;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    private void init(boolean z) {
        try {
            getVendorCountryCode();
            if (isCodeValidate()) {
                AppLog.d(COUNTRY_CODE_LABLE, LOG_STRING, "VENDOR_COUNTRY");
            } else if (isCodeValidate()) {
                AppLog.d(COUNTRY_CODE_LABLE, LOG_STRING, "SIM_COUNTRY");
            } else {
                getLocaleCountryCode();
                if (isCodeValidate()) {
                    AppLog.d(COUNTRY_CODE_LABLE, LOG_STRING, "LOCALE_INFO");
                }
            }
        } catch (Exception unused) {
            AppLog.w(COUNTRY_CODE_LABLE, "get CountryCode error", new Object[0]);
        }
    }

    private boolean isCodeValidate() {
        return !PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT.equals(this.countryCode);
    }

    private void checkCodeLength() {
        String str = this.countryCode;
        if (str == null || str.length() != 2) {
            this.countryCode = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
            this.countrySource = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
        }
    }

    private void getVendorCountryCode() {
        this.countrySource = "VENDOR_COUNTRY";
        this.countryCode = SystemPropUtils.getProperty("get", VENDORCOUNTRY_SYSTEMPROP, ANDRIOD_SYSTEMPROP, PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT);
        if (SPECIAL_COUNTRYCODE_EU.equalsIgnoreCase(this.countryCode) || SPECIAL_COUNTRYCODE_LA.equalsIgnoreCase(this.countryCode)) {
            this.countryCode = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
            this.countrySource = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
            return;
        }
        checkCodeLength();
    }

    private void getLocaleCountryCode() {
        this.countryCode = SystemPropUtils.getProperty("get", LOCALE_COUNTRYSYSTEMPROP, ANDRIOD_SYSTEMPROP, PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT);
        this.countrySource = "LOCALE_INFO";
        if (!SPECIAL_COUNTRYCODE_CN.equalsIgnoreCase(this.countryCode)) {
            AppLog.w(COUNTRY_CODE_LABLE, "countryCode from system language is not reliable.", new Object[0]);
            this.countryCode = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
            this.countrySource = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
        }
    }
}
