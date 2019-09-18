package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaMovieInfoHelper extends AEntityHelper<MetaMovieInfo> {
    private static final MetaMovieInfoHelper INSTANCE = new MetaMovieInfoHelper();

    private MetaMovieInfoHelper() {
    }

    public static MetaMovieInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaMovieInfo object) {
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
        String mMovieName = object.getMMovieName();
        if (mMovieName != null) {
            statement.bindString(3, mMovieName);
        } else {
            statement.bindNull(3);
        }
        Date mMovideStartTime = object.getMMovideStartTime();
        if (mMovideStartTime != null) {
            statement.bindLong(4, mMovideStartTime.getTime());
        } else {
            statement.bindNull(4);
        }
        String mCinemaAddr = object.getMCinemaAddr();
        if (mCinemaAddr != null) {
            statement.bindString(5, mCinemaAddr);
        } else {
            statement.bindNull(5);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(6, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(6);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(7, mReservedText);
        } else {
            statement.bindNull(7);
        }
    }

    public MetaMovieInfo readObject(Cursor cursor, int offset) {
        return new MetaMovieInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaMovieInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaMovieInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
