package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefTimeLineLocationRecordHelper extends AEntityHelper<RefTimeLineLocationRecord> {
    private static final RefTimeLineLocationRecordHelper INSTANCE = new RefTimeLineLocationRecordHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RefTimeLineLocationRecord refTimeLineLocationRecord) {
        return null;
    }

    private RefTimeLineLocationRecordHelper() {
    }

    public static RefTimeLineLocationRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RefTimeLineLocationRecord refTimeLineLocationRecord) {
        Integer mRecordId = refTimeLineLocationRecord.getMRecordId();
        if (mRecordId != null) {
            statement.bindLong(1, (long) mRecordId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mStartTime = refTimeLineLocationRecord.getMStartTime();
        if (mStartTime != null) {
            statement.bindLong(2, mStartTime.getTime());
        } else {
            statement.bindNull(2);
        }
        Date mEndTime = refTimeLineLocationRecord.getMEndTime();
        if (mEndTime != null) {
            statement.bindLong(3, mEndTime.getTime());
        } else {
            statement.bindNull(3);
        }
        Integer mClusterLocId = refTimeLineLocationRecord.getMClusterLocId();
        if (mClusterLocId != null) {
            statement.bindLong(4, (long) mClusterLocId.intValue());
        } else {
            statement.bindNull(4);
        }
        String mReserved0 = refTimeLineLocationRecord.getMReserved0();
        if (mReserved0 != null) {
            statement.bindString(5, mReserved0);
        } else {
            statement.bindNull(5);
        }
        String mReserved1 = refTimeLineLocationRecord.getMReserved1();
        if (mReserved1 != null) {
            statement.bindString(6, mReserved1);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RefTimeLineLocationRecord readObject(Cursor cursor, int i) {
        return new RefTimeLineLocationRecord(cursor);
    }

    public void setPrimaryKeyValue(RefTimeLineLocationRecord refTimeLineLocationRecord, long j) {
        refTimeLineLocationRecord.setMRecordId(Integer.valueOf((int) j));
    }
}
