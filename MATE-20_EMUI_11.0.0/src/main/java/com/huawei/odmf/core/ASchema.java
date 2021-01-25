package com.huawei.odmf.core;

import android.content.ContentValues;
import android.database.Cursor;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.Relationship;

public class ASchema {
    private PersistentStore persistentStore = null;

    public ASchema(PersistentStore persistentStore2) {
        if (persistentStore2 != null) {
            this.persistentStore = persistentStore2;
            return;
        }
        throw new ODMFIllegalArgumentException("The persistentStore used to create a ASchema is null.");
    }

    public void addEntity(Entity entity) {
        this.persistentStore.createEntityForMigration(entity);
    }

    public void addAttribute(Entity entity, Attribute attribute) {
        this.persistentStore.addColumnForMigration(entity, attribute);
    }

    public void renameEntity(String str, String str2) {
        this.persistentStore.renameEntityForMigration(str, str2);
    }

    public void dropEntity(String str) {
        this.persistentStore.dropEntityForMigration(str);
    }

    public void addRelationship(Entity entity, Relationship relationship) {
        this.persistentStore.addRelationshipForMigration(entity, relationship);
    }

    public void insertData(String str, ContentValues contentValues) {
        this.persistentStore.insertDataForMigration(str, contentValues);
    }

    public Cursor rawQuery(String str) {
        return this.persistentStore.executeRawQuerySQL(str);
    }

    public void setNewDBVersion(String str) {
        this.persistentStore.setNewDbVersions(str, 1);
    }

    public void setNewEntityVersion(String str, String str2) {
        this.persistentStore.setNewEntityVersions(str, str2, 1);
    }
}
