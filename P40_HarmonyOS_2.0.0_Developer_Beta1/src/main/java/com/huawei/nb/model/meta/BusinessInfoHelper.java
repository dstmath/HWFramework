package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessInfoHelper extends AEntityHelper<BusinessInfo> {
    private static final BusinessInfoHelper INSTANCE = new BusinessInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, BusinessInfo businessInfo) {
        return null;
    }

    private BusinessInfoHelper() {
    }

    public static BusinessInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, BusinessInfo businessInfo) {
        Integer mId = businessInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mBusinessName = businessInfo.getMBusinessName();
        if (mBusinessName != null) {
            statement.bindString(2, mBusinessName);
        } else {
            statement.bindNull(2);
        }
        String mDescription = businessInfo.getMDescription();
        if (mDescription != null) {
            statement.bindString(3, mDescription);
        } else {
            statement.bindNull(3);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public BusinessInfo readObject(Cursor cursor, int i) {
        return new BusinessInfo(cursor);
    }

    public void setPrimaryKeyValue(BusinessInfo businessInfo, long j) {
        businessInfo.setMId(Integer.valueOf((int) j));
    }
}
