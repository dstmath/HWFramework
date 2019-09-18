package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaSysEventHelper extends AEntityHelper<MetaSysEvent> {
    private static final MetaSysEventHelper INSTANCE = new MetaSysEventHelper();

    private MetaSysEventHelper() {
    }

    public static MetaSysEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaSysEvent object) {
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

    public MetaSysEvent readObject(Cursor cursor, int offset) {
        return new MetaSysEvent(cursor);
    }

    public void setPrimaryKeyValue(MetaSysEvent object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaSysEvent object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
