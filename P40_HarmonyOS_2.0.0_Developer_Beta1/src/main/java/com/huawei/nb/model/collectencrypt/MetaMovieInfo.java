package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaMovieInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaMovieInfo> CREATOR = new Parcelable.Creator<MetaMovieInfo>() {
        /* class com.huawei.nb.model.collectencrypt.MetaMovieInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaMovieInfo createFromParcel(Parcel parcel) {
            return new MetaMovieInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaMovieInfo[] newArray(int i) {
            return new MetaMovieInfo[i];
        }
    };
    private String mCinemaAddr;
    private Integer mId;
    private Date mMovideStartTime;
    private String mMovieName;
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
        return "com.huawei.nb.model.collectencrypt.MetaMovieInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaMovieInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mMovieName = cursor.getString(3);
        this.mMovideStartTime = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mCinemaAddr = cursor.getString(5);
        this.mReservedInt = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
        this.mReservedText = cursor.getString(7);
    }

    public MetaMovieInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mMovieName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMovideStartTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mCinemaAddr = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaMovieInfo(Integer num, Date date, String str, Date date2, String str2, Integer num2, String str3) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mMovieName = str;
        this.mMovideStartTime = date2;
        this.mCinemaAddr = str2;
        this.mReservedInt = num2;
        this.mReservedText = str3;
    }

    public MetaMovieInfo() {
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

    public String getMMovieName() {
        return this.mMovieName;
    }

    public void setMMovieName(String str) {
        this.mMovieName = str;
        setValue();
    }

    public Date getMMovideStartTime() {
        return this.mMovideStartTime;
    }

    public void setMMovideStartTime(Date date) {
        this.mMovideStartTime = date;
        setValue();
    }

    public String getMCinemaAddr() {
        return this.mCinemaAddr;
    }

    public void setMCinemaAddr(String str) {
        this.mCinemaAddr = str;
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
        if (this.mMovieName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMovieName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMovideStartTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mMovideStartTime.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCinemaAddr != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCinemaAddr);
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
    public AEntityHelper<MetaMovieInfo> getHelper() {
        return MetaMovieInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaMovieInfo { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mMovieName: " + this.mMovieName + ", mMovideStartTime: " + this.mMovideStartTime + ", mCinemaAddr: " + this.mCinemaAddr + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
