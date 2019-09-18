package com.huawei.odmf.store;

import com.huawei.odmf.core.DatabaseQueryService;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.Relationship;

public class DatabaseTableHelper {
    static final String ODMF_PREFIX = "ODMF";

    public static String getRelationshipColumnName(Entity entity) {
        return "ODMF_" + entity.getTableName() + "_" + DatabaseQueryService.getRowidColumnName();
    }

    public static String getManyToManyMidTableName(Relationship relationship) {
        if (relationship.isMajor()) {
            return relationship.getBaseEntity().getTableName() + "_" + relationship.getRelatedEntity().getTableName() + "_" + relationship.getFieldName();
        }
        return relationship.getRelatedEntity().getTableName() + "_" + relationship.getBaseEntity().getTableName() + "_" + relationship.getInverseRelationship().getFieldName();
    }

    public static String getColumnType(int attributeType) {
        if (attributeType == 0 || attributeType == 1 || attributeType == 8 || attributeType == 10 || attributeType == 9 || attributeType == 11 || attributeType == 12 || attributeType == 13 || attributeType == 15 || attributeType == 16 || attributeType == 17 || attributeType == 21) {
            return "INTEGER";
        }
        if (attributeType == 2 || attributeType == 7 || attributeType == 14 || attributeType == 22) {
            return "TEXT";
        }
        if (attributeType == 3 || attributeType == 20) {
            return "INTEGER";
        }
        if (attributeType == 4 || attributeType == 18) {
            return "REAL";
        }
        if (attributeType == 5 || attributeType == 19) {
            return "REAL";
        }
        if (attributeType == 6) {
            return "BLOB";
        }
        throw new UnsupportedOperationException("Unsupported field type " + attributeType);
    }
}
