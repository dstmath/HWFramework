package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaAppProbe extends AManagedObject {
    public static final Parcelable.Creator<MetaAppProbe> CREATOR = new Parcelable.Creator<MetaAppProbe>() {
        /* class com.huawei.nb.model.collectencrypt.MetaAppProbe.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaAppProbe createFromParcel(Parcel parcel) {
            return new MetaAppProbe(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaAppProbe[] newArray(int i) {
            return new MetaAppProbe[i];
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
        return "com.huawei.nb.model.collectencrypt.MetaAppProbe";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaAppProbe(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mEventID = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mPackageName = cursor.getString(4);
        this.mContent = cursor.getString(5);
        this.mAppVersion = cursor.getString(6);
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public MetaAppProbe(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mEventID = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mPackageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mContent = parcel.readByte() == 0 ? null : parcel.readString();
        this.mAppVersion = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaAppProbe(Integer num, Date date, Integer num2, String str, String str2, String str3, Integer num3, String str4) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mEventID = num2;
        this.mPackageName = str;
        this.mContent = str2;
        this.mAppVersion = str3;
        this.mReservedInt = num3;
        this.mReservedText = str4;
    }

    public MetaAppProbe() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date date) {
        this.mTimeStamp = date;
        setValue();
    }

    public Integer getMEventID() {
        return this.mEventID;
    }

    public void setMEventID(Integer num) {
        this.mEventID = num;
        setValue();
    }

    public String getMPackageName() {
        return this.mPackageName;
    }

    public void setMPackageName(String str) {
        this.mPackageName = str;
        setValue();
    }

    public String getMContent() {
        return this.mContent;
    }

    public void setMContent(String str) {
        this.mContent = str;
        setValue();
    }

    public String getMAppVersion() {
        return this.mAppVersion;
    }

    public void setMAppVersion(String str) {
        this.mAppVersion = str;
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
        if (this.mTimeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mTimeStamp.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEventID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mEventID.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mPackageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPackageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mContent != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mContent);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAppVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mAppVersion);
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
    public AEntityHelper<MetaAppProbe> getHelper() {
        return MetaAppProbeHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaAppProbe { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mEventID: " + this.mEventID + ", mPackageName: " + this.mPackageName + ", mContent: " + this.mContent + ", mAppVersion: " + this.mAppVersion + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
