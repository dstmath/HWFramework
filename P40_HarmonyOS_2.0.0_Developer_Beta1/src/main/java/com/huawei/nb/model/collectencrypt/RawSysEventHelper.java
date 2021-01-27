package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSysEventHelper extends AEntityHelper<RawSysEvent> {
    private static final RawSysEventHelper INSTANCE = new RawSysEventHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawSysEvent rawSysEvent) {
        return null;
    }

    private RawSysEventHelper() {
    }

    public static RawSysEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawSysEvent rawSysEvent) {
        Integer mId = rawSysEvent.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawSysEvent.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mEventName = rawSysEvent.getMEventName();
        if (mEventName != null) {
            statement.bindString(3, mEventName);
        } else {
            statement.bindNull(3);
        }
        String mEventParam = rawSysEvent.getMEventParam();
        if (mEventParam != null) {
            statement.bindString(4, mEventParam);
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = rawSysEvent.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = rawSysEvent.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawSysEvent readObject(Cursor cursor, int i) {
        return new RawSysEvent(cursor);
    }

    public void setPrimaryKeyValue(RawSysEvent rawSysEvent, long j) {
        rawSysEvent.setMId(Integer.valueOf((int) j));
    }
}
