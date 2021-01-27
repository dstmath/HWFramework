package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBrowserBookmark extends AManagedObject {
    public static final Parcelable.Creator<RawBrowserBookmark> CREATOR = new Parcelable.Creator<RawBrowserBookmark>() {
        /* class com.huawei.nb.model.collectencrypt.RawBrowserBookmark.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawBrowserBookmark createFromParcel(Parcel parcel) {
            return new RawBrowserBookmark(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawBrowserBookmark[] newArray(int i) {
            return new RawBrowserBookmark[i];
        }
    };
    private Date mBookmarkAddTime;
    private String mBookmarkTitle;
    private String mBookmarkUrl;
    private Integer mId;
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
        return "com.huawei.nb.model.collectencrypt.RawBrowserBookmark";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RawBrowserBookmark(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mBookmarkTitle = cursor.getString(3);
        this.mBookmarkUrl = cursor.getString(4);
        this.mBookmarkAddTime = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mReservedInt = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
        this.mReservedText = cursor.getString(7);
    }

    public RawBrowserBookmark(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mBookmarkTitle = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBookmarkUrl = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBookmarkAddTime = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private RawBrowserBookmark(Integer num, Date date, String str, String str2, Date date2, Integer num2, String str3) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mBookmarkTitle = str;
        this.mBookmarkUrl = str2;
        this.mBookmarkAddTime = date2;
        this.mReservedInt = num2;
        this.mReservedText = str3;
    }

    public RawBrowserBookmark() {
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

    public String getMBookmarkTitle() {
        return this.mBookmarkTitle;
    }

    public void setMBookmarkTitle(String str) {
        this.mBookmarkTitle = str;
        setValue();
    }

    public String getMBookmarkUrl() {
        return this.mBookmarkUrl;
    }

    public void setMBookmarkUrl(String str) {
        this.mBookmarkUrl = str;
        setValue();
    }

    public Date getMBookmarkAddTime() {
        return this.mBookmarkAddTime;
    }

    public void setMBookmarkAddTime(Date date) {
        this.mBookmarkAddTime = date;
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
        if (this.mBookmarkTitle != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBookmarkTitle);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBookmarkUrl != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBookmarkUrl);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBookmarkAddTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mBookmarkAddTime.getTime());
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
    public AEntityHelper<RawBrowserBookmark> getHelper() {
        return RawBrowserBookmarkHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawBrowserBookmark { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mBookmarkTitle: " + this.mBookmarkTitle + ", mBookmarkUrl: " + this.mBookmarkUrl + ", mBookmarkAddTime: " + this.mBookmarkAddTime + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
