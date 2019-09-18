package com.huawei.wallet.sdk.business.bankcard.modle;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.business.buscard.base.util.MoneyUtil;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class IssuerInfoItem {
    private String aid;
    private String aid2;
    private String appInfo;
    private String bankAgreementTitle;
    private String bankAgreementUrl;
    private int buildingStandard = 0;
    private String busAgreementTitle;
    private String busAgreementUrl;
    private int cancelRestoreConfig;
    private String cityCode;
    private String contactDay;
    private String contactNumber;
    private String creditCallCenterNumber;
    private String creditTcUrl;
    private String creditTermsTitle;
    private String creditTermsUrl;
    private String creditWebsite;
    private String debitCallCenterNumber;
    private String debitTcUrl;
    private String debitTermsTitle;
    private String debitTermsUrl;
    private String debitWebsite;
    private int deleteMode = 1;
    private String description;
    public boolean extConditionCheck = false;
    private Integer groupType;
    private boolean isSupportAutoRefundCheck = false;
    private int isSupportDelete = 0;
    private boolean isSupportManualRefund = false;
    private boolean issuerCardSupport = true;
    private String issuerId;
    private int issuerType;
    private String logoUrl;
    public String mMinSupportCancelRestoreWalletVersion;
    public String mMinSupportCloudTransferWalletVersion;
    public String mMinSupportCloudUpgradeWalletVersion;
    private String mMinSupportDeleteWalletVersion;
    public String mMinSupportPointWalletVersion;
    public String mPointSubType;
    private String mUpgradeIllustrateUrl;
    private String mUpgradeNewIssuerId;
    private String mUpgradeNoticeTitle;
    private String mUpgradeNoticeUrl;
    public int minRechargeAmount;
    private int mode;
    private String name;
    private boolean needRechargeScript = false;
    private boolean needUpdateParam = false;
    private String payTypeString;
    private String privacyPolicyUrl;
    private String productId;
    private String pushMode;
    private boolean rechargeSupport = true;
    private String refundDay = null;
    private boolean refundIssueFee = false;
    private int refundMaxDays = 5;
    private String removeAllowedTimes;
    private String reservedInfo;
    public String restoreFailTime;
    private boolean showCityName = true;
    private int sn;
    private String subCardDescription;
    private String subCardTags;
    private int supportType;
    private String supportedDevices;
    private String thirdDeleteGuideUrl;
    private String thirdH5Url;
    private long timeStamp;
    private String transferUrl;
    private String uiMode;
    private int walletVersion;
    private boolean wxLedger = false;
    private boolean wxPaySupport = false;

    public IssuerInfoItem() {
    }

    public IssuerInfoItem(IssuerInfoServerItem item) {
        this.issuerId = item.getIssuerId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.logoUrl = item.getLogoUrl();
        this.issuerType = item.getIssuerType();
        this.supportType = item.getSupportType();
        this.mode = item.getMode();
        try {
            this.walletVersion = Integer.parseInt(item.getWalletVersion());
        } catch (NumberFormatException e) {
            this.walletVersion = 0;
        }
        this.contactNumber = item.getContactNumber();
        this.debitCallCenterNumber = item.getDebitCallCenterNumber();
        this.creditCallCenterNumber = item.getCreditCallCenterNumber();
        this.debitTcUrl = item.getDebitTcUrl();
        this.creditTcUrl = item.getCreditTcUrl();
        this.debitWebsite = item.getDebitWebsite();
        this.creditWebsite = item.getCreditWebsite();
        this.timeStamp = item.getTimeStamp();
        this.appInfo = item.getAppInfo();
        this.reservedInfo = item.getReservedInfo();
        this.sn = item.getSn();
        this.cityCode = item.getCityCode();
        this.payTypeString = item.getPayType();
        this.subCardDescription = item.getSubCardDescription();
        this.subCardTags = item.getSubCardTags();
        this.groupType = item.getGroupType();
        parseReservedJson();
    }

    public boolean isSupportAutoRefundCheck() {
        return this.isSupportAutoRefundCheck;
    }

    public void setSupportAutoRefundCheck(boolean supportAutoRefundCheck) {
        this.isSupportAutoRefundCheck = supportAutoRefundCheck;
    }

    public List<String> getPayType() {
        if (StringUtil.isEmpty(this.payTypeString, true)) {
            return null;
        }
        return Arrays.asList(this.payTypeString.split(SNBConstant.FILTER));
    }

    public final void parseReservedJson() {
        try {
            if (!StringUtil.isEmpty(this.reservedInfo, true)) {
                Object json = new JSONTokener(this.reservedInfo).nextValue();
                if (json instanceof JSONObject) {
                    JSONObject jo = (JSONObject) json;
                    parseBankInfoFromReservedInfo(jo);
                    parsePointConfigCheck(jo);
                    if (jo.has("aid")) {
                        this.aid = jo.getString("aid");
                    }
                    if (jo.has("productId")) {
                        this.productId = jo.getString("productId");
                    }
                    if (jo.has("bus_support")) {
                        this.supportedDevices = jo.getString("bus_support");
                    }
                    if (jo.has("bus_agreement_url")) {
                        this.busAgreementUrl = jo.getString("bus_agreement_url");
                    }
                    if (jo.has("min_recharge_amount")) {
                        int amount = MoneyUtil.convertYuanToFen(jo.getString("min_recharge_amount"));
                        this.minRechargeAmount = amount < 0 ? 0 : amount;
                    }
                    if (jo.has("is_support_delete")) {
                        this.isSupportDelete = jo.getInt("is_support_delete");
                    }
                    if (jo.has("is_building_standard")) {
                        this.buildingStandard = jo.getInt("is_building_standard");
                    }
                    parseReserved(jo);
                    parseAutoRefundOrderCheck(jo);
                    parseRemoveAllowedTimes(jo);
                    parseRestoreCancelCheck(jo);
                    parseWXinfoFromReserved(jo);
                    parseUIModeFromReserved(jo);
                    parseOpenCard(jo);
                    parseAgreeTitle(jo);
                    parseDeleteConfig(jo);
                }
            }
        } catch (JSONException e) {
            LogX.e("parseReservedJson error ");
        }
    }

    private void parseRestoreCancelCheck(JSONObject jo) throws JSONException {
        if (jo.has("cancel_restore_config")) {
            parseCancelRestoreConfig(jo.getJSONObject("cancel_restore_config"));
        }
    }

    private void parsePointConfigCheck(JSONObject jo) throws JSONException {
        if (jo.has("pointConfig")) {
            parsePointConfig(jo.getJSONObject("pointConfig"));
        }
    }

    private void parseAutoRefundOrderCheck(JSONObject jo) throws JSONException {
        if (jo.has("isSupportAutoRefundCheck")) {
            this.isSupportAutoRefundCheck = jo.getBoolean("isSupportAutoRefundCheck");
        }
    }

    private void parseRemoveAllowedTimes(JSONObject jo) throws JSONException {
        if (jo.has("remove_allowed_times")) {
            this.removeAllowedTimes = String.valueOf(jo.getInt("remove_allowed_times"));
            LogX.i("IssuerInfoItem parseRemoveAllowedTimes, card removeAllowedTimes:" + this.removeAllowedTimes);
        }
    }

    private void parseAgreeTitle(JSONObject jo) throws JSONException {
        if (jo.has("bus_agreement_title")) {
            this.busAgreementTitle = jo.getString("bus_agreement_title");
        }
    }

    private void parseReserved(JSONObject jo) throws JSONException {
        if (jo.has("transfer_agreement_url")) {
            this.transferUrl = jo.getString("transfer_agreement_url");
        }
        if (jo.has("recharge_support")) {
            this.rechargeSupport = jo.getBoolean("recharge_support");
        }
        if (jo.has("aid2")) {
            this.aid2 = jo.getString("aid2");
        }
        if (jo.has("push_mode")) {
            this.pushMode = jo.getString("push_mode");
        }
        if (jo.has("issuer_support")) {
            this.issuerCardSupport = jo.getBoolean("issuer_support");
        }
        if (jo.has("updateParam")) {
            this.needUpdateParam = jo.getBoolean("updateParam");
        }
        if (jo.has("refund_day")) {
            this.refundDay = jo.getString("refund_day");
        }
        if (jo.has("contact_day")) {
            this.contactDay = jo.getString("contact_day");
        }
        if (jo.has("is_support_client_delete")) {
            this.isSupportDelete = jo.getInt("is_support_client_delete");
        }
        if (!TextUtils.isEmpty(this.issuerId)) {
            if (jo.has("is_support_client_delete_" + this.issuerId)) {
                this.isSupportDelete = jo.getInt("is_support_client_delete_" + this.issuerId);
            }
        }
        if (jo.has("questions")) {
            this.debitWebsite = jo.getString("questions");
        }
        if (jo.has("backupConfig")) {
            parseBackUpConfig(jo.getJSONObject("backupConfig"));
        }
        if (jo.has("upgradeConfig")) {
            parseUpgradeConfig(jo.getJSONObject("upgradeConfig"));
        }
        if (jo.has("refund_issue_fee")) {
            this.refundIssueFee = jo.getBoolean("refund_issue_fee");
        }
        if (jo.has("refund_max_days")) {
            this.refundMaxDays = jo.getInt("refund_max_days");
        }
    }

    private void parseDeleteConfig(JSONObject reservedInfoJo) throws JSONException {
        if (reservedInfoJo.has("deleteConfig")) {
            JSONObject deleteConfig = reservedInfoJo.getJSONObject("deleteConfig");
            if (deleteConfig != null && deleteConfig.length() > 0) {
                if (deleteConfig.has("walletSdkVersion")) {
                    this.mMinSupportDeleteWalletVersion = deleteConfig.getString("walletSdkVersion");
                }
                if (deleteConfig.has("deleteMode")) {
                    this.deleteMode = deleteConfig.getInt("deleteMode");
                }
                if (deleteConfig.has("guide_url")) {
                    this.thirdDeleteGuideUrl = deleteConfig.getString("guide_url");
                }
            }
        }
    }

    private void parseBackUpConfig(JSONObject jo) throws JSONException {
        if (jo.has("walletSdkVersion")) {
            this.mMinSupportCloudTransferWalletVersion = jo.getString("walletSdkVersion");
        }
        if (jo.has("extConditionCheck")) {
            this.extConditionCheck = jo.getBoolean("extConditionCheck");
        }
    }

    private void parseCancelRestoreConfig(JSONObject jo) throws JSONException {
        if (jo.has("walletVersion")) {
            this.mMinSupportCancelRestoreWalletVersion = jo.getString("walletVersion");
        }
        if (jo.has("restore_fail_time")) {
            this.restoreFailTime = jo.getString("restore_fail_time");
        }
    }

    private void parseUpgradeConfig(JSONObject jo) throws JSONException {
        if (jo.has("walletVersion")) {
            this.mMinSupportCloudUpgradeWalletVersion = jo.getString("walletVersion");
        }
        if (jo.has("newIssuerId")) {
            this.mUpgradeNewIssuerId = jo.getString("newIssuerId");
        }
        if (jo.has("upgrade_Illustrate_url")) {
            this.mUpgradeIllustrateUrl = jo.getString("upgrade_Illustrate_url");
        }
        if (jo.has("upgrade_notice")) {
            this.mUpgradeNoticeTitle = jo.getString("upgrade_notice");
        }
        if (jo.has("upgrade_notice_url")) {
            this.mUpgradeNoticeUrl = jo.getString("upgrade_notice_url");
        }
    }

    private void parsePointConfig(JSONObject jo) throws JSONException {
        if (jo.has("walletVersion")) {
            this.mMinSupportPointWalletVersion = jo.getString("walletVersion");
        }
        if (jo.has("pointSubType")) {
            this.mPointSubType = jo.getString("pointSubType");
        }
    }

    public String getUpgradeNewIssuerId() {
        return this.mUpgradeNewIssuerId;
    }

    public String getUpgradeIllustrateUrl() {
        return this.mUpgradeIllustrateUrl;
    }

    public String getUpgradeNoticeTitle() {
        return this.mUpgradeNoticeTitle;
    }

    public String getUpgradeNoticeUrl() {
        return this.mUpgradeNoticeUrl;
    }

    private void parseWXinfoFromReserved(JSONObject jo) throws JSONException {
        if (jo.has("wxpay_support")) {
            this.wxPaySupport = jo.getBoolean("wxpay_support");
        }
        if (jo.has("wxledger_support")) {
            this.wxLedger = jo.getBoolean("wxledger_support");
        }
    }

    private void parseUIModeFromReserved(JSONObject jo) throws JSONException {
        if (jo.has("uiMode")) {
            this.uiMode = jo.getString("uiMode");
        }
        if (jo.has("third_h5Url")) {
            this.thirdH5Url = jo.getString("third_h5Url");
        }
        if (jo.has("rechargeScript")) {
            this.needRechargeScript = jo.getBoolean("rechargeScript");
        }
        if (jo.has("client_manual_refund_control_vfc")) {
            this.isSupportManualRefund = jo.getBoolean("client_manual_refund_control_vfc");
        }
        if (!TextUtils.isEmpty(this.issuerId)) {
            if (jo.has("client_manual_apply_refund_control_" + this.issuerId)) {
                this.isSupportManualRefund = jo.getBoolean("client_manual_apply_refund_control_" + this.issuerId);
            }
        }
        if (!TextUtils.isEmpty(this.issuerId)) {
            if (jo.has("apply_refund_control_" + this.issuerId)) {
                this.isSupportManualRefund = jo.getBoolean("apply_refund_control_" + this.issuerId);
            }
        }
    }

    private void parseOpenCard(JSONObject jo) throws JSONException {
        if (jo.has("show_city_name")) {
            this.showCityName = jo.getBoolean("show_city_name");
            LogX.d("showCityName is true or false:" + this.showCityName);
        }
    }

    private void parseBankInfoFromReservedInfo(JSONObject reservedInfoJo) {
        if (reservedInfoJo == null) {
            LogX.e("parseBankInfo : reservedInfoJo = null!");
            return;
        }
        try {
            if (reservedInfoJo.has("credit_terms_title")) {
                this.creditTermsTitle = reservedInfoJo.getString("credit_terms_title");
            }
            if (reservedInfoJo.has("credit_terms_url")) {
                this.creditTermsUrl = reservedInfoJo.getString("credit_terms_url");
            }
            if (reservedInfoJo.has("debit_terms_title")) {
                this.debitTermsTitle = reservedInfoJo.getString("debit_terms_title");
            }
            if (reservedInfoJo.has("debit_terms_url")) {
                this.debitTermsUrl = reservedInfoJo.getString("debit_terms_url");
            }
            if (reservedInfoJo.has("bank_agreement_title")) {
                this.bankAgreementTitle = reservedInfoJo.getString("bank_agreement_title");
            }
            if (reservedInfoJo.has("bank_agreement_url")) {
                this.bankAgreementUrl = reservedInfoJo.getString("bank_agreement_url");
            }
        } catch (JSONException e) {
            LogX.e("parseBankInfoFromReservedInfo exception");
        }
    }

    public String getContactNumber() {
        return this.contactNumber;
    }

    public void setContactNumber(String contactNumber1) {
        this.contactNumber = contactNumber1;
    }

    public String getDebitCallCenterNumber() {
        return this.debitCallCenterNumber;
    }

    public void setDebitCallCenterNumber(String debitCallCenterNumber1) {
        this.debitCallCenterNumber = debitCallCenterNumber1;
    }

    public String getCreditCallCenterNumber() {
        return this.creditCallCenterNumber;
    }

    public void setCreditCallCenterNumber(String creditCallCenterNumber1) {
        this.creditCallCenterNumber = creditCallCenterNumber1;
    }

    public String getDebitTcUrl() {
        return this.debitTcUrl;
    }

    public void setDebitTcUrl(String debitTcUrl1) {
        this.debitTcUrl = debitTcUrl1;
    }

    public String getCreditTcUrl() {
        return this.creditTcUrl;
    }

    public void setCreditTcUrl(String creditTcUrl1) {
        this.creditTcUrl = creditTcUrl1;
    }

    public String getAppInfo() {
        return this.appInfo;
    }

    public void setAppInfo(String appInfo2) {
        this.appInfo = appInfo2;
    }

    public String getCreditTermsTitle() {
        return this.creditTermsTitle;
    }

    public void setCreditTermsTitle(String creditTermsTitle2) {
        this.creditTermsTitle = creditTermsTitle2;
    }

    public String getCreditTermsUrl() {
        return this.creditTermsUrl;
    }

    public void setCreditTermsUrl(String creditTermsUrl2) {
        this.creditTermsUrl = creditTermsUrl2;
    }

    public String getDebitTermsTitle() {
        return this.debitTermsTitle;
    }

    public void setDebitTermsTitle(String debitTermsTitle2) {
        this.debitTermsTitle = debitTermsTitle2;
    }

    public String getDebitTermsUrl() {
        return this.debitTermsUrl;
    }

    public void setDebitTermsUrl(String debitTermsUrl2) {
        this.debitTermsUrl = debitTermsUrl2;
    }

    public String getCreditWebsite() {
        return this.creditWebsite;
    }

    public void setCreditWebsite(String creditWebsite2) {
        this.creditWebsite = creditWebsite2;
    }

    public String getBankAgreementTitle() {
        return this.bankAgreementTitle;
    }

    public void setBankAgreementTitle(String bankAgreementTitle2) {
        this.bankAgreementTitle = bankAgreementTitle2;
    }

    public String getPrivacyPolicyUrl() {
        return this.privacyPolicyUrl;
    }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl2) {
        this.privacyPolicyUrl = privacyPolicyUrl2;
    }

    public String getBankAgreementUrl() {
        return this.bankAgreementUrl;
    }

    public void setBankAgreementUrl(String bankAgreementUrl2) {
        this.bankAgreementUrl = bankAgreementUrl2;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid3) {
        this.aid = aid3;
    }

    public String getAid2() {
        return this.aid2;
    }

    public void setAid2(String aid22) {
        this.aid2 = aid22;
    }

    public String getBusAgreementUrl() {
        return this.busAgreementUrl;
    }

    public void setBusAgreementUrl(String busAgreementUrl2) {
        this.busAgreementUrl = busAgreementUrl2;
    }

    public String getBusAgreementTitle() {
        return this.busAgreementTitle;
    }

    public void setBusAgreementTitle(String busAgreementTitle2) {
        this.busAgreementTitle = busAgreementTitle2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId1) {
        this.issuerId = issuerId1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name1) {
        this.name = name1;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description1) {
        this.description = description1;
    }

    public String getLogoUrl() {
        return this.logoUrl;
    }

    public void setLogoUrl(String logoUrl1) {
        this.logoUrl = logoUrl1;
    }

    public int getIssuerType() {
        return this.issuerType;
    }

    public void setIssuerType(int issuerType1) {
        this.issuerType = issuerType1;
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode2) {
        this.mode = mode2;
    }

    public String getReservedInfo() {
        return this.reservedInfo;
    }

    public void setReservedInfo(String reservedInfo2) {
        this.reservedInfo = reservedInfo2;
    }

    public String getDebitWebsite() {
        return this.debitWebsite;
    }

    public void setDebitWebsite(String debitWebsite2) {
        this.debitWebsite = debitWebsite2;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId2) {
        this.productId = productId2;
    }

    public String getSupportedDevices() {
        return this.supportedDevices;
    }

    public void setSupportedDevices(String supportedDevices2) {
        this.supportedDevices = supportedDevices2;
    }

    public int getSn() {
        return this.sn;
    }

    public void setSn(int sn2) {
        this.sn = sn2;
    }

    public int getMinRechargeAmount() {
        return this.minRechargeAmount;
    }

    public void setMinRechargeAmount(int minRechargeAmount2) {
        this.minRechargeAmount = minRechargeAmount2;
    }

    public int getIsSupportDelete() {
        return this.isSupportDelete;
    }

    public void setIsSupportDelete(int isSupportDelete2) {
        this.isSupportDelete = isSupportDelete2;
    }

    public int getSupportType() {
        return this.supportType;
    }

    public void setSupportType(int supportType2) {
        this.supportType = supportType2;
    }

    public int getWalletVersion() {
        return this.walletVersion;
    }

    public void setWalletVersion(int walletVersion2) {
        this.walletVersion = walletVersion2;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public int getBuildingStandard() {
        return this.buildingStandard;
    }

    public void setBuildingStandard(int buildingStandard2) {
        this.buildingStandard = buildingStandard2;
    }

    public String getTransferUrl() {
        return this.transferUrl;
    }

    public void setTransferUrl(String transferUrl2) {
        this.transferUrl = transferUrl2;
    }

    public boolean getRechargeSupport() {
        return this.rechargeSupport;
    }

    public void setRechargeSupport(boolean rechargeSupport2) {
        this.rechargeSupport = rechargeSupport2;
    }

    public boolean getNeedUpdateParam() {
        return this.needUpdateParam;
    }

    public void setNeedUpdateParam(boolean needUpdateParam2) {
        this.needUpdateParam = needUpdateParam2;
    }

    public boolean getIssuerCardSupport() {
        return this.issuerCardSupport;
    }

    public void setIssuerCardSupport(boolean issuerCardSupport2) {
        this.issuerCardSupport = issuerCardSupport2;
    }

    public String getRefundDay() {
        return this.refundDay;
    }

    public void setRefundDay(String refundDay2) {
        this.refundDay = refundDay2;
    }

    public String getUIMode() {
        return this.uiMode;
    }

    public void setUiMode(String uiMode2) {
        this.uiMode = uiMode2;
    }

    public String getThirdH5Url() {
        return this.thirdH5Url;
    }

    public void setThirdH5Url(String thirdH5Url2) {
        this.thirdH5Url = thirdH5Url2;
    }

    public boolean isWxPaySupport() {
        return this.wxPaySupport;
    }

    public void setWxPaySupport(boolean wxPaySupport2) {
        this.wxPaySupport = wxPaySupport2;
    }

    public boolean isNeedRechargeScript() {
        return this.needRechargeScript;
    }

    public boolean isSupportManualRefund() {
        return this.isSupportManualRefund;
    }

    public boolean isWxLedger() {
        return this.wxLedger;
    }

    public void setWxLedger(boolean wxLedger2) {
        this.wxLedger = wxLedger2;
    }

    public String getCityCode() {
        return this.cityCode;
    }

    public void setCityCode(String cityCode2) {
        this.cityCode = cityCode2;
    }

    public String getPayTypeString() {
        return this.payTypeString;
    }

    public void setPayTypeString(String payTypeString2) {
        this.payTypeString = payTypeString2;
    }

    public String getmMinSupportCancelRestoreWalletVersion() {
        return this.mMinSupportCancelRestoreWalletVersion;
    }

    public void setmMinSupportCancelRestoreWalletVersion(String mMinSupportCancelRestoreWalletVersion2) {
        this.mMinSupportCancelRestoreWalletVersion = mMinSupportCancelRestoreWalletVersion2;
    }

    public String getRestoreFailTime() {
        return this.restoreFailTime;
    }

    public void setRestoreFailTime(String restoreFailTime2) {
        this.restoreFailTime = restoreFailTime2;
    }

    public String getMinSupportPointWalletVersion() {
        return this.mMinSupportPointWalletVersion;
    }

    public void setMinSupportPointWalletVersion(String minSupportPointWalletVersion) {
        this.mMinSupportPointWalletVersion = minSupportPointWalletVersion;
    }

    public String getPointSubType() {
        return this.mPointSubType;
    }

    public void setPointSubType(String pointSubType) {
        this.mPointSubType = pointSubType;
    }

    public String getContactDay() {
        return this.contactDay;
    }

    public void setContactDay(String contactDay2) {
        this.contactDay = contactDay2;
    }

    public boolean isShowCityName() {
        return this.showCityName;
    }

    public String getSubCardDescription() {
        return this.subCardDescription;
    }

    public void setSubCardDescription(String subCardDes) {
        this.subCardDescription = subCardDes;
    }

    public String getSubCardTags() {
        return this.subCardTags;
    }

    public void setSubCardTags(String subCardTag) {
        this.subCardTags = subCardTag;
    }

    public Integer getGroupType() {
        return this.groupType;
    }

    public void setGroupType(Integer groupTypes) {
        this.groupType = groupTypes;
    }

    public boolean isRefundIssueFee() {
        return this.refundIssueFee;
    }

    public void setRefundIssueFee(boolean refundIssueFee2) {
        this.refundIssueFee = refundIssueFee2;
    }

    public int getRefundMaxDays() {
        return this.refundMaxDays;
    }

    public void setRefundMaxDays(int refundMaxDays2) {
        this.refundMaxDays = refundMaxDays2;
    }

    public boolean isSupportSyncPush(Context context) {
        int versionCode = PackageUtil.getVersionCode(context);
        boolean isPush = false;
        try {
            if (TextUtils.isEmpty(this.pushMode)) {
                return false;
            }
            double minVersion = Double.parseDouble(this.pushMode);
            if (minVersion > 0.0d && ((double) versionCode) >= minVersion) {
                isPush = true;
            }
            return isPush;
        } catch (NumberFormatException e) {
            LogX.e("push mode parse error", false);
            return false;
        }
    }

    public int getDeleteMode() {
        return this.deleteMode;
    }

    public String getMinSupportDeleteWalletVersion() {
        return this.mMinSupportDeleteWalletVersion;
    }

    public String getThirdDeleteGuideUrl() {
        return this.thirdDeleteGuideUrl;
    }

    public String getRemoveAllowedTimes() {
        return this.removeAllowedTimes;
    }

    public void setRemoveAllowedTimes(String removeAllowedTimes2) {
        this.removeAllowedTimes = removeAllowedTimes2;
    }
}
