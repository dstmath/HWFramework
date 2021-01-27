package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawARStatusHelper extends AEntityHelper<RawARStatus> {
    private static final RawARStatusHelper INSTANCE = new RawARStatusHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawARStatus rawARStatus) {
        return null;
    }

    private RawARStatusHelper() {
    }

    public static RawARStatusHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawARStatus rawARStatus) {
        Integer mId = rawARStatus.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawARStatus.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mMotionType = rawARStatus.getMMotionType();
        if (mMotionType != null) {
            statement.bindLong(3, (long) mMotionType.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mStatus = rawARStatus.getMStatus();
        if (mStatus != null) {
            statement.bindLong(4, (long) mStatus.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = rawARStatus.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = rawARStatus.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawARStatus readObject(Cursor cursor, int i) {
        return new RawARStatus(cursor);
    }

    public void setPrimaryKeyValue(RawARStatus rawARStatus, long j) {
        rawARStatus.setMId(Integer.valueOf((int) j));
    }
}
