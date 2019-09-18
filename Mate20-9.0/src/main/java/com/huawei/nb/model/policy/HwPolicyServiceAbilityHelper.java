package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class HwPolicyServiceAbilityHelper extends AEntityHelper<HwPolicyServiceAbility> {
    private static final HwPolicyServiceAbilityHelper INSTANCE = new HwPolicyServiceAbilityHelper();

    private HwPolicyServiceAbilityHelper() {
    }

    public static HwPolicyServiceAbilityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, HwPolicyServiceAbility object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String type = object.getType();
        if (type != null) {
            statement.bindString(3, type);
        } else {
            statement.bindNull(3);
        }
        Long versionCode = object.getVersionCode();
        if (versionCode != null) {
            statement.bindLong(4, versionCode.longValue());
        } else {
            statement.bindNull(4);
        }
        String versionName = object.getVersionName();
        if (versionName != null) {
            statement.bindString(5, versionName);
        } else {
            statement.bindNull(5);
        }
        String reserve = object.getReserve();
        if (reserve != null) {
            statement.bindString(6, reserve);
        } else {
            statement.bindNull(6);
        }
    }

    public HwPolicyServiceAbility readObject(Cursor cursor, int offset) {
        return new HwPolicyServiceAbility(cursor);
    }

    public void setPrimaryKeyValue(HwPolicyServiceAbility object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, HwPolicyServiceAbility object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
