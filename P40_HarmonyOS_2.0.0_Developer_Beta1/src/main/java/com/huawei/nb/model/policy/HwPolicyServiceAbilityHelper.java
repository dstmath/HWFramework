package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class HwPolicyServiceAbilityHelper extends AEntityHelper<HwPolicyServiceAbility> {
    private static final HwPolicyServiceAbilityHelper INSTANCE = new HwPolicyServiceAbilityHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, HwPolicyServiceAbility hwPolicyServiceAbility) {
        return null;
    }

    private HwPolicyServiceAbilityHelper() {
    }

    public static HwPolicyServiceAbilityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, HwPolicyServiceAbility hwPolicyServiceAbility) {
        Long id = hwPolicyServiceAbility.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = hwPolicyServiceAbility.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String type = hwPolicyServiceAbility.getType();
        if (type != null) {
            statement.bindString(3, type);
        } else {
            statement.bindNull(3);
        }
        Long versionCode = hwPolicyServiceAbility.getVersionCode();
        if (versionCode != null) {
            statement.bindLong(4, versionCode.longValue());
        } else {
            statement.bindNull(4);
        }
        String versionName = hwPolicyServiceAbility.getVersionName();
        if (versionName != null) {
            statement.bindString(5, versionName);
        } else {
            statement.bindNull(5);
        }
        String reserve = hwPolicyServiceAbility.getReserve();
        if (reserve != null) {
            statement.bindString(6, reserve);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public HwPolicyServiceAbility readObject(Cursor cursor, int i) {
        return new HwPolicyServiceAbility(cursor);
    }

    public void setPrimaryKeyValue(HwPolicyServiceAbility hwPolicyServiceAbility, long j) {
        hwPolicyServiceAbility.setId(Long.valueOf(j));
    }
}
