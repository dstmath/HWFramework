package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RefSMARStatusHelper extends AEntityHelper<RefSMARStatus> {
    private static final RefSMARStatusHelper INSTANCE = new RefSMARStatusHelper();

    private RefSMARStatusHelper() {
    }

    public static RefSMARStatusHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RefSMARStatus object) {
        Integer mSmarId = object.getMSmarId();
        if (mSmarId != null) {
            statement.bindLong(1, (long) mSmarId.intValue());
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
        Integer mMotionType = object.getMMotionType();
        if (mMotionType != null) {
            statement.bindLong(4, (long) mMotionType.intValue());
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

    public RefSMARStatus readObject(Cursor cursor, int offset) {
        return new RefSMARStatus(cursor);
    }

    public void setPrimaryKeyValue(RefSMARStatus object, long value) {
        object.setMSmarId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RefSMARStatus object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
