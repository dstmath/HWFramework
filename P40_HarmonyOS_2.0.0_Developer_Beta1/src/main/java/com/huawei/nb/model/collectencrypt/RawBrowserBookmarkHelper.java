package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawBrowserBookmarkHelper extends AEntityHelper<RawBrowserBookmark> {
    private static final RawBrowserBookmarkHelper INSTANCE = new RawBrowserBookmarkHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawBrowserBookmark rawBrowserBookmark) {
        return null;
    }

    private RawBrowserBookmarkHelper() {
    }

    public static RawBrowserBookmarkHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawBrowserBookmark rawBrowserBookmark) {
        Integer mId = rawBrowserBookmark.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawBrowserBookmark.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mBookmarkTitle = rawBrowserBookmark.getMBookmarkTitle();
        if (mBookmarkTitle != null) {
            statement.bindString(3, mBookmarkTitle);
        } else {
            statement.bindNull(3);
        }
        String mBookmarkUrl = rawBrowserBookmark.getMBookmarkUrl();
        if (mBookmarkUrl != null) {
            statement.bindString(4, mBookmarkUrl);
        } else {
            statement.bindNull(4);
        }
        Date mBookmarkAddTime = rawBrowserBookmark.getMBookmarkAddTime();
        if (mBookmarkAddTime != null) {
            statement.bindLong(5, mBookmarkAddTime.getTime());
        } else {
            statement.bindNull(5);
        }
        Integer mReservedInt = rawBrowserBookmark.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(6, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(6);
        }
        String mReservedText = rawBrowserBookmark.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(7, mReservedText);
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawBrowserBookmark readObject(Cursor cursor, int i) {
        return new RawBrowserBookmark(cursor);
    }

    public void setPrimaryKeyValue(RawBrowserBookmark rawBrowserBookmark, long j) {
        rawBrowserBookmark.setMId(Integer.valueOf((int) j));
    }
}
