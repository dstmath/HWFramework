package com.huawei.odmf.store;

import com.huawei.odmf.core.DatabaseQueryService;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.Relationship;

public class DatabaseTableHelper {
    static final String ODMF_PREFIX = "ODMF";

    private DatabaseTableHelper() {
    }

    public static String getRelationshipColumnName(Entity entity) {
        return "ODMF_" + entity.getTableName() + "_" + DatabaseQueryService.getRowidColumnName();
    }

    public static String getManyToManyMidTableName(Relationship relationship) {
        return relationship.isMajor() ? relationship.getBaseEntity().getTableName() + "_" + relationship.getRelatedEntity().getTableName() + "_" + relationship.getFieldName() : relationship.getRelatedEntity().getTableName() + "_" + relationship.getBaseEntity().getTableName() + "_" + relationship.getInverseRelationship().getFieldName();
    }

    public static String getColumnType(int i) {
        if (!(i == 0 || i == 1 || i == 8 || i == 10 || i == 9 || i == 11 || i == 12 || i == 13 || i == 15 || i == 16 || i == 17 || i == 21)) {
            if (i == 2 || i == 7 || i == 14 || i == 22) {
                return "TEXT";
            }
            if (!(i == 3 || i == 20)) {
                if (i == 4 || i == 18 || i == 5 || i == 19) {
                    return "REAL";
                }
                if (i == 6) {
                    return "BLOB";
                }
                throw new UnsupportedOperationException("Unsupported field type " + i);
            }
        }
        return "INTEGER";
    }
}
