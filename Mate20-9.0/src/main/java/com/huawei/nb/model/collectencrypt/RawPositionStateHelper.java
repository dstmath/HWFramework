package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawPositionStateHelper extends AEntityHelper<RawPositionState> {
    private static final RawPositionStateHelper INSTANCE = new RawPositionStateHelper();

    private RawPositionStateHelper() {
    }

    public static RawPositionStateHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawPositionState object) {
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
        Integer mStatus = object.getMStatus();
        if (mStatus != null) {
            statement.bindLong(3, (long) mStatus.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(4, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(4);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(5, mReservedText);
        } else {
            statement.bindNull(5);
        }
    }

    public RawPositionState readObject(Cursor cursor, int offset) {
        return new RawPositionState(cursor);
    }

    public void setPrimaryKeyValue(RawPositionState object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawPositionState object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
