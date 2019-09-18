package com.huawei.nb.model.geofence;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class FilterInfoHelper extends AEntityHelper<FilterInfo> {
    private static final FilterInfoHelper INSTANCE = new FilterInfoHelper();

    private FilterInfoHelper() {
    }

    public static FilterInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, FilterInfo object) {
        Integer mRuleId = object.getMRuleId();
        if (mRuleId != null) {
            statement.bindLong(1, (long) mRuleId.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer mBlockId = object.getMBlockId();
        if (mBlockId != null) {
            statement.bindLong(2, (long) mBlockId.intValue());
        } else {
            statement.bindNull(2);
        }
        Integer mType = object.getMType();
        if (mType != null) {
            statement.bindLong(3, (long) mType.intValue());
        } else {
            statement.bindNull(3);
        }
        Blob mContent = object.getMContent();
        if (mContent != null) {
            statement.bindBlob(4, BindUtils.bindBlob(mContent));
        } else {
            statement.bindNull(4);
        }
        Long mLastUpdated = object.getMLastUpdated();
        if (mLastUpdated != null) {
            statement.bindLong(5, mLastUpdated.longValue());
        } else {
            statement.bindNull(5);
        }
        Integer mReserved1 = object.getMReserved1();
        if (mReserved1 != null) {
            statement.bindLong(6, (long) mReserved1.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mReserved2 = object.getMReserved2();
        if (mReserved2 != null) {
            statement.bindLong(7, (long) mReserved2.intValue());
        } else {
            statement.bindNull(7);
        }
    }

    public FilterInfo readObject(Cursor cursor, int offset) {
        return new FilterInfo(cursor);
    }

    public void setPrimaryKeyValue(FilterInfo object, long value) {
        object.setMRuleId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, FilterInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
