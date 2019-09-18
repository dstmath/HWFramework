package com.unionpay.tsmservice.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

@Deprecated
public class AppDetail implements Parcelable {
    public static final Parcelable.Creator<AppDetail> CREATOR = new Parcelable.Creator<AppDetail>() {
        public final AppDetail createFromParcel(Parcel parcel) {
            return new AppDetail(parcel);
        }

        public final AppDetail[] newArray(int i) {
            return new AppDetail[i];
        }
    };
    private String mApkDownloadUrl = "";
    private String mApkIcon = "";
    private String mApkName = "";
    private String mApkPackageName = "";
    private String mApkSign = "";
    private String mAppApplyId;
    private String mAppDesc = "";
    private AppID mAppID;
    private String mAppIcon = "";
    private String mAppName = "";
    private String mAppProviderAgreement = "";
    private String mAppProviderLogo = "";
    private String mAppProviderName = "";
    private String mApplyMode = "";
    private String mCallCenterNumber = "";
    private String mCardType = "";
    private long mDownloadTimes = 0;
    private String mEmail = "";
    private String mIssuerName = "";
    private String mLastDigits = "";
    private String mMpan = "";
    private String mMpanId = "";
    private String mMpanStatus = "";
    private String mOpStatus = "";
    private String mPublishData = "";
    private String mPublishStatus = "";
    private String mQuota = "";
    private String mRechargeLowerLimit = "";
    private String mRechargeMode = "";
    private String mServicePhone = "";
    private AppStatus mStatus;
    private String mUpAgreement = "";
    private String mWebsite = "";

    public AppDetail() {
    }

