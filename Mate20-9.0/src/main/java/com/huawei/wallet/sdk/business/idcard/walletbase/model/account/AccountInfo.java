package com.huawei.wallet.sdk.business.idcard.walletbase.model.account;

import android.graphics.Bitmap;

public class AccountInfo {
    private String accessToken = null;
    private String deviceId = null;
    private String deviceType = null;
    private Bitmap headBitmap = null;
    private String languageCode;
    private String serviceCountryCode;
    private String serviceToken = null;
    private int siteId;
    private String userId = null;

    public void setLanguageCode(String languageCode2) {
        this.languageCode = languageCode2;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public String getServiceCountryCode() {
        return this.serviceCountryCode;
    }

    public void setServiceCountryCode(String serviceCountryCode2) {
        this.serviceCountryCode = serviceCountryCode2;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken2) {
        this.accessToken = accessToken2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
    }

    public String getServiceToken() {
        return this.serviceToken;
    }

    public void setServiceToken(String serviceToken2) {
        this.serviceToken = serviceToken2;
    }

    public int getSiteId() {
        return this.siteId;
    }

    public void setSiteId(int siteId2) {
        this.siteId = siteId2;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(String deviceType2) {
        this.deviceType = deviceType2;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public Bitmap getHeadBitmap() {
        return this.headBitmap;
    }

    public void setHeadBitmap(Bitmap bitmap) {
        this.headBitmap = bitmap;
    }
}
