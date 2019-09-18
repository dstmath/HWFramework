package com.huawei.odmf.store;

import android.content.Context;
import com.huawei.hwsqlite.SQLiteDatabase;
import com.huawei.hwsqlite.SQLiteOpenHelper;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.database.ODMFSQLiteDatabase;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.ObjectModel;
import com.huawei.odmf.model.api.Relationship;
import com.huawei.odmf.utils.LOG;

public class ODMFDatabaseHelper extends SQLiteOpenHelper implements DatabaseHelper {
    private final ObjectModel model;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ODMFDatabaseHelper(Context context, String databaseName, ObjectModel model2, boolean throwException, boolean detectDelete) {
        super(context, databaseName, null, model2 != null ? model2.getDatabaseVersionCode() : 1);
        this.model = model2;
        setWriteAheadLoggingEnabled(true);
        if (throwException) {
            setDatabaseOpenFlags(Integer.MIN_VALUE);
        }
        if (detectDelete) {
            setDatabaseOpenFlags(16777216);
        }
    }

    public void onCreate(SQLiteDatabase odmfDb) {
        ODMFSQLiteDatabase odmfSQLiteDatabase = new ODMFSQLiteDatabase(odmfDb);
        DatabaseHelperTools.createMetadataTable(odmfSQLiteDatabase);
        if (this.model != null) {
            for (Entity entity : this.model.getEntities().values()) {
                DatabaseHelperTools.createEntityTable(odmfSQLiteDatabase, entity);
                DatabaseHelperTools.handleManyToManyRelationship(odmfSQLiteDatabase, entity);
            }
        }
        DatabaseHelperTools.generateMetadata(odmfSQLiteDatabase, this.model);
    }

    public void onUpgrade(SQLiteDatabase odmfDb, int oldVersion, int newVersion) {
        LOG.logW("This database may need to be upgraded. Please pay attention.");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOG.logW("This database may need to be downgraded. Please pay attention.");
    }

    public void clearDatabase(DataBase odmfDb) {
        DatabaseHelperTools.dropTables(odmfDb);
        onCreate(odmfDb.getODMFSQLiteDatabase());
    }

    public void addTable(DataBase odmfDb, Entity entity) {
        DatabaseHelperTools.addTable(odmfDb, entity);
    }

    public void dropTable(DataBase odmfDb, String tableName) {
        DatabaseHelperTools.dropTable(odmfDb, tableName);
    }

    public void alterTableName(DataBase odmfDb, String oldEntityName, String newEntityName) {
        DatabaseHelperTools.alterTableName(odmfDb, oldEntityName, newEntityName);
        Entity entity = this.model.getEntity(oldEntityName);
        if (entity != null) {
            DatabaseHelperTools.rebuildMidTable(odmfDb, entity, newEntityName);
            return;
        }
        Entity entity2 = this.model.getEntity(newEntityName);
        if (entity2 != null) {
            DatabaseHelperTools.rebuildMidTable(odmfDb, oldEntityName, entity2);
        }
    }

    public void alterTableAddColumn(DataBase odmfDb, String tableName, Attribute attribute) {
        DatabaseHelperTools.alterTableAddColumn(odmfDb, tableName, attribute.getFieldName(), DatabaseTableHelper.getColumnType(attribute.getType()));
    }

    public void alterTableAddRelationship(DataBase odmfDb, String tableName, Relationship relationship) {
        DatabaseHelperTools.alterTableAddRelationship(odmfDb, tableName, relationship);
    }

    public String getDatabaseVersion(DataBase odmfDb) {
        return DatabaseHelperTools.getDatabaseVersion(odmfDb);
    }

    public void setDatabaseVersions(DataBase odmfDb, String newVersion, int newVersionCode) {
        DatabaseHelperTools.setDatabaseVersions(odmfDb, newVersion, newVersionCode);
    }

    public String getEntityVersion(DataBase odmfDb, String tableName) {
        return DatabaseHelperTools.getEntityVersion(odmfDb, tableName);
    }

    public void setEntityVersions(DataBase odmfDb, String tableName, String newVersion, int newVersionCode) {
        DatabaseHelperTools.setEntityVersions(odmfDb, tableName, newVersion, newVersionCode);
    }

    public int getDatabaseVersionCode(DataBase odmfDb) {
        return DatabaseHelperTools.getDatabaseVersionCode(odmfDb);
    }

    public int getEntityVersionCode(DataBase odmfDb, String tableName) {
        return DatabaseHelperTools.getEntityVersionCode(odmfDb, tableName);
    }

    public void resetMetadata(DataBase db) {
        DatabaseHelperTools.resetMetadata(db, this.model);
    }
}
