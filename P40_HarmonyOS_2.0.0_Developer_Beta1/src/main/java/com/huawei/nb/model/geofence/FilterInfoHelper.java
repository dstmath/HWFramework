package com.huawei.nb.model.geofence;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class FilterInfoHelper extends AEntityHelper<FilterInfo> {
    private static final FilterInfoHelper INSTANCE = new FilterInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, FilterInfo filterInfo) {
        return null;
    }

    private FilterInfoHelper() {
    }

    public static FilterInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, FilterInfo filterInfo) {
        Integer mRuleId = filterInfo.getMRuleId();
        if (mRuleId != null) {
            statement.bindLong(1, (long) mRuleId.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer mBlockId = filterInfo.getMBlockId();
        if (mBlockId != null) {
            statement.bindLong(2, (long) mBlockId.intValue());
        } else {
            statement.bindNull(2);
        }
        Integer mType = filterInfo.getMType();
        if (mType != null) {
            statement.bindLong(3, (long) mType.intValue());
        } else {
            statement.bindNull(3);
        }
        Blob mContent = filterInfo.getMContent();
        if (mContent != null) {
            statement.bindBlob(4, BindUtils.bindBlob(mContent));
        } else {
            statement.bindNull(4);
        }
        Long mLastUpdated = filterInfo.getMLastUpdated();
        if (mLastUpdated != null) {
            statement.bindLong(5, mLastUpdated.longValue());
        } else {
            statement.bindNull(5);
        }
        Integer mReserved1 = filterInfo.getMReserved1();
        if (mReserved1 != null) {
            statement.bindLong(6, (long) mReserved1.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mReserved2 = filterInfo.getMReserved2();
        if (mReserved2 != null) {
            statement.bindLong(7, (long) mReserved2.intValue());
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public FilterInfo readObject(Cursor cursor, int i) {
        return new FilterInfo(cursor);
    }

    public void setPrimaryKeyValue(FilterInfo filterInfo, long j) {
        filterInfo.setMRuleId(Integer.valueOf((int) j));
    }
}
