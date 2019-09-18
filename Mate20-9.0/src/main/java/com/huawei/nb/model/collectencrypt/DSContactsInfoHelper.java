package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSContactsInfoHelper extends AEntityHelper<DSContactsInfo> {
    private static final DSContactsInfoHelper INSTANCE = new DSContactsInfoHelper();

    private DSContactsInfoHelper() {
    }

    public static DSContactsInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSContactsInfo object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer contactNum = object.getContactNum();
        if (contactNum != null) {
            statement.bindLong(2, (long) contactNum.intValue());
        } else {
            statement.bindNull(2);
        }
        Integer callDialNum = object.getCallDialNum();
        if (callDialNum != null) {
            statement.bindLong(3, (long) callDialNum.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer callRecvNum = object.getCallRecvNum();
        if (callRecvNum != null) {
            statement.bindLong(4, (long) callRecvNum.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer callDurationTime = object.getCallDurationTime();
        if (callDurationTime != null) {
            statement.bindLong(5, (long) callDurationTime.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(6, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(6);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(7, mReservedText);
        } else {
            statement.bindNull(7);
        }
        Long mTimeStamp = object.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(8, mTimeStamp.longValue());
        } else {
            statement.bindNull(8);
        }
    }

    public DSContactsInfo readObject(Cursor cursor, int offset) {
        return new DSContactsInfo(cursor);
    }

    public void setPrimaryKeyValue(DSContactsInfo object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DSContactsInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
