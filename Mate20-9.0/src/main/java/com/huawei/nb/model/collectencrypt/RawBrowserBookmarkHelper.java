package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBrowserBookmarkHelper extends AEntityHelper<RawBrowserBookmark> {
    private static final RawBrowserBookmarkHelper INSTANCE = new RawBrowserBookmarkHelper();

    private RawBrowserBookmarkHelper() {
    }

    public static RawBrowserBookmarkHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawBrowserBookmark object) {
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
        String mBookmarkTitle = object.getMBookmarkTitle();
        if (mBookmarkTitle != null) {
            statement.bindString(3, mBookmarkTitle);
        } else {
            statement.bindNull(3);
        }
        String mBookmarkUrl = object.getMBookmarkUrl();
        if (mBookmarkUrl != null) {
            statement.bindString(4, mBookmarkUrl);
        } else {
            statement.bindNull(4);
        }
        Date mBookmarkAddTime = object.getMBookmarkAddTime();
        if (mBookmarkAddTime != null) {
            statement.bindLong(5, mBookmarkAddTime.getTime());
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
    }

    public RawBrowserBookmark readObject(Cursor cursor, int offset) {
        return new RawBrowserBookmark(cursor);
    }

    public void setPrimaryKeyValue(RawBrowserBookmark object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawBrowserBookmark object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
