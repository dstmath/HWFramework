package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSContactsInfo extends AManagedObject {
    public static final Parcelable.Creator<DSContactsInfo> CREATOR = new Parcelable.Creator<DSContactsInfo>() {
        public DSContactsInfo createFromParcel(Parcel in) {
            return new DSContactsInfo(in);
        }

        public DSContactsInfo[] newArray(int size) {
            return new DSContactsInfo[size];
        }
    };
    private Integer callDialNum;
    private Integer callDurationTime;
    private Integer callRecvNum;
    private Integer contactNum;
    private Integer id;
    private Integer mReservedInt;
    private String mReservedText;
    private Long mTimeStamp = 0L;

    public DSContactsInfo(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.contactNum = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.callDialNum = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.callRecvNum = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.callDurationTime = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mReservedInt = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReservedText = cursor.getString(7);
        this.mTimeStamp = !cursor.isNull(8) ? Long.valueOf(cursor.getLong(8)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DSContactsInfo(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.contactNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.callDialNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.callRecvNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.callDurationTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() == 0 ? null : in.readString();
        this.mTimeStamp = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private DSContactsInfo(Integer id2, Integer contactNum2, Integer callDialNum2, Integer callRecvNum2, Integer callDurationTime2, Integer mReservedInt2, String mReservedText2, Long mTimeStamp2) {
        this.id = id2;
        this.contactNum = contactNum2;
        this.callDialNum = callDialNum2;
        this.callRecvNum = callRecvNum2;
        this.callDurationTime = callDurationTime2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
        this.mTimeStamp = mTimeStamp2;
    }

    public DSContactsInfo() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public Integer getContactNum() {
        return this.contactNum;
    }

    public void setContactNum(Integer contactNum2) {
        this.contactNum = contactNum2;
        setValue();
    }

    public Integer getCallDialNum() {
        return this.callDialNum;
    }

    public void setCallDialNum(Integer callDialNum2) {
        this.callDialNum = callDialNum2;
        setValue();
    }

    public Integer getCallRecvNum() {
        return this.callRecvNum;
    }

    public void setCallRecvNum(Integer callRecvNum2) {
        this.callRecvNum = callRecvNum2;
        setValue();
    }

    public Integer getCallDurationTime() {
        return this.callDurationTime;
    }

    public void setCallDurationTime(Integer callDurationTime2) {
        this.callDurationTime = callDurationTime2;
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

    public Long getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Long mTimeStamp2) {
        this.mTimeStamp = mTimeStamp2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.contactNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.contactNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.callDialNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.callDialNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.callRecvNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.callRecvNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.callDurationTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.callDurationTime.intValue());
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
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<DSContactsInfo> getHelper() {
        return DSContactsInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.DSContactsInfo";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DSContactsInfo { id: ").append(this.id);
        sb.append(", contactNum: ").append(this.contactNum);
        sb.append(", callDialNum: ").append(this.callDialNum);
        sb.append(", callRecvNum: ").append(this.callRecvNum);
        sb.append(", callDurationTime: ").append(this.callDurationTime);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
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
