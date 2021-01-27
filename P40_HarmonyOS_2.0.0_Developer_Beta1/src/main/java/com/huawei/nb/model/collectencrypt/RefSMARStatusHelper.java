package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefSMARStatusHelper extends AEntityHelper<RefSMARStatus> {
    private static final RefSMARStatusHelper INSTANCE = new RefSMARStatusHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RefSMARStatus refSMARStatus) {
        return null;
    }

    private RefSMARStatusHelper() {
    }

    public static RefSMARStatusHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RefSMARStatus refSMARStatus) {
        Integer mSmarId = refSMARStatus.getMSmarId();
        if (mSmarId != null) {
            statement.bindLong(1, (long) mSmarId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mStartTime = refSMARStatus.getMStartTime();
        if (mStartTime != null) {
            statement.bindLong(2, mStartTime.getTime());
        } else {
            statement.bindNull(2);
        }
        Date mEndTime = refSMARStatus.getMEndTime();
        if (mEndTime != null) {
            statement.bindLong(3, mEndTime.getTime());
        } else {
            statement.bindNull(3);
        }
        Integer mMotionType = refSMARStatus.getMMotionType();
        if (mMotionType != null) {
            statement.bindLong(4, (long) mMotionType.intValue());
        } else {
            statement.bindNull(4);
        }
        String mReserved0 = refSMARStatus.getMReserved0();
        if (mReserved0 != null) {
            statement.bindString(5, mReserved0);
        } else {
            statement.bindNull(5);
        }
        String mReserved1 = refSMARStatus.getMReserved1();
        if (mReserved1 != null) {
            statement.bindString(6, mReserved1);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RefSMARStatus readObject(Cursor cursor, int i) {
        return new RefSMARStatus(cursor);
    }

    public void setPrimaryKeyValue(RefSMARStatus refSMARStatus, long j) {
        refSMARStatus.setMSmarId(Integer.valueOf((int) j));
    }
}
