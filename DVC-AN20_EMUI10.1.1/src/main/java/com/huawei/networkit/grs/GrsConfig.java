package com.huawei.networkit.grs;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.networkit.grs.common.PackageUtils;
import com.huawei.networkit.grs.common.StringUtils;

public class GrsConfig {
    private String appName;
    private String countrySource;
    private String issueCountry;
    private String regCountry;
    private String serCountry;
    private String userId;
    private String versionName;

    public String getCountrySource() {
        return this.countrySource;
    }

    public void setCountrySource(String countrySource2) {
        this.countrySource = countrySource2;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName2) {
        this.appName = appName2;
    }

    public String getuserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
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

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName2) {
        this.versionName = versionName2;
    }

    public String getSerCountry() {
        return this.serCountry;
    }

    public void setSerCountry(String serCountry2) {
        this.serCountry = serCountry2;
    }

    public GrsBaseInfo getGrsBaseInfo(Context context) {
        GrsBaseInfo params = new GrsBaseInfo();
        params.setSerCountry(this.serCountry);
        params.setVersionName(TextUtils.isEmpty(this.versionName) ? PackageUtils.getVersionName(context) : this.versionName);
        params.setAppName(this.appName);
        params.setUid(this.userId);
        params.setRegCountry(this.regCountry);
        params.setIssueCountry(this.issueCountry);
        params.setCountrySource(this.countrySource);
        return params;
    }

    public boolean equal(GrsConfig config) {
        return config != null && StringUtils.strEquals(this.appName, config.getAppName()) && StringUtils.strEquals(this.serCountry, config.getSerCountry()) && StringUtils.strEquals(this.regCountry, config.getRegCountry()) && StringUtils.strEquals(this.issueCountry, config.getIssueCountry()) && StringUtils.strEquals(this.userId, config.getuserId()) && StringUtils.strEquals(this.versionName, config.getVersionName()) && StringUtils.strEquals(this.countrySource, config.getCountrySource());
    }
}
