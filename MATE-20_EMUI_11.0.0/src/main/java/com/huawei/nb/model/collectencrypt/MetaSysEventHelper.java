package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaSysEventHelper extends AEntityHelper<MetaSysEvent> {
    private static final MetaSysEventHelper INSTANCE = new MetaSysEventHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaSysEvent metaSysEvent) {
        return null;
    }

    private MetaSysEventHelper() {
    }

    public static MetaSysEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaSysEvent metaSysEvent) {
        Integer mId = metaSysEvent.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaSysEvent.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mEventName = metaSysEvent.getMEventName();
        if (mEventName != null) {
            statement.bindString(3, mEventName);
        } else {
            statement.bindNull(3);
        }
        String mEventParam = metaSysEvent.getMEventParam();
        if (mEventParam != null) {
            statement.bindString(4, mEventParam);
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = metaSysEvent.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = metaSysEvent.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaSysEvent readObject(Cursor cursor, int i) {
        return new MetaSysEvent(cursor);
    }

    public void setPrimaryKeyValue(MetaSysEvent metaSysEvent, long j) {
        metaSysEvent.setMId(Integer.valueOf((int) j));
    }
}
