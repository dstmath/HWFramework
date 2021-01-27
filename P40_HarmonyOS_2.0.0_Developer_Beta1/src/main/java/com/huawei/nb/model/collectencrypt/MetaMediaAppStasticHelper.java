package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaMediaAppStasticHelper extends AEntityHelper<MetaMediaAppStastic> {
    private static final MetaMediaAppStasticHelper INSTANCE = new MetaMediaAppStasticHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaMediaAppStastic metaMediaAppStastic) {
        return null;
    }

    private MetaMediaAppStasticHelper() {
    }

    public static MetaMediaAppStasticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaMediaAppStastic metaMediaAppStastic) {
        Integer mId = metaMediaAppStastic.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaMediaAppStastic.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mPhotoTagInfo = metaMediaAppStastic.getMPhotoTagInfo();
        if (mPhotoTagInfo != null) {
            statement.bindString(3, mPhotoTagInfo);
        } else {
            statement.bindNull(3);
        }
        String mTopCameraMode = metaMediaAppStastic.getMTopCameraMode();
        if (mTopCameraMode != null) {
            statement.bindString(4, mTopCameraMode);
        } else {
            statement.bindNull(4);
        }
        Integer mTourismPhotoNum = metaMediaAppStastic.getMTourismPhotoNum();
        if (mTourismPhotoNum != null) {
            statement.bindLong(5, (long) mTourismPhotoNum.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mFrontPhotoNum = metaMediaAppStastic.getMFrontPhotoNum();
        if (mFrontPhotoNum != null) {
            statement.bindLong(6, (long) mFrontPhotoNum.intValue());
        } else {
            statement.bindNull(6);
        }
        String mAppInstalled = metaMediaAppStastic.getMAppInstalled();
        if (mAppInstalled != null) {
            statement.bindString(7, mAppInstalled);
        } else {
            statement.bindNull(7);
        }
        String mAppUsageTime = metaMediaAppStastic.getMAppUsageTime();
        if (mAppUsageTime != null) {
            statement.bindString(8, mAppUsageTime);
        } else {
            statement.bindNull(8);
        }
        String mMusicGenres = metaMediaAppStastic.getMMusicGenres();
        if (mMusicGenres != null) {
            statement.bindString(9, mMusicGenres);
        } else {
            statement.bindNull(9);
        }
        String mMusicArtist = metaMediaAppStastic.getMMusicArtist();
        if (mMusicArtist != null) {
            statement.bindString(10, mMusicArtist);
        } else {
            statement.bindNull(10);
        }
        String mMusicYear = metaMediaAppStastic.getMMusicYear();
        if (mMusicYear != null) {
            statement.bindString(11, mMusicYear);
        } else {
            statement.bindNull(11);
        }
        Integer mReservedInt = metaMediaAppStastic.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(12, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(12);
        }
        String mReservedText = metaMediaAppStastic.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(13, mReservedText);
        } else {
            statement.bindNull(13);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaMediaAppStastic readObject(Cursor cursor, int i) {
        return new MetaMediaAppStastic(cursor);
    }

    public void setPrimaryKeyValue(MetaMediaAppStastic metaMediaAppStastic, long j) {
        metaMediaAppStastic.setMId(Integer.valueOf((int) j));
    }
}
