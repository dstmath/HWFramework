package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DicEventPolicy extends AManagedObject {
    public static final Parcelable.Creator<DicEventPolicy> CREATOR = new Parcelable.Creator<DicEventPolicy>() {
        public DicEventPolicy createFromParcel(Parcel in) {
            return new DicEventPolicy(in);
        }

        public DicEventPolicy[] newArray(int size) {
            return new DicEventPolicy[size];
        }
    };
    private Integer mColdDownTime;
    private String mEventDesc;
    private String mEventName;
    private Integer mEventType;
    private Integer mId;
    private Integer mMaxRecordOneday;
    private Integer mReservedInt;
    private String mReservedText;

    public DicEventPolicy(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mEventType = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.mEventName = cursor.getString(3);
        this.mEventDesc = cursor.getString(4);
        this.mColdDownTime = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mMaxRecordOneday = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DicEventPolicy(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mEventType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mEventName = in.readByte() == 0 ? null : in.readString();
        this.mEventDesc = in.readByte() == 0 ? null : in.readString();
        this.mColdDownTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mMaxRecordOneday = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private DicEventPolicy(Integer mId2, Integer mEventType2, String mEventName2, String mEventDesc2, Integer mColdDownTime2, Integer mMaxRecordOneday2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mEventType = mEventType2;
        this.mEventName = mEventName2;
        this.mEventDesc = mEventDesc2;
        this.mColdDownTime = mColdDownTime2;
        this.mMaxRecordOneday = mMaxRecordOneday2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public DicEventPolicy() {
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

    public Integer getMEventType() {
        return this.mEventType;
    }

    public void setMEventType(Integer mEventType2) {
        this.mEventType = mEventType2;
        setValue();
    }

    public String getMEventName() {
        return this.mEventName;
    }

    public void setMEventName(String mEventName2) {
        this.mEventName = mEventName2;
        setValue();
    }

    public String getMEventDesc() {
        return this.mEventDesc;
    }

    public void setMEventDesc(String mEventDesc2) {
        this.mEventDesc = mEventDesc2;
        setValue();
    }

    public Integer getMColdDownTime() {
        return this.mColdDownTime;
    }

    public void setMColdDownTime(Integer mColdDownTime2) {
        this.mColdDownTime = mColdDownTime2;
        setValue();
    }

    public Integer getMMaxRecordOneday() {
        return this.mMaxRecordOneday;
    }

    public void setMMaxRecordOneday(Integer mMaxRecordOneday2) {
        this.mMaxRecordOneday = mMaxRecordOneday2;
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
        if (this.mEventType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mEventType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEventName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mEventName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mEventDesc != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mEventDesc);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mColdDownTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mColdDownTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMaxRecordOneday != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mMaxRecordOneday.intValue());
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

    public AEntityHelper<DicEventPolicy> getHelper() {
        return DicEventPolicyHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.DicEventPolicy";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DicEventPolicy { mId: ").append(this.mId);
        sb.append(", mEventType: ").append(this.mEventType);
        sb.append(", mEventName: ").append(this.mEventName);
        sb.append(", mEventDesc: ").append(this.mEventDesc);
        sb.append(", mColdDownTime: ").append(this.mColdDownTime);
        sb.append(", mMaxRecordOneday: ").append(this.mMaxRecordOneday);
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
