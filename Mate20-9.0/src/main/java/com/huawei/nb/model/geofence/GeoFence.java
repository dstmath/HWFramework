package com.huawei.nb.model.geofence;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class GeoFence extends AManagedObject {
    public static final Parcelable.Creator<GeoFence> CREATOR = new Parcelable.Creator<GeoFence>() {
        public GeoFence createFromParcel(Parcel in) {
            return new GeoFence(in);
        }

        public GeoFence[] newArray(int size) {
            return new GeoFence[size];
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

    public GeoFence(Cursor cursor) {
        Character ch = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public GeoFence(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mRuleId = null;
            in.readInt();
        } else {
            this.mRuleId = Integer.valueOf(in.readInt());
        }
        this.mFenceID = in.readByte() == 0 ? null : in.readString();
        this.mOutFenceID = in.readByte() == 0 ? null : in.readString();
        this.mBlocksID = in.readByte() == 0 ? null : in.readString();
        this.mCategory = in.readByte() == 0 ? null : in.readString();
        this.mSubCategory = in.readByte() == 0 ? null : in.readString();
        this.mName = in.readByte() == 0 ? null : in.readString();
        this.mInoutdoor = in.readByte() == 0 ? null : Character.valueOf(in.createCharArray()[0]);
        this.mFloor = in.readByte() == 0 ? null : in.readString();
        this.mTeleoperators = in.readByte() == 0 ? null : Character.valueOf(in.createCharArray()[0]);
        this.mNearCellid = in.readByte() == 0 ? null : in.readString();
        this.mEnterCellid = in.readByte() == 0 ? null : in.readString();
        this.mShape = in.readByte() == 0 ? null : Character.valueOf(in.createCharArray()[0]);
        this.mEnterLocation = in.readByte() == 0 ? null : in.readString();
        this.mNearLocation = in.readByte() == 0 ? null : in.readString();
        this.mCenter = in.readByte() == 0 ? null : in.readString();
        this.mCityCode = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mImportance = in.readByte() == 0 ? null : Character.valueOf(in.createCharArray()[0]);
        this.mLastUpdated = in.readByte() == 0 ? null : in.readString();
        this.mStatus = in.readByte() == 0 ? null : Short.valueOf((short) in.readInt());
        this.mReserved1 = in.readByte() == 0 ? null : in.readString();
        this.mReserved2 = in.readByte() == 0 ? null : in.readString();
        this.mLastLeftTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.mEnteredNum = in.readByte() == 0 ? null : Short.valueOf((short) in.readInt());
        this.mDisabled = in.readByte() == 0 ? null : Character.valueOf(in.createCharArray()[0]);
        this.mNearWifiBssid = in.readByte() == 0 ? null : in.readString();
        this.mEnterWifiBssid = in.readByte() == 0 ? null : in.readString();
        this.mWifiChannel = in.readByte() != 0 ? in.readString() : str;
    }

    private GeoFence(Integer mRuleId2, String mFenceID2, String mOutFenceID2, String mBlocksID2, String mCategory2, String mSubCategory2, String mName2, Character mInoutdoor2, String mFloor2, Character mTeleoperators2, String mNearCellid2, String mEnterCellid2, Character mShape2, String mEnterLocation2, String mNearLocation2, String mCenter2, Integer mCityCode2, Character mImportance2, String mLastUpdated2, Short mStatus2, String mReserved12, String mReserved22, Long mLastLeftTime2, Short mEnteredNum2, Character mDisabled2, String mNearWifiBssid2, String mEnterWifiBssid2, String mWifiChannel2) {
        this.mRuleId = mRuleId2;
        this.mFenceID = mFenceID2;
        this.mOutFenceID = mOutFenceID2;
        this.mBlocksID = mBlocksID2;
        this.mCategory = mCategory2;
        this.mSubCategory = mSubCategory2;
        this.mName = mName2;
        this.mInoutdoor = mInoutdoor2;
        this.mFloor = mFloor2;
        this.mTeleoperators = mTeleoperators2;
        this.mNearCellid = mNearCellid2;
        this.mEnterCellid = mEnterCellid2;
        this.mShape = mShape2;
        this.mEnterLocation = mEnterLocation2;
        this.mNearLocation = mNearLocation2;
        this.mCenter = mCenter2;
        this.mCityCode = mCityCode2;
        this.mImportance = mImportance2;
        this.mLastUpdated = mLastUpdated2;
        this.mStatus = mStatus2;
        this.mReserved1 = mReserved12;
        this.mReserved2 = mReserved22;
        this.mLastLeftTime = mLastLeftTime2;
        this.mEnteredNum = mEnteredNum2;
        this.mDisabled = mDisabled2;
        this.mNearWifiBssid = mNearWifiBssid2;
        this.mEnterWifiBssid = mEnterWifiBssid2;
        this.mWifiChannel = mWifiChannel2;
    }

    public GeoFence() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMRuleId() {
        return this.mRuleId;
    }

    public void setMRuleId(Integer mRuleId2) {
        this.mRuleId = mRuleId2;
        setValue();
    }

    public String getMFenceID() {
        return this.mFenceID;
    }

    public void setMFenceID(String mFenceID2) {
        this.mFenceID = mFenceID2;
        setValue();
    }

    public String getMOutFenceID() {
        return this.mOutFenceID;
    }

    public void setMOutFenceID(String mOutFenceID2) {
        this.mOutFenceID = mOutFenceID2;
        setValue();
    }

    public String getMBlocksID() {
        return this.mBlocksID;
    }

    public void setMBlocksID(String mBlocksID2) {
        this.mBlocksID = mBlocksID2;
        setValue();
    }

    public String getMCategory() {
        return this.mCategory;
    }

    public void setMCategory(String mCategory2) {
        this.mCategory = mCategory2;
        setValue();
    }

    public String getMSubCategory() {
        return this.mSubCategory;
    }

    public void setMSubCategory(String mSubCategory2) {
        this.mSubCategory = mSubCategory2;
        setValue();
    }

    public String getMName() {
        return this.mName;
    }

    public void setMName(String mName2) {
        this.mName = mName2;
        setValue();
    }

    public Character getMInoutdoor() {
        return this.mInoutdoor;
    }

    public void setMInoutdoor(Character mInoutdoor2) {
        this.mInoutdoor = mInoutdoor2;
        setValue();
    }

    public String getMFloor() {
        return this.mFloor;
    }

    public void setMFloor(String mFloor2) {
        this.mFloor = mFloor2;
        setValue();
    }

    public Character getMTeleoperators() {
        return this.mTeleoperators;
    }

    public void setMTeleoperators(Character mTeleoperators2) {
        this.mTeleoperators = mTeleoperators2;
        setValue();
    }

    public String getMNearCellid() {
        return this.mNearCellid;
    }

    public void setMNearCellid(String mNearCellid2) {
        this.mNearCellid = mNearCellid2;
        setValue();
    }

    public String getMEnterCellid() {
        return this.mEnterCellid;
    }

    public void setMEnterCellid(String mEnterCellid2) {
        this.mEnterCellid = mEnterCellid2;
        setValue();
    }

    public Character getMShape() {
        return this.mShape;
    }

    public void setMShape(Character mShape2) {
        this.mShape = mShape2;
        setValue();
    }

    public String getMEnterLocation() {
        return this.mEnterLocation;
    }

    public void setMEnterLocation(String mEnterLocation2) {
        this.mEnterLocation = mEnterLocation2;
        setValue();
    }

    public String getMNearLocation() {
        return this.mNearLocation;
    }

    public void setMNearLocation(String mNearLocation2) {
        this.mNearLocation = mNearLocation2;
        setValue();
    }

    public String getMCenter() {
        return this.mCenter;
    }

    public void setMCenter(String mCenter2) {
        this.mCenter = mCenter2;
        setValue();
    }

    public Integer getMCityCode() {
        return this.mCityCode;
    }

    public void setMCityCode(Integer mCityCode2) {
        this.mCityCode = mCityCode2;
        setValue();
    }

    public Character getMImportance() {
        return this.mImportance;
    }

    public void setMImportance(Character mImportance2) {
        this.mImportance = mImportance2;
        setValue();
    }

    public String getMLastUpdated() {
        return this.mLastUpdated;
    }

    public void setMLastUpdated(String mLastUpdated2) {
        this.mLastUpdated = mLastUpdated2;
        setValue();
    }

    public Short getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(Short mStatus2) {
        this.mStatus = mStatus2;
        setValue();
    }

    public String getMReserved1() {
        return this.mReserved1;
    }

    public void setMReserved1(String mReserved12) {
        this.mReserved1 = mReserved12;
        setValue();
    }

    public String getMReserved2() {
        return this.mReserved2;
    }

    public void setMReserved2(String mReserved22) {
        this.mReserved2 = mReserved22;
        setValue();
    }

    public Long getMLastLeftTime() {
        return this.mLastLeftTime;
    }

    public void setMLastLeftTime(Long mLastLeftTime2) {
        this.mLastLeftTime = mLastLeftTime2;
        setValue();
    }

    public Short getMEnteredNum() {
        return this.mEnteredNum;
    }

    public void setMEnteredNum(Short mEnteredNum2) {
        this.mEnteredNum = mEnteredNum2;
        setValue();
    }

    public Character getMDisabled() {
        return this.mDisabled;
    }

    public void setMDisabled(Character mDisabled2) {
        this.mDisabled = mDisabled2;
        setValue();
    }

    public String getMNearWifiBssid() {
        return this.mNearWifiBssid;
    }

    public void setMNearWifiBssid(String mNearWifiBssid2) {
        this.mNearWifiBssid = mNearWifiBssid2;
        setValue();
    }

    public String getMEnterWifiBssid() {
        return this.mEnterWifiBssid;
    }

    public void setMEnterWifiBssid(String mEnterWifiBssid2) {
        this.mEnterWifiBssid = mEnterWifiBssid2;
        setValue();
    }

    public String getMWifiChannel() {
        return this.mWifiChannel;
    }

    public void setMWifiChannel(String mWifiChannel2) {
        this.mWifiChannel = mWifiChannel2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mRuleId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mRuleId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mFenceID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mFenceID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mOutFenceID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mOutFenceID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBlocksID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mBlocksID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCategory != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCategory);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSubCategory != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSubCategory);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mInoutdoor != null) {
            out.writeByte((byte) 1);
            out.writeCharArray(new char[]{this.mInoutdoor.charValue()});
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mFloor != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mFloor);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTeleoperators != null) {
            out.writeByte((byte) 1);
            out.writeCharArray(new char[]{this.mTeleoperators.charValue()});
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNearCellid != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mNearCellid);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEnterCellid != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mEnterCellid);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mShape != null) {
            out.writeByte((byte) 1);
            out.writeCharArray(new char[]{this.mShape.charValue()});
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEnterLocation != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mEnterLocation);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNearLocation != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mNearLocation);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCenter != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCenter);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCityCode != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCityCode.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mImportance != null) {
            out.writeByte((byte) 1);
            out.writeCharArray(new char[]{this.mImportance.charValue()});
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLastUpdated != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mLastUpdated);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mStatus.shortValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReserved1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReserved1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReserved2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReserved2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLastLeftTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mLastLeftTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEnteredNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mEnteredNum.shortValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mDisabled != null) {
            out.writeByte((byte) 1);
            out.writeCharArray(new char[]{this.mDisabled.charValue()});
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNearWifiBssid != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mNearWifiBssid);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEnterWifiBssid != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mEnterWifiBssid);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWifiChannel != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mWifiChannel);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<GeoFence> getHelper() {
        return GeoFenceHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.geofence.GeoFence";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("GeoFence { mRuleId: ").append(this.mRuleId);
        sb.append(", mFenceID: ").append(this.mFenceID);
        sb.append(", mOutFenceID: ").append(this.mOutFenceID);
        sb.append(", mBlocksID: ").append(this.mBlocksID);
        sb.append(", mCategory: ").append(this.mCategory);
        sb.append(", mSubCategory: ").append(this.mSubCategory);
        sb.append(", mName: ").append(this.mName);
        sb.append(", mInoutdoor: ").append(this.mInoutdoor);
        sb.append(", mFloor: ").append(this.mFloor);
        sb.append(", mTeleoperators: ").append(this.mTeleoperators);
        sb.append(", mNearCellid: ").append(this.mNearCellid);
        sb.append(", mEnterCellid: ").append(this.mEnterCellid);
        sb.append(", mShape: ").append(this.mShape);
        sb.append(", mEnterLocation: ").append(this.mEnterLocation);
        sb.append(", mNearLocation: ").append(this.mNearLocation);
        sb.append(", mCenter: ").append(this.mCenter);
        sb.append(", mCityCode: ").append(this.mCityCode);
        sb.append(", mImportance: ").append(this.mImportance);
        sb.append(", mLastUpdated: ").append(this.mLastUpdated);
        sb.append(", mStatus: ").append(this.mStatus);
        sb.append(", mReserved1: ").append(this.mReserved1);
        sb.append(", mReserved2: ").append(this.mReserved2);
        sb.append(", mLastLeftTime: ").append(this.mLastLeftTime);
        sb.append(", mEnteredNum: ").append(this.mEnteredNum);
        sb.append(", mDisabled: ").append(this.mDisabled);
        sb.append(", mNearWifiBssid: ").append(this.mNearWifiBssid);
        sb.append(", mEnterWifiBssid: ").append(this.mEnterWifiBssid);
        sb.append(", mWifiChannel: ").append(this.mWifiChannel);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.3";
    }

    public int getEntityVersionCode() {
        return 3;
    }
}
