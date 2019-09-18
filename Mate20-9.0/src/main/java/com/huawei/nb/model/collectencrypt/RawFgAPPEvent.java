package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawFgAPPEvent extends AManagedObject {
    public static final Parcelable.Creator<RawFgAPPEvent> CREATOR = new Parcelable.Creator<RawFgAPPEvent>() {
        public RawFgAPPEvent createFromParcel(Parcel in) {
            return new RawFgAPPEvent(in);
        }

        public RawFgAPPEvent[] newArray(int size) {
            return new RawFgAPPEvent[size];
        }
    };
    private String mActivityName;
    private Integer mId;
    private String mPackageName;
    private Integer mReservedInt;
    private String mReservedText;
    private Integer mStatus;
    private Date mTimeStamp;

    public RawFgAPPEvent(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mPackageName = cursor.getString(3);
        this.mStatus = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mReservedInt = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.mReservedText = cursor.getString(6);
        this.mActivityName = cursor.getString(7);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawFgAPPEvent(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mPackageName = in.readByte() == 0 ? null : in.readString();
        this.mStatus = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() == 0 ? null : in.readString();
        this.mActivityName = in.readByte() != 0 ? in.readString() : str;
    }

    private RawFgAPPEvent(Integer mId2, Date mTimeStamp2, String mPackageName2, Integer mStatus2, Integer mReservedInt2, String mReservedText2, String mActivityName2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mPackageName = mPackageName2;
        this.mStatus = mStatus2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
        this.mActivityName = mActivityName2;
    }

    public RawFgAPPEvent() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer mId2) {
        this.mId = mId2;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date mTimeStamp2) {
        this.mTimeStamp = mTimeStamp2;
        setValue();
    }

    public String getMPackageName() {
        return this.mPackageName;
    }

    public void setMPackageName(String mPackageName2) {
        this.mPackageName = mPackageName2;
        setValue();
    }

    public Integer getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(Integer mStatus2) {
        this.mStatus = mStatus2;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer mReservedInt2) {
        this.mReservedInt = mReservedInt2;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String mReservedText2) {
        this.mReservedText = mReservedText2;
        setValue();
    }

    public String getMActivityName() {
        return this.mActivityName;
    }

    public void setMActivityName(String mActivityName2) {
        this.mActivityName = mActivityName2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mPackageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mPackageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mStatus.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReservedInt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReservedText);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mActivityName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mActivityName);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawFgAPPEvent> getHelper() {
        return RawFgAPPEventHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawFgAPPEvent";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawFgAPPEvent { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mPackageName: ").append(this.mPackageName);
        sb.append(", mStatus: ").append(this.mStatus);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
        sb.append(", mActivityName: ").append(this.mActivityName);
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
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
