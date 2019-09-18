package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventStatusHelper extends AEntityHelper<MetaEventStatus> {
    private static final MetaEventStatusHelper INSTANCE = new MetaEventStatusHelper();

    private MetaEventStatusHelper() {
    }

    public static MetaEventStatusHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaEventStatus object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mStatusName = object.getMStatusName();
        if (mStatusName != null) {
            statement.bindString(2, mStatusName);
        } else {
            statement.bindNull(2);
        }
        String mStatus = object.getMStatus();
        if (mStatus != null) {
            statement.bindString(3, mStatus);
        } else {
            statement.bindNull(3);
        }
        Date mBegin = object.getMBegin();
        if (mBegin != null) {
            statement.bindLong(4, mBegin.getTime());
        } else {
            statement.bindNull(4);
        }
        Date mEnd = object.getMEnd();
        if (mEnd != null) {
            statement.bindLong(5, mEnd.getTime());
        } else {
            statement.bindNull(5);
        }
        String mEventParam = object.getMEventParam();
        if (mEventParam != null) {
            statement.bindString(6, mEventParam);
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

    public MetaEventStatus readObject(Cursor cursor, int offset) {
        return new MetaEventStatus(cursor);
    }

    public void setPrimaryKeyValue(MetaEventStatus object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaEventStatus object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
