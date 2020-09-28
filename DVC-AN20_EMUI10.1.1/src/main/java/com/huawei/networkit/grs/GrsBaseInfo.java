package com.huawei.networkit.grs;

import android.text.TextUtils;
import com.huawei.networkit.grs.cache.GrsPreferences;
import com.huawei.networkit.grs.common.ContainerUtils;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.local.Route;
import com.huawei.networkit.grs.utils.Encrypt;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GrsBaseInfo implements Cloneable {
    private static final String TAG = GrsBaseInfo.class.getSimpleName();
    private String androidVersion;
    private String appName;
    private String countrySource;
    private String deviceModel;
    private String issueCountry;
    private String regCountry;
    private String romVersion;
    private String serCountry;
    private String uid;
    private String versionName;

    public @interface CountryCodeSource {
        public static final String APP = "APP";
        public static final String LOCALE_INFO = "LOCALE_INFO";
        public static final String NETWORK_COUNTRY = "NETWORK_COUNTRY";
        public static final String SIM_COUNTRY = "SIM_COUNTRY";
        public static final String UNKNOWN = "UNKNOWN";
        public static final String VENDOR_COUNTRY = "VENDOR_COUNTRY";
    }

    public String getSerCountry() {
        return this.serCountry;
    }

    public void setSerCountry(String serCountry2) {
        this.serCountry = serCountry2;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName2) {
        this.versionName = versionName2;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName2) {
        this.appName = appName2;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid2) {
        this.uid = uid2;
    }

    public String getRegCountry() {
        return this.regCountry;
    }

    public void setRegCountry(String regCountry2) {
        this.regCountry = regCountry2;
    }

    public String getIssueCountry() {
        return this.issueCountry;
    }

    public void setIssueCountry(String issueCountry2) {
        this.issueCountry = issueCountry2;
    }

    public String getAndroidVersion() {
        return this.androidVersion;
    }

    public void setAndroidVersion(String androidVersion2) {
        this.androidVersion = androidVersion2;
    }

    public String getRomVersion() {
        return this.romVersion;
    }

    public void setRomVersion(String romVersion2) {
        this.romVersion = romVersion2;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setDeviceModel(String deviceModel2) {
        this.deviceModel = deviceModel2;
    }

    public String getCountrySource() {
        return this.countrySource;
    }

    public void setCountrySource(String countrySource2) {
        this.countrySource = countrySource2;
    }

    @Override // java.lang.Object
    public GrsBaseInfo clone() throws CloneNotSupportedException {
        return (GrsBaseInfo) super.clone();
    }

    public GrsBaseInfo copy() {
        GrsBaseInfo info = new GrsBaseInfo();
        info.setAppName(this.appName);
        info.setSerCountry(this.serCountry);
        info.setRegCountry(this.regCountry);
        info.setIssueCountry(this.issueCountry);
        info.setCountrySource(this.countrySource);
        info.setAndroidVersion(this.androidVersion);
        info.setDeviceModel(this.deviceModel);
        info.setRomVersion(this.romVersion);
        info.setUid(this.uid);
        info.setVersionName(this.versionName);
        return info;
    }

    public String getGrsParasKey(boolean isProguard, boolean isEncry) {
        StringBuffer stringBuffer = new StringBuffer();
        String localAppName = getAppName();
        if (!TextUtils.isEmpty(localAppName)) {
            stringBuffer.append("app_name");
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localAppName);
        }
        String reqParam = getGrsReqParamJoint(isProguard, isEncry);
        if (!TextUtils.isEmpty(reqParam)) {
            stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            stringBuffer.append(reqParam);
        }
        return stringBuffer.toString();
    }

    public String getGrsReqParamJoint(boolean isProguard, boolean isEncry) {
        StringBuffer stringBuffer = new StringBuffer();
        String localVersionName = getVersionName();
        if (!TextUtils.isEmpty(localVersionName)) {
            stringBuffer.append("app_version");
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localVersionName);
        }
        String localUid = getUid();
        if (!TextUtils.isEmpty(localUid)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append("uid");
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            if (isProguard) {
                stringBuffer.append(Encrypt.formatWithStar(localUid));
            } else if (isEncry) {
                stringBuffer.append(Encrypt.encryptBySHA256(localUid));
            } else {
                stringBuffer.append(localUid);
            }
        }
        String localRegCountry = getRegCountry();
        if (!TextUtils.isEmpty(localRegCountry) && !"UNKNOWN".equals(localRegCountry)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append(Route.REG_COUNTRY_POLICY);
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localRegCountry);
        }
        String localSerCountry = getSerCountry();
        if (!TextUtils.isEmpty(localSerCountry) && !"UNKNOWN".equals(localSerCountry)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append(Route.SER_COUNTRY_POLICY);
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localSerCountry);
        }
        String localIssueCountry = getIssueCountry();
        if (!TextUtils.isEmpty(localIssueCountry) && !"UNKNOWN".equals(localIssueCountry)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append(Route.ISSUE_COUNTRY_POLICY);
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localIssueCountry);
        }
        return getStringBuffer(stringBuffer, isEncry).toString();
    }

    private StringBuffer getStringBuffer(StringBuffer stringBuffer, boolean isEncry) {
        String localAndroidVersion = getAndroidVersion();
        if (!TextUtils.isEmpty(localAndroidVersion)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append("android_version");
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localAndroidVersion);
        }
        String localRomVersion = getRomVersion();
        if (!TextUtils.isEmpty(localRomVersion)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append("rom_version");
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localRomVersion);
        }
        String localDeviceModel = getDeviceModel();
        if (!TextUtils.isEmpty(localDeviceModel)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append("device_model");
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localDeviceModel);
        }
        String cp = GrsPreferences.getInstance().getCp();
        if (!TextUtils.isEmpty(cp) && !isEncry) {
            try {
                String cp2 = URLEncoder.encode(cp, "UTF-8");
                if (!TextUtils.isEmpty(stringBuffer.toString())) {
                    stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
                }
                stringBuffer.append("cp");
                stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
                stringBuffer.append(cp2);
            } catch (UnsupportedEncodingException e) {
                Logger.e(TAG, "cp UnsupportedEncodingException.", e);
            }
        }
        String localCountrySource = getCountrySource();
        if (!TextUtils.isEmpty(localCountrySource)) {
            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                stringBuffer.append(ContainerUtils.FIELD_DELIMITER);
            }
            stringBuffer.append("country_source");
            stringBuffer.append(ContainerUtils.KEY_VALUE_DELIMITER);
            stringBuffer.append(localCountrySource);
        }
        return stringBuffer;
    }
}
