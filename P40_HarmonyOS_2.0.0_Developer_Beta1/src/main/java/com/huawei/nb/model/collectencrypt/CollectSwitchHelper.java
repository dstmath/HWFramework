package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CollectSwitchHelper extends AEntityHelper<CollectSwitch> {
    private static final CollectSwitchHelper INSTANCE = new CollectSwitchHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CollectSwitch collectSwitch) {
        return null;
    }

    public void setPrimaryKeyValue(CollectSwitch collectSwitch, long j) {
    }

    private CollectSwitchHelper() {
    }

    public static CollectSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CollectSwitch collectSwitch) {
        String mDataName = collectSwitch.getMDataName();
        if (mDataName != null) {
            statement.bindString(1, mDataName);
        } else {
            statement.bindNull(1);
        }
        String mModuleName = collectSwitch.getMModuleName();
        if (mModuleName != null) {
            statement.bindString(2, mModuleName);
        } else {
            statement.bindNull(2);
        }
        String mTimeText = collectSwitch.getMTimeText();
        if (mTimeText != null) {
            statement.bindString(3, mTimeText);
        } else {
            statement.bindNull(3);
        }
        Integer mReservedInt = collectSwitch.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(4, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(4);
        }
        String mReservedText = collectSwitch.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(5, mReservedText);
        } else {
            statement.bindNull(5);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CollectSwitch readObject(Cursor cursor, int i) {
        return new CollectSwitch(cursor);
    }
}
