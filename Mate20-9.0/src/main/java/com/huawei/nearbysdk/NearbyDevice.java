package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.NearbyConfig;
import com.huawei.nearbysdk.util.Util;
import java.io.Serializable;

public final class NearbyDevice implements Parcelable, Serializable {
    public static final Parcelable.Creator<NearbyDevice> CREATOR = new Parcelable.Creator<NearbyDevice>() {
        public NearbyDevice createFromParcel(Parcel source) {
            NearbyDevice nearbyDevice = new NearbyDevice(source.readString(), source.readString(), source.readString(), source.readString(), source.readInt(), NearbySDKUtils.getEnumFromInt(source.readInt()), source.readInt() == 1, source.readString(), source.readInt() == 1, source.readString(), source.readString(), source.readInt(), source.readString(), source.readString(), source.readInt(), source.readInt() == 1, source.readInt(), source.readInt(), source.readInt(), source.readString(), source.readString(), source.readInt() == 1, source.readInt(), source.readString());
            return nearbyDevice;
        }

        public NearbyDevice[] newArray(int size) {
            return new NearbyDevice[size];
        }
    };
    static final String TAG = "for outer NearbyDevice";
    public static final int TYPE_1_0_PHONE = 1;
    public static final int TYPE_2_0_PHONE = 2;
    public static final int TYPE_NO_BLE_CONNECT_PC = 3;
    private String mApMac;
    private boolean mAvailability;
    private String mBluetoothMac;
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
        return this.mBluetoothMac;
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

    public NearbyDevice(String summary, String bluetoothMac, String huaweiIdName, String btName, int businessId, NearbyConfig.BusinessTypeEnum businessType, boolean availability, String apMac, boolean sameHwAccount) {
        this(summary, bluetoothMac, huaweiIdName, btName, businessId, businessType, availability, apMac, sameHwAccount, null, null, -1, null, null, 0, false, 0, 0, 0, null, null, false, 0, null);
    }

    public NearbyDevice(String summary, String bluetoothMac, String huaweiIdName, String btName, int businessId, NearbyConfig.BusinessTypeEnum businessType, boolean availability, String apMac, boolean sameHwAccount, String wifiSsid, String wifiPwd, int wifiBand, String remoteIp, String localIp, int wifiPort, boolean isNeedClose, int devType, int manufacturer, int vendorDeviceType, String productID, String devID, boolean updatePwd, int pwdVer, String freq) {
        this.mBusinessType = NearbyConfig.BusinessTypeEnum.AllType;
        this.mSummary = summary;
        this.mBluetoothMac = bluetoothMac;
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

    public boolean equals(Object anObject) {
        boolean z = true;
        if (this == anObject) {
            return true;
        }
        if (!(anObject instanceof NearbyDevice)) {
            return false;
        }
        NearbyDevice anDevice = (NearbyDevice) anObject;
        if (this.mSummary != null || anDevice.mSummary != null) {
            return equals(this.mSummary, anDevice.mSummary);
        }
        if (!equals(this.mWifiSsid, anDevice.mWifiSsid) || !equals(this.mWifiPwd, anDevice.mWifiPwd) || !equals(Integer.valueOf(this.mWifiBand), Integer.valueOf(anDevice.mWifiBand))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this.mSummary == null) {
            return 0;
        }
        return this.mSummary.hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "NearbyDevice:{Summary:" + Util.toFrontHalfString(this.mSummary) + ";BusinessId:" + this.mBusinessId + ";BusinessType:" + this.mBusinessType.toNumber() + ";Availability:" + this.mAvailability + "}";
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.mSummary);
        dest.writeString(this.mBluetoothMac);
        dest.writeString(this.mHuaweiIdName);
        dest.writeString(this.mBtName);
        dest.writeInt(this.mBusinessId);
        dest.writeInt(this.mBusinessType.toNumber());
        dest.writeInt(this.mAvailability ? 1 : 0);
        dest.writeString(this.mApMac);
        dest.writeInt(this.mSameHwAccount ? 1 : 0);
        dest.writeString(this.mWifiSsid);
        dest.writeString(this.mWifiPwd);
        dest.writeInt(this.mWifiBand);
        dest.writeString(this.mRemoteIp);
        dest.writeString(this.mLocalIp);
        dest.writeInt(this.mWifiPort);
        dest.writeInt(this.mIsNeedClose ? 1 : 0);
        dest.writeInt(this.mManufacturer);
        dest.writeInt(this.mDeviceType);
        dest.writeInt(this.mVendorDeviceType);
        dest.writeString(this.mProductID);
        dest.writeString(this.mDeviceID);
        dest.writeInt(this.mIsNeedUpdatePswd ? 1 : 0);
        dest.writeInt(this.mPwdVer);
        dest.writeString(this.mWifiFreq);
    }
}
