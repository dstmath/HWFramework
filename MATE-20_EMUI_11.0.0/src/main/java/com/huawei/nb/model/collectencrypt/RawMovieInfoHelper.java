package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMovieInfoHelper extends AEntityHelper<RawMovieInfo> {
    private static final RawMovieInfoHelper INSTANCE = new RawMovieInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawMovieInfo rawMovieInfo) {
        return null;
    }

    private RawMovieInfoHelper() {
    }

    public static RawMovieInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawMovieInfo rawMovieInfo) {
        Integer mId = rawMovieInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawMovieInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mMovieName = rawMovieInfo.getMMovieName();
        if (mMovieName != null) {
            statement.bindString(3, mMovieName);
        } else {
            statement.bindNull(3);
        }
        Date mMovideStartTime = rawMovieInfo.getMMovideStartTime();
        if (mMovideStartTime != null) {
            statement.bindLong(4, mMovideStartTime.getTime());
        } else {
            statement.bindNull(4);
        }
        String mCinemaAddr = rawMovieInfo.getMCinemaAddr();
        if (mCinemaAddr != null) {
            statement.bindString(5, mCinemaAddr);
        } else {
            statement.bindNull(5);
        }
        String mCinemaRoomNo = rawMovieInfo.getMCinemaRoomNo();
        if (mCinemaRoomNo != null) {
            statement.bindString(6, mCinemaRoomNo);
        } else {
            statement.bindNull(6);
        }
        String mCinemaSeatNo = rawMovieInfo.getMCinemaSeatNo();
        if (mCinemaSeatNo != null) {
            statement.bindString(7, mCinemaSeatNo);
        } else {
            statement.bindNull(7);
        }
        Integer mReservedInt = rawMovieInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(8, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(8);
        }
        String mReservedText = rawMovieInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(9, mReservedText);
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawMovieInfo readObject(Cursor cursor, int i) {
        return new RawMovieInfo(cursor);
    }

    public void setPrimaryKeyValue(RawMovieInfo rawMovieInfo, long j) {
        rawMovieInfo.setMId(Integer.valueOf((int) j));
    }
}
