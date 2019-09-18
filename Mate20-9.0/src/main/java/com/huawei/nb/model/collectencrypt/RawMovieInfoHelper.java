package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMovieInfoHelper extends AEntityHelper<RawMovieInfo> {
    private static final RawMovieInfoHelper INSTANCE = new RawMovieInfoHelper();

    private RawMovieInfoHelper() {
    }

    public static RawMovieInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawMovieInfo object) {
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
        String mCinemaRoomNo = object.getMCinemaRoomNo();
        if (mCinemaRoomNo != null) {
            statement.bindString(6, mCinemaRoomNo);
        } else {
            statement.bindNull(6);
        }
        String mCinemaSeatNo = object.getMCinemaSeatNo();
        if (mCinemaSeatNo != null) {
            statement.bindString(7, mCinemaSeatNo);
        } else {
            statement.bindNull(7);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(8, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(8);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(9, mReservedText);
        } else {
            statement.bindNull(9);
        }
    }

    public RawMovieInfo readObject(Cursor cursor, int offset) {
        return new RawMovieInfo(cursor);
    }

    public void setPrimaryKeyValue(RawMovieInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawMovieInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
