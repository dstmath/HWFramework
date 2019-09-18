package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMailInfo extends AManagedObject {
    public static final Parcelable.Creator<RawMailInfo> CREATOR = new Parcelable.Creator<RawMailInfo>() {
        public RawMailInfo createFromParcel(Parcel in) {
            return new RawMailInfo(in);
        }

        public RawMailInfo[] newArray(int size) {
            return new RawMailInfo[size];
        }
    };
    private Integer mId;
    private String mMailAddress;
    private String mMailClientName;
    private String mMailContent;
    private String mMailFrom;
    private String mMailSubject;
    private Date mMailTime;
    private String mMailTo;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public RawMailInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mMailClientName = cursor.getString(3);
        this.mMailAddress = cursor.getString(4);
        this.mMailSubject = cursor.getString(5);
        this.mMailContent = cursor.getString(6);
        this.mMailTime = cursor.isNull(7) ? null : new Date(cursor.getLong(7));
        this.mMailFrom = cursor.getString(8);
        this.mMailTo = cursor.getString(9);
        this.mReservedInt = !cursor.isNull(10) ? Integer.valueOf(cursor.getInt(10)) : num;
        this.mReservedText = cursor.getString(11);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawMailInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mMailClientName = in.readByte() == 0 ? null : in.readString();
        this.mMailAddress = in.readByte() == 0 ? null : in.readString();
        this.mMailSubject = in.readByte() == 0 ? null : in.readString();
        this.mMailContent = in.readByte() == 0 ? null : in.readString();
        this.mMailTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mMailFrom = in.readByte() == 0 ? null : in.readString();
        this.mMailTo = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawMailInfo(Integer mId2, Date mTimeStamp2, String mMailClientName2, String mMailAddress2, String mMailSubject2, String mMailContent2, Date mMailTime2, String mMailFrom2, String mMailTo2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mMailClientName = mMailClientName2;
        this.mMailAddress = mMailAddress2;
        this.mMailSubject = mMailSubject2;
        this.mMailContent = mMailContent2;
        this.mMailTime = mMailTime2;
        this.mMailFrom = mMailFrom2;
        this.mMailTo = mMailTo2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawMailInfo() {
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

    public String getMMailClientName() {
        return this.mMailClientName;
    }

    public void setMMailClientName(String mMailClientName2) {
        this.mMailClientName = mMailClientName2;
        setValue();
    }

    public String getMMailAddress() {
        return this.mMailAddress;
    }

    public void setMMailAddress(String mMailAddress2) {
        this.mMailAddress = mMailAddress2;
        setValue();
    }

    public String getMMailSubject() {
        return this.mMailSubject;
    }

    public void setMMailSubject(String mMailSubject2) {
        this.mMailSubject = mMailSubject2;
        setValue();
    }

    public String getMMailContent() {
        return this.mMailContent;
    }

    public void setMMailContent(String mMailContent2) {
        this.mMailContent = mMailContent2;
        setValue();
    }

    public Date getMMailTime() {
        return this.mMailTime;
    }

    public void setMMailTime(Date mMailTime2) {
        this.mMailTime = mMailTime2;
        setValue();
    }

    public String getMMailFrom() {
        return this.mMailFrom;
    }

    public void setMMailFrom(String mMailFrom2) {
        this.mMailFrom = mMailFrom2;
        setValue();
    }

    public String getMMailTo() {
        return this.mMailTo;
    }

    public void setMMailTo(String mMailTo2) {
        this.mMailTo = mMailTo2;
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
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMailClientName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMailClientName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMailAddress != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMailAddress);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMailSubject != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMailSubject);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMailContent != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMailContent);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMailTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mMailTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMailFrom != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMailFrom);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMailTo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMailTo);
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

    public AEntityHelper<RawMailInfo> getHelper() {
        return RawMailInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawMailInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawMailInfo { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mMailClientName: ").append(this.mMailClientName);
        sb.append(", mMailAddress: ").append(this.mMailAddress);
        sb.append(", mMailSubject: ").append(this.mMailSubject);
        sb.append(", mMailContent: ").append(this.mMailContent);
        sb.append(", mMailTime: ").append(this.mMailTime);
        sb.append(", mMailFrom: ").append(this.mMailFrom);
        sb.append(", mMailTo: ").append(this.mMailTo);
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
