package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawFgAPPEventHelper extends AEntityHelper<RawFgAPPEvent> {
    private static final RawFgAPPEventHelper INSTANCE = new RawFgAPPEventHelper();

    private RawFgAPPEventHelper() {
    }

    public static RawFgAPPEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawFgAPPEvent object) {
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
        String mPackageName = object.getMPackageName();
        if (mPackageName != null) {
            statement.bindString(3, mPackageName);
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
        String mActivityName = object.getMActivityName();
        if (mActivityName != null) {
            statement.bindString(7, mActivityName);
        } else {
            statement.bindNull(7);
        }
    }

    public RawFgAPPEvent readObject(Cursor cursor, int offset) {
        return new RawFgAPPEvent(cursor);
    }

    public void setPrimaryKeyValue(RawFgAPPEvent object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawFgAPPEvent object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
