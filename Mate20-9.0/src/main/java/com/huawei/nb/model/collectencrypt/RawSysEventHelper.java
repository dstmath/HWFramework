package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSysEventHelper extends AEntityHelper<RawSysEvent> {
    private static final RawSysEventHelper INSTANCE = new RawSysEventHelper();

    private RawSysEventHelper() {
    }

    public static RawSysEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawSysEvent object) {
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
        String mEventName = object.getMEventName();
        if (mEventName != null) {
            statement.bindString(3, mEventName);
        } else {
            statement.bindNull(3);
        }
        String mEventParam = object.getMEventParam();
        if (mEventParam != null) {
            statement.bindString(4, mEventParam);
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

    public RawSysEvent readObject(Cursor cursor, int offset) {
        return new RawSysEvent(cursor);
    }

    public void setPrimaryKeyValue(RawSysEvent object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawSysEvent object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
