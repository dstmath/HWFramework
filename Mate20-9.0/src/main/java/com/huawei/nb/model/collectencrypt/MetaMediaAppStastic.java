package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaMediaAppStastic extends AManagedObject {
    public static final Parcelable.Creator<MetaMediaAppStastic> CREATOR = new Parcelable.Creator<MetaMediaAppStastic>() {
        public MetaMediaAppStastic createFromParcel(Parcel in) {
            return new MetaMediaAppStastic(in);
        }

        public MetaMediaAppStastic[] newArray(int size) {
            return new MetaMediaAppStastic[size];
        }
    };
    private String mAppInstalled;
    private String mAppUsageTime;
    private Integer mFrontPhotoNum;
    private Integer mId;
    private String mMusicArtist;
    private String mMusicGenres;
    private String mMusicYear;
    private String mPhotoTagInfo;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private String mTopCameraMode;
    private Integer mTourismPhotoNum;

    public MetaMediaAppStastic(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mPhotoTagInfo = cursor.getString(3);
        this.mTopCameraMode = cursor.getString(4);
        this.mTourismPhotoNum = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mFrontPhotoNum = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mAppInstalled = cursor.getString(7);
        this.mAppUsageTime = cursor.getString(8);
        this.mMusicGenres = cursor.getString(9);
        this.mMusicArtist = cursor.getString(10);
        this.mMusicYear = cursor.getString(11);
        this.mReservedInt = !cursor.isNull(12) ? Integer.valueOf(cursor.getInt(12)) : num;
        this.mReservedText = cursor.getString(13);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MetaMediaAppStastic(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == 0 ? null : new Date(in.readLong());
        this.mPhotoTagInfo = in.readByte() == 0 ? null : in.readString();
        this.mTopCameraMode = in.readByte() == 0 ? null : in.readString();
        this.mTourismPhotoNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mFrontPhotoNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mAppInstalled = in.readByte() == 0 ? null : in.readString();
        this.mAppUsageTime = in.readByte() == 0 ? null : in.readString();
        this.mMusicGenres = in.readByte() == 0 ? null : in.readString();
        this.mMusicArtist = in.readByte() == 0 ? null : in.readString();
        this.mMusicYear = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private MetaMediaAppStastic(Integer mId2, Date mTimeStamp2, String mPhotoTagInfo2, String mTopCameraMode2, Integer mTourismPhotoNum2, Integer mFrontPhotoNum2, String mAppInstalled2, String mAppUsageTime2, String mMusicGenres2, String mMusicArtist2, String mMusicYear2, Integer mReservedInt2, String mReservedText2) {
        this.mId = mId2;
        this.mTimeStamp = mTimeStamp2;
        this.mPhotoTagInfo = mPhotoTagInfo2;
        this.mTopCameraMode = mTopCameraMode2;
        this.mTourismPhotoNum = mTourismPhotoNum2;
        this.mFrontPhotoNum = mFrontPhotoNum2;
        this.mAppInstalled = mAppInstalled2;
        this.mAppUsageTime = mAppUsageTime2;
        this.mMusicGenres = mMusicGenres2;
        this.mMusicArtist = mMusicArtist2;
        this.mMusicYear = mMusicYear2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public MetaMediaAppStastic() {
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

    public String getMPhotoTagInfo() {
        return this.mPhotoTagInfo;
    }

    public void setMPhotoTagInfo(String mPhotoTagInfo2) {
        this.mPhotoTagInfo = mPhotoTagInfo2;
        setValue();
    }

    public String getMTopCameraMode() {
        return this.mTopCameraMode;
    }

    public void setMTopCameraMode(String mTopCameraMode2) {
        this.mTopCameraMode = mTopCameraMode2;
        setValue();
    }

    public Integer getMTourismPhotoNum() {
        return this.mTourismPhotoNum;
    }

    public void setMTourismPhotoNum(Integer mTourismPhotoNum2) {
        this.mTourismPhotoNum = mTourismPhotoNum2;
        setValue();
    }

    public Integer getMFrontPhotoNum() {
        return this.mFrontPhotoNum;
    }

    public void setMFrontPhotoNum(Integer mFrontPhotoNum2) {
        this.mFrontPhotoNum = mFrontPhotoNum2;
        setValue();
    }

    public String getMAppInstalled() {
        return this.mAppInstalled;
    }

    public void setMAppInstalled(String mAppInstalled2) {
        this.mAppInstalled = mAppInstalled2;
        setValue();
    }

    public String getMAppUsageTime() {
        return this.mAppUsageTime;
    }

    public void setMAppUsageTime(String mAppUsageTime2) {
        this.mAppUsageTime = mAppUsageTime2;
        setValue();
    }

    public String getMMusicGenres() {
        return this.mMusicGenres;
    }

    public void setMMusicGenres(String mMusicGenres2) {
        this.mMusicGenres = mMusicGenres2;
        setValue();
    }

    public String getMMusicArtist() {
        return this.mMusicArtist;
    }

    public void setMMusicArtist(String mMusicArtist2) {
        this.mMusicArtist = mMusicArtist2;
        setValue();
    }

    public String getMMusicYear() {
        return this.mMusicYear;
    }

    public void setMMusicYear(String mMusicYear2) {
        this.mMusicYear = mMusicYear2;
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
        if (this.mPhotoTagInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mPhotoTagInfo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTopCameraMode != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTopCameraMode);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTourismPhotoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mTourismPhotoNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mFrontPhotoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mFrontPhotoNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mAppInstalled != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mAppInstalled);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mAppUsageTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mAppUsageTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMusicGenres != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMusicGenres);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMusicArtist != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMusicArtist);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMusicYear != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMusicYear);
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

    public AEntityHelper<MetaMediaAppStastic> getHelper() {
        return MetaMediaAppStasticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaMediaAppStastic";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MetaMediaAppStastic { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mPhotoTagInfo: ").append(this.mPhotoTagInfo);
        sb.append(", mTopCameraMode: ").append(this.mTopCameraMode);
        sb.append(", mTourismPhotoNum: ").append(this.mTourismPhotoNum);
        sb.append(", mFrontPhotoNum: ").append(this.mFrontPhotoNum);
        sb.append(", mAppInstalled: ").append(this.mAppInstalled);
        sb.append(", mAppUsageTime: ").append(this.mAppUsageTime);
        sb.append(", mMusicGenres: ").append(this.mMusicGenres);
        sb.append(", mMusicArtist: ").append(this.mMusicArtist);
        sb.append(", mMusicYear: ").append(this.mMusicYear);
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
