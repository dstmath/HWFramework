package com.huawei.nb.model.geofence;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class GeoFence extends AManagedObject {
    public static final Parcelable.Creator<GeoFence> CREATOR = new Parcelable.Creator<GeoFence>() {
        /* class com.huawei.nb.model.geofence.GeoFence.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GeoFence createFromParcel(Parcel parcel) {
            return new GeoFence(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public GeoFence[] newArray(int i) {
            return new GeoFence[i];
        }
    };
    private String mBlocksID;
    private String mCategory;
    private String mCenter;
    private Integer mCityCode;
    private Character mDisabled;
    private String mEnterCellid;
    private String mEnterLocation;
    private String mEnterWifiBssid;
    private Short mEnteredNum;
    private String mFenceID;
    private String mFloor;
    private Character mImportance;
    private Character mInoutdoor;
    private Long mLastLeftTime;
    private String mLastUpdated;
    private String mName;
    private String mNearCellid;
    private String mNearLocation;
    private String mNearWifiBssid;
    private String mOutFenceID;
    private String mReserved1;
    private String mReserved2;
    private Integer mRuleId;
    private Character mShape;
    private Short mStatus;
    private String mSubCategory;
    private Character mTeleoperators;
    private String mWifiChannel;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.geofence.GeoFence";
    }

    public String getEntityVersion() {
        return "0.0.3";
    }

    public int getEntityVersionCode() {
        return 3;
    }

    public GeoFence(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Character ch = null;
        this.mRuleId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mFenceID = cursor.getString(2);
        this.mOutFenceID = cursor.getString(3);
        this.mBlocksID = cursor.getString(4);
        this.mCategory = cursor.getString(5);
        this.mSubCategory = cursor.getString(6);
        this.mName = cursor.getString(7);
        this.mInoutdoor = cursor.isNull(8) ? null : Character.valueOf(cursor.getString(8).charAt(0));
        this.mFloor = cursor.getString(9);
        this.mTeleoperators = cursor.isNull(10) ? null : Character.valueOf(cursor.getString(10).charAt(0));
        this.mNearCellid = cursor.getString(11);
        this.mEnterCellid = cursor.getString(12);
        this.mShape = cursor.isNull(13) ? null : Character.valueOf(cursor.getString(13).charAt(0));
        this.mEnterLocation = cursor.getString(14);
        this.mNearLocation = cursor.getString(15);
        this.mCenter = cursor.getString(16);
        this.mCityCode = cursor.isNull(17) ? null : Integer.valueOf(cursor.getInt(17));
        this.mImportance = cursor.isNull(18) ? null : Character.valueOf(cursor.getString(18).charAt(0));
        this.mLastUpdated = cursor.getString(19);
        this.mStatus = cursor.isNull(20) ? null : Short.valueOf(cursor.getShort(20));
        this.mReserved1 = cursor.getString(21);
        this.mReserved2 = cursor.getString(22);
        this.mLastLeftTime = cursor.isNull(23) ? null : Long.valueOf(cursor.getLong(23));
        this.mEnteredNum = cursor.isNull(24) ? null : Short.valueOf(cursor.getShort(24));
        this.mDisabled = !cursor.isNull(25) ? Character.valueOf(cursor.getString(25).charAt(0)) : ch;
        this.mNearWifiBssid = cursor.getString(26);
        this.mEnterWifiBssid = cursor.getString(27);
        this.mWifiChannel = cursor.getString(28);
    }

    public GeoFence(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mRuleId = null;
            parcel.readInt();
        } else {
            this.mRuleId = Integer.valueOf(parcel.readInt());
        }
        this.mFenceID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mOutFenceID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBlocksID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCategory = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSubCategory = parcel.readByte() == 0 ? null : parcel.readString();
        this.mName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mInoutdoor = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mFloor = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTeleoperators = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mNearCellid = parcel.readByte() == 0 ? null : parcel.readString();
        this.mEnterCellid = parcel.readByte() == 0 ? null : parcel.readString();
        this.mShape = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mEnterLocation = parcel.readByte() == 0 ? null : parcel.readString();
        this.mNearLocation = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCenter = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCityCode = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mImportance = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mLastUpdated = parcel.readByte() == 0 ? null : parcel.readString();
        this.mStatus = parcel.readByte() == 0 ? null : Short.valueOf((short) parcel.readInt());
        this.mReserved1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReserved2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.mLastLeftTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.mEnteredNum = parcel.readByte() == 0 ? null : Short.valueOf((short) parcel.readInt());
        this.mDisabled = parcel.readByte() == 0 ? null : Character.valueOf(parcel.createCharArray()[0]);
        this.mNearWifiBssid = parcel.readByte() == 0 ? null : parcel.readString();
        this.mEnterWifiBssid = parcel.readByte() == 0 ? null : parcel.readString();
        this.mWifiChannel = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private GeoFence(Integer num, String str, String str2, String str3, String str4, String str5, String str6, Character ch, String str7, Character ch2, String str8, String str9, Character ch3, String str10, String str11, String str12, Integer num2, Character ch4, String str13, Short sh, String str14, String str15, Long l, Short sh2, Character ch5, String str16, String str17, String str18) {
        this.mRuleId = num;
        this.mFenceID = str;
        this.mOutFenceID = str2;
        this.mBlocksID = str3;
        this.mCategory = str4;
        this.mSubCategory = str5;
        this.mName = str6;
        this.mInoutdoor = ch;
        this.mFloor = str7;
        this.mTeleoperators = ch2;
        this.mNearCellid = str8;
        this.mEnterCellid = str9;
        this.mShape = ch3;
        this.mEnterLocation = str10;
        this.mNearLocation = str11;
        this.mCenter = str12;
        this.mCityCode = num2;
        this.mImportance = ch4;
        this.mLastUpdated = str13;
        this.mStatus = sh;
        this.mReserved1 = str14;
        this.mReserved2 = str15;
        this.mLastLeftTime = l;
        this.mEnteredNum = sh2;
        this.mDisabled = ch5;
        this.mNearWifiBssid = str16;
        this.mEnterWifiBssid = str17;
        this.mWifiChannel = str18;
    }

    public GeoFence() {
    }

    public Integer getMRuleId() {
        return this.mRuleId;
    }

    public void setMRuleId(Integer num) {
        this.mRuleId = num;
        setValue();
    }

    public String getMFenceID() {
        return this.mFenceID;
    }

    public void setMFenceID(String str) {
        this.mFenceID = str;
        setValue();
    }

    public String getMOutFenceID() {
        return this.mOutFenceID;
    }

    public void setMOutFenceID(String str) {
        this.mOutFenceID = str;
        setValue();
    }

    public String getMBlocksID() {
        return this.mBlocksID;
    }

    public void setMBlocksID(String str) {
        this.mBlocksID = str;
        setValue();
    }

    public String getMCategory() {
        return this.mCategory;
    }

    public void setMCategory(String str) {
        this.mCategory = str;
        setValue();
    }

    public String getMSubCategory() {
        return this.mSubCategory;
    }

    public void setMSubCategory(String str) {
        this.mSubCategory = str;
        setValue();
    }

    public String getMName() {
        return this.mName;
    }

    public void setMName(String str) {
        this.mName = str;
        setValue();
    }

    public Character getMInoutdoor() {
        return this.mInoutdoor;
    }

    public void setMInoutdoor(Character ch) {
        this.mInoutdoor = ch;
        setValue();
    }

    public String getMFloor() {
        return this.mFloor;
    }

    public void setMFloor(String str) {
        this.mFloor = str;
        setValue();
    }

    public Character getMTeleoperators() {
        return this.mTeleoperators;
    }

    public void setMTeleoperators(Character ch) {
        this.mTeleoperators = ch;
        setValue();
    }

    public String getMNearCellid() {
        return this.mNearCellid;
    }

    public void setMNearCellid(String str) {
        this.mNearCellid = str;
        setValue();
    }

    public String getMEnterCellid() {
        return this.mEnterCellid;
    }

    public void setMEnterCellid(String str) {
        this.mEnterCellid = str;
        setValue();
    }

    public Character getMShape() {
        return this.mShape;
    }

    public void setMShape(Character ch) {
        this.mShape = ch;
        setValue();
    }

    public String getMEnterLocation() {
        return this.mEnterLocation;
    }

    public void setMEnterLocation(String str) {
        this.mEnterLocation = str;
        setValue();
    }

    public String getMNearLocation() {
        return this.mNearLocation;
    }

    public void setMNearLocation(String str) {
        this.mNearLocation = str;
        setValue();
    }

    public String getMCenter() {
        return this.mCenter;
    }

    public void setMCenter(String str) {
        this.mCenter = str;
        setValue();
    }

    public Integer getMCityCode() {
        return this.mCityCode;
    }

    public void setMCityCode(Integer num) {
        this.mCityCode = num;
        setValue();
    }

    public Character getMImportance() {
        return this.mImportance;
    }

    public void setMImportance(Character ch) {
        this.mImportance = ch;
        setValue();
    }

    public String getMLastUpdated() {
        return this.mLastUpdated;
    }

    public void setMLastUpdated(String str) {
        this.mLastUpdated = str;
        setValue();
    }

    public Short getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(Short sh) {
        this.mStatus = sh;
        setValue();
    }

    public String getMReserved1() {
        return this.mReserved1;
    }

    public void setMReserved1(String str) {
        this.mReserved1 = str;
        setValue();
    }

    public String getMReserved2() {
        return this.mReserved2;
    }

    public void setMReserved2(String str) {
        this.mReserved2 = str;
        setValue();
    }

    public Long getMLastLeftTime() {
        return this.mLastLeftTime;
    }

    public void setMLastLeftTime(Long l) {
        this.mLastLeftTime = l;
        setValue();
    }

    public Short getMEnteredNum() {
        return this.mEnteredNum;
    }

    public void setMEnteredNum(Short sh) {
        this.mEnteredNum = sh;
        setValue();
    }

    public Character getMDisabled() {
        return this.mDisabled;
    }

    public void setMDisabled(Character ch) {
        this.mDisabled = ch;
        setValue();
    }

    public String getMNearWifiBssid() {
        return this.mNearWifiBssid;
    }

    public void setMNearWifiBssid(String str) {
        this.mNearWifiBssid = str;
        setValue();
    }

    public String getMEnterWifiBssid() {
        return this.mEnterWifiBssid;
    }

    public void setMEnterWifiBssid(String str) {
        this.mEnterWifiBssid = str;
        setValue();
    }

    public String getMWifiChannel() {
        return this.mWifiChannel;
    }

    public void setMWifiChannel(String str) {
        this.mWifiChannel = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mRuleId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mRuleId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mFenceID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mFenceID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mOutFenceID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mOutFenceID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBlocksID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBlocksID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCategory != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCategory);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSubCategory != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSubCategory);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mInoutdoor != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mInoutdoor.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mFloor != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mFloor);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTeleoperators != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mTeleoperators.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mNearCellid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mNearCellid);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEnterCellid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEnterCellid);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mShape != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mShape.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEnterLocation != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEnterLocation);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mNearLocation != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mNearLocation);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCenter != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCenter);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCityCode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mCityCode.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mImportance != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mImportance.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLastUpdated != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mLastUpdated);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mStatus.shortValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReserved1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReserved2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLastLeftTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mLastLeftTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEnteredNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mEnteredNum.shortValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mDisabled != null) {
            parcel.writeByte((byte) 1);
            parcel.writeCharArray(new char[]{this.mDisabled.charValue()});
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mNearWifiBssid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mNearWifiBssid);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEnterWifiBssid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEnterWifiBssid);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mWifiChannel != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mWifiChannel);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<GeoFence> getHelper() {
        return GeoFenceHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "GeoFence { mRuleId: " + this.mRuleId + ", mFenceID: " + this.mFenceID + ", mOutFenceID: " + this.mOutFenceID + ", mBlocksID: " + this.mBlocksID + ", mCategory: " + this.mCategory + ", mSubCategory: " + this.mSubCategory + ", mName: " + this.mName + ", mInoutdoor: " + this.mInoutdoor + ", mFloor: " + this.mFloor + ", mTeleoperators: " + this.mTeleoperators + ", mNearCellid: " + this.mNearCellid + ", mEnterCellid: " + this.mEnterCellid + ", mShape: " + this.mShape + ", mEnterLocation: " + this.mEnterLocation + ", mNearLocation: " + this.mNearLocation + ", mCenter: " + this.mCenter + ", mCityCode: " + this.mCityCode + ", mImportance: " + this.mImportance + ", mLastUpdated: " + this.mLastUpdated + ", mStatus: " + this.mStatus + ", mReserved1: " + this.mReserved1 + ", mReserved2: " + this.mReserved2 + ", mLastLeftTime: " + this.mLastLeftTime + ", mEnteredNum: " + this.mEnteredNum + ", mDisabled: " + this.mDisabled + ", mNearWifiBssid: " + this.mNearWifiBssid + ", mEnterWifiBssid: " + this.mEnterWifiBssid + ", mWifiChannel: " + this.mWifiChannel + " }";
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
