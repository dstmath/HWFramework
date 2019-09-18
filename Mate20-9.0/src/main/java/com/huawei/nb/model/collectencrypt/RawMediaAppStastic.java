package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMediaAppStastic extends AManagedObject {
    public static final Parcelable.Creator<RawMediaAppStastic> CREATOR = new Parcelable.Creator<RawMediaAppStastic>() {
        public RawMediaAppStastic createFromParcel(Parcel in) {
            return new RawMediaAppStastic(in);
        }

        public RawMediaAppStastic[] newArray(int size) {
            return new RawMediaAppStastic[size];
        }
    };
    private String mAppInstalled;
    private String mAppUsageTime;
    private Integer mFrontPhotoNum;
    private Integer mId;
    private String mMusicArtist;
    private String mMusicGenres;
    private Integer mMusicNum;
    private String mMusicYear;
    private Integer mPhotoNum;
    private String mPhotoTagInfo;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private String mTopCameraMode;
    private Integer mTourismPhotoNum;
    private Integer mVideoNum;

    public RawMediaAppStastic(Cursor cursor) {
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
        this.mReservedInt = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.mReservedText = cursor.getString(13);
        this.mMusicNum = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.mVideoNum = cursor.isNull(15) ? null : Integer.valueOf(cursor.getInt(15));
        this.mPhotoNum = !cursor.isNull(16) ? Integer.valueOf(cursor.getInt(16)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RawMediaAppStastic(Parcel in) {
        super(in);
        Integer num = null;
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
        this.mReservedText = in.readByte() == 0 ? null : in.readString();
        this.mMusicNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mVideoNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mPhotoNum = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private RawMediaAppStastic(Integer mId2, Date mTimeStamp2, String mPhotoTagInfo2, String mTopCameraMode2, Integer mTourismPhotoNum2, Integer mFrontPhotoNum2, String mAppInstalled2, String mAppUsageTime2, String mMusicGenres2, String mMusicArtist2, String mMusicYear2, Integer mReservedInt2, String mReservedText2, Integer mMusicNum2, Integer mVideoNum2, Integer mPhotoNum2) {
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
        this.mMusicNum = mMusicNum2;
        this.mVideoNum = mVideoNum2;
        this.mPhotoNum = mPhotoNum2;
    }

    public RawMediaAppStastic() {
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

    public Integer getMMusicNum() {
        return this.mMusicNum;
    }

    public void setMMusicNum(Integer mMusicNum2) {
        this.mMusicNum = mMusicNum2;
        setValue();
    }

    public Integer getMVideoNum() {
        return this.mVideoNum;
    }

    public void setMVideoNum(Integer mVideoNum2) {
        this.mVideoNum = mVideoNum2;
        setValue();
    }

    public Integer getMPhotoNum() {
        return this.mPhotoNum;
    }

    public void setMPhotoNum(Integer mPhotoNum2) {
        this.mPhotoNum = mPhotoNum2;
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
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMusicNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mMusicNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mVideoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mVideoNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mPhotoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mPhotoNum.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawMediaAppStastic> getHelper() {
        return RawMediaAppStasticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawMediaAppStastic";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawMediaAppStastic { mId: ").append(this.mId);
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
        sb.append(", mMusicNum: ").append(this.mMusicNum);
        sb.append(", mVideoNum: ").append(this.mVideoNum);
        sb.append(", mPhotoNum: ").append(this.mPhotoNum);
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
