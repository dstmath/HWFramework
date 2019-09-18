package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CollectSwitchHelper extends AEntityHelper<CollectSwitch> {
    private static final CollectSwitchHelper INSTANCE = new CollectSwitchHelper();

    private CollectSwitchHelper() {
    }

    public static CollectSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CollectSwitch object) {
        String mDataName = object.getMDataName();
        if (mDataName != null) {
            statement.bindString(1, mDataName);
        } else {
            statement.bindNull(1);
        }
        String mModuleName = object.getMModuleName();
        if (mModuleName != null) {
            statement.bindString(2, mModuleName);
        } else {
            statement.bindNull(2);
        }
        String mTimeText = object.getMTimeText();
        if (mTimeText != null) {
            statement.bindString(3, mTimeText);
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

    public CollectSwitch readObject(Cursor cursor, int offset) {
        return new CollectSwitch(cursor);
    }

    public void setPrimaryKeyValue(CollectSwitch object, long value) {
    }

    public Object getRelationshipObject(String field, CollectSwitch object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
