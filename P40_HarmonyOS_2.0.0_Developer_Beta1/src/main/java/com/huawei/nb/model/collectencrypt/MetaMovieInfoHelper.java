package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaMovieInfoHelper extends AEntityHelper<MetaMovieInfo> {
    private static final MetaMovieInfoHelper INSTANCE = new MetaMovieInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaMovieInfo metaMovieInfo) {
        return null;
    }

    private MetaMovieInfoHelper() {
    }

    public static MetaMovieInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaMovieInfo metaMovieInfo) {
        Integer mId = metaMovieInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaMovieInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mMovieName = metaMovieInfo.getMMovieName();
        if (mMovieName != null) {
            statement.bindString(3, mMovieName);
        } else {
            statement.bindNull(3);
        }
        Date mMovideStartTime = metaMovieInfo.getMMovideStartTime();
        if (mMovideStartTime != null) {
            statement.bindLong(4, mMovideStartTime.getTime());
        } else {
            statement.bindNull(4);
        }
        String mCinemaAddr = metaMovieInfo.getMCinemaAddr();
        if (mCinemaAddr != null) {
            statement.bindString(5, mCinemaAddr);
        } else {
            statement.bindNull(5);
        }
        Integer mReservedInt = metaMovieInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(6, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(6);
        }
        String mReservedText = metaMovieInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(7, mReservedText);
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaMovieInfo readObject(Cursor cursor, int i) {
        return new MetaMovieInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaMovieInfo metaMovieInfo, long j) {
        metaMovieInfo.setMId(Integer.valueOf((int) j));
    }
}
