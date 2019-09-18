package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DicEventPolicyHelper extends AEntityHelper<DicEventPolicy> {
    private static final DicEventPolicyHelper INSTANCE = new DicEventPolicyHelper();

    private DicEventPolicyHelper() {
    }

    public static DicEventPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DicEventPolicy object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer mEventType = object.getMEventType();
        if (mEventType != null) {
            statement.bindLong(2, (long) mEventType.intValue());
        } else {
            statement.bindNull(2);
        }
        String mEventName = object.getMEventName();
        if (mEventName != null) {
            statement.bindString(3, mEventName);
        } else {
            statement.bindNull(3);
        }
        String mEventDesc = object.getMEventDesc();
        if (mEventDesc != null) {
            statement.bindString(4, mEventDesc);
        } else {
            statement.bindNull(4);
        }
        Integer mColdDownTime = object.getMColdDownTime();
        if (mColdDownTime != null) {
            statement.bindLong(5, (long) mColdDownTime.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mMaxRecordOneday = object.getMMaxRecordOneday();
        if (mMaxRecordOneday != null) {
            statement.bindLong(6, (long) mMaxRecordOneday.intValue());
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

    public DicEventPolicy readObject(Cursor cursor, int offset) {
        return new DicEventPolicy(cursor);
    }

    public void setPrimaryKeyValue(DicEventPolicy object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DicEventPolicy object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