    public AppDetail(Parcel parcel) {
        this.mAppID = (AppID) parcel.readParcelable(AppID.class.getClassLoader());
        this.mAppName = parcel.readString();
        this.mAppIcon = parcel.readString();
        this.mAppDesc = parcel.readString();
        this.mAppProviderLogo = parcel.readString();
        this.mAppProviderName = parcel.readString();
        this.mAppProviderAgreement = parcel.readString();
        this.mUpAgreement = parcel.readString();
        this.mApplyMode = parcel.readString();
        this.mServicePhone = parcel.readString();
        this.mDownloadTimes = parcel.readLong();
        this.mPublishData = parcel.readString();
        this.mPublishStatus = parcel.readString();
        this.mRechargeMode = parcel.readString();
        this.mRechargeLowerLimit = parcel.readString();
        this.mAppApplyId = parcel.readString();
        this.mStatus = (AppStatus) parcel.readParcelable(AppStatus.class.getClassLoader());
        this.mMpanId = parcel.readString();
        this.mMpan = parcel.readString();
        this.mCardType = parcel.readString();
        this.mIssuerName = parcel.readString();
        this.mLastDigits = parcel.readString();
        this.mMpanStatus = parcel.readString();
        this.mOpStatus = parcel.readString();
        this.mQuota = parcel.readString();
        this.mCallCenterNumber = parcel.readString();
        this.mEmail = parcel.readString();
        this.mWebsite = parcel.readString();
        this.mApkIcon = parcel.readString();
        this.mApkName = parcel.readString();
        this.mApkPackageName = parcel.readString();
        this.mApkDownloadUrl = parcel.readString();
        this.mApkSign = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getApkDownloadUrl() {
        return this.mApkDownloadUrl;
    }

    public String getApkIcon() {
        return this.mApkIcon;
    }

    public String getApkName() {
        return this.mApkName;
    }

    public String getApkPackageName() {
        return this.mApkPackageName;
    }

    public String getApkSign() {
        return this.mApkSign;
    }

    public String getAppApplyId() {
        return this.mAppApplyId;
    }

    public String getAppDesc() {
        return this.mAppDesc;
    }

    public AppID getAppID() {
        return this.mAppID;
    }

    public String getAppIcon() {
        return this.mAppIcon;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public String getAppProviderAgreement() {
        return this.mAppProviderAgreement;
    }

    public String getAppProviderLogo() {
        return this.mAppProviderLogo;
    }

    public String getAppProviderName() {
        return this.mAppProviderName;
    }

    public String getApplyMode() {
        return this.mApplyMode;
    }

    public String getCallCenterNumber() {
        return this.mCallCenterNumber;
    }

    public String getCardType() {
        return this.mCardType;
    }

    public long getDownloadTimes() {
        return this.mDownloadTimes;
    }

    public String getEmail() {
        return this.mEmail;
    }

    public String getIssuerName() {
        return this.mIssuerName;
    }

    public String getLastDigits() {
        return this.mLastDigits;
    }

    public String getMpan() {
        return this.mMpan;
    }

    public String getMpanId() {
        return this.mMpanId;
    }

    public String getMpanStatus() {
        return this.mMpanStatus;
    }

    public String getOpStatus() {
        return this.mOpStatus;
    }

    public String getPublishData() {
        return this.mPublishData;
    }

    public String getPublishStatus() {
        return this.mPublishStatus;
    }

    public String getQuota() {
        return this.mQuota;
    }

    public String getRechargeLowerLimit() {
        return this.mRechargeLowerLimit;
    }

    public String getRechargeMode() {
        return this.mRechargeMode;
    }

    public String getServicePhone() {
        return this.mServicePhone;
    }

    public AppStatus getStatus() {
        return this.mStatus;
    }

    public String getUpAgreement() {
        return this.mUpAgreement;
    }

    public String getWebsite() {
        return this.mWebsite;
    }

    public void setApkDownloadUrl(String str) {
        this.mApkDownloadUrl = str;
    }

    public void setApkIcon(String str) {
        this.mApkIcon = str;
    }

    public void setApkName(String str) {
        this.mApkName = str;
    }

    public void setApkPackageName(String str) {
        this.mApkPackageName = str;
    }

    public void setApkSign(String str) {
        this.mApkSign = str;
    }

    public void setAppApplyId(String str) {
        this.mAppApplyId = str;
    }

    public void setAppDesc(String str) {
        this.mAppDesc = str;
    }

    public void setAppID(AppID appID) {
        this.mAppID = appID;
    }

    public void setAppIcon(String str) {
        this.mAppIcon = str;
    }

    public void setAppName(String str) {
        this.mAppName = str;
    }

    public void setAppProviderAgreement(String str) {
        this.mAppProviderAgreement = str;
    }

    public void setAppProviderLogo(String str) {
        this.mAppProviderLogo = str;
    }

    public void setAppProviderName(String str) {
        this.mAppProviderName = str;
    }

    public void setApplyMode(String str) {
        this.mApplyMode = str;
    }

    public void setCallCenterNumber(String str) {
        this.mCallCenterNumber = str;
    }

    public void setCardType(String str) {
        this.mCardType = str;
    }

    public void setDownloadTimes(long j) {
        this.mDownloadTimes = j;
    }

    public void setEmail(String str) {
        this.mEmail = str;
    }

    public void setIssuerName(String str) {
        this.mIssuerName = str;
    }

    public void setLastDigits(String str) {
        this.mLastDigits = str;
    }

    public void setMpan(String str) {
        this.mMpan = str;
    }

    public void setMpanId(String str) {
        this.mMpanId = str;
    }

    public void setMpanStatus(String str) {
        this.mMpanStatus = str;
    }

    public void setOpStatus(String str) {
        this.mOpStatus = str;
    }

    public void setPublishData(String str) {
        this.mPublishData = str;
    }

    public void setPublishStatus(String str) {
        this.mPublishStatus = str;
    }

    public void setQuota(String str) {
        this.mQuota = str;
    }

    public void setRechargeLowerLimit(String str) {
        this.mRechargeLowerLimit = str;
    }

    public void setRechargeMode(String str) {
        this.mRechargeMode = str;
    }

    public void setServicePhone(String str) {
        this.mServicePhone = str;
    }

    public void setStatus(AppStatus appStatus) {
        this.mStatus = appStatus;
    }

    public void setUpAgreement(String str) {
        this.mUpAgreement = str;
    }

    public void setWebsite(String str) {
        this.mWebsite = str;
    }

    public String toString() {
        return "AppDetail [mAppID=" + this.mAppID + ", mAppName=" + this.mAppName + ", mAppIcon=" + this.mAppIcon + ", mAppDesc=" + this.mAppDesc + ", mAppProviderLogo=" + this.mAppProviderLogo + ", mAppProviderName=" + this.mAppProviderName + ", mAppProviderAgreement=" + this.mAppProviderAgreement + ", mUpAgreement=" + this.mUpAgreement + ", mApplyMode=" + this.mApplyMode + ", mServicePhone=" + this.mServicePhone + ", mDownloadTimes=" + this.mDownloadTimes + ", mPublishData=" + this.mPublishData + ", mPublishStatus=" + this.mPublishStatus + ", mRechargeMode=" + this.mRechargeMode + ", mRechargeLowerLimit=" + this.mRechargeLowerLimit + ", mStatus=" + this.mStatus + ", mAppApplyId=" + this.mAppApplyId + ", mMpanId=" + this.mMpanId + ", mMpan=" + this.mMpan + ", mCardType=" + this.mCardType + ", mIssuerName=" + this.mIssuerName + ", mLastDigits=" + this.mLastDigits + ", mMpanStatus=" + this.mMpanStatus + ", mOpStatus=" + this.mOpStatus + ", mQuota=" + this.mQuota + ", mCallCenterNumber=" + this.mCallCenterNumber + ", mEmail=" + this.mEmail + ", mWebsite=" + this.mWebsite + ", mApkIcon=" + this.mApkIcon + ", mApkName=" + this.mApkName + ", mApkPackageName=" + this.mApkPackageName + ", mApkDownloadUrl=" + this.mApkDownloadUrl + ", mApkSign=" + this.mApkSign + "]";
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mAppID, i);
        parcel.writeString(this.mAppName);
        parcel.writeString(this.mAppIcon);
        parcel.writeString(this.mAppDesc);
        parcel.writeString(this.mAppProviderLogo);
        parcel.writeString(this.mAppProviderName);
        parcel.writeString(this.mAppProviderAgreement);
        parcel.writeString(this.mUpAgreement);
        parcel.writeString(this.mApplyMode);
        parcel.writeString(this.mServicePhone);
        parcel.writeLong(this.mDownloadTimes);
        parcel.writeString(this.mPublishData);
        parcel.writeString(this.mPublishStatus);
        parcel.writeString(this.mRechargeMode);
        parcel.writeString(this.mRechargeLowerLimit);
        parcel.writeString(this.mAppApplyId);
        parcel.writeParcelable(this.mStatus, i);
        parcel.writeString(this.mMpanId);
        parcel.writeString(this.mMpan);
        parcel.writeString(this.mCardType);
        parcel.writeString(this.mIssuerName);
        parcel.writeString(this.mLastDigits);
        parcel.writeString(this.mMpanStatus);
        parcel.writeString(this.mOpStatus);
        parcel.writeString(this.mQuota);
        parcel.writeString(this.mCallCenterNumber);
        parcel.writeString(this.mEmail);
        parcel.writeString(this.mWebsite);
        parcel.writeString(this.mApkIcon);
        parcel.writeString(this.mApkName);
        parcel.writeString(this.mApkPackageName);
        parcel.writeString(this.mApkDownloadUrl);
        parcel.writeString(this.mApkSign);
    }
}
