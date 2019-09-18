package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventStatus extends AManagedObject {
    public static final Parcelable.Creator<MetaEventStatus> CREATOR = new Parcelable.Creator<MetaEventStatus>() {
        public MetaEventStatus createFromParcel(Parcel in) {
            return new MetaEventStatus(in);
        }

        public MetaEventStatus[] newArray(int size) {
            return new MetaEventStatus[size];
        }
    };
    private Date mBegin;
    private Date mEnd;
    private String mEventParam;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private String mStatus;
    private String mStatusName;

    public MetaEventStatus(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mStatusName = cursor.getString(2);
        this.mStatus = cursor.getString(3);
        this.mBegin = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mEnd = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mEventParam = cursor.getString(6);
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaEventStatus(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mStatusName = in.readByte() == 0 ? null : in.readString();
        this.mStatus = in.readByte() == 0 ? null : in.readString();
        this.mBegin = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mEnd = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mEventParam = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaEventStatus(Integer mId2, String mStatusName2, String mStatus2, Date mBegin2, Date mEnd2, String mEventParam2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mStatusName = mStatusName2;
        this.mStatus = mStatus2;
        this.mBegin = mBegin2;
        this.mEnd = mEnd2;
        this.mEventParam = mEventParam2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaEventStatus() {
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

    public String getMStatusName() {
        return this.mStatusName;
    }

    public void setMStatusName(String mStatusName2) {
        this.mStatusName = mStatusName2;
        setValue();
    }

    public String getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(String mStatus2) {
        this.mStatus = mStatus2;
        setValue();
    }

    public Date getMBegin() {
        return this.mBegin;
    }

    public void setMBegin(Date mBegin2) {
        this.mBegin = mBegin2;
        setValue();
    }

    public Date getMEnd() {
        return this.mEnd;
    }

    public void setMEnd(Date mEnd2) {
        this.mEnd = mEnd2;
        setValue();
    }

    public String getMEventParam() {
        return this.mEventParam;
    }

    public void setMEventParam(String mEventParam2) {
        this.mEventParam = mEventParam2;
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

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mStatusName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mStatusName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mStatus);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBegin != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mBegin.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEnd != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mEnd.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEventParam != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mEventParam);
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
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<MetaEventStatus> getHelper() {
        return MetaEventStatusHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaEventStatus";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaEventStatus { mId: ").append(this.mId);
        sb.append(", mStatusName: ").append(this.mStatusName);
        sb.append(", mStatus: ").append(this.mStatus);
        sb.append(", mBegin: ").append(this.mBegin);
        sb.append(", mEnd: ").append(this.mEnd);
        sb.append(", mEventParam: ").append(this.mEventParam);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
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
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
