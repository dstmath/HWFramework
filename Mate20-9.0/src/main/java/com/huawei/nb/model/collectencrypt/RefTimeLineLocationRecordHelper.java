package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineLocationRecordHelper extends AEntityHelper<RefTimeLineLocationRecord> {
    private static final RefTimeLineLocationRecordHelper INSTANCE = new RefTimeLineLocationRecordHelper();

    private RefTimeLineLocationRecordHelper() {
    }

    public static RefTimeLineLocationRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RefTimeLineLocationRecord object) {
        Integer mRecordId = object.getMRecordId();
        if (mRecordId != null) {
            statement.bindLong(1, (long) mRecordId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mStartTime = object.getMStartTime();
        if (mStartTime != null) {
            statement.bindLong(2, mStartTime.getTime());
        } else {
            statement.bindNull(2);
        }
        Date mEndTime = object.getMEndTime();
        if (mEndTime != null) {
            statement.bindLong(3, mEndTime.getTime());
        } else {
            statement.bindNull(3);
        }
        Integer mClusterLocId = object.getMClusterLocId();
        if (mClusterLocId != null) {
            statement.bindLong(4, (long) mClusterLocId.intValue());
        } else {
            statement.bindNull(4);
        }
        String mReserved0 = object.getMReserved0();
        if (mReserved0 != null) {
            statement.bindString(5, mReserved0);
        } else {
            statement.bindNull(5);
        }
        String mReserved1 = object.getMReserved1();
        if (mReserved1 != null) {
            statement.bindString(6, mReserved1);
        } else {
            statement.bindNull(6);
        }
    }

    public RefTimeLineLocationRecord readObject(Cursor cursor, int offset) {
        return new RefTimeLineLocationRecord(cursor);
    }

    public void setPrimaryKeyValue(RefTimeLineLocationRecord object, long value) {
        object.setMRecordId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RefTimeLineLocationRecord object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
