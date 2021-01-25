package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMediaAppStastic extends AManagedObject {
    public static final Parcelable.Creator<RawMediaAppStastic> CREATOR = new Parcelable.Creator<RawMediaAppStastic>() {
        /* class com.huawei.nb.model.collectencrypt.RawMediaAppStastic.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawMediaAppStastic createFromParcel(Parcel parcel) {
            return new RawMediaAppStastic(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawMediaAppStastic[] newArray(int i) {
            return new RawMediaAppStastic[i];
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
        return "com.huawei.nb.model.collectencrypt.RawMediaAppStastic";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public RawMediaAppStastic(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
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

    public RawMediaAppStastic(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mTimeStamp = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mPhotoTagInfo = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTopCameraMode = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTourismPhotoNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mFrontPhotoNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mAppInstalled = parcel.readByte() == 0 ? null : parcel.readString();
        this.mAppUsageTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMusicGenres = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMusicArtist = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMusicYear = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() == 0 ? null : parcel.readString();
        this.mMusicNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mVideoNum = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mPhotoNum = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private RawMediaAppStastic(Integer num, Date date, String str, String str2, Integer num2, Integer num3, String str3, String str4, String str5, String str6, String str7, Integer num4, String str8, Integer num5, Integer num6, Integer num7) {
        this.mId = num;
        this.mTimeStamp = date;
        this.mPhotoTagInfo = str;
        this.mTopCameraMode = str2;
        this.mTourismPhotoNum = num2;
        this.mFrontPhotoNum = num3;
        this.mAppInstalled = str3;
        this.mAppUsageTime = str4;
        this.mMusicGenres = str5;
        this.mMusicArtist = str6;
        this.mMusicYear = str7;
        this.mReservedInt = num4;
        this.mReservedText = str8;
        this.mMusicNum = num5;
        this.mVideoNum = num6;
        this.mPhotoNum = num7;
    }

    public RawMediaAppStastic() {
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

    public String getMPhotoTagInfo() {
        return this.mPhotoTagInfo;
    }

    public void setMPhotoTagInfo(String str) {
        this.mPhotoTagInfo = str;
        setValue();
    }

    public String getMTopCameraMode() {
        return this.mTopCameraMode;
    }

    public void setMTopCameraMode(String str) {
        this.mTopCameraMode = str;
        setValue();
    }

    public Integer getMTourismPhotoNum() {
        return this.mTourismPhotoNum;
    }

    public void setMTourismPhotoNum(Integer num) {
        this.mTourismPhotoNum = num;
        setValue();
    }

    public Integer getMFrontPhotoNum() {
        return this.mFrontPhotoNum;
    }

    public void setMFrontPhotoNum(Integer num) {
        this.mFrontPhotoNum = num;
        setValue();
    }

    public String getMAppInstalled() {
        return this.mAppInstalled;
    }

    public void setMAppInstalled(String str) {
        this.mAppInstalled = str;
        setValue();
    }

    public String getMAppUsageTime() {
        return this.mAppUsageTime;
    }

    public void setMAppUsageTime(String str) {
        this.mAppUsageTime = str;
        setValue();
    }

    public String getMMusicGenres() {
        return this.mMusicGenres;
    }

    public void setMMusicGenres(String str) {
        this.mMusicGenres = str;
        setValue();
    }

    public String getMMusicArtist() {
        return this.mMusicArtist;
    }

    public void setMMusicArtist(String str) {
        this.mMusicArtist = str;
        setValue();
    }

    public String getMMusicYear() {
        return this.mMusicYear;
    }

    public void setMMusicYear(String str) {
        this.mMusicYear = str;
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

    public Integer getMMusicNum() {
        return this.mMusicNum;
    }

    public void setMMusicNum(Integer num) {
        this.mMusicNum = num;
        setValue();
    }

    public Integer getMVideoNum() {
        return this.mVideoNum;
    }

    public void setMVideoNum(Integer num) {
        this.mVideoNum = num;
        setValue();
    }

    public Integer getMPhotoNum() {
        return this.mPhotoNum;
    }

    public void setMPhotoNum(Integer num) {
        this.mPhotoNum = num;
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
        if (this.mPhotoTagInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPhotoTagInfo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTopCameraMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTopCameraMode);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTourismPhotoNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mTourismPhotoNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mFrontPhotoNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mFrontPhotoNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAppInstalled != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mAppInstalled);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAppUsageTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mAppUsageTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMusicGenres != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMusicGenres);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMusicArtist != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMusicArtist);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMusicYear != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mMusicYear);
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
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mMusicNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mMusicNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mVideoNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mVideoNum.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mPhotoNum != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mPhotoNum.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RawMediaAppStastic> getHelper() {
        return RawMediaAppStasticHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RawMediaAppStastic { mId: " + this.mId + ", mTimeStamp: " + this.mTimeStamp + ", mPhotoTagInfo: " + this.mPhotoTagInfo + ", mTopCameraMode: " + this.mTopCameraMode + ", mTourismPhotoNum: " + this.mTourismPhotoNum + ", mFrontPhotoNum: " + this.mFrontPhotoNum + ", mAppInstalled: " + this.mAppInstalled + ", mAppUsageTime: " + this.mAppUsageTime + ", mMusicGenres: " + this.mMusicGenres + ", mMusicArtist: " + this.mMusicArtist + ", mMusicYear: " + this.mMusicYear + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + ", mMusicNum: " + this.mMusicNum + ", mVideoNum: " + this.mVideoNum + ", mPhotoNum: " + this.mPhotoNum + " }";
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
