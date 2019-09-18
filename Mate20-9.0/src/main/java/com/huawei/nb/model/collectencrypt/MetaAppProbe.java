package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaAppProbe extends AManagedObject {
    public static final Parcelable.Creator<MetaAppProbe> CREATOR = new Parcelable.Creator<MetaAppProbe>() {
        public MetaAppProbe createFromParcel(Parcel in) {
            return new MetaAppProbe(in);
        }

        public MetaAppProbe[] newArray(int size) {
            return new MetaAppProbe[size];
        }
    };
    private String mAppVersion;
    private String mContent;
    private Integer mEventID;
    private Integer mId;
    private String mPackageName;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public MetaAppProbe(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEventID = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mPackageName = cursor.getString(4);
        this.mContent = cursor.getString(5);
        this.mAppVersion = cursor.getString(6);
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaAppProbe(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mEventID = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mPackageName = in.readByte() == 0 ? null : in.readString();
        this.mContent = in.readByte() == 0 ? null : in.readString();
        this.mAppVersion = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaAppProbe(Integer mId2, Date mTimeStamp2, Integer mEventID2, String mPackageName2, String mContent2, String mAppVersion2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mEventID = mEventID2;
        this.mPackageName = mPackageName2;
        this.mContent = mContent2;
        this.mAppVersion = mAppVersion2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaAppProbe() {
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

    public Integer getMEventID() {
        return this.mEventID;
    }

    public void setMEventID(Integer mEventID2) {
        this.mEventID = mEventID2;
        setValue();
    }

    public String getMPackageName() {
        return this.mPackageName;
    }

    public void setMPackageName(String mPackageName2) {
        this.mPackageName = mPackageName2;
        setValue();
    }

    public String getMContent() {
        return this.mContent;
    }

    public void setMContent(String mContent2) {
        this.mContent = mContent2;
        setValue();
    }

    public String getMAppVersion() {
        return this.mAppVersion;
    }

    public void setMAppVersion(String mAppVersion2) {
        this.mAppVersion = mAppVersion2;
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
        if (this.mEventID != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mEventID.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mPackageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mPackageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mContent != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mContent);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mAppVersion != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mAppVersion);
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

    public AEntityHelper<MetaAppProbe> getHelper() {
        return MetaAppProbeHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaAppProbe";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaAppProbe { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mEventID: ").append(this.mEventID);
        sb.append(", mPackageName: ").append(this.mPackageName);
        sb.append(", mContent: ").append(this.mContent);
        sb.append(", mAppVersion: ").append(this.mAppVersion);
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
