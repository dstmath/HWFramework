package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class MetaStatusPolicy extends AManagedObject {
    public static final Parcelable.Creator<MetaStatusPolicy> CREATOR = new Parcelable.Creator<MetaStatusPolicy>() {
        /* class com.huawei.nb.model.collectencrypt.MetaStatusPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaStatusPolicy createFromParcel(Parcel parcel) {
            return new MetaStatusPolicy(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaStatusPolicy[] newArray(int i) {
            return new MetaStatusPolicy[i];
        }
    };
    private String mAirplaneMode;
    private String mBatteryState;
    private String mBluetoothState;
    private String mEyeComfortSwitch;
    private String mHeadsetState;
    private String mHiBoardSwitch;
    private Integer mId;
    private String mNfcSwitch;
    private String mNoDisturb;
    private String mPhoneState;
    private String mPowerSaving;
    private String mPowerSupply;
    private Integer mReservedInt;
    private String mReservedText;
    private String mRoamingState;
    private String mScreenState;
    private String mSilentMode;
    private String mStudentState;
    private String mWifiState;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaStatusPolicy";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaStatusPolicy(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mPhoneState = cursor.getString(2);
        this.mRoamingState = cursor.getString(3);
        this.mBatteryState = cursor.getString(4);
        this.mPowerSaving = cursor.getString(5);
        this.mPowerSupply = cursor.getString(6);
        this.mScreenState = cursor.getString(7);
        this.mHeadsetState = cursor.getString(8);
        this.mBluetoothState = cursor.getString(9);
        this.mWifiState = cursor.getString(10);
        this.mStudentState = cursor.getString(11);
        this.mAirplaneMode = cursor.getString(12);
        this.mNoDisturb = cursor.getString(13);
        this.mSilentMode = cursor.getString(14);
        this.mNfcSwitch = cursor.getString(15);
        this.mEyeComfortSwitch = cursor.getString(16);
        this.mHiBoardSwitch = cursor.getString(17);
        this.mReservedInt = !cursor.isNull(18) ? Integer.valueOf(cursor.getInt(18)) : num;
        this.mReservedText = cursor.getString(19);
    }

    public MetaStatusPolicy(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mPhoneState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mRoamingState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBatteryState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mPowerSaving = parcel.readByte() == 0 ? null : parcel.readString();
        this.mPowerSupply = parcel.readByte() == 0 ? null : parcel.readString();
        this.mScreenState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mHeadsetState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBluetoothState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWifiState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mStudentState = parcel.readByte() == 0 ? null : parcel.readString();
        this.mAirplaneMode = parcel.readByte() == 0 ? null : parcel.readString();
        this.mNoDisturb = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSilentMode = parcel.readByte() == 0 ? null : parcel.readString();
        this.mNfcSwitch = parcel.readByte() == 0 ? null : parcel.readString();
        this.mEyeComfortSwitch = parcel.readByte() == 0 ? null : parcel.readString();
        this.mHiBoardSwitch = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaStatusPolicy(Integer num, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, Integer num2, String str17) {
        this.mId = num;
        this.mPhoneState = str;
        this.mRoamingState = str2;
        this.mBatteryState = str3;
        this.mPowerSaving = str4;
        this.mPowerSupply = str5;
        this.mScreenState = str6;
        this.mHeadsetState = str7;
        this.mBluetoothState = str8;
        this.mWifiState = str9;
        this.mStudentState = str10;
        this.mAirplaneMode = str11;
        this.mNoDisturb = str12;
        this.mSilentMode = str13;
        this.mNfcSwitch = str14;
        this.mEyeComfortSwitch = str15;
        this.mHiBoardSwitch = str16;
        this.mReservedInt = num2;
        this.mReservedText = str17;
    }

    public MetaStatusPolicy() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getMPhoneState() {
        return this.mPhoneState;
    }

    public void setMPhoneState(String str) {
        this.mPhoneState = str;
        setValue();
    }

    public String getMRoamingState() {
        return this.mRoamingState;
    }

    public void setMRoamingState(String str) {
        this.mRoamingState = str;
        setValue();
    }

    public String getMBatteryState() {
        return this.mBatteryState;
    }

    public void setMBatteryState(String str) {
        this.mBatteryState = str;
        setValue();
    }

    public String getMPowerSaving() {
        return this.mPowerSaving;
    }

    public void setMPowerSaving(String str) {
        this.mPowerSaving = str;
        setValue();
    }

    public String getMPowerSupply() {
        return this.mPowerSupply;
    }

    public void setMPowerSupply(String str) {
        this.mPowerSupply = str;
        setValue();
    }

    public String getMScreenState() {
        return this.mScreenState;
    }

    public void setMScreenState(String str) {
        this.mScreenState = str;
        setValue();
    }

    public String getMHeadsetState() {
        return this.mHeadsetState;
    }

    public void setMHeadsetState(String str) {
        this.mHeadsetState = str;
        setValue();
    }

    public String getMBluetoothState() {
        return this.mBluetoothState;
    }

    public void setMBluetoothState(String str) {
        this.mBluetoothState = str;
        setValue();
    }

    public String getMWifiState() {
        return this.mWifiState;
    }

    public void setMWifiState(String str) {
        this.mWifiState = str;
        setValue();
    }

    public String getMStudentState() {
        return this.mStudentState;
    }

    public void setMStudentState(String str) {
        this.mStudentState = str;
        setValue();
    }

    public String getMAirplaneMode() {
        return this.mAirplaneMode;
    }

    public void setMAirplaneMode(String str) {
        this.mAirplaneMode = str;
        setValue();
    }

    public String getMNoDisturb() {
        return this.mNoDisturb;
    }

    public void setMNoDisturb(String str) {
        this.mNoDisturb = str;
        setValue();
    }

    public String getMSilentMode() {
        return this.mSilentMode;
    }

    public void setMSilentMode(String str) {
        this.mSilentMode = str;
        setValue();
    }

    public String getMNfcSwitch() {
        return this.mNfcSwitch;
    }

    public void setMNfcSwitch(String str) {
        this.mNfcSwitch = str;
        setValue();
    }

    public String getMEyeComfortSwitch() {
        return this.mEyeComfortSwitch;
    }

    public void setMEyeComfortSwitch(String str) {
        this.mEyeComfortSwitch = str;
        setValue();
    }

    public String getMHiBoardSwitch() {
        return this.mHiBoardSwitch;
    }

    public void setMHiBoardSwitch(String str) {
        this.mHiBoardSwitch = str;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer num) {
        this.mReservedInt = num;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String str) {
        this.mReservedText = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mPhoneState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPhoneState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mRoamingState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mRoamingState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBatteryState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBatteryState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mPowerSaving != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPowerSaving);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mPowerSupply != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPowerSupply);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mScreenState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mScreenState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHeadsetState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHeadsetState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBluetoothState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBluetoothState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWifiState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mWifiState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mStudentState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mStudentState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAirplaneMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mAirplaneMode);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mNoDisturb != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mNoDisturb);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSilentMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSilentMode);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mNfcSwitch != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mNfcSwitch);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEyeComfortSwitch != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEyeComfortSwitch);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mHiBoardSwitch != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mHiBoardSwitch);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReservedInt.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReservedText);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<MetaStatusPolicy> getHelper() {
        return MetaStatusPolicyHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaStatusPolicy { mId: " + this.mId + ", mPhoneState: " + this.mPhoneState + ", mRoamingState: " + this.mRoamingState + ", mBatteryState: " + this.mBatteryState + ", mPowerSaving: " + this.mPowerSaving + ", mPowerSupply: " + this.mPowerSupply + ", mScreenState: " + this.mScreenState + ", mHeadsetState: " + this.mHeadsetState + ", mBluetoothState: " + this.mBluetoothState + ", mWifiState: " + this.mWifiState + ", mStudentState: " + this.mStudentState + ", mAirplaneMode: " + this.mAirplaneMode + ", mNoDisturb: " + this.mNoDisturb + ", mSilentMode: " + this.mSilentMode + ", mNfcSwitch: " + this.mNfcSwitch + ", mEyeComfortSwitch: " + this.mEyeComfortSwitch + ", mHiBoardSwitch: " + this.mHiBoardSwitch + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
