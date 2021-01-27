package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessHelper extends AEntityHelper<Business> {
    private static final BusinessHelper INSTANCE = new BusinessHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Business business) {
        return null;
    }

    private BusinessHelper() {
    }

    public static BusinessHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Business business) {
        Long id = business.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = business.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        Integer businessType = business.getBusinessType();
        if (businessType != null) {
            statement.bindLong(3, (long) businessType.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer level = business.getLevel();
        if (level != null) {
            statement.bindLong(4, (long) level.intValue());
        } else {
            statement.bindNull(4);
        }
        String description = business.getDescription();
        if (description != null) {
            statement.bindString(5, description);
        } else {
            statement.bindNull(5);
        }
        Long parentId = business.getParentId();
        if (parentId != null) {
            statement.bindLong(6, parentId.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Business readObject(Cursor cursor, int i) {
        return new Business(cursor);
    }

    public void setPrimaryKeyValue(Business business, long j) {
        business.setId(Long.valueOf(j));
    }
}
