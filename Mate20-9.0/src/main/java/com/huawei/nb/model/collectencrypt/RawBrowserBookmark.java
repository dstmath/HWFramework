package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBrowserBookmark extends AManagedObject {
    public static final Parcelable.Creator<RawBrowserBookmark> CREATOR = new Parcelable.Creator<RawBrowserBookmark>() {
        public RawBrowserBookmark createFromParcel(Parcel in) {
            return new RawBrowserBookmark(in);
        }

        public RawBrowserBookmark[] newArray(int size) {
            return new RawBrowserBookmark[size];
        }
    };
    private Date mBookmarkAddTime;
    private String mBookmarkTitle;
    private String mBookmarkUrl;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public RawBrowserBookmark(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mBookmarkTitle = cursor.getString(3);
        this.mBookmarkUrl = cursor.getString(4);
        this.mBookmarkAddTime = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mReservedInt = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
        this.mReservedText = cursor.getString(7);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawBrowserBookmark(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mBookmarkTitle = in.readByte() == 0 ? null : in.readString();
        this.mBookmarkUrl = in.readByte() == 0 ? null : in.readString();
        this.mBookmarkAddTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private RawBrowserBookmark(Integer mId2, Date mTimeStamp2, String mBookmarkTitle2, String mBookmarkUrl2, Date mBookmarkAddTime2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mBookmarkTitle = mBookmarkTitle2;
        this.mBookmarkUrl = mBookmarkUrl2;
        this.mBookmarkAddTime = mBookmarkAddTime2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public RawBrowserBookmark() {
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

    public String getMBookmarkTitle() {
        return this.mBookmarkTitle;
    }

    public void setMBookmarkTitle(String mBookmarkTitle2) {
        this.mBookmarkTitle = mBookmarkTitle2;
        setValue();
    }

    public String getMBookmarkUrl() {
        return this.mBookmarkUrl;
    }

    public void setMBookmarkUrl(String mBookmarkUrl2) {
        this.mBookmarkUrl = mBookmarkUrl2;
        setValue();
    }

    public Date getMBookmarkAddTime() {
        return this.mBookmarkAddTime;
    }

    public void setMBookmarkAddTime(Date mBookmarkAddTime2) {
        this.mBookmarkAddTime = mBookmarkAddTime2;
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
        if (this.mBookmarkTitle != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mBookmarkTitle);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBookmarkUrl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mBookmarkUrl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mBookmarkAddTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mBookmarkAddTime.getTime());
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

    public AEntityHelper<RawBrowserBookmark> getHelper() {
        return RawBrowserBookmarkHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawBrowserBookmark";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawBrowserBookmark { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mBookmarkTitle: ").append(this.mBookmarkTitle);
        sb.append(", mBookmarkUrl: ").append(this.mBookmarkUrl);
        sb.append(", mBookmarkAddTime: ").append(this.mBookmarkAddTime);
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
