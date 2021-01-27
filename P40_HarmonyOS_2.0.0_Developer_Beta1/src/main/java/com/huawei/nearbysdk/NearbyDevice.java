package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.NearbyConfig;
import com.huawei.nearbysdk.util.Util;
import java.io.Serializable;

public final class NearbyDevice implements Parcelable, Serializable {
    public static final Parcelable.Creator<NearbyDevice> CREATOR = new Parcelable.Creator<NearbyDevice>() {
        /* class com.huawei.nearbysdk.NearbyDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearbyDevice createFromParcel(Parcel source) {
            String summary = source.readString();
            String bluetoothMac = source.readString();
            String huaweiIdName = source.readString();
            String btName = source.readString();
            int businessId = source.readInt();
            NearbyConfig.BusinessTypeEnum businessType = NearbySDKUtils.getEnumFromInt(source.readInt());
            boolean availability = source.readInt() == 1;
            String apMac = source.readString();
            boolean sameHwAccount = source.readInt() == 1;
            String wifiSsid = source.readString();
            String wifiPwd = source.readString();
            int wifiBand = source.readInt();
            String remoteIp = source.readString();
            String localIp = source.readString();
            int wifiPort = source.readInt();
            boolean isNeedClose = source.readInt() == 1;
            int manufacturer = source.readInt();
            int devType = source.readInt();
            int vendorDeviceType = source.readInt();
            String productID = source.readString();
            String devID = source.readString();
            boolean updatePwd = source.readInt() == 1;
            return new Builder().withSummary(summary).withBrMac(bluetoothMac).withHuaWeiIdName(huaweiIdName).withBtName(btName).withBusinessId(businessId).withBusinessType(businessType).withIsAvailability(availability).withApMac(apMac).withIsSameHwAccount(sameHwAccount).withWifiSsid(wifiSsid).withWifiPwd(wifiPwd).withWifiBand(wifiBand).withRemoteIp(remoteIp).withLocalIp(localIp).withWifiPort(wifiPort).withIsNeedClose(isNeedClose).withDeviceType(devType).withManufacturer(manufacturer).withVendorDeviceType(vendorDeviceType).withProductId(productID).withDeviceId(devID).withIsNeedUpdatePwd(updatePwd).withPwdVer(source.readInt()).withWifiFreq(source.readString()).withBleMac(source.readString()).withAccountId(source.readString()).withNearbyVersion(source.readInt()).build();
        }

        @Override // android.os.Parcelable.Creator
        public NearbyDevice[] newArray(int size) {
            return new NearbyDevice[size];
        }
    };
    static final String TAG = "for outer NearbyDevice";
    public static final int TYPE_1_0_PHONE = 1;
    public static final int TYPE_2_0_PHONE = 2;
    public static final int TYPE_NO_BLE_CONNECT_PC = 3;
    public static final int TYPE_TV = 4;
    private static final long serialVersionUID = -4791989426702122475L;
    private String mAccountId;
    private String mApMac;
    private boolean mAvailability;
    private String mBleMac;
    private String mBrMac;
    private String mBtName;
    private int mBusinessId;
    private NearbyConfig.BusinessTypeEnum mBusinessType;
    private String mDeviceID;
    private int mDeviceType;
    private String mHuaweiIdName;
    private boolean mIsNeedClose;
    private boolean mIsNeedUpdatePswd;
    private String mLocalIp;
    private int mManufacturer;
    private int mNearbyVersion;
    private String mProductID;
    private int mPwdVer;
    private String mRemoteIp;
    private boolean mSameHwAccount;
    private String mSummary;
    private int mVendorDeviceType;
    private int mWifiBand;
    private String mWifiFreq;
    private int mWifiPort;
    private String mWifiPwd;
    private String mWifiSsid;

    public boolean isSameHwAccount() {
        return this.mSameHwAccount;
    }

    public void setSameHwAccount(boolean sameHwAccount) {
        this.mSameHwAccount = sameHwAccount;
    }

    public String getSummary() {
        return this.mSummary;
    }

    public String getBluetoothMac() {
        return this.mBrMac;
    }

    public String getBleMac() {
        return this.mBleMac;
    }

    public String getHuaweiIdName() {
        return this.mHuaweiIdName;
    }

    public String getBtName() {
        return this.mBtName;
    }

    public int getBusinessId() {
        return this.mBusinessId;
    }

    public NearbyConfig.BusinessTypeEnum getBusinessType() {
        return this.mBusinessType;
    }

    public boolean getAvailability() {
        return this.mAvailability;
    }

    public String getApMac() {
        return this.mApMac;
    }

    public String getWifiSsid() {
        return this.mWifiSsid;
    }

    public String getWifiPwd() {
        return this.mWifiPwd;
    }

    public int getWifiBand() {
        return this.mWifiBand;
    }

    public String getRemoteIp() {
        return this.mRemoteIp;
    }

    public String getLocalIp() {
        return this.mLocalIp;
    }

    public int getWifiPort() {
        return this.mWifiPort;
    }

    public boolean getNeedClose() {
        return this.mIsNeedClose;
    }

    public void setNeedClose(boolean needClose) {
        this.mIsNeedClose = needClose;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public String getDeviceID() {
        return this.mDeviceID;
    }

    public int getManufacturer() {
        return this.mManufacturer;
    }

    public int getVendorDeviceType() {
        return this.mVendorDeviceType;
    }

    public String getProductID() {
        return this.mProductID;
    }

    public void setmWifiPwd(String pwd) {
        this.mWifiPwd = pwd;
    }

    public boolean isNeedUpdatePwd() {
        return this.mIsNeedUpdatePswd;
    }

    public String getWifiFreq() {
        return this.mWifiFreq;
    }

    public int getPwdVer() {
        return this.mPwdVer;
    }

    public void setAccountId(String accountId) {
        this.mAccountId = accountId;
    }

    public String getAccountId() {
        return this.mAccountId;
    }

    public void setNearbyVersion(int nearbyVersion) {
        this.mNearbyVersion = nearbyVersion;
    }

    public int getNearbyVersion() {
        return this.mNearbyVersion;
    }

    public NearbyDevice(String summary, String bluetoothMac, String huaweiIdName, String btName, int businessId, NearbyConfig.BusinessTypeEnum businessType, boolean availability, String apMac, boolean sameHwAccount) {
        this(summary, bluetoothMac, huaweiIdName, btName, businessId, businessType, availability, apMac, sameHwAccount, null, null, -1, null, null, 0, false, 0, 0, 0, null, null, false, 0, null);
    }

    public NearbyDevice(String summary, String bluetoothMac, String huaweiIdName, String btName, int businessId, NearbyConfig.BusinessTypeEnum businessType, boolean availability, String apMac, boolean sameHwAccount, String wifiSsid, String wifiPwd, int wifiBand, String remoteIp, String localIp, int wifiPort, boolean isNeedClose, int devType, int manufacturer, int vendorDeviceType, String productID, String devID, boolean updatePwd, int pwdVer, String freq) {
        this.mBusinessType = NearbyConfig.BusinessTypeEnum.AllType;
        this.mSummary = summary;
        this.mBrMac = bluetoothMac;
        this.mHuaweiIdName = huaweiIdName;
        this.mBtName = btName;
        this.mBusinessId = businessId;
        this.mBusinessType = businessType;
        this.mAvailability = availability;
        this.mApMac = apMac;
        this.mSameHwAccount = sameHwAccount;
        this.mWifiSsid = wifiSsid;
        this.mWifiPwd = wifiPwd;
        this.mWifiBand = wifiBand;
        this.mRemoteIp = remoteIp;
        this.mLocalIp = localIp;
        this.mWifiPort = wifiPort;
        this.mIsNeedClose = isNeedClose;
        this.mDeviceType = devType;
        this.mManufacturer = manufacturer;
        this.mVendorDeviceType = vendorDeviceType;
        this.mProductID = productID;
        this.mDeviceID = devID;
        this.mIsNeedUpdatePswd = updatePwd;
        this.mPwdVer = pwdVer;
        this.mWifiFreq = freq;
    }

    public NearbyDevice(String wifiSsid, String wifiPwd, int wifiBand, String wifiIp, int wifiPort) {
        this.mBusinessType = NearbyConfig.BusinessTypeEnum.AllType;
        this.mWifiSsid = wifiSsid;
        this.mWifiPwd = wifiPwd;
        this.mWifiBand = wifiBand;
        this.mRemoteIp = wifiIp;
        this.mWifiPort = wifiPort;
    }

    public NearbyDevice(String wifiSsid, String bssid, String wifiPwd, String wifiFreq, int vendorDeviceType, int manufacturer, String productID, String deviceID, int pwdVer, boolean isNeedUpdatePwd) {
        this.mBusinessType = NearbyConfig.BusinessTypeEnum.AllType;
        this.mWifiSsid = wifiSsid;
        this.mWifiPwd = wifiPwd;
        this.mApMac = bssid;
        this.mWifiFreq = wifiFreq;
        this.mVendorDeviceType = vendorDeviceType;
        this.mManufacturer = manufacturer;
        this.mProductID = productID;
        this.mDeviceID = deviceID;
        this.mPwdVer = pwdVer;
        this.mIsNeedUpdatePswd = isNeedUpdatePwd;
    }

    private static boolean equals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override // java.lang.Object
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (!(anObject instanceof NearbyDevice)) {
            return false;
        }
        NearbyDevice anDevice = (NearbyDevice) anObject;
        if (this.mSummary == null && anDevice.mSummary == null) {
            return equals(this.mWifiSsid, anDevice.mWifiSsid) && equals(this.mWifiPwd, anDevice.mWifiPwd) && equals(Integer.valueOf(this.mWifiBand), Integer.valueOf(anDevice.mWifiBand));
        }
        return equals(this.mSummary, anDevice.mSummary);
    }

    @Override // java.lang.Object
    public int hashCode() {
        if (this.mSummary == null) {
            return 0;
        }
        return this.mSummary.hashCode();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return "NearbyDevice:{Summary:" + Util.toFrontHalfString(this.mSummary) + ";BusinessId:" + this.mBusinessId + ";BusinessType:" + this.mBusinessType.toNumber() + ";Availability:" + this.mAvailability + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flag) {
        int i;
        int i2;
        int i3 = 1;
        dest.writeString(this.mSummary);
        dest.writeString(this.mBrMac);
        dest.writeString(this.mHuaweiIdName);
        dest.writeString(this.mBtName);
        dest.writeInt(this.mBusinessId);
        dest.writeInt(this.mBusinessType.toNumber());
        dest.writeInt(this.mAvailability ? 1 : 0);
        dest.writeString(this.mApMac);
        if (this.mSameHwAccount) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.mWifiSsid);
        dest.writeString(this.mWifiPwd);
        dest.writeInt(this.mWifiBand);
        dest.writeString(this.mRemoteIp);
        dest.writeString(this.mLocalIp);
        dest.writeInt(this.mWifiPort);
        if (this.mIsNeedClose) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.mManufacturer);
        dest.writeInt(this.mDeviceType);
        dest.writeInt(this.mVendorDeviceType);
        dest.writeString(this.mProductID);
        dest.writeString(this.mDeviceID);
        if (!this.mIsNeedUpdatePswd) {
            i3 = 0;
        }
        dest.writeInt(i3);
        dest.writeInt(this.mPwdVer);
        dest.writeString(this.mWifiFreq);
        dest.writeString(this.mBleMac);
        dest.writeString(this.mAccountId);
        dest.writeInt(this.mNearbyVersion);
    }

    public static class Builder {
        private String mAccountId;
        private String mApMac;
        private String mBleMac;
        private String mBrMac;
        private String mBtName;
        private int mBusinessId;
        private NearbyConfig.BusinessTypeEnum mBusinessType = NearbyConfig.BusinessTypeEnum.AllType;
        private String mDeviceId;
        private int mDeviceType;
        private String mHuaWeiIdName;
        private boolean mIsAvailability;
        private boolean mIsNeedClose;
        private boolean mIsNeedUpdatePwd;
        private boolean mIsSameHwAccount;
        private String mLocalIp;
        private int mManufacturer;
        private int mNearbyVersion;
        private String mProductId;
        private int mPwdVer;
        private String mRemoteIp;
        private String mSummary;
        private int mVendorDeviceType;
        private int mWifiBand;
        private String mWifiFreq;
        private int mWifiPort;
        private String mWifiPwd;
        private String mWifiSsid;

        public Builder withSummary(String summary) {
            this.mSummary = summary;
            return this;
        }

        public Builder withBrMac(String brMac) {
            this.mBrMac = brMac;
            return this;
        }

        public Builder withBleMac(String bleMac) {
            this.mBleMac = bleMac;
            return this;
        }

        public Builder withAccountId(String accountId) {
            this.mAccountId = accountId;
            return this;
        }

        public Builder withHuaWeiIdName(String huaWeiIdName) {
            this.mHuaWeiIdName = huaWeiIdName;
            return this;
        }

        public Builder withBtName(String btName) {
            this.mBtName = btName;
            return this;
        }

        public Builder withBusinessId(int businessId) {
            this.mBusinessId = businessId;
            return this;
        }

        public Builder withBusinessType(NearbyConfig.BusinessTypeEnum businessType) {
            this.mBusinessType = businessType;
            return this;
        }

        public Builder withIsAvailability(boolean isAvailability) {
            this.mIsAvailability = isAvailability;
            return this;
        }

        public Builder withApMac(String apMac) {
            this.mApMac = apMac;
            return this;
        }

        public Builder withIsSameHwAccount(boolean isSameHwAccount) {
            this.mIsSameHwAccount = isSameHwAccount;
            return this;
        }

        public Builder withWifiSsid(String wifiSsid) {
            this.mWifiSsid = wifiSsid;
            return this;
        }

        public Builder withWifiPwd(String wifiPwd) {
            this.mWifiPwd = wifiPwd;
            return this;
        }

        public Builder withWifiBand(int wifiBand) {
            this.mWifiBand = wifiBand;
            return this;
        }

        public Builder withWifiFreq(String wifiFreq) {
            this.mWifiFreq = wifiFreq;
            return this;
        }

        public Builder withRemoteIp(String remoteIp) {
            this.mRemoteIp = remoteIp;
            return this;
        }

        public Builder withLocalIp(String localIp) {
            this.mLocalIp = localIp;
            return this;
        }

        public Builder withWifiPort(int wifiPort) {
            this.mWifiPort = wifiPort;
            return this;
        }

        public Builder withIsNeedClose(boolean isNeedClose) {
            this.mIsNeedClose = isNeedClose;
            return this;
        }

        public Builder withDeviceType(int deviceType) {
            this.mDeviceType = deviceType;
            return this;
        }

        public Builder withIsNeedUpdatePwd(boolean isNeedUpdatePwd) {
            this.mIsNeedUpdatePwd = isNeedUpdatePwd;
            return this;
        }

        public Builder withVendorDeviceType(int vendorDeviceType) {
            this.mVendorDeviceType = vendorDeviceType;
            return this;
        }

        public Builder withManufacturer(int manufacturer) {
            this.mManufacturer = manufacturer;
            return this;
        }

        public Builder withProductId(String productId) {
            this.mProductId = productId;
            return this;
        }

        public Builder withDeviceId(String deviceId) {
            this.mDeviceId = deviceId;
            return this;
        }

        public Builder withPwdVer(int pwdVer) {
            this.mPwdVer = pwdVer;
            return this;
        }

        public Builder withNearbyVersion(int nearbyVersion) {
            this.mNearbyVersion = nearbyVersion;
            return this;
        }

        public NearbyDevice build() {
            return new NearbyDevice(this);
        }
    }

    private NearbyDevice(Builder builder) {
        this.mBusinessType = NearbyConfig.BusinessTypeEnum.AllType;
        this.mSummary = builder.mSummary;
        this.mBrMac = builder.mBrMac;
        this.mBleMac = builder.mBleMac;
        this.mAccountId = builder.mAccountId;
        this.mHuaweiIdName = builder.mHuaWeiIdName;
        this.mBtName = builder.mBtName;
        this.mBusinessId = builder.mBusinessId;
        this.mBusinessType = builder.mBusinessType;
        this.mAvailability = builder.mIsAvailability;
        this.mApMac = builder.mApMac;
        this.mSameHwAccount = builder.mIsSameHwAccount;
        this.mWifiSsid = builder.mWifiSsid;
        this.mWifiPwd = builder.mWifiPwd;
        this.mWifiBand = builder.mWifiBand;
        this.mWifiFreq = builder.mWifiFreq;
        this.mRemoteIp = builder.mRemoteIp;
        this.mLocalIp = builder.mLocalIp;
        this.mWifiPort = builder.mWifiPort;
        this.mIsNeedClose = builder.mIsNeedClose;
        this.mDeviceType = builder.mDeviceType;
        this.mIsNeedUpdatePswd = builder.mIsNeedUpdatePwd;
        this.mVendorDeviceType = builder.mVendorDeviceType;
        this.mManufacturer = builder.mManufacturer;
        this.mProductID = builder.mProductId;
        this.mDeviceID = builder.mDeviceId;
        this.mPwdVer = builder.mPwdVer;
        this.mNearbyVersion = builder.mNearbyVersion;
    }
}
