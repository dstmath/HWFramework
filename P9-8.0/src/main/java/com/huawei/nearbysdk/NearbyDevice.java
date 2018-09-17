package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.nearbysdk.NearbyConfig.BusinessTypeEnum;
import java.io.Serializable;

public final class NearbyDevice implements Parcelable, Serializable {
    public static final Creator<NearbyDevice> CREATOR = new Creator<NearbyDevice>() {
        public NearbyDevice createFromParcel(Parcel source) {
            return new NearbyDevice(source.readString(), source.readString(), source.readString(), source.readString(), source.readInt(), NearbySDKUtils.getEnumFromInt(source.readInt()), source.readInt() == 1, source.readString(), source.readInt() == 1);
        }

        public NearbyDevice[] newArray(int size) {
            return new NearbyDevice[size];
        }
    };
    static final String TAG = "for outer NearbyDevice";
    private String mApMac;
    private boolean mAvailability;
    private String mBluetoothMac;
    private String mBtName;
    private int mBusinessId;
    private BusinessTypeEnum mBusinessType;
    private String mHuaweiIdName;
    private boolean mSameHwAccount;
    private String mSummary;

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

    public BusinessTypeEnum getBusinessType() {
        return this.mBusinessType;
    }

    public boolean getAvailability() {
        return this.mAvailability;
    }

    public String getApMac() {
        return this.mApMac;
    }

    public NearbyDevice(String summary, String bluetoothMac, String huaweiIdName, String btName, int businessId, BusinessTypeEnum businessType, boolean availability, String apMac, boolean sameHwAccount) {
        this.mSummary = summary;
        this.mBluetoothMac = bluetoothMac;
        this.mHuaweiIdName = huaweiIdName;
        this.mBtName = btName;
        this.mBusinessId = businessId;
        this.mBusinessType = businessType;
        this.mAvailability = availability;
        this.mApMac = apMac;
        this.mSameHwAccount = sameHwAccount;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (!(anObject instanceof NearbyDevice)) {
            return false;
        }
        return this.mSummary.equals(((NearbyDevice) anObject).mSummary);
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
        return "NearbyDevice:{Summary:" + this.mSummary + ";BusinessId:" + this.mBusinessId + ";BusinessType:" + this.mBusinessType.toNumber() + ";Availability:" + this.mAvailability + "}";
    }

    public void writeToParcel(Parcel dest, int flag) {
        int i;
        int i2 = 1;
        dest.writeString(this.mSummary);
        dest.writeString(this.mBluetoothMac);
        dest.writeString(this.mHuaweiIdName);
        dest.writeString(this.mBtName);
        dest.writeInt(this.mBusinessId);
        dest.writeInt(this.mBusinessType.toNumber());
        if (this.mAvailability) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.mApMac);
        if (!this.mSameHwAccount) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }
}
