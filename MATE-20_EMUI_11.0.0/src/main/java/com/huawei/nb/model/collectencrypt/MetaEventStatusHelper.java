package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventStatusHelper extends AEntityHelper<MetaEventStatus> {
    private static final MetaEventStatusHelper INSTANCE = new MetaEventStatusHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaEventStatus metaEventStatus) {
        return null;
    }

    private MetaEventStatusHelper() {
    }

    public static MetaEventStatusHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaEventStatus metaEventStatus) {
        Integer mId = metaEventStatus.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mStatusName = metaEventStatus.getMStatusName();
        if (mStatusName != null) {
            statement.bindString(2, mStatusName);
        } else {
            statement.bindNull(2);
        }
        String mStatus = metaEventStatus.getMStatus();
        if (mStatus != null) {
            statement.bindString(3, mStatus);
        } else {
            statement.bindNull(3);
        }
        Date mBegin = metaEventStatus.getMBegin();
        if (mBegin != null) {
            statement.bindLong(4, mBegin.getTime());
        } else {
            statement.bindNull(4);
        }
        Date mEnd = metaEventStatus.getMEnd();
        if (mEnd != null) {
            statement.bindLong(5, mEnd.getTime());
        } else {
            statement.bindNull(5);
        }
        String mEventParam = metaEventStatus.getMEventParam();
        if (mEventParam != null) {
            statement.bindString(6, mEventParam);
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = metaEventStatus.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = metaEventStatus.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaEventStatus readObject(Cursor cursor, int i) {
        return new MetaEventStatus(cursor);
    }

    public void setPrimaryKeyValue(MetaEventStatus metaEventStatus, long j) {
        metaEventStatus.setMId(Integer.valueOf((int) j));
    }
}
