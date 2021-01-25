package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaTextInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaTextInfo> CREATOR = new Parcelable.Creator<MetaTextInfo>() {
        /* class com.huawei.nb.model.collectencrypt.MetaTextInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaTextInfo createFromParcel(Parcel parcel) {
            return new MetaTextInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaTextInfo[] newArray(int i) {
            return new MetaTextInfo[i];
        }
    };
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private String mTitle;
    private Integer mType;

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
        return "com.huawei.nb.model.collectencrypt.MetaTextInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaTextInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mTitle = cursor.getString(4);
        this.mReservedInt = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.mReservedText = cursor.getString(6);
    }

    public MetaTextInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mTitle = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaTextInfo(Integer num, Date date, Integer num2, String str, Integer num3, String str2) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mType = num2;
        this.mTitle = str;
        this.mReservedInt = num3;
        this.mReservedText = str2;
    }

    public MetaTextInfo() {
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

    public Integer getMType() {
        return this.mType;
    }

    public void setMType(Integer num) {
        this.mType = num;
        setValue();
    }

    public String getMTitle() {
        return this.mTitle;
    }

    public void setMTitle(String str) {
        this.mTitle = str;
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
        if (this.mType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTitle != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTitle);
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
    public AEntityHelper<MetaTextInfo> getHelper() {
        return MetaTextInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaTextInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mType: " + this.mType + ", mTitle: " + this.mTitle + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
