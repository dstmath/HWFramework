package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaAppProbeHelper extends AEntityHelper<MetaAppProbe> {
    private static final MetaAppProbeHelper INSTANCE = new MetaAppProbeHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaAppProbe metaAppProbe) {
        return null;
    }

    private MetaAppProbeHelper() {
    }

    public static MetaAppProbeHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaAppProbe metaAppProbe) {
        Integer mId = metaAppProbe.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaAppProbe.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mEventID = metaAppProbe.getMEventID();
        if (mEventID != null) {
            statement.bindLong(3, (long) mEventID.intValue());
        } else {
            statement.bindNull(3);
        }
        String mPackageName = metaAppProbe.getMPackageName();
        if (mPackageName != null) {
            statement.bindString(4, mPackageName);
        } else {
            statement.bindNull(4);
        }
        String mContent = metaAppProbe.getMContent();
        if (mContent != null) {
            statement.bindString(5, mContent);
        } else {
            statement.bindNull(5);
        }
        String mAppVersion = metaAppProbe.getMAppVersion();
        if (mAppVersion != null) {
            statement.bindString(6, mAppVersion);
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = metaAppProbe.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = metaAppProbe.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaAppProbe readObject(Cursor cursor, int i) {
        return new MetaAppProbe(cursor);
    }

    public void setPrimaryKeyValue(MetaAppProbe metaAppProbe, long j) {
        metaAppProbe.setMId(Integer.valueOf((int) j));
    }
}
