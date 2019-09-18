package com.huawei.wallet.sdk.business.bankcard.modle;

import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class IssuerInfoServerItem {
    private String appInfo;
    private String cityCode;
    private String contactNumber;
    private String creditCallCenterNumber;
    private String creditTcUrl;
    private String creditWebsite;
    private String debitCallCenterNumber;
    private String debitTcUrl;
    private String debitWebsite;
    private String description;
    private Integer groupType;
    private String issuerId;
    private int issuerType;
    private String logoUrl;
    private int mode;
    private String name;
    private String payType;
    private String reservedInfo;
    private int sn;
    private String subCardDescription;
    private String subCardTags;
    private int supportType;
    private long timeStamp;
    private String walletVersion;

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
    }

    public String getLogoUrl() {
        return this.logoUrl;
    }

    public void setLogoUrl(String logoUrl2) {
        this.logoUrl = logoUrl2;
    }

    public int getIssuerType() {
        return this.issuerType;
    }

    public void setIssuerType(int issuerType2) {
        this.issuerType = issuerType2;
    }

    public int getSupportType() {
        return this.supportType;
    }

    public void setSupportType(int supportType2) {
        this.supportType = supportType2;
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode2) {
        this.mode = mode2;
    }

    public String getWalletVersion() {
        return this.walletVersion;
    }

    public void setWalletVersion(String walletVersion2) {
        this.walletVersion = walletVersion2;
    }

    public String getContactNumber() {
        return this.contactNumber;
    }

    public void setContactNumber(String contactNumber2) {
        this.contactNumber = contactNumber2;
    }

    public String getDebitCallCenterNumber() {
        return this.debitCallCenterNumber;
    }

    public void setDebitCallCenterNumber(String debitCallCenterNumber2) {
        this.debitCallCenterNumber = debitCallCenterNumber2;
    }

    public String getCreditCallCenterNumber() {
        return this.creditCallCenterNumber;
    }

    public void setCreditCallCenterNumber(String creditCallCenterNumber2) {
        this.creditCallCenterNumber = creditCallCenterNumber2;
    }

    public String getDebitTcUrl() {
        return this.debitTcUrl;
    }

    public void setDebitTcUrl(String debitTcUrl2) {
        this.debitTcUrl = debitTcUrl2;
    }

    public String getCreditTcUrl() {
        return this.creditTcUrl;
    }

    public void setCreditTcUrl(String creditTcUrl2) {
        this.creditTcUrl = creditTcUrl2;
    }

    public String getDebitWebsite() {
        return this.debitWebsite;
    }

    public void setDebitWebsite(String debitWebsite2) {
        this.debitWebsite = debitWebsite2;
    }

    public String getCreditWebsite() {
        return this.creditWebsite;
    }

    public void setCreditWebsite(String creditWebsite2) {
        this.creditWebsite = creditWebsite2;
    }

    public String getAppInfo() {
        return this.appInfo;
    }

    public void setAppInfo(String appInfo2) {
        this.appInfo = appInfo2;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public String toString() {
        return "IssuerInfoServerItem{issuerId='" + this.issuerId + '\'' + ", name='" + this.name + '\'' + ", description='" + this.description + '\'' + ", logoUrl='***" + '\'' + ", issuerType=" + this.issuerType + ", supportType=" + this.supportType + ", mode=" + this.mode + ", walletVersion='" + this.walletVersion + '\'' + ", contactNumber='" + this.contactNumber + '\'' + ", debitCallCenterNumber='" + this.debitCallCenterNumber + '\'' + ", creditCallCenterNumber='" + this.creditCallCenterNumber + '\'' + ", debitTcUrl='***" + '\'' + ", creditTcUrl='***" + '\'' + ", debitWebsite='" + this.debitWebsite + '\'' + ", creditWebsite='" + this.creditWebsite + '\'' + ", appInfo='" + this.appInfo + '\'' + ", timeStamp=" + this.timeStamp + ", reservedInfo='" + this.reservedInfo + '\'' + ", cityCode='" + this.cityCode + '\'' + ", payType='" + this.payType + '\'' + '}';
    }

    public String getReservedInfo() {
        return this.reservedInfo;
    }

    public void setReservedInfo(String reservedInfo2) {
        this.reservedInfo = reservedInfo2;
    }

    public int getSn() {
        return this.sn;
    }

    public void setSn(int sn2) {
        this.sn = sn2;
    }

    public String getCityCode() {
        return this.cityCode;
    }

    public void setCityCode(String cityCode2) {
        this.cityCode = cityCode2;
    }

    public String getPayType() {
        return this.payType;
    }

    public void setPayType(String payType2) {
        this.payType = payType2;
    }

    public String getSubCardDescription() {
        return this.subCardDescription;
    }

    public void setSubCardDescription(String subCardDescription2) {
        this.subCardDescription = subCardDescription2;
    }

    public String getSubCardTags() {
        return this.subCardTags;
    }

    public void setSubCardTags(String subCardTags2) {
        this.subCardTags = subCardTags2;
    }

    public Integer getGroupType() {
        return this.groupType;
    }

    public void setGroupType(Integer groupType2) {
        this.groupType = groupType2;
    }

    public IssuerInfoServerItem() {
    }

    public IssuerInfoServerItem(JSONObject tempJsonItem) throws JSONException {
        if (tempJsonItem != null) {
            try {
                this.issuerId = JSONHelper.getStringValue(tempJsonItem, ServerAccessApplyAPDURequest.ReqKey.ISSUERID);
                this.name = JSONHelper.getStringValue(tempJsonItem, "name");
                this.description = JSONHelper.getStringValue(tempJsonItem, "description");
                this.logoUrl = JSONHelper.getStringValue(tempJsonItem, "logo");
                this.issuerType = JSONHelper.getIntValue(tempJsonItem, "issuerType");
                this.supportType = JSONHelper.getIntValue(tempJsonItem, "supportedProduct");
                this.mode = JSONHelper.getIntValue(tempJsonItem, "mode");
                this.walletVersion = JSONHelper.getStringValue(tempJsonItem, "walletVersion");
                this.contactNumber = JSONHelper.getStringValue(tempJsonItem, "contactNumber");
                this.debitCallCenterNumber = JSONHelper.getStringValue(tempJsonItem, "debitCallCenterNumber");
                this.creditCallCenterNumber = JSONHelper.getStringValue(tempJsonItem, "creditCallCenterNumber");
                this.debitTcUrl = JSONHelper.getStringValue(tempJsonItem, "debitTcUrl");
                this.creditTcUrl = JSONHelper.getStringValue(tempJsonItem, "creditTcUrl");
                this.debitWebsite = JSONHelper.getStringValue(tempJsonItem, "debitWebsite");
                this.creditWebsite = JSONHelper.getStringValue(tempJsonItem, "creditWebsite");
                this.timeStamp = JSONHelper.getLongValue(tempJsonItem, "timestamp");
                this.appInfo = JSONHelper.getStringValue(tempJsonItem, "appInfo");
                this.reservedInfo = JSONHelper.getStringValue(tempJsonItem, "reserved");
                this.sn = JSONHelper.getIntValue(tempJsonItem, "sn");
                this.cityCode = JSONHelper.getStringValue(tempJsonItem, "cityCode");
                this.payType = JSONHelper.getStringValue(tempJsonItem, "payType");
                this.subCardDescription = JSONHelper.getStringValue(tempJsonItem, "subCardDescription");
                this.subCardTags = JSONHelper.getStringValue(tempJsonItem, "subCardTags");
                this.groupType = Integer.valueOf(JSONHelper.getIntValue(tempJsonItem, "groupType"));
            } catch (JSONException e) {
                LogC.e("IssuerInfoQueryTask createIssuerInfoItem JSONException : " + e.getMessage(), true);
                throw e;
            }
        }
    }
}
