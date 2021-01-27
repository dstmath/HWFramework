package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaTextInfoHelper extends AEntityHelper<MetaTextInfo> {
    private static final MetaTextInfoHelper INSTANCE = new MetaTextInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaTextInfo metaTextInfo) {
        return null;
    }

    private MetaTextInfoHelper() {
    }

    public static MetaTextInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaTextInfo metaTextInfo) {
        Integer mId = metaTextInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaTextInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mType = metaTextInfo.getMType();
        if (mType != null) {
            statement.bindLong(3, (long) mType.intValue());
        } else {
            statement.bindNull(3);
        }
        String mTitle = metaTextInfo.getMTitle();
        if (mTitle != null) {
            statement.bindString(4, mTitle);
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = metaTextInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = metaTextInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaTextInfo readObject(Cursor cursor, int i) {
        return new MetaTextInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaTextInfo metaTextInfo, long j) {
        metaTextInfo.setMId(Integer.valueOf((int) j));
    }
}
