package com.huawei.odmf.core;

import android.content.ContentValues;
import android.database.Cursor;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.Relationship;

public class ASchema {
    private PersistentStore ps = null;

    public ASchema(PersistentStore persistentStore) {
        if (persistentStore == null) {
            throw new ODMFIllegalArgumentException("The persistentStore used to create a ASchema is null.");
        }
        this.ps = persistentStore;
    }

    public void addEntity(Entity entity) {
        this.ps.createEntityForMigration(entity);
    }

    public void addAttribute(Entity entity, Attribute attribute) {
        this.ps.addColumnForMigration(entity, attribute);
    }

    public void renameEntity(String newName, String oldName) {
        this.ps.renameEntityForMigration(newName, oldName);
    }

    public void dropEntity(String tableName) {
        this.ps.dropEntityForMigration(tableName);
    }

    public void addRelationship(Entity entity, Relationship relationship) {
        this.ps.addRelationshipForMigration(entity, relationship);
    }

    public void insertData(String tableName, ContentValues values) {
        this.ps.insertDataForMigration(tableName, values);
    }

    public Cursor rawQuery(String sql) {
        return this.ps.executeRawQuerySQL(sql);
    }

    public void setNewDBVersion(String newVersion) {
        this.ps.setNewDBVersions(newVersion, 1);
    }

    public void setNewEntityVersion(String entityName, String newVersion) {
        this.ps.setNewEntityVersions(entityName, newVersion, 1);
    }
}
