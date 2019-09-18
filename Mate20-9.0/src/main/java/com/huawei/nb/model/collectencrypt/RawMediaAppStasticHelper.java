package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMediaAppStasticHelper extends AEntityHelper<RawMediaAppStastic> {
    private static final RawMediaAppStasticHelper INSTANCE = new RawMediaAppStasticHelper();

    private RawMediaAppStasticHelper() {
    }

    public static RawMediaAppStasticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawMediaAppStastic object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = object.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mPhotoTagInfo = object.getMPhotoTagInfo();
        if (mPhotoTagInfo != null) {
            statement.bindString(3, mPhotoTagInfo);
        } else {
            statement.bindNull(3);
        }
        String mTopCameraMode = object.getMTopCameraMode();
        if (mTopCameraMode != null) {
            statement.bindString(4, mTopCameraMode);
        } else {
            statement.bindNull(4);
        }
        Integer mTourismPhotoNum = object.getMTourismPhotoNum();
        if (mTourismPhotoNum != null) {
            statement.bindLong(5, (long) mTourismPhotoNum.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mFrontPhotoNum = object.getMFrontPhotoNum();
        if (mFrontPhotoNum != null) {
            statement.bindLong(6, (long) mFrontPhotoNum.intValue());
        } else {
            statement.bindNull(6);
        }
        String mAppInstalled = object.getMAppInstalled();
        if (mAppInstalled != null) {
            statement.bindString(7, mAppInstalled);
        } else {
            statement.bindNull(7);
        }
        String mAppUsageTime = object.getMAppUsageTime();
        if (mAppUsageTime != null) {
            statement.bindString(8, mAppUsageTime);
        } else {
            statement.bindNull(8);
        }
        String mMusicGenres = object.getMMusicGenres();
        if (mMusicGenres != null) {
            statement.bindString(9, mMusicGenres);
        } else {
            statement.bindNull(9);
        }
        String mMusicArtist = object.getMMusicArtist();
        if (mMusicArtist != null) {
            statement.bindString(10, mMusicArtist);
        } else {
            statement.bindNull(10);
        }
        String mMusicYear = object.getMMusicYear();
        if (mMusicYear != null) {
            statement.bindString(11, mMusicYear);
        } else {
            statement.bindNull(11);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(12, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(12);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(13, mReservedText);
        } else {
            statement.bindNull(13);
        }
        Integer mMusicNum = object.getMMusicNum();
        if (mMusicNum != null) {
            statement.bindLong(14, (long) mMusicNum.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer mVideoNum = object.getMVideoNum();
        if (mVideoNum != null) {
            statement.bindLong(15, (long) mVideoNum.intValue());
        } else {
            statement.bindNull(15);
        }
        Integer mPhotoNum = object.getMPhotoNum();
        if (mPhotoNum != null) {
            statement.bindLong(16, (long) mPhotoNum.intValue());
            return;
        }
        statement.bindNull(16);
    }

    public RawMediaAppStastic readObject(Cursor cursor, int offset) {
        return new RawMediaAppStastic(cursor);
    }

    public void setPrimaryKeyValue(RawMediaAppStastic object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawMediaAppStastic object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
