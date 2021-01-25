package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelAuthorityHelper extends AEntityHelper<AiModelAuthority> {
    private static final AiModelAuthorityHelper INSTANCE = new AiModelAuthorityHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AiModelAuthority aiModelAuthority) {
        return null;
    }

    private AiModelAuthorityHelper() {
    }

    public static AiModelAuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelAuthority aiModelAuthority) {
        Long id = aiModelAuthority.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long aimodel_id = aiModelAuthority.getAimodel_id();
        if (aimodel_id != null) {
            statement.bindLong(2, aimodel_id.longValue());
        } else {
            statement.bindNull(2);
        }
        String business_name = aiModelAuthority.getBusiness_name();
        if (business_name != null) {
            statement.bindString(3, business_name);
        } else {
            statement.bindNull(3);
        }
        String business_attribute = aiModelAuthority.getBusiness_attribute();
        if (business_attribute != null) {
            statement.bindString(4, business_attribute);
        } else {
            statement.bindNull(4);
        }
        Integer authority = aiModelAuthority.getAuthority();
        if (authority != null) {
            statement.bindLong(5, (long) authority.intValue());
        } else {
            statement.bindNull(5);
        }
        String reserved_1 = aiModelAuthority.getReserved_1();
        if (reserved_1 != null) {
            statement.bindString(6, reserved_1);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AiModelAuthority readObject(Cursor cursor, int i) {
        return new AiModelAuthority(cursor);
    }

    public void setPrimaryKeyValue(AiModelAuthority aiModelAuthority, long j) {
        aiModelAuthority.setId(Long.valueOf(j));
    }
}
