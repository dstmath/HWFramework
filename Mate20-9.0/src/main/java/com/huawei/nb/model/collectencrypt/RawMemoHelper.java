package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMemoHelper extends AEntityHelper<RawMemo> {
    private static final RawMemoHelper INSTANCE = new RawMemoHelper();

    private RawMemoHelper() {
    }

    public static RawMemoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawMemo object) {
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
        String mMemoTitle = object.getMMemoTitle();
        if (mMemoTitle != null) {
            statement.bindString(3, mMemoTitle);
        } else {
            statement.bindNull(3);
        }
        String mMemoContent = object.getMMemoContent();
        if (mMemoContent != null) {
            statement.bindString(4, mMemoContent);
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

    public RawMemo readObject(Cursor cursor, int offset) {
        return new RawMemo(cursor);
    }

    public void setPrimaryKeyValue(RawMemo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawMemo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
