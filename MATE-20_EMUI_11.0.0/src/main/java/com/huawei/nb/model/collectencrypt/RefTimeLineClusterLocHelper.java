package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineClusterLocHelper extends AEntityHelper<RefTimeLineClusterLoc> {
    private static final RefTimeLineClusterLocHelper INSTANCE = new RefTimeLineClusterLocHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RefTimeLineClusterLoc refTimeLineClusterLoc) {
        return null;
    }

    private RefTimeLineClusterLocHelper() {
    }

    public static RefTimeLineClusterLocHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RefTimeLineClusterLoc refTimeLineClusterLoc) {
        Integer mClusterID = refTimeLineClusterLoc.getMClusterID();
        if (mClusterID != null) {
            statement.bindLong(1, (long) mClusterID.intValue());
        } else {
            statement.bindNull(1);
        }
        Double mLongitude = refTimeLineClusterLoc.getMLongitude();
        if (mLongitude != null) {
            statement.bindDouble(2, mLongitude.doubleValue());
        } else {
            statement.bindNull(2);
        }
        Double mLatitude = refTimeLineClusterLoc.getMLatitude();
        if (mLatitude != null) {
            statement.bindDouble(3, mLatitude.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Integer mRange = refTimeLineClusterLoc.getMRange();
        if (mRange != null) {
            statement.bindLong(4, (long) mRange.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mDuration = refTimeLineClusterLoc.getMDuration();
        if (mDuration != null) {
            statement.bindLong(5, (long) mDuration.intValue());
        } else {
            statement.bindNull(5);
        }
        Date mLastVisit = refTimeLineClusterLoc.getMLastVisit();
        if (mLastVisit != null) {
            statement.bindLong(6, mLastVisit.getTime());
        } else {
            statement.bindNull(6);
        }
        String mReserved0 = refTimeLineClusterLoc.getMReserved0();
        if (mReserved0 != null) {
            statement.bindString(7, mReserved0);
        } else {
            statement.bindNull(7);
        }
        String mReserved1 = refTimeLineClusterLoc.getMReserved1();
        if (mReserved1 != null) {
            statement.bindString(8, mReserved1);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RefTimeLineClusterLoc readObject(Cursor cursor, int i) {
        return new RefTimeLineClusterLoc(cursor);
    }

    public void setPrimaryKeyValue(RefTimeLineClusterLoc refTimeLineClusterLoc, long j) {
        refTimeLineClusterLoc.setMClusterID(Integer.valueOf((int) j));
    }
}
