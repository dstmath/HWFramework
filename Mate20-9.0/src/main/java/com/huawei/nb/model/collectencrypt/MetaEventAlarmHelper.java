package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventAlarmHelper extends AEntityHelper<MetaEventAlarm> {
    private static final MetaEventAlarmHelper INSTANCE = new MetaEventAlarmHelper();

    private MetaEventAlarmHelper() {
    }

    public static MetaEventAlarmHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaEventAlarm object) {
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
        Integer mEventID = object.getMEventID();
        if (mEventID != null) {
            statement.bindLong(3, (long) mEventID.intValue());
        } else {
            statement.bindNull(3);
        }
        Date mAlarmTime = object.getMAlarmTime();
        if (mAlarmTime != null) {
            statement.bindLong(4, mAlarmTime.getTime());
        } else {
            statement.bindNull(4);
        }
        String mEventInfo = object.getMEventInfo();
        if (mEventInfo != null) {
            statement.bindString(5, mEventInfo);
        } else {
            statement.bindNull(5);
        }
        String mAddress = object.getMAddress();
        if (mAddress != null) {
            statement.bindString(6, mAddress);
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    public MetaEventAlarm readObject(Cursor cursor, int offset) {
        return new MetaEventAlarm(cursor);
    }

    public void setPrimaryKeyValue(MetaEventAlarm object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaEventAlarm object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
