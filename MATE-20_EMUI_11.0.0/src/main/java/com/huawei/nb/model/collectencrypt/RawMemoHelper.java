package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMemoHelper extends AEntityHelper<RawMemo> {
    private static final RawMemoHelper INSTANCE = new RawMemoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawMemo rawMemo) {
        return null;
    }

    private RawMemoHelper() {
    }

    public static RawMemoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawMemo rawMemo) {
        Integer mId = rawMemo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawMemo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mMemoTitle = rawMemo.getMMemoTitle();
        if (mMemoTitle != null) {
            statement.bindString(3, mMemoTitle);
        } else {
            statement.bindNull(3);
        }
        String mMemoContent = rawMemo.getMMemoContent();
        if (mMemoContent != null) {
            statement.bindString(4, mMemoContent);
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = rawMemo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = rawMemo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawMemo readObject(Cursor cursor, int i) {
        return new RawMemo(cursor);
    }

    public void setPrimaryKeyValue(RawMemo rawMemo, long j) {
        rawMemo.setMId(Integer.valueOf((int) j));
    }
}
