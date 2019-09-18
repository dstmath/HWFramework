package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineClusterLocHelper extends AEntityHelper<RefTimeLineClusterLoc> {
    private static final RefTimeLineClusterLocHelper INSTANCE = new RefTimeLineClusterLocHelper();

    private RefTimeLineClusterLocHelper() {
    }

    public static RefTimeLineClusterLocHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RefTimeLineClusterLoc object) {
        Integer mClusterID = object.getMClusterID();
        if (mClusterID != null) {
            statement.bindLong(1, (long) mClusterID.intValue());
        } else {
            statement.bindNull(1);
        }
        Double mLongitude = object.getMLongitude();
        if (mLongitude != null) {
            statement.bindDouble(2, mLongitude.doubleValue());
        } else {
            statement.bindNull(2);
        }
        Double mLatitude = object.getMLatitude();
        if (mLatitude != null) {
            statement.bindDouble(3, mLatitude.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Integer mRange = object.getMRange();
        if (mRange != null) {
            statement.bindLong(4, (long) mRange.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mDuration = object.getMDuration();
        if (mDuration != null) {
            statement.bindLong(5, (long) mDuration.intValue());
        } else {
            statement.bindNull(5);
        }
        Date mLastVisit = object.getMLastVisit();
        if (mLastVisit != null) {
            statement.bindLong(6, mLastVisit.getTime());
        } else {
            statement.bindNull(6);
        }
        String mReserved0 = object.getMReserved0();
        if (mReserved0 != null) {
            statement.bindString(7, mReserved0);
        } else {
            statement.bindNull(7);
        }
        String mReserved1 = object.getMReserved1();
        if (mReserved1 != null) {
            statement.bindString(8, mReserved1);
        } else {
            statement.bindNull(8);
        }
    }

    public RefTimeLineClusterLoc readObject(Cursor cursor, int offset) {
        return new RefTimeLineClusterLoc(cursor);
    }

    public void setPrimaryKeyValue(RefTimeLineClusterLoc object, long value) {
        object.setMClusterID(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RefTimeLineClusterLoc object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
