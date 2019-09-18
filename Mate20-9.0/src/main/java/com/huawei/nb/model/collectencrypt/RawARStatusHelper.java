package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawARStatusHelper extends AEntityHelper<RawARStatus> {
    private static final RawARStatusHelper INSTANCE = new RawARStatusHelper();

    private RawARStatusHelper() {
    }

    public static RawARStatusHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawARStatus object) {
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
        Integer mMotionType = object.getMMotionType();
        if (mMotionType != null) {
            statement.bindLong(3, (long) mMotionType.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mStatus = object.getMStatus();
        if (mStatus != null) {
            statement.bindLong(4, (long) mStatus.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    public RawARStatus readObject(Cursor cursor, int offset) {
        return new RawARStatus(cursor);
    }

    public void setPrimaryKeyValue(RawARStatus object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawARStatus object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
